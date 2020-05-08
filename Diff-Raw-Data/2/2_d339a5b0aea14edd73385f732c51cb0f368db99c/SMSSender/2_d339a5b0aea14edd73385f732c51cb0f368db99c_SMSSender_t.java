 package final_project.control;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Scanner;
 
 import final_project.model.IncompleteResult;
 import final_project.model.store.*;
 
 import java.util.*;
 
 public class SMSSender implements Constants {
 
 	private String _username, _password;
 	private IDataStore _store;
 	private SMSController _control; //Needed to alert when there are no more credits, etc.
 
 	public SMSSender(IDataStore s, SMSController ctrl, String username, String password) {
 		_store = s;
 		_control = ctrl;
 		_username = username;
 		_password = password;
 	}
 
 	/**
 	 * This is the main method that handles sending a certain message to
 	 * a certain phone number. It is called by all of the other, more
 	 * specialized send methods below.
 	 *
 	 * @param message
 	 * @param number
 	 */
 	public boolean sendMessage(String message, String number) {
 		OutputStreamWriter wr = null;
 		BufferedReader rd = null;
 		boolean toReturn = false;
 
 		try {
 			//Constructing the data
 			String data = "";
 			data += "username=" + URLEncoder.encode(_username, "ISO-8859-1");
 			data += "&password=" + URLEncoder.encode(_password, "ISO-8859-1");
 			data += "&message=" + URLEncoder.encode(message, "ISO-8859-1");
 			data += "&want_report=";
 			data += "&msisdn=1" + number;
 
 			URL url = new URL("http://usa.bulksms.com:5567/eapi/submission/send_sms/2/2.0");
 			URLConnection conn = url.openConnection();
 			conn.setDoOutput(true);
 			wr = new OutputStreamWriter(conn.getOutputStream());
 			wr.write(data);	//Does this block?
 			wr.flush();
 
 			// Get the response
 			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line, output = "";
 			while ((line = rd.readLine()) != null) {
 				output += line;
 			}
 			/**
 			 * For now, only care about first line. It should look something like this:
 			 * 0|IN_PROGRESS|274166347
 			 * where the 0 is the status code (all we care about).
 			 */
 
 			Scanner s = new Scanner(output);
 			s.useDelimiter("\\|");
 			if(!s.hasNext() || !s.hasNextInt()) {
 				toReturn = false;
 			}
 			else {
 				int status_code = s.nextInt();
 				if(status_code == 0) {
 					toReturn = true; //SMS in progress
 				}
 				else if(status_code == 23) { //Authentication failure
 					_control.alertGUI(null, "Authentication failure: SMS send could not go through", _control.getTime());
 					toReturn = false;
 				}
 				else if(status_code == 25) {
 					_control.alertGUI(null, "SMS API needs more credits! Send failure!", _control.getTime());
 					toReturn = false;
 				}
 				else {
					//_control.alertGUI(null, "Send SMS Failure", _control.getTime());
 					toReturn = false;
 				}
 			}
 		} catch (Exception e) {
 			toReturn = false;
 			e.printStackTrace();
 		} finally {
 			try {
 				if(wr!=null) wr.close();
 				if(wr!=null) rd.close();
 			} catch (IOException e) { } //Currently don't care if an exception is thrown
 		}
 		return toReturn;
 	}
 
 	/**
 	 * The following methods are convenience methods that the
 	 * other classes can call so that they don't have to deal
 	 * with following our SMS protocol.
 	 */
 
 	/**
 	 * This method allows the admin to send a message to every person
 	 * in the tournament.
 	 */
 	public boolean sendAllMessage(String message) {
 		//concatenating phone numbers
 		String number = "";
 		for(IPerson i: _store.getPeople()) {
 			if (i!= null)
 				number += i.getPhoneNumber() + ",1";
 		}
 		return this.sendMessage(message, number);
 	}
 
 	/**
 	 * Similar to sendAllMessage, this method also takes a group param,
 	 * meaning that only members of this certain group will be alerted.
 	 *
 	 * @param group
 	 * @param message
 	 * @return
 	 */
 	public boolean sendGroupMessage(String group, String message) {
 		String number = "";
 		for(IPerson i: _store.getPeopleForGroup(group)) {
 			if (i!=null && !i.getPhoneNumber().equals(""))
 				number += i.getPhoneNumber() + ",1";
 		}
 		return this.sendMessage(message, number);
 	}
 
 	//Should I have another method, where this is organized into a batch? TODO
 	public boolean sendFencerStripMessage(int id,  int strip) {
 		String message = "Fencer id: " + id + " Strip assignment: " + strip;
 
 		//Look up the fencer in the database to get their phone number
 		IPerson i = _store.getPerson(id);
 		if(i==null) {
 			return false;
 		}
 
 		String number = i.getPhoneNumber();
 		if(number.equals("")) {
 			return false;
 		}
 
 		return this.sendMessage(message, number);
 	}
 
 	public boolean sendMatchNotifications(IncompleteResult result, int refID, int stripID) {
 		int p1 = result.getPlayer1();
 		int p2 = result.getPlayer2();
 
 		boolean toReturn = false;
 		String message = "You are now fencing " + _store.getPerson(p2).getFirstName() + " " +
 			_store.getPerson(p2).getLastName() + " on strip " + stripID + ".";
 		toReturn = this.sendMessage(message, _store.getPerson(p1).getPhoneNumber());
 
 		message = "You are now fencing " + _store.getPerson(p1).getFirstName() + " " +
 			_store.getPerson(p1).getLastName() + " on strip " + stripID + ".";
 		toReturn = this.sendMessage(message, _store.getPerson(p2).getPhoneNumber());
 
 		message = "You are reffing " + _store.getPerson(p1).getFirstName() + " " +
 			_store.getPerson(p1).getLastName() + "(id: " + p1 + ") and " + _store.getPerson(p2).getFirstName() + " " +
 			_store.getPerson(p2).getLastName() + "(id: " + p2 + ") on strip " + stripID + ".";
 		toReturn = this.sendMessage(message, _store.getPerson(refID).getPhoneNumber());
 
 		return toReturn;
 	}
 
     public void sendCollectionMessage(String message, Collection<Integer> people) {
         String number = "";
         Iterator<Integer> iter = people.iterator();
         while(iter.hasNext()) {
         	Integer id = iter.next();
             IPerson p = _store.getPerson(id);
             if (p==null || p.getPhoneNumber()==null || p.getPhoneNumber().isEmpty())
             	continue;
             
             number += p.getPhoneNumber();
             if (iter.hasNext())
             	number += ",1";
         }
         if (!number.isEmpty())
             sendMessage(message, number);
     }
 
 	public boolean sendSubscriberMessage(String message, int fencerID) {
 		//Getting the observers for this fencer
 		Collection<Integer> subscribers = _store.getPlayer(fencerID).getObservers();
 		
 		boolean toReturn = true;
 		for(Integer id: subscribers) {
 			IPerson p = _store.getPerson(id);
 			if(toReturn)
 				toReturn = this.sendMessage(message, p.getPhoneNumber());
 			else
 				this.sendMessage(message, p.getPhoneNumber());
 		}
 		return toReturn;
 	}
 }
