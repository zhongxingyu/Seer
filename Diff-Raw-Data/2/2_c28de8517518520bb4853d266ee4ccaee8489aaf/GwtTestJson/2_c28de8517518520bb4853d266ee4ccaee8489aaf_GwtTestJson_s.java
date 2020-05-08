 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.totsp.gwittir.serialtest.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.junit.client.GWTTestCase;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  *
  * @author kebernet
  */
 public class GwtTestJson extends GWTTestCase {
     public String getModuleName() {
         return "com.totsp.gwittir.serialtest.JSONSerializerTest";
     }
 
 
     public void testBasic() throws Exception {
         try{
        String json = " { string:'a string', integer: 5} ";
         TestBeanCodec codec = GWT.create(TestBeanCodec.class);
         TestBean b = codec.deserialize(json);
 
         TestChildBean tcb = new TestChildBean();
         tcb.setBooleanProperty(true);
 
         HashSet<TestChildBean> hs = new HashSet<TestChildBean>();
         hs.add(tcb);
         b.setChildSet(hs);
         b.setChild(tcb);
 
         ArrayList<Integer> intList = new ArrayList<Integer>();
         for(int i=0; i< 20;i++ ){
             intList.add(i);
         }
         b.setIntegerList(intList);
 
         String ser = codec.serialize(b);
 
         TestBean b2 = codec.deserialize(ser);
         System.out.println( ser );
         System.out.println( codec.serialize(b2));
         assertEquals( b, b2 );
 
         } catch(Throwable e){
             while(e.getCause() != null ){
                 e = e.getCause();
             }
             e.printStackTrace();
         }
     }
 
 }
