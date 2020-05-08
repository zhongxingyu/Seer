 package gwt.client;
 
 import gwt.shared.user.student.ObservableStudent;
 import gwt.shared.user.student.Student;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.core.shared.GWT;
 
import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 /**
  * The start of the website.
  */
 public class Main implements EntryPoint {
 	
 	private String SESSION = "";
 	private TextBox txtUser;
 	private TextBox txtPW;
 	
 	private ObservableStudent session;
 	
 	/**
 	 * 	Acts like the Main() method
 	 */
 	public void onModuleLoad() {
 		
 		History.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				RootPanel.get().clear();
 				String historyToken = event.getValue();
 				if ( historyToken.equals("Login") ) {
 					loginGUI();
 				} else if ( historyToken.equals("LorisPlus") ) {
 					lorisPlusGUI();
 				} else {
 					loginGUI();
 				}
 				
 			}
 		});
 		History.newItem("Login");
 	}
 	
 	private void loginGUI() {
 		  VerticalPanel vp = new VerticalPanel();
 		  HorizontalPanel hp = new HorizontalPanel();
 		  
 		  hp.add(new Label("Username: "));
 		  this.txtUser = new TextBox();
 		  hp.add(this.txtUser);
 		  vp.add(hp);
 		  
 		  hp = new HorizontalPanel();
 		  hp.add(new Label("Password: "));
 		  this.txtPW = new TextBox();
 		  hp.add(this.txtPW);
 		  vp.add(hp);
 		  
 		  Button btnLogin = new Button("Login");
 		  btnLogin.addMouseUpHandler(new MouseUpHandler() {
 			
 			@Override
 			public void onMouseUp(MouseUpEvent event) {
 				if ( !txtPW.getText().equals("") && !txtUser.getText().equals("") ) {
 					SESSION = txtUser.getText();
 					LoginServiceAsync loginService = GWT.create(LoginService.class);
 					AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							History.newItem("Login");
 							
 						}
 
 						@Override
 						public void onSuccess(Integer result) {
 							if ( result >= 0 )
 								History.newItem("LorisPlus");
 						}
 					};
 					
 					loginService.checkLogin(txtUser.getText(), txtPW.getText(), callback);
 				}
 			}
 		});
 		  vp.add(btnLogin);
 		  RootPanel.get().add(vp);	  
 	}
 	
 	private void lorisPlusGUI() {
 		CleanStudentServiceAsync getMyStudent = GWT.create(CleanStudentService.class);
 		AsyncCallback<Student> callback = new AsyncCallback<Student>(){
 		@Override
 		public void onFailure(Throwable caught) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onSuccess(Student s) {
 			// TODO Auto-generated method stub
 						// Calendar area		z
 			session = new ObservableStudent(s);
 			// decloration for all classes, set to null, inside class will have a setter if needed
 		
 			CourseNavagation cn = null;
 			CourseSearch cs = null;
 			BookMarks bookmark = null;
 			Schedule schedule = null;
 		
 			// Course Navagation Area
 		
 			cn = new CourseNavagation(session);
 			cn.setStyleName("cn");
 
 			// Search Area
 		
 			cs = new CourseSearch(cn);
 			cs.initCourseSearch();
 			cs.setStyleName("searchStyle");
 		
 			//Calendar and bookmark area ======================================
 		
 			HorizontalPanel scheduleAndBookmarks = new HorizontalPanel();
 
 			schedule = new Schedule(7,10, session);		
 			schedule.initCalendar();
 			session.addView(schedule);
 			schedule.setStyleName("schedule");
 			
 			scheduleAndBookmarks.add(schedule);
 		
 			// Bookmarked area
 			bookmark = new BookMarks(schedule, session);
 			session.addView(bookmark);
 
 			bookmark.setStyleName("bookmarkStyle");
 
 			scheduleAndBookmarks.add(bookmark);
 			scheduleAndBookmarks.setStyleName("sAndBStyle");
 			// end of Calendar and bookmark area ===============================
 	
 			cn.initObject();
 			session.updateViews();
 			// Add to screen\
 			RootPanel.get("search").add(cs);
 			RootPanel.get("cn").add(cn);
 			RootPanel.get("schedule").add(schedule);
 			RootPanel.get("bookmark").add(bookmark);
 		}
 		};
 		getMyStudent.getStudent(SESSION, callback);
 	}
 }
 
