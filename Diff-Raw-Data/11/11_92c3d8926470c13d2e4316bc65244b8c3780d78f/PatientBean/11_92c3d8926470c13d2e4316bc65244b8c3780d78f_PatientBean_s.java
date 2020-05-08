 package beans;
 
 //TODO : faire la liste des visites
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.primefaces.model.map.LatLng;
 
 import util.Map;
 
 public class PatientBean implements Serializable {
 
 	private static final long serialVersionUID = -3039185811317013109L;
 
 	private String name;
 	private String surname;
 	private String ssid;
 
 	private Date birthdate;
 	private Address address;
 	private String phone;
 	private Personn referPersonn;
 	private long key;
 	private Personn doctor;
 	private String drugStoreName;
 	private String drugStorePhone;
 	private String laboratoryName;
 	private String laboratoryPhone;
 	private LatLng latLng;
 	
 	private List<Visite> visiteList;
 
 	// private String personns;
 
 	
 	public PatientBean(PatientBean patient) {
 		this.name = patient.name;
 		this.surname = patient.surname;
 		this.ssid = patient.ssid;
 		this.birthdate = patient.birthdate;
 		this.address = patient.address;
 		this.phone = patient.phone;
 		this.referPersonn = patient.referPersonn;
 		this.key = patient.key;
 		this.doctor = patient.doctor;
 		this.drugStoreName = patient.drugStoreName;
 		this.drugStorePhone = patient.drugStorePhone;
 		this.laboratoryName = patient.laboratoryName;
 		this.laboratoryPhone = patient.laboratoryPhone;
 		// this.personns = personns;
 		this.latLng = patient.latLng;
		visiteList = new ArrayList<Visite>();
 	}
 
 	public PatientBean(String name, String surname, String ssid,
 			Date birthdate, Address address, String phone,
 			Personn referPersonn, long key, Personn doctor,
 			String drugStoreName, String drugStorePhone, String laboratoryName,
 			String laboratoryPhone, int priority, LatLng latlng) {
 		this.name = name;
 		this.surname = surname;
 		this.ssid = ssid;
 		this.birthdate = birthdate;
 		this.address = address;
 		this.phone = phone;
 		this.referPersonn = referPersonn;
 		this.key = key;
 		this.doctor = doctor;
 		this.drugStoreName = drugStoreName;
 		this.drugStorePhone = drugStorePhone;
 		this.laboratoryName = laboratoryName;
 		this.laboratoryPhone = laboratoryPhone;
 		// this.personns = personns;
 		this.latLng = latlng;
 		visiteList = new ArrayList<Visite>();
 	}
 
 	public PatientBean(String name, String surname, String ssid, Date date) {
 		this.name = name;
 		this.surname = surname;
 		this.ssid = ssid;
 		this.birthdate = date;
 		visiteList = new ArrayList<Visite>();
 	}
 
 	// TODO a effacer si pas besoin, need only for unit test
 	public PatientBean() {
 		visiteList = new ArrayList<Visite>();
 
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 	public String getSsid() {
 		return ssid;
 	}
 
 	public void setSsid(String ssid) {
 		this.ssid = ssid;
 	}
 
 	public Date getBirthdate() {
 		return birthdate;
 	}
 
 	public void setBirthdate(Date birthdate) {
 		this.birthdate = birthdate;
 	}
 
 	public Address getAddress() {
 		return address;
 	}
 
 	public void setAddress(Address address) {
 		this.address = address;
 		this.latLng = Map.geocode(address.toString());
 	}
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	public Personn getReferPersonn() {
 		return referPersonn;
 	}
 
 	public void setReferPersonn(Personn referPersonn) {
 		this.referPersonn = referPersonn;
 	}
 
 	public long getKey() {
 		return key;
 	}
 
 	public void setKey(long key) {
 		this.key = key;
 	}
 
 	public Personn getDoctor() {
 		return doctor;
 	}
 
 	public void setDoctor(Personn doctor) {
 		this.doctor = doctor;
 	}
 
 	public String getDrugStoreName() {
 		return drugStoreName;
 	}
 
 	public void setDrugStoreName(String drugStoreName) {
 		this.drugStoreName = drugStoreName;
 	}
 
 	public String getDrugStorePhone() {
 		return drugStorePhone;
 	}
 
 	public void setDrugStorePhone(String drugStorePhone) {
 		this.drugStorePhone = drugStorePhone;
 	}
 
 	public String getLaboratoryName() {
 		return laboratoryName;
 	}
 
 	public void setLaboratoryName(String laboratoryName) {
 		this.laboratoryName = laboratoryName;
 	}
 
 	public String getLaboratoryPhone() {
 		return laboratoryPhone;
 	}
 
 	public void setLaboratoryPhone(String laboratoryPhone) {
 		this.laboratoryPhone = laboratoryPhone;
 	}
 
 	public LatLng getLatLng() {
 		return latLng;
 	}
 
 	public List<Visite> getVisiteList() {
 		return visiteList;
 	}
 
 	public void setVisiteList(List<Visite> visiteList) {
 		this.visiteList = visiteList;
 	}
 	
 	public void addVisit(Visite visite) {
 		this.visiteList.add(visite);
 //		for(Visite v : visiteList){
 //			System.out.println(v.getPriority() + " " + v.getDuree());
 //		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((address == null) ? 0 : address.hashCode());
 		result = prime * result
 				+ ((birthdate == null) ? 0 : birthdate.hashCode());
 		result = prime * result + ((doctor == null) ? 0 : doctor.hashCode());
 		result = prime * result
 				+ ((drugStoreName == null) ? 0 : drugStoreName.hashCode());
 		result = prime * result
 				+ ((drugStorePhone == null) ? 0 : drugStorePhone.hashCode());
 		result = prime * result + (int) (key ^ (key >>> 32));
 		result = prime * result
 				+ ((laboratoryName == null) ? 0 : laboratoryName.hashCode());
 		result = prime * result
 				+ ((laboratoryPhone == null) ? 0 : laboratoryPhone.hashCode());
 		result = prime * result + ((latLng == null) ? 0 : latLng.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
 		result = prime * result
 				+ ((referPersonn == null) ? 0 : referPersonn.hashCode());
 		result = prime * result + ((ssid == null) ? 0 : ssid.hashCode());
 		result = prime * result + ((surname == null) ? 0 : surname.hashCode());
 		result = prime * result
 				+ ((visiteList == null) ? 0 : visiteList.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		PatientBean other = (PatientBean) obj;
 		if (address == null) {
 			if (other.address != null)
 				return false;
 		} else if (!address.equals(other.address))
 			return false;
 		if (birthdate == null) {
 			if (other.birthdate != null)
 				return false;
 		} else if (!birthdate.equals(other.birthdate))
 			return false;
 		if (doctor == null) {
 			if (other.doctor != null)
 				return false;
 		} else if (!doctor.equals(other.doctor))
 			return false;
 		if (drugStoreName == null) {
 			if (other.drugStoreName != null)
 				return false;
 		} else if (!drugStoreName.equals(other.drugStoreName))
 			return false;
 		if (drugStorePhone == null) {
 			if (other.drugStorePhone != null)
 				return false;
 		} else if (!drugStorePhone.equals(other.drugStorePhone))
 			return false;
 		if (key != other.key)
 			return false;
 		if (laboratoryName == null) {
 			if (other.laboratoryName != null)
 				return false;
 		} else if (!laboratoryName.equals(other.laboratoryName))
 			return false;
 		if (laboratoryPhone == null) {
 			if (other.laboratoryPhone != null)
 				return false;
 		} else if (!laboratoryPhone.equals(other.laboratoryPhone))
 			return false;
 		if (latLng == null) {
 			if (other.latLng != null)
 				return false;
 		} else if (!latLng.equals(other.latLng))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (phone == null) {
 			if (other.phone != null)
 				return false;
 		} else if (!phone.equals(other.phone))
 			return false;
 		if (referPersonn == null) {
 			if (other.referPersonn != null)
 				return false;
 		} else if (!referPersonn.equals(other.referPersonn))
 			return false;
 		if (ssid == null) {
 			if (other.ssid != null)
 				return false;
 		} else if (!ssid.equals(other.ssid))
 			return false;
 		if (surname == null) {
 			if (other.surname != null)
 				return false;
 		} else if (!surname.equals(other.surname))
 			return false;
 //		if (visiteList == null) {
 //			if (other.visiteList != null)
 //				return false;
 //		} else if (!visiteList.equals(other.visiteList))
 //			return false;
 		return true;
 	}
 
 }
