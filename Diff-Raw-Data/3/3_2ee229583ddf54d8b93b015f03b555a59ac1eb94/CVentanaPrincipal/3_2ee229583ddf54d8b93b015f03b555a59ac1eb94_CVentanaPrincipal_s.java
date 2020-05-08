 package es.miguelgonzalezgomez.dataBaseFun.qt.controladores;
 
 import com.google.gson.reflect.TypeToken;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.core.Qt.WindowStates;
 import es.miguelgonzalezgomez.dataBaseFun.configuracion.ConfiguracionVentanaPrincipal;
 import es.miguelgonzalezgomez.dataBaseFun.Preferencias;
 import es.miguelgonzalezgomez.dataBaseFun.PreferenciasException;
 import es.miguelgonzalezgomez.dataBaseFun.estilos.ObtencionEstilo;
 import es.miguelgonzalezgomez.dataBaseFun.qt.VentanaPrincipal;
import es.miguelgonzalezgomez.dataBaseFun.qt.PanelMostrarConsultasRealizadas;
 import java.lang.reflect.Type;
 
 /**
  *
  * @author Miguel González
  */
 public class CVentanaPrincipal {
     
     private Preferencias prefs;
     private static String nombreFicheroPreferencias = "confVentanaPrincipal";
     public VentanaPrincipal ventanaPrincipal;
     private ConfiguracionVentanaPrincipal configuracionVentanaPrincipal;
     
     private CMenuSuperior controladorMenuSup;
     private CWidgetPestanasEditores controladorPestanasEditores;
     private CPanelMostrarConsultasRealizadas controladorConsultasRealizadas;
     
     public CVentanaPrincipal() {
         controladorMenuSup = new CMenuSuperior();
         controladorPestanasEditores = new CWidgetPestanasEditores();
         controladorConsultasRealizadas = new CPanelMostrarConsultasRealizadas();
         
         prefs = Preferencias.getInstance();
         
         cargarPropiedadesVentanaPrincipal();
         crearYPosicionarVentana();
         
         establecerMenuSuperior();
         establecerPestanasEditores();
         establecerWidgets();
         
         mostrarVentanaPrincipal();
     }
     
     private void cargarPropiedadesVentanaPrincipal() {
         Type fooType = new TypeToken<ConfiguracionVentanaPrincipal>() {}.getType();
         try {
             configuracionVentanaPrincipal = prefs.getConfiguracion(
                     fooType,
                     nombreFicheroPreferencias);
         } catch (PreferenciasException ex) {
            ex.printStackTrace();
             cargarPropiedadesDefectoVentanaPrincipal();
         }
     }
     
     private void cargarPropiedadesDefectoVentanaPrincipal() {
         configuracionVentanaPrincipal = new ConfiguracionVentanaPrincipal();
         configuracionVentanaPrincipal.estadoVentana = new WindowStates();
         configuracionVentanaPrincipal.width = 300;
         configuracionVentanaPrincipal.height = 200;
         configuracionVentanaPrincipal.posX = 100;
         configuracionVentanaPrincipal.posY = 100;
     }
     
     private void crearYPosicionarVentana() {
         crearVentana();
         posicionarVentanaPrincipal();
     }
     
     private void crearVentana() {
         ventanaPrincipal = new VentanaPrincipal(
                 "DataBaseFun",
                 this);
         ventanaPrincipal.setStyleSheet(
                 ObtencionEstilo.getEstiloVentana("ventanaPrincipal.css")
         );
     }
     
     private void establecerMenuSuperior() {
         ventanaPrincipal.setMenuBar(
                 controladorMenuSup.getVistaMenuSuperior()
         );
     }
     
     private void establecerPestanasEditores() {
         ventanaPrincipal.setCentralWidget(
                 controladorPestanasEditores.
                         getVistaPestanasEditores()
         );
     }
     
     private void establecerWidgets() {
         ventanaPrincipal.addDockWidget(
                 Qt.DockWidgetArea.BottomDockWidgetArea,
                 controladorConsultasRealizadas.getPanelMostrarConsultasRealizadas(),
                 Qt.Orientation.Horizontal);
     }
      
     private void posicionarVentanaPrincipal() {
         ventanaPrincipal.resize(
                 configuracionVentanaPrincipal.width,
                 configuracionVentanaPrincipal.height);
         ventanaPrincipal.move(
                 configuracionVentanaPrincipal.posX,
                 configuracionVentanaPrincipal.posY);
         ventanaPrincipal.setWindowState(
                 configuracionVentanaPrincipal.estadoVentana);
         
     }
     
     private void mostrarVentanaPrincipal() {
         ventanaPrincipal.show();
     }
     
     public void salirAplicacion() {
         guardarEstadoVentana();
         guardarPreferencias();
         
         ventanaPrincipal.close();
     }
     
     private void guardarEstadoVentana() {
         configuracionVentanaPrincipal.estadoVentana = ventanaPrincipal.windowState();
         configuracionVentanaPrincipal.width = ventanaPrincipal.size().width();
         configuracionVentanaPrincipal.height = ventanaPrincipal.size().height();
         configuracionVentanaPrincipal.posX = ventanaPrincipal.pos().x();
         configuracionVentanaPrincipal.posY = ventanaPrincipal.pos().y();
     }
     
     private void guardarPreferencias() {
         Type fooType = new TypeToken<ConfiguracionVentanaPrincipal>() {}.getType();
         prefs.guardarConfiguracion(fooType,
                 configuracionVentanaPrincipal,
                 nombreFicheroPreferencias);
     }
 }
