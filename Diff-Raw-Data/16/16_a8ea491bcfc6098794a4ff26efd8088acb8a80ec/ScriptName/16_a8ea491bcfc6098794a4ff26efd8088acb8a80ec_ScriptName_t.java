// Based on Geek Explains
// http://geekexplains.blogspot.com/2009/03/getting-class-name-in-static-method-in.html

 public class ScriptName {
 	public static void main(String[] args) {
		System.out.println("Program: " + getProgram());
	}
	public static String getProgram() {
		return (new ProgramGetter()).getClassName();
	}

	public static class ProgramGetter extends SecurityManager {
		public String getClassName() {
			return getClassContext()[1].getName();
		}
 	}
 }
