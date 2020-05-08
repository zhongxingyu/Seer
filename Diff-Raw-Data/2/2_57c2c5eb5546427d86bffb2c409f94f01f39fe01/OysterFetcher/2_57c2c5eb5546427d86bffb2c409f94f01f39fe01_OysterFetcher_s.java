 package com.papagiannis.tuberun.fetchers;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.http.impl.client.BasicCookieStore;
 
 public class OysterFetcher extends Fetcher {
 	private static final long serialVersionUID = 1L;
 	private String username="";
 	private String password="";
 	private String oyster_no="";
 	private String oyster_balance="";
 	private HashMap<String,String> cards=new HashMap<String,String>();
 	@SuppressWarnings("deprecation")
 	private Date update_time=new Date(2000,1,1);
 	private transient RequestTask task=null;
 
 	private static  HashMap<String,OysterFetcher> instances =new HashMap<String, OysterFetcher>();
 	public static synchronized OysterFetcher getInstance(String username, String password) {
 		if (!instances.containsKey(username+password)) {
 			instances.put(username+password,new OysterFetcher(username, password));
 		}
 		return instances.get(username+password);
 	}
 	
 	private OysterFetcher(String username, String password) {
 		super();
 		this.username=(username==null)?"":username.trim();
 		this.password=(password==null)?"":password.trim();
 	}
 
 	@Override
 	public Date getUpdateTime() {
 		return update_time;
 	}
 
 	BasicCookieStore cookies;
 	StringBuilder postData;
 	private String errors="";
 	public String getErrors() {
 		return errors;
 	}
 
 	protected AtomicBoolean isFirst = new AtomicBoolean(true);
 	
 	@Override
 	public void update() {
 		boolean first = isFirst.compareAndSet(true, false);
 		if (!first)	return; // only one at a time
 		errors="";
 		cards.clear();
 		cardsReturned.set(0);
 		postData = new StringBuilder();
 		cookies = new BasicCookieStore();
 		String q1 = "https://oyster.tfl.gov.uk/oyster/security_check";
 		postData.append("j_username="+username+"&j_password="+password+"&Sign+in=Sign+in");
 
 		PostRequestTask r = new PostRequestTask(new HttpCallback() {
 			public void onReturn(String s) {
 				getCallBack1(s);
 			}
 		});
 		r.setPostData(postData);
 		r.setCookies(cookies);
 		task=r;
 		r.execute(q1);
 	}
 
 	String param = "";
 	private int totalCards=0;
 	private AtomicInteger cardsReturned=new AtomicInteger(0);
 
 	private void getCallBack1(String response) {
 		try {
 			if (response==null || response.equals("")) 
 				throw new Exception("The server oyster.tfl.gov.uk did not respond to your request (1)");
 			if (response.contains("Login failed")) {
 				throw new Exception("Login failed, please check your credentials.");
 			}
 			int i=response.indexOf("Choose card number");
 			int j=response.indexOf("Balance: &pound;");
 			if (i>0) {
 				totalCards=0;
 				response=response.substring(i);
 				while (true) {
 					String mark="<option value=\"";
 					int ni=response.indexOf(mark);
 					if (ni==-1) {
 						if (totalCards==0) throw new Exception("No cards detected"); 
 						break;
 					}
 					response=response.substring(ni+mark.length());
 					int end=response.indexOf("\"");
 					oyster_no=response.substring(0, end);
 					response=response.substring(end);
 					
 					postData = new StringBuilder();
 					String q = "https://oyster.tfl.gov.uk/oyster/selectCard.do";
 					postData.append("method=input&cardId="+oyster_no);
 
 					PostRequestTask r = new PostRequestTask(new HttpCallback() {
 						public void onReturn(String s) {
 							getCallBack2(s);
 						}
 					});
 					r.setPostData(postData);
 					r.setCookies(cookies);
 					task=r;
 					r.execute(q);
 					totalCards++;
 				}
 				
 				//multiple cards parsing
 			}
 			else if (j>0) {
 				totalCards=1;
 				//single card parsing
 				getCallBack2(response);
 			}
 			else {
 				errors+="Failed to locate card number";
 				isFirst.set(true);
 				notifyClients();
 			}
 			return;
 
 		} catch (Exception e) {
 			errors+=e.getMessage();
 			isFirst.set(true);
 			notifyClients();
 		}
 	}
 	
 	private void getCallBack2(String response) {
 		try {
 			if (response==null || response.equals("")) 
 				throw new Exception("The server tfl.gov.uk did not respond to your request (3)");
 			String mark="<h2>Card No: ";
 			int i=response.indexOf(mark);
 			if (i<0) throw new Exception("Cannot parse server response");
 			response=response.substring(i+mark.length());
 			String number=response.substring(0,response.indexOf("</h2>"));
 			
 			response=response.substring(response.indexOf("</h2>"));
 			
 			mark="Balance: &pound;";
 			i=response.indexOf(mark);
 			if (i<0) {
 				mark="&pound;";
 				i=response.indexOf(mark);
 			}
 			if (i<0) throw new Exception("Cannot parse server response");
 			response=response.substring(i+mark.length());
			oyster_balance=""+response.substring(0,response.indexOf("</span>"));
 			
 			cards.put(number, oyster_balance);
 			update_time=new Date();
 			
 			cardsReturned.incrementAndGet();
 			
 		} catch (Exception e) {
 			errors+=e.getMessage();
 		} finally {
 			isFirst.set(true);
 			if (totalCards==cardsReturned.get()) notifyClients();
 		}
 	}
 	
 	public Boolean hasResult() {
 		Date now=new Date();
 		Boolean isFresh = (now.getTime() - update_time.getTime()) / 1000 < 5 * 60;
 		return isFresh && !isErrorResult() && !oyster_balance.equals("");
 	}
 
 	public CharSequence getResult() {
 		if (!errors.equals("")) return "ERROR";
 		else return oyster_balance;
 	}
 	
 	public boolean isErrorResult() {
 		return !errors.equals("");
 	}
 
 	@Override
     public void abort() {
 		isFirst.set(true);
     	if (task!=null) task.cancel(true);
     }
 
 	public HashMap<String,String> getCards() {
 		return cards;
 	}
 	
 }
