 package ua.dp.primat.schedule.admin.groupManagement;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.RangeValidator;
 import org.apache.wicket.validation.validator.StringValidator.LengthBetweenValidator;
 import ua.dp.primat.domain.StudentGroup;
 import ua.dp.primat.repositories.StudentGroupRepository;
 
 /**
  *
  * @author EniSh
  */
 public final class EditGroupPage extends WebPage {
 
     public EditGroupPage() {
         this(new StudentGroup());
     }
 
     public EditGroupPage(StudentGroup group) {
         add(new EditGroupForm("form", group));
     }
 
     private class EditGroupForm extends Form<StudentGroup> {
 
         public EditGroupForm(String id, StudentGroup group) {
             super(id, new CompoundPropertyModel<StudentGroup>(group));
 
             add(new FeedbackPanel("feedback"));
 
             RequiredTextField<String> speciality = new RequiredTextField<String>("code");
             speciality.add(new LengthBetweenValidator(2, 2));
             add(speciality);
 
             RequiredTextField<Long> year = new RequiredTextField<Long>("year");
             year.add(new RangeValidator<Long>(1950L, 2050L));
             add(year);
 
             RequiredTextField<Long> number = new RequiredTextField<Long>("number");
             number.add(new RangeValidator<Long>(1L, 10L));
             add(number);
         }
 
         @Override
         protected void onSubmit() {
             groupRepository.store(getModelObject());
             setResponsePage(ManageGroupsPage.class);
         }
 
     }
 
     @SpringBean
     private StudentGroupRepository groupRepository;
 }
 
