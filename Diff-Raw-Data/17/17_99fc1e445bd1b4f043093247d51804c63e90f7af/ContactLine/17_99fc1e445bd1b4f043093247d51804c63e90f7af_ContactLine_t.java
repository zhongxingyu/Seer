 /**
  * 
  */
 package net.mysocio.ui.data.objects;
 
 import net.mysocio.data.ui.UiObject;
 
 import com.google.code.morphia.annotations.Entity;
 
 /**
  * @author Aladdin
  *
  */
 @Entity("ui_objects")
 public class ContactLine extends UiObject {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7232276926453378696L;
	public static final String NAME = "Contact";
 	public static final String CATEGORY = "ContactLine";
 	
 	public ContactLine(){
 		super();
 		setName(NAME);
 		setCategory(CATEGORY);
		addTextLabel("contact.username");
		addTextLabel("contact.userpic");
 	}
 	@Override
 	public String getPageFile() {
		return "contact.html";
 	}
 }
