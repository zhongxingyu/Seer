 package gui;
 
 import net.rim.device.api.ui.component.Dialog;
 import persistence.Persistence;
 import core.Juzgado;
 
 public class VerJuzgado {
 	private VerJuzgadoScreen _screen;
 	private Juzgado _juzgado;
 
 	public VerJuzgado(Juzgado juzgado) {
 		_screen = new VerJuzgadoScreen(juzgado);
 		_juzgado = juzgado;
 	}
 
 	public VerJuzgadoScreen getScreen() {
 		return _screen;
 	}
 
 	public void actualizarJuzgado() {
 		if (_screen.isGuardado()) {
 			try {
 				Persistence persistence = new Persistence();
 				boolean cambio = false;
 				Juzgado juzgado = _screen.getJuzgado();
 
 				if (!juzgado.getNombre().equals(_screen.getNombre()))
 					cambio = true;
 				if (!juzgado.getCiudad().equals(_screen.getCiudad()))
 					cambio = true;
 				if (!juzgado.getTelefono().equals(_screen.getTelefono()))
 					cambio = true;
 				if (!juzgado.getDireccion().equals(_screen.getDireccion()))
 					cambio = true;
 				if (!juzgado.getTipo().equals(_screen.getTipo()))
 					cambio = true;
 
 				if (cambio) {
 					_juzgado = new Juzgado(_screen.getNombre(),
 							_screen.getCiudad(), _screen.getDireccion(),
							_screen.getTelefono(), _screen.getTipo(), _juzgado.getId_juzgado());
 					persistence.actualizarJuzgado(_juzgado);
 				}
 			} catch (Exception e) {
 				Dialog.alert("actualizarJuzgado -> " + e.toString());
 			}
 		}
 	}
 
 	public Juzgado getJuzgado() {
 		return _juzgado;
 	}
 }
