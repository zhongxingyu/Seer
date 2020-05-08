 package generated.test141;
 
 import junit.framework.TestCase;
 
 import java.util.List;
 
 abstract public cclass AspectA
 {
	pointcut myPt(List lst) : call(* test(List)) && args(lst);
	
	void around(List lst) : myPt(lst) {
 		ADTestCase.result.append(":around testA");
 		proceed(lst);
		List l = lst;
 	}	
 }
 
 
 
