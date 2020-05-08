 /**
  * 
  */
 package module.contacts.domain;
 
 import myorg.domain.groups.IRoleEnum;
 
 /**
  * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
  * 
  */
 public enum ContactsRoles implements IRoleEnum {
     MODULE_CONTACTS_DOMAIN_CONTACTSEDITOR;
 
     @Override
     public String getRoleName() {
	return name();
     }

    public String getLocalizedName() {
	return getRoleName();
     }
 
 }
