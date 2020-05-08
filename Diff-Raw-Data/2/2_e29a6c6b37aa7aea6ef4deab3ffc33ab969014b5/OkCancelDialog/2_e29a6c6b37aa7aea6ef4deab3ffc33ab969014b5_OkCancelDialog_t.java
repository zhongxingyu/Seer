 package ui.swing.presenter;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import ui.swing.utils.SwingUtils;
 import ui.swing.utils.UIEventsExecutor;
 
 public class OkCancelDialog extends JDialog {
 	private static final long serialVersionUID = 1L;
 	private final UIEventsExecutor executor;
 
 	public OkCancelDialog(final UIEventsExecutor executor,
 			final ActionPane pane, final String title, final JFrame mainScreen) {
 		super(mainScreen);
 		this.executor = executor;
 		SwingUtils.makeLocationrelativeToParent(this, mainScreen);
 		this.setTitle(title);
 
 		final JPanel panel = pane.getPanel();
 		if (panel == null) {
 			throw new IllegalStateException("panel must not be null");
 		}
 
 		panel.setBorder(BorderFactory.createEmptyBorder());
 
 		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
 		this.add(new BinaryButtonBar(new Runnable() {
 			@Override
 			public void run() {
 				doAction(pane);
 			}
 		}, new Runnable() {
 			@Override
 			public void run() {
 				OkCancelDialog.this.setVisible(false);
 			}
 		}, "ok", "cancel"), BorderLayout.SOUTH);
 
 		this.getRootPane().registerKeyboardAction(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				OkCancelDialog.this.setVisible(false);
 			}
 		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
 				JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		this.getRootPane().registerKeyboardAction(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				doAction(pane);
 			}
 		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
 				JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		this.pack();
 	}
 
 	void doAction(final ActionPane pane) {
 		executor.execute(new Runnable() {
 			@Override
 			public void run() {
 				internalDoAction(pane);
 			}
 		});
 
 	}
 
 	void internalDoAction(final ActionPane pane) {
 		try {
 			pane.action().run();
 		} catch (final ValidationException e) {
 			JOptionPane.showMessageDialog(this, e.getUserMessage());
 			return;
 		}
 		OkCancelDialog.this.setVisible(false);
 	}
 
 }
