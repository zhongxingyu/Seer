 package cz.muni.fi.pv243.sportleaguesystem.service;
 
 import java.util.List;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import org.apache.log4j.Logger;
 import cz.muni.fi.pv243.sportleaguesystem.dao.interfaces.SportDAO;
 import cz.muni.fi.pv243.sportleaguesystem.entities.Sport;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.SportService;
 
 @ApplicationScoped
 public class SportServiceImpl implements SportService {
 
 	@Inject
 	private Logger logger;
 	
 	@Inject
 	private SportDAO sportDAO;
 	
 	@Override
 	public void createSport(Sport sport) {
 		if (sport == null) {
 			logger.error("Creating a null sport.");
 			throw new IllegalArgumentException("null sport");
 		}
 		if (sport.getId() != null) {
			logger.error("Creating a sport that has a set id.");
 			throw new IllegalArgumentException("id already set");
 		}
 		sportDAO.create(sport);
		logger.info("Created new sport. " + sport);
 	}
 
 	@Override
 	public void updateSport(Sport sport) {
 		if (sport == null) {
 			logger.error("Updating a null sport.");
 			throw new IllegalArgumentException("null sport");
 		}
 		if (sport.getId() == null || sportDAO.get(sport.getId()) == null) {
 			logger.error("Updating a nonexistent sport.");
 			throw new IllegalArgumentException("nonexistent sport");
 		}
 		sportDAO.update(sport);
		logger.info("Updated sport with id=" + sport.getId());
 		
 	}
 
 	@Override
 	public Sport getById(Long id) {
 		if (id == null) {
			logger.error("Getting a sport with null id.");
 			throw new IllegalArgumentException("null id");
 		}
		logger.info("Returning sport with id=" + id);
 		return sportDAO.get(id);
 	}
 
 	@Override
 	public List<Sport> getAll() {
 		logger.info("Returning all sports");
 		return sportDAO.findAll();
 	}
 
 	@Override
 	public void deleteSport(Sport sport) {
 		if (sport == null) {
 			logger.error("Deleting a null sport.");
 			throw new IllegalArgumentException("null sport");
 		}
 		sportDAO.delete(sport);
		logger.info("Deleted sport with id=" + sport.getId());
 	}
 
 	@Override
 	public List<Sport> findSportsByName(String name) {
 		if (name == null) {
 			logger.error("Trying to find a sport with null name.");
 			throw new IllegalArgumentException("null name");
 		}
		logger.info("Returning sports found by name=" + name);
 		return sportDAO.findSportsByName(name);
 	}
 
 }
