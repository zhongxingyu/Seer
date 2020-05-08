 package com.ingloriouscoders.chatbackend;
 
 import android.R.integer;
 import android.graphics.Color;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.os.ParcelableCompat;
 import android.util.Log;
 
 
 public class Message implements Parcelable{
 	
 	private String msgtext;
 	private String sender;
 	private boolean incoming;
 	private int color;
 	
 	
 	public Message(String _msgtext, String _sender, boolean _incoming, int _color) {
 		this.msgtext = _msgtext;
 		this.sender  = _sender;
 		this.incoming = _incoming;
 		this.color = _color;
 	}
 	
 	public Message() {
		this.msgtext = "";
		this.sender  = "";
 	}
 	
 
 	public String getMessageText()
 	{
 		return msgtext;
 	}
 	public String getSender()
 	{
 		return sender;
 	}
 	public boolean getIncoming()
 	{
 		return incoming;
 	}
 	
 	public int getColor()
 	{
 		return color;
 	}
 	
 	
 	public void setMessageText(String _msgtext) {
 		this.msgtext = _msgtext;
 	}
 	public void setSender(String _sender) {
 		this.sender = _sender;
 	}
 	public void setIncoming(boolean _state)
 	{
 		this.incoming = _state;
 	}
 	public void setColor(int _color)
 	{
 		this.color = _color;
 	}
 	
 	public static Message getPlaceholder()
 	{
 		return new Message("null","null name",false,0);
 	}
 	//Parcelable Zeugs
 	
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 	   dest.writeString(msgtext);
 	   dest.writeString(sender);
 	   dest.writeInt(incoming ? 0 : 1); // Weil es kein writeBoolean() gibet
 	   dest.writeInt(color);
 	}	
 	public static final Parcelable.Creator<Message> CREATOR
 	    = new Parcelable.Creator<Message>() {
 	public Message createFromParcel(Parcel in) {
 	    return new Message(in);
 	}
 	
 	public Message[] newArray(int size) {
 	    return new Message[size];
 	}
 	};
 	
 	private Message(Parcel in) {
 		msgtext = in.readString();
 		sender = in.readString();
 		incoming = in.readInt() == 0;
 		color = in.readInt();
 	}
 	//Parcelable Zeugs ENDE
 	
 	
 }
