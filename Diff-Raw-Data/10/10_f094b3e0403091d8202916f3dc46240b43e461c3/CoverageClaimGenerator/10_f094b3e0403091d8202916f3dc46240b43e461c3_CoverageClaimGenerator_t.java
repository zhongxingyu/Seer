 /**
  * KDM Analytics Inc - (2012)
  * 
  * @Author Adam Nunn <adam@kdmanalytics.com>
  * @Date Apr 1, 2012
  */
 
 package com.kdmanalytics.toif.ccr;
 
 import generated.CWECoverageClaimType;
 import generated.CWECoverageClaimType.Claims;
 import generated.CWECoverageClaimType.Claims.Claim;
 import generated.CWECoverageClaimType.Claims.Claim.RuleSet;
 import generated.CWECoverageClaimType.Claims.Claim.RuleSet.Rule;
 import generated.CWECoverageClaims;
 import generated.MatchAccuracyType;
 import generated.ObjectFactory;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.openrdf.model.Value;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.sail.nativerdf.NativeStore;
 
 /**
  * 
  * generate the ccr report.
  * 
  * @author adam
  * 
  */
 public class CoverageClaimGenerator
 {
     
     private List<String> generatorNames = new ArrayList<String>();
     
     /**
      * get the output file.
      * 
      * @param args
      *            the command line args
      * @return
      */
     static File getOutputFileFromArguments(String[] args)
     {
         return new File(args[args.length - 1]);
     }
     
     /**
      * get the connection to the repository.
      * 
      * @return
      */
     static RepositoryConnection getRepositoryConnection(Repository rep)
     {
         RepositoryConnection connection = null;
         
         try
         {
             connection = rep.getConnection();
         }
         catch (RepositoryException e)
         {
             System.err.println("Could not obtain repository connection!");
             System.exit(1);
         }
         
         return connection;
     }
     
     /**
      * get the repository from the command line args
      * 
      * @param args
      *            the command line args
      * @return
      */
     static Repository getRepositoryFromArguments(String[] args)
     {
         File dataDir = new File(args[args.length - 2]);
         
         Repository rep = new SailRepository(new NativeStore(dataDir));
         
         try
         {
             rep.initialize();
         }
         catch (RepositoryException e)
         {
             System.err.println("Repository could not be initialized!");
             System.exit(1);
         }
         
         return rep;
     }
     
     /**
      * any vendor-specific rule identifiers, numbers or names that correspond to
      * the associated CWE ID
      * 
      * @param objFactory
      * @param segment
      * @param repository
     * @param segment 
      * @return
      */
    private static Collection<? extends Rule> getRules(ObjectFactory objFactory, Value cwe, Repository repository, Value segment)
     {
         HashMap<String, Rule> ruleSet = new HashMap<String, CWECoverageClaimType.Claims.Claim.RuleSet.Rule>();
         RepositoryConnection con = null;
         try
         {
             con = getRepositoryConnection(repository);
             
            String adaptorQuery = "SELECT ?descriptionText WHERE {<"+segment+"> <http://toif/contains> ?finding. ?finding <http://toif/toif:FindingHasCWEIdentifier> <" + cwe + "> . "
                     + "?finding <http://toif/toif:FindingIsDescribedByWeaknessDescription> ?description . "
                     + "?description <http://toif/description> ?descriptionText . }";
             
             TupleQuery adaptorTupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, adaptorQuery);
             
             TupleQueryResult queryResult = adaptorTupleQuery.evaluate();
             
             while (queryResult.hasNext())
             {
                 BindingSet adaptorSet = queryResult.next();
                 Value name = adaptorSet.getValue("descriptionText");
                 
                 String nameString = name.stringValue();
                 
                 String[] descArray = nameString.split(":");
                 
                 String ident = descArray[0];
                 String description = descArray[1];
                 
                 if (!ruleSet.containsKey(ident))
                 {
                     Rule rule = objFactory.createCWECoverageClaimTypeClaimsClaimRuleSetRule();
                     ruleSet.put(ident, rule);
                 }
                 
                 Rule rule = ruleSet.get(ident);
                 rule.setRuleID(ident);
                 rule.setRuleName(ident);
                 rule.setRuleComments(description);
                 
             }
             
             queryResult.close();
         }
         catch (RepositoryException e)
         {
             e.printStackTrace();
         }
         catch (MalformedQueryException e)
         {
             e.printStackTrace();
         }
         catch (QueryEvaluationException e)
         {
             e.printStackTrace();
         }
         finally
         {
             try
             {
                 con.close();
             }
             catch (RepositoryException e)
             {
                 e.printStackTrace();
             }
         }
         return ruleSet.values();
         
     }
     
     /**
      * main entry point
      * 
      * @param args
      */
     public static void main(String[] args)
     {
         
         new CoverageClaimGenerator(args);
         
     }
     
     /**
      * make sure that the arguments are correct.
      * 
      * @param args
      *            command line arguments.
      * @return
      */
     static Boolean verifyArguments(String[] args)
     {
         if (args.length < 2)
         {
             return false;
         }
         
         return true;
     }
     
     /**
      * create a new generator
      * 
      * @param args
      *            command line args
      */
     public CoverageClaimGenerator(String[] args)
     {
         Boolean argVerified = verifyArguments(args);
         
         if (!argVerified)
         {
             System.err.println("Incorrect arguments!");
             System.exit(1);
         }
         
         Repository repository = getRepositoryFromArguments(args);
         
         File outputFile = getOutputFileFromArguments(args);
         
         JAXBContext jc = getJaxContext();
         
         CWECoverageClaims claims = parseRepository(jc, repository);
         
         // write to TSV or marshall to XML.
         if (isWriteToTSV(args))
         {
             writeToTSV(claims, outputFile);
         }
         else
         {
             marshalClaims(jc, claims, outputFile);
         }
         
     }
     
     /**
      * start the Coverege generator without using the main method.
      * 
      * @param rep
      *            the repository to use to get the findings.
      * @param out
      *            the output file to write to.
      * @param writeToTsv
      *            whether to write to tsv or xml. TRUE writes to tsv file, FALSE
      *            writes to xml.
      */
     public CoverageClaimGenerator(Repository rep, File out, boolean writeToTsv)
     {
         Repository repository = rep;
         
         File outputFile = out;
         
         JAXBContext jc = getJaxContext();
         
         CWECoverageClaims claims = parseRepository(jc, repository);
         
         // write to TSV or marshall to XML.
         if (writeToTsv)
         {
             writeToTSV(claims, outputFile);
         }
         else
         {
             marshalClaims(jc, claims, outputFile);
         }
     }
     
     /**
      * claims are groups of claim. a claim is essentially a cwe.
      * 
      * @param objFactory
      * @param segment
      * @param repository
      * @return
      */
     private Collection<Claim> createClaims(ObjectFactory objFactory, Value segment, Repository repository)
     {
         HashMap<String, Claim> claims = new HashMap<String, CWECoverageClaimType.Claims.Claim>();
         
         RepositoryConnection con = null;
         
         try
         {
             con = getRepositoryConnection(repository);
             
             String adaptorQuery = "SELECT ?cwe ?cweName WHERE { " + "<" + segment + "> <http://toif/contains> ?cwe ."
                     + "?cwe <http://toif/type> \"toif:CWEIdentifier\" . " + "?cwe <http://toif/name> ?cweName . }";
             
             TupleQuery adaptorTupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, adaptorQuery);
             
             TupleQueryResult queryResult = adaptorTupleQuery.evaluate();
             
             while (queryResult.hasNext())
             {
                 BindingSet adaptorSet = queryResult.next();
                 Value name = adaptorSet.getValue("cweName");
                 Value cwe = adaptorSet.getValue("cwe");
                 
                 String cweIdString = name.stringValue();
                 if (!claims.containsKey(cweIdString))
                 {
                     Claim claim = objFactory.createCWECoverageClaimTypeClaimsClaim();
                     String cweId = cweIdString.replace("CWE-", "");
                     claim.setCWEID(cweId);
                     // use this to set any additional comments.
                     claim.setCWEClaimComments("");
                     
                     String cweName = getCweName(cweId);
                     
                     claim.setCWEName(cweName);
                     claim.setMatchAccuracy(MatchAccuracyType.UNKNOWN);
                     
                     claims.put(cweIdString, claim);
                 }
                 
                 Claim claim = claims.get(cweIdString);
                 RuleSet ruleSet = objFactory.createCWECoverageClaimTypeClaimsClaimRuleSet();
                ruleSet.getRule().addAll(getRules(objFactory, cwe, repository,segment));
                 claim.setRuleSet(ruleSet);
                 
             }
             
             queryResult.close();
         }
         catch (RepositoryException e)
         {
             e.printStackTrace();
         }
         catch (MalformedQueryException e)
         {
             e.printStackTrace();
         }
         catch (QueryEvaluationException e)
         {
             e.printStackTrace();
         }
         finally
         {
             try
             {
                 con.close();
             }
             catch (RepositoryException e)
             {
                 e.printStackTrace();
             }
         }
         
         return claims.values();
     }
     
     /**
      * coverage claim are a group of claims from a tool
      * 
      * @param objFactory
      * @param repository
      * @return
      */
     private List<CWECoverageClaimType> createCoverageClaims(ObjectFactory objFactory, Repository repository)
     {
         List<CWECoverageClaimType> results = new ArrayList<CWECoverageClaimType>();
         
         RepositoryConnection con = null;
         try
         {
             con = getRepositoryConnection(repository);
             
             String adaptorQuery = "SELECT ?generatorName ?segment ?version WHERE { ?y <http://toif/path> ?x . ?segment <http://toif/contains> ?y . ?segment <http://toif/toif:TOIFSegmentIsGeneratedByGenerator> ?generator . "
                     + "?generator <http://toif/name> ?generatorName . " + "?generator <http://toif/version> ?version}";
             
             TupleQuery adaptorTupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, adaptorQuery);
             
             TupleQueryResult queryResult = adaptorTupleQuery.evaluate();
             
             while (queryResult.hasNext())
             {
                 BindingSet adaptorSet = queryResult.next();
                 Value generatorName = adaptorSet.getValue("generatorName");
                 Value segment = adaptorSet.getValue("segment");
                 Value version = adaptorSet.getValue("version");
                 
                 if (generatorNames.contains(generatorName.stringValue()))
                 {
                     continue;
                 }
                 else
                 {
                     generatorNames.add(generatorName.stringValue());
                 }
                 
                 CWECoverageClaimType coverageClaim = objFactory.createCWECoverageClaimType();
                 
                 coverageClaim.setVendorName("KDM Analytics Inc");
                 coverageClaim.setToolsetName(generatorName.stringValue());
                 coverageClaim.setToolsetVersion(version.stringValue());
                 String fileName = getAFileFromSegment(segment, repository);
                 
                 String langType = getLanguageType(fileName);
                 coverageClaim.setLanguageType(langType);
                 
                 String lang = getLanguage(fileName);
                 coverageClaim.setLanguage(lang);
                 
                 XMLGregorianCalendar date = getDate(segment, repository);
                 
                 coverageClaim.setDateOfClaim(date);
                 
                 Claims claims = objFactory.createCWECoverageClaimTypeClaims();
                 
                 Collection<Claim> claimList = createClaims(objFactory, segment, repository);
                 
                 claims.getClaim().addAll(claimList);
                 coverageClaim.setClaims(claims);
                 
                 results.add(coverageClaim);
                 
             }
             
             queryResult.close();
         }
         catch (RepositoryException e)
         {
             e.printStackTrace();
         }
         catch (MalformedQueryException e)
         {
             e.printStackTrace();
         }
         catch (QueryEvaluationException e)
         {
             e.printStackTrace();
         }
         finally
         {
             try
             {
                 con.close();
             }
             catch (RepositoryException e)
             {
                 e.printStackTrace();
             }
         }
         return results;
     }
     
     /**
      * find a file in the segment.
      * 
      * @param segment
      * @param repository
      * @return
      */
     private String getAFileFromSegment(Value segment, Repository repository)
     {
         
         RepositoryConnection con = null;
         try
         {
             con = getRepositoryConnection(repository);
             
             String adaptorQuery = "SELECT ?fileName WHERE { <" + segment + "> <http://toif/contains> ?file . "
                     + "?file <http://toif/type> \"toif:File\" ." + "?file <http://toif/name> ?fileName . }";
             
             TupleQuery adaptorTupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, adaptorQuery);
             
             TupleQueryResult queryResult = adaptorTupleQuery.evaluate();
             
             while (queryResult.hasNext())
             {
                 BindingSet adaptorSet = queryResult.next();
                 Value name = adaptorSet.getValue("fileName");
                 
                 return name.stringValue();
             }
             
             queryResult.close();
         }
         catch (RepositoryException e)
         {
             e.printStackTrace();
         }
         catch (MalformedQueryException e)
         {
             e.printStackTrace();
         }
         catch (QueryEvaluationException e)
         {
             e.printStackTrace();
         }
         finally
         {
             try
             {
                 con.close();
             }
             catch (RepositoryException e)
             {
                 e.printStackTrace();
             }
         }
         return null;
     }
     
     /**
      * get the cwe name from the id.
      * 
      * @param cweId
      * @return
      */
     private String getCweName(String cweId)
     {
         CweToName cweToNameUtil = new CweToName();
         return cweToNameUtil.getCweName(cweId);
     }
     
     /**
      * get the date from the segment.
      * 
      * @param segment
      *            toif segment in the repository
      * @param repository
      *            the toif repository
      * @return
      */
     private XMLGregorianCalendar getDate(Value segment, Repository repository)
     {
         RepositoryConnection con = null;
         try
         {
             con = getRepositoryConnection(repository);
             
             String adaptorQuery = "SELECT ?dateValue WHERE { <" + segment + "> <http://toif/contains> ?date . "
                     + "?date <http://toif/date> ?dateValue . }";
             
             TupleQuery adaptorTupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, adaptorQuery);
             
             TupleQueryResult queryResult = adaptorTupleQuery.evaluate();
             
             while (queryResult.hasNext())
             {
                 BindingSet adaptorSet = queryResult.next();
                 Value dateValue = adaptorSet.getValue("dateValue");
                 
                 try
                 {
                     GregorianCalendar gregorianCalendar = new GregorianCalendar();
                     
                     DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     
                     Date date = formatter.parse(dateValue.stringValue());
                     gregorianCalendar.setTime(date);
                     
                     XMLGregorianCalendar xmlGrogerianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                     
                     xmlGrogerianCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
                     xmlGrogerianCalendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                             DatatypeConstants.FIELD_UNDEFINED);
                     
                     return xmlGrogerianCalendar;
                 }
                 catch (DatatypeConfigurationException e)
                 {
                     e.printStackTrace();
                 }
                 catch (ParseException e)
                 {
                     e.printStackTrace();
                 }
                 
             }
             
             queryResult.close();
         }
         catch (RepositoryException e)
         {
             e.printStackTrace();
         }
         catch (MalformedQueryException e)
         {
             e.printStackTrace();
         }
         catch (QueryEvaluationException e)
         {
             e.printStackTrace();
         }
         finally
         {
             try
             {
                 con.close();
             }
             catch (RepositoryException e)
             {
                 e.printStackTrace();
             }
         }
         
         return null;
         
     }
     
     /**
      * get the jaxb context.
      * 
      * @return
      * 
      */
     private JAXBContext getJaxContext()
     {
         JAXBContext jc = null;
         
         try
         {
             jc = JAXBContext.newInstance("generated");
         }
         catch (JAXBException e)
         {
             e.printStackTrace();
         }
         return jc;
     }
     
     /**
      * get the language of the file.
      * 
      * @param fileName
      *            the file to get the language from.
      * @return
      */
     private String getLanguage(String fileName)
     {
         if (fileName.endsWith(".C"))
         {
             return "C";
         }
         else if (fileName.endsWith(".Java"))
         {
             return "Java";
         }
         else if (fileName.endsWith(".CPP"))
         {
             return "C++";
         }
         else if (fileName.endsWith(".Class"))
         {
             return "Java";
         }
         if (fileName.endsWith(".c"))
         {
             return "C";
         }
         else if (fileName.endsWith(".java"))
         {
             return "Java";
         }
         else if (fileName.endsWith(".cpp"))
         {
             return "C++";
         }
         else if (fileName.endsWith(".class"))
         {
             return "Java";
         }
         else if (fileName.endsWith(".h"))
         {
             return "C";
         }
         else
         {
             return "??";
         }
     }
     
     /**
      * get the language type from the file name.
      * 
      * @param stringValue
      *            the language type
      * @return
      */
     private String getLanguageType(String fileName)
     {
         if (fileName.endsWith(".c"))
         {
             return "Source Code";
         }
         else if (fileName.endsWith(".java"))
         {
             return "Source Code";
         }
         else if (fileName.endsWith(".cpp"))
         {
             return "Source Code";
         }
         else if (fileName.endsWith(".class"))
         {
             return "Byte Code";
         }
         if (fileName.endsWith(".C"))
         {
             return "Source Code";
         }
         else if (fileName.endsWith(".Java"))
         {
             return "Source Code";
         }
         else if (fileName.endsWith(".CPP"))
         {
             return "Source Code";
         }
         else if (fileName.endsWith(".Class"))
         {
             return "Byte Code";
         }
         else if (fileName.endsWith(".h"))
         {
             return "Source Code";
         }
         else
         {
             return "??";
         }
     }
     
     /**
      * are we writing to tsv?
      * 
      * @param args
      *            command line args.
      * @return
      */
     private boolean isWriteToTSV(String[] args)
     {
         if (args[0].equals("-tsv"))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
     
     /**
      * marshall the claims to the output.
      * 
      * @param jc
      *            jaxc ontext
      * @param claims
      *            claims to output
      * @param outputFile
      *            the file to write to.
      */
     private void marshalClaims(JAXBContext jc, CWECoverageClaims claims, File outputFile)
     {
         
         try
         {
             Marshaller m = jc.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             m.marshal(claims, outputFile);
         }
         catch (JAXBException e)
         {
             e.printStackTrace();
         }
         
     }
     
     /**
      * parse the repository
      * 
      * @param jc
      *            jax context
      * @param repository
      *            the repository to parse.
      * @return
      */
     private CWECoverageClaims parseRepository(JAXBContext jc, Repository repository)
     {
         
         ObjectFactory objFactory = new ObjectFactory();
         
         // root element of the
         CWECoverageClaims CWECoverageClaimRoot = objFactory.createCWECoverageClaims();
         
         List<CWECoverageClaimType> claims = CWECoverageClaimRoot.getCWECoverageClaim();
         
         List<CWECoverageClaimType> coverageClaims = createCoverageClaims(objFactory, repository);
         
         claims.addAll(coverageClaims);
         
         return CWECoverageClaimRoot;
         
     }
     
     /**
      * write each individual claim.
      * 
      * @param writer
      *            the writer to output
      * @param cweCoverageClaimType
      *            the claim type.
      * @throws IOException
      */
     private void writeIndividualClaimsToWriter(FileWriter writer, CWECoverageClaimType cweCoverageClaimType) throws IOException
     {
         List<Claim> individualClaims = cweCoverageClaimType.getClaims().getClaim();
         
         for (Claim claim : individualClaims)
         {
             writer.append("\tCWE ID: " + claim.getCWEID());
             writer.append("\n");
             writer.append("\tCWE Name: " + claim.getCWEName());
             writer.append("\n");
             writer.append("\tMatch Accuracy: " + claim.getMatchAccuracy());
             writer.append("\n");
             
             writeRuleSetDetailToTSV(writer, claim);
         }
     }
     
     /**
      * write the cwe-coverage-claim to output
      * 
      * @param writer
      *            the output writer
      * @param overallClaims
      *            the cwe-coverage-claim.
      * @throws IOException
      */
     private void writeOverallCoverageClaimsToWriter(FileWriter writer, List<CWECoverageClaimType> overallClaims) throws IOException
     {
         for (CWECoverageClaimType cweCoverageClaimType : overallClaims)
         {
             writer.append("CWE Version: " + cweCoverageClaimType.getCWEVersion());
             writer.append('\n');
             writer.append("Vendor Name: " + cweCoverageClaimType.getVendorName());
             writer.append('\n');
             writer.append("Toolset Name: " + cweCoverageClaimType.getToolsetName());
             writer.append('\n');
             writer.append("Toolset Version: " + cweCoverageClaimType.getToolsetVersion());
             writer.append('\n');
             writer.append("Claim Date: " + cweCoverageClaimType.getDateOfClaim());
             writer.append('\n');
             writer.append("Language Type: " + cweCoverageClaimType.getLanguageType());
             writer.append('\n');
             writer.append("Language: " + cweCoverageClaimType.getLanguage());
             writer.append('\n');
             
             writeIndividualClaimsToWriter(writer, cweCoverageClaimType);
         }
     }
     
     /**
      * write the rule set to file
      * 
      * @param writer
      *            the output writer
      * @param claim
      *            the claim
      * @throws IOException
      */
     private void writeRuleSetDetailToTSV(FileWriter writer, Claim claim) throws IOException
     {
         List<Rule> rules = claim.getRuleSet().getRule();
         
         for (Rule rule : rules)
         {
             writer.append("\t\tRule ID: " + rule.getRuleID());
             writer.append("\n");
             writer.append("\t\tRule Name: " + rule.getRuleName());
             writer.append("\n");
             writer.append("\t\tRule Comments: " + rule.getRuleComments());
             writer.append("\n");
             
             writer.append("\n\n");
         }
         
     }
     
     /**
      * write to tsv.
      * 
      * @param claims
      * @param outputFile
      */
     private void writeToTSV(CWECoverageClaims claims, File outputFile)
     {
         FileWriter writer = null;
         
         try
         {
             writer = new FileWriter(outputFile);
             
             // write the header.
             writer.append("Overall Coverage Claim");
             writer.append('\t');
             writer.append("Individual CWE Claims");
             writer.append('\t');
             writer.append("Rule Set Detail");
             writer.append('\n');
             
             List<CWECoverageClaimType> overallClaims = claims.getCWECoverageClaim();
             
             writeOverallCoverageClaimsToWriter(writer, overallClaims);
             
             writer.flush();
             writer.close();
             
         }
         catch (IOException e)
         {
             System.err.println("There was an exit whilst writing to the TSV file.");
             System.exit(1);
         }
         
     }
     
 }
