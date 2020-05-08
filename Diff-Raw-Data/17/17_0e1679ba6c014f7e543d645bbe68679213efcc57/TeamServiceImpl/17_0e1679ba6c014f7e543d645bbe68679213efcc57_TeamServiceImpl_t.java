 package com.jacad.footapp.service.impl;
 
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.jacad.footapp.dao.TeamDao;
 import com.jacad.footapp.domain.Team;
 import com.jacad.footapp.service.TeamService;
 
@Service("teamService")
 public class TeamServiceImpl implements TeamService {
 	
 	/** The team dao. */
 	@Autowired
 	private TeamDao teamDao;
 
 	@Override
 	@Transactional(readOnly=true)
 	public Team getTeamById(Integer id) {
 		
 		return this.teamDao.getTeamById(id);
 	}
 
 	@Override
 	@Transactional(readOnly=true)
 	public Collection<Team> getAllTeams() {
 		
 		return this.teamDao.getAllTeams();
 	}
 
 	@Override
 	@Transactional
 	public void addTeam(Team team) {
 		
 		this.teamDao.addTeam(team);
 	}
 
 	@Override
 	@Transactional
 	public void removeTeam(Integer id) {
 		
 		this.teamDao.removeTeam(id);
 	}
 
 	@Override
 	@Transactional
 	public void updateTeam(Team team) {
 		
 		this.teamDao.updateTeam(team);
 	}
 
 }
