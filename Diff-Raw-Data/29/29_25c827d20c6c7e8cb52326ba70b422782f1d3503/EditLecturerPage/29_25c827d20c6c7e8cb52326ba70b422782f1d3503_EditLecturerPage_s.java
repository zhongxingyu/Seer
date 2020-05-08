 package ua.dp.primat.schedule.admin;
 
 import java.util.Arrays;
 import java.util.List;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.PatternValidator;
 import org.apache.wicket.validation.validator.StringValidator.LengthBetweenValidator;
 import ua.dp.primat.domain.Cathedra;
 import ua.dp.primat.domain.Lecturer;
 import ua.dp.primat.repositories.LecturerRepositoryImpl;
 import ua.dp.primat.domain.LecturerType;
 import ua.dp.primat.repositories.CathedraRepository;
 
 /**
  *
  * @author EniSh
  */
 public final class EditLecturerPage extends WebPage {
 
     public EditLecturerPage() {
         this(new Lecturer());
     }
 
     public EditLecturerPage(Lecturer lecturer) {
         this.lecturer = lecturer;
         add(new EditLecturerForm("lecturer"));
     }
 
     private class EditLecturerForm extends Form<Lecturer> {
 
         public EditLecturerForm(String cname) {
             super(cname, new CompoundPropertyModel<Lecturer>(lecturer));
             add(new FeedbackPanel("feedback"));
 
             TextField name = new TextField("name");
             name.setRequired(true);
             name.add(new LengthBetweenValidator(5, 60));
             add(name);
             List<Cathedra> cathedras = cathedraRepository.getCathedras();
             add(new DropDownChoice("cathedra", cathedras));
             List<LecturerType> lecturerTypes = Arrays.asList(LecturerType.values());
             add(new DropDownChoice("lecturerType", lecturerTypes));
         }
 
         @Override
         protected void onSubmit() {
             lecturerRepository.store(lecturer);
             setResponsePage(ManageLecturersPage.class);
         }
     }
     private Lecturer lecturer;
     @SpringBean
     private LecturerRepositoryImpl lecturerRepository;
     @SpringBean
     private CathedraRepository cathedraRepository;
 }
 
