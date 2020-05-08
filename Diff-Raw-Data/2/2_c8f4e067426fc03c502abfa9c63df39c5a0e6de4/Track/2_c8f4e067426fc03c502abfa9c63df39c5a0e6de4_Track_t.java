 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package momobilan;
 
 /**
  *
  * @author Andhika
  */
 public class Track {
     Vehicle[][] matrix;    
     int laneCount = 5;
     int trackLength = 20;
 
     /**
      * Konstruktor utama
      */
     public Track() {
         matrix = new Vehicle[trackLength][laneCount];
     }
     
     /**
      * Konstruktor dengan jumlah lajur dan panjang trek
      * 
      * @param lanes jumlah lajur
      * @param trackLength panjang trek
      */
     public Track(int laneCount, int trackLength) {
         this.laneCount = laneCount;
         this.trackLength = trackLength;
 
        matrix = new Vehicle[trackLength][laneCount];
     }
     
     /**
      * Mendaftarkan perpindahan benda pada trek
      * 
      * I.S. Pada currentLane dan currentDistance terdefinisi sebuah Vehicle T.
      * F.S. Jika pada newLane dan newDistance terdapat Vehicle lain,
      *        panggil V.hasCrashed().
      *      Jika newLane dan newDistance melebihi boundary atas-bawah track,
      *        panggil V.hasTrespassed().
      *      Else, V dipindahkan ke newLane dan newDistance,
      *        panggil V.hasMoved()
      * 
      * @param currentLane lajur mula-mula benda
      * @param currentDistance jarak mula-mula benda
      * @param newLane lajur baru benda
      * @param newDistance jarak baru benda
      */
     public synchronized void registerMovement(int currentLane, int currentDistance,
             int newLane, int newDistance) {
         Vehicle V = matrix[currentDistance][currentLane];
         
         if (newDistance < 0 || newDistance >= trackLength) {
             V.hasTrespassed();
         }
         else {
             Vehicle W = matrix[newDistance][newLane];
 
             if (newLane < 0 || newLane >= laneCount || W != null) {
                 // Menabrak tembok atau mobil lain
                 V.hasCrashed();
                 W.hasCrashed();
             }
             else { // W == null
                 matrix[newDistance][newLane] = V;
                 matrix[currentDistance][currentLane] = null;
                 V.hasMoved();
             }
         }
     }
     
     /**
      * Melahirkan sebuah mobil ke dalam dunia trek.
      * 
      * @param vehicle mobil yang akan dilahirkan
      * @param lane lajur mobil tersebut
      * @param distance jarak mobil tersebut
      */
     public void spawn(Vehicle vehicle, int lane, int distance) {
         matrix[distance][lane] = vehicle;
         
         vehicle.attach(this, lane, distance); // this starts the thread.
     }
     
     /**
      * Mengembalikan isi matriks trek
      * 
      * @return matriks trek - [distance][lane].
      */
     public Vehicle[][] getMatrix() {
         return matrix;
     }
 }
