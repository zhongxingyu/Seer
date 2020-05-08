 package ua.com.zhanna;
 
 import java.io.*;
 import java.nio.charset.*;
 
 import org.pmw.tinylog.Logger;
 
 import ua.com.zhanna.watching.*;
 import ua.com.zhanna.operation.*;
 import ua.com.zhanna.printing.*;
 
 public class Application {
     private static final String HELP_FILE = "/usage.txt";
     private static final String HELP_FILE_ENCODING = "UTF-8";
 
     private Operation operation;
     private Watching watching;
     private Printing printing;
 
     public Application(String encoding, String path) throws IOException {
 	operation = new EncodingOperation(encoding);
 	watching = new Watching(path);
 	printing = new Printing();
     }
 
     public Application() throws IOException {
 	this(Charset.defaultCharset().toString(), ".");
     }
 
     public void start() {
 	watching.addWatchingListener(new WatchingListener() {
 		public void entryIsCreated(EntryEvent e) {
		    printing.print(operation.operate(e.getEntry()));
 		}
 		public void interruptedError(ErrorEvent event) {
 		    Logger.error("interrupted error");
 		}
 		public void watchKeyError(ErrorEvent event) {
 		    Logger.error("watchkey error");
 		}
 	    });
 	watching.start();
     }
 
     public void usage() {
 	try {
 	    BufferedReader in = 
 		new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(HELP_FILE),
 							 Charset.forName(HELP_FILE_ENCODING)));
 	    String s;
 	    while ((s = in.readLine()) != null)
 		System.out.println(s);
 	} catch (Exception e) {
 	    Logger.error("Ooops: " + e.getMessage());
 	}
     }
 
     /* ENTER POINT */
     public static void main(String[] args) throws IOException {
 	if (args.length != 2 || args[0].equals("--help"))
 	    new Application().usage();
 	else
 	    new Application(args[0], args[1]).start();
     }
 }
 
 
 
 
