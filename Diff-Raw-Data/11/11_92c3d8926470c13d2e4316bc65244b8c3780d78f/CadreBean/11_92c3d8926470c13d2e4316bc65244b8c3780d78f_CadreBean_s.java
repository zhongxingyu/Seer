 package beans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.primefaces.model.map.LatLng;
 
 import util.Map;
 import util.PlageHoraire;
 
 public class CadreBean implements Serializable {
 
 	private static final long serialVersionUID = -3039185811317013109L;
 
 	private String name;
 	private String surname;
 	private String ssid;
 	private String login;
 
 	private Date birthdate;
 	private Address address;
 	private String phone;
 	private Personn referPersonn;
 	private LatLng latLng;
 	private List<SectorBean> sectors = new ArrayList<SectorBean>();
 	// private List<Sectors> sectors = new ArrayList<Sectors>();
 	private List<SectorBean> all = createAll();
 
 	private Set<Integer> daysOfWeek= new HashSet<Integer>(); // Calendar.DAY_OF_WEEK
 	private List<Visite> visitList = new ArrayList<Visite>();
 
 	private ArrayList<SectorBean> createAll() {
 		ArrayList<SectorBean> all = new ArrayList<SectorBean>();
 
 		all.add(new SectorBean("Gagny"));
 		all.add(new SectorBean("Montfermeil"));
 		all.add(new SectorBean("Champs"));
 
 		return all;
 	}
 
 	// private String personns;
 
 	public CadreBean() {
 
 	}
 
 	public CadreBean(CadreBean cadre) {
 		this.name = cadre.name;
 		this.surname = cadre.surname;
 		this.ssid = cadre.ssid;
 		this.birthdate = cadre.birthdate;
 		this.address = cadre.address;
 		this.phone = cadre.phone;
 		this.referPersonn = cadre.referPersonn;
 		this.latLng = cadre.latLng;
 		this.sectors = cadre.sectors;
 		this.daysOfWeek = cadre.daysOfWeek;
 		this.login= cadre.login;
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
 
 	public LatLng getLatLng() {
 		return latLng;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public List<SectorBean> getSectors() {
 //		System.out.println("Get sectors : ");
 //		for (SectorBean s : sectors) {
 //			System.out.println("\t" + s.toString());
 //		}
 		return sectors;
 	}
 
 	public void setSectors(List<SectorBean> sectors) {
 		this.sectors = sectors;
 	}
 
 	public List<SectorBean> getAll() {
 		return all;
 	}
 
 	public void setAll(List<SectorBean> all) {
 		this.all = all;
 	}
 
 	public void setLatLng(LatLng latLng) {
 		this.latLng = latLng;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((address == null) ? 0 : address.hashCode());
 		result = prime * result + ((all == null) ? 0 : all.hashCode());
 		result = prime * result
 				+ ((birthdate == null) ? 0 : birthdate.hashCode());
 		result = prime * result
 				+ ((daysOfWeek == null) ? 0 : daysOfWeek.hashCode());
 		result = prime * result + ((latLng == null) ? 0 : latLng.hashCode());
 		result = prime * result + ((login == null) ? 0 : login.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
 		result = prime * result
 				+ ((referPersonn == null) ? 0 : referPersonn.hashCode());
 		result = prime * result + ((sectors == null) ? 0 : sectors.hashCode());
 		result = prime * result + ((ssid == null) ? 0 : ssid.hashCode());
 		result = prime * result + ((surname == null) ? 0 : surname.hashCode());
 		result = prime * result
 				+ ((visitList == null) ? 0 : visitList.hashCode());
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
 		CadreBean other = (CadreBean) obj;
 		if (address == null) {
 			if (other.address != null)
 				return false;
 		} else if (!address.equals(other.address))
 			return false;
 		if (all == null) {
 			if (other.all != null)
 				return false;
 		} else if (!all.equals(other.all))
 			return false;
 		if (birthdate == null) {
 			if (other.birthdate != null)
 				return false;
 		} else if (!birthdate.equals(other.birthdate))
 			return false;
 		if (daysOfWeek == null) {
 			if (other.daysOfWeek != null)
 				return false;
 		} else if (!daysOfWeek.equals(other.daysOfWeek))
 			return false;
 		if (latLng == null) {
 			if (other.latLng != null)
 				return false;
 		} else if (!latLng.equals(other.latLng))
 			return false;
 		if (login == null) {
 			if (other.login != null)
 				return false;
 		} else if (!login.equals(other.login))
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
 		if (sectors == null) {
 			if (other.sectors != null)
 				return false;
 		} else if (!sectors.equals(other.sectors))
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
 		if (visitList == null) {
 			if (other.visitList != null)
 				return false;
 		} else if (!visitList.equals(other.visitList))
 			return false;
 		return true;
 	}
 
 	public Set<Integer> getDaysOfWeek() {
 		return daysOfWeek;
 	}
 
 	public void setDaysOfWeek(Set<Integer> daysOfWeek) {
 		this.daysOfWeek = daysOfWeek;
 	}
 
 	public List<Visite> getVisitList() {
 		return visitList;
 	}
 
 	public void setVisitList(List<Visite> visitList) {
 		this.visitList = visitList;
 	}
 
 	/**
 	 * 
 	 * @param day
 	 *            must be one of Calendar.DAY_OF_WEEK
 	 * @return
 	 */
 	public boolean workThisDay(Integer day) {
 		return daysOfWeek.contains(day);
 	}
 	
 	public boolean isAvailable(PlageHoraire ph) {
 		for (Visite v : visitList) {
 			if (v.getPlageHoraire().contains(ph)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void addVisite(Visite v) {
 		visitList.add(v);
 	}
 
 }
