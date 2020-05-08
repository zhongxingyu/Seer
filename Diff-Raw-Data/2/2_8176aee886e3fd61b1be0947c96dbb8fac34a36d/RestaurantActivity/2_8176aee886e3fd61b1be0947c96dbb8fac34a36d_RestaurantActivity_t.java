 package fi.aalto.lounaspaikka;
 
 
 //import java.util.Calendar;
 
 import fi.aalto.lounaspaikka.objectfiles.ObjectsContainer;
 import fi.aalto.lounaspaikka.objectfiles.Restaurant;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 public class RestaurantActivity extends Activity{
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.restaurant);
 		ImageView restIcon = (ImageView) findViewById(R.id.restIcon);
 		TextView restInfo = (TextView) findViewById(R.id.restInfo);
 		Intent myIntent = getIntent();
 		String restName = myIntent.getStringExtra("restName");
 		//TO BE CHANGED TO SPECIFIC ICON
 		restIcon.setImageResource(R.drawable.respic);
 		Restaurant nowRest = new Restaurant();
 		int rnumber=0;
 		for (int i=0;i<ObjectsContainer.restaurants.size();i++)
 			if (ObjectsContainer.restaurants.get(i).name.equals(restName)){
 				nowRest = ObjectsContainer.restaurants.get(i);
 				rnumber=i;
 				break;
 			}
 
 		if (nowRest.name!=null){
 			restInfo.append("Name: "+nowRest.name+"\n");
 			restInfo.append("Campus: "+nowRest.campus+"\n");
 			restInfo.append("Address: "+nowRest.location.address+"\n");
 			restInfo.append("Opening hours: \n");
 			int opensc=0;
 			int openlistsize=nowRest.isopen.listOfDays.size();
 			while (openlistsize>opensc) {
 				restInfo.append(opens(opensc,rnumber));
 				opensc++;
 			}
 
 			/*	if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Mon: no data\n");
 			else
 				restInfo.append("Mon: "+nowRest.isopen.listOfDays.get(0).opens+" - "+nowRest.isopen.listOfDays.get(0).closes+"\n");
 			if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Tue: no data\n");
 			else
 				restInfo.append("Tue: "+nowRest.isopen.listOfDays.get(1).opens+" - "+nowRest.isopen.listOfDays.get(1).closes+"\n");
 			if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Wed: no data\n");
 			else
 				restInfo.append("Wed: "+nowRest.isopen.listOfDays.get(2).opens+" - "+nowRest.isopen.listOfDays.get(2).closes+"\n");
 			if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Thu: no data\n");
 			else
 				restInfo.append("Thu: "+nowRest.isopen.listOfDays.get(3).opens+" - "+nowRest.isopen.listOfDays.get(3).closes+"\n");
 			if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Fri: no data\n");
 			else
 				restInfo.append("Fri: "+nowRest.isopen.listOfDays.get(4).opens+" - "+nowRest.isopen.listOfDays.get(4).closes+"\n");
 			if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Sat: no data\n");
 			else
 				restInfo.append("Sat: "+nowRest.isopen.listOfDays.get(5).opens+" - "+nowRest.isopen.listOfDays.get(5).closes+"\n");
 			if (nowRest.isopen.listOfDays.get(0).opens.equals(""))
 				restInfo.append("Sun: no data\n");
 			else
 				restInfo.append("Sun: "+nowRest.isopen.listOfDays.get(6).opens+" - "+nowRest.isopen.listOfDays.get(6).closes+"\n");
 		}*/
 
 		}
 	}
 
 	private String opens(int day, int rnumber) //rnumber is restaurant number
 	{
 		String opens="";
 		String closes="";
 		String openclose="";
 		if (ObjectsContainer.restaurants.get(rnumber).isopen.listOfDays.get(day).opens.equals("") || ObjectsContainer.restaurants.get(rnumber).isopen.listOfDays.get(day).closes.equals("")  ) {
 			openclose= "Hours can't be retrived" + System.getProperty("line.separator");
 		} 
 		else 
 		{
 			opens=ObjectsContainer.restaurants.get(rnumber).isopen.listOfDays.get(day).opens;
			closes= ObjectsContainer.restaurants.get(rnumber).isopen.listOfDays.get(day).closes;
 			openclose = opens + "-" + closes;
 		}
 		if (day==0) {
 			openclose= "Monday: " +openclose+System.getProperty("line.separator"); 	
 		}
 		 else if (day==1) {		
 			 openclose= "Tuesday: " +openclose+System.getProperty("line.separator"); 	
 		 }
 		else if (day==2) {
 			openclose= "Wednesday: " +openclose+System.getProperty("line.separator"); 		
 		}
 		else if (day==3) {
 			openclose= "Thursday: " +openclose+System.getProperty("line.separator"); 	
 		}
 		else if (day==4) {
 			openclose= "Friday: " +openclose+System.getProperty("line.separator"); 	
 		}
 		else if (day==5) {
 			openclose= "Saturday: " +openclose+System.getProperty("line.separator"); 		
 		}
 		else if (day==6) {
 			openclose= "Sunday: " +openclose+System.getProperty("line.separator"); 	
 		}
 		return openclose;
 	}
 
 
 }
