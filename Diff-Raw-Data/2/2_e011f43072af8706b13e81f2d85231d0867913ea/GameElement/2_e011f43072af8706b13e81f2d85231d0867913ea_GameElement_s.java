 /**
  * 
  */
 package dungeonCrawler;
 
 import java.util.EnumSet;
 
 /**
  * @author Mattes, Tissen
  *
  */
 public abstract class GameElement implements Drawable, GameListener {
 
 	public EnumSet<ElementType> type;
 	private String name;
 	protected Vector2d position;
 	protected Vector2d size;
 
 	/**
 	 * 
 	 */
 	public GameElement(Vector2d position, Vector2d size) {
 		this.type = EnumSet.of(ElementType.IMMOVABLE);
 		this.position = position;
 		this.size = size;
 		this.name = "";
 	}
 
 	public GameElement(Vector2d position, Vector2d size, String name, EnumSet<ElementType> type) {
 		this.type = type;
 		this.position = position;
 		this.size = size;
 		this.name = name;
 	}
 
 	/*public GameElement() {
 		this.type = EnumSet.of(ElementType.IMMOVABLE);
 		this.position = new Vector2d();
 		this.size = new Vector2d();
 		this.name = "";
 	}*/
 
 	public Vector2d getPosition() {
 		return position;
 	}
 
 	public Vector2d getSize() {
 		return size;
 	}
 
 	private Vector2d getTopLeft() {
 		return new Vector2d(position);
 	}
 
 	private Vector2d getTopRight() {
 		return position.addX(size.getX());
 	}
 
 	private Vector2d getBottomLeft() {
 		return position.addY(size.getY());
 	}
 	
 	private int getLeft(){
 		return this.position.getX();
 	}
 	
 	private int getTop(){
 		return this.position.getY();
 	}
 	
 	private int getRight(){
 		return this.position.getX() + this.size.getX();
 	}
 	
 	private int getBottom(){
 		return this.position.getY() + this.size.getY();
 	}
 	
 	public boolean isInnerPoint(Vector2d point){
 		if(this.getLeft() < point.getX() && this.getRight() > point.getX() &&
 				this.getTop() < point.getY() && this.getBottom() > point.getY())
 			return true;
 		return false;
 	}
 
 	public boolean collision(GameElement element) {
 		if(element != this){ //TODO kollidiert nicht richtig mit W�nden
 			int xl 	= 	this	.getTopLeft().getX();
 			int xr 	= 	this	.getTopRight().getX();
 			int yt 	= 	this	.getTopLeft().getY();
 			int yb 	= 	this	.getBottomLeft().getY();
 			int xel = 	element	.getTopLeft().getX();
 			int xer = 	element	.getTopRight().getX();
 			int yet = 	element	.getTopLeft().getY();
 			int yeb = 	element	.getBottomLeft().getY();	
 			
 			if (xl<xer && xr>xer && yt<yeb && (yb>yet)){					//Kollision 	rechts 	am element 
 				return true;
 			}
 			
 			else if (xr>xel && xl<xel && yt<yeb && (yb>yet)){			//Kollision 	links 	am element 
 				return true;
 			}
 		
 			else if (yt<yeb && yb>yeb && xr>xel && xl<xer){				//Kollision 	unten 	am element 
 				return true;
 			}	
 			
 			else if (yb>yet && yt<yet && xr>xel && xl<xer){				//Kollision 	oben 	am element 
 				return true;
 			}
			else if (xl>xel && xr<xer && yt> yet && yb<yeb){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setPosition(Vector2d pos) {
 		this.position = pos;
 	}
 
 	/* (non-Javadoc)
 	 * @see dungeonCrawler.Drawable#draw()
 	 */
 	/*@Override
 	public void draw() {
 		// TODO Auto-generated method stub
 
 	}*/
 
 }
