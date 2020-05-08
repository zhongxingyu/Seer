 /* IndividualListController */
 
 import org.apache.log4j.Logger;
 
 import com.apple.cocoa.foundation.*;
 import com.apple.cocoa.application.*;
 import com.redbugz.macpaf.Individual;
 
 public class IndividualListController extends NSWindowController {
 	private static Logger log = Logger.getLogger(IndividualListController.class);
	private static final NSSelector selectIndividualSelector = new NSSelector("selectIndividual", new Class[] {Object.class});
 
     public NSTextField individualCountText; /* IBOutlet */
     public IndividualList individualList; /* IBOutlet */
     public NSTableView individualListTableView; /* IBOutlet */
     public NSTextField searchField; /* IBOutlet */
     public IndividualDetailController individualDetailController; /* IBOutlet */
 
 	private SortableFilteredTableViewDataSource sortableFilteredTableViewDataSource;
 	
 	public IndividualListController() {
 		// for use by IB
 	}	
 
 	public IndividualListController(IndividualList dataSource) {
 		super("IndividualListWindow");
 		individualList = dataSource;
 	}
 	
 	public void search(Object sender) { /* IBAction */
 		log.debug("search for "+searchField.stringValue());
 		sortableFilteredTableViewDataSource.setFilterString(searchField.stringValue());
 		refreshData();
 	}
 
 	public void selectIndividual(Object sender) { /* IBAction */
 		log.debug("selectIndividual: "+individualListTableView.selectedRow());
 		if (individualListTableView.selectedRow() >= 0) {
 			Individual selectedIndividual = individualList.getSelectedIndividual();
 			log.debug("selectIndividual Document:"+document());
 			((MyDocument) document()).setPrimaryIndividual(selectedIndividual);
 			((MyDocument) document()).mainWindow.makeKeyAndOrderFront(this);
 			((MyDocument) document()).displayFamilyView(this);
 		}
 	}
 
 	public void windowDidLoad() {
 		super.windowDidLoad();
 		setup();
 	}
 
 	public void setup() {
 		log.debug("IndividualListController.setup()");
 		sortableFilteredTableViewDataSource = new SortableFilteredTableViewDataSource(individualListTableView, individualList);
 		individualListTableView.setDataSource(sortableFilteredTableViewDataSource);
 		individualListTableView.setDelegate(this);
		individualListTableView.setDoubleAction(selectIndividualSelector);
 		individualList.setDataSource(sortableFilteredTableViewDataSource);
 		searchField.setDelegate(this);
 		refreshData();
 		if (individualListTableView.numberOfRows() > 0) {
 			tableViewSelectionDidChange(new NSNotification(NSTableView.TableViewSelectionDidChangeNotification, individualListTableView));
 		}
 //		refreshSelection(individualList.getSelectedIndividual());
 	}
 
 	public String windowTitleForDocumentDisplayName(String displayName) {
 		return displayName + ": "+"Individual List";
 	}
 	
 	  public void tableViewSelectionDidChange(NSNotification aNotification) {
 			log.debug("IndividualListController tableViewSelectionDidChange():" + aNotification);
 			individualList.tableViewSelectionDidChange(aNotification);
 			individualDetailController.setIndividual(individualList.getSelectedIndividual());
 		  }
 
 	  public void controlTextDidChange(NSNotification aNotification) {
 		log.debug("IndividualListController.controlTextDidChange()");
 		// TODO For now, let's not do live updating of filters - just too slow. I'll add this in later once optimized -- LTA		
 //		System.out.println(aNotification);
 //		System.out.println(aNotification.valueForKey("object"));
 //		System.out.println(aNotification.object());
 //		NSTextField field = (NSTextField) aNotification.object();
 //		// hold on to selection so we can find it in the filtered list
 //		Individual selectedIndividual = individualList.getSelectedIndividual();
 //		System.out.println("search value:"+field.stringValue());
 //		sortableFilteredTableViewDataSource.setFilterString(searchField.stringValue());
 //		refreshData();
 //		refreshSelection(selectedIndividual);
 	  }
 
 	private void refreshSelection(Individual selectedIndividual) {
 		// TODO implement this to keep selection if still in filtered list
 		// for now, just change selection to update selected individual view
 //		int currentSelectedIndex = sortableFilteredTableViewDataSource.getCurrentSelectedIndex();
 		if (individualListTableView.numberOfRows() > 0) {
 			tableViewSelectionDidChange(new NSNotification(NSTableView.TableViewSelectionDidChangeNotification, individualListTableView));
 		}
 	}
 
 	public void refreshData() {
 		individualListTableView.reloadData();
 		individualCountText.setStringValue("Displaying "+individualListTableView.numberOfRows()+" out of "+individualList.size()+" Individuals");
 		refreshSelection(individualList.getSelectedIndividual());
 	}
 
 	  // SDMovingRowsProtocol
 //	- (unsigned int)dragReorderingMask:(int)forColumn;
 	public int dragReorderingMaskForColumn(int column) {
 		return 0; // drag always2
 	}
 	  //- (BOOL)tableView:(NSTableView *)tv didDepositRow:(int)rowToMove at:(int)newPosition;
 	  public boolean tableViewDidDepositRowToMoveAt(NSTableView nSTableView, int rowToMove, int newPosition) {
 		  log.debug("IndividualList.tableViewDidDepositRowToMoveAt()");
 		  return true;
 	  }
 
 //	This gives you a chance to decline to drop particular rows on other particular
 //	row. Return YES if you don't care
 //	- (BOOL) tableView:(SDTableView *)tableView draggingRow:(int)draggedRow overRow:(int) targetRow;
 	  public boolean tableViewDraggingRowOverRow(NSTableView tableView, int draggedRow, int targetRow) {
 		  log.debug("IndividualList.tableViewDraggingRowOverRow()");
 		  return true;
 	  }
 }
