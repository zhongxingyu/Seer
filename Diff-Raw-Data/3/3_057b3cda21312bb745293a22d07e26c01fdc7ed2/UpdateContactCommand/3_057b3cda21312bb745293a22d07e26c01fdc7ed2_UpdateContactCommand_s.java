 package edu.rit.taskers.command;
 
 
 import edu.rit.taskers.model.Contact;
 import edu.rit.taskers.persistence.ContactDao;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class UpdateContactCommand {
 
     private Contact contact;
 
     @Autowired
     private ContactDao contactDao;
 
     public UpdateContactCommand(Contact contact){
         this.contact = contact;
     }
 
     public void execute() {
         if(contact.getId() != 0) {
             contactDao.update(contact);
         } else {
             contactDao.save(contact);
         }
 
     }
 
     public void setContactDao(ContactDao contactDao) {
         this.contactDao = contactDao;
     }
 }
 
