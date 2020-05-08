 package no.niths.services;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import no.niths.aop.ApiEvent;
 import no.niths.domain.Subject;
 import no.niths.infrastructure.interfaces.SubjectRepository;
 import no.niths.services.interfaces.SubjectService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class SubjectServiceImpl implements SubjectService{
 
 	private Logger logger = LoggerFactory.getLogger(CommitteeServiceImpl.class);
 	private CustomBeanUtilsBean beanCopy = new CustomBeanUtilsBean();
 
 	
     @Autowired
     private SubjectRepository repo;
 
     @ApiEvent(title = "Subject created")
     public Long create(Subject topic) {
         return repo.create(topic);
     }
 
     public Subject getById(long id) {
     	Subject s = repo.getById(id);
     	s.getTutors().size();
     	return s;
    }
 
     public List<Subject> getAll(Subject topic) {
     	return repo.getAll(topic);
     }
 
     @ApiEvent(title = "Subject updated")
     public void update(Subject subject) {
     	Subject subjectToUpdate = repo.getById(subject.getId());
 		try {
 			beanCopy.copyProperties(subjectToUpdate, subject);
 		} catch (IllegalAccessException | InvocationTargetException e) {
 			logger.error("error",e);
 			e.printStackTrace();
 		}
 		repo.update(subjectToUpdate);
     }
 
     public boolean delete(long id) {
         return repo.delete(id);
     }
 
 	@Override
 	public void hibernateDelete(long id) {
 		repo.hibernateDelete(id);
 	}

	@Override
	public void updateForTutor(Subject subject) {
		repo.update(subject);
	}
 }
