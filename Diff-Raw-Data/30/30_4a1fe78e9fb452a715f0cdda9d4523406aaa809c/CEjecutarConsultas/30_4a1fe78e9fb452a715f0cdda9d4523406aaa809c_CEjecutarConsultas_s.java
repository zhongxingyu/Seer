 package es.miguelgonzalezgomez.dataBaseFun.qt.controladores;
 
 import com.trolltech.qt.gui.QTabWidget;
 import es.miguelgonzalezgomez.dataBaseFun.bd.ManejadorConsulta;
 import es.miguelgonzalezgomez.dataBaseFun.bd.ManejadorConsultaErrorSQL;
 import es.miguelgonzalezgomez.dataBaseFun.bd.ManejadorConsultaNoHayConexion;
 import es.miguelgonzalezgomez.dataBaseFun.gestionadores.GEditoresAplicacion;
 import es.miguelgonzalezgomez.dataBaseFun.modelos.MPestanaEditor;
import es.miguelgonzalezgomez.dataBaseFun.modelos.PestanaEditorListener;
 import es.miguelgonzalezgomez.dataBaseFun.qt.PanelConsultas;
 import es.miguelgonzalezgomez.dataBaseFun.qt.modals.ModalMostrarAviso;
 import es.miguelgonzalezgomez.dataBaseFun.qt.TablaResultadoConsulta;
 import java.util.List;
 
 /**
  *
  * @author Miguel González
  */
 public class CEjecutarConsultas {
 
     private CEjecutarConsultasEscuchaCambios escuchaCambiosConsultas;
     private GEditoresAplicacion editoresAplicacion;
     private PanelConsultas panelConsultas;
     
     private MPestanaEditor mPestanaEditor;
     private ManejadorConsulta manejadorConsultas;
     
     public CEjecutarConsultas() {
         panelConsultas = new PanelConsultas(this);
         escuchaCambiosConsultas = new CEjecutarConsultasEscuchaCambios(this);
         editoresAplicacion = new GEditoresAplicacion();
         
         inicializarEscuchaCambios();
     }
     
     private void inicializarEscuchaCambios() {
         editoresAplicacion.addPestanasEditorListener(escuchaCambiosConsultas);
     }
 
     public void lanzarConsultaTexto(MPestanaEditor mPestanaEditor) {
         this.mPestanaEditor = mPestanaEditor;
 
         manejadorConsultas = new ManejadorConsulta(
                 mPestanaEditor.mConexion,
                 getConsultaFormateada()
         );
         
         lanzarConsultasYPintarlas();
     }
     
     public QTabWidget getPanelConsultas() {
         return panelConsultas;
         
     }
     
     private void lanzarConsultasYPintarlas() {
         try {
             while(manejadorConsultas.next()) {
                 manejadorConsultas.conectarContraBaseDeDatos(getConsultaFormateada());
                 
                 if(manejadorConsultas.isEjecutarQuery()) {
                     manejadorConsultas.lanzarConsultaActual();
                     pintarResultadoConsultaActual();
                 }
                 manejadorConsultas.cerrarConsultaSQL();
             }
         } catch (ManejadorConsultaErrorSQL ex) {
             mostrarErrorEnPantalla(
                     "SQL Exception",
                     ex.getMessage()
             );
         } catch (ManejadorConsultaNoHayConexion ex) {
             mostrarErrorEnPantalla(
                     "Connection exception",
                     ex.getMessage()
             );
         }
     }
     
     private void mostrarErrorEnPantalla(String tituloError, String mensajeError) {
         ModalMostrarAviso.mostrarErrorEnPantalla(tituloError,
                 mensajeError);
     }
     
     private void pintarResultadoConsultaActual() throws ManejadorConsultaErrorSQL {
         TablaResultadoConsulta tablaResultado = new TablaResultadoConsulta(this);
         
         List<String> columnas = manejadorConsultas.getNombresColumnas();
         tablaResultado.establecerColumnas(columnas);
         
         while(manejadorConsultas.haySiguienteFila()) {
             List<String> datosFila = manejadorConsultas.getFila();
             
             tablaResultado.anadirDatosFila(datosFila);
         }
 
         panelConsultas.addTab(
                 "Query",
                 tablaResultado
         );
     }
     
     private String getConsultaFormateada() {
         String consultaSQL;
         
         if(mPestanaEditor.hayTextoSeleccionado) {
             consultaSQL = mPestanaEditor.textoSeleccionado;
         } else {
             consultaSQL = mPestanaEditor.contenidoTexto;
         }
         
         consultaSQL= consultaSQL.replaceAll("\\r", "").
                 replaceAll("\\n", " ").
                 replaceAll("\\r\\n", " ").
                 replaceAll("" + (char)8233, "");
         consultaSQL = consultaSQL.trim();
         
         return consultaSQL;
     }
 }
