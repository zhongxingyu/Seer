 package wars.dragon.engine;
 
 public class Dragon extends Unit implements RangedUnit {
     private String name = "Dragon";
 
     public Dragon() {
 	super(10.0, 5, 3.0, 3.0, 2.0);
     }
 
     public Double getRange() {
	return 5.0;
     }
 }
