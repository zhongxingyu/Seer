 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 public class Wall extends Obs {
 	
 	//private Sound collisionSound = new Sound(HANDSONWALL,getPos(),10,10);
 	
 	private Wall() {
 		super();
 	}
 	
 	public Wall(Point point,float dx,float dy,float dz,float sabins) {
 		super(point, dx,dy,dz,sabins);
 	}
 	
 	public void collision(){
		//waterSound.play(); only when the player walks.
 	}
 	
 	@Override
 	public String toString() {
 		return "Wall [pos=" + getPos() + ", dx=" + getDx() + ", dz=" + getDz()
 				+ ", lenght=" + getDy() + ", sabins=" + getSabins() + "]";
 	}
 	@Override
 	public void draw(){
 		glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
 	   	 glLineWidth(1.5f);
 	   	 glBegin(GL_LINES);
 	   		 //Bottom Line
 	   		 glVertex2i((int)getPos().getX(), (int)getPos().getY());
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)getPos().getY());
 	   		 //Left Line
 	   		 glVertex2i((int)getPos().getX(), (int)getPos().getY());
 	   		 glVertex2i((int)getPos().getX(), (int)(getPos().getY()+this.getDy()));
 	   		 //Top Line
 	   		 glVertex2i((int)getPos().getX(), (int)(getPos().getY()+this.getDy()));
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)(getPos().getY()+this.getDy()));
 	   		 //Right Line
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)(getPos().getY()+this.getDy()));
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)getPos().getY());
 	   	 glEnd();
 	}
 }
