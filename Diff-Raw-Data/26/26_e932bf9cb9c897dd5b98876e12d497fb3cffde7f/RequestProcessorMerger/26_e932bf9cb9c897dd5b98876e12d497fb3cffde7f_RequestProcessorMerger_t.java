 package com.gentics.cr;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.Map.Entry;
 
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class RequestProcessorMerger {
 	
 	/**
 	 * Merge the results of two request processor. The primary RequestProcessor overrules the secondary RP. The request is made using the CRRequest
 	 * @param uniquemergeattribute
 	 * @param primaryRP
 	 * @param secondaryRP
 	 * @param request
 	 * @return
 	 * @throws CRException
 	 */
 	public static Collection<CRResolvableBean> merge(String uniquemergeattribute,RequestProcessor primaryRP, RequestProcessor secondaryRP, CRRequest request) throws CRException
 	{
 		
 		Collection<CRResolvableBean> rp1res = primaryRP.getObjects(request);
 		
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		
 		HashMap<Object,CRResolvableBean> resultMap = new HashMap<Object,CRResolvableBean>();
		HashMap<Object,CRResolvableBean> resultMap2 = new HashMap<Object,CRResolvableBean>();
 		
 		String mergefilter="";
 		
 		boolean first=true;
 		
 		for(CRResolvableBean crBean:rp1res)
 		{
 			Object id = crBean.get(uniquemergeattribute);
 			if(first)
 			{
 				first=false;
 				mergefilter+="\""+id+"\"";
 			}
 			else
 			{
 				mergefilter+=","+"\""+id+"\"";
 			}
 			resultMap.put(id, crBean);
 		}
 		rp1res=null;
 		request.setRequestFilter("object."+uniquemergeattribute+" CONTAINSONEOF ["+mergefilter+"]");
 		Collection<CRResolvableBean> rp2res = secondaryRP.getObjects(request);
 
 		String[] attributes = request.getAttributeArray();
 		
 		for(CRResolvableBean crBean:rp2res)
 		{
 			Object id = crBean.get(uniquemergeattribute);
			if(first)
			{
				first=false;
				mergefilter+="\""+id+"\"";
			}
			else
			{
				mergefilter+=","+"\""+id+"\"";
			}
			resultMap2.put(id, crBean);
		}
		
		for(Entry<Object,CRResolvableBean> e:resultMap.entrySet())
		{
			
			Object id = e.getKey();
			CRResolvableBean resultBean2 = resultMap2.remove(id);
			CRResolvableBean resultBean = e.getValue();
 			//MERGE ATTRIBUTES
 			if(resultBean!=null)
 			{
 				for(String attribute:attributes)
 				{
					resultBean.set(attribute, resultBean2.get(attribute));
 				}
 				result.add(resultBean);
 			}
 		}
 			
 		return(result);
 	}
 
 }
