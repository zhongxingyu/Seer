 //Prototype implementation of Car Control
 //Mandatory assignment
 //Course 02158 Concurrent Programming, DTU, Fall 2013
 
 //Hans Henrik Løvengreen     Oct 6, 2013
 
 
 import java.awt.Color;
 
 class Gate {
 
     Semaphore g = new Semaphore(0);
     Semaphore e = new Semaphore(1);
     boolean isopen = false;
 
     public void pass() throws InterruptedException {
         g.P();
         g.V();
     }
 
     public void open() {
         try { e.P(); } catch (InterruptedException e) {}
         if (!isopen) { g.V();  isopen = true; }
         e.V();
     }
 
     public void close() {
         try { e.P(); } catch (InterruptedException e) {}
         if (isopen) {
             try { g.P(); } catch (InterruptedException e) {}
             isopen = false;
         }
         e.V();
     }
 
 }
 
 class Car extends Thread {
 
     int basespeed = 100;             // Rather: degree of slowness
     int variation =  50;             // Percentage of base speed
 
     CarDisplayI cd;                  // GUI part
 
     int no;                          // Car number
     Pos startpos;                    // Startpositon (provided by GUI)
     Pos barpos;                      // Barrierpositon (provided by GUI)
     Color col;                       // Car  color
     Gate mygate;                     // Gate at startposition
     Alley alley;
     Collision collision;
     Barrier barrier;
 
    boolean removed = false;

 
     int speed;                       // Current car speed
     Pos curpos;                      // Current position
     Pos newpos;                      // New position to go to
 
     public Car(int no, CarDisplayI cd, Gate g, Alley a, Collision c, Barrier b) {
 
         this.no = no;
         this.cd = cd;
         mygate = g;
         alley = a;
         collision = c;
         barrier = b;
         startpos = cd.getStartPos(no);
         barpos = cd.getBarrierPos(no);  // For later use
 
         col = chooseColor();
 
         // do not change the special settings for car no. 0
         if (no==0) {
             basespeed = 0;
             variation = 0;
             setPriority(Thread.MAX_PRIORITY);
         }
     }
 
     public synchronized void setSpeed(int speed) {
         if (no != 0 && speed >= 0) {
             basespeed = speed;
         }
         else
             cd.println("Illegal speed settings");
     }
 
     public synchronized void setVariation(int var) {
         if (no != 0 && 0 <= var && var <= 100) {
             variation = var;
         }
         else
             cd.println("Illegal variation settings");
     }
 
     synchronized int chooseSpeed() {
         double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
         return (int)Math.round(factor*basespeed);
     }
 
     private int speed() {
         // Slow down if requested
         final int slowfactor = 3;
         return speed * (cd.isSlow(curpos)? slowfactor : 1);
     }
 
     Color chooseColor() {
         return Color.blue; // You can get any color, as longs as it's blue
     }
 
     Pos nextPos(Pos pos) {
         // Get my track from display
         return cd.nextPos(no,pos);
     }
 
     boolean atGate(Pos pos) {
         return pos.equals(startpos);
     }
 
     boolean inAlley(Pos pos) {
     	return pos.col == 0 ||
     		   pos.row == 1 && pos.col == 1;
     }
 
     boolean infrontOfBarrier(Pos curpos, Pos newpos) {
         if (curpos.col < 3) return false;
         return curpos.row == 5 && newpos.row == 6 ||
                curpos.row == 6 && newpos.row == 5;
     }
 
     public void run() {
         boolean inbetweenFields = false;
 
     	try {
             speed = chooseSpeed();
             curpos = startpos;
             cd.mark(curpos,col,no);
 
             collision.enter(curpos);
 
             while (true) {
                 sleep(speed());
 
                 if (atGate(curpos)) {
                     mygate.pass();
                     speed = chooseSpeed();
                 }
 
                 newpos = nextPos(curpos);
 
                 if (inAlley(curpos)) {
                 	if (!inAlley(newpos)) {
                 		alley.leave(no);
                 	}
                 } else {
                 	if (inAlley(newpos)) {
                 		alley.enter(no);
                 	}
                 }
 
                 collision.enter(newpos);
                 inbetweenFields = true;
 
                 if (infrontOfBarrier(curpos, newpos)) {
                     barrier.sync();
                 }
 
                 //  Move to new position
                 cd.clear(curpos);
                 cd.mark(curpos,newpos,col,no);
                 sleep(speed());
                 cd.clear(curpos,newpos);
                 cd.mark(newpos,col,no);
 
                 collision.leave(curpos);
                 curpos = newpos;
                 inbetweenFields = false;
             }
         } catch (InterruptedException e) {
            removed = true;

             if (inbetweenFields) {
                 collision.leave(curpos);
                 collision.leave(newpos);

                if (inAlley(newpos)) {
                    try { alley.leave(no); } catch (InterruptedException ie) {};
                }

                 cd.clear(curpos, newpos);
             } else {
                 collision.leave(curpos);

                if (inAlley(curpos)) {
                    try { alley.leave(no); } catch (InterruptedException ie) {};
                }

                 cd.clear(curpos);
             }
 
             cd.println("Car no. " + no + " removed");
         } catch (Exception e) {
             cd.println("Exception in Car no. " + no);
             System.err.println("Exception in Car no. " + no + ":" + e);
             e.printStackTrace();
         }
     }
 
 }
 
 public class CarControl implements CarControlI{
 
     CarDisplayI cd;           // Reference to GUI
     Car[]  car;               // Cars
     Gate[] gate;              // Gates
     Alley alley;
     Collision collision;
     Barrier barrier;
 
     public CarControl(CarDisplayI cd) {
         int numberOfCars = 9;
 
         this.cd = cd;
         car  = new  Car[numberOfCars];
         gate = new Gate[numberOfCars];
         alley = new Alley();
         collision = new Collision(11, 12);
         barrier = new Barrier(numberOfCars);
 
         for (int no = 0; no < numberOfCars; no++) {
             gate[no] = new Gate();
             car[no] = new Car(no, cd, gate[no], alley, collision, barrier);
             car[no].start();
         }
     }
 
     public boolean hasBridge() {
         return false;				// Change for bridge version
     }
 
     public void startCar(int no) {
         gate[no].open();
     }
 
     public void stopCar(int no) {
         gate[no].close();
     }
 
     public void barrierOn() {
         barrier.on();
     }
 
     public void barrierOff() {
         try { barrier.off(); } catch (InterruptedException e) {}
     }
 
     public void barrierShutDown() {
         cd.println("Barrier shut down not implemented in this version");
         // This sleep is for illustrating how blocking affects the GUI
         // Remove when shutdown is implemented.
         try { Thread.sleep(5000); } catch (InterruptedException e) { }
         // Recommendation:
         //   If not implemented call barrier.off() instead to make graphics consistent
     }
 
     public void setLimit(int k) {
         cd.println("Setting of bridge limit not implemented in this version");
     }
 
     public void removeCar(int no) {
        if (car[no].isAlive() && !car[no].removed) {
             car[no].interrupt();
         }
     }
 
     public void restoreCar(int no) {
         if (!car[no].isAlive()) {
             car[no] = new Car(no, cd, gate[no], alley, collision, barrier);
             car[no].start();
             cd.println("Car no. " + no + " restored");
         }
     }
 
     /* Speed settings for testing purposes */
 
     public void setSpeed(int no, int speed) {
         car[no].setSpeed(speed);
     }
 
     public void setVariation(int no, int var) {
         car[no].setVariation(var);
     }
 
 }
 
 
 
 
 
 
