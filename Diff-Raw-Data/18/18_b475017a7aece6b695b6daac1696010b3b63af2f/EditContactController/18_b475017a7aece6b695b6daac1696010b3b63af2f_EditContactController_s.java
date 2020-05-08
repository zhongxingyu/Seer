 package br.com.jera.example.jsf;
 
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 
 import br.com.jera.example.domain.AddressBook;
 import br.com.jera.example.domain.Contact;
 
 @Name("editContactController")
 public class EditContactController {
 
 	@In(create = true)
 	private AddressBook addressBook;
 
 	@In
 	private Contact contact;
 
 	public String save() {
 		addressBook.add(contact);
 		return "success";
 	}
 
 }
