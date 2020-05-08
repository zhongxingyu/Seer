 package gui.items;
 
 import javax.swing.JMenuItem;
 
import data.Pair;
 import data.Service;
 
 import gui.MainWindow;
 import interfaces.Command;
 import interfaces.Gui;
 
 public class RefuseOfferItem extends GuiAbstractItem implements Command {
 
 	public RefuseOfferItem(MainWindow window, Gui gui) {
 		this.window = window;
 		this.gui = gui;
 
 		addActionListener(window.getActionListener());
 	}
 
 	@Override
 	public void execute() {
 		System.out.println("Refuse Request");
		gui.refuseOffer(new Pair<Service, Integer>(service, row));
 	}
 
 }
