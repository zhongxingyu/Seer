 package org.common.accounts;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import static ch.lambdaj.Lambda.*;
 
 import org.hamcrest.Matchers;
 
 //import javax.xml.bind.JAXBContext;
 //import javax.xml.bind.JAXBException;
 //import javax.xml.bind.Marshaller;
 //import javax.xml.bind.Unmarshaller;
 
 //import org.common.configs.Config;
 
 public class Accounts {
 
 	private ArrayList<Account> data = new ArrayList<Account>();
 	private boolean blocked = false;
 	
 	public Accounts() {
 //		try {
 
 			for (int i = 0, l = 5; i < l; i++) {
 				Account acc = new Account();
 				acc.setAmount(100.00);
 				acc.setCardNumber("1000000000000"+(i+1));
 				acc.setPassword("pass"+i);
 				acc.setStatus(true);
 				data.add(acc);
 			}
 //			JAXBContext jaxbContextWrite = JAXBContext.newInstance(HashMap.class);
 //			Marshaller jaxbMarshaller = jaxbContextWrite.createMarshaller();
 //	 
 //			// output pretty printed
 //			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 //	 
 //			jaxbMarshaller.marshal((Object)data, Accounts.class.getResourceAsStream(Config.accountsFile));
 //			
 //			
 //			JAXBContext jaxbContextRead = JAXBContext.newInstance(HashMap.class);
 //			Unmarshaller jaxbUnmarshaller = jaxbContextRead.createUnmarshaller();
 //			Object loadedData = jaxbUnmarshaller.unmarshal(Accounts.class.getResourceAsStream(Config.accountsFile));
 //			data = (HashMap<String, Account>) loadedData;
 //			System.out.println(data);
 
 //		} catch (JAXBException e) {
 //			e.printStackTrace();
 //		}
 	 
 	}
 	
 	public void clear() {
 		data.clear();
 	}
 	
 	public Account getAccount(String cardNumber) {
 		List<Account> accounts = filter(having(on(Account.class).getCardNumber(), Matchers.equalTo(cardNumber)), data);
 		Object[] values = accounts.toArray();
		return (Account) values[0];
 	}
 	
 	public void addAccount(Account account) {
 		data.add(account);
 	}
 
 	public boolean isBlocked() {
 		return blocked;
 	}
 
 	public void setBlocked(boolean blocked) {
 		this.blocked = blocked;
 	}
 
 	public int count() {
 		return data.size();
 	}
 
 	public Account getAccount(int index) {
 		Object[] values = data.toArray();
 		if (values.length > 0 && ( (index-1) <= values.length && (index-1) >= 0) ) {
 			return (Account) values[index-1];
 		}
 		return null;
 	}
 
 	public void removeAccount(String accountNumber) {
 		
 	}
 
 	public boolean isExisting(String accountNumber) {
 		return false;
 	}
 }
