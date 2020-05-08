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
 
 
 package org.opensixen.model;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 import org.compiere.model.I_C_DocType;
 import org.compiere.model.MDocType;
 import org.compiere.model.ModelValidationEngine;
 import org.compiere.model.ModelValidator;
 import org.compiere.process.DocAction;
 import org.compiere.process.DocumentEngine;
 import org.compiere.util.DB;
 import org.compiere.util.Env;
 import org.compiere.util.Msg;
 import org.compiere.util.TimeUtil;
 
 /**
  * 
  * MRemittance 
  *
  * @author Alejandro González
  * Nexis Servicios Informáticos http://www.nexis.es
  */
 
 public class MRemittance extends X_C_Remittance implements DocAction {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Variables globales
 	 */
 	
 	private MRemittanceLine[]	m_lines	= null;
 	
 	/**
 	 * 	Standard Constructor
 	 *	@param ctx context
 	 *	@param C_Remittance_ID id
 	 *	@param trxName trx
 	 */
 	
 	public MRemittance(Properties ctx, int C_Remittance_ID, String trxName) {
 		super(ctx, C_Remittance_ID, trxName);
 		//Recogemos la fecha actual
 		Timestamp today=new Timestamp(TimeUtil.getToday().getTimeInMillis());
 		//Escogemos el tipo de documento remesa
 		
 		QParam param = new
 		QParam(I_C_DocType.COLUMNNAME_DocBaseType+"='"+org.opensixen.model.X_C_DocType.DOCBASETYPE_Remittance+"' AND "+I_C_DocType.COLUMNNAME_AD_Client_ID+"="+Env.getAD_Client_ID(Env.getCtx()));
 		
 		org.opensixen.model.X_C_DocType doctype =
 		POFactory.get(org.opensixen.model.X_C_DocType.class, param);
 		if(doctype==null){
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "NoDoctype"), Msg.translate(Env.getCtx(), "NoDoctype"), JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		this.setTotalAmt(BigDecimal.ZERO);
 		this.setGenerateDate(today);
 		this.setExecuteDate(today);
 		this.setC_DocTypeTarget_ID(doctype.getC_DocType_ID());
 		this.setC_DocType_ID(0);
 		setDocStatus(DOCSTATUS_Drafted);
 		setDocAction (DOCACTION_Complete);
 	}
 	
 	
 	/**
 	 * 	Load Constructor
 	 *	@param ctx context
 	 *	@param rs result set
 	 *	@param trxName trx
 	 */
 	
 	public MRemittance (Properties ctx, ResultSet rs, String trxName)
 	{
 		super (ctx, rs, trxName);
 	}	
 
 	
 	/**
 	 * 	Get Lines
 	 *	@param reload data
 	 *	@return array of lines
 	 */
 	public MRemittanceLine[] getLines (boolean reload)
 	{
 		if (m_lines != null && !reload) {
 			set_TrxName(m_lines, get_TrxName());
 			return m_lines;
 		}
 		
 		//Lista de objetos de lineas de remesa
 		ArrayList<MRemittanceLine> rems = (ArrayList<MRemittanceLine>) 
 			POFactory.getList(Env.getCtx(),MRemittanceLine.class, new QParam(I_C_remittanceLine.COLUMNNAME_C_Remittance_ID,getC_Remittance_ID()));
 
 		m_lines = new MRemittanceLine[rems.size ()];
 		rems.toArray (m_lines);
 		return m_lines;
 	}	//	getLines
 
 	
 	/**
 	 * 	Set Processed
 	 *	@param processed processed
 	 */
 	public void setProcessed (boolean processed)
 	{
 		super.setProcessed (processed);
 		if (get_ID() == 0)
 			return;
 		String set = "SET Processed='"
 			+ (processed ? "Y" : "N")
 			+ "' WHERE C_Remittance_ID=" + getC_Remittance_ID();
 		int noLine = DB.executeUpdate("UPDATE C_RemittanceLine " + set, get_TrxName());
 		log.fine(processed + " - Lines=" + noLine);
 		
 	}	//	setProcessed
 
 
 	@Override
 	public void setDocStatus(String newStatus) {
 		super.setDocStatus(newStatus);
 		
 	}
 
 
 	@Override
 	public String getDocStatus() {
 		return super.getDocStatus();
 	}
 
 
 	@Override
 	public boolean processIt(String action) throws Exception {
 		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
 		return engine.processIt (action, getDocAction());
 	}
 
 
 	@Override
 	public boolean unlockIt() {
 		setProcessing(false);
 		return true;
 	}
 
 
 	@Override
 	public boolean invalidateIt() {
 		log.info("invalidateIt - " + toString());
 		setDocAction(DOCACTION_Prepare);
 		return true;
 	}
 
 
 	@Override
 	public String prepareIt() {
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
 		if (m_processMsg != null)
 			return DocAction.STATUS_Invalid;
 
 		if (!DOCACTION_Complete.equals(getDocAction()))
 			setDocAction(DOCACTION_Complete);
 		return DocAction.STATUS_InProgress;
 		
 	}
 
 
 	@Override
 	public boolean approveIt() {
 		log.info(toString());
 		return true;
 	}
 
 
 	@Override
 	public boolean rejectIt() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 
 	@Override
 	public String completeIt() {
 		// TODO Auto-generated method stub
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
 		if (m_processMsg != null)
 			return DocAction.STATUS_Invalid;
 		approveIt();
 		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
 		if (valid != null)
 		{
 			m_processMsg = valid;
 			return DocAction.STATUS_Invalid;
 		}
 		setProcessed(true);
 		setDocAction(DOCACTION_Close);
 		return DocAction.STATUS_Completed;
 	}
 
 	
 	public void refreshTotal(){
 		
 		BigDecimal total=BigDecimal.ZERO;
 		//Por todas las lineas
		for(MRemittanceLine line :getLines(false)){
 			total=total.add(line.getGrandTotal());
 		}
 		
 		this.setTotalAmt(total);
 	}
 	
 	
 	/**************************************************************************
 	 * 	Before Save
 	 *	@param newRecord new
 	 *	@return save
 	 */
 	
 	protected boolean beforeSave (boolean newRecord)
 	{

 		return true;
 	}
 
 	@Override
 	public boolean voidIt() {
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
 		if (m_processMsg != null)
 			return false;
 		
 		// After Void
 		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
 		if (m_processMsg != null)
 			return false;
 
 		setProcessed(true);
 		setDocAction(DOCACTION_None);
 		return true;
 	}
 
 
 	@Override
 	public boolean closeIt() {
 		// Before Close
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
 		if (m_processMsg != null)
 			return false;
 
 		setProcessed(true);
 		setDocAction(DOCACTION_None);
 
 		// After Close
 		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
 		if (m_processMsg != null)
 			return false;
 		return true;
 	}
 
 
 	@Override
 	public boolean reverseCorrectIt() {
 		// After reverseCorrect
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
 		if (m_processMsg != null)
 			return false;
 
 		return true;
 	}
 
 
 	@Override
 	public boolean reverseAccrualIt() {
 		log.info(toString());
 		// Before reverseAccrual
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSEACCRUAL);
 		if (m_processMsg != null)
 			return false;
 
 		// After reverseAccrual
 		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSEACCRUAL);
 		if (m_processMsg != null)
 			return false;
 
 		return false;
 	}
 
 
 	@Override
 	public boolean reActivateIt() {
 		log.info(toString());
 		// Before reActivate
 		String m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
 		if (m_processMsg != null)
 			return false;
 
 		// After reActivate
 		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
 		if (m_processMsg != null)
 			return false;
 
 
 		return false;
 	}
 
 
 	@Override
 	public String getSummary() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public String getDocumentInfo() {
 		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
 		return dt.getName() + " " + getDocumentNo();
 	}
 
 
 	@Override
 	public File createPDF() {
 		
 		return null;
 	}
 
 
 	@Override
 	public String getProcessMsg() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public int getDoc_User_ID() {
 		// TODO Auto-generated method stub
 		return getCreatedBy();
 	}
 
 
 	@Override
 	public int getC_Currency_ID() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public BigDecimal getApprovalAmt() {
 		// TODO Auto-generated method stub
 		return getTotalAmt();
 	}
 	
 	/**
 	 * 	Set DocAction
 	 *	@param DocAction doc action
 	 *	@param forceCreation force creation
 	 */
 	public void setDocAction (String DocAction, boolean forceCreation)
 	{
 		super.setDocAction (DocAction);
 	}	//	setDocAction
 
 	/**
 	 * Eliminamos las lineas de la remesa
 	 * @return
 	 */
 	
 	public boolean deleteLines() {
 		for(MRemittanceLine line : getLines(true)){
 			if(!line.delete(false))
 				return false;
 		}
 		return true;
 	}
 	
 }
