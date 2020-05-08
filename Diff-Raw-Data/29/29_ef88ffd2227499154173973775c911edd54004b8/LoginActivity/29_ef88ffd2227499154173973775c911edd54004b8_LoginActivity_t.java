 package logbook.client.a_nonroo.app.client.activity;
 
 
 import static logbook.shared.util.UtilityLogBook.DECIMAL_FORMAT;
 import static logbook.shared.util.UtilityLogBook.getEmptyStringIfNull;
 import static logbook.shared.util.UtilityLogBook.getFormatedString;
 import static logbook.shared.util.UtilityLogBook.join;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 
 import logbook.client.a_nonroo.app.client.LogBookNav;
 import logbook.client.a_nonroo.app.client.place.LoginPlace;
 import logbook.client.a_nonroo.app.client.ui.StudentEditPopupViewImpl;
 import logbook.client.a_nonroo.app.client.ui.StudentInformationView;
 import logbook.client.a_nonroo.app.client.ui.StudentInformationViewImpl;
 import logbook.client.a_nonroo.app.client.ui.custom.widget.CustomProgressbar;
 import logbook.client.a_nonroo.app.request.LogBookRequestFactory;
 import logbook.client.managed.proxy.SkillAcquiredProxy;
 import logbook.client.managed.proxy.StudentProxy;
 import logbook.client.managed.request.StudentRequest;
 import logbook.shared.StudentStatus;
 import logbook.shared.StudyYears;
 import logbook.shared.i18n.LogBookConstants;
 import logbook.shared.scaffold.LogBookConstant;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 /**
  * @author Darshan
  *
  */
 public class LoginActivity extends AbstractActivity implements StudentInformationView.presenter, StudentInformationView.Delegate{
 	
 	
 	private LogBookRequestFactory requests;
 	private PlaceController placeController;
 	private AcceptsOneWidget widget;
 	private StudentInformationView view;
 //	private StudentSubDetailsView view_s = new StudentSubDetailsViewImpl();
 	private LoginActivity LoginActivity;
 	private LoginPlace place;
 	private int tabIndex=0; 
 	
 	private ActivityManager activityManager;
 	
 	public HandlerManager handlerManager;// = new HandlerManager(this);
 //	private HandlerRegistration rangeChangeHandler;
 	
 	public int currenttab= 0;	
 	
 	LogBookConstants constants = GWT.create(LogBookConstants.class);
 	
 	// Home Tab Skill Acquired Table Members
 	CellTable<SkillAcquiredProxy> table;
 	public String sortorder="DESC";
 	
 	
 	public LoginActivity(LogBookRequestFactory requests, PlaceController placeController,LoginPlace loginPlace) 
 	{
 		Log.info("Call Activity Login");
 		
     	this.requests = requests;
     	this.placeController = placeController;
     	this.place = loginPlace;
     	this.handlerManager = loginPlace.handler;
 
     	LoginActivity = this;
     	
     	LogBookNav.logBookNav.getMainLoogBookTabpanel().selectTab(0,false);
 		
 
 	}	
 	
 	
 	/*public void addSelectChangeHandler(SelectChangeHandler handler) {
 		handlerManager.addHandler(SelectChangeEvent.getType(), handler);
 		
 	}*/
 	
 	public LoginActivity(LogBookRequestFactory requests , PlaceController placeController) {
 		Log.info("Call Activity Login..");
 		this.requests = requests;
     	this.placeController = placeController;
 
     	LogBookNav.logBookNav.getMainLoogBookTabpanel().selectTab(0,false);
 	}
 
 	
 	public void onStop(){
 		widget.setWidget(null);
 	}
 	
 	@Override
 	public void start(AcceptsOneWidget panel, EventBus eventBus) {
 		Log.info("SystemStartActivity.start()");
 		this.widget = panel;
 		init();
 		
 
 	}
 	
 
 	private void init(){
 		StudentInformationView systemStartView = new StudentInformationViewImpl();
 		systemStartView.setPresenter(this);
 		this.view = systemStartView;
 		
 		widget.setWidget(systemStartView.asWidget());
 		
 		//Fix in default style( without it tab content will not show properly)
 		Log.info("HTML :" +systemStartView.asWidget().getElement().getParentElement().getParentElement());		
 		systemStartView.asWidget().getElement().getParentElement().getParentElement().getStyle().setPosition(Position.RELATIVE);
 		
 		view.setDelegate(this);
 		
 		initStudentInfo(); // Initialize StudentInformation
 		intiSkillTableRange();
 		//intiSkillTable();
 		
 		
 
 	}
 
 	private void intiSkillTableRange() 
 	{
 		table=view.getTable();
 		table.addRangeChangeHandler(new RangeChangeEvent.Handler() 
 		{
 			public void onRangeChange(RangeChangeEvent event) 
 			{
 				onRangeChanged();
 			}
 		});
 	 
 	 table.addColumnSortHandler(new ColumnSortEvent.Handler() 
 	 {
 			@Override
 			public void onColumnSort(ColumnSortEvent event) 
 			{
 
 				if(event.isSortAscending())
 				{
 					sortorder="ASC";
 				}
 				else
 				{
 					sortorder="DESC";
 				}
 					Log.info("Call Init Search from addColumnSortHandler");
 					intiSkillTable("skill.shortcut");
 					
 				//}
 			}
 		});		
 	 
 	 table.setColumnWidth(table.getColumn(0), "70%");
 	 table.setColumnWidth(table.getColumn(1), "20%");
 	 table.setColumnWidth(table.getColumn(2), "10%");
 	 
 	 
 	 table.addColumnStyleName(1, "skillTableShortcutColumnStyle");
 	}
 	
 	
 	
 
 
 	private void intiSkillTable(String sortBy) 
 	{
 		Log.info("Init Table ");
 		
 		
 		Log.info("Student Proxy11: " + table);
 		Log.info("Student Proxy1: " + view.getStudentProxy().getId());
 		
 		final Range range=table.getVisibleRange();
 		Log.info("Start Range: " + range.getStart());
 		Log.info("Range Length: " + range.getLength());
 		
 		requests.skillAcquiredRequestNonRoo().findCountLatestAcquiredSkillByStudent(view.getStudentProxy().getId(),LogBookConstant.TOTAL_SKILL_ACQUIRED_DISPLAY,sortorder,sortBy).with("skill","skillLevel","skill.topic","skill.topic.classificationTopic","skill.topic.classificationTopic.mainClassification").fire(new Receiver<Integer>() {
 
 			@Override
 			public void onSuccess(Integer response) {
 				table.setRowCount(response,true);
 			}
 		
 		});
 		
 		requests.skillAcquiredRequestNonRoo().findLatestAcquiredSkillByStudent(view.getStudentProxy().getId(),sortorder,sortBy,range.getStart(),range.getLength()).with("skill","skillLevel","skill.topic","skill.topic.classificationTopic","skill.topic.classificationTopic.mainClassification").fire(new Receiver<List<SkillAcquiredProxy>>() {
 
 			@Override
 			public void onSuccess(List<SkillAcquiredProxy> response) 
 			{
 				Log.info("Total Skill Acquired By Student: " + response.size());
 			/*	table.setRowCount(response.size());*/
 				table.setRowData(range.getStart(),response);
 			}
 			@Override
 			public void onConstraintViolation(java.util.Set<javax.validation.ConstraintViolation<?>> violations) 
 			{
 				super.onConstraintViolation(violations);
 				Log.info("Violation");
 			};
 			@Override
 			public void onFailure(ServerFailure error) {
 				super.onFailure(error);
 				Log.info("Failure");
 			}
 			
 		});
 		 
 	}
 
 	protected void onRangeChanged() {
 		/*final Range range = table.getVisibleRange();*/
 		Log.info("range change for role topic ");
 		intiSkillTable("created");
 	}
 	
 	
 	
 	
 	private void initStudentInfo() 
 	{
 		view.getLblError().setVisible(false);
 		requests.studentRequestNonRoo().findStudentFromSession().fire(new Receiver<StudentProxy>() 
 		{
 			@Override
 			public void onSuccess(StudentProxy studentProxy) 
 			{
 				Log.info("Success");	
 				view.setStudentProxy(studentProxy);
 				intiPersonnelInformation(studentProxy); // Set Value to Personnel Information Panel i.e. Student Name, Study Year etc..
 				intiCurrentProgressInformation(studentProxy); // Set aValue to Current Progress Information Panel i.e. Total Level 1 and Level 2 Skill Acquired
 				intiSkillTable("created");				
 				
 			}
 			@Override
 			public void onConstraintViolation(java.util.Set<javax.validation.ConstraintViolation<?>> violations) 
 			{
 				super.onConstraintViolation(violations);
 				Log.info("Violation");
 			};
 			@Override
 			public void onFailure(ServerFailure error) {
 				super.onFailure(error);
 				Log.info("Failure");
 			}
 		});
 	}
 
 	protected void intiCurrentProgressInformation(StudentProxy studentProxy) 
 	{
 		Log.info("intiCurrentProgressInformation Student Id: " +studentProxy.getId());
 	
 		final long studentId=studentProxy.getId();
 		// Find Total Skill for Level 1 and Level 2
 		requests.skillRequestNonRoo().findCountOfSkillBySkillLevel().fire(new Receiver<List<Long>>() 
 		{
 
 				@Override
 				public void onSuccess(final List<Long> totalSkillListByLevel) 
 				{					
 					Log.info("Success");
 					Log.info("Total Skills By Level : " + join(totalSkillListByLevel,","));
 					
 					if(totalSkillListByLevel.size()==2)
 					{
 						requests.skillAcquiredRequestNonRoo().findTotalSkillAcquiredByStudentLevelVise(studentId).fire(new Receiver<List<Long>>() {
 
 							@Override
 							public void onSuccess(List<Long> totalSkillAcquiredBySkillLevel) 
 							{
 								Log.info("Success");
 								Log.info("Total Acquired Skills By Level : " + join(totalSkillAcquiredBySkillLevel, ","));
 								
 								if (totalSkillAcquiredBySkillLevel.size()==2) 
 								{
 									// total percentage Of Skill Acquired for level 1
 									float totalPercentageLevel1= totalSkillListByLevel.get(0)==0?totalPercentageLevel1=0.0f:((totalSkillAcquiredBySkillLevel.get(0)/(float)totalSkillListByLevel.get(0)));									
 									String level1SkillAcquiredPercentage=constants.level1()+": "+  totalSkillAcquiredBySkillLevel.get(0) + " " + constants.of() + " " +totalSkillListByLevel.get(0) + " " +constants.skill()+ " ("+DECIMAL_FORMAT.format(totalPercentageLevel1)+")";
 									
 									float totalPercentageLevel2= totalSkillListByLevel.get(1)==0.0f?totalPercentageLevel2=0.0f:((totalSkillAcquiredBySkillLevel.get(1)/(float)totalSkillListByLevel.get(1)));
 									String level2SkillAcquiredPercentage=constants.level2()+": "+ totalSkillAcquiredBySkillLevel.get(1) + " " + constants.of() + " " +totalSkillListByLevel.get(1)  + " " +constants.skill()+ " ("+DECIMAL_FORMAT.format(totalPercentageLevel2)+")";
 									
 									long totalSkillAcquiredByStudent=totalSkillAcquiredBySkillLevel.get(0)+totalSkillAcquiredBySkillLevel.get(1);
 									long totalSkillBySkillLevel=totalSkillListByLevel.get(0)+totalSkillListByLevel.get(1);
 									float totalPercentage=totalSkillBySkillLevel==0?totalPercentage=0.0f:((totalSkillAcquiredByStudent/(float)totalSkillBySkillLevel));
 									
 									String totalSkillAcquiredPercentage=constants.total()+": "+ totalSkillAcquiredByStudent  + " " + constants.of() + " " +totalSkillBySkillLevel + " " +constants.skill()+ " ("+DECIMAL_FORMAT.format(totalPercentage)+")";
 									
 									Log.info(level1SkillAcquiredPercentage);
 									Log.info(level2SkillAcquiredPercentage);
 									Log.info(totalSkillAcquiredPercentage);
 									
 									
									view.getLblLevel1Progress().setText(getFormatedString(level1SkillAcquiredPercentage, 34));
 									view.getLblLevel1Progress().setTitle(level1SkillAcquiredPercentage);
 									//setProgress(view.getPrgBarLevel1(),totalSkillAcquiredBySkillLevel.get(1),totalSkillListByLevel.get(1),totalPercentageLevel1);
 									setProgress(view.getPrgBarLevel1(),0L,totalSkillListByLevel.get(0),totalSkillAcquiredBySkillLevel.get(0));
 									
 									
									view.getLblLevel2Progress().setText(getFormatedString(level2SkillAcquiredPercentage, 34));
 									view.getLblLevel2Progress().setTitle(level2SkillAcquiredPercentage);
 									//setProgress(view.getPrgBarLevel2(),totalSkillAcquiredBySkillLevel.get(0),totalSkillListByLevel.get(0),totalPercentageLevel2);
 									setProgress(view.getPrgBarLevel2(),0L,totalSkillListByLevel.get(1),totalSkillAcquiredBySkillLevel.get(1));
 									
 									
									view.getLblTotalProgress().setText(getFormatedString(totalSkillAcquiredPercentage, 34));
 									view.getLblTotalProgress().setTitle(totalSkillAcquiredPercentage);
 									//view.getPrgBarTotal().setProgress(totalPercentage);
 									setProgress(view.getPrgBarTotal(),0L,totalSkillBySkillLevel,totalSkillAcquiredByStudent);
 									
 								}
 								else
 								{
 									Log.error("Skill Acquired Size should be Two, Current Size is : " + totalSkillAcquiredBySkillLevel.size());
 								}
 									
 							}
 							
 							@Override
 							public void onFailure(ServerFailure error) 
 							{
 								super.onFailure(error);
 								Log.info("Failure");
 							}
 							@Override
 							public void onConstraintViolation(java.util.Set<javax.validation.ConstraintViolation<?>> violations) 
 							{
 								super.onConstraintViolation(violations);
 								Log.info("Violation");
 							};
 						});
 						
 					}
 					else
 					{
 						Log.error("Skill Level Size should be Two, Current Size is : " + totalSkillListByLevel.size());
 					}
 					
 				}
 				@Override
 				public void onFailure(ServerFailure error) 
 				{
 					super.onFailure(error);
 					Log.info("Failure");
 				}
 				@Override
 				public void onConstraintViolation(java.util.Set<javax.validation.ConstraintViolation<?>> violations) 
 				{
 					super.onConstraintViolation(violations);
 					Log.info("Violation");
 				};
 			});			
 	}
 
 	/*
 	 *	Set Value to Progress bar 
 	 */
 	private void setProgress(CustomProgressbar prgBar, Long minValue,Long maxValue, float currentProgress) 
 	{
 		prgBar.setMinProgress(minValue);
 		prgBar.setMaxProgress(maxValue);
 		prgBar.setProgress(currentProgress);
 	}
 	
 	/*
 	 * Set Value to Personnel Information Panel i.e. Student Name, Study Year etc..
 	 */
 	public void intiPersonnelInformation(StudentProxy studentProxy)
 	{
 		Log.info("Student Id: " +studentProxy.getId());
 		String studyYear="";
 		String studentName=getEmptyStringIfNull(studentProxy.getName()) + " " + getEmptyStringIfNull(studentProxy.getPreName());	
 		String studentId=getEmptyStringIfNull(studentProxy.getStudentId());		
 		if(studentProxy.getStudyYear()!=null)
 			studyYear=getEmptyStringIfNull(studentProxy.getStudyYear().name());
 		String email=getEmptyStringIfNull(studentProxy.getEmail());
 		
 		view.getLblNameVal().setText(getFormatedString(studentName, 16));
 		view.getLblNameVal().setTitle(studentName);		
 		view.getLblStudentIdVal().setText(getFormatedString(studentId, 16));
 		view.getLblStudentIdVal().setTitle(studentId);		
 		view.getLblStudeyYearvalue().setText(getFormatedString(studyYear, 16));
 		view.getLblStudeyYearvalue().setTitle(studyYear);		
 		view.getLblEmailVal().setText(getFormatedString(email, 16));
 		view.getLblEmailVal().setTitle(email);
 		
 	}
 	@Override
 	public void goTo(Place place) {
 		
 	}
 
 
 	@Override
 	public void changeStudentInformationClicked(ClickEvent event) 
 	{
 		Log.info("Change student Infomation Clicked");
 		view.getLblError().setVisible(false);
 		initStudentEditPopup(view.getStudentProxy(),event);
 	}
 
 	private void initStudentEditPopup(final StudentProxy studentProxy, ClickEvent event) 
 	{
 		Log.info("Student Proxy: " + studentProxy.getId());
 		final StudentEditPopupViewImpl popupView=new StudentEditPopupViewImpl();
 		popupView.setPopupPosition(event.getClientX()-60, event.getClientY()-135);
 		
 		popupView.addCloseHandler(new CloseHandler<PopupPanel>() 
 		{
 			@Override
 			public void onClose(CloseEvent<PopupPanel> event) 
 			{
 				view.getLblError().setVisible(false);
 			}
 		});
 		
 		requests.studentRequest().findStudent(studentProxy.getId()).fire(new Receiver<StudentProxy>() {
 
 			@Override
 			public void onSuccess(StudentProxy response) {
 				popupView.setTxtEmailValue(studentProxy.getEmail()!=null?studentProxy.getEmail():"");
 				popupView.setLstBoxStudyYear(studentProxy.getStudyYear()!=null?studentProxy.getStudyYear():null);
 				popupView.show();	
 				
 				popupView.getBtnSave().addClickHandler(new ClickHandler() {
 					
 					@Override
 					public void onClick(ClickEvent event) 
 					{
 						/*if(popupView.getLstBoxStudyYear().getValue()==null)
 						{
 							Window.alert(constants.studyYearCanNotbeNull());
 						}
 						else
 						{*/
 							final String email=popupView.getTxtEmailValue().getText();
							if(email.length()>=40)
							{
								view.getLblError().setVisible(true);
								view.getLblError().setText(constants.exceedEmailLength());
								return;
							}
 							final StudyYears studyYear=popupView.getLstBoxStudyYear().getValue();							
 							
 							StudentRequest studentRequest=requests.studentRequest();
 							StudentProxy proxy=studentProxy;
 							proxy=studentRequest.edit(proxy);
 							proxy.setEmail(email);
 							if(popupView.getLstBoxStudyYear().getValue()==null)
 							{
 								Log.info("Study Year is Null");
 								proxy.setStudyYear(null);
 							}
 							else
 								proxy.setStudyYear(studyYear);
 							
 							
 							final StudentProxy tempStudentProxy=proxy;
 							
 							
 							studentRequest.persist().using(proxy).fire(new Receiver<Void>() {
 
 								@Override
 								public void onSuccess(Void response) {
 									
 									view.getLblError().setVisible(false);
 									Log.info("Successfully updated.");
 									view.getLblEmailVal().setText(getFormatedString(email, 15));
 									view.getLblEmailVal().setTitle(email);
 									if(studyYear==null)
 									{
 										view.getLblStudeyYearvalue().setText(getFormatedString("", 15));
 										view.getLblStudeyYearvalue().setTitle("");
 									}
 									else
 									{
 										view.getLblStudeyYearvalue().setText(getFormatedString(studyYear.name(), 15));
 										view.getLblStudeyYearvalue().setTitle(studyYear.name());										
 									}
 									view.setStudentProxy(tempStudentProxy);
 									popupView.hide();
 									
 								}
 								@Override
 								public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
 									view.getLblError().setVisible(true);
 									view.getLblError().setText(constants.enterValidEmail());
 									//Window.alert("Please enter valid Email Address");
 								}
 								@Override
 								public void onFailure(ServerFailure error) {
 									super.onFailure(error);
 									Log.info("Failure");
 								}
 							});
 
 						//}
 					}
 				});
 				
 				popupView.getBtnClose().addClickHandler(new ClickHandler() {
 					
 					@Override
 					public void onClick(ClickEvent event) {
 						view.getLblError().setVisible(false);
 						popupView.hide();
 					}
 				});
 			}
 		});
 		
 	}
 
 	// Finalize Student
 	@Override
 	public void finalizeLogBookClick(StudentProxy studentProxy) 
 	{
 		Log.info("Student is going to Finalize.");
 		Log.info("Student Id: " + studentProxy.getId());
 		
 		view.getLblError().setVisible(false);
 		
 		StudentRequest studentRequest=requests.studentRequest();
 		StudentProxy proxy=studentProxy;		
 		
 		proxy=studentRequest.edit(proxy);
 		
 		proxy.setStudentStatus(StudentStatus.Fianllized);
 		
 		studentRequest.persist().using(proxy).fire(new Receiver<Void>() 
 		{
 			@Override
 			public void onSuccess(Void response) 
 			{
 				Log.info("Successfully Saved.");
 			}
 			@Override
 			public void onFailure(ServerFailure error) 
 			{
 				super.onFailure(error);
 				Log.info("Failure to Save.");
 			}
 			@Override
 			public void onConstraintViolation(Set<ConstraintViolation<?>> violations) 
 			{
 				super.onConstraintViolation(violations);
 				Log.info("Constraint Violate when Save.");
 			}
 		});
 	
 	
 	
 	}
 	
 }	
