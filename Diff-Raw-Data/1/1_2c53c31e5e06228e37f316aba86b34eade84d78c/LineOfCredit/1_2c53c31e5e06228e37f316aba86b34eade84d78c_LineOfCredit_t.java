 package project.model;
 
 import java.math.BigDecimal;
 
 public final class LineOfCredit extends AbstractLoan {
     /**
      * The maximum magnitude of the balance of this account, stored as a negative number.
      * TODO: enforce the sign to avoid problems
      */
 	private BigDecimal creditLimit;
 	
 	public LineOfCredit(BigDecimal creditLimit, BigDecimal interestPremium) throws LoanCapException {
 		super(interestPremium);
         Bank.getInstance().authorizeLoan(creditLimit.negate());
         this.creditLimit = creditLimit;
 	}
 
     @Override
     public Type getType() {
         return Type.LINE_OF_CREDIT;
     }
 	
 	@Override
 	public void close() {
         Bank.getInstance().returnLoan(creditLimit.negate());
 	}
 	
 	@Override
 	public Transaction withdraw(BigDecimal amount) throws InsufficientFundsException, OverdraftException {
 		if (getBalance().subtract(amount).compareTo(getCreditLimit()) >= 0) {
 			return super.withdraw(amount);
 		} else {
 			throw new OverdraftException();
 		}
 	}
 	
 	@Override
 	public BigDecimal getCreditLimit() {
 		return creditLimit;
 	}
 
     public void setCreditLimit(BigDecimal creditLimit) throws LoanCapException {
         if (getBalance().compareTo(creditLimit) < 0) {
             throw new IllegalArgumentException("current balance exceeds the new credit limit");
         } else if (this.creditLimit.compareTo(creditLimit) < 0) {
             Bank.getInstance().returnLoan(this.creditLimit.subtract(creditLimit).negate());
         } else {
             Bank.getInstance().authorizeLoan(creditLimit.subtract(this.creditLimit).negate());
         }
     }
 	
 	@Override
 	protected BigDecimal getBaseInterest() {
 		return Bank.getInstance().getPaymentSchedule().getLocInterest();
 	}
 
 	@Override
 	protected BigDecimal getMinimumPayment() {
 		BigDecimal percentPayment = Bank.getInstance().getPaymentSchedule().getLocPercentPayment();
 		BigDecimal fixedPayment = Bank.getInstance().getPaymentSchedule().getLocFixedPayment().negate();
 		BigDecimal percentPaymentValue = percentPayment.multiply(getBalance());
 		
 		if (fixedPayment.compareTo(getBalance()) < 0) {
 			return getBalance();
 		} else if (percentPaymentValue.compareTo(fixedPayment) < 0) {
 			return percentPaymentValue;
 		} else {
 			return fixedPayment;
 		}
 	}
 	
 	@Override
 	protected BigDecimal getPenalty() {
 		return Bank.getInstance().getPaymentSchedule().getLocPenalty();
 	}
 }
