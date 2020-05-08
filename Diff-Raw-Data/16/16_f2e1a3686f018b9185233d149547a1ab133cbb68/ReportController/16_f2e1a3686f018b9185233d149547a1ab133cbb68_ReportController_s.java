 package controllers.backend;
 
 /*
  * @Author Xenovon
  * Kelas ReportController digunakan untuk menangani request berkaitan dengan 
  * manajemen iklan dan campaign
  */
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import be.objectify.deadbolt.java.actions.Group;
 import be.objectify.deadbolt.java.actions.Restrict;
 import be.objectify.deadbolt.java.actions.SubjectPresent;
 import models.custom_helper.DateBinder;
 import models.data.Campaign;
 import models.data.User;
 import models.data.UserRole;
 import models.data.enumeration.RoleEnum;
 import models.dataWrapper.TemplateData;
 import models.dataWrapper.report.ReportData;
 import models.dataWrapper.report.SummaryData;
 import models.dataWrapper.report.UserListPage;
 import models.service.Authenticator;
 import models.service.report.ReportGenerator;
 import play.Logger;
 import play.data.DynamicForm;
 import play.data.Form;
 import play.mvc.Http;
 import play.mvc.Result;
 import play.mvc.With;
 import controllers.CompressController;
 import controllers.action.DataFiller;
 import views.html.backendView.report_view.*;
 import views.html.helper.form;
 
 public class ReportController extends CompressController {
 
	public static SimpleDateFormat format=new SimpleDateFormat("MM/dd/yyyy");
 	public static DateBinder binder=new DateBinder();
 	public static ReportGenerator report=new ReportGenerator(binder);
 	public static Authenticator auth=new Authenticator();
 	
 	@SubjectPresent
 	@With(DataFiller.class)
 	public static Result index(){
 		TemplateData data = (TemplateData) 
 				Http.Context.current().args.get("templateData");	
 		DynamicForm form=Form.form().bindFromRequest();
 		ReportData reportData=null;
 		User selectedUser=null;
 		try {
 			if(auth.getUserRole(session()).getName().equals("advertiser")){
 				selectedUser=auth.getUserLogin(session());
 			}else{
 				UserRole advertiser_role=UserRole.find.where().eq("name", RoleEnum.ADVERTISER).findUnique();
 				List<User> userList=User.find.where().eq("role", advertiser_role).findList();
 				if(userList.size()!=0){
 					selectedUser=userList.get(0);				
 				}else{
 					return ok();
 				}
 			}
 			Campaign campaign=null;
 			Calendar calendar=Calendar.getInstance();
 			Date to=calendar.getTime();
 			calendar.set(Calendar.DATE, -90);
 			Date from=calendar.getTime();
 			
 			reportData=report.getReport(selectedUser, campaign, from, to);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ok(report_index.render(data, selectedUser, reportData));
 	}
 	@SubjectPresent
 	@With(DataFiller.class)
 	public static Result indexWithForm(int user){
 		TemplateData data = (TemplateData) 
 				Http.Context.current().args.get("templateData");	
 		DynamicForm form=Form.form().bindFromRequest();
 		ReportData reportData=null;
 		User selectedUser;
 		try {
 			int idUser=user;
 			int idCampaign;
 			try {
 				idCampaign = Integer.parseInt(form.get("campaign"));
 			} catch (Exception e1) {
 				idCampaign=0;
 				e1.printStackTrace();
 			}
 			
 			Campaign campaign=null;
 			
 			if(auth.getUserRole(session()).getName().equals("advertiser")){
 				selectedUser=auth.getUserLogin(session());
 			}			
 			if(idCampaign!=0){
 				campaign=Campaign.find.byId(idCampaign);				
 			}			
 			if(idUser!=0){
 				selectedUser=User.find.byId(idUser);				
 			}else{
 				UserRole advertiser_role=UserRole.find.where().eq("name", RoleEnum.ADVERTISER).findUnique();
 				List<User> userList=User.find.where().eq("role", advertiser_role).findList();
 				if(userList.size()!=0){
 					selectedUser=userList.get(0);				
 				}else{
 					return ok();
 				}
 			}
 			Date from;
 			Date to;
 			try {
 				from = format.parse(form.get("from"));
 				to = format.parse(form.get("to"));
 			} catch (Exception e) {
 				Calendar calendar=Calendar.getInstance();
 				to=calendar.getTime();
 				calendar.set(Calendar.DATE, -90);
 				from=calendar.getTime();
 				e.printStackTrace();
 				Logger.debug("Calendar error");
 			}
 			
 			reportData=report.getReport(selectedUser, campaign, from, to);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return redirect(controllers.backend.routes.ReportController.index());
 		}
 		return ok(report_index.render(data, selectedUser, reportData));
 	}
 	
 	@Restrict({@Group("administrator"),@Group("manager")})
 	@With(DataFiller.class)
 	public static Result userPerformanceReport(int page){
 		TemplateData data = (TemplateData) 
 				Http.Context.current().args.get("templateData");	
 		UserListPage userPage=report.getReportCMO(page-1, 30);
 		
 		return ok(management_report.render(data,userPage));
 	}
 	
 	@SubjectPresent
 	@With(DataFiller.class)
 	public static Result getSummary(){
 		TemplateData data = (TemplateData) 
 				Http.Context.current().args.get("templateData");	
 		/* Custom
 		 * Hari ini
 		 * Kemarin 
 		 * Bulan ini
 		 * Bulan Lalu
 		 * semuanya
 		 */
 		//custom
 		Date from;
 		Date to;
 		User user=null;
 		if(auth.getUserRole(session()).getName().equals("advertiser")){
 			user=auth.getUserLogin(session());
 		}			
 		DynamicForm form=Form.form().bindFromRequest();
 		if(form.get("from")!=null)
 			if(form.get("to")!=null){
 				try{
 					from = format.parse(form.get("from"));
 					to = format.parse(form.get("to"));
 					flash("show","Tampilkan Semua");
 					SummaryData summaryData=report.getSummary(from, to, user);
 					summaryData.setTitle("Tanggal "+format.format(from)+" Hingga "+format.format(to));
 					List<SummaryData> returnData=new ArrayList<SummaryData>();
 					returnData.add(summaryData);
 					return ok(summary_report.render(data, returnData));
 				}catch(Exception e){
 					flash("error", "Format Tanggal salah");
 				}				
 			}
 
 
 		//untuk hari ini
 		Date today_from;
 		Date today_to;
 		Calendar calendar=Calendar.getInstance();
 		today_to=calendar.getTime();
 		calendar.set(Calendar.HOUR_OF_DAY, 0);
 		today_from=calendar.getTime();
 		SummaryData today=report.getSummary(today_from, today_to, user);
 		today.setTitle("Hari Ini");
 		Logger.debug("Hari Ini : "+format.format(today_from)+" "+format.format(today_to));
 		
 		//untuk kemarin
 		Date yasterday_from;
 		Date yasterday_to;
		Calendar calendar2=Calendar.getInstance();
		calendar2.set(Calendar.DATE, -1);
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.MINUTE, 0);
		yasterday_from=calendar2.getTime();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		yasterday_to=calendar2.getTime();
 		SummaryData yasterday=report.getSummary(yasterday_from, yasterday_to, user);
 		yasterday.setTitle("Kemarin");
 		Logger.debug("Kemarin: "+format.format(yasterday_from)+" "+format.format(yasterday_to));
 		
 		//untuk bulan ini
 		Date thisMonth_from;
 		Date thisMonth_to;
 		Calendar calendar3=Calendar.getInstance();
 		thisMonth_to=calendar3.getTime();
 		calendar3.set(Calendar.DAY_OF_MONTH, 1);
 		calendar3.set(Calendar.HOUR_OF_DAY, 0);
 		calendar3.set(Calendar.MINUTE, 0);		
 		thisMonth_from=calendar3.getTime();
 		SummaryData thisMonth=report.getSummary(thisMonth_from, thisMonth_to, user);		
 		thisMonth.setTitle("Bulan Ini");
 		Logger.debug("Bulan Ini : "+format.format(thisMonth_from)+" "+format.format(thisMonth_to));
 		
 		//untuk bulan lalu
 		Date lastMonth_from;
 		Date lastMonth_to;
 		lastMonth_from=new DateTime().minusMonths(1).withDayOfMonth(1).toDate();
 		lastMonth_to=new DateTime().minusMonths(1).withDayOfMonth(1).dayOfMonth()
 					 .withMaximumValue().toDate();
 
 		SummaryData lastMonth=report.getSummary(lastMonth_from, lastMonth_to, user);
 		lastMonth.setTitle("Bulan Lalu");		
 		Logger.debug("Bulan lalu : "+format.format(lastMonth_from)+" "+format.format(lastMonth_to));
 
 		//All time
 		Date alltime_from;
 		Date alltime_to=new Date();
 		Calendar calendar5=Calendar.getInstance();
 		calendar5.set(Calendar.YEAR, 2005);
 		alltime_from=calendar5.getTime();
 		SummaryData allTime=report.getSummary(alltime_from, alltime_to, user);
 		allTime.setTitle("Semua Waktu");
 		Logger.debug("Semua Waktu: "+format.format(alltime_from)+" "+format.format(alltime_to));
 		
 		List<SummaryData> returnData=new ArrayList<SummaryData>();
 		
 		returnData.add(allTime);
 		returnData.add(today);
 		returnData.add(yasterday);
 		returnData.add(thisMonth);
 		returnData.add(lastMonth);
 		
 		
 		return ok(summary_report.render(data, returnData));
 		
 		
 	}
 
 }
 
