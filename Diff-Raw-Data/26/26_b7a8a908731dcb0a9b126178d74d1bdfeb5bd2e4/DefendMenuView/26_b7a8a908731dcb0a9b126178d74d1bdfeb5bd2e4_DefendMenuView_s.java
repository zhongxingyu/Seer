 package dtb;
 import jgame.GContainer;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import dtb.Defend;
 import dtb.Defend.Views;
 import jgame.ButtonState;
 import jgame.Context;
 import jgame.GButton;
 import jgame.GMessage;
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.ImageCache;
 import jgame.controller.Interpolation;
 import jgame.controller.MovementTween;
 import jgame.controller.RotationTween;
 import jgame.controller.ScaleTween;
 import jgame.listener.ButtonListener;
 import jgame.listener.DelayListener;
 
 public class DefendMenuView extends GContainer {
 	 public DefendMenuView() {
          setSize(900, 700);
          this.setBackgroundColor(Color.BLACK);
          this.setBackgroundSprite(ImageCache.getSprite("other/MenuScreen.png"));
          //Instead of new GButton();
          GButton mbPlay = this.createButton(0, "Play");
          mbPlay.setLocation(-100, 200);
 //         add(mbPlay);
          GButton mbInstructions = createButton(1, "Instructions");
          mbInstructions.setLocation(-100, 300);
 //         add(mbInstructions);
          GButton mbQuit = this.createButton(2, "Quit");
          mbPlay.setLocation(-100, 200);
 
          ButtonListener bl = new ButtonListener() {
                  // goto Source --> Override and Implement
                  @Override
                  public void mouseClicked(Context context) {
                          super.mouseClicked(context);
                          context.setCurrentGameView(Views.GAME);
                  }
 
          };
          mbPlay.addListener(bl);
 
  }
 
  private GButton createButton(final int buttonIndex, String buttonText) {
          
         MovementTween mt = new MovementTween(24, Interpolation.EASE_IN,  400, 0);
         MovementTween mtb = new MovementTween(6, Interpolation.EASE_OUT, -40,
                         0);
         //RotationTween rt = new RotationTween(12, 0, 360);
         //ScaleTween st = new ScaleTween(10, 1, 5);
         //ScaleTween st2 = new ScaleTween(10, 5, 1);
          mt.chain(mtb);
         //rt.with(st);
         //st.chain(st2);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          );
          final GButton btn = new GButton();
          btn.addController(mt);
         btn.setStateSprite(ButtonState.NONE,
                         createButtonSprite("Tiles/menubarnew.png"));
         btn.setStateSprite(ButtonState.HOVERED,
                         createButtonSprite("Tiles/selectnew.png"));
         btn.setStateSprite(ButtonState.PRESSED,
                         createButtonSprite("Tiles/selectnew.png"));
         btn.setSize(250, 50);
          GMessage gm = new GMessage(buttonText);
          
          gm.setSize(btn.getWidth(), btn.getHeight());
          gm.setAlignmentX(0.5);
          gm.setAlignmentY(0.5);
          gm.setFontSize(28);
         gm.setColor(Color.BLUE);
          btn.addAtCenter(gm);
          
          DelayListener dl = new DelayListener(buttonIndex * 10) {
                  
                  @Override
                  public void invoke(GObject target, Context context) {
                          addAt(btn, -100, buttonIndex * 100+200);
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
