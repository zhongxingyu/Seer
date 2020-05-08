 /**
  * 
  */
 package eu.neq.mais.domain.gnuhealth;
 
 import java.util.Date;
 import java.util.List;
 
 import com.google.gson.Gson;
 
 import eu.neq.mais.domain.Diagnose;
 import eu.neq.mais.domain.Doctor;
 import eu.neq.mais.domain.gnuhealth.annotations.MapToGnu;
 
 /**
  * @author user
  * 
  */
 public class DiagnoseGnu extends Diagnose {
 
 	private boolean pregnancy_warning, is_active, is_infectious, is_allergy,
 			is_on_treatment;
 	private String short_comment, disease_severity, treatment_description,
 			extra_info, allergy_type, status;
 
 	@MapToGnu("pathology.rec_name")
 	private String pathology_rec_name;
 
 	@MapToGnu("doctor.rec_name")
 	private String doctor_rec_name;
 
 	@MapToGnu("pds_code.rec_name")
 	private String pds_code_rec_name;
 
 	int pcs_code, pathology, age, weeks_of_pregnancy, id, doctor;
 	private Object diagnosed_date, healed_date, date_start_treatment,
 			date_stop_treatment;
 
 	public void prepareDateFormat() {
 
 		if (!long.class.isInstance(diagnosed_date)
 				&& !long.class.isInstance(healed_date)
 				&& !long.class.isInstance(date_start_treatment)
 				&& !long.class.isInstance(date_stop_treatment)) {
 
 			
 			DateGnu diagnosed = new Gson().fromJson(
 					String.valueOf(diagnosed_date), DateGnu.class);
 			DateGnu healed = new Gson().fromJson(String.valueOf(healed_date),
 					DateGnu.class);
 			DateGnu start = new Gson().fromJson(
 					String.valueOf(date_start_treatment), DateGnu.class);
 			DateGnu stop = new Gson().fromJson(
 					String.valueOf(date_stop_treatment), DateGnu.class);
 
 			if (diagnosed == null)
 				diagnosed_date = new Integer(0);
 			else
 				diagnosed_date = diagnosed.getTimeInMillis();
 
 			if (healed == null)
 				healed_date = new Integer(0);
 			else
 				healed_date = healed.getTimeInMillis();
 
 			if (start == null)
 				date_start_treatment = new Integer(0);
 				else
 				date_start_treatment = start.getTimeInMillis();
 
 			if (stop == null)
 				date_stop_treatment = new Integer(0);
 			else
 				date_stop_treatment = stop.getTimeInMillis();
 			
 		}
 	}
 
 	public DiagnoseGnu returnLatest(DiagnoseGnu dg) {

 		
 		try {
 			DateGnu date = new Gson().fromJson(
 					String.valueOf(this.getDiagnosed_date()), DateGnu.class);
 			DateGnu dgDate = new Gson().fromJson(
 					String.valueOf(dg.getDiagnosed_date()), DateGnu.class);

 			if (date.latest(dgDate)) {
 				return this;
 			} else {
 				return dg;
 			}
 		} catch (Exception e) {
 			Date date = new Date(Long.parseLong(String.valueOf(this
 					.getDiagnosed_date())));
 			Date dgDate = new Date(Long.parseLong(String.valueOf(dg
 					.getDiagnosed_date())));
 
 			if (date.after(dgDate)) {
 				return this;
 			} else {
 				return dg;
 			}
		} 
 	}
 
 	/**
 	 * @return the doctor
 	 */
 	public int getDoctor() {
 		return doctor;
 	}
 
 	/**
 	 * @param doctor
 	 *            the doctor to set
 	 */
 	public void setDoctor(int doctor) {
 		this.doctor = doctor;
 	}
 
 	/**
 	 * @return the pregnancy_warning
 	 */
 	public boolean isPregnancy_warning() {
 		return pregnancy_warning;
 	}
 
 	/**
 	 * @param pregnancy_warning
 	 *            the pregnancy_warning to set
 	 */
 	public void setPregnancy_warning(boolean pregnancy_warning) {
 		this.pregnancy_warning = pregnancy_warning;
 	}
 
 	/**
 	 * @return the is_active
 	 */
 	public boolean isIs_active() {
 		return is_active;
 	}
 
 	/**
 	 * @param is_active
 	 *            the is_active to set
 	 */
 	public void setIs_active(boolean is_active) {
 		this.is_active = is_active;
 	}
 
 	/**
 	 * @return the is_infectious
 	 */
 	public boolean isIs_infectious() {
 		return is_infectious;
 	}
 
 	/**
 	 * @param is_infectious
 	 *            the is_infectious to set
 	 */
 	public void setIs_infectious(boolean is_infectious) {
 		this.is_infectious = is_infectious;
 	}
 
 	/**
 	 * @return the is_allergy
 	 */
 	public boolean isIs_allergy() {
 		return is_allergy;
 	}
 
 	/**
 	 * @param is_allergy
 	 *            the is_allergy to set
 	 */
 	public void setIs_allergy(boolean is_allergy) {
 		this.is_allergy = is_allergy;
 	}
 
 	/**
 	 * @return the is_on_treatment
 	 */
 	public boolean isIs_on_treatment() {
 		return is_on_treatment;
 	}
 
 	/**
 	 * @param is_on_treatment
 	 *            the is_on_treatment to set
 	 */
 	public void setIs_on_treatment(boolean is_on_treatment) {
 		this.is_on_treatment = is_on_treatment;
 	}
 
 	/**
 	 * @return the short_comment
 	 */
 	public String getShort_comment() {
 		return short_comment;
 	}
 
 	/**
 	 * @param short_comment
 	 *            the short_comment to set
 	 */
 	public void setShort_comment(String short_comment) {
 		this.short_comment = short_comment;
 	}
 
 	/**
 	 * @return the disease_severity
 	 */
 	public String getDisease_severity() {
 		return disease_severity;
 	}
 
 	/**
 	 * @param disease_severity
 	 *            the disease_severity to set
 	 */
 	public void setDisease_severity(String disease_severity) {
 		this.disease_severity = disease_severity;
 	}
 
 	/**
 	 * @return the pathology_rec_name
 	 */
 	public String getPathology_rec_name() {
 		return pathology_rec_name;
 	}
 
 	/**
 	 * @param pathology_rec_name
 	 *            the pathology_rec_name to set
 	 */
 	public void setPathology_rec_name(String pathology_rec_name) {
 		this.pathology_rec_name = pathology_rec_name;
 	}
 
 	/**
 	 * @return the doctor_rec_name
 	 */
 	public String getDoctor_rec_name() {
 		return doctor_rec_name;
 	}
 
 	/**
 	 * @param doctor_rec_name
 	 *            the doctor_rec_name to set
 	 */
 	public void setDoctor_rec_name(String doctor_rec_name) {
 		this.doctor_rec_name = doctor_rec_name;
 	}
 
 	/**
 	 * @return the treatment_description
 	 */
 	public String getTreatment_description() {
 		return treatment_description;
 	}
 
 	/**
 	 * @param treatment_description
 	 *            the treatment_description to set
 	 */
 	public void setTreatment_description(String treatment_description) {
 		this.treatment_description = treatment_description;
 	}
 
 	/**
 	 * @return the extra_info
 	 */
 	public String getExtra_info() {
 		return extra_info;
 	}
 
 	/**
 	 * @param extra_info
 	 *            the extra_info to set
 	 */
 	public void setExtra_info(String extra_info) {
 		this.extra_info = extra_info;
 	}
 
 	/**
 	 * @return the allergy_type
 	 */
 	public String getAllergy_type() {
 		return allergy_type;
 	}
 
 	/**
 	 * @param allergy_type
 	 *            the allergy_type to set
 	 */
 	public void setAllergy_type(String allergy_type) {
 		this.allergy_type = allergy_type;
 	}
 
 	/**
 	 * @return the pds_code_rec_name
 	 */
 	public String getPds_code_rec_name() {
 		return pds_code_rec_name;
 	}
 
 	/**
 	 * @param pds_code_rec_name
 	 *            the pds_code_rec_name to set
 	 */
 	public void setPds_code_rec_name(String pds_code_rec_name) {
 		this.pds_code_rec_name = pds_code_rec_name;
 	}
 
 	/**
 	 * @return the status
 	 */
 	public String getStatus() {
 		return status;
 	}
 
 	/**
 	 * @param status
 	 *            the status to set
 	 */
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	/**
 	 * @return the pcs_code
 	 */
 	public int getPcs_code() {
 		return pcs_code;
 	}
 
 	/**
 	 * @param pcs_code
 	 *            the pcs_code to set
 	 */
 	public void setPcs_code(int pcs_code) {
 		this.pcs_code = pcs_code;
 	}
 
 	/**
 	 * @return the pathology
 	 */
 	public int getPathology() {
 		return pathology;
 	}
 
 	/**
 	 * @param pathology
 	 *            the pathology to set
 	 */
 	public void setPathology(int pathology) {
 		this.pathology = pathology;
 	}
 
 	/**
 	 * @return the age
 	 */
 	public int getAge() {
 		return age;
 	}
 
 	/**
 	 * @param age
 	 *            the age to set
 	 */
 	public void setAge(int age) {
 		this.age = age;
 	}
 
 	/**
 	 * @return the weeks_of_pregnancy
 	 */
 	public int getWeeks_of_pregnancy() {
 		return weeks_of_pregnancy;
 	}
 
 	/**
 	 * @param weeks_of_pregnancy
 	 *            the weeks_of_pregnancy to set
 	 */
 	public void setWeeks_of_pregnancy(int weeks_of_pregnancy) {
 		this.weeks_of_pregnancy = weeks_of_pregnancy;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the diagnosed_date
 	 */
 	public Object getDiagnosed_date() {
 		return diagnosed_date;
 	}
 
 	/**
 	 * @param diagnosed_date
 	 *            the diagnosed_date to set
 	 */
 	public void setDiagnosed_date(DateGnu diagnosed_date) {
 		this.diagnosed_date = diagnosed_date;
 	}
 
 	/**
 	 * @return the healed_date
 	 */
 	public Object getHealed_date() {
 		return healed_date;
 	}
 
 	/**
 	 * @param healed_date
 	 *            the healed_date to set
 	 */
 	public void setHealed_date(DateGnu healed_date) {
 		this.healed_date = healed_date;
 	}
 
 	/**
 	 * @return the date_start_treatment
 	 */
 	public Object getDate_start_treatment() {
 		return date_start_treatment;
 	}
 
 	/**
 	 * @param date_start_treatment
 	 *            the date_start_treatment to set
 	 */
 	public void setDate_start_treatment(DateGnu date_start_treatment) {
 		this.date_start_treatment = date_start_treatment;
 	}
 
 	/**
 	 * @return the date_stop_treatment
 	 */
 	public Object getDate_stop_treatment() {
 		return date_stop_treatment;
 	}
 
 	/**
 	 * @param date_stop_treatment
 	 *            the date_stop_treatment to set
 	 */
 	public void setDate_stop_treatment(DateGnu date_stop_treatment) {
 		this.date_stop_treatment = date_stop_treatment;
 	}
 
 }
