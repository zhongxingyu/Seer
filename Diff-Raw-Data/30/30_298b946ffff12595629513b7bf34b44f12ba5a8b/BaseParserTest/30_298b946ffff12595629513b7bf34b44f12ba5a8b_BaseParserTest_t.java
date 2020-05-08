 package net.anei.cadpage.parsers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import net.anei.cadpage.SmsMmsMessage;
 import net.anei.cadpage.SmsMsgInfo;
import net.anei.cadpage.SmsPopupUtils;
 import net.anei.cadpage.TestManagePreferences;
 import net.anei.cadpage.parsers.FieldProgramParser.Field;
 
 import static org.junit.Assert.*;
 
 
 public abstract class BaseParserTest {
   
   // Flag set to turn on strict map address checking
   // This should be turned on when testing individual test classes
   // it needs to be turned off when running the entire suite as it causes
   // about a third of existing test classes to fail.
   private static final boolean STRICT_MAP_CHECK = true;
   
   private static final String FROM_ADDRESS = "1112223333";
 
   private SmsMsgParser parser = null;
   private String defCity;
   private String defState;
   private String fromAddress = FROM_ADDRESS;
   private TestManagePreferences preferences;
   private boolean skipBadTest = false;
   
   protected BaseParserTest() {
     preferences = new TestManagePreferences();
     preferences.setTestOverrideDefault(false);
   }
   
   protected TestManagePreferences getPreferences() {
     return preferences;
   }
   
   public void setParser(SmsMsgParser parser, String defCity, String defState) {
     setParser(parser);
     this.defCity = defCity;
     this.defState = defState;
   }
   
   public void setParser(SmsMsgParser parser) {
     // If parser is set twice, assume this is a group parser test
     // in which case bad message tests that should fail for the target parser
     // might be passed by another parser in the group.
     if (this.parser != null) skipBadTest = true;
     this.parser = parser;
   }
   
   public void setDefaults(String defCity, String defState) {
     this.defCity = defCity;
     this.defState = defState;
   }
   
   public void setFromAddress(String fromAddress) {
     this.fromAddress = fromAddress;
   }
   
   
   @Before
   public void baseSetup() {
     this.setFromAddress(FROM_ADDRESS);
   }
   
   @Test
   public void testBadMsg() {
     // Just call the parser with a badly formated msg and make sure it doesn't croak
     checkError("BAD MSG");
     checkError("");
   }
   
   private void checkError(String test) {
     parser.isPageMsg(new SmsMmsMessage(fromAddress, test, 0L, 0));
   }
   
   public void doBadTest(String test) {
     if (skipBadTest) return;
     SmsMmsMessage msg = new SmsMmsMessage(fromAddress, test, 0L, 0);
     assertFalse(parser.isPageMsg(msg));
   }
   
   /**
    * Main parse test method
    * @param title - test name
    * @param test - test message
    * @param result - expected results
    */
   public void doTest(String title, String test, String ... result) {
     doSubTest(title, "", test, result);
   }
   
   /**
    * Main parse test method
    * @param title - test name
    * @param chkMapAddr - true if map addresses should be checked
    * @param test - test message
    * @param result - expected results
    */
   public void doTest(String title, boolean chkMapAddr, String test, String ... result) {
     doSubTest(title, chkMapAddr, "", test, result);
   }
   
   /**
    * Main parse test method
    * @param title - test name
    * @param subject - test subject
    * @param test - test message
    * @param result - expected results
    */
   public void doSubTest(String title, String subject, String test, String ... result) {
     doSubTest(title, true, subject, test, result);
   }
     
   
   /**
    * Main parse test method
    * @param title - test name
    * @param chkMapAddr - true if map addresses should be checked
    * @param subject - test subject
    * @param test - test message
    * @param result - expected results
    */
   public void doSubTest(String title, boolean chkMapAddr, String subject, String test, String ... result) {
     SmsMsgInfo.Data data = new SmsMsgInfo.Data();
     String expMapAddr = "";
     for (String str : result) {
       int pt = str.indexOf(':');
       String sType = str.substring(0, pt);
       str = str.substring(pt+1);
       if (sType.equals("CALL")) data.strCall = str;
       else if (sType.equals("PLACE")) data.strPlace = str;
       else if (sType.equals("ADDR")) data.strAddress = str;
       else if (sType.equals("MADDR")) expMapAddr = str;
       else if (sType.equals("CITY")) data.strCity = str;
       else if (sType.equals("APT")) data.strApt = str;
       else if (sType.equals("X")) data.strCross = str;
       else if (sType.equals("BOX")) data.strBox = str;
       else if (sType.equals("UNIT")) data.strUnit = str;
       else if (sType.equals("ST")) data.strState = str;
       else if (sType.equals("MAP")) data.strMap = str;
       else if (sType.equals("ID")) data.strCallId = str;
       else if (sType.equals("PHONE")) data.strPhone = str;
       else if (sType.equals("INFO")) data.strSupp = str;
       else if (sType.equals("SRC")) data.strSource = str;
       else if (sType.equals("CODE")) data.strCode = str;
       else if (sType.equals("NAME")) data.strName = str;
       else if (sType.equals("PRI")) data.strPriority = str;
       else if (sType.equals("CH")) data.strChannel = str;
       else if (sType.equals("GPS")) data.strGPSLoc = str;
       else if (sType.equals("DATE")) data.strDate = str;
       else if (sType.equals("TIME")) data.strTime = str;
       else fail("Keyword " + sType + " is not defined");
     }
     
     SmsMmsMessage msg = new SmsMmsMessage(fromAddress, test, 0L, 0);
     assertTrue(title + ":parse", parser.isPageMsg(msg));
     SmsMsgInfo info = msg.getInfo();
     String actMapAddr = "";
     if (chkMapAddr) {
       actMapAddr = info.getMapAddress();
      if (!SmsPopupUtils.GPSPattern.matcher(actMapAddr).find()) {
        int pt = actMapAddr.indexOf(',');
        if (pt >= 0) actMapAddr = actMapAddr.substring(0,pt).trim();
      }
       if (actMapAddr.equals(info.getAddress())) actMapAddr = "";
       if (!STRICT_MAP_CHECK && expMapAddr.length() == 0) actMapAddr = "";
     }
     
     assertEquals(title + ":Call", data.strCall, info.getCall());
     assertEquals(title + ":Place", data.strPlace, info.getPlace());
     assertEquals(title + ":Address", data.strAddress, info.getAddress());
     assertEquals(title + ":MapAddr", expMapAddr, actMapAddr);
     assertEquals(title + ":City", data.strCity, info.getCity());
     assertEquals(title + ":Apt", data.strApt, info.getApt());
     assertEquals(title + ":Cross", data.strCross, info.getCross());
     assertEquals(title + ":Box", data.strBox, info.getBox());
     assertEquals(title + ":Unit", data.strUnit, info.getUnit());
     assertEquals(title + ":State", data.strState, info.getState());
     assertEquals(title + ":Map", data.strMap, info.getMap());
     assertEquals(title + ":ID", data.strCallId, info.getCallId());
     assertEquals(title + ":Phone", data.strPhone, info.getPhone());
     assertEquals(title + ":Supp", data.strSupp, info.getSupp());
     assertEquals(title + ":Src", data.strSource, info.getSource());
     assertEquals(title + ":Code", data.strCode, info.getCode());
     assertEquals(title + ":Name", data.strName, info.getName());
     assertEquals(title + ":Pri", data.strPriority, info.getPriority());
     assertEquals(title + ":Ch", data.strChannel, info.getChannel());
     assertEquals(title + ":GPS", data.strGPSLoc, info.getGPSLoc());
     assertEquals(title + ":Date", data.strDate, info.getDate());
     assertEquals(title + ":Time", data.strTime, info.getTime());
     
     assertEquals(title + ":DefCity", defCity, info.getDefCity());
     assertEquals(title + ":DefState", defState, info.getDefState());
   }
   
   private static final Map<String,String> KEYWORD_MAP = new HashMap<String,String>(); 
   static {
     KEYWORD_MAP.put("CALL", "CALL");
     KEYWORD_MAP.put("PLACE", "PLACE");
     KEYWORD_MAP.put("ADDR", "ADDR");
     KEYWORD_MAP.put("CITY", "CITY");
     KEYWORD_MAP.put("APT", "APT");
     KEYWORD_MAP.put("X", "X");
     KEYWORD_MAP.put("BOX","BOX");
     KEYWORD_MAP.put("UNIT", "UNIT");
     KEYWORD_MAP.put("ST", "ST");
     KEYWORD_MAP.put("MAP", "MAP");
     KEYWORD_MAP.put("ID", "ID");
     KEYWORD_MAP.put("PHONE", "PHONE");
     KEYWORD_MAP.put("INFO", "INFO");
     KEYWORD_MAP.put("SRC", "SRC");
     KEYWORD_MAP.put("CODE", "CODE");
     KEYWORD_MAP.put("NAME", "NAME");
     KEYWORD_MAP.put("PRI", "PRI");
     KEYWORD_MAP.put("CH", "CH");
     KEYWORD_MAP.put("GPS", "GPS");
     KEYWORD_MAP.put("DATE", "DATE");
     KEYWORD_MAP.put("TIME", "TIME");
   }
   
   /**
    * Generate test code for current parser from text string read from stdin
    */
   public void generateTests() {
     generateTests("T1", null);
   }
   
   /**
    * Generate test code for current parser from text string read from stdin
    * @param title title of first test
    */
   public void generateTests(String title) {
     generateTests(title, null);
   }
     
     /**
      * Generate test code for current parser from text string read from stdin
      * @param title title of first test
      */
     public void generateTests(String title, String fieldNames) {
     
     // Get list of terms to display
     String[] terms;
     if (fieldNames != null) {
       terms = fieldNames.split(" ");
     } else {
       terms = getTestTerms();
     }
     
     // Read list of test strings
     String[] testMsgs = getTestMsgs();
     
     // And generate a test for each test string
     for (String testMsg : testMsgs) {
       generateTest(title, testMsg, terms);
       title = incTitle(title);
     }
   }
   
   private String[] getTestTerms() {
 
     String prog;
     if (parser instanceof FieldProgramParser) {
       prog = ((FieldProgramParser)parser).getProgram();
       prog = prog.replaceAll("\\(|\\||\\)", " ").trim();
     } else {
       throw new RuntimeException("Cannot construct field list for this parser");
     }
     
     List<String> termList = new ArrayList<String>();
     Set<String> termSet = new HashSet<String>();
     for (String term : prog.split(" +")) {
       int st = term.indexOf(':')+1;
       int pt = st;
       while (pt < term.length() && Character.isJavaIdentifierPart(term.charAt(pt))) pt++;
       String term1 = term.substring(st,pt);
       String qual = null;
       if (pt < term.length() && term.charAt(pt) == '/') {
         st = ++pt;
         while (pt < term.length() && Character.isJavaIdentifierPart(term.charAt(pt))) pt++;
         qual = term.substring(st,pt); 
       }
       Field fld = ((FieldProgramParser)parser).getField(term1);
       fld.setQual(qual);
       String term2 = fld.getFieldNames();
       if (term2 == null) term2 = KEYWORD_MAP.get(term1);
       if (term2 == null) continue;
       for (String term3 : term2.split(" +")) {
         if (termSet.add(term3)) termList.add(term3);
       }
     }
     
     return termList.toArray(new String[termList.size()]);
   }
 
   /**
    * Get a list of test messages. terminated by a blank line or EOF
    * @return array of test messages
    */
   private String[] getTestMsgs() {
     
     BufferedReader ir = new BufferedReader(new InputStreamReader(System.in));
     
     List<String> msgList = new ArrayList<String>();
     System.out.println("Enter test messages, terminated by an empty line");
     while (true) {
       String line = null;
       try { line = ir.readLine(); } catch (IOException ex) {};
       if (line == null) break;
       if (line.trim().length() == 0) break;
       line = line.replace("\\n", "\n");
       msgList.add(line);
     }
     return msgList.toArray(new String[msgList.size()]);
   }
 
   /**
    * Write one test code to stdout
    * @param title test title
    * @param test text message to be tested
    * @param terms list of expected value names
    */
   private void generateTest(String title, String test, String[] terms) {
     
     SmsMmsMessage msg = new SmsMmsMessage(fromAddress, test, 0L, 0);
     if (!parser.isPageMsg(msg)) {
       System.out.println();
       System.out.println("// ************************ PARSE FAILURE *****************************");
       System.out.println("// " + test);
       return;
     }
     
     SmsMsgInfo info = msg.getInfo();
     System.out.println("");
     System.out.println("    doTest(\"" + title + "\",");
     System.out.print("        \"" + escape(test) + "\"");
     for (int jj = 0; jj<terms.length; jj++) {
       String term = terms[jj];
       String value = getValue(info, term);
       if (value.length() == 0) continue;
       System.out.print(",\n        \"" + term + ":" + escape(value) + "\"");
       if (term.equals("ADDR")) {
         term = "MADDR";
          value = getValue(info, term);
         if (value.length() == 0) continue;
         System.out.print(",\n        \"" + term + ":" + escape(value) + "\"");
       }
     }
     System.out.println(");");
   }
 
   private String escape(String value) {
     value = value.replace("\\", "\\\\");
     value = value.replace("\"", "\\\"");
     value = value.replace("\n", "\\n");
     return value;
   }
 
   /**
    * Retrieve specified value from message info object
    * @param info message info object
    * @param sType requested value type
    * @return value of requested type
    */
   private String getValue(SmsMsgInfo info, String sType) {
     if (sType.equals("CALL")) return info.getCall();
     if (sType.equals("PLACE")) return info.getPlace();
     if (sType.equals("ADDR")) return info.getAddress();
     if (sType.equals("MADDR")) {
       String mapAddr = info.getMapAddress();
       int pt = mapAddr.indexOf(',');
       if (pt >= 0) mapAddr = mapAddr.substring(0,pt).trim();
       if (mapAddr.equals(info.getAddress())) mapAddr = "";
       return mapAddr;
     }
     if (sType.equals("CITY")) return info.getCity();
     if (sType.equals("APT")) return info.getApt();
     if (sType.equals("X")) return info.getCross();
     if (sType.equals("BOX")) return info.getBox();
     if (sType.equals("UNIT")) return info.getUnit();
     if (sType.equals("ST")) return info.getState();
     if (sType.equals("MAP")) return info.getMap();
     if (sType.equals("ID")) return info.getCallId();
     if (sType.equals("PHONE")) return info.getPhone();
     if (sType.equals("INFO")) return info.getSupp();
     if (sType.equals("SRC")) return info.getSource();
     if (sType.equals("CODE")) return info.getCode();
     if (sType.equals("NAME")) return info.getName();
     if (sType.equals("PRI")) return info.getPriority();
     if (sType.equals("CH")) return info.getChannel();
     if (sType.equals("GPS")) return info.getGPSLoc();
     if (sType.equals("DATE")) return info.getDate();
     if (sType.equals("TIME")) return info.getTime();
     else throw new RuntimeException("Keyword " + sType + " is not defined");
   }
   
   /**
    * Increment test title
    * @param title current test title
    * @return next test title
    */
   private String incTitle(String title) {
     
     // Identify the trailing digit portion of the title
     int brk = 0;
     for (int jj = 0; jj<title.length(); jj++) {
       if (! Character.isDigit(title.charAt(jj))) brk = jj+1;
     }
     int testNo = Integer.parseInt(title.substring(brk)) + 1;
     return title.substring(0,brk) + String.format("%" + (title.length()-brk) + "d", testNo);
     
   }
 }
