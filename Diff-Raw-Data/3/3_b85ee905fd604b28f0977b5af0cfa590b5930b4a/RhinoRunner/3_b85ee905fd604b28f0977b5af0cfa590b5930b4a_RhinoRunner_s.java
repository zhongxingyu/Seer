 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.eclipse.dltk.rhino.dbgp.DBGPDebugger;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 
 public class RhinoRunner {
 
 	public static void main(String[] args) {
 		Context cx = Context.enter();
 		if (args.length > 1) {
 			String host = args[1];
 			String porg = args[2];
 
 			DBGPDebugger debugger;
 			try {
 				final Socket s = new Socket(host, Integer.parseInt(porg));
 				debugger = new DBGPDebugger(s, args[0], args[3], cx);
 
 				debugger.start();
 				cx.setDebugger(debugger, null);
 
 				Scriptable scope = cx.initStandardObjects();
 				synchronized (debugger) {
 					try {
 						debugger.wait();
 					} catch (InterruptedException e) {
 						throw new IllegalStateException();
 					}
 				}
 				try {
 					cx.setGeneratingDebug(true);
 					cx.setOptimizationLevel(-1);
 					cx.evaluateReader(scope, new FileReader(args[0]), args[0],
 							0, null);
 
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				debugger.notifyEnd();
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			Scriptable scope = cx.initStandardObjects();
 			try {
 				cx.evaluateReader(scope, new FileReader(args[0]), args[0], 0,
 						null);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 }
