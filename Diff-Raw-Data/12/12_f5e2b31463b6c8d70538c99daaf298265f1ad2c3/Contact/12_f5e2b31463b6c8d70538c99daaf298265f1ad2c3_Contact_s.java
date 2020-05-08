 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package database.tables;
 
 import database.Filterable;
 import database.extra.Record;
 import database.models.ContactModel;
 import tools.Configuration;
 
 /**
  *
  * @author Robin jr
  */
 public class Contact extends Record implements Filterable{
     
     public static final int both = 0;
     public static final int supplier = 1;
     public static final int client = 2;
     
     private String firm;
     private String contact;
     private String address;
     private String zipcode;
     private String municipality;
     private String telephone;
     private String telephone2;
     private String cellphone;
     private String fax;
     private String email;
     private String taxnumber;
     private String pricecode;
     private String notes;
     private String type;
 
     private Contact(int id, String firm, String contact, String address, String zipcode, String municipality, String telephone, String telephone2, String cellphone, String fax, String email, String taxnumber, String pricecode, String notes, String type){
 	super(id, Configuration.center().getDB_TABLE_CON());
 	this.firm = firm;
 	this.contact = contact;
 	this.address = address;
 	this.zipcode = zipcode;
 	this.municipality = municipality;
 	this.telephone = telephone;
 	this.telephone2 = telephone2;
 	this.cellphone = cellphone;
 	this.fax = fax;
 	this.email = email;
 	this.taxnumber = taxnumber;
 	this.pricecode = pricecode;
 	this.notes = notes;
 	this.type = type;
     }
     
     public Contact(Contact copy){
 	this(copy.getPrimaryKeyValue(), copy.getFirm(), copy.getContact(), copy.getAddress(), copy.getZipcode(), copy.getMunicipality(), copy.getTelephone(), copy.getTelephone2(), copy.getCellphone(), copy.getFax(), copy.getEmail(), copy.getTaxnumber(), copy.getPricecode(), copy.getNotes(), copy.getType());
     }
     
     public static Contact loadWithValues(int id, String firm, String contact, String address, String zipcode, String municipality, String telephone, String telephone2, String cellphone, String fax, String email, String taxnumber, String pricecode, String notes, String type){
 	return new Contact(id, firm, contact, address, zipcode, municipality, telephone, telephone2, cellphone, fax, email, taxnumber, pricecode, notes, type);
     }
     
     public static Contact createFromModel(int id, ContactModel model){
 	return new Contact(id, model.getFirm(), model.getContact(), model.getAddress(), model.getZipcode(), model.getMunicipality(), model.getTelephone(), model.getTelephone2(), model.getCellphone(), model.getFax(), model.getEmail(), model.getTaxnumber(), model.getPricecode(), model.getNotes(), model.getType());
     }
     
     public void copy(Contact copy){
 	firm = copy.getFirm();
 	contact = copy.getContact();
 	address = copy.getAddress();
 	zipcode = copy.getZipcode(); 
 	municipality = copy.getMunicipality();
 	telephone = copy.getTelephone();
 	telephone2 = copy.getTelephone2();
 	cellphone = copy.getCellphone();
 	fax = copy.getFax();
 	email = copy.getEmail();
 	taxnumber = copy.getTaxnumber();
 	pricecode = copy.getPricecode(); 
 	notes = copy.getNotes();
 	type = copy.getType();
     }
 
     public String getFirm() {
 	return firm;
     }
 
     public String getAddress() {
 	return address;
     }
 
     public String getMunicipality() {
 	return municipality;
     }
 
     public String getTelephone() {
 	return telephone;
     }
 
     public String getCellphone() {
 	return cellphone;
     }
 
     public String getEmail() {
 	return email;
     }
 
     public void setFirm(String firm) {
 	this.firm = firm;
     }
 
     public void setAddress(String address) {
 	this.address = address;
     }
 
     public void setMunicipality(String municipality) {
 	this.municipality = municipality;
     }
 
     public void setTelephone(String telephone) {
 	this.telephone = telephone;
     }
 
     public void setCellphone(String cellphone) {
 	this.cellphone = cellphone;
     }
 
     public void setEmail(String email) {
 	this.email = email;
     }
 
     public String getContact() {
 	return contact;
     }
 
     public void setContact(String contact) {
 	this.contact = contact;
     }
 
     public String getZipcode() {
 	return zipcode;
     }
 
     public void setZipcode(String zipcode) {
 	this.zipcode = zipcode;
     }
 
     public String getTelephone2() {
 	return telephone2;
     }
 
     public void setTelephone2(String telephone2) {
 	this.telephone2 = telephone2;
     }
 
     public String getFax() {
 	return fax;
     }
 
     public void setFax(String fax) {
 	this.fax = fax;
     }
 
     public String getTaxnumber() {
 	return taxnumber;
     }
 
     public void setTaxnumber(String taxnumber) {
 	this.taxnumber = taxnumber;
     }
 
     public String getPricecode() {
 	return pricecode;
     }
 
     public void setPricecode(String pricecode) {
 	this.pricecode = pricecode;
     }
 
     public String getNotes() {
 	return notes;
     }
 
     public void setNotes(String notes) {
 	this.notes = notes;
     }
     
     public void setType(String type){
 	this.type = type;
     }
     
     public String getType(){
 	return type;
     }
     
     public boolean isSupplier(){
 	return type.equalsIgnoreCase("supplier");
     }
     
     public String getSortKey(){
 	return isSupplier() ? firm : contact;
     }
     
     @Override
     public String toString(){
 	return getSortKey();
     }
     
     public String getFullRepresentation(){
 	return isSupplier() ? firm+" (L)" : contact+" (K)";
     }
 
     @Override
     public String filterable(){
	return firm+";"+address+";"+municipality+";"+telephone+";"+cellphone+";"+email+";"+zipcode+";"+telephone2+";"+fax+";"+notes+";"+contact+";"+type;
     }
     
     @Override
     public boolean update() {
 	return database.Database.driver().executeUpdate(Configuration.center().getDB_TABLE_CON(), getPrimaryKey(), getPrimaryKeyValue(), 
 		"firm = "+ (firm.length()>0 ? "\""+ firm +"\"":"NULL")+", "
 		+ "contact = "+(contact.length()>0 ? "\""+ contact +"\"":"NULL")+", "
 		+ "address = "+(address.length()>0 ?"\""+address +"\"":"NULL")+", "
 		+ "zipcode = "+(zipcode.length()>0? "\""+zipcode +"\"":"\"\"")+", "
 		+ "municipality = "+(municipality.length()>0? "\""+municipality +"\"":"NULL")+", "
 		+ "telephone = "+(telephone.length()>0? "\""+telephone +"\"":"NULL")+", "
 		+ "telephone2 = "+(telephone2.length()>=0? "\""+telephone2 +"\"":"NULL")+", "
 		+ "cellphone = "+(cellphone.length()>0? "\""+cellphone +"\"":"NULL")+", "
 		+ "fax = "+(fax.length()>0? "\""+fax +"\"":"NULL")+", "
 		+ "email = "+(email.length()>0? "\""+email +"\"":"NULL")+", "
 		+ "taxnr = "+(taxnumber.length()>0? "\""+taxnumber +"\"":"NULL")+", "
 		+ "pricecode = "+((pricecode != null && pricecode.length()>0)? "\""+pricecode +"\"":"NULL")+", "
 		+ "notes = "+(notes.length()>0? "\""+notes +"\"":"NULL")+", "
 		+ "type = "+(type.length()>0 ? "\""+type +"\"":"NULL"));
     }
 
     @Override
     public boolean delete() {
 	throw new UnsupportedOperationException("Not supported yet.");
     }
 }
