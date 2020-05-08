 package business;
 
 import java.util.*;
 
 public class CalcStack {
 //	static boolean hasInstance = false;
 	static CalcStack theStack = null;
 	
 	private ArrayList<Object> operandList = null;
 	static int idx = 0;
 
 	private CalcStack(){	
 		operandList = new ArrayList<Object>();
 	}
 	
 	public static CalcStack uniqueInstance(){
 		if( theStack == null ) {
 		   theStack = new CalcStack();			
 		}
 		return theStack;		
 	}
 	
 	public boolean push(String string){
 		try { 
			return push(new Double(string));			
 		} catch (NumberFormatException e) {
 			return false;
 		} 		
 	}
 
 	public boolean push(double dbl) {
 		return push(new Double(dbl));
 	}	
 	
 	private boolean push(Object o) {	
 		   operandList.add( o );
 		   ++idx;
 		   return true;
 	}
 	
 	public double pop() {
 		try {
 		   Double tmpDouble = (Double) operandList.remove(--idx);
 		   return tmpDouble.doubleValue();
 		} catch (ArrayIndexOutOfBoundsException e) {
 		   return 0;
 		}		
 	}
 
 	public void clearList(){
 		operandList.clear();
 	}
 	
 	@Override
 	public String toString() {	
 		return operandList + " ";
 	}
 }
