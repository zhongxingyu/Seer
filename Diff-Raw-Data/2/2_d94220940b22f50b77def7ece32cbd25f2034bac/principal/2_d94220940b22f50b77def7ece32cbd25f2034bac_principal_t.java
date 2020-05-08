 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package proyecto1;
 
 import java.util.Scanner;
 
 /**
  *
  * @author NIGHTMARE
  */
class principal{
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Scanner lea=new Scanner(System.in);
         General general=new General();
         Jugadores jugadores[]=new Jugadores[10];
         Tablero tablero=new Tablero();
         Fichas fichas[]=new Fichas[16];
         
         fichas[0]=new Torre(0,0,0);
         fichas[1]=new Caballo(1,0,0);
         fichas[2]=new Elefante(2,0,0);
         fichas[3]=new Visir(3,0,0);
         
         int eterno=0;
         String nombre1,nombre2;
         char menu;
         
         while(eterno==0){
             general.menuPrincipal();
             
             do{
                 System.out.print("Ingrese la opcion que desea realizar (1/2/3): ");
                 menu=lea.next().charAt(0);
             }while(general.validarMenu(menu));
             
             if(menu=='1'){
                 System.out.println("");
                 System.out.print("Ingrese el nombre del jugador 1: ");
                 nombre1=lea.next();
                 System.out.print("Ingrese el nombre del jugador 2: ");
                 nombre2=lea.next();
                 
                 jugadores[Jugadores.contador]=new Jugadores(nombre1,nombre2);
                 
                 tablero.iniciarTabla();
                 tablero.imprimirTabla();
                 
                 
             }else if(menu=='2'){
                 
             }else{
                 System.exit(0);
             }
             
         }
     }
 }
