 package gui;
 
 import java.util.Date;
 
 import javax.microedition.pim.Event;
 import javax.microedition.pim.PIMItem;
 
 import net.rim.blackberry.api.pdap.BlackBerryEvent;
 import core.CalendarManager;
 
 public class Cita {
 
 	private String _descripcion;
 	private Date _fecha;
 	private int _alarma;
 	private String _uid;
 
 	public static final String DIAS = "Das";
 	public static final String HORAS = "Horas";
 	public static final String MINUTOS = "Minutos";
 
 	public Cita(String descripcion, Date date, int alarma, String uid) {
 		_descripcion = descripcion;
 		_fecha = date;
 		_alarma = alarma;
 		_uid = uid;
 	}
 
 	/*
 	 * public Cita(String descripcion, Date date) { this(descripcion, date, 0,
 	 * null); }
 	 * 
 	 * public Cita(String descripcion, Date date, String uid) {
 	 * this(descripcion, date, 0, uid); }
 	 * 
 	 * public Cita(String descripcion, Date date, int alarma) {
 	 * this(descripcion, date, alarma, null); }
 	 */
 
 	public Cita() {
 		this(null, null, 0, null);
 	}
 
 	public Cita(String uid) {
 		try {
 			BlackBerryEvent e = CalendarManager.consultarCita(uid);
 			_fecha = new Date(e.getDate(Event.START, PIMItem.ATTR_NONE));
 			_descripcion = e.getString(Event.SUMMARY, PIMItem.ATTR_NONE);
 			if (e.countValues(Event.ALARM) > 0) {
 				_alarma = e.getInt(Event.ALARM, PIMItem.ATTR_NONE);
 			}
 			_uid = uid;
 		} catch (NullPointerException e) {
 			_uid = null;
 		} catch (Exception e) {
 			Util.alert(e.toString());
 		}
 	}
 
 	public Object[] getAlarmaConFormato() {
 		Object[] ret = new Object[2];
 		if (_alarma >= 86400) {
 			ret[0] = new Integer(_alarma /= 86400);
 			ret[1] = DIAS;
 		} else if (_alarma >= 3600) {
 			ret[0] = new Integer(_alarma /= 3600);
 			ret[1] = HORAS;
 		} else if (_alarma >= 60) {
 			ret[0] = new Integer(_alarma /= 60);
 			ret[1] = MINUTOS;
 		}
 		return ret;
 	}
 
 	public void eliminarCita() {
 		try {
 			CalendarManager.borrarCita(_uid);
 		} catch (NullPointerException e) {
 
 		} catch (Exception e) {
 			Util.alert(e.toString());
 		} finally {
 			_uid = null;
 		}
 	}
 
 	public void guardarCita() {
 		try {
 			if (hasAlarma()) {
 				_uid = CalendarManager.agregarCita(_fecha, _descripcion,
 						_alarma);
 			} else {
 				_uid = CalendarManager.agregarCita(_fecha, _descripcion);
 			}
 		} catch (Exception e) {
 			Util.citaErrorMessage();
 		}
 	}
 
 	public void actualizarCita() {
 		try {
 			if (hasAlarma()) {
 				CalendarManager.actualizarCita(_uid, _fecha, _descripcion,
 						_alarma);
 			} else {
 				CalendarManager.actualizarCita(_uid, _fecha, _descripcion);
 			}
 		} catch (NullPointerException e) {
 			guardarCita();
 		} catch (Exception e) {
 			Util.citaErrorMessage();
 		}
 	}
 
 	public String getDescripcion() {
 		return _descripcion;
 	}
 
 	public boolean exist() {
 		if (_uid == null) {
 			return false;
 		} else {
 			try {
 				CalendarManager.consultarCita(_uid);
 			} catch (NullPointerException e) {
 				return false;
 			} catch (Exception e) {
 				Util.alert(e.toString());
 				return false;
 			}
 			return true;
 		}
 	}
 
	public int getAlarma() {
		return _alarma;
	}

 	public Date getFecha() {
 		return _fecha;
 	}
 
 	public String getUid() {
 		return _uid;
 	}
 
 	public void setDescripcion(String descripcion) {
 		_descripcion = descripcion;
 	}
 
 	public void setAlarma(int alarma) {
 		_alarma = alarma;
 	}
 
 	public void setAlarma(Object[] alarma) {
 		String intervalo = (String) alarma[1];
 		Integer duracion = (Integer) alarma[0];
 		if (intervalo == DIAS) {
 			_alarma = duracion.intValue() * 86400;
 		} else if (intervalo == HORAS) {
 			_alarma = duracion.intValue() * 3600;
 		} else if (intervalo == MINUTOS) {
 			_alarma = duracion.intValue() * 60;
 		}
 	}
 
 	public void setFecha(Date fecha) {
 		_fecha = fecha;
 	}
 
	public void setUid(String uid) {
		_uid = uid;
	}

 	public boolean hasAlarma() {
 		if (_alarma != 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
