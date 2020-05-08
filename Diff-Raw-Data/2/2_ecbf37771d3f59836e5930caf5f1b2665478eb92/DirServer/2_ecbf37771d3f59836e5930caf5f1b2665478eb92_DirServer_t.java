 package dolda.jsvc.scgi;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.logging.*;
 import dolda.jsvc.*;
 import dolda.jsvc.util.*;
 import dolda.jsvc.j2ee.PosixArgs;
 
 public class DirServer extends Server {
     private final Map<File, DSContext> contexts = new HashMap<File, DSContext>();
     private final Environment env;
     private final Logger logger = Logger.getLogger("dolda.jsvc.scgi.dirserver");
     
     public DirServer(ServerSocket sk, Environment env) {
 	super(sk);
 	this.env = env;
     }
 
     private DSContext context(File file) throws ThreadContext.CreateException {
 	synchronized(contexts) {
 	    DSContext ctx = contexts.get(file);
 	    String act = "loaded %s as %s";
 	    if(ctx != null) {
 		if(ctx.mtime < file.lastModified()) {
		    ctx.tg.shutdown();
 		    contexts.remove(file);
 		    ctx = null;
 		    act = "reloaded %s as %s";
 		}
 	    }
 	    if(ctx == null) {
 		ctx = new DSContext(file, env);
 		contexts.put(file, ctx);
 		logger.config(String.format(act, file, ctx.name()));
 	    }
 	    return(ctx);
 	}
     }
 
     public void handle(Map<String, String> head, Socket sk) throws Exception {
 	String filename = head.get("SCRIPT_FILENAME");
 	if(filename == null)
 	    throw(new Exception("Request for DirServer must contain SCRIPT_FILENAME"));
 	File file = new File(filename);
 	if(!file.exists() || !file.canRead())
 	    throw(new Exception("Cannot access the requested JSvc file " + file.toString()));
 	DSContext ctx = context(file);
 	Request req = new ScgiRequest(sk, head);
 	RequestThread w = ctx.tg.respond(req);
 	w.start();
     }
 
     private static void usage(PrintStream out) {
 	out.println("usage: dolda.jsvc.scgi.DirServer [-h] [-e CHARSET] [-d DATADIR] PORT");
     }
     
     public static void main(String[] args) {
 	PosixArgs opt = PosixArgs.getopt(args, "h");
 	if(opt == null) {
 	    usage(System.err);
 	    System.exit(1);
 	}
 	String charset = null;
 	File datroot = null;
 	for(char c : opt.parsed()) {
 	    switch(c) {
 	    case 'e':
 		charset = opt.arg;
 		break;
 	    case 'd':
 		datroot = new File(opt.arg);
 		if(!datroot.exists() || !datroot.isDirectory()) {
 		    System.err.println(opt.arg + ": no such directory");
 		    System.exit(1);
 		}
 		break;
 	    case 'h':
 		usage(System.out);
 		return;
 	    }
 	}
 	if(opt.rest.length < 1) {
 	    usage(System.err);
 	    System.exit(1);
 	}
 	Environment env = (datroot == null)?new Environment():new Environment(datroot);
 	env.initvm();
 	int port = Integer.parseInt(opt.rest[0]);
 	ServerSocket sk;
 	try {
 	    sk = new ServerSocket(port);
 	} catch(IOException e) {
 	    System.err.println("could not bind to port " + port + ": " + e.getMessage());
 	    System.exit(1);
 	    return; /* Because javac is stupid. :-/ */
 	}
 	DirServer s = new DirServer(sk, env);
 	if(charset != null)
 	    s.headcs = charset;
 	
 	new Thread(s, "SCGI server thread").start();
     }
 }
