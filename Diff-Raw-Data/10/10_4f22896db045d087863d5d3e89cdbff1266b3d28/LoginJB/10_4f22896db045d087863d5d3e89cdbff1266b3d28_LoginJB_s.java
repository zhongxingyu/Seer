 package org.silix.the9ull.microbit.controlinterface;
 
 import java.math.BigDecimal;
 import java.rmi.RemoteException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.silix.the9ull.microbit.control.GetInfoBeanRemote;
 import org.silix.the9ull.microbit.control.UserBeanRemote;
 import org.silix.the9ull.microbit.model.HistoryP;
 import org.silix.the9ull.microbit.model.Tx;
 
 // Java Bean
 
 public class LoginJB {
 
 	private String idOrAddress;
 	private int id;
 	private String address;
 	private String password;
 	private boolean logged = false;
 	
 	//newContact
 	private String alias;
 	private String contactAddress;
 	private boolean addedContact;
 	private boolean removedContact;
 	private String sendTo;
 	private String howMuch;
 	private boolean sentTo;
 	private BigDecimal fee;
 	private String strError;
 	
 	private String historyTableRows;
 
 	private Contacts contacts;
 	
 	private UserBeanRemote ub; //The Bean
 	
 	public LoginJB() {
 		this.howMuch = ""; // Instantiate the object
 	}
 
 
 	public void setIdOrAddress(String idOrAddress) {
 		id = -1;
 		address = null;
 		if(Pattern.matches("^\\d*$", idOrAddress)){
 			id = Integer.parseInt(idOrAddress);
 			
 		}
 		else {
 			address = idOrAddress;
 			
 		}
 		this.idOrAddress = idOrAddress;
 	}
 
 
 	public int getId() {
 		return id;
 	}
 
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 
 	public String getAddress() {
 		return address;
 	}
 
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 
 	public String getIdOrAddress() {
 		return idOrAddress;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public void setLogged(boolean logged) {
 		ub = EJBUtils.getUser();
 		
 		GetInfoBeanRemote gib = EJBUtils.getGetInfo();
 		
 		if(logged){
 			if(this.getId() == -1){
 				this.setId(gib.getIdFromAddress(this.getAddress()));
 			} else {
 				this.setAddress(gib.getAddressFromId(this.getId()));
 			}
 			try {
 				this.logged = ub.login(this.getId(), this.getPassword());
 				
 				if(this.logged){
 					contacts = new Contacts(this.ub);
 				
 				}
 					
 				System.out.println("id "+this.id);
 				System.out.println("address "+this.address);
 				
 				System.out.println("Logged: "+this.logged);
 				
 			} catch (RemoteException e) {
 				System.out.println("EJB server problem.");
 				e.printStackTrace();
 				setStrError("Server error");
 				return;
 			}	
 		}
 		else {
 			System.out.println("LoginJB: Logout");
 			try {
 				this.contacts.logout();
 				this.contacts = null;
 				ub.logout();
 				this.logged = false;
 			} catch (RemoteException e) {
 				System.out.println("EJB server problem.");
 				e.printStackTrace();
 				setStrError("Server error");
 				return;
 			}
 		}
 	}
 	
 	public boolean isLogged() {
 		return logged;
 	}
 
 	public String getFund() {
 		System.out.println("LoginJB: updateFund");
 		
 		try {
 			BigDecimal ret = ub.getFund(); 
 			if(ret==null) {
 				setStrError("Bitcon server error");
 				return "Error";
 			}
 			return ret.toPlainString(); 
 		} catch (Exception e) {
 			System.out.println("EJB server problem.");
 			setStrError("Server error");
 		}
 		return "";
 	}
 
 	public void setFund(String fund) {
 		// read only value
 		System.out.println("LoginJB: setFund: Read only value");
 	}
 
 	public void updateFund() {
 		// TODO: ???
 	}
 
 	final static String htmldelete = "<form name=\"deleteContact\" action=\"index.jsp?deleteContact\" method=\"POST\">" +
 			"<input type=\"hidden\" name=\"alias\" value=\"$ALIAS\" />" +
 			"<input type=\"hidden\" name=\"contactAddress\" value=\"$ADDRESS\" />" +
 			"<input type=\"submit\" value=\"x\" />" +
 			"</form>";
 	final static String htmlrestorecontact = "<form name=\"restoreContact\" action=\"index.jsp?restoreContact\" method=\"POST\">" +
 			"<input type=\"hidden\" name=\"alias\" value=\"$ALIAS\" />" +
 			"<input type=\"hidden\" name=\"contactAddress\" value=\"$ADDRESS\" />" +
 			"<input type=\"submit\" value=\"restore deleted contact\" />" +
 			"</form>"; 
 	final static String htmlsendto = "<form name=\"payment\" action=\"index.jsp?contacts&sendTo\" method=\"POST\">" +
 			"<input type=\"text\" name=\"howMuch\" value=\"0.00000000\" />" +
 			"<input type=\"hidden\" name=\"alias\" value=\"$ALIAS\" />" +
 			"<input type=\"hidden\" name=\"contactAddress\" value=\"$ADDRESS\" />" +
 			"<input type=\"submit\" value=\"To\" />" +
 			"</form>";
 	
 	public String getContactsTable() {
 		List<String> header = new LinkedList<String>();
 		header.add("Pay");
 		header.add("Name");
 		header.add("Address");
 		header.add("Delete");
 		
 		List<List<String>> cc = contacts.get();
 		if(cc==null) {
 			setStrError("Server error");
 			return "";
 		}
 		for(List<String> l : cc) {
 			l.add(0, htmlsendto.replace("$ALIAS", l.get(0)).replace("$ADDRESS", l.get(1))); // Old index 1
 			l.add(htmldelete.replace("$ALIAS", l.get(1)).replace("$ADDRESS", l.get(2))); // New index 1 (old 0)
 		}
 		return HTMLUtilities.printTable(cc, header);
 	}
 
 	public String getRestoreForm() {
 		return htmlrestorecontact.replace("$ALIAS",getAlias()).replace("$ADDRESS", getContactAddress());
 	}
 	
 	public String getContactAddress() {
 		return contactAddress;
 	}
 
 
 	public void setContactAddress(String contactAddress) {
 		this.contactAddress = contactAddress;
 	}
 	
 	public void addNewContact() {
 		try {
 			addedContact = ub.newContact(alias, contactAddress);
 		} catch (RemoteException e) {
 			addedContact = false;
 			System.out.println("EJB server problem.");
 			e.printStackTrace();
 			setStrError("Server error");
 		}
 	}
 	
 	public void removeContact(){
 		try {
 			setRemovedContact(ub.removeContact(alias));
 		} catch (RemoteException e) {
 			setRemovedContact(false);
 			System.out.println("EJB server problem.");
 			e.printStackTrace();
 			setStrError("Server error");
 		}
 	}
 	
 	public boolean isContactAddressValid() {
 		try {
 			return ub.isAddressValid(getContactAddress());
 		} catch (RemoteException e) {
 			System.out.println("EJB server problem.");
 			e.printStackTrace();
 			setStrError("Server error");
 		}
 		return false;
 	}
 	
 	public void setAddedContact(boolean addedContact) {
 		this.addedContact = addedContact;
 	}
 
 
 	public boolean isAddedContact() {
 		return addedContact;
 	}
 
 
 	public boolean isRemovedContact() {
 		return removedContact;
 	}
 
 
 	public void setRemovedContact(boolean removedContact) {
 		this.removedContact = removedContact;
 	}
 
 	public void sendTo() {
 		System.out.println("LoginJB: "+contactAddress+" "+howMuch);
 		BigDecimal hm = null;
 		try {
 			hm = new BigDecimal(getHowMuch()).setScale(8,BigDecimal.ROUND_HALF_DOWN); // BitCoin format;
 			setHowMuch(hm.toPlainString());
 		} catch(NumberFormatException e) {
 			System.out.println("NumberFormatException: "+getHowMuch());
 			setSentTo(false);
 			return;
 		}
 		try {
 			Tx tx = ub.sendTo(contactAddress,hm);
			if(tx.getStrError()!=null) {
 				setSentTo(false);
 				setStrError(tx.getStrError());
 				return;
			}
			if(tx!=null){
 				setSentTo(true);
 				BigDecimal f = tx.getFee();
 				if(f==null) {
 					setFee(new BigDecimal(0));
 				}
 				else if(f.compareTo(new BigDecimal(0))<=0)
 					setFee(f.negate());
 				else
 					setFee(f);
			} else
				setSentTo(false);
 		} catch (RemoteException e) {
 			setSentTo(false);
 			System.out.println("EJB server problem.");
 			e.printStackTrace();
 			setStrError("Server error");
 		}
 	}
 	
 	public String getAlias() {
 		return alias;
 	}
 
 
 	public void setAlias(String alias) {
 		this.alias = alias;
 	}
 
 
 	public String getSendTo() {
 		return sendTo;
 	}
 
 	public void setSendTo(String sendTo) {
 		this.sendTo = sendTo;
 	}
 
 
 	public boolean isSentTo() {
 		return sentTo;
 	}
 
 	public void setSentTo(boolean sentTo) {
 		this.sentTo = sentTo;
 	}
 
 
 	public String getHowMuch() {
 		return howMuch;
 	}
 
 
 	public void setHowMuch(String howMuch) {
 		this.howMuch = howMuch;
 	}
 
 
 	public BigDecimal getFee() {
 		return fee;
 	}
 
 
 	public void setFee(BigDecimal fee) {
 		this.fee = fee;
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public String getHistoryTable() {
 		List<HistoryP> history = null;
 		List<List<String>> table = new LinkedList<List<String>>();
 		List<String> header =  new LinkedList<String>();
 		List<String> l, m;
 		
 		String nRowsS = getHistoryTableRows();
 		int nRows = 0;
 		if(!nRowsS.equals("")) {
 			try {
 				nRows = new Integer(nRowsS);
 			} catch(NumberFormatException e){}
 		}
 		
 		try {
 
 			history = new ArrayList<HistoryP>(ub.getHistory(0,nRows));
 			Collections.sort(history);
 			Collections.reverse(history);
 			
 		} catch (RemoteException e) {
 			System.out.println("EJB server problem.");
 			e.printStackTrace();
 			return null;
 		}
 		header.add("From user");
 		header.add("To user");
 		header.add("Timestamp");
 		header.add("Amoun");
 		header.add("Fee");
 		
 		BigDecimal howMuch, fee;
 		int from_id, to_id;
 		String from_str, to_str;
 		String external = "external";
 		String lastDate = "";
 		for(HistoryP h : history) {
 			l = new LinkedList<String>();
 			from_id = h.getFrom().getId();
 			to_id = h.getTo().getId();
 			howMuch = h.getHowmuch();
 			Date when = h.getWhen();
 			
 			String[] date = when.toString().split(" ");
 			assert date.length == 2;
 			assert(date[1].length()>=8);
 			System.out.println("LoginBD: Data: "+date[1]);
 			date[1] = date[1].substring(0, 8);
 			if(!date[0].equals(lastDate)) {
 				lastDate = date[0];
 				m = new LinkedList<String>();
 				m.add(""); m.add("");
 				m.add("<b>"+date[0]+"</b>");
 				m.add(""); m.add("");
 				table.add(m);
 			}
 			if(from_id==to_id){
 				//External transaction
 				if(howMuch.compareTo(new BigDecimal(0))<0) {
 					// Send
 					from_str = ""+from_id;
 					to_str = external;
 				} else {
 					// Receive
 					from_str = external;
 					to_str = ""+to_id;
 				}
 			} else {
 				from_str = ""+from_id;
 				to_str = ""+to_id;
 			}
 			l.add(from_str.equals(""+getId()) ? "me" : from_str);
 			l.add(to_str.equals(""+getId()) ? "me" : to_str);
 			l.add(date[1]);
 			l.add(howMuch.signum()>0 ? howMuch.toPlainString() : howMuch.negate().toPlainString());
 			fee = h.getFee();
 			if(fee==null)
 				l.add("");
 			else {
 				l.add(fee.signum()>0 ? fee.toPlainString() : fee.negate().toPlainString());
 			}
 			table.add(l);
 		}
 		
 		return HTMLUtilities.printTable(table, header);
 	}
 
 
 	public String getHistoryTableRows() {
 		return historyTableRows;
 	}
 
 
 	public void setHistoryTableRows(String historyTableRows) {
 		this.historyTableRows = historyTableRows;
 	}
 
 
 	private String htmlerror = "<font color=\"red\">$ERROR</font><br />";
 	public String getStrError() {
 		if(strError==null)
 			return "";
 		String ret = htmlerror.replace("$ERROR", strError);
 		setStrError(null); //return an error only once
 		return ret;
 	}
 
 
 	public void setStrError(String strError) {
 		this.strError = strError;
 	}
 	
 }
