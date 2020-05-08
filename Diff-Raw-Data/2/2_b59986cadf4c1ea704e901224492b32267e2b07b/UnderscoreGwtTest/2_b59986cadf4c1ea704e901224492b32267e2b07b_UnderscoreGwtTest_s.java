 package org.sgx.underscoregwttest.client;
 
 import org.sgx.jsutil.client.JsArray;
 import org.sgx.jsutil.client.JsFunction;
 import org.sgx.jsutil.client.JsObject;
 import org.sgx.jsutil.client.JsUtil;
 import org.sgx.jsutil.client.TestUtil;
 import org.sgx.underscoregwt.client._;
 import org.sgx.underscoregwt.client.loader.UnderscoreGwtLoader;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.user.client.Window;
 
 /**
  * underscoreGwt library usage example - asynch loading.
  * 
  * @author sg
  * 
  */
 public class UnderscoreGwtTest implements EntryPoint {
 
 	@Override
 	public void onModuleLoad() {
 		UnderscoreGwtLoader.load(new UnderscoreGwtLoader.Listener() {
 			@Override
 			public void success() {
 				TestUtil test = new TestUtil();
 				test1(test);
 				testObjects(test);
 				testArrays(test);
 				Window.alert(test.printAssertsFailed());
 			}
 
 			@Override
 			public void failure(Exception e) {
 				Window.alert("Error: " + e);
 			}
 		});
 	}
 
 	protected void testObjects(TestUtil test) {
 		test.assertTrue(!_.isEqual(JsArray.create("2", "3", "4", "2", "3", "5"), JsArray.create("3", "2", "4", "2", "3", "5")), "testObjects_isEquals_1");
 		test.assertTrue(_.isEqual(JsArray.create("2", "3", "4", "2", "3", "5"), JsArray.create("2", "3", "4", "2", "3", "5")), "testObjects_isEquals_2"); 
 	}
 
 	protected void testArrays(TestUtil test) {
 		JsArray arr1 = JsArray.create("2", "3", "4", "2", "3", "5");
 		JsArray result1 = _.without(arr1, JsArray.create("2", "3"));
 		test.assertTrue(result1.length()==2 && result1.getString(0).equals("4")&& result1.getString(1).equals("5"), "testArrays1");
 		
 		result1 = _.without(arr1, "4", "3");
 //		System.out.println(result1.dump());
 		test.assertTrue(result1.length()==3 && result1.getString(0).equals("2")&& result1.getString(1).equals("2")&& result1.getString(2).equals("5"), "testArrays2");
 		
 		JsArray arr2 = JsArray.create(3,5,7,9);
 		JsArray result2 = _.without(arr2, 3,9);
 		test.assertTrue(result2.length()==2&&result2.getInt(0)==5&&result2.getInt(1)==7, "testArrays3");
 		
 		result2=_.union(JsArray.create(1, 2, 3), JsArray.create(101, 2, 1, 10), JsArray.create(2, 1));
 		test.assertTrue(result2.length()==5&&result2.getInt(0)==1&&result2.getInt(1)==2, "testArrays4");
 		test.assertTrue(_.isEqual(result2, JsArray.create(1,2,3,101,10)),"testArrays5"); 
 		
 		test.assertTrue(_.indexOf(JsArray.create(1.1, 2.2, 3.3), 2.2)==1, "testArrays_indexOf1");
 		test.assertTrue(_.lastIndexOf(JsArray.create(1.1, 2.2, 3.3, 2.2), 2.2)==3, "testArrays_lastIndexOf1");
 		
 		
		_.map(JsArray.create(1, 2, 3), new NN)
 //		_.each(_.range(10)
 	}
 
 	private void test1(TestUtil test) {
 		JsArray arrInt1 = JsArray.create(2, 3, 4);
 		// .getInstance();
 
 		test.assertTrue(_.first(arrInt1).asInteger() == 2, "test1_1");
 		test.assertTrue(_.firstInteger(arrInt1) == 2, "test1_2");
 
 		//calling underscore from js functioncode
 //		JsFunction amazing1 = JsFunction.create("return _.last(_.initial(arguments)); ");
 //		int ret1 = amazing1.applyInteger(arrInt1); 
 //		test.assertTrue(ret1==3, "underscorefromjs"
 		
 		JsArray arrInt2 = _.initial(arrInt1).cast();
 		test.assertTrue(arrInt2.length()==2 && arrInt2.getInt(0)==2 && arrInt2.getInt(1)==3, "test1_3");
 		
 		arrInt2 = _.initial(arrInt1, 2).cast();
 		test.assertTrue(arrInt2.length()==1 && arrInt2.getInt(0)==2, "test1_4");
 		
 	}
 
 }
