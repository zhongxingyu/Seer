 package ch.codedump.ooplss.test;
 
 import org.junit.Test;
 
 import ch.codedump.ooplss.symbolTable.exceptions.ArgumentDoesntMatchException;
 import ch.codedump.ooplss.utils.ErrorHandler;
 
 
 public class TestArgumentTyping extends OoplssTest {
	
	@Test
	public void testNoArgumentCalling() throws Exception {
		String str = 	"class foo {" +
						"	def func():Void {}" +
						"	def __construct() {" +
						"		func();" +
						"	}" +
						"}";
		this.createRef(str);
		ErrorHandler.getInstance().throwException();
	}

 
 	@Test
 	public void testArgumentRecording() throws Exception {
 		String str = 	"class foo {" +
 						"	def func(x:Int):Void {}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testCallArgumentReferencing() throws Exception {
 		String str = 	"class foo {" +
 						"	def func(x:Int):Void {}" +
 						"	def func2():Void {" +
 						"		func(3);" +
 						"	}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testCallSimpleArgumentType() throws Exception {
 		String str = 	"class foo {" +
 						"	def func(x:Int):Void {}" +
 						"	def func2():Void {" +
 						"		func(3);" +
 						"	}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=ArgumentDoesntMatchException.class)
 	public void testCallIllegalSimpleArgumentType() throws Exception {
 		String str = 	"class foo {" +
 						"	def func(x:Int):Void {}" +
 						"	def func2():Void {" +
 						"		func(\"blah\");" +
 						"	}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test 
 	public void testCallSubtypeArguments() throws Exception {
 		String str = 	"class foo {}" +
 						"class bar subtypeOf foo {" +
 						"	def dd(x:foo):Void {}" +
 						"	def aa():Void {" +
 						"		dd(new bar());" +
 						"	}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();
 	}
 }
