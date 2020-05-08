 package org.fao.fi.vme.msaccess.tables;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fao.fi.vme.VmeException;
 import org.fao.fi.vme.domain.GeoRef;
 import org.fao.fi.vme.domain.Profile;
 import org.fao.fi.vme.domain.ValidityPeriod;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.fao.fi.vme.msaccess.mapping.TableDomainMapper;
 import org.fao.fi.vme.msaccess.mapping.ValidityPeriodRule;
 
 public class VME implements TableDomainMapper {
 	int ID;
 	String RFB_ID;
 	String VME_ID;
 	int Year_ID;
 
 	String VME_Inventory_Identifier;
 	String VME_Feature_ID;
 
 	int VME_Validity_Start;
 	int VME_Validity_End;
 	String VME_Geoform;
 	String VME_GeogArea1;
 	String VME_GeogArea2;
 	String VME_GeogAreaFAO;
 	String VME_Coord;
 	String VME_Area_Type;
 	String VME_Description_Physical;
 	String VME_Description_Biology;
 	String VME_Description_Impact;
 
 	public int getID() {
 		return ID;
 	}
 
 	public void setID(int iD) {
 		ID = iD;
 	}
 
 	public String getRFB_ID() {
 		return RFB_ID;
 	}
 
 	public void setRFB_ID(String rFB_ID) {
 		RFB_ID = rFB_ID;
 	}
 
 	public String getVME_ID() {
 		return VME_ID;
 	}
 
 	public void setVME_ID(String vME_ID) {
 		VME_ID = vME_ID;
 	}
 
 	public int getYear_ID() {
 		return Year_ID;
 	}
 
 	public void setYear_ID(int year_ID) {
 		Year_ID = year_ID;
 	}
 
 	public String getVME_Inventory_Identifier() {
 		return VME_Inventory_Identifier;
 	}
 
 	public void setVME_Inventory_Identifier(String vME_Inventory_Identifier) {
 		VME_Inventory_Identifier = vME_Inventory_Identifier;
 	}
 
 	public String getVME_Feature_ID() {
 		return VME_Feature_ID;
 	}
 
 	public void setVME_Feature_ID(String vME_Feature_ID) {
 		VME_Feature_ID = vME_Feature_ID;
 	}
 
 	public int getVME_Validity_Start() {
 		return VME_Validity_Start;
 	}
 
 	public void setVME_Validity_Start(int vME_Validity_Start) {
 		VME_Validity_Start = vME_Validity_Start;
 	}
 
 	public int getVME_Validity_End() {
 		return VME_Validity_End;
 	}
 
 	public void setVME_Validity_End(int vME_Validity_End) {
 		VME_Validity_End = vME_Validity_End;
 	}
 
 	public String getVME_Geoform() {
 		return VME_Geoform;
 	}
 
 	public void setVME_Geoform(String vME_Geoform) {
 		VME_Geoform = vME_Geoform;
 	}
 
 	public String getVME_GeogArea1() {
 		return VME_GeogArea1;
 	}
 
 	public void setVME_GeogArea1(String vME_GeogArea1) {
 		VME_GeogArea1 = vME_GeogArea1;
 	}
 
 	public String getVME_GeogArea2() {
 		return VME_GeogArea2;
 	}
 
 	public void setVME_GeogArea2(String vME_GeogArea2) {
 		VME_GeogArea2 = vME_GeogArea2;
 	}
 
 	public String getVME_GeogAreaFAO() {
 		return VME_GeogAreaFAO;
 	}
 
 	public void setVME_GeogAreaFAO(String vME_GeogAreaFAO) {
 		VME_GeogAreaFAO = vME_GeogAreaFAO;
 	}
 
 	public String getVME_Coord() {
 		return VME_Coord;
 	}
 
 	public void setVME_Coord(String vME_Coord) {
 		VME_Coord = vME_Coord;
 	}
 
 	public String getVME_Area_Type() {
 		return VME_Area_Type;
 	}
 
 	public void setVME_Area_Type(String vME_Area_Type) {
 		VME_Area_Type = vME_Area_Type;
 	}
 
 	public String getVME_Description_Physical() {
 		return VME_Description_Physical;
 	}
 
 	public void setVME_Description_Physical(String vME_Description_Physical) {
 		VME_Description_Physical = vME_Description_Physical;
 	}
 
 	public String getVME_Description_Biology() {
 		return VME_Description_Biology;
 	}
 
 	public void setVME_Description_Biology(String vME_Description_Biology) {
 		VME_Description_Biology = vME_Description_Biology;
 	}
 
 	public String getVME_Description_Impact() {
 		return VME_Description_Impact;
 	}
 
 	public void setVME_Description_Impact(String vME_Description_Impact) {
 		VME_Description_Impact = vME_Description_Impact;
 	}
 
 	@Override
 	public Object map() {
 		Vme o = new Vme();
 
 		MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 		o.setName(u.english(this.getVME_ID()));
 
 		o.setAreaType(this.VME_Area_Type);
 		// o.setCriteria(this.)
 		o.setGeoform(this.VME_Geoform);
 		// o.setGeographicLayerId(this.)
 		o.setId(new Long(this.ID));
 		// o.setName(this.get)
 		// o.setRfmo(rfmo)
 
 		ValidityPeriodRule r = new ValidityPeriodRule(this.VME_Validity_Start, this.VME_Validity_End);
 		ValidityPeriod vp = new ValidityPeriod();
 		vp.setBeginYear(r.getStart());
 		vp.setEndYear(r.getEnd());
 		o.setValidityPeriod(vp);
 
 		Profile f = new Profile();
 		f.setDescriptionBiological(u.english(this.VME_Description_Biology));
 		f.setDescriptionImpact(u.english(this.VME_Description_Impact));
 		f.setDescriptionPhisical(u.english(this.VME_Description_Physical));
 
 		List<Profile> l = new ArrayList<Profile>();
 		l.add(f);
 
 		List<GeoRef> geoRefList = new ArrayList<GeoRef>();
 		GeoRef geoRef = new GeoRef();
 		geoRef.setGeographicFeatureID(this.VME_Feature_ID);
 		geoRefList.add(geoRef);
 		o.setGeoRefList(geoRefList);
 		o.setInventoryIdentifier(this.VME_Inventory_Identifier);
 
 		if (this.getRFB_ID() == null) {
 			throw new VmeException("found vme without reference to rfb. Vme = " + VME_ID + ", RFMO is ");
 		}
 
 		return o;
 	}
 }
