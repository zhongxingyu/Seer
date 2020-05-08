 /**
  * 
  */
 package eu.play_project.dcep.distributedetalis.join;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.dcep.distributedetalis.api.HistoricalData;
 import eu.play_project.play_platformservices.api.HistoricalQuery;
 import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
 import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
 
 /**
  * @author Ningyuan Pan
  *
  */
 public class Engine implements HistoricalData {
 	
 	private final EcConnectionManager ecConnection;
 	private Logger logger;
 	
 	public Engine(EcConnectionManager ecm){
 		if(ecm == null)
 			throw new IllegalArgumentException("EcConnection should not be null");
 		ecConnection = ecm;
 		logger = LoggerFactory.getLogger(this.getClass());
 	}
 	
 	
 	@Override
 	public Map<String, List<String>> get(
 			List<HistoricalQuery> queries,
 			Map<String, List<String>> variableBindings) {
 
 		Map<String, String> q = new HashMap<String, String>();
 		Map<String, List<String>> v = new HashMap<String, List<String>>();
 		for (HistoricalQuery historicalQuery : queries) {
 			q.put(historicalQuery.getCloudId(), historicalQuery.getQuery());
 			v.put(historicalQuery.getCloudId(), historicalQuery.getVariables());
 		}
 		
 		return this.get(q, v, variableBindings);
 	}
 
 	public Map<String, List<String>> get(Map<String, String> queries, Map<String, List<String>> variableNames, Map<String, List<String>> variableBindings){
 		Map<String, List<String>> ret = new HashMap<String, List<String>>();
 		
 		// data structures used by core
 		List<ResultRegistry> rrs = new ArrayList<ResultRegistry>();
 		Map<String, SelectVariable> svs = new HashMap<String, SelectVariable>();
 		
 		// init data structures used by core
 		for(String stream : queries.keySet()){
 			String query = queries.get(stream);
 			Map<String, List<String>> vb = new HashMap<String, List<String>>();
 			
 			List<String> vars = variableNames.get(stream);
 			for(String var : vars){
 				vb.put(var, variableBindings.get(var));
 			}
 			
 			HistoricalQueryContainer hq = new HistoricalQueryContainer(query, vb);
 			
 			if(!addResultRegistry(stream, hq.getQuery(), rrs, svs, variableNames)){
 				return ret;
 			}
 			System.out.println(hq.getQuery());
 		}
 		
 		Core.make(svs, rrs);
 		transform(ret, rrs.get(0));
 		return ret;
 	}
 	
 	private boolean addResultRegistry(String stream, String hquery, List<ResultRegistry> rrs, Map<String, SelectVariable> svs, Map<String, List<String>> variableNames) {
 		
 		// connect event cloud
 		SparqlSelectResponse result;
 		try {
 			result = ecConnection.getDataFromCloud(hquery, stream);
 		} catch (EventCloudIdNotManaged e) {
 			logger.error("Unknown event cloud in historic query.", e);
 			return false;
 		} 
 		
 		ResultRegistry rr = new ResultRegistry(result.getResult());
 		if(rr.getSize() == 0)
 			return false;
 		else{
 			List<String> vars = variableNames.get(stream);
 			for(String var : vars){
 				SelectVariable sv = svs.get(var);
 				if(sv == null){
 					sv = new SelectVariable();
 					svs.put(var, sv);
 				}
 				sv.addRelResult(rr);
 			}
 			rrs.add(rr);
 			return true;
 		}
 	}
 	
 	private void transform(Map<String, List<String>> m, ResultRegistry rr){
 		List<String> vars = rr.getVariables();
 		List<List> result = rr.getResult();
 		
 		List<String> [] values = new ArrayList [vars.size()];
 		for(int i = 0; i < vars.size(); i++){
 			values[i] = new ArrayList<String>();
 			m.put(vars.get(i), values[i]);
 		}
 		
 		// every row in result
 		for(int i = 0; i < result.size(); i++){
 			List<String> r = result.get(i);
 			// every column in one row
 			for(int j = 0; j < r.size(); j++){
 				values[j].add(r.get(j));
 			}
 		}
 	}
 }
