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
 
 import net.rim.blackberry.api.pdap.BlackBerryContactGroup;
 import net.rim.blackberry.api.pdap.BlackBerryContactList;
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.ui.ContextMenu;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.EditField;
 
 import javax.microedition.pim.Contact;
 import javax.microedition.pim.PIM;
 import javax.microedition.pim.PIMException;
 import javax.microedition.pim.PIMItem;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.util.StringParser;
 
 
 /**
  * Implements a field for entering e-mail addresses, supporting the
  * ability to input addresses from the address book.
  */
 public class EmailAddressBookEditField extends EditField {
 	protected static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
 
 	public final static int ADDRESS_TO = 1;
     public final static int ADDRESS_CC = 2;
     public final static int ADDRESS_BCC = 3;
     
     private final static int MODE_ADDRESS = 1;
     private final static int MODE_NAME = 2;
     
     private final static char[] invalidChars = { '<', '>', '\"', '{', '}', '|', '\\', '^', '[', ']', '`' };
     
     private String name;
     private String address;
     private int addressMode;
     private int addressType;
 
     private MenuItem addressPropertiesMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_PROPERTIES),
             200000, 10) {
             public void run() {
                 addressProperties();
             }
         };
 
     private MenuItem addressBookMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_SELECT_ADDRESS),
     		200010, 10) {
             public void run() {
                 addressBookChooser();
             }
         };
 
     /**
      * Creates a new instance of EmailAddressBookEditField.
      * @param addressType The type of address (ADDRESS_TO, ADDRESS_CC, or ADDRESS_BCC)
      * @param initialValue The initial value of the field
      */
     public EmailAddressBookEditField(int addressType, String initialValue) {
         super("", "");
 
         setAddressType(addressType);
         setText(initialValue);
     }
 
     /**
      * Sets the address type.
      * Will default to ADDRESS_TO if an invalid type is passed.
      * 
      * @param addressType The type of address (ADDRESS_TO, ADDRESS_CC, or ADDRESS_BCC)
      */
     public void setAddressType(int addressType) {
         switch(addressType) {
         case ADDRESS_TO:
         	this.setLabel(resources.getString(LogicMailResource.MESSAGEPROPERTIES_TO) + ' ');
         	break;
         case ADDRESS_CC:
         	this.setLabel(resources.getString(LogicMailResource.MESSAGEPROPERTIES_CC) + ' ');
         	break;
         case ADDRESS_BCC:
         	this.setLabel(resources.getString(LogicMailResource.MESSAGEPROPERTIES_BCC) + ' ');
         	break;
     	default:
     		this.setLabel(resources.getString(LogicMailResource.MESSAGEPROPERTIES_TO) + ' ');
     		addressType = ADDRESS_TO;
         }
 
         this.addressType = addressType;
     }
 
     /**
      * Gets the address type.
      * 
      * @return the address type
      */
     public int getAddressType() {
     	return this.addressType;
     }
     
     /**
      * Sets the address mode.
      * Also makes any other field state changes
      * that go along with the new mode.
      * 
      * @param addressMode the new address mode
      */
     private void setAddressMode(int addressMode) {
     	this.addressMode = addressMode;
     	switch(addressMode) {
     	case MODE_ADDRESS:
         	super.setText(this.address);
         	super.setEditable(true);
         	break;
     	case MODE_NAME:
             super.setText(this.name);
             super.setEditable(false);
             break;
     	}
     	this.invalidate();
     }
     
     /**
      * Set the address contained within the field.
      * 
      * @return Address in the standard format
      * 
      * @see net.rim.device.api.ui.component.BasicEditField#getText()
      */
     public String getText() {
     	return StringParser.mergeRecipient(name, address);
     }
 
     /**
      * Set the address contained within the field.
      * Supports handling the "John Doe &lt;jdoe@generic.org&gt;" format.
      * 
      * @param text Address to set the field to
      */
     public void setText(String text) {
     	
     	String[] recipient = StringParser.parseRecipient(text);
     	this.name = recipient[0];
     	this.address = recipient[1];
     	
         // Sanity check for empty addresses
         if (this.address.length() == 0) {
         	setAddressMode(MODE_ADDRESS);
             return;
         }
 
         // Determine whether we are in MODE_ADDRESS or MODE_NAME
         // and configure the field accordingly
         if (this.name != null) {
             setAddressMode(MODE_NAME);
         }
         else {
         	setAddressMode(MODE_ADDRESS);
         }
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.component.BasicEditField#makeContextMenu(net.rim.device.api.ui.ContextMenu)
      */
     protected void makeContextMenu(ContextMenu contextMenu) {
     	if (addressMode == MODE_NAME) {
             contextMenu.addItem(addressPropertiesMenuItem);
         }
 
         contextMenu.addItem(addressBookMenuItem);
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.component.BasicEditField#keyChar(char, int, int)
      */
     protected boolean keyChar(char key, int status, int time) {
     	boolean result = false;
 
     	if(addressMode == MODE_NAME && status == 0) {
 	    	switch(key) {
 	        case Keypad.KEY_BACKSPACE:
 	        case Keypad.KEY_DELETE:
 	        	// Empty the field and sets us
 	        	// back into address mode.
                 name = null;
                 address = "";
                 setAddressMode(MODE_ADDRESS);
                 setFocus();
                 result = true;
 	            break;
 	        }
     	}
     	else {
     		switch(key) {
     		case Keypad.KEY_SPACE:
     			if(address.indexOf('@') == -1) {
     				result = super.keyChar('@', status, time);
     			}
     			else {
     				result = super.keyChar('.', status, time);
     			}
     			break;
 			default:
 				// Ignore any invalid characters
 				for(int i=0; i<invalidChars.length; i++) {
     				if(key == invalidChars[i]) { result = true; break; }
     			}
     		}
     	}
     	if(!result) {
     		result = super.keyChar(key, status, time);
     	}
         return result;
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.component.TextField#fieldChangeNotify(int)
      */
     protected void fieldChangeNotify(int context) {
     	if(addressMode == MODE_ADDRESS) {
     		this.address = super.getText();
     	}
     	super.fieldChangeNotify(context);
     }
     
     /**
      * Handle the address properties dialog
      */
     private void addressProperties() {
         String localName = this.name;
         String localAddress = this.address;
 
         EmailAddressPropertiesDialog dialog =
         	new EmailAddressPropertiesDialog(localName, localAddress);
 
         if (dialog.doModal() == Dialog.OK) {
             if (!localName.equals(dialog.getName()) ||
                     !localAddress.equals(dialog.getAddress())) {
                 localName = dialog.getName();
                 localAddress = dialog.getAddress();
 
                 if (localName.length() > 0) {
                     this.name = localName;
                 } else {
                     this.name = null;
                 }
 
                 this.address = localAddress;
 
                 if (this.name != null) {
                 	setAddressMode(MODE_NAME);
                 } else {
                 	setAddressMode(MODE_ADDRESS);
                 }
             }
         }
     }
 
     /**
      * Handle choosing an address from the address book
      */
     private void addressBookChooser() {
     	Contact contact = null;
 		try {
 			BlackBerryContactList list = (BlackBerryContactList)PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
 	    	PIMItem item = list.choose();
 	    	if (item instanceof Contact) {
 		    	contact = (Contact)item;
 	    	}
 	    	else if (item instanceof BlackBerryContactGroup) {
 	    		BlackBerryContactGroup contactGroup = (BlackBerryContactGroup)item;
 	    		addContactGroup(contactGroup);
 	    	}
 		} catch (PIMException e) {
 			EventLogger.logEvent(AppInfo.GUID,
                 ("Unable to open contact list:\r\n" + e.toString()).getBytes(),
                 EventLogger.ERROR);
 			return;
 		}
     	
 		if(contact != null) {
 			String contactName;
 			String[] contactEmail;
 
             String[] values = contact.getStringArray(Contact.NAME, 0);
             contactName = values[1] + ' ' + values[0];
 
             int count = contact.countValues(Contact.EMAIL);
             contactEmail = new String[count];
             for (int i = 0; i < count; i++) {
             	contactEmail[i] = contact.getString(Contact.EMAIL, i);
             }
             
             if(count > 1) {
               Dialog addressDialog = new Dialog(
             		  resources.getString(LogicMailResource.EMAILADDRESSBOOKEDIT_WHICH_ADDRESS),
             		  contactEmail, null, 0,
             		  Bitmap.getPredefinedBitmap(Bitmap.QUESTION));
               int choice = addressDialog.doModal();
               if(choice != -1) {
 	              address = contactEmail[choice];
 	              name = contactName;
 	              setAddressMode(MODE_NAME);
               }
             }
             else if(count == 1) {
 				address = contactEmail[0];
 				name = contactName;
 				setAddressMode(MODE_NAME);
             }
             else {
             	Dialog.alert(resources.getString(LogicMailResource.EMAILADDRESSBOOKEDIT_ALERT_NO_ADDRESS));
             }
 		}
     }
 
     /**
      * Adds a selected contact group.
      * 
      * @param contactGroup the contact group
      */
     private void addContactGroup(BlackBerryContactGroup contactGroup) {
     	// Implementing this either requires having the composition
     	// screen add multiple recipient lines, or actually supporting
     	// contact groups directly in this field
     	Dialog.alert(resources.getString(LogicMailResource.EMAILADDRESSBOOKEDIT_ALERT_GROUPS_UNSUPPORTED));
     }
 }
