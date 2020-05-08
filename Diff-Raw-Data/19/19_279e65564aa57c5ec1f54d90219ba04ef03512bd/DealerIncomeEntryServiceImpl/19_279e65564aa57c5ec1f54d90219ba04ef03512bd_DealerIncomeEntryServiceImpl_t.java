 package com.jdc.themis.dealer.service.impl;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.time.Instant;
 import javax.time.calendar.LocalDate;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.jdc.themis.dealer.data.dao.IncomeJournalDAO;
 import com.jdc.themis.dealer.domain.AccountReceivableDuration;
 import com.jdc.themis.dealer.domain.DealerEntryItemStatus;
 import com.jdc.themis.dealer.domain.DealerLedgerMetadata;
 import com.jdc.themis.dealer.domain.DealerPostSalesLedger;
 import com.jdc.themis.dealer.domain.DealerVehicleSalesLedger;
 import com.jdc.themis.dealer.domain.EmployeeFee;
 import com.jdc.themis.dealer.domain.EmployeeFeeSummary;
 import com.jdc.themis.dealer.domain.GeneralJournal;
 import com.jdc.themis.dealer.domain.HumanResourceAllocation;
 import com.jdc.themis.dealer.domain.InventoryDuration;
 import com.jdc.themis.dealer.domain.SalesServiceJournal;
 import com.jdc.themis.dealer.domain.TaxJournal;
 import com.jdc.themis.dealer.domain.VehicleSalesJournal;
 import com.jdc.themis.dealer.service.DealerIncomeEntryService;
 import com.jdc.themis.dealer.service.RefDataQueryService;
 import com.jdc.themis.dealer.utils.Performance;
 import com.jdc.themis.dealer.utils.Utils;
 import com.jdc.themis.dealer.web.domain.AccountReceivableDurationDetail;
 import com.jdc.themis.dealer.web.domain.DealerEntryItemStatusDetail;
 import com.jdc.themis.dealer.web.domain.DealerLedgerMetadataDetail;
 import com.jdc.themis.dealer.web.domain.DealerPostSalesLedgerDetail;
 import com.jdc.themis.dealer.web.domain.DealerVehicleSalesLedgerDetail;
 import com.jdc.themis.dealer.web.domain.EmployeeFeeDetail;
 import com.jdc.themis.dealer.web.domain.EmployeeFeeSummaryDetail;
 import com.jdc.themis.dealer.web.domain.GeneralJournalDetail;
 import com.jdc.themis.dealer.web.domain.GeneralJournalItemDetail;
 import com.jdc.themis.dealer.web.domain.GetAccountReceivableDurationResponse;
 import com.jdc.themis.dealer.web.domain.GetDealerEntryItemStatusResponse;
 import com.jdc.themis.dealer.web.domain.GetDealerLedgerMetadataResponse;
 import com.jdc.themis.dealer.web.domain.GetDealerLedgerResponse;
 import com.jdc.themis.dealer.web.domain.GetDealerPostSalesLedgerResponse;
 import com.jdc.themis.dealer.web.domain.GetDealerVehicleSalesLedgerResponse;
 import com.jdc.themis.dealer.web.domain.GetEmployeeFeeResponse;
 import com.jdc.themis.dealer.web.domain.GetEmployeeFeeSummaryResponse;
 import com.jdc.themis.dealer.web.domain.GetGeneralJournalResponse;
 import com.jdc.themis.dealer.web.domain.GetHumanResourceAllocationResponse;
 import com.jdc.themis.dealer.web.domain.GetInventoryDurationResponse;
 import com.jdc.themis.dealer.web.domain.GetSalesServiceJournalResponse;
 import com.jdc.themis.dealer.web.domain.GetTaxResponse;
 import com.jdc.themis.dealer.web.domain.GetVehicleSalesJournalResponse;
 import com.jdc.themis.dealer.web.domain.HumanResourceAllocationDetail;
 import com.jdc.themis.dealer.web.domain.InventoryDurationDetail;
 import com.jdc.themis.dealer.web.domain.SalesServiceJournalDetail;
 import com.jdc.themis.dealer.web.domain.SalesServiceJournalItemDetail;
 import com.jdc.themis.dealer.web.domain.SaveAccountReceivableDurationRequest;
 import com.jdc.themis.dealer.web.domain.SaveDealerEntryItemStatusRequest;
 import com.jdc.themis.dealer.web.domain.SaveDealerPostSalesLedgerRequest;
 import com.jdc.themis.dealer.web.domain.SaveDealerVehicleSalesLedgerRequest;
 import com.jdc.themis.dealer.web.domain.SaveEmployeeFeeRequest;
 import com.jdc.themis.dealer.web.domain.SaveEmployeeFeeSummaryRequest;
 import com.jdc.themis.dealer.web.domain.SaveGeneralJournalRequest;
 import com.jdc.themis.dealer.web.domain.SaveHumanResourceAllocationRequest;
 import com.jdc.themis.dealer.web.domain.SaveInventoryDurationRequest;
 import com.jdc.themis.dealer.web.domain.SaveSalesServiceRevenueRequest;
 import com.jdc.themis.dealer.web.domain.SaveTaxRequest;
 import com.jdc.themis.dealer.web.domain.SaveVehicleSalesJournalRequest;
 import com.jdc.themis.dealer.web.domain.VehicleDetail;
 import com.jdc.themis.dealer.web.domain.VehicleSalesJournalDetail;
 
 import fj.data.Option;
 
 @Service
 public class DealerIncomeEntryServiceImpl implements DealerIncomeEntryService {
 
 	@Autowired
 	private IncomeJournalDAO incomeJournalDAL;
 	@Autowired
 	private RefDataQueryService refDataQueryService;
 
 	public void setRefDataQueryService(RefDataQueryService refDataQueryService) {
 		this.refDataQueryService = refDataQueryService;
 	}
 
 	public void setIncomeJournalDAL(IncomeJournalDAO incomeJournalDAL) {
 		this.incomeJournalDAL = incomeJournalDAL;
 	}
 
 	/**
 	 * Save a list of vehicle sales revenue.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	@Performance
 	@Override
 	public Instant saveVehicleSalesRevenue(
 			final SaveVehicleSalesJournalRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkArgument(request.getDetail().size() != 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 
 		final List<VehicleSalesJournal> journals = Lists.newArrayList();
 		final Integer requestedDeparmentID = request.getDepartmentID() == null ? DEFAULT_VEHICLE_DEPARTMENT_ID
 				: request.getDepartmentID();
 		for (final VehicleSalesJournalDetail detail : request.getDetail()) {
 			final VehicleSalesJournal journal = new VehicleSalesJournal();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setMargin(detail.getMargin() != null ? new BigDecimal(detail.getMargin()) : BigDecimal.ZERO);
 			journal.setCount(detail.getCount() != null ? detail.getCount() : 0);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getVehicleID(),
 					"vehicle id can't be null");
 			journal.setId(refDataQueryService.getVehicle(detail.getVehicleID()).getId());
 			journal.setDepartmentID(requestedDeparmentID);
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveVehicleSalesJournal(
 				request.getDealerID(), requestedDeparmentID, journals);
 		return timestamp;
 	}
 
 	private final static Integer DEFAULT_VEHICLE_DEPARTMENT_ID = 1; // new
 																	// vehicle
 																	// department
 
 	/**
 	 * Get a list of vehicle sales revenue per vehicle type.
 	 * 
 	 * @param dealerID
 	 *            Dealer company id
 	 * @param validDate
 	 *            Date to query report
 	 * @return
 	 */
 	@Performance
 	@Override
 	public GetVehicleSalesJournalResponse getVehicleSalesRevenue(
 			final Integer dealerID, final Option<Integer> departmentID, final String validDate, final Option<Integer> categoryID) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		if ( departmentID.isSome() ) {
 			Preconditions.checkArgument(
 				refDataQueryService.getDepartment(departmentID.some()) != null,
 				"unknown department id " + departmentID);
 		}
 		final GetVehicleSalesJournalResponse response = new GetVehicleSalesJournalResponse();
 		final Integer requestedDeparmentID = departmentID.isNone() ? DEFAULT_VEHICLE_DEPARTMENT_ID
 				: departmentID.some();
 		final Collection<VehicleSalesJournal> list = incomeJournalDAL
 				.getVehicleSalesJournal(dealerID, requestedDeparmentID,
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setDepartmentID(requestedDeparmentID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final VehicleSalesJournal journal : list) {
 			final VehicleDetail vehicle = refDataQueryService.getVehicle(journal.getId());
 			if ( categoryID.isSome() && !vehicle.getCategoryID().equals(categoryID.some()) ) {
 				continue;
 			}
 			final VehicleSalesJournalDetail item = new VehicleSalesJournalDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setCount(journal.getCount());
 			item.setMargin(journal.getMargin().doubleValue());
 			item.setName(vehicle.getName());
 			item.setVehicleID(vehicle.getId());
 			item.setCategoryID(vehicle.getCategoryID());
 			item.setCategory(vehicle.getCategory());
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 		return response;
 	}
 
 	/**
 	 * Save a list of sales & service revenue.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	@Performance
 	@Override
 	public Instant saveSalesServiceRevenue(
 			final SaveSalesServiceRevenueRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getDepartmentID(),
 				"department id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(request.getDepartmentID()) != null,
 				"unknown department id " + request.getDepartmentID());
 		
 		final List<SalesServiceJournal> journals = Lists.newArrayList();
 		for (final SalesServiceJournalDetail detail : request.getDetail()) {
 			final SalesServiceJournal journal = new SalesServiceJournal();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getSalesServiceRevenueItem(detail.getItemID()).getId());
 			journal.setMargin(detail.getMargin() != null && !"Expense".equals(refDataQueryService.getSalesServiceRevenueItem(detail.getItemID()).getJournalType()) ? new BigDecimal(detail.getMargin()) : BigDecimal.ZERO);
 			journal.setCount(detail.getCount() != null && !"Expense".equals(refDataQueryService.getSalesServiceRevenueItem(detail.getItemID()).getJournalType()) ? detail.getCount() : 0);			
 			journal.setDepartmentID(request.getDepartmentID());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveSalesServiceJournal(
 				request.getDealerID(), request.getDepartmentID(), journals);
 		return timestamp;
 	}
 
 	/**
 	 * Get a list sales & service revenue per sales item.
 	 * 
 	 * @param dealerID
 	 *            Dealer company id
 	 * @param departmentID
 	 *            Department id
 	 * @param validDate
 	 *            Date to query report
 	 * @return
 	 */
 	@Override
 	@Performance
 	public GetSalesServiceJournalResponse getSalesServiceRevenue(
 			final Integer dealerID, final Integer departmentID, final String validDate, final Option<Integer> categoryID) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(departmentID, "department id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(departmentID) != null,
 				"unknown department id " + departmentID);
 		
 		final GetSalesServiceJournalResponse response = new GetSalesServiceJournalResponse();
 		final Collection<SalesServiceJournal> list = incomeJournalDAL
 				.getSalesServiceJournal(dealerID, departmentID,
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setDepartmentID(departmentID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final SalesServiceJournal journal : list) {
 			final SalesServiceJournalItemDetail itemDetail = refDataQueryService.getSalesServiceRevenueItem(journal.getId());
 			if ( categoryID.isSome() && !itemDetail.getCategoryID().equals(categoryID.some()) ) {
 				continue;
 			}
 			final SalesServiceJournalDetail item = new SalesServiceJournalDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setCount(journal.getCount());
 			item.setMargin(journal.getMargin().doubleValue());
 			item.setName(itemDetail.getName());
 			item.setItemID(itemDetail.getId());
 			item.setCategory(itemDetail.getCategory());
 			item.setCategoryID(itemDetail.getCategoryID());
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 
 	/**
 	 * Save income tax.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	@Override
 	@Performance
 	public Instant saveIncomeTax(final SaveTaxRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions
 				.checkNotNull(request.getTax(), "tax amount can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 
 		final List<TaxJournal> journals = Lists.newArrayList();
 		final TaxJournal taxJournal = new TaxJournal();
 		taxJournal.setAmount(new BigDecimal(request.getTax()));
 		taxJournal.setDealerID(request.getDealerID());
 		taxJournal.setUpdatedBy(request.getUpdateBy());
 		taxJournal.setValidDate(LocalDate.parse(request.getValidDate()));
 		journals.add(taxJournal);
 		final Instant timestamp = incomeJournalDAL.saveTaxJournal(
 				request.getDealerID(), journals);
 		return timestamp;
 	}
 
 	/**
 	 * Get a list sales & service revenue per sales item.
 	 * 
 	 * @param dealerID
 	 *            Dealer company id
 	 * @param departmentID
 	 *            Department id
 	 * @param validDate
 	 *            Date to query report
 	 * @return
 	 */
 	@Override
 	@Performance
 	public GetTaxResponse getIncomeTax(final Integer dealerID, final String validDate) {
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		
 		final GetTaxResponse response = new GetTaxResponse();
 		final Collection<TaxJournal> list = incomeJournalDAL.getTaxJournal(
 				dealerID, LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final TaxJournal journal : list) {
 			response.setTax(journal.getAmount().doubleValue());
 		}
 		return response;
 	}
 
 	/**
 	 * Save dealer entry item status.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	@Override
 	@Performance
 	public Instant saveDealerEntryItemStatus(
 			final SaveDealerEntryItemStatusRequest request) {
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 
 		final List<DealerEntryItemStatus> journals = Lists.newArrayList();
 		final DealerEntryItemStatus journal = new DealerEntryItemStatus();
 		Preconditions.checkArgument(refDataQueryService.getMenu(request.getItemID()) != null, "unknown item id");
 		journal.setEntryItemID(request.getItemID());
 		journal.setDealerID(request.getDealerID());
 		journal.setUpdateBy(request.getUpdateBy());
 		journal.setValidDate(LocalDate.parse(request.getValidDate()));
 		journals.add(journal);
 		final Instant timestamp = incomeJournalDAL.saveDealerEntryItemStatus(
 				request.getDealerID(), journals);
 		return timestamp;
 	}
 
 	/**
 	 * Get dealer entry item status.
 	 * 
 	 * @param dealerID
 	 *            Dealer company id
 	 * @param validDate
 	 *            Date to query report
 	 * @return
 	 */
 	@Override
 	@Performance
 	public GetDealerEntryItemStatusResponse getDealerEntryItemStatus(
 			final Integer dealerID, final String validDate) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 
 		final GetDealerEntryItemStatusResponse response = new GetDealerEntryItemStatusResponse();
 		final Collection<DealerEntryItemStatus> list = incomeJournalDAL
 				.getDealerEntryItemStatus(dealerID, LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final DealerEntryItemStatus journal : list) {
 			final DealerEntryItemStatusDetail status = new DealerEntryItemStatusDetail();
 			status.setId(journal.getEntryItemID());
 			status.setName(refDataQueryService.getMenu(status.getId()).getName());
 			status.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(status);
 		}
 		return response;
 	}
 
 	@Override
 	@Performance
 	public Instant saveGeneralIncome(final SaveGeneralJournalRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getDepartmentID(),
 				"department id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(request.getDepartmentID()) != null,
 				"unknown department id " + request.getDepartmentID());
 
 		
 		final List<GeneralJournal> journals = Lists.newArrayList();
 		for (final GeneralJournalDetail detail : request.getDetail()) {
 			final GeneralJournal journal = new GeneralJournal();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getGeneralIncomeItem(detail.getItemID()).getId());
 			journal.setDepartmentID(request.getDepartmentID());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveGeneralJournal(
 				request.getDealerID(), request.getDepartmentID(), journals);
 		return timestamp;
 	}
 
 	@Override
 	@Performance
 	public GetGeneralJournalResponse getGeneralIncome(final Integer dealerID,
 			final Integer departmentID, final String validDate, final Option<Integer> categoryID) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(departmentID, "department id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(departmentID) != null,
 				"unknown department id " + departmentID);
 		
 		final GetGeneralJournalResponse response = new GetGeneralJournalResponse();
 		final Collection<GeneralJournal> list = incomeJournalDAL
 				.getGeneralJournal(dealerID, departmentID,
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setDepartmentID(departmentID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final GeneralJournal journal : list) {
 			final GeneralJournalItemDetail itemDetail = refDataQueryService.getGeneralIncomeItem(journal.getId());
 			if ( categoryID.isSome() && !itemDetail.getCategoryID().equals(categoryID.some()) ) {
 				continue;
 			}
 			final GeneralJournalDetail item = new GeneralJournalDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setName(itemDetail.getName());
 			item.setItemID(itemDetail.getId());
 			item.setCategory(itemDetail.getCategory());
 			item.setCategoryID(itemDetail.getCategoryID());
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 
 	@Override
 	@Performance
 	public Instant saveAccountReceivableDuration (
 			final SaveAccountReceivableDurationRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		
 		final List<AccountReceivableDuration> journals = Lists.newArrayList();
 		for (final AccountReceivableDurationDetail detail : request.getDetail()) {
 			final AccountReceivableDuration journal = new AccountReceivableDuration();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getAccountReceivableDurationItem(detail.getItemID()).getId());
 			journal.setDurationID(refDataQueryService.getDuration(detail.getDurationID()).getId());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveAccountReceivableDuration(
 				request.getDealerID(), journals);
 		return timestamp;
 	}
 
 	@Override
 	@Performance
 	public GetAccountReceivableDurationResponse getAccountReceivableDuration(
 			final Integer dealerID, final String validDate) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 
 		final GetAccountReceivableDurationResponse response = new GetAccountReceivableDurationResponse();
 		final Collection<AccountReceivableDuration> list = incomeJournalDAL
 				.getAccountReceivableDuration(dealerID, 
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final AccountReceivableDuration journal : list) {
 			final AccountReceivableDurationDetail item = new AccountReceivableDurationDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setName(refDataQueryService.getAccountReceivableDurationItem(journal.getId()).getName());
 			item.setItemID(journal.getId());
 			item.setDurationID(journal.getDurationID());
 			item.setDurationDesc(Utils.getDurationDesc(refDataQueryService.getDuration(journal.getDurationID()), refDataQueryService));
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 
 	@Override
 	@Performance
 	public Instant saveInventoryDuration(final SaveInventoryDurationRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(request.getDepartmentID()) != null,
 				"unknown department id " + request.getDepartmentID());
 		
 		final List<InventoryDuration> journals = Lists.newArrayList();
 		for (final InventoryDurationDetail detail : request.getDetail()) {
 			final InventoryDuration journal = new InventoryDuration();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getInventoryDurationItem(detail.getItemID()).getId());
 			journal.setDepartmentID(request.getDepartmentID());
 			journal.setDurationID(refDataQueryService.getDuration(detail.getDurationID()).getId());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveInventoryDuration(
 				request.getDealerID(), request.getDepartmentID(), journals);
 		return timestamp;
 	}
 
 	@Override
 	@Performance
 	public GetInventoryDurationResponse getInventoryDuration(final Integer dealerID,
 			final Integer departmentID, final String validDate) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(departmentID) != null,
 				"unknown department id " + departmentID);
 		
 		final GetInventoryDurationResponse response = new GetInventoryDurationResponse();
 		final Collection<InventoryDuration> list = incomeJournalDAL
 				.getInventoryDuration(dealerID, departmentID,
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setDeparmentID(departmentID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final InventoryDuration journal : list) {
 			final InventoryDurationDetail item = new InventoryDurationDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setName(refDataQueryService.getInventoryDurationItem(journal.getId()).getName());
 			item.setItemID(journal.getId());
 			item.setDurationID(journal.getDurationID());
 			item.setDurationDesc(Utils.getDurationDesc(refDataQueryService.getDuration(journal.getDurationID()), refDataQueryService));
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 
 	@Override
 	@Performance
 	public Instant saveEmployeeFee(final SaveEmployeeFeeRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(request.getDepartmentID()) != null,
 				"unknown department id " + request.getDepartmentID());
 		
 		final List<EmployeeFee> journals = Lists.newArrayList();
 		for (final EmployeeFeeDetail detail : request.getDetail()) {
 			final EmployeeFee journal = new EmployeeFee();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getEmployeeFeeItem(detail.getItemID()).getId());
 			journal.setFeeTypeID(refDataQueryService.getEnumValue("FeeType", detail.getFeeTypeID()).getValue());
 			journal.setDepartmentID(request.getDepartmentID());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveEmployeeFee(
 				request.getDealerID(), request.getDepartmentID(), journals);
 		return timestamp;
 	}
 
 	@Override
 	@Performance
 	public GetEmployeeFeeResponse getEmployeeFee(Integer dealerID,
 			final Integer departmentID, final String validDate) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(departmentID) != null,
 				"unknown department id " + departmentID);
 		
 		final GetEmployeeFeeResponse response = new GetEmployeeFeeResponse();
 		final Collection<EmployeeFee> list = incomeJournalDAL
 				.getEmployeeFee(dealerID, departmentID,
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setDepartmentID(departmentID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final EmployeeFee journal : list) {
 			final EmployeeFeeDetail item = new EmployeeFeeDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setName(refDataQueryService.getEmployeeFeeItem(journal.getId()).getName());
 			item.setItemID(journal.getId());
 			item.setFeeTypeID(journal.getFeeTypeID());
 			item.setFeeType(refDataQueryService.getEnumValue("FeeType", journal.getFeeTypeID()).getName());
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 
 	@Override
 	@Performance
 	public Instant saveEmployeeFeeSummary(final SaveEmployeeFeeSummaryRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(request.getDepartmentID()) != null,
 				"unknown department id " + request.getDepartmentID());
 		
 		final List<EmployeeFeeSummary> journals = Lists.newArrayList();
 		for (final EmployeeFeeSummaryDetail detail : request.getDetail()) {
 			final EmployeeFeeSummary journal = new EmployeeFeeSummary();
 			journal.setAmount(detail.getAmount() != null ? new BigDecimal(detail.getAmount()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getEmployeeFeeSummaryItem(detail.getItemID()).getId());
 			journal.setDepartmentID(request.getDepartmentID());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveEmployeeFeeSummary(
 				request.getDealerID(), request.getDepartmentID(), journals);
 		return timestamp;
 	}
 
 	@Override
 	@Performance
 	public GetEmployeeFeeSummaryResponse getEmployeeFeeSummary(
 			final Integer dealerID, final Integer departmentID, final String validDate) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(departmentID, "department id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(departmentID) != null,
 				"unknown department id " + departmentID);
 		
 		final GetEmployeeFeeSummaryResponse response = new GetEmployeeFeeSummaryResponse();
 		final Collection<EmployeeFeeSummary> list = incomeJournalDAL
 				.getEmployeeFeeSummary(dealerID, departmentID,
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setDepartmentID(departmentID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final EmployeeFeeSummary journal : list) {
 			final EmployeeFeeSummaryDetail item = new EmployeeFeeSummaryDetail();
 			item.setAmount(journal.getAmount().doubleValue());
 			item.setName(refDataQueryService.getEmployeeFeeSummaryItem(journal.getId()).getName());
 			item.setItemID(journal.getId());
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 
 	@Override
 	@Performance
 	public Instant saveHumanResourceAllocation(
 			final SaveHumanResourceAllocationRequest request) {
 		Preconditions.checkNotNull(request.getDealerID(),
 				"dealer id can't be null");
 		Preconditions.checkNotNull(request.getValidDate(),
 				"valid date can't be null");
 		Preconditions.checkNotNull(request.getDetail().size() == 0,
 				"no detail is posted");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(request.getDealerID()) != null,
 				"unknown dealer id " + request.getDealerID());
 		Preconditions.checkArgument(
 				refDataQueryService.getDepartment(request.getDepartmentID()) != null,
 				"unknown department id " + request.getDepartmentID());
 		
 		final List<HumanResourceAllocation> journals = Lists.newArrayList();
 		for (final HumanResourceAllocationDetail detail : request.getDetail()) {
 			final HumanResourceAllocation journal = new HumanResourceAllocation();
 			journal.setAllocation(detail.getAllocation() != null ? new BigDecimal(detail.getAllocation()) : BigDecimal.ZERO);
 			journal.setDealerID(request.getDealerID());
 			Preconditions.checkNotNull(detail.getItemID(),
 					"item id can't be null");
 			journal.setId(refDataQueryService.getHumanResourceAllocationItem(detail.getItemID()).getId());
 			journal.setDepartmentID(request.getDepartmentID());
 			journal.setUpdatedBy(request.getUpdateBy());
 			journal.setValidDate(LocalDate.parse(request.getValidDate()));
 			journals.add(journal);
 		}
 		final Instant timestamp = incomeJournalDAL.saveHumanResourceAllocation(
 				request.getDealerID(), request.getDepartmentID(), journals);
 
 		return timestamp;
 	}
 
 	@Override
 	@Performance
 	public GetHumanResourceAllocationResponse getHumanResourceAllocation(
 			final Integer dealerID, final String validDate) {
 		Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 		Preconditions.checkNotNull(validDate, "valid date can't be null");
 		Preconditions.checkArgument(
 				refDataQueryService.getDealer(dealerID) != null,
 				"unknown dealer id " + dealerID);
 		
 		final GetHumanResourceAllocationResponse response = new GetHumanResourceAllocationResponse();
 		final Collection<HumanResourceAllocation> list = incomeJournalDAL
 				.getHumanResourceAllocation(dealerID, 
 						LocalDate.parse(validDate));
 
 		response.setDealerID(dealerID);
 		response.setValidDate(LocalDate.parse(validDate));
 		for (final HumanResourceAllocation journal : list) {
 			final HumanResourceAllocationDetail item = new HumanResourceAllocationDetail();
 			item.setAllocation(journal.getAllocation().doubleValue());
 			item.setName(refDataQueryService.getHumanResourceAllocationItem(journal.getId()).getName());
 			item.setItemID(journal.getId());
 			item.setDepartmentID(journal.getDepartmentID());
 			item.setTimestamp(journal.getTimestamp());
 			response.getDetail().add(item);
 		}
 
 		return response;
 	}
 	
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * getDealerLedgerMetadata(java.lang.Integer)
      */
     @Override
     public GetDealerLedgerMetadataResponse getDealerLedgerMetadata(final Integer dealerID) {
         Preconditions.checkNotNull(dealerID, "dealer id can't be null");
 
         final GetDealerLedgerMetadataResponse response = new GetDealerLedgerMetadataResponse();
         final Collection<DealerLedgerMetadata> vehicleSalesLedgerMetadataList = incomeJournalDAL
                 .getDealerLedgerMetadata(dealerID, 1);
         metadataToDetail(response.getVehicleSalesLedger(), vehicleSalesLedgerMetadataList);
         final Collection<DealerLedgerMetadata> postSalesLedgerMetadataList = incomeJournalDAL.getDealerLedgerMetadata(
                 dealerID, 2);
         metadataToDetail(response.getPostSalesLedger(), postSalesLedgerMetadataList);
         response.setDealerID(dealerID);
 
         return response;
     }
 
     private void metadataToDetail(final List<DealerLedgerMetadataDetail> detailList,
             final Collection<DealerLedgerMetadata> metadataList) {
         for (final DealerLedgerMetadata metadata : metadataList) {
             final DealerLedgerMetadataDetail item = new DealerLedgerMetadataDetail();
             item.setName(metadata.getName());
             item.setDisplayName(metadata.getDisplayName());
             item.setType(metadata.getType());
             if ("select".equalsIgnoreCase(metadata.getType()) || "multiselect".equalsIgnoreCase(metadata.getType())) {
                 if (metadata.getOptions() != null) {
                     List<String> options = Lists.newArrayList(metadata.getOptions().split(","));
                     item.setOptions(options);
                 } else {
                     item.setOptions(null);
                 }
             } else {
                 item.setOptions(null);
             }
             detailList.add(item);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * saveDealerVehicleSalesLedger
      * (com.jdc.themis.dealer.web.domain.SaveDealerVehicleSalesLedgerRequest)
      */
     @Override
     public Instant saveDealerVehicleSalesLedger(final SaveDealerVehicleSalesLedgerRequest request) {
         Preconditions.checkNotNull(request.getContractNo(), "contract number can't be null");
         Preconditions.checkNotNull(request.getDealerID(), "dealer id can't be null");
         Preconditions.checkNotNull(request.getValidDate(), "valid date can't be null");
         final DealerVehicleSalesLedger ledger = new DealerVehicleSalesLedger();
         ledger.setContractNo(request.getContractNo());
         ledger.setDealerID(request.getDealerID());
         ledger.setModel(request.getModel());
         ledger.setType(request.getType());
         ledger.setColor(request.getColor());
         ledger.setLpNumber(request.getLpNumber());
         ledger.setFrameNo(request.getFrameNo());
         if (request.getManufacturerDebitDate() != null) {
             ledger.setManufacturerDebitDate(LocalDate.parse(request.getManufacturerDebitDate()));
         }
         if (request.getWarehousingDate() != null) {
             ledger.setWarehousingDate(LocalDate.parse(request.getWarehousingDate()));
         }
         if (request.getSalesDate() != null) {
             ledger.setSalesDate(LocalDate.parse(request.getSalesDate()));
         }
         ledger.setGuidingPrice(request.getGuidingPrice() != null ? request.getGuidingPrice() : Double.valueOf(0));
         ledger.setCustomerName(request.getCustomerName());
         ledger.setIdentificationNo(request.getIdentificationNo());
         ledger.setSalesConsultant(request.getSalesConsultant());
         ledger.setCustomerType(request.getCustomerType());
         ledger.setVehicleInvoiceAmount(request.getVehicleInvoiceAmount() != null ? request.getVehicleInvoiceAmount()
                 : Double.valueOf(0));
         ledger.setAccessoryInvoiceAmount(request.getAccessoryInvoiceAmount() != null ? request
                 .getAccessoryInvoiceAmount() : Double.valueOf(0));
         ledger.setVehicleMarkup(request.getVehicleMarkup() != null ? request.getVehicleMarkup() : Double.valueOf(0));
         ledger.setInvoiceType(request.getInvoiceType());
         if (request.getInvoiceDate() != null) {
             ledger.setInvoiceDate(LocalDate.parse(request.getInvoiceDate()));
         }
         ledger.setPosBank(request.getPosBank());
         ledger.setPosCharge(request.getPosCharge() != null ? request.getPosCharge() : Double.valueOf(0));
         ledger.setCash(request.getCash() != null ? request.getCash() : Double.valueOf(0));
         ledger.setCheque(request.getCheque() != null ? request.getCheque() : Double.valueOf(0));
         ledger.setTransfer(request.getTransfer() != null ? request.getTransfer() : Double.valueOf(0));
         ledger.setPaidSubscription(request.getPaidSubscription() != null ? request.getPaidSubscription() : Double
                 .valueOf(0));
         ledger.setPaidRemainder(request.getPaidRemainder() != null ? request.getPaidRemainder() : Double.valueOf(0));
         ledger.setRefund(request.getRefund() != null ? request.getRefund() : Double.valueOf(0));
         ledger.setVehicleSalesRevenue(request.getVehicleSalesRevenue() != null ? request.getVehicleSalesRevenue()
                 : Double.valueOf(0));
         ledger.setVehicleSalesCost(request.getVehicleSalesCost() != null ? request.getVehicleSalesCost() : Double
                 .valueOf(0));
         ledger.setUsedVehicleCharge(request.getUsedVehicleCharge() != null ? request.getUsedVehicleCharge() : Double
                 .valueOf(0));
         ledger.setUsedVehicleCost(request.getUsedVehicleCost() != null ? request.getUsedVehicleCost() : Double
                 .valueOf(0));
         ledger.setUsedVehicleSellingPrice(request.getUsedVehicleSellingPrice() != null ? request
                 .getUsedVehicleSellingPrice() : Double.valueOf(0));
         ledger.setDistributor(request.getDistributor());
         ledger.setUsedVehicleManufacturerSubsidy(request.getUsedVehicleManufacturerSubsidy() != null ? request
                 .getUsedVehicleManufacturerSubsidy() : Double.valueOf(0));
         ledger.setAccessorySalesRevenue(request.getAccessorySalesRevenue() != null ? request.getAccessorySalesRevenue()
                 : Double.valueOf(0));
         ledger.setAccessorySalesCostBuyerPart(request.getAccessorySalesCostBuyerPart() != null ? request
                 .getAccessorySalesCostBuyerPart() : Double.valueOf(0));
         ledger.setAccessorySalesCostGiftPart(request.getAccessorySalesCostGiftPart() != null ? request
                 .getAccessorySalesCostGiftPart() : Double.valueOf(0));
         ledger.setInsuranceRebateMargin(request.getInsuranceRebateMargin() != null ? request.getInsuranceRebateMargin()
                 : Double.valueOf(0));
         ledger.setInsuranceCost(request.getInsuranceCost() != null ? request.getInsuranceCost() : Double.valueOf(0));
         ledger.setInsuranceAgengcy(request.getInsuranceAgengcy());
         ledger.setLpRevenue(request.getLpRevenue() != null ? request.getLpRevenue() : Double.valueOf(0));
         ledger.setLpCost(request.getLpCost() != null ? request.getLpCost() : Double.valueOf(0));
         ledger.setInstallmentCharge(request.getInstallmentCharge() != null ? request.getInstallmentCharge() : Double
                 .valueOf(0));
         ledger.setFcCommission(request.getFcCommission() != null ? request.getFcCommission() : Double.valueOf(0));
         ledger.setInstallmentManufacturerSubsidy(request.getInstallmentManufacturerSubsidy() != null ? request
                 .getInstallmentManufacturerSubsidy() : Double.valueOf(0));
         ledger.setInstallmentOthers(request.getInstallmentOthers() != null ? request.getInstallmentOthers() : Double
                 .valueOf(0));
         ledger.setInstallmentAgency(request.getInstallmentAgency());
         ledger.setExtendedWarrantyRevenue(request.getExtendedWarrantyRevenue() != null ? request
                 .getExtendedWarrantyRevenue() : Double.valueOf(0));
         ledger.setExtendedWarrantyCost(request.getExtendedWarrantyCost() != null ? request.getExtendedWarrantyCost()
                 : Double.valueOf(0));
         ledger.setFinancialLeasingMargin(request.getFinancialLeasingMargin() != null ? request
                 .getFinancialLeasingMargin() : Double.valueOf(0));
         ledger.setExitWarehouseMargin(request.getExitWarehouseMargin() != null ? request.getExitWarehouseMargin()
                 : Double.valueOf(0));
         ledger.setInspectionMargin(request.getInspectionMargin() != null ? request.getInspectionMargin() : Double
                 .valueOf(0));
         ledger.setMemberCardRevenue(request.getMemberCardRevenue() != null ? request.getMemberCardRevenue() : Double
                 .valueOf(0));
         ledger.setMemberCardCost(request.getMemberCardCost() != null ? request.getMemberCardCost() : Double.valueOf(0));
         ledger.setOtherMargin(request.getOtherMargin() != null ? request.getOtherMargin() : Double.valueOf(0));
         ledger.setPostSalesVoucherManHourPart(request.getPostSalesVoucherManHourPart() != null ? request
                 .getPostSalesVoucherManHourPart() : Double.valueOf(0));
         ledger.setPostSalesVoucherDecorationPart(request.getPostSalesVoucherDecorationPart() != null ? request
                 .getPostSalesVoucherDecorationPart() : Double.valueOf(0));
         ledger.setPostSalesVoucherAccessoryPart(request.getPostSalesVoucherAccessoryPart() != null ? request
                 .getPostSalesVoucherAccessoryPart() : Double.valueOf(0));
         ledger.setFuelCard(request.getFuelCard() != null ? request.getFuelCard() : Double.valueOf(0));
         ledger.setGasoline(request.getGasoline() != null ? request.getGasoline() : Double.valueOf(0));
         ledger.setOtherGift(request.getOtherGift() != null ? request.getOtherGift() : Double.valueOf(0));
         ledger.setInventoryDays(request.getInventoryDays() != null ? request.getInventoryDays() : Double.valueOf(0));
         ledger.setFinancingRate(request.getFinancingRate() != null ? request.getFinancingRate() : Double.valueOf(0));
         ledger.setManagementFee(request.getManagementFee() != null ? request.getManagementFee() : Double.valueOf(0));
         ledger.setMonthlyRebate(request.getMonthlyRebate() != null ? request.getMonthlyRebate() : Double.valueOf(0));
         ledger.setQuarterlyRebate(request.getQuarterlyRebate() != null ? request.getQuarterlyRebate() : Double
                 .valueOf(0));
         ledger.setSatisfactionRebate(request.getSatisfactionRebate() != null ? request.getSatisfactionRebate() : Double
                 .valueOf(0));
         ledger.setPickupRebate(request.getPickupRebate() != null ? request.getPickupRebate() : Double.valueOf(0));
         ledger.setBiddingSubsidy(request.getBiddingSubsidy() != null ? request.getBiddingSubsidy() : Double.valueOf(0));
         ledger.setTerminalSalesBonus(request.getTerminalSalesBonus() != null ? request.getTerminalSalesBonus() : Double
                 .valueOf(0));
         ledger.setOtherRebate(request.getOtherRebate() != null ? request.getOtherRebate() : Double.valueOf(0));
         ledger.setRebateAdjustment(request.getRebateAdjustment() != null ? request.getRebateAdjustment() : Double
                 .valueOf(0));
         ledger.setVehiclePushMoney(request.getVehiclePushMoney() != null ? request.getVehiclePushMoney() : Double
                 .valueOf(0));
         ledger.setAccessoryPushMoney(request.getAccessoryPushMoney() != null ? request.getAccessoryPushMoney() : Double
                 .valueOf(0));
         ledger.setInsurancePushMoney(request.getInsurancePushMoney() != null ? request.getInsurancePushMoney() : Double
                 .valueOf(0));
         ledger.setLpPushMoney(request.getLpPushMoney() != null ? request.getLpPushMoney() : Double.valueOf(0));
         ledger.setInstallmentPushMoney(request.getInstallmentPushMoney() != null ? request.getInstallmentPushMoney()
                 : Double.valueOf(0));
         ledger.setExtendedWarrantyPushMoney(request.getExtendedWarrantyPushMoney() != null ? request
                 .getExtendedWarrantyPushMoney() : Double.valueOf(0));
         ledger.setFinancialLeasingPushMoney(request.getFinancialLeasingPushMoney() != null ? request
                 .getFinancialLeasingPushMoney() : Double.valueOf(0));
         ledger.setMemberCardPushMoney(request.getMemberCardPushMoney() != null ? request.getMemberCardPushMoney()
                 : Double.valueOf(0));
         ledger.setExchangePushMoney(request.getExchangePushMoney() != null ? request.getExchangePushMoney() : Double
                 .valueOf(0));
         ledger.setOtherPushMoney(request.getOtherPushMoney() != null ? request.getOtherPushMoney() : Double.valueOf(0));
         ledger.setPushMoneyDeduction(request.getPushMoneyDeduction() != null ? request.getPushMoneyDeduction() : Double
                 .valueOf(0));
 
         ledger.setValidDate(LocalDate.parse(request.getValidDate()));
         ledger.setUpdatedBy(request.getUpdatedBy());
 
         final Instant timestamp = incomeJournalDAL.saveDealerVehicleSalesLedger(ledger);
         return timestamp;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * queryDealerVehicleSalesLedger(java.lang.Integer, java.lang.String,
      * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
      * java.lang.String, java.lang.String, java.lang.String, java.lang.Double,
      * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
      * java.lang.Integer, java.lang.Integer, java.lang.Integer)
      */
     @Override
     public GetDealerLedgerResponse queryDealerVehicleSalesLedger(Integer contractNo, String model, String type,
             String color, String lpNumber, String frameNo, String manufacturerDebitDate, String warehousingDate,
             String salesDate, Double guidingPrice, String customerName, String identificationNo,
             String salesConsultant, String customerType, Integer dealerID, Integer marker, Integer limit) {
         final GetDealerLedgerResponse response = new GetDealerLedgerResponse();
         Collection<DealerVehicleSalesLedger> list = incomeJournalDAL.queryDealerVehicleSalesLedger(contractNo, model,
                 type, color, lpNumber, frameNo, manufacturerDebitDate, warehousingDate, salesDate, guidingPrice,
                 customerName, identificationNo, salesConsultant, customerType, dealerID, marker, limit);
 
         for (DealerVehicleSalesLedger ledger : list) {
             List<String> summary = new ArrayList<String>();
             summary.add(String.valueOf(ledger.getContractNo()));
             summary.add(ledger.getModel());
             summary.add(ledger.getType());
             summary.add(ledger.getColor());
             summary.add(ledger.getLpNumber());
             summary.add(ledger.getFrameNo());
             if (null != ledger.getManufacturerDebitDate()) {
                 summary.add(ledger.getManufacturerDebitDate().toString());
             } else {
                 summary.add("");
             }
 
             if (null != ledger.getWarehousingDate()) {
                 summary.add(ledger.getWarehousingDate().toString());
             } else {
                 summary.add("");
             }
 
             if (null != ledger.getSalesDate()) {
                 summary.add(ledger.getSalesDate().toString());
             } else {
                 summary.add("");
             }
 
             if (null != ledger.getGuidingPrice()) {
                 summary.add(String.valueOf(ledger.getGuidingPrice()));
             } else {
                 summary.add("");
             }
 
             summary.add(ledger.getCustomerName());
             summary.add(ledger.getIdentificationNo());
             summary.add(ledger.getSalesConsultant());
             summary.add(ledger.getCustomerType());
             response.getSummaries().add(summary);
         }
 
         return response;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * getDealerVehicleSalesLedger(java.lang.Integer)
      */
     @Override
     public GetDealerVehicleSalesLedgerResponse getDealerVehicleSalesLedger(final Integer contractNo) {
         Preconditions.checkNotNull(contractNo, "contract number can't be null");
 
         final GetDealerVehicleSalesLedgerResponse response = new GetDealerVehicleSalesLedgerResponse();
         final Collection<DealerVehicleSalesLedger> list = incomeJournalDAL.getDealerVehicleSalesLedger(contractNo);
         for (DealerVehicleSalesLedger ledger : list) {
             final DealerVehicleSalesLedgerDetail item = new DealerVehicleSalesLedgerDetail();
             item.setContractNo(ledger.getContractNo());
             item.setDealerID(ledger.getDealerID());
             item.setModel(ledger.getModel());
             item.setType(ledger.getType());
             item.setColor(ledger.getColor());
             item.setLpNumber(ledger.getLpNumber());
             item.setFrameNo(ledger.getFrameNo());
             if (ledger.getManufacturerDebitDate() != null) {
                 item.setManufacturerDebitDate(ledger.getManufacturerDebitDate().toString());
             }
             if (ledger.getWarehousingDate() != null) {
                 item.setWarehousingDate(ledger.getWarehousingDate().toString());
             }
             if (ledger.getSalesDate() != null) {
                 item.setSalesDate(ledger.getSalesDate().toString());
             }
             item.setGuidingPrice(ledger.getGuidingPrice());
             item.setCustomerName(ledger.getCustomerName());
             item.setIdentificationNo(ledger.getIdentificationNo());
             item.setSalesConsultant(ledger.getSalesConsultant());
             item.setCustomerType(ledger.getCustomerType());
             item.setVehicleInvoiceAmount(ledger.getVehicleInvoiceAmount());
             item.setAccessoryInvoiceAmount(ledger.getAccessoryInvoiceAmount());
             item.setVehicleMarkup(ledger.getVehicleMarkup());
             item.setInvoiceType(ledger.getInvoiceType());
             if (ledger.getInvoiceDate() != null) {
                 item.setInvoiceDate(ledger.getInvoiceDate().toString());
             }
             item.setPosBank(ledger.getPosBank());
             item.setPosCharge(ledger.getPosCharge());
             item.setCash(ledger.getCash());
             item.setCheque(ledger.getCheque());
             item.setTransfer(ledger.getTransfer());
             item.setPaidSubscription(ledger.getPaidSubscription());
             item.setPaidRemainder(ledger.getPaidRemainder());
             item.setRefund(ledger.getRefund());
             item.setVehicleSalesRevenue(ledger.getVehicleSalesRevenue());
             item.setVehicleSalesCost(ledger.getVehicleSalesCost());
             item.setUsedVehicleCharge(ledger.getUsedVehicleCharge());
             item.setUsedVehicleCost(ledger.getUsedVehicleCost());
             item.setUsedVehicleSellingPrice(ledger.getUsedVehicleSellingPrice());
             item.setDistributor(ledger.getDistributor());
             item.setUsedVehicleManufacturerSubsidy(ledger.getUsedVehicleManufacturerSubsidy());
             item.setAccessorySalesRevenue(ledger.getAccessorySalesRevenue());
             item.setAccessorySalesCostBuyerPart(ledger.getAccessorySalesCostBuyerPart());
             item.setAccessorySalesCostGiftPart(ledger.getAccessorySalesCostGiftPart());
             item.setInsuranceRebateMargin(ledger.getInsuranceRebateMargin());
             item.setInsuranceCost(ledger.getInsuranceCost());
             item.setInsuranceAgengcy(ledger.getInsuranceAgengcy());
             item.setLpRevenue(ledger.getLpRevenue());
             item.setLpCost(ledger.getLpCost());
             item.setInstallmentCharge(ledger.getInstallmentCharge());
             item.setFcCommission(ledger.getFcCommission());
             item.setInstallmentManufacturerSubsidy(ledger.getInstallmentManufacturerSubsidy());
             item.setInstallmentOthers(ledger.getInstallmentOthers());
             item.setInstallmentAgency(ledger.getInstallmentAgency());
             item.setExtendedWarrantyRevenue(ledger.getExtendedWarrantyRevenue());
             item.setExtendedWarrantyCost(ledger.getExtendedWarrantyCost());
             item.setFinancialLeasingMargin(ledger.getFinancialLeasingMargin());
             item.setExitWarehouseMargin(ledger.getExitWarehouseMargin());
             item.setInspectionMargin(ledger.getInspectionMargin());
             item.setMemberCardRevenue(ledger.getMemberCardRevenue());
             item.setMemberCardCost(ledger.getMemberCardCost());
             item.setOtherMargin(ledger.getOtherMargin());
             item.setPostSalesVoucherManHourPart(ledger.getPostSalesVoucherManHourPart());
             item.setPostSalesVoucherDecorationPart(ledger.getPostSalesVoucherDecorationPart());
             item.setPostSalesVoucherAccessoryPart(ledger.getPostSalesVoucherAccessoryPart());
             item.setFuelCard(ledger.getFuelCard());
             item.setGasoline(ledger.getGasoline());
             item.setOtherGift(ledger.getOtherGift());
             item.setInventoryDays(ledger.getInventoryDays());
             item.setFinancingRate(ledger.getFinancingRate());
             item.setManagementFee(ledger.getManagementFee());
             item.setMonthlyRebate(ledger.getMonthlyRebate());
             item.setQuarterlyRebate(ledger.getQuarterlyRebate());
             item.setSatisfactionRebate(ledger.getSatisfactionRebate());
             item.setPickupRebate(ledger.getPickupRebate());
             item.setBiddingSubsidy(ledger.getBiddingSubsidy());
             item.setTerminalSalesBonus(ledger.getTerminalSalesBonus());
             item.setOtherRebate(ledger.getOtherRebate());
             item.setRebateAdjustment(ledger.getRebateAdjustment());
             item.setVehiclePushMoney(ledger.getVehiclePushMoney());
             item.setAccessoryPushMoney(ledger.getAccessoryPushMoney());
             item.setInsurancePushMoney(ledger.getInsurancePushMoney());
             item.setLpPushMoney(ledger.getLpPushMoney());
             item.setInstallmentPushMoney(ledger.getInstallmentPushMoney());
             item.setExtendedWarrantyPushMoney(ledger.getExtendedWarrantyPushMoney());
             item.setFinancialLeasingPushMoney(ledger.getFinancialLeasingPushMoney());
             item.setMemberCardPushMoney(ledger.getMemberCardPushMoney());
             item.setExchangePushMoney(ledger.getExchangePushMoney());
             item.setOtherPushMoney(ledger.getOtherPushMoney());
             item.setPushMoneyDeduction(ledger.getPushMoneyDeduction());
             response.getVehicleSalesLedger().add(item);
         }
 
         return response;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * saveDealerPostSalesLedger
      * (com.jdc.themis.dealer.web.domain.SaveDealerPostSalesLedgerRequest)
      */
     @Override
     public Instant saveDealerPostSalesLedger(final SaveDealerPostSalesLedgerRequest request) {
         Preconditions.checkNotNull(request.getWorkOrderNo(), "work order number can't be null");
         Preconditions.checkNotNull(request.getDealerID(), "dealer id can't be null");
         Preconditions.checkNotNull(request.getValidDate(), "valid date can't be null");
         final DealerPostSalesLedger ledger = new DealerPostSalesLedger();
         ledger.setWorkOrderNo(request.getWorkOrderNo());
         ledger.setDealerID(request.getDealerID());
         if (request.getSalesDate() != null) {
             ledger.setSalesDate(LocalDate.parse(request.getSalesDate()));
         }
         ledger.setMileage(request.getMileage() != null ? request.getMileage() : Double.valueOf(0));
         ledger.setLpNumber(request.getLpNumber());
         ledger.setCustomerName(request.getCustomerName());
         ledger.setColor(request.getColor());
         ledger.setFrameNo(request.getFrameNo());
         ledger.setModel(request.getModel());
         if (request.getEnterFactoryDate() != null) {
             ledger.setEnterFactoryDate(LocalDate.parse(request.getEnterFactoryDate()));
         }
         if (request.getExitFactoryDate() != null) {
             ledger.setExitFactoryDate(LocalDate.parse(request.getExitFactoryDate()));
         }
         ledger.setCustomerType(request.getCustomerType());
        ledger.setInsuranceAgengcy(request.getInsuranceAgency());
         if (request.getInsuranceDueDate() != null) {
             ledger.setInsuranceDueDate(LocalDate.parse(request.getInsuranceDueDate()));
         }
         ledger.setInsuranceClaimNumber(request.getInsuranceClaimNumber() != null ? request.getInsuranceClaimNumber()
                 : Integer.valueOf(0));
         ledger.setMaintenancePostSalesConsultant(request.getMaintenancePostSalesConsultant());
         ledger.setMaintenanceTechnician(request.getMaintenanceTechnician());
         ledger.setMaintenanceType(request.getMaintenanceType());
         ledger.setInsuranceType(request.getInsuranceType());
         ledger.setBodyShopPostSalesConsultant(request.getBodyShopPostSalesConsultant());
         ledger.setSheetMetalTechinician(request.getSheetMetalTechinician());
         ledger.setPaintSprayTechinician(request.getPaintSprayTechinician());
         ledger.setMineralEngineOil(request.getMineralEngineOil() != null ? request.getMineralEngineOil()
                 : Boolean.FALSE);
         ledger.setSynthesizedEngineOil(request.getSynthesizedEngineOil() != null ? request.getSynthesizedEngineOil()
                 : Boolean.FALSE);
         ledger.setAirFilterElement(request.getAirFilterElement() != null ? request.getAirFilterElement()
                 : Boolean.FALSE);
         ledger.setAirConditionerFilterElement(request.getAirConditionerFilterElement() != null ? request
                 .getAirConditionerFilterElement() : Boolean.FALSE);
         ledger.setGasolineFilterElement(request.getGasolineFilterElement() != null ? request.getGasolineFilterElement()
                 : Boolean.FALSE);
         ledger.setBrakeFluid(request.getBrakeFluid() != null ? request.getBrakeFluid() : Boolean.FALSE);
         ledger.setSparkPlug(request.getSparkPlug() != null ? request.getSparkPlug() : Boolean.FALSE);
         ledger.setDirectionEngineOil(request.getDirectionEngineOil() != null ? request.getDirectionEngineOil()
                 : Boolean.FALSE);
         ledger.setTransmissionOil(request.getTransmissionOil() != null ? request.getTransmissionOil() : Boolean.FALSE);
         ledger.setCoolingFluid(request.getCoolingFluid() != null ? request.getCoolingFluid() : Boolean.FALSE);
         ledger.setTimingChain(request.getTimingChain() != null ? request.getTimingChain() : Boolean.FALSE);
         ledger.setLubricationSystemClean(request.getLubricationSystemClean() != null ? request
                 .getLubricationSystemClean() : Boolean.FALSE);
         ledger.setTransmissionClean(request.getTransmissionClean() != null ? request.getTransmissionClean()
                 : Boolean.FALSE);
         ledger.setDirectionSystemClean(request.getDirectionSystemClean() != null ? request.getDirectionSystemClean()
                 : Boolean.FALSE);
         ledger.setCoolingSystemClean(request.getCoolingSystemClean() != null ? request.getCoolingSystemClean()
                 : Boolean.FALSE);
         ledger.setAirConditionerClean(request.getAirConditionerClean() != null ? request.getAirConditionerClean()
                 : Boolean.FALSE);
         ledger.setFuelSystemClean(request.getFuelSystemClean() != null ? request.getFuelSystemClean() : Boolean.FALSE);
         ledger.setOtherSystemClean(request.getOtherSystemClean() != null ? request.getOtherSystemClean()
                 : Boolean.FALSE);
         ledger.setMaintenanceProductAmount(request.getMaintenanceProductAmount() != null ? request
                 .getMaintenanceProductAmount() : Integer.valueOf(0));
         ledger.setTyreCount(request.getTyreCount() != null ? request.getMaintenanceProductAmount() : Integer.valueOf(0));
         ledger.setBatteryCount(request.getBatteryCount() != null ? request.getBatteryCount() : Integer.valueOf(0));
         ledger.setSparkPlugCount(request.getSparkPlugCount() != null ? request.getSparkPlugCount() : Integer.valueOf(0));
         ledger.setWiperBladeCount(request.getWiperBladeCount() != null ? request.getWiperBladeCount() : Integer
                 .valueOf(0));
         ledger.setMaintenanceManHourRevenuePaidPart(request.getMaintenanceManHourRevenuePaidPart() != null ? request
                 .getMaintenanceManHourRevenuePaidPart() : Double.valueOf(0));
         ledger.setMaintenanceManHourDiscountPaidPart(request.getMaintenanceManHourDiscountPaidPart() != null ? request
                 .getMaintenanceManHourDiscountPaidPart() : Double.valueOf(0));
         ledger.setMaintenanceManHourCouponPaidPart(request.getMaintenanceManHourCouponPaidPart() != null ? request
                 .getMaintenanceManHourCouponPaidPart() : Double.valueOf(0));
         ledger.setMaintenanceManHourCostPaidPart(request.getMaintenanceManHourCostPaidPart() != null ? request
                 .getMaintenanceManHourCostPaidPart() : Double.valueOf(0));
         ledger.setMaintenanceManHourRevenueUnpaidPart(request.getMaintenanceManHourRevenueUnpaidPart() != null ? request
                 .getMaintenanceManHourRevenueUnpaidPart() : Double.valueOf(0));
         ledger.setMaintenanceManHourCostUnpaidPart(request.getMaintenanceManHourCostUnpaidPart() != null ? request
                 .getMaintenanceManHourCostUnpaidPart() : Double.valueOf(0));
         ledger.setSheetMetalManHourRevenue(request.getSheetMetalManHourRevenue() != null ? request
                 .getSheetMetalManHourRevenue() : Double.valueOf(0));
         ledger.setPaintSprayManHourRevenue(request.getPaintSprayManHourRevenue() != null ? request
                 .getPaintSprayManHourRevenue() : Double.valueOf(0));
         ledger.setBodyShopManHourDiscount(request.getBodyShopManHourDiscount() != null ? request
                 .getBodyShopManHourDiscount() : Double.valueOf(0));
         ledger.setBodyShopManHourCoupon(request.getBodyShopManHourCoupon() != null ? request.getBodyShopManHourCoupon()
                 : Double.valueOf(0));
         ledger.setBodyShopManHourCost(request.getBodyShopManHourCost() != null ? request.getBodyShopManHourCost()
                 : Double.valueOf(0));
         ledger.setAccessoryManHourRevenue(request.getAccessoryManHourRevenue() != null ? request
                 .getAccessoryManHourRevenue() : Double.valueOf(0));
         ledger.setAccessoryManHourDiscount(request.getAccessoryManHourDiscount() != null ? request
                 .getAccessoryManHourDiscount() : Double.valueOf(0));
         ledger.setAccessoryManHourCoupon(request.getAccessoryManHourCoupon() != null ? request
                 .getAccessoryManHourCoupon() : Double.valueOf(0));
         ledger.setAccessoryManHourCost(request.getAccessoryManHourCost() != null ? request.getAccessoryManHourCost()
                 : Double.valueOf(0));
         ledger.setOtherManHourRevenue(request.getOtherManHourRevenue() != null ? request.getOtherManHourRevenue()
                 : Double.valueOf(0));
         ledger.setOtherManHourDiscount(request.getOtherManHourDiscount() != null ? request.getOtherManHourDiscount()
                 : Double.valueOf(0));
         ledger.setOtherManHourCoupon(request.getOtherManHourCoupon() != null ? request.getOtherManHourCoupon() : Double
                 .valueOf(0));
         ledger.setOtherManHourCost(request.getOtherManHourCost() != null ? request.getOtherManHourCost() : Double
                 .valueOf(0));
         ledger.setMaintenancePartsRevenue(request.getMaintenancePartsRevenue() != null ? request
                 .getMaintenancePartsRevenue() : Double.valueOf(0));
         ledger.setMaintenancePartsDiscount(request.getMaintenancePartsDiscount() != null ? request
                 .getMaintenancePartsDiscount() : Double.valueOf(0));
         ledger.setMaintenancePartsCoupon(request.getMaintenancePartsCoupon() != null ? request
                 .getMaintenancePartsCoupon() : Double.valueOf(0));
         ledger.setMaintenancePartsCost(request.getMaintenancePartsCost() != null ? request.getMaintenancePartsCost()
                 : Double.valueOf(0));
         ledger.setAccessoryPartsRevenue(request.getAccessoryPartsRevenue() != null ? request.getAccessoryPartsRevenue()
                 : Double.valueOf(0));
         ledger.setAccessoryPartsDiscount(request.getAccessoryPartsDiscount() != null ? request
                 .getAccessoryPartsDiscount() : Double.valueOf(0));
         ledger.setAccessoryPartsCoupon(request.getAccessoryPartsCoupon() != null ? request.getAccessoryPartsCoupon()
                 : Double.valueOf(0));
         ledger.setAccessoryPartsCost(request.getAccessoryPartsCost() != null ? request.getAccessoryPartsCost() : Double
                 .valueOf(0));
         ledger.setMaintenanceProductRevenue(request.getMaintenanceProductRevenue() != null ? request
                 .getMaintenanceProductRevenue() : Double.valueOf(0));
         ledger.setMaintenanceProductDiscount(request.getMaintenanceProductDiscount() != null ? request
                 .getMaintenanceProductDiscount() : Double.valueOf(0));
         ledger.setMaintenanceProductCoupon(request.getMaintenanceProductCoupon() != null ? request
                 .getMaintenanceProductCoupon() : Double.valueOf(0));
         ledger.setMaintenanceProductCost(request.getMaintenanceProductCost() != null ? request
                 .getMaintenanceProductCost() : Double.valueOf(0));
         ledger.setVehicleDecorationRevenue(request.getVehicleDecorationRevenue() != null ? request
                 .getVehicleDecorationRevenue() : Double.valueOf(0));
         ledger.setVehicleDecorationDiscount(request.getVehicleDecorationDiscount() != null ? request
                 .getVehicleDecorationDiscount() : Double.valueOf(0));
         ledger.setVehicleDecorationCoupon(request.getVehicleDecorationCoupon() != null ? request
                 .getVehicleDecorationCoupon() : Double.valueOf(0));
         ledger.setVehicleDecorationCost(request.getVehicleDecorationCost() != null ? request.getVehicleDecorationCost()
                 : Double.valueOf(0));
 
         ledger.setValidDate(LocalDate.parse(request.getValidDate()));
         ledger.setUpdatedBy(request.getUpdatedBy());
 
         final Instant timestamp = incomeJournalDAL.saveDealerPostSalesLedger(ledger);
         return timestamp;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * queryDealerPostSalesLedger(java.lang.Integer, java.lang.String,
      * java.lang.Double, java.lang.String, java.lang.String, java.lang.String,
      * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
      * java.lang.String, java.lang.String, java.lang.String, java.lang.Integer,
      * java.lang.Integer, java.lang.Integer, java.lang.Integer)
      */
     @Override
     public GetDealerLedgerResponse queryDealerPostSalesLedger(Integer workOrderNo, String salesDate, Double mileage,
             String lpNumber, String customerName, String color, String frameNo, String model, String enterFactoryDate,
             String exitFactoryDate, String customerType, String insuranceAgengcy, String insuranceDueDate,
             Integer insuranceClaimNumber, Integer dealerID, Integer marker, Integer limit) {
         final GetDealerLedgerResponse response = new GetDealerLedgerResponse();
         final Collection<DealerPostSalesLedger> list = incomeJournalDAL.queryDealerPostSalesLedger(workOrderNo,
                 salesDate, mileage, lpNumber, customerName, color, frameNo, model, enterFactoryDate, exitFactoryDate,
                 customerType, insuranceAgengcy, insuranceDueDate, insuranceClaimNumber, dealerID, marker, limit);
         for (DealerPostSalesLedger ledger : list) {
             List<String> summary = new ArrayList<String>();
             summary.add(String.valueOf(ledger.getWorkOrderNo()));
             if (null != ledger.getSalesDate()) {
                 summary.add(ledger.getSalesDate().toString());
             } else {
                 summary.add("");
             }
 
             summary.add(String.valueOf(ledger.getMileage()));
             summary.add(ledger.getLpNumber());
             summary.add(ledger.getCustomerName());
             summary.add(ledger.getColor());
             summary.add(ledger.getFrameNo());
             summary.add(ledger.getModel());
             if (null != ledger.getEnterFactoryDate()) {
                 summary.add(ledger.getEnterFactoryDate().toString());
             } else {
                 summary.add("");
             }
 
             if (null != ledger.getExitFactoryDate()) {
                 summary.add(ledger.getExitFactoryDate().toString());
             } else {
                 summary.add("");
             }
 
             summary.add(ledger.getCustomerType());
             summary.add(ledger.getInsuranceAgengcy());
 
             if (null != ledger.getInsuranceDueDate()) {
                 summary.add(ledger.getInsuranceDueDate().toString());
             } else {
                 summary.add("");
             }
 
             summary.add(String.valueOf(ledger.getInsuranceClaimNumber()));
             response.getSummaries().add(summary);
         }
 
         return response;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.jdc.themis.dealer.service.DealerIncomeEntryService#
      * getDealerPostSalesLedger(java.lang.Integer)
      */
     @Override
     public GetDealerPostSalesLedgerResponse getDealerPostSalesLedger(Integer workOrderNo) {
         Preconditions.checkNotNull(workOrderNo, "work order number can't be null");
 
         final GetDealerPostSalesLedgerResponse response = new GetDealerPostSalesLedgerResponse();
         final Collection<DealerPostSalesLedger> list = incomeJournalDAL.getDealerPostSalesLedger(workOrderNo);
         for (DealerPostSalesLedger ledger : list) {
             final DealerPostSalesLedgerDetail item = new DealerPostSalesLedgerDetail();
             item.setWorkOrderNo(ledger.getWorkOrderNo());
             item.setDealerID(ledger.getDealerID());
             if (ledger.getSalesDate() != null) {
                 item.setSalesDate(ledger.getSalesDate().toString());
             }
             item.setMileage(ledger.getMileage());
             item.setLpNumber(ledger.getLpNumber());
             item.setCustomerName(ledger.getCustomerName());
             item.setColor(ledger.getColor());
             item.setFrameNo(ledger.getFrameNo());
             item.setModel(ledger.getModel());
             if (ledger.getEnterFactoryDate() != null) {
                 item.setEnterFactoryDate(ledger.getEnterFactoryDate().toString());
             }
             if (ledger.getExitFactoryDate() != null) {
                 item.setExitFactoryDate(ledger.getExitFactoryDate().toString());
             }
             item.setCustomerType(ledger.getCustomerType());
             item.setInsuranceAgengcy(ledger.getInsuranceAgengcy());
             if (ledger.getInsuranceDueDate() != null) {
                 item.setInsuranceDueDate(ledger.getInsuranceDueDate().toString());
             }
             item.setInsuranceClaimNumber(ledger.getInsuranceClaimNumber());
             item.setMaintenancePostSalesConsultant(ledger.getMaintenancePostSalesConsultant());
             item.setMaintenanceTechnician(ledger.getMaintenanceTechnician());
             item.setMaintenanceType(ledger.getMaintenanceType());
             item.setInsuranceType(ledger.getInsuranceType());
             item.setBodyShopPostSalesConsultant(ledger.getBodyShopPostSalesConsultant());
             item.setSheetMetalTechinician(ledger.getSheetMetalTechinician());
             item.setPaintSprayTechinician(ledger.getPaintSprayTechinician());
             item.setMineralEngineOil(ledger.getMineralEngineOil());
             item.setSynthesizedEngineOil(ledger.getSynthesizedEngineOil());
             item.setAirFilterElement(ledger.getAirFilterElement());
             item.setAirConditionerFilterElement(ledger.getAirConditionerFilterElement());
             item.setGasolineFilterElement(ledger.getGasolineFilterElement());
             item.setBrakeFluid(ledger.getBrakeFluid());
             item.setSparkPlug(ledger.getSparkPlug());
             item.setDirectionEngineOil(ledger.getDirectionEngineOil());
             item.setTransmissionOil(ledger.getTransmissionOil());
             item.setCoolingFluid(ledger.getCoolingFluid());
             item.setTimingChain(ledger.getTimingChain());
             item.setLubricationSystemClean(ledger.getLubricationSystemClean());
             item.setTransmissionClean(ledger.getTransmissionClean());
             item.setDirectionSystemClean(ledger.getDirectionSystemClean());
             item.setCoolingSystemClean(ledger.getCoolingSystemClean());
             item.setAirConditionerClean(ledger.getAirConditionerClean());
             item.setFuelSystemClean(ledger.getFuelSystemClean());
             item.setOtherSystemClean(ledger.getOtherSystemClean());
             item.setMaintenanceProductAmount(ledger.getMaintenanceProductAmount());
             item.setTyreCount(ledger.getTyreCount());
             item.setBatteryCount(ledger.getBatteryCount());
             item.setSparkPlugCount(ledger.getSparkPlugCount());
             item.setWiperBladeCount(ledger.getWiperBladeCount());
             item.setMaintenanceManHourRevenuePaidPart(ledger.getMaintenanceManHourRevenuePaidPart());
             item.setMaintenanceManHourDiscountPaidPart(ledger.getMaintenanceManHourDiscountPaidPart());
             item.setMaintenanceManHourCouponPaidPart(ledger.getMaintenanceManHourCouponPaidPart());
             item.setMaintenanceManHourCostPaidPart(ledger.getMaintenanceManHourCostPaidPart());
             item.setMaintenanceManHourRevenueUnpaidPart(ledger.getMaintenanceManHourRevenueUnpaidPart());
             item.setMaintenanceManHourCostUnpaidPart(ledger.getMaintenanceManHourCostUnpaidPart());
             item.setSheetMetalManHourRevenue(ledger.getSheetMetalManHourRevenue());
             item.setPaintSprayManHourRevenue(ledger.getPaintSprayManHourRevenue());
             item.setBodyShopManHourDiscount(ledger.getBodyShopManHourDiscount());
             item.setBodyShopManHourCoupon(ledger.getBodyShopManHourCoupon());
             item.setBodyShopManHourCost(ledger.getBodyShopManHourCost());
             item.setAccessoryManHourRevenue(ledger.getAccessoryManHourRevenue());
             item.setAccessoryManHourDiscount(ledger.getAccessoryManHourDiscount());
             item.setAccessoryManHourCoupon(ledger.getAccessoryManHourCoupon());
             item.setAccessoryManHourCost(ledger.getAccessoryManHourCost());
             item.setOtherManHourRevenue(ledger.getOtherManHourRevenue());
             item.setOtherManHourDiscount(ledger.getOtherManHourDiscount());
             item.setOtherManHourCoupon(ledger.getOtherManHourCoupon());
             item.setOtherManHourCost(ledger.getOtherManHourCost());
             item.setMaintenancePartsRevenue(ledger.getMaintenancePartsRevenue());
             item.setMaintenancePartsDiscount(ledger.getMaintenancePartsDiscount());
             item.setMaintenancePartsCoupon(ledger.getMaintenancePartsCoupon());
             item.setMaintenancePartsCost(ledger.getMaintenancePartsCost());
             item.setAccessoryPartsRevenue(ledger.getAccessoryPartsRevenue());
             item.setAccessoryPartsDiscount(ledger.getAccessoryPartsDiscount());
             item.setAccessoryPartsCoupon(ledger.getAccessoryPartsCoupon());
             item.setAccessoryPartsCost(ledger.getAccessoryPartsCost());
             item.setMaintenanceProductRevenue(ledger.getMaintenanceProductRevenue());
             item.setMaintenanceProductDiscount(ledger.getMaintenanceProductDiscount());
             item.setMaintenanceProductCoupon(ledger.getMaintenanceProductCoupon());
             item.setMaintenanceProductCost(ledger.getMaintenanceProductCost());
             item.setVehicleDecorationRevenue(ledger.getVehicleDecorationRevenue());
             item.setVehicleDecorationDiscount(ledger.getVehicleDecorationDiscount());
             item.setVehicleDecorationCoupon(ledger.getVehicleDecorationCoupon());
             item.setVehicleDecorationCost(ledger.getVehicleDecorationCost());
             response.getPostSalesLedger().add(item);
         }
 
         return response;
     }
 	
 }
