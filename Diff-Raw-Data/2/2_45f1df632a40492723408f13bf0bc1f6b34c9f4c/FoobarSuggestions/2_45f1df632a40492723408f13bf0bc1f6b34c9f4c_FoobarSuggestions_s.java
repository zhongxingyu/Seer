 package foobar;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 
 /**
  * The FoobarSuggestions are displayed to the user from a Popup window, which is
  * triggered on an individual key press in FoobarField as dictated by
  * FoobarKeyListener. FoobarSuggestions are immutable JLists of Fooables.
  * 
  * @author Frank Goodman
  * 
  */
 public final class FoobarSuggestions extends JList<Fooable> {
 	private static final long serialVersionUID = 1L;
 
 	private final DefaultListModel<Fooable> model;
 
 	/**
 	 * The Fooables contained in this JList
 	 */
 	private final List<Fooable> suggestions;
 
 	/**
 	 * Create a JList containing a list of suggestions provided as 'results',
 	 * generated from the parent Foobar 'parent'.
 	 * 
 	 * @param parent
 	 *            The parent Foobar
 	 * @param results
 	 *            The list of results displayed
 	 */
 	protected FoobarSuggestions(final Foobar parent, List<Fooable> results) {
 		super();
 
 		// Store the list of suggestions
 		this.suggestions = results;
 
 		// Use the DefaultListModel
 		this.model = new DefaultListModel<>();
 		this.setModel(this.model);
 
 		// Allow a maximum of one option to be selected
 		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		// Execute any Fooable that is double-clicked
 		this.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2)
 					parent.executeFooable();
 			}
 		});
 
 		// Populate the result list with up to 5 Fooables
 		for (Fooable result : results.subList(0, results.size() > 5 ? 5
 				: results.size())) {
 			this.model.addElement(result);
 		}
 
 		// Select the first option
 		this.setSelectedIndex(0);
 
 		// Adjust the size of the result list to fit all the Fooables
 		this.setPreferredSize(new Dimension(parent.getParent().getWidth(),
				results.size() * 17));
 
 		this.setVisible(true);
 	}
 
 	/**
 	 * Get a list of the Fooables contained in this object.
 	 * 
 	 * @return The list of the Fooables contained in this object
 	 */
 	protected List<Fooable> getFooables() {
 		return this.suggestions;
 	}
 
 	/**
 	 * Select the previous indexed item in the list, If there are no more items
 	 * in the list, make a beep sound.
 	 */
 	protected void setSelectedIndexPrevious() {
 		if (this.getSelectedIndex() > 0) {
 			this.setSelectedIndex(this.getSelectedIndex() - 1);
 		} else {
 			Toolkit.getDefaultToolkit().beep();
 		}
 	}
 
 	/**
 	 * Select the next indexed item in the list. If there are no more items in
 	 * the list, make a beep sound.
 	 */
 	protected void setSelectedIndexNext() {
 		if (this.getSelectedIndex() < this.getLastVisibleIndex()) {
 			this.setSelectedIndex(this.getSelectedIndex() + 1);
 		} else {
 			Toolkit.getDefaultToolkit().beep();
 		}
 	}
 }
