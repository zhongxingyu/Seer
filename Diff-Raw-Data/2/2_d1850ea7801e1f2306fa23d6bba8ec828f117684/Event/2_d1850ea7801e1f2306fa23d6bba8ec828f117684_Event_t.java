package some.project.com;

 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 
 public class Event 
 {
 
 	private String name;
 	private int startTime;
 	private int endTime;
 	private String description;
 	private String location;
 	private Date date;
 	private boolean academic;
 	private boolean social;
 	private boolean professional;
 	
 	public Event (String name, int startTime, int endTime, String description,
 			Date date, boolean academic, boolean social, boolean professional,
 			String location)
 	{
 		this.name = name;
 		this.startTime=startTime;
 		this.endTime=endTime;
 		this.description=description;
 		this.date=date;
 		this.academic=academic;
 		this.social=social;
 		this.professional=professional;
 		this.location = location;
 	}
 	
 	public Event (String [] eventParts)
 	{
 		this.name = eventParts[0];
 		this.startTime = Integer.parseInt(eventParts[2].replaceAll(":", ""));
 		this.endTime = Integer.parseInt(eventParts[3].replaceAll(":", ""));
 		String [] dateParts = eventParts[1].split("-");
 		this.date = new Date (Integer.parseInt(dateParts[0]), 
 				Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
 		this.description = eventParts[4];
 		this.location = eventParts[5];
 		this.academic = false;
 		if (eventParts[6].equals("1"))
 			academic = true;
 		this.social = false;
 		if (eventParts[7].equals("1"))
 			social = true;
 		this.professional = false;
 		if (eventParts[8].equals("1"))
 			professional = true;
 		
 	}
 	
 	public void setProfessional(boolean professional) {
 		this.professional = professional;
 	}
 	public boolean isProfessional() {
 		return professional;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 	
 	public void setEndTime(int endTime) {
 		this.endTime = endTime;
 	}
 	public int getEndTime() {
 		return endTime;
 	}
 	public void setDate(Date date) {
 		this.date = date;
 	}
 	public Date getDate() {
 		return date;
 	}
 	public void setAcademic(boolean academic) {
 		this.academic = academic;
 	}
 	public boolean isAcademic() {
 		return academic;
 	}
 	public void setSocial(boolean social) {
 		this.social = social;
 	}
 	public boolean isSocial() {
 		return social;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setStartTime(int startTime) {
 		this.startTime = startTime;
 	}
 	public int getStartTime() {
 		return startTime;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 	
 	
 	public static ArrayList <Event> getMatchingEvents (String category, String categoryValue)
 	{
 		ArrayList <Event> matchingEvents = new ArrayList <Event> ();
 		String result = "";
 		try
     	{
    
     		HttpClient httpClient = new DefaultHttpClient();
     		String urlValue = "http://team1.appjam.roboteater.com/selecter.php?";
     		urlValue += "Category=" + category + "&" + category + "=" + categoryValue;
     		HttpPost httpPost = 
     			new HttpPost(urlValue);
 //    		httpPost.setEntity (new UrlEncodedFormEntity(namePairs));
     		HttpResponse response = httpClient.execute(httpPost);
     		HttpEntity entity = response.getEntity();
     		InputStream is = entity.getContent();
     		BufferedReader reader = 
     			new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
             StringBuilder sb = new StringBuilder();
             String line = null;
             while ((line = reader.readLine()) != null) {
                     sb.append(line + "\n");
             }
             is.close();
      
             result=sb.toString();
     	}
     	catch (Exception e)
     	{
     		System.err.println (e.getMessage());
     	}
     	for (String eventsS: result.split("NEW_EVENT"))
     	{
     		matchingEvents.add (new Event(eventsS.split("##")));
     	}
     	return matchingEvents;		
 	}
 	
 	
 }
