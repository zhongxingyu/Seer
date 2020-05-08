 package business;
 
 public class Addition extends Operation{
 
 	public Addition(OperationContainer owner, String id) {
 		super(owner, id);
 	}
 
 	@Override
 	public boolean calcResult() {
         double a, b, c;
        a = CalcStack.uniqueInstance().pop();
		b = CalcStack.uniqueInstance().pop();
 		
 		c = a + b;
 		
 		CalcStack.uniqueInstance().push(c);		
 		return true;
 	}
 	
 }
