 package org.gsoft.openserv.repositories.rates;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.gsoft.openserv.domain.interest.LoanRateValue;
 import org.gsoft.openserv.repositories.BaseRepository;
 import org.gsoft.openserv.repositories.BaseSpringRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class LoanRateValueRepository extends BaseRepository<LoanRateValue, Long>{
 	@Resource
 	private LoanRateValueSpringRepository springRepository;
 	
 	@Override
 	protected LoanRateValueSpringRepository getSpringRepository() {
 		return springRepository;
 	}
 
 	public List<LoanRateValue> findAllLoanRateValuesThruDate(Long loanID, Date toDate){
 		return this.getSpringRepository().findAllLoanRateValuesThruDate(loanID, toDate);
 	}
 	
 	public List<LoanRateValue> findAllLoanRateValues(Long loanID){
 		return this.getSpringRepository().findAllLoanRateValues(loanID);
 	}
 
 	public List<LoanRateValue> findAllLoanRateValuesFromDateToDate(Long loanID, Date fromDate, Date toDate){
 		return this.getSpringRepository().findAllLoanRateValuesFromDateToDate(loanID, fromDate, toDate);
 	}
 	
 	public LoanRateValue findLoanRateValueForLoanAsOf(Long loanID, Date asOfDate){
 		return this.getSpringRepository().findLoanRateValueForLoanAsOf(loanID, asOfDate);
 	}
 	
 	public LoanRateValue findMostRecentLoanRateValueForLoan(Long loanID){
 		return this.getSpringRepository().findMostRecentForLoan(loanID);
 	}
 }
 
 @Repository
 interface LoanRateValueSpringRepository extends BaseSpringRepository<LoanRateValue, Long>{
 
 	@Query("SELECT lrv FROM LoanRateValue lrv " +
 			"WHERE lrv.loanID = :loanID order by lrv.lockedDate asc")
 	List<LoanRateValue> findAllLoanRateValues(@Param("loanID") Long loanID);
 
 	@Query("SELECT lrv FROM LoanRateValue lrv " +
 			"WHERE lrv.loanID = :loanID AND lrv.lockedDate <= :toDate order by lrv.lockedDate asc")
 	List<LoanRateValue> findAllLoanRateValuesThruDate(@Param("loanID") Long loanID, @Param("toDate") Date toDate);
 
 	@Query("SELECT lrv FROM LoanRateValue lrv " +
 			"WHERE lrv.loanID = :loanID AND lrv.lockedDate >= :fromDate AND lrv.lockedDate <= :toDate")
 	List<LoanRateValue> findAllLoanRateValuesFromDateToDate(@Param("loanID") Long loanID, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
 	
	@Query("SELECT lrv FROM LoanRateValue lrv WHERE lrv.loanID = :loanID AND lrv.lockedDate <= :asOfDate")
 	LoanRateValue findLoanRateValueForLoanAsOf(@Param("loanID") Long loanID, @Param("asOfDate") Date asOfDate);
 	
 	@Query("SELECT lrv FROM LoanRateValue lrv WHERE lrv.loanID = :loanID AND lrv.lockedDate = (" +
			"SELECT MAX(lrv2.lockedDate) FROM LoanRateValue lrv2)")
 	LoanRateValue findMostRecentForLoan(@Param("loanID") Long loanID);
 }
