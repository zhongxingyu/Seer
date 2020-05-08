 package usr.erichschroeter.progressive;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.HeadlessException;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.util.ResourceBundle;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 import org.jdesktop.swingx.JXCollapsiblePane;
 
 /**
  * A dialog providing an interface for the user to be updated on progress of an
  * action being performed by an application.
  * <p>
  * The dialog is displayed differently based on some constraints. Namely, if
  * there is no delegate defined to handle canceling the action it is assumed
  * that the action is not cancelable, and thus no cancel button is displayed.
  * <p>
  * The progress bar is displayed depending on whether the progress of an action
  * is determinate or indeterminate. See {@link JProgressBar} for its features.
  * <p>
  * Another feature provided by the dialog is to allow the dialog to
  * automatically be closed based on two switches.
  * <ul>
  * <li>once a the progress being monitored is completed (via
  * {@link #setAutoCloseOnComplete(boolean)})</li>
  * <li>if the progress is canceled (via {@link #setAutoCloseOnCancel(boolean)})</li>
  * </ul>
  * 
  * @author Santhosh Kumar T, Erich Schroeter
  * @see ProgressUtil
  * @see DeterminateProgressMonitor
  * @see IndeterminateProgressMonitor
  */
 @SuppressWarnings("serial")
 public class ProgressDialog extends JDialog implements ProgressListener {
 
 	protected static final ResourceBundle i18ln = ResourceBundle
 			.getBundle("usr.erichschroeter.progressive.i18ln.ProgressDialog");
 
 	/** A label displaying the latest status text. */
 	private JLabel statusLabel;
 	/** The progress bar for displaying the progress. */
 	private JProgressBar progressBar;
 	/** A place to aggregate all status text. */
 	private JTextArea statusHistory;
 	/** The monitor monitoring progress. */
 	private AbstractProgressMonitor monitor;
 	/**
 	 * The cancel button. Not displayed if progress is not cancelable.
 	 * 
 	 * @see #isCancelable()
 	 */
 	private JButton cancelButton;
 	/**
 	 * The delegate called if the progress monitoring is canceled. By default,
 	 * this determines if the progress is cancelable.
 	 * 
 	 * @see #isCancelable()
 	 */
 	private CancelHandler cancelDelegate;
 	/** Whether to automatically close the dialog when progress completes. */
 	private boolean autoCloseOnComplete;
 	/** Whether to automatically close the dialog when progress is canceled. */
 	private boolean autoCloseOnCancel;
 	/** The previous status text. */
 	private String previousStatus;
 
 	/**
 	 * Constructs a <code>ProgressDialog</code> specifying the owner, the
 	 * progress monitor, and an optional cancel delegate.
 	 * 
 	 * @param owner
 	 *            the owner of the dialog
 	 * @param monitor
 	 *            the progress monitor
 	 * @param cancelDelegate
 	 *            the cancel delegate (<code>null</code> permitted)
 	 * @throws HeadlessException
 	 *             if <code>GraphicsEnvironment.isHeadless()</code> returns
 	 *             <code>true</code>
 	 * @see #ProgressDialog(Frame, String, AbstractProgressMonitor,
 	 *      CancelHandler)
 	 */
 	public ProgressDialog(Frame owner, AbstractProgressMonitor monitor,
 			CancelHandler cancelDelegate) throws HeadlessException {
 		this(owner, i18ln.getString("defaultDialogTitle"), monitor,
 				cancelDelegate);
 	}
 
 	/**
 	 * Constructs a <code>ProgressDialog</code> specifying the owner, the
 	 * dialog's title, the progress monitor, and an optional cancel delegate.
 	 * 
 	 * @param owner
 	 *            the owner of the dialog
 	 * @param title
 	 *            the <code>String</code> to display in the dialog's title bar
 	 * @param monitor
 	 *            the progress monitor
 	 * @param cancelDelegate
 	 *            the cancel delegate (<code>null</code> permitted)
 	 * @throws HeadlessException
 	 *             if <code>GraphicsEnvironment.isHeadless()</code> returns
 	 *             <code>true</code>
 	 */
 	public ProgressDialog(Frame owner, String title,
 			AbstractProgressMonitor monitor, CancelHandler cancelDelegate)
 			throws HeadlessException {
 		super(owner, title, true);
 		this.cancelDelegate = cancelDelegate;
 		init(monitor);
 	}
 
 	/**
 	 * Constructs a <code>ProgressDialog</code> specifying the owner, the
 	 * progress monitor, and an optional cancel delegate.
 	 * 
 	 * @param owner
 	 *            the owner of the dialog
 	 * @param monitor
 	 *            the progress monitor
 	 * @param cancelDelegate
 	 *            the cancel delegate (<code>null</code> permitted)
 	 * @throws HeadlessException
 	 *             if <code>GraphicsEnvironment.isHeadless()</code> returns
 	 *             <code>true</code>
 	 * @see #ProgressDialog(Dialog, String, AbstractProgressMonitor,
 	 *      CancelHandler)
 	 */
 	public ProgressDialog(Dialog owner, AbstractProgressMonitor monitor,
 			CancelHandler cancelDelegate) throws HeadlessException {
 		this(owner, i18ln.getString("defaultDialogTitle"), monitor,
 				cancelDelegate);
 	}
 
 	/**
 	 * Constructs a <code>ProgressDialog</code> specifying the owner, the
 	 * dialog's title, the progress monitor, and an optional cancel delegate.
 	 * 
 	 * @param owner
 	 *            the owner of the dialog
 	 * @param title
 	 *            the <code>String</code> to display in the dialog's title bar
 	 * @param monitor
 	 *            the progress monitor
 	 * @param cancelDelegate
 	 *            the cancel delegate (<code>null</code> permitted)
 	 * @throws HeadlessException
 	 *             if <code>GraphicsEnvironment.isHeadless()</code> returns
 	 *             <code>true</code>
 	 */
 	public ProgressDialog(Dialog owner, String title,
 			AbstractProgressMonitor monitor, CancelHandler cancelDelegate)
 			throws HeadlessException {
 		super(owner, title);
 		this.cancelDelegate = cancelDelegate;
 		init(monitor);
 	}
 
 	private void init(AbstractProgressMonitor monitor) {
 		this.monitor = monitor;
		this.autoCloseOnCancel = true;
		this.autoCloseOnComplete = true;
 
 		if (monitor instanceof DeterminateProgressMonitor) {
 			DeterminateProgressMonitor dMonitor = (DeterminateProgressMonitor) monitor;
 			progressBar = new JProgressBar(dMonitor.getMin(), dMonitor.getMax());
 			progressBar.setValue(dMonitor.getCurrent());
 		} else {
 			progressBar = new JProgressBar();
 			progressBar.setIndeterminate(true);
 		}
 
 		getContentPane().setLayout(new BorderLayout());
 
 		GridBagConstraints c;
 		JPanel north = new JPanel(new GridBagLayout());
 
 		statusHistory = new JTextArea();
 		final JXCollapsiblePane collapsePane = new JXCollapsiblePane();
 		collapsePane.setLayout(new BorderLayout());
 		collapsePane.setCollapsed(true);
 		collapsePane.add(new JScrollPane(statusHistory), BorderLayout.CENTER);
 
 		c = new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
 				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 				new Insets(2, 2, 2, 2), 0, 0);
 		statusLabel = new JLabel();
 		north.add(statusLabel, c);
 		c = new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
 				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 				new Insets(2, 2, 2, 2), 0, 0);
 		north.add(progressBar, c);
 
 		AbstractAction detailsAction = (AbstractAction) collapsePane
 				.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
 		detailsAction.putValue(AbstractAction.NAME,
 				i18ln.getString("detailsButton"));
 		detailsAction.putValue(
 				JXCollapsiblePane.COLLAPSE_ICON,
 				Utils.imageIcon(R.png("collapse.png"),
 						ProgressDialog.class.getClassLoader()));
 		detailsAction.putValue(
 				JXCollapsiblePane.EXPAND_ICON,
 				Utils.imageIcon(R.png("expand.png"),
 						ProgressDialog.class.getClassLoader()));
 		JButton collapseButton = new JButton(detailsAction);
 
 		JPanel horizPanel = new JPanel(new GridBagLayout());
 		c = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
 				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 				new Insets(2, 2, 2, 2), 0, 0);
 		horizPanel.add(collapseButton, c);
 		c = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
 				GridBagConstraints.BASELINE, GridBagConstraints.BOTH,
 				new Insets(2, 2, 2, 2), 0, 0);
 		horizPanel.add(new JSeparator(), c);
 
 		if (isCancelable()) {
 			cancelButton = new JButton(new AbstractAction(
 					i18ln.getString("cancelButton")) {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					// the cancel button is dual purposed. Once the progress has
 					// completed it turns into a close button, and this logic is
 					// implemented here.
 					if (getTitle().equals(i18ln.getString("closeButton"))) {
 						dispose();
 					} else {
 						ProgressDialog.this.monitor.cancel();
 					}
 				}
 			});
 			c = new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
 					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 					new Insets(2, 2, 2, 2), 0, 0);
 			horizPanel.add(cancelButton, c);
 		}
 		c = new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
 				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 				new Insets(2, 2, 2, 2), 0, 0);
 		north.add(horizPanel, c);
 
 		// add Progress components and the Collapsable Details
 		// c = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
 		// GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 		// new Insets(2, 2, 2, 2), 0, 0);
 		getContentPane().add(north, BorderLayout.NORTH);
 		// c = new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
 		// GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 		// new Insets(2, 2, 2, 2), 0, 0);
 		getContentPane().add(collapsePane, BorderLayout.CENTER);
 
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		monitor.addProgressListener(this);
 	}
 
 	/**
 	 * Allows derived classes to customize how the dialog decides whether or not
 	 * the progress is cancelable.
 	 * <p>
 	 * The default implementation decides by whether the cancel delegate is
 	 * <code>null</code> or not.
 	 * 
 	 * @return <code>true</code> if the progress is cancelable, else
 	 *         <code>false</code> if not cancelable
 	 */
 	protected boolean isCancelable() {
 		return cancelDelegate != null;
 	}
 
 	/**
 	 * Sets whether to automatically close the dialog if the progress has been
 	 * canceled.
 	 * 
 	 * @param autoCloseOnCancel
 	 *            <code>true</code> to automatically close the dialog, or
 	 *            <code>false</code> to leave open
 	 */
 	public void setAutoCloseOnCancel(boolean autoCloseOnCancel) {
 		this.autoCloseOnCancel = autoCloseOnCancel;
 	}
 
 	/**
 	 * Sets whether to automatically close the dialog once the progress has
 	 * completed.
 	 * 
 	 * @param autoCloseOnComplete
 	 *            <code>true</code> to automatically close the dialog, or
 	 *            <code>false</code> to leave open
 	 */
 	public void setAutoCloseOnComplete(boolean autoCloseOnComplete) {
 		this.autoCloseOnComplete = autoCloseOnComplete;
 	}
 
 	/**
 	 * Sets the latest status of the progress. This updates the status label on
 	 * the dialog and pushes the previous status to the status history text
 	 * area.
 	 * <p>
 	 * If <code>status</code> is <code>null</code> the status label is cleared.
 	 * The status history will be appended only if the previous status is not
 	 * <code>null</code> and the previous status is not equal to
 	 * <code>status</code>.
 	 * 
 	 * @param status
 	 *            the latest status
 	 */
 	public void updateStatus(String status) {
 		previousStatus = statusLabel.getText();
 		statusLabel.setText(status);
 		if (previousStatus != null && !previousStatus.equals(status)) {
 			statusHistory.append(String.format("%s%n", previousStatus));
 		}
 	}
 
 	/**
 	 * Updates the status label with the status event text. If the dialog's
 	 * progress monitor is a {@link DeterminateProgressMonitor} the progress bar
 	 * is synced with the status event's values.
 	 * <p>
 	 * If the progress is completed and the auto-close on complete feature is
 	 * enabled, the dialog is disposed. If the progress is completed and the
 	 * auto-close on complete feature is disabled, the cancel button is changed
 	 * to a close button and the default close operation of the dialog is set to
 	 * dispose.
 	 * 
 	 * @param e
 	 *            the status event
 	 * @see #updateStatus(String)
 	 */
 	@Override
 	public void statusChanged(final StatusEvent e) {
 		// to ensure EDT thread
 		if (!SwingUtilities.isEventDispatchThread()) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					statusChanged(e);
 				}
 			});
 			return;
 		}
 		updateStatus(e.getText());
 		if (monitor instanceof DeterminateProgressMonitor) {
 			DeterminateProgressMonitor dMonitor = (DeterminateProgressMonitor) monitor;
 			// sync the min and max values with the monitor
 			if (progressBar.getMinimum() != dMonitor.getMin()) {
 				progressBar.setMinimum(dMonitor.getMin());
 			}
 			if (progressBar.getMaximum() != dMonitor.getMax()) {
 				progressBar.setMaximum(dMonitor.getMax());
 			}
 			progressBar.setValue(dMonitor.getCurrent());
 		}
 		// automatically close if feature enabled
 		if (e.isCompleted()) {
 			// regardless of whether the monitor is determinate or
 			// indeterminate, we need to stop the progress monitor and force it
 			// to the max value to notify the user that the progress has
 			// finished
 			progressBar.setIndeterminate(false);
 			progressBar.setValue(progressBar.getMaximum());
 			if (autoCloseOnComplete) {
 				dispose();
 			} else {
 				setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 				if (cancelButton != null) {
 					cancelButton.setText(i18ln.getString("closeButton"));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Calls the {@link CancelHandler} delegate. If the auto-close on cancel
 	 * feature is enabled, the dialog is disposed.
 	 * 
 	 * @param e
 	 *            the status event
 	 */
 	@Override
 	public void canceled(StatusEvent e) {
 		cancelDelegate.canceled();
 		if (autoCloseOnCancel) {
 			dispose();
 		}
 	}
 }
