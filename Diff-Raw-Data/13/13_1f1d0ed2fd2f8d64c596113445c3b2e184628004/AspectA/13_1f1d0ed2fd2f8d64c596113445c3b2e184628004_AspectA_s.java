 package generated.test141;
 
 import junit.framework.TestCase;
 
 import java.util.List;
 
 abstract public cclass AspectA
 {
	void around(List lst) : call(* test(List)) && args(lst) {
 		ADTestCase.result.append(":around testA");
 		proceed(lst);
 	}	
 }
 
 
 
