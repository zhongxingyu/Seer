 package mpa.main.loader;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import mpa.mad.MadHome;
 
 public class TeamLoaderStream {
 	private MadHome madHome;
 	private ConfigStream configStream;
 
 	public TeamLoaderStream(MadHome madHome, ConfigStream streamData) {
 		this.madHome = madHome;
 		this.configStream = streamData;
 	}
 
 	public void load() {
 		try {
 			for (String team : configStream.getTeamsStream().split("\n"))
 				createTeam(team);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void createTeam(String team) {
 		List<String> names = nextTeamNames(team);
 		String teamName = names.remove(0);
		madHome.createTeam(teamName.trim(), names);
 	}
 
 	private List<String> nextTeamNames(String team) {
		return new LinkedList<String>(Arrays.asList(team.split("/")));
 	}
 }
