 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import presentation.model.MainWindowModel;
 import presentation.model.SearchResultListModel;
 import domain.Book;
 import domain.Library;
 
 /**
  * Displays search results for books and users combined with specific actions
  * for each item.
  */
 public class ResultList extends JList {
 
 	private static final long serialVersionUID = 1L;
 	private JList resultList;
 	private final LibTabPaneModel tabModel;
 	private final Library library;
 	private ResultCellRenderer cellRenderer;
 
 	public ResultList(LibTabPaneModel tabModel, Library library) {
 		this.tabModel = tabModel;
 		this.library = library;
 		setLayout(new BorderLayout());
 		initResultList();
 	}
 
 	private void initResultList() {
 		resultList = new JList();
 		resultList.setModel(new SearchResultListModel(library));
 		cellRenderer = new ResultCellRenderer(library);
 		resultList.setCellRenderer(cellRenderer);
 		resultList.setDoubleBuffered(false);
 		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		resultList.addMouseMotionListener(new MouseAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				resultList.setSelectedIndex(resultList
 						.locationToIndex(new Point(e.getX(), e.getY())));
 			}
 		});
 
 		resultList.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
				int clickedIndex = resultList.locationToIndex(new Point(e
						.getX(), e.getY()));
				if (clickedIndex < 0)
					return;
				Book selectedBook = (Book) resultList.getModel().getElementAt(
						clickedIndex);
 				tabModel.setActiveBook(selectedBook);
 				tabModel.setActiveTab(MainWindowModel.BOOK_TAB);
 			}
 		});
 
		add(new JScrollPane(resultList));
 	}
 
 	/**
 	 * Dynamically adapt title length to size of list by telling the cell
 	 * renderer its size.
 	 */
 	public void paint(Graphics g) {
		// TODO: Wert anders ermittlen, damit horizontal scrollbar? [Martin]
 		cellRenderer.setPreferredWidth(this.getWidth());
 		super.paint(g);
 	}
 }
