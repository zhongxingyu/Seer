 package environment;
 
 import graphics.IDrawable;
 import graphics.opengl.IDrawableGL;
 
 import java.awt.Graphics2D;
 import java.util.LinkedList;
 import java.util.List;
 
 import structure.Organism;
 import structure.OrganismFactory;
 
 public class Environment implements IDrawable, IDrawableGL {
 	
 	public static final double GRAVITY = 0.1;
 	public static final double MOUSE_CONSTANT = 0.1;
 	public static final double FRICTION = 0.1;
 	public static final double VISCOSITY = 0.004;
 
 	public List<Organism> organisms;
 	private int width, height;
 	
 	private int[] mouse_in;
 	private int[] mouse_buttons;
 	private int mousex, mousey;
 	private boolean spaceIsPressed;
 	
 	public Environment(int w, int h){
 		organisms = new LinkedList<Organism>();
 		// DEBUGGING
		organisms.add(OrganismFactory.testDummy(OrganismFactory.SIMPLE_JELLYFISH,this));
 //		organisms.add(OrganismFactory.testDummy(OrganismFactory.SIMPLE_JELLYFISH,this));
 //		organisms.add(OrganismFactory.testDummy(OrganismFactory.GENE_TEST, this));
 		for(int i = 0; i < 20; i++)
 			organisms.add(OrganismFactory.testDummy(OrganismFactory.POINT_MASS, this));
 		
 		width = w;
 		height = h;
 	}
 	
 	public void update(double dt){
 		mousex = mouse_in[0];
 		mousey = mouse_in[1];
 	
 		dt /= 100.0;
 		for(Organism o : organisms){
 //			o.drift(0, -GRAVITY);
 			o.physicsUpdate();
 			o.contain(this);
 			
 			 if(mouse_buttons[0] != 0) {
 			 	double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
 //			 	o.drift((mousex - o.getX()) / dist, (mousey - o.getY())/ dist);
 			 	o.getPoints().get(0).addForce(MOUSE_CONSTANT*(mousex - o.getX()) / dist, MOUSE_CONSTANT*(mousey - o.getY())/ dist);
 			 	System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
 			 }
 			 
 			 try{
 			 if(spaceIsPressed)
 				 o.getFirstMuscle().setStrength(-1);
 			 else
 				 o.getFirstMuscle().setStrength(0.2);
 			 } catch(Exception e){}
 		}
 		
 		for(int i = 0; i < organisms.size(); i++) {
 			for(int j = i+1; j < organisms.size(); j++) {
 				organisms.get(i).doCollisions(organisms.get(j));
 			}
 		}
 		
 		for(Organism o : organisms) {
 			o.move(dt > 1.0 ? 1.0 : dt);
 		}
 	}
 
 	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
 		for(Organism o : organisms)
 			o.draw(g, sx, sy, scx, scy);
 	}
 	
 	public void glDraw() {
 		// TODO - draw some sort of background?
 		for(Organism o : organisms)
 			o.glDraw();
 	}
 	
 	/**
 	 * get boundaries of this environment
 	 * @return double array [xmin, ymin, xmax, ymax] of environment's bounding area
 	 */
 	public double[] getBounds(){
 		return new double[] {-width/2, -height/2, width/2, height/2};
 	}
 	
 	public void mouse_move(int mx, int my){
 		mouse_buttons[0] = 1;
 		mousex = mx;
 		mousey = my;
 	}
 	
 	public void space_press(boolean isPressed) {
 		spaceIsPressed = isPressed;
 	}
 
 	public void bindInput(int[] mouse_buttons, int[] mouse_move) {
 		this.mouse_buttons = mouse_buttons;
 		this.mouse_in = mouse_move;
 	}
 
 }
