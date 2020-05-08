 package com.megalogika.sv.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 
 import com.megalogika.sv.model.conversion.JsonFilterable;
 
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class E implements Serializable, JsonFilterable {
 	private static final long serialVersionUID = -3924282354391493302L;
 	public static final String MIN_HAZARD = "-1"; //$NON-NLS-1$
 	public static final String NO_HAZARD = "0"; //$NON-NLS-1$
 	public static final String UNKNOWN_HAZARD = "-1";
 
 	public static final String[] HAZARD_DESC = new String[] { "hazard.0.desc", "hazard.1.desc", "hazard.2.desc", "hazard.3.desc", "hazard.4.desc" };
 
 	long id;
 
 	String number;
 	String name;
 	String nameEnglish;
 	String aliases;
 	String function;
 	String properties;
 	String origin;
 	String sideEffect;
 	String diseases;
 	String category = MIN_HAZARD;
 	boolean vegan;
 	List<Product> products;
 	int productCount = 0;
 	private boolean approved = false;
 	private boolean gmo = false;
 
 	private boolean bannedInUsa = false;
 	private boolean bannedInCanada = false;
 	private boolean bannedInAustralia = false;
 
 	private String linksDiseases;
 	private String linksBanned;
 
 	@Transient
 	public Map<String, String> getHazardDescriptions() {
 		HashMap<String, String> ret = new HashMap<String, String>();
 		ret.put(UNKNOWN_HAZARD, "hazard.-1.desc");
 		ret.put(NO_HAZARD, "hazard.0.desc");
 		for (int i = 1; i < HAZARD_DESC.length; i++) {
 			ret.put("" + i, HAZARD_DESC[i]);
 		}
 		return ret;
 	}
 
 	@Basic
 	public String getNumber() {
 		return number;
 	}
 
 	public void setNumber(String number) {
 		this.number = number;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(length = 512)
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getAliases() {
 		return aliases;
 	}
 
 	public void setAliases(String aliases) {
 		this.aliases = aliases;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getFunction() {
 		return function;
 	}
 
 	public void setFunction(String function) {
 		this.function = function;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getProperties() {
 		return properties;
 	}
 
 	public void setProperties(String properties) {
 		this.properties = properties;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getOrigin() {
 		return origin;
 	}
 
 	public void setOrigin(String origin) {
 		this.origin = origin;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getSideEffect() {
 		return sideEffect;
 	}
 
 	public void setSideEffect(String sideEffect) {
 		this.sideEffect = sideEffect;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	public boolean isVegan() {
 		return vegan;
 	}
 
 	public void setVegan(boolean vegan) {
 		this.vegan = vegan;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.TABLE)
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	@Basic
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	@ManyToMany(mappedBy = "conservants", fetch = FetchType.LAZY)//$NON-NLS-1$
 	@BatchSize(size = 64)
 	@LazyCollection(LazyCollectionOption.EXTRA)
 	public List<Product> getProducts() {
 		return products;
 	}
 
 	public void setProducts(List<Product> products) {
 		this.products = products;
 		setProductCount(this.products == null ? 0 : this.products.size());
 	}
 
 	@Transient
 	public int getProductCount() {
 		return productCount;
 	}
 
 	public void setProductCount(int count) {
 		productCount = count;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getDiseases() {
 		return diseases;
 	}
 
 	public void setDiseases(String diseases) {
 		this.diseases = diseases;
 	}
 
 	public void setApproved(boolean approved) {
 		this.approved = approved;
 	}
 
 	@Basic
 	public boolean isApproved() {
 		return approved;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(length = 512)
 	public String getNameEnglish() {
 		return nameEnglish;
 	}
 
 	public void setNameEnglish(String nameEnglish) {
 		this.nameEnglish = nameEnglish;
 	}
 
 	public void setLinksDiseases(String linksDiseases) {
 		this.linksDiseases = linksDiseases;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getLinksDiseases() {
 		return linksDiseases;
 	}
 
 	public void setLinksBanned(String linksBanned) {
 		this.linksBanned = linksBanned;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	@Column(columnDefinition = "text")//$NON-NLS-1$
 	public String getLinksBanned() {
 		return linksBanned;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	public boolean isGmo() {
 		return gmo;
 	}
 
 	public void setGmo(boolean gmo) {
 		this.gmo = gmo;
 	}
 
 	public void setBannedInUsa(boolean bannedInUse) {
 		this.bannedInUsa = bannedInUse;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	public boolean isBannedInUsa() {
 		return bannedInUsa;
 	}
 
 	public void setBannedInCanada(boolean bannedInCanada) {
 		this.bannedInCanada = bannedInCanada;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	public boolean isBannedInCanada() {
 		return bannedInCanada;
 	}
 
 	public void setBannedInAustralia(boolean bannedInAustralia) {
 		this.bannedInAustralia = bannedInAustralia;
 	}
 
 	@Basic(fetch = FetchType.LAZY)
 	public boolean isBannedInAustralia() {
 		return bannedInAustralia;
 	}
 
 	@Transient
 	public boolean isColorant() {
 		if (!number.toLowerCase().startsWith("e")) { //$NON-NLS-1$
 			return false;
 		}
 		try {
 			int numberInt = Integer.parseInt(number.substring(1));
 			return numberInt >= 100 && numberInt < 200;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (id ^ (id >>> 32));
 		result = prime * result + ((number == null) ? 0 : number.hashCode());
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
 		E other = (E) obj;
 		if (id != other.id)
 			return false;
 		if (number == null) {
 			if (other.number != null)
 				return false;
 		} else if (!number.equals(other.number))
 			return false;
 		return true;
 	}
 
 	@Transient
 	public String getDescription() {
 		return getNumber() + "(" + getName() + ")"; 
 	}
 
 	@Override
 	public boolean applyFilter(String fieldName) {
 		return null != fieldName && 
 				!(fieldName.equals("number") || fieldName.equals("name") || fieldName.equals("nameEnglish") || fieldName.equals("aliases")
 				|| fieldName.equals("function") || fieldName.equals("properties") || fieldName.equals("origin")
 				|| fieldName.equals("properties") || fieldName.equals("origin") || fieldName.equals("gmo")
 				|| fieldName.equals("sideEffect") || fieldName.equals("diseases") || fieldName.equals("category")
 				|| fieldName.equals("vegan") || fieldName.equals("productCount")
 				|| fieldName.equals("gmo") || fieldName.equals("bannedInUsa") || fieldName.equals("bannedInCanada")
 				|| fieldName.equals("bannedInAustralia") || fieldName.equals("linksDiseases") || fieldName.equals("linksBanned"));
 	}
 
 }
