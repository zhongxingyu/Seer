 package usr.erichschroeter.progressive;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Frame;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ResourceBundle;
 
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 /**
  * Utility class which providing methods for creating progress monitors. The
  * progress monitors created via this utility use a custom
  * {@link ProgressListener} which will display the dialog automatically (after
  * the milliseconds to wait) once the monitor has started.
  * 
  * @author Santhosh Kumar T, Erich Schroeter
  */
 public class ProgressUtil {
 
 	protected static final ResourceBundle i18ln = ResourceBundle
 			.getBundle("usr.erichschroeter.progressive.i18ln.ProgressDialog");
 
 	/** Custom progress listener to handle displaying a dialog. */
 	static class MonitorListener implements ProgressListener, ActionListener {
 
 		AbstractProgressMonitor monitor;
 		Window owner;
 		String title;
 		Timer timer;
 		CancelHandler cancelDelegate;
 		ProgressDialog dlg;
 
 		public MonitorListener(Window owner, String title,
 				AbstractProgressMonitor monitor, CancelHandler handler) {
 			this.owner = owner;
 			this.title = title;
 			this.monitor = monitor;
 			this.cancelDelegate = handler;
 		}
 
 		@Override
 		public void statusChanged(StatusEvent e) {
 			AbstractProgressMonitor monitor = (AbstractProgressMonitor) e
 					.getSource();
 			if (monitor instanceof DeterminateProgressMonitor) {
 				DeterminateProgressMonitor dMonitor = (DeterminateProgressMonitor) monitor;
 				if (dMonitor.getCurrent() != dMonitor.getMax()) {
 					if (timer == null) {
 						timer = new Timer(monitor.getMilliSecondsToWait(), this);
 						timer.setRepeats(false);
 						timer.start();
 					}
 				} else {
 					if (timer != null && timer.isRunning())
 						timer.stop();
 					monitor.removeProgressListener(this);
 				}
 			} else {
 				if (timer == null) {
 					timer = new Timer(monitor.getMilliSecondsToWait(), this);
 					timer.setRepeats(false);
 					timer.start();
 				} else {
 					if (timer != null && timer.isRunning())
 						timer.stop();
 					monitor.removeProgressListener(this);
 				}
 			}
 		}
 
 		@Override
 		public void canceled(StatusEvent e) {
 			String cancelText = e.getText();
 			if (cancelText == null) {
 				cancelText = i18ln.getString("defaultCanceledText");
 			}
 			dlg.updateStatus(cancelText);
 			cancelDelegate.canceled();
 
 			dlg.setVisible(false);
 			dlg.dispose();
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			monitor.removeProgressListener(this);
 			dlg = owner instanceof Frame ? new ProgressDialog((Frame) owner,
 					title, monitor, cancelDelegate) : new ProgressDialog(
 					(Dialog) owner, title, monitor, cancelDelegate);
 			monitor.addProgressListener(dlg);
 			dlg.pack();
 			dlg.setLocationRelativeTo(null);
 			dlg.setVisible(true);
 		}
 
 	}
 
 	/**
 	 * Creates and returns a {@link DeterminateProgressMonitor} with the
 	 * specified parameters. Adds a change listener to the monitor in order to
 	 * popup a modal {@link ProgressDialog} when the progress monitor starts and
 	 * after <code>millSecondsToWait</code> milliseconds.
 	 * 
 	 * @param owner
 	 *            preferably a {@link Window}, else used to find the
 	 *            <code>Window</code> ancestor
 	 * @param title
 	 *            the title of the dialog
 	 * @param min
 	 *            the minimum boundary for the status value
 	 * @param max
 	 *            the maximum boundary for the status value
 	 * @param current
 	 *            the current status value
 	 * @param milliSecondsToWait
 	 *            time to wait before dialog is displayed
 	 * @param cancelDelegate
 	 *            the delegate to handle if the action is canceled
 	 * @return the <code>ProgressMonitor</code> object
 	 */
 	public static DeterminateProgressMonitor createModalDeterminateProgressMonitor(
 			Component owner, String title, int min, int max, int current,
 			int milliSecondsToWait, CancelHandler cancelDelegate) {
 		DeterminateProgressMonitor monitor = new DeterminateProgressMonitor(
 				min, max, current, milliSecondsToWait);
 		Window window = owner instanceof Window ? (Window) owner
				: SwingUtilities.getWindowAncestor(owner);
 		monitor.addProgressListener(new MonitorListener(window, title, monitor,
 				cancelDelegate));
 		return monitor;
 	}
 
 	/**
 	 * Creates and returns a {@link IndeterminateProgressMonitor} with the
 	 * specified parameters. Adds a change listener to the monitor in order to
 	 * popup a modal {@link ProgressDialog} when the progress monitor starts and
 	 * after <code>millSecondsToWait</code> milliseconds.
 	 * 
 	 * @param owner
 	 *            preferably a {@link Window}, else used to find the
 	 *            <code>Window</code> ancestor
 	 * @param title
 	 *            the title of the dialog
 	 * @param milliSecondsToWait
 	 *            time to wait before dialog is displayed
 	 * @param cancelDelegate
 	 *            the delegate to handle if the action is canceled
 	 * @return the <code>ProgressMonitor</code> object
 	 */
 	public static IndeterminateProgressMonitor createModalIndeterminateProgressMonitor(
 			Component owner, String title, int milliSecondsToWait,
 			CancelHandler cancelDelegate) {
 		IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(
 				milliSecondsToWait);
 		Window window = owner instanceof Window ? (Window) owner
				: SwingUtilities.getWindowAncestor(owner);
 		monitor.addProgressListener(new MonitorListener(window, title, monitor,
 				cancelDelegate));
 		return monitor;
 	}
 
 }
