 package base;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 import org.jfree.data.general.DefaultPieDataset;
 import org.jfree.data.general.PieDataset;
 import org.jfree.data.xy.IntervalXYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public class EshopV2 extends Eshop {
 	
 	private boolean isSameDay(Date d1, Date d2) {
 		Calendar c1 = new GregorianCalendar();
 		c1.setTime(d1);
 		Calendar c2 = new GregorianCalendar();
 		c2.setTime(d2);
 		
 		if(c1.get(Calendar.YEAR)  == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean isBeforeDay(Date d1, Date comapre_to) {
 		Calendar c1 = new GregorianCalendar();
 		c1.setTime(d1);
 		Calendar c2 = new GregorianCalendar();
 		c2.setTime(comapre_to);
 		
 		if(c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR) || (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) < c2.get(Calendar.DAY_OF_YEAR)) ) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public double getTotalOrdersAmountByDate(Date date) {
 		double amount = 0;
 		
 		Iterator<Order> ordersIterator = this.getOrders().iterator();
         while (ordersIterator.hasNext()) {
         	Order order = ordersIterator.next();
         	
         	if(isSameDay(order.getCreatedAt(), date)) {
         		amount = amount + order.getTotalPrice();
         	}
         	
         	if(isBeforeDay(order.getCreatedAt(), date)) {
         		break;
         	}
         }
         
 		return amount;
 	}
 	
 	public PieDataset availableQuantityDataset() {
         DefaultPieDataset result = new DefaultPieDataset();
         
         Iterator<Product> productsIterator = this.getProducts().iterator();
 
 		while (productsIterator.hasNext()) {
 			Product product = productsIterator.next();
 			result.setValue(product.getName(), product.getQuantity());
 		}
         return result;
     }
 	
 	public IntervalXYDataset weekSalesDataset() {
        XYSeries series = new XYSeries("Day sales");
         
         Calendar cal = new GregorianCalendar();
         cal.add(Calendar.DAY_OF_MONTH, -7);
         
         int i;
         for(i=1; i <= 7; i++) {
         	cal.add(Calendar.DAY_OF_MONTH, 1);
     		series.add(cal.get(Calendar.DAY_OF_MONTH), getTotalOrdersAmountByDate(cal.getTime()));
         }
         
         
         XYSeriesCollection dataset = new XYSeriesCollection(series);
         return dataset;
     }
 	
 	
 }
