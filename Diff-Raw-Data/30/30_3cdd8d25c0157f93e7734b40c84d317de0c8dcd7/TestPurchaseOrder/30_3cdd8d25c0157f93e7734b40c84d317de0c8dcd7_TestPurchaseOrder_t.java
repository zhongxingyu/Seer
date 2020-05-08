 package org.megatome.frame2.jaxb;
 
 import java.io.OutputStream;
 import java.math.BigInteger;
 
 import junit.framework.TestCase;
 
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.jaxbgen.Items;
 import org.megatome.frame2.jaxbgen.PurchaseOrder;
 import org.megatome.frame2.jaxbgen.USAddress;
 import org.megatome.frame2.jaxbgen.impl.ItemsImpl;
 import org.megatome.frame2.jaxbgen.impl.PurchaseOrderImpl;
 import org.megatome.frame2.util.Helper;
 
 
 /**
  *
  */
 public class TestPurchaseOrder extends TestCase {
    private final String TARGET_PACKAGE = "org.megatome.frame2.jaxbgen";
 
    public void testUnmarshall_InputStream() throws Exception {
       PurchaseOrder po = unmarshall();
 
       assertNotNull(po);
 
       assertEquals("1999-10-20", Helper.calendarToString(po.getOrderDate()));
 
       USAddress address = po.getShipTo();
 
       assertNotNull(address);
       assertEquals("Alice Smith", address.getName());
 
       Items items = po.getItems();
 
       assertNotNull(items);
       assertEquals(2, items.getItem().size());
    }
 
    public void testMarshall() throws Exception {
       PurchaseOrder po = unmarshall( );
 
       ItemsImpl.ItemTypeImpl item = (ItemsImpl.ItemTypeImpl) po.getItems().getItem().get(0);
 
       item.setComment("This comment has been changed");
 
       OutputStream ostream = Helper.marshall(po,TARGET_PACKAGE,getClass().getClassLoader());
 
       assertTrue(ostream.toString().indexOf("This comment has been changed") > 0);
    }
 
    public void testValidate() throws Exception {
       PurchaseOrderImpl po = (PurchaseOrderImpl) unmarshall( );
 
      Errors errors = Errors.newInstance();
 
       ItemsImpl.ItemTypeImpl item1 = (ItemsImpl.ItemTypeImpl) po.getItems().getItem().get(0);
 
       assertTrue(item1.validate(errors));
       assertEquals(0, errors.size());
 
       item1.setPartNum("AAAAAA");
 
       assertFalse(item1.validate(errors));
       assertEquals(1, errors.size());
 
       item1.setQuantity(new BigInteger("101"));
 
       errors.release();
      errors = Errors.newInstance();
 
       assertFalse(item1.validate(errors));
       assertEquals(2, errors.size());
 
       errors.release();
      errors = Errors.newInstance();
 
       assertFalse(po.validate(errors));
       assertEquals(2, errors.size());
    }
 
    private PurchaseOrder unmarshall( ) throws Exception {
       return (PurchaseOrder) Helper.unmarshall("org/megatome/frame2/jaxb/po.xml",TARGET_PACKAGE,getClass().getClassLoader());
    }
 
 }
