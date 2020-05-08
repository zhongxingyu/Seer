 import processing.core.PApplet;
 import processing.core.PVector;
 
 class BackgroundElement extends GameElement {
 	private int bgcolor;
 
 	public BackgroundElement(PApplet par, String id) {
 		super(par, id);
         position = new PVector(0,0);
         myWidth = parent.width;
         myHeight = parent.height;
         setPriority(0);
 	}
 
 	@Override
 	public void whileUnfocused() {
         myWidth = parent.width;
         myHeight = parent.height;
 		return;
 	}
 
 	@Override
 	public void keyPressed() {
 		switch(parent.key) {
 			case 'f':
 				ForceObject force = new ForceObject(parent, "ballgame.ForceObject#" + ForceObject.getNumber(), new PVector(parent.mouseX, parent.mouseY), 0, 0);
 				break;
 
 			case 'p':
 				Portals portals = new Portals(parent, "ballgame.Portals#" + Portals.getNumber(), new PVector(parent.mouseX, parent.mouseY));
 				break;
 
 			case 'b':
 				GameBall ball = new GameBall(parent, "ballgame.GameBall#" + GameBall.getNumber(), new PVector(parent.mouseX, parent.mouseY), new PVector(0, 0));
 				break;
 
 			case 's':
 				Scatterer scatterer = new Scatterer(parent, "ballgame.Scatterer#" + Scatterer.getNumber(), new PVector(parent.mouseX, parent.mouseY));
 				break;
 
 			case 'd':
 				DrawMenu menu = new DrawMenu(parent, "ballgame.DrawMenu", new PVector(parent.mouseX, parent.mouseY));
 				break;
 
 			case 'q':
 				ElementFactory.spawnElement();
 				break;
 		}
 	}
 	
 	@Override
 	public void mousePressed() {
 		if(parent.mouseButton == parent.LEFT) {
 			ElementFactory.spawnElement();
 		} else if(parent.mouseButton == parent.RIGHT) {
 			DrawMenu menu = new DrawMenu(parent, "ballgame.DrawMenu", new PVector(parent.mouseX, parent.mouseY));
 		}
 	}
 }
