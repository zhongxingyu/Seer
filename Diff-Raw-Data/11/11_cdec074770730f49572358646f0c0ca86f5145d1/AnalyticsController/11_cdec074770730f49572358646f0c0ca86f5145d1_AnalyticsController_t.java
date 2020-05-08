 package net.iicaptain.homeport.web;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.DatagramSocketImpl;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobExecution;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersBuilder;
 import org.springframework.batch.core.launch.JobLauncher;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.Message;
 import org.springframework.integration.MessageChannel;
 import org.springframework.integration.support.MessageBuilder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.gemstone.gemfire.cache.Cache;
 import com.gemstone.gemfire.cache.Region;
 import com.gemstone.gemfire.cache.query.FunctionDomainException;
 import com.gemstone.gemfire.cache.query.NameResolutionException;
 import com.gemstone.gemfire.cache.query.Query;
 import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
 import com.gemstone.gemfire.cache.query.QueryService;
 import com.gemstone.gemfire.cache.query.SelectResults;
 import com.gemstone.gemfire.cache.query.TypeMismatchException;
 
 import java.util.Iterator;
 
 @Controller
 public class AnalyticsController {
 
 	@Autowired 
 	FileSystem fs;
 
 	@Autowired
 	Cache cache;
 	
 	@Autowired
 	JobLauncher jl;
 	
 	@Autowired
 	Job job;
 	
 	@RequestMapping(value = { "/start" }, method = RequestMethod.GET)
	public String pivotal(@RequestParam(value="app", defaultValue="www.springsource.org") String app, Model model, HttpSession session) {
 		model.addAttribute("app", app);
		session.setAttribute("app", app);
 		return "pivotal";
 	}
 	
 	@RequestMapping(value = { "/startjob" }, method = RequestMethod.GET)
	public String triggerbatch(Model model, HttpSession session) {
 
 		try {
 			
 			JobParameters jobParameters = new JobParametersBuilder().addString(
 					            "input.id", ""+System.currentTimeMillis()).toJobParameters();
 			JobExecution je= jl.run(job, jobParameters);
 			je.getExitStatus().addExitDescription("MyExitDescription");
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
		model.addAttribute("app", session.getAttribute("app"));
 		return "pivotal";
 	}
 	
 
 	@Autowired @Qualifier("locationsToGem")
 	MessageChannel locationsChannel;
 	
 	@Autowired @Qualifier("worldsToGem")
 	MessageChannel worldsChannel;
 	
 	@RequestMapping(value = { "/location" }, method = RequestMethod.GET)
 	public @ResponseBody String location(@RequestParam("longitude") String longitude,
 			@RequestParam("latitude") String latitude,
 			@RequestParam(value="altitude", required=false) String altitude,
 			@RequestParam(value="accuracy", required=false) String accuracy,
 			@RequestParam(value="altitudeAccuracy", required=false) String altitudeAccuracy,
 			@RequestParam(value="heading", required=false) String heading,
 			@RequestParam(value="speed", required=false) String speed,
 			@RequestParam(value="timestamp", required=false) String timestamp,
 			HttpServletRequest request, HttpServletResponse response) {
 		
 		String key= request.getRemoteAddr()+":"+System.currentTimeMillis();
 		String user= "anonymous";
 		String val= null;
 		try {
 			user= request.getUserPrincipal().getName();
 		}
 		catch(NullPointerException e) {
 			
 		}
 		val= user+","+request.getRemoteAddr()+","+latitude+","+longitude+","+altitude+","+accuracy+","+altitudeAccuracy+","+heading+","+speed+","+timestamp+","+System.currentTimeMillis();
 		HashMap<String, String> map= new HashMap<String, String>();
 		map.put(key, val);
 	    locationsChannel.send(MessageBuilder.withPayload(map).build());
 		return null;
 	}
 	
 	@RequestMapping(value = { "/analytics/realtime" }, method = RequestMethod.GET)
 	public String realtime(Model model) throws FunctionDomainException, TypeMismatchException, NameResolutionException, QueryInvocationTargetException {
 		HashMap<Location, Integer> locations= new HashMap<Location, Integer>();
 		// Identify your query string.
 		 String queryString = "SELECT * FROM /iicaptainLocations";
 		 
 		 // Get QueryService from Cache.
 		 QueryService queryService = cache.getQueryService();
 		 
 		 // Create the Query Object.
 		 Query query = queryService.newQuery(queryString);
 		 
 		 // Execute Query locally. Returns results set.
 		 SelectResults results = (SelectResults)query.execute();
 		
 		 long total= 0;
 		 int locs= 0;
 		for (Object object : results) {
 			String line= (String)object;
 			
 			StringTokenizer tk = new StringTokenizer(line, ",");
 			Location loc= new Location();
 			try {
 				tk.nextToken();
 				tk.nextToken();
 				Double d = Double.parseDouble(tk.nextToken());
 				d *= 100;
 				long l = Math.round(d);
 				loc.longitude="" + l / 100.0;
 				d = Double.parseDouble(tk.nextToken());
 				d *= 100;
 				l = Math.round(d);
 				loc.latitude="" + l / 100.0;
 				
 				if(locations.containsKey(loc)) {
 					Integer n= locations.get(loc);
 					loc.n= n+1;
 					locations.remove(loc);
 					locations.put(loc, loc.n);
 				}
 				else {
 					locs++;
 					loc.n= 1;
 					locations.put(loc, 1);
 				}
 				total++;
 			}
 			catch (Exception e) {
 				continue;
 			}
 			
 		}
 		StringBuffer ret= new StringBuffer("{ ;total;:"+total+", ;locations;: [");
 		Set<Location> keys= locations.keySet();
 		int n= 0;
 		for (Location l : keys) {
 			ret.append(l.toString());
 			if(n< locs-1)
 				ret.append(", ");
 			n++;
 		}
 		ret.append("] }");
 		
 		model.addAttribute("locations", ret);
 		
 		return "map";
 	}
 	
 	@RequestMapping(value = { "/analytics/lasthour" }, method = RequestMethod.GET)
 	public String lasthour(Model model) {
 		
 		Path filenamePath = new Path(
 				"/user/vfabric/locations-lastHour/part-r-00000");
 		String ret= getLocations(filenamePath);
 	
 		
 		model.addAttribute("locations", ret);	
 		
 		return "map";
 	}
 
 	@RequestMapping(value = { "/analytics/lastweek" }, method = RequestMethod.GET)
 	public String lastweek(Model model) {
 		
 		Path filenamePath = new Path(
 				"/user/vfabric/locations-lastWeek/part-r-00000");
 		String ret= getLocations(filenamePath);
 	
 		
 		model.addAttribute("locations", ret);	
 		
 		return "map";
 	}
 	
 	@RequestMapping(value = { "/analytics/lastday" }, method = RequestMethod.GET)
 	public String lastday(Model model) {
 		
 		Path filenamePath = new Path(
 				"/user/vfabric/locations-lastDay/part-r-00000");
 		String ret= getLocations(filenamePath);
 	
 		
 		model.addAttribute("locations", ret);	
 		
 		return "map";
 	}
 	
 	@RequestMapping(value = { "/analytics/all" }, method = RequestMethod.GET)
 	public String all(Model model) {
 		
 		Path filenamePath = new Path(
 				"/user/vfabric/locations-all/part-r-00000");
 		String ret= getLocations(filenamePath);
 	
 		
 		model.addAttribute("locations", ret);	
 		
 		return "map";
 	}
 	
 	private String getLocations(Path filenamePath) {
 		Map<Integer, Location> map = new HashMap<Integer, Location>();
 		long total = 0;
 		int locs = 0;
 		try {
 			FSDataInputStream in = fs.open(filenamePath);
 			BufferedReader buf = new BufferedReader(new InputStreamReader(in));
 			StringTokenizer stk;
 			String line= null;
 			do {
 				line = buf.readLine();
 				if (line != null) {
 					stk = new StringTokenizer(line, "/");
 					Location loc = new Location();
 					loc.latitude = stk.nextToken().trim();
 					loc.longitude = stk.nextToken().trim();					
 					loc.n = Integer.parseInt(stk.nextToken().trim());
 					total += loc.n;
 					map.put(locs, loc);
 					locs++;
 				}
 			} while (line != null);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		StringBuffer ret= new StringBuffer("{ ;total;:"+total+", ;locations;: [");
 		Set<Integer> keys= map.keySet();
 		int n= 0;
 		for (Integer key : keys) {
 			ret.append(map.get(key).toString());
 			if(n< locs-1)
 				ret.append(", ");
 			n++;
 		}
 		
 		ret.append("] }");
 		return ret.toString();
 	}
 }
