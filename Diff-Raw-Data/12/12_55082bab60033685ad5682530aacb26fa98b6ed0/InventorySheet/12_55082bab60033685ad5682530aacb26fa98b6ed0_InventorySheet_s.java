 package common.entity.inventorysheet;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import common.entity.accountreceivable.ARPayment;
 import common.entity.accountreceivable.AccountReceivable;
 import common.entity.accountreceivable.AccountReceivableDetail;
 import common.entity.cashadvance.CAPayment;
 import common.entity.cashadvance.CashAdvance;
 import common.entity.dailyexpenses.DailyExpenses;
 import common.entity.dailyexpenses.DailyExpensesType;
 import common.entity.delivery.Delivery;
 import common.entity.delivery.DeliveryDetail;
 import common.entity.deposit.Deposit;
 import common.entity.discountissue.DiscountIssue;
 import common.entity.profile.Account;
 import common.entity.pullout.PullOut;
 import common.entity.pullout.PullOutDetail;
 import common.entity.salary.SalaryRelease;
 import common.entity.sales.Sales;
 import common.entity.sales.SalesDetail;
 
 public class InventorySheet implements InventorySheetManager {
 
 	public static final String OVER_CAPS = "OVER";
 	public static final String SHORT_CAPS = "SHORT";
 	public static final String OVER_SHORT_CAPS = "OVER/SHORT";
 
 	public static final String OVER = "Over";
 	public static final String SHORT = "Short";
 	public static final String OVER_SHORT = "Over/Short";
 
 	// include all is related transactions here!!
 
 	private InventorySheetData inventorySheetData;
 	private Map<Integer, InventorySheetDetail> productInventories = new HashMap<Integer, InventorySheetDetail>();
 	private int maxId = 0;
 
 	public InventorySheet() {
 		super();
 	}
 
 	public InventorySheet(InventorySheetData inventorySheetData) {
 		super();
 		this.inventorySheetData = inventorySheetData;
 		build();
 	}
 
 	// ///////////////////////////////// methods from inventory sheet data
 
 	public int getId() {
 		return inventorySheetData.getId();
 	}
 
 	public Date getDate() {
 		return inventorySheetData.getDate();
 	}
 
 	public void setDate(Date date) {
 		inventorySheetData.setDate(date);
 	}
 
 	public double getPreviousAcoh() {
 		return inventorySheetData.getPreviousAcoh();
 	}
 
 	public void setPreviousAcoh(double previousAcoh) {
 		inventorySheetData.setPreviousAcoh(previousAcoh);
 	}
 
 	@Override
 	public double getOverAmount() {
 		return inventorySheetData.getOverAmount();
 	}
 
 	public void setOverAmount(double overAmount) {
 		inventorySheetData.setOverAmount(overAmount);
 	}
 
 	@Override
 	public double getShortAmount() {
 		return inventorySheetData.getShortAmount();
 	}
 
 	public void setShortAmount(double shortAmount) {
 		inventorySheetData.setShortAmount(shortAmount);
 	}
 
 	public Account getIssuedBy() {
 		return inventorySheetData.getIssuedBy();
 	}
 
 	public void setIssuedBy(Account issuedBy) {
 		inventorySheetData.setIssuedBy(issuedBy);
 	}
 
 	public String getRemarks() {
 		return inventorySheetData.getRemarks();
 	}
 
 	public void setRemarks(String remarks) {
 		inventorySheetData.setRemarks(remarks);
 	}
 
 	// //////////////////////////////////
 
 	public InventorySheet(InventorySheetData inventorySheetData, Set<Delivery> deliveries, Set<PullOut> pullOuts, Set<Sales> sales,
 			Set<AccountReceivable> accountReceivables, Set<DiscountIssue> discountIssues, Set<ARPayment> arPayments, Set<CAPayment> caPayments,
 			Set<DailyExpenses> dailyExpenses, Set<CashAdvance> cashAdvances, Set<SalaryRelease> salaryReleases, Set<Deposit> deposits) {
 		super();
 		this.inventorySheetData = inventorySheetData;
 		build(deliveries, pullOuts, sales, accountReceivables, discountIssues, arPayments, caPayments, dailyExpenses, cashAdvances, salaryReleases,
 				deposits);
 	}
 
 	private List<InventorySheetDetail> initInventorySheetDetails() {
 		Set<InventorySheetDataDetail> inventorySheetDataDetails = inventorySheetData.getInventorySheetDataDetails();
 		List<InventorySheetDetail> inventorySheetDetails = new ArrayList<InventorySheetDetail>();
 		for (InventorySheetDataDetail isdd : inventorySheetDataDetails) {
 			inventorySheetDetails.add(new InventorySheetDetail(isdd));
 		}
 		return inventorySheetDetails;
 	}
 
 	private void init(List<InventorySheetDetail> inventorySheetDetails) {
 		for (InventorySheetDetail pi : inventorySheetDetails) {
 			this.productInventories.put(pi.getProduct().getId(), pi);
 			if (pi.getProduct().getId() > maxId)
 				maxId = pi.getProduct().getId();
 		}
 	}
 
 	public void build(Set<Delivery> deliveries, Set<PullOut> pullOuts, Set<Sales> sales, Set<AccountReceivable> accountReceivables,
 			Set<DiscountIssue> discountIssues, Set<ARPayment> arPayments, Set<CAPayment> caPayments, Set<DailyExpenses> dailyExpenses,
 			Set<CashAdvance> cashAdvances, Set<SalaryRelease> salaryReleases, Set<Deposit> deposits) {
 		init(initInventorySheetDetails());
 		addDeliveriesToProductInventory(deliveries);
 		addPullOutsToProductInventory(pullOuts);
 		addSalesToProductInventory(sales);
 		addAccountReceivablesToProductInventory(accountReceivables);
 		inventorySheetData.setDeliveries(deliveries);
 		inventorySheetData.setPullouts(pullOuts);
 		inventorySheetData.setSales(sales);
 		inventorySheetData.setAccountReceivables(accountReceivables);
 		inventorySheetData.setDiscountIssues(discountIssues);
 		inventorySheetData.setArPayments(arPayments);
 		inventorySheetData.setCaPayments(caPayments);
 		inventorySheetData.setDailyExpenses(dailyExpenses);
 		inventorySheetData.setCashAdvances(cashAdvances);
 		inventorySheetData.setSalaryReleases(salaryReleases);
 		inventorySheetData.setDeposits(deposits);
 	}
	
	public void finalize(){
		
 	}
 
 	public void build() {
 		init(initInventorySheetDetails());
 		addDeliveriesToProductInventory(inventorySheetData.getDeliveries());
 		addPullOutsToProductInventory(inventorySheetData.getPullouts());
 		addSalesToProductInventory(inventorySheetData.getSales());
 		addAccountReceivablesToProductInventory(inventorySheetData.getAccountReceivables());
 	}
 
 	private void addDeliveriesToProductInventory(Set<Delivery> deliveries) {
 		for (Delivery d : deliveries) {
 			inventorySheetData.addDelivery(d);
 			Set<DeliveryDetail> deliveryDetails = d.getDeliveryDetails();
 			for (DeliveryDetail dd : deliveryDetails) {
 				InventorySheetDetail pi = productInventories.get(dd.getProduct().getId());
 				pi.setDeliveriesInKilo(pi.getDeliveriesInKilo() + dd.getQuantityInKilo());
 				pi.setDeliveriesInSack(pi.getDeliveriesInSack() + dd.getQuantityInSack());
 			}
 		}
 	}
 
 	private void addPullOutsToProductInventory(Set<PullOut> pullOuts) {
 		for (PullOut po : pullOuts) {
 			inventorySheetData.addPullOut(po);
 			Set<PullOutDetail> pullOutDetails = po.getPullOutDetails();
 			for (PullOutDetail pod : pullOutDetails) {
 				InventorySheetDetail pi = productInventories.get(pod.getProduct().getId());
 				pi.setPullOutsInKilo(pi.getPullOutsInKilo() + pod.getQuantityInKilo());
 				pi.setPullOutsInSack(pi.getPullOutsInSack() + pod.getQuantityInSack());
 			}
 		}
 	}
 
 	private void addSalesToProductInventory(Set<Sales> sales) {
 		for (Sales s : sales) {
 			inventorySheetData.addSales(s);
 			Set<SalesDetail> salesDetails = s.getSalesDetails();
 			for (SalesDetail sd : salesDetails) {
 				InventorySheetDetail pi = productInventories.get(sd.getProduct().getId());
 				pi.setCashAndCheckOffTakeInSack(pi.getCashAndCheckOffTakeInSack() + sd.getQuantityInSack());
 				pi.setCashAndCheckOffTakeInKilo(pi.getCashAndCheckOffTakeInKilo() + sd.getQuantityInKilo());
 				// pi.setOffTakeInKilo(pi.getOffTakeInKilo() +
 				// sd.getQuantityInKilo());
 				// pi.setOffTakeInSack(pi.getOffTakeInSack() +
 				// sd.getQuantityInSack());
 			}
 		}
 	}
 
 	private void addAccountReceivablesToProductInventory(Set<AccountReceivable> accountReceivables) {
 		for (AccountReceivable ar : accountReceivables) {
 			inventorySheetData.addAccountReceivable(ar);
 			Set<AccountReceivableDetail> arDetails = ar.getAccountReceivableDetails();
 			for (AccountReceivableDetail ard : arDetails) {
 				InventorySheetDetail pi = productInventories.get(ard.getProduct().getId());
 				pi.setAccountReceivableOffTakeInSack(pi.getAccountReceivableOffTakeInSack() + ard.getQuantityInSack());
 				pi.setAccountReceivableOffTakeInKilo(pi.getAccountReceivableOffTakeInKilo() + ard.getQuantityInKilo());
 				// pi.setOffTakeInKilo(pi.getOffTakeInKilo() +
 				// ard.getQuantityInKilo());
 				// pi.setOffTakeInSack(pi.getOffTakeInSack() +
 				// ard.getQuantityInSack());
 			}
 		}
 	}
 
 	public int getMaxId() {
 		return maxId;
 	}
 
 	@Override
 	public double getBeginningInventoryInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getBeginningInventoryInSack();
 		return 0d;
 	}
 
 	@Override
 	public double getBeginningInventoryInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getBeginningInventoryInKilo();
 		return 0d;
 	}
 
 	@Override
 	public double getTotalBeginningInventoryInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getBeginningInventoryInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalBeginningInventoryInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getBeginningInventoryInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getOnDisplayInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getOnDisplayInSack();
 		return 0d;
 	}
 
 	@Override
 	public double getOnDisplayInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getOnDisplayInKilo();
 		return 0d;
 	}
 
 	@Override
 	public double getTotalOnDisplayInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getOnDisplayInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalOnDisplayInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getOnDisplayInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getDeliveriesInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getDeliveriesInSack();
 		return 0d;
 	}
 
 	@Override
 	public double getDeliveriesInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getDeliveriesInKilo();
 		return 0d;
 	}
 
 	@Override
 	public double getTotalDeliveriesInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getDeliveriesInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalDeliveriesInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getDeliveriesInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallCostOfDeliveries() {
 		double total = 0d;
 		for (Delivery d : inventorySheetData.getDeliveries()) {
 			total += d.getDeliveryAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getPulloutsInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getPullOutsInSack();
 		return 0d;
 	}
 
 	@Override
 	public double getPulloutsInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getPullOutsInKilo();
 		return 0d;
 	}
 
 	@Override
 	public double getTotalPulloutsInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getPulloutsInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalPulloutsInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getPulloutsInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallCostOfPullouts() {
 		double total = 0d;
 		for (PullOut po : inventorySheetData.getPullouts()) {
 			total += po.getPulloutAmount();
 		}
 		return total;
 	}
 
 	/*
 	 * beginning inventory - on display + deliveries - pullouts
 	 */
 	@Override
 	public double getEndingInventoryInSackForProduct(int productId) {
 		return ((getBeginningInventoryInSackForProduct(productId) - getOnDisplayInSackForProduct(productId)) + getDeliveriesInSackForProduct(productId))
 				- getPulloutsInSackForProduct(productId);
 	}
 
 	/*
 	 * beginning inventory - on display + deliveries - pullouts
 	 */
 	@Override
 	public double getEndingInventoryInKiloForProduct(int productId) {
 		return ((getBeginningInventoryInKiloForProduct(productId) - getOnDisplayInKiloForProduct(productId)) + getDeliveriesInKiloForProduct(productId))
 				- getPulloutsInKiloForProduct(productId);
 	}
 
 	@Override
 	public double getTotalEndingInventoryInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getEndingInventoryInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalEndingInventoryInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getEndingInventoryInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getOfftakesInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getOffTakeInSack();
 		return 0d;
 	}
 
 	@Override
 	public double getOfftakesInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getOffTakeInKilo();
 		return 0d;
 	}
 
 	@Override
 	public double getTotalOfftakesInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getOfftakesInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalOfftakesInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getOfftakesInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getPricePerSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getPricePerSack();
 		return 0d;
 	}
 
 	@Override
 	public double getPricePerKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getPricePerKilo();
 		return 0d;
 	}
 
 	@Override
 	public double getTotalPricesInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getPricePerSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getTotalPricesInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getPricePerKiloForProduct(i);
 		}
 		return total;
 	}
 
 	/*
 	 * sales (cash and check) + account receivables
 	 */
 	@Override
 	public double getCombinedSalesAmountInSackForProduct(int productId) {
 		return getCashAndCheckSalesAmountInSackForProduct(productId) + getAccountReceivablesAmountInSackForProduct(productId);
 	}
 
 	/*
 	 * sales (cash and check) + account receivables
 	 */
 	@Override
 	public double getCombinedSalesAmountInKiloForProduct(int productId) {
 		return getCashAndCheckSalesAmountInKiloForProduct(productId) + getAccountReceivablesAmountInKiloForProduct(productId);
 	}
 
 	@Override
 	public double getCombinedSalesAmountInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getCombinedSalesAmountInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getCombinedSalesAmountInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getCombinedSalesAmountInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallCombinedSalesAmount() {
 		return getCombinedSalesAmountInSack() + getCombinedSalesAmountInKilo();
 	}
 
 	@Override
 	public double getCashAndCheckSalesAmountInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getCashAndCheckOffTakeInSackAmount();
 		return 0d;
 	}
 
 	@Override
 	public double getCashAndCheckSalesAmountInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getCashAndCheckOffTakeInKiloAmount();
 		return 0d;
 	}
 
 	@Override
 	public double getCashAndCheckSalesAmountInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getCashAndCheckSalesAmountInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getCashAndCheckSalesAmountInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getCashAndCheckSalesAmountInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallCashAndCheckSalesAmount() {
 		double total = 0d;
 		Set<Sales> sales = inventorySheetData.getSales();
 		for (Sales s : sales) {
 			total += s.getSalesAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallAccountReceivables() {
 		double total = 0d;
 		Set<AccountReceivable> ars = inventorySheetData.getAccountReceivables();
 		for (AccountReceivable ar : ars) {
 			total += ar.getAccountReceivablesAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallAccountReceivablesPayments() {
 		double total = 0d;
 		Set<ARPayment> ars = inventorySheetData.getArPayments();
 		for (ARPayment ar : ars) {
 			total += ar.getAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallCashAdvances() {
 		double total = 0d;
 		Set<CashAdvance> cas = inventorySheetData.getCashAdvances();
 		for (CashAdvance ca : cas) {
 			total += ca.getAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallCashAdvancesPayments() {
 		double total = 0d;
 		Set<CAPayment> caps = inventorySheetData.getCaPayments();
 		for (CAPayment cap : caps) {
 			total += cap.getAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallPersonalExpenses() {
 		double total = 0d;
 		Set<DailyExpenses> des = inventorySheetData.getDailyExpenses();
 		for (DailyExpenses de : des) {
 			if (de.getExpenseType().getName().equals(DailyExpensesType.personal))
 				total += de.getDailyExpensesAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallStoreExpenses() {
 		double total = 0d;
 		Set<DailyExpenses> des = inventorySheetData.getDailyExpenses();
 		for (DailyExpenses de : des) {
 			if (de.getExpenseType().getName().equals(DailyExpensesType.store))
 				total += de.getDailyExpensesAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallExpenses() {
 		double total = 0d;
 		Set<DailyExpenses> des = inventorySheetData.getDailyExpenses();
 		for (DailyExpenses de : des) {
 			total += de.getDailyExpensesAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallSalaryReleases() {
 		double total = 0d;
 		Set<SalaryRelease> srs = inventorySheetData.getSalaryReleases();
 		for (SalaryRelease sr : srs) {
 			total += sr.getNetAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getOverallDiscounts() {
 		double total = 0d;
 		Set<DiscountIssue> dis = inventorySheetData.getDiscountIssues();
 		for (DiscountIssue di : dis) {
 			total += di.getAmount();
 		}
 		return total;
 
 	}
 
 	@Override
 	public double getOverallDeposits() {
 		double total = 0d;
 		Set<Deposit> ds = inventorySheetData.getDeposits();
 		for (Deposit d : ds) {
 			total += d.getAmount();
 		}
 		return total;
 	}
 
 	@Override
 	public double getPreviousCashOnHand() {
 		return inventorySheetData.getPreviousAcoh();
 	}
 
 	@Override
 	public double getTotalAssets() {
 		return getPreviousAcoh() + getOverallCombinedSalesAmount() + getOverallAccountReceivables() + getOverallAccountReceivablesPayments()
 				+ getOverallCashAdvancesPayments();
 	}
 
 	@Override
 	public double getTotalLiabilities() {
 		return getOverallExpenses() + getOverallSalaryReleases() + getOverallCashAdvances() + getOverallAccountReceivables() + getOverallDiscounts()
 				+ getOverallDeposits();
 	}
 
 	@Override
 	public double getActualCashOnHand() {
 		return (getPreviousCashOnHand() + getTotalAssets()) - getTotalLiabilities();
 	}
 
 	@Override
 	public double getActualCashCount() {
 		return inventorySheetData.getActualCashCount();
 	}
 
 	@Override
 	public Set<Delivery> getDeliveries() {
 		return inventorySheetData.getDeliveries();
 	}
 
 	@Override
 	public Set<PullOut> getPullOuts() {
 		return inventorySheetData.getPullouts();
 	}
 
 	@Override
 	public Set<Sales> getSales() {
 		return inventorySheetData.getSales();
 	}
 
 	@Override
 	public double getAccountReceivablesAmountInSackForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getAccountReceivableOffTakeInSackAmount();
 		return 0d;
 	}
 
 	@Override
 	public double getAccountReceivablesAmountInKiloForProduct(int productId) {
 		InventorySheetDetail isd = productInventories.get(productId);
 		if (isd != null)
 			return isd.getAccountReceivableOffTakeInKiloAmount();
 		return 0d;
 	}
 
 	@Override
 	public double getAccountReceivablesAmountInSack() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getAccountReceivablesAmountInSackForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public double getAccountReceivablesAmountInKilo() {
 		double total = 0d;
 		for (int i = 0; i < maxId; i++) {
 			total += getAccountReceivablesAmountInKiloForProduct(i);
 		}
 		return total;
 	}
 
 	@Override
 	public Set<AccountReceivable> getAccountReceivables() {
 		return inventorySheetData.getAccountReceivables();
 	}
 
 	@Override
 	public Breakdown getBreakdown() {
 		return inventorySheetData.getBreakdown();
 	}
 
 	public Map<Integer, InventorySheetDetail> getProductInventories() {
 		return productInventories;
 	}
 
 	public InventorySheetDetail getInventorySheetDetail(int productId) {
 		return productInventories.get(productId);
 	}
 
 	public void setProductInventories(Map<Integer, InventorySheetDetail> productInventories) {
 		this.productInventories = productInventories;
 	}
 
 	public InventorySheetData getInventorySheetData() {
 		return inventorySheetData;
 	}
 
 	public static String overOrShortCaps(double actualCashOnHand, double actualCashCount) {
 		if ((actualCashOnHand - actualCashCount) > 0) {
 			return SHORT_CAPS;
 		} else {
 			if ((actualCashOnHand - actualCashCount) < 0) {
 				return OVER_CAPS;
 			} else {
 				return OVER_SHORT_CAPS;
 			}
 		}
 	}
 
 	public static String overOrShort(double actualCashOnHand, double actualCashCount) {
 		if ((actualCashOnHand - actualCashCount) > 0) {
 			return SHORT;
 		} else {
 			if ((actualCashOnHand - actualCashCount) < 0) {
 				return OVER;
 			} else {
 				return OVER_SHORT;
 			}
 		}
 	}
 
 	public static double overOrShortAmount(double actualCashOnHand, double actualCashCount) {
 		return Math.abs(actualCashOnHand - actualCashCount);
 	}
 
 }
