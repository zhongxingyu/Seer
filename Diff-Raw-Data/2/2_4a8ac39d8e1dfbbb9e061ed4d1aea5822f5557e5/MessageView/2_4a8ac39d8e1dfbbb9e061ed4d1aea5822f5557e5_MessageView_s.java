 package com.ioabsoftware.DroidFAQs.Views;
 
 import java.util.HashMap;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.ioabsoftware.DroidFAQs.AllInOneV2;
 import com.ioabsoftware.DroidFAQs.HandlesNetworkResult.NetDesc;
 import com.ioabsoftware.DroidFAQs.Networking.Session;
 import com.ioabsoftware.gameraven.R;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.text.Html;
 import android.text.Html.ImageGetter;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.TextPaint;
 import android.text.method.LinkMovementMethod;
 import android.text.method.MovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MessageView extends LinearLayout implements View.OnClickListener {
 
 	private String userContent, messageID, boardID, topicID;
 	private Element messageContent;
 	private AllInOneV2 aio;
 	
 	public String getUser() {
 		return userContent;
 	}
 	
 	public String getMessageID() {
 		return messageID;
 	}
 	
 	public String getTopicID() {
 		return topicID;
 	}
 	
 	public String getBoardID() {
 		return boardID;
 	}
 	
 	public String getMessageDetailLink() {
 		return Session.ROOT + "/boards/" + boardID + "/" + topicID + "/" + messageID;
 	}
 	
 	public String getUserDetailLink() {
		return Session.ROOT + "/users/" + userContent + "/boards";
 	}
 	
 	
 	
 	public MessageView(final AllInOneV2 aioIn, String userIn, String userTitles, String postNum,
 					   String postTimeIn, Element messageIn, String BID, String TID, String MID) {
 		super(aioIn);
 		
 		aio = aioIn;
         
         userContent = userIn;
         messageContent = messageIn;
         messageID = MID;
         topicID = TID;
         boardID = BID;
 		
 		LayoutInflater inflater = (LayoutInflater) aio.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.msgview, this);
         
         ((TextView) findViewById(R.id.mvUser)).setText(userContent + userTitles);
         ((TextView) findViewById(R.id.mvPostNumber)).setText("#" + postNum + ", " + postTimeIn);
         
         String html = null;
         if (messageContent.getElementsByClass("board_poll").isEmpty()) {
         	html = messageContent.html();
 		}
         else {
         	Element ed = messageContent.clone();
         	ed.getElementsByClass("board_poll").first().remove();
         	html = ed.html();
 
     		LinearLayout pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);
     		pollWrapper.setPadding(5, 0, 5, 5);
 			pollWrapper.addView(new HeaderView(aio, messageContent.getElementsByClass("poll_head").first().text()));
         	
         	if (messageContent.getElementsByTag("form").isEmpty()) {
         		// poll_foot_left
         		TextView t;
         		for (Element e : messageContent.getElementsByClass("table_row")) {
         			Elements c = e.children();
         			t = new TextView(aio);
         			t.setText(c.get(0).text() + ": " + c.get(1).text() + ", " + c.get(3).text() + " votes");
         			pollWrapper.addView(t);
         		}
         		
         		String foot = messageContent.getElementsByClass("poll_foot_left").text();
         		if (foot.length() > 0) {
         			t = new TextView(aio);
         			t.setText(foot);
         			pollWrapper.addView(t);
         		}
         		
         	}
         	else {
             	final String action = "/boards/" + boardID + "/" + topicID;
     			String key = messageContent.getElementsByAttributeValue("name", "key").attr("value");
     			
     			int x = 0;
     			for (Element e : messageContent.getElementsByAttributeValue("name", "poll_vote")) {
     				x++;
     				Button b = new Button(aio);
     				b.setText(e.nextElementSibling().text());
     				final HashMap<String, String> data = new HashMap<String, String>();
     				data.put("key", key);
     				data.put("poll_vote", Integer.toString(x));
     				data.put("submit", "Vote");
     				b.setOnClickListener(new OnClickListener() {
     					@Override
     					public void onClick(View v) {
     						aio.getSession().post(NetDesc.TOPIC, action, data);
     					}
     				});
     				pollWrapper.addView(b);
     			}
     			
     			Button b = new Button(aio);
     			b.setText("View Results");
     			b.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						aio.getSession().get(NetDesc.TOPIC, action + "?results=1", null);
 					}
 				});
 				pollWrapper.addView(b);
     			
     			pollWrapper.setVisibility(View.VISIBLE);
         	}
         }
         
         TextView message = (TextView) findViewById(R.id.mvMessage);
         message.setText(Html.fromHtml(html, null, null));
         Linkify.addLinks(message, Linkify.WEB_URLS);
         
         findViewById(R.id.mvTopWrapper).setOnClickListener(this);
         findViewById(R.id.mvTopWrapper).setBackgroundResource(R.drawable.msgheadselector);
 	}
 
 	@Override
 	public void onClick(View v) {
 		aio.messageMenuClicked(MessageView.this);
 	}
 	
 	public String getMessageForQuoting() {
 		return processContent(true);
 	}
 	
 	public String getMessageForEditing() {
 		return processContent(false);
 	}
 
 	private String processContent(boolean removeSig) {
 		Element clonedBody = messageContent.getElementsByClass("msg_body").first().clone();
 		if (!clonedBody.getElementsByClass("board_poll").isEmpty()) {
 			clonedBody.getElementsByClass("board_poll").first().remove();
 		}
 
 		String finalBody = clonedBody.html();
 
 		if (removeSig) {
 			int sigStart = finalBody.lastIndexOf("---");
 			if (sigStart != -1)
 				finalBody = finalBody.substring(0, sigStart);
 		}
 		
 		finalBody = finalBody.replace("<span class=\"fspoiler\">", "<spoiler>").replace("</span>", "</spoiler>");
 		
 		while (finalBody.contains("<a href")) {
 			int start = finalBody.indexOf("<a href");
 			int end = finalBody.indexOf(">", start) + 1;
 			finalBody = finalBody.replace(finalBody.substring(start, end), "");
 		}
 		finalBody = finalBody.replace("</a>", "");
 		if (finalBody.endsWith("<br />"))
 			finalBody = finalBody.substring(0, finalBody.length() - 6);
 		finalBody = finalBody.replace("\n", "");
 		finalBody = finalBody.replace("<br />", "\n");
 		
 		finalBody = StringEscapeUtils.unescapeHtml4(finalBody);
 		return finalBody;
 	}
 }
