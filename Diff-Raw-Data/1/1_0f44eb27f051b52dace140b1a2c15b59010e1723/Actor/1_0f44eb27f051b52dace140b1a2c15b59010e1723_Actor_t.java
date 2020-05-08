 package devxs.cyberpeople.soul;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.widget.Toast;
 
 public class Actor {
 	
 	private String url;
 	private CommandsActivity ca;
 	
 	Actor (String url, CommandsActivity ca)
 	{
 		this.url = url;
 		this.ca = ca;
 	}
 	
 	public void move (String s)
 	{
 		if (s == "forwards")
 			move(Action.FORWARDS);
 		else if (s == "backwards")
 			move(Action.BACKWARDS);
 		else if (s == "spin")
 			move(Action.TURNLEFT);
 		else if (s == "rotate")
 			move(Action.TURNRIGHT);
 		else if (s == "stop")
 			move(Action.STOP);
 	}
 	
 	public void move (Action a)
 	{
		toast("Moving in direction " + a);
 		sendRequest( doMove(a) );
 	}
 	
 	public String doMove(Action a)
 	{
 		switch (a)
 		{
 			case FORWARDS:
 				return "FORWARD";
 			case BACKWARDS:
 				return "BACK";
 			case TURNMINORLEFT:
 				return "LEFT45";
 			case TURNMINORRIGHT:
 				return "RIGHT45";
 			case TURNLEFT:
 				return "LEFT";
 			case TURNRIGHT:
 				return "RIGHT";
 			case STOP:
 			default:
 				return "STOP";
 		}
 	}
 	
 	public void toast (String m)
 	{
 		Toast.makeText(ca, m, Toast.LENGTH_SHORT).show();
 	}
 	
 	public void sendRequest(String s)
 	{
 		
 		try {
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet(url + s);
 			HttpResponse response = client.execute(request);
 			String html = "";
 			InputStream in = response.getEntity().getContent();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 			StringBuilder str = new StringBuilder();
 			String line = null;
 			while((line = reader.readLine()) != null)
 			{
 			    str.append(line);
 			}
 			in.close();
 			html = str.toString();
 
 		    // toast(html);
 		    ca.setMsg(html);
 		} catch (ClientProtocolException e) {
 			toast(e.getMessage());
 		} catch (IOException e) {
 			toast(e.getMessage());
 		}
 
 		
 	}
 	
 	public Action getDirection(String s)
 	{
 		if (s.equals("forwards"))
 			return Action.FORWARDS;
 		else if (s.equals("backwards"))
 			return Action.BACKWARDS;
 		else if (s.equals("left"))
 			return Action.TURNLEFT;
 		else if (s.equals("right"))
 			return Action.TURNRIGHT;
 		else // if (s.equals("stop"))
 			return Action.STOP;
 	}
 }
