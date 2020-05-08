 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 public class EqualPrincipal {
 	private BigDecimal totalAmountOfLoan = null;
 	private int termInMonth = 0;
 	private BigDecimal rate = null;
	private BigDecimal paidPrincipal = new BigDecimal(0);
 
 	public EqualPrincipal(BigDecimal totalAmountOfLoan, int termInMonth,
 			BigDecimal rate) {
 		this.totalAmountOfLoan = totalAmountOfLoan;
 		this.termInMonth = termInMonth;
 		this.rate = rate;
 	}
 
 	public RepayPlan calculateRepayPlan() {
 		// 每月还款金额 = （贷款本金 / 还款月数）+（本金 — 已归还本金累计额）×每月利率
 		BigDecimal monthlyPaidPrincipal = BigDecimal.ZERO;
 
 		monthlyPaidPrincipal = this.totalAmountOfLoan.divide(new BigDecimal(
 				this.termInMonth));
 		List<BigDecimal> monthlyPayments = new ArrayList<BigDecimal>();
 		BigDecimal totalAmount = BigDecimal.ZERO;
 
 		for (int idx = 0; idx < this.termInMonth; idx++) {
 			BigDecimal monthlyPayment = monthlyPaidPrincipal
 					.add(totalAmountOfLoan.subtract(paidPrincipal).multiply(
 							this.rate.divide(new BigDecimal("12"))));
 			monthlyPayments.add(monthlyPayment);
 			paidPrincipal = paidPrincipal.add(monthlyPaidPrincipal);
 			totalAmount = totalAmount.add(monthlyPayment);
 		}
 
 		RepayPlan repayPlan = new RepayPlan();
 		repayPlan.setTotalAmount(totalAmount);
 		repayPlan.setMonthlyPayments(monthlyPayments);
 
 		return repayPlan;
 	}
 }
