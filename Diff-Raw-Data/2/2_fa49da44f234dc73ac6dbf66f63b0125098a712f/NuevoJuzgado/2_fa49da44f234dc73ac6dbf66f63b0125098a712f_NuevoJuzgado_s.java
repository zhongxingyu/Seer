 package gui;
 
 import net.rim.device.api.ui.component.Dialog;
 import persistence.Persistence;
 import core.Juzgado;
 
 public class NuevoJuzgado {
 	private Juzgado _juzgado;
 	private NuevoJuzgadoScreen _screen;
 
 	/**
 	 * Crea un NuevoJuzgado, asociando a ese una pantalla
 	 */
 	public NuevoJuzgado() {
 		_screen = new NuevoJuzgadoScreen();
 	}
 
 	/**
 	 * @return La pantalla asociada al objeto
 	 */
 	public NuevoJuzgadoScreen getScreen() {
 		return _screen;
 	}
 
 	/**
 	 * @return El nuevo Juzgado, s este no ha sido creado y aguardado con
 	 * guardarJuzgado(); se llama dicho mtodo
 	 */
 	public Juzgado getJuzgado() {
 		if(_juzgado == null) {
 			guardarJuzgado();
 		}
 		return _juzgado;
 	}
 
 	/**
 	 * Crea el nuevo Juzgado en base a los datos capturados desde la pantalla
 	 * y guardandolo en la base de datos
 	 */
 	public void guardarJuzgado() {
 		Persistence guardado = null;
 		try {
 			guardado = new Persistence();
 		} catch (Exception e) {
 			Dialog.alert(e.toString());
 		}
 		_juzgado = new Juzgado(_screen.getNombre(), _screen.getCiudad(),
 				_screen.getDireccion(), _screen.getTelefono(),
				_screen.getTelefono());
 		try {
 			guardado.guardarJuzgado(_juzgado);
 		} catch (Exception e) {
 			Dialog.alert(e.toString());
 		}
 	}
 }
