 import java.io.File;
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Shape;
 
 import Entities.Entity;
 import Entities.Player;
 
 
 public class Pathogenum extends BasicGame{
 
 	ArrayList<Image> images;
 	ArrayList<Shape> shapes;
 	ArrayList<Entity> entities;
 	Player play;
 	//TODO Extend world?
 	PathogenumWorld world;
 	int[] keys = new int[4];
 	
 	public Pathogenum(String title) {
 		super(title);
 		images = new ArrayList<Image>();
 		shapes = new ArrayList<Shape>();
 		entities = new ArrayList<Entity>();
 		world = new PathogenumWorld(new Vec2(0f,0f));
 	}
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 		//for(Shape s: shapes){
 		//	arg1.draw(s);
 		//}
 		for(Entity e: entities){
 			e.draw(arg1);
 		}
 	}
 
 	@Override
 	public void init(GameContainer arg0) throws SlickException {
		readImages("resources/gfx/");
 		float[] points = new float[6];
 		points[0] = 0;
 		points[1] = 0;
 		points[2] = 45;
 		points[3] = 0;
 		points[4] = (float)22.5;
 		points[5] = (float)22.5;
 		Polygon p = new Polygon(points);
 		shapes.add(p);
 		play = new Player(0,0,"Player1",p, 100, world);
 		//play = new Player(0,0,"Player1",images.get(0), 100, world);
 		entities.add(play);
 	}
 
 	private void readImages(String dir) throws SlickException {
 		File res_dir = new File(dir);
 		String[] files = res_dir.list();
 		for(String s: files){
 			Image im = new Image(dir+s);
 			images.add(im);
 		}
 	}
 
 	@Override
 	public void update(GameContainer arg0, int arg1) throws SlickException {
 		int[] acc = checkMovementKey();
 		world.step(arg1, 3, 3);
 		play.addForce(acc, arg1);
 	}
 	
 	/**
 	 * @param args
 	 * @throws SlickException 
 	 */
 	public static void main(String[] args) throws SlickException {
 		Pathogenum petriDish = new Pathogenum("Pathogenum");
 		AppGameContainer agc = new AppGameContainer(petriDish);
 		agc.setDisplayMode(1024, 768, false);
 		agc.setTargetFrameRate(60);
 		agc.start();
 	}
 	//Needs to be changed entirely to allow many keys, maybe manipulate key-events with on_press and on_release?
 	private int[] checkMovementKey(){
 		Input i = new Input(0);
 		if(i.isKeyDown(Input.KEY_UP)){
 			System.out.println("UP");
 			keys[0] = 1;
 		} else {
 			keys[0] = 0; 
 		}
 		
 		if(i.isKeyDown(Input.KEY_DOWN)){
 			System.out.println("DOWN");
 			keys[1] = 1;
 		} else {
 			keys[1] = 0; 
 		}
 		if(i.isKeyDown(Input.KEY_LEFT)){
 			System.out.println("LEFT");
 			keys[2] = 1;
 		} else {
 			keys[2] = 0; 
 		}
 		if(i.isKeyDown(Input.KEY_RIGHT)){
 			System.out.println("RIGHT");
 			keys[3] = 1;
 		} else {
 			keys[3] = 0; 
 		}
 		return keys;
 	}
 	
 }
