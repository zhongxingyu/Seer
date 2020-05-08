 package adhocpes.erp.timetracker.repository;
 
 import java.util.List;
 
 import org.joda.time.LocalDate;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 import org.springframework.transaction.annotation.Transactional;
 
 import adhocpes.erp.timetracker.domain.Tache;
 import adhocpes.erp.timetracker.domain.Imputation;
 import adhocpes.erp.domain.Projet;
 import adhocpes.erp.domain.Consultant;
 
 /**
  * 
  * @author toledo
  *
  */
 @Transactional
 public interface ImputationRepository  extends JpaRepository<Imputation,Long>{
 
 	static final String findByProjet = "SELECT i FROM T_Imputation i, T_Tache t WHERE i.tache.nom = t.nom AND t.projet = :projet";
 	static final String findByDateConsultant = "SELECT i FROM T_Imputation i WHERE i.consultant = :consultant AND i.jour = :date";
 	static final String findByDateProjet = "SELECT i FROM T_Imputation i, T_Tache t WHERE i.tache.projet = :projet AND i.jour = :date";
 	static final String findByDateTache = "SELECT i FROM T_Imputation i WHERE i.tache = :tache AND i.jour = :date";
 	List<Imputation> findAll();
 
 	@Query(findByProjet)
 	List<Imputation> findByProjet(@Param("projet")Projet projet);
 	List<Imputation> findByConsultant(Consultant consultant);
 	List<Imputation> findByTache(Tache tache);
 	@Query(findByDateConsultant)
	Imputation findByDateConsultant(@Param("date")LocalDate date, @Param("consultant")Consultant consultant);
 	@Query(findByDateProjet)
	Imputation findByDateProjet(@Param("date")LocalDate date, @Param("projet")Projet projet);
 	@Query(findByDateTache)
 	Imputation findByDateTache(@Param("date")LocalDate date, @Param("tache")Tache tache);
 
 }
