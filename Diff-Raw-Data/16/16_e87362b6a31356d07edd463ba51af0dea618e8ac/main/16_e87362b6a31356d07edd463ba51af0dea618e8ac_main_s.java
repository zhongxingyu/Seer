 
 public class main {
	private static String firstRun;
 
 	public static void main(String[] args) {
	WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\fifthdimensionsoftware\\Any-Service", firstRun);
 	}
 
 }
