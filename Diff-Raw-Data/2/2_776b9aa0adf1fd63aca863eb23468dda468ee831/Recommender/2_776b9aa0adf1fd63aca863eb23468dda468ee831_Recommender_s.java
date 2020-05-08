 package edu.usu.cosl.recommender;
 
 import java.sql.PreparedStatement;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Vector;
 import java.io.IOException;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.Hit;
 import org.apache.lucene.search.HitIterator;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.similar.MoreLikeThis;
 
 import edu.usu.cosl.recommenderd.Base;
 import edu.usu.cosl.recommenderd.EntryInfo;
 import edu.usu.cosl.util.Locales;
 
 public class Recommender extends Base 
 {
 
 	private PreparedStatement pstFlagEntryRecommended;
     private PreparedStatement pstGetRecommendationID;
 	private PreparedStatement pstAddRecommendation;
 	private PreparedStatement pstUpdateRecommendation;
 	private PreparedStatement pstSetDocumentRecommendations;
 	private PreparedStatement pstGetEntryRecommendations;
 	private int nGlobalAggregationID;
 	private int nEntry;
 	
 	private void updateRecommendations(Vector<Integer> vIDs, boolean bGenerateRecommendations) {
 		nEntry = 1;
 		Enumeration<Integer> eIDs = vIDs.elements(); 
 		while (eIDs.hasMoreElements()) {
 			updateRecommendations(eIDs, bGenerateRecommendations);
 			try {
 				cn.close();
 				reloadDBDriver();
 				cn = getConnection();
 			}catch(Exception e) {}
 			System.gc();
 		}
 	}
 	private void updateRecommendations(Enumeration<Integer> eIDs, boolean bGenerateRecommendations)
 	{
 		try
 		{
 			createAnalyzers();
 			createIndexReaders();
 			createIndexSearchers();
 		    
 		    try
 		    {
 				pstGetRecommendationID = cn.prepareStatement(
 					"SELECT id, clicks, avg_time_at_dest FROM recommendations WHERE entry_id = ? AND dest_entry_id = ?");
 				
 				pstAddRecommendation = cn.prepareStatement(
 					"INSERT INTO recommendations (entry_id, dest_entry_id, rank, relevance) " +
 					"VALUES (?, ?, ?, ?)");
 				
 				pstUpdateRecommendation = cn.prepareStatement(
 					"UPDATE recommendations SET rank = ?, relevance = ? WHERE id = ? ");
 				
 				pstSetDocumentRecommendations = cn.prepareStatement(
 					"UPDATE entries SET popular = ?, relevant = ?, other = ?, " + 
 					"relevance_calculated_at = now() WHERE id = ?");
 				
 				pstFlagEntryRecommended = cn.prepareStatement(
 					"UPDATE entries SET relevance_calculated_at = now() WHERE id = ?");
 		
 				pstGetEntryRecommendations = cn.prepareStatement("SELECT e.*, e.id AS id, r.id AS recommendation_id, f.short_title FROM recommendations AS r INNER JOIN entries AS e ON r.dest_entry_id = e.id INNER JOIN feeds f ON e.feed_id = f.id WHERE entry_id = ?");
 			
 				PreparedStatement pstEntryToCreateRecommendationsFor = cn.prepareStatement(
 					"SELECT id, feed_id, permalink, direct_link, title, description, language_id, grain_size " +
 					"FROM entries WHERE id = ?");
 				
 //				for (Enumeration<Integer> eIDs = vIDs.elements(); eIDs.hasMoreElements();)
 				while (nEntry % 1000 != 0 && eIDs.hasMoreElements())
 				{
 					pstEntryToCreateRecommendationsFor.setInt(1, eIDs.nextElement().intValue());
 					ResultSet rsEntryToCreateRecommendationsFor = pstEntryToCreateRecommendationsFor.executeQuery();
 					if (rsEntryToCreateRecommendationsFor.next())
 					{
 						if (bGenerateRecommendations) {
 							updateRecommendationsForEntry(new EntryInfo(rsEntryToCreateRecommendationsFor));
 						} else {
 							updateRecommendationCacheForEntry(new EntryInfo(rsEntryToCreateRecommendationsFor));
 						}
 						nEntry++;
 					}
 					rsEntryToCreateRecommendationsFor.close();
 //					if (nEntry % 100 == 0)
 //					{
 //						pstSetDocumentRecommendations.executeBatch();
 //						pstUpdateRecommendation.executeBatch();
 //					}
 					if (nEntry % 1000 == 0)logger.info("Recommending: " + nEntry);
 					else if (nEntry % 100 == 0)logger.debug("Recommending: " + nEntry);
 				}
 				nEntry++;
 				pstEntryToCreateRecommendationsFor.close();
 				
 				pstGetEntryRecommendations.close();
 	
 				pstFlagEntryRecommended.executeBatch();
 				pstSetDocumentRecommendations.executeBatch();
 				pstUpdateRecommendation.executeBatch();
 				
 				pstFlagEntryRecommended.close();
 				pstSetDocumentRecommendations.close();
 				pstUpdateRecommendation.close();
 				pstAddRecommendation.close();
 				
 				pstGetRecommendationID.close();
 			}
 		    catch (Exception e)
 		    {
 				logger.error("updateRecommendations(1) - ", e);
 		    }
 		    closeIndexSearchers();
 		    closeIndexReaders();
 		    closeCores();
 		}
 		catch (Exception e)
 		{
 			logger.error("updateRecommendations(2) - ", e);
 		}
 	}
 
 	private double relevanceAverage(Vector<EntryInfo> vEntries)
 	{
 		double dSum = 0;
 		for (Enumeration<EntryInfo> eEntries = vEntries.elements(); eEntries.hasMoreElements();)
 		{
 			EntryInfo entry = eEntries.nextElement();
 			dSum += entry.dRelevance;
 		}
 		return dSum / vEntries.size();
 	}
 	private double relevanceStandardDeviation(double dAverage, Vector<EntryInfo> vEntries)
 	{
 		double dSum = 0;
 		for (Enumeration<EntryInfo> eEntries = vEntries.elements(); eEntries.hasMoreElements();)
 		{
 			EntryInfo entry = eEntries.nextElement();
 			dSum += Math.pow((entry.dRelevance - dAverage),2);
 		}
 		return Math.sqrt(dSum / vEntries.size());
 	}
 	
 	private double calcRelevanceThreshold(Vector<EntryInfo> vEntries)
 	{
 		if (vEntries.size() == 0) return 0;
 		double dAverage = relevanceAverage(vEntries);
 		double dStandardDeviation = relevanceStandardDeviation(dAverage, vEntries);
 		return dAverage + dStandardDeviation;
 	}
 	
 	private double clickAverage(Vector<EntryInfo> vEntries)
 	{
 		double dSum = 0;
 		for (Enumeration<EntryInfo> eEntries = vEntries.elements(); eEntries.hasMoreElements();)
 		{
 			EntryInfo entry = eEntries.nextElement();
 			dSum += entry.nClicks;
 		}
 		return dSum / vEntries.size();
 	}
 	private double clickStandardDeviation(double dAverage, Vector<EntryInfo> vEntries)
 	{
 		double dSum = 0;
 		for (Enumeration<EntryInfo> eEntries = vEntries.elements(); eEntries.hasMoreElements();)
 		{
 			EntryInfo entry = eEntries.nextElement();
 			dSum += Math.pow((entry.nClicks - dAverage),2);
 		}
 		return Math.sqrt(dSum / vEntries.size());
 	}
 	
 	private int calcClickThreshold(Vector<EntryInfo> vEntries)
 	{
 		int nClickThreshold = 5;
 		if (vEntries.size() != 0)
 		{
 			double dAverage = clickAverage(vEntries);
 			double dStandardDeviation = clickStandardDeviation(dAverage, vEntries);
 			nClickThreshold = (int)Math.round(dAverage + dStandardDeviation);
 		}
 		return nClickThreshold > 5 ? nClickThreshold : 5;
 	}
 	
 	static private String getEntryJSON(EntryInfo entry)
 	{
 		if (entry.nRecommendationID == 0) {
 			logger.debug("oh no!");
 		}
 		return "{" 
 		+ "\"id\": " + entry.nRecommendationID 
 		+ ", \"uri\": \"" + entry.sURI + "\"" 
 		+ ", \"direct_link\": \"" + entry.sDirectLink + "\"" 
 		+ ", \"title\": \"" + quoteEncode(entry.sTitle) + "\"" 
 		+ ", \"collection\": \"" + quoteEncode(entry.sFeedShortTitle) + "\"" 
 //		+ ", \"clicks\": 0" 
 //		+ ", \"avg_time_on_target\": 60" 
 		+ ", \"clicks\": " + entry.nClicks 
 		+ ", \"avg_time_on_target\": " + entry.lAvgTimeAtDest 
 		+ ", \"relevance\": " + entry.dRelevance 
 		+ "}";
 	}
 	
 	// we calculate popularity by combining average time on page and number of clicks
 	// average time on page is the true measure of popularity
 	// number of clicks adds certainty to the average time on page
 	// first we find the entries that have been clicked on a lot more than other pages
 	// then for those entries, we rank them according to average time on page
 	
 	private void storeRecommendationsInEntry(EntryInfo entry, Vector<EntryInfo> vRelatedEntries) throws SQLException
 	{
 		double dThreshold = calcRelevanceThreshold(vRelatedEntries);
 		int nClickThreshold = calcClickThreshold(vRelatedEntries);
 		
 		String sPopular = "[";
 		String sRelevant = "[";
 		String sOther = "[";
 		
 		Vector<EntryInfo> vPopular = new Vector<EntryInfo>(); 
 		
 		// sort the list into popular, relevant, and other
 		for (Enumeration<EntryInfo> eRelatedEntries = vRelatedEntries.elements(); eRelatedEntries.hasMoreElements();)
 		{
 			EntryInfo relatedEntry = eRelatedEntries.nextElement();
 			
 			// popular recommendations have more than one standard deviation of clicks above the average
 			if (relatedEntry.nClicks >= nClickThreshold)
 			{
 				vPopular.add(relatedEntry);
 				continue;
 			}
 			String sJSON = getEntryJSON(relatedEntry);
 			
 			// relevant recommendations have a relevance more than one standard deviation above the average
 			if (relatedEntry.dRelevance > dThreshold) { 
 				sRelevant += sRelevant.length() > 1 ? "," + sJSON : sJSON;
 			// other is everything else
 			} else { 
 				sOther += sOther.length() > 1 ? "," + sJSON : sJSON;
 			}
 		}
 		
 		// sort the popular entries by average time on page
 		Collections.sort(vPopular);
 		for (Enumeration<EntryInfo> ePopular = vPopular.elements(); ePopular.hasMoreElements();)
 		{
 			EntryInfo popularEntry = ePopular.nextElement();
 			String sJSON = getEntryJSON(popularEntry);
 			sPopular += sPopular.length() > 1 ? "," + sJSON : sJSON; 
 		}
 		
 		sPopular += "]";
 		sRelevant += "]";
 		sOther += "]";
 		
 //		logger.debug("Relevant: " + sRelevant);
 //		logger.debug("Other: " + sOther);
 		
 		pstSetDocumentRecommendations.setString(1, sPopular);
 		pstSetDocumentRecommendations.setString(2, sRelevant);
 		pstSetDocumentRecommendations.setString(3, sOther);
 		pstSetDocumentRecommendations.setInt(4, entry.nEntryID);
 		pstSetDocumentRecommendations.executeUpdate();
 	}
 	
 	private void flagEntryRecommended(int nEntryID) throws SQLException
 	{
 		pstFlagEntryRecommended.setInt(1, nEntryID);
 		pstFlagEntryRecommended.addBatch();
 	}
 	
 	private String getCommaSeparatedList(Vector<EntryInfo> vEntries)
 	{
 		Enumeration<EntryInfo> eEntries = vEntries.elements();
 		String sList = "(" + eEntries.nextElement().nEntryID;
 		while (eEntries.hasMoreElements())
 		{
 			sList += ", " + eEntries.nextElement().nEntryID;
 		}
 		return sList + ")";
 	}
 	
 	private void deleteNoLongerRelevantRecommendations(EntryInfo entry, Vector<EntryInfo> vRecommendations) throws SQLException
 	{
 		Statement stDeleteRecommendations = cn.createStatement();
 		String sIDList = getCommaSeparatedList(vRecommendations);
 		stDeleteRecommendations.executeUpdate("DELETE FROM recommendations WHERE entry_id = " + entry.nEntryID + " AND dest_entry_id NOT IN " + sIDList);
 		stDeleteRecommendations.close();
 	}
 	
 	private Vector<EntryInfo> getRelatedEntries(EntryInfo entry) throws Exception
 	{
 		Vector<EntryInfo> vEntries = new Vector<EntryInfo>();
 		try
 		{
 			String sCode = Locales.getCode(entry.nLanguageID);
 			
 			// if we don't have an anlyzer for the language, bail
 			Analyzer analyzer = htAnalyzers.get(sCode);
 		    if (analyzer == null) return vEntries;
 		    
 		    // find the lucene document for the entry
 			TermQuery query = new TermQuery(new Term("id","Entry:" + entry.nEntryID));
 			IndexSearcher searcher = htSearchers.get(sCode);
 		    Hits hits = searcher.search(query);
 		    int nDocID = hits.id(0);
 		    if (hits.length() == 0 || nDocID == 0) return vEntries;
 		    
 		    // ask lucene for more entries like this on
 		    IndexReader reader = htReaders.get(sCode);
 		    MoreLikeThis mlt = new MoreLikeThis(reader);
 		    mlt.setMinTermFreq(1);
 		    mlt.setAnalyzer(analyzer);
 		    mlt.setFieldNames(new String[]{"text"});
 		    mlt.setMinWordLen(("zh".equals(sCode) || "ja".equals(sCode)) ? 1 : 4);
 		    mlt.setMinDocFreq(2);
 		    mlt.setBoost(true);
 		    Query like = mlt.like(nDocID);
 			TermQuery globalAggregationQuery = new TermQuery(new Term("aggregation_i","" + nGlobalAggregationID));
 			BooleanQuery gabq = new BooleanQuery();
 			gabq.add(globalAggregationQuery, BooleanClause.Occur.MUST);
 			Query lq = Query.mergeBooleanQueries(new Query[]{like,gabq});
 		    Hits relatedDocs = searcher.search(lq);
 //		    Hits relatedDocs = searcher.search(like);
 		    
 			int nSameDomainHits = 0;
 			int nOtherDomainHits = 0;
 			int nEnd = entry.sURI.indexOf("/", 10);
 			String sDomain = nEnd == -1 ? entry.sURI : entry.sURI.substring(0, nEnd);
 			String sNormalizedEntryTitle = normalizedTitle(entry.sTitle);
 			HashSet<String> hsRecommendations = new HashSet<String>();
 			hsRecommendations.add(entry.sURI);
 			if (entry.sDirectLink != null) hsRecommendations.add(entry.sDirectLink);
 			int nHit = 0;
 			
 			// loop through the related entries
 		    for (HitIterator docs = (HitIterator)relatedDocs.iterator(); docs.hasNext() && !(nSameDomainHits == nMaxRecommendations && nOtherDomainHits == nMaxRecommendations) && nHit < 100;)
 		    {
 				nHit++;
 	
 				Hit hit = (Hit)docs.next();
 		    	Document lDoc = hit.getDocument();
 		    	
 		    	// don't include ourselves as a recommendation
 		    	if (nDocID == hit.getId()) continue;
 		    	int id=hit.getId();
 
 		    	String sTitle = null;
 				String sNormalizedTitle = null;
 				String sURI = null;
 				String sDirectLink = null;
 				double dRelevance = 0;
 		    	try
 		    	{
 			    	sTitle = lDoc.getField("title").stringValue();
 					sNormalizedTitle = normalizedTitle(sTitle);
 					sURI = lDoc.getField("permalink").stringValue();
 					Field directLink = lDoc.getField("direct_link");
 					sDirectLink = directLink == null ? "" : directLink.stringValue();
 					dRelevance = hit.getScore();
 		    	}
 		    	catch (Exception e3)
 		    	{
 		    		// some times for some strange reason entries don't have permalinks and this blows chunks
 		    		continue;
 		    	}
 	
 		    	// avoid adding duplicate or near duplicate titles (MIT courses offered at different times)
 				if (!(
 						hsRecommendations.contains(sNormalizedTitle) // same title 
 					 || hsRecommendations.contains(sURI) // same uri
 					 || hsRecommendations.contains(sDirectLink) // same direct link
 					 || (dRelevance > 0.99 && sNormalizedTitle.equals(sNormalizedEntryTitle)) // perfect relevance and same title as the source document
 				   ))
 				{
 //					logger.debug("hit: " + nHit++);
 					boolean bSameDomain = sURI.startsWith(sDomain);
 					
 					if (nSameDomainHits == nMaxRecommendations && bSameDomain ||
 						nOtherDomainHits == nMaxRecommendations && !bSameDomain) continue;
 					
 					if (bSameDomain) nSameDomainHits++;
 					else nOtherDomainHits++;
 					
 			    	EntryInfo relatedEntry = new EntryInfo();
 			    	relatedEntry.nEntryID = Integer.parseInt(lDoc.getField("pk_i").stringValue());
 			    	relatedEntry.sURI = sURI;
 			    	relatedEntry.sDirectLink = sDirectLink;
 			    	relatedEntry.sTitle = sTitle;
 			    	relatedEntry.dRelevance = dRelevance;
 			    	Integer intFeedID = new Integer(lDoc.getField("feed_id_i").stringValue());
 			    	relatedEntry.nFeedID = intFeedID.intValue();
 			    	relatedEntry.sFeedShortTitle = lDoc.getField("collection").stringValue(); 
 			    	
 //			    	logger.debug("Relevance:" + relatedEntry.dRelevance + " " + nSameDomainHits + ", " + nOtherDomainHits);
 
 			    	vEntries.add(relatedEntry);
 
 			    	// keep track of titles and uris so we don't duplicate them
 			    	hsRecommendations.add(sNormalizedTitle);
 					hsRecommendations.add(sURI);
					if (sDirectLink != null) hsRecommendations.add(sDirectLink);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error processing getRelatedEntries for entry ID: " + entry.nEntryID);
 			logger.error(e);
 		}
 		return vEntries;
 	}
 
 	private Vector<EntryInfo> getRelatedEntriesFromDB(EntryInfo entry) throws Exception
 	{
 		try
 		{
 			Vector<EntryInfo> vEntries = new Vector<EntryInfo>();
 			pstGetEntryRecommendations.setInt(1, entry.nEntryID);
 			ResultSet rsRecommendations = pstGetEntryRecommendations.executeQuery();
 			while (rsRecommendations.next()) {
 				EntryInfo e = new EntryInfo(rsRecommendations);
 				e.nRecommendationID = rsRecommendations.getInt("recommendation_id");
 				e.sFeedShortTitle = rsRecommendations.getString("short_title");
 				vEntries.add(e);
 			}
 			rsRecommendations.close();
 			return vEntries;
 		}
 		catch (Exception e)
 		{
 			logger.error("Error in getRelatedEntriesFromDB", e);
 			throw e;
 		}
 	}
 	
 	private void updateRecommendationCacheForEntry(EntryInfo entry) throws Exception
 	{
 		try
 		{
 			Vector<EntryInfo> vRecommendations = getRelatedEntriesFromDB(entry);
 			if (vRecommendations.size() > 0)
 			{
 				storeRecommendationsInEntry(entry,vRecommendations);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error in updateRecommendationCacheForEntry");
 			throw e;
 		}
 	}
 	
 	private void updateRecommendationsForEntry(EntryInfo entry) throws Exception
 	{
 		try
 		{
 			Vector<EntryInfo> vRecommendations = getRelatedEntries(entry);
 			if (vRecommendations.size() > 0)
 			{
 				addRecommendationsToDB(entry, vRecommendations);
 				storeRecommendationsInEntry(entry,vRecommendations);
 				deleteNoLongerRelevantRecommendations(entry, vRecommendations);
 			}
 			else flagEntryRecommended(entry.nEntryID);
 		}
 		catch (Exception e)
 		{
 			logger.error("Error in updateEntryRecommendations");
 			throw e;
 		}
 	}
 	
 	private void addRecommendation(EntryInfo entry, EntryInfo relatedEntry, int nRank) throws SQLException
 	{
 		pstAddRecommendation.setInt(1, entry.nEntryID);
 		pstAddRecommendation.setInt(2, relatedEntry.nEntryID);
 		pstAddRecommendation.setInt(3, nRank);
 		pstAddRecommendation.setDouble(4, relatedEntry.dRelevance);
 		pstAddRecommendation.execute();
 		relatedEntry.nRecommendationID = getLastID(pstAddRecommendation);
 	}
 	
 	private void updateRecommendation(EntryInfo relatedEntry, int nRank) throws SQLException
 	{
 		pstUpdateRecommendation.setInt(1, nRank);
 		pstUpdateRecommendation.setDouble(2, relatedEntry.dRelevance);
 		pstUpdateRecommendation.setInt(3, relatedEntry.nRecommendationID);
 		pstUpdateRecommendation.addBatch();
 	}
 	
 	private void getRecommendationInfo(int nEntryID, EntryInfo relatedEntry) throws SQLException
 	{
 		pstGetRecommendationID.setInt(1, nEntryID);
 		pstGetRecommendationID.setInt(2, relatedEntry.nEntryID);
 		ResultSet rsRecommendationID = pstGetRecommendationID.executeQuery();
 		if (rsRecommendationID.next())
 		{
 			relatedEntry.nRecommendationID = rsRecommendationID.getInt("id");
 			relatedEntry.nClicks = rsRecommendationID.getInt("clicks");
 			relatedEntry.lAvgTimeAtDest = rsRecommendationID.getLong("avg_time_at_dest");
 		}
 		else relatedEntry.nRecommendationID = 0;
 		rsRecommendationID.close();
 	}
 
 	private void addRecommendationsToDB(EntryInfo entry, Vector<EntryInfo> vRelatedEntries) throws SQLException
 	{
 		try
 		{
 			int nRank = 1;
 			for (Enumeration<EntryInfo> eEntries = vRelatedEntries.elements(); eEntries.hasMoreElements();)
 			{
 				EntryInfo relatedEntry = eEntries.nextElement();
 	
 				getRecommendationInfo(entry.nEntryID, relatedEntry);
 				
 				if (relatedEntry.nRecommendationID == 0)
 				{
 					addRecommendation(entry, relatedEntry, nRank);
 				}
 				else
 				{
 					updateRecommendation(relatedEntry, nRank);
 				}
 				nRank++;
 			}
 			pstUpdateRecommendation.executeBatch();
 		}
 		catch (SQLException e)
 		{
 			logger.error("Error in addRecommendationsToDB");
 			throw e;
 		}
 	}
 	
 	private void updateRecommendations(boolean bRedoAllRecommendations) throws Exception
 	{
 		logger.info("==========================================================Create Recommendations");
 		cn = getConnection();
 		nGlobalAggregationID = getGlobalAggregationID(cn);
 		Vector<Integer> vIDs = getIDsOfEntries(bRedoAllRecommendations ? "":"WHERE indexed_at > relevance_calculated_at");
 		if (vIDs.size() > 0) {
 			logger.info("updateRecommendations - begin (entries to update): " + vIDs.size());
 			updateRecommendations(vIDs, true);
 			logger.info("updateRecommendations - end");
 		}
 		cn.close();
 	}
 	
 	private void updateCache() throws Exception
 	{
 		logger.info("==========================================================Rebuild recommendation caches");
 		cn = getConnection();
 		Vector<Integer> vIDs = getIDsOfEntries("");
 		if (vIDs.size() > 0) {
 			logger.info("updateRecommendationCache - begin (entries to update): " + vIDs.size());
 			updateRecommendations(vIDs, false);
 			logger.info("updateRecommendationCache - end");
 		}
 		cn.close();
 	}
 
 	public static void rebuildCache(String sPropertiesFile) throws Exception
 	{
 		Recommender r = new Recommender();
 		r.loadOptions(sPropertiesFile);
 		r.updateCache();
 	}
 	
 	public static void update(String sPropertiesFile, boolean bRedoAllRecommendations) throws Exception
 	{
 		Recommender r = new Recommender();
 		r.loadOptions(sPropertiesFile);
 		r.updateRecommendations(bRedoAllRecommendations);
 	}
 
 	public static void main(String[] args) 
 	{
 		try {
 			String sPropertiesFile = args.length > 0 ? args[0] : "recommenderd.properties";
 			String sTask = args.length > 1 ? args[1] : "full";
 			if ("rebuild_cache".equals(sTask)) rebuildCache(sPropertiesFile);
 			else update(sPropertiesFile, true);
 		} catch (Exception e) {
 			logger.error(e);
 		}
 	}
 
 }
