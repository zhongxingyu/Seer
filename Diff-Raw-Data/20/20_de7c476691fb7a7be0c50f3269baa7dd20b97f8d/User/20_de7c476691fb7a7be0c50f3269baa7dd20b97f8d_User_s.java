 package com.powertac.tourney.beans;
 
 import java.util.Vector;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import com.powertac.tourney.actions.ActionAccount;
 import com.powertac.tourney.actions.ActionBrokerDetail;
 
 @SessionScoped
 @ManagedBean
 public class User {
 
 	/*
 	 * Possible permisssions that a user can have Changes the form displayed to
 	 * the user
 	 */
 
 	public static final String key = "user";
 
 	private String username;
 	private String password;
 	private int permissions;
 	private boolean loggedIn;
 
 	// Brokers
	private Vector<Broker> brokers;
 
 	public User() {
 		this.brokers = new Vector<Broker>();
 		this.username = "Guest";
 		this.password = "";
 		this.permissions = Permission.GUEST;
 		this.loggedIn = false;

 	}
 
 	public Broker addBroker(String brokerName, String shortDescription) {
 		Broker b = new Broker(brokerName, shortDescription);
 		brokers.add(b);
 
 		return b;
 
 	}
 
 	public void deleteBroker(int brokerId) {
 		for (Broker b : brokers) {
 			if (b.getBrokerId() == brokerId) {
 				brokers.remove(b);
 				break;
 			}
 		}
 	}
 
 	public Broker getBroker(int brokerId) {
 		for (Broker b : brokers) {
 			if (b.getBrokerId() == brokerId) {
 				return b;
 			}
 		}
 
 		return null;
 	}
 
 	public static String getKey() {
 		return key;
 	}
 
 	public String getUsername() {
 		return this.username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return this.password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public int getPermissions() {
 		return this.permissions;
 	}
 
 	public void setPermissions(int permissions) {
 		this.permissions = permissions;
 	}
 
 	public boolean login() {
 		// Set loggedIn value
 		this.loggedIn = true;
 
 		return false;
 	}
 
 	public boolean logout() {
 		// There is probably a better way to do this
 		this.username = "Guest";
 		this.password = "";
 		this.permissions = Permission.GUEST;
 		this.loggedIn = false;
		this.brokers = null;
 
 		// TODO: Manually destroy session variables
 		ActionBrokerDetail abd = (ActionBrokerDetail) FacesContext
 				.getCurrentInstance().getExternalContext().getSessionMap()
 				.remove(ActionBrokerDetail.getKey());
 
 		return false;
 
 	}
 
 	public boolean getLoggedIn() {
 		return this.loggedIn;
 	}
 
 	public Vector<Broker> getBrokers() {
 		/*Broker[] newBroker = new Broker[brokers.size()];
 		int i = 0;
 		for (Broker b : brokers) {
 			newBroker[i] = brokers.get(i);
 			i++;
 		}*/
 		return brokers;
 	}
 
 }
