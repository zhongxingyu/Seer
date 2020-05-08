 package jdbc_component;
 
 import component.*;
 import entity.*;
 
 import java.util.*;
 import java.math.*;
 
 import global.*;
 
 public class JdbcInvoiceComponent extends JdbcBaseComponent implements InvoiceComponent
 {
 
     public Invoice getById(int invoiceId)
     {
         String sql = "select * from invoice where id = " + invoiceId;
         String[][] rows = JdbcDbUtil.executeMultiColumnQuery(sql);
         if (rows.length == 0)
         {
             return null;
         }
         else
         {
             int id = Integer.parseInt(rows[0][0]);
             int customerId = Integer.parseInt(rows[0][1]);
             CustomerComponent customerComponent =
                     GlobalContext.getComponentFactory().getCustomerComponent();
             Customer customer = customerComponent.getById(customerId);
             Date invoiceDate = JdbcDbUtil.parseDate(rows[0][2]);
             BigDecimal amount = new BigDecimal(rows[0][3]);
             BigDecimal pendingBalance = new BigDecimal(rows[0][4]);
             Invoice invoice = new Invoice(id);
             invoice.setCustomer(customer);
             invoice.setInvoiceDate(invoiceDate);
             invoice.setPendingBalance(pendingBalance);
             List<LineItem> lineItems = getLineItems(invoice);
             invoice.setLineItems(lineItems);
             // Sanity check
             if (!amount.equals(invoice.getSubTotal()))
             {
                 throw new RuntimeException("Invoice stored subtotal "
                                            + amount
                                            + " does not equal calculated "
                                            + invoice.getSubTotal());
             }
             return invoice;
         }
     }
 
     private List<LineItem> getLineItems(Invoice invoice)
     {
         String sql =
                 "select * from line_item where invoice_id = " + invoice.getId() + " order by line_number";
         String[][] rows = JdbcDbUtil.executeMultiColumnQuery(sql);
         List<LineItem> lineItems = new ArrayList<LineItem>(rows.length);
         for (int i = 0; i < rows.length; i++)
         {
             String[] row = rows[i];
             int productId = Integer.parseInt(row[3]);
             Product product = GlobalContext.getComponentFactory().getProductComponent().getById(productId);
             BigDecimal qty = new BigDecimal(row[2]);
             BigDecimal price = new BigDecimal(row[4]);
             LineItem lineItem = new LineItem(qty, product, price, invoice);
             lineItems.add(lineItem);
         }
         return lineItems;
 
 
     }
     public void insert(Invoice invoice) throws DuplicateRecordException
     {
 
         String sql =
                 "insert into invoice (id, customer_id, invoice_date, amount, pending_balance) " +
                 "values (" + invoice.getId() + ", " + invoice.getCustomer().getId() + ", '"
                 + JdbcDbUtil.formatDate(invoice.getInvoiceDate()) + "','"
                 + invoice.getSubTotal() + "', '"
                 + invoice.getPendingBalance() + "')";
         JdbcDbUtil.update(sql);
 
         createLineItems(invoice);
 
     }
 
     private void createLineItems(Invoice invoice)
     {
         List<LineItem> lineItems = invoice.getLineItems();
         int lineNumber = 0;
         for (Iterator<LineItem> lineItemIterator = lineItems.iterator(); lineItemIterator.hasNext();)
         {
             LineItem lineItem = lineItemIterator.next();
             String sql =
                     "insert into line_item(invoice_id, line_number, qty, product_id, price) values ("
                     + invoice.getId()
                     + ", "
                     + lineNumber
                     + ", "
                     + lineItem.getQuantity()
                     + ", "
                     + lineItem.getProduct().getId()
                     + ", "
                     + lineItem.getPrice()
                     + ")";
             JdbcDbUtil.update(sql);
             lineNumber++;
         }
     }
 
     public void update(Invoice invoice) throws DuplicateRecordException
     {
         String sql =
                 "update invoice " +
                 "set amount = '" + invoice.getSubTotal() + "', pending_balance = '" +
                 invoice.getPendingBalance() + "' where id = " + invoice.getId();
         
         JdbcDbUtil.update(sql);
         deleteLineItems(invoice);
 
         createLineItems(invoice);
     }
 
     private void deleteLineItems(Invoice invoice)
     {
         String sqlDeleteLineItems  = "delete from line_item where invoice_id = " + invoice.getId();
         JdbcDbUtil.update(sqlDeleteLineItems);
     }
 
     public boolean delete(Invoice invoice)
     {
        String sql = "delete from line_item where invoice_id = " + invoice.getId();
         int rowCount = JdbcDbUtil.update(sql);
        sql = "delete from invoice where id = " + invoice.getId();
        rowCount = JdbcDbUtil.update(sql);
         return rowCount > 0;
     }
 
 
     /**
      * @return All the invoices for the customer, ordered chronologically
      */
     public List<Invoice> getInvoicesForCustomer(Customer customer)
     {
         String query = "customer_id = " + customer.getId() + " order by invoice_date";
         List<Invoice> result = query(query, customer);
         return result;
     }
 
     /**
      *
      * @param query
      * @param customer optional to avoid refetching customers when creating Invoice records
      * @return
      */
     private List<Invoice> query(String query, Customer customer)
     {
         String sql = "select * from invoice where " + query;
         String[][] data = JdbcDbUtil.executeMultiColumnQuery(sql);
         List<Invoice> invoices = new ArrayList<Invoice>();
         for (int row = 0; row < data.length; row++)
         {
             Invoice invoice = getInvoiceForRow(data[row], customer);
             invoices.add(invoice);
         }
         return invoices; // if none found
 
     }
 
     private Invoice getInvoiceForRow(String[] columns, Customer customer)
     {
         Invoice invoice = new Invoice(Integer.parseInt(columns[0]));
         if (customer == null)
         {
             customer =
                     GlobalContext.getComponentFactory()
                             .getCustomerComponent()
                             .getById(Integer.parseInt(columns[1]));
         }
         invoice.setCustomer(customer);
         String sDate = columns[2];
         Date date = JdbcDbUtil.parseDate(sDate);
         invoice.setInvoiceDate(date);
         invoice.setSubTotal(new BigDecimal(columns[3]));
         invoice.setPendingBalance(new BigDecimal(columns[4]));
         return invoice;
     }
 
     public String getTableName()
     {
         return "invoice";
     }
 
 }
