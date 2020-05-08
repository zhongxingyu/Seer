 package net.marcuswhybrow.uni.g52gui.cw2.menu;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Stack;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 import net.marcuswhybrow.uni.g52gui.cw2.Browser;
 import net.marcuswhybrow.uni.g52gui.cw2.BrowserPage;
 import net.marcuswhybrow.uni.g52gui.cw2.ClosedItemsChangeListener;
 import net.marcuswhybrow.uni.g52gui.cw2.Page;
 import net.marcuswhybrow.uni.g52gui.cw2.bookmarks.Bookmark;
 import net.marcuswhybrow.uni.g52gui.cw2.bookmarks.BookmarkMenuItem;
 import net.marcuswhybrow.uni.g52gui.cw2.history.History;
 import net.marcuswhybrow.uni.g52gui.cw2.history.HistoryChangeListener;
 import net.marcuswhybrow.uni.g52gui.cw2.history.HistoryEntry;
 import net.marcuswhybrow.uni.g52gui.cw2.history.VisitCountEntry;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.Reopenable;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.Window;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.tabs.Tab;
 
 /**
  *
  * @author marcus
  */
 public class HistoryMenu extends Menu implements HistoryChangeListener, ClosedItemsChangeListener
 {
 	private ArrayList<VisitCountEntry> visitCount = null;
 
 	public HistoryMenu()
 	{
 		super("History");
 
 		History.get().addHistoryChangeListener(this);
 		Browser.get().addClosedItemsChangeListener(this);
		
		// Ensure that history has been loaded!
		History.get().notifyHistoryChangeListeners();

		this.rebuild();
 	}
 
 	private void rebuild()
 	{
 		this.removeAll();
 
 		this.addMenuItem("Home", KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		this.addMenuItem("Back", KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		this.addMenuItem("Forward", KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 
 		this.addSeparator();
 
 		JMenuItem title = new JMenuItem("Most Visited");
 		title.setEnabled(false);
 		this.add(title);
 
 		if (visitCount != null)
 		{
 			int number = 0;
 			for (VisitCountEntry entry : visitCount)
 			{
 				// Ignore browser pages (such as history and bookmarks manager)
 				if (entry.getPage() instanceof BrowserPage)
 					continue;
 				
 				String pageTitle = entry.getPage().getTitle();
 				if (pageTitle.length() > 30)
 					pageTitle = pageTitle.substring(0, 30) + " ...";
 				this.add(new PageMenuItem(entry.getPage()));
 
 				number ++;
 				if (number >= 9)
 					break;
 			}
 		}
 		
 		this.addSeparator();
 
 		title = new JMenuItem("Resently Closed");
 		title.setEnabled(false);
 		this.add(title);
 
 		for (Reopenable item : Browser.get().getClosedItems())
 		{
 			if (item instanceof Tab)
 				this.add(new ClosedMenuItem(item, ((Tab) item).getCurrentLocation()));
 			else if (item instanceof Window)
 			{
 				Window window = (Window) item;
 				if (window.getTabs().getActiveTab() != null)
 					this.add(new ClosedMenuItem(item, window.getTabs().getActiveTab().getCurrentLocation()));
 			}
 		}
 		
 		this.addSeparator();
 
 		this.addMenuItem("Show Full History", KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 	}
 
 	public void historyHasChanged(ArrayList<HistoryEntry> history)
 	{
 		// do nothing (not interested in this event).
 	}
 
 	public void visitCountHasChanged(ArrayList<VisitCountEntry> visitCount)
 	{
 		this.visitCount = visitCount;
 		this.rebuild();
 	}
 
 	public void closedItemsHaveChanged(Stack<Reopenable> items)
 	{
 		this.rebuild();
 	}
 
 	private class ClosedMenuItem extends BookmarkMenuItem
 	{
 		private Reopenable item;
 
 		public ClosedMenuItem(Reopenable item , Page page)
 		{
 			super(new Bookmark(page));
 			this.item = item;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae)
 		{
 			Browser.get().openClosedItem(item);
 		}
 	}
 }
