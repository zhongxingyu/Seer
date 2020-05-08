 package gui.reports.supply;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 
 import model.reports.*;
 import gui.common.*;
 
 /**
  * Controller class for the N-month supply report view.
  */
 	public class SupplyReportController extends Controller implements
 		ISupplyReportController {
 /*---	STUDENT-INCLUDE-BEGIN
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view Reference to the N-month supply report view
 	 */	
 	public SupplyReportController(IView view) {
 		super(view);
 		getView().setMonths("3");
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
 	protected ISupplyReportView getView() {
 		return (ISupplyReportView)super.getView();
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
 		int months;
 		try {
 			months = Integer.parseInt(getView().getMonths());
 		} catch (NumberFormatException e) {
			e.printStackTrace();
 			months = -1;
 		}
 		boolean valid = (months > 0 && months <= 100) ? true : false;
 		getView().enableOK(valid);
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
 	// IExpiredReportController overrides
 	//
 
 	/**
 	 * This method is called when any of the fields in the
 	 * N-month supply report view is changed by the user.
 	 */
 	@Override
 	public void valuesChanged() {
 		enableComponents();
 	}
 	
 	/**
 	 * This method is called when the user clicks the "OK"
 	 * button in the N-month supply report view.
 	 */
 	@Override
 	public void display() {
 		ReportBuilder builder = (getView().getFormat() == FileFormat.HTML) ? new HTMLReportBuilder() : new PDFReportBuilder();
 		IReportDirector director = new NSupplyReport(Integer.parseInt(this.getView().getMonths()));
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
 
