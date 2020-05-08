 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.Allergen;
 import models.Analysis;
 import models.AnalysisNorm;
 import models.ClinicalManifestation;
 import models.ClinicalManifestationComplaint;
 import models.ClinicalManifestationNorm;
 import models.Complaint;
 import models.ComplaintType;
 import models.Insertion;
 import models.MedicationDetail;
 import models.MedicineCard;
 import models.Nosology;
 import models.NosologyAnalysis;
 import models.NosologyClinicalManifestation;
 import models.NosologyComplaintType;
 import models.Survey;
 import models.SurveyAnalysis;
 import models.SurveyAnswer;
 import models.SurveyClinicalManifestation;
 import models.SurveyTreatment;
 import models.Syndrome;
 import models.User;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.With;
 import wrappers.AnalysisNormWrapper;
 import wrappers.ClinicalManifestationNormWrapper;
 import wrappers.ComplaintWrapper;
 import wrappers.SurveyAnswerWrapper;
 import wrappers.SurveyWrapper;
 import wrappers.TreatmentWrapper;
 
 import com.google.gson.Gson;
 import com.immunology.enums.TherapyType;
 
 @With(Secure.class)
 public class SurveyView extends Controller {
 
 	public static void add(Long id) {
 		User user = User.find("byLogin", Security.connected()).first();
 		List<Syndrome> syndromes = Syndrome.findAll();
 		MedicineCard medicineCard = MedicineCard.findById(id);
 
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("user", user);
 		model.put("syndromes", syndromes);
 		model.put("medicineCardId", medicineCard.medicineCardId);
 		model.put("medicineCard", medicineCard);
 
 		renderTemplate(model);
 	}
 
     public static void addNosology(Long id) {
         User user = User.find("byLogin", Security.connected()).first();
        List<Nosology> nosologies = Nosology.findAll();
         MedicineCard medicineCard = MedicineCard.findById(id);
 
        render(user, nosologies, medicineCard.medicineCardId, medicineCard);
     }
 
 
     public static void edit(Long id) {
 		User user = User.find("byLogin", Security.connected()).first();
 		Survey survey = Survey.findById(id);
 
 		List<Syndrome> syndromes = Syndrome.findAll();
 
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("user", user);
 		model.put("survey", survey);
 		model.put("syndromes", syndromes);
 
 		renderTemplate(model);
 	}
 
 	public static void delete() {
 		Long id = Long.parseLong(request.params.get("body"));
 		Survey survey = Survey.findById(id);
 		try {
 			Survey.em().remove(survey);
 		} catch (Exception e) {
 			flash.error("Syrvey deleting error", "args");
 		}
 		flash.success(Messages.get("survey.deleted"));
 	}
 
 	public static void show(Long id) {
 		User user = User.find("byLogin", Security.connected()).first();
 		Survey survey = Survey.findById(id);
 
 		List<Syndrome> syndromes = Syndrome.findAll();
 
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("user", user);
 		model.put("survey", survey);
 		model.put("syndromes", syndromes);
 
 		renderTemplate(model);
 	}
 
 	public static void reloadComplaints(Long id) {
 		Syndrome syndrome = Syndrome.findById(id);
 		render("tags/survey/complaintsAddTab.html", syndrome);
 	}
 
	public static void reloadComplaintsForNosology(Long id) {
		Nosology syndrome = Nosology.findById(id);
		render("tags/survey/complaintsAddTab.html", syndrome);
	}

 	public static void reloadAnalysis(Long id) {
 		Syndrome syndrome = Syndrome.findById(id);
 		render("tags/survey/analysesAddTab.html", syndrome);
 	}
 
 	public static void reloadClinicalManifestations(Long id) {
 		Syndrome syndrome = Syndrome.findById(id);
 		render("tags/survey/clinicsAddTab.html", syndrome);
 	}
 
 	public static void reloadTreatments(Long id) {
 		Syndrome syndrome = Syndrome.findById(id);
 		render("tags/survey/treatmentsAddTab.html", syndrome);
 	}
 
 	public static void reloadNosology(Long id) {
 		Syndrome syndrome = Syndrome.findById(id);
 		render("tags/survey/nosologyAddTab.html", syndrome);
 	}
 
 	public static void showComplaints(Long syndromeId, Long surveyId) {
 		Map<ComplaintType, List<SurveyAnswer>> contentMap = new HashMap<ComplaintType, List<SurveyAnswer>>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyAnswer> surveyAnswers = survey.surveyAnswers;
 
 		ComplaintType complaintType = null;
 		List<SurveyAnswer> surveyAnswerList = null;
 		for (SurveyAnswer surveyAnswer : surveyAnswers) {
 			if (surveyAnswer.answerValue > 0) {
 				complaintType = surveyAnswer.complaint.complaintType;
 				if (complaintType.syndrome.equals(syndrome)) {
 					if (contentMap.containsKey(complaintType)) {
 						contentMap.get(complaintType).add(surveyAnswer);
 					} else {
 						surveyAnswerList = new ArrayList<SurveyAnswer>();
 						surveyAnswerList.add(surveyAnswer);
 						contentMap.put(complaintType, surveyAnswerList);
 					}
 				}
 			}
 		}
 		render("tags/survey/complaintsShowTab.html", contentMap);
 	}
 
 	public static void showAnalysis(Long syndromeId, Long surveyId) {
 		List<SurveyAnalysis> surveyAnalyses = new ArrayList<SurveyAnalysis>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyAnalysis> surveyAnalysisList = survey.surveyAnalyses;
 
 		for (SurveyAnalysis surveyAnalysis : surveyAnalysisList) {
 			if (surveyAnalysis.isChecked) {
 				if (surveyAnalysis.analysisNorm.analysis.syndrome.equals(syndrome)) {
 					surveyAnalyses.add(surveyAnalysis);
 				}
 			}
 		}
 		render("tags/survey/analysesShowTab.html", surveyAnalyses);
 	}
 
 	public static void showClinicalManifestations(Long syndromeId, Long surveyId) {
 		Map<ClinicalManifestationComplaint, List<SurveyClinicalManifestation>> contentMap = new HashMap<ClinicalManifestationComplaint, List<SurveyClinicalManifestation>>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyClinicalManifestation> surveyClinicalManifestationList = null;
 		List<SurveyClinicalManifestation> clinicalManifestationList = survey.surveyClinicalManifestations;
 		ClinicalManifestationComplaint clinicalManifestationComplaint = null;
 		for (SurveyClinicalManifestation surveyClinicalManifestation : clinicalManifestationList) {
 			if (surveyClinicalManifestation.isChecked) {
 				clinicalManifestationComplaint = surveyClinicalManifestation.clinicalManifestationNorm.clinicalManifestation.clinicalManifestationComplaint;
 				if (clinicalManifestationComplaint.syndrome.equals(syndrome)) {
 					if (contentMap.containsKey(clinicalManifestationComplaint)) {
 						contentMap.get(clinicalManifestationComplaint).add(surveyClinicalManifestation);
 					} else {
 						surveyClinicalManifestationList = new ArrayList<SurveyClinicalManifestation>();
 						surveyClinicalManifestationList.add(surveyClinicalManifestation);
 						contentMap.put(clinicalManifestationComplaint, surveyClinicalManifestationList);
 					}
 				}
 			}
 		}
 		render("tags/survey/clinicsShowTab.html", contentMap);
 	}
 
 	public static void showTreatments(Long syndromeId, Long surveyId) {
 		List<SurveyTreatment> surveyTreatments = new ArrayList<SurveyTreatment>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyTreatment> clinicalTreatmentList = survey.surveyTreatments;
 
 		for (SurveyTreatment surveyTreatment : clinicalTreatmentList) {
 			if (surveyTreatment.medicationDetail.medication.treatmentType.syndrome.equals(syndrome)) {
 				surveyTreatments.add(surveyTreatment);
 			}
 		}
 		render("tags/survey/treatmentsShowTab.html", surveyTreatments);
 	}
 
 	public static void showNosology(Long syndromeId, Long surveyId) {
 		Survey survey = Survey.findById(surveyId);
 
 		Nosology nosology = survey.nosology;
 		render("tags/survey/nosologyShowTab.html", nosology);
 	}
 
 	public static void editComplaints(Long syndromeId, Long surveyId) {
 		Map<ComplaintType, List<SurveyAnswer>> contentMap = new HashMap<ComplaintType, List<SurveyAnswer>>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyAnswer> surveyAnswers = survey.surveyAnswers;
 
 		ComplaintType complaintType = null;
 		List<SurveyAnswer> surveyAnswerList = null;
 		for (SurveyAnswer surveyAnswer : surveyAnswers) {
 			complaintType = surveyAnswer.complaint.complaintType;
 
 			if (complaintType.syndrome.equals(syndrome)) {
 				if (contentMap.containsKey(complaintType)) {
 					contentMap.get(complaintType).add(surveyAnswer);
 				} else {
 					surveyAnswerList = new ArrayList<SurveyAnswer>();
 					surveyAnswerList.add(surveyAnswer);
 					contentMap.put(complaintType, surveyAnswerList);
 				}
 			}
 		}
 		render("tags/survey/complaintsEditTab.html", contentMap);
 	}
 
 	public static void editAnalysis(Long syndromeId, Long surveyId) {
 		List<SurveyAnalysis> surveyAnalyses = new ArrayList<SurveyAnalysis>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyAnalysis> surveyAnalysisList = survey.surveyAnalyses;
 
 		for (SurveyAnalysis surveyAnalysis : surveyAnalysisList) {
 			if (surveyAnalysis.analysisNorm.analysis.syndrome.equals(syndrome)) {
 				surveyAnalyses.add(surveyAnalysis);
 			}
 		}
 		render("tags/survey/analysesEditTab.html", surveyAnalyses);
 	}
 
 	public static void editClinicalManifestations(Long syndromeId, Long surveyId) {
 		Map<ClinicalManifestationComplaint, List<SurveyClinicalManifestation>> contentMap = new HashMap<ClinicalManifestationComplaint, List<SurveyClinicalManifestation>>();
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		List<SurveyClinicalManifestation> surveyClinicalManifestationList = null;
 		List<SurveyClinicalManifestation> clinicalManifestationList = survey.surveyClinicalManifestations;
 		ClinicalManifestationComplaint clinicalManifestationComplaint = null;
 		for (SurveyClinicalManifestation surveyClinicalManifestation : clinicalManifestationList) {
 			clinicalManifestationComplaint = surveyClinicalManifestation.clinicalManifestationNorm.clinicalManifestation.clinicalManifestationComplaint;
 			if (clinicalManifestationComplaint.syndrome.equals(syndrome)) {
 				if (contentMap.containsKey(clinicalManifestationComplaint)) {
 					contentMap.get(clinicalManifestationComplaint).add(surveyClinicalManifestation);
 				} else {
 					surveyClinicalManifestationList = new ArrayList<SurveyClinicalManifestation>();
 					surveyClinicalManifestationList.add(surveyClinicalManifestation);
 					contentMap.put(clinicalManifestationComplaint, surveyClinicalManifestationList);
 				}
 			}
 		}
 		render("tags/survey/clinicsEditTab.html", contentMap);
 	}
 
 	public static void editTreatments(Long syndromeId, Long surveyId) {
 		Survey survey = Survey.findById(surveyId);
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		List<SurveyTreatment> surveyTreatments = new ArrayList<SurveyTreatment>();
 		List<MedicationDetail> medicationDetails = new ArrayList<MedicationDetail>();
 
 		List<SurveyTreatment> clinicalTreatmentList = survey.surveyTreatments;
 
 		for (SurveyTreatment surveyTreatment : clinicalTreatmentList) {
 			if (surveyTreatment.medicationDetail.medication.treatmentType.syndrome.equals(syndrome)) {
 				surveyTreatments.add(surveyTreatment);
 				medicationDetails.add(surveyTreatment.medicationDetail);
 			}
 		}
 		render("tags/survey/treatmentsEditTab.html", syndrome, surveyTreatments, medicationDetails);
 	}
 
 	public static void editNosology(Long syndromeId, Long surveyId) {
 		Syndrome syndrome = Syndrome.findById(syndromeId);
 		Survey survey = Survey.findById(surveyId);
 
 		Nosology nosology = survey.nosology;
 		render("tags/survey/nosologyEditTab.html", syndrome, nosology);
 	}
 
 	public static void saveComplaints() {
 		User user = User.find("byLogin", Security.connected()).first();
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 		Nosology nosology = null;
 
 		Long surveyId = surveyWrapper.surveyId;
 		Long medicineCardId = surveyWrapper.medicineCardId;
 		Long syndromeId = surveyWrapper.syndromeId;
 		List<ComplaintWrapper> complaintWrappers = surveyWrapper.complaints;
 
 		Survey survey = null;
 		if (surveyId != -1) {
 			survey = Survey.findById(surveyId);
 			nosology = survey.nosology;
 		} else {
 			survey = new Survey();
 			survey.user = user;
 			survey.medicineCard = MedicineCard.findById(medicineCardId);
 			survey.syndrome = Syndrome.findById(syndromeId);
 			survey.surveyDate = new Date();
 			survey.alergeticDifficultyDegree = 0;
 			survey.alergeticInsufficiencyDegree = 0;
 			survey.surveyAnswers = null;
 			survey.surveyAnalyses = null;
 		}
 
 		List<NosologyComplaintType> nosologyComplaintTypes = null;
 		double val = 0;
 		double maxVal = 0;
 		int difficultyDegree = 0;
 		if (nosology != null) {
 			nosologyComplaintTypes = nosology.nosologyComplaintTypes;
 
 			for (SurveyClinicalManifestation surveyClinicalManifestation : survey.surveyClinicalManifestations) {
 				if (surveyClinicalManifestation.multiplier != null) {
 					maxVal += 3 * surveyClinicalManifestation.multiplier;
 					val += surveyClinicalManifestation.value * surveyClinicalManifestation.multiplier;
 				}
 			}
 
 		}
 
 		SurveyAnswer surveyAnswer = null;
 		Complaint complaint = null;
 		ComplaintType complaintType = null;
 		List<SurveyAnswer> surveyAnswers = new ArrayList<SurveyAnswer>();
 		for (ComplaintWrapper complaintWrapper : complaintWrappers) {
 			complaint = Complaint.findById(complaintWrapper.id);
 			complaintType = complaint.complaintType;
 
 			surveyAnswer = new SurveyAnswer();
 			surveyAnswer.complaint = complaint;
 			surveyAnswer.answerValue = complaintWrapper.value;
 			surveyAnswer.multiplier = 0.0;
 			surveyAnswer.survey = survey;
 			surveyAnswers.add(surveyAnswer);
 
 			if (nosologyComplaintTypes != null) {
 				for (NosologyComplaintType nosologyComplaintType : nosologyComplaintTypes) {
 					if (nosologyComplaintType.complaintTypeId.equals(complaintType.complaintTypeId)) {
 						surveyAnswer.multiplier = nosologyComplaintType.multyplier;
 						break;
 					}
 				}
 			}
 			if (surveyAnswer.multiplier != null) {
 				maxVal += 3 * surveyAnswer.multiplier;
 				val += surveyAnswer.answerValue * surveyAnswer.multiplier;
 			}
 		}
 		difficultyDegree = (int) (val / maxVal * 100);
 		survey.surveyAnswers = new ArrayList<SurveyAnswer>(surveyAnswers);
 		survey.alergeticDifficultyDegree = difficultyDegree;
 		survey = survey.save();
 
 		renderJSON(survey.surveyId);
 	}
 
 	public static void saveAnalyses() {
 		User user = User.find("byLogin", Security.connected()).first();
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Long surveyId = surveyWrapper.surveyId;
 		Long medicineCardId = surveyWrapper.medicineCardId;
 		Long syndromeId = surveyWrapper.syndromeId;
 		List<AnalysisNormWrapper> analysisNormWrappers = surveyWrapper.analysesNorms;
 
 		Survey survey = null;
 		Nosology nosology = null;
 		if (surveyId != -1) {
 			survey = Survey.findById(surveyId);
 			nosology = survey.nosology;
 		} else {
 			survey = new Survey();
 			survey.user = user;
 			survey.medicineCard = MedicineCard.findById(medicineCardId);
 			survey.syndrome = Syndrome.findById(syndromeId);
 			survey.surveyDate = new Date();
 			survey.alergeticDifficultyDegree = 0;
 			survey.alergeticInsufficiencyDegree = 0;
 			survey.surveyAnswers = null;
 			survey.surveyAnalyses = null;
 		}
 
 		List<NosologyAnalysis> nosologyAnalyses = null;
 		if (nosology != null) {
 			nosologyAnalyses = nosology.nosologyAnalysis;
 		}
 
 		double val = 0;
 		double maxVal = 0;
 		int insufficiencyDegree = 0;
 		AnalysisNorm analysisNorm = null;
 		Analysis analysis = null;
 		SurveyAnalysis surveyAnalysis = null;
 		List<SurveyAnalysis> surveyAnalyses = new ArrayList<SurveyAnalysis>();
 		for (AnalysisNormWrapper analysisNormWrapper : analysisNormWrappers) {
 			analysisNorm = AnalysisNorm.findById(analysisNormWrapper.id);
 			analysis = analysisNorm.analysis;
 
 			surveyAnalysis = new SurveyAnalysis();
 			surveyAnalysis.survey = survey;
 			surveyAnalysis.analysisNorm = analysisNorm;
 			surveyAnalysis.multiplier = 0.0;
 			surveyAnalysis.isChecked = analysisNormWrapper.isChecked;
 			if (surveyAnalysis.isChecked) {
 				surveyAnalysis.description = analysisNormWrapper.description;
 				surveyAnalysis.value = analysisNorm.value;
 			} else {
 				surveyAnalysis.value = 0;
 				surveyAnalysis.description = null;
 			}
 			surveyAnalyses.add(surveyAnalysis);
 
 			if (nosologyAnalyses != null) {
 				for (NosologyAnalysis nosologyAnalysis : nosologyAnalyses) {
 					if (nosologyAnalysis.analysisId.equals(analysis.analysisId)) {
 						surveyAnalysis.multiplier = nosologyAnalysis.multyplier;
 						break;
 					}
 				}
 			}
 			if (surveyAnalysis.multiplier != null) {
 				val += surveyAnalysis.value * surveyAnalysis.multiplier;
 				maxVal += 3 * surveyAnalysis.multiplier;
 			}
 		}
 		insufficiencyDegree = (int) (val / maxVal * 100);
 
 		survey.surveyAnalyses = surveyAnalyses;
 		survey.alergeticInsufficiencyDegree = insufficiencyDegree;
 
 		survey = survey.save();
 
 		renderJSON(survey.surveyId);
 	}
 
 	public static void saveClinicalManifestations() {
 		User user = User.find("byLogin", Security.connected()).first();
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Long surveyId = surveyWrapper.surveyId;
 		Long medicineCardId = surveyWrapper.medicineCardId;
 		Long syndromeId = surveyWrapper.syndromeId;
 		List<ClinicalManifestationNormWrapper> clinicalManifestationNormWrappers = surveyWrapper.clinicalManifestationNorms;
 		Nosology nosology = null;
 		Survey survey = null;
 		if (surveyId != -1) {
 			survey = Survey.findById(surveyId);
 			nosology = survey.nosology;
 		} else {
 			survey = new Survey();
 			survey.user = user;
 			survey.medicineCard = MedicineCard.findById(medicineCardId);
 			survey.syndrome = Syndrome.findById(syndromeId);
 			survey.surveyDate = new Date();
 			survey.alergeticDifficultyDegree = 0;
 			survey.alergeticInsufficiencyDegree = 0;
 			survey.surveyAnswers = null;
 			survey.surveyAnalyses = null;
 			survey.surveyClinicalManifestations = null;
 		}
 
 		double val = 0;
 		double maxVal = 0;
 		int difficultyDegree = 0;
 		List<NosologyClinicalManifestation> nosologyClinicalManifestations = null;
 		ClinicalManifestationNorm clinicalManifestationNorm = null;
 		SurveyClinicalManifestation surveyClinicalManifestation = null;
 		List<SurveyClinicalManifestation> surveyClinicalManifestations = new ArrayList<SurveyClinicalManifestation>();
 		if (nosology != null) {
 			nosologyClinicalManifestations = nosology.nosologyClinicalManifestations;
 
 			for (SurveyAnswer surveyAnswer : survey.surveyAnswers) {
 				if (surveyAnswer.multiplier != null) {
 					maxVal += 3 * surveyAnswer.multiplier;
 					val += surveyAnswer.answerValue * surveyAnswer.multiplier;
 				}
 			}
 		}
 
 		for (ClinicalManifestationNormWrapper clinicalManifestationNormWrapper : clinicalManifestationNormWrappers) {
 			clinicalManifestationNorm = ClinicalManifestationNorm.findById(clinicalManifestationNormWrapper.id);
 			surveyClinicalManifestation = new SurveyClinicalManifestation();
 			surveyClinicalManifestation.survey = survey;
 			surveyClinicalManifestation.clinicalManifestationNorm = clinicalManifestationNorm;
 			surveyClinicalManifestation.value = clinicalManifestationNorm.value;
 			surveyClinicalManifestation.description = null;
 			surveyClinicalManifestation.multiplier = 0.0;
 			surveyClinicalManifestation.isChecked = clinicalManifestationNormWrapper.isChecked;
 			if (surveyClinicalManifestation.isChecked) {
 				surveyClinicalManifestation.value = clinicalManifestationNorm.value;
 			} else {
 				surveyClinicalManifestation.value = 0;
 			}
 
 			surveyClinicalManifestations.add(surveyClinicalManifestation);
 
 			if (nosologyClinicalManifestations != null) {
 				for (NosologyClinicalManifestation nosologyClinicalManifestation : nosologyClinicalManifestations) {
 					if (nosologyClinicalManifestation.clinicalManifestationId
 							.equals(clinicalManifestationNorm.clinicalManifestation.id)) {
 						surveyClinicalManifestation.multiplier = nosologyClinicalManifestation.multyplier;
 						break;
 					}
 				}
 			}
 			if (surveyClinicalManifestation.multiplier != null) {
 				val += surveyClinicalManifestation.value * surveyClinicalManifestation.multiplier;
 				maxVal += 3 * surveyClinicalManifestation.multiplier;
 			}
 		}
 		if (nosology != null) {
 			difficultyDegree = (int) (val / maxVal * 100);
 		}
 
 		survey.surveyClinicalManifestations = surveyClinicalManifestations;
 		survey.alergeticDifficultyDegree = difficultyDegree;
 		survey = survey.save();
 
 		renderJSON(survey.surveyId);
 	}
 
 	public static void saveTreatments() {
 		User user = User.find("byLogin", Security.connected()).first();
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Long surveyId = surveyWrapper.surveyId;
 		Long medicineCardId = surveyWrapper.medicineCardId;
 		Long syndromeId = surveyWrapper.syndromeId;
 		List<TreatmentWrapper> treatmentWrappers = surveyWrapper.treatments;
 
 		Survey survey = null;
 		if (surveyId != -1) {
 			survey = Survey.findById(surveyId);
 		} else {
 			survey = new Survey();
 			survey.user = user;
 			survey.medicineCard = MedicineCard.findById(medicineCardId);
 			survey.syndrome = Syndrome.findById(syndromeId);
 			survey.surveyDate = new Date();
 			survey.surveyAnswers = null;
 			survey.surveyAnalyses = null;
 			survey.surveyClinicalManifestations = null;
 			survey.surveyTreatments = null;
 		}
 
 		SurveyTreatment surveyTreatment = null;
 		List<SurveyTreatment> surveyTreatments = new ArrayList<SurveyTreatment>();
 		if (treatmentWrappers != null && !treatmentWrappers.isEmpty()) {
 			for (TreatmentWrapper treatmentWrapper : treatmentWrappers) {
 				if (treatmentWrapper.isChecked) {
 					surveyTreatment = new SurveyTreatment();
 					surveyTreatment.survey = survey;
 					surveyTreatment.medicationDetail = MedicationDetail.findById(treatmentWrapper.id);
 					surveyTreatment.treatmentDescription = treatmentWrapper.description;
 					surveyTreatment.dose = treatmentWrapper.dose;
 					surveyTreatment.multiplicity = treatmentWrapper.multiplicity;
 					if (treatmentWrapper.insertionId != null) {
 						surveyTreatment.insertion = Insertion.findById(treatmentWrapper.insertionId);
 					}
 					if (treatmentWrapper.therapy != null) {
 						surveyTreatment.therapyType = TherapyType.valueOf(treatmentWrapper.therapy);
 					}
 					if (treatmentWrapper.allegrenId != null) {
 						surveyTreatment.allergen = Allergen.findById(treatmentWrapper.allegrenId);
 					}
 
 					surveyTreatments.add(surveyTreatment);
 				}
 			}
 		}
 
 		survey.surveyTreatments = surveyTreatments;
 
 		survey = Survey.em().merge(survey).save();
 
 		renderJSON(survey.surveyId);
 	}
 
 	public static void saveNosology() {
 		User user = User.find("byLogin", Security.connected()).first();
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Long surveyId = surveyWrapper.surveyId;
 		Long medicineCardId = surveyWrapper.medicineCardId;
 		Long syndromeId = surveyWrapper.syndromeId;
 		Long nosologyId = surveyWrapper.nosologyId;
 
 		Nosology nosology = null;
 		if (nosologyId != null) {
 			nosology = Nosology.findById(nosologyId);
 		}
 
 		Survey survey = null;
 		if (surveyId != -1) {
 			survey = Survey.findById(surveyId);
 			survey.nosology = nosology;
 			if (nosology != null) {
 				survey.nosology = nosology;
 				// TODO get by syndrome
 				List<SurveyAnswer> surveyAnswers = survey.surveyAnswers;
 
 				double val = 0;
 				double maxVal = 0;
 				int difficultyDegree = 0;
 				ComplaintType complaintType = null;
 				for (SurveyAnswer surveyAnswer : surveyAnswers) {
 					surveyAnswer.multiplier = 0.0;
 					complaintType = surveyAnswer.complaint.complaintType;
 					for (NosologyComplaintType nosologyComplaintType : nosology.nosologyComplaintTypes) {
 						if (nosologyComplaintType.complaintTypeId.equals(complaintType.complaintTypeId)) {
 							surveyAnswer.multiplier = nosologyComplaintType.multyplier;
 							break;
 						}
 					}
 
 					if (surveyAnswer.multiplier != null) {
 						maxVal += 3 * surveyAnswer.multiplier;
 						val += surveyAnswer.answerValue * surveyAnswer.multiplier;
 					}
 				}
 
 				ClinicalManifestation clinicalManifestation = null;
 				for (SurveyClinicalManifestation surveyClinicalManifestation : survey.surveyClinicalManifestations) {
 					surveyClinicalManifestation.multiplier = 0.0;
 					clinicalManifestation = surveyClinicalManifestation.clinicalManifestationNorm.clinicalManifestation;
 
 					for (NosologyClinicalManifestation nosologyClinicalManifestation : nosology.nosologyClinicalManifestations) {
 						if (nosologyClinicalManifestation.clinicalManifestationId.equals(clinicalManifestation.id)) {
 							surveyClinicalManifestation.multiplier = nosologyClinicalManifestation.multyplier;
 							break;
 						}
 					}
 
 					if (surveyClinicalManifestation.multiplier != null) {
 						maxVal += 3 * surveyClinicalManifestation.multiplier;
 						val += surveyClinicalManifestation.value * surveyClinicalManifestation.multiplier;
 					}
 				}
 
 				difficultyDegree = (int) (val / maxVal * 100);
 				survey.alergeticDifficultyDegree = difficultyDegree;
 
 				val = 0;
 				maxVal = 0;
 				int insufficiencyDegree = 0;
 				Analysis analysis = null;
 				List<SurveyAnalysis> surveyAnalyses = survey.surveyAnalyses;
 				for (SurveyAnalysis surveyAnalysis : surveyAnalyses) {
 					if (surveyAnalysis.isChecked) {
 						surveyAnalysis.multiplier = null;
 						analysis = surveyAnalysis.analysisNorm.analysis;
 						for (NosologyAnalysis nosologyAnalysis : nosology.nosologyAnalysis) {
 							if (nosologyAnalysis.analysisId.equals(analysis.analysisId)) {
 								surveyAnalysis.multiplier = nosologyAnalysis.multyplier;
 								break;
 							}
 						}
 
 						if (surveyAnalysis.multiplier != null && surveyAnalysis.multiplier != 0) {
 							maxVal += 3 * surveyAnalysis.multiplier;
 							val += surveyAnalysis.value * surveyAnalysis.multiplier;
 						}
 					}
 				}
 
 				insufficiencyDegree = (int) (val / maxVal * 100);
 				survey.alergeticInsufficiencyDegree = insufficiencyDegree;
 			}
 		} else {
 			survey = new Survey();
 			survey.user = user;
 			survey.medicineCard = MedicineCard.findById(medicineCardId);
 			survey.syndrome = Syndrome.findById(syndromeId);
 			survey.surveyDate = new Date();
 			survey.alergeticDifficultyDegree = 0;
 			survey.alergeticInsufficiencyDegree = 0;
 			survey.surveyAnswers = null;
 			survey.surveyAnalyses = null;
 			survey.nosology = nosology;
 		}
 
 		survey.save();
 
 		renderJSON(survey.surveyId);
 	}
 
 	public static void updateComplaints() {
 		String jsonString = request.params.get("body");
 
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Long surveyId = surveyWrapper.surveyId;
 		List<SurveyAnswerWrapper> surveyAnswerWrappers = surveyWrapper.surveyAnswers;
 
 		Survey survey = Survey.findById(surveyId);
 		Nosology nosology = survey.nosology;
 
 		double val = 0;
 		double maxVal = 0;
 		int difficultyDegree = 0;
 
 		if (nosology != null) {
 			for (SurveyClinicalManifestation surveyClinicalManifestation : survey.surveyClinicalManifestations) {
 				if (surveyClinicalManifestation.multiplier != null) {
 					maxVal += 3 * surveyClinicalManifestation.multiplier;
 					val += surveyClinicalManifestation.value * surveyClinicalManifestation.multiplier;
 				}
 			}
 
 		}
 
 		SurveyAnswer surveyAnswer = null;
 		Integer value = null;
 		Long surveyAnswerId = null;
 		for (SurveyAnswerWrapper surveyAnswerWrapper : surveyAnswerWrappers) {
 			surveyAnswerId = surveyAnswerWrapper.id;
 			value = surveyAnswerWrapper.value;
 
 			surveyAnswer = SurveyAnswer.findById(surveyAnswerId);
 			surveyAnswer.answerValue = value;
 			surveyAnswer.save();
 
 			if (surveyAnswer.multiplier != null) {
 				maxVal += 3 * surveyAnswer.multiplier;
 				val += value * surveyAnswer.multiplier;
 			}
 		}
 
 		difficultyDegree = (int) (val / maxVal * 100);
 		survey.alergeticDifficultyDegree = difficultyDegree;
 
 		survey.save();
 		renderJSON(survey.surveyId);
 	}
 
 	public static void updateAnalyses() {
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Survey survey = Survey.findById(surveyWrapper.surveyId);
 		List<AnalysisNormWrapper> analysisNormWrappers = surveyWrapper.analysesNorms;
 
 		Nosology nosology = survey.nosology;
 		List<NosologyAnalysis> nosologyAnalyses = null;
 		if (nosology != null) {
 			nosologyAnalyses = nosology.nosologyAnalysis;
 		}
 
 		double val = 0;
 		double maxVal = 0;
 		int insufficiencyDegree = 0;
 		AnalysisNorm analysisNorm = null;
 		Analysis analysis = null;
 		SurveyAnalysis surveyAnalysis = null;
 		List<SurveyAnalysis> surveyAnalyses = new ArrayList<SurveyAnalysis>();
 		for (AnalysisNormWrapper analysisNormWrapper : analysisNormWrappers) {
 			analysisNorm = AnalysisNorm.findById(analysisNormWrapper.id);
 			analysis = analysisNorm.analysis;
 			surveyAnalysis = new SurveyAnalysis();
 			surveyAnalysis.survey = survey;
 			surveyAnalysis.analysisNorm = analysisNorm;
 			surveyAnalysis.multiplier = 0.0;
 			surveyAnalysis.isChecked = analysisNormWrapper.isChecked;
 			if (surveyAnalysis.isChecked) {
 				surveyAnalysis.value = analysisNorm.value;
 				surveyAnalysis.description = analysisNormWrapper.description;
 			} else {
 				surveyAnalysis.value = 0;
 				surveyAnalysis.description = null;
 			}
 			surveyAnalyses.add(surveyAnalysis);
 
 			if (nosologyAnalyses != null) {
 				for (NosologyAnalysis nosologyAnalysis : nosologyAnalyses) {
 					if (nosologyAnalysis.analysisId.equals(analysis.analysisId)) {
 						surveyAnalysis.multiplier = nosologyAnalysis.multyplier;
 						break;
 					}
 				}
 			}
 			if (surveyAnalysis.multiplier != null) {
 				val += surveyAnalysis.value * surveyAnalysis.multiplier;
 				maxVal += 3 * surveyAnalysis.multiplier;
 			}
 		}
 		insufficiencyDegree = (int) (val / maxVal * 100);
 		// TODO change functionality
 		for (SurveyAnalysis surveyAnalysis2 : survey.surveyAnalyses) {
 			surveyAnalysis2.delete();
 		}
 		survey.surveyAnalyses.clear();
 
 		survey.surveyAnalyses = surveyAnalyses;
 		survey.alergeticInsufficiencyDegree = insufficiencyDegree;
 		survey.save();
 		renderJSON(survey.surveyId);
 	}
 
 	public static void updateClinicalManifestations() {
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Survey survey = Survey.findById(surveyWrapper.surveyId);
 		List<ClinicalManifestationNormWrapper> clinicalManifestationNormWrappers = surveyWrapper.clinicalManifestationNorms;
 
 		Nosology nosology = survey.nosology;
 
 		double val = 0;
 		double maxVal = 0;
 		int difficultyDegree = 0;
 		List<NosologyClinicalManifestation> nosologyClinicalManifestations = null;
 		ClinicalManifestationNorm clinicalManifestationNorm = null;
 		SurveyClinicalManifestation surveyClinicalManifestation = null;
 		List<SurveyClinicalManifestation> surveyClinicalManifestations = new ArrayList<SurveyClinicalManifestation>();
 		if (nosology != null) {
 			nosologyClinicalManifestations = nosology.nosologyClinicalManifestations;
 
 			for (SurveyAnswer surveyAnswer : survey.surveyAnswers) {
 				if (surveyAnswer.multiplier != null) {
 					maxVal += 3 * surveyAnswer.multiplier;
 					val += surveyAnswer.answerValue * surveyAnswer.multiplier;
 				}
 			}
 		}
 
 		for (ClinicalManifestationNormWrapper clinicalManifestationNormWrapper : clinicalManifestationNormWrappers) {
 			clinicalManifestationNorm = ClinicalManifestationNorm.findById(clinicalManifestationNormWrapper.id);
 			surveyClinicalManifestation = new SurveyClinicalManifestation();
 			surveyClinicalManifestation.survey = survey;
 			surveyClinicalManifestation.clinicalManifestationNorm = clinicalManifestationNorm;
 			surveyClinicalManifestation.value = clinicalManifestationNorm.value;
 			surveyClinicalManifestation.description = null;
 			surveyClinicalManifestation.multiplier = 0.0;
 			surveyClinicalManifestation.isChecked = clinicalManifestationNormWrapper.isChecked;
 			if (surveyClinicalManifestation.isChecked) {
 				surveyClinicalManifestation.value = clinicalManifestationNorm.value;
 			} else {
 				surveyClinicalManifestation.value = 0;
 			}
 
 			surveyClinicalManifestations.add(surveyClinicalManifestation);
 
 			if (nosologyClinicalManifestations != null) {
 				for (NosologyClinicalManifestation nosologyClinicalManifestation : nosologyClinicalManifestations) {
 					if (nosologyClinicalManifestation.clinicalManifestationId
 							.equals(clinicalManifestationNorm.clinicalManifestation.id)) {
 						surveyClinicalManifestation.multiplier = nosologyClinicalManifestation.multyplier;
 						break;
 					}
 				}
 			}
 			if (surveyClinicalManifestation.multiplier != null) {
 				val += surveyClinicalManifestation.value * surveyClinicalManifestation.multiplier;
 				maxVal += 3 * surveyClinicalManifestation.multiplier;
 			}
 		}
 		if (nosology != null) {
 			difficultyDegree = (int) (val / maxVal * 100);
 		}
 
 		// TODO change functionality
 		for (SurveyClinicalManifestation clinicalManifestation : survey.surveyClinicalManifestations) {
 			clinicalManifestation.delete();
 		}
 		survey.surveyClinicalManifestations.clear();
 
 		survey.surveyClinicalManifestations = surveyClinicalManifestations;
 		survey.alergeticDifficultyDegree = difficultyDegree;
 		survey = survey.save();
 
 		renderJSON(survey.surveyId);
 	}
 
 	public static void updateTreatments() {
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 
 		Long surveyId = surveyWrapper.surveyId;
 		List<TreatmentWrapper> treatmentWrappers = surveyWrapper.treatments;
 
 		Survey survey = Survey.findById(surveyId);
 
 		SurveyTreatment surveyTreatment = null;
 		List<SurveyTreatment> surveyTreatments = new ArrayList<SurveyTreatment>();
 		if (treatmentWrappers != null && !treatmentWrappers.isEmpty()) {
 			for (TreatmentWrapper treatmentWrapper : treatmentWrappers) {
 				if (treatmentWrapper.isChecked) {
 					surveyTreatment = new SurveyTreatment();
 					surveyTreatment.survey = survey;
 					surveyTreatment.medicationDetail = MedicationDetail.findById(treatmentWrapper.id);
 					surveyTreatment.treatmentDescription = treatmentWrapper.description;
 					surveyTreatment.dose = treatmentWrapper.dose;
 					surveyTreatment.multiplicity = treatmentWrapper.multiplicity;
 					if (treatmentWrapper.insertionId != null) {
 						surveyTreatment.insertion = Insertion.findById(treatmentWrapper.insertionId);
 					}
 					if (treatmentWrapper.therapy != null) {
 						surveyTreatment.therapyType = TherapyType.valueOf(treatmentWrapper.therapy);
 					}
 					if (treatmentWrapper.allegrenId != null) {
 						surveyTreatment.allergen = Allergen.findById(treatmentWrapper.allegrenId);
 					}
 
 					surveyTreatments.add(surveyTreatment);
 				}
 			}
 		}
 
 		for (SurveyTreatment treatment : survey.surveyTreatments) {
 			treatment.delete();
 		}
 		survey.surveyTreatments.clear();
 
 		survey.surveyTreatments = surveyTreatments;
 		survey._save();
 		renderJSON(survey.surveyId);
 	}
 
 	public static void updateNosology() {
 		String jsonString = request.params.get("body");
 		SurveyWrapper surveyWrapper = new Gson().fromJson(jsonString, SurveyWrapper.class);
 		Long surveyId = surveyWrapper.surveyId;
 		Long nosologyId = surveyWrapper.nosologyId;
 
 		double val = 0;
 		double maxVal = 0;
 		int difficultyDegree = 0;
 		int insufficiencyDegree = 0;
 		Survey survey = Survey.findById(surveyId);
 		List<SurveyAnswer> surveyAnswers = null;
 		ComplaintType complaintType = null;
 		if (nosologyId != null) {
 			// TODO get by syndrome
 			surveyAnswers = survey.surveyAnswers;
 			survey.nosology = Nosology.findById(nosologyId);
 
 			for (SurveyAnswer surveyAnswer : surveyAnswers) {
 				surveyAnswer.multiplier = null;
 				complaintType = surveyAnswer.complaint.complaintType;
 				for (NosologyComplaintType nosologyComplaintType : survey.nosology.nosologyComplaintTypes) {
 					if (nosologyComplaintType.complaintTypeId.equals(complaintType.complaintTypeId)) {
 						surveyAnswer.multiplier = nosologyComplaintType.multyplier;
 						break;
 					}
 				}
 
 				if (surveyAnswer.multiplier != null && surveyAnswer.multiplier != 0) {
 					maxVal += 3 * surveyAnswer.multiplier;
 					val += surveyAnswer.answerValue * surveyAnswer.multiplier;
 				}
 			}
 
 			ClinicalManifestation clinicalManifestation = null;
 			for (SurveyClinicalManifestation surveyClinicalManifestation : survey.surveyClinicalManifestations) {
 				surveyClinicalManifestation.multiplier = 0.0;
 				clinicalManifestation = surveyClinicalManifestation.clinicalManifestationNorm.clinicalManifestation;
 
 				for (NosologyClinicalManifestation nosologyClinicalManifestation : survey.nosology.nosologyClinicalManifestations) {
 					if (nosologyClinicalManifestation.clinicalManifestationId.equals(clinicalManifestation.id)) {
 						surveyClinicalManifestation.multiplier = nosologyClinicalManifestation.multyplier;
 						break;
 					}
 				}
 
 				if (surveyClinicalManifestation.multiplier != null) {
 					maxVal += 3 * surveyClinicalManifestation.multiplier;
 					val += surveyClinicalManifestation.value * surveyClinicalManifestation.multiplier;
 				}
 			}
 
 			difficultyDegree = (int) (val / maxVal * 100);
 			survey.alergeticDifficultyDegree = difficultyDegree;
 
 			val = 0;
 			maxVal = 0;
 			Analysis analysis = null;
 			List<SurveyAnalysis> surveyAnalyses = survey.surveyAnalyses;
 			for (SurveyAnalysis surveyAnalysis : surveyAnalyses) {
 				if (surveyAnalysis.isChecked) {
 					surveyAnalysis.multiplier = null;
 					analysis = surveyAnalysis.analysisNorm.analysis;
 					for (NosologyAnalysis nosologyAnalysis : survey.nosology.nosologyAnalysis) {
 						if (nosologyAnalysis.analysisId.equals(analysis.analysisId)) {
 							surveyAnalysis.multiplier = nosologyAnalysis.multyplier;
 							break;
 						}
 					}
 
 					if (surveyAnalysis.multiplier != null && surveyAnalysis.multiplier != 0) {
 						maxVal += 3 * surveyAnalysis.multiplier;
 						val += surveyAnalysis.value * surveyAnalysis.multiplier;
 					}
 				}
 			}
 
 			insufficiencyDegree = (int) (val / maxVal * 100);
 			survey.alergeticInsufficiencyDegree = insufficiencyDegree;
 			System.out.println("degree: " + insufficiencyDegree);
 		}
 
 		survey.save();
 		renderJSON(survey.surveyId);
 	}
 }
