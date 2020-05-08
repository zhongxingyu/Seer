 package gui.main;
 
 import model.common.VaultPickler;
 import model.storage.StorageManager;
 import gui.common.*;
 
 /**
  * Controller class for the main view.  The main view allows the user
  * to print reports and exit the application.
  */
 public class MainController extends Controller implements IMainController {
 
 	
 
 	/**
 	 * Constructor.
 	 *  
 	 * @param view Reference to the main view
 	 */
 	public MainController(IMainView view) {
 		super(view);
 		construct();
		_pickler = new VaultPickler();	
 	}
 	
 	/**
 	 * Returns a reference to the view for this controller.
 	 */
 	@Override
 	protected IMainView getView() {
 		return (IMainView)super.getView();
 	}
 
 	//
 	// IMainController overrides
 	//
 
 	/**
 	 * Returns true if and only if the "Exit" menu item should be enabled.
 	 */
 	@Override
 	public boolean canExit() {
 		return true;
 	}
 
 	/**
 	 * This method is called when the user exits the application.
 	 */
 	@Override
 	public void exit() {
 		StorageManager.getInstance().hitClose();
 	}
 
 	/**
 	 * Returns true if and only if the "Expired Items" menu item should be enabled.
 	 */
 	@Override
 	public boolean canPrintExpiredReport() {
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Expired Items" 
 	 * menu item.
 	 */
 	@Override
 	public void printExpiredReport() {
 		getView().displayExpiredReportView();
 	}
 
 	/**
 	 * Returns true if and only if the "N-Month Supply" menu item should be enabled.
 	 */
 	@Override
 	public boolean canPrintSupplyReport() {
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "N-Month Supply" menu 
 	 * item.
 	 */
 	@Override
 	public void printSupplyReport() {
 		getView().displaySupplyReportView();
 	}
 
 	/**
 	 * Returns true if and only if the "Product Statistics" menu item should be enabled.
 	 */
 	@Override
 	public boolean canPrintProductReport() {
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Product Statistics" menu 
 	 * item.
 	 */
 	@Override
 	public void printProductReport() {
 		getView().displayProductReportView();
 	}
 
 	/**
 	 * Returns true if and only if the "Notices" menu item should be enabled.
 	 */
 	@Override
 	public boolean canPrintNoticesReport() {
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Notices" menu 
 	 * item.
 	 */
 	@Override
 	public void printNoticesReport() {
 		getView().displayNoticesReportView();
 	}
 
 	/**
 	 * Returns true if and only if the "Removed Items" menu item should be enabled.
 	 */
 	@Override
 	public boolean canPrintRemovedReport() {
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Removed Items" menu 
 	 * item.
 	 */
 	@Override
 	public void printRemovedReport() {
 		getView().displayRemovedReportView();
 	}
 
 }
 
