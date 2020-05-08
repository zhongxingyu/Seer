 package cz.cuni.mff.odcleanstore.installer.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.FlowLayout;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import cz.cuni.mff.odcleanstore.installer.utils.AwtUtils;
 
 /**
  * A Base class from GUI wizard based installer.
  * 
  * @author Petr Jerman
  */
 public abstract class InstallationWizardFrame {
 
 	private JFrame frame;
 	private JLabel title;
 
 	private JPanel mainPanel;
 	private JPanel form;
 	private JPanel navigator;
 	private JButton next;
 	private JButton cancel;
 	private JButton finish;
 
 	private int stepNumber;
 	private InstallationWizardStep step;
 
 	/**
 	 * Create instance of wizard frame object with all GUI elements.
 	 * 
 	 * @throws IOException
 	 */
 	public InstallationWizardFrame() throws IOException {
 
 		frame = new JFrame();
 		frame.setTitle("ODCleanStore Installer");
 		frame.setIconImage(AwtUtils.loadResourceToImage("/cube.png"));
 		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
 		frame.setBounds(center.x - 400, center.y - 300, 800, 600);
 		frame.setResizable(false);
 
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new WindowAdapter() {
 
 			public void windowClosing(WindowEvent e) {
 				onCancel();
 			}
 		});
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 
 		JPanel panelStepTitle = new JPanel();
 		panelStepTitle.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelStepTitle.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
 		mainPanel.add(panelStepTitle, BorderLayout.PAGE_START);
 
 		form = new JPanel();
 		form.setLayout(new FlowLayout(FlowLayout.LEFT));
 		form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
 		mainPanel.add(form, BorderLayout.CENTER);
 
 		navigator = new JPanel();
 		navigator.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		navigator.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
 		mainPanel.add(navigator, BorderLayout.PAGE_END);
 
 		// step label
 		title = new JLabel();
 		title.setForeground(new Color(0, 0, 128));
 		title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
 		panelStepTitle.add(title);
 
 		// next button
 		next = new JButton("Next");
 		navigator.add(next);
 		next.setToolTipText("Go to the next step");
 		next.addActionListener(actionListener);
 
 		// cancel button
 		cancel = new JButton("Cancel");
 		cancel.setToolTipText("Terminate installation wizard");
 		navigator.add(cancel);
 		cancel.addActionListener(actionListener);
 
 		// finish button
 		finish = new JButton("Finish");
 		finish.setToolTipText("Close installation wizard");
 		finish.addActionListener(actionListener);
 
 		// activate panel
 		frame.setContentPane(mainPanel);
 		mainPanel.updateUI();
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Called on finishing wizard.
 	 */
 	protected void onFinish() {
 		frame.setVisible(false);
 		frame.dispose();
 		System.exit(0);
 	}
 
 	/**
 	 * Called on closing request from user before wizard close by user decision.
 	 */
 	protected void onCancel() {
 		if (step != null) {
			if (showConfirmDialog("Are you sure terminate installation?", "Terminate installation")) {
 				step.onCancel();
 				onFinish();
 			}
 		}
 	}
 
 	/**
 	 * Set next wizard step, null parameter value sets finish page.
 	 * 
 	 * @param step next wizard step
 	 * @throws IOException
 	 */
 	protected void setNextStep(InstallationWizardStep step) throws IOException {
 		form.removeAll();
 		this.step = step;
 		if (step != null) {
 			title.setText("Step " + ++stepNumber + " - " + step.getStepTitle());
 			JPanel panel = step.getFormPanel();
 			form.add(panel);
 		} else {
 			navigator.removeAll();
 			navigator.add(finish);
 			title.setText("Installation successfully completed");
 		}
 		mainPanel.updateUI();
 	}
 
 	/**
 	 * Called from ui framework on ui event.
 	 * 
 	 * @param arg parameters of ui action
 	 * @throws IOException
 	 */
 	private void ActionPerformed(ActionEvent arg) throws IOException {
 		if (arg.getSource() == finish) {
 			onFinish();
 		} else if (arg.getSource() == next) {
 			if (step != null && step.onNext()) {
 				next();
 			}
 		} else if (arg.getSource() == cancel) {
 			onCancel();
 		} else if (step != null) {
 			step.onFormEvent(arg);
 		}
 	}
 
 	private class ActionListener implements java.awt.event.ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg) {
 			try {
 				ActionPerformed(arg);
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	private ActionListener actionListener = new ActionListener();
 
 	/**
 	 * Get action listener for listening button action.
 	 * 
 	 * @return action listener object
 	 */
 	public ActionListener getActionListener() {
 		return actionListener;
 	}
 
 	/**
 	 * Show confirm modal dialog and return user decision.
 	 * 
 	 * @param message message to display in dialog
 	 * @param title caption of dialog
 	 * @return user decision of confirmation
 	 */
 	public boolean showConfirmDialog(String message, String title) {
 		return JOptionPane.showConfirmDialog(form, message, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
 	}
 
 	/**
 	 * Show warning modal dialog.
 	 * 
 	 * @param message message to display in dialog
 	 * @param title caption of dialog
 	 */
 	public void showWarningDialog(String message, String title) {
 		JOptionPane.showMessageDialog(form, message, title, JOptionPane.WARNING_MESSAGE);
 	}
 
 	/**
 	 * Show information modal dialog.
 	 * 
 	 * @param message message to display in dialog
 	 * @param title caption of dialog
 	 */
 	public void showInfoDialog(String message, String title) {
 		JOptionPane.showMessageDialog(form, message, title, JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	/**
 	 * Get current step wizard number.
 	 * 
 	 * @return current step wizard number.
 	 */
 	public int getStepNumber() {
 		return stepNumber;
 	}
 
 	/**
 	 * Abstract method for moving wizard to next step.
 	 * @throws IOException
 	 */
 	public abstract void next() throws IOException;
 
 	/**
 	 * Starts long running instalation, hide next button and set wait cursor.
 	 */
 	public void startInstallation() {
 		next.setVisible(false);
 		form.setCursor(new Cursor(Cursor.WAIT_CURSOR));
 	}
 
 	/**
 	 * Ends long running instalation, restore cursor.
 	 */
 	public void endInstallation() {
 		form.setCursor(null);
 	}
 
 	/**
 	 * Display information message and terminate wizard.
 	 * 
 	 * @param message message to display in dialog box
 	 */
 	public void cancelInstallation(String message) {
 		JOptionPane.showMessageDialog(form, message + "\n installation terminate", "Error", JOptionPane.ERROR_MESSAGE);
 		onFinish();
 	}
 }
