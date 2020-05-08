 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package WEB;
 
 import DAO.EstacionDAO;
     import Modelo.Dato;
     import Modelo.Estacion; 
 import Modelo.Micro;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.Window;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.zkoss.zhtml.Map;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Column;
 import org.zkoss.zul.Columns;
 import org.zkoss.zul.Grid;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listhead;
 import org.zkoss.zul.Listheader;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Row;
 import org.zkoss.zul.Rows;
 import net.sf.jasperreports.engine.export.*;
 /**
  *
  * @author athanatos
  */
 public class Reportes {
     
     private Window win;
 
     public Reportes(Window win) {
         this.win = win;
     }
 
     public Reportes() {
     }
     public Window getReportes() throws InterruptedException{
     Window wini = new Window();
     Listbox listbox = new Listbox();
     Listhead cabeza = new Listhead();
     Listheader cabeza1 = new Listheader();
     cabeza1.appendChild(new Label("Reportes"));                 //.setValue("Reportes");
     Listheader cabeza2 = new Listheader();
     cabeza.appendChild(cabeza1);
     cabeza.appendChild(cabeza2);
     
     listbox.appendChild(cabeza);
     Listitem item = new Listitem();
     Listcell cell = new Listcell(); 
     cell.appendChild(new Label("Estacion"));        //.setValue(new Label("estacion"));
     final Listcell cell2 = new Listcell();
     final Listbox select = new Listbox();
     select.setMold("select");
     select.setId("Esta");
     select.setRows(1);
         List allEstacion = new EstacionDAO().getLoadEstacion();
         for(int i=0;i<allEstacion.size(); i++){
            Listitem estacion = new Listitem();
            Estacion Est = new Estacion();
             Est = (Estacion) allEstacion.get(i);           
            estacion.setLabel(Est.getNombreestacion());
            estacion.setValue(Est.getIdEstacion());
            estacion.setParent(select);
         }
     
     cell2.appendChild(select);        
     select.addEventListener("onSelect", new EventListener() {
         public void onEvent(Event event) throws Exception {
             //Messagebox.show(" mesaje elejido:"+select.getSelectedItem().getValue());
                 int z = Integer.parseInt(select.getSelectedItem().getValue().toString());
              List listMicro = new EstacionDAO().getMicroByID(z);
               Listbox select2 = new Listbox();
                 select2.setMold("select");
                 select2.setId("Mic");
                 select2.setRows(1);
              for(int i=0;i<listMicro.size(); i++){
                Listitem micro = new Listitem();
                Micro Mic = new Micro();
                 Mic = (Micro) listMicro.get(i);           
                micro.setLabel(Mic.getNombremicro());
                micro.setValue(Mic.getIdMicro());
                micro.setParent(select2);
                } 
              //select2.setParent(cell2);
             cell2.appendChild(select2);
             
             Button GeneraReport = new Button();
             GeneraReport.setLabel("Generar Reporte");
             cell2.appendChild(GeneraReport);
            GeneraReport.addEventListener("onClick", new EventListener() {
           		 public void onEvent(Event event) throws Exception {
              
                             //crear iframe  y llamar a report.zul
                             
                             //pasar los parametros estacion, micro , sensor, frchas 
         	   }
      	  });
            
            
         }
    });
     //agregado
             
     item.appendChild(cell);
     item.appendChild(cell2);  
     listbox.appendChild(item);
         
     //listbox.appendItem("valor1", "1");
     wini.appendChild(listbox);
 
     return wini;
     }
     
 }
