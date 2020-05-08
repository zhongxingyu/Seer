 package pt.com.broker.monitorization.db.queries.agents;
 
 import java.sql.ResultSet;
 import java.util.List;
 import java.util.Map;
 
 import org.caudexorigo.jdbc.Db;
 import org.caudexorigo.jdbc.DbPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.monitorization.AgentHostname;
 
 public class AllAgentsGeneralInfoQuery
 {
 	private static final Logger log = LoggerFactory.getLogger(AllAgentsGeneralInfoQuery.class);
 	
 	private static String QUERY = "SELECT agents.agent_name , last_event_for_subject_predicate_agent('agent', 'status', agents.agent_name, now(), '00:10') AS status , last_event_input_message_for_agent(agents.agent_name, now(), '00:10') AS input , last_event_ouput_message_for_agent(agents.agent_name, now(), '00:10') AS output , last_event_for_subject_predicate_agent('faults', 'rate', agents.agent_name, now(), '00:10') AS faulTrate , last_event_for_subject_predicate_agent('system-message', 'failed-delivery', agents.agent_name, now(), '00:10') AS pending_sys_msg , last_event_for_subject_predicate_agent('dropbox', 'count', agents.agent_name, now(), '00:10') AS dropboxcount FROM (SELECT DISTINCT agent_name FROM raw_data WHERE event_time > (now() - '00:10'::time) ) AS agents ORDER BY 3 DESC";
 
 	public String getId()
 	{
 		return "allAgentGeneralInfo";
 	}
 
 	public String getJsonData(Map<String, List<String>> params)
 	{
 		Db db = null;
 
 		StringBuilder sb = new StringBuilder();
 
 		try
 		{
 			db = DbPool.pick();
 
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
 				sb.append("{");
 				sb.append("\"agentName\":\"");
 				String agentName = queryResult.getString(idx++);
 				sb.append(agentName);
 				
 				sb.append("\",");
 				sb.append("\"agentHostname\":\"");
 				sb.append(AgentHostname.get(agentName));
 				sb.append("\",");
 
 				sb.append("\"status\":\"");
 				sb.append( (queryResult.getDouble(idx++) == 1) ? "Ok" : "Down" );
 				sb.append("\",");
 
 				sb.append("\"inputRate\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"outputRate\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"faultRate\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"pendingAckSystemMsg\":\"");
 				sb.append(queryResult.getDouble(idx++));
 				sb.append("\",");
 				
 				sb.append("\"dropboxCount\":\"");
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
 		return db.runRetrievalPreparedStatement(QUERY);
 	}
 }
