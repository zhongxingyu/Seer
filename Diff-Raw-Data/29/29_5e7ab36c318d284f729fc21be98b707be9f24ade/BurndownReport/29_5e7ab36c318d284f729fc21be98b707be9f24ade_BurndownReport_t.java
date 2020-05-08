 package reports;
 
 import java.util.List;
 
import play.Logger;

 import com.google.common.collect.Lists;
 
 import models.Burndown;
 import models.Iteration;
 
 
 /**
  * object model representing a burndown report- consumable by generators
  * 
  * @author dlange
  *
  */
 public class BurndownReport extends Report {
 	
 	public String project;
 	public String iteration;
 	public int hoursMax;
 	public int pointsMax;
 	public List<Burndown> data;
 	
 	public BurndownReport(Iteration iteration) {
 		this.project = iteration.team.name;
 		this.iteration = iteration.name;
 		this.hoursMax = iteration.totalHours;
 		this.pointsMax = iteration.totalPoints;
 		this.data = backfill(iteration);
 	}
 
 	
 	public Report generate() {
 		htmlReport = views.html.burndownreport.render(this).body();
 		generated = true;
 		return this;
 	}
 	
 	private List<Burndown> backfill(Iteration iteration) {
 		List<Burndown> filled = Lists.newArrayList();
 		for (String day : iteration.workdays()) {
 			// fill in gaps with nuthin
 			Burndown burndown = iteration.hasBurndownForDay(day);
 			if (null == burndown) {
 				burndown = new Burndown(day, iteration);
 			} 
 			filled.add(burndown);
 		}
		Logger.info("BurndownReport.backfill size:"+filled.size());
 		// fill in ideal based on total hrs
		double topline = iteration.totalHours;
		double delta = topline / (filled.size() - 1);
 		for (Burndown burndown : filled) {
			burndown.ideal = (topline > 0)? (int)topline : 0;
 			topline -= delta;
 		}
 		return filled;
 	}
 }
