 /*-
  * Copyright (c) 2009, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.ui;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 import javax.microedition.io.HttpConnection;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.message.ContentPart;
 import org.logicprobe.LogicMail.message.ImageContent;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.TextContent;
 import org.logicprobe.LogicMail.message.TextPart;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.util.UnicodeNormalizer;
 
 import net.rim.blackberry.api.browser.Browser;
 import net.rim.blackberry.api.browser.BrowserSession;
 import net.rim.device.api.browser.field.BrowserContent;
 import net.rim.device.api.browser.field.Event;
 import net.rim.device.api.browser.field.RenderingApplication;
 import net.rim.device.api.browser.field.RenderingException;
 import net.rim.device.api.browser.field.RenderingSession;
 import net.rim.device.api.browser.field.RequestedResource;
 import net.rim.device.api.browser.field.UrlRequestedEvent;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.system.EncodedImage;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.ui.Field;
 
 /**
  * Creates a browser field for displaying HTML.
  */
 public class BrowserFieldRenderer implements RenderingApplication {
 	private MessageNode messageNode;
 	private TextContent content;
 	
 	private RenderingSession renderingSession;
 	private BrowserContent browserContent;
 	private boolean loadingFinished;
 	
 	/**
 	 * Instantiates a new browser field renderer.
 	 * 
 	 * @param messageNode the message node
 	 * @param content the content
 	 */
 	public BrowserFieldRenderer(MessageNode messageNode, TextContent content) {
 		this.messageNode = messageNode;
 		this.content = content;
 		this.renderingSession = RenderingSession.getNewInstance();
 	}
 
 	/**
 	 * Gets the browser field for the content this renderer was initialized with.
 	 * 
 	 * @return the browser field
 	 */
 	public Field getBrowserField() {
 		Field field = null;
 		
 		try {
 			browserContent = renderingSession.getBrowserContent(new LocalDataHttpConnection(content), this, null);
 			if (browserContent != null) {
 				field = browserContent.getDisplayableContent();
 			}
 		} catch (RenderingException e) {
 			EventLogger.logEvent(AppInfo.GUID, ("RenderingException: " + e.toString()).getBytes(), EventLogger.ERROR);
 		}
 		
 		if(field != null) {
 		    ActiveFieldManager fieldManager = new ActiveFieldManager() {
 		        protected void onDisplay() {
 		            (new Thread() { public void run() {
 		                BrowserFieldRenderer.this.finishLoading();
 		            }}).start();
 		        }
 		    };
 			fieldManager.add(field);
 			field = fieldManager;
 		}
 		return field;
 	}
 
 	/**
 	 * Does final processing of the field to render correctly.
 	 * This method should only be called after the field returned
 	 * by {@link #getBrowserField()} has been added to a manager
 	 * and rendered to the display.
 	 */
 	private void finishLoading() {
 	    if(loadingFinished) { return; }
 	    
 		if(browserContent != null) {
 			try {
 				browserContent.finishLoading();
 				loadingFinished = true;
 			} catch (RenderingException e) {
 				EventLogger.logEvent(AppInfo.GUID, ("RenderingException: " + e.toString()).getBytes(), EventLogger.ERROR);
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.browser.field.RenderingApplication#eventOccurred(net.rim.device.api.browser.field.Event)
 	 */
 	public Object eventOccurred(Event event) {
 	    if(event instanceof UrlRequestedEvent) {
 	        UrlRequestedEvent urlEvent = (UrlRequestedEvent)event;
 	        String url = urlEvent.getURL();
 	        // Make sure this event is a user-triggered HTTP GET
 	        if(!urlEvent.isProgrammatic() && urlEvent.getPostData() == null
 	                && url != null && url.length() > 0) {
 	            BrowserSession browserSession = Browser.getDefaultSession();
 	            browserSession.displayPage(url);
 	        }
 	    }
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.browser.field.RenderingApplication#getAvailableHeight(net.rim.device.api.browser.field.BrowserContent)
 	 */
 	public int getAvailableHeight(BrowserContent browserContent) {
 		return Display.getHeight();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.browser.field.RenderingApplication#getAvailableWidth(net.rim.device.api.browser.field.BrowserContent)
 	 */
 	public int getAvailableWidth(BrowserContent browserContent) {
 		return Display.getWidth();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.browser.field.RenderingApplication#getHTTPCookie(java.lang.String)
 	 */
 	public String getHTTPCookie(String url) {
 		// no cookie support
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.browser.field.RenderingApplication#getHistoryPosition(net.rim.device.api.browser.field.BrowserContent)
 	 */
 	public int getHistoryPosition(BrowserContent browserContent) {
 		// no history support
 		return 0;
 	}
 
 	/**
 	 * Returns the requested resource, finding it in the content of the {@link MessageNode}
 	 * if available.
 	 * @see net.rim.device.api.browser.field.RenderingApplication#getResource(net.rim.device.api.browser.field.RequestedResource, net.rim.device.api.browser.field.BrowserContent)
 	 */
 	public HttpConnection getResource(RequestedResource resource, BrowserContent referrer) {
 		if (resource == null) {
             return null;
         }
 
         // check if this is cache-only request
         if (resource.isCacheOnly()) {
             // no cache support
             return null;
         }
 
         String url = resource.getUrl();
 
         if (url == null) {
             return null;
         }
 
         // Find a matching content section
         if(messageNode != null) {
         	int p = url.indexOf("cid:");
         	if(p == -1 || url.length() < 5) { return null; }
         	String contentId = '<' + url.substring(4) + '>';
         	MimeMessageContent contentMatch = null;
         	
         	MimeMessageContent[] contentArray = messageNode.getAllMessageContent();
         	for(int i=0; i<contentArray.length; i++) {
         		ContentPart part = contentArray[i].getMessagePart();
         		if(contentId.equals(part.getContentId())) {
         			contentMatch = contentArray[i];
         			break;
         		}
         	}
         	
         	if(contentMatch != null) {
         		return new LocalDataHttpConnection(contentMatch);
         	}
         }
         
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.browser.field.RenderingApplication#invokeRunnable(java.lang.Runnable)
 	 */
 	public void invokeRunnable(Runnable runnable) {
 		(new Thread(runnable)).start();
 	}
 	
 	/**
 	 * Implementation that provides local message content data over a
 	 * simulated HTTP connection.
 	 */
 	private static class LocalDataHttpConnection implements HttpConnection {
 		private ContentPart contentPart;
 		private String mimeType;
 		private byte[] data;
 		
 		/**
 		 * Instantiates a new local data HTTP connection.
 		 * This method also extracts the MIME type, and attempts
 		 * to transform the data portion of the content into a
 		 * byte array.
 		 * 
 		 * @param content the message content
 		 */
 		public LocalDataHttpConnection(MimeMessageContent content) {
 			contentPart = content.getMessagePart();
 			
 			if(content instanceof TextContent) {
 				String text = getNormalizedText((TextContent)content);
 				mimeType = contentPart.getMimeType() + '/' + contentPart.getMimeSubtype();
 				
 				try {
 					data = text.getBytes("UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 			else if(content instanceof ImageContent) {
 				ImageContent imageContent = (ImageContent)content;
 				EncodedImage image = imageContent.getImage();
 				mimeType = image.getMIMEType();
 				data = image.getData();
 			}
 			else {
 				mimeType = contentPart.getMimeType() + '/' + contentPart.getMimeSubtype();
 			}
 			
 			if(data == null) { data = new byte[0]; }
 		}
 		
 		public long getDate() throws IOException {
 			return 0;
 		}
 
 		public long getExpiration() throws IOException {
 			return 0;
 		}
 
 		public String getFile() {
 			return null;
 		}
 
 		public String getHeaderField(String name) throws IOException {
 			String result;
 			if(name.equalsIgnoreCase("content-type")) {
 				result = getType();
 			}
 			else if(name.equalsIgnoreCase("content-length")) { 
 				result = Long.toString(getLength());
 			}
 			else if(name.equalsIgnoreCase("content-location")) {
 				result = getURL();
 			}
 			else if(name.equalsIgnoreCase("accept-charset")) { 
 				result = "UTF-8";
 			}
 			else {
 				result = null;
 			}
 			return result;
 		}
 
 		public String getHeaderField(int n) throws IOException {
 			return "";
 		}
 
 		public long getHeaderFieldDate(String name, long def)
 				throws IOException {
 			return def;
 		}
 
 		public int getHeaderFieldInt(String name, int def) throws IOException {
 			return def;
 		}
 
 		public String getHeaderFieldKey(int n) throws IOException {
 			return null;
 		}
 
 		public String getHost() {
 			return "localhost";
 		}
 
 		public long getLastModified() throws IOException {
 			return 0;
 		}
 
 		public int getPort() {
 			return 80;
 		}
 
 		public String getProtocol() {
 			return "http";
 		}
 
 		public String getQuery() {
 			return null;
 		}
 
 		public String getRef() {
 			return null;
 		}
 
 		public String getRequestMethod() {
 			return "GET";
 		}
 
 		public String getRequestProperty(String key) {
 			return null;
 		}
 
 		public int getResponseCode() throws IOException {
 			return 200;
 		}
 
 		public String getResponseMessage() throws IOException {
 			return "OK";
 		}
 
 		public String getURL() {
 			return "http://localhost";
 		}
 
 		public void setRequestMethod(String method) throws IOException {
 		}
 
 		public void setRequestProperty(String key, String property)
 				throws IOException {
 		}
 
 		public String getEncoding() {
 			return null;
 		}
 
 		public long getLength() {
 			return data.length;
 		}
 
 		public String getType() {
 			return mimeType;
 		}
 
 		public DataInputStream openDataInputStream() throws IOException {
 			return new DataInputStream(openInputStream());
 		}
 
 		public InputStream openInputStream() throws IOException {
 			return new ByteArrayInputStream(data);
 		}
 
 		public void close() throws IOException {
 		}
 
 		public DataOutputStream openDataOutputStream() throws IOException {
 			// Output is not supported
 			return null;
 		}
 
 		public OutputStream openOutputStream() throws IOException {
 			// Output is not supported
 			return null;
 		}
 		
 	}
 
 	/**
      * Run the Unicode normalizer on the provide content,
      * only if normalization is enabled in the configuration.
      * If normalization is disabled, this method returns
      * the input unmodified.
      * 
      * @param input Input content
      * @return Normalized string
      */
     private static String getNormalizedText(TextContent content) {
         if(MailSettings.getInstance().getGlobalConfig().getUnicodeNormalization()) {
         	String text = content.getText();
        	
            // If the encoding type is QP, then we need to do a charset
            // juggle to make sure that the data is using the right
            // characters instead of ISO-8859-1 characters
        	if(((TextPart)content.getMessagePart()).getEncoding().equalsIgnoreCase("quoted-printable")) {
                try{
                    byte[] encodedBytes = text.getBytes();
                    text = new String(encodedBytes, ((TextPart)content.getMessagePart()).getCharset());
                } catch(Exception e){}
        	}
        	
         	return UnicodeNormalizer.getInstance().normalize(text);
         }
         else {
             return content.getText();
         }
     }
 }
