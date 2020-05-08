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
     private int numero; // id del grupo, donde id >= 0
     private int capacidad; //Numero maximo de alumnos en el grupo, estrictamente positivo
      
    public Grupo(){
        numero = -1;
        capacidad = -1;
     }
    public Grupo (int numero,int capacidad){
        this.numero = numero;
        this.capacidad = capacidad;
     }
     public int id() {
        return numero;
    }
    public int capacidad() {
        return capacidad;
    }
    public void modificar_id(int numero){
       this.numero = numero;
    }
    public void modificar_capacitat(int capacidad){
       capacidad = capacidad;
    }
     
 }
