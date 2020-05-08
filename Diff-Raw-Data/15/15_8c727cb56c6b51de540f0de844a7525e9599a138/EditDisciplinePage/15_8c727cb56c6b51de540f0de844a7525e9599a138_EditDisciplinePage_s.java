 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ua.dp.primat.schedule.admin;
 import java.util.List;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
import ua.dp.primat.curriculum.data.Cathedra;
import ua.dp.primat.curriculum.data.Discipline;
import ua.dp.primat.curriculum.data.DisciplineRepository;
 import ua.dp.primat.utils.view.ChoosePanel;
 /**
  *
  * @author pesua
  */
 public final class EditDisciplinePage extends WebPage {
 
     public EditDisciplinePage() {
         this(new Discipline());
     }
 
     public EditDisciplinePage(Discipline discipline) {
         super();
         this.discipline = discipline;
 
         add(new EditDisciplineForm("discipline"));
     }
 
     private class EditDisciplineForm extends Form<Discipline> {
 
         public EditDisciplineForm(String cName) {
             super(cName, new CompoundPropertyModel<Discipline>(discipline));
 
             add(new TextField("name"));
             //List<Cathedra> cathedra = CathedraRepository.getAllLecturers();
             //add(new DropDownChoice("cathedra", cathedra));
         }
 
         @Override
         protected void onSubmit() {
             super.onSubmit();
             disciplineRepository.store(discipline);
             setResponsePage(ManageDisciplines.class);
         }
     }
 
     private Discipline discipline;
 
     @SpringBean
     private DisciplineRepository disciplineRepository;
 }
 
