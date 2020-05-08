 package org.openmrs.module.maternalsummary;
 
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.maternalsummary.data.ANCVisitsEntry;
 import org.openmrs.module.maternalsummary.data.DeliverySummaryEntry;
 import org.openmrs.module.maternalsummary.data.MedicalHistory;
 import org.openmrs.module.maternalsummary.data.ObsHistory;
 import org.openmrs.module.maternalsummary.data.RapidSMSMessage;
 import org.openmrs.module.maternalsummary.data.Referrals;
 import org.openmrs.module.maternalsummary.data.TestsAndTreatment;
 import org.openmrs.module.maternalsummary.pdf.PDFRenderer;
 import org.openmrs.module.maternalsummary.pdf.PDFRenderer.PDFRendererException;
 import org.openmrs.module.maternalsummary.pdf.impl.ITextRenderer;
 
 public class MaternalSummary {
 	
 	private static DateFormat mediumFormat = new SimpleDateFormat("dd-MMM-yyyy");
 	private static DateFormat longFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
 
 	private Patient patient;
 	private List<DeliverySummaryEntry> deliverySummary;
 	private ObsHistory obsHistory;
 	private MedicalHistory medicalHistory;
 	private TestsAndTreatment testsAndTreatment;
 	private List<ANCVisitsEntry> ANCVisits;
 	private Referrals referrals;
 	private List<RapidSMSMessage> rapidSMSMessages;
 	
 	private transient MessageSourceService mss;
 	
 	
 	public Patient getPatient() {
 		return patient;
 	}
 	public void setPatient(Patient patient) {
 		this.patient = patient;
 	}
 	public List<DeliverySummaryEntry> getDeliverySummary() {
 		return deliverySummary;
 	}
 	public void setDeliverySummary(List<DeliverySummaryEntry> deliverySummary) {
 		this.deliverySummary = deliverySummary;
 	}
 	public ObsHistory getObsHistory() {
 		return obsHistory;
 	}
 	public void setObsHistory(ObsHistory obsHistory) {
 		this.obsHistory = obsHistory;
 	}
 	public MedicalHistory getMedicalHistory() {
 		return medicalHistory;
 	}
 	public void setMedicalHistory(MedicalHistory medicalHistory) {
 		this.medicalHistory = medicalHistory;
 	}
 	public TestsAndTreatment getTestsAndTreatment() {
 		return testsAndTreatment;
 	}
 	public void setTestsAndTreatment(TestsAndTreatment testsAndTreatment) {
 		this.testsAndTreatment = testsAndTreatment;
 	}
 	public List<ANCVisitsEntry> getANCVisits() {
 		return ANCVisits;
 	}
 	public void setANCVisits(List<ANCVisitsEntry> aNCVisits) {
 		ANCVisits = aNCVisits;
 	}
 	public Referrals getReferrals() {
 		return referrals;
 	}
 	public void setReferrals(Referrals referrals) {
 		this.referrals = referrals;
 	}
 	public List<RapidSMSMessage> getRapidSMSMessages() {
 		return rapidSMSMessages;
 	}
 	public void setRapidSMSMessages(List<RapidSMSMessage> rapidSMSMessages) {
 		this.rapidSMSMessages = rapidSMSMessages;
 	}
 	
 	public void renderPDF(OutputStream out) throws PDFRendererException {
 		mss = Context.getMessageSourceService();
 		PDFRenderer renderer = new ITextRenderer();
 		try {
 			renderer.create(out);
 			renderer.addHeader1(patient.getGivenName() + " " + patient.getFamilyName());
 			renderer.addHeader2(formatMedium(new Date()));
 			
 			renderDeliverySummary(renderer);
 			renderObsHistory(renderer);
 			renderMedicalHistory(renderer);
 			renderTestsAndTreatment(renderer);
 			renderANCVisits(renderer);
 			renderReferralCommentsBox(renderer);
 		
 		} finally {
 			renderer.close();
 		}
 	}
 	
 	private void renderDeliverySummary(PDFRenderer renderer) throws PDFRendererException {
 		renderer.addHeader2(mss.getMessage("maternalsummary.previousPregnancies"));
 		
 		renderer.tableStart(9);
 		
 		renderer.tableAddBold(mss.getMessage("maternalsummary.no"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.date"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.modeOfDelivery"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.typeOfBirth"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.birthWeight"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.gender"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.pretermTerm"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.bloodLossAtDelivery"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.maternalOutcome"));
 		
 		for (DeliverySummaryEntry entry : deliverySummary) {
 			renderer.tableAdd(toString(entry.getNumber()));
 			renderer.tableAdd(formatLong(entry.getDateTime()));
 			renderer.tableAdd(entry.getModeOfDelivery());
 			renderer.tableAdd(entry.getTypeOfBirth());
 			renderer.tableAdd(toString(entry.getBirthWeight()));
 			renderer.tableAdd(entry.getGender());
 			renderer.tableAdd(entry.getPretermOrTerm());
 			renderer.tableAdd(entry.getBloodLoss());
 			renderer.tableAdd(entry.getMaternalOutcome());
 		}
 			
 		renderer.tableEnd();
 	}
 	
 	private void renderObsHistory(PDFRenderer renderer) throws PDFRendererException {
 		renderer.addHeader2(mss.getMessage("maternalsummary.obsHistory"));
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.numberOf"));
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.pregnancies"));
 		renderer.tableAdd(toString(obsHistory.getNumPregnancies()));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.births"));
 		renderer.tableAdd(toString(obsHistory.getNumBirths()));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.liveBirths"));
 		renderer.tableAdd(toString(obsHistory.getNumLiveBirths()));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.stillBirths"));
 		renderer.tableAdd(toString(obsHistory.getNumStillBirths()));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.cSections"));
 		renderer.tableAdd(toString(obsHistory.getNumCSections()));
 		renderer.tableEnd();
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.lastBorn"));
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.status"));
 		if (obsHistory.getLastBornAlive()!=null)
 			if (obsHistory.getLastBornAlive())
 				renderer.tableAdd(mss.getMessage("maternalsummary.alive"));
 			else
 				renderer.tableAdd(mss.getMessage("maternalsummary.dead"));
 		else
 			renderer.tableAdd("");
 		renderer.tableAddBold(mss.getMessage("maternalsummary.birthDate"));
 		renderer.tableAdd(formatMedium(obsHistory.getLastBornBirthDate()));
 		renderer.tableEnd();
 		
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.dateOfLMP"));
 		renderer.tableAdd(formatMedium(obsHistory.getDateOfLMP()));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.expectedDeliveryDate"));
 		renderer.tableAdd(formatMedium(obsHistory.getExpectedDeliveryDate()));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.gestationalAge"));
 		renderer.tableAdd(toString(obsHistory.getGestationalAge()) + " " + mss.getMessage("maternalsummary.weeks"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.childsPresentation"));
 		renderer.tableAdd(obsHistory.getPresentation());
 		//if (obsHistory.getIsSeroPositive()) {
 		//	renderer.tableAddBold(mss.getMessage("maternalsummary.highestWHOStage"));
 		//	renderer.tableAdd(toString(obsHistory.getHighestWHOStage()));
 		//}
 		renderer.tableEnd();
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.risks"));
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.risk"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.dateReported"));
 		for (ObsHistory.Risk risk : obsHistory.getRisks()) {
 			renderer.tableAdd(risk.getRisk());
 			renderer.tableAdd(formatMedium(risk.getDateReported()));
 		}
 		renderer.tableEnd();
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.obsRisks"));
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.risk"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.dateReported"));
 		for (ObsHistory.Risk risk : obsHistory.getObsRisks()) {
 			renderer.tableAdd(risk.getRisk());
 			renderer.tableAdd(formatMedium(risk.getDateReported()));
 		}
 		renderer.tableEnd();
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.pastMedicalHistory"));
 		renderer.tableStart(2);
 		for (MedicalHistory.HistoryItem h : medicalHistory.getHistory()) {
 			renderer.tableAdd(formatMedium(h.getDate()));
 			renderer.tableAdd(h.getHistory());
 		}
 		renderer.tableEnd();
 	}
 	
 	private void renderMedicalHistory(PDFRenderer renderer) throws PDFRendererException {
 		renderer.addHeader2(mss.getMessage("maternalsummary.medicationsAndTreatment"));
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.currentMedication"));
 		renderer.tableStart(2);
 		for (MedicalHistory.Medication med : medicalHistory.getMedication()) {
 			renderer.tableAdd(formatMedium(med.getDate()));
 			renderer.tableAdd(med.getMedication());
 		}
 		renderer.tableEnd();
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.treatmentInterventions"));
 		renderer.tableStart(2);
 		for (TestsAndTreatment.TreatmentIntervention ti : testsAndTreatment.getInterventions()) {
 			renderer.tableAdd(formatMedium(ti.getDate()));
 			renderer.tableAdd(mss.getMessage("maternalsummary.given") + " " + ti.getIntervention());
 		}
 		renderer.tableEnd();
 	}
 	
 	private void renderTestsAndTreatment(PDFRenderer renderer) throws PDFRendererException {
 		renderer.addHeader2(mss.getMessage("maternalsummary.lab"));
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.testing"));
 		renderer.tableStart(3);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.date"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.test"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.result"));
 		renderer.tableAdd(formatMedium(testsAndTreatment.getTests().getHIVTestDate()));
 		renderer.tableAdd("HIV");
 		renderer.tableAdd(testsAndTreatment.getTests().getHIVResult());
 		renderer.tableAdd(formatMedium(testsAndTreatment.getTests().getRPRTestDate()));
 		renderer.tableAdd("RPR");
 		renderer.tableAdd(testsAndTreatment.getTests().getRPRResult());
 		renderer.tableEnd();
 		
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.treatedForSyphilisLong"));
 		renderer.tableAdd(testsAndTreatment.getTests().getTreatedForSyphilis());
 		renderer.tableEnd();
 		
 		renderer.addHeader3(mss.getMessage("maternalsummary.partnerTesting"));
 		renderer.tableStart(3);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.date"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.test"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.result"));
 		renderer.tableAdd(formatMedium(testsAndTreatment.getPartnerTests().getHIVTestDate()));
 		renderer.tableAdd("HIV");
 		renderer.tableAdd(testsAndTreatment.getPartnerTests().getHIVResult());
 		renderer.tableAdd(formatMedium(testsAndTreatment.getPartnerTests().getRPRTestDate()));
 		renderer.tableAdd("RPR");
 		renderer.tableAdd(testsAndTreatment.getPartnerTests().getRPRResult());
 		renderer.tableEnd();
 		
 		renderer.tableStart(2);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.partnerTreatedForSyphilisLong"));
 		renderer.tableAdd(testsAndTreatment.getPartnerTests().getTreatedForSyphilis());
 		renderer.tableEnd();
 		
 		//renderer.addHeader3(mss.getMessage("maternalsummary.arvRegimens"));
 		//renderer.tableStart(7);
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.creatinineLevel"));
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.cd4Count"));
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.cd4Date"));
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.whoStage"));
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.arvRegimen"));
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.arvDate"));
 		//renderer.tableAddBold(mss.getMessage("maternalsummary.cotrimoxazoleDate"));
 		//for (TestsAndTreatment.SeroPositiveWomen spw : testsAndTreatment.getSeroPositiveWomen()) {
 		//	renderer.tableAdd(toString(spw.getCreatinineLevel()) + " mmol/L");
 		//	renderer.tableAdd(toString(spw.getCD4Count()));
 		//	renderer.tableAdd(formatMedium(spw.getCD4CountDate()));
 		//	renderer.tableAdd(spw.getWHOStage());
 		//	renderer.tableAdd(spw.getARVProphylaxis());
 		//	renderer.tableAdd(formatMedium(spw.getARVProphylaxisDate()));
 		//	renderer.tableAdd(formatMedium(spw.getCotrimoxazoleStartDate()));
 		//}
 		//renderer.tableEnd();
 	}
 	
 	private void renderANCVisits(PDFRenderer renderer) throws PDFRendererException {
 		renderer.addHeader2(mss.getMessage("maternalsummary.ancVisits"));
 		
 		renderer.tableStart(7);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.date"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.weight"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.weightChange"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.bloodPressure"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.temperature"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.fundalHeight"));
 		renderer.tableAddBold(mss.getMessage("maternalsummary.fetalHeartRate"));
 		for (ANCVisitsEntry visit : ANCVisits) {
 			renderer.tableAdd(formatMedium(visit.getDate()));
 			renderer.tableAdd(toString(visit.getWeight()) + " kg");
 			renderer.tableAdd(toString(visit.getWeightChange()) + " kg");
 			renderer.tableAdd( toString(visit.getBloodPressureSystolic()) + " / " + toString(visit.getBloodPressureDiastolic()) + " mmHg" );
			renderer.tableAdd(toString(visit.getTemperature()) + " °C");
 			renderer.tableAdd(toString(visit.getUterusLength()) + " cm");
 			renderer.tableAdd(toString(visit.getFetalHeartRate()) + " bpm");
 		}
 		renderer.tableEnd();
 	}
 	
 	
 	private void renderReferralCommentsBox(PDFRenderer renderer) throws PDFRendererException  {
 		renderer.addHeader2(mss.getMessage("maternalsummary.referral"));
 		
 		renderer.tableStart(1);
 		renderer.tableAddBold(mss.getMessage("maternalsummary.comments"));
 		renderer.tableAddInputField();
 		renderer.tableEnd();
 	}
 	
 	
 	private static String toString(Object o) {
 		return o!=null ? o.toString() : "";
 	}
 	
 	private static String formatMedium(Date date) {
 		return formatDate(mediumFormat, date);
 	}
 	
 	private static String formatLong(Date date) {
 		return formatDate(longFormat, date);
 	}
 	
 	private static String formatDate(DateFormat format, Date date) {
 		return date!=null ? format.format(date) : "";
 	}
 }
