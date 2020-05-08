 package no.fll.plan.bruteforce;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import no.fll.activity.Activity;
 import no.fll.activity.ActivityService;
 import no.fll.plan.Plan;
 import no.fll.plan.PlanFactory;
 import no.fll.plan.Time;
 import no.fll.schedule.Schedule;
 import no.fll.schedule.ScheduleService;
 
 
 public class BruteForcePlanFactory implements PlanFactory {
 
 	@Autowired
 	private ActivityService activityService;
 	@Autowired
 	private ScheduleService scheduleService;
 	
 	@Override
 	public List<Plan> generatePlan(String startTime, String endTime, int pitTime) {
 		List<Activity> activities = activityService.getActivities().getObjectsToConvertToRecords();
 		List<Schedule> schedules = scheduleService.getSchedule().getObjectsToConvertToRecords();
 		int minutes = new Time(endTime).getTotalMinutes() - new Time(startTime).getTotalMinutes();
 		List<TeamSchedule> teams = createTeamList(new Time(startTime).getTotalMinutes(), minutes, schedules);
 		for (Activity activity : activities) {
 			ActivitySchedule activitySchedule = new ActivitySchedule(activity);
 			boolean rc = set_activity(0, minutes, teams, activitySchedule);
 			if (rc == false) 
				break;
 		}
 		
 		List<Plan> plans = new ArrayList<Plan>();
 		for (TeamSchedule team : teams) {
 			int last = 0;
 			int start = 0; 
 			for (int i=0; i<minutes; i++) {
 				if (team.getActivity(i) != last && last != 0) {
 					Plan plan = new Plan(new Time(startTime).to_time(start), team.getName(), activities.get(last-1).getActivity());
 					plans.add(plan);
 				}
 				if (team.getActivity(i) != last) {
 					start = i;
 					last = team.getActivity(i);
 				}
 			}
 			if (last != 0) {
 				Plan plan = new Plan(new Time(startTime).to_time(start), team.getName(), activities.get(last-1).getActivity());
 				plans.add(plan);
 			}
 		}
 		
 		return plans;
 	}
 
 	private List<TeamSchedule> createTeamList(int startTime, int totalMinutes, List<Schedule> schedules) {
 		List<TeamSchedule> teams = new ArrayList<TeamSchedule>();
 		for (Schedule schedule : schedules) {
 			TeamSchedule team1 = getTeamSchedule(teams, schedule.getTeam1(), totalMinutes);
 			team1.set(new Time(schedule.getTime()).getTotalMinutes() - startTime, 6, 1);
 			TeamSchedule team2 = getTeamSchedule(teams, schedule.getTeam2(), totalMinutes);
 			team2.set(new Time(schedule.getTime()).getTotalMinutes() - startTime, 6, 1);
 		}
 		return teams;
 	}
 
 	private TeamSchedule getTeamSchedule(List<TeamSchedule> teams, no.fll.team.Team teamName, int totalMinutes) {
 		TeamSchedule team = new TeamSchedule(teamName, totalMinutes);
 		while (teams.size() < team.getId())
 			teams.add(null);
 		if (teams.get(team.getId()-1) != null)
 			return teams.get(team.getId()-1);
 		teams.set(team.getId()-1, team);
 		return team;
 	}
 
 	private boolean set_activity(int minute, int TOTAL_MINUTES, List<TeamSchedule> teams, ActivitySchedule activitySchedule) {
 		Activity activity = activitySchedule.getActivity();
 		if (minute >= TOTAL_MINUTES)
 			return finished(teams, activity);
 		for (TeamSchedule team : teams) {
 			if (!team.hasDone(activity.getId()) && team.hasTime(minute, activity.getDuration()) && activitySchedule.isFree(minute))	{
 				team.set(minute, activity.getDuration(), activity.getId());
 				activitySchedule.set(minute, team.getTeam());
 				if (finished(teams, activity))
 					return true;
 				else if (minute < TOTAL_MINUTES-1) {
 					boolean rc = set_activity(minute+activity.getDuration(), TOTAL_MINUTES, teams, activitySchedule);
 					if (rc == false) {
 						team.clear(minute, activity.getDuration());
 						activitySchedule.clear(minute, team.getTeam());
 					}
 				}
 				else return finished(teams, activity);
 			}
 		}
		return finished(teams, activity);
 	}
 
 	private boolean finished(List<TeamSchedule> teams, Activity activity) {
 		for (TeamSchedule team : teams) {
 			if (!team.hasDone(activity.getId()))
 				return false;
 		}
 		return true;
 	}
 }
