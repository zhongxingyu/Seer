 
 public class A {
 
	public String meth1(String param1, Integer param2) {
 
		System.out.println(param1);
 
		return meth2("this is an arg to method2", 25);
	}
 
	public String meth2(String msg, Integer i) {
		return "class A::meth2";
	}
 }
