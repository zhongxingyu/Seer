 /**
  * <p>Title: BizLogicFactory Class>
  * <p>Description:  BizLogicFactory is a factory for DAO instances of various domain objects.</p>
  * Copyright:    Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Gautam Shetty
  * @version 1.00
  */
 
 package edu.common.dynamicextensions.bizlogic;
 
 
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.wustl.common.bizlogic.AbstractBizLogic;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.bizlogic.QueryBizLogic;
 
 /**
  * BizLogicFactory is a factory for DAO instances of various domain objects.
  * @author gautam_shetty
  * Kapil: test
  */
 public class BizLogicFactory
 {
	xzxX
 	/**
 	 * Returns DAO instance according to the form bean type.
 	 * @param FORM_TYPE The form bean type.
 	 * @return An AbstractDAO object.
 	 */
 	public static AbstractBizLogic getBizLogic(int FORM_TYPE)
 	{
 		AbstractBizLogic abstractBizLogic = null;
 		
 		switch(FORM_TYPE)
 		{
         case Constants.QUERY_INTERFACE_ID:
             abstractBizLogic = new QueryBizLogic();
         break;
         
     
     
 		default:
 			abstractBizLogic = new DefaultBizLogic();
 		break;
 		}
 		
 		return abstractBizLogic;
 	}
 	
 	public static DefaultBizLogic getDefaultBizLogic()
 	{
 		return new DefaultBizLogic();
 	}
 	
 	/**
 	 * Returns DAO instance according to the fully qualified class name.
 	 * @param className The name of the class.
 	 * @return An AbstractDAO object.
 	 */
 	public static AbstractBizLogic getBizLogic(String className)
 	{
 		AbstractBizLogic abstractBizLogic = null;
 		
 		
 		
 		return abstractBizLogic;
 	}
 	
 	
 	
 }
