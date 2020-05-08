 package com.jdc.themis.dealer.service.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.time.calendar.LocalDate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableListMultimap;
 import com.google.common.collect.Multimaps;
 import com.jdc.themis.dealer.data.dao.ReportDAO;
 import com.jdc.themis.dealer.domain.DealerAccountReceivableFact;
 import com.jdc.themis.dealer.domain.DealerEmployeeFeeFact;
 import com.jdc.themis.dealer.domain.DealerHRAllocationFact;
 import com.jdc.themis.dealer.domain.DealerIncomeExpenseFact;
 import com.jdc.themis.dealer.domain.DealerIncomeRevenueFact;
 import com.jdc.themis.dealer.domain.ReportItem;
 import com.jdc.themis.dealer.report.DealerAccountReceivableReportCalculator;
 import com.jdc.themis.dealer.report.DealerExpensePercentageReportCalculator;
 import com.jdc.themis.dealer.report.DealerHRAllocationReportCalculator;
 import com.jdc.themis.dealer.report.DealerIncomeReportCalculator;
 import com.jdc.themis.dealer.report.DealerPostSalesDepartmentIncomeReportCalculator;
 import com.jdc.themis.dealer.report.DealerPostSalesIncomeReportCalculator;
 import com.jdc.themis.dealer.report.DealerReportCalculator;
 import com.jdc.themis.dealer.report.DealerSalesIncomeReportCalculator;
 import com.jdc.themis.dealer.report.DealerSalesReportCalculator;
 import com.jdc.themis.dealer.report.DepartmentReportCalculator;
 import com.jdc.themis.dealer.report.JournalOp;
 import com.jdc.themis.dealer.service.DealerIncomeReportService;
 import com.jdc.themis.dealer.service.RefDataQueryService;
 import com.jdc.themis.dealer.utils.Performance;
 import com.jdc.themis.dealer.web.domain.ImportReportDataRequest;
 import com.jdc.themis.dealer.web.domain.QueryDealerAccountReceivableResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerExpensePercentageResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerHRAllocationResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerIncomeResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerMaintenanceIncomeResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerPostSalesResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerSalesIncomeResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerSalesResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerSheetSprayIncomeResponse;
 import com.jdc.themis.dealer.web.domain.QueryDealerSparePartIncomeResponse;
 import com.jdc.themis.dealer.web.domain.QueryDepartmentIncomeResponse;
 import com.jdc.themis.dealer.web.domain.ReportDealerDataList;
 import com.jdc.themis.dealer.web.domain.ReportDealerExpensePercentageDataList;
 import com.jdc.themis.dealer.web.domain.ReportDealerSalesDataList;
 import com.jdc.themis.dealer.web.domain.ReportDepartmentDataList;
 
 import fj.data.Option;
 
 @Service
 public class DealerIncomeReportServiceImpl implements DealerIncomeReportService {
 	private final static Logger logger = LoggerFactory
 			.getLogger(DealerIncomeReportServiceImpl.class);
 
 	@Autowired
 	private ReportDAO reportDAL;
 	@Autowired
 	private RefDataQueryService refDataDAL;
 
 	// add this for test
 	public void setRefDataDAL(RefDataQueryService refDataDAL) {
 		this.refDataDAL = refDataDAL;
 	}
 	// add this for test
 	public void setReportDAL(ReportDAO reportDAL) {
 		this.reportDAL = reportDAL;
 	}
 
 	/**
 	 * Import report data by given dates.
 	 * 
 	 * User can provide a range of dates, i.e. 'fromDate' and 'toDate'.
 	 */
 	@Override
 	@Performance
 	public void importReportData(final ImportReportDataRequest request) {
 		Preconditions.checkNotNull(request.getFromDate(),
 				"from date can't be null");
 		Preconditions
 				.checkNotNull(request.getToDate(), "to date can't be null");
 		final Calendar c = new GregorianCalendar();
 		c.setTime(new Date());
 		final LocalDate today = LocalDate.of(c.get(Calendar.YEAR),
 				c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
 		Preconditions.checkArgument(!LocalDate.parse(request.getToDate())
 				.isAfter(today), "cannot import future date");
 
 		final LocalDate fromDate = LocalDate.parse(request.getFromDate());
 		int counter = 0;
 		while (true) {
 			final LocalDate currentDate = fromDate.plusMonths(counter);
 			if (currentDate.isAfter(LocalDate.parse(request.getToDate()))) {
 				break;
 			}
 			logger.debug("importing report data for date {}", currentDate);
 			reportDAL.importVehicleSalesJournal(currentDate);
 			reportDAL.importSalesServiceJournal(currentDate);
 			reportDAL.importGeneralJournal(currentDate);
 			reportDAL.importTaxJournal(currentDate);
 			reportDAL.importHRAllocation(currentDate);
 			reportDAL.importAccountReceivable(currentDate);
 			reportDAL.importInventory(currentDate);
 			reportDAL.importEmployeeFee(currentDate);
 			counter++;
 		}
 	}
 
 	/**
 	 * Query overall income report data. 
 	 */
 	@Override
 	@Performance
 	public QueryDealerIncomeResponse queryOverallIncomeReport(final Integer year,
 			final Option<Integer> monthOfYear,
 			final Option<Integer> departmentID,
 			final Option<Integer> denominator, final Option<Integer> groupBy) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("OverallIncomeReport");
 		
 		if ( groupBy.isSome() ) {
 			final ReportDealerDataList reportDetailCurrentYear = getDealerReportDataDetail(
 					year, Option.<Integer> none(),
 					Option.<ReportDealerDataList> none(),
 					Option.<Integer> none(), Option.<Integer> none(), JournalOp.SUM, groupBy);
 			reportDetailCurrentYear.setYear(year);
 			response.getDetail().add(reportDetailCurrentYear);
 			return response;
 		}
 
 		if (monthOfYear.isNone()) {
 			int previousYear = year - 1;
 			final ReportDealerDataList reportDetailPreviousYear = getDealerReportDataDetail(
 					previousYear, Option.<Integer> none(),
 					Option.<ReportDealerDataList> none(), departmentID,
 					denominator, JournalOp.SUM, Option.<Integer> none());
 			reportDetailPreviousYear.setYear(previousYear);
 			response.getDetail().add(reportDetailPreviousYear);
 
 			final ReportDealerDataList reportDetailCurrentYear = getDealerReportDataDetail(
 					year, Option.<Integer> none(),
 					Option.<ReportDealerDataList> some(reportDetailPreviousYear),
 					departmentID, denominator, JournalOp.SUM, Option.<Integer> none());
 			reportDetailCurrentYear.setYear(year);
 			response.getDetail().add(reportDetailCurrentYear);
 		} else {
 			final ReportDealerDataList reportDetailMonthlyAvg = getDealerReportDataDetail(
 					year, monthOfYear, Option.<ReportDealerDataList> none(),
 					departmentID, denominator, JournalOp.AVG, Option.<Integer> none());
 			response.getDetail().add(reportDetailMonthlyAvg);
 
 			final ReportDealerDataList reportDetailCurrentMonth = getDealerReportDataDetail(
 					year, monthOfYear,
 					Option.<ReportDealerDataList> some(reportDetailMonthlyAvg),
 					departmentID, denominator, JournalOp.SUM, Option.<Integer> none());
 			response.getDetail().add(reportDetailCurrentMonth);
 		}
 		return response;
 	}
 	
 	@Override
 	public QueryDealerIncomeResponse queryPostSalesOverallIncomeReport(
 			Integer year, Option<Integer> monthOfYear, Option<Integer> denominatorIDOption) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("PostSalesOverallIncomeReport");
 		
 		final DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year)
 					.withDenominator(denominatorIDOption);
 		calculator.withMonth(monthOfYear);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		if ( monthOfYear.isSome() ) {
 			queryBuilder.withLessThanMonthOfYear(monthOfYear.some());
 		} else {
 			queryBuilder.withMonthOfYear(monthOfYear.some());
 		}
 		
 		queryBuilder.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 					.withDepartmentID(refDataDAL.getDepartment("备件部").getId())
 					.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId());
 		
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder.withItemCategory("维修收入")
 								.withItemCategory("配件收入")
 								.withItemCategory("钣喷收入")
 								.withItemCategory("维修其它收入")
 								.withItemCategory("钣喷其它收入").queryRevenues();
 		
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		// Get all expenses
 		final Collection<DealerIncomeExpenseFact> expenseFacts = 
 												queryBuilder.clear() // clear the builder for next query
 															.withItemCategory("变动费用")
 															.withItemCategory("销售费用")
 															.withItemCategory("人工费用")
 															.withItemCategory("半固定费用")
 															.withItemCategory("固定费用")
 															.queryExpenses();
 
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 		if ( monthOfYear.isNone() ) {
 			calculator.calcRevenues(dealerRevenueFacts, JournalOp.SUM)
 					.calcMargins(dealerRevenueFacts, JournalOp.SUM)
 					.calcExpenses(dealerExpenseFacts, JournalOp.SUM).calcOpProfit();
 		} else {
 			calculator.calcRevenues(dealerRevenueFacts, JournalOp.AVG)
 			.calcMargins(dealerRevenueFacts, JournalOp.AVG)
 			.calcExpenses(dealerExpenseFacts, JournalOp.AVG).calcOpProfit();
 		}
 		if (denominatorIDOption.isSome()) {
 			calculator.prepareDenominators()
 					.adjustRevenueByDenominator()
 					.adjustExpenseByDenominator()
 					.adjustMarginByDenominator()
 					.adjustOpProfitByDenominator();
 		}
 		
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 	
 	@Override
 	public QueryDealerIncomeResponse queryNonRecurrentPNLReport(Integer year) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("NonRecurrentPNLReport");
 		
 		final DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder otherQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		
 		final Collection<DealerIncomeRevenueFact> otherRevenueFacts = otherQueryBuilder.withItemCategory("非经营性损益进项").queryRevenues();
 
 		final Collection<DealerIncomeExpenseFact> otherExpenseFacts = otherQueryBuilder.clear()// clear the builder for next query
 							.withItemCategory("非经营性损益削项").queryExpenses();
 
 		calculator.calcNonRecurrentPNL(Multimaps.index(otherRevenueFacts,GetDealerIDFromRevenueFunction.INSTANCE),
 				Multimaps.index(otherExpenseFacts,GetDealerIDFromExpenseFunction.INSTANCE), JournalOp.SUM);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 	
 	@Override
 	public QueryDealerIncomeResponse queryNonSalesProfitReport(Integer year) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("NonSalesProfitReport");
 		
 		final DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder otherQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		
 		final Collection<DealerIncomeRevenueFact> revenueFacts = otherQueryBuilder.withItemCategory("非销售类返利").queryRevenues();
 
 		calculator.calcRevenues(Multimaps.index(revenueFacts,GetDealerIDFromRevenueFunction.INSTANCE), JournalOp.SUM);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 	
 	@Override
 	public QueryDealerIncomeResponse queryNewVehicleRetailSalesReport(
 			Integer year) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("NewVehicleRetailSalesReport");
 
 		DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 				.withItemCategory("新轿车零售").withItemCategory("新货车零售")
 				.queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		calculator.calcCount(dealerRevenueFacts);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	public QueryDealerIncomeResponse queryNewVehicleSalesReport(Integer year) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("NewVehicleSalesReport");
 
 		DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 				.withItemCategory("新轿车零售").withItemCategory("新货车零售")
 				.queryRevenues();
 		final Collection<DealerIncomeRevenueFact> kaRevenueFacts = queryBuilder
 				.clear()
 				.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 				.withItemCategory("新车其它收入").withItemName("大客户采购（租车公司，政府机关）")
 				.queryRevenues();
 		revenueFacts.addAll(kaRevenueFacts);
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		calculator.calcCount(dealerRevenueFacts);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	public QueryDealerIncomeResponse queryNewVehicleRetailMarginReport(
 			Integer year) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerIncomeResponse response = new QueryDealerIncomeResponse();
 		response.setReportName("NewVehicleRetailMarginReport");
 
 		DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 				.withItemCategory("新轿车零售").withItemCategory("新货车零售")
 				.queryRevenues();
 		final Collection<DealerIncomeRevenueFact> kaRevenueFacts = queryBuilder
 				.clear()
 				.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 				.withItemCategory("新车其它收入").withItemName("零售销售返利")
 				.queryRevenues();
 		revenueFacts.addAll(kaRevenueFacts);
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts, JournalOp.SUM).calcCount(
 				dealerRevenueFacts);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	public QueryDealerSalesIncomeResponse queryDealerSalesIncomeReport(
 			Integer year, Integer monthOfYear) {
 		final DealerSalesIncomeReportCalculator calculator = new DealerSalesIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 		final QueryDealerSalesIncomeResponse response = new QueryDealerSalesIncomeResponse();
 		response.setReportName("SalesIncomeReport");
 		
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 								.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 								.withDepartmentID(refDataDAL.getDepartment("二手车部").getId())
 								.withItemCategory("新轿车零售")
 								.withItemCategory("新货车零售")
 								.withItemCategory("附加产品业务")
 								.withItemCategory("二手车零售")
 								.withItemCategory("新车其它收入")
 								.withItemCategory("二手车其它收入")
 								.queryRevenues();
 
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		// Get all expenses
 		final Collection<DealerIncomeExpenseFact> expenseFacts = 
 												queryBuilder.clear() // clear the builder for next query
 															.withDepartmentID(refDataDAL.getDepartment("新车销售部").getId())
 															.withDepartmentID(refDataDAL.getDepartment("二手车部").getId())
 															.withItemCategory("变动费用")
 															.withItemCategory("销售费用")
 															.withItemCategory("人工费用")
 															.withItemCategory("半固定费用")
 															.withItemCategory("固定费用")
 															.queryExpenses();
 
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts)
 				.calcMargins(dealerRevenueFacts)
 				.calcExpenses(dealerExpenseFacts)
 				.calcOpProfit();
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 	
 	@Override
 	public QueryDealerExpensePercentageResponse queryOverallExpensePercentageReport(
 			final Integer year, final Integer monthOfYear, final Integer denominator, final Option<String> category, final Option<String> itemName) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		Preconditions.checkNotNull(denominator, "denominator can't be null");
 		
 		final QueryDealerExpensePercentageResponse response = new QueryDealerExpensePercentageResponse();
 		response.setReportName("OverallExpensePercentageReport");
 		
 		final ReportDealerExpensePercentageDataList reportDetailMonthlyAvg = getDealerExpensePercentageReportDataDetail(
 					year, monthOfYear, denominator, category, itemName);
 		response.getDetail().add(reportDetailMonthlyAvg);
 		return response;
 	}
 
 	/**
 	 * Query department income report data.
 	 */
 	@Override
 	@Performance
 	public QueryDepartmentIncomeResponse queryDepartmentIncomeReport(
 			final Integer year, final Option<Integer> monthOfYear, 
 			final Option<Integer> dealerID, final Option<Integer> departmentID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDepartmentIncomeResponse response = new QueryDepartmentIncomeResponse();
 		response.setReportName("DepartmentIncomeReport");
 
 		if (monthOfYear.isNone()) {
 			int previousYear = year - 1;
 			final ReportDepartmentDataList reportDetailPreviousYear = getDepartmentReportDataDetail(
 					previousYear, dealerID, departmentID, Option.<Integer> none(),
 					Option.<ReportDepartmentDataList> none(), JournalOp.SUM);
 			reportDetailPreviousYear.setYear(previousYear);
 			response.getDetail().add(reportDetailPreviousYear);
 			final ReportDepartmentDataList reportDetailCurrentYear = getDepartmentReportDataDetail(
 					year, dealerID, departmentID, Option.<Integer> none(),
 					Option.<ReportDepartmentDataList> some(reportDetailPreviousYear),
 					JournalOp.SUM);
 			reportDetailCurrentYear.setYear(year);
 			response.getDetail().add(reportDetailCurrentYear);
 		} else {
 			final ReportDepartmentDataList reportDetailMonthlyAvg = getDepartmentReportDataDetail(
 					year, dealerID, departmentID, monthOfYear,
 					Option.<ReportDepartmentDataList> none(), JournalOp.AVG);
 			response.getDetail().add(reportDetailMonthlyAvg);
 
 			final ReportDepartmentDataList reportDetailCurrentMonth = getDepartmentReportDataDetail(
 					year, dealerID, departmentID, monthOfYear,
 					Option.<ReportDepartmentDataList> some(reportDetailMonthlyAvg),
 					JournalOp.SUM);
 			response.getDetail().add(reportDetailCurrentMonth);
 		}
 		return response;
 	}
 
 	/**
 	 * Query sales report data. 
 	 */
 	@Override
 	@Performance
 	public QueryDealerSalesResponse queryDealerSalesReport(final Integer year,
 			final Option<Integer> monthOfYear,
 			final Option<Integer> departmentID, final Option<Integer> denominator) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final QueryDealerSalesResponse response = new QueryDealerSalesResponse();
 		response.setReportName("SalesReport");
 
 		if (monthOfYear.isNone()) {
 			int previousYear = year - 1;
 			final ReportDealerSalesDataList reportDetailPreviousYear = getDealerSalesReportDataDetail(
 					previousYear, Option.<Integer> none(),
 					Option.<ReportDealerSalesDataList> none(), departmentID,
 					JournalOp.SUM, denominator);
 			reportDetailPreviousYear.setYear(previousYear);
 			response.getDetail().add(reportDetailPreviousYear);
 
 			final ReportDealerSalesDataList reportDetailCurrentYear = getDealerSalesReportDataDetail(
 					year, Option.<Integer> none(),
 					Option.<ReportDealerSalesDataList> some(reportDetailPreviousYear),
 					departmentID, JournalOp.SUM, denominator);
 			reportDetailCurrentYear.setYear(year);
 			response.getDetail().add(reportDetailCurrentYear);
 		} else {
 			final ReportDealerSalesDataList reportDetailMonthlyAvg = getDealerSalesReportDataDetail(
 					year, monthOfYear, Option.<ReportDealerSalesDataList> none(),
 					departmentID, JournalOp.AVG, denominator);
 			response.getDetail().add(reportDetailMonthlyAvg);
 
 			final ReportDealerSalesDataList reportDetailCurrentMonth = getDealerSalesReportDataDetail(
 					year, monthOfYear,
 					Option.<ReportDealerSalesDataList> some(reportDetailMonthlyAvg),
 					departmentID, JournalOp.SUM, denominator);
 			response.getDetail().add(reportDetailCurrentMonth);
 		}
 		return response;
 	}
 
 	@Override
 	public QueryDealerHRAllocationResponse queryDealerHRAllocationReport(
 			final Integer year, final Integer monthOfYear, final Option<Integer> departmentID,
 			final Option<Integer> positionID, final Option<Integer> groupByOption) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		
 		final QueryDealerHRAllocationResponse response = new QueryDealerHRAllocationResponse();
 		response.setReportName("OverallHRAllocationReport");
 		
 		final DealerHRAllocationReportCalculator calculator = new DealerHRAllocationReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 		calculator.withGroupBy(groupByOption);
 		// Get current margin
 		final DealerIncomeFactsQueryBuilder currentQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		currentQueryBuilder.withMonthOfYear(monthOfYear);
 		if ( departmentID.isSome() ) {
 			currentQueryBuilder.withDepartmentID(departmentID.some());
 		} 
 		if ( positionID.isSome() ) {
 			currentQueryBuilder.withPosition(positionID.some());
 		} 
 		final ImmutableListMultimap<Integer, DealerHRAllocationFact> factsMap = Multimaps
 				.index(currentQueryBuilder.queryHRAllocations(),
 						GetDealerIDFromHRAllocFunction.INSTANCE);
 		
 		response.getDetail().add(calculator.calcAllocations(factsMap, refDataDAL, reportDAL).getReportDetail());
 		return response;
 	}
 	
 	@Override
 	public QueryDealerAccountReceivableResponse queryDealerAccountReceivableReport(
 			final Integer year, final Integer monthOfYear, final Option<String> itemName) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		
 		final QueryDealerAccountReceivableResponse response = new QueryDealerAccountReceivableResponse();
 		response.setReportName("AccountReceivableReport");
 		
 		final DealerAccountReceivableReportCalculator calculator = new DealerAccountReceivableReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 		// Get current margin
 		final DealerIncomeFactsQueryBuilder currentQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		currentQueryBuilder.withMonthOfYear(monthOfYear);
 		
 		if ( itemName.isSome() ) {
 			currentQueryBuilder.withItemName(itemName.some());
 		} 
 		final ImmutableListMultimap<Integer, DealerAccountReceivableFact> factsMap = Multimaps
 				.index(currentQueryBuilder.queryAccountReceivables(),
 						GetDealerIDFromAccountReceivableFunction.INSTANCE);
 		
 		response.getDetail().add(calculator.calc(factsMap).getReportDetail());
 		return response;
 	}
 	
 	@Override
 	public QueryDealerAccountReceivableResponse queryDealerAccountReceivablePercentageReport(
 			final Integer year, final Integer monthOfYear) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		
 		final QueryDealerAccountReceivableResponse response = new QueryDealerAccountReceivableResponse();
 		response.setReportName("AccountReceivablePercentageReport");
 		
 		final DealerAccountReceivableReportCalculator calculator = new DealerAccountReceivableReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 		// Get current margin
 		final DealerIncomeFactsQueryBuilder currentQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		currentQueryBuilder.withMonthOfYear(monthOfYear);
 		
 		final ImmutableListMultimap<Integer, DealerAccountReceivableFact> factsMap = Multimaps
 				.index(currentQueryBuilder.queryAccountReceivables(),
 						GetDealerIDFromAccountReceivableFunction.INSTANCE);
 		
 		currentQueryBuilder.clear();
 		currentQueryBuilder.withDuration(1); // 0~30 days
 		final ImmutableListMultimap<Integer, DealerAccountReceivableFact> excludingFactsMap = Multimaps
 				.index(currentQueryBuilder.queryAccountReceivables(),
 						GetDealerIDFromAccountReceivableFunction.INSTANCE);
 		
 		response.getDetail().add(calculator.calcPercentage(excludingFactsMap, factsMap).getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerPostSalesResponse queryDealerPostSalesIncomeReport(
 			final Integer year, final Integer monthOfYear,
 			final Option<Integer> groupBy) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final QueryDealerPostSalesResponse response = new QueryDealerPostSalesResponse();
 		response.setReportName("PostSalesIncomeReport");
 
 		DealerPostSalesIncomeReportCalculator calculator = new DealerPostSalesIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), refDataDAL
 						.getPostSalesDepartments().getItems(), year,
 				monthOfYear);
 		if (groupBy.isSome()) {
 			calculator = calculator.withGroupBy(groupBy);
 		}
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("备件部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.withItemCategory("维修收入").withItemCategory("配件收入")
 				.withItemCategory("钣喷收入").withItemCategory("维修其它收入")
 				.withItemCategory("钣喷其它收入").queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts, refDataDAL).calcMargins(
 				dealerRevenueFacts, refDataDAL);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerPostSalesResponse queryDealerPostSalesExpenseReport(
 			final Integer year, final Integer monthOfYear,
 			final Option<Integer> groupBy, final Option<String> itemCategory) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final QueryDealerPostSalesResponse response = new QueryDealerPostSalesResponse();
 		response.setReportName("PostSalesExpenseReport");
 
 		DealerPostSalesIncomeReportCalculator calculator = new DealerPostSalesIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), refDataDAL
 						.getPostSalesDepartments().getItems(), year,
 				monthOfYear);
 		if (groupBy.isSome()) {
 			calculator = calculator.withGroupBy(groupBy);
 		}
 
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year);
 
 		if (itemCategory.isSome()) {
 			queryBuilder.withItemCategory(itemCategory.some());
 		}
 
 		// Get all expenses
 		final Collection<DealerIncomeExpenseFact> expenseFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("备件部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.queryExpenses();
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 
 		calculator.calcExpenses(dealerExpenseFacts, refDataDAL);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerPostSalesResponse queryDealerPostSalesOpProfitReport(
 			final Integer year, final Integer monthOfYear,
 			final Option<Integer> groupBy) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final QueryDealerPostSalesResponse response = new QueryDealerPostSalesResponse();
 		response.setReportName("PostSalesOpProfitReport");
 
 		DealerPostSalesIncomeReportCalculator calculator = new DealerPostSalesIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), refDataDAL
 						.getPostSalesDepartments().getItems(), year,
 				monthOfYear);
 		if (groupBy.isSome()) {
 			calculator = calculator.withGroupBy(groupBy);
 		}
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("备件部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.withItemCategory("维修收入").withItemCategory("配件收入")
 				.withItemCategory("钣喷收入").withItemCategory("维修其它收入")
 				.withItemCategory("钣喷其它收入").queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		// Get all expenses
 		final Collection<DealerIncomeExpenseFact> expenseFacts = queryBuilder
 				.clear()
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("备件部").getId())
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.queryExpenses();
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts, refDataDAL)
 				.calcMargins(dealerRevenueFacts, refDataDAL)
 				.calcExpenses(dealerExpenseFacts, refDataDAL).calcOpProfit();
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerMaintenanceIncomeResponse queryDealerMaintenanceIncomeReport(
 			final Integer year, final Integer monthOfYear, final String itemName) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		Preconditions.checkNotNull(itemName, "item name can't be null");
 		final QueryDealerMaintenanceIncomeResponse response = new QueryDealerMaintenanceIncomeResponse();
 		response.setReportName("MaintenanceIncomeReport");
 
 		DealerPostSalesDepartmentIncomeReportCalculator calculator = new DealerPostSalesDepartmentIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year).withLessThanMonthOfYear(monthOfYear);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withItemName(itemName).queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		calculator.calcRevenues(dealerRevenueFacts).calcMargins(
 				dealerRevenueFacts);
 
 		if (!itemName.equalsIgnoreCase("外修")
 				&& !itemName.equalsIgnoreCase("延保")
 				&& !itemName.equalsIgnoreCase("其它工时")) {
 			final Collection<DealerEmployeeFeeFact> employeeFeeFacts = queryBuilder
 					.clear()
 					.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 					.withItemName(itemName).queryEmployeeFees();
 			final ImmutableListMultimap<Integer, DealerEmployeeFeeFact> dealerEmployeeFeeFacts = Multimaps
 					.index(employeeFeeFacts,
 							GetDealerIDFromEmployeeFeeFunction.INSTANCE);
 			calculator.calcManHour(dealerRevenueFacts, dealerEmployeeFeeFacts);
 		}
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerMaintenanceIncomeResponse queryDealerMaintenanceWorkOrderReport(
 			Integer year, Integer monthOfYear) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final QueryDealerMaintenanceIncomeResponse response = new QueryDealerMaintenanceIncomeResponse();
 		response.setReportName("MaintenanceWorkOrderReport");
 
 		DealerPostSalesDepartmentIncomeReportCalculator calculator = new DealerPostSalesDepartmentIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year).withLessThanMonthOfYear(monthOfYear);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withItemName("客户付费工时").queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		final Collection<DealerEmployeeFeeFact> employeeFeeFacts = queryBuilder
 				.clear()
 				.withDepartmentID(refDataDAL.getDepartment("维修部").getId())
 				.withItemName("客户付费工时").queryEmployeeFees();
 		final ImmutableListMultimap<Integer, DealerEmployeeFeeFact> dealerEmployeeFeeFacts = Multimaps
 				.index(employeeFeeFacts,
 						GetDealerIDFromEmployeeFeeFunction.INSTANCE);
 		calculator
 				.calcRevenues(dealerRevenueFacts)
 				.calcManHour(dealerRevenueFacts, dealerEmployeeFeeFacts)
 				.calcManHourPerWorkOrder(dealerRevenueFacts,
 						dealerEmployeeFeeFacts);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerSparePartIncomeResponse queryDealerSparePartIncomeReport(
 			final Integer year, final Integer monthOfYear, final String itemName) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		Preconditions.checkNotNull(itemName, "item name can't be null");
 		final QueryDealerSparePartIncomeResponse response = new QueryDealerSparePartIncomeResponse();
 		response.setReportName("SparePartIncomeReport");
 
 		DealerPostSalesDepartmentIncomeReportCalculator calculator = new DealerPostSalesDepartmentIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year).withLessThanMonthOfYear(monthOfYear);
 
 		List<ReportItem> reportItems = new ArrayList<ReportItem>(
 				reportDAL.getReportItem(
 						new ArrayList<String>(Arrays.asList("配件收入")),
 						new ArrayList<String>(Arrays.asList(itemName)),
 						new ArrayList<Integer>()));
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("备件部").getId())
				.withItemID(reportItems.size() > 0 ? reportItems.get(0).getId() : -9999L).queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts).calcMargins(
 				dealerRevenueFacts);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerSheetSprayIncomeResponse queryDealerSheetSprayIncomeReport(
 			final Integer year, final Integer monthOfYear, final String itemName) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		Preconditions.checkNotNull(itemName, "item name can't be null");
 		final QueryDealerSheetSprayIncomeResponse response = new QueryDealerSheetSprayIncomeResponse();
 		response.setReportName("SheetSprayIncomeReport");
 
 		DealerPostSalesDepartmentIncomeReportCalculator calculator = new DealerPostSalesDepartmentIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year).withLessThanMonthOfYear(monthOfYear);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.withItemName(itemName).queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts).calcMargins(
 				dealerRevenueFacts);
 
 		if (!itemName.equalsIgnoreCase("外修")
 				&& !itemName.equalsIgnoreCase("钣喷材料收入")) {
 			final Collection<DealerEmployeeFeeFact> employeeFeeFacts = queryBuilder
 					.clear()
 					.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 					.withItemName(itemName).queryEmployeeFees();
 			final ImmutableListMultimap<Integer, DealerEmployeeFeeFact> dealerEmployeeFeeFacts = Multimaps
 					.index(employeeFeeFacts,
 							GetDealerIDFromEmployeeFeeFunction.INSTANCE);
 			calculator.calcManHour(dealerRevenueFacts, dealerEmployeeFeeFacts);
 		}
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public QueryDealerSheetSprayIncomeResponse queryDealerSheetSprayWorkOrderReport(
 			Integer year, Integer monthOfYear) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final QueryDealerSheetSprayIncomeResponse response = new QueryDealerSheetSprayIncomeResponse();
 		response.setReportName("SheetSprayWorkOrderReport");
 
 		DealerPostSalesDepartmentIncomeReportCalculator calculator = new DealerPostSalesDepartmentIncomeReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 
 		final DealerIncomeFactsQueryBuilder queryBuilder = new DealerIncomeFactsQueryBuilder(
 				reportDAL).withYear(year).withLessThanMonthOfYear(monthOfYear);
 
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.withItemName("客户付费工时").queryRevenues();
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		final Collection<DealerEmployeeFeeFact> employeeFeeFacts = queryBuilder
 				.clear()
 				.withDepartmentID(refDataDAL.getDepartment("钣喷部").getId())
 				.withItemName("客户付费工时").queryEmployeeFees();
 		final ImmutableListMultimap<Integer, DealerEmployeeFeeFact> dealerEmployeeFeeFacts = Multimaps
 				.index(employeeFeeFacts,
 						GetDealerIDFromEmployeeFeeFunction.INSTANCE);
 		calculator
 				.calcRevenues(dealerRevenueFacts)
 				.calcManHour(dealerRevenueFacts, dealerEmployeeFeeFacts)
 				.calcManHourPerWorkOrder(dealerRevenueFacts,
 						dealerEmployeeFeeFacts);
 
 		response.getDetail().add(calculator.getReportDetail());
 		return response;
 	}
 	
 	/** Private functions **/
 	private ReportDepartmentDataList getDepartmentReportDataDetail(final Integer year,
 			final Option<Integer> dealerIDOption,
 			final Option<Integer> departmentIDOption,
 			final Option<Integer> monthOfYearOption,
 			final Option<ReportDepartmentDataList> previousDetailOption,
 			final JournalOp op) {
 		final DepartmentReportCalculator calculator = new DepartmentReportCalculator(
 				refDataDAL.getDepartments().getItems(), year)
 				.withMonth(monthOfYearOption)
 				.withPrevious(previousDetailOption);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL);
 		queryBuilder.withYear(year);
 		if ( monthOfYearOption.isSome() ) {
 			if ( JournalOp.AVG.equals(op) ) {
 				queryBuilder.withLessThanMonthOfYear(monthOfYearOption.some());
 			} else {
 				queryBuilder.withMonthOfYear(monthOfYearOption.some());
 			} 
 		} 
 		if ( departmentIDOption.isSome() ) {
 			queryBuilder.withDepartmentID(departmentIDOption.some());
 		}
 		if ( dealerIDOption.isSome() ) {
 			queryBuilder.withDealerID(dealerIDOption.some());
 		}
 		final Collection<DealerIncomeRevenueFact> revenueFacts =
 				queryBuilder.withItemCategory("新轿车零售")
 								.withItemCategory("新货车零售")
 								.withItemCategory("附加产品业务")
 								.withItemCategory("二手车零售")
 								.withItemCategory("维修收入")
 								.withItemCategory("配件收入")
 								.withItemCategory("钣喷收入")
 								.withItemCategory("新车其它收入")
 								.withItemCategory("二手车其它收入")
 								.withItemCategory("维修其它收入")
 								.withItemCategory("钣喷其它收入")
 								.withItemCategory("租赁收入").queryRevenues();
 		
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts,
 						GetDepartmentIDFromRevenueFunction.INSTANCE);
 		// Get all expenses
 		final Collection<DealerIncomeExpenseFact> expenseFacts =
 				queryBuilder.clear().withItemCategory("变动费用")
 								.withItemCategory("销售费用")
 								.withItemCategory("人工费用")
 								.withItemCategory("半固定费用")
 								.withItemCategory("固定费用").queryExpenses();
 
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts,
 						GetDepartmentIDFromExpenseFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts, op)
 				.calcMargins(dealerRevenueFacts, op)
 				.calcExpenses(dealerExpenseFacts, op).calcOpProfit();
 
 		return calculator.getReportDetail();
 	}
 
 	private ReportDealerSalesDataList getDealerSalesReportDataDetail(final Integer year,
 			final Option<Integer> monthOfYearOption,
 			final Option<ReportDealerSalesDataList> previousDetailOption,
 			final Option<Integer> departmentIDOption, final JournalOp op, final Option<Integer> denominator) {
 		final DealerSalesReportCalculator calculator = new DealerSalesReportCalculator(
 				refDataDAL.getDealers().getItems(), year)
 				.withMonth(monthOfYearOption)
 				.withPrevious(previousDetailOption);
 
 		final DealerIncomeFactsQueryBuilder revenueFactsQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL);
 		revenueFactsQueryBuilder.withYear(year);
 		if ( monthOfYearOption.isSome() ) {
 			if ( JournalOp.AVG.equals(op) ) {
 				revenueFactsQueryBuilder.withLessThanMonthOfYear(monthOfYearOption.some());
 			} else {
 				revenueFactsQueryBuilder.withMonthOfYear(monthOfYearOption.some());
 			} 
 		} 
 		if ( departmentIDOption.isSome() ) {
 			revenueFactsQueryBuilder.withDepartmentID(departmentIDOption.some());
 		}
 		final Collection<DealerIncomeRevenueFact> overallSalesFacts = revenueFactsQueryBuilder.withItemCategory("新轿车零售")
 										.withItemCategory("新货车零售")
 										.withItemCategory("附加产品业务")
 										.withItemCategory("二手车零售")
 										.withItemCategory("新车其它收入")
 										.withItemCategory("二手车其它收入").queryRevenues();
 		
 		final Collection<DealerIncomeRevenueFact> retailFacts = revenueFactsQueryBuilder.clear()
 								.withItemCategory("新轿车零售")
 								.withItemCategory("新货车零售").queryRevenues();
 		
 		final Collection<DealerIncomeRevenueFact> newCarRetailFacts = revenueFactsQueryBuilder.clear()
 								.withItemCategory("新轿车零售").queryRevenues();
 
 		final Collection<DealerIncomeRevenueFact> newVanRetailFacts = revenueFactsQueryBuilder.clear()
 								.withItemCategory("新货车零售").queryRevenues();
 
 		final Collection<DealerIncomeRevenueFact> wholesaleFacts = revenueFactsQueryBuilder.clear()
 								.withItemCategory("新车其它收入")
 								.withItemCategory("二手车其它收入")
 								.withItemID(refDataDAL
 									.getSalesServiceRevenueItem(
 										"大客户采购（租车公司，政府机关）",
 										"新车其它收入").getId().longValue())
 								.withItemID(refDataDAL
 										.getSalesServiceRevenueItem("批发销售",
 										"二手车其它收入").getId().longValue()).queryRevenues();
 
 		final Collection<DealerIncomeRevenueFact> otherFacts = revenueFactsQueryBuilder.clear()
 								.withItemCategory("新车其它收入")
 								.withItemID(refDataDAL
 										.getSalesServiceRevenueItem("他店调车",
 												"新车其它收入").getId().longValue()).queryRevenues();
 
 		calculator
 				.withDenominator(denominator)
 				.calcOverall(
 						Multimaps.index(overallSalesFacts,
 								GetDealerIDFromRevenueFunction.INSTANCE), op)
 				.calcRetail(
 						Multimaps.index(retailFacts,
 								GetDealerIDFromRevenueFunction.INSTANCE), op)
 				.calcNewCarRetail(
 						Multimaps.index(newCarRetailFacts,
 								GetDealerIDFromRevenueFunction.INSTANCE), op)
 				.calcNewVanRetail(
 						Multimaps.index(newVanRetailFacts,
 								GetDealerIDFromRevenueFunction.INSTANCE), op)
 				.calcWholesale(
 						Multimaps.index(wholesaleFacts,
 								GetDealerIDFromRevenueFunction.INSTANCE), op)
 				.calcOther(
 						Multimaps.index(otherFacts,
 								GetDealerIDFromRevenueFunction.INSTANCE), op)
 								.adjustNewCarRetailByDenominator()
 								.adjustNewVanRetailByDenominator()
 								.adjustRetailByDenominator()
 								.adjustOtherByDenominator()
 								.adjustWholesaleByDenominator();
 
 		return calculator.getReportDetail();
 	}
 
 	private ReportDealerDataList getDealerReportDataDetail(final Integer year,
 			final Option<Integer> monthOfYearOption,
 			final Option<ReportDealerDataList> previousDetailOption,
 			final Option<Integer> departmentIDOption,
 			final Option<Integer> denominatorIDOption, final JournalOp op, 
 			final Option<Integer> groupBy) {
 		if ( groupBy.isSome() ) {
 			final DealerIncomeReportCalculator calculator = new DealerIncomeReportCalculator(
 					refDataDAL.getDealers().getItems(), refDataDAL.getDepartments().getItems(), year)
 						.withGroupBy(groupBy);
 
 			// Get all revenues
 			final DealerIncomeFactsQueryBuilder queryBuilder = 
 					new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 			final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder.withItemCategory("新轿车零售")
 									.withItemCategory("新货车零售")
 									.withItemCategory("附加产品业务")
 									.withItemCategory("二手车零售")
 									.withItemCategory("维修收入")
 									.withItemCategory("配件收入")
 									.withItemCategory("钣喷收入")
 									.withItemCategory("新车其它收入")
 									.withItemCategory("二手车其它收入")
 									.withItemCategory("维修其它收入")
 									.withItemCategory("钣喷其它收入")
 									.withItemCategory("租赁收入").queryRevenues();
 
 			final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 					.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 			// Get all expenses
 			final Collection<DealerIncomeExpenseFact> expenseFacts = 
 													queryBuilder.clear() // clear the builder for next query
 																.withItemCategory("变动费用")
 																.withItemCategory("销售费用")
 																.withItemCategory("人工费用")
 																.withItemCategory("半固定费用")
 																.withItemCategory("固定费用")
 																.queryExpenses();
 
 			final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 					.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 
 			calculator.calcRevenues(dealerRevenueFacts, op, refDataDAL)
 					.calcMargins(dealerRevenueFacts, op, refDataDAL)
 					.calcExpenses(dealerExpenseFacts, op, refDataDAL).calcOpProfit();
 
 			return calculator.getReportDetail();
 		}
 		final DealerReportCalculator calculator = new DealerReportCalculator(
 				refDataDAL.getDealers().getItems(), year)
 					.withMonth(monthOfYearOption)
 					.withPrevious(previousDetailOption)
 					.withDenominator(denominatorIDOption);
 
 		// Get all revenues
 		final DealerIncomeFactsQueryBuilder queryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		if ( monthOfYearOption.isSome() ) {
 			if ( JournalOp.AVG.equals(op) ) {
 				queryBuilder.withLessThanMonthOfYear(monthOfYearOption.some());
 			} else {
 				queryBuilder.withMonthOfYear(monthOfYearOption.some());
 			} 
 		} 
 		if ( departmentIDOption.isSome() ) {
 			queryBuilder.withDepartmentID(departmentIDOption.some());
 		}
 		final Collection<DealerIncomeRevenueFact> revenueFacts = queryBuilder.withItemCategory("新轿车零售")
 								.withItemCategory("新货车零售")
 								.withItemCategory("附加产品业务")
 								.withItemCategory("二手车零售")
 								.withItemCategory("维修收入")
 								.withItemCategory("配件收入")
 								.withItemCategory("钣喷收入")
 								.withItemCategory("新车其它收入")
 								.withItemCategory("二手车其它收入")
 								.withItemCategory("维修其它收入")
 								.withItemCategory("钣喷其它收入")
 								.withItemCategory("租赁收入").queryRevenues();
 
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> dealerRevenueFacts = Multimaps
 				.index(revenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		// Get all expenses
 		final Collection<DealerIncomeExpenseFact> expenseFacts = 
 												queryBuilder.clear() // clear the builder for next query
 															.withItemCategory("变动费用")
 															.withItemCategory("销售费用")
 															.withItemCategory("人工费用")
 															.withItemCategory("半固定费用")
 															.withItemCategory("固定费用")
 															.queryExpenses();
 
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 
 		calculator.calcRevenues(dealerRevenueFacts, op)
 				.calcMargins(dealerRevenueFacts, op)
 				.calcExpenses(dealerExpenseFacts, op).calcOpProfit();
 
 		if (departmentIDOption.isNone()) {
 			// calculate net profit
 			final DealerIncomeFactsQueryBuilder otherQueryBuilder = 
 					new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 			if ( monthOfYearOption.isSome() ) {
 				otherQueryBuilder.withMonthOfYear(monthOfYearOption.some());
 			} 
 			final Collection<DealerIncomeRevenueFact> otherRevenueFacts = otherQueryBuilder.withItemCategory("非经营性损益进项")
 									.withItemCategory("非销售类返利").queryRevenues();
 
 			final Collection<DealerIncomeExpenseFact> otherExpenseFacts = otherQueryBuilder.clear()// clear the builder for next query
 								.withItemCategory("非经营性损益削项")
 								.withItemCategory("员工分红").queryExpenses();
 			
 			calculator.calcNetProfit(Multimaps.index(otherRevenueFacts,GetDealerIDFromRevenueFunction.INSTANCE),
 									Multimaps.index(otherExpenseFacts,GetDealerIDFromExpenseFunction.INSTANCE), op);
 		}
 		if (denominatorIDOption.isSome()) {
 			calculator.prepareDenominators()
 					.adjustRevenueByDenominator()
 					.adjustExpenseByDenominator()
 					.adjustMarginByDenominator().adjustNetProfitByDenominator()
 					.adjustOpProfitByDenominator();
 		}
 		return calculator.getReportDetail();
 	}
 	
 	private ReportDealerExpensePercentageDataList getDealerExpensePercentageReportDataDetail(final Integer year,
 			final Integer monthOfYear,
 			final Integer denominatorID, 
 			final Option<String> category, final Option<String> itemName) {
 		final DealerExpensePercentageReportCalculator calculator = new DealerExpensePercentageReportCalculator(
 				refDataDAL.getDealers().getItems(), year, monthOfYear);
 		
 		// Get current margin
 		final DealerIncomeFactsQueryBuilder currentQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year);
 		currentQueryBuilder.withLessThanMonthOfYear(monthOfYear);
 		
 		if ( DealerExpensePercentageReportCalculator.Denominator.valueOf(denominatorID).some()
 				.equals(DealerExpensePercentageReportCalculator.Denominator.MARGIN) ) {
 			currentQueryBuilder.withItemCategory("新轿车零售")
 						.withItemCategory("新货车零售")
 						.withItemCategory("附加产品业务")
 						.withItemCategory("二手车零售")
 						.withItemCategory("维修收入")
 						.withItemCategory("配件收入")
 						.withItemCategory("钣喷收入")
 						.withItemCategory("新车其它收入")
 						.withItemCategory("二手车其它收入")
 						.withItemCategory("维修其它收入")
 						.withItemCategory("钣喷其它收入")
 						.withItemCategory("租赁收入");
 		} else {
 			currentQueryBuilder.withItemCategory("新轿车零售")
 				.withItemCategory("新货车零售")
 				.withItemCategory("二手车零售");
 		}
 		
 		final Collection<DealerIncomeRevenueFact> currentRevenueFacts = currentQueryBuilder.queryRevenues();
 
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> currentDealerRevenueFacts = Multimaps
 				.index(currentRevenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		
 		final DealerIncomeFactsQueryBuilder previousQueryBuilder = 
 				new DealerIncomeFactsQueryBuilder(reportDAL).withYear(year - 1);
 		previousQueryBuilder.withLessThanMonthOfYear(12);
 		
 		if ( DealerExpensePercentageReportCalculator.Denominator.valueOf(denominatorID).some()
 				.equals(DealerExpensePercentageReportCalculator.Denominator.MARGIN) ) {
 			previousQueryBuilder.withItemCategory("新轿车零售")
 						.withItemCategory("新货车零售")
 						.withItemCategory("附加产品业务")
 						.withItemCategory("二手车零售")
 						.withItemCategory("维修收入")
 						.withItemCategory("配件收入")
 						.withItemCategory("钣喷收入")
 						.withItemCategory("新车其它收入")
 						.withItemCategory("二手车其它收入")
 						.withItemCategory("维修其它收入")
 						.withItemCategory("钣喷其它收入")
 						.withItemCategory("租赁收入");
 		} else {
 			previousQueryBuilder.withItemCategory("新轿车零售")
 				.withItemCategory("新货车零售")
 				.withItemCategory("二手车零售");
 		}
 		
 		final Collection<DealerIncomeRevenueFact> previousRevenueFacts = previousQueryBuilder.queryRevenues();
 
 		final ImmutableListMultimap<Integer, DealerIncomeRevenueFact> previousDealerRevenueFacts = Multimaps
 				.index(previousRevenueFacts, GetDealerIDFromRevenueFunction.INSTANCE);
 		
 		// Get all expenses
 		currentQueryBuilder.clear();
 		if ( category.isSome() ) {
 			currentQueryBuilder.withItemCategory(category.some());
 		} 
 		if ( itemName.isSome() ) {
 			currentQueryBuilder.withItemName(itemName.some());
 		} 
 		final Collection<DealerIncomeExpenseFact> expenseFacts = currentQueryBuilder.queryExpenses();
 
 		final ImmutableListMultimap<Integer, DealerIncomeExpenseFact> dealerExpenseFacts = Multimaps
 				.index(expenseFacts, GetDealerIDFromExpenseFunction.INSTANCE);
 
 		calculator.calcExpenses(dealerExpenseFacts)
 				.calcCurrentMargins(currentDealerRevenueFacts)
 				.calcPreviousMargins(previousDealerRevenueFacts);
 		
 		return calculator.getReportDetail();
 	}
 	
 }
