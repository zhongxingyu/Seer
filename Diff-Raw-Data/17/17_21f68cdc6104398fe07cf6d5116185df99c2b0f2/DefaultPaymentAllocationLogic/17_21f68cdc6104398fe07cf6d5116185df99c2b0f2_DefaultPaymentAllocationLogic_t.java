 package org.gsoft.openserv.buslogic.payment.allocation;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.annotation.Resource;
 
 import org.gsoft.openserv.buslogic.payment.PaymentAllocationLogic;
 import org.gsoft.openserv.domain.loan.Loan;
 import org.gsoft.openserv.domain.loan.LoanTypeProfile;
 import org.gsoft.openserv.domain.payment.LoanPayment;
 import org.gsoft.openserv.domain.payment.Payment;
 import org.gsoft.openserv.repositories.loan.LoanTypeProfileRepository;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Component;
 
 @Component
 public class DefaultPaymentAllocationLogic implements PaymentAllocationLogic{
 	@Resource
 	private LoanTypeProfileRepository loanTypeProfileRepository;
 	
 	private DateTime paymentEffectiveDate;
 	
 	@Override
 	public void allocatePayment(Payment payment, List<Loan> loans) {
 		this.paymentEffectiveDate = new DateTime(payment.getEffectiveDate());
 		Set<Loan> sortedLoans = new TreeSet<Loan>(new LoanDueDateComparator());
 		sortedLoans.addAll(loans);
 		AllocationGroup allocationGroup = new AllocationGroup();
 		for(Loan loan:loans){
 			allocationGroup.addLoan(loan);
 		}
 		allocationGroup.allocateAmount(payment.getPaymentAmount());
 		List<LoanPayment> loanPayments = allocationGroup.getLoanPayments();
 		payment.getLoanPayments().addAll(loanPayments);
 		for(LoanPayment loanPayment:loanPayments){
 			loanPayment.setPayment(payment);
 		}
 	}
 	
 	private interface SyncedAllocation{
 		public int getAmountNeededToAdvance();
 		public DateTime getCurrentDueDate();
 		public void allocateAmount(int amountToAllocate);
 		public void resort();
 		public List<LoanPayment> getLoanPayments();
 		public int size();
 		public String getID();
 	}
 	
 	private class LoanAllocation implements SyncedAllocation, Comparable<SyncedAllocation>{
 		private Loan loan;
 		private int amountAllocated = 0;
 		private LoanTypeProfile loanTypeProfile;
 		private DateTime currentDueDate;
 
 		public LoanAllocation(Loan loan) {
 			loanTypeProfile = loanTypeProfileRepository.findOne(loan.getEffectiveLoanTypeProfileID());
 			this.loan = loan;
 			this.updateCurrentDueDate();
 		}
 		
 		private void updateCurrentDueDate(){
 			currentDueDate = (loan.getCurrentUnpaidDueDate() == null)?null:new DateTime(loan.getCurrentUnpaidDueDate()).minus(loanTypeProfile.getPrepaymentDays());
 			if(currentDueDate != null)
 				currentDueDate = currentDueDate.plusMonths(amountAllocated/loan.getMinimumPaymentAmount());
 		}
 
 		public int getAmountNeededToAdvance(){
 			if(this.getCurrentDueDate() == null || this.getCurrentDueDate().isAfter(paymentEffectiveDate))
 				return loan.getTotalBalance();
 			return loan.getMinimumPaymentAmount();
 		}
 		public int getAllocatedAmount(){
 			return this.amountAllocated;
 		}
 		public void allocateAmount(int amountToAllocate){
 			this.amountAllocated += amountToAllocate;
 			this.updateCurrentDueDate();
 		}
 		public DateTime getCurrentDueDate(){
 			return this.currentDueDate;
 		}
 		public void resort(){}
 		public List<LoanPayment> getLoanPayments(){
 			ArrayList<LoanPayment> loanPayments = new ArrayList<LoanPayment>();
 			if(this.getAllocatedAmount()>0){
 				LoanPayment loanPayment = new LoanPayment();
 				loanPayment.setAppliedAmount(this.getAllocatedAmount());
 				loanPayment.setLoanID(this.loan.getLoanID());
 				loanPayments.add(loanPayment);
 			}
 			return loanPayments;
 		}
 		public int size(){return 1;}
 
 		@Override
 		public int compareTo(SyncedAllocation arg1) {
 			if(this.getCurrentDueDate() == null && arg1.getCurrentDueDate() == null)
 				return 0;
 			if(this.getCurrentDueDate()==null)return 1;
 			if(arg1.getCurrentDueDate()==null)return -1;
 			int dateCompVal = this.getCurrentDueDate().compareTo(arg1.getCurrentDueDate());
 			if(dateCompVal == 0){
 				return this.getID().compareTo(arg1.getID());
 			}
 			return dateCompVal;
 		}
 		
 		@Override
 		public boolean equals(Object obj){
 			if(!(obj instanceof LoanAllocation))return false;
 			return this.getID().equals(((LoanAllocation)obj).getID());
 		}
 		
 		@Override
 		public String getID(){
 			return ""+this.loan.getLoanID();
 		}
 	}
 	
 	private class LoanAllocationGroup implements SyncedAllocation, Comparable<SyncedAllocation>{
 		private TreeSet<SyncedAllocation> allocations;
 		
 		private TreeSet<SyncedAllocation> getAllocations() {
 			if(allocations == null){
 				allocations = new TreeSet<SyncedAllocation>();
 			}
 			return allocations;
 		}
 		
 		public boolean addAllocation(SyncedAllocation newAllocation){
 			if(this.getAllocations().size() == 0 || 
 					newAllocation.getCurrentDueDate().isEqual(this.getCurrentDueDate()) ||
 					(newAllocation.getCurrentDueDate().isAfter(paymentEffectiveDate) &&
 							this.getCurrentDueDate().isAfter(paymentEffectiveDate))){
 				this.getAllocations().add(newAllocation);
 			}
 			else if(this.getCurrentDueDate().getDayOfMonth() != newAllocation.getCurrentDueDate().getDayOfMonth())return false;
 			else{
 				SyncedAllocation existingAllocation = null;
 				for(SyncedAllocation allocation:this.getAllocations()){
 					if(allocation.getCurrentDueDate().isEqual(newAllocation.getCurrentDueDate())){
 						existingAllocation = allocation;
 						break;
 					}
 				}
 				if(existingAllocation == null){
 					LoanAllocationGroup newGroup = new LoanAllocationGroup();
 					newGroup.addAllocation(newAllocation);
 				}
 				else if(existingAllocation instanceof LoanAllocationGroup){
 					((LoanAllocationGroup) existingAllocation).addAllocation(existingAllocation);
 				}
 				else{
 					this.getAllocations().remove(existingAllocation);
 					LoanAllocationGroup newGroup = new LoanAllocationGroup();
 					newGroup.addAllocation(existingAllocation);
 					newGroup.addAllocation(newAllocation);
 				}
 			}
 			return true;
 		}
 		
 		public boolean addLoan(Loan loan){
 			return this.addAllocation(new LoanAllocation(loan));
 		}
 		
 		public DateTime getCurrentDueDate() {
 			if(this.getAllocations().size() > 0)
 				return this.getAllocations().first().getCurrentDueDate();
 			return null;
 		}
 		public int getAmountNeededToAdvance(){
 			int totalAmount = 0;
 			for(SyncedAllocation allocation:this.getAllocations()){
 				totalAmount+=allocation.getAmountNeededToAdvance();
 			}
 			return totalAmount;
 		}
 		public void allocateAmount(int amountToAllocate){
 			while(amountToAllocate > this.getAmountNeededToAdvance()){
 				amountToAllocate -= this.getAmountNeededToAdvance();
 				this.allocateAmount(this.getAmountNeededToAdvance());
 			}
 			if(amountToAllocate == this.getAmountNeededToAdvance()){
 				for(SyncedAllocation allocation:this.getAllocations()){
 					allocation.allocateAmount(allocation.getAmountNeededToAdvance());
 				}
 			}
 			else{
 				int allocatedAmount = 0;
 				for(SyncedAllocation allocation:this.getAllocations()){
 					long allocationAmtToAdv = allocation.getAmountNeededToAdvance();
 					long totalAmtToAdv = this.getAmountNeededToAdvance();
 					int amountToAllocatHere = (int)((allocationAmtToAdv*(long)amountToAllocate)/totalAmtToAdv);
 					allocation.allocateAmount(amountToAllocatHere);
 					allocatedAmount += amountToAllocatHere;
 				}
 				for(SyncedAllocation allocation:this.getAllocations()){
 					if(allocatedAmount == amountToAllocate)
 						return;
 					allocation.allocateAmount(1);
 					allocatedAmount++;
 				}
 			}
 		}
 		public void resort(){
 			for(SyncedAllocation allocation:this.getAllocations()){
 				allocation.resort();
 			}
 			TreeSet<SyncedAllocation> oldSet = this.getAllocations();
 			this.allocations = new TreeSet<SyncedAllocation>();
 			this.getAllocations().addAll(oldSet);
 		}
 		public List<LoanPayment> getLoanPayments(){
 			List<LoanPayment> loanPayments = null;
 			for(SyncedAllocation allocation:this.getAllocations()){
 				if(loanPayments == null)
 					loanPayments = allocation.getLoanPayments();
 				else
 					loanPayments.addAll(allocation.getLoanPayments());
 			}
 			return loanPayments;
 		}
 		@Override
 		public int compareTo(SyncedAllocation arg1) {
 			if(this.getCurrentDueDate() == null && arg1.getCurrentDueDate() == null)
 				return 0;
 			if(this.getCurrentDueDate()==null)return 1;
 			if(arg1.getCurrentDueDate()==null)return -1;
 			int dateCompVal = this.getCurrentDueDate().compareTo(arg1.getCurrentDueDate());
 			if(dateCompVal == 0){
 				return this.getID().compareTo(arg1.getID());
 			}
 			return dateCompVal;
 		}
 		@Override
 		public int size(){
 			int size = 0;
 			for(SyncedAllocation allocation:this.getAllocations()){
 				size+=allocation.size();
 			}
 			return size;
 		}
 		@Override
 		public boolean equals(Object obj){
 			if(!(obj instanceof LoanAllocation))return false;
 			return this.getID().equals(((LoanAllocation)obj).getID());
 		}
 		
 		@Override
 		public String getID(){
 			String id = "";
 			for(SyncedAllocation allocation:this.getAllocations())
 				id += allocation.getID()+":";
 			return id;
 		}
 	}
 	
 	private class AllocationGroup{
 		private TreeSet<LoanAllocationGroup> allocations = new TreeSet<LoanAllocationGroup>();
 		
 		private TreeSet<LoanAllocationGroup> getAllocations(){
 			if(allocations == null){
 				allocations = new TreeSet<LoanAllocationGroup>();
 			}
 			return allocations;
 		}
 		
 		public void addLoan(Loan loan){
 			boolean added = false;
 			for(LoanAllocationGroup allocation:this.getAllocations()){
 				if(allocation.addLoan(loan)){
 					added = true;
 					break;
 				}
 			}
 			if(!added){
 				LoanAllocationGroup group = new LoanAllocationGroup();
 				group.addLoan(loan);
 				this.getAllocations().add(group);
 			}
 			this.resort();
 		}
 		
 		public void allocateAmount(int amountToAllocate){
 			while(amountToAllocate>0){
 				LoanAllocationGroup firstGroup = this.getAllocations().first();
 				int minToAdvance = firstGroup.getAmountNeededToAdvance();
 				if(minToAdvance>amountToAllocate){
 					firstGroup.allocateAmount(amountToAllocate);
 					return;
 				}
 				firstGroup.allocateAmount(minToAdvance);
 				this.resort();
 				amountToAllocate -= minToAdvance;
 			}
 		}
 		
 		private void resort(){
 			for(LoanAllocationGroup allocation:this.getAllocations()){
 				allocation.resort();
 			}
 			TreeSet<LoanAllocationGroup> oldSet = this.getAllocations();
 			this.allocations = new TreeSet<LoanAllocationGroup>();
 			this.getAllocations().addAll(oldSet);
			System.out.println("***Order of payment allocations after resort:");
 			for(SyncedAllocation syncedAllocation:this.getAllocations()){
 				System.out.println("***"+syncedAllocation.size()+" due on "+syncedAllocation.getCurrentDueDate());
 			}
 		}
 
 		public List<LoanPayment> getLoanPayments(){
 			List<LoanPayment> loanPayments = null;
 			for(SyncedAllocation allocation:this.getAllocations()){
 				if(loanPayments == null)
 					loanPayments = allocation.getLoanPayments();
 				else
 					loanPayments.addAll(allocation.getLoanPayments());
 			}
 			return loanPayments;
 		}
 	}
 
 	private class LoanDueDateComparator implements Comparator<Loan>{
 		public int compare(Loan arg0, Loan arg1) {
 			if(arg0.getCurrentUnpaidDueDate() == null && arg1.getCurrentUnpaidDueDate() == null)
 				return 0;
 			if(arg0.getCurrentUnpaidDueDate() == null)return 1;
 			if(arg1.getCurrentUnpaidDueDate() == null)return 0;
 			return arg0.getCurrentUnpaidDueDate().compareTo(arg1.getCurrentUnpaidDueDate());
 		}
 	}
 	
 }
