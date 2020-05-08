 package aic.main;
 
import aic.domain.Customer;
import aic.domain.Money;
import aic.domain.Rating;
 import at.ac.tuwien.infosys.aic11.services.BankTransfer;
 import at.ac.tuwien.infosys.aic11.services.Cheque;
 import at.ac.tuwien.infosys.aic11.services.DisbursementPreference;
 import at.ac.tuwien.infosys.aic11.services.IRegistryService_RegistryService_Client;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
 
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
 import java.net.URL;
 
 public class DisbursementTest {
 	private void execute(String args[]) throws Exception {
 		Cheque c = new Cheque();
 		c.setName("lol");
 
 		BankTransfer bt = new BankTransfer();
 		bt.setBic("roflbic");
 		bt.setBankName("roflbank");
 		bt.setIban("rofliban");
 
 		DisbursementPreference pref = bt;
 
 		URL wsdlURL = new URL(IRegistryService_RegistryService_Client.getEndpoint(pref).getLocation() + "?wsdl");
 
 		System.out.println(wsdlURL);
 
 /*		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
 		Client client = dcf.createClient(wsdlURL, this.getClass().getClassLoader());
 
 		Customer refCustomer = new Customer(0, "bob", new BigDecimal(10), "gaystreet", Rating.A);
 		Object customer = makeCustomer(refCustomer);
 
 		Money refMoney = new Money("235235", 230l);
 		Object money = makeMoney(refMoney);
 
 		Object response = client.invoke("start_money_transfer_process", pref, money, customer);*/
 	}
 
 /*	private Object makeStartMoneyTransferProcess(Object preference, Object money, Object customer) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
 		Object process = Thread.currentThread().getContextClassLoader().loadClass("at.ac.tuwien.infosys.aic11.services.StartMoneyTransferProcess").newInstance();
 		process.getClass().getMethod("setArg0", preference.getClass().getSuperclass()).invoke(process, preference);
 		process.getClass().getMethod("setArg1", money.getClass()).invoke(process, money);
 		process.getClass().getMethod("setArg2", customer.getClass()).invoke(process, customer);
 
 		return process;
 	}
 
 	private Object makeCustomer(Customer ref) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
 		Object customer = Thread.currentThread().getContextClassLoader().loadClass("at.ac.tuwien.infosys.aic11.services.Customer").newInstance();
 		customer.getClass().getMethod("setFirstName", String.class).invoke(customer, ref.getName());
 		customer.getClass().getMethod("setLastName", String.class).invoke(customer, ref.getName());
 		customer.getClass().getMethod("setMiddleName", String.class).invoke(customer, ref.getName());
 
 		Object address = Thread.currentThread().getContextClassLoader().loadClass("at.ac.tuwien.infosys.aic11.services.Address").newInstance();
 		address.getClass().getMethod("setCity", String.class).invoke(address, ref.getAddress());
 		address.getClass().getMethod("setCountryCode", String.class).invoke(address, ref.getAddress());
 		address.getClass().getMethod("setDoor", String.class).invoke(address, ref.getAddress());
 		address.getClass().getMethod("setHouse", String.class).invoke(address, ref.getAddress());
 		address.getClass().getMethod("setPostalCode", String.class).invoke(address, ref.getAddress());
 		address.getClass().getMethod("setStreet", String.class).invoke(address, ref.getAddress());
 
 		customer.getClass().getMethod("setAddress", address.getClass()).invoke(customer, address);
 
 		return customer;
 	}
 
 	public Object makeMoney(Money ref) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
 		Object money = Thread.currentThread().getContextClassLoader().loadClass("at.ac.tuwien.infosys.aic11.services.Money").newInstance();
 
 		money.getClass().getMethod("setAmount", long.class).invoke(money, ref.getAmount());
 		money.getClass().getMethod("setCurrencyCode", String.class).invoke(money, ref.getCurrencyCode());
 
 		return money;
 	}*/
 
 
 	public static void main(String args[]) throws Exception {
 		(new DisbursementTest()).execute(args);
 	}
 }
