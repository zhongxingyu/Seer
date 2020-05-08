 package object_out;
 
 public class TestUninitializedFieldsIntoFinal {
 	
 	final int a = 0;
 	final int b = 0, c = 0;
 	
 	// Check non-ints
 	final boolean d = false;
 	final byte e = 0;
 	final char f = '\0'; 
 	final float g = 0.0f;
 	final double h = 0.0d;
 	final short i = 0;
	final long j = 0;
 	final String k = "";
 	final Object l = null;
 	
 	// Check that it does not destroy the present initialization in a list where only one is initialized
 	final int m = 0, n = 42;
 	
 	// Check that it does not initialize values that are initialized in a constructor
 	final int o;
 	TestUninitializedFieldsIntoFinal(int o) {
 		this.o = o;
 	}
 	
 	// Check that it adds an initialization of o to a second constructor so that it is initialized in all cases
 	TestUninitializedFieldsIntoFinal(float irrelevant) {
 		this.o = 0;	
 	}
 }
