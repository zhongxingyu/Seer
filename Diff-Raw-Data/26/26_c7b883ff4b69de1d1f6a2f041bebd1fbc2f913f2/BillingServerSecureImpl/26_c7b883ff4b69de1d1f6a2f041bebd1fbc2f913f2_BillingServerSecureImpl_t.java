 package BillingServer;
 
 import RMI.BillingServerSecure;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 /**
  *
  * @author Dave
  */
 public class BillingServerSecureImpl extends UnicastRemoteObject implements BillingServerSecure {
 
     Logger logger = LogManager.getLogger(BillingsServer.class.getSimpleName() + "." + BillingServerSecureImpl.class.getSimpleName());
     private PriceSteps ips = new PriceSteps();
     private Bill bill = new Bill();
 
     public BillingServerSecureImpl() throws RemoteException {
         super();
         logger.info("BillingServerSecureImpl started!");
     }
 
     @Override
     public PriceSteps getPriceSteps() {
         logger.debug("Inside getPriceSteps");
         return this.ips;
     }
 
     @Override
     public void createPriceStep(double min_value, double max_value, double fee_fixed, double fee_variable) throws RemoteException {
         logger.debug("Inside createPriceStep");
         if (min_value < 0 || max_value < 0 || fee_fixed < 0 || fee_variable < 0) {
             throw new RemoteException("There is a negative number.");
         }
         if (!this.ips.check(min_value, max_value)) {
             throw new RemoteException("There is an overlap.");
         }
         this.ips.createPriceStep(min_value, max_value, fee_fixed, fee_variable);
         logger.info("PriceStep added successfully");
 
     }
 
     @Override
     public void deletePriceStep(double min_value, double max_value) throws RemoteException {
         logger.debug("Inside deletePriceStep");
        if(!this.ips.deletePriceStep(min_value, max_value))
            throw new RemoteException("PriceStep could not be deleted. False Range Definitions.");
     }
 
     @Override
     public void billAuction(String user, long auctionID, double price) throws RemoteException {
         logger.debug("Inside billAuction");
         User u = null;
 
         if (this.bill.isUserAvailable(user)) {
             logger.debug("The user list already contains the user " + user);
 
             Auction a = new Auction(auctionID, price);
             this.bill.putAuctiontoUser(a, user);
             //u.getAuctions().add(a);
         } else {
             logger.debug("The user " + user + " is not in the list and will be created.");
             u = new User(user);
             Auction a = new Auction(auctionID, price);
             u.putAuction(a);
             if (!this.bill.adduser(u)) {
                 logger.fatal(":billAuction:User " + user + " was not added to the bill.user List.");
             }
         }
         logger.info("An ended Auction with id " + auctionID + "  was saved to the User " + user);
     }
 
     @Override
     public Bill getBill(String user) throws RemoteException {
         logger.debug("Inside getBill(" + user + ")");
         this.bill.createBill(user, this.ips);
         return this.bill;
     }
 }
