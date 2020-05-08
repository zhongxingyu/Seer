 import static com.joeylawrance.language.Regular.*;

 import com.joeylawrance.language.Parser;
 
 public class RegexTest {
 
 	public static void main(String[] args) {
 		long before = System.currentTimeMillis();
 		Parser r = catenation(times(alpha(),3,8), optional(digit()), string("@bridgew.edu"));
 		System.out.println(r);
 		System.out.println(r.recognize("somebody@bridgew.edu"));
 		System.out.println(r.recognize("somebody@wit.edu"));
 		System.out.println(System.currentTimeMillis() - before); // 183ms is bad
 		r = catenation(not(symbol('\n')), string("\n"));
 		System.out.println(r);
 		System.out.println(r.recognize("abc\n\n"));
 		r = difference(alnum(),digit());
 		System.out.println(r);
 		System.out.println(r.recognize("a"));
		
 	}
 
 }
