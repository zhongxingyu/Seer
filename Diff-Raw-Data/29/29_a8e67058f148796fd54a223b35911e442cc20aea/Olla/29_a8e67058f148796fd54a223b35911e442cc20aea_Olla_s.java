 package co.edu.eafit.dis.st0257.cenasalvajes;
 
 public class Olla {
 
   private int porciones = 0, tamanio = 0;
 
   public Olla(int porciones) {
     this.porciones = this.tamanio = porciones;
   }
 
   public synchronized int obtenerPorcion() {
    System.out.println(" test2 ");    
    /*if(porciones == 0){ 
       notify();
       desactivar();
     }
    int ret = porciones--;*/
    return 2;
   }
   
   public synchronized void ponerPorciones() {
     if(porciones == tamanio) desactivar();
     int cont = tamanio-porciones;
     porciones = tamanio;
     System.out.println(" Cocinero lleno la olla ");
     if( tamanio == cont) notify();
     desactivar();
   }
   
   public void desactivar(){
    try{
      wait();
    } catch(InterruptedException ie) {
    }
   }
 }
