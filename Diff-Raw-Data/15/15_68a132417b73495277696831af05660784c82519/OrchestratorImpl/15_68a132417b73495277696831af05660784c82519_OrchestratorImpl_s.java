 package br.usp.ime.futuremarket.orchestration;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import br.usp.ime.futuremarket.Bank;
 import br.usp.ime.futuremarket.CustomerInfo;
 import br.usp.ime.futuremarket.Purchase;
 import br.usp.ime.futuremarket.ServiceName;
 import br.usp.ime.futuremarket.Shipper;
 import br.usp.ime.futuremarket.ShopList;
 import br.usp.ime.futuremarket.choreography.BrokerImpl;
 
 public class OrchestratorImpl extends BrokerImpl implements Orchestrator {
 
     private Bank bank = null;
 
     @Override
     public boolean deliver(final Purchase purchase) throws MalformedURLException {
         final Shipper shipper = market.getClient(purchase.getShipper(), ServiceName.SHIPPER,
                 Shipper.class);
         return shipper.deliver(purchase);
     }
 
     @Override
     public Purchase smPurchase(final ShopList list, final CustomerInfo customer) throws IOException {
         return super.purchaseFromOneStore(list, customer);
     }
 
     @Override
     public boolean requestPayment(final double amount, final CustomerInfo customer)
             throws IOException {
         return getBank().requestPayment(amount, customer);
     }
 
     private Bank getBank() throws IOException {
         if (bank == null) {
             final String role = Role.BANK;
             final String name = ServiceName.BANK;
             bank = market.getClientByRole(role, name, Bank.class);
         }
         return bank;
     }
 }
