 package client;
 
 /* Author: CS2212 Group 2
  * File Name: StandingsScreen.java
  * Date: 25/01/2012
  * Project: UWOSurvivorPool
  * Course: CS2212b
  * Description:
  * */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Vector;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.rim.device.api.system.Bitmap;
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
 	private RichList list;
 
 	private final static String DEFAULT_IMAGE = "res/test/defaultpic.png";
 	
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
 		
 		Contestant[] contArray = new Contestant[contList.size()];
 		contList.copyInto(contArray);
 		ocfActiveContestant = new ObjectChoiceField(" Cast your "+ voteType + " vote: ",contArray);
 		
 		
 		System.out.println("PickScreen: adding contestants to table");
 		
 		//TODO: change to true after images are implemented
 		list = new RichList(vertFieldManager, true, 3, 0);
 
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
 			Bitmap imgContestant = getImage(cont.getPicture());
 			list.add(new Object[] {imgContestant,lblContName, labelContTribe,labelTempStatus });
 			
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
 
 		horFieldManager.add(btnVoted);
 		horFieldManager.add(ocfActiveContestant);
 		horFieldManager.setFont(font2);
 		
 		
 		this.setTitle(horFieldManager);
 		this.add(vertFieldManager);
 		this.setStatus(Common.getToolbar());
 		vertFieldManager.setFocus(); //THIS NEEDS TO BE HERE. APP CRASHES WITHOUT IT
 		System.out.println("PickScreen: Constructor end");
 	}
 
 	public boolean onSavePrompt() {
 		return true;
 	}
 
 	public void fieldChanged(Field arg0, int arg1) {
 		if (arg0 == btnVoted) { // if the okay button is clicked
 			if (voteType.equals("weekly")) {
 				GameData.getCurrentGame().getCurrentUser()
 						.setWeeklyPick(getChosenContestant());
 			} else if (voteType.equals("ultimate")) {
 				GameData.getCurrentGame().getCurrentUser()
 						.setUltimatePick(getChosenContestant());
 			} else if (voteType.equals("final")) {
 				GameData.getCurrentGame().getCurrentUser()
 						.setWeeklyPick(getChosenContestant());
 			}
 		}
 	}
 
 	/**
 	 * Gets the contestant from the drop down box or null if none chosen
 	 * 
 	 * @return
 	 */
 	private Contestant getChosenContestant() {
 		int i = ocfActiveContestant.getSelectedIndex();
		System.out.println("**********:" + i);
 		if (i == -1)
 			return null;
 
 		Contestant contestant = (Contestant) ocfActiveContestant.getChoice(i);
 		System.out.println(contestant.getID());
 		return contestant;
 	}
 
 	// original from
 	// http://supportforums.blackberry.com/t5/Java-Development/How-to-read-display-image-from-SD-card-on-Device/m-p/621671#M129277
 
 	public Bitmap getImage(String path) {
 		StringBuffer sb = new StringBuffer(1024);
 		sb.append("file:///SDCard/").append(path);
 		String filename = sb.toString();
 		sb = null;
 		Bitmap image = null;
 
 		FileConnection fc = null;
 		InputStream input = null;
 		try {
 			fc = (FileConnection) Connector
 					.open(filename, Connector.READ_WRITE);
 			if (fc.exists()) {
 				input = fc.openInputStream();
 				byte[] data = new byte[(int) fc.fileSize()];
 				input.read(data);
 				image = Bitmap.createBitmapFromPNG(data, 0, data.length);
 			}
 		} catch (Exception e) {
 			System.out.println("MLS:" + e);
 		} finally {
 			if (input != null) {
 				try {
 					input.close();
 				} catch (IOException e) {
 				}
 			}
 			if (fc != null) {
 				try {
 					fc.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 
 		if (image == null) {
 			System.out.println("image not found");
 			return getImage(DEFAULT_IMAGE);//TODO: possible recursive loop
 		}
 		return image;
 	}
 
 }
