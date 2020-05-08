 package gui;
 
 import persistence.Persistence;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 import core.Preferencias;
 
 public class PreferenciasGenerales {
 
 	private PreferenciasGeneralesScreen _screen;
 
 	public PreferenciasGenerales() {
 
 		_screen = new PreferenciasGeneralesScreen();
 		_screen.setNombreUsuario(Preferencias.getNombreUsuario());
 		_screen.setMostrarBusqueda(Preferencias.isMostrarCampoBusqueda());
 		_screen.setMostrarTitulos(Preferencias.isMostrarTitulosPantallas());
 		_screen.setPantallaInicial(Preferencias.getPantallaInicial());
 		_screen.setCantidadActuacionesCriticas(Preferencias
 				.getCantidadActuacionesCriticas());
 		_screen.setRecordarUltimaCategoria(Preferencias
 				.isRecodarUltimaCategoria());
 		_screen.setFuente(Preferencias.getTipoFuente());
 		_screen.setChangeListener(listener);
 	}
 
 	private FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			switch (context) {
 			case Util.GUARDAR:
 				guardarPreferencias();
 				break;
 			case Util.COPIA_SEGURIDAD:
 				copiaDeSeguridad();
 				break;
 			case Util.RESTAURAR_PREFERENCIAS:
 				restaurarPreferencias();
 				break;
 			case Util.LLAVES:
 				activacion();
 				break;
 			}
 		}
 	};
 
 	public PreferenciasGeneralesScreen getScreen() {
 		return _screen;
 	}
 
 	private void guardarPreferencias() {
 		Preferencias.setNombreUsuario(_screen.getNombreUsuario());
 		Preferencias.setMostrarCampoBusqueda(_screen.isMostrarBusqueda());
 		Preferencias.setMostrarTitulosPantallas(_screen.isMostrarTitulos());
 		Preferencias.setRecodarUltimaCategoria(_screen
 				.isRecordarUltimaCategoria());
 		Preferencias.setTipoFuente(_screen.getFuente());
 		Preferencias.setPantallaInicial(_screen.getPantallaInicial());
 		Preferencias.setCantidadActuacionesCriticas(_screen
 				.getCantidadActuacionesCriticas());
 		try {
 			Persistence per = new Persistence();
 			per.actualizarPreferencias();
 		} catch (NullPointerException npe) {
 			_screen.alert(Util.noSDString());
 			System.exit(0);
 		} catch (Exception e) {
 			_screen.alert("No se han podido guardar las preferencias. Error Desconocido");
 		}
 		Util.popScreen(_screen);
 	}
 
 	private void copiaDeSeguridad() {
 		Backup b = new Backup();
 		UiApplication.getUiApplication().pushModalScreen(b.getScreen());
 		b = null;
 	}
 
 	private void restaurarPreferencias() {
 		try {
 			Persistence per = new Persistence();
 			per.borrarPreferencias();
 		} catch (NullPointerException npe) {
 			_screen.alert(Util.noSDString());
 			System.exit(0);
 		} catch (Exception e) {
 			_screen.alert("No se han podido guardar las preferencias. Error Desconocido");
 		}
 		Util.popScreen(_screen);
 	}
 
 	private void activacion() {
 		Llaves llaves = new Llaves();
 		Util.pushModalScreen(llaves.getScreen());
 	}
 }
