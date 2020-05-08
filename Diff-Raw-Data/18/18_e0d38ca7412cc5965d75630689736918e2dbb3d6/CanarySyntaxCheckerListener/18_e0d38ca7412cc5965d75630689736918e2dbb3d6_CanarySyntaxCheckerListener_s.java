 /**
  * @author Brian McCarthy
  */
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 
 
 public class CanarySyntaxCheckerListener implements ActionListener{
 	CanarySyntaxChecker instance;
 	String currText = "";
 
 	JPanel panel;
 	JTextArea textArea;
 
 	public CanarySyntaxCheckerListener(CanarySyntaxChecker paramInstance){
 		instance = paramInstance;
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e){
 
 		}
 	}// end CanarySyntaxCheckerListener(CanarySyntaxChecker) 
 
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource().equals(instance.menuItem_Add)){
 			if (instance.textArea.isEditable()){
 				currText = JOptionPane.showInputDialog(null, "Enter text:");
 				instance.textArea.append(currText + "\n");
 			} else {
				JOptionPane.showMessageDialog(null, "You can't add a line to read results! Click Text - Clear Text to start adding text again.");
 			}
 
 		} else if (event.getSource().equals(instance.menuItem_Clear)){
 			instance.textArea.setText("");
 			instance.textArea.setEditable(true);
 		} else if (event.getSource().equals(instance.menuItem_Groups)){
 			if (!instance.textArea.isEditable()){
 				JOptionPane.showMessageDialog(null, "You may not read the results of a read action! Click Text - Clear Text to start adding text again.");
 				return;
 			}
 			if (instance.textArea.getText().trim().equalsIgnoreCase("")){
 				instance.textArea.setText("Please enter data!");
 				return;
 			}
 			GroupAnalyser analyser = new GroupAnalyser(instance.textArea.getText()); 
 			instance.analyser = analyser;
 			analyser.setup();
 			analyser.startAnalysing();
 			instance.textArea.setText("");
 			for (int i = 0; i < Analyser.filteredData.size(); i++){
 				DataGroup dataGroup = Analyser.getDataGroupFromLineNum(i);
 				instance.textArea.append("Name: " + dataGroup.getName() + "\n");
 				instance.textArea.append("Prefix: " + dataGroup.getPrefix() + "\n");
 				instance.textArea.append("Commands: " + dataGroup.getCommands() + "\n");
 				instance.textArea.append("Inherited: " + dataGroup.getInherited() + "\n");
 				instance.textArea.append("Admin: " + dataGroup.getAdmin() + "\n");
 				instance.textArea.append("IP: " + dataGroup.getIP() + "\n");
 				instance.textArea.append("Group: " + dataGroup.getGroup()+ "\n");
 				instance.textArea.append("\n");
 				instance.textArea.append("Validity: " + dataGroup.getValidity() + "\n");
 				instance.textArea.append("--\n");
 				instance.textArea.append("\n");
 			}// end for
 			instance.textArea.setEditable(false);
 		} else if (event.getSource().equals(instance.menuItem_Users)){
 			if (!instance.textArea.isEditable()){
 				JOptionPane.showMessageDialog(null, "You may not read the results of a read action! Click Text - Clear Text to start adding text again.");
 				return;
 			}
 			if (instance.textArea.getText().trim().equalsIgnoreCase("")){
				instance.textArea.setText("Please enter data!");
 				return;
 			}
 			UserAnalyser analyser = new UserAnalyser(instance.textArea.getText());
 			instance.analyser = analyser;
 			analyser.setup();
 			analyser.startAnalysing();
 			instance.textArea.setText("");
 			for (int i = 0; i < Analyser.filteredData.size(); i++){
 				DataGroup dataGroup = Analyser.getDataGroupFromLineNum(i);
 				instance.textArea.append("Name: " + dataGroup.getName() + "\n");
 				instance.textArea.append("Group: " + dataGroup.getGroup()+ "\n");
 				instance.textArea.append("Admin: " + dataGroup.getAdmin() + "\n");
 				instance.textArea.append("Prefix: " + dataGroup.getPrefix() + "\n");
 				instance.textArea.append("Commands: " + dataGroup.getCommands() + "\n");
 				instance.textArea.append("Inherited: " + dataGroup.getInherited() + "\n");
 				instance.textArea.append("IP: " + dataGroup.getIP() + "\n");
 				instance.textArea.append("\n");
 				instance.textArea.append("Validity: " + dataGroup.getValidity() + "\n");
 				instance.textArea.append("\n\n");
 				instance.textArea.append("--\n");
 				instance.textArea.append("\n\n");
 			}// end for
 			instance.textArea.setEditable(false);
 		} else if (event.getSource().equals(instance.menuItem_Help)){
 			JOptionPane.showMessageDialog(null, "Enter text directly in the text area. \nAfter a read you must click Text - Clear Text to be able to edit the text box again.");
 		} else if (event.getSource().equals(instance.menuItem_About)){
			JOptionPane.showMessageDialog(null, "CanarySyntaxChecker v:1.2 \n\nAuthor: WWOL (Brian McCarthy) \n");
 		} else if (event.getSource().equals(instance.menuItem_Change)){
			JOptionPane.showMessageDialog(null, "Changelog:\n1.2 - \nFixing bugs, \nReading allready read results, \nAdding a line to results,\n1.1 - \nText area editing, \nReading with no text check, \nHelp, About and Change Log menu items, \nFixed spelling, \n\n1.0 - \nInitial release,");
 		}
 	}// end actionPerformed
 }// end CanarySyntaxCheckerListener
