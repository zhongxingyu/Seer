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
 package es.indeos.osx.finreports.model;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * FinReportLine 
  *
  * @author Eloy Gomez
  * Indeos Consultoria http://www.indeos.es
  */
 public class FinReportLine {
 	private String name;
 	
 	private String source;
 	
 	private FinReportColumn[] columns;	
 	
 	private boolean ignored = false;
 	
 	private boolean isCalculation = false;
 	
 	private boolean isCalculated = false;		
 	
 	/**
 	 * @param name
 	 * @param source
 	 */
 	public FinReportLine(String name, String source) {
 		super();
 		this.name = name;
 		setSource(source);
 	}
 
 	/**
 	 * 
 	 */
 	public FinReportLine() {
 		super();
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 		
 	/**
 	 * @return the source
 	 */
 	public String getSource() {
 		return source;
 	}
 
 	/**
 	 * @param source the source to set
 	 */
 	public void setSource(String source) {
 		this.source = source;
 		if (source.length() == 0)	{
 			ignored = true;
 		}
 		
 		if (source.startsWith("?="))	{
 			isCalculation = true;
 		}
 	}
 
 	/**
 	 * @return the columns
 	 */
 	public FinReportColumn[] getColumns() {
 		return columns;
 	}
 
 	/**
 	 * @param columns the columns to set
 	 */
 	public void setColumns(FinReportColumn[] columns) {
 		this.columns = columns;
 	}
 			
 	/**
 	 * @return the isCalculation
 	 */
 	public boolean isCalculation() {
 		return isCalculation;
 	}
 	
 	/**
 	 * @return the isCalculated
 	 */
 	public boolean isCalculated() {
 		return isCalculated;
 	}
 
 	/**
 	 * @param isCalculated the isCalculated to set
 	 */
 	public void setCalculated(boolean isCalculated) {
 		this.isCalculated = isCalculated;
 	}
 	
 	
 
 	/**
 	 * @return the ignore
 	 */
 	public boolean isIgnored() {
 		return ignored;
 	}
 
 	/**
 	 * Get source account and make calcs
 	 * @param trees
 	 */
 	public void calculate(AccountsTree<Account>[] trees) {
 		columns = new FinReportColumn[trees.length];
 
 		for (int t = 0; t < trees.length; t++) {
 			FinReportColumn col = new FinReportColumn();
 			List<Account> sources = new ArrayList<Account>();
 			String pattern = "([+|-]?[0-9]*)";
 			// Create a Pattern object
 			Pattern r = Pattern.compile(pattern);
 			Matcher m = r.matcher(getSource());
 			while (m.find()) {
 				String a = m.group();
 				if (a.length() == 0) {
 					continue;
 				}
 				// Clean account name
 				String acct = a.replaceAll("\\D", "");
 
 				AccountsTree<Account> child = trees[t].getChild(acct);
 				if (child != null) {
 					sources.add(child.getData());
 					BigDecimal amt = child.getData().getChildsBalance();
 
 					if (a.startsWith("-")) {
						 amt = amt.negate();
 					}
 					col.setBalance(col.getBalance().add(amt));
 				}
 			}
 
 			columns[t] = col;
 		}
 	}
 }
