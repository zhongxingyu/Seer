 package wicket.contrib.activewidgets.examples;
 
 import wicket.contrib.activewidgets.ActiveWidgetsConfiguration;
 import wicket.contrib.activewidgets.grid.Grid;
 
 
 public class GridPage extends WicketExamplePage {
 	public GridPage() {
 		super();
 		String isArgumentGiven = System.getProperty(ActiveWidgetsConfiguration.KEY_AW_LICENSE);
 		if (isArgumentGiven == null) {
 			ActiveWidgetsConfiguration.setLicenseType(ActiveWidgetsConfiguration.AW_TRIAL_LICENSE);
 		}
 		add(new Grid("grid")
				.setWidth(300)
				.setHeight(150)
				.setSelectorVisible(true)
				.setRowCount(20)
 		);
 	}
 	
 }
