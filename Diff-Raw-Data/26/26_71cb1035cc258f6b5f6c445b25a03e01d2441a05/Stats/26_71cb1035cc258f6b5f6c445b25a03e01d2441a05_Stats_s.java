 package the.app.admin;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 
 import com.mongodb.BasicDBObject;
 
 public class Stats extends Bean 
 {
 
 	/**
 	 * empty constructor
 	 */
 	public Stats() {}
 	
 	/**
 	 * loaded constructor
 	 * @param _id
 	 * @param name
 	 */
 	public Stats(String _id, String serverUsed, String db, String collections, String objects, String avgObjSize, String dataSize, String storageSize, String numExtents, 
 			String indexes, String indexSize, String fileSize, String nsSizeMB, String ok)
 	{
 		System.out.println("  new Status("+_id+","+serverUsed+")");
 		data.put("_id", _id);
 		data.put("serverUsed", serverUsed);
 		data.put("db", db);
 		data.put("collections", collections);
 		data.put("objects", objects);
 		data.put("avgObjSize", avgObjSize);
 		data.put("dataSize", dataSize);
 		data.put("storageSize", storageSize);
 		data.put("numExtents", numExtents);
 		data.put("indexes", indexes);
 		data.put("indexSize", indexSize);
 		data.put("fileSize", fileSize);
 		data.put("nsSizeMB", nsSizeMB);
 		data.put("ok", ok);
 		
 	}
 	
 	public Stats(BasicDBObject obj) 
 	{
		convertBasicDBObjectToLeague(obj);
 	}
 
 	// variables containing league collection information
	private String[] cols = {"_id",
			                 "serverUsed",
 			                 "db",
 			                 "collections",
 			                 "objects",
 			                 "avgObjSize",
 			                 "dataSize",
 			                 "storageSize",
 			                 "numExtents",
 			                 "indexes",
 			                 "indexSize",
			                 "fileSIze",
 			                 "nsSizeMB",
 			                 "ok"};
 	
 	private String[] types = {STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
 							  STRING_TYPE, 
							  STRING_TYPE, 
 							  STRING_TYPE};
 	private Hashtable data = new Hashtable();
 
 	// setters
	public void setId(String s) 			{ data.put("_id", s); }
 	public void serverUsed(String s) 		{ data.put("serverUsed", s); }
     public void setDb(String s)				{ data.put("db", s); }
     public void setCollections(String s)	{ data.put("collections", s); }
     public void setObjects(String s)		{ data.put("objects", s); }
     public void setAvgObjSize(String s)		{ data.put("avgObjSize", s); }
     public void setDataSize(String s)		{ data.put("dataSize", s); }
     public void setStorageSize(String s)	{ data.put("storageSize", s); }
     public void setNumExtents(String s)		{ data.put("numExtends", s); }
     public void setIndexes(String s)		{ data.put("indexes", s); }
     public void setIndexSize(String s)		{ data.put("indexSize", s); }
     public void setFileSIze(String s)		{ data.put("fileSize", s); }
     public void setNsSizeMB(String s)		{ data.put("nsSizeMB", s); }
     public void setOk(String s)				{ data.put("ok", s); }
 	
 	// getters
	public String getId() 					{ return (String)data.get("_id"); }
 	public String getServerUsed() 			{ return (String)data.get("serverUsed"); }	
     public String getDb()					{ return (String)data.get("db"); }
     public String getCollections()			{ return (String)data.get("collections"); }
     public String getObjects()				{ return (String)data.get("objects"); }
     public String getAvgObjSize()			{ return (String)data.get("avgObjSize"); }
     public String getDataSize()				{ return (String)data.get("dataSize"); }
     public String getStorageSize()			{ return (String)data.get("storageSize"); }
     public String getNumExtents()			{ return (String)data.get("numExtends"); }
     public String getIndexes()				{ return (String)data.get("indexes"); }
     public String getIndexSize()			{ return (String)data.get("indexSize"); }
     public String getFileSIze()				{ return (String)data.get("fileSize"); }
     public String getNsSizeMB()				{ return (String)data.get("nsSizeMB"); }
     public String getOk()					{ return (String)data.get("ok"); }
     
 	/**
 	 * creates a BasicDBObject from the data Hashtable
 	 * @return BasicDBObject
 	 */
	public BasicDBObject convertLeagueToBasicDBObject()
 	{
 		BasicDBObject obj = new BasicDBObject();
 		Enumeration<String> e = data.keys();
 		while (e.hasMoreElements())
 		{
 			String key = e.nextElement();
 			obj.put(key, String.valueOf(data.get(key)));
 		}
 		return obj;
 	}
 	
 	/**
 	 * creates a league object from a BasicDBObject
 	 * @param obj
 	 */
	public void convertBasicDBObjectToLeague(BasicDBObject obj)
 	{
 		for (String s : cols)
 		{
 			data.put(s, obj.get(s));
 		}
 	}
 	
 }
