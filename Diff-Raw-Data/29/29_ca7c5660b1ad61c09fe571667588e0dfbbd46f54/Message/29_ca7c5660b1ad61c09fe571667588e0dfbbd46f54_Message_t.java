 package org.touchirc.model;
 
 import java.security.Timestamp;
 import java.util.Date;
 
 import org.touchirc.R;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.format.Time;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.text.style.TypefaceSpan;
 import android.text.util.Linkify;
 import android.widget.TextView;
 
 public class Message {
 
 	public final static int TYPE_MESSAGE = 0;
 	public final static int TYPE_NOTICE = 1;
 	public final static int TYPE_MENTION = 2;
 	public final static int TYPE_ACTION = 3;
 	
 	private String content;
 	private String author;
 	private int type;
 	private long timestamp;
 			
 	public Message(String text) {
 		this(text, null, TYPE_MESSAGE);
 	}
 	
 	public Message(String text, String author) {
 		this(text, author, TYPE_MESSAGE);
 	}
 	
 	public Message(String text, int type) {
 		this(text,null,type);
 	}
 
 	public Message(String text, String author, int type) {
 		this.content = text;
 		this.author = author;
 		this.type = type;
 		this.timestamp = new Date().getTime();
 	}
 	
 	public String getAuthor(){
 		return this.author;
 	}
 	
 	public String getMessage(){
 		return this.content;
 	}
 	
 	public int getType(){
 		return this.type;
 	}
 	
 	private SpannableString getText(Context c){
 		// TODO Add some colors, timestamp or not, etc
 		String author = (type == TYPE_ACTION) ? this.author + " " : "<" + this.author + "> ";
 		String message = content;
 		String time = getTime(true, false) + " ";
 		
 		SpannableString span = new SpannableString(time + author + message);
 
 		if(type == TYPE_ACTION)
 			span.setSpan(new StyleSpan(Typeface.ITALIC), time.length(), span.length(), 0);
 		if(type == TYPE_MENTION){
 			span.setSpan(new ForegroundColorSpan(c.getResources().getColor(R.color.ic_red)), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			span.setSpan(new StyleSpan(Typeface.BOLD), time.length(), time.length()+author.length(), 0);
 	}
 		return span;
 	}
 	
 	public TextView getTextView(Context c){
 		TextView t = new TextView(c);
 		
 		t.setAutoLinkMask(Linkify.ALL);
 		t.setLinksClickable(true);
 		// TODO Find a color for a link :)
 		// t.setLinkTextColor(color);
 		t.setText(getText(c));
 		
 		return t;
 	}
 	
 	public String getTime(boolean use24, boolean includeSec)
     {
         Date date = new Date(timestamp);
 
         int hours = date.getHours();
         int minutes = date.getMinutes();
         int seconds = date.getSeconds();
 
         if (!use24) {
             hours = Math.abs(12 - hours);
             if (hours == 12) {
                 hours = 0;
             }
         }
 
         if (includeSec) {
             return String.format("[%02d:%02d:%02d]",hours,minutes,seconds);
         } else {
             return String.format("[%02d:%02d]",hours,minutes);
         }
     }
 	
 }
