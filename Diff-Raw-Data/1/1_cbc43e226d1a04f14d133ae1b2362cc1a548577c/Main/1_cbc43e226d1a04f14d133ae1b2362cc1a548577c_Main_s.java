 import model.ModelFacade;
 import model.data.Data;
 import model.services.calculation.CalculationFacade;
 import model.services.importexport.ExportFacade;
 import view.ViewFacade;
 import controller.Controller;
 
 
 public class Main {
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 			CalculationFacade.makeInstance(Data.getInstance());
 			ModelFacade.makeInstance(Data.getInstance(), CalculationFacade.getInstance(), ExportFacade.getInstance());
 			Controller.makeInstance(ModelFacade.getInstance());
 			ViewFacade.makeInstance(Controller.getInstance());
 			Controller.getInstance().setView(ViewFacade.getInstance());
 			ViewFacade.getInstance().show();
 	}
 }
