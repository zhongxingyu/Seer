 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package co.tecnomati.java.controlcaja.modelo;
 
 
 import co.tecnomati.java.controlcaja.dominio.Cliente;
 import java.util.ArrayList;
 import javax.swing.table.AbstractTableModel;
 import co.tecnomati.java.controlcaja.dominio.dao.imp.*;
 import java.util.Iterator;
 
 /**
  *
  * @author AnahiAramayo
  */
 public class ModeloCliente  extends AbstractTableModel{
  Cliente persona = new Cliente();
     ClienteDaoImp personaDAO = new ClienteDaoImp();
     ArrayList<Cliente> listaPersonas = new ArrayList<Cliente>();
     
     public ModeloCliente() {            
      try {
         listaPersonas = (ArrayList<Cliente>) personaDAO.listarCliente();
      } catch (ClassCastException ex) {
        listaPersonas = new ArrayList<Cliente>();            
      }finally{
         }
     }
 
     public final void actualizar() {
         fireTableDataChanged();
     }    
     @Override
     public int getRowCount() {
         return listaPersonas.size();
     }
 
     @Override
     public int getColumnCount() {
         return cabeceras.length;
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         Object objeto = new Object();
         persona = (Cliente) listaPersonas.get(rowIndex);
         
         switch (columnIndex) {
             case 0:objeto = persona.getIdCliente();break; 
              /* No hay necesidad de mostrar este campo a menos q se 
              pida para nosotros seria nuestra manera de manejarlo*/ 
             case 1:objeto = persona.getCuit();break;
            case 2:objeto = persona.getRazonSocial()+" ";
                            break;
            case 3:objeto = persona.getDomicilio()+" "; 
                            break;
            
         }      
         return objeto;
     }
     
     private String[] cabeceras = {
         "IdCliente","Cuit", "RazonSocial", "Ddomicilio",
     };
     
     public Cliente Consulta(int col) {
         return (Cliente) listaPersonas.get(col);
     }
     
     @Override
     public String getColumnName(int column) {
         return cabeceras[column];
     }
     
     public void addElement(Object nuevoElemento){
         listaPersonas.add((Cliente)nuevoElemento);        
         actualizar();
     }
     
     public Cliente getCliente(int fila){
         return listaPersonas.get(fila);
     }
     
     public void removeElement(Object elemento)
     {
         listaPersonas.remove(elemento);
         actualizar();
     }
     
     
 }
 
