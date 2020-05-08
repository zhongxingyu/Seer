 package controllers;
 
 import caches.ResponseCache;
 import com.google.common.collect.Lists;
 import constants.AbsenceType;
 import dto.AbsenceDTO;
 import exceptions.AbsenceAlreadyExistException;
 import exceptions.AbsenceEndIllegalDateException;
 import exceptions.AbsenceStartIllegalDateException;
 import exceptions.ContainsOnlyWeekEndOrDayOfException;
 import export.PDF;
 import mail.MailerAbsence;
 import models.DbFile;
 import models.JAbsence;
 import models.JDay;
 import models.JUser;
 import org.apache.commons.collections.CollectionUtils;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.io.File;
 import java.util.List;
 
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JAbsences extends Controller {
 
 	@BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result create() {
 		final Form<CreateAbsenceForm> form = Form.form(CreateAbsenceForm.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final CreateAbsenceForm createAbsenceForm = form.get();
 		try {
 			final JAbsence absence = JAbsence.create(createAbsenceForm.to());
 			JDay.addAbsenceDays(absence);
 			return created(toJson(AbsenceDTO.of(absence)));
 		} catch(AbsenceAlreadyExistException | ContainsOnlyWeekEndOrDayOfException | AbsenceStartIllegalDateException | AbsenceEndIllegalDateException e) {
 			return internalServerError(toJson(e.getMessage()));
 		}
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result remove(final String userId, final String id) {
 		// Suppression de l'absence concernée
 		final JAbsence absence = JAbsence.delete(id);
 		if(absence.fileId != null) {
 
 			// supprimer le fichier
 			DbFile.remove(absence.fileId);
 			// récupérer toutes les absences de ce fichier SAUF celle concerné
 			final List<JAbsence> remainAbsences = JAbsence.byFileId(absence.fileId);
 			if(CollectionUtils.isNotEmpty(remainAbsences)) {
 				// Créer nouveau fichier et l'affecter à celle restante
 				PDF.createAbsenceData(remainAbsences);
 			}
 		}
 		// Envoi de l'annulation sans stockage du fichier
 		final JUser user = JUser.account(userId);
 		final File file = PDF.createCancelAbsenceFile(absence, user);
 		MailerAbsence.sendCancelAbsence(user, file);
 		JDay.deleteAbsenceDays(absence);
 		return ok(toJson(AbsenceDTO.of(absence)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result history(final String userId, final String absenceType, final Integer year, final Integer month) {
 		final List<JAbsence> absences = Lists.newArrayList();
 		final AbsenceType at = AbsenceType.of(absenceType);
 		if(year == 0) {
 			absences.addAll(JAbsence.fetch(userId, at));
 		} else {
 			if(month == 0) {
 				switch(at) {
 					case CP:
 						absences.addAll(JAbsence.fetch(userId, at, year, DateTimeConstants.JUNE, year + 1, DateTimeConstants.MAY));
 						break;
 					case RTT:
 						absences.addAll(JAbsence.fetch(userId, at, year, DateTimeConstants.JANUARY, year, DateTimeConstants.DECEMBER));
 						break;
 					default:
 						absences.addAll(JAbsence.fetch(userId, at, year, DateTimeConstants.JANUARY, year + 1, DateTimeConstants.MAY));
 						break;
 				}
 			} else {
 				absences.addAll(JAbsence.fetch(userId, at, year, month, year, month));
 			}
 		}
 		return ok(toJson(AbsenceDTO.of(absences)));
 	}
 
 	public static Result send(final String id) {
 		final JAbsence absence = JAbsence.fetch(id);
 		final File file = PDF.getOrCreateAbsenceFile(absence, JUser.account(absence.userId));
 		final DateTime date = MailerAbsence.send(absence, file);
 		JAbsence.updateSentDate(absence.id, date);
 		return ok();
 	}
 
	public static Result export(final String id) {
 		return ok(PDF.getAbsenceData(JAbsence.fetch(id))).as("application/pdf");
 	}
 
 	public static class CreateAbsenceForm {
 
 		public Boolean day = Boolean.TRUE;
 		public String username;
 		public ObjectId missionId;
 		public Long startDate;
 		public Boolean startMorning;
 		public Long endDate;
 		public Boolean endAfternoon;
 		public String comment;
 
 		public List<ValidationError> validate() {
 			final List<ValidationError> errors = Lists.newArrayList();
 			if(missionId == null) {
 				errors.add(new ValidationError("missionId", "Le motif est requis."));
 			}
 			if(Boolean.TRUE.equals(day)) {
 				if(startDate == null) {
 					errors.add(new ValidationError("date", "La date est requise."));
 				} else if(new DateTime(startDate).isBefore(DateTime.now().withDayOfMonth(1))) {
 					errors.add(new ValidationError("date", "Vous ne pouvez pas saisir une absence précédant le mois en cours."));
 				}
 				if(!Boolean.TRUE.equals(startMorning) && !Boolean.TRUE.equals(endAfternoon)) {
 					errors.add(new ValidationError("limits", "Vous devez sélectionner au moins une demi-journée."));
 				}
 			} else {
 				if(startDate == null) {
 					errors.add(new ValidationError("startDate", "La date de début est requise."));
 				}
 				if(endDate == null) {
 					errors.add(new ValidationError("endDate", "La date de fin est requise."));
 				}
 				if(startDate != null && endDate != null && endDate < startDate) {
 					errors.add(new ValidationError("dates", "La date de début doit être antérieur la date de fin."));
 				} else if(new DateTime(startDate).isBefore(DateTime.now().withDayOfMonth(1))) {
 					errors.add(new ValidationError("dates", "Vous ne pouvez pas saisir une absence précédant le mois en cours."));
 				}
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 
 		public JAbsence to() {
 			final JAbsence holiday = new JAbsence();
 			holiday.userId = JUser.id(this.username);
 			holiday.startDate = Boolean.TRUE.equals(startMorning) ? new DateTime(this.startDate).withTimeAtStartOfDay() : new DateTime(this.startDate).withTime(12, 0, 0, 0);
 			holiday.endDate = Boolean.TRUE.equals(endAfternoon) ? new DateTime(this.endDate).withTime(0, 0, 0, 0).plusDays(1) : new DateTime(this.endDate).withTime(12, 0, 0, 0);
 			holiday.missionId = ObjectId.massageToObjectId(this.missionId);
 			holiday.comment = this.comment;
 			return holiday;
 		}
 	}
 }
