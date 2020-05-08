 package co.edu.eafit.dis.st0257.cenasalvajes;
 
 import java.util.Random;
 
 public class Salvaje implements Runnable {
 
   private int id;
   private Olla olla;
   private Random rand;
   private Cocinero cocinero;
 
   public Salvaje(int id, Olla olla) {
     this.id = id;
     this.olla = olla;
     rand = new Random();
   }
 
   public void run() {
     while (true) {
      //System.out.println(this.toString() + " solicita comida");
      System.out.println("test1");
       int porcion = olla.obtenerPorcion();
      System.out.println("test3");
       System.out.println(this.toString() + " solicita comida test");
       assert porcion > 0; // nunca deberia ser menor que 1
      System.out.println(this.toString() + " come y quedan " + porcion);
       comer(porcion);
     }
   }
 
   public void comer(int porcion) {
     try {
       System.out.println(this.toString() + " comiendo ");
       Thread.sleep(rand.nextInt(porcion)*100);
     }
     catch (InterruptedException ie) {
     }
   }
 
   public String toString() {
     return "Salvaje: " + id;
   }
 }
