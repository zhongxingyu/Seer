 package haven;
 
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.image.BufferedImage;
 
 public class Button extends SSWidget {
 	static BufferedImage bl = Resource.loadimg("gfx/hud/buttons/tbtn/left");
 	static BufferedImage br = Resource.loadimg("gfx/hud/buttons/tbtn/right");
 	static BufferedImage bt = Resource.loadimg("gfx/hud/buttons/tbtn/top");
 	static BufferedImage bb = Resource.loadimg("gfx/hud/buttons/tbtn/bottom");
 	static BufferedImage dt = Resource.loadimg("gfx/hud/buttons/tbtn/dtex");
 	static BufferedImage ut = Resource.loadimg("gfx/hud/buttons/tbtn/utex");
 	public Text text;
 	public BufferedImage cont;
 	static Text.Foundry tf = new Text.Foundry(new Font("Serif", Font.PLAIN, 12), Color.YELLOW);
 	boolean a = false;
 	
 	static {
 		Widget.addtype("btn", new WidgetFactory() {
 			public Widget create(Coord c, Widget parent, Object[] args) {
 				return(new Button(c, (Integer)args[0], parent, (String)args[1]));
 			}
 		});
 		Widget.addtype("ltbtn", new WidgetFactory() {
 			public Widget create(Coord c, Widget parent, Object[] args) {
 				return(wrapped(c, (Integer)args[0], parent, (String)args[1]));
 			}
 		});
 	}
 	
         public static Button wrapped(Coord c, int w, Widget parent, String text) {
                 Button ret = new Button(c, w, parent, tf.renderwrap(text, w - 10));
                 return(ret);
         }
         
 	public Button(Coord c, Integer w, Widget parent, String text) {
 		super(c, new Coord(w, 19), parent);
 		this.text = tf.render(text);
 		this.cont = this.text.img;
 		render();
 	}
         
 	public Button(Coord c, Integer w, Widget parent, Text text) {
 		super(c, new Coord(w, 19), parent);
 		this.text = text;
 		this.cont = text.img;
 		render();
 	}
 	
 	public Button(Coord c, Integer w, Widget parent, BufferedImage cont) {
 		super(c, new Coord(w, 19), parent);
		this.cont = cont;
 		render();
 	}
 	
 	public void render() {
 		Graphics g = graphics();
 		g.drawImage(a?dt:ut, 3, 3, sz.x - 6, 13, null);
 		g.drawImage(bl, 0, 0, null);
 		g.drawImage(br, sz.x - br.getWidth(), 0, null);
 		g.drawImage(bt, 3, 0, sz.x - 6, bt.getHeight(), null);
 		g.drawImage(bb, 3, sz.y - bb.getHeight(), sz.x - 6, bb.getHeight(), null);
 		Coord tc = sz.div(2).add(Utils.imgsz(cont).div(2).inv());
 		if(a)
 			tc = tc.add(1, 1);
 		g.drawImage(cont, tc.x, tc.y, null);
 		update();
 	}
 	
 	public void click() {
 		wdgmsg("activate");
 	}
 	
 	public boolean mousedown(Coord c, int button) {
 		if(button != 1)
 			return(false);
 		a = true;
 		render();
 		ui.grabmouse(this);
 		return(true);
 	}
 	
 	public boolean mouseup(Coord c, int button) {
 		if(a && button == 1) {
 			a = false;
 			render();
 			ui.grabmouse(null);
 			if(c.isect(new Coord(0, 0), sz))
 				click();
 			return(true);
 		}
 		return(false);
 	}
 }
