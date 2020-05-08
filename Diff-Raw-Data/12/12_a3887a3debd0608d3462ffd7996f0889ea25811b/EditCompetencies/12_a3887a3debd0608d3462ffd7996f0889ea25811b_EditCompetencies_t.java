 package org.wwald.view;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.util.List;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.list.ListItemModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.wwald.WWALDApplication;
 import org.wwald.WWALDConstants;
 import org.wwald.WicketIdConstants;
 import org.wwald.model.ConnectionPool;
 import org.wwald.model.Course;
 import org.wwald.model.Mentor;
 import org.wwald.model.Permission;
 import org.wwald.service.DataException;
 
 public class EditCompetencies extends AccessControlledPage {
 		
 	public EditCompetencies(PageParameters parameters) {
 		super(parameters); 
 		
 		try {
 			Connection conn = ConnectionPool.getConnection(getDatabaseId());
 			String courseId = parameters.getString(WWALDConstants.SELECTED_COURSE);
 			Course course = ((WWALDApplication)getApplication()).getDataFacade().retreiveCourse(conn, courseId);
 			System.out.println("Course Mentor Before" + course.getMentor());
 			add(new Label("course.name", courseId));
 			add(getCompetenciesAndCourseDescriptionEditForm(course, parameters));
 		} catch(DataException de) {
 			String msg = "Sorry we could not perform the requested action, due to an internal error. We will look into this issue as soon as we can";
 			parameters.add(WicketIdConstants.MESSAGES, msg);
 			//TODO: Find out how to access page history and go to the page from where we came
 			setResponsePage(CoursePage.class, parameters);
 		}
 	}
 
 	private Form getCompetenciesAndCourseDescriptionEditForm(final Course course, final PageParameters pageParams) throws DataException {
 		Form editCompetenciesForm = new Form("competencies.edit.form") {
 			@Override
 			public void onSubmit() {
 				try {
 					Connection conn = ConnectionPool.getConnection(getDatabaseId());					
 
 					TextArea courseDescriptionTextArea = (TextArea)get(1);
 					course.setDescription((String)courseDescriptionTextArea.getModelObject());
 					((WWALDApplication)getApplication()).getDataFacade().updateCourse(conn, course);
 															
 					TextArea competenciesListTextArea = (TextArea)get(2);					
 					((WWALDApplication)getApplication()).getDataFacade().updateCompetenciesWikiContents(ConnectionPool.getConnection(getDatabaseId()), course.getId(), (String)competenciesListTextArea.getModelObject());
 					
 					setResponsePage(CoursePage.class, pageParams);
 				} catch(DataException de) {
 					String msg = "Sorry we could not perform the action you requested " +
 								 "due to an internal error. We will look into this " +
 								 "issue as soon as we can";
 					pageParams.add(WicketIdConstants.MESSAGES, msg);
 					setResponsePage(GenericErrorPage.class, pageParams);
 				}
 			}
 		};
 						
 		DropDownChoice selectMentors = new DropDownChoice("select_mentor", new PropertyModel(course, "mentor"), getMentors());		
 		TextArea editCourseDescriptionTextArea = new TextArea("course.description.edit.form.textarea", new Model(course.getDescription()));
 		TextArea editCompetenciesFormTextArea = new TextArea("competencies.edit.form.textarea", 
 															 new Model(getCompetenciesWikiContents(course.getId())));
 		
 		editCompetenciesForm.add(selectMentors);
 		editCompetenciesForm.add(editCourseDescriptionTextArea);
 		editCompetenciesForm.add(editCompetenciesFormTextArea);
 		return editCompetenciesForm;
 	}
 
	private List<Mentor> getMentors() throws DataException {
 		WWALDApplication app = (WWALDApplication)getApplication();
		List<Mentor> retVal = app.getDataFacade().retreiveAllMentors(ConnectionPool.getConnection(getDatabaseId()));
		return retVal;
 	}
 
 	private Serializable getCompetenciesWikiContents(String courseId) throws DataException {
 		WWALDApplication app = (WWALDApplication)getApplication();
 		return app.getDataFacade().retreiveCompetenciesWiki(ConnectionPool.getConnection(getDatabaseId()), 
 															courseId);
 	}
 
 	@Override
 	protected Permission getRequiredPermission() {
 		return Permission.EDIT_COURSE;
 	}
 }
