 package org.tg8.sdt;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import javax.swing.WindowConstants;
 
 import org.tg8.sdt.data.SDTDAO;
 import org.tg8.sdt.data.file.SDTDAOImpl;
 import org.tg8.sdt.domain.SDTDomainLogic;
 import org.tg8.sdt.gui.SDTFrame;
 
 import apple.dts.samplecode.osxadapter.OSXAdapter;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//Initialize DAO. Initializing the file DAO directly
 		//is crude, but sufficient until the app becomes
 		//more complex.
 		//Cleanup safely destroys the DAO 
 		try {
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SDT");
 			SDTDAO dao = new SDTDAOImpl();
 			SDTDomainLogic.createSDTDomainLogic(dao);
			SDTFrame frame = new SDTFrame("Student Development Tracker");
 			Cleanup clean = new Cleanup(dao);
 			frame.addWindowListener(clean);
 			frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 			
 			if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
 				initMACOSXQuit(clean);
 			}
 			frame.setVisible(true);
 		} catch (Exception e) {
 			try {
				PrintWriter errorLog = new PrintWriter(new BufferedWriter(new FileWriter("error.log")));
 				e.printStackTrace(errorLog);
 				errorLog.close();
 			} catch (IOException e1) {
				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private static void initMACOSXQuit(Cleanup clean) {
 		 try {
 			 Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
 			 Class<?> applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
 			 Method addListenerMethod = applicationClass.getDeclaredMethod("addApplicationListener", new Class[] { applicationListenerClass });
 			 Object cleanProxy = Proxy.newProxyInstance(OSXAdapter.class.getClassLoader(), new Class[] { applicationListenerClass }, clean);
 			 addListenerMethod.invoke(applicationClass.getConstructor((Class[])null).newInstance((Object[])null), new Object[] { cleanProxy });
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
