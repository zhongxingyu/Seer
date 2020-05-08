 package net.unikernel.npss.core;
 
 import net.unikernel.npss.model.PMP;
 import com.mongodb.MongoException;
import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeSet;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import org.json.simple.JSONValue;
 
 /**
  *
  * @author uko
  */
 @Stateless
 @Path("/crushroom")
 public class Worker
 {
 	private String data;
 	@EJB
 	private PMP mongodbAnt;
 	
 	public Worker()
 	{
 		data="";
 //		String user = "crawler";
 //		char[] pass = "J7vVBYCiGTjcnhN6Qe".toCharArray();
 //		try
 //		{
 //			Mongo m = new Mongo("localhost");
 //			DB db = m.getDB("npss");
 //			boolean auth = db.authenticate(user, pass);
 //			if (auth)
 //			{
 //				data+="<br />good";
 //			}
 //			else
 //			{
 //				data+="<br />bad";
 //			}
 //			DBCollection coll = db.getCollection("testCollection");
 //			Set<String> colls = db.getCollectionNames();
 //			for (String s : colls)
 //			{
 //				data+="<br />"+s;
 //			}
 //			BasicDBObject doc = new BasicDBObject();
 //
 //			doc.put("name", "MongoDB");
 //			doc.put("type", "database");
 //			doc.put("count", 1);
 //
 //			BasicDBObject info = new BasicDBObject();
 //
 //			info.put("x", 203);
 //			info.put("y", 102);
 //
 //			doc.put("info", info);
 //
 //			coll.insert(doc);
 //			for (int i = 0; i < 100; i++)
 //			{
 //				coll.insert(new BasicDBObject().append("i", i));
 //			}
 //			data+="<br />"+coll.getCount();
 //			DBCursor cur = coll.find();
 //
 //        while(cur.hasNext()) {
 //			DBObject one=cur.next();
 //			data+="<br />"+one;
 //        }
 //
 //		}
 //		catch (UnknownHostException ex)
 //		{
 //			System.out.print("UnknownHOST");
 //		}
 //		catch (MongoException ex)
 //		{
 //			System.out.print("MongoError");
 //		}
 //		catch(Exception e)
 //		{
 //			data += "<br/>Exception was handled: " + e.toString();
 //			if(mongodbAnt == null)
 //				data += "; mongodbAnt is null";
 //			StackTraceElement[] trace = e.getStackTrace();
 //			for (int i = trace.length - 1; i > -1; i--)
 //			{
 //				data += "<br/>" + trace[i].toString();
 //			}
 //		}
 	}
 	
 	@GET
 	@Produces("text/html")
 	public String testPMP()
 	{
 		String result = "";
 		try
 		{
 			Map<String, String> testMap = new HashMap<String, String>();
 			testMap.put("x", "bbb");
 			testMap.put("y", "ccc");
 			testMap.put("z", "ddd");
 			String taskName = "firstTestTask";
 			String factoryName = "firstTestFactory";
 
 			result += "<br/>mongodbAnt: setting data...";
 			if (mongodbAnt.create(taskName, factoryName, testMap, "aaaaa"))
 				result += "<br/>mongodbAnt: data was set";
 			else
 				result += "<br/>mongodbAnt: data was NOT set, there is alredy some value under this key";
 			result += "<br/>mongodbAnt: getting data...";
 			String value = mongodbAnt.read(taskName, factoryName, testMap);
 			result += "<br/>mongodbAnt: data was extracted from MongoDB";
 			result += "<br/>Task: " + taskName;
 			result += "<br/>Factory: " + factoryName;
 			result += "<br/>Parameters: x => " + testMap.get("x");
 			result += " y => " + testMap.get("y");
 			result += " z => " + testMap.get("z");
 			result += "<br/>JSON: " + JSONValue.toJSONString(testMap);
 			result += "<br/>Value: " + value;
 			result += "<br/><br/>Structure";
			DecimalFormat format = new DecimalFormat("#0.00");
 			for (Map.Entry<String,TreeSet<String>> i: mongodbAnt.getStructure().entrySet())
 			{
				result += "<br/>__"+i.getKey();
 				for(String j : i.getValue())
 				{
					result += "<br/>____"+ j + " " + format.format(mongodbAnt.getSize(i.getKey(), j, PMP.SizeType.DATA_SIZE, PMP.SizeUnit.KILOBYTE))+" KB";
 				}
 			}
 		}
 		catch (MongoException e)
 		{
 			result += "<br/>Exception#" + e.getCode() + ": " + e.toString();
 			e.printStackTrace();
 		}
 		catch(Exception e)
 		{
 			result += "<br/>Exception was handled: " + e.toString();
 			if(mongodbAnt == null)
 				result += "; mongodbAnt is null";
 			StackTraceElement[] trace = e.getStackTrace();
 			for (int i = 0; i < trace.length; i++)
 			{
 				result += "<br/>" + trace[i].toString();
 			}
 			e.printStackTrace();
 		}
 		return result;
 	}
 	/**
 	 * @return the name
 	 */
 	public String getData()
 	{
 		return data;
 	}
 	/**
 	 * @param name the name to set
 	 */ 
 //	 public void setData(String data)
 //	{
 //		this.data = data;
 //	}
 }
