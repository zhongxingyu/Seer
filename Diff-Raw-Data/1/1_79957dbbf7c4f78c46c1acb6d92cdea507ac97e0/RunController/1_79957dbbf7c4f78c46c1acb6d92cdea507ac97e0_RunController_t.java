 package be.vodelee.belote.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import be.vodelee.belote.entity.Game;
 import be.vodelee.belote.entity.Run;
 import be.vodelee.belote.entity.Team;
 
 public class RunController {
 
 	public Run buildRun(List<Team> teamList, List<Run> runList) {
 		Run run = new Run();
 		run.setGames(new ArrayList<Game>());
 
 		// # of game in a run = # team /2
 		// int numberOfGames = (Integer) teamList.size() / 2;
 
 		List<TeamWithAlreadyPlayedTeamList> teamWithAlreadyPlayedTeamList = buildAlreadyPlayedList(teamList, runList);
 		int plantage = 0;
 		
 		boolean isEveryTeamAssigned = false;
 		label : while (!isEveryTeamAssigned) {
 			try {
 				Game game = new Game();
 				boolean isGameValid = false;
 				game.setTeam1(teamWithAlreadyPlayedTeamList.remove(0).getTeam());
 				Random r = new Random();
 				int i = r.nextInt(teamWithAlreadyPlayedTeamList.size());
 				while (!isGameValid) {
 					if (!teamWithAlreadyPlayedTeamList.get(i).getAlreadyPlayedList().contains(game.getTeam1())) {
 						game.setTeam2(teamWithAlreadyPlayedTeamList.remove(i).getTeam());
 						isGameValid = true;
 					} else {
 						i++;
 					}
 				}
 				run.getGames().add(game);
 
 				if (teamWithAlreadyPlayedTeamList.isEmpty()) {
 					isEveryTeamAssigned = true;
 				}
 			} catch (Exception e) {
 				System.err.println(plantage++);
 				//TODO Fix this with a proper while loop
 				teamWithAlreadyPlayedTeamList = buildAlreadyPlayedList(teamList, runList);
				// TODO bug : reset the game list
 				continue label;
 			}
 		}
 
 		return run;
 	}
 
 	private List<TeamWithAlreadyPlayedTeamList> buildAlreadyPlayedList(List<Team> teamList, List<Run> runList) {
 		List<TeamWithAlreadyPlayedTeamList> teamWithAlreadyPlayedTeamList = new ArrayList<TeamWithAlreadyPlayedTeamList>();
 		for (Team t : teamList) {
 			TeamWithAlreadyPlayedTeamList couple = new TeamWithAlreadyPlayedTeamList();
 			couple.setTeam(t);
 			List<Team> alreadyPlayedList = new ArrayList<Team>();
 			for (Run r : runList) {
 				for (Game g : r.getGames()) {
 					// For this game, if the team 1 is the team.
 					if (g.getTeam1().equals(t)) {
 						alreadyPlayedList.add(g.getTeam2());
 					}
 					if (g.getTeam2().equals(t)) {
 						alreadyPlayedList.add(g.getTeam1());
 					}
 				}
 			}
 			couple.setAlreadyPlayedList(alreadyPlayedList);
 			teamWithAlreadyPlayedTeamList.add(couple);
 		}
 		return teamWithAlreadyPlayedTeamList;
 	}
 
 	private class TeamWithAlreadyPlayedTeamList {
 		private Team team;
 
 		private List<Team> alreadyPlayedList;
 
 		public Team getTeam() {
 			return team;
 		}
 
 		public void setTeam(Team team) {
 			this.team = team;
 		}
 
 		public List<Team> getAlreadyPlayedList() {
 			return alreadyPlayedList;
 		}
 
 		public void setAlreadyPlayedList(List<Team> alreadyPlayedList) {
 			this.alreadyPlayedList = alreadyPlayedList;
 		}
 
 		@Override
 		public String toString() {
 			return "TeamWithAlreadyPlayedTeamList [team=" + team + ", alreadyPlayedList=" + alreadyPlayedList + "]";
 		}
 
 	}
 }
