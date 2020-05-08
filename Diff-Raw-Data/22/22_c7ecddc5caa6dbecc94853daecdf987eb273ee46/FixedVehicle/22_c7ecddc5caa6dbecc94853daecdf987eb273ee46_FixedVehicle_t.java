 class FixedVehicle implements Cloneable {
 
    private int x;
 
    public FixedVehicle(int y) { setX(y);}
 
   public Object clone() {          
		try {	
			Object o = super.clone();
			FixedVehicle result = (FixedVehicle)o;
			result.x=this.x;
			return result;
 		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
 		}
 
    }
    // other methods omitted
 
 public int getX() {
 	return x;
 }
 
 public void setX(int x) {
 	this.x = x;
 }
 }
