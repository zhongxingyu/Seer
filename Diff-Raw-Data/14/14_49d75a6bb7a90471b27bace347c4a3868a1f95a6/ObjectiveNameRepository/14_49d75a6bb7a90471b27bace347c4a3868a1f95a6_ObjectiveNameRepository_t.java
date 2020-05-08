 package biz.thaicom.eBudgeting.repositories;
 
 
 
 import java.util.List;
 
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.PagingAndSortingRepository;
 
 import biz.thaicom.eBudgeting.models.pln.ObjectiveName;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveType;
 
 public interface ObjectiveNameRepository extends JpaSpecificationExecutor<ObjectiveName>,
 		PagingAndSortingRepository<ObjectiveName, Long> {
 	
 	@Query("" +
 			"SELECT max(o.index) " +
 			"FROM ObjectiveName o " +
 			"WHERE o.type=?1 AND o.fiscalYear=?2 ")
 	public Integer findMaxIndexOfTypeAndFiscalYear(ObjectiveType type,
 			Integer fiscalYear);
 
 	@Query("" +
 			"SELECT o " +
 			"FROM ObjectiveName o " +
			" 	INNER JOIN o.type type " +
 			"WHERE o.fiscalYear=?1 AND o.type.id = ?2 ")
 	public Page<ObjectiveName> findAllObjectiveNameByFiscalYearAndTypeId(
 			Integer fiscalYear, Long typeId,  Pageable pageable);
 
 	@Query("" +
 			"SELECT o " +
 			"FROM ObjectiveName o " +
 			"	INNER JOIN FETCH o.type type " +
 			"WHERE o.fiscalYear=?1 AND o.type.parent.id = ?2 ")
 	public List<ObjectiveName> findAllChildrenTypeObjectiveNameByFiscalYearAndTypeId(
 			Integer fiscalYear, Long typeId);
 	
 	@Query("" +
 			"SELECT o " +
 			"FROM ObjectiveName o " +
 			"	INNER JOIN FETCH o.type type " +
 			"WHERE o.fiscalYear=?1 AND o.type.parent.id = ?2 " +
 			"	AND (o.name like ?3 OR o.code like ?3) ")
 	public List<ObjectiveName> findAllChildrenTypeObjectiveNameByFiscalYearAndTypeId(
 			Integer fiscalYear, Long id, String searchQuery);
 	
 	
 	@Query("" +
 			"SELECT max(o.code) " +
 			"FROM Objective o " +
 			"WHERE o.type=? AND o.fiscalYear=?2 ")
 	public String findMaxCodeOfTypeAndFiscalYear(ObjectiveType type,
 			Integer fiscalYear);
 
 
 	
 
 }
