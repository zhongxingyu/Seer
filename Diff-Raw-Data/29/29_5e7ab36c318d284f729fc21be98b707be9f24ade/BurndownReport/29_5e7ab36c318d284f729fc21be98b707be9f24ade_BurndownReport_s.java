 package reports;
 
 import java.util.List;
 
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
 		// fill in ideal based on total hrs
		int topline = iteration.totalHours;
		int delta = topline / filled.size();
 		for (Burndown burndown : filled) {
			burndown.ideal = (topline > 0)? topline : 0;
 			topline -= delta;
 		}
 		return filled;
 	}
 }
