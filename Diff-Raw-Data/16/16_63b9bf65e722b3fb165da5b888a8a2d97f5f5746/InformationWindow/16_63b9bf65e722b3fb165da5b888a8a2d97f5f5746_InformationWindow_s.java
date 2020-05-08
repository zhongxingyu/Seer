 package jisssea.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jisssea.util.CollectionsUtility;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import charva.awt.Color;
 import charva.awt.Toolkit;
 import charvax.swing.BorderFactory;
 import charvax.swing.Box;
 import charvax.swing.BoxLayout;
 import charvax.swing.JLabel;
 import charvax.swing.JPanel;
 import charvax.swing.JTextField;
 
 public class InformationWindow extends JPanel {
 
 	private static final Log log = LogFactory.getLog(InformationWindow.class);
 
 	/*
 	 * public static void main(String[] args) { //
 	 * System.setProperty("charva.color", ""); try { final InformationWindow win
 	 * = new InformationWindow(); SwingUtilities.invokeLater(new Runnable() {
 	 * 
 	 * @Override public void run() { try { Thread.sleep(2000); win.addWindow(3,
 	 * Urgency.NORMAL); win.removeWindow(1); Thread.sleep(2000); } catch
 	 * (InterruptedException e) {
 	 * 
 	 * } System.exit(0); } }); win.addWindow(1, Urgency.NORMAL);
 	 * win.addWindow(2, Urgency.BOLD); win.show(); } catch (Throwable e) {
 	 * log.debug("ffs - error", e); } }
 	 */
 
 	public enum Urgency {
 		NORMAL {
 			@Override
 			public Color getColor() {
 				return Color.green;
 			}
 		},
 		BOLD {
 			@Override
 			public Color getColor() {
 				return Color.red;
 			}
 		};
 		public abstract Color getColor();
 	}
 
 	private final List<SingleWindowInfo> windows;
 
 	private class SingleWindowInfo {
 		protected SingleWindowInfo(int n, Urgency urgency) {
 			super();
 			this.n = n;
 			label = new JLabel("" + n);
 			label.setForeground(urgency.getColor());
 			label.setBorder(BorderFactory.createLineBorder(Color.black, 1));
 		}
 
 		public final int n;
 		public final JLabel label;
 
 		@Override
 		public String toString() {
 			return "SingleWindowInfo [n=" + n + ", label=" + label + "]";
 		}
 	}
 
 	public InformationWindow() {
 
 		windows = new ArrayList<SingleWindowInfo>();
 
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		add(new JTextField(1));
 		add(Box.createHorizontalBox());
 
 		setLocation(0, 0);
 		setSize(Toolkit.getDefaultToolkit().getScreenColumns(), UIConstants.INFO_WINDOW_HEIGHT);
 		validate();
 	}
 
 	public void addWindow(int n, Urgency urgency) {
 		final SingleWindowInfo info = new SingleWindowInfo(n, urgency);
 		windows.add(info);
 		add(info.label);
 		forceRedraw();
 	}
 
 	public void removeWindow(final int n) {
 		final SingleWindowInfo found = CollectionsUtility.findFirst(windows, new CollectionsUtility.Predicate<SingleWindowInfo>() {
 			@Override
 			public boolean check(SingleWindowInfo info) {
 				return info.n == n;
 			}
 		});
 		log.debug(found);
 		windows.remove(found);
 		remove(found.label);
 		forceRedraw();
 	}
 
 	private void forceRedraw() {
		hide();
		show();
 	}
 }
