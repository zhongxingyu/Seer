 package oneway.g8;
 
 import oneway.sim.MovingCar;
 import oneway.sim.Parking;
 import java.util.*;
 
 public class Player extends oneway.sim.Player
 {
     // if the parking lot is almost full
     // it asks the opposite direction to yield
     private static double AlmostFull = 0.8;
 
     //Indicator points right if true and left if not true.
     public boolean indicator = false;
 
     //Number of ticks since the game began
     public int timer;
 
     //Number of ticks until the indicator should change
     public int changeIndicatorTicks = 15;
 
     private boolean[] llights;
     private boolean[] rlights;
     private Parking[] right;
     private Parking[] left;
     MovingCar[] movingCars;
 
     public Player() {}
 
     public void init(int nsegments, int[] nblocks, int[] capacity)
     {
         this.nsegments = nsegments;
         this.nblocks = nblocks;
         this.capacity = capacity.clone();
 
         indicator = true;
         timer = 0;
     }
 
     //Reverse the indicator and set the lights accordingly
     public void changeIndicator(){
 
         indicator = !indicator;
 
         //Change all of the lights to point in the direction of the indicator
         for(int i = 0;i<llights.length;i++){
             //The indicator points right
             if(indicator){
                 llights[i] = true;
                 rlights[i] = false;
             }
 
             //The indicator points left
            if (!indicator){
                 llights[i] = false;
                 rlights[i] = true;
             }
         }
     }
 
     public void setLights(MovingCar[] movingCars,
             Parking[] left,
             Parking[] right,
             boolean[] llights,
             boolean[] rlights)
     {
         this.movingCars = movingCars;
         this.left = left;
         this.right = right;
         this.llights = llights;
         this.rlights = rlights;
 
         //Change the indicator once changeIndicatorTicks ticks have passed
         if(timer%changeIndicatorTicks == 0){
             //changeIndicator();
         }
 
         for (int i=0; i<llights.length; i++) {
             llights[i] = true;
             rlights[i] = true;
         }
 
         OppositeMovements();
 
        print_lights();

         //avoidCollissionAndOverflow();
 
         timer++;
     }
 
    private void print_lights() {
        for (int i=0; i<rlights.length; i++)
            System.out.print(((rlights[i])?0:1) + " ");
        System.out.print("\n  ");
        for (int i=0; i<llights.length; i++)
            System.out.print(((llights[i])?0:1) + " ");
        System.out.println("");
    }
 
     private void OppositeMovements() 
     {
         Vector<Car> opposite = new Vector<Car>( ); // to store all cars in opposite direction as indicator
         Vector<Car> forward  = new Vector<Car>( );
 
         // segment, block, dir, startTime
 
         for (MovingCar c : movingCars) {
             if (indicator) {
                 if (c.dir < 0)  opposite.add(new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                 else            forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                 
             } else { 
                 if (c.dir < 0)  forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                 else            opposite.add(new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
             }
         }
 
         int left_parking_num = 0;
         int right_parking_num = 0;
 
         // take care of the cars in parking lots
         if (indicator) {
             if (right!=null)
                 for (int i=0; i<right.length; i++) {
                     if (right[i]==null) continue;
 
                     right_parking_num += right[i].size();
                     for (int j=0; j<right[i].size(); j++) 
                         forward.add( new Car(true, i, nblocks) );
                 }
             if (left!=null)
                 for (int i=0; i<left.length; i++) {
                     if (left[i]==null) continue;
                     left_parking_num += left[i].size();
 
                     for (int j=0; j<left[i].size(); j++)
                         opposite.add( new Car(false, i, nblocks) );
                 }
         } else {
             if (right!=null)
                 for (int i=0; i<right.length; i++) {
                     if (right[i]==null) continue;
 
                     right_parking_num += right[i].size();
                     for (int j=0; j<right[i].size(); j++)
                         opposite.add( new Car(true, i, nblocks) );
                 }
             if (left!=null)
                 for (int i=0; i<left.length; i++) {
                     if (left[i]==null) continue;
 
                     left_parking_num += left[i].size();
                     for (int j=0; j<left[i].size(); j++)
                         forward.add( new Car(false, i, nblocks) );
                 }
         }
 
         Collections.sort( forward );
         Collections.sort( opposite );
 
         if (forward.size() == 0 || opposite.size() == 0) return ; // if only have cars in one direction, do nothing, just return
 
         System.out.println("*************** forward cars******************");
         System.out.println(forward.size() + " cars moving forward");
         for (Car c : forward)
             System.out.println(c.toString());
         System.out.println("*************** opposite cars*****************");
         System.out.println(opposite.size() + " cars moving opposite");
         for (Car c : opposite)
             System.out.println(c.toString());
         System.out.println("**********************************************");
 
         int fstep = forward.get(0).steps; // get the first car in the forward direction
         oneway.sim.Parking[] L = left.clone();
         oneway.sim.Parking[] R = right.clone();
 
         /*
            r0      r1      r2 
            ->      ->      ->
                seg0    seg1    seg2
             | - - - | - - - | - - - |
            p0      p1       p2     p3
                    <-      <-      <-
                    l0       l1     l2  
            */
 
 
         if (indicator) {
             for (int i=llights.length/2; i<llights.length; i++)
                 llights[i] = true;
             for (Car c : opposite) {
                 int cstep = (fstep + c.steps) / 2; // collision steps
                 int cseg = cstep / nblocks[0]; // collision segment,FIXME right now all segments are regarded as same length
                 int latest_parking = cseg + 1;
 
                if (c.steps <= fstep) { // first forward car already passed and the current car is in parking lot
                    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                    int seg = c.steps / nblocks[0];
                    llights[seg - 1] = false;
                    continue;
                }

                 for (int i=latest_parking; i<L.length; i++) {
                     if (R[i].size() + L[i].size() < capacity[i]) {
                         L[i].add(0);
 
                         int car_seg = c.steps / nblocks[0];
                         int car_blk = c.steps % nblocks[0];
                         if (car_seg == i && car_blk == 0)
                             llights[i-1] = false;
                         break;
                     }
                 }
             }
         } else {
             for (int i=0; i<rlights.length/2; i++)
                 rlights[i] = true;
             for (Car c : opposite) {
                 int cstep = (fstep + c.steps) / 2; // collision steps
                 int cseg = cstep / nblocks[0]; // collision segment,FIXME right now all segments are regarded as same length
                 int latest_parking = cseg;
 
                if (c.steps >= fstep) { // first forward car already passed and the current car is in parking lot
                    int seg = c.steps / nblocks[0];
                    rlights[seg] = false;
                    continue;
                }

                 for (int i=latest_parking; i>=0; i--) {
                     if (R[i].size() + L[i].size() < capacity[i]) {
                         R[i].add(0);
 
                         int car_seg = c.steps / nblocks[0];
                         int car_blk = c.steps % nblocks[0];
                         if (car_seg == i && car_blk == 0)
                             rlights[i] = false;
                         break;
                     }
                 }
             }
         }
     }
 
     //Modify the lights to avoid overflow when the indicator changes
     private void avoidCollissionAndOverflow(){
 
         for(int seg = 0;seg<nsegments;seg++){
 
             //Ensure parking lot p_(seg+1) does not overflow 
             //If there is not enough time for a car to pass the parking lot betewen seg and seg+1
             if(seg<nsegments-1&&(changeIndicatorTicks-(timer&changeIndicatorTicks)) <= nblocks[seg]+nblocks[seg+1]){
                 //Cars moving to parking lot p_(seg+1) from the right
                 int carsRight = 0;
 
                 //Cars moving to parking lot p_(seg+1) from the left
                 int carsLeft = 0;
 
                 for(int car = 0;car<movingCars.length;car++){
                     if(movingCars[car].segment==seg&&movingCars[car].dir==1)
                         carsRight++;
 
                     if(movingCars[car].segment==seg+1&&movingCars[car].dir==-1)
                         carsLeft++;
                 }
 
                 //If there is not enough room to accomodate more cars at parking lost seg+1, do not send more cars to that parking lot
                 if(carsRight+carsLeft>=capacity[seg+1]-left[seg+1].size()-right[seg+1].size()){
                     if(indicator)
                         rlights[seg] = false;
                     if(!indicator)
                         llights[seg+1]= false;
                 }
             }
 
             //Ensure parking lot p_(seg+1) does not overflow while indicator points left
             //If there is not enough time for a car to pass the parking lot betewen seg and seg+1
             if(indicator&&seg<nsegments-1&&(changeIndicatorTicks-(timer&changeIndicatorTicks)) <= nblocks[seg]+nblocks[seg+1]){
 
                 //Cars moving to parking lot p_(seg+1) from the right
                 int carsRight = 0;
 
                 //Cars moving to parking lot p_(seg+1) from the left
                 int carsLeft = 0;
 
                 for(int car = 0;car<movingCars.length;car++){
                     if(movingCars[car].segment==seg)
                         carsRight++;
 
                     if(movingCars[car].segment==seg+1)
                         carsLeft++;
                 }
                 //If there is not enough room to accomodate more cars at parking lost seg+1, do not send more cars to that parking lot
                 if(carsRight+carsLeft>=capacity[seg+1]-left[seg+1].size()-right[seg+1].size())
                     rlights[seg] =false;
             }
 
             //If the time left before the indicator change is less than the length of segment seg
             System.out.println("Time left before change:" + (changeIndicatorTicks-(timer&changeIndicatorTicks)) + "\n");
             if((changeIndicatorTicks-(timer%changeIndicatorTicks)) < nblocks[seg]){
                 if(indicator)
                     rlights[seg] = false;
 
                 if(!indicator)
                     llights[seg] = false;
             }
         }
     }
 
     private int nsegments;
     private int[] nblocks;
     private int[] capacity;
 }
