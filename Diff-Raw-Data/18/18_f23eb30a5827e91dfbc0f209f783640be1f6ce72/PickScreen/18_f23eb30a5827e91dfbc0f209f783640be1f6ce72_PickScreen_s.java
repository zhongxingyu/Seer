 package client;
 
 /* Author: CS2212 Group 2
  * File Name: StandingsScreen.java
  * Date: 25/01/2012
  * Project: UWOSurvivorPool
  * Course: CS2212b
  * Description:
  * */
 
 import java.util.Vector;
 
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.FontFamily;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.component.table.RichList;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import client.data.GameData;
 import data.Contestant;
 
 public class PickScreen extends MainScreen implements FieldChangeListener {
 	/* Variables */
 	private LabelField lblContName, labelContTribe, labelTempStatus; // various
 																		// labels.
 	private String voteType;
 	private FontFamily ff1; // fonts.
 	private Font font2; // fonts.
 	private ButtonField btnVoted;
 	private ObjectChoiceField ocfActiveContestant;
 	private String[] choices;
 	private RichList list;	
 	
 	public PickScreen(String voteType) {
 		super();
 		System.out.println("PickScreen constructor");
 		this.voteType = voteType;
 
 		VerticalFieldManager vertFieldManager = new VerticalFieldManager(
 				VerticalFieldManager.USE_ALL_WIDTH
 						| VerticalFieldManager.VERTICAL_SCROLLBAR) {
 			// Override the paint method to draw the background image.
 			public void paint(Graphics graphics) {
 				graphics.setColor(Color.BLACK);
 				graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
 				super.paint(graphics);
 			}
 		};
 		;
 
 		setToolbar(Common.getToolbar());
 
 		try { // set up the smaller list font
 			ff1 = FontFamily.forName("Verdana");
 			font2 = ff1.getFont(Font.BOLD, 20);
 		} catch (final ClassNotFoundException cnfe) {
 			UiApplication.getUiApplication().invokeLater(new Runnable() {
 				public void run() {
 					Dialog.alert("FontFamily.forName() threw "
 							+ cnfe.toString());
 				}
 			});
 		}
 		
		Vector contList = GameData.getCurrentGame().getActiveContestants();		
 		String[] choices = new String[contList.size()];
 		//get all active contestants for drop down box
 		for(int i =0;i<contList.size();i++){
 			Contestant c = (Contestant) contList.elementAt(i);
 			choices[i]=c.getFirstName()+ " "+c.getLastName()+" "+" ID: "+c.getID();
 		}
 		
 		System.out.println("PickScreen: adding contestants to table");
 		
 		//TODO: change to true after images are implemented
 		list = new RichList(vertFieldManager, false, 3, 0);
 
 		//get all contestants for list
 		contList = GameData.getCurrentGame().getAllContestants();
 		for (int i = 0; i < contList.size(); i++) {
 			Contestant cont = (Contestant) contList.elementAt(i);
 			/* list contains labels so that the text colour can change */
 			lblContName = new LabelField(cont.getFirstName() + " " + cont.getLastName(), LabelField.ELLIPSIS) {
 				public void paint(Graphics g) {
 					g.setColor(Color.WHITE);
 					super.paint(g);
 				}
 			};
 			lblContName.setFont(font2);
 
 			labelContTribe = new LabelField(cont.getTribe(), LabelField.ELLIPSIS) {
 				public void paint(Graphics g) {
 					g.setColor(Color.WHITE);
 					super.paint(g);
 				}
 			};
 			lblContName.setFont(font2);
 
 			String tempString = "";
 			
 			if (cont.isCastOff())
 				tempString = "Castoff";
 			else
 				tempString = "Active";
 			
 			labelTempStatus = new LabelField(tempString, LabelField.ELLIPSIS) {
 				public void paint(Graphics g) {
 					g.setColor(Color.WHITE);
 					super.paint(g);
 				}
 			};
 			lblContName.setFont(font2);
 			list.add(new Object[] {lblContName, labelContTribe,labelTempStatus });
 			
 		}
 		System.out.println("PickScreen: contestants added to list");
 		
 		HorizontalFieldManager horFieldManager = new HorizontalFieldManager(
 				HorizontalFieldManager.USE_ALL_WIDTH
 						| HorizontalFieldManager.FIELD_HCENTER) {
 			// Override the paint method to draw the background image.
 			public void paint(Graphics graphics) {
 				graphics.setColor(Color.GREEN);
 				graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
 				super.paint(graphics);
 			}
 		};
 		;
 
 		btnVoted = new ButtonField("Okay");
 		btnVoted.setChangeListener(this);
 
		ocfActiveContestant = new ObjectChoiceField(" Cast your "
				+ voteType + " vote: ",choices, 0,
				ObjectChoiceField.FORCE_SINGLE_LINE
						| ObjectChoiceField.FIELD_HCENTER);
 		horFieldManager.add(btnVoted);
 		horFieldManager.add(ocfActiveContestant);
 		horFieldManager.setFont(font2);
 		
		/* Build the components to MainScreen */
 		
		//ocfActiveContestant.setEditable(false);
 		this.setTitle(horFieldManager);
 		this.add(vertFieldManager);
 		this.setStatus(Common.getToolbar());
 		System.out.println("PickScreen: Constructor end");
 	}
 
 	public void fieldChanged(Field arg0, int arg1) {
 		if (arg0 == btnVoted) { // if the okay button is clicked
 			if (voteType.equals("weekly")) {
 				GameData.getCurrentGame().getCurrentUser().setWeeklyPick(getChosenContestant());
 			} else if (voteType.equals("ultimate")) { 
 				GameData.getCurrentGame().getCurrentUser().setUltimatePick(getChosenContestant());
 			} else if(voteType.equals("final")){ //TODO: are these the same?
 				GameData.getCurrentGame().getCurrentUser().setUltimatePick(getChosenContestant());
 			}
 		}
 	}
 	
 	/**
 	 * Gets the contestant from the drop down box or null if none chosen
 	 * @return
 	 */
 	private Contestant getChosenContestant(){
 		int i =ocfActiveContestant.getSelectedIndex();
 		if(i==-1)
 			return null;
 		
 		String conName = choices[i];
 		
 		//int idLoc = conName.lastIndexOf((int)' ');
 		//String conId = conName.substring(idLoc+1);
 		//return GameData.getCurrentGame().getContestant(conId);
 		return null;
 	
 	}
 
 }
