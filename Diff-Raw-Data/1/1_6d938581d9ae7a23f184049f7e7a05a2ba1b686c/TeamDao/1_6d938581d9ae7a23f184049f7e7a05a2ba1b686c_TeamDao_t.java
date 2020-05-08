 package dk.alumananx.corndogtour.engine.dao;
 
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import dk.alumananx.corndogtour.engine.model.Team;
 
 @Repository("teamDao")
 public class TeamDao {
 
 	@PersistenceContext
 	private EntityManager entityManager;
 
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly=true)
 	public List<Team> findTeamByStatus(int status) {
 		Query query = entityManager.createQuery("SELECT team FROM Team team WHERE team.status = ?1");
 		query.setParameter(1, status);
 		return query.getResultList();
 	}
 
 	//TODO Lav denne om, sÂ den returnere antallet af teams per level og ikke alle teams.
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly=true)
 	public List<Team> findTeamByLevel(Level level) {
 		return entityManager.createQuery("SELECT t FROM Team t WHERE t.level = :level").setParameter("level", level).getResultList();
 	}
 	
	// TODO Lav denne om så den fejler, hvis brugeren findes i forvejen.
 	@Transactional
 	public Team updateTeam(Team team) {
 		return entityManager.merge(team);
 	}
 	
 	@Transactional(readOnly=true)
 	public Team getTeam(String username) {
 		return (Team)entityManager.createQuery("SELECT t FROM Team t WHERE t.username = :username").setParameter("username", username).getSingleResult();
 	}
 	
 }
