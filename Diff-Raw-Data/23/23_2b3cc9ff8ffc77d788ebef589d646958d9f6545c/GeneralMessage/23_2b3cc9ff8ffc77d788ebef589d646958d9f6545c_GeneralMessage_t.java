 /**
  * 
  */
 package net.mysocio.data.messages;
 
 import net.mysocio.data.IUniqueObject;
 import net.mysocio.data.SocioObject;
 import net.mysocio.ui.data.objects.DefaultMessage;
 
 import com.github.jmkgreen.morphia.annotations.Entity;
 
 
 
 /**
  * @author Aladdin
  *
  */
 @Entity("messages")
 public abstract class GeneralMessage extends SocioObject implements IUniqueObject{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 352828420023827718L;
 	private String title =  new String();
 	private String text =  new String();
 	
 	private long date;
 
 	public long getDate() {
 		return date;
 	}
 
 	public void setDate(long date) {
 		this.date = date;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getText() {
 		return text;
 	}
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 	public abstract String getLink();
 
 	public String getUiCategory() {
 		return DefaultMessage.CATEGORY;
 	}
 
 	public String getUiName() {
 		return DefaultMessage.NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.IUniqueObject#getUniqueFieldValue()
 	 */
 	@Override
 	abstract public Object getUniqueFieldValue();
 	
 	@Override
 	abstract public String getUniqueFieldName();
 	
 	public String getButtons(){
 		return "<div class=\"MessageButton\" onclick=\"likeMessage('message.id')\"><img alt=\"Like\" src=\"images/message/Like.png\"><div>message.like</div></div>";
 	}
 	
 	public String replacePlaceholders(String template) {
 		String message = template.replace("message.title", getTitle());
 		message = message.replace("message.buttons", getButtons());
		String text2 = getText().replace("<script>", "");
		text2 = getText().replace("</script>", "");
		message = message.replace("message.text", text2);
 		message = message.replace("message.id", getId().toString());
 		String dateString = Long.toString(getDate());
 		message = message.replace("date.long", dateString);
 		message = message.replace("message.date", dateString);
 		
 		return message;
 	}
 }
