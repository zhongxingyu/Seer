 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JFrame;
 
 /**
  * A frame that displays information about the game.
  */
 public class AboutFrame extends JFrame implements ActionListener
 {
 	private Gui currentGui;
 
 	/**
 	 * Constructs an AboutFrame.
 	 *
 	 * @param currentGui The current type of Gui the player is using.  For hiding the Gui when the AboutFrame is clicked.
 	 */
 	public AboutFrame(Gui currentGui)
 	{
 		super("About Robots");
 		this.currentGui = currentGui;
		JLabel informationLabel = new JLabel("<html><pre>Robots v1.00<br>Based of of the unix game of the same name.<br>code: Jacob Patterson<br>Special thanks to CKHS AP-CS:AB 2009</pre></html>");
 
 		JButton closeButton = new JButton("close");
 			closeButton.setActionCommand("close");
 			closeButton.addActionListener(this);
 		this.add(informationLabel, BorderLayout.NORTH);
 		this.add(closeButton, BorderLayout.SOUTH);
 		this.pack();
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.setLocationRelativeTo(currentGui);
 	}
 
 	/**
 	 * The action performed when a button is pressed.
 	 *
 	 * @param event The event triggered by the close button.
 	 */
 	public void actionPerformed(ActionEvent event)
 	{
 		if(event.getActionCommand().equals("close"))
 		{
 			this.setVisible(false);
 		}
 	}
 
 	/**
 	 * Shows or hides this component depending on the value of parameter visible.
 	 *
 	 * @param visible If true, shows this component; otherwise, hides this component.
 	 */
 	public void setVisible(boolean visible)
 	{
 		currentGui.setVisible(!visible);
 		super.setVisible(visible);
 	}
 }
