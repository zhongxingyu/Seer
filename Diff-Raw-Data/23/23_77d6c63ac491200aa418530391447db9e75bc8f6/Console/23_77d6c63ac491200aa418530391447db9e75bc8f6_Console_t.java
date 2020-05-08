 package edu.berkeley.eduride.base_plugin.util;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleFactory;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 
 import edu.berkeley.eduride.base_plugin.EduRideBase;
 
 
 
 public class Console implements IConsoleFactory {
	
 
 	public static void msg (String msg) {
 		System.out.println(msg);
 		out.println(msg);
 	}
 	
 	public static void msg (Exception e) {
 		msg(e.toString());
 	}
 	
 	public static void err (String msg) {
 		System.err.println(msg);
 		err.println(msg);
 	}
 	
 	public static void err (Exception e) {

		err(e.toString());  // first line from exception only
		
		//java.util.Properties properties = System.getProperties();
		//String launcher = properties.getProperty("eclipse.launcher");
		//if (launcher != null && launcher.endsWith("eclipse.exe")) {
			e.printStackTrace();  // full exception
		//} 

		
	}
	
	public static void err (String msg, Exception e) {
		err(msg + " (cont-->)");
		err(e);
 	}
 	
 	
 	// TODO this should throw an exception and put an entry into the problem view, yo
 	public static void isaErr (String msg) {
 		err(msg);
 		
 	}
 	
 	/////////////
 	
 	static private final String CONSOLE_NAME = "EduRide Console";
 	static private Device device = Display.getCurrent();
 	static private final Color RED = new Color(device, 255, 0, 0);
 	static private final Color BLACK = new Color(device, 0, 0, 0);
 	
 	static private MessageConsole console = null;
 	static private MessageConsoleStream out;
 	static private MessageConsoleStream err;
 	
 	static {
 		new Console();
 	}
 	
 	
 	public Console() {
 		if (console == null) {
 			ImageDescriptor imgDesc = EduRideBase
 					.getImageDescriptor(EduRideBase.CAR_RED_IMAGE);
 			console = new MessageConsole(CONSOLE_NAME, imgDesc);
 			out = console.newMessageStream();
 			out.setColor(BLACK);
 			err = console.newMessageStream();
 			err.setColor(RED);
 
 			ConsolePlugin plugin = ConsolePlugin.getDefault();
 			IConsoleManager conMan = plugin.getConsoleManager();
 			conMan.addConsoles(new IConsole[]{console});
 		}
 		
 	}
 
 
 	
 	@Override
 	public void openConsole() {
 		console.activate();
 
 	}
 	
 	
 	
 
 	
 	
 	
 
 	
 	
 
 	
 }
