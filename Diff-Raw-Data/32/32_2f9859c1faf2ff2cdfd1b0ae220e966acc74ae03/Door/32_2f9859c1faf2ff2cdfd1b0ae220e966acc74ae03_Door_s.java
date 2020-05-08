 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 public class Door extends Obs{
 
 	//private Sound collisionSound = new Sound(HANDSONWALL,getPos(),10,10);
 
 	private Door() {
 		super();
 	}
 
 	public Door(Point point,float dx,float dy,float dz,float sabins) {
 		super(point, dx,dy,dz,sabins);
 	}
 
	private void collision(Player player, Level level, int currentRoom){
		player.setPos(level.getRoomList().get(currentRoom+1).exit);
 	}
 
 	@Override
 	public String toString() {
 		return "Door [pos=" + getPos() + ", dx=" + getDx() + ", dz=" + getDz()
 				+ ", lenght=" + getDy() + ", sabins=" + getSabins() + "]";
 	}
 	@Override
 	public void draw(){
 		glColor4f(0.54f, 0.27f, 0.07f, 0.5f);
 		glLineWidth(1f);
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
