 package com.aaasen.smsvis.util;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import android.text.TextUtils;
 
 /**
  * A simple container for SMS message data.
  * 
  * This class only contains very basic fields relevant to data analysis;
  * if you want to make a messaging app or something that requires more data,
  * you will need to extend it.
  * 
  * @author 
  */
 
 public class SMS implements Serializable {
 	private static final long serialVersionUID = -4816869712631136352L;
 	private String body, address;
 	private Date date;
 	private boolean sent;
 
 	public SMS(String body, String address, Date date, int type) {
 		this.setBody(TextUtils.htmlEncode(body));
 		this.setAddress(PhoneUtils.formatNumber(address));
 		this.setDate(date);
 		this.setSent(type);
 	}
 
 	public SMS(String body, String address, String date, String type) {
 		this(body, address, 
 				new Date(Long.parseLong(date)),
 				Integer.parseInt(type));
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(" type: " + (sent ? "sent" : "received"));
 		sb.append(" time: " + date.toString());
 		sb.append(" from: " + address);
 		sb.append(" body: \"" + body + "\"");
 		return sb.toString();
 	}
 
 	public String getBody() { return body; }
 	private void setBody(String body) { this.body = body; }
 
 	public String getAddress() { return address; }
 	private void setAddress(String address) { this.address = address; }
 
 	public Date getDate() { return date; }
 	private void setDate(Date date) { this.date = date; }
 
 	public boolean isSent() { return sent; }
 	private void setSent(boolean sent) { this.sent = sent; }
	private void setSent(int type) { this.setSent(this.sent = type - 1 == 1); }
 }
 
 
