 package log;
 
 public class PrintLogger extends Logger {
 
 	@Override
 	public void logProgramStart(String programName, String args, String whitelist, String blacklist) {
 		// TODO Auto-generated method stub
 		System.out.println("Program started with name [" + programName + "] and args [" + args + "]");
 		System.out.println("Whitelist: " + whitelist);
 		System.out.println("Blacklist: " + blacklist);
 		System.out.println("=============================");
 	}
 
 	@Override
 	public void logVarCreated(String name, String type) {
 		// TODO Auto-generated method stub
 		System.out.println("\tVariable created: [" + type + "] " + name );
 	}
 
 	@Override
 	public void logVarChanged(String var, String value) {
 		// TODO Auto-generated method stub
		System.out.println("\t>>> " + var + " = " + value);
 	}
 
 	@Override
 	public void logVarDeath(String var) {
 		// TODO Auto-generated method stub
 		System.out.println("\tVariable death: " + var);
 	}
 
 	@Override
 	public void logVarUsed(String var) {
 		// TODO Auto-generated method stub
 		System.out.println("\tVariable used: " + var);
 	}
 
 	@Override
 	public void logProgramExit(int exitCode, String exception) {
 		// TODO Auto-generated method stub
 		System.out.println("Program exited with code " + exitCode + " [" + exception + "]");
 	}
 
 	@Override
 	public void logLines(String filePath, int lineNum) {
 		// TODO Auto-generated method stub
 		System.out.println("\nLine executed: " + filePath + ":" + lineNum);
 	}
 
 
 }
