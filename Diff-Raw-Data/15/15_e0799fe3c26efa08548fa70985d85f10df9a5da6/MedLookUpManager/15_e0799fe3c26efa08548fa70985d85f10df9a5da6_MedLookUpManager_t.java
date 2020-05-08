 package edu.wustl.common.query.pvmanager.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.util.global.Constants;
 
 public class MedLookUpManager
 {
 	Map<String,List<String> > pvMap = null;
 	private static MedLookUpManager medLookUpManager = null;
 	
 	private MedLookUpManager()
 	{
 		
 	}
 	
 	public static MedLookUpManager instance()
 	{
 		if(medLookUpManager == null)
 		{
 			medLookUpManager = new MedLookUpManager();
 			medLookUpManager.init();
 		}
 		
 		return medLookUpManager;
 		
 	}
 	
 	private void init()
 	{
 		JDBCDAO dao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 		try
 		{
 			dao.openSession(null);
 			List<List<String>> dataList = dao.executeQuery("select synonym,id from MED_LOOKUP_TABLE", null, false, false, null);
 			if(!dataList.isEmpty())
 			{
 				pvMap = new HashMap<String, List<String>>();
 				String pvFilter;
 				for(int i=0; i < dataList.size(); i++)
 				{
 					pvFilter = dataList.get(i).get(0).substring(0,dataList.get(i).get(0).indexOf("^") + 1) + "%";
 					if(pvMap.containsKey(pvFilter))
 					{
 						pvMap.get(pvFilter).add(dataList.get(i).get(1));
 					}
 					else
 					{
 						List<String> pvList = new ArrayList<String>();
 						pvList.add(dataList.get(i).get(1));
 						pvMap.put(pvFilter,pvList);
 					}
 				}
 			}
 		}
 		catch (DAOException e)
 		{
 			// TODO Auto-generated catch block
			Logger.out.error(e.getMessage(),e);
 		}
 		catch (ClassNotFoundException e)
 		{
 			// TODO Auto-generated catch block
			Logger.out.error(e.getMessage(),e);
 		}
 		
 	}
 	
 	public List<String> getPermissibleValues(String pvFilter)
 	{
 		if(pvMap != null)
		{
 			return pvMap.get(pvFilter);
		}
 		else
		{
 			return null;
		}
 
 	}
 }
