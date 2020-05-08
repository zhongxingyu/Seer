 package org.berkelium.java.examples.browser;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import org.berkelium.java.api.Rect;
 import org.berkelium.java.api.Window;
 import org.berkelium.java.api.WindowAdapter;
 import org.berkelium.java.api.WindowDelegate;
 
 public class SimpleBrowser extends JFrame {
 	private static final long serialVersionUID = 8835790859223385092L;
 	private final Toolbar toolbar = new Toolbar(this);
 	private final TabAdapter adapter = new TabAdapter();
 	private final WindowDelegate delegate = new WindowAdapter() {
 		public void onTitleChanged(Window win, String title) {
 			setTitle(title);
 		}
 
 		public void onPaintDone(Window win, final Rect rect) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
					repaint(rect.left(), rect.top(), rect.right(),
 							rect.bottom());
 				}
 			});
 		}
 	};
 
 	public SimpleBrowser() {
 		setTitle("Berkelium-Java Simple Browser");
 		setSize(new Dimension(640, 480));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		add(toolbar, BorderLayout.PAGE_START);
 		add(adapter, BorderLayout.CENTER);
 		setBackground(Color.green);
 		setVisible(true);
 	}
 
 	public void setTab(Tab tab) {
 		Tab old = adapter.getTab();
 		if (old != null) {
 			old.removeDelegate(delegate);
 		}
 		toolbar.setTab(tab);
 		adapter.setTab(tab);
 		tab.addDelegate(delegate);
 	}
 }
