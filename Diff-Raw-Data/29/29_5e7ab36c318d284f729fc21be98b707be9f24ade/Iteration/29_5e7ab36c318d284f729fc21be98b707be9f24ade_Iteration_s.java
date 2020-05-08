 package models;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.*;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.collect.Lists;
 
 import play.Logger;
 import play.db.ebean.*;
 import play.data.validation.*;
 
 
 /**
  * Iteration entity managed by Ebean
  */
 @SuppressWarnings("serial")
 @Entity 
 public class Iteration extends Model {
 
     @Id
     public Long id;
     
     @Constraints.Required
     public String name;
 
     public String objId;
     
     public Date iterationStart;
     
     public Date iterationEnd;
     
     public int totalPoints;
     
     public int totalHours;
     
     public int completedPoints;
     
     public int completedHours;
     
     @ManyToOne
     public Team team;
     
     @OneToMany(cascade=CascadeType.ALL)
     public List<Burndown> burndowns;
     
     public Iteration(serialized.Iteration src) {
     	this.name = src.Name;
     	this.objId= src.ObjectID;
     	this.totalHours = 0;
     	this.totalPoints = 0;
     	this.completedPoints = 0;
     	this.completedHours = 0;
     	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
     	try {
 			this.iterationStart = formatter.parse(src.StartDate);
 	    	this.iterationEnd = formatter.parse(src.EndDate);
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(this.iterationEnd);
 			cal.add(Calendar.HOUR_OF_DAY, -24);
 			this.iterationEnd = cal.getTime();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	this.team = new Team(src.Project);
     	this.team.iterations.add(this);
     }
     
     public Iteration(String name) {
     	this.name = name;
     	this.burndowns = Lists.newArrayList();
     }
     
     public String toString() {
        	StringBuffer buf = new StringBuffer();
     	buf.append(name+"\n");
     	for (Burndown burndown : burndowns) {
     		buf.append(burndown).append("\n");
     	}
     	return buf.toString();
     }
     
     /**
      * Business methods
      */
     
     public boolean reportable() {
     	if (null == team.release) {
     		return false;
     	} else if (0  == totalHours || 0 == totalPoints) {
     		return false;
     	}
     	
     	return true;
     }
     
     @Override
     public boolean equals(Object dst) {
     	if (this == dst) return true;
     	if (null == dst) return false;
     	if (!(dst instanceof Iteration)) return false;
     	Iteration dstItr = (Iteration)dst;
     	if (!StringUtils.equals(dstItr.objId, this.objId)) {
     		return false;
     	}
     	return true;
     }
 
     public Burndown createBurndown(Burndown src) {
     	addBurndown(src).save();
     	return src;
     }
 
     public Burndown addBurndown(Burndown src) {
     	burndowns.add(src);
     	src.iteration = this;
     	return src;
     }
     
     /**
      * If given burndown does not exist and fits then add to list and save
      * TODO guava predicate to order and append
      * @param burndown
      */
     public void merge(Iteration src) {
     	Burndown srcToday = src.burndownToday();
 		Burndown existing = burndownToday();
     	// run on another day ignores
     	if (null == existing && null != srcToday) {
     		addBurndown(srcToday);
     		Logger.info("Iteration:merge add burndown:"+srcToday);
     	} else if (null != srcToday) {
     		// update existing hours, points
     		if (existing.hours != srcToday.hours || existing.points != srcToday.points) {
         		Logger.info("Iteration:merge update existing:"+existing+" src:"+srcToday);
         		existing.setHours(srcToday.getHours());
         		existing.setPoints(srcToday.getPoints());
     		}
     	}
 		// update existing points if different (less or more)
 		totalPoints = (totalPoints != src.totalPoints) ? src.totalPoints : totalPoints;
 		completedPoints = (completedPoints != src.completedPoints) ? src.completedPoints : completedPoints;
     }
 
     
     
     /**
      * answer back the burndown matching given day
      * 
      * @param day
      * @return
      */
     public Burndown burndownToday() {
 		Calendar rightNow = Calendar.getInstance();
 		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
 		String day = formatter.format(rightNow.getTime());
 		return hasBurndownForDay(day);
     }
 
     public Burndown hasBurndownForDay(String day) {
     	for (Burndown burndown : burndowns) {
     		// TODO comparitor
     		if (StringUtils.equals(burndown.day, day)) {
     			return burndown;
     		}
     	}
     	return null;
     }
     
     public List<String> workdays() {
     	List<String> dates = Lists.newArrayList();
 		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(iterationStart);
 
        while (calendar.getTime().before(iterationEnd)) {
 			if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
 				String day = formatter.format(calendar.getTime());
 				dates.add(day);
 			}
             calendar.add(Calendar.DATE, 1);
        }
         return dates;
     }
     /**
      * Generic query helper for entity Iteration with id Long
      */
     public static Model.Finder<Long,Iteration> find = new Model.Finder<Long,Iteration>(Long.class, Iteration.class);
 
     /**
      * Static query methods
      */
 
     /**
      * Answer back matching Iterations overlapping today
      * @return
      */
     public static List<Iteration> getTeamIterationsToday() {
     	Calendar rightNow = Calendar.getInstance();
     	rightNow.clear(Calendar.HOUR);
     	rightNow.clear(Calendar.MINUTE);
     	rightNow.clear(Calendar.SECOND);
     	rightNow.clear(Calendar.MILLISECOND);
     	return getTeamIterationsOn(rightNow.getTime());
     }
 
     /**
      * Answer back matching Iterations overlapping last sprint
      * @return
      */
     public static List<Iteration> getTeamIterationsLastSprint() {
     	Calendar previousSprint = Calendar.getInstance();
     	previousSprint.clear(Calendar.HOUR);
     	previousSprint.clear(Calendar.MINUTE);
     	previousSprint.clear(Calendar.SECOND);
     	previousSprint.clear(Calendar.MILLISECOND);
     	previousSprint.add(Calendar.DAY_OF_YEAR, 14);
     	return getTeamIterationsOn(previousSprint.getTime());
     }
 
     /**
      * Answer back matching Iterations
      * @return
      */
     public static List<Iteration> getTeamIterationsOn(Date iterationDate) {
     	List<Iteration> iterations = find.where().lt("iterationStart", iterationDate).gt("iterationEnd", iterationDate).findList();
     	// eager load teams
     	for (Iteration iter : iterations) {
     		String name = iter.team.name;
     	}
     	return iterations;
     }
 
     public static List<Iteration> getLastNTeamIterations(int n, Long teamId) {
     	List<Iteration> iterations = find.where().eq("team_id", teamId).order("iterationStart").findList();
     	return iterations;
     }
 
     /**
      * Answer back iteration of given name
      * 
      * @param release
      * @return
      */
     public static Iteration getByName(String name) {
         return find.where().eq("name", name).findUnique();
     }    
 }
 
