 package thinkingInJava.ch10;
 
 public class Outer {
 
 	protected class Inner implements Content {
 
 		private String str;
 		
		@Override
 		public String value() {
 			return null;
 		}
 	}
 	
 	public void t(){
 		String s=new Inner().str;
 	}
 
 }
 
 class Child extends Outer{
 	
 	public Content get(){
 //		Outer p=new Outer();
 //		return p.new Inner();
 		return new Inner();
 	}
 }
