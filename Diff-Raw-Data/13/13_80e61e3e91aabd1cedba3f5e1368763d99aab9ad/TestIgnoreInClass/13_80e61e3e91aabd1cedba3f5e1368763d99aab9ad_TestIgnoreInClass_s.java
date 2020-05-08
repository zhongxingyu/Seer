 package cdoclet.test;
 
 /**
 * @java.class def
 * @as.class asd
 * @cs.class ghi
  */
 public class TestIgnoreInClass<T> extends TestIgnoreInClassBase<T> {
 // --------------------- Interface TestOverrideInterfaceBase ---------------------
 
 	@Override
 	public T getTest() {
 		return super.getTest();
 	}
 
 	@Override
 	public void testMethod() {
 	}
 }
