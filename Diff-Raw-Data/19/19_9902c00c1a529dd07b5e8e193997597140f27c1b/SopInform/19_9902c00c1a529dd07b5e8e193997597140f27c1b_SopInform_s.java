 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package clase;
 
 import java.util.Vector;
 /**
  *
  * @author Javier
  */
 
 abstract class SopInform extends Publicaciones
 {//practica de arrays dinamicos...
        private static Vector elementos;
         static SopInform estuche[];
         private static int maximodeestuches=10;
 
 	public SopInform(String prefijo, String pregunta)
 	{
 		super(prefijo,pregunta);
                 int i=0;
                 boolean e=false;
//                for(i=0;estuche[i]!=null&&i<maximodeestuches;i++);
//                if(i<maximodeestuches)
//                    estuche[i]=this;
                
 	}
 
         abstract void InfoPC();
 }
