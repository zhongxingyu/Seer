 package code.google.com.opengis.gestion;
 
 import java.sql.ResultSet;
 
 import javax.swing.JOptionPane;
 
 import code.google.com.opengis.gestionDAO.ConectarDBA;
 import code.google.com.opengis.gestionDAO.Idioma;
 
 /**
  * @author kAStRo! Esta clase nos permite validar los datos de un Apero.
  * 
  */
 
 public class Apero {
 	public int idApero;
 	public String nomApero;
 	public int tamApero;
 	public String descApero;
 	public int idTarea_Apero;
 	public boolean activ_Apero;
 	public String idUser;
 
 	// ////////// C O N S T R U C T O R ////////////////
 
 	public Apero(int id, String nombre, int tamao, String descrip, int tarea,
 			boolean activo, String idUser) {
 
 		this.idApero = id;
 		this.nomApero = nombre;
 		this.tamApero = tamao;
 		this.descApero = descrip;
 		this.idTarea_Apero = tarea;
 		this.activ_Apero = activo;
 		this.idUser = idUser;
 
 	}
 
 	// ///////// M E T O D O S G E T T E R A N D S E T T E R /////////
 
 	// ID APERO
 	public int getIdApero() {
 		return idApero;
 	}
 
 	public void setIdApero(int idApero) {
 		String str = idApero + ""; //$NON-NLS-1$
 		if ((str.length() > 1) && (str.length() < 9)) { // comprobamos que
 														// idapero tenga entre 1
 														// y 8 digitos
 			this.idApero = idApero;
 		} else {
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgImplementIdBetween1And8")); //$NON-NLS-1$
 		}
 	}
 
 	// NOMBRE APERO
 	public String getNomApero() {
 		return nomApero;
 	}
 
 	public void setNomApero(String nomApero) {
 		Boolean nm = isInteger(nomApero);
 		if (nm.equals(true) || nomApero.length() < 2 || nomApero.length() > 20) {
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgImplementNameNumericNorEmpty")); //$NON-NLS-1$
 		} else {
 			this.nomApero = nomApero;
 		}
 	}
 
 	// TAMAO APERO
 	public int getTamApero() {
 		return tamApero;
 	}
 
 	public void setTamApero(int tamApero) {
 		if (this.tamApero <= 0 || this.tamApero > 15) {
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgImplementSizeCharsNorNegative")); //$NON-NLS-1$
 		} else {
 			this.tamApero = tamApero;
 		}
 	}
 
 	// DESCRIPCION APERO
 	public String getDescApero() {
 		return descApero;
 	}
 
 	public void setDescApero(String descApero) {
 		if (descApero.length() > 30 || descApero.length() <= 0) {
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgImplementWrongDesc")); //$NON-NLS-1$
 		} else {
 			this.descApero = descApero;
 		}
 	}
 
 	// TAREA APERO
 	public int getIdTarea_Apero() {
 		return idTarea_Apero;
 	}
 
 	public void setIdTarea_Apero(int idTarea_Apero) {
 		if (idTarea_Apero < 1 || idTarea_Apero > 4) {
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgImplementWrongTask")); //$NON-NLS-1$
 		} else {
 			this.idTarea_Apero = idTarea_Apero;
 		}
 	}
 
 	// APERO ACTIVO
 	public boolean isActiv_Apero() {
 		return activ_Apero;
 	}
 
 	public void setActiv_Apero(boolean activ_Apero) {
 		this.activ_Apero = activ_Apero;
 	}
 
 	// USUARIO APERO
 	public String getIdUser() {
 		return idUser;
 	}
 
 	public void setIdUser(String idUser) {
 		if (idUser.length() != 9) { // El DNI del propietario debe tener el
 									// siguiente formato: ########L
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgImplementWrongOwner")); //$NON-NLS-1$
 		} else {
 			this.idUser = idUser;
 		}
 	}
 
 	// /////////// M E T O D O D E V A L I D A C I O N //////////////////
 	public Boolean validarDatos(String id, String nombre, String tamao,
 			String descrip, String tarea, String activo, String idUser) {
 
 		if (idUser.length() != 9) {
 			JOptionPane.showMessageDialog(null,
 				Idioma.getString("msgImplementWrongOwner")); //$NON-NLS-1$
 			
 			return false;
 		} else {
 			
 
 			if (ValidacionDatos.validarTexto(this.nomApero, "nombre") == false) {
 				
 				return false;
 			} else {
 				
 				if (nombre.length() > 20) {
 
 					JOptionPane
 							.showMessageDialog(
 									null,
 									Idioma.getString("msgImplementNameNumericNorEmpty")); //$NON-NLS-1$
 				
 					return false;
 
 				} else {
 						
						if (this.tamApero <= 0 || this.tamApero > 500) {
 							JOptionPane
 									.showMessageDialog(
 											null,
 											Idioma.getString("msgImplementSizeCharsNorNegative")); //$NON-NLS-1$
 							return false;
 
 						} else {
 							if (descrip.length() > 30 || descrip.length() <= 0) {
 								JOptionPane.showMessageDialog(null, Idioma
 										.getString("msgImplementWrongDesc")); //$NON-NLS-1$
 								return false;
 							} else {
 								int tar = Integer.parseInt(tarea);
 								if (tar < 1 || tar > 4) {
 									JOptionPane.showMessageDialog(null, Idioma
 											.getString("msgImplementNotTask")); //$NON-NLS-1$
 									return false;
 								} else {
 
 									boolean usuarioValido = false;
 
 									try {
 										
 
 										ConectarDBA dba = new ConectarDBA();
 
 										dba.acceder();
 
 										String sql = "SELECT dni FROM usuario WHERE dni= '"
 												+ idUser + "'";
 
 										ResultSet resu = dba.buscar(sql);
 
 										resu.next();
 
 										if (resu.getString(1) != null) {
 
 											usuarioValido = true;
 
 										} else {
 
 											usuarioValido = false;
 
 										}
 
 										dba.cerrarCon();
 
 									} catch (Exception e2) {
 
 									}
 
 									if (usuarioValido == false) {
 
 										JOptionPane
 												.showMessageDialog(
 														null,
 														Idioma.getString("msgErrorIDUnmatchUser"));
 
 										return false;
 
 									} else {
 										System.out.println("Validacion OK!"); //$NON-NLS-1$
 										return true;
 
 									}
 								}
 							}
 						}
 					
 				}
 			}
 		}
 	}
 
 	// //////////// M E T O D O S A L T E R N A T I V O S /////////////////
 
 	public boolean isInteger(String input) {
 		try {
 			Integer.parseInt(input);
 			return true;
 		} catch (Exception e2) {
 			return false;
 		}
 	}
 
 	public boolean esDecimal(String cad) {
 		try {
 			Double.parseDouble(cad);
 			return true;
 		} catch (NumberFormatException nfe) {
 			return false;
 		}
 	}
 }
