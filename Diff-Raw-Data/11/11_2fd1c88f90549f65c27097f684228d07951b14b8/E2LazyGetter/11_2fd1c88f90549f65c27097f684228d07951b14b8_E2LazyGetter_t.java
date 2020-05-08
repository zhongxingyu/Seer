 package E2;
 import lombok.*;
 
 
 public class E2LazyGetter {
 	@Getter(lazy=true)
 	private final int oldName = 1;
 	public void someMethod(){
 		int oldName;
		/*1: ExtractLocalVariable(5, newName) :1*/
		oldName = 5;
 		/*:1:*/
 		System.out.println(oldName);
 	}
 }
