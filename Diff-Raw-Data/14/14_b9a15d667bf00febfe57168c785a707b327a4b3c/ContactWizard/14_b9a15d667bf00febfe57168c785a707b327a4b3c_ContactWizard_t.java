 package de.hswt.hrm.contact.ui.wizard;
 
 import java.util.HashMap;
 
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.contact.service.ContactService;
 
 public class ContactWizard extends Wizard {
 
     private static final Logger LOG = LoggerFactory.getLogger(ContactWizard.class);
     private ContactWizardPageOne first;
     private Optional<Contact> contact;
 
     public ContactWizard(Optional<Contact> contact) {
 
         this.contact = contact;
         first = new ContactWizardPageOne("First Page", contact);
         setWindowTitle("Neuen Kontakt hinzufügen");
 
     }
 
     @Override
     public void addPages() {
         addPage(first);
     }
 
     @Override
     public boolean canFinish() {
         return first.isPageComplete();
     }
 
     @Override
     public boolean performFinish() {
        if (!contact.isPresent()) {
            LOG.debug("contact is not present");
            return insertNewContact();

        }
        else {
            LOG.debug("contact is present");
             return editExistingCustomer();
         }

     }
 
     private boolean editExistingCustomer() {
         Contact c = this.contact.get();
         try {
             c = ContactService.findById(c.getId());
            c = setContent(contact);
             ContactService.update(c);
             contact = Optional.fromNullable(c);
 
         }
         catch (DatabaseException e1) {
             LOG.error("An error occured", e1);
         }
 
         return true;
 
     }
 
     private boolean insertNewContact() {
         Contact c = null;
         c = setContent(Optional.fromNullable(c));
 
         try {
             c = ContactService.insert(c);
             contact = Optional.fromNullable(c);
         }
         catch (SaveException e) {
             LOG.error("Could not save Element: " + contact + "into Database", e);
         }
 
         return true;
     }
 
     public Optional<Contact> getContact() {
         return contact;
     }
 
     private Contact setContent(Optional<Contact> c) {
 
         Contact contact;
 
         HashMap<String, Text> mandatoryWidgets = first.getMandatoryWidgets();
         String firstName = mandatoryWidgets.get("firstName").getText();
         String lastName = mandatoryWidgets.get("lastName").getText();
         String street = mandatoryWidgets.get("street").getText();
         String streetNumber = mandatoryWidgets.get("streetNumber").getText();
         String city = mandatoryWidgets.get("city").getText();
         String zipCode = mandatoryWidgets.get("zipCode").getText();
 
         HashMap<String, Text> optionalWidgets = first.getOptionalWidgets();
         String shortcut = optionalWidgets.get("shortcut").getText();
         String phone = optionalWidgets.get("phone").getText();
         String fax = optionalWidgets.get("fax").getText();
         String mobilePhone = optionalWidgets.get("mobilePhone").getText();
         String email = optionalWidgets.get("email").getText();
 
         if (c.isPresent()) {
 
             contact = c.get();
 
             contact.setFirstName(firstName);
             contact.setLastName(lastName);
             contact.setStreet(street);
             contact.setStreetNo(streetNumber);
             contact.setCity(city);
             contact.setPostCode(zipCode);
         }
 
         else {
             contact = new Contact(lastName, firstName, street, streetNumber, zipCode, city);
         }
         contact.setShortcut(shortcut);
         contact.setPhone(phone);
         contact.setMobile(mobilePhone);
         contact.setFax(fax);
         contact.setEmail(email);
 
         return contact;
 
     }
 
 }
