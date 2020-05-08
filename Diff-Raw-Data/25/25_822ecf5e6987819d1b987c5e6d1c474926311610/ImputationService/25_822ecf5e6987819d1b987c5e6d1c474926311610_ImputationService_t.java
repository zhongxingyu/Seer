 package adhocpes.erp.timetracker.services;
 
 import java.util.List;
 
 import org.joda.time.LocalDate;
 import org.springframework.stereotype.Service;
 
 import adhocpes.erp.domain.Consultant;
 import adhocpes.erp.domain.Projet;
 import adhocpes.erp.timetracker.domain.Imputation;
 import adhocpes.erp.timetracker.domain.Tache;
 
 public interface ImputationService {
 	
 	void insertImputation(Imputation i);
 	void deleteImputation(long id);
 	void updateImputation(Imputation i);
 
 	Imputation getOne(long id);
 	List<Imputation> getAll();
 	List<Imputation> getImputationsByProjet(Projet p);
 	List<Imputation> getImputationsByConsultant(Consultant c);
 	List<Imputation> getImputationsByTache(Tache t);
 
	Imputation getByDateConsultant(LocalDate date, Consultant c);
	Imputation getByDateProjet(LocalDate date, Projet p);
 	Imputation getByDateTache(LocalDate date, Tache t);
 
 }
