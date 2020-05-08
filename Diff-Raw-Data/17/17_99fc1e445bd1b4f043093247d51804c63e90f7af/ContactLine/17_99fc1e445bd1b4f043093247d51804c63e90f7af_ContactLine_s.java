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
	public static final String NAME = "Account";
 	public static final String CATEGORY = "ContactLine";
 	
 	public ContactLine(){
 		super();
 		setName(NAME);
 		setCategory(CATEGORY);
		addTextLabel("account.make.primary");
		addTextLabel("account.remove");
 	}
 	@Override
 	public String getPageFile() {
		return "account.html";
 	}
 }
