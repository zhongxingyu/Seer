 import java.io.*;
 
 import com.ericsson.otp.erlang.*;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.RenderedWebElement;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class Server {
     private OtpNode node;
 
     private BufferedWriter out;
 
     public void start() throws java.io.IOException, 
 			       com.ericsson.otp.erlang.OtpErlangExit,
 			       com.ericsson.otp.erlang.OtpErlangDecodeException {
 	FileWriter fstream = new FileWriter("out.txt");
         out = new BufferedWriter(fstream);
 	node = new OtpNode("WD@localhost", "Yjb5XSNf");
 	OtpMbox mbox = node.createMbox("server");
 	System.out.println("Starting\n");
 
 	System.out.println("test\n");
 
 	OtpErlangObject o;
 	OtpErlangTuple msg;
 	OtpErlangPid from;
 	while (true) {
	    System.out.println("wait");
 	    o = mbox.receive();
	    System.out.println("received");
	    System.out.println("testing 123");
 	    if (o instanceof OtpErlangTuple) {
		
 		msg = (OtpErlangTuple)o;
 		from = (OtpErlangPid)(msg.elementAt(0));
 		OtpErlangAtom fun = (OtpErlangAtom)(msg.elementAt(1));
 		OtpErlangAtom arg = (OtpErlangAtom)(msg.elementAt(2));
 		execute(fun.toString());
 		mbox.send(from,fun);
 	    }
 	}
 
     }
 
     public void execute(String fun) throws java.io.IOException {
 	System.out.println(fun);
 	if (fun.compareTo("new") == 0) {
 	    WebDriver driver = new FirefoxDriver();
 	} else if (fun.compareTo("stop") == 0) {
 	    System.exit(0);
 	};
     }
 
 }
