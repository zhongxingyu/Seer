 package final_project.control;
 import java.io.*;
 import java.net.*;
 import java.util.Scanner;
 import java.util.TimerTask;
 
 /**
  * The SMSReceiver task extends the
  * @author Miranda
  *
  */
 public class SMSReceiver extends TimerTask implements Constants{
 
 	private SMSController _control;
 	private String _username, _password; //Not the most secure but who cares.
 	private int _lastRetrievedID;
 	private boolean _flushed;
 	
 	public SMSReceiver(SMSController ctrl, String username, String password) {
 		_control = ctrl;
 		_username = username;
 		_password = password;
 		_lastRetrievedID = 0;
 		_flushed = false;
 	}
 
 	public void run() {
 			this.getInbox();
 	}
 
 	/**
 	 * GET INBOX METHOD
 	 * Calls on the API to get all of the messages not previously
 	 */
 	public boolean getInbox() {
 		if(!_flushed) {
 			this.flushInbox();
 			return false;
 		}
 		OutputStreamWriter wr = null;
 		BufferedReader rd = null;
 		boolean toReturn = false;
 
 		try {
 			//Constructing data
 			String data = "";
 			data += "username=" + URLEncoder.encode(_username, "ISO-8859-1");
 			data += "&password=" + URLEncoder.encode(_password, "ISO-8859-1");
 			data += "&last_retrieved_id=" + _lastRetrievedID;
 
 			URL url = new URL(API_RECEIVE_URL);
 			URLConnection conn = url.openConnection();
 			conn.setDoOutput(true);
 			wr = new OutputStreamWriter(conn.getOutputStream());
 			wr.write(data);
 			wr.flush();
 
 			// Get the response
 			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			boolean firstLine = true;
 			while ((line = rd.readLine()) != null) {
 				//Parsing the very first line
 				if(firstLine) {
 					//First line from API looks something like:
 					//0|records to follow|3 --> only care about status code (the zero)
 					Scanner s = new Scanner(line);
 					s.useDelimiter("\\|");
 
 					if(!s.hasNextInt()) {
 						toReturn = false;
 						break;
 					}
 					int status_code = s.nextInt();
 					if(status_code == 0) {
 						toReturn = true; //SMS in progress
 					}
 					else if(status_code == 23) { //Authentication failure
 						_control.alertGUI(null, "Authentication failure: SMS send could not go through", _control.getTime());
 						toReturn = false;
 						break;
 					}
 					else if(status_code == 25) {
 						_control.alertGUI(null, "SMS API needs more credits! Send failure!", _control.getTime());
 						toReturn = false;
 						break;
 					}
 					else {
						//_control.alertGUI(null, "Send SMS Failure", _control.getTime());
 						toReturn = false;
 						break;
 					}
 
 					//Eating the second (empty) line
 					line = rd.readLine();
 					firstLine = false;
 				}
 				else {
 					//Example input:
 					//19|4412312345|Hi there|2004-01-20 16:06:40|44771234567|0
 					Scanner s = new Scanner(line);
 					s.useDelimiter("\\|"); //Must escape bar to satisfy regex
 
 					//First, getting out the message id & storing it
 					if(!s.hasNextInt()) {
 						toReturn = false;
 						break;
 					}
 					_lastRetrievedID = s.nextInt();
 					//Next, getting the phone number the text was sent from
 					if(!s.hasNext()) {
 						toReturn = false;
 						break;
 					}
 					String number = s.next().substring(1); //Eating the first one
 
 					//Lastly, getting the message receieved. Don't care about the rest.
 					if(!s.hasNext()) {
 						toReturn = false;
 						break;
 					}
 					String message = s.next();
 					//Calling control's parse output method
 					System.out.println("Message received! " + message + " from " + number);
 					_control.parseOutput(message, number);
 				}
 			}
 			toReturn = true; //Input successfully processed
 		} catch (UnknownHostException e) {
 			//Letting the GUI know it ain't got no internet
 			_control.alertGUI(null, "You are not currently connected to the internet. SMS notification system disabled", _control.getTime());
 		} catch (Exception e) {
 			e.printStackTrace(); //What to do with these??
 		}
 		finally {
 			try {
 				if(wr!=null) wr.close();
 				if(wr!=null) rd.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return toReturn;
 	}
 
 
 	/**
 	 * Gets inbox without parsing messages
 	 */
 	public void flushInbox() {
 		OutputStreamWriter wr = null;
 		BufferedReader rd = null;
 		
 		try {
 			//Constructing data
 			String data = "";
 			data += "username=" + URLEncoder.encode(_username, "ISO-8859-1");
 			data += "&password=" + URLEncoder.encode(_password, "ISO-8859-1");
 			data += "&last_retrieved_id=" + _lastRetrievedID;
 
 			URL url = new URL(API_RECEIVE_URL);
 			URLConnection conn = url.openConnection();
 			conn.setDoOutput(true);
 			wr = new OutputStreamWriter(conn.getOutputStream());
 			wr.write(data);
 			wr.flush();
 
 			// Get the response
 			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			boolean firstLine = true;
 			while ((line = rd.readLine()) != null) {
 				//Parsing the very first line
 				if(firstLine) {
 					//First line from API looks something like:
 					//0|records to follow|3 --> only care about status code (the zero)
 					Scanner s = new Scanner(line);
 					s.useDelimiter("\\|");
 
 					if(!s.hasNextInt()) {
 						break;
 					}
 					int status_code = s.nextInt();
 					if(status_code == 0) {
 						//Do nothing --> no error
 					}
 					else if(status_code == 23) { //Authentication failure
 						_control.alertGUI(null, "Authentication failure: SMS send could not go through", _control.getTime());
 						break;
 					}
 					else if(status_code == 25) {
 						_control.alertGUI(null, "SMS API needs more credits! Send failure!", _control.getTime());
 						break;
 					}
 					else {
 						_control.alertGUI(null, "Send SMS Failure", _control.getTime());
 						break;
 					}
 
 					//Eating the second (empty) line
 					line = rd.readLine();
 					firstLine = false;
 				}
 				else {
 					//Example input:
 					//19|4412312345|Hi there|2004-01-20 16:06:40|44771234567|0
 					Scanner s = new Scanner(line);
 					s.useDelimiter("\\|"); //Must escape bar to satisfy regex
 
 					//First, getting out the message id & storing it
 					if(!s.hasNextInt()) {
 						break;
 					}
 					_lastRetrievedID = s.nextInt();
 				}
 			}
 		} catch (UnknownHostException e) {
 			//Letting the GUI know it ain't got no internet
 			_control.alertGUI(null, "You are not currently connected to the internet. SMS notification system disabled", _control.getTime());
 		} catch (Exception e) {
 			e.printStackTrace(); //What to do with these??
 		}
 		finally {
 			try {
 				if(wr!=null) wr.close();
 				if(wr!=null) rd.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		_flushed = true;
 	}
 	
 }
