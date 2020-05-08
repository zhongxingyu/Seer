 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ua.dp.primat.schedule.admin;
 
 import java.util.List;
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.IPageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.link.PageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
import ua.dp.primat.curriculum.data.Discipline;
import ua.dp.primat.curriculum.data.DisciplineRepository;
 
 /**
  *
  * @author pesua
  */
 public final class ManageDisciplines extends WebPage {
 
     public ManageDisciplines() {
         super();
         final List<Discipline> Disciplines = DisciplineRepository.getDisciplines();
 
         ListView<Discipline> DisciplineView = new ListView<Discipline>("repeating", Disciplines) {
 
             @Override
             protected void populateItem(ListItem<Discipline> li) {
                 final Discipline Discipline = li.getModelObject();
                 li.add(new Label("Discipline", Discipline.toString()));
                 li.add(new PageLink("editDiscipline", new IPageLink() {
 
                     public Page getPage() {
                         return new EditDisciplinePage(Discipline);
                     }
 
                     public Class<? extends Page> getPageIdentity() {
                         return EditDisciplinePage.class;
                     }
                 }));
                 li.add(new Link("deleteDiscipline") {
 
                     @Override
                     public void onClick() {
                         DisciplineRepository.delete(Discipline);
                         Disciplines.remove(Discipline);
                     }
                 });
             }
         };
         add(DisciplineView);
     }
     @SpringBean
     private DisciplineRepository DisciplineRepository;
 }
