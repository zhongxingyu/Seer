 package cj.demo;
 
 import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static org.springframework.web.bind.annotation.RequestMethod.PUT;
 
import static cj.demo.RestResponse.*;
 
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 @RequestMapping(value="/contact")
 public class ContactController {
 
     @Autowired private ContactDao contactDao;
    
     @RequestMapping(value="{id}",method=GET)
     public ResponseEntity<Contact> findContactById(@PathVariable Integer id) { 
        Contact contact = contactDao.findContactById(id); 
         return (contact == null)  ? notFound() : ok(contact);
     }
     
     @RequestMapping(method=GET)
     public ResponseEntity<Collection<Contact>> findAllContacts() {
         return ok(contactDao.findAllContacts());
     }
 
     @RequestMapping(method=POST, consumes="application/json", produces="application/json")
     public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
         return created(contactDao.addContact(contact));
     }
 
     @RequestMapping(value="{id}",method=PUT, consumes="application/json")
     public ResponseEntity<Contact> updateContact(@PathVariable Integer id, @RequestBody Contact contact) {
         contact.setId(id);
         Contact updated = contactDao.updateContact(contact);
         return (updated == null) ? notFound() : ok(updated);
     }
 
     @RequestMapping(value="{id}",method=DELETE)
     public ResponseEntity<Contact> deleteContact(@PathVariable Integer id) {
         Contact contact = contactDao.deleteContact(id);
         return (contact == null) ? notFound() : noContent();
     }
 
     public void setContactDao(HashMapContactDao dao) {
         contactDao = dao;
     }
 }
