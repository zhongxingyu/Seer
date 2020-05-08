 package pt.com.broker.monitorization.db.queries.agents;
 
 import java.sql.ResultSet;
 import java.util.List;
 import java.util.Map;
 
 import org.caudexorigo.jdbc.Db;
 import org.caudexorigo.jdbc.DbPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.monitorization.http.QueryStringParameters;
 
 public class AgentMiscInformationQuery
 {
 	private static final Logger log = LoggerFactory.getLogger(AgentMiscInformationQuery.class);
 	
	private static String QUERY = "SELECT last_event_for_subject_predicate_agent('agent', 'status', ?, now(), '00:10') AS status , last_event_for_subject_predicate_agent('tcp', 'connections', ?, now(), '00:10') AS tcp , last_event_for_subject_predicate_agent('tcp-legacy', 'connections', ?, now(), '00:10') AS tcp_legacy , last_event_for_subject_predicate_agent('ssl', 'connections', ?, now(), '00:10') AS ssl , last_event_for_subject_predicate_agent('dropbox', 'count', ?, now(), '00:10') AS dropboxcount , last_event_for_subject_predicate_agent('faults', 'rate', ?, now(), '00:10') AS faulrate , last_event_for_subject_predicate_agent('system-message', 'failed-delivery', ?, now(), '00:10') AS pending_sys_msg;";
 
 	public String getId()
 	{
 		return "agentMiscInfo";
 	}
 
 	public String getJsonData(Map<String, List<String>> params)
 	{
 		Db db = null;
 
 		StringBuilder sb = new StringBuilder();
 
 		try
 		{
 			db = DbPool.obtain();
 
 			ResultSet queryResult = getResultSet(db, params);
 			if (queryResult == null)
 				return "";
 
 			boolean first = true;
 
 			while (queryResult.next())
 			{
 				if (first)
 				{
 					first = false;
 				}
 				else
 				{
 					sb.append(",");
 				}
 				int idx = 1;
 	
 				sb.append("{\"status\":\"");
 				sb.append( (queryResult.getDouble(idx++) == 1) ? "Ok" : "Down" );
 				sb.append("\",");
 
 				sb.append("\"tcpConnections\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"tcpLegacyConnections\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"ssl\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 
 				sb.append("\"dropboxCount\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 
 				sb.append("\"faultRate\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"pendingAckSystemMsg\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\"");
 				
 				sb.append("}");
 			}
 		}
 		catch (Throwable t)
 		{
 			log.error("Failed to get all queue genral info", t);
 		}
 		finally
 		{
 			DbPool.release(db);
 		}
 
 		return sb.toString();
 	}
 
 	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
 	{
 		String agentName = QueryStringParameters.getAgentNameParam(params);
 
 		if(agentName == null)
 		{
 			return null;
 		}
 		return db.runRetrievalPreparedStatement(QUERY, agentName, agentName, agentName, agentName, agentName, agentName, agentName);
 	}
 }
