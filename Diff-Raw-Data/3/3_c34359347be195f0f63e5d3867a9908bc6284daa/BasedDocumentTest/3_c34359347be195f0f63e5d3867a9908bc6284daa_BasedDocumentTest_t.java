 package com.keebraa.docs.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 
 import com.keebraa.docs.exceptions.DocumentHandlingException;
 import com.keebraa.docs.model.impl.NewState;
 import com.keebraa.docs.model.stub.SomeBasedDocumentStub;
 import com.keebraa.docs.model.stub.SutDocumentTestStub;
 
 public class BasedDocumentTest
 {
     @Test(expected=DocumentHandlingException.class)
     public void testConstructionWithNullBaseDoc() throws Exception
     {
         new SomeBasedDocumentStub(null);
     }
     
     @Test
     public void testNormalConstruction() throws Exception
     {
         Document doc = new SomeBasedDocumentStub(new SutDocumentTestStub());
         assertNotNull(doc.getState());
         assertEquals(NewState.CAPTION, doc.getState().getStateCaption());
    }  
 }
 
 
