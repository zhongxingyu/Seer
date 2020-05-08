 package syntax;
 
 
 public class Application extends Expression{
 	Expression func;
 	Expression param;
 
 	public String toString(){
 		return "(" + func.toString() + " " + param.toString() + ")";
 	}
 	
 	public Application(Object yysv, Object yysv2){
 		func = (Expression) yysv;
 		param = (Expression) yysv2;
 	}
 	
 	public Object eval() {
 		AnonymousFunction finalFunc=null;
 		try{
 			finalFunc = (AnonymousFunction) func.eval();
 		}catch (Exception e) {
 			System.out.println("Type Error!");
 		}
 		Object result = finalFunc.apply(param);
 		return result;
 	}
 }
