 package cs.ut.repository;
 import java.util.Date;
 import java.util.List;
 
 import cs.ut.domain.HireRequestStatus;
 import cs.ut.domain.Plant;
 
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;
 import org.springframework.transaction.annotation.Transactional;
 
 @RooJpaRepository(domainType = Plant.class)
 public interface PlantRepository {
 	
	@Query("SELECT p FROM Plant AS p WHERE p.id NOT IN (SELECT po.plant FROM PurchaseOrder po WHERE po.startDate <= :endDate and po.endDate >= :startDate and po.status != :pendingConfirmation)")
 	
 	@Transactional(readOnly = true)
 	List<Plant> findByDateRange(@Param("startDate") Date startD, @Param("endDate") Date endD, @Param("pendingConfirmation") HireRequestStatus pendingConfirmation);
 	
 	//select p from plant as p where p.id NOT IN 
 	//(SELECT po.plant FROM purchase_order po WHERE (po.start_date >= '2013-12-07' and po.end_date <= '2013-12-10' and po.status != 0 and po.status != 4)  )
 	
 	@Query("SELECT p FROM Plant AS p WHERE p.id = :plantId AND p.id NOT IN (SELECT po.plant FROM PurchaseOrder po WHERE po.startDate >= :startDate and po.endDate <= :endDate and po.status != :pending and po.status != :rejected and po.status != :closed)")
 	
 	@Transactional(readOnly = true)
 	List<Plant> findPlantAvailablilityByDateRange(@Param("startDate") Date startD, @Param("endDate") Date endD, @Param("plantId") long plantId, @Param("pending") HireRequestStatus pending, @Param("rejected") HireRequestStatus rejected, @Param("closed") HireRequestStatus closed);
 	
 }
