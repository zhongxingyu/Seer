 package com.shealevy.android.model.test.app.test.spec;
 
 import com.shealevy.android.model.AndroidModel;
 import com.shealevy.android.model.TableDelegateField;
 import com.shealevy.android.model.test.app.test.TestTableDelegate;
 
 import android.test.AndroidTestCase;
 
 public class AndroidModelTest extends AndroidTestCase {
 
 	public AndroidModelTest() {
 		super();
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	// Describe get(TableDelegateField<T> field)
 	public void testItReturns0WhenAnUninitializedByteIsRequested() {
 		ByteField b = new ByteField();
 		AndroidModel<TestTableDelegate> model = new AndroidModel<TestTableDelegate>();
 		assertEquals((byte) 0, ((Byte) model.get(b)).byteValue());
 	}
 	
 	public void testItReturnsNullWhenAnUninitializedStringIsRequested() {
 		StringField s = new StringField();
 		AndroidModel<TestTableDelegate> model = new AndroidModel<TestTableDelegate>();
 		assertEquals(null, (String) model.get(s));
 	}
 	
 	private class ByteField implements TableDelegateField<TestTableDelegate> {
		public String getValue() {
 			return "byte";
 		}
 		
		public Class<?> getType() {
 			return byte.class;
 		}
 	}
 	
 	private class StringField implements TableDelegateField<TestTableDelegate> {
		public String getValue() {
 			return "string";
 		}
 		
		public Class<?> getType() {
 			return String.class;
 		}
 	}
 }
