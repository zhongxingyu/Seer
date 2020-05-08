 package E2;
 
 import lombok.val;
 
 public class E2Val0 {
 		public void someMethod(){
 			String oldName = "Able was I ere I saw Elba";
			/*1: ExtractLocalVariable(oldName, newName) :1*/
 			val name = new String(oldName);
 			/*:1:*/
 			System.out.println(name);
 		}
 }
