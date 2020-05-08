 package net.bubbaland.trivia.client;
 
 
 import java.awt.LayoutManager;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 
 /**
  * Super-class for most of the dialog boxes in the trivia GUI.
  * 
  * Implements an AncestorListener that will focus on the element when created. 
  * 
  */
 public class TriviaDialog extends JPanel implements AncestorListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long	serialVersionUID	= -4127179718225373888L;
 
 	public TriviaDialog() {
 		super();
 	}
 
 	public TriviaDialog(LayoutManager layout) {
 		super(layout);
 	}
 
 	public TriviaDialog(boolean isDoubleBuffered) {
 		super(isDoubleBuffered);
 	}
 
 	public TriviaDialog(LayoutManager layout, boolean isDoubleBuffered) {
 		super(layout, isDoubleBuffered);
 	}
 
 	@Override
	public void ancestorAdded(AncestorEvent event) {
 		// Change the focus to the text area when created
		final JComponent component = event.getComponent();
		component.requestFocusInWindow();
 	}
 
 	@Override
 	public void ancestorMoved(AncestorEvent event) {
 	}
 
 	@Override
 	public void ancestorRemoved(AncestorEvent event) {
 	}
 
 }
