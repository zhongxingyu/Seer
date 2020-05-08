 package org.common.transactions;
 
 import static ch.lambdaj.Lambda.filter;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 
 import java.util.ArrayList;
 import java.util.List;
 
import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.common.configs.Config;
 import org.common.conversion.XMLConversion;
 import org.hamcrest.Matchers;
 
 @XmlRootElement
 public class Transactions {
    @XmlElement(name = "transaction")
 	private ArrayList<Transaction> data = new ArrayList<Transaction>();
 	private static String path = Config.transactionsFile;
 	
 
 	public ArrayList<Transaction> init(){
 		Transactions transactions = XMLConversion.unMarshall(Transactions.class, path);
         int i = 1;
 		try{
 			while(transactions.getTransaction(i) != null){
 				data.add(transactions.getTransaction(i));
 				i++;
 			}
 		}
 		catch(ArrayIndexOutOfBoundsException e){
 			e.getMessage();			
 		}
 
 		return data;
 	}
 
 	public void setData(ArrayList<Transaction> data) {
 		this.data = data;
 	}
 	
 	public Transaction getTransaction(int index) {
 		Object[] values = data.toArray();
 		if (values.length > 0 && ( (index-1) <= values.length && (index-1) >= 0) ) {
 			return (Transaction) values[index-1];
 		}
 		return null;
 	}
 	
 	public Transaction getTransaction(String cardNumber) {
 		List<Transaction> transaction = filter(having(on(Transaction.class).getCardNumber(), Matchers.equalTo(cardNumber)), data);
 		Object[] values = transaction.toArray();
 		return (values.length > 0) ? (Transaction) values[0] : null;
 	}
 	
 	public void addTransaction(Transaction transaction) {
 		data.add(transaction);
 		Transactions transactions = new Transactions();
 		transactions.data = data;
 		XMLConversion.marshall(transactions, path);
 	}
 	
 	public Transactions getTransactions() {
 		Transactions transactions = XMLConversion.unMarshall(Transactions.class, path);
 		return transactions;
 	}
 	
 	public void store(){
 		Transactions transactions = new Transactions();
 		transactions.data = data;
 		XMLConversion.marshall(transactions, path);
 	}
 }
