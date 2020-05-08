 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 
 import presentation.model.ModelController;
 import presentation.model.TabSearchModel;
 
 /**
  * Represents the whole contents of the "search"-Tab and handles Updates on its
  * state. This is the default tab which is shown after startup or a reset. This
  * view is visually and internally divided in two parts:
  * 
  * <ul>
  * <li>Content pane: consists of the search field and its results
  * <li>Action pane: displays buttons for special search queries
  * </ul>
  * 
  * There is a dependency on the main window model because a click on a result
  * must show the details of a certain book.
  */
 public class TabSearchPanel extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 	private static final String SEARCH_FIELD_TITLE = "Suchmaske";
 	private JPanel contentPanel;
 	private TabSearchModel model;
 	private JTextField searchField;
 	private JPanel searchTab;
 	private JPanel resultPane;
 	private ActionSearchPanel action_search_panel;
 	private ModelController controller;
 
 	/**
 	 * Create the search tab.
 	 * 
 	 * @param mainmodel
 	 */
 	public TabSearchPanel(ModelController controller){
 		setLayout(new BorderLayout());
 		this.controller = controller;
 		model = controller.searchtab_model;
 		model.addObserver(this);
 		initContentPane();
 		action_search_panel = new ActionSearchPanel(controller);
 		add(action_search_panel, BorderLayout.EAST);
 	}
 
 	private void initContentPane() {
 		contentPanel = new JPanel();
 		contentPanel.setLayout(new BorderLayout());
 
 		initSearchBox();
 		initResultList();
 
 		add(contentPanel);
 	}
 
 	private void initSearchBox() {
 		searchTab = new JPanel();
 		searchTab.setLayout(new BorderLayout());
 		searchTab.setBorder(new TitledBorder(SEARCH_FIELD_TITLE));
 		searchField = new JTextField(model.getSearchText());
 		searchField.setFont(new Font(null, Font.PLAIN, 18));
 		searchField.setForeground(Color.GRAY);
 		searchField.setBorder(new EmptyBorder(10, 10, 10, 10));
 		searchField.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
				model.ForwardKeyEvent(e);
 			}
 		});
 		
 		setSearchFieldDefaultTextListeners();
 		searchTab.add(searchField);
 		contentPanel.add(searchTab, BorderLayout.NORTH);
 	}
 
 	/**
 	 * Set up the default text displaying behaviour of the search field.
 	 */
 	private void setSearchFieldDefaultTextListeners() {
 		searchField.addCaretListener(new CaretListener() {
 			public void caretUpdate(CaretEvent e) {
 				if (model.isDefaultSearchText()
 						&& searchField.getCaretPosition() != 0)
 					searchField.setCaretPosition(0);
 			}
 		});
 		searchField.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				if (searchField.getText().length() == 0)
 					model.resetSearchText();
 			}
 
 			public void focusGained(FocusEvent e) {
 				model.setSearchText(searchField.getText(), false);
 			}
 		});
 		searchField.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				model.setSearchText(searchField.getText(), false);
 				if (model.isDefaultSearchText())
 					model.setSearchText("", true);
 			}
 		});
 	}
 
 	private void initResultList() {
 		resultPane = new JPanel();
 		resultPane.setLayout(new BorderLayout());
 		resultPane.setBorder(new TitledBorder("Suchergebnisse"));
 		resultPane.add(new SearchResultList(controller));
 		contentPanel.add(resultPane, BorderLayout.CENTER);
 	}
 
 	/**
 	 * For changing the search field text a reference to the model of this view
 	 * is needed.
 	 * 
 	 * @return the model of this tab panel.
 	 */
 	public TabSearchModel getModel() {
 		return model;
 	}
 
 	/**
 	 * If focus requested, set it to the search field.
 	 */
 	public void requestFocus() {
 		super.requestFocus();
 		searchField.requestFocus();
 		searchField.setCaretPosition(0);
 	}
 
 	/**
 	 * TODO: "You ask why? Well, let’s see what happens when I use the standard
 	 * Java implementations of Observer and Observable: I end up checking the
 	 * type of the Observable each time the update() method is called on the
 	 * observer — using instanceof. Unnecessary to say that this is considered a
 	 * code smell by some people."
 	 * 
 	 * @see http 
 	 *      ://gnoack.wordpress.com/2008/02/26/observer-pattern-revisited-using-
 	 *      java-5-generics/
 	 */
 	public void update(Observable o, Object arg) {
 		if (arg instanceof String) {
 			if (arg.equals("requestFocus"))
 				requestFocus();
 			else {
 				searchField.setForeground(model.getSearchFieldColor());
 				searchField.setText((String) arg);
 			}
 		}
 	}
 }
