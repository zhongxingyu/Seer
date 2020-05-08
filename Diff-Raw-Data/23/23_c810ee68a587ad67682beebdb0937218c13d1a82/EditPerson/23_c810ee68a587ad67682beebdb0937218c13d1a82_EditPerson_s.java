 package net.tirasa.addressbook;
 
 import net.tirasa.addressbook.dao.PersonDAO;
 import net.tirasa.addressbook.data.Person;
 import net.tirasa.addressbook.exceptions.DatabaseException;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.EmailTextField;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.HiddenField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.LoggerFactory;
 
 public class EditPerson extends WebPage {
 
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(EditPerson.class);
 
     private static final long serialVersionUID = 320083532829771040L;
 
     private Person person;
 
     @SpringBean
     private PersonDAO personDao;
 
    EditPerson(Person person) {
 
         this.person = person;
         FeedbackPanel fp = new FeedbackPanel("feedback");
         add(fp);
         add(new PersonForm("personForm"));
     }
 
     public final class PersonForm extends Form {
 
         private static final long serialVersionUID = 1099337525795355841L;
 
         public PersonForm(final String formId) {
             super(formId, new CompoundPropertyModel(person));
             HiddenField<Long> id = new HiddenField<Long>("id");
             id.setRequired(true);
             add(id);
             TextField<String> name = new TextField<String>("name");
             name.setRequired(true);
             add(name);
             EmailTextField email = new EmailTextField("email");
             add(email);
             TextField<String> telephone = new TextField<String>("telephone");
             telephone.add(new TelephoneValidator());
             add(telephone);
         }
 
         @Override
         public final void onSubmit() {
             try {
                 LOG.debug("SUBMIT OF: ".concat(person.getName()).concat("NAME: ").concat(person.getName()).concat(
                         "EMAIL: ").concat("TELEPHONE: ").concat(person.getTelephone()));
                 personDao.save(person);
                 setResponsePage(new HomePage());
             } catch (DatabaseException ex) {
                 LOG.error("ERROR IN SUBMIT OF FORM: ".concat(ex.getMessage()));
             }
         }
     }
 }
