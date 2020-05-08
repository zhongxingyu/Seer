 package challenge_it.racbit.model.reports.generators;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 
 import challenge_it.racbit.model.core.Country;
 import challenge_it.racbit.model.core.Product;
 import challenge_it.racbit.model.core.Product.Group;
 import challenge_it.racbit.model.core.Product.InsurancePackage;
 import challenge_it.racbit.model.core.Product.SupplierType;
 import challenge_it.racbit.model.core.exceptions.CurrencyConversionException;
 import challenge_it.racbit.model.core.exceptions.ReportGenerationException;
 
 public class App {
 
 	/**
 	 * @param args
 	 */
	@SuppressWarnings("deprecation")
 	public static void main(String[] args) {
 
 		LinkedList<Product> list = new LinkedList<Product>();
 		
 		list.add(new Product("www.auto-europe.co.uk", "Lisboa", "Jonny", SupplierType.UNKNOWN, Group.MINI_A, 69.58, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk", "Lisboa", "AV", SupplierType.UNKNOWN, Group.MINI_A, 75.32, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk", "Lisboa", "HZ",  SupplierType.UNKNOWN, Group.MINI_A, 89.68, InsurancePackage.FULLY_REFUNDABLE, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "EU", SupplierType.UNKNOWN, Group.E, 72.60, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "AV", SupplierType.UNKNOWN, Group.C, 89.31, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "HZ", SupplierType.UNKNOWN, Group.C, 93.39, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "AJ", SupplierType.UNKNOWN, Group.C, 105.87, InsurancePackage.NO_EXCESS, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "GU",  SupplierType.UNKNOWN, Group.C, 88.33, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "Interent", SupplierType.UNKNOWN, Group.C, 67.15, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "Go Rent", SupplierType.UNKNOWN, Group.C, 73.2, InsurancePackage.FULLY_REFUNDABLE, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("www.auto-europe.co.uk","Lisboa", "Goldcar", SupplierType.UNKNOWN, Group.C, 68.03, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		
 
 		list.add(new Product("kate","Lisboa", "EU", SupplierType.UNKNOWN, Group.E, 72.60, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("kate","Lisboa", "AV", SupplierType.UNKNOWN, Group.C, 89.31, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 		list.add(new Product("kate","Lisboa", "HZ", SupplierType.UNKNOWN, Group.C, 93.39, InsurancePackage.REGULAR, new Date(Calendar.getInstance().getTimeInMillis()), 3));
 
 		RateShopUKReportGenerator rateShop = new RateShopUKReportGenerator();			
 		
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(2012, Calendar.NOVEMBER, 5);
 		
 		try {
 			rateShop.generate(Calendar.getInstance(), Country.Portugal, list);
 		} catch (ReportGenerationException | CurrencyConversionException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
