 package no.fll.plan.bruteforce;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import no.fll.activity.Activity;
 import no.fll.activity.ActivityService;
 import no.fll.plan.Plan;
 import no.fll.plan.PlanFactory;
 import no.fll.plan.Time;
 import no.fll.schedule.Schedule;
 import no.fll.schedule.ScheduleService;
 import no.fll.team.Team;
 
 public class BruteForcePlanFactory implements PlanFactory {
 
 	@Autowired
 	private ActivityService activityService;
 	@Autowired
 	private ScheduleService scheduleService;
 
 	@Override
 	public List<Plan> generatePlan(String startTime, String endTime, int pitTime, int slack) {
 		List<Activity> activities = activityService.getActivities()
 				.getObjectsToConvertToRecords();
 		List<Schedule> schedules = scheduleService.getSchedule()
 				.getObjectsToConvertToRecords();
 		int minutes = Time.parseTime(endTime).getTotalMinutes()
 				- Time.parseTime(startTime).getTotalMinutes();
 		Activity ringside = null;
 		Activity pit = null;
 		for (Activity activity : activities) {
 			if (activity.getName().equals("Robotkamper")) {
 				ringside = activity;
 			} else if (activity.getName().equals("Pit")) {
 				pit = activity;
 			}
 		}
 		if (ringside == null || pit == null) {
 			return null;
 		}
 		Collection<TeamSchedule> teams = createTeamList(
 				Time.parseTime(startTime).getTotalMinutes(), minutes,
 				schedules, ringside, pit);
 		for (Activity activity : activities) {
 			if (Time.isTime(activity.getTime())) {
 				int time = Time.parseTime(activity.getTime()).getTotalMinutes()
 						- Time.parseTime(startTime).getTotalMinutes();
 				for (TeamSchedule team : teams) {
 					team.set(time, activity.getDuration(), activity.getId());
 				}
 			}
 		}
 		for (Activity activity : activities) {
 			ActivitySchedule activitySchedule = new ActivitySchedule(activity);
 			if (activity.getTime().equals("Auto")) {
 				boolean rc = set_activity(0, minutes, teams, activitySchedule, slack);
 				if (rc == false)
 					return null;
 			}
 		}
 
 		List<Plan> plans = new ArrayList<Plan>();
 		for (TeamSchedule team : teams) {
 			int last = 0;
 			int start = 0;
 			for (int i = 0; i < minutes; i++) {
 				if (team.getActivity(i) != last && last != 0) {
 					Plan plan = new Plan(Time.parseTime(startTime).to_time(
 							start), team.getTeam(), getActivity(last, activities));
 					plans.add(plan);
 				}
 				if (team.getActivity(i) != last) {
 					start = i;
 					last = team.getActivity(i);
 				}
 			}
 			if (last != 0) {
 				Plan plan = new Plan(Time.parseTime(startTime).to_time(start),
 						team.getTeam(), getActivity(last, activities));
 				plans.add(plan);
 			}
 		}
 
 		return plans;
 	}
 
 	private Activity getActivity(int id, List<Activity> activities) {
 		for (Activity activity : activities) {
 			if (activity.getId() == id)
 				return activity;
 		}
 		throw new RuntimeException("Internal error, activity " + id + " not found");
 	}
 	
 	private Collection<TeamSchedule> createTeamList(int startTime,
 			int totalMinutes, List<Schedule> schedules, Activity ringside,
 			Activity pit) {
 		Collection<TeamSchedule> teams = new ArrayList<TeamSchedule>();
 		for (Schedule schedule : schedules) {
 			int time = Time.parseTime(schedule.getTime()).getTotalMinutes()
 					- startTime;
 			if (schedule.getTeam1() != null)
 				setTeamSchedule(
 						getTeamSchedule(teams, schedule.getTeam1(),
 								totalMinutes), ringside, pit, time);
 			if (schedule.getTeam2() != null)
 				setTeamSchedule(
 						getTeamSchedule(teams, schedule.getTeam2(),
 								totalMinutes), ringside, pit, time);
 		}
 		return teams;
 	}
 
 	private void setTeamSchedule(TeamSchedule teamSchedule, Activity ringside,
 			Activity pit, int time) {
 		teamSchedule.set(time - pit.getDuration(), pit.getDuration(),
 				pit.getId());
 		teamSchedule.set(time, ringside.getDuration(), ringside.getId());
 	}
 
 	private TeamSchedule getTeamSchedule(Collection<TeamSchedule> teams,
 			Team team, int totalMinutes) {
 		for (TeamSchedule teamSchedule : teams) {
 			if (teamSchedule.getId() == team.getId())
 				return teamSchedule;
 		}
 		TeamSchedule teamSchedule = new TeamSchedule(team, totalMinutes);
 		teams.add(teamSchedule);
 		return teamSchedule;
 	}
 
 	private boolean set_activity(int minute, int TOTAL_MINUTES,
 			Collection<TeamSchedule> teams, ActivitySchedule activitySchedule, int slack) {
 		Activity activity = activitySchedule.getActivity();
 		if (minute >= TOTAL_MINUTES)
			throw new RuntimeException("Umulig  lage plan, endre dine parametre");
 		for (TeamSchedule team : teams) {
 			if (!team.hasDone(activity.getId())
 					&& team.hasTime(minute, activity.getDuration(), slack)
 					&& activitySchedule.isFree(minute)) {
 				team.set(minute, activity.getDuration(), activity.getId());
 				activitySchedule.set(minute, team.getTeam());
 				if (finished(teams, activity))
 					return true;
 				else if (minute < TOTAL_MINUTES - 1) {
 					boolean rc = set_activity(minute + activity.getDuration(),
 							TOTAL_MINUTES, teams, activitySchedule, slack);
 					if (rc == false) {
 						team.clear(minute, activity.getDuration());
 						activitySchedule.clear(minute, team.getTeam());
 					}
 				} else
 					return finished(teams, activity);
 			}
 		}
 		if (finished(teams, activity))
 			return true;
 		return set_activity(minute + activity.getDuration(), TOTAL_MINUTES,
 				teams, activitySchedule, slack);
 	}
 
 	private boolean finished(Collection<TeamSchedule> teams, Activity activity) {
 		for (TeamSchedule team : teams) {
 			if (!team.hasDone(activity.getId()))
 				return false;
 		}
 		return true;
 	}
 }
