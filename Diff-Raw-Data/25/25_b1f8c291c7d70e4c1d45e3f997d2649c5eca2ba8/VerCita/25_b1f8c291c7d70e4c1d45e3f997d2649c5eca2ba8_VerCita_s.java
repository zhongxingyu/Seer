 package gui.Ver;
 
 import gui.Cita;
 import gui.Util;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 
 public class VerCita {
 	private VerCitaPopUp _screen;
 	private Cita _cita;
 
 	public VerCita(Cita cita) {
 		_cita = cita;
 
 		Object[] choices = { Cita.MINUTOS, Cita.HORAS, Cita.DIAS };
 
 		_screen = new VerCitaPopUp();
 		_screen.setChangeListener(listener);
 		_screen.setDescripcion(_cita.getDescripcion());
 		_screen.setFecha(_cita.getFecha());
 		_screen.setChoices(choices);
 		if (_cita.hasAlarma()) {
 			_screen.setAlarma(_cita.getAlarmaConFormato());
 			_screen.setChecked(true);
 		} else {
 			Object[] alarma = { new Integer(5), Cita.MINUTOS };
 			_screen.setAlarma(alarma);
 		}
 	}
 
 	FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (context == _screen.GUARDAR) {
 				actualizarCita();
 			} else if (context == _screen.CERRAR) {
 				cerrarPantalla();
 			}
 		}
 	};
 
 	public VerCitaPopUp getScreen() {
 		return _screen;
 	}
 
 	private void actualizarCita() {
 		if (_screen.getDescripcion().equals("")) {
 			Util.alert("La descripcin est vaca!. Por favor ingrese algo");
 		} else {
 			if (_screen.hasAlarma()) {
 				_cita.setAlarma(_screen.getAlarma());
 			} else {
 				_cita.setAlarma(0);
 			}
 			_cita.setDescripcion(_screen.getDescripcion());
 			_cita.setFecha(_screen.getFecha());
			_cita.guardarCita();
 			Util.popScreen(_screen);
 		}
 	}
 
 	private void cerrarPantalla() {
 		Util.popScreen(_screen);
 	}
 }
