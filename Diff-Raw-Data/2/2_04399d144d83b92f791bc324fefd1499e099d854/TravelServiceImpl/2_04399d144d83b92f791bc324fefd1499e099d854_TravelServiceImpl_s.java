 package croo.szakdolgozat.server;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpSession;
 
 import us.monoid.json.JSONException;
 import us.monoid.json.JSONObject;
 import us.monoid.web.Resty;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import croo.szakdolgozat.client.stubs.TravelService;
 import croo.szakdolgozat.shared.TravelInfos;
 
 @SuppressWarnings("serial")
 public class TravelServiceImpl extends RemoteServiceServlet implements TravelService
 {
 
	public TravelInfos getTravelInfos() throws Throwable
 	{
 		TravelInfos infos = ElviraApi.getTravelInfosFromJson(session(), getElviraJson());
 		return infos;
 	}
 
 	private JSONObject getElviraJson() throws JSONException, IOException
 	{
 		JSONObject json = new Resty().json(ElviraApi.buildQueryURL(session())).object();
 		return json;
 	}
 
 	private HttpSession session()
 	{
 		return this.getThreadLocalRequest().getSession();
 	}
 }
