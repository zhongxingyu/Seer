 
 package IA.Bicing;
 
 import java.util.Random;
 
 /**
  *
  * This class generates the elements for the BicingGenerator problem
  *
  *  - The number of bicycles of each station that will not be used this hour
  *  - The probable state of the stations the next hour (only accounting the users)
  *  - Probable demand for each station the next hour
  *  - Coordinates of the stations
  *
  * @author bejar
  */
 public class BicingGenerator {
 
     private int numEstaciones;
     private int numBicicletas;
     private int modoDem;
     private int[][] coordEstaciones;
     private int[] stay;
     private int[] next;
     private int[] demand;
     public final static int EQUILIBRIUM = 0;
     public final static int RUSH_HOUR = 1;
     private Random myRandom;
     private final static int VAR_DISTRIBUTION_STATE = 2;
     private final static double PERCENTAGE_USER_MOVS = .8;
     private final static double VAR_DEMAND_EQ = 0.5;
     private final static double VAR_DEMAND_RUSH = 0.9;
     private int seed = 100;
     /**
      * The constructor receives as parameters the number of stations,
      *  the number of bicycles and the type of the demand
      *
      */
     public BicingGenerator(int est, int bic, int dem) {
         setSeed(seed);
         numEstaciones = est;
         numBicicletas = bic;
         modoDem = dem;
 
         BicingState.setStationsNum(est);
 
         generaCoordenadas();
         generaEstadoActual();
         generaEstadoMovimientos();
         generaProximaDemanda();
     }
 
 
     /**
      *  The same constructor but the seed of the random number generator can be changed
      *
      * @param est
      * @param bic
      * @param dem
      * @param seed
      */
     public BicingGenerator(int est, int bic, int dem, int seed) {
         setSeed(seed);
         numEstaciones = est;
         numBicicletas = bic;
         modoDem = dem;
 
         generaCoordenadas();
         generaEstadoActual();
         generaEstadoMovimientos();
         generaProximaDemanda();
     }
 
     private void setSeed(int seed){
         if(seed < 0){
             seed = -(seed);
         }
         myRandom = new Random(seed);
     }
 
     /**
      *  Generates the coordinates of the stations
      *  Half are generated uniformly inside a 100x100 square
      *  The rest are genrated inside the inner 50x50 square
      *
      */
     private void generaCoordenadas() {
         coordEstaciones = new int[numEstaciones][2];
         int estMitad = numEstaciones / 2;
         for (int i = 0; i < estMitad; i++) {
             coordEstaciones[i][0] = myRandom.nextInt(100);
             coordEstaciones[i][1] = myRandom.nextInt(100);
         }
 
         for (int i = estMitad; i < numEstaciones; i++) {
             coordEstaciones[i][0] = myRandom.nextInt(50) + 25;
             coordEstaciones[i][1] = myRandom.nextInt(50) + 25;
         }
 
     }
 
     /**
      * Generates the current number of bicycles in each station
      *
      * The the number of Bicycles are generated using a combination of VAR_DISTRIBUTION_STATE
      * uniform distributions and distributed uniformly among the stations
      *
      */
     private void generaEstadoActual() {
         int numB;
         int numE;
 
          stay = new int[numEstaciones];
         for (int i = 0; i < numEstaciones; i++) {
              stay[i] = 0;
         }
 
         for (int i = numBicicletas; i > 0;) {
             numB = myRandom.nextInt(VAR_DISTRIBUTION_STATE);
             numE = myRandom.nextInt(numEstaciones);
              stay[numE] =  stay[numE] + numB;
             i = i - numB;
         }
     }
 
     /**
      * This generates the state for the next hour and the number of
      * bicycles that will not be used (and that we can move to other stations)
      * 
      * The state is generated using PERCENTAGE_USER_MOVS * numBicicletas
      * random bicycle moves
      *
      */
     private void generaEstadoMovimientos() {
         int numMovs = (int) (numBicicletas * PERCENTAGE_USER_MOVS);
         int in, out;
 
         next = new int[numEstaciones];
 
         for (int i = 0; i < numEstaciones; i++) {
             next[i] = 0;
         }
 
         for (int i = 0; i < numMovs; i++) {
             out = myRandom.nextInt(numEstaciones);
             in = myRandom.nextInt(numEstaciones);
             if ( stay[out] > 0) {
                  stay[out]--;
                 next[in]++;
             }
         }
 
            for (int i = 0; i < numEstaciones; i++) {
                next[i]=next[i]+ stay[i];
            }
     }
 
     /**
      * Generates the demand of bicycles for the next hour
      *
      * The demand is generated using a perturbation of the mean number of bicycles
      *
      */
     private void generaProximaDemanda() {
        int numMB = (int)(numBicicletas / numEstaciones);
         double var[] = new double[2];
         int nBvar[]=new int[2];
         int sign = 0;
        int r;
 
         if (modoDem == EQUILIBRIUM) {
             var[0] = VAR_DEMAND_EQ;
             var[1] = 1- VAR_DEMAND_EQ;
         } else {
             var [0] = VAR_DEMAND_RUSH;
            var [1] = 1-VAR_DEMAND_RUSH;
 
         }
 
         demand = new int[numEstaciones];
         for (int i = 0; i < numEstaciones; i++) {
             if (myRandom.nextBoolean()) {
                 sign = 1;
             } else {
                 sign = -1;
             }
            r = (int) (numMB * var[myRandom.nextInt(1)]);
            if(r < 1){
                r = 1;
            }
            demand[i] = numMB + (sign * myRandom.nextInt(r));
         }
 
     }
 
     /**
      * Returns the coordinates of a station
      *
      * @param est Station number
      * @return
      */
     public int[] getStationCoord(int est) {
         int[] coord = new int[2];
 
         coord[0] = coordEstaciones[est][0];
         coord[1] = coordEstaciones[est][1];
 
         return coord;
     }
 
     /**
      * Retutns the distance between two stations
      *
        * @param est1 Station one number
        * @param est2 Station two number
        * @return
        */
     public double getStationsDistance(int est1,int est2) {
         double dist;
 
         dist= Math.sqrt(Math.pow(coordEstaciones[est1][0]- coordEstaciones[est2][0],2)
                        +Math.pow(coordEstaciones[est1][1]-coordEstaciones[est2][1],2));
         return dist;
     }
 
     public int[][] getStationsCoordinates(){
         return coordEstaciones;
     }
 
   /**
      *  Returns the number of bicycles that do not move from the station the current hour
      *
      * @param st Station number
      * @return
      */
     public int getStationDoNotMove(int st) {
         return stay[st];
     }
 
     /**
      *  Returns the number of bicycles of an station the next hour
      *  after the user moves
      *
      * @param st Station number
      * @return
      */
     public int getStationState(int st) {
         return next[st];
     }
 
     /**
      *  Returns the demand of bicycles for the next hour
      *
      * @param st Station number
      * @return
      */
     public int getDemandNextHour(int st) {
         return demand[st];
     }
 
     /**
      * Returns the number of stations
      *
      * @return
      */
     public int getNumStations() {
         return numEstaciones;
     }
 
     public int[] getCurrent(){
         return  stay;
     }
 
     public int[] getNext(){
         return next;
     }
 
     public int[] getDemand(){
         return demand;
     }
 
     @Override
     public String toString(){
         StringBuffer sb = new StringBuffer();
         sb.append("stations: "+numEstaciones+", ");
         sb.append("bikes: "+numBicicletas+", ");
         sb.append("mode: "+modoDem+", ");
         sb.append("seed: "+seed+"\n");
         return sb.toString();
     }
 }
