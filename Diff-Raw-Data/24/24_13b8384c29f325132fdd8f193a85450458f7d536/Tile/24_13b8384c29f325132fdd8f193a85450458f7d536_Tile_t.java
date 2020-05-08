 public abstract class Tile {
 
     protected boolean hasMule;
    protected Player owner;
     protected String type;
 
     public Tile(String type) {
         this.type = type;
         hasMule = false;
     }
 
     public abstract void collectResources(Player player);
 
     // return the String of the image to draw at this location, or null if we don't need to draw anything
     public abstract String image();
 
     public boolean addMule() {
 
         if (hasMule == true && owner != null) {
             System.out.println("Tile already has a MULE!");
             return false;
         }
         hasMule = true;
         return true;
     }
 
     public boolean removeMule() {
         if (hasMule != true && owner != null) {
             System.out.println("Tile does not have a MULE!");
             return false;
         }
         hasMule = false;
         return true;
     }
 
    public boolean buyProperty(Player owner) {
         if (this.owner != null) {
             System.out.println("This property is already owned!");
             return false;
         }
         this.owner = owner;
         return true;
     }
 
    public Player getOwner() {
         return owner;
     }
 
     public boolean hasMule() {
         return hasMule;
     }
 
     public String getType() {
         return type;
     }
 }
