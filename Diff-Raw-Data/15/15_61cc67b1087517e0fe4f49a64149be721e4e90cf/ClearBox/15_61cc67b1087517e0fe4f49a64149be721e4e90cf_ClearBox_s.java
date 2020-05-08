 
 public class ClearBox extends Box{
 	
	private double width;
	private double height;
	
 	public ClearBox(double width, double height) {
 		super('*',' ',width,height);
		this.width = width;
		this.height = height;
 	}
 	
 	public double getRatio(){
 		return width/height;
 	}
 }
