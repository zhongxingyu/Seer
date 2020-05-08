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
        private static Vector elementos=new Vector(0,1);
         static SopInform estuche[];
         private static int maximodeestuches=10;
 
 	public SopInform(String prefijo, String pregunta)
 	{
 		super(prefijo,pregunta);
                 int i=0;
                 boolean e=false;
                for(i=0;elementos.size()>i&&!e;i++)
                {
                    if((elementos.elementAt(i).(SopInform)getTitulo().compareTo(this.getTitulo()))>=0)
                        e=true;
                }
                if(e)
                {
                    if((elementos.elementAt(i).(String)getTitulo().compareTo(this.getTitulo()))==0)
                        elementos.setElementAt(this, i);
                    else
                        elementos.add(this);
                }
 	}
 
         abstract void InfoPC();
 }
