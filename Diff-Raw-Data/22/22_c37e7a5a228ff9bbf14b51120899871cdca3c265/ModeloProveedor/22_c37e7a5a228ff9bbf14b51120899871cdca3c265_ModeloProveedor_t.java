 /*
  * Cooperativa de Trabajo Tecnomática Ltda.
  */
 package co.tecnomati.java.controlcaja.modelo;
 
 import co.tecnomati.java.controlcaja.dominio.Proveedor;
 import java.util.ArrayList;
 import javax.swing.table.AbstractTableModel;
 import co.tecnomati.java.controlcaja.dominio.dao.imp.*;
 import java.util.Iterator;
 
 /**
  *
  * @author AnahiAramayo
  */
 public class ModeloProveedor extends AbstractTableModel{
  Proveedor persona = new Proveedor();
     ProveedorDaoImp personaDAO = new ProveedorDaoImp();
     ArrayList<Proveedor> listaPersonas = new ArrayList<Proveedor>();
     public ModeloProveedor() {            
      try {
         listaPersonas = (ArrayList<Proveedor>) personaDAO.listarProveedor();
      } catch (ClassCastException ex) {
        listaPersonas = new ArrayList<Proveedor>();            
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
         persona = (Proveedor) listaPersonas.get(rowIndex);
         
         switch (columnIndex) {
             case 0:objeto = persona.getIdProveedor();break; 
              /* No hay necesidad de mostrar este campo a menos q se 
              pida para nosotros seria nuestra manera de manejarlo*/ 
             case 1:objeto = persona.getCuit();break;
            case 2:objeto = persona.getRazonSocial()+" ";break;
            case 3:objeto = persona.getDomicilio()+" ";break;
             case 4:objeto = persona.getEstado();break;
         }      
         return objeto;
     }
     
     private String[] cabeceras = {
         "IdProveedor","Cuit", "RazonSocial", "domicilio","Estado",
     };
     
     public Proveedor Consulta(int col) {
         return (Proveedor) listaPersonas.get(col);
     }
     
     @Override
     public String getColumnName(int column) {
         return cabeceras[column];
     }
     
     public void addElement(Object nuevoElemento){
         listaPersonas.add((Proveedor)nuevoElemento);        
         actualizar();
     }
     
     public Proveedor getProveedor(int fila){
         return listaPersonas.get(fila);
     }
     
     public void removeElement(Object elemento)
     {
         listaPersonas.remove(elemento);
         actualizar();
     }
     
     
 }
