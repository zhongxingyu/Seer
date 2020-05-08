 package org.openmrs.module.maternalsummary.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import org.openmrs.ConceptMap;
 import org.openmrs.DrugOrder;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Form;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.User;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.OrderService;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.maternalsummary.DataCache;
 import org.openmrs.module.maternalsummary.MaternalSummary;
 import org.openmrs.module.maternalsummary.MaternalSummaryService;
 import org.openmrs.module.maternalsummary.data.ANCVisitsEntry;
 import org.openmrs.module.maternalsummary.data.DeliverySummaryEntry;
 import org.openmrs.module.maternalsummary.data.MedicalHistory;
 import org.openmrs.module.maternalsummary.data.ObsHistory;
 import org.openmrs.module.maternalsummary.data.RapidSMSMessage;
 import org.openmrs.module.maternalsummary.data.Referrals;
 import org.openmrs.module.maternalsummary.data.TestsAndTreatment;
 
 public class MaternalSummaryServiceImpl extends BaseOpenmrsService implements MaternalSummaryService {
 
 	protected static final Log log = LogFactory.getLog(MaternalSummaryServiceImpl.class);
 
 
 	@Override
 	public MaternalSummary getMaternalSummary(int patientId) {
 		Integer key = buildKey(MaternalSummary.class, patientId);
 		MaternalSummary res = (MaternalSummary)DataCache.get(key);
 		if (res!=null)
 			return res;
 		
 		res = new MaternalSummary();
 		Patient p = Context.getPatientService().getPatient(patientId);
 		
 		res.setPatient(p);
 		res.setDeliverySummary(getDeliverySummary(p));
 		res.setObsHistory(getObsHistory(p));
 		res.setMedicalHistory(getMedicalHistory(p));
 		res.setTestsAndTreatment(getTestsAndTreatment(p));
 		res.setANCVisits(getANCVisits(p));
 		res.setReferrals(getReferrals(p));
 		res.setRapidSMSMessages(getRapidSMSMessages(p));
 		
 		DataCache.put(key, res);
 		return res;
 	}
 	
 
 	/* Delivery Summary */
 	
 	private List<DeliverySummaryEntry> getDeliverySummary(Patient p) {
 		List<DeliverySummaryEntry> res = new ArrayList<DeliverySummaryEntry>();
 		
 		for (Encounter enc : getEncountersForForm(p, "RHEA ANC Delivery Report", "Delivery Report")) {
 			DeliverySummaryEntry dse = new DeliverySummaryEntry();
 			
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.DATE_OF_DELIVERY.getConcept().equals(obs.getConcept()))
 					dse.setDateTime(obs.getValueDatetime());
 				else if (MaternalConcept.MODE_OF_DELIVERY.getConcept().equals(obs.getConcept()))
 					dse.setModeOfDelivery(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.TYPE_OF_BIRTH.getConcept().equals(obs.getConcept()))
 					dse.setTypeOfBirth(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.BIRTH_WEIGHT.getConcept().equals(obs.getConcept()))
 					dse.setBirthWeight(obs.getValueNumeric());
 				else if (MaternalConcept.GENDER.getConcept().equals(obs.getConcept()))
 					dse.setGender(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.PRETERM_OR_TERM.getConcept().equals(obs.getConcept()))
 					dse.setPretermOrTerm(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.BLOOD_LOSS.getConcept().equals(obs.getConcept()))
 					dse.setBloodLoss(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.MATERNAL_OUTCOME.getConcept().equals(obs.getConcept()))
 					dse.setMaternalOutcome(obs.getValueAsString(Context.getLocale()));
 			}
 			
 			res.add(dse);
 		}
 		
 		//sort according to delivery date desc
 		Collections.sort(res, new Comparator<DeliverySummaryEntry>() {
 			public int compare(DeliverySummaryEntry o1, DeliverySummaryEntry o2) {
 				if (o1==o2) return 0;
 				return o2.getDateTime().compareTo(o1.getDateTime());
 			}
 		});
 		int num = res.size();
 		for (DeliverySummaryEntry dse : res)
 			dse.setNumber(num--);
 		
 		return res;
 	}
 
 	
 	/* Obs History */
 	
 	private ObsHistory getObsHistory(Patient p) {
 		ObsHistory res = new ObsHistory();
 		
 		fillOBAndPastMedicalHistory(res, p);
 		fillPhysical(res, p);
 		calcGestationalAge(res);
 		//calcHighestWHOStage(res, p);
 		
 		return res;
 	}
 	
 	private void fillOBAndPastMedicalHistory(ObsHistory dst, Patient p) {
 		List<Encounter> encs = getEncountersForForm(p, "RHEA OB and Past Medical History", "Past Medical History");
 		if (encs==null || encs.isEmpty())
 			return;
 		
 		Encounter latest = getLatestEncounter(encs);
 		
 		for (Obs obs : latest.getObs()) {
 			if (MaternalConcept.NUM_PREGNANCIES.getConcept().equals(obs.getConcept()))
 				dst.setNumPregnancies(obs.getValueNumeric().intValue());
 			else if (MaternalConcept.NUM_TERM_BIRTHS.getConcept().equals(obs.getConcept()))
 				dst.setNumLiveBirths(obs.getValueNumeric().intValue());
 			else if (MaternalConcept.NUM_STILL_BIRTHS.getConcept().equals(obs.getConcept()))
 				dst.setNumStillBirths(obs.getValueNumeric().intValue());
 			else if (MaternalConcept.NUM_CSECTIONS.getConcept().equals(obs.getConcept()))
 				dst.setNumCSections(obs.getValueNumeric().intValue());
 			else if (MaternalConcept.LAST_BORN_STATUS.getConcept().equals(obs.getConcept()))
 				dst.setLastBornAlive(lastBornStatusObsValueAsBoolean(obs));
 			else if (MaternalConcept.LAST_BORN_BIRTHDATE.getConcept().equals(obs.getConcept()))
 				dst.setLastBornBirthDate(obs.getValueDatetime());
 			else if (MaternalConcept.DATE_OF_LMP.getConcept().equals(obs.getConcept()))
 				dst.setDateOfLMP(obs.getValueDatetime());
 			else if (MaternalConcept.EXPECTED_DELIVERY_DATE.getConcept().equals(obs.getConcept()))
 				dst.setExpectedDeliveryDate(obs.getValueDatetime());
 		}
 		
 		if (dst.getNumLiveBirths()!=null && dst.getNumStillBirths()!=null)
 			dst.setNumBirths(dst.getNumLiveBirths() + dst.getNumStillBirths());
 		
 		for (int i=encs.size()-1; i>=0; i--) {
 			Encounter enc = encs.get(i);
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.OBS_RISK.getConcept().equals(obs.getConcept()))
 					addRiskIfLatest(
 						dst.getObsRisks(),
 						new ObsHistory.Risk(obs.getValueAsString(Context.getLocale()), enc.getEncounterDatetime())
 					);
 			}
 		}
 	}
 	
 	private void addRiskIfLatest(List<ObsHistory.Risk> dst, ObsHistory.Risk risk) {
 		for (int i=0; i<dst.size(); i++) {
 			if (dst.get(i).getRisk().equals(risk.getRisk())) {
 				if (dst.get(i).getDateReported().before(risk.getDateReported()))
 					dst.set(i, risk);
 				return;
 			}
 		}
 		dst.add(risk);
 	}
 	
 	private Boolean lastBornStatusObsValueAsBoolean(Obs obs) {
 		String v = obs.getValueAsString(Context.getLocale());
 		if (v==null)
 			return null;
 		if (v.toLowerCase().contains("alive"))
 			return true;
 		if (v.toLowerCase().contains("dead") || v.toLowerCase().contains("deceased"))
 			return false;
 		return null;
 	}
 	
 	private void fillPhysical(ObsHistory dst, Patient p) {
 		List<Encounter> encs = getEncountersForForm(p, "RHEA ANC Physical", "Physical");
 		if (encs==null || encs.isEmpty())
 			return;
 		
 		Encounter latest = getLatestEncounter(encs);
 		for (Obs obs : latest.getObs()) {
 			if (MaternalConcept.PRESENTATION.getConcept().equals(obs.getConcept()))
 				dst.setPresentation(obs.getValueAsString(Context.getLocale()));
 		}
 			
 		for (int i=encs.size()-1; i>=0; i--) {
 			Encounter enc = encs.get(i);
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.RISK.getConcept().equals(obs.getConcept()))
 					addRiskIfLatest(
 						dst.getRisks(),
 						new ObsHistory.Risk(obs.getValueAsString(Context.getLocale()), enc.getEncounterDatetime())
 					);
 			}
 		}
 	}
 	
 	private void calcGestationalAge(ObsHistory dst) {
 		if (dst.getDateOfLMP()==null)
 			return;
 		
 		DateTime from = new DateTime(dst.getDateOfLMP());
 		DateTime to = new DateTime();
 		dst.setGestationalAge( Weeks.weeksBetween(from, to).getWeeks() );
 		
 		if (dst.getGestationalAge()<0 || dst.getGestationalAge()>46)
 			dst.setGestationalAge(null);
 	}
 	
 	@Deprecated
 	private void calcHighestWHOStage(ObsHistory dst, Patient p) {
 		List<TestsAndTreatment.SeroPositiveWomen> spwList = getTestsAndTreatment(p).getSeroPositiveWomen();
 		
 		if (spwList==null || spwList.isEmpty()) {
 			dst.setIsSeroPositive(Boolean.FALSE);
 			return;
 		} else {
 			dst.setIsSeroPositive(Boolean.TRUE);
 		}
 			
 		dst.setHighestWHOStage( parseLevel(spwList.get(0).getWHOStage()) );
 		for (int i=1; i<spwList.size(); i++) {
 			Integer level = parseLevel(spwList.get(i).getWHOStage());
 			if (level!=null && dst.getHighestWHOStage()!=null && dst.getHighestWHOStage() < level)
 				dst.setHighestWHOStage(level);
 		}
 	}
 	
 	private Integer parseLevel(String whoLevel) {
 		for (String s : whoLevel.split(" ")) {
 			try {
 				return Integer.parseInt(s);
 			} catch (NumberFormatException ex) {}
 		}
 		
 		return null;
 	}
 	
 
 	/* Medical History */
 	
 	private MedicalHistory getMedicalHistory(Patient p) {
 		MedicalHistory res = new MedicalHistory();
 		
 		fillMedication(res, p);
 		fillHistory(res, p);
 		
 		return res;
 	}
 	
 	private void fillMedication(MedicalHistory dst, Patient p) {
 		OrderService os = Context.getOrderService();
 		
 		for (DrugOrder drugOrder : os.getDrugOrdersByPatient(p, OrderService.ORDER_STATUS.CURRENT)) {
 			MedicalHistory.Medication medication = new MedicalHistory.Medication(drugOrder.getDrug().getName(), drugOrder.getStartDate());
 			dst.addMedication(medication);
 		}
 	}
 		
 	private void fillHistory(MedicalHistory dst, Patient p) {
 		List<Encounter> encs = getEncountersForForm(p, "RHEA OB and Past Medical History", "Past Medical History");
 		if (encs==null || encs.isEmpty())
 			return;
 		
 		for (int i=encs.size()-1; i>=0; i--) {
 			Encounter enc = encs.get(i);
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.MEDICAL_HISTORY.getConcept().equals(obs.getConcept()))
 					dst.addHistory(new MedicalHistory.HistoryItem(obs.getValueAsString(Context.getLocale()), enc.getEncounterDatetime()));
 			}
 		}
 	}
 
 	
 	/* Tests and Treatment */
 	
 	private TestsAndTreatment getTestsAndTreatment(Patient p) {
 		Integer key = buildKey(TestsAndTreatment.class, p.getId());
 		TestsAndTreatment res = (TestsAndTreatment)DataCache.get(key);
 		if (res!=null)
 			return res;
 		
 		res = new TestsAndTreatment();
 		
 		fillTests(res, p);
 		//fillSeroPositiveWomen(res, p);
 		fillTreatmentInterventions(res, p);
 		
 		DataCache.put(key, res);
 		return res;
 	}
 	
 	private void fillTests(TestsAndTreatment dst, Patient p) {
 		Encounter latest = getLatestEncounterForForm(p, "RHEA ANC Testing", "Testing");
 		if (latest==null)
 			return;
 		
 		for (Obs obs : latest.getObs()) {
 			if (MaternalConcept.RPR_RESULT.getConcept().equals(obs.getConcept()))
 				dst.getTests().setHIVResult(obs.getValueAsString(Context.getLocale()));
 			else if (MaternalConcept.RPR_TEST_DATE.getConcept().equals(obs.getConcept()))
 				dst.getTests().setHIVTestDate(obs.getValueDatetime());
 			else if (MaternalConcept.RPR_RESULT_DATE.getConcept().equals(obs.getConcept()))
 				dst.getTests().setHIVResultDate(obs.getValueDatetime());
 			else if (MaternalConcept.HIV_RESULT.getConcept().equals(obs.getConcept()))
 				dst.getTests().setHIVResult(obs.getValueAsString(Context.getLocale()));
 			else if (MaternalConcept.HIV_TEST_DATE.getConcept().equals(obs.getConcept()))
 				dst.getTests().setHIVTestDate(obs.getValueDatetime());
 			else if (MaternalConcept.HIV_RESULT_DATE.getConcept().equals(obs.getConcept()))
 				dst.getTests().setHIVResultDate(obs.getValueDatetime());
 			else if (MaternalConcept.TREATED_FOR_SYPHILIS.getConcept().equals(obs.getConcept()))
 				dst.getTests().setTreatedForSyphilis(obs.getValueAsString(Context.getLocale()));
 			
 			else if (MaternalConcept.PARTNER_RPR_RESULT.getConcept().equals(obs.getConcept()))
 				dst.getPartnerTests().setHIVResult(obs.getValueAsString(Context.getLocale()));
 			else if (MaternalConcept.PARTNER_RPR_TEST_DATE.getConcept().equals(obs.getConcept()))
 				dst.getPartnerTests().setHIVTestDate(obs.getValueDatetime());
 			else if (MaternalConcept.PARTNER_HIV_RESULT.getConcept().equals(obs.getConcept()))
 				dst.getPartnerTests().setHIVResult(obs.getValueAsString(Context.getLocale()));
 			else if (MaternalConcept.PARTNER_HIV_TEST_DATE.getConcept().equals(obs.getConcept()))
 				dst.getPartnerTests().setHIVTestDate(obs.getValueDatetime());
 			else if (MaternalConcept.PARTNER_TREATED_FOR_SYPHILIS.getConcept().equals(obs.getConcept()))
 				dst.getPartnerTests().setTreatedForSyphilis(obs.getValueAsString(Context.getLocale()));
 		}
 	}
 	
 	@Deprecated
 	private void fillSeroPositiveWomen(TestsAndTreatment dst, Patient p) {
 		for (Encounter enc : getEncountersForForm(p, "RHEA Sero Positive Women", "Sero Positive")) {
 			TestsAndTreatment.SeroPositiveWomen spw = new TestsAndTreatment.SeroPositiveWomen();
 			
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.CREATININE_LEVEL.getConcept().equals(obs.getConcept()))
 					spw.setCreatinineLevel(obs.getValueNumeric());
 				else if (MaternalConcept.CD4_DATE.getConcept().equals(obs.getConcept()))
 					spw.setCD4CountDate(obs.getValueDatetime());
 				else if (MaternalConcept.CD4_COUNT.getConcept().equals(obs.getConcept()))
 					spw.setCD4Count(obs.getValueNumeric());
 				else if (MaternalConcept.WHO_STAGE.getConcept().equals(obs.getConcept()))
 					spw.setWHOStage(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.ARV_REGIMEN.getConcept().equals(obs.getConcept()))
 					spw.setARVProphylaxis(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.ARV_START_DATE.getConcept().equals(obs.getConcept()))
 					spw.setARVProphylaxisDate(obs.getValueDatetime());
 				else if (MaternalConcept.COTRIMOXAZOLE_DATE.getConcept().equals(obs.getConcept()))
 					spw.setCotrimoxazoleStartDate(obs.getValueDatetime());
 			}
 			
 			dst.addSeroPositiveWomenEncounter(spw);
 		}
 		Collections.reverse(dst.getSeroPositiveWomen());
 	}
 	
 	private void fillTreatmentInterventions(TestsAndTreatment dst, Patient p) {
 		List<Encounter> encs = getEncountersForForm(p, "RHEA Maternal Treatments and Interventions", "Maternal Treatments and Interventions");
 		if (encs==null || encs.isEmpty())
 			return;
 		
 		for (Encounter enc : encs) {
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.INTERVENTION_IRON_AND_FOLIC_ACID.getConcept().equals(obs.getConcept()))
 					addInterventionIfGiven(enc, obs, dst, "Iron and Folic Acid");
 				else if (MaternalConcept.INTERVENTION_PYRIMETHAMINE_SULFADOXINE.getConcept().equals(obs.getConcept()))
 					addInterventionIfGiven(enc, obs, dst, "Pyrimethamine Sulfadoxine");
 				else if (MaternalConcept.INTERVENTION_MEBENDAZOLE.getConcept().equals(obs.getConcept()))
 					addInterventionIfGiven(enc, obs, dst, "Mebendazole");
 				else if (MaternalConcept.INTERVENTION_MOSQUITO_NET.getConcept().equals(obs.getConcept()))
 					addInterventionIfGiven(enc, obs, dst, "Mosquito Net");
 				else if (MaternalConcept.INTERVENTION_TETANUS_VACCINE.getConcept().equals(obs.getConcept()))
 					addInterventionIfGiven(enc, obs, dst, "Tetanus Vaccine");
 			}
 		}
 	}
 	
 	private static void addInterventionIfGiven(Encounter enc, Obs obs, TestsAndTreatment dst, String interventionName) {
 		if (MaternalConcept.YES.getConcept().equals(obs.getValueCoded()))
 			dst.addIntervention(new TestsAndTreatment.TreatmentIntervention(interventionName, enc.getEncounterDatetime()));
 	}
 
 	
 	/* ANC Visits */
 	
 	private List<ANCVisitsEntry> getANCVisits(Patient p) {
 		List<ANCVisitsEntry> res = new ArrayList<ANCVisitsEntry>();
 		
 		for (Encounter enc : getEncountersForForm(p, "RHEA ANC Physical", "Physical")) {
 			ANCVisitsEntry entry = new ANCVisitsEntry();
 			entry.setDate(enc.getEncounterDatetime());
 			
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.WEIGHT.getConcept().equals(obs.getConcept()))
 					entry.setWeight(obs.getValueNumeric());
 				else if (MaternalConcept.WEIGHT_CHANGE.getConcept().equals(obs.getConcept()))
 					entry.setWeightChange(obs.getValueNumeric());
 				else if (MaternalConcept.NUMBER_OF_WEEKS_PREGNANT.getConcept().equals(obs.getConcept()))
 					entry.setWeeksPregnant(obs.getValueNumeric().intValue());
 				else if (MaternalConcept.TEMPERATURE.getConcept().equals(obs.getConcept()))
 					entry.setTemperature(obs.getValueNumeric());
 				else if (MaternalConcept.BP_SYSTOLIC.getConcept().equals(obs.getConcept()))
 					entry.setBloodPressureSystolic(castInt(obs.getValueNumeric()));
 				else if (MaternalConcept.BP_DIASTOLIC.getConcept().equals(obs.getConcept()))
 					entry.setBloodPressureDiastolic(castInt(obs.getValueNumeric()));
 				else if (MaternalConcept.UTERUS_LENGTH.getConcept().equals(obs.getConcept()))
 					entry.setUterusLength(obs.getValueNumeric());
 				else if (MaternalConcept.FETAL_HEART_RATE.getConcept().equals(obs.getConcept()))
 					entry.setFetalHeartRate(castInt(obs.getValueNumeric()));
 				else if (MaternalConcept.PRESENTATION.getConcept().equals(obs.getConcept()))
 					entry.setPresentation(obs.getValueAsString(Context.getLocale()));
 			}
 				
 			res.add(entry);
 		}
 		
 		Collections.reverse(res);
 		return res;
 	}
 	
 	/* Referrals */
 	
 	private Referrals getReferrals(Patient p) {
 		Referrals dst = new Referrals();
 		EncounterService es = Context.getEncounterService();
 		EncounterType referralEncounterType = Context.getEncounterService().getEncounterType("ANC Referral");
 		EncounterType referralConfirmationEncounterType = Context.getEncounterService().getEncounterType("ANC Referral Confirmation");
 		
 		List<Encounter> referrals = es.getEncounters(
 			p, null, null, null, null, Collections.singleton(referralEncounterType), null, false
 		);
 		List<Encounter> confirmations = es.getEncounters(
 			p, null, null, null, null, Collections.singleton(referralConfirmationEncounterType), null, false
 		);
 		
 		Collections.sort(referrals, new EncDateSorter());
 		Collections.sort(confirmations, new EncDateSorter());
 		
 		dst.setReferrals(convertToReferrals(referrals));
 		dst.setConfirmations(convertToConfirmations(confirmations));
 		
 		if (referrals.size()==0) {
 			dst.setReferredButNotConfirmed(false);
 		} else if (confirmations.size()==0) {
 			dst.setReferredButNotConfirmed(true);
 		} else {
 			dst.setReferredButNotConfirmed(
 				referrals.get(0).getEncounterDatetime().after(confirmations.get(0).getEncounterDatetime())
 			);
 		}
 		
 		return dst;
 	}
 	
 	private List<Referrals.Referral> convertToReferrals(List<Encounter> encs) {
 		List<Referrals.Referral> res = new ArrayList<Referrals.Referral>(encs.size());
 		
 		for (Encounter enc : encs) {
 			Referrals.Referral ref = new Referrals.Referral();
 			ref.setDate(enc.getEncounterDatetime());
 			
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.REFERRED_TO.getConcept().equals(obs.getConcept()))
 					ref.setReferredTo(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.REFERRAL_URGENCY.getConcept().equals(obs.getConcept()))
 					ref.setUrgency(obs.getValueAsString(Context.getLocale()));
 				else if (MaternalConcept.REFERRAL_REASON.getConcept().equals(obs.getConcept()))
 					ref.setReason(obs.getValueAsString(Context.getLocale()));
 			}
 			
 			res.add(ref);
 		}
 		
 		return res;
 	}
 	
 	private List<Referrals.Confirmation> convertToConfirmations(List<Encounter> encs) {
 		List<Referrals.Confirmation> res = new ArrayList<Referrals.Confirmation>(encs.size());
 		
 		for (Encounter enc : encs) {
 			Referrals.Confirmation ref = new Referrals.Confirmation();
 			ref.setDate(enc.getEncounterDatetime());
 			
 			for (Obs obs : enc.getObs()) {
 				if (MaternalConcept.REFERRAL_CONFIRMATION_COMMENTS.getConcept().equals(obs.getConcept()))
 					ref.setComments(obs.getValueAsString(Context.getLocale()));
 			}
 			
 			res.add(ref);
 		}
 		
 		return res;
 	}
 	
 	private static class EncDateSorter implements Comparator<Encounter> {
 		@Override
 		public int compare(Encounter enc1, Encounter enc2) {
 			if (enc1==enc2)
 				return 0;
 			return enc2.getEncounterDatetime().compareTo(enc1.getEncounterDatetime());
 		}
 	}
 	
 	/* RapidSMS Messages */
 	
 	private List<RapidSMSMessage> getRapidSMSMessages(Patient p) {
 		EncounterService es = Context.getEncounterService();
 		List<RapidSMSMessage> res = new ArrayList<RapidSMSMessage>();
 		List<EncounterType> types = new ArrayList<EncounterType>(3);
 		types.add( Context.getEncounterService().getEncounterType("RapidSMS Notification BIRTH") );
 		types.add( Context.getEncounterService().getEncounterType("RapidSMS Notification RISK") );
 		types.add( Context.getEncounterService().getEncounterType("RapidSMS Notification Maternal Death") );
 		
 		for (EncounterType type : types) {
 			if (type==null) {
 				log.error("RapidSMS Notification encounter types are not correctly setup on the system");
 				return Collections.emptyList();
 			}
 		}
 		
 		List<Encounter> encs = es.getEncounters(
 			p, null, null, null, null, types, null, false
 		);
 		
		for (int i=encs.size()-1; i>=0; i--) {
 			Encounter enc = encs.get(i);
 			for (Obs obs : enc.getObs()) {
 				RapidSMSMessage msg = new RapidSMSMessage();
 				msg.setDate(enc.getEncounterDatetime());
 				msg.setType(enc.getEncounterType().getName());
 				msg.setName(obs.getConcept().getDisplayString());
 				msg.setValue(obs.getValueAsString(Context.getLocale()));
 				
 				for (ConceptMap mapping : obs.getConcept().getConceptMappings()) {
 					if ("RSMS".equalsIgnoreCase(mapping.getSource().getName())) {
 						msg.setCode(mapping.getSourceCode());
 						break;
 					}
 				}
 				
 				res.add(msg);
 			}
 		}
 		
 		return res;
 	}
 	
 	/**/
 	
 	private Form searchForForm(String nameLike) {
 		String query = nameLike.toLowerCase();
 		
 		for (Form form : Context.getFormService().getAllForms()) {
 			if (form.getName().toLowerCase().contains(query))
 				return form;
 		}
 		
 		throw new RuntimeException("Form with name like " + nameLike + " not found. Have the forms been uploaded?");
 	}
 	
 	private List<Encounter> getEncountersForForm(Patient p, String formName, String fallbackName) {
 		Form form = Context.getFormService().getForm(formName);
 		if (form==null)
 			form = searchForForm(fallbackName);
 		
 		return Context.getEncounterService().getEncounters(
 			p, (Location)null, (Date) null, (Date) null, (Collection<Form>)Collections.singleton(form),
 			(Collection<EncounterType>)null, (Collection<User>)null, false
 		);
 	}
 	
 	private Encounter getLatestEncounterForForm(Patient p, String formName, String fallbackName) {
 		List<Encounter> encounters = getEncountersForForm(p, formName, fallbackName);
 		if (encounters==null || encounters.isEmpty())
 			return null;
 		
 		return getLatestEncounter(encounters);
 	}
 	
 	private Encounter getLatestEncounter(List<Encounter> encounters) {
 		Encounter latest = encounters.get(0);
 		
 		for (int i=1; i<encounters.size(); i++) {
 			if (latest.getEncounterDatetime().compareTo(encounters.get(i).getEncounterDatetime()) < 0)
 				latest = encounters.get(i);
 		}
 		
 		return latest;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private static Integer buildKey(Class clazz, int patientId) {
 		return clazz.getName().hashCode()*clazz.hashCode() + patientId;
 	}
 	
 	private static final Integer castInt(Double d) {
 		return d!=null ? d.intValue() : null;
 	}
 }
