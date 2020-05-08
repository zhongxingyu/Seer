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
  *  Nexis Servicios Informáticos S.L. - http://www.nexis.es
  *
  * Contribuyente(s):
  *  Alejandro González <alejandro@opensixen.org> 
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
 
 package org.opensixen.process;
 
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import javax.swing.JOptionPane;
 import org.compiere.model.MInvoice;
 import org.compiere.util.CLogger;
 import org.compiere.util.Env;
 import org.compiere.util.Msg;
 import org.opensixen.bankoperations.form.RemittanceParams;
 import org.opensixen.bankoperations.form.RemittanceResultsSearch;
 import org.opensixen.model.MRemittance;
 import org.opensixen.model.MRemittanceLine;
 import org.opensixen.model.RVOpenItem;
 
 
 /**
  * 
  * RemittanceCreate 
  *
  * @author Alejandro González
  * Nexis Servicios Informáticos http://www.nexis.es
  */
 
 public class RemittanceCreate {
 	
 	/**
 	 * Descripcion de campos
 	 */
 	private HashMap<Integer, RVOpenItem> invoicelist=null;
 	private RemittanceParams params=null;
 	private String TrxName =null;
     private static CLogger log = CLogger.getCLogger( RemittanceCreate.class );
 	
     
     /**
 	 * Creamos Remesa en base de datos a partir de Lineas de factura
 	 */
 	
 	
 	public RemittanceCreate(HashMap<Integer, RVOpenItem> list){
 		invoicelist=list;
 	}
 	
 	/**
 	 * Constructor por defecto
 	 */
 	
 	public RemittanceCreate(){
 		invoicelist=RemittanceResultsSearch.getListSelected();
 	}
 	
 	/**
 	 * Creamos la remesa
 	 * @return
 	 */
 	public MRemittance Create(int Remittance_ID){
 		if(!checkValues()){
 			return null;
 		}
 		if(invoicelist.size()==0){
 			//Si no existen lineas seleccionadas no creamos la remesa
 			return null;
 		}
 		MRemittance remit = new MRemittance(Env.getCtx(),Remittance_ID,TrxName);
 		//Norma
 		remit.setC_BankRegulation_ID((Integer)params.getBankRegulation());
 		//Cuenta bancaria
 		remit.setC_Bank_ID((Integer)params.getBank());
 		remit.setC_BankAccount_ID((Integer)params.getBankAccount());
 		//Fecha de generacion
 		remit.setGenerateDate(params.getGenerateDate());
 		//Fecha de cargo
 		remit.setExecuteDate(params.getExecuteDate());
 
 		if(!remit.deleteLines()){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Remittance Processed"), Msg.translate(Env.getCtx(), "Remittance Processed"), JOptionPane.ERROR_MESSAGE);
 			return null;
 		}

		if(!remit.save(TrxName))
 			return null;

 		if(!CreateLines(remit))
 			return null;
 		//Actualizamos el total de la remesa despues de generar las lineas

 		remit.refreshTotal();

		remit.save(TrxName);

 		//Creamos el archivo de remesa
 		new RemittanceCreateFile(remit);
 		
 		return remit;
 	}
 	
 
 	
 	/**
 	 * Creamos las lineas de la remesa
 	 * @return true si se han creado con éxito
 	 */
 
 	public boolean CreateLines(MRemittance rem) {
 		//Comprobamos que existan lineas seleccionadas
 		if(invoicelist.size()==0){
 			return false;
 		}
 		
 		//Para cada registro de la lista
 		Iterator<Map.Entry<Integer,RVOpenItem>> it = invoicelist.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<Integer,RVOpenItem> e = (Map.Entry<Integer,RVOpenItem>)it.next();
 
 			//Clases necesarias de datos
 			RVOpenItem item=e.getValue();
 			MInvoice inv = new MInvoice(Env.getCtx(),e.getKey(),TrxName);
 			//Creamos el nuevo objeto
 			MRemittanceLine line = new MRemittanceLine(Env.getCtx(),0,TrxName);
 			line.setC_InvoicePaySchedule_ID(item.getC_InvoicePaySchedule_ID());
 			line.setC_Invoice_ID(e.getKey());
 			line.setAD_Org_ID(rem.getAD_Org_ID());
 			line.setC_BPartner_ID(inv.getC_BPartner_ID());
 			line.setC_Remittance_ID(rem.getC_Remittance_ID());
 			line.setGrandTotal(item.getDueAmt());
 			line.save();
 
 		}
 		
 		
 		return true;
 	}
 	
 	/**
 	 * Setea el panel de parametros necesario
 	 * @param panelParams
 	 */
 	
 	public void setPanelValues(RemittanceParams panelParams) {
 		params=panelParams;
 		
 	}
 	
 	/**
 	 * Comprueba que existan los valores necesarios para la creacion de la remesa
 	 */
 	
 	private boolean checkValues(){
 		
 		if(params==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Null"), Msg.translate(Env.getCtx(), "Null"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		if(params.getBank()==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Null Bank"), Msg.translate(Env.getCtx(), "C_Bank"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		if(params.getBankAccount()==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Null BankAccount"), Msg.translate(Env.getCtx(), "C_BankAccount"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		if(params.getGenerateDate()==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Null GenerateDate"), Msg.translate(Env.getCtx(), "SendDate"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		if(params.getExecuteDate()==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Null ExecuteDate"), Msg.translate(Env.getCtx(), "ExecuteDate"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		if(params.getBankRegulation()==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "Null BankRegulation"), Msg.translate(Env.getCtx(), "C_BankRegulation"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		
 		return true;
 	}
 
 
 
 }
