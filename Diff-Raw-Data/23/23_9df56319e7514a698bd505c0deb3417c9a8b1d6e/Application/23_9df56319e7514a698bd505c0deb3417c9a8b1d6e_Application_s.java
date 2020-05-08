 package jp.thisnor.dre.app;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 
 
 public class Application {
 	public static String APP_NAME = "DRE Similar Images Detector";
	public static String VERSION = "1.0 Beta1";
 
 	public static void main(String[] args) {
 		if (args.length == 0) {
 			PrintStream out = null;
 			PrintStream err = null;
 			try {
 				if (isInJar()) {
 					out = new PrintStream(new FileOutputStream("stdout"), true); //$NON-NLS-1$
 					err = new PrintStream(new FileOutputStream("stderr"), true); //$NON-NLS-1$
 					System.setOut(out);
 					System.setErr(err);
 				}
 				new DREFrame().open();
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (out != null) { out.flush(); out.close(); }
 				if (err != null) { err.flush(); err.close(); }
 			}
 		} else {
 			if (!new ConsoleApp(args).run()) {
 				System.err.println("Exit on failure.");
 			}
 		}
 	}
 
 	public static boolean isInJar() {
 		return !new File("res").exists();
 	}
 }
