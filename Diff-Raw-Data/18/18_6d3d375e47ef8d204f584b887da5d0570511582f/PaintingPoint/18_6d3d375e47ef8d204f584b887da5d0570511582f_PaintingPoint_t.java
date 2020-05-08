 package cz.vutbr.fit.tam.paint3d;
 
 class PaintingPoint {
 
     public static final String TAG = "3Dpaint|PaintingPoint";
     public static final String TABLE_NAME = "painting_point";
     public static final String[] TABLE_COLUMNS = {"painting_point_id", "painting_id", "x", "y", "z"};
     public float x;
     public float y;
     public float z;
 
     public PaintingPoint(float x, float y, float z) {
         this.x = x;
         this.y = y;
         this.z = z;
     }
     
     public void add(PaintingPoint p) {
         this.x += p.x;
         this.y += p.y;
         this.z += p.z;
     }
     
     public void sub(PaintingPoint p) {
         this.x -= p.x;
         this.y -= p.y;
         this.z -= p.z;
     }

    @Override
    public String toString() {
        return "x: " + this.x + " y: " + this.y + " z: " + this.z;
    }
 }
