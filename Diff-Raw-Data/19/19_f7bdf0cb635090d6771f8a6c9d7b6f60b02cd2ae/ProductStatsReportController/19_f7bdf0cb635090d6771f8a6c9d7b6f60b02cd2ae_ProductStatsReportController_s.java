 package gui.reports.productstats;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 
 import model.reports.*;
 import gui.common.*;
 
 /**
  * Controller class for the product statistics report view.
  */
 public class ProductStatsReportController extends Controller implements
 		IProductStatsReportController {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view Reference to the item statistics report view
 	 */
 	public ProductStatsReportController(IView view) {
 		super(view);
		
 		construct();
 	}
 
 	//
 	// Controller overrides
 	//
 	
 	/**
 	 * Returns a reference to the view for this controller.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post Returns a reference to the view for this controller.}
 	 */
 	@Override
 	protected IProductStatsReportView getView() {
 		return (IProductStatsReportView)super.getView();
 	}
 
 	/**
 	 * Sets the enable/disable state of all components in the controller's view.
 	 * A component should be enabled only if the user is currently
 	 * allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view
 	 * have been set appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
 	}
 
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 *  {@pre None}
 	 *  
 	 *  {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
 	}
 
 	//
 	// IProductStatsReportController overrides
 	//
 
 	/**
 	 * This method is called when any of the fields in the
 	 * product statistics report view is changed by the user.
 	 */
 	@Override
 	public void valuesChanged() {
        this.getView().enableOK(true);
        try{
            Integer.parseInt(getView().getMonths());
        } catch (Exception e){
            this.getView().enableOK(false);
        }
 	}
 	
 	/**
 	 * This method is called when the user clicks the "OK"
 	 * button in the product statistics report view.
 	 */
 	@Override
 	public void display() {
 		ReportBuilder builder = (getView().getFormat() == FileFormat.HTML) ? new HTMLReportBuilder() : new PDFReportBuilder();
 		IReportDirector director = new StatisticReport();
 		director.setBuilder(builder);
         int months = Integer.parseInt(getView().getMonths());
         ((StatisticReport)director).setMonths(months);
 		director.constructReport();
 		if (Desktop.isDesktopSupported()) {
 			  try {
 			  File myFile = new File(builder.returnReport());
 			  Desktop.getDesktop().open(myFile);
 			  } catch (IOException ex) {
 			  // no application registered for PDFs
 			  }
 		}
 	}
 
 }
 
