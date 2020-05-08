 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Pruebas;
 
 import Auxiliares.Pair;
 import Pruebas.testfunciones;
 import elementos.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import mapa.*;
 import restricciones.*;
 import restricciones.Restriccion;
 
 /**
  *
  * @author ivanmartinez
  */
 public class MainIvan {
     
     public static void main(String arg[]) throws Exception{
         
         //Crea 4 elementos del 0 al 4
         ArrayList<Integer> elementos = new ArrayList<>();
         for(int i=1;i<=4;++i){
             elementos.add(i);
         }
         
         System.out.println("Printo mi elementos\n");
         for(int i=0;i<4;++i){
             System.out.println(elementos.get(i)+" ");
         }
         System.out.println("\n");
         
         System.out.println("Printo su vector de ultima posiciones visitadas\n");
         //Crea el vector de ultimas visitas, para controlar los valores del dominio(pos en el mapa) que toman los elementos
         Pair[] lastVisited = new Pair[5];
         for(int i=1;i<=4;++i){
             Pair p = new Pair(0,0);
             lastVisited[i]=p;
         }
         
         for(int i=1;i<=4;++i){
             System.out.println(lastVisited[i].getFirst()+""+lastVisited[i].getSecond()+" ");
         }
         System.out.println("\n");
         
         
         //creamos las estrcuturas para las restricciones
         HashMap<Integer,ArrayList<Restriccion>> res = new HashMap<>();
         
         ArrayList<Restriccion> r0 = new ArrayList();
         ArrayList<Restriccion> r1 = new ArrayList();
         ArrayList<Restriccion> r2 = new ArrayList();
         ArrayList<Restriccion> r3 = new ArrayList();
         
         //Creamos restricciones
         Restriccion_ubicacion res12;
        res12 = new Restriccion_ubicacion("0","0","1","2",2);
         Restriccion_ubicacion res21;
        res21 = new Restriccion_ubicacion("0","0","2","1",2);
         Restriccion_ubicacion res34;
        res34 = new Restriccion_ubicacion("0","0","3","4",1);
         Restriccion_ubicacion res43;
        res43 = new Restriccion_ubicacion("0","0","4","3",1);
         
         //Añadimos las restricciones a su conjunto
         r0.add(res12);
         r1.add(res21);
         r2.add(res34);
         r3.add(res43);
         
         //añadimos todo al hashMap
         res.put(0, r0);
         res.put(1, r1);
         res.put(2, r2);
         res.put(3, r3);
         
         Plano p = new Plano(10,10);
         
      
         testfunciones tt = new testfunciones();
         System.out.println("Llamo al backtracking");
         tt.bactracking(0,elementos,lastVisited,res,p);
         
     }
 }
