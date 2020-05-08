 package screenCore;
 
 import java.util.List;
 
 import common.PlayerCharacteristics;
 
 import graphics.Color;
 import math.Vector2;
 import GUI.controls.Button;
 import GUI.controls.Label;
 import GUI.controls.ListBox;
 import content.ContentManager;
 import utils.TimeSpan;
 
 public class PonySelector extends TransitioningGUIScreen{
 	private ListBox<String> poniesListBox;
 	private List<PlayerCharacteristics> characters;
 
 	public PonySelector(String lookAndFeelPath) {
 		super(false, TimeSpan.fromSeconds(2.0f), TimeSpan.fromSeconds(1.0f), lookAndFeelPath);
 	}
 
 	public void initialize(ContentManager manager) {
 		super.initialize(manager);
 
 		Label myPoniesLabel = new Label();
 		myPoniesLabel.setBounds(50, 50, 200, 50);
 		myPoniesLabel.setText("Select a pony:");
 		this.addGuiControl(myPoniesLabel, new Vector2(-200, 50), new Vector2(50, 50), new Vector2(-200, 50));
 
		//TODO LOAD PONY BUTTONS!
 		
 //		if(characters != null) {
 //			for(int i = 0; i < 3; i++) {
 //				Button b = new Button();
 //				b.setImage(null);//PONYIMAGE
 //				b.setText(characters.get(i).name);
 //				
 //				switch(i) {
 //				case 0:
 //					b.setBounds(50, 100, 250, 250);	
 //				case 1:
 //					b.setBounds(250, 100, 250, 250);
 //				case 2:
 //					b.setBounds(450, 100, 250, 250);
 //				}	
 //			}
 //		}
 	}
 }
