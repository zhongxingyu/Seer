 package no.niths.services;
 
 import java.util.List;
 
 import no.niths.domain.Committee;
 import no.niths.domain.Student;
 import no.niths.infrastructure.interfaces.CommitteeRepositorty;
 import no.niths.services.interfaces.CommitteeService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class CommitteeServiceImpl implements CommitteeService {
 
 	@Autowired
 	private CommitteeRepositorty repo;
 
 	/**
 	 * 
 	 * @param committee
 	 * @return
 	 */
 	public Long create(Committee committee) {
 		return repo.create(committee);
 	}
 
 	/**
 	 * <
 	 * @return
 	 */
 	public List<Committee> getAll(Committee committee) {
 		List<Committee> temp = repo.getAll(committee);
 	
 		for (int i = 0; i < temp.size(); i++) {
 			temp.get(i).setEvents(null);
 			temp.get(i).setLeaders(null);
 		}
 		return temp;
 	}
 	
 	
 	/**
 	 * 
 	 * @param cid
 	 * @return
 	 */
 	public Committee getById(long cid) {
 		Committee c = repo.getById(cid);
 		
 		if(c != null){
 			List<Student> leaders = c.getLeaders();
 			for (int i = 0; i < leaders.size(); i++){
 				leaders.get(i).setCommittees(null);
 				leaders.get(i).setCourses(null);
 			}
 			if(c.getEvents().size() < 1){
 				c.setEvents(null);
 			}
 		}
 		return c;
 	}
 
 
 
 	/**
 	 * 
 	 * @param committee
 	 */
 	public void update(Committee committee) {
 		repo.update(committee);
 	}
 	
 	/**
 	 * 
 	 * @param committee
 	 */
 	public boolean delete(long id) {
 		return repo.delete(id);
 	}
 	
 
 }
