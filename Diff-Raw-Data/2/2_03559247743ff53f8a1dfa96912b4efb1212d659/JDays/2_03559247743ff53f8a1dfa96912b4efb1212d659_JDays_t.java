 package controllers;
 
 import caches.ResponseCache;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import constants.MomentOfDay;
 import dto.DayDTO;
 import exceptions.IllegalDayOperation;
 import models.JClaim;
 import models.JCra;
 import models.JDay;
 import models.JHalfDay;
 import models.JMission;
 import models.JPeriod;
 import models.JUser;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import javax.annotation.Nullable;
 import java.util.List;
 
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JDays extends Controller {
 
 	@ResponseCache.NoCacheResponse
 	public static Result fetch(final String craId, final Long date) {
 		final DateTime dt = new DateTime(date);
 		final ObjectId idCra = ObjectId.massageToObjectId(craId);
 
 		JDay day = JDay.find(idCra, dt);
 		final List<ObjectId> missionsIds = Lists.newArrayList();
 		if (day == null) {
 			day = new JDay(dt);
 		} else {
 			missionsIds.addAll(day.missionIds());
 		}
 		final ImmutableMap<ObjectId, JMission> jMissions = JMission.codeAndMissionType(ImmutableList.copyOf(missionsIds));
 		return ok(toJson(DayDTO.of(day, jMissions, dt.getYear(), dt.getMonthOfYear())));
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result create() {
 		final Form<CreateForm> form = Form.form(CreateForm.class).bind(request().body().asJson());
 		if (form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final CreateForm createForm = form.get();
 		final String craId = createForm.craId;
 		final String username = createForm.username;
 		final Integer year = createForm.year;
 		final Integer month = createForm.month;
 		final ObjectId userId = JUser.id(username);
 		final JCra cra = JCra.getOrCreate(ObjectId.massageToObjectId(craId), userId, year, month);
 
 		try {
 			final List<JDay> days = JDay.createDays(cra.id, createForm.days());
 			JClaim.computeMissionAllowance(userId, days);
 			return created("Journée(s) sauvegardée(s)");
 		} catch (IllegalDayOperation e) {
 			return badRequest(toJson(e));
 		}
 	}
 
 	public static Result removeHalfDay(final String craId, final Long date, final String mod) {
 		final MomentOfDay momentOfDay = MomentOfDay.valueOf(mod);
 		final DateTime dt = new DateTime(date);
 		final ObjectId idCra = ObjectId.massageToObjectId(craId);
 
 		final JHalfDay deleteHalfDay = JDay.findHalfDay(idCra, dt, momentOfDay);
 		if (JMission.isAbsenceMission(deleteHalfDay.missionId)) {
 			return badRequest(toJson("Vous ne pouvez pas supprimer une absence."));
 		}
 		final JDay day = JDay.deleteHalfDay(idCra, dt, momentOfDay);
 
 		// Day empty
 		if (day.morning == null && day.afternoon == null) {
 			return remove(craId, date);
 		}
 		JClaim.computeMissionAllowance(day.userId, day);
 		return ok();
 	}
 
 	public static Result remove(final String craId, final Long date) {
 		final DateTime dt = new DateTime(date);
 		final ObjectId idCra = ObjectId.massageToObjectId(craId);
 
 		final JDay day = JDay.find(idCra, dt);
 		if ((day.morning != null && JMission.isAbsenceMission(day.morning.missionId))
			    || (day.afternoon != null && JMission.isAbsenceMission(day.afternoon.missionId))) {
 			return badRequest(toJson("Vous ne pouvez pas supprimer une absence."));
 		}
 		JDay.delete(craId, date);
 		JClaim.deleteMissionAllowance(day.userId, day.date);
 		return ok();
 	}
 
 	public static class CreateForm {
 
 		public String username;
 		public String craId;
 		public Integer year;
 		public Integer month;
 		public List<Long> dates;
 		public CreateDayForm day;
 
 		public List<ValidationError> validate() {
 			return null;
 		}
 
 		public List<JDay> days() {
 			return Lists.newArrayList(Collections2.transform(dates, new Function<Long, JDay>() {
 				@Nullable
 				@Override
 				public JDay apply(@Nullable final Long date) {
 					JDay d = new JDay(date);
 					d.craId = ObjectId.massageToObjectId(craId);
 					d.userId = JUser.id(username);
 					d.morning = day.morning();
 					d.afternoon = day.afternoon();
 					d.comment = day.comment;
 					return d;
 				}
 			}));
 		}
 	}
 
 	public static class CreateDayForm {
 
 		public CreateHalfDayForm morning;
 		public CreateHalfDayForm afternoon;
 		public String comment;
 
 		public JHalfDay morning() {
 			if (this.morning == null) {
 				return null;
 			}
 			final JHalfDay hd = new JHalfDay();
 			if (this.morning.missionId == null) {
 				hd.periods.addAll(this.morning.periods());
 			} else {
 				hd.missionId = ObjectId.massageToObjectId(this.morning.missionId);
 			}
 			return hd;
 		}
 
 		public JHalfDay afternoon() {
 			if (this.afternoon == null) {
 				return null;
 			}
 			final JHalfDay hd = new JHalfDay();
 			if (this.afternoon.missionId == null) {
 				hd.periods.addAll(this.afternoon.periods());
 			} else {
 				hd.missionId = ObjectId.massageToObjectId(this.afternoon.missionId);
 			}
 			return hd;
 		}
 
 	}
 
 	public static class CreateHalfDayForm {
 
 		public String missionId;
 		public List<CreatePeriodForm> periods;
 
 		public List<JPeriod> periods() {
 			return Lists.newArrayList(Collections2.transform(periods, new Function<CreatePeriodForm, JPeriod>() {
 				@Nullable
 				@Override
 				public JPeriod apply(@Nullable final CreatePeriodForm p) {
 					return p.to();
 				}
 			}));
 		}
 	}
 
 	public static class CreatePeriodForm {
 
 		public String missionId;
 		public Long startTime;
 		public Long endTime;
 
 		public JPeriod to() {
 			return new JPeriod(ObjectId.massageToObjectId(missionId), new DateTime(startTime).toLocalTime(), new DateTime(endTime).toLocalTime());
 		}
 	}
 
 }
