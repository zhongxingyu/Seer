 package wicket.contrib.activewidgets.examples;
 
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

 import wicket.contrib.activewidgets.ActiveWidgetsConfiguration;
 import wicket.contrib.activewidgets.grid.Grid;
 
 
 public class GridPage extends WicketExamplePage {
	int width = 300;
	int height = 200;
	boolean selectorVisible = true;
	int rowCount = 20;
	
 	public GridPage() {
 		super();
 		String isArgumentGiven = System.getProperty(ActiveWidgetsConfiguration.KEY_AW_LICENSE);
 		if (isArgumentGiven == null) {
 			ActiveWidgetsConfiguration.setLicenseType(ActiveWidgetsConfiguration.AW_TRIAL_LICENSE);
 		}
 		add(new Grid("grid")
				.setWidth(width)
				.setHeight(height)
				.setSelectorVisible(selectorVisible)
				.setRowCount(rowCount)
 		);
		Form form;
		add(form = new Form("form"));
		form.add(new TextField("width", new PropertyModel(this, "width")));
		form.add(new TextField("height", new PropertyModel(this, "height")));
 	}
 	
 }
