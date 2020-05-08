 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package modelo;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author HP
  */
 public class DescuentoServicios extends SueldoDecorador{
 
     ArrayList<Servicio> servicios;
     private double descuentoPorServicios;
     public DescuentoServicios(String idEmpleado, Sueldo sueldoDecorado)
     {
         this.idEmpleado = idEmpleado;
         this.sueldoDecorado = sueldoDecorado;
     }
     public double calcularDescuentoPorServicios()
     {
         double descuento = 0.0;
         for (int i = 0; i < servicios.size(); i++) {
             descuento += servicios.get(i).getMonto();
         }
        return 0.0;
     }
     public double calcularSueldo() {
         return sueldoDecorado.calcularSueldo()- descuentoPorServicios;
     }
 
 
 }
