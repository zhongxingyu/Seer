 package pf.gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 
 /**
  * Dialog which can contain more cards, which are switched by next, previous
  * buttons.
  * 
  * @author Adam Juraszek
  * 
  */
 public abstract class CardDialog extends JDialog {
 	/**
 	 * Cancel button listener
 	 * 
 	 * @author Adam Juraszek
 	 * 
 	 */
 	public class CancelAL implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			closedProperly = false;
 			dispose();
 			cancelled();
 		}
 	}
 
 	/**
 	 * Finish button listener
 	 * 
 	 * @author Adam Juraszek
 	 * 
 	 */
 	public class FinishAL implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			closedProperly = true;
 			dispose();
 			finished();
 		}
 	}
 
 	/**
 	 * Next button listener
 	 * 
 	 * @author Adam Juraszek
 	 * 
 	 */
 	public class NextAL implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (!isLast()) {
 				cl.next(content);
 				current++;
 				updateButtons();
 				flipNext();
 			}
 		}
 	}
 
 	/**
 	 * Previous button listener
 	 * 
 	 * @author Adam Juraszek
 	 * 
 	 */
 	public class PrevAL implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (!isFirst()) {
 				cl.next(content);
 				current--;
 				updateButtons();
 				flipPrev();
 			}
 		}
 	}
 
 	private static final long serialVersionUID = 1L;
 	private JPanel content;
 
 	private CardLayout cl;
 
 	private List<JPanel> cards;
 	private int current;
 
 	private boolean closedProperly = false;
 
 	private JButton prev;
 	private JButton next;
 	private JButton cancel;
 	private JButton finish;
 
 	public CardDialog(JDialog owner, String title) {
 		super(owner, title, true);
 		initDialog();
 	}
 
 	public CardDialog(JFrame owner, String title) {
 		super(owner, title, true);
 		initDialog();
 	}
 
 	/**
 	 * called if cancelled button has been pressed
 	 */
 	public abstract void cancelled();
 
 	/**
 	 * called if finish button has been pressed
 	 */
 	public abstract void finished();
 
 	/**
 	 * @return true of pressed finish, false otherwise
 	 */
 	public boolean isClosedProperly() {
 		return closedProperly;
 	}
 
 	/**
 	 * init method which is common for both constructors
 	 */
 	private void initDialog() {
 		Container pane = getContentPane();
 		pane.setLayout(new BorderLayout());
 
 		content = new JPanel(cl = new CardLayout());
 		cards = new ArrayList<JPanel>();
 		makeContent();
 		if (cards.size() == 0) {
 			throw new IllegalStateException("No cards added");
 		}
 		pane.add(content, BorderLayout.CENTER);
 
 		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 
 		prev = new JButton("< Prev");
 		buttons.add(prev);
 		prev.addActionListener(new PrevAL());
 		next = new JButton("Next >");
 		buttons.add(next);
 		next.addActionListener(new NextAL());
 		cancel = new JButton("Cancel");
 		buttons.add(cancel);
 		cancel.addActionListener(new CancelAL());
 		finish = new JButton("Finish");
 		buttons.add(finish);
 		finish.addActionListener(new FinishAL());
 
 		pane.add(buttons, BorderLayout.SOUTH);
 		if (cards.size() == 1) {
 			prev.setVisible(false);
 			next.setVisible(false);
 		}
 		updateButtons();
 
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		pack();
 	}
 
 	/**
 	 * @return true if this card is the first one, false otherwise
 	 */
 	private boolean isFirst() {
 		return current == 0;
 	}
 
 	/**
 	 * @return true if this card is the last one, false otherwise
 	 */
 	private boolean isLast() {
 		return current == cards.size() - 1;
 	}
 
 	/**
 	 * adds a panel as a new card
 	 * 
 	 * @param panel
 	 */
 	protected void addCard(JPanel panel) {
 		cards.add(panel);
		content.add(panel, panel.toString());
 	}
 
 	/**
 	 * @return true if user can press finish button, false otherwise
 	 */
 	protected abstract boolean canFinish();
 
 	/**
 	 * @return true if user can flip to the next card, false otherwise
 	 */
 	protected abstract boolean canNext();
 
 	/**
 	 * @return true if user can flip to the previous card, false otherwise
 	 */
 	protected abstract boolean canPrev();
 
 	/**
 	 * called after card has been flipped to the next card
 	 */
 	protected abstract void flipNext();
 
 	/**
 	 * called after card has been flipped to the previous card
 	 */
 	protected abstract void flipPrev();
 
 	/**
 	 * @return index of current card
 	 */
 	protected int getCurrent() {
 		return current;
 	}
 
 	/**
 	 * @return current card
 	 */
 	protected JPanel getCurrentCard() {
 		return cards.get(current);
 	}
 
 	/**
 	 * called from {@link #initDialog()} method to make a content. At least one
 	 * card must be added.
 	 */
 	protected abstract void makeContent();
 
 	/**
 	 * updates control buttons at the bottom edge
 	 */
 	protected void updateButtons() {
 		finish.setEnabled(isLast() && canFinish());
 		next.setEnabled(!isLast() && canNext());
 		prev.setEnabled(!isFirst() && canPrev());
 		cancel.setEnabled(true);
 	}
 
 }
