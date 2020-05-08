 package kornell.gui.client.presentation.course.generic;
 
 import kornell.api.client.Callback;
 import kornell.api.client.KornellClient;
 import kornell.api.client.data.Person;
 import kornell.gui.client.KornellConstants;
 import kornell.gui.client.event.CourseBarEvent;
 import kornell.gui.client.event.CourseBarEventHandler;
 import kornell.gui.client.event.MenuLeftWelcomeEvent;
 import kornell.gui.client.event.MenuLeftWelcomeEventHandler;
 import kornell.gui.client.presentation.atividade.AtividadePlace;
 import kornell.gui.client.presentation.atividade.AtividadeView.Presenter;
 import kornell.gui.client.presentation.course.CoursePlace;
 import kornell.gui.client.presentation.course.CourseView;
 import kornell.gui.client.presentation.terms.TermsView;
 import kornell.gui.client.presentation.welcome.WelcomePlace;
 import kornell.gui.client.presentation.welcome.generic.GenericMenuLeftView;
 import kornell.gui.client.presentation.welcome.generic.GenericWelcomeCoursesView;
 
 import com.github.gwtbootstrap.client.ui.Paragraph;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 
 public class GenericCourseView extends Composite implements CourseView {
 	interface MyUiBinder extends UiBinder<Widget, GenericCourseView> {
 	}
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
 	@UiField
 	FlowPanel coursePanel;
 	
 	private KornellClient client;
 
 	private PlaceController placeCtrl;
 
 	private Presenter presenter;
 
 	private static KornellConstants constants = GWT.create(KornellConstants.class);
 	
 	private EventBus bus;
 	
 	private GenericCourseCourseView courseView;
 	private GenericCourseDetailsView detailsView;
 	private GenericCourseLibraryView libraryView;
 	private GenericCourseForumView forumView;
 	private GenericCourseChatView chatView;
 	private GenericCourseSpecialistsView specialistsView;
 	private GenericCourseNotesView notesView;
 	
 	private static String COURSE_VIEW = constants.course();
 	private static String DETAILS_VIEW = constants.details();
 	private static String LIBRARY_VIEW = constants.library();
 	private static String FORUM_VIEW = constants.forum();
 	private static String CHAT_VIEW = constants.chat();
 	private static String SPECIALISTS_VIEW = constants.specialists();
 	private static String NOTES_VIEW = constants.notes();
 	
 	
 	public GenericCourseView(EventBus eventBus, KornellClient client, PlaceController placeCtrl) {
 		this.bus = eventBus;
 		this.client = client;
 		this.placeCtrl = placeCtrl;
 		initWidget(uiBinder.createAndBindUi(this));
 		initData();		
 		bus.addHandler(CourseBarEvent.TYPE, new CourseBarEventHandler() {
 			
 			@Override
 			public void onItemSelected(CourseBarEvent event) {
 				display(event.getCourseBarItemSelected());
 			}
 		});
 	}
 	
 	private void initData() {
 		client.getCurrentUser(new Callback<Person>() {
 			@Override
 			protected void ok(Person person) {
 				// TODO
				display(COURSE_VIEW);
 			}
 		});
 	}
 	
 	private void display(String viewName) {
 		coursePanel.clear();
 		if(COURSE_VIEW.equals(viewName)){
 			coursePanel.add(getCourseView());
 			
 		} else if(DETAILS_VIEW.equals(viewName)){
 			coursePanel.add(getDetailsView());
 			
 		} else if(LIBRARY_VIEW.equals(viewName)){
 			coursePanel.add(getLibraryView());
 			
 		} else if(FORUM_VIEW.equals(viewName)){
 			coursePanel.add(getForumView());
 			
 		} else if(CHAT_VIEW.equals(viewName)){
 			coursePanel.add(getChatView());
 			
 		} else if(SPECIALISTS_VIEW.equals(viewName)){
 			coursePanel.add(getSpecialistsView());
 			
 		} else {
 			coursePanel.add(getNotesView());
 		}
 	}
 
 	private Widget getCourseView() {
 		if(courseView == null)
 			courseView = new GenericCourseCourseView(client, placeCtrl);
 		return courseView;
 	}
 
 	private Widget getDetailsView() {
 		if(detailsView == null)
 			detailsView = new GenericCourseDetailsView(client, placeCtrl);
 		return detailsView;
 	}
 
 	private Widget getLibraryView() {
 		if(libraryView == null)
 			libraryView = new GenericCourseLibraryView(client, placeCtrl);
 		return libraryView;
 	}	
 
 	private Widget getForumView() {
 		if(forumView == null)
 			forumView = new GenericCourseForumView(client, placeCtrl);
 		return forumView;
 	}
 
 	private Widget getChatView() {
 		if(chatView == null)
 			chatView = new GenericCourseChatView(client, placeCtrl);
 		return chatView;
 	}
 
 	private Widget getSpecialistsView() {
 		if(specialistsView == null)
 			specialistsView = new GenericCourseSpecialistsView(client, placeCtrl);
 		return specialistsView;
 	}
 
 	private Widget getNotesView() {
 		if(notesView == null)
 			notesView = new GenericCourseNotesView(client, placeCtrl);
 		return notesView;
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 }
