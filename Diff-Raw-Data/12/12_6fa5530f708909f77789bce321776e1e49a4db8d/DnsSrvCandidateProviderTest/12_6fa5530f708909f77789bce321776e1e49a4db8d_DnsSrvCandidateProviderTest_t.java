 package org.littleshoot.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.junit.Test;
 
 
 public class DnsSrvCandidateProviderTest
     {
     
     private static final String NSLOOKUP =
         "Server:  home\r\n"+
         "Address:  192.168.40.254\r\n"+
         "Non-authoritative answer:\r\n"+
         "_stun._udp.littleshoot.org      SRV service location:\r\n"+
         " priority       = 0\r\n"+
         " weight         = 0\r\n"+
         " port           = 3478\r\n"+
         " svr hostname   = stun.ekiga.net\r\n"+
         "_stun._udp.littleshoot.org      SRV service location:\r\n"+
         " priority       = 0\r\n"+
         " weight         = 0\r\n"+
         " port           = 3478\r\n"+
         " svr hostname   = stun.ideasip.com\r\n"+
         "_stun._udp.littleshoot.org      SRV service location:\r\n"+
         " priority       = 0\r\n"+
         " weight         = 0\r\n"+
         " port           = 3478\r\n"+
         " svr hostname   = stun01.sipphone.com";
     
     private static final String DIG =
         ";; global options:  printcmd\n"+
         ";; Got answer:\n"+
         ";; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 8911\n"+
         ";; flags: qr rd ra; QUERY: 1, ANSWER: 3, AUTHORITY: 0, ADDITIONAL: 0\n"+
 
         ";; QUESTION SECTION:\n"+
         ";_stun._udp.littleshoot.org.    IN  SRV\n"+
 
         ";; ANSWER SECTION:\n"+
         "_stun._udp.littleshoot.org. 3658 IN SRV 0 0 3478 stun01.sipphone.com.\n"+
         "_stun._udp.littleshoot.org. 3658 IN SRV 0 0 3478 stun.ekiga.net.\n"+
         "_stun._udp.littleshoot.org. 3658 IN SRV 0 0 3478 stun.ideasip.com.\n"+
 
         ";; Query time: 44 msec\n"+
         ";; SERVER: 8.8.8.8#53(8.8.8.8)\n"+
         ";; WHEN: Thu May 27 21:45:43 2010\n"+
         ";; MSG SIZE  rcvd: 153";
 
     private static final Pattern NSLOOKUP_MATCHER = 
         Pattern.compile("\\s+port\\s+=\\s+(\\S+)\\s+svr hostname\\s+=\\s+(\\S+)");
     
     private static final Pattern DIG_MATCHER = 
         Pattern.compile("^svr hostname\\s+=\\s+(\\S+)");
     
     private static final class OsCandidateProvider 
         implements CandidateProvider<InetSocketAddress>
         {
         private final Collection<InetSocketAddress> candidates =
             new LinkedList<InetSocketAddress>();
         private OsCandidateProvider(final String data, final Pattern pattern)
             {
             final Matcher match = pattern.matcher(data);
             while (match.find()) 
                 {
                 final int port = Integer.parseInt(match.group(1));
                 final String host = match.group(2);
                 candidates.add(new InetSocketAddress(host, port));
                 }
             }
     
         public InetSocketAddress getCandidate()
             {
             if (!candidates.isEmpty())
                 {
                 return candidates.iterator().next();
                 }
             return null;
             }
     
         public Collection<InetSocketAddress> getCandidates()
             {
             return candidates;
             }
         }
     
     @Test public void testDnsSrv() throws Exception 
         {
         final DnsSrvCandidateProvider provider = 
             new DnsSrvCandidateProvider("_stun._udp.littleshoot.org");
         final Collection<InetSocketAddress> candidates = provider.getCandidates();
         assertEquals(3, candidates.size());
         assertTrue(candidates.contains(new InetSocketAddress("stun.ekiga.net", 3478)));
         assertTrue(candidates.contains(new InetSocketAddress("stun.ideasip.com", 3478)));
        //assertTrue(candidates.contains(new InetSocketAddress("stun01.sipphone.com", 3478)));
         }
     
     public void testNsLookup() throws Exception 
         {
         final String name = "_stun._udp.littleshoot.org";
         final Pattern digMatcher = 
             Pattern.compile(name+".\\s+\\d+\\s+IN SRV \\d+ \\d+ (\\d+) ([\\w\\.]+)\\.");
         testLookup(DIG, digMatcher);
         }
     
     private void testLookup(final String nslookup, 
         final Pattern nslookupMatcher)
         {
         final CandidateProvider<InetSocketAddress> provider =
             new OsCandidateProvider(nslookup, nslookupMatcher);
         final Collection<InetSocketAddress> candidates = provider.getCandidates();
         assertEquals(3, candidates.size());
         assertTrue(candidates.contains(new InetSocketAddress("stun.ekiga.net", 3478)));
         assertTrue(candidates.contains(new InetSocketAddress("stun.ideasip.com", 3478)));
        //assertTrue(candidates.contains(new InetSocketAddress("stun01.sipphone.com", 3478)));
         }
 
     @Test public void testSrv() throws Exception 
         {
         final String[] args;
         
         if (SystemUtils.IS_OS_WINDOWS)
             {
             args = new String[] {"nslookup", "-type=srv", "_sip._tcp.example.com"};
             }
         else
             {
             args = new String[]{"dig", "SRV", "_stun._udp.littleshoot.org"};
             }
         final Runtime runtime = Runtime.getRuntime();
         final Process process = runtime.exec(args);
         final InputStream is = process.getInputStream();
         //final InputStreamReader isr = new InputStreamReader(is);
         //final BufferedReader br = new BufferedReader(isr);
         //String line;
 
         final String data = IOUtils.toString(is);
         System.out.println(data);
         /*
         while ((line = br.readLine()) != null) 
             {
             System.out.println(line);
             }
             */
         }
     }
