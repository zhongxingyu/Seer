 package tr.org.linux.opencourseorganizer.client.activity;
 
 import tr.org.linux.opencourseorganizer.client.ClientFactory;
 import tr.org.linux.opencourseorganizer.client.place.HomePlace;
 import tr.org.linux.opencourseorganizer.client.ui.Home;
 import tr.org.linux.opencourseorganizer.client.ui.HomeView;
 
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.inject.Inject;
 
 public class HomeActivity extends AbstractActivity {
 
 	private final ClientFactory clientFactory;
 
 	@Inject
 	public HomeActivity(ClientFactory clientFactory, HomePlace place) {
 		this.clientFactory = clientFactory;
 	}
 
 	@Override
	public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
		start(panel, (com.google.web.bindery.event.shared.EventBus) eventBus);
	}

 	public void start(AcceptsOneWidget panel, EventBus eventBus) {
 		Home.Presenter presenter = clientFactory.getHomePresenter();
 		presenter.setView(new HomeView());
 		presenter.go(panel);
 	}
 
 }
