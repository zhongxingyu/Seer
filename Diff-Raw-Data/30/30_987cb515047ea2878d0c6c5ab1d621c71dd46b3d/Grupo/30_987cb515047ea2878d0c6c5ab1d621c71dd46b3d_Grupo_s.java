 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 
 /**
  *
  * @author Joan Pol
  */
 public class Grupo {
     int Numero; // id del grupo, donde id >= 0
     int Capacidad; //Numero maximo de alumnos en el grupo, estrictamente positivo
      
    public void Grupo(){
        Numero = -1;
        Capacidad = -1;
     }
    public void Grupo (int numero,int capacidad){
        this.Numero = numero;
        this.Capacidad = capacidad;
     }
     public int id() {
        return Numero;
    }
    public int capacidad() {
        return Capacidad;
    }
    public void modificar_id(int numero){
       this.Numero = numero;
    }
    public void modificar_capacitat(int capacidad){
       Capacidad = capacidad;
    }
     
 }
