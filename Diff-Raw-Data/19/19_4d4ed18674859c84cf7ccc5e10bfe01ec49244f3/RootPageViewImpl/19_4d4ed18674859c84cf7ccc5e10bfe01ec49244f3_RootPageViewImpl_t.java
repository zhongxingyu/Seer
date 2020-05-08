 /**
  * 
  */
 package ch.ethz.e4mooc.client.page.root;
 
 import java.util.LinkedList;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.constants.ButtonType;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * The simple page only provides 2 input areas and a button.
  * 
  * @author hce
  *
  */
 public class RootPageViewImpl extends Composite implements RootPageView {
 
 	private static SimplePageViewImplUiBinder uiBinder = GWT.create(SimplePageViewImplUiBinder.class);
 
 	interface SimplePageViewImplUiBinder extends UiBinder<Widget, RootPageViewImpl> {
 	}
 	
 	@UiField
 	HTMLPanel mainPanel;
 	
 	@UiField
 	Grid projectGrid;
 	
 	/** The presenter of this view */
 	private Presenter presenter;
 
 	/**
 	 * Because this class has a default constructor, it can
 	 * be used as a binder template. In other words, it can be used in other
 	 * *.ui.xml files as follows:
 	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
 	 *   xmlns:g="urn:import:**user's package**">
 	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
 	 * </ui:UiBinder>
 	 * Note that depending on the widget that is used, it may be necessary to
 	 * implement HasHTML instead of HasText.
 	 */
 	public RootPageViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
		
		// we set the bg picutre directly in the body element
		RootPanel.getBodyElement().getStyle().setBackgroundImage("url(img/texturetastic_gray.png)");
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void addProjectNames(LinkedList<String> projectNames) {
 		
 		// text shown on the button that's used to show the IFRAME code
 		final String btnText = "Share";
 		
 		int numOfProjects = projectNames.size(); 
 		// add as many rows to the table as we have project names (+1 for the header)
 		projectGrid.resizeRows(numOfProjects + 1);
 		
 
 		
 		// the loop starts at 1 because the row 0 is the header
 		for(int i = 1; i <= numOfProjects; i++) {
 			// get the name of the project
 			final String projectName = projectNames.get(i - 1);
 			
 			// create a hyperlink for the project name; clicking it will cause a history event
 			Hyperlink hl = new Hyperlink(projectName, projectName);
 			
 			// add the link to the table
 			projectGrid.setWidget(i, 0, hl);
 			
 			final VerticalPanel vPanel = new VerticalPanel();
 			projectGrid.setWidget(i, 1, vPanel);
 			
 			if(i % 2 == 0)
 				projectGrid.getRowFormatter().addStyleName(i, "even");
 			else
 				projectGrid.getRowFormatter().addStyleName(i, "uneven");
 			
 			final Button b = new Button(btnText);
 			b.setType(ButtonType.DEFAULT);
 			b.addStyleName("share-Button");
 			
 			b.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					
 					if(vPanel.getWidgetCount() > 1) {
 					 // we're displaying more the than the button, thus a click on the button should hide everyting
 						b.setText(btnText);
 						vPanel.remove(1);
 					}
 					else {
 						b.setText(btnText);
 						TextArea code = new TextArea();
 						code.setWidth("273px");
 						code.setHeight("100px");
 						code.setText("<iframe width=\"800\" height=\"1000\"\nsrc=\"http://bmse-sandbox.inf.ethz.ch:8080/e4mooc/#" + projectName + "\" frameborder=\"0\"></iframe>");
 						vPanel.add(code);
 					}
 					
 				}
 			});
 			vPanel.add(b);
 		
 		}	
 		
 	}
 
 	@Override
 	public HasWidgets getContentContainer() {
 		return mainPanel;
 	}
 }
