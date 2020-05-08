 package org.openmrs.module.radiotest;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.openmrs.BaseOpenmrsData;
 
 public class RadioTransaction extends BaseOpenmrsData {
 
 	private Integer id;
 	
 	private RadioPatient patient;
 	private Set<RadioTransExam> exams;
 	
 	private Date visitDate;
 	private Date visitTime;
 	private Boolean pending;
 	private Boolean paid;
 	
 	private Double total;
 	private String purpose;
 	private String orNumber;
 	private Set<RadioNote> notes;
 	
 	private Boolean claimed;
 	private Boolean voided = Boolean.FALSE;
 	
 	// not saved in database
 	private HashMap<String, Double> fees;
 	
 	public RadioTransaction(){
 		
 	}
 	
 	public RadioTransaction(boolean isInitialized){
 		if(isInitialized){
 			initialize();
 		}
 	}
 	
 	private void initialize(){
 		pending = true;
 		paid = false;
 		total = 0.0;
 		claimed = false;
 	}
 	
 	@Override
 	public Integer getId() {
 		// TODO Auto-generated method stub
 		return id;
 	}
 
 	@Override
 	public void setId(Integer id) {
 		// TODO Auto-generated method stub
 		this.id = id;
 	}
 
 	public RadioPatient getPatient() {
 		return patient;
 	}
 
 	public void setPatient(RadioPatient patient) {
 		this.patient = patient;
 	}
 
 	public Set<RadioTransExam> getExams() {
 		return exams;
 	}
 
 	public void setExams(Set<RadioTransExam> exams) {
 		this.exams = exams;
 	}
 
 	public Date getVisitDate() {
 		return visitDate;
 	}
 
 	public void setVisitDate(Date visitDate) {
 		this.visitDate = visitDate;
 	}
 
 	public Date getVisitTime() {
 		return visitTime;
 	}
 
 	public void setVisitTime(Date visitTime) {
 		this.visitTime = visitTime;
 	}
 
//	public boolean isPending() {
//		return pending;
//	}
//
//	public void setPending(boolean pending) {
//		this.pending = pending;
//	}

 	public Boolean getPending() {
 		return pending;
 	}
 
 	public void setPending(Boolean pending) {
 		this.pending = pending;
 	}
 	
 	public Boolean getPaid() {
 		return paid;
 	}
 
 	public void setPaid(Boolean paid) {
 		this.paid = paid;
 	}
 
 	public String getPurpose() {
 		return purpose;
 	}
 
 	public void setPurpose(String purpose) {
 		this.purpose = purpose;
 	}
 
 	public String getOrNumber() {
 		return orNumber;
 	}
 
 	public void setOrNumber(String orNumber) {
 		this.orNumber = orNumber;
 	}
 
 	public Set<RadioNote> getNotes() {
 		return notes;
 	}
 
 	public void setNotes(Set<RadioNote> notes) {
 		this.notes = notes;
 	}
 	
 	public void addNote(RadioNote note){
 		if (notes == null){
 			notes = new LinkedHashSet<RadioNote>();
 		}
 		notes.add(note);
 	}
 	
 	public Boolean getClaimed() {
 		return claimed;
 	}
 
 	public void setClaimed(Boolean claimed) {
 		this.claimed = claimed;
 	}
 
 	public Double getTotal(){
 		return total;
 	}
 
 	public void setTotal(Double total) {
 		this.total = total;
 	}
 
 	public Boolean isVoided() {
 		return voided;
 	}
 
 	public void setVoided(Boolean voided) {
 		this.voided = voided;
 	}
 	
 	// CUSTOM FUNCTIONS
 	public int getNumberOfExams(){
 		return exams.size();
 	}
 	
	public void computeFees(){		
 		RadioCategory category = patient.getCategory();
 		
 		initializeMap();
 		for(RadioTransExam exam : exams){
 			for(RadioFee fee : exam.getFees(category)){
 				String type = fee.getType().getName();
 				Double totalFee = fees.get(type); 
 				
 				if (totalFee != null){
 					totalFee = totalFee + fee.getAmount();
 				} else {
 					totalFee = fee.getAmount();
 				}
 				
 				fees.put(type, totalFee);
 			}
 		}
 		
 		for(String key : fees.keySet()){
 			total = total + fees.get(key);
 		}
 	}
 	
 	private void initializeMap(){
 		if (fees == null){
 			fees = new HashMap<String, Double>();
 		}
 	}
 	
 	public int getDoneExams(){
 		int doneExams = 0;
 		for(RadioTransExam exam : exams){
 			if (!exam.isPending()){
 				doneExams++;
 			}
 		}
 		
 		return doneExams;
 	}
 	
 	public void update(){
 		for(RadioTransExam exam : exams){
 			if (exam.isPending()){
 				pending = true;
 				return;
 			}
 		}
 		pending = false;
 	}
 	
 	public boolean isBorrowed(){
 		for(RadioTransExam exam : exams){
 			if(exam.isBorrowed()){
 				return true;
 			}
 		}
 		return false;
 	}
 }
