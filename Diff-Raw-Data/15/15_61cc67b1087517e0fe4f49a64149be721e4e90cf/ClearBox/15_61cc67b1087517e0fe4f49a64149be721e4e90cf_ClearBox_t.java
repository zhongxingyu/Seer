 
 public class ClearBox extends Box{
 	
 	public ClearBox(double width, double height) {
 		super('*',' ',width,height);
 	}
 	
 	public double getRatio(){
 		return width/height;
 	}
 }
