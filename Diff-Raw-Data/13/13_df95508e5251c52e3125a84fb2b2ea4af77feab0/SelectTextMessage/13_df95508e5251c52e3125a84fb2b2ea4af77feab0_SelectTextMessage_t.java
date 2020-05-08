 /**
  * 
  */
 package name.webdizz.clt.crx.client.event.message;
 
 import com.google.gwt.dom.client.NativeEvent;
 
 public class SelectTextMessage extends Message {
 
 	public static final String TYPE = "SelectTextMessage";
 
 	protected SelectTextMessage() {
 	}
 
 	public final native String getText() /*-{
 		return this.text;
 	}-*/;
 
 	public final native int getOffsetX() /*-{
 		return this.offsetX;
 	}-*/;
 
 	public final native int getOffsetY() /*-{
 		return this.offsetY;
 	}-*/;
 
 	public static final native SelectTextMessage create(String text, NativeEvent event)/*-{
		return {text: text, type:'SelectTextMessage', offsetX: event.clientX, offsetY: event.pageY};
 	}-*/;
 
 }
