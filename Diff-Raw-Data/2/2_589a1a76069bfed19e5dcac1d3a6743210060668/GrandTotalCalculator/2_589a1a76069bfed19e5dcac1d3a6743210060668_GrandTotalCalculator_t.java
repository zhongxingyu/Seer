 package org.meteornetwork.meteor.business;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.exolab.castor.types.Date;
 import org.meteornetwork.meteor.common.xml.dataresponse.Award;
 import org.meteornetwork.meteor.common.xml.dataresponse.CollectionCosts;
 import org.meteornetwork.meteor.common.xml.dataresponse.DataProviderAggregateTotal;
 import org.meteornetwork.meteor.common.xml.dataresponse.LateFees;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorDataProviderDetailInfo;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorDataProviderInfo;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorRsMsg;
 import org.meteornetwork.meteor.common.xml.dataresponse.OtherFees;
 import org.meteornetwork.meteor.common.xml.dataresponse.ServicingFees;
 
 /**
  * Calculates Original Balance, Outstanding Balance, and Total Other fees for
  * the Repayment summary screen.
  * 
  * @author jlazos
  * 
  */
 public class GrandTotalCalculator {
 
 	private MeteorDataProviderInfo dataProviderInfo;
 	private String borrowerSsn;
 	private transient List<Date> consolidationLoanDates;
 
 	private static final long TWO_HUNDRED_TEN_DAYS_MILLIS = 18144000000L;
 
 	private enum PaidLoanStatus {
 		PC("PC"), PN("PN"), DP("DP"), PF("PF"), DN("DN");
 
 		private final String code;
 
 		private PaidLoanStatus(String code) {
 			this.code = code;
 		}
 
 		public String getCode() {
 			return code;
 		}
 
 		public static PaidLoanStatus getByCodeIgnoreCase(String name) {
 			for (PaidLoanStatus value : PaidLoanStatus.values()) {
				if (value.getCode().equalsIgnoreCase(name)) {
 					return value;
 				}
 			}
 
 			return null;
 		}
 	}
 
 	public GrandTotalCalculator() {
 	}
 
 	public GrandTotalCalculator(MeteorDataProviderInfo dataProviderInfo) {
 		this.dataProviderInfo = dataProviderInfo;
 	}
 
 	public MeteorDataProviderInfo getDataProviderInfo() {
 		return dataProviderInfo;
 	}
 
 	/**
 	 * The parent data provider info object to calculate grand totals for.
 	 * 
 	 * @param dataProviderInfo
 	 */
 	public void setDataProviderInfo(MeteorDataProviderInfo dataProviderInfo) {
 		this.dataProviderInfo = dataProviderInfo;
 	}
 
 	public String getBorrowerSsn() {
 		return borrowerSsn;
 	}
 
 	public void setBorrowerSsn(String borrowerSsn) {
 		this.borrowerSsn = borrowerSsn;
 	}
 
 	/**
 	 * Preconditions: dataProviderInfo and borrowerSsn must have been set on
 	 * this object. if setConsolidationLoanDates has not been called, it will be
 	 * assumed there are no consolidation loans in the entire response
 	 * 
 	 * Calculates the grand total original balance and stores it in the meteor
 	 * data provider info set in this object. The calculation is performed as
 	 * follows:
 	 * 
 	 * Sums all awards where all of the following conditions are met:
 	 * 
 	 * 1) Borrower ssn = queried ssn
 	 * 
 	 * 2) If the loan status is paid (PC, PN, DP, PF, DN), no consolidation loan
 	 * in the ENTIRE RESPONSE has an award begin date less than 210 days away
 	 * from the award's loan status date. (ENTIRE RESPONSE means all
 	 * consolidation loans returned from all data providers are checked.) If a
 	 * consolidation loan has no award begin date, loan date is checked. If no
 	 * loan date, loan status date.
 	 */
 	public void calculateOriginalBalance() {
 		assert dataProviderInfo != null : "dataProviderInfo is null";
 		assert borrowerSsn != null : "borrowerSsn is null";
 
 		createAggregatesIfNotExist();
 
 		DataProviderAggregateTotal totals = dataProviderInfo.getMeteorDataProviderDetailInfo().getDataProviderAggregateTotal();
 		if (dataProviderInfo.getMeteorDataProviderAwardDetails() == null || dataProviderInfo.getMeteorDataProviderAwardDetails().getAwardCount() <= 0) {
 			totals.setOriginalBalanceGrandTotal(BigDecimal.ZERO);
 			return;
 		}
 
 		BigDecimal total = BigDecimal.ZERO;
 		for (Award award : dataProviderInfo.getMeteorDataProviderAwardDetails().getAward()) {
 			if (award.getBorrower() != null && borrowerSsn.equals(award.getBorrower().getSSNum().getContent()) && !consolLoanWithinDaysOfPaidLoanStatDt(award)) {
 				BigDecimal toAdd = award.getGrossLoanAmount() == null ? (award.getAwardAmt() == null ? BigDecimal.ZERO : award.getAwardAmt()) : award.getGrossLoanAmount();
 				total = total.add(toAdd);
 			}
 		}
 
 		totals.setOriginalBalanceGrandTotal(total);
 	}
 
 	private boolean consolLoanWithinDaysOfPaidLoanStatDt(Award award) {
 		if (PaidLoanStatus.getByCodeIgnoreCase(award.getLoanStat()) == null) {
 			return false;
 		}
 
 		Date loanStatDt = award.getLoanStatDt();
 
 		if (consolidationLoanDates != null && loanStatDt != null) {
 			long loanStatDtLong = loanStatDt.toLong();
 			for (Date date : consolidationLoanDates) {
 				if (Math.abs(date.toLong() - loanStatDtLong) < TWO_HUNDRED_TEN_DAYS_MILLIS) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Preconditions: dataProviderInfo and borrowerSsn must have been set on
 	 * this object.
 	 * 
 	 * Calculates the grand total outstanding balance and stores it in the
 	 * meteor data provider info set in this object. The calculation sums up the
 	 * Repyament/AcctBal elements of all awards where borrower ssn = queried ssn
 	 */
 	public void calculateOutstandingBalance() {
 		assert dataProviderInfo != null : "dataProviderInfo is null";
 		assert borrowerSsn != null : "borrowerSsn is null";
 
 		createAggregatesIfNotExist();
 
 		DataProviderAggregateTotal totals = dataProviderInfo.getMeteorDataProviderDetailInfo().getDataProviderAggregateTotal();
 		if (dataProviderInfo.getMeteorDataProviderAwardDetails() == null || dataProviderInfo.getMeteorDataProviderAwardDetails().getAwardCount() <= 0) {
 			totals.setOriginalBalanceGrandTotal(BigDecimal.ZERO);
 			return;
 		}
 
 		BigDecimal total = BigDecimal.ZERO;
 		for (Award award : dataProviderInfo.getMeteorDataProviderAwardDetails().getAward()) {
 			if (award.getBorrower() != null && borrowerSsn.equals(award.getBorrower().getSSNum().getContent())) {
 				if (award.getRepayment() != null && award.getRepayment().getAcctBal() != null) {
 					total = total.add(award.getRepayment().getAcctBal());
 				}
 			}
 		}
 
 		totals.setOutstandingBalanceGrandTotal(total);
 	}
 
 	/**
 	 * Preconditions: dataProviderInfo and borrowerSsn must have been set on
 	 * this object.
 	 * 
 	 * Calculates the grand total other fees and stores it in the meteor data
 	 * provider info set in this object. The calculation sums up all of the
 	 * following fees:
 	 * 
 	 * 1) Repayment/LateFees/LateFeesAmount
 	 * 
 	 * 2) Repayment/CollectionCosts/CollectionCostsAmount
 	 * 
 	 * 3) Repayment/ServicingFees/ServicingFeesAmount
 	 * 
 	 * 4) Repayment/OtherFees
 	 */
 	public void calculateOtherFees() {
 		assert dataProviderInfo != null : "dataProviderInfo is null";
 		assert borrowerSsn != null : "borrowerSsn is null";
 
 		createAggregatesIfNotExist();
 
 		DataProviderAggregateTotal totals = dataProviderInfo.getMeteorDataProviderDetailInfo().getDataProviderAggregateTotal();
 		if (dataProviderInfo.getMeteorDataProviderAwardDetails() == null || dataProviderInfo.getMeteorDataProviderAwardDetails().getAwardCount() <= 0) {
 			totals.setOriginalBalanceGrandTotal(BigDecimal.ZERO);
 			return;
 		}
 
 		BigDecimal total = BigDecimal.ZERO;
 		for (Award award : dataProviderInfo.getMeteorDataProviderAwardDetails().getAward()) {
 			if (award.getBorrower() != null && borrowerSsn.equals(award.getBorrower().getSSNum().getContent()) && award.getRepayment() != null) {
 				if (award.getRepayment().getLateFeesCount() > 0) {
 					for (LateFees fees : award.getRepayment().getLateFees()) {
 						total = total.add(fees.getLateFeesAmount());
 					}
 				}
 
 				if (award.getRepayment().getCollectionCostsCount() > 0) {
 					for (CollectionCosts fees : award.getRepayment().getCollectionCosts()) {
 						total = total.add(fees.getCollectionCostsAmount());
 					}
 				}
 
 				if (award.getRepayment().getServicingFeesCount() > 0) {
 					for (ServicingFees fees : award.getRepayment().getServicingFees()) {
 						total = total.add(fees.getServicingFeesAmount());
 					}
 				}
 
 				if (award.getRepayment().getOtherFeesCount() > 0) {
 					for (OtherFees fees : award.getRepayment().getOtherFees()) {
 						total = total.add(fees.getOtherFeesAmount());
 					}
 				}
 			}
 		}
 
 		totals.setOtherFeesOutstandingGrandTotal(total);
 	}
 
 	private void createAggregatesIfNotExist() {
 		if (dataProviderInfo.getMeteorDataProviderDetailInfo() == null) {
 			dataProviderInfo.setMeteorDataProviderDetailInfo(new MeteorDataProviderDetailInfo());
 		}
 
 		if (dataProviderInfo.getMeteorDataProviderDetailInfo().getDataProviderAggregateTotal() == null) {
 			dataProviderInfo.getMeteorDataProviderDetailInfo().setDataProviderAggregateTotal(new DataProviderAggregateTotal());
 		}
 	}
 
 	public void setConsolidationLoanDates(MeteorRsMsg responseData) {
 		consolidationLoanDates = new ArrayList<Date>();
 
 		for (MeteorDataProviderInfo info : responseData.getMeteorDataProviderInfo()) {
 			if (info.getMeteorDataProviderAwardDetails() != null && info.getMeteorDataProviderAwardDetails().getAwardCount() > 0) {
 				for (Award award : info.getMeteorDataProviderAwardDetails().getAward()) {
 					LoanTypeEnum awardType = LoanTypeEnum.getNameIgnoreCase(award.getAwardType());
 					if (awardType != null && LoanTypeEnum.isConsolidation(awardType)) {
 						Date date = getConsolidationLoanDate(award);
 						if (date != null) {
 							consolidationLoanDates.add(date);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private Date getConsolidationLoanDate(Award consolidationAward) {
 		Date date = consolidationAward.getAwardBeginDt();
 		if (date == null) {
 			date = consolidationAward.getLoanDt();
 		}
 		if (date == null) {
 			date = consolidationAward.getLoanStatDt();
 		}
 		return date;
 	}
 }
