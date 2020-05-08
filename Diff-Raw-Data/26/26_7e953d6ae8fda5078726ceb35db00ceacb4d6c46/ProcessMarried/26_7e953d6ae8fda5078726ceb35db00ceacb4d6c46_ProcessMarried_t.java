 package lib;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import ctrl.Controller;
 
 import obj.Survey;
 
 /**
  * The Class ProcessMarried processes the list of surveys for married, divorced,
  * and single surveys as well as sets spouses for married surveys.
  */
 public class ProcessMarried {
 
 	/** The list of surveys. */
 	private List<Survey> lstSurveys = Controller.getInstance().getSurveys();
 
 	/** The list of males. */
 	private List<Survey> lstMales = new ArrayList<Survey>();
 
 	/** The list of females. */
 	private List<Survey> lstFemales = new ArrayList<Survey>();
 
 	/** The list of married males. */
 	private List<Survey> lstMarriedMales = new ArrayList<Survey>();
 
 	/** The list of married females. */
 	private List<Survey> lstMarriedFemales = new ArrayList<Survey>();
 
 	/** The list of divorced males. */
 	private List<Survey> lstDivorcedMales = new ArrayList<Survey>();
 
 	/** The list of divorced females. */
 	private List<Survey> lstDivorcedFemales = new ArrayList<Survey>();
 
 	/** The married requirement. */
 	private double marriedReq = .4 * 100f; // 40%
 
 	/** The divorced requirement. */
 	private double divorcedReq = .35 * 100f; // 35%
 
 	/** The actual number of married surveys. */
 	private float actualMarried;
 
 	/** The actual number of divorced surveys. */
 	private float actualDivorced;
 
 	/**
 	 * Do processing.
 	 * 
 	 * @return the list of surveys after being processed
 	 */
 	public List<Survey> doProcess() {
 
 		for (Survey survey : lstSurveys) {
 
 			// Populate gender lists
 			if (survey.getGender() == 0) // Male
 				lstMales.add(survey);
 			if (survey.getGender() == 1) // Female
 				lstFemales.add(survey);
 
 			// Populate single list (based on single/no kids)
 
 			// Populate married lists (based on income)
 			if (survey.getMarried() == 1) {
 				if (survey.getGender() == 0) // Married Male
 					lstMarriedMales.add(survey);
 				if (survey.getGender() == 1) // Married Female
 					lstMarriedFemales.add(survey);
 			}
 
 			// Populate divorced lists
 			if (survey.getMarried() == 2) {
 				if (survey.getGender() == 0) // Divorced Male
 					lstDivorcedMales.add(survey);
 				if (survey.getGender() == 1) // Divorced Female
 					lstDivorcedFemales.add(survey);
 			}
 		}
 
 		// Make sure we have 40% of the group married
 		actualMarried = (lstMarriedMales.size() + lstMarriedFemales.size())
 				* (100f / lstSurveys.size());
 		// Make sure we have 35% of the group divorced
 		actualDivorced = (lstDivorcedMales.size() + lstDivorcedFemales.size())
 				* (100f / lstSurveys.size());
 
 		// Remove this block to randomize married regardless of preferred
 		if (actualMarried < marriedReq) {
 			setMarriedMales();
 			setMarriedFemales();
 		}
 
 		// Set spouses
		setSpouses();
 
 		// Make sure we have 35% of the group divorced
 		if (actualDivorced < divorcedReq) {
 			setDivorcedMales();
 			setDivorcedFemales();
 		}
 
 		return lstSurveys;
 	}
 
 	/**
 	 * Check spouse.
 	 * 
 	 * @param survey
 	 *            : the survey being checked
 	 * @return true, if has spouse
 	 */
 	private boolean checkSpouse(Survey survey) {
 		if (survey.getSpouse() > 0)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Loop through males and make some married.
 	 */
 	private void setMarriedMales() {
 		Random rndMarMale = new Random();
 		float actualMarriedMales = lstMarriedMales.size()
 				* (100f / lstSurveys.size());
 
 		while (actualMarriedMales < (marriedReq / 2)) {
 			Survey survey = lstMales.get(rndMarMale.nextInt(lstMales.size()));
 			if (survey.getMarried() == 0) {
 				survey.setMarried(1);
 				lstMarriedMales.add(survey);
 				actualMarriedMales = lstMarriedMales.size()
 						* (100f / lstSurveys.size());
 			}
 		}
 	}
 
 	/**
 	 * Loop through females and make some married.
 	 */
 	private void setMarriedFemales() {
 		Random rndMarFemale = new Random();
 		float actualMarriedFemales = lstMarriedFemales.size()
 				* (100f / lstSurveys.size());
 
 		while (actualMarriedFemales < (marriedReq / 2)) {
 			Survey survey = lstFemales.get(rndMarFemale.nextInt(lstFemales
 					.size()));
 			if (survey.getMarried() == 0) {
 				survey.setMarried(1);
 				lstMarriedFemales.add(survey);
 				actualMarriedFemales = lstMarriedFemales.size()
 						* (100f / lstSurveys.size());
 			}
 		}
 	}
 
 	/**
 	 * Loop through males and make some divorced.
 	 */
 	private void setDivorcedMales() {
 		Random rndDivMale = new Random();
 		float actualDivorcedMales = lstDivorcedMales.size()
 				* (100f / lstSurveys.size());
 
 		while (actualDivorcedMales < (divorcedReq / 2)) {
 			Survey survey = lstMales.get(rndDivMale.nextInt(lstMales.size()));
 			if (survey.getMarried() == 0) {
 				survey.setMarried(2);
 				lstDivorcedMales.add(survey);
 				actualDivorcedMales = lstDivorcedMales.size()
 						* (100f / lstSurveys.size());
 			}
 		}
 	}
 
 	/**
 	 * Loop through females and make some divorced.
 	 */
 	private void setDivorcedFemales() {
 		Random rndDivFemale = new Random();
 		float actualDivorcedFemales = lstDivorcedFemales.size()
 				* (100f / lstSurveys.size());
 
 		while (actualDivorcedFemales < (divorcedReq / 2)) {
 			Survey survey = lstFemales.get(rndDivFemale.nextInt(lstFemales
 					.size()));
 			if (survey.getMarried() == 0) {
 				survey.setMarried(2);
 				lstDivorcedFemales.add(survey);
 				actualDivorcedFemales = lstDivorcedFemales.size()
 						* (100f / lstSurveys.size());
 			}
 		}
 	}
 
 	/**
 	 * Sets the spouses for the group.
 	 */
 	private void setSpouses() {
 		Random rndSpouse = new Random();
 		// TODO: Try to match similar income
 
 		List<Survey> surveys;
 		List<Survey> spouseSurveys;
 
 		if (lstMarriedFemales.size() > lstMarriedMales.size()) {
 			surveys = lstMarriedFemales;
 			spouseSurveys = lstMarriedMales;
 		} else {
 			surveys = lstMarriedMales;
 			spouseSurveys = lstMarriedFemales;
 		}
 
 		for (int i = 0; i < surveys.size(); i++) {
 
 			if (lstMarriedMales.size() > 0 && lstMarriedFemales.size() > 0) {
 
 				Survey survey = surveys.get(i);
 				if (!checkSpouse(survey) && survey.getMarried() != 2) {
 
 					try {
 						// Set a spouse
 						Survey spouse = spouseSurveys.get(rndSpouse
 								.nextInt(spouseSurveys.size()));
 
						// Find an eligible spouse
						while (checkSpouse(spouse) && spouseSurveys.size() > 1) {

							// If spouse is already married, remove from
							// eligible spouses
							if (checkSpouse(spouse))
								spouseSurveys.remove(spouse);

 							spouse = spouseSurveys.get(rndSpouse
 									.nextInt(spouseSurveys.size()));

 						}
 
 						if (spouse != null) {
 
 							// Remove the old surveys
 							lstSurveys.remove(survey);
 							lstSurveys.remove(spouse);
 
 							// Make the 2 spouses to each other
 							survey.setSpouse(spouse.getID());
 							spouse.setSpouse(survey.getID());
 
 							// Add the new surveys

 							lstSurveys.add(survey);
 							lstSurveys.add(spouse);
 						}
 					} catch (IndexOutOfBoundsException iobe) {
 						break;
 					}
 				} // CheckSpouse
 			}
 		} // For Loop
 	}
 }
