 package dtb;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import dtb.Defend.Views;
 import jgame.ButtonState;
 import jgame.Context;
 import jgame.GButton;
 import jgame.GContainer;
 import jgame.GMessage;
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.ImageCache;
 import jgame.controller.Interpolation;
 import jgame.controller.MovementTween;
 import jgame.listener.ButtonListener;
 import jgame.listener.DelayListener;
 
 public class DefendPauseView extends GContainer {
 	
 	public DefendPauseView() {
 		setSize(1280, 720);
         this.setBackgroundColor(Color.BLACK);
 	}
 	
 	public void viewShown() {
 		 initDefendMenuView();
 	 }
 	
 	public void initDefendMenuView() {
 		removeAllChildren();
 		
 		BufferedImage bg = ImageCache.forClass(Defend.class).get("Other/MenuScreen.png");
 		GSprite g = new GSprite(bg);
 		setBackgroundSprite(g);
         GButton mbResume = this.createButton(0, "Resume");
         mbResume.setLocation(-100, 500);
         GButton mbMainMenu = createButton(1, "Main Menu");
         mbMainMenu.setLocation(-100, 600);
         ButtonListener blPlay = new ButtonListener() {
                 // goto Source --> Override and Implement
                 @Override
                 public void mouseClicked(Context context) {
                         super.mouseClicked(context);
                         context.setCurrentGameView(Views.GAME);
                 }
          };
          
         ButtonListener blInstructions = new ButtonListener() {
        	  @Override
        	  public void mouseClicked(Context context) {
        		  super.mouseClicked(context);
       		  Defend.setPaused(false);
        		  context.setCurrentGameView(Views.MENU);
        	  }
         };
         
         mbResume.addListener(blPlay);
         mbMainMenu.addListener(blInstructions);
         
         BufferedImage dtb = ImageCache.forClass(Defend.class).get("Other/dtb.png");
         GSprite dtbspr = new GSprite(dtb);
         addAt(dtbspr, 1280/2, 80);
 	}
 	
 	private GButton createButton(final int buttonIndex, String buttonText) {
         
         MovementTween mt = new MovementTween(24, Interpolation.EASE, 800, 0);
         MovementTween mtb = new MovementTween(6, Interpolation.EASE, -40, 0);
         mt.chain(mtb);
         final GButton btn = new GButton();
         btn.addController(mt);
         btn.setStateSprite(ButtonState.NONE, createButtonSprite("Tiles/menubarnew.png"));
         btn.setStateSprite(ButtonState.HOVERED, createButtonSprite("Tiles/selectnew.png"));
         btn.setStateSprite(ButtonState.PRESSED, createButtonSprite("Tiles/pressedbarnew.png"));
         btn.setSize(250, 55);
         GMessage gm = new GMessage(buttonText);
         
         gm.setSize(btn.getWidth(), btn.getHeight());
         gm.setAlignmentX(0.5);
         gm.setAlignmentY(0.5);
         gm.setFontSize(28);
         gm.setColor(Color.WHITE);
         btn.addAtCenter(gm);
         
         DelayListener dl = new DelayListener(buttonIndex * 10) {
                 
                 @Override
                 public void invoke(GObject target, Context context) {
                         add(btn);
                 }
         };
         addListener(dl);
         return btn;
 	}
 	
 	public static GSprite createButtonSprite(String fn) {
         BufferedImage img = ImageCache.forClass(Defend.class).get(fn);
         GSprite gs = new GSprite(img);
 
         Rectangle nineSliceCenter = new Rectangle(15, 15, 6, 6);
         gs.setNineSliceCenter(nineSliceCenter);
         return gs;
 	}
 }
