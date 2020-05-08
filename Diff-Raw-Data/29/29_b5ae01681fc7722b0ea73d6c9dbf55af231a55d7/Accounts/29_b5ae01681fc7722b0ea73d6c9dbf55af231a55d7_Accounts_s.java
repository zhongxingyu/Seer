 package org.common.accounts;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import static ch.lambdaj.Lambda.*;
 
 import org.common.configs.Config;
 import org.common.conversion.XMLConversion;
 import org.common.operations.OperationType;
 import org.common.transactions.Transaction;
 import org.common.transactions.Transactions;
 import org.hamcrest.Matchers;
 
 @XmlRootElement
 public class Accounts {
 	@XmlElement(name = "account")
 	private ArrayList<Account> data = new ArrayList<Account>();
 	private boolean blocked = false;
 	private static String path = Config.accountsFile;
 
 	public void clear() {
 		data.clear();
 	}
 	public ArrayList<Account> init(){
 		Accounts acc = XMLConversion.unMarshall(Accounts.class, path);
 		int i = 1;
 		try{
 			while(acc.getAccount(i) != null){
 				data.add(acc.getAccount(i));
 				i++;
 			}
 		}
 		catch(ArrayIndexOutOfBoundsException e){
 			e.getMessage();			
 		}
 
 		return data;
 	}
 	
 	public Accounts getAccounts(){
 		Accounts accs = XMLConversion.unMarshall(Accounts.class, path);
 		return accs;
 	}
 	
 	public Account getAccount(String cardNumber) {
 		List<Account> accounts = filter(having(on(Account.class).getCardNumber(), Matchers.equalTo(cardNumber)), data);
 		Object[] values = accounts.toArray();
 		return (values.length > 0) ? (Account) values[0] : null;
 	}
 
 	public Account getAccount(String cardNumber, int pass) {
 		List<Account> accounts = filter(having(on(Account.class).getCardNumber(), Matchers.equalTo(cardNumber)), data);
 		Object[] values = accounts.toArray();
 		
 		if (values.length == 0) {
 			return null;
 		}
 		Account accaunt = (Account)(values[0]); 
 		if (accaunt.getPassword() != pass) {
 			return null;
 		}
 		
 		Transactions transactions = new Transactions();
 		Transaction transaction = new Transaction();
 		
 		transaction.setCardNumber(cardNumber);
 		transaction.setOperationName(OperationType.UserLogin.toString());
 		transaction.setCreated(new Date());
 		
 		transactions = transactions.getTransactions();
 		transactions.addTransaction(transaction);
 		return accaunt;
 	}
 	
 	public void addAccount(Account account) {
 		data.add(account);
         Accounts accs = new Accounts();
         accs.data = data;
         XMLConversion.marshall(accs, path);
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
 
 	public void removeAccount(String cardNumber) {
 		List<Account> acountList = filter(having(on(Account.class).getCardNumber(), Matchers.equalTo(cardNumber)), data);
 		if (!acountList.isEmpty()) {
 			Account account = acountList.get(0);
 			data.remove(account);
 		}
         Accounts accs = new Accounts();
         accs.data = data;
         XMLConversion.marshall(accs, path);
 	}
 
 	public boolean isExisting(String cardNumber) {
 		List<Account> acountList = filter(having(on(Account.class).getCardNumber(), Matchers.equalTo(cardNumber)), data);
 		if (acountList.isEmpty()){
 			return false;
 		}
 		return true;
 	}
 	public void store() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public ArrayList<Account> getAll() {
 		return data;
 	}
 }
