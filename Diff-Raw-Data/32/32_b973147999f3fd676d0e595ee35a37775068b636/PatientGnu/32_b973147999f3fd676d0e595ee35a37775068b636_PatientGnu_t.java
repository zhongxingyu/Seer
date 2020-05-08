 package eu.neq.mais.domain.gnuhealth;
 
import java.util.Calendar;
 import java.util.Date;
import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.google.gson.Gson;
 
 import eu.neq.mais.NeqServer;
 import eu.neq.mais.domain.Patient;
 import eu.neq.mais.domain.gnuhealth.annotations.MapToGnu;
 
 /**
  * 
  * @author Jan Gansen
  * 
  */
 public class PatientGnu extends Patient {
 
 	private String rec_name;
 	// contains diagnoses id, but has to be named diseases because of automatic
 	// json to instance process
 	private List<String> diseases;
 	private String latestDiagnoseRecName;
 	// private List<DiagnoseGnu> diagnoseList;
 	private Object age;
 
 	private String sex;
 	private String id;
 
 	@MapToGnu("primary_care_doctor.rec_name")
 	private String primary_care_doctor_rec_name;
 
 	@MapToGnu("primary_care_doctor.name")
 	private String primary_care_doctor_name;
 
 	public void prepareDateFormat() {
 
 		Logger logger = Logger.getLogger("eu.neq.mais.domain.gnuhealth");
 		
 		if (!age.getClass().isAssignableFrom(Long.class)) {
 			try {
 				String[] age_tmp = String.valueOf(age).split(" ");
				Calendar cal = new GregorianCalendar();
				int y =  cal.get(Calendar.YEAR)
 						- Integer.parseInt(age_tmp[0].replace("y", ""));
				int m = (cal.get(Calendar.MONTH)+1)-Integer.parseInt(age_tmp[1].replace("m", ""));
				if(m <= 0){
					y = y-1;
					m = 12+m;
				}
				int d = (cal.get(Calendar.DAY_OF_MONTH)+1)-Integer.parseInt(age_tmp[2].replace("d", "")) -1;
//				System.out.println("dof:     day: "+d+" month: "+m+" year: "+y);
				cal = new GregorianCalendar();
				cal.set(Calendar.DAY_OF_MONTH, d);
				cal.set(Calendar.MONTH,m-1);
				cal.set(Calendar.YEAR, y);
		    	age = cal.getTimeInMillis();	
 
 			} catch (NumberFormatException e) {
 				logger.info("PatientGnu: NumberFormatException: " + age);
 				age = new Integer(0);
 			} catch (IllegalStateException e) {
 				logger.info("PatientGnu: IllegalStateException: " + age);
 				age = new Integer(0);
 			}
 		}
 	}
 
 	public String getRec_name() {
 		return rec_name;
 	}
 
 	public void setRec_name(String rec_name) {
 		this.rec_name = rec_name;
 	}
 
 	public List<String> getDiagnoseIds() {
 		return diseases;
 	}
 
 	public void setDiagnoseIds(List<String> diagnoseIds) {
 		this.diseases = diagnoseIds;
 	}
 
 	public Object getAge() {
 		return age;
 	}
 
 	public void setAge(String age) {
 		this.age = age;
 	}
 
 	public String getSex() {
 		return sex;
 	}
 
 	public void setSex(String sex) {
 		this.sex = sex;
 	}
 
 	public PatientGnu() {
 
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	// public List<DiagnoseGnu> getDiagnoseList() {
 	// return diagnoseList;
 	// }
 	//
 	// public void setDiagnoseList(List<DiagnoseGnu> diagnoseList) {
 	// this.diagnoseList = diagnoseList;
 	// }
 
 	public String getPrimary_care_doctor_rec_name() {
 		return primary_care_doctor_rec_name;
 	}
 
 	public void setPrimary_care_doctor_rec_name(
 			String primary_care_doctor_rec_name) {
 		this.primary_care_doctor_rec_name = primary_care_doctor_rec_name;
 	}
 
 	public String getPrimary_care_doctor_id() {
 		return primary_care_doctor_name;
 	}
 
 	public void setPrimary_care_doctor_id(String primary_care_doctor_id) {
 		this.primary_care_doctor_name = primary_care_doctor_id;
 	}
 
 	public String getLatestDiagnoseRecName() {
 		return latestDiagnoseRecName;
 	}
 
 	public void setLatestDiagnoseRecName(String latestDiagnoseRecName) {
 		this.latestDiagnoseRecName = latestDiagnoseRecName;
 	}
 
 	public String toString() {
 		return "rec_name: " + rec_name + ", diseases: "
 				+ ((diseases == null) ? "none" : diseases.size())
 				+ ", latestDiagnoseRecName: " + latestDiagnoseRecName
 				+ ", age: " + age + ", sex: " + sex + ", id: " + id
 				+ ", pcdrecname: " + primary_care_doctor_rec_name;
 	}
 
 }
