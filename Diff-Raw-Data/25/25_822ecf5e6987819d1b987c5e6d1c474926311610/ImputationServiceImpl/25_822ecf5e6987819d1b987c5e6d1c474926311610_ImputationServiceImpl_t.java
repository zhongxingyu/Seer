 package adhocpes.erp.timetracker.services;
 
 import java.util.List;
 
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import adhocpes.erp.domain.Consultant;
 import adhocpes.erp.domain.Projet;
 import adhocpes.erp.timetracker.domain.Imputation;
 import adhocpes.erp.timetracker.domain.Tache;
 
 import adhocpes.erp.timetracker.repository.ImputationRepository;
 
 @Service
 @Transactional
 public class ImputationServiceImpl implements ImputationService {
 	
 	@Autowired
 	private ImputationRepository imputationRepository;
 	
 	public ImputationServiceImpl(){
 	}
 
 	public void insertImputation(Imputation i) {
 		// TODO Auto-generated method stub
 		imputationRepository.save(i);
 	}
 
 	public void deleteImputation(long id) {
 		// TODO Auto-generated method stub
 		imputationRepository.delete(id);
 		imputationRepository.flush();
 	}
 
 	public void updateImputation(Imputation i) {
 		// TODO Auto-generated method stub
 		imputationRepository.save(i);
 	}
 	
 	
 	public Imputation getOne(long id) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findOne(id);
 	}
 
 	public List<Imputation> getAll() {
 		// TODO Auto-generated method stub
 		return imputationRepository.findAll();
 	}
 
 	public List<Imputation> getImputationsByProjet(Projet p) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findByProjet(p);
 	}
 
 	public List<Imputation> getImputationsByConsultant(Consultant c) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findByConsultant(c);
 	}
 
 	public List<Imputation> getImputationsByTache(Tache t) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findByTache(t);
 	}
 
	public Imputation getByDateConsultant(LocalDate date, Consultant c) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findByDateConsultant(date, c);
 	}
 
	public Imputation getByDateProjet(LocalDate date, Projet p) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findByDateProjet(date, p);
 	}
 
 	public Imputation getByDateTache(LocalDate date, Tache t) {
 		// TODO Auto-generated method stub
 		return imputationRepository.findByDateTache(date, t);
 	}
 
 	
 
 
 }
