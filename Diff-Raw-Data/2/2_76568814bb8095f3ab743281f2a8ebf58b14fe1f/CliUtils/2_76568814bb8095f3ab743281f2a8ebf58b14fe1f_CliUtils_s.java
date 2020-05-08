 package iTests.framework.utils;
 
 import com.gigaspaces.admin.cli.GS;
 import com.gigaspaces.client.ReadMultipleException;
 import com.gigaspaces.internal.client.spaceproxy.IDirectSpaceProxy;
 import com.j_spaces.core.Constants;
 import com.j_spaces.core.IJSpace;
 import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
 import com.j_spaces.core.admin.SpaceRuntimeInfo;
 import iTests.framework.tools.SGTestHelper;
 import net.jini.core.entry.UnusableEntryException;
 import net.jini.core.transaction.TransactionException;
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.springframework.util.StringUtils;
 
 import javax.xml.stream.*;
 import java.io.*;
 import java.lang.reflect.Field;
 import java.rmi.RemoteException;
 import java.security.Permission;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static iTests.framework.utils.LogUtils.log;
 import static org.testng.AssertJUnit.assertEquals;
 
 public class CliUtils {
 
     //used as the 1'st argument of GS.main
     private static String REQUIERED_ARGUMENT;
     
     private static ByteArrayOutputStream mainOutputStream;
     private static OutputStream stdOut;
     private static InputStream stdIn;
     private static CountDownLatch latch;
     
     //used to get the last regular expression match
     private static ArrayList<String> patternsMatched;
     
     //used to intercept "System.exit" in GS.main
     @SuppressWarnings("serial")
     private static class ExitTrappedException extends SecurityException
     {
     	private static final long serialVersionUID = 1L;
     }
     
     //used to intercept "System.exit" in GS.main
     private static SecurityManager securityManager = new SecurityManager() {
         public void checkPermission(Permission permission) {
             if (permission.getName().contains("exitVM")) {
                 throw new ExitTrappedException();
             }
         }
     };
     
     private static void forbidSystemExitCall() {
         System.setSecurityManager(securityManager);
     }
 
     private static void enableSystemExitCall() {
         System.setSecurityManager(null);
     }
     
     /***
      * 
      * @param args The agrguments to pass to the cli. (e.g: invokeGSMainOn("list","gsc","cpu"))
      * @return The System.out during GS main method run is retured
      */
     public static String invokeGSMainOn(String ... args) {
         return invokeGSMainOn(true, args);
     }
     
     /***
      * 
      * @param captureOutput if true, System.out will be captured and returned, otherwise "" is returned.
      * @param args The agrguments to pass to the cli. (e.g: invokeGSMainOn("list","gsc","cpu"))
      * @return The System.out during GS main method run is retured
      */
     public static String invokeGSMainOn(boolean captureOutput, String ... args) {
         if (captureOutput) {
             // Intercept System.out
             mainOutputStream = new ByteArrayOutputStream();
             stdOut = System.out;
             System.setOut(new PrintStream(new TeeOutputStream(stdOut,mainOutputStream)));
             
             invokeMain(args);
             
             String mainOutput = mainOutputStream.toString();
             
             // Restore System.out
             System.setOut(new PrintStream(stdOut));
             
             try {
                 mainOutputStream.close();
             } catch (IOException e) {
                 log("Error while closing Stream", e);
             }
             
             return mainOutput;
         
         } else {
             invokeMain(args);
             return "";
         }
     }
     
     private static void invokeMain(String ... args) {
         String[] commandArguments = new String[1 + args.length];
 
         if(SGTestHelper.isXap()){
             REQUIERED_ARGUMENT = "config/services/services.config";
         }
         else{
             REQUIERED_ARGUMENT = "config/tools/gs_cli.config";
         }
 
         commandArguments[0] = REQUIERED_ARGUMENT;
         for (int i = 1; i < commandArguments.length; i++) {
             commandArguments[i] = args[i-1];
         }
         
         try {
             forbidSystemExitCall();
             log(StringUtils.arrayToDelimitedString(commandArguments, " "));
             GS.main(commandArguments);
         } catch (ExitTrappedException e) { } // nop
         finally {
             enableSystemExitCall();
         }
         
         nullifyCLIStaticFields();
     }
     
     @SuppressWarnings("unchecked")
     private static void nullifyCLIStaticFields()  {
        try { 
             Class cl = Class.forName("com.gigaspaces.admin.cli.utils.CLISecurityRepository");
             Field f = cl.getDeclaredField("_cliSecurityRepository"); 
             f.setAccessible(true);
             f.set(null, null);
        } catch (Exception e) {
            log("While trying to change CLI's private fields", e);
        }
     }
     
     @SuppressWarnings("deprecation")
     public static String invokeGSMainWithResourceAsInputStreamOn(String resource, boolean captureOutput, String ... args) throws IOException {
      
         String mainOutput;
         stdIn = System.in;
         latch = new CountDownLatch(1);
         
         InputStream is = new FileBlockingInputStream(stdIn, resource);
         
         System.setIn(is);
         
         Thread t = new Thread(new GsMainWrapper(args));
 
         try {
         	if (captureOutput) {
         		// Intercept System.out
         		mainOutputStream = new ByteArrayOutputStream();
         		stdOut = System.out;
         		System.setOut(new PrintStream(mainOutputStream));
 
         		t.start();
         		try {
         			latch.await();
         		} catch (InterruptedException e1) { }
                 t.stop();
 
                 mainOutput = mainOutputStream.toString();
 
         		// Restore System.out
         		System.setOut(new PrintStream(stdOut));
 
         		try {
         			mainOutputStream.close();
         		} catch (IOException e) {
         			log("Error while closing Stream", e);
         		}
 
         	} else {
         		t.start();
         		try {
         			latch.await();
         		} catch (InterruptedException e1) { }
                 t.stop();
 
                 mainOutput = "";
         	}
 
         	try {
         		is.close();
         	} catch (IOException e) {
         		log("Error while closing stream", e);
         	}
 
         	System.setIn(stdIn);
 
         } finally {
         	try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
         	t.stop();
         }
         
         return mainOutput;
     }
     
     private static class GsMainWrapper implements Runnable {
         
         String[] commandArguments;
         
         public GsMainWrapper(String ... args) {
             commandArguments = new String[1 + args.length];
             commandArguments[0] = REQUIERED_ARGUMENT;
             for (int i = 1; i < commandArguments.length; i++) {
                 commandArguments[i] = args[i-1];
             }
         }
         
         public void run() {
             GS.main(commandArguments);
             nullifyCLIStaticFields();
         }
         
     }
     
     private static class FileBlockingInputStream extends InputStream {
 
         private boolean readFirstByte = false;
         private final InputStream is;
         int lastRead = 0;
         
         public FileBlockingInputStream(InputStream systemIn,String resource) throws IOException {
             is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
         }
         
         public int read() throws IOException {
             if (!readFirstByte) {
                 try {
                     Thread.sleep(2000);
                     readFirstByte = true;
                 } catch (InterruptedException e) { }
             }
             
             if (lastRead != -1) {
                 lastRead = is.read();
                 return lastRead;
             } else {
                 latch.countDown();
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) { }
                 return -1;
             }
         }
     }
     
     /***
      * 
      * @param regex the regex to look in the source
      * @param source the string to look upon
      * @return number of regex occurences in source 
      */
     public static int patternCounter(String regex, String source) {
         Pattern pattern = Pattern.compile(regex);
         Matcher patternCounterMatcher = pattern.matcher(source);
         patternsMatched = new ArrayList<String>();
         
         int counter = 0;
         while (patternCounterMatcher.find()) {
             patternsMatched.add(patternCounterMatcher.group());
             counter += 1;
         }
         
         return counter;
     }
     
     public static String getLastPatternMatch() {
         
         if (patternsMatched != null) {
             StringBuilder sb = new StringBuilder("[\n");
             for (String match : patternsMatched) {
                 sb.append(" " + match + " ,\n");
             }
             sb.deleteCharAt(sb.length()-1);
             sb.append("]");
             return sb.toString();
         }
         
         return "null";
     }
     
     /***
      * Used in the CLI test's Data providers.
      * @author Dan Kilman
      */
     public static class CommandPatternIterator implements Iterator<Object[]> {
 
         private final InputStream is;
         private final XMLStreamReader parser;
         private int event;
 
         public CommandPatternIterator(String xmlPath) throws XMLStreamException, FactoryConfigurationError, IOException {
             is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlPath);
             parser = XMLInputFactory.newInstance().createXMLStreamReader(is);
             nextScript();
         }
         
         public boolean hasNext() {
             return (event != XMLStreamConstants.END_DOCUMENT);
         }
 
         public Object[] next() {
             String currentScript = parser.getAttributeValue(null, "args");
             
             List<String[]> patterns = new ArrayList<String[]>();
             
             try {
                 nextPattern();
             } catch (XMLStreamException e1) {
                 e1.printStackTrace();
             }
             while (event != XMLStreamConstants.END_ELEMENT ||
                    !parser.getName().toString().equals("command")) {
                 
                 String currentRegex = parser.getAttributeValue(null, "regex");
                 String currentExpectedAmount = parser.getAttributeValue(null, "expected-amount");
                 
                 patterns.add(new String[] { currentRegex, currentExpectedAmount });
                 
                 try {
                     nextPattern();
                 } catch (XMLStreamException e) {
                     e.printStackTrace();
                 }
             }
             
             String[][] patternObjects = new String[patterns.size()][];
             for (int i = 0; i < patterns.size(); i++) {
                 patternObjects[i] = patterns.get(i);
             }
             
             try {
                 nextScript();
             } catch (XMLStreamException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             
             return new Object[] { currentScript, patternObjects };
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
         
         private void nextScript() throws XMLStreamException, IOException {
             while (true) {
                 event = parser.next();
                 if (event == XMLStreamConstants.END_DOCUMENT) {
                     parser.close();
                     is.close();
                     return;
                 }
                 if (event == XMLStreamConstants.START_ELEMENT
                         && parser.getName().toString().equals("command")) {
                     return;
                 }
             }
         }
         
         private void nextPattern() throws XMLStreamException {
             while (true) {
                 event = parser.next();
                 if (event == XMLStreamConstants.END_ELEMENT
                         && parser.getName().toString().equals("command")) {
                     return;
                 }
                 if (event == XMLStreamConstants.START_ELEMENT
                         && parser.getName().toString().equals("pattern")) {
                     return;
                 }
             }
         }
         
     }
     
     /***
      *  
      *  Used to cound objects in a backup space instance
      *  
      * @param target The space instance to count objects in
      * @param expected Expected amount of objects matching template
      * @param tmpl The template to check. 
      * @throws Exception
      * 
      */
     public static void assertCountInTarget(IJSpace target, int expected, Object tmpl)
             throws Exception {
         try {
             int actual = count(target, tmpl);
             assertEquals("wrong result for count target <"
                     + getSpaceName(target) + ">", expected, actual);
         } catch (ReadMultipleException ignore) {
             RemoteException firstCause = (RemoteException) ignore
                     .getMajorityCause();
             handleInactiveSpaceException(target, expected, firstCause);
         }
     }
 
     private static void handleInactiveSpaceException(IJSpace target,
             int expectedCount, RemoteException ignore) throws Exception {
         System.err.println("[see GS-1743] - ignored: " + ignore);
         String cachePolicy = ((IDirectSpaceProxy) target).getProxySettings()
                 .getSpaceAttributes().getCachePolicy();
 
         if (!cachePolicy.equals(String
                 .valueOf(Constants.CacheManager.CACHE_POLICY_ALL_IN_CACHE)))
             return;
         SpaceRuntimeInfo info = ((IRemoteJSpaceAdmin) target.getAdmin())
                 .getRuntimeInfo(Object.class.getName());
         int actualCount = 0;
         if (!info.m_NumOFEntries.isEmpty()) {
             for (Integer count : info.m_NumOFEntries) {
                 actualCount += count;
             }
         }
 
         assertEquals("wrong count for target <" + getSpaceName(target)
                 + "> Expected: [" + expectedCount + "], Actual: ["
                 + actualCount + "]", expectedCount , actualCount);
     }
 
     private static String getSpaceName(IJSpace space) throws Exception {
        return space.getContainer().getName() + ":" + space.getName();
     }
 
     private static int count(IJSpace target, Object tmpl) throws TransactionException,
             UnusableEntryException, RemoteException {
         return target.readMultiple(tmpl, null, Integer.MAX_VALUE).length;
     }
     
     
     ///////////////////////
     
     
     public static File createFileFromInputStream(InputStream is, String fileName) throws IOException {
      
         File file = new File(fileName);
         OutputStream out = new FileOutputStream(file);
         byte buf[] = new byte[1024];
         int len;
         while((len=is.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         out.close();
         
         return file;
     }
     
     
 
     
 }
