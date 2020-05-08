 package kornell.gui.client.presentation.course.generic;
 
 import kornell.api.client.UserSession;
 import kornell.gui.client.event.ShowDetailsEvent;
 import kornell.gui.client.event.ShowDetailsEventHandler;
 import kornell.gui.client.presentation.course.ClassroomView;
 import kornell.gui.client.presentation.course.generic.details.GenericCourseDetailsView;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class GenericClassroomView extends Composite implements ClassroomView, ShowDetailsEventHandler {
 	interface MyUiBinder extends UiBinder<Widget, GenericClassroomView> {
 	}
 	private PlaceController placeCtrl;
 	private UserSession session;
 	private EventBus bus;
 	
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
 	@UiField
 	FlowPanel contentPanel;
 	@UiField
 	FlowPanel detailsPanel;
 	
 	private GenericCourseDetailsView detailsView;
 
 	private Presenter presenter;
 
 	public GenericClassroomView(PlaceController placeCtrl, UserSession session, EventBus bus) {
 		GWT.log("new GenericClassroomView");
 		this.placeCtrl = placeCtrl;
 		this.session = session;
 		this.bus = bus;
 		bus.addHandler(ShowDetailsEvent.TYPE,this);
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public void display() {
 		detailsView = new GenericCourseDetailsView(bus, session, placeCtrl);
 		detailsView.setPresenter(presenter);
 		detailsView.initData();
		contentPanel.setVisible(false);
 		detailsPanel.clear();
 		detailsPanel.add(detailsView);
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 	
 	@Override
 	public FlowPanel getContentPanel() {
 		return contentPanel;
 	}
 
 	@Override
 	public void onShowDetails(ShowDetailsEvent event) {
 		boolean showDetails = event.isShowDetails();
 		contentPanel.setVisible(!showDetails);
 		detailsPanel.setVisible(showDetails);
 	}
 }
