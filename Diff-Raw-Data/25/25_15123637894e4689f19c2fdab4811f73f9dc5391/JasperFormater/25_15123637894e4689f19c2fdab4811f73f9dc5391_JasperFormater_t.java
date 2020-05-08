 /******* BEGIN LICENSE BLOCK *****
  * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
  *
  * Los contenidos de este fichero están sujetos a la Licencia
  * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
  * usar este fichero, excepto bajo las condiciones que otorga dicha 
  * Licencia y siempre de acuerdo con el contenido de la presente. 
  * Una copia completa de las condiciones de de dicha licencia,
  * traducida en castellano, deberá estar incluida con el presente
  * programa.
  * 
  * Adicionalmente, puede obtener una copia de la licencia en
  * http://www.gnu.org/licenses/gpl-2.0.html
  *
  * Este fichero es parte del programa opensiXen.
  *
  * OpensiXen es software libre: se puede usar, redistribuir, o
  * modificar; pero siempre bajo los términos de la Licencia 
  * Pública General de GNU, tal y como es publicada por la Free 
  * Software Foundation en su versión 2.0, o a su elección, en 
  * cualquier versión posterior.
  *
  * Este programa se distribuye con la esperanza de que sea útil,
  * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
  * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
  * los detalles de la Licencia Pública General GNU para obtener una
  * información más detallada. 
  *
  * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
  * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
  * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
  *
  * El desarrollador/es inicial/es del código es
  *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
  *  Indeos Consultoria S.L. - http://www.indeos.es
  *
  * Contribuyente(s):
  *  Eloy Gómez García <eloy@opensixen.org> 
  *
  * Alternativamente, y a elección del usuario, los contenidos de este
  * fichero podrán ser usados bajo los términos de la Licencia Común del
  * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
  * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
  * copia completa de las condiciones de dichas licencias, traducida en 
  * castellano, deberán de estar incluidas con el presente programa.
  * Adicionalmente, es posible obtener una copia original de dichas 
  * licencias en su versión original en
  *  http://www.opensource.org/licenses/cddl1.php  y en  
  *  http://www.opensource.org/licenses/eclipse-1.0.php
  *
  * Si el usuario desea el uso de SU versión modificada de este fichero 
  * sólo bajo los términos de una o más de las licencias, y no bajo los 
  * de las otra/s, puede indicar su decisión borrando las menciones a la/s
  * licencia/s sobrantes o no utilizadas por SU versión modificada.
  *
  * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
  * puede utilizar este fichero bajo cualquiera de las tres licencias que 
  * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
  *
  * ***** END LICENSE BLOCK ***** */
 package org.opensixen.report;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.util.Locale;
 import org.opensixen.osgi.Service;
 import org.opensixen.osgi.interfaces.IJasperFormater;
 
 /**
  * JasperFormater
  * 
  * @author Eloy Gomez Indeos Consultoria http://www.indeos.es
  */
 public class JasperFormater {
 
 	/** Online mode */
 	private static boolean online = false;
 
 	/** Formater */
 	private static IJasperFormater formater;
 
 	/**
 	 * Static constructor
 	 */
 	static {
 		JasperFormater.formater = new OSXJasperFormater();
 	}
 
 	/**
 	 * Get online mode
 	 * 
 	 * @return
 	 */
 	public static boolean isOnline() {
 		return online;
 	}
 
 	/**
 	 * Set online mode
 	 * 
 	 * @param online
 	 */
 	public static void setOnline(boolean online) {
 		JasperFormater.online = online;
 		if (online) {
 			IJasperFormater f = Service.locate(IJasperFormater.class);
 
 			// If no default formater
 			if (f != null) {
 				formater = f;
 			}
 		}
 	}
 
 	/**
 	 * Format qty
 	 * 
 	 * @param locale
 	 * @param qty
 	 * @return
 	 */
 	public static String formatQty(Locale locale, BigDecimal qty) {
 		return formater.formatQty(locale, qty);
 	}
 
 	/**
 	 * Format amount with currency
 	 * 
 	 * @param locale
 	 * @param amount
 	 * @param isoCode
 	 * @return
 	 */
 	public static String formatAmt(Locale locale, BigDecimal amount,
 			String isoCode) {
 		return formater.formatAmt(locale, amount, isoCode);
 	}
 
 	/**
 	 * Format date
 	 * 
 	 * @param locale
 	 * @param date
 	 * @return
 	 */
 	public static String formatDate(Locale locale, Timestamp date) {
 		return formater.formatDate(locale, date);
 	}
 
 	/**
 	 * Format a location
 	 * 
 	 * @param C_BPartner_Location_ID
 	 * @return
 	 */
 	public static String formatLoc(Integer C_BPartner_Location_ID) {
 		if (isOnline()) {
 			return formater.formatLoc(C_BPartner_Location_ID);
 		} else {
 			return "C\\ General Prim, 4 1ºA\nJunto a iglesia\n03158 - Catral (Alicante)\nSpain";
 		}
 	}
 
 	/**
 	 * Format a full Address
 	 * 
 	 * @param C_BPartner_ID
 	 * @param C_Bpartner_Location_ID
 	 * @param addTaxID
 	 * @return
 	 */
 	public static String formatLocation(int C_BPartner_ID,
			int C_Bpartner_Location_ID, boolean addTaxID) {		
 		if (isOnline()) {
			return formater.formatLocation(C_BPartner_ID,C_Bpartner_Location_ID, addTaxID);
 		} else {
			StringBuffer address = new StringBuffer();
 			address.append("<b>Partner de Envio&Facturacion.</b>").append("\n");
 			if (addTaxID) {
 				address.append("B00000001").append("\n");
 			}
 			address.append(formatLoc(0));
			String str = address.toString();
			return str.replaceAll("\\n", "<br/>");
 		}
		
 	}
 
 	/**
 	 * Get Ship To BPartner in InOut
 	 * 
 	 * @param M_InOut_ID
 	 * @return
 	 */
 	public static String getShipToInOut(Integer M_InOut_ID) {
 		if (isOnline()) {
 			return formater.getShipToInOut(M_InOut_ID);
 		}
 		// Offline
 		else {
 			return formatLocation(0, 0, false);
 		}
 	}
 
 	/**
 	 * Get BPartner in Invoice
 	 * 
 	 * @param C_Invoice_ID
 	 * @return
 	 */
 	public static String getInvoiceToInvoice(Integer C_Invoice_ID) {
 		if (isOnline()) {
 			return formater.getInvoiceToInvoice(C_Invoice_ID);
 		} else {
 			return formatLocation(0, 0, true);
 		}
 	}
 
 	/**
 	 * Get BPartner in Order
 	 * 
 	 * @param C_Invoice_ID
 	 * @return
 	 */
 	public static String getInvoiceToOrder(Integer C_Order_ID) {
 		if (isOnline()) {
 			return formater.getInvoiceToOrder(C_Order_ID);
 		} else {
 			return formatLocation(0, 0, true);
 		}
 	}
 
 	/**
 	 * Get Ship To BPartner in Order
 	 * 
 	 * @param M_InOut_ID
 	 * @return
 	 */
 	public static String getShipToOrder(Integer C_Order_ID) {
 		if (isOnline()) {
 			return formater.getShipToOrder(C_Order_ID);
 		} else {
 			return formatLocation(0, 0, true);
 		}
 	}
 
 	/**
 	 * Get Payment terms from this invoice
 	 * 
 	 * @param locale
 	 * @param C_Invoice_ID
 	 * @param isoCode
 	 * @return
 	 */
 	public static String getPaymentTermInvoice(Locale locale,
 			Integer C_Invoice_ID, String isoCode) {
 		if (isOnline()) {
 			return formater
 					.getPaymentTermInvoice(locale, C_Invoice_ID, isoCode);
 		} else {
 			StringBuffer term = new StringBuffer();
 			term.append("01/01/1979 100,00€");
 			return term.toString();
 		}
 	}
 
 	/**
 	 * Get Payment terms from this order
 	 * 
 	 * @param locale
 	 * @param C_Invoice_ID
 	 * @param isoCode
 	 * @return
 	 */
 	public static String getPaymentTermOrder(Locale locale, Integer C_Order_ID,
 			String isoCode) {
 		if (isOnline()) {
 			return formater.getPaymentTermOrder(locale, C_Order_ID, isoCode);
 		} else {
 			StringBuffer term = new StringBuffer();
 			term.append("01/01/1979 100,00€");
 			return term.toString();
 		}
 	}
 }
