 package models;
 
 import com.github.jmkgreen.morphia.Key;
 import com.github.jmkgreen.morphia.annotations.*;
 import com.github.jmkgreen.morphia.mapping.Mapper;
 import com.github.jmkgreen.morphia.query.Query;
 import com.github.jmkgreen.morphia.query.UpdateOperations;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.mongodb.WriteConcern;
 import com.mongodb.WriteResult;
 import constants.MomentOfDay;
 import exceptions.IllegalDayOperation;
 import leodagdag.play2morphia.Model;
 import leodagdag.play2morphia.MorphiaPlugin;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.map.annotate.JsonDeserialize;
 import org.joda.time.DateTime;
 import play.Logger;
 import play.libs.F;
 import utils.business.AbsenceUtils;
 import utils.deserializer.DateTimeDeserializer;
 import utils.time.TimeUtils;
 import utils.transformer.Transformer;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
 import java.util.*;
 
 /**
  * @author f.patin
  */
 @Entity(JDay.COLLECTION_NAME)
 @Indexes({
 	         @Index("craId"),
 	         @Index("_date"),
 	         @Index("userId, year, month"),
 	         @Index("userId, _date")
 })
 public class JDay extends Model implements MongoModel {
 
 	public static final Comparator<JDay> BY_DATE = new Comparator<JDay>() {
 		@Override
 		public int compare(final JDay day1, final JDay day2) {
 			return day1.date.compareTo(day2.date);
 		}
 	};
 
 	public static final String COLLECTION_NAME = "Day";
 	@Id
 	public ObjectId id;
 	public ObjectId craId;
 	public ObjectId userId;
 	@Transient
 	@JsonDeserialize(using = DateTimeDeserializer.class)
 	public DateTime date;
 	public Integer year;
 	public Integer month;
 	public Integer week;
 	@Embedded
 	public JHalfDay morning;
 	@Embedded
 	public JHalfDay afternoon;
 	public String comment;
 	private Date _date;
 
 	public JDay() {
 	}
 
 	public JDay(final Long date) {
 		this(new DateTime(date));
 	}
 
 	public JDay(final DateTime date) {
 		this.date = date;
 		this.month = date.getMonthOfYear();
 		this.year = date.getYear();
 		this.week = date.getWeekOfWeekyear();
 	}
 
 	private static Query<JDay> q() {
 		return MorphiaPlugin.ds().createQuery(JDay.class);
 	}
 
 	private static List<JDay> filterByMission(final List<JDay> days, final ObjectId missionId) {
 		final List<JDay> result= Lists.newArrayList(Collections2.transform(days, new Function<JDay, JDay>() {
 			@Nullable
 			@Override
 			public JDay apply(@Nullable final JDay day) {
 				if(day.missionIds().contains(missionId)) {
 					if(day.morning != null) {
 						if(day.morning.isSpecial()) {
 							day.morning.periods.retainAll(Lists.newArrayList(Iterables.filter(day.morning.periods, new Predicate<JPeriod>() {
 								@Override
 								public boolean apply(@Nullable final JPeriod period) {
 									return period.missionId.equals(missionId);
 								}
 							})));
 						} else if(!missionId.equals(day.morning.missionId)) {
 							day.morning = null;
 						}
 					}
 					if(day.afternoon != null) {
 						if(day.afternoon.isSpecial()) {
 							day.afternoon.periods.retainAll(Lists.newArrayList(Iterables.filter(day.afternoon.periods, new Predicate<JPeriod>() {
 								@Override
 								public boolean apply(@Nullable final JPeriod period) {
 									return period.missionId.equals(missionId);
 								}
 							})));
 						} else if(!missionId.equals(day.afternoon.missionId)) {
 							day.afternoon = null;
 						}
 					}
 				} else {
 					day.morning = null;
 					day.afternoon = null;
 				}
 				if(day.morning == null && day.afternoon == null) {
 					return null;
 				}
 				return day;
 			}
 		}));
 		result.removeAll(Collections.singletonList(null));
 		return result;
 	}
 
 	@SuppressWarnings({"unused"})
 	@PrePersist
 	private void prePersist() {
 		if(date != null) {
 			_date = date.toDate();
 			year = date.getYear();
 			month = date.getMonthOfYear();
 			week = date.getWeekOfWeekyear();
 		}
 	}
 
 	@SuppressWarnings({"unused"})
 	@PostLoad
 	private void postLoad() {
 		if(_date != null) {
 			date = new DateTime(_date.getTime());
 			if(year == null) {
 				year = date.getYear();
 			}
 			if(month == null) {
 				month = date.getMonthOfYear();
 			}
 			if(week == null) {
 				week = date.getWeekOfWeekyear();
 			}
 		}
 	}
 
 	public static List<JDay> find(final ObjectId craId, final ObjectId userId, final Integer year, final Integer month, final Boolean withPastAndFuture) {
 		final List<DateTime> allDates = Lists.newArrayList(TimeUtils.getDaysOfMonth(year, month, withPastAndFuture));
 		final List<JDay> days = Lists.newArrayList();
 		if(craId != null) {
 			final Query<JDay> q = q()
 				                      .field("userId").equal(userId)
 				                      .field("_date").greaterThanOrEq(allDates.get(0).toDate())
 				                      .field("_date").lessThanOrEq(allDates.get(allDates.size() - 1).toDate());

			Logger.trace(String.format("%s.%s %s",JDay.class.getName(),"find", q.toString()) );
 			days.addAll(q.asList());
 		} else {
 			days.addAll(Collections2.transform(allDates, new Function<DateTime, JDay>() {
 				@Nullable
 				@Override
 				public JDay apply(@Nullable final DateTime dt) {
 					return new JDay(dt);
 				}
 			}));
 		}
 		for(final DateTime dt : allDates) {
 			final JDay existDay = Iterables.find(days, new Predicate<JDay>() {
 				@Override
 				public boolean apply(@Nullable final JDay jDay) {
 					return jDay.date.isEqual(dt);
 				}
 			}, null);
 			if(existDay == null) {
 				days.add(new JDay(dt));
 			}
 		}
 		Collections.sort(days, new Comparator<JDay>() {
 			@Override
 			public int compare(final JDay d1, final JDay d2) {
 				return d1.date.compareTo(d2.date);
 			}
 		});
 		return days;
 	}
 
 	public static List<JDay> find(final ObjectId userId, final Set<DateTime> dateTimes) {
 		return q()
 			       .field("userId").equal(userId)
 			       .field("_date").in(Transformer.fromDateTimes(dateTimes))
 			       .asList();
 	}
 
 	public static List<JDay> fetch(final JCra cra) {
 		return q()
 			       .field("craId").equal(cra.id)
 			       .order("_date").asList();
 	}
 
 	public static List<JDay> fetch(final JCra cra, final JMission mission) {
 		return filterByMission(fetch(cra), mission.id);
 	}
 
 	public static JHalfDay findHalfDay(final ObjectId craId, final DateTime dateTime, final MomentOfDay momentOfDay) {
 		final JDay day = q()
 			                 .field("craId").equal(craId)
 			                 .field("_date").equal(dateTime.toDate())
 			                 .retrievedFields(true, momentOfDay.name())
 			                 .disableValidation()
 			                 .get();
 		switch(momentOfDay) {
 			case morning:
 				return day.morning;
 			case afternoon:
 				return day.afternoon;
 			default:
 				return null;
 		}
 	}
 
 	public static List<JDay> createDays(final ObjectId craId, final List<JDay> days) throws IllegalDayOperation {
 		final List<Date> dates = Transformer.toDates(days);
 		// Extract corresponding days in database
 		final List<JDay> oldDays = q()
 			                           .field("craId").equal(craId)
 			                           .field("_date").in(dates)
 			                           .asList();
 		if(CollectionUtils.isNotEmpty(oldDays)) {
 			final List<JDay> holidays = Transformer.removeHolidays(oldDays);
 			// Report holidays in new days
 			for(JDay h : holidays) {
 				for(JDay d : days) {
 					if(h.date.isEqual(d.date)) {
 						d.morning = (h.morning != null) ? h.morning : d.morning;
 						d.afternoon = (h.afternoon != null) ? h.afternoon : d.afternoon;
 					}
 				}
 			}
 			// delete existing days (non Holidays)
 			final List<ObjectId> oldDaysIds = Transformer.toObjectId(new ArrayList<MongoModel>(oldDays));
 			MorphiaPlugin.ds().delete(q().field(Mapper.ID_KEY).in(oldDaysIds), WriteConcern.ACKNOWLEDGED);
 		}
 		// create new days
 		final List<JDay> newDays = Transformer.setCraId(days, craId);
 		Iterable<Key<JDay>> keys = MorphiaPlugin.ds().save(newDays, WriteConcern.ACKNOWLEDGED);
 		return MorphiaPlugin.ds().getByKeys(keys);
 	}
 
 	public static List<JDay> addAbsenceDays(final JAbsence absence) {
 		final Map<DateTime, F.Tuple<Boolean, Boolean>> dates = AbsenceUtils.extractDays(absence);
 		final List<JDay> result = Lists.newArrayList();
 		JCra cra = null;
 
 		for(DateTime dt : dates.keySet()) {
 			if(cra == null || (cra.year != dt.getYear() || cra.month != dt.getMonthOfYear())) {
 				cra = JCra.getOrCreate(absence.userId, dt.getYear(), dt.getMonthOfYear());
 			}
 			// Search JDay...
 			JDay day = JDay.find(cra.id, dt);
 			// ...If not exist we create it
 			if(day == null) {
 				day = new JDay(dt);
 				day.craId = cra.id;
 				day.userId = absence.userId;
 			}
 			day.comment = absence.comment;
 			if(Boolean.TRUE.equals(dates.get(dt)._1)) {
 				day.morning = new JHalfDay(MomentOfDay.morning, absence.missionId);
 			}
 			if(Boolean.TRUE.equals(dates.get(dt)._2)) {
 				day.afternoon = new JHalfDay(MomentOfDay.afternoon, absence.missionId);
 			}
 			result.add(day.<JDay>insert());
 		}
 		JClaim.computeMissionAllowance(result.get(0).userId, result);
 		return result;
 	}
 
 	public static JDay find(final ObjectId craId, final DateTime dts) {
 		return q()
 			       .field("craId").equal(craId)
 			       .field("_date").equal(dts.toDate())
 			       .get();
 	}
 
 	public static void deleteAbsenceDays(final JAbsence absence) {
 		final Map<DateTime, F.Tuple<Boolean, Boolean>> dates = AbsenceUtils.extractDays(absence);
 		final ObjectId userId = absence.userId;
 
 		final Set<F.Tuple<Integer, Integer>> yearMonth = Sets.newHashSet();
 		for(DateTime dt : dates.keySet()) {
 			final Query<JDay> q = q()
 				                      .field("userId").equal(userId)
 				                      .field("_date").equal(dt.toDate());
 			if(Boolean.TRUE.equals(dates.get(dt)._1) && Boolean.TRUE.equals(dates.get(dt)._2)) {
 				MorphiaPlugin.ds().delete(q);
 			} else if(Boolean.TRUE.equals(dates.get(dt)._1)) {
 				final UpdateOperations<JDay> ops = MorphiaPlugin.ds().createUpdateOperations(JDay.class)
 					                                   .unset("morning");
 				MorphiaPlugin.ds().update(q, ops, false, WriteConcern.ACKNOWLEDGED);
 			} else {
 				final UpdateOperations<JDay> ops = MorphiaPlugin.ds().createUpdateOperations(JDay.class)
 					                                   .unset("afternoon");
 				MorphiaPlugin.ds().update(q, ops, false, WriteConcern.ACKNOWLEDGED);
 			}
 			// Delete Day if needed
 			final Query<JDay> deleteDay = q()
 				                              .field("userId").equal(userId)
 				                              .field("_date").equal(dt.toDate())
 				                              .field("morning").doesNotExist()
 				                              .field("afternoon").doesNotExist();
 			MorphiaPlugin.ds().delete(deleteDay, WriteConcern.ACKNOWLEDGED);
 			// Save CRA year & month
 			yearMonth.add(F.Tuple(dt.getYear(), dt.getMonthOfYear()));
 		}
 
 		// Delete empty CRA ?
 		for(F.Tuple<Integer, Integer> ym : yearMonth) {
 			final Integer year = ym._1;
 			final Integer month = ym._2;
 			final Query<JDay> q = q()
 				                      .field("userId").equal(userId)
 				                      .field("year").equal(year)
 				                      .field("month").equal(month);
 			if(MorphiaPlugin.ds().getCount(q) == 0) {
 				JCra.delete(userId, year, month);
 			}
 		}
 	}
 
 	public static JDay addPartTime(final ObjectId craId, final ObjectId userId, final DateTime date, final Boolean morning, final Boolean afternoon) {
 		final ObjectId partTimeId = JMission.getPartTimeId();
 
 		JDay day = q()
 			           .field("craId").equal(craId)
 			           .field("userId").equal(userId)
 			           .field("_date").equal(date.toDate())
 			           .get();
 		if(day == null) {
 			day = new JDay(date);
 			day.craId = craId;
 			day.userId = userId;
 		}
 		boolean toSave = false;
 		if(day.morning == null && Boolean.TRUE.equals(morning)) {
 			day.morning = new JHalfDay(MomentOfDay.morning, partTimeId);
 			toSave = true;
 		}
 		if(day.afternoon == null && Boolean.TRUE.equals(afternoon)) {
 			day.afternoon = new JHalfDay(MomentOfDay.afternoon, partTimeId);
 			toSave = true;
 		}
 
 		if(toSave) {
 			MorphiaPlugin.ds().save(day);
 		}
 		return day;
 	}
 
 	public static WriteResult delete(final String craId, final Long date) {
 		final Query<JDay> q = q()
 			                      .field("craId").equal(ObjectId.massageToObjectId(craId))
 			                      .field("_date").equal(new DateTime(date).toDate());
 		return MorphiaPlugin.ds().delete(q, WriteConcern.ACKNOWLEDGED);
 	}
 
 	public static JDay deleteHalfDay(final ObjectId craId, final DateTime dateTime, final MomentOfDay momentOfDay) {
 		final Query<JDay> q = q().field("craId").equal(craId)
 			                      .field("_date").equal(dateTime.toDate());
 		final UpdateOperations<JDay> uop = MorphiaPlugin.ds().createUpdateOperations(JDay.class)
 			                                   .unset(momentOfDay.name());
 
 		return MorphiaPlugin.ds().findAndModify(q, uop, false, false);
 	}
 
 	public static List<JMission> extractMissions(final ObjectId craId) {
 		final List<JDay> days = q().field("craId").equal(craId).asList();
 		final Set<ObjectId> missionIds = Sets.newHashSet();
 		for(JDay day : days) {
 			missionIds.addAll(day.missionIds());
 		}
 		return JMission.fetch(missionIds);
 	}
 
 	public Set<ObjectId> missionIds() {
 		final Set<ObjectId> result = Sets.newHashSet();
 		if(morning != null) {
 			result.addAll(morning.missionIds());
 		}
 		if(afternoon != null) {
 			result.addAll(afternoon.missionIds());
 		}
 		return result;
 	}
 
 	public Boolean isSaturday() {
 		return date != null && TimeUtils.isSaturday(date);
 	}
 
 	public Boolean isSunday() {
 		return date != null && TimeUtils.isSunday(date);
 	}
 
 	public Boolean isDayOff() {
 		return date != null && TimeUtils.isDayOff(date);
 	}
 
 	public Boolean inPastOrFuture(final Integer year, final Integer month) {
 		return year != date.getYear() || month != date.getMonthOfYear();
 	}
 
 	public BigDecimal inGenesisHour() {
 		final BigDecimal morning = this.morning != null ? this.morning.inGenesisHour() : BigDecimal.ZERO;
 		final BigDecimal afternoon = this.afternoon != null ? this.afternoon.inGenesisHour() : BigDecimal.ZERO;
 		return morning.add(afternoon);
 	}
 
 	public BigDecimal inGenesisHour(final JMission customerMission) {
 		final BigDecimal morning = this.morning != null ? this.morning.inGenesisHour(customerMission) : BigDecimal.ZERO;
 		final BigDecimal afternoon = this.afternoon != null ? this.afternoon.inGenesisHour(customerMission) : BigDecimal.ZERO;
 		return morning.add(afternoon);
 	}
 
 	@Override
 	public ObjectId id() {
 		return id;
 	}
 
 }
