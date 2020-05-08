 package com.jacad.footapp.controller;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.jsf.FacesContextUtils;
 
 import com.jacad.footapp.domain.Player;
 import com.jacad.footapp.domain.Team;
 import com.jacad.footapp.service.TeamService;
 
 @ManagedBean
 @RequestScoped
 public class TeamController {
 	
 	static Logger logger = Logger.getLogger(TeamController.class);
 
 	private TeamService teamService;
 	
 	private Team team;
 	
 	private Integer id;
 	
 	private Collection<Team> teams;
 	
 	private String name;
 	
 	private String colors;
 	
 	private String stadiumName;
 	
 	private Integer creationYear;
 	
 	public TeamController() {
 		
 		logger.info("In TeamController constructor");
 		
 		logger.info("Get context");
 		ApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
 		
 		logger.info("Get bean");
		//this.teamService = ctx.getBean("teamService", TeamService.class);
 		
 		this.id = new Integer(0);
 		
 		this.name = "";
 		this.colors = "";
 		this.creationYear = 0;
 		this.stadiumName = "";
 		
		//this.teams = this.teamService.getAllTeams();
 	}
 
 	public Team getTeam() {
 		return team;
 	}
 
 	public void setTeam(Team team) {
 		this.team = team;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Collection<Team> getTeams() {
 		return teams;
 	}
 
 	public void setTeams(Collection<Team> teams) {
 		this.teams = teams;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getColors() {
 		return colors;
 	}
 
 	public void setColors(String colors) {
 		this.colors = colors;
 	}
 
 	public String getStadiumName() {
 		return stadiumName;
 	}
 
 	public void setStadiumName(String stadiumName) {
 		this.stadiumName = stadiumName;
 	}
 
 	public Integer getCreationYear() {
 		return creationYear;
 	}
 
 	public void setCreationYear(Integer creationYear) {
 		this.creationYear = creationYear;
 	}
 
 	public void findTeam() {
 		if(this.isSetId()) {
 			this.team = this.teamService.getTeamById(this.id);
 		}
 		else {
 			Set<Player> players = new HashSet<>();
 			this.team = new Team("Undefined", "", "", 0, players);
 		}
 	}
 	
 	public boolean isSetId(){
 		if(this.id == 0 || (this.id > this.teams.size()) ){
 			return Boolean.FALSE;
 		}
 		else {
 			return Boolean.TRUE;
 		}
 	}
 	
 	public String createTeam() {
 		if(this.name.isEmpty() || this.colors.isEmpty() || this.creationYear == 0 || this.stadiumName.isEmpty()) {
 			return "addTeam";
 		}
 		else {
 			Team team = new Team();
 			team.setName(this.name);
 			team.setColors(this.colors);
 			team.setStadiumName(this.stadiumName);
 			team.setCreationYear(this.creationYear);
 			team.setPlayers(new HashSet<Player>());
 			
 			this.teamService.addTeam(team);
 			
 			return "teams";
 		}
 	}
 	
 	public String editTeam() {
 		if(this.name.isEmpty() || this.colors.isEmpty() || this.creationYear == 0 || this.stadiumName.isEmpty()) {
 			return "editTeam";
 		}
 		else {
 			this.team.setName(this.name);
 			this.team.setColors(this.colors);
 			this.team.setStadiumName(this.stadiumName);
 			this.team.setCreationYear(this.creationYear);
 			
 			this.teamService.updateTeam(team);
 			
 			return "teams";
 		}
 	}
 	
 }
 
