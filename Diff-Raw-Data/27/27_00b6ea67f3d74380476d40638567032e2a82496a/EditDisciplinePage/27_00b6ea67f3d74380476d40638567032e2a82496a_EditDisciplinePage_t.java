 package ua.dp.primat.schedule.admin;
 
 import java.util.List;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import ua.dp.primat.domain.Cathedra;
 import ua.dp.primat.domain.workload.Discipline;
 import ua.dp.primat.repositories.CathedraRepository;
 import ua.dp.primat.repositories.DisciplineRepository;
 
 /**
  *
  * @author EniSh
  */
 public final class EditDisciplinePage extends WebPage {
 
     public EditDisciplinePage() {
         this(new Discipline());
     }
 
     public EditDisciplinePage(Discipline discipline) {
         super();
         add(new EditDisciplineForm("discipline", discipline));
     }
 
     private static class EditDisciplineForm extends Form<Discipline> {
 
         public EditDisciplineForm(String id, Discipline discipline) {
             super(id, new CompoundPropertyModel<Discipline>(discipline));
             add(new FeedbackPanel("feedback"));
 
             this.discipline = discipline;
             final TextField name = new TextField("name");
             name.setRequired(true);
             add(name);
 
             final List<Cathedra> cathedras = cathedraRepository.getCathedras();
             add(new DropDownChoice("cathedra", cathedras));
         }
 
         @Override
         protected void onSubmit() {
            disciplineRepository.update(discipline);
            setResponsePage(ManageDisciplines.class);
         }
         private Discipline discipline;
         @SpringBean
         private DisciplineRepository disciplineRepository;
         @SpringBean
         private CathedraRepository cathedraRepository;
         private static final long serialVersionUID = 1L;
     }
     private static final long serialVersionUID = 1L;
 }
