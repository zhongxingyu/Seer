 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import java.util.Enumeration;
 import java.util.HashSet;
 
 /**
  * The Settings Frame is used to change what "mode" the game is in.  
  */
 public class SettingsFrame extends JFrame implements ActionListener
 {
 	private Game currentGame;
 	private Gui currentGui;
 	private String changeModeTo;
 
 	/**
 	 * Constructs a new SettingsFrame.
 	 *
 	 * @param currentGame The current type of game the player is using.
 	 * @param currentGui The current type of Gui the player is using.  Also for hiding the Gui when the AboutFrame is clicked.
 	 */
 	public SettingsFrame(Game currentGame, Gui currentGui)
 	{
 		super("Settings");
 		this.currentGame = currentGame;
 		this.currentGui = currentGui;
 		changeModeTo = "";
 
 		JLabel gameModeLabel = new JLabel("Current game mode is: " + this.currentGui.getGameType() + " mode.");
 		JButton okButton = new JButton("Ok");
 			okButton.setActionCommand("ok");
 			okButton.addActionListener(this);
 		JButton closeButton = new JButton("Close");
 			closeButton.setActionCommand("close");
 			closeButton.addActionListener(this);
 		JButton applyButton = new JButton("Apply");
 			applyButton.setActionCommand("apply");
 			applyButton.addActionListener(this);
 		HashSet<JRadioButtonMenuItem> buttonGroup = new HashSet<JRadioButtonMenuItem>();
 
 //Begin	radioButton addition section. DO NOT DELETE THIS LINE.
 		buttonGroup.add(new JRadioButtonMenuItem("Classic"));
 		buttonGroup.add(new JRadioButtonMenuItem("Safe teleports"));
		buttonGroup.add(new JRadioButtonMenuItem("Fast Robots"));
 //End	radioButton addition section. DO NOT DELETE THIS LINE.
 
 		JPanel gameModePanel = new JPanel(new GridLayout(0, 1));
 		gameModePanel.add(gameModeLabel);
 
 		ButtonGroup gameModeButtonGroup = new ButtonGroup();
 		for(JRadioButtonMenuItem radioButton : buttonGroup)
 		{
 			if(radioButton.getText().equals(currentGui.getGameType()))
 			{
 				radioButton.setSelected(true);
 			}
 			radioButton.setActionCommand(radioButton.getText());
 			radioButton.addActionListener(this);
 			gameModeButtonGroup.add(radioButton);
 			gameModePanel.add(radioButton);
 		}
 		JToolBar buttonToolBar = new JToolBar();
 		buttonToolBar.setFloatable(false);
 		buttonToolBar.add(okButton);
 		buttonToolBar.add(closeButton);
 		buttonToolBar.add(applyButton);
 
 		this.add(gameModePanel, BorderLayout.NORTH);
 		this.add(buttonToolBar, BorderLayout.SOUTH);
 		this.pack();
 	}
 
 	/**
 	 * The action performed when a button is pressed.
 	 *
 	 * @param event The event triggered by the close button.
 	 */
 	public void actionPerformed(ActionEvent event)
 	{
 		String command = event.getActionCommand();
 		if(command.equals("apply") || command.equals("ok"))
 		{
 			if(changeModeTo.equals("Classic"))
 			{
 				currentGui = new ClassicGui();
 			}
 			else if(changeModeTo.equals("Safe teleports"))
 			{
 				currentGui = new SafeTeleportsGui();
 			}
			else if(changeModeTo.equals("Fast Robots"))
			{
				currentGui = new FastRobotsGui();
			}
 //Add new button selecton modes HERE. Should be an "else if".  The Command should be the same as the button's text. DO NOT DELETE THIS LINE.
 			if(command.equals("ok"))
 			{
 				closeSettingsFrame();
 			}
 		}
 		else if(command.equals("close"))
 		{
 			closeSettingsFrame();
 		}
 		else //command is a radioButton press
 		{
 			changeModeTo = command;
 		}
 	}
 
 	/**
 	 * Shows or hides this component depending on the value of parameter visible.
 	 *
 	 * @param visible If true, shows this component; otherwise, hides this component
 	 */
 	public void setVisible(boolean visible)
 	{
 		currentGui.setVisible(!visible);
 		super.setVisible(visible);
 	}
 
 	//Closes the settings frame and forgets what the user wants to change the gameModeTo.
 	private void closeSettingsFrame()
 	{
 		this.setVisible(false);
 		changeModeTo= "";
 	}
 }
