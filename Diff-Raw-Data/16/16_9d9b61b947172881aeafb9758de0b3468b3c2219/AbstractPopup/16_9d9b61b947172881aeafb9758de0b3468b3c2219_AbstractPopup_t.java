 /*
  * Created on 20-may-2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package zz.utils.ui.popup;
 
 import java.awt.*;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JWindow;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 
 import zz.utils.ui.TransparentPanel;
 import zz.utils.ui.UIUtils;
 
 
 /**
  * Base class for displaying popups.
  * Delegates to subclasses the determination of where to display the popup.
  * @see #show
  * @see #hide
  */
 public abstract class AbstractPopup 
 {
 	private JComponent itsContent;
 
 	private boolean itsShown = false;
 
 	private List itsPopupListeners = new ArrayList ();
 
	private JPopupMenu itsPopupMenu;

 	/**
 	 * Creates a popup object.
 	 *
 	 * @param aContent The content of the popup window.
 	 */
 	public AbstractPopup (JComponent aContent)
 	{
 		setContent (aContent);
 		PopupManager.getInstance().registerPopup(this);
 	}
 
 	/**
 	 * Returns the component that owns this popup. When the owner component is 
 	 * removed, the popup should be hidden.
 	 */
 	public abstract JComponent getOwner ();
 	
 	public void addPopupListener (PopupListener aListener)
 	{
 		itsPopupListeners.add (aListener);
 	}
 
 	public void removePopupListener (PopupListener aListener)
 	{
 		itsPopupListeners.remove (aListener);
 	}
 
 	void firePopupShown ()
 	{
 		if (itsContent instanceof PopupListener)
 		{
 			PopupListener thePopupListener = (PopupListener) itsContent;
 			thePopupListener.popupShown ();
 		}
 
 		for (Iterator theIterator = itsPopupListeners.iterator (); theIterator.hasNext ();)
 		{
 			PopupListener theListener = (PopupListener) theIterator.next ();
 			theListener.popupShown();
 		}
 	}
 
 	void firePopupHidden ()
 	{
 		if (itsContent instanceof PopupListener)
 		{
 			PopupListener thePopupListener = (PopupListener) itsContent;
 			thePopupListener.popupHidden ();
 		}
 
 		for (Iterator theIterator = itsPopupListeners.iterator (); theIterator.hasNext ();)
 		{
 			PopupListener theListener = (PopupListener) theIterator.next ();
 			theListener.popupHidden();
 		}
 	}
 
 	public void setContent (JComponent aContent)
 	{
 		if (itsContent != aContent)
 		{
 			itsContent = aContent;
 			if (isPopupShown())
 			{
 				hide();
 				show();
 			}
 		}
 	}
 
 	/**
 	 * Returns the content displayed by the popup.
 	 */
 	public JComponent getContent ()
 	{
 		return itsContent;
 	}
 
 	public boolean isPopupShown ()
 	{
 		return itsShown;
 	}
 
 	public void togglePopup ()
 	{
 		if (itsShown) hide ();
 		else show ();
 	}
 
 	/**
 	 * Returns the bounds where to display the popup.
 	 */
 	protected abstract Rectangle getPopupBounds ();
 	
 	/**
 	 * Returns the frame that contains the popup.
 	 * Might return null (eg. with SWT_AWT). In this case, {@link #getRootPane()} should
 	 * be overridden.
 	 */
 	protected Frame getOwnerFrame ()
 	{
 		return UIUtils.getFrame(getRootPane());
 	}
 	
 	/**
 	 * Returns the root pane of the popup's main component
 	 */
 	protected abstract JRootPane getRootPane();
 
 	/**
 	 * Displays the popup window on the screen, next to the trigger component.
 	 * Tries to respect the preferred direction.
 	 */
 	public void show ()
 	{
 		show (true);
 	}
 
 	/**
 	 * Displays the popup window on the screen, next to the trigger component.
 	 * Tries to respect the preferred direction.
 	 */
 	public void show (boolean aNotify)
 	{
 		if (itsShown) return;
 		if (getContent () == null) return;
 
		itsPopupMenu = new JPopupMenu()
 		{
 			@Override
 			public void menuSelectionChanged(boolean aIsIncluded)
 			{
 				// Avoid cancelling the menu
 				super.menuSelectionChanged(aIsIncluded);
 			}
 		};
		itsPopupMenu.add(getContent());
 		
		itsPopupMenu.addPopupMenuListener(new PopupMenuListener()
 		{
 			public void popupMenuCanceled(PopupMenuEvent aE)
 			{
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent aE)
 			{
 				itsShown = false;
 				firePopupHidden();
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent aE)
 			{
 			}
 			
 		});
 		
 		Rectangle thePopupBounds = getPopupBounds();
 		Point theLocationOnScreen = getOwner().getLocationOnScreen();
		itsPopupMenu.show(getOwner(), thePopupBounds.x-theLocationOnScreen.x, thePopupBounds.y-theLocationOnScreen.y);
 
 		itsShown = true;
 
 		if (aNotify) firePopupShown();
 	}
 
 	/**
 	 * Hides the popup.
 	 */
 	public void hide ()
 	{
 		hide (true);
 	}
 
 	/**
 	 * Hides the popup.
 	 */
 
 	public void hide (boolean aNotify)
 	{
 		if (! itsShown) return;
 //		if (itsAutoHide)
 //		{
 //			getRootPane().getLayeredPane().remove (itsScreen);
 //		}
 //		itsPopupWindow.setVisible(false);
		itsPopupMenu.setVisible(false);
 		itsShown = false;
 
 		if (aNotify) firePopupHidden();
 	}
 
 }
