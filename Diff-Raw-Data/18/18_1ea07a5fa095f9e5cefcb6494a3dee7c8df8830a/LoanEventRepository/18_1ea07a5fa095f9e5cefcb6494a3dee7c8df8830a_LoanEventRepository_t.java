 package org.gsoft.openserv.repositories.loan;
 
 import java.util.Date;
 import java.util.List;
 
 import org.gsoft.openserv.domain.loan.LoanEvent;
 import org.gsoft.openserv.domain.loan.LoanEventType;
 import org.gsoft.openserv.repositories.BaseRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public interface LoanEventRepository extends BaseRepository<LoanEvent, Long>{
 	
	@Query("select le from LoanEvent le where le.loanID = :loanID and le.sequence = " +
			"(select max(le2.sequence) from LoanEvent le2 join le2.loanTransaction where le2.loanID = :loanID and le2.effectiveDate <= :checkDate)")
 	public LoanEvent findMostRecentLoanEventWithTransactionEffectiveOnOrBeforeDate(@Param("loanID") Long loanID, @Param("checkDate") Date checkDate);
 
 	@Query("select le from LoanEvent le where le.loanTransaction is not null and le.loanID = :loanID and le.sequence = " +
 			"(select max(le2.sequence) from LoanEvent le2 where le2.loanID = :loanID and le2.sequence < :sequence)")
 	public LoanEvent findMostRecentLoanEventWithTransactionPriorToSequence(@Param("loanID") Long loanID, @Param("sequence") Integer sequence);
 
 	@Query("select le from LoanEvent le where le.loanTransaction is not null and le.loanID = :loanID and le.sequence = " +
 			"(select max(le2.sequence) from LoanEvent le2 where le2.loanID = :loanID)")
 	public LoanEvent findMostRecentLoanEventWithTransaction(@Param("loanID") Long loanID);
 
 	@Query("select le from LoanEvent le where le.loanID = :loanID order by le.effectiveDate desc, le.sequence desc")
 	public List<LoanEvent> findAllByLoanID(@Param("loanID") Long loanID);
 	
 	@Query("select le from LoanEvent le where le.loanID = :loanID and le.effectiveDate > :fromDate order by le.effectiveDate desc, le.sequence desc")
 	public List<LoanEvent> findAllLoanEventsAfterDate(@Param("loanID") Long loanID, @Param("fromDate") Date fromDate);
 	
 	@Query("select le from LoanEvent le where le.loanID = :loanID and le.loanEventType = :loanEventType order by le.effectiveDate desc, le.sequence desc")
 	public List<LoanEvent> findAllLoanEventsOfTypeForLoan(@Param("loanID") Long loanID, @Param("loanEventType") LoanEventType loanEventType);

	@Query("select le from LoanEvent le where le.loanID = :loanID and le.sequence > :sequence order by le.sequence desc")
	public List<LoanEvent> findAllLoanEventsForLoanAfterSequence(@Param("loanID") Long loanID, @Param("sequence") Integer sequence);
 }
