 package br.com.jera.example.jsf;
 
import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
 
 import br.com.jera.example.domain.AddressBook;
 import br.com.jera.example.domain.Contact;
 
 @Name("editContactController")
@Scope(ScopeType.EVENT)
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
