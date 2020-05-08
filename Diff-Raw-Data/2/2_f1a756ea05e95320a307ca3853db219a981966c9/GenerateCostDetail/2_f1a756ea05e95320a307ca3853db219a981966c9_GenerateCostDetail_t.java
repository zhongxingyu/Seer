 /******************************************************************************
  * Product: Adempiere ERP & CRM Smart Business Solution                       *
  * This program is free software; you can redistribute it and/or modify it    *
  * under the terms version 2 of the GNU General Public License as published   *
  * by the Free Software Foundation. This program is distributed in the hope   *
  * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
  * See the GNU General Public License for more details.                       *
  * You should have received a copy of the GNU General Public License along    *
  * with this program; if not, write to the Free Software Foundation, Inc.,    *
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
  * For the text or an alternative of this public license, you may reach us    *
  * Copyright (C) 2003-2010 e-Evolution,SC. All Rights Reserved.               *
  * Contributor(s): victor.perez@e-evolution.com http://www.e-evolution.com    *
  *****************************************************************************/
 
 package org.eevolution.process;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.adempiere.engine.CostEngineFactory;
 import org.adempiere.exceptions.FillMandatoryException;
 import org.compiere.model.MAcctSchema;
 import org.compiere.model.MCostDetail;
 import org.compiere.model.MCostElement;
 import org.compiere.model.MCostType;
 import org.compiere.model.MInOutLine;
 import org.compiere.model.MMatchInv;
 import org.compiere.model.MMatchPO;
 import org.compiere.model.MProduct;
 import org.compiere.model.MTransaction;
 import org.compiere.model.Query;
 import org.compiere.process.ProcessInfoParameter;
 import org.compiere.process.SvrProcess;
 import org.compiere.util.DB;
 import org.eevolution.model.MPPCostCollector;
 
 
 /**
  *	Regenerate Cost Detail
  *	The Generate Cost Transaction process allows the detailed cost calculation and cost generation beginning from a date. 
  *	If you have not yet set COGs Adjustment, you should execute this process before a period's end in order to fix the cost layers.
  *	@author victor.perez@e-evolution.com, www.e-evolution.com
  */
 public class GenerateCostDetail extends SvrProcess
 {	
     /** Parameters **/
 	private int p_M_Product_ID;
 	private int p_M_CostElement_ID;
 	private int p_M_CostType_ID;
 	private int p_C_AcctSchema_ID;
 	private Timestamp p_DateAcct;
 	
 	/**  Variables **/
 	private ArrayList<Object> deleteParameters;
 	private ArrayList<Object> trxParameters;
 	private List<MAcctSchema> acctSchemas = new ArrayList();
 	private List<MCostType> costTypes= new ArrayList();
 	private List<MCostElement> costElements = new ArrayList();
 	private StringBuffer deleteWhereClause;
 	private StringBuffer trxWhereClause;
 	
 	/**
 	 *  Prepare - e.g., get Parameters.
 	 */
 	protected void prepare()
 	{
          ProcessInfoParameter[] parameters = getParameter();
          for(ProcessInfoParameter parameter: parameters)
          {
         	 String name = parameter.getParameterName();
  			if (parameter.getParameter() == null)
  				;
 
  			if (name.equals(MCostDetail.COLUMNNAME_C_AcctSchema_ID))
  			{	
  	 			p_C_AcctSchema_ID = parameter.getParameterAsInt();
  			}
  			else if (name.equals(MCostDetail.COLUMNNAME_M_CostType_ID))
  			{
  				p_M_CostType_ID =  parameter.getParameterAsInt();
  			}
  			else if (name.equals(MCostDetail.COLUMNNAME_M_CostElement_ID))
  			{	
  				p_M_CostElement_ID = parameter.getParameterAsInt();
  			}
  			else if (name.equals(MCostDetail.COLUMNNAME_M_Product_ID))
  			{	
  				p_M_Product_ID = parameter.getParameterAsInt();
  			}	
  			else if (name.equals(MCostDetail.COLUMNNAME_DateAcct))
  			{	
  				p_DateAcct = (Timestamp) parameter.getParameter();
  				if(p_DateAcct == null)
  		    		throw new FillMandatoryException(MCostDetail.COLUMNNAME_DateAcct);
  			}	
          }
          if(p_DateAcct != null)
          { 
         	 setup();
          }	 
          
 	}	//	prepare
 
         
 	/**
 	 * execute the Generate Cost Detail
 	 */
     protected String doIt() throws Exception                
 	{
     	generateCostDetail();
 		return "@Ok@";
 	
 	}
     
     /**
      * Delete Cost Detail
      */
     private void deleteCostDetail()
     {
     	StringBuffer sqlDelete = new StringBuffer("DELETE M_CostDetail cd WHERE ");
     	sqlDelete.append(deleteWhereClause);
     	int record = DB.executeUpdateEx(sqlDelete.toString(), deleteParameters.toArray() ,get_TrxName());
     }
     
     /**
      * Setup the collections
      */
     private void setup()
     {  	
       	
     	if(p_C_AcctSchema_ID > 0)
         	acctSchemas.add(MAcctSchema.get(getCtx(), p_C_AcctSchema_ID));
     	else
     		acctSchemas = new ArrayList(Arrays.asList(MAcctSchema.getClientAcctSchema(getCtx(), getAD_Client_ID(), get_TrxName())));
     	
     	if(p_M_CostType_ID > 0)   		
     		costTypes.add(new MCostType(getCtx(), p_M_CostType_ID , get_TrxName()));
     	else
     		costTypes= MCostType.get(getCtx(), get_TrxName());
     	
     	if(p_M_CostElement_ID > 0)
     		costElements.add(MCostElement.get(getCtx(), p_M_CostElement_ID));
     	else
     		costElements = MCostElement.getCostElement(getCtx(), get_TrxName());
     }
     
     /**
      * Apply Criteria for where clause
      * @param C_AcctSchema_ID
      * @param M_CostType_ID
      * @param M_CostElement_ID
      * @param M_Product_ID
      * @param DateAcct
      */
     private void applyCriterial(int C_AcctSchema_ID,int M_CostType_ID ,int M_CostElement_ID ,int M_Product_ID, Timestamp DateAcct)
     {
     	deleteParameters = new ArrayList();
     	trxParameters = new ArrayList();
     	deleteWhereClause = new StringBuffer("1=1");
     	trxWhereClause = new StringBuffer("1=1 ");
     	
     	if(C_AcctSchema_ID > 0)
     	{	
         	deleteWhereClause.append(" AND ").append(MCostDetail.COLUMNNAME_C_AcctSchema_ID).append("=? ");
         	deleteParameters.add(C_AcctSchema_ID);
         }
     	if(M_CostType_ID > 0 )
     	{	
     		deleteWhereClause.append(" AND ").append(MCostDetail.COLUMNNAME_M_CostType_ID).append("=? ");
     		deleteParameters.add(p_M_CostType_ID);
     	}
 		if(M_CostElement_ID > 0)
 		{	
 			deleteWhereClause.append(" AND ").append(MCostDetail.COLUMNNAME_M_CostElement_ID).append("=? ");
 			deleteParameters.add(p_M_CostElement_ID);
 		}			
     	if(M_Product_ID > 0)
     	{
     		deleteWhereClause.append(" AND ").append(MCostDetail.COLUMNNAME_M_Product_ID).append("=? ");
     		deleteParameters.add(M_Product_ID);
     		trxWhereClause.append(" AND ").append(MCostDetail.COLUMNNAME_M_Product_ID).append("=? ");
 	    	trxParameters.add(M_Product_ID);
     	}
     	if(DateAcct != null)
     	{
     		deleteWhereClause.append(" AND ").append(MCostDetail.COLUMNNAME_DateAcct).append(">=? ");
     		deleteParameters.add(DateAcct);
     		trxWhereClause.append("AND EXISTS (SELECT 1 FROM RV_Transaction rvt WHERE rvt.M_Transaction_ID=M_Transaction.M_Transaction_ID AND rvt.DateAcct >= ?)");
     		trxParameters.add(DateAcct);
     	}
     	return;
     }
     
     /**
      * generate Cost Detail from transaction history
      */
     private void generateCostDetail()
     {    	
 
     		//for each Account Schema
     		for(MAcctSchema as :acctSchemas)
     		{	
     			for (MCostType ct : costTypes)
     			{	
     				for (MCostElement ce : costElements)
     				{	
 		    			applyCriterial(as.getC_AcctSchema_ID(),ct.getM_CostType_ID() ,ce.getM_CostElement_ID() , p_M_Product_ID, p_DateAcct);
 		    			deleteCostDetail();
 		    			
 		    			List<MTransaction> trxs = new Query(getCtx(), MTransaction.Table_Name, trxWhereClause.toString(), get_TrxName())
 		    			.setParameters(trxParameters)
 		    			.setClient_ID()
		    			.setOrderBy("M_Product_ID , MovementDate , M_Transaction_ID")
 		    			.list();
 		    			
 				    	for (MTransaction trx : trxs)
 				    	{				    						    		
 				    		CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetail(as, trx, trx.getDocumentLine(), ce, ct);
 				    		
 				    		if(MTransaction.MOVEMENTTYPE_VendorReceipts.equals(trx.getMovementType()))
 				    		{
 				    			MInOutLine line = (MInOutLine) trx.getDocumentLine();
 				    			MMatchPO[] orderMatches = MMatchPO.getOrderLine(getCtx(), line.getC_OrderLine_ID(), get_TrxName());
 				    			for(MMatchPO match: orderMatches)
 				    			{	
 				    				if(match.getM_InOutLine_ID() == line.getM_InOutLine_ID())
 				    				CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetail(as, trx, match , ce, ct );
 				    			}
 				    			
 				    			MMatchInv[] invoiceMatches = MMatchInv.getInOutLine(getCtx(), line.getM_InOutLine_ID(), get_TrxName());
 				    			for (MMatchInv match: invoiceMatches)
 				    			{
 				    				if(match.getC_InvoiceLine_ID()==match.getC_InvoiceLine_ID())
 				    				CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetail(as , trx, match , ce , ct);
 				    			}
 				    		}   	
 				    	}
 				    	
 				    	MProduct product = null;
 				    	
 				    	if(p_M_Product_ID > 0)
 				    	{
 				    		product = new MProduct(getCtx(), p_M_Product_ID, get_TrxName());				    		
 				    	}	
 				    	
 				    	List<MPPCostCollector> ccs = MPPCostCollector.getCostCollectorNotTransaction(product , getAD_Client_ID(), p_DateAcct);
 				    	
 				    	for(MPPCostCollector cc : ccs)
 				    	{
 				    		CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetail(null, cc);	    		
     					}
     				}
     			}
 	    	}
     	}
 }
