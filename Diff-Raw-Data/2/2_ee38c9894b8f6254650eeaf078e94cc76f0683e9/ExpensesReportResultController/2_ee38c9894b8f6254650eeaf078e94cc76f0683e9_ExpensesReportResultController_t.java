 package es.rchavarria.raccount.frontend.expensesReport.gui;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import es.rchavarria.raccount.frontend.expensesReport.models.ExpensesReportResultTableModel;
 import es.rchavarria.raccount.frontend.gui.view.GuiView;
 import es.rchavarria.raccount.model.ExpensesByConcept;
 
 public class ExpensesReportResultController {
 
     private final static Logger log = LoggerFactory.getLogger(ExpensesReportResultController.class);
 
 	private ExpensesReportResultView view;
 
 	public ExpensesReportResultController() {
 		view = new ExpensesReportResultView();
 	}
 
 	public GuiView getView() {
 		return view;
 	}
 
 	public void load(List<ExpensesByConcept> expenses) {
	    log.info("loading {} expenses", expenses.size());
 	    
 		view.setTableModel(new ExpensesReportResultTableModel(expenses));
 	}
 }
