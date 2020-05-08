 /**
  * 
  */
 package name.webdizz.clt.crx.client.event.message;
 
 /**
  * @author Izzet_Mustafayev
  * 
  */
 public class ShowTranslatedTextMessage extends Message {
 
 	public static final String TYPE = "ShowTranslatedTextMessage";
 
 	protected ShowTranslatedTextMessage() {
 	}
 
 	public final native String getWidget() /*-{
 		return this.widget;
 	}-*/;
 
 	public final native SelectTextMessage getMessage() /*-{
		return this.message;
 	}-*/;
 
 	public static final native ShowTranslatedTextMessage create(
 			SelectTextMessage message, String widget)/*-{
 		return {widget: widget, message: message, type:'ShowTranslatedTextMessage'};
 	}-*/;
 }
