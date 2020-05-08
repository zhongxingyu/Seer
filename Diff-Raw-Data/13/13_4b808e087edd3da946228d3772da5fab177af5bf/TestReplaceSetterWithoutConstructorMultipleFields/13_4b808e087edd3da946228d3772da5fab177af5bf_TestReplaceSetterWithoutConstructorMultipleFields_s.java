 package object_out;
 
 public class TestReplaceSetterWithoutConstructorMultipleFields {
 	final int i, j, k;

	public TestReplaceSetterWithoutConstructorMultipleFields(int i, int j, int k) {
		this.i = i;
		this.j = j;
		this.k = k;
	}
 	
 	public TestReplaceSetterWithoutConstructorMultipleFields() {
 		this.i = 0;
 		this.j = 0;
 		this.k = 0;
 	}
 	
 	TestReplaceSetterWithoutConstructorMultipleFields setI(int anInt) {
 		return new TestReplaceSetterWithoutConstructorMultipleFields(this.i, anInt, this.k);
 	}
 }
