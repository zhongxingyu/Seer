 package net.tirasa.addressbook;
 
 import java.util.List;
 import net.tirasa.addressbook.dao.PersonDAO;
 import net.tirasa.addressbook.data.Person;
 import net.tirasa.addressbook.exceptions.DatabaseException;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
 
 public class HomePage extends WebPage {
 
     private static final long serialVersionUID = 1L;
 
     @SpringBean
     private PersonDAO personDAO;
 
    private final static Logger LOG = LoggerFactory.getLogger(HomePage.class);
 
     public HomePage() {
 
         try {
             final List<Person> personListDb = personDAO.list();
             add(new ListView<Person>("listview", personListDb) {
 
                 private static final long serialVersionUID = 4949588177564901031L;
 
                 @Override
                 protected void populateItem(final ListItem<Person> item) {
                     final Person person = item.getModelObject();
                     item.add(new Label("name", person.getName()));
                     item.add(new Label("email", person.getEmail()));
                     item.add(new Label("telephone", person.getTelephone()));
                     item.add(new Link<Person>("edit", item.getModel()) {
 
                         private static final long serialVersionUID = -4331619903296515985L;
 
                         @Override
                         public void onClick() {
                             setResponsePage(new EditPerson(item.getModelObject()));
                         }
                     });
                     item.add(new Link<Person>("remove", item.getModel()) {
 
                         private static final long serialVersionUID = -4331619903296515985L;
 
                         @Override
                         public void onClick() {
                             try {
                                 personDAO.delete(getModelObject().getId());
                                 setResponsePage(HomePage.class);
                             } catch (DatabaseException ex) {
                                 LOG.error("ERROR DURING DELETE IN HOME PAGE".concat(ex.getMessage()));
                             }
                         }
                     });
                 }
             });
 
             add(new Link<Person>("addPerson") {
 
                 private static final long serialVersionUID = -4331619903296515985L;
 
                 @Override
                 public void onClick() {
                     setResponsePage(new EditPerson(new Person()));
                 }
             });
         } catch (DatabaseException ex) {
             LOG.error("ERROR DURING LIST OPERATION" + ex.getCause().getMessage());
         }
     }
 }
