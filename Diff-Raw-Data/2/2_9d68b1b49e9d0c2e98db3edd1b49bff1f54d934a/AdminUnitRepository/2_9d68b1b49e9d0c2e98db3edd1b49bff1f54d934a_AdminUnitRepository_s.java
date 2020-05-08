 package border.repository;
 
 import java.util.List;
 
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 
 import border.model.AdminUnit;
 
 public interface AdminUnitRepository extends JpaRepository<AdminUnit, Long>,
 		AdminUnitRepositoryCustom {
 
 	@Query(value = "select * from AdminUnit where AdminUnitTypeID in "
 			+ "(select SubordinateAdminUnitTypeID from AdminUnitTypeSubordination "
 			+ "where MasterAdminUnitTypeID=:adminUnitTypeID and "
 			+ "OpenedDate < NOW() and ClosedDate > NOW() and FromDate < NOW() and ToDate > NOW()) and "
 			+ "OpenedDate < NOW() and ClosedDate > NOW() and FromDate < NOW() and ToDate > NOW() and "
 			+ "id not in "
 			+ "(select SubordinateAdminUnitID from AdminUnitSubordination "
 			+ "where "
 			+ "OpenedDate < NOW() and ClosedDate > NOW() and FromDate < NOW() and ToDate > NOW())", nativeQuery = true)
 	List<AdminUnit> getAdminUnitSubordinatesPossible(
 			@Param("adminUnitTypeID") Long adminUnitTypeID);
 
 	@Query(value = "select * from AdminUnit where AdminUnitTypeID in "
 			+ "(select masterAdminUnitTypeID from AdminUnitTypeSubordination "
 			+ "where SubordinateAdminUnitTypeID=:adminUnitTypeID "
 			+ "and OpenedDate < NOW() and ClosedDate > NOW() and FromDate < NOW() and ToDate > NOW()) and " +
 			"OpenedDate < NOW() and ClosedDate > NOW() and FromDate < NOW() and ToDate > NOW()", nativeQuery = true)
 	List<AdminUnit> getAdminUnitMastersPossible(@Param("adminUnitTypeID") Long adminUnitTypeID);
 	
 	
	@Query(value = "select au from AdminUnit au where AdminUnitTypeID = :adminUnitTypeID "
 								+ "and OpenedDate < NOW() and ClosedDate > NOW() " +
 								"and FromDate < NOW() and ToDate > NOW()", nativeQuery = true)
 	List<AdminUnit> getAdminUnitsOfCertainType(@Param("adminUnitTypeID") Long adminUnitTypeID) ;
 }
