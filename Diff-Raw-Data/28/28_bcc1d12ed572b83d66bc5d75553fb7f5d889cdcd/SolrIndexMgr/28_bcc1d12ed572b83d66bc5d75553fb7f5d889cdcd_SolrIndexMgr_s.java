 package org.neuro4j.storage.solr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrInputDocument;
 import org.neuro4j.core.ERBase;
 import org.neuro4j.core.Entity;
 import org.neuro4j.core.Network;
 import org.neuro4j.core.Relation;
 import org.neuro4j.storage.StorageException;
 import org.neuro4j.storage.qp.NQLProcessor;
 import org.neuro4j.utils.KVUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 public class SolrIndexMgr {
 
 
 	/**
 	 * 
 	 * Max fully loaded connections (e.g. relations per relation group name)
 	 * 
 	 * E.g. Imagine user uploaded 1000 photos (have 1000 relations). 
 	 * Queried network will have MAX_QUERIED_CONNECTIONS_LIMIT loaded relations for user object (others - just ids)
 	 * 
 	 * MAX_QUERIED_CONNECTIONS_LIMIT is used for single entity / relation 
 	 * 
 	 */
 	private int MAX_QUERIED_CONNECTIONS_LIMIT = NQLProcessor.DEFAULT_MAX_QUERIED_CONNECTIONS_LIMIT; 
 
 	/**
 	 * if sorl query is short - GET method is used
 	 * if solr query longer -> POST method is used
 	 */
 //	private final int MAX_QUERY_SIZE_FOR_GET_METHOD = 3000; 
 	
 //	private final int QUERIED_ROWS_LIMIT = 1000; 
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	/**
 	 * Solr URL e.g. http://localhost:8080/solr/n4j-storage
 	 */
 	private String solrCoreURL = null;
 
 	private SolrServer solrServer = null;
 	
 	public SolrIndexMgr(Properties properties)
 	{
 		solrCoreURL = KVUtils.getStringProperty(properties, "n4j.storage.solr.server_url");
 		solrServer = SearchIndexHandler.getSolrServer(solrCoreURL);
 		
 //		MAX_QUERIED_ROWS = KVUtils.getIntProperty(properties, "n4j.storage.solr.max_queried_rows", MAX_QUERIED_ROWS);
 		MAX_QUERIED_CONNECTIONS_LIMIT = KVUtils.getIntProperty(properties, "n4j.storage.max_queried_connections_limit", MAX_QUERIED_CONNECTIONS_LIMIT);
 	}
 	
 
 	public void saveOrUpdate(ERBase... ers)  throws StorageException
 	{
 	    List<SolrInputDocument> batchDocumentList = new ArrayList<SolrInputDocument>();
 		
 	    for (ERBase er : ers)
 	    {
 	        SolrInputDocument doc = SearchIndexHandler.createSolrInputDocument(er);
 	    	batchDocumentList.add(doc);
 	    }
     	SearchIndexHandler.sendData4Index(solrServer, batchDocumentList);		
 	}
 
 //	public void saveOrUpdate(Entity... entities)  throws StorageException
 //	{
 //	    List<SolrInputDocument> batchDocumentList = new ArrayList<SolrInputDocument>();
 //		
 //	    for (Entity entity : entities)
 //	    {
 //	        SolrInputDocument doc = SearchIndexHandler.createSolrInputDocument(entity);
 //	    	batchDocumentList.add(doc);
 //	    }
 //    	SearchIndexHandler.sendData4Index(solrServer, batchDocumentList);		
 //	}
 //
 //	public void saveOrUpdate(Relation... relations) throws StorageException
 //	{
 //	    List<SolrInputDocument> batchDocumentList = new ArrayList<SolrInputDocument>();
 //		
 //	    for (Relation relation : relations)
 //	    {
 //	        SolrInputDocument doc = SearchIndexHandler.createSolrInputDocument(relation);
 //	    	batchDocumentList.add(doc);
 //	    }
 //    	SearchIndexHandler.sendData4Index(solrServer, batchDocumentList);
 //	}
 	
 	public void commit() throws StorageException
 	{
 		SearchIndexHandler.commit(solrServer);
 	}
 
 
 	public String[] getIdsBy(String field, String value)
 	{
     	SolrQuery solrQuery = new SolrQuery();
     	solrQuery.add("fl", "UUID score");
     	
     	if (!"UUID".equalsIgnoreCase(field) && !"name".equalsIgnoreCase(field))
     		field = SearchIndexConfiguration.PROPERTY_PREFIX + field;
 
     	solrQuery.setQuery(field + ":" + value);
     	solrQuery.add("fl", SearchIndexConfiguration.FIELD_UUID); // return ids only
 
     	Iterator<SolrDocument> iter = query(solrQuery);
 
     	Set<String> idSet = SearchIndexHandler.response2Ids(iter);
     	String[] ids = new String[idSet.size()];
         idSet.toArray(ids);
 
     	return ids;
 	}
 
 	public ERBase getById(String uuid)
 	{
     	SolrQuery solrQuery = new SolrQuery();
 //    	solrQuery.add("fl", "UUID score"); // return all fields
 
     	solrQuery.setQuery(SearchIndexConfiguration.FIELD_UUID + ":" + uuid);
 
     	Iterator<SolrDocument> iter = query(solrQuery);
 
 		if (iter.hasNext())
 		{
 			SolrDocument doc = iter.next();
             String erType = (String) doc.getFieldValue(SearchIndexConfiguration.FIELD_ER_TYPE); 
             if (SearchIndexConfiguration.ER_TYPE_ENTITY.equals(erType))
             	return SearchIndexHandler.doc2entity(doc);
             else if (SearchIndexConfiguration.ER_TYPE_RELATION.equals(erType))
             	return SearchIndexHandler.doc2relation(doc);
 		}
 
 		return null;
 	}
 	
 	public Set<ERBase> getByIds(Set<String> ids)
 	{
 		Set<ERBase> erset = new HashSet<ERBase>();
     	SolrQuery solrQuery = new SolrQuery();
 //    	solrQuery.add("fl", "UUID score"); // return all fields
 
     	StringBuffer sqSB = new StringBuffer();
     	// UUID:(Ot2p_oc0Vw8AAAE6ozVcZi3m jdqp_oc0Vw4AAAE6ozVcZi3m)
     	sqSB.append(SearchIndexConfiguration.FIELD_UUID + ":(");
     	for (String id : ids)
     		sqSB.append(id).append(" ");
     	sqSB.append(") ");
     	
     	solrQuery.setQuery(sqSB.toString());
 
     	Iterator<SolrDocument> iter = query(solrQuery);
 
 		while (iter.hasNext())
 		{
 			SolrDocument doc = iter.next();
 			erset.add(SearchIndexHandler.doc2erbase(doc));
 		}
 
 		return erset;
 	}
 	
 	public Entity getEntityById(String uuid)
 	{
 		ERBase er = getById(uuid);
 		if (null != er)
 			return (Entity) er;
 			
         return null;
 	}
 	
 	public void deleteById(String... uuids) throws StorageException
 	{
 		try {
 			
 			for (String id : uuids)
 			{
 				solrServer.deleteById(id);
 				
 				//start update tails
 		    	SolrQuery solrQuery = new SolrQuery();
 		    	solrQuery.setQuery("entities:(" + id + ") OR relations:(" + id + ")");
 
 		    	Iterator<SolrDocument> iter = query(solrQuery);
 
 	    		while (iter.hasNext())
 	    		{
 	    			SolrDocument doc = iter.next();
 	    			String docERType = (String) doc.getFieldValue(SearchIndexConfiguration.FIELD_ER_TYPE);
 	    			if(SearchIndexConfiguration.ER_TYPE_ENTITY.equals(docERType))
 	    			{
 	                    Entity e = SearchIndexHandler.doc2entity(doc);
 	                    e.removeRelation(id);
 	                    saveOrUpdate(e);
 	    			} else if (SearchIndexConfiguration.ER_TYPE_RELATION.equals(docERType)) {
 	                    Relation r = SearchIndexHandler.doc2relation(doc);
 	                    r.removeParticipant(id);
 	                    saveOrUpdate(r);
 	    				
 	    			} else {
 	    				throw new RuntimeException("Wrong doc type (FIELD_ER_TYPE)");
 	    			}
 	    		}
 
 			} // for (String id : uuids)
 			
 			
 	    	
             solrServer.commit(true, true);
 		} catch (SolrServerException e) {
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		return;
 	}
     
     public void cleanup() throws StorageException
     {
     	SearchIndexHandler.clearIndex(solrServer);
     }
 
 	public Network getNetworkByQuery(String[] previousIds, String previousQueryType, String queryType, String solrQueryStr) {
     	SolrQuery solrQuery = new SolrQuery();
     	
     	StringBuffer sqSB = new StringBuffer();
     	if (null != previousQueryType)
     	{
 
         	if ("entity".equals(previousQueryType))
         		sqSB.append("entities:("); 
         	else if ("relation".equals(previousQueryType))
         		sqSB.append("relations:("); 
         	
 			for (String id : previousIds)
 				sqSB.append(id).append(" ");
 			sqSB.append(")");
     		
 			sqSB.append("  AND ( ");
     	}
     	
 		sqSB.append(SearchIndexConfiguration.FIELD_ER_TYPE)
 			.append(":")
 			.append(queryType)
 			.append(" AND ")
 			.append("(")
 			.append(solrQueryStr)
 			.append(")");
 
     	if (null != previousQueryType)
 			sqSB.append(" ) ");
 		
 	
     	solrQuery.setQuery(sqSB.toString());
 //    	solrQuery.add("fl", SearchIndexConfiguration.FIELD_UUID); // return ids only
 
     	Network net = new Network();
     	
     	Iterator<SolrDocument> iter = query(solrQuery);
 
     	SearchIndexHandler.docs2net(net, iter);
 
 		return net;
 	}
 
 	public Iterator<SolrDocument> query(String solrQuery)
 	{
 		return query(new SolrQuery(solrQuery));
 	}
 	
 	private Iterator<SolrDocument> query(SolrQuery solrQuery)
 	{
 		return new SolrIterator(this.solrServer, solrQuery);
 	}
 	
 
 	public Set<String> checkIds(Set<String> inIds, int maxAmount)
 	{
 		Set<String> outIds = new HashSet<String>(); 
 
 		int inPageCounter = 0;
 		int totalCounter = 0;
 
 		StringBuffer idListSB = new StringBuffer();
     	for (String id : inIds)
     	{
     		idListSB.append(id).append(" ");
     		inPageCounter++;
     		totalCounter++;
     		if (inPageCounter >= SolrIterator.SOLR_QUERY_PAGE_SIZE || totalCounter == inIds.size())
     		{
     			// send query
     			StringBuffer sqSB = new StringBuffer();
     	    	// UUID:(Ot2p_oc0Vw8AAAE6ozVcZi3m jdqp_oc0Vw4AAAE6ozVcZi3m)
     	    	sqSB.append("UUID:(");
     	    	sqSB.append(idListSB);
     	    	sqSB.append(") ");
 
     	    	SolrQuery solrQuery = new SolrQuery(sqSB.toString());
     	    	solrQuery.add("fl", SearchIndexConfiguration.FIELD_UUID); // return ids only
 
 
     	    	outIds.addAll(SearchIndexHandler.response2Ids(query(solrQuery)));
     	    	
     	    	idListSB = new StringBuffer();
     	    	inPageCounter = 0;
     		}
     		
     		if (maxAmount > 0)
     		{
     			if (outIds.size() >= maxAmount)
     				break;
     		}
     	} // for (String id : inIds)
     	
 		return outIds;
 	}
 	
 
 }
