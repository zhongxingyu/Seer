 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import presentation.model.LibraryTabbedPaneModel;
 import presentation.model.ModelController;
 import presentation.model.SearchResultListModel;
 import domain.Book;
 import domain.Customer;
 import domain.Library;
 import domain.Loan;
 
 /**
  * Displays search results for books and users combined with specific actions
  * for each item.
  */
 public class SearchResultList extends JList  implements Observer {
 	private static final long serialVersionUID = 1L;
 	private JList resultList;
 	private final LibraryTabbedPaneModel tabModel;
 	private final Library library;
 	private SearchResultCellRenderer cellRenderer;
 	private ModelController controller;
 	private SearchResultListModel model;
 
 	public SearchResultList(ModelController controller) {
 		this.controller = controller;
 		tabModel = controller.tabbed_model;
 		library = controller.library;
 		model = controller.resultlist_model;
 		model.addObserver(this);
 		setLayout(new BorderLayout());
 		initResultList();
 	}
 
 	private void initResultList() {
 		resultList = new JList();
 		
 		resultList.setModel(controller.resultlist_model);
 		cellRenderer = new SearchResultCellRenderer(controller);
 	
 		resultList.setCellRenderer(cellRenderer);
 		resultList.setDoubleBuffered(false);
 		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		resultList.addMouseMotionListener(new MouseAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				int index = eventToListIndex(e);
 				if (index < 0)
 					return;
 				resultList.setSelectedIndex(resultList
 						.locationToIndex(new Point(e.getX(), e.getY())));
 			}
 		});
 
 		resultList.addMouseListener(new ListItemMouseListener());
 
 		add(new JScrollPane(resultList));
 	}
 
 	private int eventToListIndex(MouseEvent e) {
 		int index = resultList.locationToIndex(new Point(e.getX(), e.getY()));
 		return index;
 	}
 
 	public void lendBook(Book selected) {
 		if (controller.activeuser_model.getCustomer() == null) {
 			// TODO: Message to first set user
 			tabModel.setActiveBook(selected);
 			return;
 		}
 		library.createAndAddLoan(controller.activeuser_model.getCustomer(), selected);
 	}
 
 	private void reserveBook(Book selected) {
 		// TODO: Implement reservation of a book.
 	}
 
 	private void returnBook(Book selected) {
 		for (Loan l : library.getLoansPerTitle(selected.getTitle())) {
 			if (l.getBook().getInventoryNumber() != (selected
 					.getInventoryNumber()))
 				continue;
 			if (!l.returnBook())
 				return;
 			return;
 		}
 	}
 
 	/**
 	 * Dynamically adapt title length to size of list by telling the cell
 	 * renderer its size.
 	 */
 	public void paint(Graphics g) {
 		cellRenderer.setPreferredWidth(this.getWidth());
 		super.paint(g);
 	}
 
 	public void update(Observable o, Object arg) {
 		System.out.println("Searchresultlist has an update");
 		resultList.invalidate();
 		resultList.repaint();
 	}
 		
 	/**
 	 * Hardcoded for laziness reasons. List returns effective height, not
 	 * maximum height.
 	 * 
 	 * @return 64, the actual height of a list item.
 	 */
 	private int getCellHeight() {
 		return 64;
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////////////////
 	private final class ListItemMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			int index = eventToListIndex(e);
 			if (index < 0)
 				return;
 
 			Object o = resultList.getModel().getElementAt(index);
 			if (o instanceof Book) {
 				Book selected = (Book) o;
 				tabModel.setActiveBook(selected);
 				handleBookClick(e, index, selected);
 				return;
 			}
 
 			if (o instanceof Customer) {
 				Customer selected = (Customer) o;
 				// TODO: Coole Idee: Direkt-setActive-button für Benutzer
 				// auswählen
 				controller.activeuser_model.setNewActiveUser(selected);
 				tabModel.setActiveTab(controller.tabbed_model.USER_TAB);
 			}
 		}
 
 		/**
 		 * Determines where an item has been clicked and executes the
 		 * appropriate action. JList items cannot listen to events so the list
 		 * itself listens for events for its items.
 		 * 
 		 * @param e
 		 *            gives information about the mouse event including its
 		 *            coordinates to calculate the clicked item.
 		 * @param index
 		 *            of the list item.
 		 * @param selected
 		 *            the book or user which is affected by this click.
 		 */
 		private void handleBookClick(MouseEvent e, int index, Book selected) {
 			tabModel.setActiveBook(selected);
 			if (isFirstIconHit(e, index)) {
 				returnBook(selected);
				repaint();
 				return;
 			}
 			if (isSecondIconHit(e, index) && library.isBookLent(selected)) {
 				reserveBook(selected);
				repaint();
 				return;
 			}
 			if (isSecondIconHit(e, index)) {
 				lendBook(selected);
				repaint();
 				return;
 			}
 			showDetailsOf(selected);
 		}
 
 		private void showDetailsOf(Book selected) {
 			tabModel.setActiveBook(selected);
 			tabModel.setActiveTab(controller.tabbed_model.BOOK_TAB);
 		}
 
 		private boolean isFirstIconHit(MouseEvent e, int index) {
 			return e.getX() > getWidth() - 120 && e.getX() < getWidth() - 88
 					&& e.getY() > 30 + getCellHeight() * index
 					&& e.getY() < 62 + getCellHeight() * index;
 		}
 
 		private boolean isSecondIconHit(MouseEvent e, int index) {
 			return e.getX() > getWidth() - 120 + 40
 					&& e.getX() < getWidth() - 88 + 40
 					&& e.getY() > 30 + getCellHeight() * index
 					&& e.getY() < 62 + getCellHeight() * index;
 		}
 	}
 }
