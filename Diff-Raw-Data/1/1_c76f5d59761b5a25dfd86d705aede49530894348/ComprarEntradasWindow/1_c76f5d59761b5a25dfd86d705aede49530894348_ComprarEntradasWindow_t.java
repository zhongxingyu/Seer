 package edu.ub.bda.ubticket.windows;
 
 import com.googlecode.lanterna.gui.Action;
 import com.googlecode.lanterna.gui.Border;
 import com.googlecode.lanterna.gui.Window;
 import com.googlecode.lanterna.gui.component.Button;
 import com.googlecode.lanterna.gui.component.Label;
 import com.googlecode.lanterna.gui.component.Panel;
 import com.googlecode.lanterna.gui.component.Table;
 import com.googlecode.lanterna.gui.component.TextBox;
 import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
 import com.googlecode.lanterna.gui.dialog.MessageBox;
 import edu.ub.bda.UBTicket;
 import edu.ub.bda.ubticket.beans.Entrada;
 import edu.ub.bda.ubticket.beans.Espacio;
 import edu.ub.bda.ubticket.beans.Espectaculo;
 import edu.ub.bda.ubticket.beans.Sesion;
 import edu.ub.bda.ubticket.utils.AutenticacionServicio;
 import edu.ub.bda.ubticket.utils.HibernateTransaction;
 import java.util.Calendar;
 import java.util.List;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.Restrictions;
 
 /**
  *
  * @author olopezsa13
  */
 public class ComprarEntradasWindow extends Window
 {
     
     private final Button seleccionarEspacioButton;
     private final Espacio espacio = new Espacio();
     private final UBTicket ubticket;
     private Panel tablaPanel;
     private Table tabla;
     private Label paginaEtiqueta;
     private Panel paginadorPanel;
     
     /* private final TextBox dia;
     private final TextBox mes;
     private final TextBox anyo;
     
     private final TextBox hora;
     private final TextBox minuto; */
     
     private Integer maxFilas = 10;
     private Integer pagina = 0;
     private Integer maxPaginas = 0;
     
     public ComprarEntradasWindow(final UBTicket ubticket)
     {
         super("Comprar entradas");
         this.ubticket = ubticket;
         
         Panel espacioPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
         espacioPanel.addComponent(new Label(" Espacio seleccionado:"));
         seleccionarEspacioButton = new Button("Ninguno", new Action() {
 
             @Override
             public void doAction()
             {
                 List<Espacio> espacios = new HibernateTransaction<List<Espacio>>() {
 
                     @Override
                     public List<Espacio> run()
                     {
                         /**
                          * @TODO Hay que soportar listas paginadas de espacios
                          */
                         Query query = session.createQuery("from Espacio");
                         return (List<Espacio>) query.list();
                     }
 
                 }.execute();
                 
                 Espacio[] vector = new Espacio[espacios.size()];
                 espacios.toArray(vector);
                 Espacio esp = ListSelectDialog.showDialog(ubticket.getGUIScreen(), "ATENCIÓN", "Seleccione el espacio:", vector);
                 if ( esp != null )
                 {
                    pagina = 0;
                     espacio.copyFrom(esp);
                     seleccionarEspacioButton.setText(esp.getNombre());
                     paginadorPanel.setVisible(true);
                     actualizarTabla();
                 }
             }
         
         });
         espacioPanel.addComponent(seleccionarEspacioButton);
         addComponent(espacioPanel);
         
         Calendar calendar = Calendar.getInstance();        
         
         /* Panel diasPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
         diasPanel.addComponent(new Label(" DIA: "));
         diasPanel.addComponent(dia = new TextBox(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)), 3));
         diasPanel.addComponent(new Label("/"));
         diasPanel.addComponent(mes = new TextBox(Integer.toString(calendar.get(Calendar.MONTH)), 3));
         diasPanel.addComponent(new Label("/"));
         diasPanel.addComponent(anyo = new TextBox(Integer.toString(calendar.get(Calendar.YEAR)), 5));
         addComponent(diasPanel);
         
         Panel horasPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
         diasPanel.addComponent(new Label("HORA: "));
         diasPanel.addComponent(hora = new TextBox(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)), 2));
         diasPanel.addComponent(new Label(":"));
         diasPanel.addComponent(minuto = new TextBox(Integer.toString(calendar.get(Calendar.MINUTE)), 2));
         addComponent(horasPanel); */
         
         tablaPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
         addComponent(tablaPanel);
         
         paginadorPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);   
         paginadorPanel.setVisible(false);
         
         paginadorPanel.addComponent(new Button("<--", new Action() {
 
             @Override
             public void doAction()
             {
                 if ( pagina > 0 )
                 {
                     pagina--;
                     actualizarTabla();
                 }
             }
         
         }));
         
         paginaEtiqueta = new Label();
         paginaEtiqueta.setText(getTextoPagina());
         paginadorPanel.addComponent(paginaEtiqueta);
         
         paginadorPanel.addComponent(new Button("-->", new Action() {
 
             @Override
             public void doAction()
             {
                 if ( ( pagina + 1 ) < maxPaginas )
                 {
                     pagina++;
                     actualizarTabla();
                 }
             }
         
         }));
         
         addComponent(paginadorPanel);
         
         addComponent(new Button("Cancelar", new Action() {
 
             @Override
             public void doAction()
             {
                 close();
             }
         
         }));
     }
     
     @Override
     public void onVisible()
     {
         this.setFocus(seleccionarEspacioButton);
     }
     
     private void actualizarTabla()
     {
         actualizarPagina();
                 
         List<Sesion> list = new HibernateTransaction<List<Sesion>>() {
 
             @Override
             public List<Sesion> run()
             {
                 Criteria query = session.createCriteria(Sesion.class)
                         .add(Restrictions.eq("espacio", espacio));
                 query.setFirstResult(pagina * maxFilas);
                 query.setMaxResults(maxFilas);
                 return query.list();
             }
         
         }.execute();
         
         if ( list != null && list.size() > 0 )
         {
             boolean addComponent = false;
             if ( tabla == null )
             {
                 tabla = new Table(4, "Espectáculos");
                 addComponent = true;
             }
             else
             {
                 tabla.removeAllRows();
             }
             
             for ( Sesion sesion : list )
             {
                 Espectaculo o = sesion.getEspectaculo();
                 tabla.addRow(new Label(o.getTitulo()),
                         new Label(o.getCategoria().getNombre()),
                         new Label(sesion.getFecha_inicio().toString()),
                         new Button("Comprar por " + sesion.getPrecio().toString() + "€", new ComprarEntradaAction(ubticket, sesion)));
             }
         
             if ( addComponent )
             {
                 tablaPanel.addComponent(tabla);
             }
         }
     }
     
     private String getTextoPagina()
     {
         Integer pag = pagina + 1;
         return pag.toString() + " / " + maxPaginas.toString();
     }
     
     private void actualizarPagina()
     {
         Integer numFilas = new HibernateTransaction<Integer>() {
 
             @Override
             public Integer run()
             {
                 Query query = session.createSQLQuery("SELECT COUNT(*) FROM SESION WHERE ESPACIO_ID = :espacio_id");
                 query.setInteger("espacio_id", espacio.getId());
                 return (Integer) query.list().get(0);
             }
         
         }.execute();
         Double maxPaginasFP = numFilas.doubleValue() / maxFilas.doubleValue();
         maxPaginas = (int) Math.ceil(maxPaginasFP.doubleValue());
         paginaEtiqueta.setText(getTextoPagina());
     }
     
     private class ComprarEntradaAction implements Action
     {
         
         private Sesion sesion;
         private UBTicket ubticket;
         
         public ComprarEntradaAction(UBTicket ubticket, Sesion sesion)
         {
             this.sesion = sesion;
             this.ubticket = ubticket;
         }
 
         @Override
         public void doAction()
         {
             new HibernateTransaction() {
 
                 @Override
                 public Object run()
                 {
                     Entrada entrada = new Entrada();
                     entrada.setSesion(sesion);
                     entrada.setUsuario(AutenticacionServicio.GetUsuario());
                     
                     session.saveOrUpdate(entrada);
                     
                     return null;
                 }
             }.execute();
             
             MessageBox.showMessageBox(ubticket.getGUIScreen(), "ATENCIÓN", "Ha comprado una entrada de '" + sesion.getEspectaculo().getTitulo() + "'.");
         }
         
     }
     
 }
