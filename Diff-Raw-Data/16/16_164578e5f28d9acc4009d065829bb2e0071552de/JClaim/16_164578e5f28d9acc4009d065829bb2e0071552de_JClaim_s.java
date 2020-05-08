 package models;
 
 import com.github.jmkgreen.morphia.annotations.*;
 import com.github.jmkgreen.morphia.mapping.Mapper;
 import com.github.jmkgreen.morphia.query.Query;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.mongodb.WriteConcern;
 import com.mongodb.WriteResult;
 import constants.ClaimType;
 import constants.MissionAllowanceType;
 import leodagdag.play2morphia.Model;
 import leodagdag.play2morphia.MorphiaPlugin;
 import org.apache.commons.lang3.StringUtils;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author f.patin
  */
 @Entity("Claim")
 @Indexes({
 	         @Index("userId, year, month")
 })
 public class JClaim extends Model implements MongoModel {
 
 	@Id
 	public ObjectId id;
 	public ObjectId userId;
 	public Integer year;
 	public Integer month;
 	@Transient
 	public DateTime date;
 	public ObjectId missionId;
 	public String claimType;
 	@Transient
 	public BigDecimal amount = BigDecimal.ZERO;
 	@Transient
 	public BigDecimal kilometer;
 	@Transient
 	public BigDecimal kilometerAmount = BigDecimal.ZERO;
 	public String journey;
 	public String comment;
 	public String _amount;
 	public String _kilometer;
 	private String _kilometerAmount;
 	private Date _date;
 
 	public JClaim() {
 	}
 
 	public JClaim(final ObjectId userId, final DateTime date, final ObjectId missionId) {
 		this.userId = userId;
 		this.date = date;
 		this.missionId = missionId;
 	}
 
 	private static Query<JClaim> q() {
 		return MorphiaPlugin.ds().createQuery(JClaim.class);
 	}
 
 	private static Query<JClaim> queryToFindMe(final ObjectId id) {
 		return q().field(Mapper.ID_KEY).equal(id);
 	}
 
 	private static WriteResult delete(final Query<JClaim> q) {
 		return MorphiaPlugin.ds().delete(q, WriteConcern.ACKNOWLEDGED);
 	}
 
 	private static void addMissionAllowance(final List<JDay> days) {
 		Collections.sort(days, JDay.BY_DATE);
 		final List<JAffectedMission> affectedMissions = JUser.affectedMissions(days.get(0).userId, days.get(0).date, days.get(days.size() - 1).date);
 		final List<JClaim> claims = Lists.newArrayList();
 		for(final JDay day : days) {
 			claims.addAll(Collections2.transform(day.missionIds(), new Function<ObjectId, JClaim>() {
 				@Nullable
 				@Override
 				public JClaim apply(@Nullable final ObjectId missionId) {
 					return JMission.isClaimable(missionId) ? new JClaim(day.userId, day.date, missionId).computeMissionAllowance(affectedMissions) : null;
 				}
 			}));
 		}
 		claims.removeAll(Collections.singletonList(null));
 		MorphiaPlugin.ds().save(claims, WriteConcern.ACKNOWLEDGED);
 	}
 
 	private JClaim computeMissionAllowance(final List<JAffectedMission> affectedMissions) {
 		//final JMission mission = JMission.fetch(this.missionId);
 		//final ObjectId missionId = this.missionId;
 		final JAffectedMission affectedMission = Iterables.find(affectedMissions, new Predicate<JAffectedMission>() {
 			@Override
 			public boolean apply(@Nullable final JAffectedMission affectedMission) {
 				return missionId.equals(affectedMission.missionId);
 			}
 		});
 		switch(MissionAllowanceType.valueOf(affectedMission.allowanceType)) {
 			case ZONE:
 				this.claimType = ClaimType.ZONE_FEE.name();
 				this.amount = JParameter.zoneAmount(date);
 				return this;
 			case FIXED:
 				this.claimType = ClaimType.FIXED_FEE.name();
 				this.amount = affectedMission.feeAmount;
 				return this;
 			case NONE:
 			default:
 				return null;
 		}
 
 	}
 
 	@SuppressWarnings({"unused"})
 	@PrePersist
 	private void prePersist() {
 		if(date != null) {
 			_date = date.toDate();
 			year = new DateTime(_date).getYear();
 			month = new DateTime(_date).getMonthOfYear();
 		}
 		if(amount != null) {
 			_amount = amount.toPlainString();
 		}
 		if(kilometer != null) {
 			_kilometer = kilometer.toPlainString();
 			computeKilometerAmount();
 		}
 	}
 
 	private void computeKilometerAmount() {
 		if(this.kilometer != null) {
 			final JVehicle vehicle = JVehicle.active(this.userId);
 			final BigDecimal coefficient = JParameter.coefficient(vehicle, this.date);
 			kilometerAmount = kilometer.multiply(coefficient);
 			_kilometerAmount = kilometerAmount.toPlainString();
 		}
 	}
 
 	@SuppressWarnings({"unused"})
 	@PostLoad
 	private void postLoad() {
 		if(_date != null) {
 			date = new DateTime(_date.getTime());
 		}
 		if(StringUtils.isNotBlank(_amount)) {
 			amount = new BigDecimal(_amount);
 		}
 		if(StringUtils.isNotBlank(_kilometer)) {
 			kilometer = new BigDecimal(_kilometer);
 		}
 		if(StringUtils.isNotBlank(_kilometerAmount)) {
 			kilometerAmount = new BigDecimal(_kilometerAmount);
 		}
 	}
 
 	public static List<JClaim> create(final List<JClaim> claims) {
 		MorphiaPlugin.ds().save(claims);
 		return claims;
 	}
 
 	public static ObjectId delete(final String id) {
 		delete(queryToFindMe(ObjectId.massageToObjectId(id)));
 		return ObjectId.massageToObjectId(id);
 	}
 
 	public static ImmutableList<JClaim> history(final ObjectId userId, final Integer year, final Integer month) {
 		return ImmutableList.copyOf(q()
 			                            .field("userId").equal(userId)
 			                            .field("year").equal(year)
 			                            .field("month").equal(month)
 			                            .asList());
 	}
 
 	public static List<JClaim> synthesis(final String userId, final Integer year, final Integer month) {
 		return synthesis(ObjectId.massageToObjectId(userId), year, month);
 	}
 
 	public static List<JClaim> synthesis(final ObjectId userId, final Integer year, final Integer month) {
 		return q()
 			       .field("userId").equal(ObjectId.massageToObjectId(userId))
 			       .field("year").equal(year)
 			       .field("month").equal(month)
 			       .asList();
 	}
 
 	public static void computeMissionAllowance(final ObjectId userId, final JDay day) {
 		computeMissionAllowance(userId, Lists.newArrayList(day));
 	}
 
 	public static void computeMissionAllowance(final ObjectId userId, final List<JDay> days) {
 		final List<DateTime> dts = Lists.newArrayList(Collections2.transform(days, new Function<JDay, DateTime>() {
 			@Nullable
 			@Override
 			public DateTime apply(@Nullable final JDay day) {
 				return day.date;
 			}
 		}));
 		deleteMissionAllowance(userId, dts);
 		addMissionAllowance(days);
 	}
 
 	public static WriteResult deleteMissionAllowance(final ObjectId userId, final DateTime dt) {
 		return deleteMissionAllowance(userId, Lists.newArrayList(dt));
 	}
 
 	public static WriteResult deleteMissionAllowance(final ObjectId userId, final List<DateTime> dts) {
 		final Collection<Date> dates = Collections2.transform(dts, new Function<DateTime, Date>() {
 			@Nullable
 			@Override
 			public Date apply(@Nullable final DateTime dt) {
 				return dt.toDate();
 			}
 		});
 		final Query<JClaim> q = q()
 			                        .field("userId").equal(userId)
 			                        .field("_date").in(dates);
 		q.or(
 			    q.criteria("claimType").equal(ClaimType.FIXED_FEE.name()),
 			    q.criteria("claimType").equal(ClaimType.ZONE_FEE.name())
 		);
 		return delete(q);
 	}
 
 	@Override
 	public ObjectId id() {
 		return this.id;
 	}
 
 }
