 package com.gentics.cr;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.datasource.DatasourceException;
 import com.gentics.api.lib.exception.ParserException;
 import com.gentics.api.lib.expressionparser.ExpressionParserException;
 import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.exceptions.CRException;
 /**
  * The CMSPageLanguageFallbackRequestProcessor will return CMS Pages (obj_type
  * 10007) with default language settings published (contentid_<lang>...) in
  * a predefined fallbacked manner.
  * @author Christopher
  *
  */
 public class CMSPageLanguageFallbackRequestProcessor extends RequestProcessor {
 	/**
 	 * Key for the prioritized languages in the request Object.
 	 */
 	private static final String LANGUAGE_KEY = "langs";
 	
 	/**
 	 * Logger instance.
 	 */
 	private static Logger logger = Logger.getLogger(CRRequestProcessor.class);
 
 	/**
 	 * Create new Instance.
 	 * @param arg0 configuration.
 	 * @throws CRException on error.
 	 */
 	public CMSPageLanguageFallbackRequestProcessor(final CRConfig arg0) 
 		throws CRException {
 		super(arg0);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public final Collection<CRResolvableBean> getObjects(
 			final CRRequest request, final boolean doNavigation) 
 			throws CRException {
 		//FETCH LANG ORDER
 		Collection<String> langs = (Collection<String>) 
 			request.get(LANGUAGE_KEY);
 		//GENERATE PREFILLATTRIBS FOR LANG REQUEST
 		ArrayList<String> langPrefills = new ArrayList<String>();
		if (langs != null) {
			for (String lang : langs) {
				langPrefills.add("contentid_" + lang);
			}
 		}
 		Datasource ds = null;
 		DatasourceFilter dsFilter;
 		Vector<CRResolvableBean> collection = new Vector<CRResolvableBean>();
 		if (request != null) {
 			//GENERATE LANG REQUEST
 			CRRequest myREQ = request.Clone();
 			myREQ.setCountString("-1");
 			myREQ.setStartString("0");
 			
 			// Parse the given expression and create a datasource filter
 			try {
 				ds = this.config.getDatasource();
 				if (ds == null) {
 					throw (new DatasourceException("No Datasource available."));
 				}
 
 				dsFilter = myREQ.getPreparedFilter(config, ds);
 
 				// add base resolvables
 				if (this.resolvables != null) {
 					for (Iterator<String> it = this.resolvables.keySet()
 							.iterator(); it.hasNext();) {
 						String name = it.next();
 						dsFilter.addBaseResolvable(name,
 								this.resolvables.get(name));
 					}
 				}
 
 				
 				Collection<Resolvable> fallbackedColl = 
 						new ArrayList<Resolvable>();
 				// do the query
 				Collection<Resolvable> col = this.toResolvableCollection(
 								ds.getResult(dsFilter, 
 								langPrefills.toArray(new String[]{}),
 						myREQ.getStart().intValue(),
 						myREQ.getCount().intValue(),
 						myREQ.getSorting()));
 				
 				//REMOVE ALL NONFITTING RESOS
 				int count = request.getCount().intValue();
 				int start = request.getStart().intValue();
 				boolean all = count == -1;
 				int objectsToProcess = start + count;
 				for (Resolvable reso : col) {
 					boolean found = false;
					if (langs != null) {
					
						for (String lang : langs) {
							Resolvable langVersion = (Resolvable) 
								reso.get("contentid_" + lang);
							if (langVersion != null) {
								found = true;
								if (!fallbackedColl.contains(langVersion)) {
									fallbackedColl.add(langVersion);
								}
								break;
 							}
 						}
						
 					}
 					if (!found) {
 						fallbackedColl.add(reso);
 					}
 					if (!all && (fallbackedColl.size() >= objectsToProcess)) {
 							break;
 					}
 				}
 				
 				if (count != 0) {
 					Collection<Resolvable> sizedColl = 
 							new ArrayList<Resolvable>();
 					Iterator<Resolvable> it = fallbackedColl.iterator();
 					int counter = 0;
 					while (it.hasNext() && counter < objectsToProcess) {
 						Resolvable r = it.next();
 						if (counter >= start) {
 							sizedColl.add(r);
 						}
 						counter++;
 					}
 					fallbackedColl = sizedColl;
 				}
 				
 				//PREFILL THE COLLECTION
 				String[] prefillAttributes = request.getAttributeArray();
 				CRResolvableBean meta = new CRResolvableBean();
 				meta.set("objects", fallbackedColl);
 				myREQ.addObjectForFilterDeployment("meta", meta);
 				myREQ.setRequestFilter(createPrefillFilter("contentid"));
 				dsFilter = myREQ.getPreparedFilter(config, ds);
 				col = this.toResolvableCollection(ds.getResult(dsFilter, 
 						prefillAttributes,
 						myREQ.getStart().intValue(),
 						myREQ.getCount().intValue(),
 						myREQ.getSorting()));
 				
 				// convert all objects to serializeable beans
 				if (col != null) {
 					for (Resolvable reso : col) {
 						CRResolvableBean crBean = new CRResolvableBean(
 								reso, request.getAttributeArray());
 						if (this.config.getFolderType()
 								.equals(crBean.getObj_type())
 								&& doNavigation) {
 							//Process child elements
 							String fltr = "object.folder_id=='"
 								+ crBean.getContentid() + "'";
 							if (request.getChildFilter() != null) {
 								fltr += "AND (" + request.getChildFilter()
 									+ ")";
 							}
 							//If object is a folder => retrieve the children of
 							//the object
 							CRRequest childReq = request.Clone();
 							childReq.setRequestFilter(fltr);
 							crBean.fillChildRepository(
 									this.getNavigation(childReq));
 						}
 						collection.add(this.replacePlinks(crBean, request));
 					}
 				}
 
 			} catch (ParserException e) {
 				logger.error("Error getting filter for Datasource.", e);
 				throw new CRException(e);
 			} catch (ExpressionParserException e) {
 				logger.error("Error getting filter for Datasource.", e);
 				throw new CRException(e);
 			} catch (DatasourceException e) {
 				logger.error("Error getting result from Datasource.", e);
 				throw new CRException(e);
 			} 
 		}
 		return collection;
 	}
 	
 	/**
 	 * Creates a rule that will match all objects in the meta.objects 
 	 * collection.
 	 * @param idAttribute attribute that defines the unique object (contentid)
 	 * @return rule as String
 	 */
 	private String createPrefillFilter(final String idAttribute) {
 				
 		return "object." + idAttribute + " CONTAINSONEOF meta.objects";
 	}
 
 	@Override
 	public void finalize() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
