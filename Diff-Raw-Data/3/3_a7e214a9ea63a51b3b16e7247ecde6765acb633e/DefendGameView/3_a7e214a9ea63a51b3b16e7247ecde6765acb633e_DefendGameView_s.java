 package dtb;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import dtb.Defend.Views;
import jgame.SoundManager;
 import jgame.ButtonState;
 import jgame.Context;
 import jgame.GButton;
 import jgame.GContainer;
 import jgame.GMessage;
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.ImageCache;
 import jgame.controller.Interpolation;
 import jgame.controller.MouseLocationController;
 import jgame.controller.MovementTween;
 import jgame.listener.ButtonListener;
 import jgame.listener.DelayListener;
 import jgame.listener.FrameListener;
 import jgame.listener.LocalClickListener;
 import Mechanics.Bank;
 import Turrets.RangeRing;
 import Turrets.Turret;
 import Turrets.Turret1;
 import Turrets.Turret2;
 import Turrets.Turret3;
 import Turrets.Turret4;
 import Turrets.Turret5;
 import areas.MenuArea;
 import areas.PlayArea;
import dtb.Defend.Views;
 
 public class DefendGameView extends GContainer {
 	private PlayArea pa = new PlayArea();
 	private boolean settingTurret = false;
 	private Bank bank;
 	private static GMessage gm = new GMessage();
 
 	public DefendGameView() {
 
 		setSize(1280, 720);
 		pa.setAnchorTopLeft();
 		add(pa);
 		
 		MenuArea ma = new MenuArea();
 		ma.setAnchorCenter();
 		ma.setLocation(1280 / 2, 720 - 22);
 		add(ma);
 
 		GButton mbMM = this.createButton(0, "Main Menu");
 		mbMM.setLocation(-100, 700);
 
 		ButtonListener blMM = new ButtonListener() {
 			@Override
 			public void mouseClicked(Context context) {
 				super.mouseClicked(context);
 				context.setCurrentGameView(Views.MENU);
 			}
 		};
 		mbMM.addListener(blMM);
 
 		bank = new Bank(500);
 
 		GSprite bankTile = createSprite();
 		addAt(bankTile, 1200, 40);
 
 		FrameListener fl = new FrameListener() {
 			@Override
 			public void invoke(GObject target, Context context) {
 				gm.setText(bank.toString());
 			}
 		};
 		addListener(fl);
 		
 	}
 
 	public static GSprite createSprite() {
 		BufferedImage img = ImageCache.forClass(Defend.class).get(
 				"Tiles/selectnew.png");
 		GSprite gs = new GSprite(img);
 
 		Rectangle nineSliceCenter = new Rectangle(15, 15, 6, 6);
 		gs.setNineSliceCenter(nineSliceCenter);
 		gs.setSize(100, 55);
 
 		gm.setAlignmentX(0.5);
 		gm.setAlignmentY(0.5);
 		gm.setFontSize(28);
 		gm.setColor(Color.WHITE);
 		gs.addAtCenter(gm);
 		return gs;
 	}
 
 	public void initializeTurret(final Turret t) {
 		if (settingTurret) {
 			return;
 		}
 		settingTurret = true;
 		final RangeRing rr = new RangeRing(t.getFireRange());
 		final MouseLocationController mlc = new MouseLocationController();
 		t.addController(mlc);
 		this.pa.addAtCenter(t);
 		this.pa.addAtCenter(rr);
 		rr.addController(mlc);
 		final LocalClickListener lcl = new LocalClickListener() {
 
 			@Override
 			public void invoke(GObject target, Context context) {
 				for (GObject child : pa.getObjects()) {
 					if (child != t && child instanceof Turret
 							&& child.hitTest(t)) {
 						return;
 					}
 				}
 				target.removeController(mlc);
 				target.removeListener(this);
 				rr.removeController(mlc);
 				rr.removeSelf();
 				settingTurret = false;
 				t.setPlaced(true);
 			}
 
 		};
 		t.addListener(lcl);
 	}
 
 	private GButton createButton(final int buttonIndex, String buttonText) {
 
 		MovementTween mt = new MovementTween(24, Interpolation.EASE, 400, 0);
 		MovementTween mtb = new MovementTween(6, Interpolation.EASE, -40, 0);
 		mt.chain(mtb);
 		final GButton btn = new GButton();
 		btn.addController(mt);
 		btn.setStateSprite(ButtonState.NONE,
 				createButtonSprite("Tiles/menubarnew.png"));
 		btn.setStateSprite(ButtonState.HOVERED,
 				createButtonSprite("Tiles/selectnew.png"));
 		btn.setStateSprite(ButtonState.PRESSED,
 				createButtonSprite("Tiles/pressedbarnew.png"));
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
 				addAt(btn, -100, 675);
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
 
 	public boolean turretAfford(int turNum) {
 		boolean bool = false;
 		switch (turNum) {
 		case 1:
 			if (Turret1.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(Turret1.getCost());
 				bool = true;
 			}
 			break;
 
 		case 2:
 			if (Turret2.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(Turret2.getCost());
 				bool = true;
 			}
 			break;
 
 		case 3:
 			if (Turret3.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(Turret3.getCost());
 				bool = true;
 			}
 			break;
 
 		case 4:
 			if (Turret4.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(Turret4.getCost());
 				bool = true;
 			}
 			break;
 
 		case 5:
 			if (Turret5.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(Turret5.getCost());
 				bool = true;
 			}
 			break;
 
 		default:
 			bool = false;
 			break;
 		}
 		return bool;
 	}
 	
 	public void newHUDMessage(String text, int time) {
 		final GMessage messageHUD = new GMessage();
 	
 		messageHUD.setAlignmentX(0.5);
 		messageHUD.setAlignmentY(0.5);
 		messageHUD.setFontSize(28);
 		messageHUD.setColor(Color.WHITE);
 		messageHUD.setAlpha(1);
 		messageHUD.setText(text);
 		addAt(messageHUD, 1280/2, 600);
 		
 		DelayListener dl = new DelayListener(time) {
 			@Override
 			public void invoke(GObject target, Context context) {
 				messageHUD.removeSelf();
 			}
 		};
 		addListener(dl);
 	}
 }
