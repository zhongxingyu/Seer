 class FixedVehicle implements Cloneable {
 
    private int x;
 
    public FixedVehicle(int y) { setX(y);}
 
   public Object clone() { 
 //     Object result = new FixedVehicle(this.x);
   //   // Location "A"
     // return result;
	      Object result;
		try {
			result = super.clone();
		      // Location "B"
		      //result = result;
		      ((FixedVehicle) result).setX(this.getX());  // throws ClassCastException
		      return result;     
 		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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
