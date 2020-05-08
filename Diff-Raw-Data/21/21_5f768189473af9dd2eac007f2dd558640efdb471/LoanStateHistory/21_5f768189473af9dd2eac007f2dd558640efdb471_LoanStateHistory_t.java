 package org.gsoft.openserv.domain.loan;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.gsoft.openserv.domain.interest.LoanRateValue;
 import org.gsoft.openserv.domain.payment.LateFee;
 import org.gsoft.openserv.domain.payment.LoanPayment;
 
 public class LoanStateHistory {
 	private ArrayList<LoanState> loanStates = null;
 	
 	public ArrayList<LoanState> getLoanStates(){
 		if(loanStates == null){
 			loanStates = new ArrayList<LoanState>();
 		}
 		return loanStates;
 	}
 	
 	private void addState(LoanState newState){
 		this.getLoanStates().add(newState);
 		Collections.sort(this.getLoanStates(), new Comparator<LoanState>(){
 				@Override
 				public int compare(LoanState arg0, LoanState arg1) {
 					if(arg0 == arg1){return 0;}
 					int compareValue = arg0.getStateEffectiveDate().compareTo(arg1.getStateEffectiveDate());
 					if(compareValue == 0){
 						if(arg0 instanceof PaymentLoanState && arg1 instanceof PaymentLoanState){
 							compareValue = arg0.getStatePostDate().compareTo(arg0.getStatePostDate());
 						}
 						else if(arg0 instanceof PaymentLoanState){compareValue = 1;}
 						else if(arg1 instanceof PaymentLoanState){compareValue = -1;}
 					}
 					return compareValue;
 				}
 			});
 		int stateIndex = this.getLoanStates().indexOf(newState);
 		if((stateIndex+1) < this.getLoanStates().size()){
 			LoanState nextState = this.getLoanStates().get(stateIndex+1);
 			nextState.setPreviousLoanState(newState);
 		}
 		if(stateIndex > 0){
 			LoanState previousState = this.getLoanStates().get(stateIndex-1);
 			newState.setPreviousLoanState(previousState);
 		}
 	}
 
 	public void addDisbursement(Disbursement disbursement){
 		LoanState newState = new DisbursementLoanState(disbursement);
 		this.addState(newState);
 	}
 	
 	public void addDisbursements(Collection<Disbursement> disbursements){
 		for(Disbursement disb:disbursements){
 			this.addDisbursement(disb);
 		}
 	}
 	
 	public void addPayment(LoanPayment payment){
 		LoanState newState = new PaymentLoanState(payment);
 		this.addState(newState);
 	}
 	
 	public void addLateFee(LateFee lateFee){
 		LoanState newState = new LateFeeState(lateFee);
 		this.addState(newState);
 	}
 	
 	public void addAllLateFees(Collection<LateFee> lateFees){
 		for(LateFee lateFee:lateFees){
 			this.addLateFee(lateFee);
 		}
 	}
 	
 	public void addAllPayments(Collection<LoanPayment> payments){
 		for(LoanPayment payment:payments){
 			this.addPayment(payment);
 		}
 	}
 	
 	public void addAdjustment(LoanBalanceAdjustment adj){
 		LoanState newState = new BalanceAdjustmentLoanState(adj);
 		this.addState(newState);
 	}
 	
 	public void addAllAdjustments(Collection<LoanBalanceAdjustment> adjs){
 		for(LoanBalanceAdjustment adj:adjs){
 			this.addAdjustment(adj);
 		}
 	}
 	
 	public void addRateChange(LoanRateValue loanRate){
 		LoanState newState = new RateValueLoanState(loanRate);
 		this.addState(newState);
 	}
 	
 	public void addAllRateChanges(Collection<LoanRateValue> loanRates){
 		for(LoanRateValue rate:loanRates){
 			this.addRateChange(rate);
 		}
 	}
 	
 	private LoanState getLastLoanState(){
 		return this.getLoanStates().get(this.getLoanStates().size()-1);
 	}
 	
 	public Integer getEndingPrincipal(){
 		return this.getLastLoanState().getPrincipal();
 	}
 	
 	public BigDecimal getEndingInterest(){
 		return this.getLastLoanState().getInterest();
 	}
 	
 	public LoanState getLoanStateAsOf(Date asOfDate){
 		LoanState lastState = null;
 		for(LoanState state:this.getLoanStates()){
 			if(!state.getStateEffectiveDate().after(asOfDate)){
 				lastState = state;
 			}
 		}
 		return lastState;
 	}
 	
 	public Integer getEndingFees(){
 		return this.getLastLoanState().getFees();
 	}
 	
 	public BigDecimal getEndingInterestRate(){
 		return this.getLastLoanState().getInterestRate();
 	}
 	
 	public BigDecimal getEndingBaseRate(){
 		return this.getLastLoanState().getBaseRate();
 	}
 	
 	public BigDecimal getEndingMargin(){
 		return this.getLastLoanState().getMargin();
 	}
 	
/*
 	public String toString(){
 		StringBuffer strBuf = new StringBuffer();
 		for(LoanState state:this.getLoanStates()){
 			strBuf.append("---------------------------------------------").append(System.getProperty("line.separator"));
 			strBuf.append(state.toString());
 			strBuf.append("---------------------------------------------").append(System.getProperty("line.separator"));
 		}
 		return strBuf.toString();
 	}
*/
 }
 
