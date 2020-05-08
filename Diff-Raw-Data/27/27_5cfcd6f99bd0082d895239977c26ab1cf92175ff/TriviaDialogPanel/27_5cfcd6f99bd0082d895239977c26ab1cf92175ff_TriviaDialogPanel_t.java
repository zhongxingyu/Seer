 package net.bubbaland.trivia.client;
 
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 
 /**
  * Super-class for most of the dialog box panels in the trivia GUI.
  * 
  * Creates a new panel using the GridBagLayout manager. Also implements an AncestorListener to allow focus on an element after the dialog is created.
  * 
  */
 public class TriviaDialogPanel extends JPanel implements AncestorListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long	serialVersionUID	= -4127179718225373888L;
 	
 	protected TriviaDialog dialog;
 
 	/**
 	 * 
 	 */
 	public TriviaDialogPanel() {
 		super( new GridBagLayout() );
 	}
 	
 	/**
 	 * Override the default behavior of the text area to click the OK button of the option pane on enter and insert a line break on shift-enter
 	 * 
	 * @param component The text are whose behavior we want to change
 	 */
	public void addEnterOverride(JComponent component) {
		component.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "Text Submit");
		component.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
		component.getActionMap().put("Text Submit", new AbstractAction() {
 			private static final long	serialVersionUID	= 1L;
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				submitText();
 			}
 		} );
 	}
 
 	/**
 	 * Change focus to the listened-to component when its ancestor is shown.
 	 */
 	@Override
 	public void ancestorAdded(final AncestorEvent event) {
 		// Change the focus to the text area when created
 		final AncestorListener al = this;
 		try {
 			Thread.sleep(100);
 		} catch (final InterruptedException e) {
 		}
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				final JComponent component = event.getComponent();
 				component.requestFocusInWindow();
 				component.removeAncestorListener(al);
 			}
 		});
 
 	}
 
 	@Override
 	public void ancestorMoved(AncestorEvent event) {	}
 
 	@Override
 	public void ancestorRemoved(AncestorEvent event) {	}
 	
 	/**
 	 * Tell the dialog to click the OK button on the option pane.
 	 */
 	public void submitText() {
 		this.dialog.clickOK();
 	}
 
 }
