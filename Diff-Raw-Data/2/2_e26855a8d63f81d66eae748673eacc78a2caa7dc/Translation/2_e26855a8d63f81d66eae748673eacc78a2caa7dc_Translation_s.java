 /**
  * 
  */
 package name.webdizz.clt.crx.client.db;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 /**
  * @author Izzet_Mustafayev
  * 
  */
public class Translation extends JavaScriptObject {
 
 	protected Translation() {
 	}
 
 	/**
 	 * @return the id
 	 */
 	public native int getId() /*-{
 		return this.id;
 	}-*/;
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public native void setId(int id) /*-{
 		this.id = id;
 	}-*/;
 
 	/**
 	 * @return the text
 	 */
 	public native String getText() /*-{
 		return this.text;
 	}-*/;
 
 	/**
 	 * @param text
 	 *            the text to set
 	 */
 	public native void setText(String text) /*-{
 		this.text = text;
 	}-*/;
 
 	/**
 	 * @return the translation
 	 */
 	public native String getTranslation() /*-{
 		return this.translation;
 	}-*/;
 
 	/**
 	 * @param translation
 	 *            the translation to set
 	 */
 	public native void setTranslation(String translation) /*-{
 		this.translation = translation;
 	}-*/;
 
 	/**
 	 * @return the src
 	 */
 	public native String getSrc() /*-{
 		return this.src;
 	}-*/;
 
 	/**
 	 * @param src
 	 *            the src to set
 	 */
 	public native void setSrc(String src) /*-{
 		this.src = src;
 	}-*/;
 
 	/**
 	 * @return the dest
 	 */
 	public native String getDest() /*-{
 		return this.dest;
 	}-*/;
 
 	/**
 	 * @param dest
 	 *            the dest to set
 	 */
 	public native void setDest(String dest) /*-{
 		this.dest = dest;
 	}-*/;
 
 	/**
 	 * @return the translated
 	 */
 	public native Date getTranslated() /*-{
 		return new Date(this.translated);
 	}-*/;
 
 	/**
 	 * @param translated
 	 *            the translated to set
 	 */
 	public native void setTranslated(Date translated) /*-{
 		this.translated = translated.getTime();
 	}-*/;
 
 	public static final native Translation instance() /*-{
 		return {};
 	}-*/;
 
 }
