import java.lang.reflect.InvocationTargetException;

import tools.WinRegistry;
 
 public class main {
	private static String firstRun = null;
 
 	public static void main(String[] args) {
		try {
			firstRun = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE,
					"SOFTWARE\\fifthdimensionsoftware\\Any-Service", "firstRun");
		} catch (IllegalArgumentException | IllegalAccessException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 }
