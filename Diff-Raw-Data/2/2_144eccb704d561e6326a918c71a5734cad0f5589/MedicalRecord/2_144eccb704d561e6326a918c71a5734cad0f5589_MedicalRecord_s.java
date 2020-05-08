 package puf.m2.hms.model;
 
 import java.sql.ResultSet;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import puf.m2.hms.db.DbException;
 import puf.m2.hms.exception.MedicalRecordException;
 import puf.m2.hms.utils.DateUtils;
 
 public class MedicalRecord extends HmsEntity {
 
 	private static Map<Integer, MedicalRecord> MR_MAP = new CacheAwareMap<Integer, MedicalRecord>();
 
 	private Patient patient;
 	private Date dateAffect;
 	private String detail;
 
 	public MedicalRecord(Patient patient, Date dateAffect, String detail) {
 		this.patient = patient;
 		this.dateAffect = dateAffect;
 		this.detail = detail;
 	}
 
 	public void save() throws MedicalRecordException {
 		final String queryTemplate = "insert into MedicalRecord values({0}, {1}, ''{2}'', ''{3}'')";
 
 		try {
 			id = getNextFreeId();
 			DB.executeUpdate(MessageFormat.format(queryTemplate, id,
 					patient.getId(), DateUtils.dateToString(dateAffect), detail));
 		} catch (Exception e) {
 			throw new MedicalRecordException(e);
 		}
 		MR_MAP.put(id, this);
 	}
 
 	public void update() throws MedicalRecordException {
		final String queryTemplate = "update MedicalRecord set patientId = {0}, dateAfect = ''{1}'', detail = ''{2}'' where id = {3})";
 		try {
 			DB.executeUpdate(MessageFormat.format(queryTemplate,
 					patient.getId(), DateUtils.dateToString(dateAffect),
 					detail, id));
 		} catch (DbException e) {
 			throw new MedicalRecordException(e);
 		}
 	}
 
 	public static List<MedicalRecord> loadMedicalRecord(Patient patient)
 			throws MedicalRecordException {
 
 		final String queryTemplate = "SELECT * FROM MedicalRecord WHERE patientId = {0}";
 		List<MedicalRecord> mrList = new ArrayList<MedicalRecord>();
 
 		try {
 			ResultSet rs = DB.executeQuery(MessageFormat.format(queryTemplate,
 					patient.getId()));
 
 			while (rs.next()) {
 				int id = rs.getInt("id");
 				MedicalRecord mr = MR_MAP.get(id);
 
 				if (mr == null) {
 					Date dateAffect = DateUtils.parseDate(rs
 							.getString("dateAffect"));
 
 					mr = new MedicalRecord(patient, dateAffect,
 							rs.getString("detail"));
 					mr.id = id;
 
 					MR_MAP.put(id, mr);
 				}
 
 				mrList.add(mr);
 			}
 		} catch (Exception e) {
 			throw new MedicalRecordException(e);
 		}
 		return mrList;
 	}
 
 	public static MedicalRecord loadMedicalRecordById(int id)
 			throws MedicalRecordException {
 
 		final String queryTemplate = "SELECT * FROM MedicalRecord WHERE id = {0}";
 
 		MedicalRecord mr = MR_MAP.get(id);
 		if (mr == null) {
 
 			try {
 				ResultSet rs = DB.executeQuery(MessageFormat.format(
 						queryTemplate, id));
 
 				if (rs.next()) {
 					Date dateAffect = DateUtils.parseDate(rs
 							.getString("dateAffect"));
 					int patientId = rs.getInt("patientId");
 
 					mr = new MedicalRecord(Patient.getPatientById(patientId),
 							dateAffect, rs.getString("detail"));
 					mr.id = id;
 
 					MR_MAP.put(id, mr);
 
 				}
 			} catch (Exception e) {
 				throw new MedicalRecordException(e);
 			}
 		}
 		return mr;
 	}
 
 	public Date getDateAffect() {
 		return dateAffect;
 	}
 
 	public void setDateAffect(Date dateAffect) {
 		this.dateAffect = dateAffect;
 	}
 
 	public String getDetail() {
 		return detail;
 	}
 
 	public void setDetail(String detail) {
 		this.detail = detail;
 	}
 
 	public Patient getPatient() {
 		return patient;
 	}
 
 }
