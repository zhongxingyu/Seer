 package dof_xml_handler;
 
 import component.*;
 import entity.*;
 import global.*;
 import junit.framework.*;
 
 import java.math.*;
 import java.util.*;
 
 import org.doframework.*;
 import jdbc_component.JdbcBaseComponent;
 import jdbc_component.JdbcDbUtil;
 
 /**
  * User: gordonju Date: Jan 13, 2008 Time: 10:53:59 PM
  */
 public class InvoiceXmlFactoryTest extends TestCase
 {
 
     InvoiceComponent invoiceComponent = GlobalContext.getComponentFactory().getInvoiceComponent();
     ProductComponent productComponent = GlobalContext.getComponentFactory().getProductComponent();
 
     public void testRequireInvoiceLoadsTwoLevelsOfDependencies()
     {
         DOF.delete("invoice.100.xml");
         assertNull(invoiceComponent.getById(100));
 
         CustomerComponent customerComponent =
                 GlobalContext.getComponentFactory().getCustomerComponent();
         assertNull(customerComponent.getById(25));
 
 
        assertNull(productComponent.getById(14)); // product 2 levels deep is null
 
         Invoice invoice = (Invoice) DOF.require("invoice.100.xml");
         assertNotNull(invoice);
         //assertSame(DOF.require("customer.25.xml"), invoice.getCustomer());
         List<LineItem> lineItems = invoice.getLineItems();
         assertEquals(3, lineItems.size());
 
         LineItem firstLineItem = lineItems.get(0);
         assertEquals(8.99, firstLineItem.getProduct().getPrice().doubleValue());
         assertEquals(new BigDecimal(5), firstLineItem.getQuantity());
         assertEquals(13, firstLineItem.getProduct().getId());
 
         LineItem secondLineItem = lineItems.get(1);
         assertEquals(4.99, secondLineItem.getProduct().getPrice().doubleValue());
         assertEquals(new BigDecimal(8), secondLineItem.getQuantity());
         assertEquals(14, secondLineItem.getProduct().getId());
 
         //assertEquals(8.25, invoice.getTaxRate().doubleValue());
         //assertEquals(15.40, invoice.getShipping().doubleValue());
 
     }
 
     public void testNewInvoiceSubtotal()
     {
         Invoice invoice = (Invoice) DOF.createScratchObject("invoice.scratch.xml");
 ////
 ////        // Get objects needed for test
 ////        Customer johnSmith = (Customer) DOF.require("customer.25.xml");
         Product coffee = (Product) DOF.require("product.13.xml");
         Product tea = (Product) DOF.require("product.14.xml");
 ////
         BigDecimal THREE = new BigDecimal("3");
         BigDecimal TWO = new BigDecimal("2");
 ////
 ////        Invoice invoice = new Invoice();
 //        invoice.setCustomer(johnSmith);
         invoice.addLineItem(THREE, coffee, coffee.getPrice());
         invoice.addLineItem(TWO, tea, tea.getPrice());
 
         // triangulation
        assertEquals(coffee.getPrice()
                .multiply(THREE).add(tea.getPrice().multiply(TWO)), invoice.getSubTotal());
 
         // Test persistence
         invoice.persist();
         Invoice invoiceFromDb =
                 GlobalContext.getComponentFactory().getInvoiceComponent().getById(invoice.getId());
         assertEquals(invoice.getSubTotal(), invoiceFromDb.getSubTotal());
     }
 
 
     public void testNewInvoiceSubtotalWithScratch()
     {
         // Get objects needed for test
         //        Customer johnSmith = (Customer) DOF.require("customer.25.xml");
         //        Product coffee = (Product) DOF.require("product.13.xml");
         //        Product tea = (Product) DOF.require("product.14.xml");
         //
         //        BigDecimal THREE = new BigDecimal("3");
         //        BigDecimal TWO = new BigDecimal("2");
 
         // Dependencies set up by the template file
 
         Invoice invoice = (Invoice) DOF.createScratchObject("invoice.scratch.xml");
 
         List<LineItem> lineItems = invoice.getLineItems();
         BigDecimal originalTotal = new BigDecimal(0);
         for (Iterator<LineItem> lineItemIterator = lineItems.iterator(); lineItemIterator.hasNext();)
         {
             LineItem lineItem = lineItemIterator.next();
             originalTotal = originalTotal.add(lineItem.getQuantity().multiply(lineItem.getPrice()));
         }
 
 
         assertEquals(originalTotal, invoice.getSubTotal());
 
         // Add one item to lineOne
         LineItem lineOne = lineItems.get(0);
         BigDecimal oldQty = lineOne.getQuantity();
         lineOne.setQuantity(oldQty.add(new BigDecimal(1)));
 
         assertEquals(originalTotal.add(lineOne.getPrice()), invoice.getSubTotal());
 
 
         // Test persistence
         invoice.persist();
         Invoice invoiceFromDb = GlobalContext.getComponentFactory().getInvoiceComponent().getById(invoice.getId());
         assertEquals(invoice.getSubTotal(), invoiceFromDb.getSubTotal());
 
 
     }
 
     public void testNewInvoiceSubtotalWithScratchRunNTimes()
     {
         int countInvoicesBefore = JdbcDbUtil.executeSingleIntQuery("select count(*) from invoice");
         int iterations = 3;
         for (int i = 0; i < iterations; i++)
         {
             testNewInvoiceSubtotalWithScratch();
         }
         int countInvoicesAfter = JdbcDbUtil.executeSingleIntQuery("select count(*) from invoice");
         assertEquals(countInvoicesBefore + iterations, countInvoicesAfter);
 
 
     }
 
 
 }
