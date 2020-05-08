 /**
  * 
  */
 package ch.ethz.e4mooc.client;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import ch.ethz.e4mooc.client.page.eiffel.EiffelPagePresenter;
 import ch.ethz.e4mooc.client.page.eiffel.EiffelPageView;
 import ch.ethz.e4mooc.client.page.eiffel.EiffelPageViewImpl;
 import ch.ethz.e4mooc.client.page.root.RootPagePresenter;
 import ch.ethz.e4mooc.client.page.root.RootPageView;
 import ch.ethz.e4mooc.client.page.root.RootPageViewImpl;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author hce
  *
  */
 public class AppController implements ValueChangeHandler<String> {
 
 	/** the event bus */
 	private EventBus eventBus;
 	
 	/** the view of the root page */
 	RootPageView rootPageView;
 	/** the presenter of the root page */
 	RootPagePresenter rootPagePresenter;
 	
 	/** the view instance of the Eiffel page */
 	EiffelPageView eiffelView;
 	/** the presenter instance of the Eiffel page */
 	EiffelPagePresenter eiffelPagePresenter;
 	
 	
 	/**
 	 * Constructor.
 	 */
 	public AppController(EventBus eventBus) {
 		this.eventBus = eventBus;
 					
 		// bind listeners
 		bind();
 	}
 	
 	
 	/**
 	 * Register to receive History events.
 	 */
 	private void bind() {
 		History.addValueChangeHandler(this);
 	}
 	
 	
 	/**
 	 * Clears the page and displays the root page.
 	 */
 	private void switchToRootPage() {
 		RootPanel.get().clear();
 		rootPageView = new RootPageViewImpl();
 		// HACK: for now, we create a new EventBus for each page because we all presenters don't unbind(), meaning that "invisible" presenters keep listening to eventbus
 		eventBus = new SimpleEventBus();
 		rootPagePresenter = new RootPagePresenter(this.eventBus, rootPageView);
 		
 		rootPagePresenter.go(RootPanel.get());
 	}
 	
 	
 	/**
 	 * Clears the page and displays the Eiffel page.
 	 * @param projectName the name of the project that should be displayed
 	 */
 	private void switchToEiffelPage(String projectName) {
 		RootPanel.get().clear();
 		eiffelView = new EiffelPageViewImpl();
 		// HACK: for now, we create a new EventBus for each page because we all presenters don't unbind(), meaning that "invisible" presenters keep listening to eventbus
 		eventBus = new SimpleEventBus();
 		eiffelPagePresenter = new EiffelPagePresenter(this.eventBus, eiffelView, projectName);
 		
 		eiffelPagePresenter.go(RootPanel.get());
 	}
 	
 	
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		
 		// the projectNameUri is the part of the URI that contains the project name (e.g. hello_world of #hello_world?id=5)
 		final String projectNameUri = extractQueryParameters(event.getValue());
 
 		// check if the server has a project which name equals the token
 		// if yes, then load the Eiffel page for this project
 		if(projectNameUri != null) {
 			
 			E4mooc.projectService.hasProject(projectNameUri, new AsyncCallback<Boolean>() {
 
 				@Override
 				public void onSuccess(Boolean result) {
 					if(result)
 						switchToEiffelPage(projectNameUri);
 					else
 						switchToRootPage();
 				}
 				
 				@Override
 				public void onFailure(Throwable caught) {
 					// TODO: useful handling ?
 				}
 			});
 		}
 	}
 	
 	/**
 	 * Extracts the parameters from the query part of a URI.
 	 * @param uriQuery the query part of a URI.
 	 * @return the URI without the query part
 	 */
 	private String extractQueryParameters(String uriQuery) {
 		// we initialize the return value to the user-given URI
 		String result  = uriQuery;
 		
 		// check if the token contains any query parameters (i.e. ?field1=value1&field2=value2&field3=value3...)
 		if(uriQuery.contains("?")) {
 			// split the URI where the query part start (indicated by ?)
 			String[] uriParts = uriQuery.split("\\?");
 			
 			// if there are more than two elements in uriParts[], then there was something wrong with the URI and stop processing it further
 			if(uriParts.length == 2) {
 				// the URI part at index 0 is the project name; we store that in the token as it's used for selecting the presenter
 				result = uriParts[0];
 				
 				// the URI part at index 1 are the query parameters
 				Map<String, String> queryParameters = getParameterMap(uriParts[1]);
 				// we store the parameters in the client state
 				if(queryParameters.containsKey("id"))
 					E4mooc.cState.setUserId(queryParameters.get("id"));
 				if(queryParameters.containsKey("outputht"))
 					E4mooc.cState.setUserOutputBoxHeight(Integer.valueOf(queryParameters.get("outputht")));
 				if(queryParameters.containsKey("bgcolor"))
 					E4mooc.cState.setUserBackgroundColor(queryParameters.get("bgcolor"));
 				if(queryParameters.containsKey("groupid"))
 					E4mooc.cState.setUserGroupId(queryParameters.get("groupid"));
 			}
 		}
 		return result;
 	}
 	
 	private Map<String, String> getParameterMap(String parameterString) {
 		Map<String, String> result = new HashMap<String, String>();
 		// split at each '&' and put the pairs in the result map
 		String[] pairs = parameterString.split("&");
 		for(String pair: pairs) {
 			int idx = pair.indexOf("=");
 			// have to decode the Url to get the original strings (e.g. space is %20 in Url formatting)
			result.put(URL.decode(pair.substring(0, idx)), URL.decode(pair.substring(idx + 1)));
 		}
 		return result;
 	}
 	
 
 	/**
 	 * Call the go method after everything has been wired up.
 	 */
 	public void go() {
 		
 		// the rootPage should be displayed at first
 		switchToRootPage();
 		
 		// get all the project names available from the server
 		E4mooc.projectService.getAllProjectNames(new AsyncCallback<LinkedList<String>>() {
 
 			// if the URL has a history token equaling a project name, we switch to the Eiffel page for that project
 			@Override
 			public void onSuccess(LinkedList<String> result) {
 				
 				final String projectNameUri = extractQueryParameters(History.getToken());
 				// check if the there's a project with the name of the history token; if yes, switch to Eiffel page
 				if(result.contains(projectNameUri))
 					switchToEiffelPage(projectNameUri);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO: useful handling?
 			}
 		});
 	}
 	
 }
