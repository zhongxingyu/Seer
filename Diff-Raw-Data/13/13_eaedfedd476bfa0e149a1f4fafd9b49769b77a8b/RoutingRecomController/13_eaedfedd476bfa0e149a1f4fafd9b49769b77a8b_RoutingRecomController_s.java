 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.internetrt.sdk.InternetRT;
 import org.internetrt.sdk.util.DescribedListenerConfig;
 import org.internetrt.sdk.util.FreeRoutingGenerator;
 import org.internetrt.sdk.util.Signal;
 
 import cn.edu.act.internetos.appmarket.service.RoutingRecommender;
 
 import config.properties;
 
 
 import play.mvc.Before;
 import play.mvc.Controller;
 import models.RoutingChoice;
 
 public class RoutingRecomController extends Controller{
 	
 	
 		public static String getAccessToken() {
 			System.out.println("get token!!!!!!!!!!!!!!!!!");
 			return session.get("token");
 		}
 		
 		   @Before(only = { "index","ConfirmRecomRouting" })
 		public static void checkUser() {
 			System.out.println("checkUser");
 			String token = getAccessToken();
 			if (token == null) {
 				System.out.println(properties.irt.getAuthCodeUrl());
 				Controller.redirect(properties.irt.getAuthCodeUrl());
 				System.out.println("checkUser!!!!");
 			}
 		}
 	   
 		public static void index(String fromAppIDString){
 			String accessToken = getAccessToken();
			
 			RoutingRecommender routingRecommender = new RoutingRecommender();
 			List<scala.Tuple3<String,Signal,DescribedListenerConfig>> result = routingRecommender.getPossibleRoutings(fromAppIDString, accessToken);
 			List<RoutingChoice> choices = generateChoieces(result);
 			System.out.println(choices.size());
 			if(choices.size()> 0)
				render("Routing/recomRouting.html",choices);
 			else
				render("Routing/success.html");
 		}
 		
 		private static List<RoutingChoice> generateChoieces(List<scala.Tuple3<String,Signal,DescribedListenerConfig>> possibleRoutings){
 			List<RoutingChoice> res = new ArrayList<RoutingChoice>();
 			for(scala.Tuple3<String,Signal,DescribedListenerConfig> data:possibleRoutings){
 				String routing = FreeRoutingGenerator.generateRouting(data._2().name(),data._2().from(),data._3());
 				String signalName = data._2().name();
 				String signaldes = data._2().description();
 				String signalApp = data._1();
 				String listenerApp = data._3().appName();
 				String listenerDes = data._3().description();
 				res.add(new RoutingChoice(signalName,signalApp,signaldes,listenerApp,listenerDes,routing));
 			}
 			return dedup(res);
 		}
 		
 		private static List<RoutingChoice> dedup(List<RoutingChoice> origin){
 			Set<RoutingChoice> set = new HashSet<RoutingChoice>(origin);
 			return new ArrayList<RoutingChoice>(set);
 		}
 
 		public static void ConfirmRecomRouting(){
 			String[] routings = request.params.getAll("choices");
 			InternetRT rt = config.properties.irt;
 			
 			String accessToken = getAccessToken();
 			
 			for(String routing:routings){
 				System.out.println(routing);
 				
 				rt.ConfirmRouting(accessToken,routing);
 			}
			render("Routing/success.html");
 			/*PrintWriter out 
 			 * = response.getWriter();
 			out.flush();
 			out.close();*/
 		}
 }
