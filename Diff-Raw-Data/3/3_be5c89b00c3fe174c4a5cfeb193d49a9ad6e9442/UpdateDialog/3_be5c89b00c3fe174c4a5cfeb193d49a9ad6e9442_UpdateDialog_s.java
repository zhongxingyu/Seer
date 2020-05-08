 package org.spoutcraft.launcher.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import org.spoutcraft.launcher.Main;
 import org.spoutcraft.launcher.modpacks.ModPackListYML;
 
 public class UpdateDialog extends JDialog implements ActionListener {
 
 	private static final long	serialVersionUID	= -4617588853047124397L;
 	private final JPanel			contentPanel			= new JPanel();
 	private final JLabel			label							= new JLabel("There is a new update for %TO_UPDATE%.");
 	private final LoginForm		lf;
 
 	public void setToUpdate(String str) {
 		label.setText(label.getText().replace("%TO_UPDATE%", str));
 	}
 
 	public UpdateDialog(LoginForm lf) {
 		this.lf = lf;
 		setBounds((Toolkit.getDefaultToolkit().getScreenSize().width - 450) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - 136) / 2, 450, 136);
 		this.toFront();
 		this.setAlwaysOnTop(true);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setLayout(new FlowLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 
 		setIconImage(ModPackListYML.favIcon);
 
 		label.setFont(new Font("Arial", Font.PLAIN, 18));
 		contentPanel.add(label);
 		JLabel lblThereIsA = new JLabel("Would you like to update?");
 		lblThereIsA.setFont(new Font("Arial", Font.PLAIN, 18));
 		contentPanel.add(lblThereIsA);
 		JPanel buttonPane = new JPanel();
 		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		getContentPane().add(buttonPane, BorderLayout.SOUTH);
 		JButton okButton = new JButton("Yes");
 		okButton.addActionListener(this);
 		okButton.setActionCommand("Yes");
 		buttonPane.add(okButton);
 		getRootPane().setDefaultButton(okButton);
 		JButton noButton = new JButton("No");
 		buttonPane.add(noButton);
 		noButton.addActionListener(this);
 		JButton cancelButton = new JButton("Cancel");
 		buttonPane.add(cancelButton);
 		cancelButton.addActionListener(this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String actionCommand = e.getActionCommand();
 		if (actionCommand.equals("Yes")) {
 			lf.updateThread();
 		} else if (actionCommand.equals("No")) {
 			lf.runGame();
 		}
		Main.loginForm.enableUI();
 		this.setVisible(false);
 	}
 
 }
