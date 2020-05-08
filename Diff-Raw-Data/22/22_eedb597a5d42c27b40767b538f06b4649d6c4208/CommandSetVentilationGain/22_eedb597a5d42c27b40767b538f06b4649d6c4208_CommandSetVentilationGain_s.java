 package dk.itu.serverside;
 
 import javax.servlet.http.HttpServletRequest;
 
 import dk.itu.restconnection.Measurement;
 import dk.itu.restconnection.RestClient;
 
 public class CommandSetVentilationGain extends Command {
 
 	protected CommandSetVentilationGain(String next) {
 		super(next);
 	}
 
 	@Override
 	public String execute(HttpServletRequest request) throws Exception {
 		RestClient client = new RestClient();
		String setGainStatus = client.setGain("room-0-ac-0-gain", "0.8");
 		request.setAttribute("SetGainStatus", setGainStatus);
 		return next;
 	}
 
 }
