 package gfx;
 
 import java.awt.Dimension;
 
 import gfx.GBC.Align;
 import gfx.Labels;
 
 import blueMaggot.Game;
 
 public class MenuAbout extends Menu {
 
 	int x, y;
 
 	public MenuAbout(Game game) {
 
 		setVisible(false);
		add(new MenuLabel(Labels.WELCOME), new GBC(x, y, null).setInsets(5, 15, 5, 5).setIpad(0, 40));
		add(new MenuLabel(Labels.ABOUT_MSG, new Dimension(480, 300)), new GBC(x, ++y, Align.MID).setWeight(1, 1));
 		add(new MenuButton(Labels.RETURN, this, game), new GBC(x, ++y, Align.MID).setIpad(150, 0));
 	}
 }
