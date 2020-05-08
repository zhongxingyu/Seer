 package com.advancedpwr.record.methods;
 
 import java.util.ArrayList;
 
 import com.advancedpwr.record.AccessPath;
 import com.advancedpwr.record.BeanRecorderTest;
 import com.advancedpwr.record.examples.CustomList;
 
 /**
  * @author hani
  *         Date: 2/6/12
  *         Time: 10:51 AM
  */
 public class CustomFactoryTest extends BeanRecorderTest
 {
 	protected void setUp()
 	{
 		setWriteFiles();
 		super.setUp();
 	}
 
 	public void testUseCustomFactory()
 	{
     recorder.addBuilderFactory(new MethodWriterFactory() {
       public boolean accept(Class inClass) {
         return inClass == CustomList.class;
       }
 
       public BuildMethodWriter createMethodBuilder(AccessPath inPath) {
         return new CollectionBuilder() {
           @Override
           protected void writeInstance() {
             writeLine(instanceName() + " = new ArrayList()");
           }
         };
       }
     });
     CustomList list = new CustomList(new ArrayList());
     list.add("bar");
 		recorder.record(list);
		assertContains("customlist = new ArrayList();", "customlist.add( \"bar\" );");
 
 	}
 }
