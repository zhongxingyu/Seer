 /*
  * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
  * All rights reserved. This material contains unpublished, copyrighted
  * work including confidential and proprietary information of Percussion.
  *
  * test.percussion.pso.utils RxItemUtilsTest.java
  *
  */
 package test.percussion.pso.utils;
 
 import static org.junit.Assert.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.percussion.cms.PSCmsException;
 import com.percussion.cms.objectstore.IPSItemAccessor;
 import com.percussion.cms.objectstore.PSBinaryValue;
 import com.percussion.cms.objectstore.PSItemField;
 import com.percussion.cms.objectstore.PSItemFieldMeta;
 import com.percussion.pso.utils.RxItemUtils;
 
 public class RxItemUtilsTest
 {
    private static Log log = LogFactory.getLog(RxItemUtilsTest.class); 
    
    Mockery context; 
    
    @Before
    public void setUp() throws Exception
    {
       context = new Mockery(){{ setImposteriser(ClassImposteriser.INSTANCE);}};    
    }
    
    @Test
    public final void testIsBinaryFieldTrue()
    {
       final IPSItemAccessor item = context.mock(IPSItemAccessor.class);
       final PSItemField fld = context.mock(PSItemField.class);
       final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
       
       context.checking(new Expectations(){{
          one(item).getFieldByName("a");
          will(returnValue(fld));
          one(fld).getItemFieldMeta();
          will(returnValue(meta));
         one(meta).getFieldValueType();
          will(returnValue(PSItemFieldMeta.DATATYPE_BINARY));
       }});
       boolean result = RxItemUtils.isBinaryField(item, "a");
       assertTrue(result); 
       context.assertIsSatisfied();
    }
    
    @Test
    public final void testIsBinaryFieldFalse()
    {
       final IPSItemAccessor item = context.mock(IPSItemAccessor.class);
       final PSItemField fld = context.mock(PSItemField.class);
       final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
       
       context.checking(new Expectations(){{
          one(item).getFieldByName("a");
          will(returnValue(fld));
          one(fld).getItemFieldMeta();
          will(returnValue(meta));
         one(meta).getFieldValueType();
          will(returnValue(PSItemFieldMeta.DATATYPE_TEXT));
       }});
       boolean result = RxItemUtils.isBinaryField(item, "a");
       assertFalse(result);
       context.assertIsSatisfied();
    }
    
    
    @Test
    public final void testGetFieldBinary()
    {
       final IPSItemAccessor item = context.mock(IPSItemAccessor.class);
       final PSItemField fld = context.mock(PSItemField.class);
       final PSBinaryValue value = context.mock(PSBinaryValue.class);
       final byte[] myArray = new byte[100]; 
       try
       {
          context.checking(new Expectations(){{
             one(item).getFieldByName("a");
             will(returnValue(fld));
             one(fld).getValue();
             will(returnValue(value));
             one(value).getValue();
             will(returnValue(myArray));
          }});
          
          Object o = RxItemUtils.getFieldBinary(item, "a"); 
          assertNotNull(o);
          context.assertIsSatisfied();
       } catch (PSCmsException ex)
       {
          log.error("Unexpected Exception " + ex,ex);
          fail("exception");
       }
    }
 }
