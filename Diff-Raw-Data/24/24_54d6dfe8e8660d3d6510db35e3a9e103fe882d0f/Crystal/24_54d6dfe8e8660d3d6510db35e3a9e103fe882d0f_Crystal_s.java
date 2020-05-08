 package state;
 
 public class Crystal extends Resource{
 
 	public Crystal(int x, int y, int width, int height,String image) {
 		super(x, y, width, height, image);
 		this.amount = 500;
 	}
 
 	public void harvest() {
 		if(amount <= 10){
 			depleted();
			setImage("/Assets/EvironmentObjects/ResourcesDepleted.png");
 		}
		amount =- 10;
 	}
 
 }
