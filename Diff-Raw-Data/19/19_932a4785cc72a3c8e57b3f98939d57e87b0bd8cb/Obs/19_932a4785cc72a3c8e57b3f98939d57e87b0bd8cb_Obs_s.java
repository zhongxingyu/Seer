 package mta13438;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class Obs {
 	private Point pos;
 	private float dx,dz,dy;
 	private MATERIALS material;
 	private Sound loopSound;
	private Boolean emitSound = true;
 	
 	
 	public Obs(){
 		setPos(new Point());
 		setDx(0);
 		setDz(0);
 		setDy(0);
 		setMaterial(MATERIALS.ROCK);
 		setLoopSound(new Sound(SOUNDS.DOOR, new Point(0,0,0), true, true));
 	}
 	
 	public Obs(Point point,float dx,float dy,float dz,MATERIALS material){
 		setPos(point);
 		setDx(dx);
 		setDz(dz);
 		setDy(dy);
 		setMaterial(material);
 		setLoopSound(new Sound(SOUNDS.DOOR, point, true, true));
 	}
 	
 	public Point getCenter(){
 		Point center = new Point(getPos().getX()+(getDx()/2),getPos().getY()+(getDy()/2),0);
 		return center;
 	}
 
 	public Point getPos() {
 		return pos;
 	}
 
 	public void setPos(Point pos) {
 		this.pos = pos;
 	}
 
 	public float getDx() {
 		return dx;
 	}
 
 	public void setDx(float dx) {
 		this.dx = dx;
 	}
 
 	public float getDz() {
 		return dz;
 	}
 
 	public void setDz(float dz) {
 		this.dz = dz;
 	}
 
 	public float getDy() {
 		return dy;
 	}
 
 	public void setDy(float dy) {
 		this.dy = dy;
 	}
 
 	public MATERIALS getMaterial() {
 		return material;
 	}
 
 	public void setMaterial(MATERIALS material) {
 		this.material = material;
 	}
 	
 	public Sound getLoopSound() {
 		return loopSound;
 	}
 
 	public void setLoopSound(Sound loopSound) {
 		this.loopSound = loopSound;
 	}
 
 	public Boolean getEmitSound() {
 		return emitSound;
 	}
 
 	public void setEmitSound(Boolean emitSound) {
 		this.emitSound = emitSound;
 	}
 
 	public void collision(Player player, Level level, int currentRoom){
 	}
 	
 	public void draw() {
 	   	 glColor4f(0.2f, 1.0f, 0.2f, 0.5f);
 	   	 glLineWidth(1.5f);
 	   	 glBegin(GL_LINES);
 	   		 //Bottom Line
 	   		 glVertex2i((int)this.pos.getX(), (int)this.pos.getY());
 	   		 glVertex2i((int)(this.pos.getX()+this.getDx()), (int)this.pos.getY());
 	   		 //Left Line
 	   		 glVertex2i((int)this.pos.getX(), (int)this.pos.getY());
 	   		 glVertex2i((int)this.pos.getX(), (int)(this.pos.getY()+this.getDy()));
 	   		 //Top Line
 	   		 glVertex2i((int)this.pos.getX(), (int)(this.pos.getY()+this.getDy()));
 	   		 glVertex2i((int)(this.pos.getX()+this.getDx()), (int)(this.pos.getY()+this.getDy()));
 	   		 //Right Line
 	   		 glVertex2i((int)(this.pos.getX()+this.getDx()), (int)(this.pos.getY()+this.getDy()));
 	   		 glVertex2i((int)(this.pos.getX()+this.getDx()), (int)this.pos.getY());
 	   	 glEnd();
 	}
 
 	@Override
 	public String toString() {
 		return "Obs [pos=" + pos + ", dx=" + dx + ", dz=" + dz
 				+ ", lenght=" + dy + ", material=" + material + "]";
 	}
 	
 	/*public boolean equals(Object obj){
 		if (obj == null) return false;
 		if (!(obj instanceof Obs)) return false;
 		Obs obs = (Obs)obj;
 		if (!getPos().equals(obs.getPos())) return false;
 		if (!(getDx() == obs.getDx())) return false;
 		if (!(getDz() == obs.getDz())) return false;
 		if (!(getLenght() == obs.getLenght())) return false;
 		if (!(getSabins() == obs.getSabins())) return false;
 		return true;
 	}
 	
 	public int compareTo(Obs obs){
 		if (obs == null) return 1;
 		if (this.equals(obs)) return 0;
 		if (getLenght() > obs.getLenght()) return 1;
 		if (getLenght() < obs.getLenght()) return -1;
 		if (getDz() > obs.getDz()) return 1;
 		if (getDz() < obs.getDz()) return -1;
 		if (getDx() > obs.getDx()) return 1;
 		if (getDx() < obs.getDx()) return -1;
 		if (getPos().compareTo(obs.getPos()) > 0) return 1;
 		if (getPos().compareTo(obs.getPos()) < 0) return -1;
 		if (getSabins() > obs.getSabins()) return 1;
 		return -1;
 	}*/
 }
