 package de.dsi8.vhackandroidgame.communication.model;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import de.dsi8.dsi8acl.connection.model.Message;
 
 public class QRCodeMessage extends Message {
 
 	/**
 	 * If text is null, the QRCode is invisible
 	 */
 	public final String	text;
 	
 	public final QRCodePosition position;
 	
 
 	public QRCodeMessage(@JsonProperty("text") String text,
			@JsonProperty("position") QRCodePosition position) {
 		this.text = text;
 		this.position = position;
 	}
 	
	public boolean QRCodeVisible() {
		return text == null;
 	}
 	
 	public static enum QRCodePosition {
 		TOP,
 		
 		RIGHT,
 		
 		BOTTOM,
 		
 		LEFT,
 		
 		CENTER
 	}
 }
