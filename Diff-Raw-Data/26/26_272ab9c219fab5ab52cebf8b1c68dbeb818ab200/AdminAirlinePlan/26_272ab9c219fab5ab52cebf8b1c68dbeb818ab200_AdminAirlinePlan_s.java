 package controllers;
 
 import models.AirCompany;
 import models.AirlinePlan;
 import models.Airport;
 import models.Page;
 import play.Logger;
 import play.data.validation.Required;
 import play.data.validation.Validation;
 import play.mvc.Controller;
 
 import java.lang.reflect.Type;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.jdt.internal.codeassist.ThrownExceptionFinder;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 //import fj.data.List;
 /**
  * Created with IntelliJ IDEA. User: reyoung Date: 3/17/13 Time: 3:07 PM To
  * change this template use File | Settings | File Templates.
  */
 public class AdminAirlinePlan extends Controller {
 	public static void create() {
 		beforeRenderCreate();
 		render();
 	}
 
 	public static boolean beforeRenderCreate() {
 		renderArgs.put("airports", Airport.findAll());
 		renderArgs.put("companies", AirCompany.findAll());
 		return true;
 	}
 
 	public static void handleCreate(@Required String Number,
 			@Required String LeaveTime, @Required Integer FlyTime,
 			@Required Long LeavePlace, @Required Long ArrivePlace,
			@Required Long Company, String Stopovers, String Repeat) {
 		Logger.debug(
 				"validation: Number = %s\nLeaveTime = %s\nFlyTime = %d\nLeavePlace = %d\nArrivePlace = %d\nAirCompany = %d",
 				Number, LeaveTime, FlyTime, LeavePlace, ArrivePlace, Company);
 		if (Validation.hasErrors()) {
 			Logger.debug("Validation Errors.");
 			badRequest();
 		}
		try {
 			AirlinePlan alp = new AirlinePlan();
 			alp.Number = Number;
 			alp.FlyTime = FlyTime;
 			SimpleDateFormat leaveTime = new SimpleDateFormat(
 					"yyyy/MM/dd HH:mm:ss");
 			alp.LeaveTime = leaveTime.parse(LeaveTime);
 			alp.LeavePlace = Airport.findById(LeavePlace);
 			alp.ArrivePlace = Airport.findById(ArrivePlace);
 			alp.Company = models.AirCompany.findById(Company);
 			alp.StopoverPlaces = new ArrayList<Airport>();
 			Gson gson = new Gson();
 			Type listType = new TypeToken<List<Long>>() {
 			}.getType();
 			List<Long> listId = gson.fromJson(Stopovers, listType);
 			for (Long id : listId) {
 				Airport ap = Airport.findById(id);
 				alp.StopoverPlaces.add(ap);
 				Logger.debug("StopoverPlace =  %s %d %d ", ap.Name,
 						ap.CountryId, ap.CityId);
 			}
 			alp.Repeat = Repeat;
 			Logger.debug("Repeat = %s ", alp.Repeat);
 			boolean ok = alp.create();//validateAndCreate false
 			Logger.debug(" ok = %s ", ok);
 			if (!ok) {
 				badRequest();
 			}
		} catch (Throwable ex) {
			badRequest();
		}
 		list(null, null);
 	}
 
 	public static void debug() {
 		List<Airport> aps = Airport.all().fetch(2);
 		AirlinePlan alp = new AirlinePlan();
 		alp.FlyTime = 60;
 		alp.Repeat = "N";
 
 		alp.LeaveTime = new Date();
 		alp.LeavePlace = aps.get(0);
 		alp.ArrivePlace = aps.get(1);
 		alp.Number = "CZ300212";
 		alp.save();
 	}
 
 	public static void delete(Long id) {
 		int rows = 0;
 		try {
 			rows = AirlinePlan.delete("Id", id);
 		} catch (Throwable ex) {
 			renderJSON(false);
 		}
 		renderJSON(rows);
 	}
 
 	public static void list(Integer page, Integer pageSize) {
 		if (page == null || page < 1) {
 			page = 1;
 		}
 		if (pageSize == null || pageSize < 1) {
 			pageSize = 10;
 		}
 		List<AirlinePlan> airlinePlans = AirlinePlan.all()
 				.fetch(page, pageSize);
 		Page<AirlinePlan> pages = new Page<AirlinePlan>(airlinePlans, page,
 				pageSize, AirlinePlan.count());
 		render(pages);
 	}
 
 	public static void edit(Long id) {
 		try {
 			AirlinePlan alp = AirlinePlan.findById(id);
 			renderArgs.put("model", alp);
 			beforeRenderCreate();
 		} catch (Throwable e) {
 			badRequest();
 		}
 		render("AdminAirlinePlan/create.html");
 	}
 
 	public static void handleEdit(@Required String Number,
 			@Required String LeaveTime, @Required Integer FlyTime,
 			@Required Long LeavePlace, @Required Long ArrivePlace,
 			@Required Long Company, String Stopovers,@Required String Repeat,
 			@Required Long editId) {
 		if (Validation.hasErrors()) {
 			badRequest();
 		}
 		try {
 			AirlinePlan alp = AirlinePlan.findById(editId);
 			SimpleDateFormat leaveTime = new SimpleDateFormat(
 					"yyyy/MM/dd HH:mm:ss");
 			alp.Number = Number;
 			alp.LeaveTime = leaveTime.parse(LeaveTime);
 			alp.FlyTime = FlyTime;
 			alp.LeavePlace = Airport.findById(LeavePlace);
 			alp.ArrivePlace = Airport.findById(ArrivePlace);
 			AirCompany com = models.AirCompany.findById(Company);
 			alp.Company = com;
 			alp.StopoverPlaces = new ArrayList<Airport>();
 			Type listType = new TypeToken<List<Airport>>() {
 			}.getType();
 			Gson gson = new Gson();
 			List<Long> listId = gson.fromJson(Stopovers, listType);
 
 			for (Long id : listId) {
 				Airport ap = Airport.findById(id);
 				alp.StopoverPlaces.add(ap);
 			}
 			alp.Repeat = Repeat;
 			alp.save();
 		} catch (Throwable ex) {
 			// TODO: handle exception
 			badRequest();
 		}
 		list(null, null);
 	}
 }
