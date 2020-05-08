 package net.robbytu.computercraft.gui;
 
 import net.robbytu.computercraft.CCMain;
 
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class ComputerBlockGUI {
 	private GenericTextField output;
 	private GenericTexture bg;
 	private GenericButton sendButton;
 	public GenericTextField input;
 	public String inputBuffer;
 	
 	public ComputerBlockGUI(int computerID) {
 		this.inputBuffer = "";
 		
 		this.bg = new GenericTexture("http://robbytu.net/spout/computercraft/resources/GUIBackground.png");
 		this.bg.setX(40).setY(1);
 		this.bg.setWidth(352).setHeight(308);
 		this.bg.setPriority(RenderPriority.High);
 		
 		this.output = new GenericTextField();
 		this.output.setWidth(325).setHeight(180);
 		this.output.setX(52).setY(12);
 		this.output.setMaximumLines(1024000);
 		this.output.setMaximumCharacters(1024);
 		this.output.setEnabled(false);
 		this.output.setPriority(RenderPriority.Lowest);
 		
 		this.input = new GenericTextField();
 		this.input.setX(52).setY(195);
 		this.input.setWidth(280).setHeight(15);
 		this.input.setMaximumCharacters(1024); // Doesn't seem to be working, SpoutAPI bug?
 		this.input.setPriority(RenderPriority.Lowest);
 		this.input.setEnabled(false);
 		
 		this.sendButton = new SendButton(this);
 		this.sendButton.setWidth(40).setHeight(15);
 		this.sendButton.setX(52+285).setY(195); // Sorry, really bad at math.. :$
 		this.sendButton.setText("OK");
 		this.sendButton.setPriority(RenderPriority.Lowest);
 	}
 	
 	public void addEntry(String text) {
 		
 		// Shitty work-around comin'!
 		String newOutput1 = "";
 		String newOutput2 = "";
 		
		// More shitty work-around comin'! YAY!
		// TODO: Need to make this let the color "spill" over if it breaks down the lines into multiple lines
		int maxOutputLength = 65; // TODO: play with this number to find the true max length
		if (text.length() > maxOutputLength)  {
			String tempText = text;
			
			if(!this.output.getText().isEmpty()) {
				newOutput1 = this.output.getText() + "\n";
			}
			
			while (tempText.length() > 0) {
				if (tempText.length() <= maxOutputLength) {
					newOutput1 += tempText;
					break;
				}
				else {
					newOutput1 += tempText.substring(0, maxOutputLength) + "\n";
					tempText = tempText.substring(maxOutputLength);
				}
			}
		}
		else {
			if(this.output.getText() == "") newOutput1 = text;
			else newOutput1 = this.output.getText() + "\n" + text;
		}
		// End of more shitty work-around
 		
 		if(newOutput1.split("\n").length == 16) {
 			for(int i = 1; i < 16; i++) {
 				if(newOutput2 != "") {
 					newOutput2 = newOutput2 + "\n" + newOutput1.split("\n")[i];
 				}
 				else newOutput2 = newOutput2 + newOutput1.split("\n")[i];
 			}
 		}
 		else {
 			newOutput2 = newOutput1;
 		}
 		
 		// End of shitty work-around :)
 		
 		this.output.setText(newOutput2);
 		
 		this.output.setDirty(true);
 	}
 	
 	public void attachToScreen(SpoutPlayer player) {
 		GenericPopup popup = new GenericPopup();
 		player.getMainScreen().attachPopupScreen(popup);
 		popup.attachWidgets(CCMain.instance, this.bg, this.output, this.input, this.sendButton);
 	}
 	
 	public void clearConsole() {
 		this.output.setText("");
 	}
 	
 	public void sendInputToScript() {
 		this.inputBuffer = this.input.getText();
 		this.input.setText("");
 	}
 }
