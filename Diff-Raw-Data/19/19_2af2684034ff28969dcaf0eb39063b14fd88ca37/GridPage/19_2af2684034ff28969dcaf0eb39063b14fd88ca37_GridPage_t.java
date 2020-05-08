 package wicket.contrib.activewidgets.examples;
 
import wicket.contrib.activewidgets.ActiveWidgetsConfiguration;
 import wicket.contrib.activewidgets.markup.html.Grid;
 
 
 public class GridPage extends WicketExamplePage {
 	public GridPage() {
 		super();
		ActiveWidgetsConfiguration.setLicenseType(ActiveWidgetsConfiguration.AW_DEVELOPER_LICENSE);
		add(new Grid("grid")
				.width(800)
		);
 	}
 	
 }
