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
	public void testItReturns0WhenAByteIsRequested() {
 		ByteField b = new ByteField();
 		AndroidModel<TestTableDelegate> model = new AndroidModel<TestTableDelegate>();
		assertEquals((byte) 0, model.get(b));
 	}
 	
 	private class ByteField implements TableDelegateField<TestTableDelegate> {
 		public String getValue() {
 			return "byte";
 		}
 		
 		public Class<?> getType() {
 			return byte.class;
 		}
 	}
 }
