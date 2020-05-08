 public class FractalSquare {
 	FractalSquare(double targetX, double targetY, double w, double h) {
 		this.targetX = targetX;
 		this.targetY = targetY;
 		this.halfWidth = w / 2.0;
 		this.halfHeight = h / 2.0;
 	}
 	
 	public void half() {
 		this.halfHeight /= 2.0;
 		this.halfWidth /= 2.0;
 	}
 	
 	public void screenToMSetCoord(double x, double y, double w, double h) {
 		//double tmpX = this.fm.getSquare().getMinRe() + this.fm.getSquare().getWidth() * (2 * e.getX() / this.normalSize.getWidth());
 		//double tmpY = -1 * this.fm.getSquare().getMaxRe() + this.fm.getSquare().getHeight() * (2 * e.getY() / this.normalSize.getHeight());
		double tmpX = x / w;
		double tmpY = y / h;
		tmpX *= getWidth();
		tmpY *= getHeight();
		tmpX -= getWidth() / 2;
		tmpY -= getHeight() / 2;
 		tmpX *= 2;
 		tmpY *= -2;
 		
 		System.out.println(tmpX + ", " + tmpY);
 	}
 	
 	// Getters and setters
 	public double getWidth() { return halfWidth * 2; }
 	public double getHeight() { return halfHeight * 2; }
 	public double getMinRe() { return targetX - halfWidth; }
 	public double getMaxRe() { return targetY + halfHeight; }
 	public double getMinIm() { return targetX + halfWidth; }
 	public double getMaxIm() { return targetY - halfHeight; }
 	public void setTarget(double x, double y) { targetX = x; targetY = y; }
 	
 	// Local variables
 	private double halfWidth;
 	private double halfHeight;
 	private double targetX;
 	private double targetY;
 }
