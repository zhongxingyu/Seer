 package org.fruct.oss.russianriddles;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.StringItem;
 
 import org.fruct.oss.russianriddles.elements.*;
 
 public class Menu extends Form implements CommandListener{
 	
     private static final int LIST_ROW_HEIGHT = 40;
     
     private MenuItem gameItem;
     private MenuItem helpItem;
     private MenuItem aboutItem;
     
     private StringItem result;
 
     public DisplayManager manager;
 	private Command	 back;
     
     AboutForm aboutForm = null;
     
     HelpForm helpForm = null;
     
     GameForm gameForm = null;
 	
 	public Menu(Display displ) {
 		super (" ");
 
 		this.manager = new DisplayManager(displ);
 		gameItem = new MenuItem(" ", this.getWidth(), LIST_ROW_HEIGHT);
 		gameItem.setMenu(this);
 		append(gameItem);
 		helpItem = new MenuItem("", this.getWidth(), LIST_ROW_HEIGHT);
 		helpItem.setMenu(this);
 		append(helpItem);
 		aboutItem = new MenuItem(" ", this.getWidth(), LIST_ROW_HEIGHT);
 		aboutItem.setMenu(this);
 		append(aboutItem);
 		
 		result = new StringItem(null, null);
 		append(result);
 		
 		this.back = new Command("Back", Command.BACK, 1);
 		this.manager.add(this);
 	}
 	
 	public void itemClicked(MenuItem itm) {
 		if (itm == gameItem) {
 			System.err.println("Start game");
 			if (gameForm == null) {
 				gameForm = new GameForm(this);
 				gameForm.setCommandListener(this);
 				gameForm.addCommand(this.back);
 			}
 			this.manager.next(gameForm);
 			gameForm.startGame();
 		}else if (itm == helpItem) {
 			if (helpForm == null){
 				helpForm = new HelpForm();
 				helpForm.setCommandListener(this);
 				helpForm.addCommand(this.back);
 			}
 			this.manager.next(helpForm);
 		}else {
 			if (aboutForm == null){
 				aboutForm = new AboutForm();
 				aboutForm.setCommandListener(this);
 				aboutForm.addCommand(this.back);
 			}
 			this.manager.next(aboutForm);
 		}
 			
 	}
 
 	public void commandAction(Command c, Displayable d) {
 		if (c == this.back) {
 			this.manager.back();
 			if (gameForm != null)
 				gameForm.stopGame();
 		}
 
 	}
 	
 	public void showResult() {
		this.result.setText("\n    \"" + gameForm.solution + "\" (\"" + gameForm.solutionTransl + "\").   " +gameForm.numSuccessQuestions + " .");
 	}
 	
 	public void hideResult() {
 		this.result.setText(null);
 	}
 }
