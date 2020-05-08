 package wicket.contrib.activewidgets.examples;
 
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.PropertyModel;
 
 import wicket.contrib.activewidgets.ActiveWidgetsConfiguration;
 import wicket.contrib.activewidgets.grid.Grid;
 
 
 public class GridPage extends WicketExamplePage {
	private static final long serialVersionUID = 1L;
	private final int width = 300;
	private final int height = 200;
	private final int rowCount = 20;
	private final boolean selectorVisible = true;
 	
 	public GridPage() {
 		super();
 		String isArgumentGiven = System.getProperty(ActiveWidgetsConfiguration.KEY_AW_LICENSE);
 		if (isArgumentGiven == null) {
 			ActiveWidgetsConfiguration.setLicenseType(ActiveWidgetsConfiguration.AW_TRIAL_LICENSE);
 		}
		Grid grid;
		add(grid = new Grid("grid")
 				.setWidth(width)
 				.setHeight(height)
 				.setSelectorVisible(selectorVisible)
 				.setRowCount(rowCount)
 		);
		
 		Form form;
		
 		add(form = new Form("form"));
		form.add(new TextField("width", new PropertyModel(grid, "width.value")));
		form.add(new TextField("height", new PropertyModel(grid, "height.value")));
		form.add(new TextField("rowCount", new PropertyModel(grid, "rowCount.value")));
 	}
 	
 }
