 package com.verisign.cl;
 
 import java.io.*;
 import java.net.SocketTimeoutException;
 import java.util.*;
 
 import org.apache.log4j.BasicConfigurator;
 import org.xbill.DNS.*;
 
 import com.verisign.tat.dnssec.CaptiveValidator;
 import com.verisign.tat.dnssec.SecurityStatus;
 import com.verisign.tat.dnssec.Util;
 
 public class DNSSECValTool {
 
     /**
      * Invoke with java -jar dnssecreconciler.jar server=127.0.0.1 \
      * query_file=queries.txt dnskey_query=net dnskey_query=edu
      */
     private CaptiveValidator validator;
     private SimpleResolver resolver;
 
     private BufferedReader queryStream;
     private PrintStream errorStream;
     private Set<Name> zoneNames;
 
     // Options
     public String server;
     public String query;
     public String queryFile;
     public String dnskeyFile;
     public List<String> dnskeyNames;
     public String errorFile;
     public long count = 0;
 
     DNSSECValTool() {
         validator = new CaptiveValidator();
     }
 
     /**
      * Convert a query line of the form: <qname> <qtype> <flags> to a request
      * message.
      * 
      * @param query_line
      * @return A query message
      * @throws TextParseException
      * @throws NameTooLongException
      */
     private Message queryFromString(String query_line)
             throws TextParseException, NameTooLongException {
 
         String[] tokens = query_line.split("[ \t]+");
 
         Name qname = null;
         int qtype = -1;
         int qclass = -1;
 
         if (tokens.length < 1)
             return null;
         qname = Name.fromString(tokens[0]);
         if (!qname.isAbsolute()) {
             qname = Name.concatenate(qname, Name.root);
         }
 
         for (int i = 1; i < tokens.length; i++) {
             if (tokens[i].startsWith("+")) {
                 // For now, we ignore flags as uninteresting
                 // All queries will get the DO bit anyway
                 continue;
             }
 
             int type = Type.value(tokens[i]);
             if (type > 0) {
                 qtype = type;
                 continue;
             }
             int cls = DClass.value(tokens[i]);
             if (cls > 0) {
                 qclass = cls;
                 continue;
             }
         }
         if (qtype < 0) {
             qtype = Type.A;
         }
         if (qclass < 0) {
             qclass = DClass.IN;
         }
 
         Message query = Message.newQuery(Record.newRecord(qname, qtype, qclass));
 
         return query;
     }
 
     /**
      * Fetch the next query from either the command line or the query file
      * 
      * @return a query Message, or null if the query list is exhausted
      * @throws IOException
      */
     private Message nextQuery() throws IOException {
         if (query != null) {
             Message res = queryFromString(query);
             query = null;
             return res;
         }
 
         if (queryStream == null && queryFile != null) {
             queryStream = new BufferedReader(new FileReader(queryFile));
         }
 
         if (queryStream != null) {
             String line = queryStream.readLine();
 
             if (line == null)
                 return null;
 
             return queryFromString(line);
         }
 
         return null;
 
     }
 
     /**
      * Figure out the correct zone from the query by comparing the qname to the
      * list of trusted DNSKEY owner names.
      * 
      * @param query
      * @return a zone name
      * @throws IOException
      */
     private Name zoneFromQuery(Message query) throws IOException {
 
         if (zoneNames == null) {
             zoneNames = new HashSet<Name>();
             for (String key : validator.listTrustedKeys()) {
                 String[] components = key.split("/");
                 Name keyname = Name.fromString(components[0]);
                 if (!keyname.isAbsolute()) {
                     keyname = Name.concatenate(keyname, Name.root);
                 }
                 zoneNames.add(keyname);
             }
         }
 
         Name qname = query.getQuestion().getName();
         for (Name n : zoneNames) {
             if (qname.subdomain(n)) {
                 return n;
             }
         }
 
         return null;
     }
 
     private Message resolve(Message query) {
 
         try {
             return resolver.send(query);
         } catch (SocketTimeoutException e) {
             System.err.println("Error: timed out querying " + server + " for "
                     + queryToString(query));
         } catch (IOException e) {
             System.err.println("Error: error querying " + server + " for "
                     + queryToString(query) + ":" + e.getMessage());
         }
         return null;
     }
 
     private String queryToString(Message query) {
         if (query == null)
             return null;
         Record question = query.getQuestion();
         return question.getName() + "/" + Type.string(question.getType()) + "/"
                 + DClass.string(question.getDClass());
     }
 
     public void execute() throws IOException {
         // Configure our resolver
         resolver = new SimpleResolver(server);
         resolver.setEDNS(0, 4096, Flags.DO, null);
 
         // Create our DNSSEC error stream
         if (errorFile != null) {
             errorStream = new PrintStream(new FileOutputStream(errorFile, true));
         } else {
             errorStream = System.out;
         }
 
         // Prime the validator
         if (dnskeyFile != null) {
             validator.addTrustedKeysFromFile(dnskeyFile);
         } else {
             for (String name : dnskeyNames) {
                 Message query = queryFromString(name + " DNSKEY");
                 Message response = resolve(query);
                 validator.addTrustedKeysFromResponse(response);
             }
         }
 
         // Log our set of trusted keys
         for (String key : validator.listTrustedKeys()) {
             System.out.println("Trusted Key: " + key);
         }
 
         // Iterate over all queries
         Message query = nextQuery();
         long total = 0;
         long validCount = 0;
         long errorCount = 0;
 
         while (query != null) {
 
             Name zone = zoneFromQuery(query);
             // Skip queries in zones that we don't have keys for
             if (zone == null) {
                 continue;
             }
 
             Message response = resolve(query);
             if (response == null) {
                 continue;
             }
             byte result = validator.validateMessage(response, zone.toString());
 
             switch (result) {
             case SecurityStatus.BOGUS:
             case SecurityStatus.INVALID:
                 errorStream.println("BOGUS Answer:");
                 errorStream.println("Query: " + queryToString(query));
                 errorStream.println("Response:\n" + response);
                 for (String err : validator.getErrorList()) {
                     errorStream.println("Error: " + err);
                 }
                 errorStream.println("");
                 errorCount++;
                 break;
             case SecurityStatus.INSECURE:
             case SecurityStatus.INDETERMINATE:
             case SecurityStatus.UNCHECKED:
                 errorStream.println("Insecure Answer:");
                 errorStream.println("Query: " + queryToString(query));
                 errorStream.println("Response:\n" + response);
                 for (String err : validator.getErrorList()) {
                     errorStream.println("Error: " + err);
                 }
                 errorCount++;
                 break;
             case SecurityStatus.SECURE:
                 validCount++;
                 break;
             }
 
             if (++total % 1000 == 0) {
                 System.out.println("Completed " + total + " queries: "
                         + validCount + " valid, " + errorCount + " errors.");
             }
          
             if (count > 0 && total >= count) {
                 break;
             }
             
             query = nextQuery();
         }
 
         System.out.println("Completed " + total
                 + (total > 1 ? " queries" : " query") +
                 ": " + validCount + " valid, " + errorCount + " errors.");
     }
 
     private static void usage() {
         System.err.println("usage: java -jar dnssecvaltool.jar [..options..]");
         System.err.println("       server:       the DNS server to query.");
         System.err.println("       query:        a name [type [flags]] string.");
         System.err.println("       query_file:   a list of queries, one query per line.");
         System.err.println("       count:        send up to'count' queries, then stop.");
         System.err.println("       dnskey_file:  a file containing DNSKEY RRs to trust.");
         System.err.println("       dnskey_query: query 'server' for DNSKEY at given name to trust, may repeat.");
         System.err.println("       error_file:   write DNSSEC validation failure details to this file.");
     }
 
     public static void main(String[] argv) {
 
         // Set up Log4J to just log to console.
         BasicConfigurator.configure();
 
         DNSSECValTool dr = new DNSSECValTool();
 
         try {
             // Parse the command line options
             for (String arg : argv) {
 
                 if (arg.indexOf('=') < 0) {
                     System.err.println("Unrecognized option: " + arg);
                     usage();
                     System.exit(1);
                 }
 
                 String[] split_arg = arg.split("=", 2);
                 String opt = split_arg[0];
                 String optarg = split_arg[1];
 
                 if (opt.equals("server")) {
                     dr.server = optarg;
                 } else if (opt.equals("query")) {
                     dr.query = optarg;
                 } else if (opt.equals("query_file")) {
                     dr.queryFile = optarg;
                 } else if (opt.equals("count")) {
                     dr.count = Util.parseInt(optarg, 0);
                 } else if (opt.equals("error_file")) {
                     dr.errorFile = optarg;
                 } else if (opt.equals("dnskey_file")) {
                     dr.dnskeyFile = optarg;
                 } else if (opt.equals("dnskey_query")) {
                     if (dr.dnskeyNames == null) {
                         dr.dnskeyNames = new ArrayList<String>();
                     }
                     dr.dnskeyNames.add(optarg);
                 } else {
                     System.err.println("Unrecognized option: " + opt);
                     usage();
                     System.exit(1);
                 }
             }
 
             // Check for minimum usage
             if (dr.server == null) {
                 System.err.println("'server' must be specified");
                 usage();
                 System.exit(1);
             }
             if (dr.query == null && dr.queryFile == null) {
                 System.err.println("Either 'query' or 'query_file' must be specified");
                 usage();
                 System.exit(1);
             }
             if (dr.dnskeyFile == null && dr.dnskeyNames == null) {
                 System.err.println("Either 'dnskey_file' or 'dnskey_query' must be specified");
                 usage();
                 System.exit(1);
             }
 
             // Execute the job
             dr.execute();
 
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 }
