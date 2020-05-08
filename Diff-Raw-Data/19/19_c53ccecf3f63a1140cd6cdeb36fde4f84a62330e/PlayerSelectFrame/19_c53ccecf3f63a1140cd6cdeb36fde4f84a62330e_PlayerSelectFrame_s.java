 package wolves;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class PlayerSelectFrame extends JFrame {
 	
 	private String[] players;
 	private String selection;
 	private JLabel label;
 	
 	public static String choosePlayer(String prompt, String[] players) {
 		PlayerSelectFrame frame = new PlayerSelectFrame("<html>" + prompt + "</html>", players);
 		String val = frame.getSelection();
 		frame.setVisible(false);
 		frame.dispose();
 		return val;
 	}
 	
 	private synchronized String getSelection() {
 		while (selection == null) {
 			try {
 				this.wait();
 			} catch (InterruptedException e) {
 				throw (new RuntimeException(e));
 				//Seriously when is it ever correct to actually handle this error
 			}
 		}
 		return selection;
 		
 	}
 
 	private PlayerSelectFrame(String prompt, String[] players) {
 		
 		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS)); 		
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		this.players = players;
 		this.label = new JLabel(prompt);
 		
 		JPanel labelPanel = new JPanel();
 		JPanel buttonPanel = new JPanel();
 		labelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		labelPanel.add(label);
 		buttonPanel.setLayout(new FlowLayout());
 		
 		JButton[] referenceToButtons = new JButton[this.players.length]; // sizes cannot be calculated until they are visible.
 		
 		for (int i = 0; i < this.players.length; i++) {
 			final String j = this.players[i];
 			JButton button = new JButton(this.players[i]);
 			button.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					PlayerSelectFrame.this.choose(j);
 				}				
 			});
 			buttonPanel.add(button);
 			referenceToButtons[i] = button;
 		}
 		this.add(labelPanel);
 		this.add(buttonPanel);		
 		this.pack();
 		
 		// Things are now packed, and getHeight() etc. now work.
 		// the following calculates how high the frame should be. (Because FlowLayout sucks...)
 		int remainingWidth = label.getWidth() + 10;
 		int TotalHeight = referenceToButtons[0].getHeight();
 		int rowMaxHeight = referenceToButtons[0].getHeight();
 		for (int i = 0; i < referenceToButtons.length; i++) {
 			if(remainingWidth < (referenceToButtons[i].getWidth() + 5)){ // button goes on next row
 				remainingWidth = label.getWidth() + 5 - referenceToButtons[i].getWidth();
 				rowMaxHeight = referenceToButtons[i].getHeight();
 				TotalHeight += rowMaxHeight + 5;
 			} else {
 				remainingWidth -= (referenceToButtons[i].getWidth() + 5);
 				if(referenceToButtons[i].getHeight() > rowMaxHeight){
 					TotalHeight += (referenceToButtons[i].getHeight() - rowMaxHeight);
 					rowMaxHeight = referenceToButtons[i].getHeight();
 				}
 			}
 		}
 
 		buttonPanel.setSize(new Dimension(label.getWidth() + 10, TotalHeight + 20));
 		buttonPanel.setPreferredSize(new Dimension(label.getWidth() + 10, TotalHeight + 20));
 		buttonPanel.setMinimumSize(new Dimension(label.getWidth() + 10, TotalHeight + 20));
 		
 		this.setSize(new Dimension(label.getWidth() + 10, TotalHeight + labelPanel.getHeight() + 20));	
 		this.setVisible(true);
 		
 		
 	}
 
 	
 	private synchronized void choose(String s) {
 		this.selection = s;
 		this.notify();
 	}
 	
 
 }
