 package puf.m2.hms.model;
 
 import java.sql.ResultSet;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import puf.m2.hms.exception.HmsException;
 import puf.m2.hms.exception.PhysicianAssignmentException;
 import puf.m2.hms.utils.DateUtils;
 
 public class PhysicianAssignment extends HmsEntity {
 
 	private static final Map<Integer, PhysicianAssignment> PA_MAP = new CacheAwareMap<Integer, PhysicianAssignment>();
 
     private Patient patient;
     private Physician physician;
     private Date startDate;
 
     private Date endDate;
 
     public PhysicianAssignment(Patient patient, Physician physician) {
         this(patient, physician, new Date(), new Date());
     }
 
     public PhysicianAssignment(Patient patient, Physician physician,
             Date startDate, Date endDate) {
 
         this.patient = patient;
         this.physician = physician;
         this.startDate = startDate;
         this.endDate = endDate;
     }
     
 
     public void save() throws PhysicianAssignmentException {
         
         final String queryTemple = "insert into PhysicianAssignment values({0}, {1}, {2}, ''{3}'', ''{4}'')";
         try {
             id = getNextFreeId();
             
             DB.createConnection();
             DB.executeUpdate(MessageFormat.format(queryTemple, id, patient.getId(),
                     physician.getId(), DateUtils.dateToString(startDate),
                     DateUtils.dateToString(endDate)));
             DB.closeConnection();
         } catch (Exception e) {
             throw new PhysicianAssignmentException(e);
         }
         
         PA_MAP.put(id, this);
 
         physician.setAvailable(false);
         try {
             physician.save();
         } catch (HmsException e) {
             throw new PhysicianAssignmentException(e);
         }
     }
 
     
 	public static List<PhysicianAssignment> getPhysicianAssignments() throws PhysicianAssignmentException {
 
 		List<PhysicianAssignment> paList = new ArrayList<PhysicianAssignment>();
 
 		final String query = "select * from PhysicianAssignment";
 		try {
 			DB.createConnection();
 			ResultSet rs = DB.executeQuery(query);
 
 			while (rs.next()) {
 				int id = rs.getInt("id");
 
 				PhysicianAssignment pa = PA_MAP.get(id);
 				if (pa == null) {
 					Date startDate = DateUtils.parseDate(rs
 							.getString("startDate"));
 					Date endDate = DateUtils.parseDate(rs.getString("endDate"));
 					Patient patient = Patient.getPatientById(rs.getInt("patientId"));
 					Physician physician = Physician.getPhysicianById(rs.getInt("physicianId"));
 
 					pa = new PhysicianAssignment(patient, physician, startDate,
 							endDate);
					pa.id = id;
 
 					PA_MAP.put(id, pa);
 				}
 				paList.add(pa);
 			}
 			DB.closeConnection();
 		} catch (Exception e) {
 			throw new PhysicianAssignmentException(e);
 		}
 
 		return paList;
 
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public Patient getPatient() {
 		return patient;
 	}
 
 	public Physician getPhysician() {
 		return physician;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 }
