 package org.gsoft.phoenix.buslogic.loanevent;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.gsoft.phoenix.buslogic.interest.InterestAccrualLogic;
 import org.gsoft.phoenix.buslogic.system.SystemSettingsLogic;
 import org.gsoft.phoenix.domain.loan.Loan;
 import org.gsoft.phoenix.domain.loan.LoanEvent;
 import org.gsoft.phoenix.domain.loan.LoanEventType;
 import org.gsoft.phoenix.domain.loan.LoanTransaction;
 import org.gsoft.phoenix.repositories.loan.LoanEventRepository;
 import org.gsoft.phoenix.repositories.loan.LoanTransactionRepository;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeComparator;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Component;
 
 @Component
 public class LoanEventLogic {
 	@Resource
 	private SystemSettingsLogic systemSettingsLogic;
 	@Resource
 	private LoanEventRepository loanEventRepository;
 	@Resource
 	private LoanTransactionRepository loanTransactionRepository;
 	@Resource
 	private InterestAccrualLogic interestAccrualLogic;
 	
 	/**
 	 * This method will create a loanevent without persisting it.  All of the monetary values 
 	 * will be 0 except for the accrued interest which will contain the interest accrued through
 	 * the effective date passed in.  This will allow the caller to adjust the amounts to apply 
 	 * to fees, interest, and principal, taking the accrued interest into account.  The next step
 	 * will be to "apply" the loanevent, which will potentially trigger a retroactive adjustment 
 	 * of all loanevents with a later effective date.
 	 * @param loan
 	 * @param loanEventType
 	 * @param effectiveDate
 	 * @return
 	 */
 	public LoanEvent createLoanEvent(Loan loan, LoanEventType loanEventType, Date effectiveDate, BigDecimal netAmount, boolean allocateToPrincipal, boolean allocateToInterest, boolean allocateToFees){
 		LoanEvent loanEvent = this.createLoanEvent(loan, loanEventType, effectiveDate);
 		loanEvent = this.applyLoanEvent(loanEvent, netAmount, allocateToPrincipal, allocateToInterest, allocateToFees);
 		this.adjustLoanEventsAfter(loanEvent);
 		return loanEvent;
 	}
 
 	public LoanEvent createLoanEvent(Loan loan, LoanEventType loanEventType, Date effectiveDate, int principalChange, BigDecimal interestChange, int feesChange){
 		LoanEvent loanEvent = this.createLoanEvent(loan, loanEventType, effectiveDate);
 		loanEvent = this.applyLoanEvent(loanEvent, principalChange, interestChange, feesChange);
 		this.adjustLoanEventsAfter(loanEvent);
 		return loanEvent;
 	}
 
 	private LoanEvent createLoanEvent(Loan loan, LoanEventType loanEventType, Date effectiveDate){
 		LoanEvent loanEvent = new LoanEvent();
 		loanEvent.setLoanID(loan.getLoanID());
 		loanEvent.setEffectiveDate(effectiveDate);
 		loanEvent.setLoanEventType(loanEventType);
 		loanEvent.setPostDate(systemSettingsLogic.getCurrentSystemDate());
 		LoanEvent lastEvent = this.findMostRecentLoanEventWithTransactionEffectivePriorToDate(loanEvent.getLoanID(), loanEvent.getEffectiveDate());
 		if(lastEvent != null && DateTimeComparator.getDateOnlyInstance().compare(lastEvent.getEffectiveDate(), effectiveDate) == 0)
 			loanEvent.setSequence(lastEvent.getSequence()+1);
 		else
 			loanEvent.setSequence(0);
 		BigDecimal accruedInterest = new BigDecimal(0);
 		if(lastEvent != null)
 			accruedInterest = interestAccrualLogic.calculateLoanInterestAmountForPeriod(loan, lastEvent.getEffectiveDate(), effectiveDate);
 		
 		LoanTransaction loanTransaction = new LoanTransaction();
 		loanTransaction.setInterestAccrued(accruedInterest);
 		loanEvent.setLoanTransaction(loanTransaction);
 		return loanEvent;
 	}
 	
 	private void adjustLoanEventsAfter(LoanEvent loanEvent){
 		List<LoanEvent> dirtyEvents = loanEventRepository.findAllLoanEventsAfterDate(loanEvent.getEffectiveDate());
 		LoanEvent lastDirty = loanEvent;
 		for(LoanEvent dirtyEvent:dirtyEvents){
 			if(dirtyEvent.getLoanEventType().isFixedAllocation()){
 				this.applyLoanEvent(dirtyEvent, dirtyEvent.getLoanTransaction().getPrincipalChange(), 
 						dirtyEvent.getLoanTransaction().getInterestChange(), 
 						dirtyEvent.getLoanTransaction().getPrincipalChange());
 			}
 			else{
 				BigDecimal netAmount = new BigDecimal(dirtyEvent.getLoanTransaction().getPrincipalChange()).add(dirtyEvent.getLoanTransaction().getInterestChange()).add(new BigDecimal(dirtyEvent.getLoanTransaction().getFeesChange()));
 				this.applyLoanEvent(loanEvent, netAmount, dirtyEvent.getLoanEventType().allocateToPrincipal(), 
 						dirtyEvent.getLoanEventType().allocateToInterest(),  dirtyEvent.getLoanEventType().allocateToFees());
 			}
 			if(DateTimeComparator.getDateOnlyInstance().compare(new DateTime(dirtyEvent.getEffectiveDate()), new DateTime(lastDirty.getEffectiveDate())) == 0)
 				dirtyEvent.setSequence(lastDirty.getSequence()+1);
 			else
 				dirtyEvent.setSequence(0);
 		}
 	}
 	
 	/**
 	 * This method will "apply" the loanevent, which could potentially trigger a retroactive adjustment 
 	 * of all loanevents that occured after this loanevent.
 	 * @param loanEvent
 	 * @return
 	 */
 	public LoanEvent applyLoanEvent(LoanEvent loanEvent, BigDecimal netAmount, boolean allocateToPrincipal, boolean allocateToInterest, boolean allocateToFees){
 		LoanTransaction savedTransaction = this.loanTransactionRepository.save(loanEvent.getLoanTransaction());
 		loanEvent.setLoanTransaction(savedTransaction);
 		LoanEvent lastEvent = this.findMostRecentLoanEventWithTransactionEffectivePriorToDate(loanEvent.getLoanID(), loanEvent.getEffectiveDate());
 		loanEvent = this.allocationNetToBalances(loanEvent, lastEvent, netAmount, allocateToPrincipal, allocateToInterest, allocateToFees);
 		return this.loanEventRepository.save(loanEvent);
 	}
 
 	public LoanEvent applyLoanEvent(LoanEvent loanEvent, int principalChange, BigDecimal interestChange, int feesChange){
 		LoanTransaction savedTransaction = this.loanTransactionRepository.save(loanEvent.getLoanTransaction());
 		loanEvent.setLoanTransaction(savedTransaction);
 		LoanEvent lastEvent = this.findMostRecentLoanEventWithTransactionEffectivePriorToDate(loanEvent.getLoanID(), loanEvent.getEffectiveDate());
 		int endingFees = (lastEvent==null)?0:lastEvent.getLoanTransaction().getEndingFees();
 		BigDecimal endingInterest = (lastEvent==null)?BigDecimal.ZERO:lastEvent.getLoanTransaction().getEndingInterest().add(loanEvent.getLoanTransaction().getInterestAccrued());
 		int endingPrincipal = (lastEvent==null)?0:lastEvent.getLoanTransaction().getEndingPrincipal();
 		if(feesChange != 0){
 			loanEvent.getLoanTransaction().setFeesChange(feesChange);
 			loanEvent.getLoanTransaction().setEndingFees(endingFees + feesChange);
 		}
 		if(interestChange.compareTo(BigDecimal.ZERO) != 0){
 			loanEvent.getLoanTransaction().setInterestChange(interestChange);
 			loanEvent.getLoanTransaction().setEndingInterest(endingInterest.add(loanEvent.getLoanTransaction().getInterestAccrued()).add(interestChange));
 		}
 		if(principalChange != 0){
 			loanEvent.getLoanTransaction().setPrincipalChange(principalChange);
 			loanEvent.getLoanTransaction().setEndingPrincipal(endingPrincipal + principalChange);
 		}
 		return this.loanEventRepository.save(loanEvent);
 	}
 	
 	/**
 	 * Signs of net amount and all ending balances must be opposite or the entire net amount will go to the fees, or the first same sign balance.  Which essentially means
 	 * that the netAmount must be negative.
 	 * @param loanEvent
 	 * @param lastEvent
 	 * @param netAmount
 	 * @param allocateToPrincipal
 	 * @param allocateToInterest
 	 * @param allocateToFees
 	 * @return
 	 */
 	private LoanEvent allocationNetToBalances(LoanEvent loanEvent, LoanEvent lastEvent, BigDecimal netAmount, boolean allocateToPrincipal, boolean allocateToInterest, boolean allocateToFees){
 		//check that the net amount is negative here and throw exception if not.
 		int endingFees = (lastEvent==null)?0:lastEvent.getLoanTransaction().getEndingFees();
 		BigDecimal endingInterest = (lastEvent==null)?BigDecimal.ZERO:lastEvent.getLoanTransaction().getEndingInterest().add(loanEvent.getLoanTransaction().getInterestAccrued());
 		int endingPrincipal = (lastEvent==null)?0:lastEvent.getLoanTransaction().getEndingPrincipal();
 		if(allocateToFees && endingFees > 0 && netAmount.compareTo(BigDecimal.ZERO) != 0){
 			int appliedFees = (endingFees<netAmount.abs().intValue())?endingFees*-1:netAmount.intValue();
 			loanEvent.getLoanTransaction().setFeesChange(appliedFees);
 			netAmount = netAmount.subtract(new BigDecimal(appliedFees));
 			loanEvent.getLoanTransaction().setEndingFees(endingFees + appliedFees);
 		}
 		if(allocateToInterest && endingInterest.compareTo(BigDecimal.ZERO) > 0 && netAmount.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal appliedInterest = (endingInterest.compareTo(netAmount.abs())<0)?endingInterest.negate():netAmount;
 			loanEvent.getLoanTransaction().setInterestChange(appliedInterest);
 			netAmount = netAmount.subtract(appliedInterest);
 			loanEvent.getLoanTransaction().setEndingInterest(endingInterest.add(appliedInterest));
 		}
 		if(allocateToPrincipal && endingPrincipal > 0 && netAmount.compareTo(BigDecimal.ZERO) != 0){
 			int appliedPrincipal = (endingPrincipal<netAmount.abs().intValue())?endingPrincipal*-1:netAmount.intValue();
 			loanEvent.getLoanTransaction().setPrincipalChange(appliedPrincipal);
 			netAmount = netAmount.subtract(new BigDecimal(appliedPrincipal));
 			loanEvent.getLoanTransaction().setEndingPrincipal(endingPrincipal + appliedPrincipal);
 		}
 		return loanEvent;
 	}
 	
 	public LoanEvent findMostRecentLoanEventWithTransactionEffectivePriorToDate(Long loanID, Date checkDate){
 		List<LoanEvent> loanEvent = this.loanEventRepository.findMostRecentLoanEventWithTransactionEffectivePriorToDate(loanID, checkDate, new PageRequest(0, 1));
 		if(loanEvent == null || loanEvent.size() == 0)
 			return null;
 		return loanEvent.get(0);
 	} 
 }
