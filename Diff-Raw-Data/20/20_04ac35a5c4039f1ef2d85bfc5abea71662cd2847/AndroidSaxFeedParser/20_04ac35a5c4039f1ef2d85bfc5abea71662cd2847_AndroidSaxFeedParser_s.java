 package com.ybi.ragequit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 
 import android.sax.Element;
 import android.sax.EndElementListener;
 import android.sax.EndTextElementListener;
 import android.sax.RootElement;
 import android.sax.StartElementListener;
 import android.util.Xml;
 
 /**
  * 
  *
  */
 public class AndroidSaxFeedParser extends BaseFeedParser {
 
 	public AndroidSaxFeedParser(String feedUrl) {
 		super(feedUrl);
 	}
 
 	@Override
 	public List<Message> parse() {
 		final Message currentMessage = new Message();
 		RootElement root = new RootElement("rss");
 		final List<Message> messages = new ArrayList<Message>();
 		Element channel = root.getChild("channel");
 		Element item = channel.getChild(ITEM);
 		item.setEndElementListener(new EndElementListener() {
 			@Override
 			public void end() {
 				messages.add(currentMessage.copy());
 			}
 		});
 		item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener() {
 			@Override
 			public void end(String body) {
 				currentMessage.setTitle(body);
 			}
 		});
 		item.getChild(LINK).setEndTextElementListener(new EndTextElementListener() {
 			@Override
 			public void end(String body) {
 				currentMessage.setLink(body);
 			}
 		});
 		item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener() {
 			@Override
 			public void end(String body) {
 				currentMessage.setDescription(body);
 			}
 		});
 		item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener() {
 			@Override
 			public void end(String body) {
 				currentMessage.setDate(body);
 			}
 		});
 		item.getChild(MEDIA, THUMBNAIL).setStartElementListener(new StartElementListener() {
 
 			@Override
 			public void start(Attributes attributes) {
 				currentMessage.setMediaThumbnail(attributes.getValue("url"));
 			}
 
 		});
 		item.getChild(MEDIA, CONTENT).setStartElementListener(new StartElementListener() {
 
 			@Override
 			public void start(Attributes attributes) {
				currentMessage.setMediaThumbnail(attributes.getValue("url"));
 			}
 
 		});
 
 		try {
 			Xml.parse(getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return messages;
 	}
 }
