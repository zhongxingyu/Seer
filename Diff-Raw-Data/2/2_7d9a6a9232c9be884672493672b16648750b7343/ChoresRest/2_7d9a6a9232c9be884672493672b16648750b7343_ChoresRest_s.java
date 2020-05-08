 package il.ac.huji.chores.server.parse;
 
 import il.ac.huji.chores.ApartmentChore;
 import il.ac.huji.chores.Chore;
 import il.ac.huji.chores.Chore.CHORE_STATUS;
 import il.ac.huji.chores.ChoreInfo;
 import org.apache.http.client.ClientProtocolException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class ChoresRest {
 	
 	public static List<Chore> scheduleChores(String apartment){
 		
 		List<Chore> chores = new ArrayList<Chore>();
 		ParseRestClientImpl parse = new ParseRestClientImpl();
 		List<ChoreInfo> choreInfoList=null;
 		try {
 			choreInfoList = parse.getApartmentChoreInfos(apartment);
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		Calendar cal = Calendar.getInstance();
 		Date currentDate = cal.getTime();
 		
 		for(ChoreInfo choreInfo :choreInfoList){
 			System.out.println("Scheduling chore :"+choreInfo.getName());
 			try {
				chores.add(scheduleChore(choreInfo,currentDate, apartment));
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 				System.out.println(e.getMessage());
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.out.println(e.getMessage());
 			}
 			
 		}
 		
 		return chores;
 		
 	}
 	
 	private static List<Chore> scheduleChore(ChoreInfo choreInfo, Date currentDate,String apartment) throws ClientProtocolException, IOException{
 		
 		List<Chore> chores=new ArrayList<Chore>();
 		Chore chore = new ApartmentChore();
 		chore.setChoreInfoId(choreInfo.getChoreInfoID());
 		chore.setCoinsNum(choreInfo.getCoinsNum());
 		chore.setName(choreInfo.getName());
 		chore.setStartsFrom(currentDate);
 		chore.setApartment(apartment);
 		chore.setStatus(CHORE_STATUS.STATUS_FUTURE);
 		for(int i=0 ; i<choreInfo.getHowManyInPeriod();i++){
 			Date deadline = calculateDeadline(choreInfo,currentDate,i);
 			chore.setDeadline(deadline);
 			chores.add(chore);
 		}
 		return chores;
 		
 	}
 	private static Date calculateDeadline(ChoreInfo choreInfo, Date currentDate,int offset){
 		Calendar c = Calendar.getInstance();
 		System.out.println("Current date : "+currentDate.toGMTString());
 		c.setTime(currentDate); // Now use today date.
 		int days=0;
 		switch(choreInfo.getPriod()){
 		
 		case CHORE_INFO_DAY:
 			days=1;
 			break;	
 		case CHORE_INFO_WEEK:
 			days=7;
 			break;
 		case CHORE_INFO_MONTH:
 			days = c.getActualMaximum(Calendar.DAY_OF_MONTH); 
 			break;
 		case CHORE_INFO_YEAR:
 			days=365;
 			break;		
 		}
 		days=(offset+1)*days/choreInfo.getHowManyInPeriod();
 		c.add(Calendar.DATE, days);
 		Date deadline = c.getTime();
 		deadline.setHours(23);
 		deadline.setMinutes(59);
 		deadline.setSeconds(59);
 		System.out.println("Calculated deadline : "+deadline.toGMTString());
 		return deadline;
 		
 	}
 
 
 }
