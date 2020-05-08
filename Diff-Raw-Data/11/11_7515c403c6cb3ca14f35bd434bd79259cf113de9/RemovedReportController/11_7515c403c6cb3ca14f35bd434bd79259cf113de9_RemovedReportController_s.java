 package gui.reports.removed;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 
 import org.joda.time.DateTime;
 
 import model.item.ItemVault;
 import model.reports.HTMLReportBuilder;
 import model.reports.IReportDirector;
 import model.reports.ObjectReportBuilder;
 import model.reports.PDFReportBuilder;
 import model.reports.RemovedItemsReport;
 import model.reports.ReportBuilder;
 import gui.common.*;
 
 /**
  * Controller class for the removed items report view.
  */
 public class RemovedReportController extends Controller implements
 		IRemovedReportController {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view Reference to the removed items report view
 	 */
 	public RemovedReportController(IView view) {
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
 	protected IRemovedReportView getView() {
 		return (IRemovedReportView) super.getView();
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
 		if(ItemVault.getInstance().sinceLastRemovedReport != null){
 			
 			this.getView().setSinceLast(true);
 			this.getView().setSinceLastValue(ItemVault.getInstance().sinceLastRemovedReport.toDate());
 			this.getView().setSinceDateValue(ItemVault.getInstance().sinceLastRemovedReport.toDate());
 		}
 		else{
 			this.getView().setSinceDate(true);
 			this.getView().setSinceDateValue(new DateTime().now().toDate());
 		}
 	}
 
 	//
 	// IExpiredReportController overrides
 	//
 
 	/**
 	 * This method is called when any of the fields in the
 	 * removed items report view is changed by the user.
 	 */
 	@Override
 	public void valuesChanged() {
 	}
 
 	/**
 	 * This method is called when the user clicks the "OK"
 	 * button in the removed items report view.
 	 */
 	@Override
 	public void display() {
 		ReportBuilder builder = (getView().getFormat() == FileFormat.HTML) ? new HTMLReportBuilder() : new PDFReportBuilder();
 		IReportDirector director;
 		
 		if(this.getView().getSinceDate() == true)
 			director = new RemovedItemsReport(new DateTime(this.getView().getSinceDateValue()));
 		else {
 			if((ItemVault.getInstance().sinceLastRemovedReport) == null)
 				director = new RemovedItemsReport(new DateTime(0));
 			else
 				director = new RemovedItemsReport(ItemVault.getInstance().sinceLastRemovedReport);
 		}
 		
 		director.setBuilder(builder);
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
 
