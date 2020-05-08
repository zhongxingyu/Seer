 import se.lth.cs.ptdc.window.SimpleWindow;
 
 public class Turtle {
 	private double x; // Sköldpaddans position i x-led
 	private double y; // Sköldpaddans position i y-led
 	private boolean drawing = false; // om Sköldpaddan ritas eller inte 
 	private double direction = Math.PI / 2; // Sköldpaddans riktning i radianer
 	SimpleWindow w; // Fönstret som sköldpaddan ska ritas i
 
 	/**
 	 * Skapar en sköldpadda som ritar i ritfönstret w. Från början befinner sig
 	 * sköldpaddan i punkten x,y med pennan lyft och huvudet pekande rakt uppåt
 	 * i fönstret (i negativ y-riktning)
 	 */
 	public Turtle(SimpleWindow w, int x, int y) {
 		this.w = w;
 		this.x = x;
 		this.y = y;
 	}
 
 	/** Sänker pennan */
 	public void penDown() {
 		drawing = true;
 	}
 
 	/** Lyfter pennan */
 	public void penUp() {
 		drawing = false;
 	}
 
 	/** Går rakt framåt n pixlar i den riktning som huvudet pekar */
 	public void forward(int n) {
		if(drawing)
			w.moveTo((int)Math.round(x), (int)Math.round(y));
 		x = x + n * Math.cos(direction);
 		y = y - n * Math.sin(direction);
		if(drawing)
 			w.lineTo((int)Math.round(x), (int)Math.round(y));
 	}
 
 	/** Vrider beta grader åt vänster runt pennan */
 	public void left(int beta) {
 		direction += Math.toRadians(beta);
 	}
 
 	/** Går till punkten newX,newY utan att rita. Pennans läge (sänkt 
 	    eller lyft) och huvudets riktning påverkas inte */
 	public void jumpTo(int newX, int newY) {
 		x = newX;
 		y = newY;
 		w.moveTo(newX, newY);
 	}
 
 	/** Återställer huvudriktningen till den ursprungliga */
 	public void turnNorth() {
 		direction = Math.PI / 2;
 	}
 
 	/** Tar reda på x-koordinaten för sköldpaddans aktuella position */
 	public int getX() {
 		return (int)x;
 	}
 
 	/** Tar reda på y-koordinaten för sköldpaddans aktuella position */
 	public int getY() {
 		return (int)y;
 	}
 
 	/** Tar reda på sköldpaddans riktning, i grader från positiv x-led */
 	public int getDirection() {
 		return ((int)Math.toDegrees(direction)) % 360;
 	}
 }
