 package code.google.com.opengis.gestion;
 
 import javax.swing.JOptionPane;
 
 import code.google.com.opengis.gestionDAO.Idioma;
 
 public class ValidacionDatos {
 
 	static String fechaValida;
 
 	public static Boolean getValido() {
 
 		return valido;
 
 	}
 
 	public static String getFecha() {
 
 		return fechaValida;
 
 	}
 
 	private static boolean valido;
 
 	/**
 	 * Este mtodo validar los datos que el usuario introduzca en la clase
 	 * visual. En el caso de que algn dato no sea correcto mostrar un mensaje
 	 * de error y cargar valido con False. A la hora de llamar los siguientes
 	 * mtodos comprobaremos si este nos ha dado FALSE, en ese caso no podremos
 	 * hacer nada hasta que no corrijamos el error.
 	 * 
 	 */
 	public static String calcularDNI(String dni) {
 		dni = dni.trim();
 		int pletra;
 		String aux = ""; //$NON-NLS-1$
 		String[] arrayLetra = { "T", "R", "W", "A", "G", "M", "Y", "F", "P", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
 				"D", "X", "B", "N", "J", "Z", "S", "Q", "V", "H", "L", "C", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
 				"K", "E", "T" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		for (int x = 0; x < 8; x++) {// este for nos cojera los primeros
 										// carcteres y los guardara en el
 										// string aux
 			aux = aux + dni.charAt(x);
 		}
 		pletra = Integer.parseInt(aux);
 		pletra = pletra % 23;
 		aux = aux + arrayLetra[pletra];
 
 		return dni;
 	}
 
 	/**
 	 * Clase que valida el DNI, y lo coloca en formato 00000000L
 	 * 
 	 * @param dni
 	 * @return boolean que nos determinar si el dni introducido es verdadero o
 	 *         falso.
 	 */
 	public static boolean validarDni(String dni) {
 		dni = dni.trim();
 		int pletra;
 		String aux = ""; //$NON-NLS-1$
 		String aux2 = ""; //$NON-NLS-1$
 		String[] arrayLetra = { "T", "R", "W", "A", "G", "M", "Y", "F", "P", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
 				"D", "X", "B", "N", "J", "Z", "S", "Q", "V", "H", "L", "C", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
 				"K", "E", "T" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		if (dni.length() != 9) {
 			JOptionPane.showMessageDialog(null,
 					Idioma.getString("msgIdCardErrorManyChars")); //$NON-NLS-1$
 
 			valido = false;
 			return false;
 		} else {
 			for (int x = 0; x < 8; x++) {// este for nos cojera los 8 primeros
 											// carcteres y los guardar en el
 											// string aux
 				aux = aux + dni.charAt(x);
 			}
 			try {
 				pletra = Integer.parseInt(aux); // si no fueran enteros
 												// saldriamos
 												// del metodo con un false
 			} catch (NumberFormatException ex) {
 				JOptionPane.showMessageDialog(null,
 						Idioma.getString("msgIdCardErrorNumbers")); //$NON-NLS-1$
 				valido = false;
 				return false;
 			}
 			pletra = pletra % 23;
 			aux2 = dni.charAt(dni.length() - 1) + ""; //$NON-NLS-1$
 
 			if (arrayLetra[pletra].equalsIgnoreCase(aux2)) {
 				dni = aux + arrayLetra[pletra];// lo colocamos en formato de 9
 												// carcteres
 				return true;
 			} else {
 				JOptionPane.showMessageDialog(null,
 						Idioma.getString("msgIdCardUnmatchWord")); //$NON-NLS-1$
 				valido = false;
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * Mtodo que comprueba si el texto que le pasamos es numrico o est vacio.
 	 * 
 	 * @param texto
 	 *            Texto a comprobar
 	 * @return devuelve si el texto es vlido o no.
 	 */
 	public static boolean validarTexto(String texto, String nombreCampo) {
 		boolean r = isInteger(texto);
 		texto = texto.trim();
 		String comilla = "'";
 		for (int i = 0; i < texto.length(); i++) {
 			if (Character.isLetter(texto.charAt(i)) == false
 					&& texto.charAt(i) != (' ')
 					|| texto.charAt(i) == comilla.charAt(0)) {
 				JOptionPane.showMessageDialog(
 						null,
 						Idioma.getString("msgErrorField") //$NON-NLS-1$
 								+ nombreCampo
 								+ Idioma.getString("msgErrorNotSpecialChar")); //$NON-NLS-1$
 				valido = false;
 
 				return false;
 
 			}
 			if (texto.charAt(i) == ' ' && texto.charAt(i - 1) == ' ') {
 
 				JOptionPane.showMessageDialog(
 						null,
 						Idioma.getString("msgErrorField") //$NON-NLS-1$
 								+ nombreCampo
 								+ Idioma.getString("msgErrorBlankSpace")); //$NON-NLS-1$
 				return false;
 			}
 		}
 
 		if (r == true || texto.length() < 2) {
 			JOptionPane.showMessageDialog(
 					null,
 					Idioma.getString("msgErrorField") //$NON-NLS-1$
 							+ nombreCampo
 							+ Idioma.getString("msgErrorEmptyNorNumeric")); //$NON-NLS-1$
 			valido = false;
 
 			return false;
 
 		} else {
 
 			return true;
 
 		}
 	}
 
 	public static boolean validarTextoEspecial(String texto, String nombreCampo) {
 		boolean r = isInteger(texto);
 		texto = texto.trim();
 		String comilla = "'";
 		for (int i = 0; i < texto.length(); i++) {
 			System.out.println(i);
 			if (texto.charAt(i) == comilla.charAt(0)) {
 				JOptionPane.showMessageDialog(
 						null,
 						Idioma.getString("msgErrorField") //$NON-NLS-1$
 								+ nombreCampo
 								+ Idioma.getString("msgErrorNotSpecialChar")); //$NON-NLS-1$
 				return false;
 			}
 			if (texto.charAt(i) == ' ' && texto.charAt(i - 1) == ' ') {
 
 				JOptionPane.showMessageDialog(
 						null,
 						Idioma.getString("msgErrorField") //$NON-NLS-1$
 								+ nombreCampo
 								+ Idioma.getString("msgErrorBlankSpace")); //$NON-NLS-1$
 				return false;
 
 			}
 		}
 
 		if (r == true || texto.length() < 2) {
 			JOptionPane.showMessageDialog(
 					null,
 					Idioma.getString("msgErrorField") //$NON-NLS-1$
 							+ nombreCampo
 							+ Idioma.getString("msgErrorEmptyNorNumeric")); //$NON-NLS-1$
 			valido = false;
 
 			return false;
 
 		} else {
 
 			return true;
 
 		}
 
 	}
 
 	public static boolean validarEmail(String email) {
 		int atCount = 0;
 		email = email.trim();
 		boolean r = isInteger(email);
 
 		if (r == true || email.length() < 2) {
 			JOptionPane.showMessageDialog(
 					null,
 					Idioma.getString("msgErrorField") //$NON-NLS-1$
 							+ Idioma.getString("etMail")
 							+ Idioma.getString("msgErrorEmptyNorNumeric")); //$NON-NLS-1$
 			valido = false;
 
 			return false;
 
 		} else {
 			boolean at = false;
 			boolean dot = false;
 			boolean comilla = false;
 			String strComilla = "'";
 			for (int i = 0; i < email.length(); i++) {
 				if (email.charAt(i) == '@') {
 					at = true;
 					atCount++;
 				} else if (email.charAt(i) == '.') {
 					dot = true;
 
 				} else if (email.charAt(i) == strComilla.charAt(0))
 					comilla = true;
 			}
 			if (at != true || dot != true || atCount != 1 || comilla != false) {
 				JOptionPane.showMessageDialog(
 						null,
 						Idioma.getString("msgErrorField") //$NON-NLS-1$
 								+ Idioma.getString("etMail")
 								+ Idioma.getString("msgMustContainEmail")); //$NON-NLS-1$
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean validarNumerico(String numero, String nombreCampo,
 			int longitud) {
 		numero = numero.trim();
		String comilla = "'";
 
 		if (numero.length() != longitud) {
 			JOptionPane.showMessageDialog(
 					null,
 					Idioma.getString("msgErrorField") + nombreCampo
 							+ Idioma.getString("msgMustContain") + longitud
 							+ Idioma.getString("etDigit"));
 			return false;
 		} else {
 			for (int i = 0; i < longitud; i++) {
				if (Character.isDigit(numero.charAt(i)) == false || numero.charAt(i) == comilla.charAt(0)) {
 					JOptionPane.showMessageDialog(
 							null,
 							Idioma.getString("msgErrorField") //$NON-NLS-1$
 									+ nombreCampo
 									+ Idioma.getString("msgOnlyNumber")); //$NON-NLS-1$
 					return false;
 				}
 			}
 			return true;
 		}
 
 	}
 
 	public static boolean validarNumerico(String numero, String nombreCampo) {
 		numero = numero.trim();
		String comilla = "'";
		int longitud = numero.length();
		for (int o = 0; o < longitud; o++) {
			if (Character.isDigit(numero.charAt(o)) == false || numero.charAt(o) == comilla.charAt(0)) {
 				JOptionPane.showMessageDialog(
 						null,
 						Idioma.getString("msgErrorField") //$NON-NLS-1$
 								+ nombreCampo
 								+ Idioma.getString("msgOnlyNumber")); //$NON-NLS-1$
 				return false;
 
 			}
			
 		}
		return true;
 
 	}
 
 	public static boolean validarFecha(String fecha) {
 
 		for (int i = 0; i < fecha.length(); i++) {
 			if (Character.isDigit(fecha.charAt(i)) == false
 					&& fecha.charAt(i) != '/') {
 
 				JOptionPane.showMessageDialog(null,
 						Idioma.getString("msgErrorWrongDate")); //$NON-NLS-1$
 
 				return false;
 
 			}
 		}
 		String dia = "";
 		String mes = "";
 		String ao = "";
 
 		for (int i = 0; i < 2; i++) {
 
 			if (Character.isDigit(fecha.charAt(i)) == true) {
 				dia = dia + fecha.charAt(i);
 			}
 		}
 
 		for (int i = 3; i < 5; i++) {
 			if (Character.isDigit(fecha.charAt(i)) == true) {
 				mes = mes + fecha.charAt(i);
 			}
 
 		}
 		for (int i = 6; i < 10; i++) {
 			if (Character.isDigit(fecha.charAt(i)) == true) {
 				ao = ao + fecha.charAt(i);
 			}
 
 		}
 		int diaValido = Integer.parseInt(dia);
 		int mesValido = Integer.parseInt(mes);
 		int aoValido = Integer.parseInt(ao);
 		
 		if (diaValido < 0 || diaValido > 31){
 			return false;
 		}
 		
 		if (mesValido < 0 || mesValido > 12){
 			return false;
 		}
 		
 		if (aoValido < 1900 || aoValido > 2012){
 			return false;
 		}
 		
 		return true;
 	}
 
 	public static boolean isInteger(String input) {
 		try {
 			Integer.parseInt(input);
 			return true;
 		} catch (Exception e2) {
 			return false;
 		}
 
 	}
 
 }
