 import java.util.*;
 import java.io.*;
 /* Soubor je ulozen v kodovani UTF-8.
  * Kontrola kodovani: Prilis zlutoucky kun upel dabelske ody. */
 
 /*******************************************************************************
  * Instance tridy {@code Calendar} predstavuji prioritni frontu (?)
  *
  * @author    jmeno autora
  * @version   0.00.000
  */
 public class Calendar extends Thread
 {
     
     public static int time; //cas simulace v minutach;
     public static PriorityQueue<Process> q = new PriorityQueue<Process>(10000, new ProcCompare());
     public static Graph g;
 
     private static boolean isRun = true;
 
     //private final Object lock = new Object();
     //private volatile boolean isClear = true;
 
     
     
     /***************************************************************************
      *
      */
     public Calendar(Graph g)
     {
         time = 0;
         this.g = g;
         start();
     }
 
     
     /*public void cont()
     {
         return;
     }*/
     
     @Override
     public synchronized void run()
     {
         //System.out.println("START!");
         Core.log("START!");
         try{
             addAllNodeToQ();
 
         }
         catch(NullPointerException e){
             Core.log("NENI GRAF!");
             return;
         }
         test();
         try
         {
             createStatistics();
         }
         catch (Exception e)
         {
             return;
         }
     }
     
//<<<<<<< HEAD
     public void end()  throws Exception
     {
         Core.log("STOP!");
         if (isRun) {
             isRun = false;
         }
         else {
             isRun = true;
         }
     }
         //super.interrupt();
//=======
     /*public synchronized void pause()
     {
         Core.log("STOP!");
         /*
         while(Core.isClear)
         {
             try
             {
                 wait();
             }
             catch (InterruptedException e)
             {
                 return;
             }
         }
         isClear = false;*/
//>>>>>>> 67850bb8e846596279e9c927ffc5b3973eb18057
         //notifyAll();
         //TODO
         //createStatistics();
     //}
     
     /********************************************************************
     * Testovaci metoda.
     */
     public void test()
     {  
         Process proc;
         while (q.size() != 0 && Core.isClear) {
             if (q.peek().time == time) {
                 proc = q.poll();
                 proc.go();
                 //System.out.println("peak time "+q.peek().time);
                 //System.out.println(proc.time);
                 //System.out.println(((Airport)proc.node.suppliedFrom.proces).actualFood);
             }
             else {
                 if (q.peek().time < time) {
                     Process pe = q.peek();
                     //Core.log("peak time "+pe.time + " "+pe.people);
                     if (pe instanceof Settle) {
                         Core.log("mesto"+pe.node.people);
                     }
                     else if (pe instanceof Car) {
                         Car pee = (Car) pe;
                         Core.log("car"+pee.nextWork+" "+pee.path[pee.path.length-1].id+" kolik: "+pee.kolik +" "+ pee.cast);
                     }
                     else {
                         Core.log("ost"+pe.nextWork);
                     }
                 }
                 time++;
                 Core.log("======"+time+"======");
             }
             if (time == 7200) {
                 break; 
             }
             
            while (isRun == false) {
                 try{
                     sleep(1);
                 }
                 catch(Exception e){
 
                 }
             }
         }
         /*synchronized(lock)
             {
                 try
                 {
                     while(!Core.isClear)
                     {
                         lock.wait();
                     }
                     lock.notify();
                 }
                 catch (InterruptedException ie)
                 {
                     ie.printStackTrace();
                 }
             }*/
     }
          
     public void addAllNodeToQ(){
         //Random r = new Random();
         Node node = g.firstNode;
         int pp = 0;
         for (int i = 0;i<g.arrayAirport.length ;i++ ) {
             g.arrayAirport[i].proces = new Airport(60);
             g.arrayAirport[i].proces.node = g.arrayAirport[i];
             q.add(g.arrayAirport[i].proces);
         }
         while (node != null) {
             if (node instanceof SettleNode) {
                 node.proces = new Settle(0);
                 node.proces.node = node;
                 q.add(node.proces);
                 pp++;
              
             }
             if (node instanceof AirportNode) {
                 //node.proces = new Airport(r.nextInt(1000));
             }
             node = node.next;
             if (pp == 3000) {
                 break;
             }
             
         }
     }
     
     public void createStatistics() throws Exception{
         FileWriter out1 = new FileWriter("zasobovano_z.txt");
         FileWriter out2 = new FileWriter("zasobovano_kdy_kolik.txt");
         out1.write("ID zasobovano z letiste\n");
         Node node = g.firstNode;
         while (node != null) {
             if (node instanceof SettleNode) {
                 out1.write(node.id+"");
                 out1.write(" "+node.suppliedFrom.id+"\n");
 
                 out2.write("==== ID "+node.id+" ====\n");
                 out2.write(node.log.toString()+"\n"); 
             }
 
             node = node.next;
         }
         out2.close();
         out1.close();
         Core.log("Vytvorena statistika");
     }
 }
 
 
