 package gov.nih.nci.rembrandt.web.inbox;
 
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.rembrandt.cache.BusinessTierCache;
 import gov.nih.nci.rembrandt.cache.PresentationTierCache;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.http.HttpSession;
 
 import uk.ltd.getahead.dwr.ExecutionContext;
 
 public class QueryInbox {
 	
 	private HttpSession session;
 	private BusinessTierCache btc;
 	private PresentationTierCache ptc;
 	
 	public QueryInbox()	{
 		//get some common stuff
 		session = ExecutionContext.get().getSession(false);
 		btc = ApplicationFactory.getBusinessTierCache();
 		ptc = ApplicationFactory.getPresentationTierCache();
 	}
 	
 	public QueryInbox(HttpSession session)	{
 		this.session = session;
 		btc = ApplicationFactory.getBusinessTierCache();
 		ptc = ApplicationFactory.getPresentationTierCache();
 	}
 	
 	
 	public String checkSingle(String sid, String tid)	{
 		//check the status of a single task
 		String currentStatus = "";
 		
 		Finding f = (Finding) btc.getObjectFromSessionCache(sid, tid);
 		
 		switch(f.getStatus())	{
 			case Completed:
 				currentStatus = "completed";
 			break;
 			case Running:
 				currentStatus = "running";
 			break;
 			case Error:
 				currentStatus = "error";
 			break;
 			default:
 				currentStatus = "running";
 			break;
 		}
 		
 		return currentStatus;
 	}
 	
 	public Map checkAllStatus(String sid)	{
 		Map currentStatuses = new HashMap();
 		
 		Collection<Finding> findings = btc.getAllSessionFindings(sid);
 		for(Finding f: findings){
 			String tmp = new String();
 			tmp = this.checkSingle(sid, f.getTaskId());
 			
 			Map fdata = new HashMap();
 			fdata.put("time", String.valueOf(f.getElapsedTime()));
 			fdata.put("status", tmp);
 			currentStatuses.put(f.getTaskId(), fdata);
 		}
 		
 		return currentStatuses;
 	}
 	
 		
 	public String checkStatus()	{
 		//simulate that the query is still running, assuming we have only 1 query for testing
 
 		Random r = new Random();
 		int randInt = Math.abs(r.nextInt()) % 11;
 		if(randInt % 2 == 0)
 			return "false";
 		else
 			return "true";
 	}
 	
 	public String deleteFinding(String key)	{
 		String success = "fail";
 		try	{
 		
 			success = "pass";
 		}
 		catch(Exception e){}
 		return success;
 	}
 
 	public Map mapTest(String testKey)	{
 		Map myMap = new HashMap();
 		myMap.put("firstKey", testKey);
 		myMap.put("secondKey", testKey+"_1");
 		return myMap;
 	}
 	
 	public String getQueryName()	{
 		String st = "nothing";
 		
 		try	{
 			st = String.valueOf(ptc.getSessionQueryBag(session.getId()).getQueries().size());
 		}
 		catch(Exception e){
 			st = "no worky";
 		}
 		
 		return st;
 		
 		
 	}
 }
