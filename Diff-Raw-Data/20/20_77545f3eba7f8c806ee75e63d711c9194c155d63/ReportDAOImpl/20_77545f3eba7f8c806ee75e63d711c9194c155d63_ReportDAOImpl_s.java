 package com.jdc.themis.dealer.data.dao.hibernate;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.List;
 
 import javax.time.calendar.LocalDate;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import ch.lambdaj.Lambda;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.jdc.themis.dealer.data.dao.IncomeJournalDAO;
 import com.jdc.themis.dealer.data.dao.RefDataDAO;
 import com.jdc.themis.dealer.data.dao.ReportDAO;
 import com.jdc.themis.dealer.domain.AccountReceivableDuration;
 import com.jdc.themis.dealer.domain.DealerAccountReceivableFact;
 import com.jdc.themis.dealer.domain.DealerEmployeeFeeFact;
 import com.jdc.themis.dealer.domain.DealerHRAllocationFact;
 import com.jdc.themis.dealer.domain.DealerIncomeExpenseFact;
 import com.jdc.themis.dealer.domain.DealerIncomeRevenueFact;
 import com.jdc.themis.dealer.domain.DealerInventoryFact;
 import com.jdc.themis.dealer.domain.EmployeeFee;
 import com.jdc.themis.dealer.domain.GeneralJournal;
 import com.jdc.themis.dealer.domain.HumanResourceAllocation;
 import com.jdc.themis.dealer.domain.InventoryDuration;
 import com.jdc.themis.dealer.domain.ReportItem;
 import com.jdc.themis.dealer.domain.ReportTime;
 import com.jdc.themis.dealer.domain.SalesServiceJournal;
 import com.jdc.themis.dealer.domain.TaxJournal;
 import com.jdc.themis.dealer.domain.VehicleSalesJournal;
 import com.jdc.themis.dealer.utils.Performance;
 import com.jdc.themis.dealer.utils.Utils;
 
 import fj.P1;
 import fj.data.Option;
 
 @Service
 public class ReportDAOImpl implements ReportDAO {
 	private final static Logger logger = LoggerFactory
 			.getLogger(ReportDAOImpl.class);
 
 	@Autowired
 	private SessionFactory sessionFactory;
 	@Autowired
 	private RefDataDAO refDataDAL;
 	@Autowired
 	private IncomeJournalDAO incomeJournalDAL;
 
 	@Override
 	@Performance
 	public void importVehicleSalesJournal(final LocalDate validDate) {
 		logger.info("Importing vehicle sales journal to income revenue");
 
 		final Collection<VehicleSalesJournal> list = incomeJournalDAL
 				.getVehicleSalesJournal(validDate, Utils.currentTimestamp());
 
 		logger.debug("journals for import {}", list);
 		final List<DealerIncomeRevenueFact> facts = Lists.newArrayList();
 		for (final VehicleSalesJournal journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerIncomeRevenueFact fact = new DealerIncomeRevenueFact();
 			fact.setAmount(journal.getAmount());
 			fact.setCount(journal.getCount());
 			fact.setMargin(journal.getMargin());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "VehicleSalesJournal").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(),
 									refDataDAL.getVehicle(journal.getId())
 											.some().getName(),
 									"VehicleSalesJournal",
 									refDataDAL
 											.getSalesServiceJournalCategory(
 													refDataDAL
 															.getVehicle(
 																	journal.getId())
 															.some()
 															.getCategoryID())
 											.some().getName());
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDepartmentID(journal.getDepartmentID());
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 		logger.info("To persist facts: " + facts);
 		this.saveDealerIncomeRevenueFacts(facts);
 	}
 
 	@Override
 	@Performance
 	public void importSalesServiceJournal(final LocalDate validDate) {
 		logger.info("Importing sales & service journal to income revenue");
 
 		final Integer revenueJournalType = refDataDAL
 				.getEnumValue("JournalType", "Revenue").some().getValue();
 		final Integer expenseJournalType = refDataDAL
 				.getEnumValue("JournalType", "Expense").some().getValue();
 
 		final Collection<SalesServiceJournal> list = incomeJournalDAL
 				.getSalesServiceJournal(validDate, Utils.currentTimestamp());
 
 		final List<DealerIncomeRevenueFact> revenueFacts = Lists.newArrayList();
 		final List<DealerIncomeExpenseFact> expenseFacts = Lists.newArrayList();
 		for (final SalesServiceJournal journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 			logger.debug("saving journal {}", journal);
 			if (revenueJournalType.equals(refDataDAL
 					.getSalesServiceJournalItem(journal.getId()).some()
 					.getJournalType())) {
 				final DealerIncomeRevenueFact fact = new DealerIncomeRevenueFact();
 				fact.setAmount(journal.getAmount());
 				fact.setCount(journal.getCount());
 				fact.setMargin(journal.getMargin());
 				fact.setTimeID(reportTime.some().getId());
 				// verify report item here
 				final Option<ReportItem> reportItem = this.getReportItem(
 						journal.getId(), "SalesServiceJournal").orElse(
 						new P1<Option<ReportItem>>() {
 
 							@Override
 							public Option<ReportItem> _1() {
 								return ReportDAOImpl.this.addReportItem(
 										journal.getId(),
 										refDataDAL
 												.getSalesServiceJournalItem(
 														journal.getId()).some()
 												.getName(),
 										"SalesServiceJournal",
 										refDataDAL
 												.getSalesServiceJournalCategory(
 														refDataDAL
 																.getSalesServiceJournalItem(
 																		journal.getId())
 																.some()
 																.getCategoryID())
 												.some().getName());
 							}
 
 						});
 
 				fact.setDealerID(journal.getDealerID());
 				fact.setDepartmentID(journal.getDepartmentID());
 				fact.setItemID(reportItem.some().getId());
 				fact.setTimestamp(journal.getTimestamp());
 				fact.setTimeEnd(journal.getTimeEnd());
 				revenueFacts.add(fact);
 			}
 			if (expenseJournalType.equals(refDataDAL
 					.getSalesServiceJournalItem(journal.getId()).some()
 					.getJournalType())) {
 				final DealerIncomeExpenseFact fact = new DealerIncomeExpenseFact();
 				fact.setAmount(journal.getMargin()); // amount and count would
 														// be zero for a expense
 														// item
 				fact.setTimeID(reportTime.some().getId());
 				// verify report item here
 				final Option<ReportItem> reportItem = this.getReportItem(
 						journal.getId(), "SalesServiceJournal").orElse(
 						new P1<Option<ReportItem>>() {
 
 							@Override
 							public Option<ReportItem> _1() {
 								return ReportDAOImpl.this.addReportItem(
 										journal.getId(),
 										refDataDAL
 												.getSalesServiceJournalItem(
 														journal.getId()).some()
 												.getName(),
 										"SalesServiceJournal",
 										refDataDAL
 												.getSalesServiceJournalCategory(
 														refDataDAL
 																.getSalesServiceJournalItem(
 																		journal.getId())
 																.some()
 																.getCategoryID())
 												.some().getName());
 							}
 
 						});
 
 				fact.setDealerID(journal.getDealerID());
 				fact.setDepartmentID(journal.getDepartmentID());
 				fact.setItemID(reportItem.some().getId());
 				fact.setTimestamp(journal.getTimestamp());
 				fact.setTimeEnd(journal.getTimeEnd());
 				expenseFacts.add(fact);
 			}
 
 		}
 		this.saveDealerIncomeRevenueFacts(revenueFacts);
 		this.saveDealerIncomeExpenseFacts(expenseFacts);
 	}
 
 	@Override
 	public void importGeneralJournal(final LocalDate validDate) {
 		logger.info("Importing general journal to income revenue and expense");
 
 		final Collection<GeneralJournal> list = incomeJournalDAL
 				.getGeneralJournal(validDate, Utils.currentTimestamp());
 
 		final Collection<GeneralJournal> revenueJournals = Lists.newArrayList();
 		final Integer revenueJournalType = refDataDAL
 				.getEnumValue("JournalType", "Revenue").some().getValue();
 		for (final GeneralJournal journal : list) {
 			if (refDataDAL.getGeneralJournalItem(journal.getId()).some()
 					.getJournalType().equals(revenueJournalType)) {
 				revenueJournals.add(journal);
 			}
 		}
 		final Collection<GeneralJournal> expenseJournals = Lists.newArrayList();
 		final Integer expenseJournalType = refDataDAL
 				.getEnumValue("JournalType", "Expense").some().getValue();
 		for (final GeneralJournal journal : list) {
 			if (refDataDAL.getGeneralJournalItem(journal.getId()).some()
 					.getJournalType().equals(expenseJournalType)) {
 				expenseJournals.add(journal);
 			}
 		}
 
 		final List<DealerIncomeExpenseFact> facts = Lists.newArrayList();
 		for (final GeneralJournal journal : expenseJournals) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerIncomeExpenseFact fact = new DealerIncomeExpenseFact();
 			fact.setAmount(journal.getAmount());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "GeneralJournal").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(),
 									refDataDAL
 											.getGeneralJournalItem(
 													journal.getId()).some()
 											.getName(),
 									"GeneralJournal",
 									refDataDAL
 											.getGeneralJournalCategory(
 													refDataDAL
 															.getGeneralJournalItem(
 																	journal.getId())
 															.some()
 															.getCategoryID())
 											.some().getName());
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDepartmentID(journal.getDepartmentID());
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 
 		this.saveDealerIncomeExpenseFacts(facts);
 
 		final List<DealerIncomeRevenueFact> revenueFacts = Lists.newArrayList();
 		for (final GeneralJournal journal : revenueJournals) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerIncomeRevenueFact fact = new DealerIncomeRevenueFact();
 			fact.setAmount(journal.getAmount());
 			fact.setMargin(BigDecimal.ZERO);
 			fact.setCount(0);
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "GeneralJournal").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(),
 									refDataDAL
 											.getGeneralJournalItem(
 													journal.getId()).some()
 											.getName(),
 									"GeneralJournal",
 									refDataDAL
 											.getGeneralJournalCategory(
 													refDataDAL
 															.getGeneralJournalItem(
 																	journal.getId())
 															.some()
 															.getCategoryID())
 											.some().getName());
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDepartmentID(journal.getDepartmentID());
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			revenueFacts.add(fact);
 		}
 		logger.info("To persist facts: " + facts);
 		this.saveDealerIncomeRevenueFacts(revenueFacts);
 	}
 
 	@Override
 	public Option<ReportTime> addReportTime(final LocalDate validDate) {
 		final Session session = sessionFactory.getCurrentSession();
 		final ReportTime time = new ReportTime();
 		time.setValidDate(validDate);
 		time.setMonthOfYear(validDate.getMonthOfYear().getValue());
 		time.setYear(validDate.getYear());
 		session.save(time);
 		session.flush();
 		return Option.<ReportTime> some(time);
 	}
 
 	@Override
 	public Option<ReportItem> addReportItem(final Integer itemID,
 			final String itemName, final String source, final String category) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Integer reportItemSource = refDataDAL
 				.getEnumValue("ReportItemSource", source).some().getValue();
 
 		final ReportItem reportItem = new ReportItem();
 		reportItem.setItemSource(reportItemSource);
 		reportItem.setSourceItemID(itemID);
 		reportItem.setName(itemName);
 		reportItem.setItemCategory(category);
 		session.save(reportItem);
 		session.flush();
 		return Option.<ReportItem> some(reportItem);
 	}
 
 	@Override
 	public Option<ReportTime> getReportTime(final LocalDate validDate) {
 		final Session session = sessionFactory.getCurrentSession();
 		@SuppressWarnings("unchecked")
 		final List<ReportTime> reportTimeList = session
 				.createCriteria(ReportTime.class)
 				.add(Restrictions.eq("validDate", validDate)).list();
 		return Option.<ReportTime> iif(!reportTimeList.isEmpty(),
 				new P1<ReportTime>() {
 
 					@Override
 					public ReportTime _1() {
 						return reportTimeList.get(0);
 					}
 
 				});
 	}
 
 	@Override
 	public Option<ReportItem> getReportItem(final Integer itemID,
 			final String source) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Integer reportItemSource = refDataDAL
 				.getEnumValue("ReportItemSource", source).some().getValue();
 		@SuppressWarnings("unchecked")
 		final List<ReportItem> reportItems = session
 				.createCriteria(ReportItem.class)
 				.add(Restrictions.eq("sourceItemID", itemID))
 				.add(Restrictions.eq("itemSource", reportItemSource)).list();
 
 		return Option.<ReportItem> iif(!reportItems.isEmpty(),
 				new P1<ReportItem>() {
 
 					@Override
 					public ReportItem _1() {
 						return reportItems.get(0);
 					}
 
 				});
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<ReportItem> getReportItem(Collection<String> itemCategories, Collection<String> itemNames, Collection<Integer> itemSources) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Criteria criteria = session.createCriteria(ReportItem.class);
 		if ( !itemCategories.isEmpty() ) {
 			criteria.add(Restrictions.in("itemCategory", itemCategories));
 		}
 		if ( !itemNames.isEmpty() ) {
 			criteria.add(Restrictions.in("name", itemNames));
 		}
 		if ( !itemSources.isEmpty() ) {
 			criteria.add(Restrictions.in("itemSource", itemSources));
 		}
 		return criteria.list();
 	}
 
 	@Override
 	public void saveDealerIncomeRevenueFacts(
 			final Collection<DealerIncomeRevenueFact> journals) {
 		logger.debug("facts to save {}", journals);
 
 		final Session session = sessionFactory.getCurrentSession();
 		for (final DealerIncomeRevenueFact newJournal : journals) {
 			// check whether this journal has been inserted before
 			session.enableFilter(DealerIncomeRevenueFact.FILTER)
 					.setParameter("timeID", newJournal.getTimeID())
 					.setParameter("itemID", newJournal.getItemID())
 					.setParameter("dealerID", newJournal.getDealerID())
 					.setParameter("departmentID", newJournal.getDepartmentID())
 					.setParameter("referenceTime", Utils.currentTimestamp()); // we
 																				// are
 																				// only
 																				// interested
 																				// in
 																				// latest
 																				// record
 			@SuppressWarnings("unchecked")
 			final List<DealerIncomeRevenueFact> list = session.createCriteria(
 					DealerIncomeRevenueFact.class).list();
 			logger.debug("facts to be removed {}", list);
 			boolean isPersisted = false;
 			if (!list.isEmpty()) {
 				for (final DealerIncomeRevenueFact oldJournal : list) {
 					// if we get here, it means we have inserted this fact
 					// before
 					if (oldJournal.getTimestamp().isBefore(
 							newJournal.getTimestamp())) {
 						oldJournal.setTimeEnd(newJournal.getTimestamp());
 						session.saveOrUpdate(oldJournal);
 					}
 					// ignore if we've already got this journal in table
 					if (oldJournal.getTimestamp().equals(
 							newJournal.getTimestamp())) {
 						// somehow, we have persisted this journal before
 						isPersisted = true;
 					}
 				}
 				session.flush();
 			} else {
 				// this is a new journal
 				session.save(newJournal);
 			}
 			if (!isPersisted) {
 				session.save(newJournal);
 			}
 			session.disableFilter(DealerIncomeRevenueFact.FILTER);
 
 			session.flush();
 		}
 
 	}
 
 	@Override
 	public Collection<ReportTime> getAllReportTime() {
 		final Session session = sessionFactory.getCurrentSession();
 		@SuppressWarnings("unchecked")
 		final List<ReportTime> list = session.createCriteria(ReportTime.class)
 				.list();
 		return ImmutableList.copyOf(list);
 	}
 
 	@Override
 	public Collection<ReportItem> getAllReportItem() {
 		final Session session = sessionFactory.getCurrentSession();
 		@SuppressWarnings("unchecked")
 		final List<ReportItem> list = session.createCriteria(ReportItem.class)
 				.list();
 		return ImmutableList.copyOf(list);
 	}
 
 	@Override
 	public Collection<ReportTime> getReportTimeLessThanGivenMonth(Integer year,
 			Option<Integer> lessThanMonthOfYear) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Criteria criteria = session.createCriteria(ReportTime.class);
 		criteria.add(Restrictions.eq("year", year));
 		if (lessThanMonthOfYear.isSome()) {
 			criteria.add(Restrictions.le("monthOfYear", lessThanMonthOfYear.some()));
 		}
 		@SuppressWarnings("unchecked")
 		final List<ReportTime> list = criteria.list();
 		return list;
 	}
 	
 	@Override
 	public Collection<ReportTime> getReportTime(Integer year,
 			Option<Integer> monthOfYear) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Criteria criteria = session.createCriteria(ReportTime.class);
 		criteria.add(Restrictions.eq("year", year));
 		if (monthOfYear.isSome()) {
 			criteria.add(Restrictions.eq("monthOfYear", monthOfYear.some()));
 		}
 		@SuppressWarnings("unchecked")
 		final List<ReportTime> list = criteria.list();
 		return list;
 	}
 
 	public Collection<ReportTime> getReportTime(Integer year,
 			Collection<Integer> monthOfYear) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Criteria criteria = session.createCriteria(ReportTime.class);
 		criteria.add(Restrictions.eq("year", year));
 		if (!monthOfYear.isEmpty()) {
 			criteria.add(Restrictions.in("monthOfYear", monthOfYear));
 		}
 		@SuppressWarnings("unchecked")
 		final List<ReportTime> list = criteria.list();
 		return list;
 	}
 
 	@Override
 	public Option<ReportItem> getReportItem(Long id) {
 		final Session session = sessionFactory.getCurrentSession();
 		final ReportItem result = (ReportItem) session.get(ReportItem.class, id);
 		return Option.<ReportItem>fromNull(result);
 	}
 
 	@Override
 	public void saveDealerIncomeExpenseFacts(
 			Collection<DealerIncomeExpenseFact> journals) {
 		final Session session = sessionFactory.getCurrentSession();
 		for (DealerIncomeExpenseFact newJournal : journals) {
 			// check whether this journal has been inserted before
 			session.enableFilter(DealerIncomeExpenseFact.FILTER)
 					.setParameter("timeID", newJournal.getTimeID())
 					.setParameter("itemID", newJournal.getItemID())
 					.setParameter("dealerID", newJournal.getDealerID())
 					.setParameter("departmentID", newJournal.getDepartmentID())
 					.setParameter("referenceTime", Utils.currentTimestamp()); // we
 																				// are
 																				// only
 																				// interested
 																				// in
 																				// latest
 																				// record
 			@SuppressWarnings("unchecked")
 			final List<DealerIncomeExpenseFact> list = session.createCriteria(
 					DealerIncomeExpenseFact.class).list();
 			boolean isPersisted = false;
 			if (!list.isEmpty()) {
 				for (final DealerIncomeExpenseFact oldJournal : list) {
 					// if we get here, it means we have inserted this fact
 					// before
 					if (oldJournal.getTimestamp().isBefore(
 							newJournal.getTimestamp())) {
 						oldJournal.setTimeEnd(newJournal.getTimestamp());
 						session.saveOrUpdate(oldJournal);
 					}
 					if (oldJournal.getTimestamp().equals(
 							newJournal.getTimestamp())) {
 						isPersisted = true;
 					}
 				}
 				session.flush();
 			} else {
 				session.save(newJournal);
 			}
 			if (!isPersisted) {
 				session.save(newJournal);
 			}
 			session.disableFilter(DealerIncomeExpenseFact.FILTER);
 
 			session.flush();
 		}
 	}
 
 	@Override
 	public void importTaxJournal(final LocalDate validDate) {
 		logger.info("Importing tax journal to income expense");
 
 		final Collection<TaxJournal> list = incomeJournalDAL.getTaxJournal(
 				validDate, Utils.currentTimestamp());
 
 		final List<DealerIncomeExpenseFact> facts = Lists.newArrayList();
 		for (final TaxJournal journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerIncomeExpenseFact fact = new DealerIncomeExpenseFact();
 			fact.setAmount(journal.getAmount());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "TaxJournal").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(), refDataDAL
 											.getTaxJournalItem(journal.getId())
 											.some().getName(), "TaxJournal",
 									null);
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDepartmentID(0); // put to NA department
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 		logger.info("To persist facts: " + facts);
 		this.saveDealerIncomeExpenseFacts(facts);
 	}
 
 	@Override
 	@Performance
 	public Collection<DealerIncomeExpenseFact> getDealerIncomeExpenseFacts(
 			Integer year, Collection<Integer> monthOfYear,
 			Collection<Integer> departmentID, Collection<Integer> itemSource,
 			Collection<String> itemCategory, Collection<String> itemName, Collection<Long> itemID,
 			Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTime(year,
 				monthOfYear);
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerIncomeExpenseFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerIncomeExpenseFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (!departmentID.isEmpty()) {
 			criteria.add(Restrictions.in("departmentID", departmentID));
 		}
 		if (!itemID.isEmpty()) {
 			criteria.add(Restrictions.in("itemID", itemID));
 		}
 		if (!itemCategory.isEmpty() || !itemSource.isEmpty() || !itemName.isEmpty()) {
 			final Collection<ReportItem> items = getReportItem(itemCategory, itemName, itemSource);
			criteria.add(Restrictions.in("itemID", Lambda.extractProperty(items, "id")));
 		}
 
 		@SuppressWarnings("unchecked")
 		final List<DealerIncomeExpenseFact> list = criteria.list();
 		session.disableFilter(DealerIncomeExpenseFact.FILTER_REFTIME);
 
 		return list;
 	}
 	
 	@Override
 	@Performance
 	public Collection<DealerIncomeExpenseFact> getDealerIncomeExpenseFacts(
 			Integer year, Option<Integer> lessThanMonthOfYear,
 			Collection<Integer> departmentID, Collection<Integer> itemSource,
 			Collection<String> itemCategory, Collection<String> itemName, Collection<Long> itemID,
 			Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTimeLessThanGivenMonth(year,
 				lessThanMonthOfYear);
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerIncomeExpenseFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerIncomeExpenseFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (!departmentID.isEmpty()) {
 			criteria.add(Restrictions.in("departmentID", departmentID));
 		}
 		if (!itemID.isEmpty()) {
 			criteria.add(Restrictions.in("itemID", itemID));
 		}
 		if (!itemCategory.isEmpty() || !itemSource.isEmpty() || !itemName.isEmpty()) {
 			final Collection<ReportItem> items = getReportItem(itemCategory, itemName, itemSource);
			criteria.add(Restrictions.in("itemID", Lambda.extractProperty(items, "id")));
 		}
 		
 		@SuppressWarnings("unchecked")
 		final List<DealerIncomeExpenseFact> list = criteria.list();
 		session.disableFilter(DealerIncomeExpenseFact.FILTER_REFTIME);
 
 		return list;
 	}
 
 	@Override
 	@Performance
 	public Collection<DealerIncomeRevenueFact> getDealerIncomeRevenueFacts(
 			Integer year, Collection<Integer> monthOfYear,
 			Collection<Integer> departmentID, Collection<Integer> itemSource,
 			Collection<String> itemCategory, Collection<Long> itemID,
 			Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTime(year,
 				monthOfYear);
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerIncomeRevenueFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerIncomeRevenueFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (!departmentID.isEmpty()) {
 			criteria.add(Restrictions.in("departmentID", departmentID));
 		}
 		if (!itemID.isEmpty()) {
 			criteria.add(Restrictions.in("itemID", itemID));
 		}
 		if (!itemCategory.isEmpty() || !itemSource.isEmpty()) {
 			final Collection<String> itemName = Lists.newArrayList();
 			final Collection<ReportItem> items = getReportItem(itemCategory, itemName, itemSource);
			criteria.add(Restrictions.in("itemID", Lambda.extractProperty(items, "id")));
 		}
 		
 		@SuppressWarnings("unchecked")
 		final List<DealerIncomeRevenueFact> list = criteria.list();
 		logger.debug("get revenue facts {}", list);
 		session.disableFilter(DealerIncomeRevenueFact.FILTER_REFTIME);
 
 		return list;
 	}
 	
 	@Override
 	@Performance
 	public Collection<DealerIncomeRevenueFact> getDealerIncomeRevenueFacts(
 			Integer year, Option<Integer> lessThanMonthOfYear,
 			Collection<Integer> departmentID, Collection<Integer> itemSource,
 			Collection<String> itemCategory, Collection<Long> itemID,
 			Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTimeLessThanGivenMonth(year,
 				lessThanMonthOfYear);
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerIncomeRevenueFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerIncomeRevenueFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (!departmentID.isEmpty()) {
 			criteria.add(Restrictions.in("departmentID", departmentID));
 		}
 		if (!itemID.isEmpty()) {
 			criteria.add(Restrictions.in("itemID", itemID));
 		}
 		if (!itemCategory.isEmpty() || !itemSource.isEmpty()) {
 			final Collection<String> itemName = Lists.newArrayList();
 			final Collection<ReportItem> items = getReportItem(itemCategory, itemName, itemSource);
 			if ( items.isEmpty() ) {
 				criteria.add(Restrictions.in("itemID", Lists.newArrayList(new Long[]{-9999L})));
 			} else {
 				criteria.add(Restrictions.in("itemID", Lambda.extractProperty(items, "id")));
 			}
 		}
 		@SuppressWarnings("unchecked")
 		final List<DealerIncomeRevenueFact> list = criteria.list();
 		logger.debug("get revenue facts {}", list);
 		session.disableFilter(DealerIncomeRevenueFact.FILTER_REFTIME);
 
 		return list;
 	}
 
 	@Override
 	public Option<ReportItem> getReportItem(String itemName, String source) {
 		final Session session = sessionFactory.getCurrentSession();
 		final Integer reportItemSource = refDataDAL
 				.getEnumValue("ReportItemSource", source).some().getValue();
 		@SuppressWarnings("unchecked")
 		final List<ReportItem> reportItems = session
 				.createCriteria(ReportItem.class)
 				.add(Restrictions.eq("name", itemName))
 				.add(Restrictions.eq("itemSource", reportItemSource)).list();
 
 		return Option.<ReportItem> iif(!reportItems.isEmpty(),
 				new P1<ReportItem>() {
 
 					@Override
 					public ReportItem _1() {
 						return reportItems.get(0);
 					}
 
 				});
 	}
 
 	@Override
 	public void importHRAllocation(final LocalDate validDate) {
 		logger.info("Importing HR allocation to income expense");
 
 		final Collection<HumanResourceAllocation> list = incomeJournalDAL.getHumanResourceAllocation(
 				validDate, Utils.currentTimestamp());
 
 		final List<DealerHRAllocationFact> facts = Lists.newArrayList();
 		for (final HumanResourceAllocation journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerHRAllocationFact fact = new DealerHRAllocationFact();
 			fact.setAllocation(journal.getAllocation());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "HumanResourceAllocation").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(), refDataDAL
 											.getJobPosition(journal.getId())
 											.some().getName(), "HumanResourceAllocation",
 									null);
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDepartmentID(journal.getDepartmentID()); 
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 		logger.info("To persist facts: " + facts);
 		this.saveDealerHRAllocationFacts(facts);
 	}
 
 	@Override
 	public void saveDealerHRAllocationFacts(
 			Collection<DealerHRAllocationFact> journals) {
 		final Session session = sessionFactory.getCurrentSession();
 		for (final DealerHRAllocationFact newJournal : journals) {
 			// check whether this journal has been inserted before
 			session.enableFilter(DealerHRAllocationFact.FILTER)
 					.setParameter("timeID", newJournal.getTimeID())
 					.setParameter("itemID", newJournal.getItemID())
 					.setParameter("dealerID", newJournal.getDealerID())
 					.setParameter("departmentID", newJournal.getDepartmentID())
 					.setParameter("referenceTime", Utils.currentTimestamp()); // we
 																				// are
 																				// only
 																				// interested
 																				// in
 																				// latest
 																				// record
 			@SuppressWarnings("unchecked")
 			final List<DealerHRAllocationFact> list = session.createCriteria(
 					DealerHRAllocationFact.class).list();
 			boolean isPersisted = false;
 			if (!list.isEmpty()) {
 				for (final DealerHRAllocationFact oldJournal : list) {
 					// if we get here, it means we have inserted this fact
 					// before
 					if (oldJournal.getTimestamp().isBefore(
 							newJournal.getTimestamp())) {
 						oldJournal.setTimeEnd(newJournal.getTimestamp());
 						session.saveOrUpdate(oldJournal);
 					}
 					if (oldJournal.getTimestamp().equals(
 							newJournal.getTimestamp())) {
 						isPersisted = true;
 					}
 				}
 				session.flush();
 			} else {
 				session.save(newJournal);
 			}
 			if (!isPersisted) {
 				session.save(newJournal);
 			}
 			session.disableFilter(DealerHRAllocationFact.FILTER);
 
 			session.flush();
 		}
 	}
 
 	@Override
 	public Collection<DealerHRAllocationFact> getDealerHRAllocationFacts(
 			Integer year, Integer monthOfYear, Option<Integer> departmentID,
 			Option<Integer> itemID, Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTime(year,
 				Option.fromNull(monthOfYear));
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerHRAllocationFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerHRAllocationFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (departmentID.isSome()) {
 			criteria.add(Restrictions.eq("departmentID", departmentID.some()));
 		}
 		if (itemID.isSome()) {
 			final Option<ReportItem> item = getReportItem(itemID.some(), "HumanResourceAllocation");
 			criteria.add(Restrictions.eq("itemID", item.some().getId()));
 		}
 		
 		@SuppressWarnings("unchecked")
 		final List<DealerHRAllocationFact> list = criteria.list();
 		logger.debug("get hr allocation facts {}", list);
 		session.disableFilter(DealerHRAllocationFact.FILTER_REFTIME);
 
 		return list;
 	}
 
 	@Override
 	public void saveDealerAccountReceivableFacts(
 			final Collection<DealerAccountReceivableFact> journals) {
 		final Session session = sessionFactory.getCurrentSession();
 		for (final DealerAccountReceivableFact newJournal : journals) {
 			// check whether this journal has been inserted before
 			session.enableFilter(DealerAccountReceivableFact.FILTER)
 					.setParameter("timeID", newJournal.getTimeID())
 					.setParameter("itemID", newJournal.getItemID())
 					.setParameter("dealerID", newJournal.getDealerID())
 					.setParameter("durationID", newJournal.getDurationID())
 					.setParameter("referenceTime", Utils.currentTimestamp()); // we
 																				// are
 																				// only
 																				// interested
 																				// in
 																				// latest
 																				// record
 			@SuppressWarnings("unchecked")
 			final List<DealerAccountReceivableFact> list = session.createCriteria(
 					DealerAccountReceivableFact.class).list();
 			boolean isPersisted = false;
 			if (!list.isEmpty()) {
 				for (final DealerAccountReceivableFact oldJournal : list) {
 					// if we get here, it means we have inserted this fact
 					// before
 					if (oldJournal.getTimestamp().isBefore(
 							newJournal.getTimestamp())) {
 						oldJournal.setTimeEnd(newJournal.getTimestamp());
 						session.saveOrUpdate(oldJournal);
 					}
 					if (oldJournal.getTimestamp().equals(
 							newJournal.getTimestamp())) {
 						isPersisted = true;
 					}
 				}
 				session.flush();
 			} else {
 				session.save(newJournal);
 			}
 			if (!isPersisted) {
 				session.save(newJournal);
 			}
 			session.disableFilter(DealerAccountReceivableFact.FILTER);
 
 			session.flush();
 		}
 	}
 
 	@Override
 	public void importAccountReceivable(final LocalDate validDate) {
 		logger.info("Importing account receivable");
 
 		final Collection<AccountReceivableDuration> list = incomeJournalDAL.getAccountReceivableDuration(
 				validDate, Utils.currentTimestamp());
 
 		final List<DealerAccountReceivableFact> facts = Lists.newArrayList();
 		for (final AccountReceivableDuration journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerAccountReceivableFact fact = new DealerAccountReceivableFact();
 			fact.setAmount(journal.getAmount());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "AccountReceivableDuration").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(), refDataDAL
 											.getAccountReceivableDurationItem(journal.getId())
 											.some().getName(), "AccountReceivableDuration",
 									null);
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDurationID(journal.getDurationID()); 
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 		logger.info("To persist facts: " + facts);
 		this.saveDealerAccountReceivableFacts(facts);
 	}
 
 	@Override
 	public Collection<DealerAccountReceivableFact> getDealerAccountReceivableFacts(
 			final Integer year, final Integer monthOfYear, final Option<Integer> durationID,
 			final Collection<String> itemName, final Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTime(year,
 				Option.fromNull(monthOfYear));
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerAccountReceivableFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerAccountReceivableFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (durationID.isSome()) {
 			criteria.add(Restrictions.eq("durationID", durationID.some()));
 		}
 		if ( !itemName.isEmpty() ) {
 			final List<String> emptyCategory = Lists.newArrayList();
 			final List<Integer> itemSource = Lists.newArrayList(new Integer[]{refDataDAL
 					.getEnumValue("ReportItemSource", "AccountReceivableDuration").some().getValue()});
 			final Collection<ReportItem> items = getReportItem(emptyCategory, itemName, itemSource);
 			if ( items.isEmpty() ) {
 				criteria.add(Restrictions.in("itemID", Lists.newArrayList(new Long[]{-9999L})));
 			} else {
 				criteria.add(Restrictions.in("itemID", Lambda.extractProperty(items, "id")));
 			}
 		}
 		
 		@SuppressWarnings("unchecked")
 		final List<DealerAccountReceivableFact> list = criteria.list();
 		logger.debug("get facts {}", list);
 		session.disableFilter(DealerAccountReceivableFact.FILTER_REFTIME);
 
 		return list;
 	}
 	
 	@Override
 	public void importInventory(final LocalDate validDate) {
 		logger.info("Importing inventory duration");
 
 		final Collection<InventoryDuration> list = incomeJournalDAL.getInventoryDuration(
 				validDate, Utils.currentTimestamp());
 
 		final List<DealerInventoryFact> facts = Lists.newArrayList();
 		for (final InventoryDuration journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 
 					});
 
 			final DealerInventoryFact fact = new DealerInventoryFact();
 			fact.setAmount(journal.getAmount());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "InventoryDuration").orElse(
 					new P1<Option<ReportItem>>() {
 
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(), refDataDAL
 											.getJobPosition(journal.getId())
 											.some().getName(), "InventoryDuration",
 									null);
 						}
 
 					});
 
 			fact.setDealerID(journal.getDealerID());
 			fact.setDurationID(journal.getDurationID()); 
 			fact.setDepartmentID(journal.getDepartmentID()); 
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 		logger.info("To persist facts: " + facts);
 		this.saveDealerInventoryFacts(facts);
 	}
 	
 	@Override
 	public void saveDealerInventoryFacts(
 			final Collection<DealerInventoryFact> journals) {
 		final Session session = sessionFactory.getCurrentSession();
 		for (final DealerInventoryFact newJournal : journals) {
 			// check whether this journal has been inserted before
 			session.enableFilter(DealerInventoryFact.FILTER)
 					.setParameter("timeID", newJournal.getTimeID())
 					.setParameter("itemID", newJournal.getItemID())
 					.setParameter("dealerID", newJournal.getDealerID())
 					.setParameter("departmentID", newJournal.getDepartmentID())
 					.setParameter("durationID", newJournal.getDurationID())
 					.setParameter("referenceTime", Utils.currentTimestamp()); // we
 																				// are
 																				// only
 																				// interested
 																				// in
 																				// latest
 																				// record
 			@SuppressWarnings("unchecked")
 			final List<DealerInventoryFact> list = session.createCriteria(
 					DealerInventoryFact.class).list();
 			boolean isPersisted = false;
 			if (!list.isEmpty()) {
 				for (final DealerInventoryFact oldJournal : list) {
 					// if we get here, it means we have inserted this fact
 					// before
 					if (oldJournal.getTimestamp().isBefore(
 							newJournal.getTimestamp())) {
 						oldJournal.setTimeEnd(newJournal.getTimestamp());
 						session.saveOrUpdate(oldJournal);
 					}
 					if (oldJournal.getTimestamp().equals(
 							newJournal.getTimestamp())) {
 						isPersisted = true;
 					}
 				}
 				session.flush();
 			} else {
 				session.save(newJournal);
 			}
 			if (!isPersisted) {
 				session.save(newJournal);
 			}
 			session.disableFilter(DealerInventoryFact.FILTER);
 
 			session.flush();
 		}
 	}
 	
 	@Override
 	public Collection<DealerInventoryFact> getDealerInventoryFacts(
 			final Integer year, final Integer monthOfYear, final Option<Integer> departmentID, final Option<Integer> durationID,
 			final Collection<String> itemName, final Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		Preconditions.checkNotNull(monthOfYear, "month can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTime(year,
 				Option.fromNull(monthOfYear));
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerInventoryFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerInventoryFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (departmentID.isSome()) {
 			criteria.add(Restrictions.eq("departmentID", departmentID.some()));
 		}
 		if (durationID.isSome()) {
 			criteria.add(Restrictions.eq("durationID", durationID.some()));
 		}
 		if ( !itemName.isEmpty() ) {
 			final List<String> emptyCategory = Lists.newArrayList();
 			final List<Integer> itemSource = Lists.newArrayList(new Integer[]{refDataDAL
 					.getEnumValue("ReportItemSource", "InventoryDuration").some().getValue()});
 			final Collection<ReportItem> items = getReportItem(emptyCategory, itemName, itemSource);
 			if ( items.isEmpty() ) {
 				criteria.add(Restrictions.in("itemID", Lists.newArrayList(new Long[]{-9999L})));
 			} else {
 				criteria.add(Restrictions.in("itemID", Lambda.extractProperty(items, "id")));
 			}
 		}
 		
 		@SuppressWarnings("unchecked")
 		final List<DealerInventoryFact> list = criteria.list();
 		logger.debug("get facts {}", list);
 		session.disableFilter(DealerInventoryFact.FILTER_REFTIME);
 
 		return list;
 	}
 
 	@Override
 	public void importEmployeeFee(final LocalDate validDate) {
 		logger.info("Importing employee fee");
 
 		final Collection<EmployeeFee> list = incomeJournalDAL.getEmployeeFee(
 				validDate, Utils.currentTimestamp());
 		final List<DealerEmployeeFeeFact> facts = Lists.newArrayList();
 		for (final EmployeeFee journal : list) {
 			// verify report time
 			final Option<ReportTime> reportTime = this.getReportTime(validDate)
 					.orElse(new P1<Option<ReportTime>>() {
 						@Override
 						public Option<ReportTime> _1() {
 							return ReportDAOImpl.this.addReportTime(validDate);
 						}
 					});
 
 			final DealerEmployeeFeeFact fact = new DealerEmployeeFeeFact();
 			fact.setAmount(journal.getAmount());
 			fact.setTimeID(reportTime.some().getId());
 			// verify report item here
 			final Option<ReportItem> reportItem = this.getReportItem(
 					journal.getId(), "EmployeeFee").orElse(
 					new P1<Option<ReportItem>>() {
 						@Override
 						public Option<ReportItem> _1() {
 							return ReportDAOImpl.this.addReportItem(
 									journal.getId(),
 									refDataDAL
 											.getEmployeeFeeItem(journal.getId())
 											.some().getName(), "EmployeeFee",
 									null);
 						}
 
 					});
 			fact.setDealerID(journal.getDealerID());
 			fact.setDepartmentID(journal.getDepartmentID());
 			fact.setItemID(reportItem.some().getId());
 			fact.setTimestamp(journal.getTimestamp());
 			fact.setTimeEnd(journal.getTimeEnd());
 			facts.add(fact);
 		}
 		logger.info("To persist facts: {}", facts);
 		this.saveDealerEmployeeFeeFacts(facts);
 	}
 
 	@Override
 	public void saveDealerEmployeeFeeFacts(
 			final Collection<DealerEmployeeFeeFact> journals) {
 		final Session session = sessionFactory.getCurrentSession();
 		for (final DealerEmployeeFeeFact newJournal : journals) {
 			// check whether this journal has been inserted before
 			session.enableFilter(DealerEmployeeFeeFact.FILTER)
 					.setParameter("timeID", newJournal.getTimeID())
 					.setParameter("itemID", newJournal.getItemID())
 					.setParameter("dealerID", newJournal.getDealerID())
 					.setParameter("departmentID", newJournal.getDepartmentID())
 					.setParameter("referenceTime", Utils.currentTimestamp());
 			@SuppressWarnings("unchecked")
 			final List<DealerEmployeeFeeFact> list = session.createCriteria(
 					DealerEmployeeFeeFact.class).list();
 			boolean isPersisted = false;
 			if (!list.isEmpty()) {
 				for (final DealerEmployeeFeeFact oldJournal : list) {
 					// if we get here, it means we have inserted this fact
 					// before
 					if (oldJournal.getTimestamp().isBefore(
 							newJournal.getTimestamp())) {
 						oldJournal.setTimeEnd(newJournal.getTimestamp());
 						session.saveOrUpdate(oldJournal);
 					}
 					if (oldJournal.getTimestamp().equals(
 							newJournal.getTimestamp())) {
 						isPersisted = true;
 					}
 				}
 				session.flush();
 			} else {
 				session.save(newJournal);
 			}
 			if (!isPersisted) {
 				session.save(newJournal);
 			}
 			session.disableFilter(DealerEmployeeFeeFact.FILTER);
 
 			session.flush();
 		}
 	}
 
 	@Override
 	public Collection<DealerEmployeeFeeFact> getDealerEmployeeFeeFacts(
 			final Integer year, final Option<Integer> lessThanMonthOfYear,
 			final Option<Integer> departmentID,
 			final Collection<String> itemName,
 			final Collection<Integer> dealerID) {
 		Preconditions.checkNotNull(year, "year can't be null");
 		final Session session = sessionFactory.getCurrentSession();
 		final Collection<ReportTime> reportTimes = getReportTimeLessThanGivenMonth(
 				year, lessThanMonthOfYear);
 		if (reportTimes.isEmpty()) {
 			return Lists.newArrayList();
 		}
 
 		session.enableFilter(DealerEmployeeFeeFact.FILTER_REFTIME)
 				.setParameter("referenceTime", Utils.currentTimestamp());
 
 		final Criteria criteria = session
 				.createCriteria(DealerEmployeeFeeFact.class);
 		criteria.add(Restrictions.in("timeID",
 				Lambda.extractProperty(reportTimes, "id")));
 		if (!dealerID.isEmpty()) {
 			criteria.add(Restrictions.in("dealerID", dealerID));
 		}
 		if (departmentID.isSome()) {
 			criteria.add(Restrictions.eq("departmentID", departmentID.some()));
 		}
 		if (!itemName.isEmpty()) {
 			final List<String> emptyCategory = Lists.newArrayList();
 			final List<Integer> itemSource = Lists
 					.newArrayList(new Integer[] { refDataDAL
 							.getEnumValue("ReportItemSource", "EmployeeFee")
 							.some().getValue() });
 			final Collection<ReportItem> items = getReportItem(emptyCategory,
 					itemName, itemSource);
 			if (items.isEmpty()) {
 				criteria.add(Restrictions.in("itemID",
 						Lists.newArrayList(new Long[] { -9999L })));
 			} else {
 				criteria.add(Restrictions.in("itemID",
 						Lambda.extractProperty(items, "id")));
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		final List<DealerEmployeeFeeFact> list = criteria.list();
 		logger.debug("get facts {}", list);
 		session.disableFilter(DealerEmployeeFeeFact.FILTER_REFTIME);
 
 		return list;
 	}
 	
 }
