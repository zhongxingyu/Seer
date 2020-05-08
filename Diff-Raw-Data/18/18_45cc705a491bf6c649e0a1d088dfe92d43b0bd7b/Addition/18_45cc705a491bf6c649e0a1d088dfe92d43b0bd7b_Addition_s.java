 package business;
 
 public class Addition extends Operation{
 
 	public Addition(OperationContainer owner, String id) {
 		super(owner, id);
 	}
 
 	@Override
 	public boolean calcResult() {
         double a, b, c;
        a = (Double) CalcStack.uniqueInstance().pop();
		b = (Double) CalcStack.uniqueInstance().pop();
 		
 		c = a + b;
 		
 		CalcStack.uniqueInstance().push(c);		
 		return true;
 	}
 	
 }
