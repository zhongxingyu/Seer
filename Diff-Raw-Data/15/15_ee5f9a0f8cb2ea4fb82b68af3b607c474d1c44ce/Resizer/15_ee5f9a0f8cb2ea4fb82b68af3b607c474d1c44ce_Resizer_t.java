 package tim.application;
 
 import java.awt.Dimension;
 
 public class Resizer extends CustomObservable {
 	private Dimension dimension;
 	
 	public void setDimension(Dimension dimension) {
 		setChanged();
 		notifyObservers(dimension);
		this.dimension = new Dimension((int)dimension.getSize().getWidth(), (int)dimension.getSize().getHeight()-88);
		//this.dimension = dimension;
 	}
 
 	public Dimension getDimension() {
 		return dimension;
 	}	
 }
