 package kornell.gui.client;
 
 import kornell.api.client.Callback;
 import kornell.api.client.KornellClient;
 import kornell.core.shared.data.Institution;
 import kornell.core.shared.to.UserInfoTO;
 import kornell.gui.client.personnel.Captain;
 import kornell.gui.client.personnel.Dean;
 import kornell.gui.client.personnel.Stalker;
 import kornell.gui.client.presentation.GlobalActivityMapper;
 import kornell.gui.client.presentation.HistoryMapper;
 import kornell.gui.client.presentation.atividade.generic.GenericCourseView;
 import kornell.gui.client.presentation.bar.MenuBarView;
 import kornell.gui.client.presentation.bar.SouthBarView;
 import kornell.gui.client.presentation.bar.generic.GenericMenuBarView;
 import kornell.gui.client.presentation.bar.generic.GenericSouthBarView;
 import kornell.gui.client.presentation.course.CoursePlace;
 import kornell.gui.client.presentation.course.CoursePresenter;
 import kornell.gui.client.presentation.course.CourseView;
 import kornell.gui.client.presentation.course.chat.CourseChatPresenter;
 import kornell.gui.client.presentation.course.chat.CourseChatView;
 import kornell.gui.client.presentation.course.chat.generic.GenericCourseChatView;
 import kornell.gui.client.presentation.course.course.CourseHomePresenter;
 import kornell.gui.client.presentation.course.course.CourseHomeView;
 import kornell.gui.client.presentation.course.course.generic.GenericCourseHomeView;
 import kornell.gui.client.presentation.course.details.CourseDetailsPresenter;
 import kornell.gui.client.presentation.course.details.CourseDetailsView;
 import kornell.gui.client.presentation.course.details.generic.GenericCourseDetailsView;
 import kornell.gui.client.presentation.course.forum.CourseForumPresenter;
 import kornell.gui.client.presentation.course.forum.CourseForumView;
 import kornell.gui.client.presentation.course.forum.generic.GenericCourseForumView;
 import kornell.gui.client.presentation.course.library.CourseLibraryPresenter;
 import kornell.gui.client.presentation.course.library.CourseLibraryView;
 import kornell.gui.client.presentation.course.library.generic.GenericCourseLibraryView;
 import kornell.gui.client.presentation.course.specialists.CourseSpecialistsPresenter;
 import kornell.gui.client.presentation.course.specialists.CourseSpecialistsView;
 import kornell.gui.client.presentation.course.specialists.generic.GenericCourseSpecialistsView;
 import kornell.gui.client.presentation.home.HomeView;
 import kornell.gui.client.presentation.home.generic.GenericHomeView;
 import kornell.gui.client.presentation.profile.ProfileView;
 import kornell.gui.client.presentation.profile.generic.GenericProfileView;
 import kornell.gui.client.presentation.sandbox.SandboxPresenter;
 import kornell.gui.client.presentation.sandbox.SandboxView;
 import kornell.gui.client.presentation.sandbox.generic.GenericSandboxView;
 import kornell.gui.client.presentation.terms.TermsPlace;
 import kornell.gui.client.presentation.terms.TermsView;
 import kornell.gui.client.presentation.terms.generic.GenericTermsView;
 import kornell.gui.client.presentation.vitrine.VitrinePlace;
 import kornell.gui.client.presentation.vitrine.VitrineView;
 import kornell.gui.client.presentation.vitrine.generic.GenericVitrineView;
 import kornell.gui.client.presentation.welcome.WelcomeView;
 import kornell.gui.client.presentation.welcome.generic.GenericWelcomeView;
 import kornell.gui.client.scorm.API_1484_11;
 import kornell.gui.client.sequence.SequencerFactory;
 import kornell.gui.client.sequence.SequencerFactoryImpl;
 import kornell.gui.client.util.ClientProperties;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceChangeEvent;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.SimpleEventBus;
 
 public class GenericClientFactoryImpl implements ClientFactory {
 	/* History Management */
 	private final EventBus bus = new SimpleEventBus();
 	private final PlaceController placeCtrl = new PlaceController(
 			bus);
 	private final HistoryMapper historyMapper = GWT.create(HistoryMapper.class);
 	private final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(
 			historyMapper);
 
 	/* Activity Managers */
 	private ActivityManager globalActivityManager;
 
 	private SimplePanel appPanel;
 
 	/* REST API Client */
 	private static final KornellClient client = KornellClient.getInstance();
 
 	/* Views */
 	private GenericMenuBarView menuBarView;
 	private SouthBarView southBarView;
 	
 	private GenericHomeView genericHomeView;
 	private CoursePresenter coursePresenter;
 	private CourseHomePresenter courseHomePresenter;
 	private CourseDetailsPresenter courseDetailsPresenter;
 	private CourseLibraryPresenter courseLibraryPresenter;
 	private CourseForumPresenter courseForumPresenter;
 	private CourseChatPresenter courseChatPresenter;
 	private CourseSpecialistsPresenter courseSpecialistsPresenter;
 
 	/* GUI */
 	SimplePanel shell = new SimplePanel();
 	public final CoursePlace DEFAULT_PLACE = new CoursePlace("d9aaa03a-f225-48b9-8cc9-15495606ac46");
 	private Place defaultPlace;
 	private SandboxPresenter sandboxPresenter;
 
 	public GenericClientFactoryImpl() {
 	}
 
 	private void initActivityManagers() {
 		initGlobalActivityManager();
 	}
 
 	private void initGlobalActivityManager() {
 		globalActivityManager = new ActivityManager(new GlobalActivityMapper(
 				this), bus);
 		globalActivityManager.setDisplay(shell);
 	}
 
 	private void initHistoryHandler(Place defaultPlace) {
 		historyHandler.register(placeCtrl, bus, defaultPlace);
 		new Stalker(bus,client,historyMapper);
 		historyHandler.handleCurrentHistory();
 	}
 
 	private void initGUI() {
 		final RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
 		final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
 		dockLayoutPanel.addNorth(getMenuBarView(), 45);
 		dockLayoutPanel.addSouth(getSouthBarView(), 35);
 		
 		ScrollPanel sp = new ScrollPanel();
 		sp.add(shell);
 		dockLayoutPanel.add(sp);
 		sp.addStyleName("vScrollBar");
 		dockLayoutPanel.addStyleName("wrapper");
 		rootLayoutPanel.add(dockLayoutPanel);
 
 		bus.addHandler(PlaceChangeEvent.TYPE,
 				new PlaceChangeEvent.Handler() {
 					@Override
 					public void onPlaceChange(PlaceChangeEvent event) {
 						setPlaceNameAsBodyStyle(event);
 						
 						Place newPlace = event.getNewPlace();
 						dockLayoutPanel.setWidgetHidden((Widget) getSouthBarView(), !getSouthBarView().isVisible());
 						
 
 						if(placeCtrl.getWhere() instanceof VitrinePlace){
 							dockLayoutPanel.setWidgetSize(getMenuBarView().asWidget(), 0);
 						} else {
 							dockLayoutPanel.setWidgetSize(getMenuBarView().asWidget(), 45);
 							getMenuBarView().display();
 						}
 					}
 
 					private void setPlaceNameAsBodyStyle(PlaceChangeEvent event) {
 						String styleName = rootLayoutPanel.getStyleName();
 						if (!styleName.isEmpty())
 							rootLayoutPanel.removeStyleName(styleName);
 						String[] split = event.getNewPlace().getClass().getName().split("\\.");
 						String newStyle = split[split.length - 1];
 						rootLayoutPanel.addStyleName(newStyle);
 					}
 				});
 
 	}
 
 	private MenuBarView getMenuBarView() {
 		if (menuBarView == null)
 			menuBarView = new GenericMenuBarView(bus, client, placeCtrl);
 		return menuBarView;
 	}
 	
 	private SouthBarView getSouthBarView() {
 		if (southBarView == null)
 			southBarView = new GenericSouthBarView(bus, placeCtrl, client);
 		return southBarView;
 	}
 
 	@Override
 	public ClientFactory startApp() {
 		//TODO: Consider caching credentials to avoid this request
 		client.getCurrentUser(new Callback<UserInfoTO>(){
 			@Override
 			protected void ok(UserInfoTO user) {
 					String token;
 					if(!"".equals(Window.Location.getHash()) && 
 							"details".equals(Window.Location.getHash().split(":")[0].split("#")[1])){
 						token = Window.Location.getHash().split("#")[1];
 					} else {
 						token = user.getLastPlaceVisited();
 					}
 					if(token != null){
 						defaultPlace = historyMapper.getPlace(token);
 					}else {
 						defaultPlace = DEFAULT_PLACE;
 					}				
 					startApp(defaultPlace);
 			}
 
 			@Override
 			protected void unauthorized() {
 				VitrinePlace vitrinePlace;
				if(Window.Location.getHash().split(":").length > 1 && "#vitrine".equalsIgnoreCase(Window.Location.getHash().split(":")[0])){
 					vitrinePlace = new VitrinePlace(Window.Location.getHash().split(":")[1]);
 				} else {
 					vitrinePlace = new VitrinePlace();
 				}
 				startApp(vitrinePlace);
 			}
 			
 			protected void startApp(final Place defaultPlace){
 				//TODO not good
 				client.institution("00a4966d-5442-4a44-9490-ef36f133a259").getInstitution(new Callback<Institution>(){
 					@Override
 					protected void ok(Institution institution){
 						ClientProperties.setEncoded(ClientProperties.INSTITUTION_ASSETS_URL, institution.getAssetsURL());
 						ClientProperties.setEncoded(ClientProperties.INSTITUTION_NAME, institution.getName());
 						initGUI();
 						initActivityManagers();
 						initHistoryHandler(defaultPlace);
 						initException();
 						initSCORM();
 						initPersonnel();
 					}
 				});
 			}
 
 			private void initPersonnel() {
 				new Captain(bus, placeCtrl);	
 				new Dean(bus, client);
 			}			
 		});
 		return this;
 	}
 
 	private void initSCORM() {
 		new API_1484_11(bus).bindToWindow();
 	}
 
 	private void initException() {
 		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
 
 			@Override
 			public void onUncaughtException(Throwable e) {
 				System.out.println("** UNCAUGHT **");
 				e.printStackTrace();
 			}
 		});
 	}
 
 	@Override
 	public HomeView getHomeView() {
 		if (genericHomeView == null) {
 			genericHomeView = new GenericHomeView(this, bus,
 					historyHandler, client, appPanel);
 		}
 		return genericHomeView;
 	}
 
 	@Override
 	public VitrineView getVitrineView() {
 		return new GenericVitrineView( historyMapper , placeCtrl, DEFAULT_PLACE, client);
 	}
 
 	private Place getDefaultPlace() {
 		return defaultPlace;
 	}
 
 	@Override
 	public WelcomeView getWelcomeView() {
 		return new GenericWelcomeView(bus, client, placeCtrl);
 	}
 
 	@Override
 	public ProfileView getProfileView() {
 		return new GenericProfileView(bus, client, placeCtrl);
 	}
 
 	@Override
 	public CourseView getCourseView() {
 		return new GenericCourseView(bus);
 	}
 
 	@Override
 	public TermsView getTermsView() {
 		return new GenericTermsView(bus, client, placeCtrl, DEFAULT_PLACE);
 	}
 	
 	
 	
 	@Override
 	public CourseHomePresenter getCourseHomePresenter() {
 		if (courseHomePresenter == null) {
 			CourseHomeView courseHomeView = getCourseHomeView();
 			
 			courseHomePresenter = new CourseHomePresenter(courseHomeView, placeCtrl);
 		}
 		return courseHomePresenter;
 	}
 	@Override
 	public CourseHomeView getCourseHomeView() {
 		return new GenericCourseHomeView(bus, client, placeCtrl);
 	}
 	
 	
 	
 	@Override
 	public CourseDetailsPresenter getCourseDetailsPresenter() {
 		if (courseDetailsPresenter == null) {
 			CourseDetailsView courseDetailsView = getCourseDetailsView();
 			
 			courseDetailsPresenter = new CourseDetailsPresenter(courseDetailsView, placeCtrl);
 		}
 		return courseDetailsPresenter;
 	}
 	@Override
 	public CourseDetailsView getCourseDetailsView() {
 		return new GenericCourseDetailsView(bus, client, placeCtrl);
 	}
 	
 	
 	
 	@Override
 	public CourseLibraryPresenter getCourseLibraryPresenter() {
 		if (courseLibraryPresenter == null) {
 			CourseLibraryView courseLibraryView = getCourseLibraryView();
 			
 			courseLibraryPresenter = new CourseLibraryPresenter(courseLibraryView, placeCtrl);
 		}
 		return courseLibraryPresenter;
 	}
 	@Override
 	public CourseLibraryView getCourseLibraryView() {
 		return new GenericCourseLibraryView(bus, client, placeCtrl);
 	}
 	
 	
 	
 	@Override
 	public CourseForumPresenter getCourseForumPresenter() {
 		if (courseForumPresenter == null) {
 			CourseForumView courseForumView = getCourseForumView();
 			
 			courseForumPresenter = new CourseForumPresenter(courseForumView, placeCtrl);
 		}
 		return courseForumPresenter;
 	}
 	@Override
 	public CourseForumView getCourseForumView() {
 		return new GenericCourseForumView(bus, client, placeCtrl);
 	}
 	
 	
 	
 	@Override
 	public CourseChatPresenter getCourseChatPresenter() {
 		if (courseChatPresenter == null) {
 			CourseChatView courseChatView = getCourseChatView();			
 			courseChatPresenter = new CourseChatPresenter(courseChatView, placeCtrl);
 		}
 		return courseChatPresenter;
 	}
 	@Override
 	public CourseChatView getCourseChatView() {
 		return new GenericCourseChatView(bus, client, placeCtrl);
 	}
 	
 	
 	
 	@Override
 	public CourseSpecialistsPresenter getCourseSpecialistsPresenter() {
 		if (courseSpecialistsPresenter == null) {
 			CourseSpecialistsView courseSpecialistsView = getCourseSpecialistsView();
 			
 			courseSpecialistsPresenter = new CourseSpecialistsPresenter(courseSpecialistsView, placeCtrl);
 		}
 		return courseSpecialistsPresenter;
 	}
 	@Override
 	public CourseSpecialistsView getCourseSpecialistsView() {
 		return new GenericCourseSpecialistsView(bus, client, placeCtrl);
 	}
 	
 
 	@Override
 	public CoursePresenter getCoursePresenter() {
 		SequencerFactory rendererFactory = new SequencerFactoryImpl(bus,placeCtrl,client);
 		if (coursePresenter == null) {
 			CourseView activityView = getCourseView();			
 			coursePresenter = new CoursePresenter(activityView, placeCtrl, rendererFactory);
 		}
 		return coursePresenter;
 	}
 
 	@Override
 	public SandboxView getSandboxView() {
 		return new GenericSandboxView();
 	}
 
 	@Override
 	public SandboxPresenter getSandboxPresenter() {
 		if (sandboxPresenter == null) {
 			SandboxView sandboxView = getSandboxView();			
 			sandboxPresenter = new SandboxPresenter(sandboxView);
 		}
 		return sandboxPresenter;
 	}
 	
 	
 }
