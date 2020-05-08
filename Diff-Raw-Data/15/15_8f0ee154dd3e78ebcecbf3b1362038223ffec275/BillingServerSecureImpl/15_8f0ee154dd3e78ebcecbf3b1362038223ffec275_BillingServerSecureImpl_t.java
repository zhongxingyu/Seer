 /**
  * 
  */
 package at.ac.tuwien.dslab2.service.billingServer;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import at.ac.tuwien.dslab2.domain.Bill;
 import at.ac.tuwien.dslab2.domain.PriceStep;
 import at.ac.tuwien.dslab2.domain.PriceSteps;
 
 /**
  * @author klaus
  * 
  */
 class BillingServerSecureImpl implements BillingServerSecure {
 	private final ConcurrentMap<String, Bill> bills;
 	private final PriceSteps priceSteps;
 
 	// Private constructor prevents instantiation from other classes
 	private BillingServerSecureImpl() {
 		bills = new ConcurrentHashMap<String, Bill>();
 		priceSteps = new PriceSteps();
 	}
 
 	private static class BillingServerSecureHolder {
 		public static final BillingServerSecure INSTANCE = new BillingServerSecureImpl();
 	}
 
 	public static BillingServerSecure getInstance() {
 		return BillingServerSecureHolder.INSTANCE;
 	}
 
 	@Override
 	public PriceSteps getPriceSteps() throws RemoteException {
 		return priceSteps;
 	}
 
 	@Override
 	public void createPriceStep(double startPrice, double endPrice,
 			double fixedPrice, double variablePricePercent)
 			throws RemoteException {
 		if (startPrice < 0 || endPrice < 0 || fixedPrice < 0
 				|| variablePricePercent < 0)
 			throw new RemoteException(
 					"One of the specified values is negative!");
		if (startPrice >= endPrice)
			throw new RemoteException("Min has to be lower than max!");
 
 		PriceStep newPs = new PriceStep(startPrice, endPrice, fixedPrice,
 				variablePricePercent);
 
 		for (PriceStep ps : priceSteps) {
 			if (ps.overlaps(newPs)) {
 				throw new RemoteException(
 						"The price step specified overlaps with another price step: "
 								+ ps.getInterval());
 			}
 		}
 
 		priceSteps.add(newPs);
 
 	}
 
 	@Override
 	public void deletePriceStep(double startPrice, double endPrice)
 			throws RemoteException {
 
 		if (!priceSteps.contains(startPrice, endPrice))
 			throw new RemoteException("Price step (" + startPrice + " "
 					+ endPrice + "] does not exist");
 
 		priceSteps.remove(startPrice, endPrice);
 	}
 
 	@Override
 	public void billAuction(String user, long auctionID, double price)
 			throws RemoteException {
 		if (user == null)
 			throw new IllegalArgumentException("user is null");
 
 		Bill b = new Bill(user);
 
 		Bill existingBill = bills.putIfAbsent(user, b);
 		if (existingBill != null)
 			b = existingBill;
 
 		b.addAuction(auctionID, price);
 	}
 
 	@Override
 	public Bill getBill(String user) throws RemoteException {
 		Bill bill = bills.get(user);
 		if (bill == null)
 			return null;
 
 		// Calculate fees
 		// TODO: If possible improve the synchronization and maybe performance
 		// too
 		for (Bill.Auction auction : bill) {
 			for (PriceStep priceStep : priceSteps) {
 				if (priceStep.contains(auction.getPrice())) {
 					synchronized (auction) {
 						auction.setCalculatedFixedFee(priceStep.getFixedPrice());
 						auction.setCalculatedVariableFee(priceStep
 								.getVariablePricePercent()
 								/ 100
 								* auction.getPrice());
 					}
 				}
 			}
 		}
 
 		return bill;
 	}
 
 	@Override
 	public void close() throws IOException {
 	}
 
 }
