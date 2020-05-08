 import java.util.ArrayList;
 import java.util.List;
 
 public class Skyline2 {
   private static final int COORDINATE_MAX_VALUE = 10000;
   private static final int MAX_BUILDINGS = 5000;
 
   public static class Building {
     private int left;
     private int height;
     private int right;
 
     public Building(int l, int h, int r) {
       this.left = l;
       this.height = h;
       this.right = r;
     }
 
     public int getLeft() { return this.left; }
     public int getHeight() { return this.height; }
     public int getRight() { return this.right; }
   }
 
   private static int[] prepareSurface(Building[] city) {
     int[] surface = new int[COORDINATE_MAX_VALUE];
     for (int i=0, n=city.length; i<n; i++) {
       Building b = city[i];
       for (int j=b.getLeft(), m=b.getRight(); j<m; j++) {
         if (b.getHeight() > surface[j]) surface[j] = b.getHeight();
       }
     }
     return surface;
   }
 
   public static List<Integer> drawSkyline(Building[] city) {
     int[] surface = prepareSurface(city);
 
     List<Integer> skyline = new ArrayList<>();
     if (surface[0] > 0) {
       skyline.add(0);
       skyline.add(surface[0]);
     }
     for (int i=1, n=surface.length; i<n; i++) {
       if (surface[i-1] != surface[i]) {
         skyline.add(i);
         skyline.add(surface[i]);
       }
     }
    if (surface[surface.length-1] > 0) {
    	skyline.add(surface[surface.length-1]);
    	skyline.add(0);
    }
     return skyline;
   }
   
   public static void main(String[] args) {
	  Building[] city = { new Building( 1, 11,  5),
			              new Building( 2,  6,  7),
			              new Building( 3, 13,  9),
			              new Building(12,  7, 16),
			              new Building(14,  3, 25),
			              new Building(19, 18, 22),
			              new Building(23, 13, 29),
			              new Building(24,  4, 28) };
 	  System.out.println(drawSkyline(city));
 	  
 	  Building[] bigCity = new Building[MAX_BUILDINGS];
 	  for (int i=0, n=bigCity.length; i<n; i++) {
 		  bigCity[i] = new Building(0, i+1, 10000);
 	  }
 	  
 	  long start = System.nanoTime();
 	  System.out.println(drawSkyline(bigCity));
 	  System.out.println();
 	  System.out.println((System.nanoTime() - start)/1000000);
   }
 }
