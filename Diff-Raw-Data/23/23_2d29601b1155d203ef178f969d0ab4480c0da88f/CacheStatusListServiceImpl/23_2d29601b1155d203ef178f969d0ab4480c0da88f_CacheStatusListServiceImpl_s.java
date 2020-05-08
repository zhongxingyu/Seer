 package com.scholastic.sbam.server.servlets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.data.LoadConfig;
 import com.scholastic.sbam.client.services.CacheStatusListService;
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.server.fastSearch.CustomerCache;
 import com.scholastic.sbam.server.fastSearch.HelpTextCache;
 import com.scholastic.sbam.server.fastSearch.InstitutionCache;
 import com.scholastic.sbam.server.fastSearch.SiteInstitutionCache;
 import com.scholastic.sbam.server.fastSearch.InstitutionCache.InstitutionCacheConflict;
 import com.scholastic.sbam.shared.objects.CacheStatusInstance;
import com.sun.tools.hat.internal.util.ArraySorter;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class CacheStatusListServiceImpl extends AuthenticatedServiceServlet implements CacheStatusListService {
 
 	@Override
 	public List<CacheStatusInstance> listCacheStatus(LoadConfig loadConfig) throws IllegalArgumentException {
 		
		authenticate("list link types");	//	SecurityManager.ROLE_CONFIG);
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		List<CacheStatusInstance> list = new ArrayList<CacheStatusInstance>();
 		try {
 			
 			String [] keys = (String []) loadConfig.get("cacheKeys");
			if (keys == null || keys.length == 0) 
 				keys = CacheStatusInstance.getAllCacheKeys();
			ArraySorter.sortArrayOfStrings(keys);
 			
 			for (String key : keys) {
 				appendCache(key, list);
 			}
 
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		
 		HibernateUtil.endTransaction();
 		HibernateUtil.closeSession();
 		
 		return list;
 	}
 	
 	public void appendCache(String key, List<CacheStatusInstance> list) throws InstitutionCacheConflict, Exception {
 		if (CacheStatusInstance.CUSTOMER_CACHE_KEY.equals(key)) {
 			
 			list.add(CustomerCache.getSingleton().getCacheStatus());
 			
 		} else if (CacheStatusInstance.HELP_TEXT_CACHE_KEY.equals(key)) {
 			
 			list.add(HelpTextCache.getSingleton().getCacheStatus());
 			
 		} else if (CacheStatusInstance.INSTITUTION_CACHE_KEY.equals(key)) {
 			
 			list.add(InstitutionCache.getSingleton().getCacheStatus());
 			
 		} else if (CacheStatusInstance.SITE_CACHE_KEY.equals(key)) {
 			
 			list.add(SiteInstitutionCache.getSingleton().getCacheStatus());
 			
 		} else
 			throw new IllegalArgumentException("Invalid cache key value " + key + ".");
 	}
 }
