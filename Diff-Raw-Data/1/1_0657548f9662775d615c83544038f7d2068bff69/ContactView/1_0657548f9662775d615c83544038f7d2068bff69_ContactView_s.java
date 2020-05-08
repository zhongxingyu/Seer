 package org.melati.example.contacts;
 
 import java.io.IOException;
 
 import org.melati.Melati;
 import org.melati.servlet.Form;
 import org.melati.servlet.TemplateServlet;
 import org.melati.PoemContext;
 import org.melati.servlet.PathInfoException;
 import org.melati.template.ServletTemplateContext;
 
 
  /**
   *  Example servlet to display or edit a contact and its categories.
   *
   */
 public class ContactView extends TemplateServlet {
   private static final long serialVersionUID = 1L;
 
   protected String doTemplateRequest(Melati melati, ServletTemplateContext context)
       throws Exception {
 
     ContactsDatabase db = (ContactsDatabase)melati.getDatabase();
     Contact contact = (Contact)melati.getObject();
     // used to display a blank page for new data entry
     if (melati.getMethod().equals("Insert")) {
       contact = (Contact)db.getContactTable().newPersistent();
     }
     // used to update or insert a record
     else if (melati.getMethod().equals("Update")) {
       if (contact == null) {
         contact = (Contact) db.getContactTable().newPersistent();
         Form.extractFields(melati.getServletTemplateContext(),contact);
         db.getContactTable().create(contact);
       } else {
         Form.extractFields(melati.getServletTemplateContext(),contact);
       }
       deleteCategories(db,contact);
 
       String[] categories = melati.getRequest().
                                getParameterValues("field_category");
       if (categories != null) {
         for (int i=0; i< categories.length; i++) {
           ContactCategory cat =
           (ContactCategory)db.getContactCategoryTable().newPersistent();
           cat.setContact(contact);
           cat.setCategoryTroid(new Integer(categories[i]));
           db.getContactCategoryTable().create(cat);
         }
       }
       try {
         melati.getResponse().sendRedirect
         ("/melati/org.melati.example.contacts.Search/contacts");
       } catch (IOException e) {
         throw new Exception(e.toString());
       }
       return null;
     }
     //  delete a record
     else if (melati.getMethod().equals("Delete")) {
       deleteCategories(db,contact);
       contact.deleteAndCommit();
       try {
         melati.getResponse().sendRedirect
         ("/melati/org.melati.example.contacts.Search/contacts");
       } catch (IOException e) {
         throw new Exception(e.toString());
       }
       return null;
     }
     else if (melati.getMethod().equals("View")) {
      return "org/melati/example/contacts/ContactView";
     }
     else { 
        throw new Exception("Invalid Method");
     }
     context.put("contact",contact);
     context.put("categories",db.getCategoryTable().selection());
     // The file extension is added by the template engine
     return "org/melati/example/contacts/ContactView";
   }
 
  /**
   *  Remove all categories associated with a Contact.
   *
   * @param db      the {@link ContactsDatabase}
   * @param contact the {@link Contact} 
   */
   public void deleteCategories(ContactsDatabase db, Contact contact) {
 
     db.sqlUpdate("DELETE FROM " + db.quotedName("contactcategory") + 
                  " WHERE " + db.quotedName("contact") + " = " + 
                  contact.getTroid());
   }
 
   protected PoemContext poemContext(Melati melati)
       throws PathInfoException {
     return (PoemContext)poemContextWithLDB(melati,"contacts");
   }
 
 }
 
 
