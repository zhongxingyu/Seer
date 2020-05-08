 package gov.nih.nci.caintegrator.ui.graphing.data.clinical;
 
 import gov.nih.nci.caintegrator.enumeration.ClinicalFactorType;
 import gov.nih.nci.caintegrator.enumeration.GenderType;
 
 public class ClinicalDataPoint {
 	
 	
 	//public static enum PatientGenderType { Male, Female };
 	
	public static final double MISSING_CLINICAL_FACTOR_VALUE = Double.NEGATIVE_INFINITY;
 	private String patientId = null;
 	private double ageAtDx = MISSING_CLINICAL_FACTOR_VALUE;
 	private String treatment = null;
 	private String diseaseName = null;
 	private double karnofskyScore = MISSING_CLINICAL_FACTOR_VALUE;
	private double survival = MISSING_CLINICAL_FACTOR_VALUE;
 	private GenderType gender = GenderType.O;
 	private int diseaseGrade = -1;
 	
 	public ClinicalDataPoint(String patientId) {
 	  this.patientId = patientId;
 	}
 
 	public ClinicalDataPoint(String patientId,GenderType gender, String diseaseName, int diseaseGrade, double ageAtDx, String treatment, double karnofskyScore, double survival) {
 	  this.patientId = patientId;
 	  this.gender = gender;
 	  this.ageAtDx = ageAtDx;
 	  this.treatment = treatment;
 	  this.karnofskyScore = karnofskyScore;
	  this.survival = survival;
 	  this.diseaseName = diseaseName;
 	  this.diseaseGrade = diseaseGrade;
 	}
 	
 	/**
 	 * Return the value of the specified clinical factor. If an invalid component is
 	 * specified, the function will return Double.NEGATIVE_INFINITY;
 	 * @param component
 	 * @return
 	 */
 	public double getFactorValue(ClinicalFactorType factor) {
 	  switch(factor) {
 	  case AgeAtDx: return getAgeAtDx();
 	  case KarnofskyAssessment: return getKarnofskyScore();
	  case SurvivalLength: return getSurvival();
 	  }
 	  return MISSING_CLINICAL_FACTOR_VALUE;
 	}
 
 	public double getAgeAtDx() {
 		return ageAtDx;
 	}
 
 	public void setAgeAtDx(double ageAtDx) {
 		this.ageAtDx = ageAtDx;
 	}
 
 	public double getKarnofskyScore() {
 		return karnofskyScore;
 	}
 
 	public void setKarnofskyScore(double karnofskyScore) {
 		this.karnofskyScore = karnofskyScore;
 	}
 
	public double getSurvival() {
		return survival;
 	}
 
 	public void setSurvival(double survival) {
		this.survival = survival;
 	}
 
 	public String getTreatment() {
 		return treatment;
 	}
 
 	public void setTreatment(String treatment) {
 		this.treatment = treatment;
 	}
 
 	public String getPatientId() {
 		return patientId;
 	}
 
 	public GenderType getGender() {
 		return gender;
 	}
 
 	public void setGender(GenderType gender) {
 		this.gender = gender;
 	}
 
 	public String getDiseaseName() {
 		return diseaseName;
 	}
 
 	public void setDiseaseName(String diseaseName) {
 		this.diseaseName = diseaseName;
 	}
 
 	public int getDiseaseGrade() {
 		return diseaseGrade;
 	}
 
 	public void setDiseaseGrade(int diseaseGrade) {
 		this.diseaseGrade = diseaseGrade;
 	}
 }
