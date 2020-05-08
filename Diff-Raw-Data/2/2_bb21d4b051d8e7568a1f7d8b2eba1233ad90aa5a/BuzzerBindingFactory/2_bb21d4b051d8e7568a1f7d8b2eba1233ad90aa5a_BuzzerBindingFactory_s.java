 package com.earlofmarch.reach.input;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.logging.*;
 
 /**
  * Return a {@link BuzzerBinding} appropriate to the system.
  * @author Ian Dewan
  */
 public class BuzzerBindingFactory {
 	private static Logger log;
 	
 	static {
 		log = Logger.getLogger("reach.input");
 		log.setLevel(Level.ALL);
 		log.addHandler(new ConsoleHandler());
 	}
 	
 	/**
  	* Return a {@link BuzzerBinding} appropriate to the system.
  	* @return The BuzzerBinding
  	* @throws IOException Something goes horribly wrong.
  	*/
 	public static BuzzerBinding getBinding() throws IOException {
 		log.log(Level.INFO, "Entering BuzzerBindingFactory.getBinding()");
 		if (System.getProperty("os.name").contains("Windows")) {
 			return windows();
 		} else { // assume Linux
 			//TODO
 			return null;
 		}
 	}
 	
 	private static WindowsBuzzerBinding windows() throws IOException {
 		Process server = null;
 		log.log(Level.INFO, "Entering BuzzerBindingFactory.windows()");
 		try {
 			server = Runtime.getRuntime().exec("./glue.exe");
 		} catch (IOException e) {
 			log.log(Level.SEVERE, "Error creating glue.exe", e);
 			throw e;
 		}
 		new Thread(new ErrorEater(server.getErrorStream())).start();
		Runtime.addShutdownHook(new Thread(new Destroyer(server)));
 		return new WindowsBuzzerBinding(server.getInputStream(),
 				server.getOutputStream());
 	}
 	
 	/**
 	 * Dispose of the stderr stream to keep the Windows process running.
 	 */
 	private static class ErrorEater implements Runnable {
 		private InputStreamReader src;
 
 		public ErrorEater(InputStream s) {
 			src = new InputStreamReader(s);
 		}
 		
 		@Override
 		public void run() {
 			char buf;
 			
 			while (true) {
 				try {
 					buf = (char) src.read();
 				} catch (IOException e) {
 					log.log(Level.WARNING, "Error reading glue.exe stderr", e);
 					return;
 				}
 				System.err.print(buf);
 			}
 		}
 		
 	}
 	
 	private static class Destroyer implements Runnable {
 		private Process p;
 		
 		public Destroyer(Process p) {
 			this.p = p;
 		}
 		
 		public void run() {
 			p.destroy();
 		}
 	}
 	
 }
