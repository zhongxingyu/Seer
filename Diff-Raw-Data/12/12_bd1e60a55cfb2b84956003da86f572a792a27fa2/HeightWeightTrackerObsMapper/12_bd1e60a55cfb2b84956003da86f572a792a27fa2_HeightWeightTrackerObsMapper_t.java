 package org.openmrs.module.heightweighttracker.impl.pih;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.heightweighttracker.mapper.BMIForAge;
 import org.openmrs.module.heightweighttracker.mapper.HeightForAge;
 import org.openmrs.module.heightweighttracker.mapper.Visit;
 import org.openmrs.module.heightweighttracker.mapper.VisitGroup;
 import org.openmrs.module.heightweighttracker.mapper.WeightForAge;
 import org.openmrs.module.heightweighttracker.mapper.WeightForHeight;
 
 public class HeightWeightTrackerObsMapper {
 
 	/** Logger instance */
 	private final Log log = LogFactory.getLog(this.getClass());
 
 	/** Patient. */
 	private Patient patient;
 
 
 	public HeightWeightTrackerObsMapper(Patient patient) {
 		this.patient = patient;
 	}
 
 	public Collection<Obs> getHeightObsList() {
 		return getObsList(ConceptDictionary.HEIGHT_CM);
 	}
 	
 	public Collection<Obs> getWeightObsList() {
 		return getObsList(ConceptDictionary.WEIGHT_KG);
 	}
 	
 	public Obs getLatestHeight() {
 		Collection<Obs> obsList = getHeightObsList();
 		if(obsList == null || obsList.size() == 0)
 			return new Obs();
 
 		Obs latestHeight = null;
 		for(Obs obs : obsList) {
 			if(latestHeight == null || latestHeight.getObsDatetime().before(obs.getObsDatetime()))
 			{
 				latestHeight = obs;
 			}
 		}
 		return latestHeight;
 	}
 	
 	public List<HeightForAge> getHeightForAge()
 	{
 		List<HeightForAge> heightForAge = new ArrayList<HeightForAge>();
 		Date birthDate = patient.getBirthdate();
 		
 		Collection<Obs> heightObs = getHeightObsList();
 		if(heightObs != null)
 		{
 			for(Obs o: heightObs)
 			{
 				int months = calculateMonthsDifference(o.getObsDatetime(), birthDate);
 				if(months > 59 && months < 230)
 				{
 					HeightForAge hfa = new HeightForAge();
 					hfa.setAgeInMonths(months);
 					hfa.setHeightInCM(o.getValueNumeric().intValue());
 					heightForAge.add(hfa);
 				}
 			}
 		}
 		
 		return heightForAge;
 	}
 	
 	public List<WeightForAge> getWeightForAgeFromBirth()
 	{
 		List<WeightForAge> weightForAge = new ArrayList<WeightForAge>();
 		Date birthDate = patient.getBirthdate();
 		
 		Collection<Obs> weightObs = getWeightObsList();
 		if(weightObs != null)
 		{
 			for(Obs o: weightObs)
 			{
 				int months = calculateMonthsDifference(o.getObsDatetime(), birthDate);
 				if(months < 61)
 				{
 					WeightForAge wfa = new WeightForAge();
 					wfa.setAgeInMonths(months);
 					wfa.setWeightInKG(o.getValueNumeric().intValue());
 					weightForAge.add(wfa);
 				}
 			}
 		}
 		
 		return weightForAge;
 	}
 	
 	
 	public List<WeightForAge> getWeightForAge()
 	{
 		List<WeightForAge> weightForAge = new ArrayList<WeightForAge>();
 		Date birthDate = patient.getBirthdate();
 		
 		Collection<Obs> weightObs = getWeightObsList();
 		if(weightObs != null)
 		{
 			for(Obs o: weightObs)
 			{
 				int months = calculateMonthsDifference(o.getObsDatetime(), birthDate);
 				if(months > 59 && months < 120)
 				{
 					WeightForAge wfa = new WeightForAge();
 					wfa.setAgeInMonths(months);
 					wfa.setWeightInKG(o.getValueNumeric().intValue());
 					weightForAge.add(wfa);
 				}
 			}
 		}
 		
 		return weightForAge;
 	}
 	
 	
 	public List<HeightForAge> getLengthForAge()
 	{
 		List<HeightForAge> heightForAge = new ArrayList<HeightForAge>();
 		Date birthDate = patient.getBirthdate();
 		
 		Collection<Obs> heightObs = getHeightObsList();
 		if(heightObs != null)
 		{
 			for(Obs o: heightObs)
 			{
 				int months = calculateMonthsDifference(o.getObsDatetime(), birthDate);
 				if(months < 61)
 				{
 					HeightForAge hfa = new HeightForAge();
 					hfa.setAgeInMonths(months);
 					hfa.setHeightInCM(o.getValueNumeric().intValue());
 					heightForAge.add(hfa);
 				}
 			}
 		}
 		
 		return heightForAge;
 	}
 	
 	public List<BMIForAge> getBmiForAge()
 	{
 		List<BMIForAge> bmiForAge = new ArrayList<BMIForAge>();
 		Date birthDate = patient.getBirthdate();
 		
 		for(VisitGroup vg: getVisitsPedi())
 		{
 			if(vg.getDate() != null)
 			{
 				int months = calculateMonthsDifference(vg.getDate(), birthDate);
 				if(months > 60 && months < 229)
 				{
 					Obs height = vg.getHeight();
 					Obs weight = vg.getWeight();
 					
 					if(height != null && height.getValueNumeric() != null && weight != null && weight.getValueNumeric() != null)
 					{
 						Double bmi = weight.getValueNumeric() / (height.getValueNumeric()/100 * height.getValueNumeric()/100);
 						BMIForAge bmiVal = new BMIForAge();
 						
 						bmiVal.setAgeInMonths(months);
 						bmiVal.setBmi(bmi);
 						bmiForAge.add(bmiVal);
 					}
 				}
 			}
 		}
 		
 		return bmiForAge;
 	}
 	
 	public List<WeightForHeight> getWeightForLength()
 	{
 		List<WeightForHeight> weightForLength = new ArrayList<WeightForHeight>();
 		Date birthDate = patient.getBirthdate();
 		
 		for(VisitGroup vg: getVisitsPedi())
 		{
 			if(vg.getDate() != null)
 			{
 				int months = calculateMonthsDifference(vg.getDate(), birthDate);
 				if(months < 25)
 				{
 					Obs height = vg.getHeight();
 					Obs weight = vg.getWeight();
 					
 					if(height != null && height.getValueNumeric() != null && weight != null && weight.getValueNumeric() != null)
 					{
 						WeightForHeight wfl = new WeightForHeight();
 						wfl.setHeight(height.getValueNumeric());
 						wfl.setWeight(weight.getValueNumeric());
 						weightForLength.add(wfl);
 					}
 				}
 			}
 		}
 		
 		return weightForLength;
 	}
 	
 	public List<WeightForHeight> getWeightForHeight()
 	{
 		List<WeightForHeight> weightForHeight = new ArrayList<WeightForHeight>();
 		Date birthDate = patient.getBirthdate();
 		
 		for(VisitGroup vg: getVisitsPedi())
 		{
 			if(vg.getDate() != null)
 			{
 				int months = calculateMonthsDifference(vg.getDate(), birthDate);
 				if(months > 24 && months < 60)
 				{
 					Obs height = vg.getHeight();
 					Obs weight = vg.getWeight();
 					
 					if(height != null && height.getValueNumeric() != null && weight != null && weight.getValueNumeric() != null)
 					{
 						WeightForHeight wfh = new WeightForHeight();
 						wfh.setHeight(height.getValueNumeric());
 						wfh.setWeight(weight.getValueNumeric());
 						weightForHeight.add(wfh);
 					}
 				}
 			}
 		}
 		
 		return weightForHeight;
 	}
 	
 	
 	private int calculateMonthsDifference(Date observation, Date startingDate)
 	{
 		int diff = 0;
 	
 		Calendar obsDate = Calendar.getInstance();	
 		obsDate.setTime(observation);
 	
 		Calendar startDate = Calendar.getInstance();
 		startDate.setTime(startingDate);
 	
 		//find out if there is any difference in years first
 		diff = obsDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
 		diff = diff * 12;
 	
 		int monthDiff = obsDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);
 		diff = diff + monthDiff;
 	
 		return diff;
 	}
 	
 	private int calculateDaysDifference(Date observation, Date startingDate)
 	{
 		long milis1 = observation.getTime();
 		long milis2 = startingDate.getTime();
 		
 		long diff = milis1 - milis2;
 		
 		long diffDays = diff / (24 * 60 * 60 * 1000);
 	
 		return (int)diffDays;
 	}
     
     public Collection<VisitGroup> getVisitsPedi() {
    	Integer[] conceptIds = { ConceptDictionary.WEIGHT_KG, ConceptDictionary.HEIGHT_WEIGHT_PERCENTILE,  ConceptDictionary.HEIGHT_CM, ConceptDictionary.Z_SCORE_HEIGHT, ConceptDictionary.Z_SCORE_WEIGHT, ConceptDictionary.Z_SCORE_BMI, ConceptDictionary.BMI };
 
 		Collection<VisitMapping> set = getObsView(conceptIds, VisitMapping.class);
 
     	List<List<VisitMapping>> sortedObsList = sortAndGroupObsByDate(set);
 
     	sortedObsList.add(new ArrayList<VisitMapping>());
  
     	List<VisitGroup> visits = new ArrayList<VisitGroup>(sortedObsList.size());
     	for(List<VisitMapping> row : sortedObsList) {
     		visits.add(new VisitGroup(new ArrayList<Visit>(row)));
     	}
 
     	return visits;    	
     }
     
     public Collection<VisitGroup> getVisitsPediShortList() {
     	Collection<VisitGroup> allVisits = getVisitsPedi();
     	Collection<VisitGroup> lastVisits = new ArrayList<VisitGroup>();
     	
     	Calendar oneYearAgo = Calendar.getInstance();
     	oneYearAgo.add(Calendar.YEAR, -1);
     	
     	int i = 0;
     	
     	for(VisitGroup visit: allVisits)
     	{
    		if(visit.getDate() != null && (visit.getDate().after(oneYearAgo.getTime()) || i < 10))
     		{
     			lastVisits.add(visit);
     		}
     		i++;
     	}
     	
     	return lastVisits;
     }
     
 	
 	/**
 	 * Wraps the underlying representation of obs values with higher level "view".  NOTE: this sorts 
 	 * the data.
 	 * 
 	 * @param <T> Class that knows how to read the various ways the obs are stored.
 	 * @param conceptIds List of concept id's that contain the data.
 	 * @param clazz Same class as T.  This is needed to construct the instances as java doesn't support T value = new T();
 	 * @return Set of view objects
 	 */
 	protected <T extends ObsMapping & Comparable<? super T>> Collection<T> getObsView(Integer[] conceptIds, Class<T> clazz) {
 		List<Obs> labObs = new LinkedList<Obs>();
 		
 		
 		//process sets first so that set members don't generate new rows if conceptId list contains both sets and regular concepts
 		for(Integer labConceptId : conceptIds) {
 			Concept c = Context.getConceptService().getConcept(labConceptId);
 			if (c != null && c.isSet()){
 				List<Obs> oList = getObsList(labConceptId);
 				labObs.addAll(oList);
 			}
 		}
 		for(Integer labConceptId : conceptIds) {
 			Concept c = Context.getConceptService().getConcept(labConceptId);
 			if (c != null && !c.isSet()){
 				List<Obs> oList = getObsList(labConceptId);
 				labObs.addAll(oList);
 			}
 		}
 
 		// Keep track of the obs so that an obs is not added twice
 		Set<Integer> usedObs = new HashSet<Integer>();
 
     	List<T> list = new ArrayList<T>();
     	for(Obs obs : labObs) {
     		if(!usedObs.contains(obs.getId())) {
     			addAllGroupIds(usedObs, obs);
     			
     			if(obs.getObsDatetime() != null) {
     				T row;
 					try {
 						row = (T)clazz.getConstructor(Obs.class).newInstance(obs);
 					} catch (Exception e) {
 						log.error("Failed to create an instance of type "+clazz, e);
 						throw new RuntimeException("Failed to create an instance of type "+clazz, e);
 					}
         			if(!row.isBlank() && !list.contains(row))
         				list.add(row);
     			}
     		}
     	}
     	
     	Collections.sort(list);
     	//List<T> list = asSortedList(set);
 
 		return list;
 	}
 
 	private List<Obs> getObsList(Integer conceptId) {
 
 			Concept concept = Context.getConceptService().getConcept(conceptId);
 			List<Obs> obsList = Context.getObsService().getObservations(Collections.singletonList((Person) this.patient), null, Collections.singletonList(concept), null, null, null, null, null, null, null, null, false);
 			
 			if(obsList == null)
 				return null;
 			
 			return obsList;
 	}
 
 	private static void addAllGroupIds(Set<Integer> obsSet, Obs obs) {
 		if(obs == null)
 			return;
 		
 		obsSet.add(obs.getId());
 		// Add all the obs this contains
 		if(obs.getGroupMembers() != null && obs.getGroupMembers().size() > 0) {
 			for(Obs groupObs : obs.getGroupMembers()) {
 				addAllGroupIds(obsSet, groupObs);
 			}
 		}
 	}
 
 	private <T extends ObsMapping & Comparable<? super T>> List<List<T>> sortAndGroupObsByDate(Collection<T> labObs) {
 		// Group the drugs by start date and end date.  If they are the same
     	// it's one row.
     	Map<String, List<T>> dateToObsMap = new HashMap<String, List<T>>();    	
     	for(T obs : labObs) {
     		if(!obs.isVoided()) {
 	    		String key = formatDate("yyyy-MM-dd", obs.getObsDate());
 	    		List<T> obsMapOnDate = dateToObsMap.get(key);
 	    		if(obsMapOnDate == null) {
 	    			obsMapOnDate = new ArrayList<T>(); 
 	    			dateToObsMap.put(key, obsMapOnDate);
 	    		}
     		
     			obsMapOnDate.add(obs);
     		}
     	}
 
     	// Make sure to sort the list
     	String[] keys = new String[dateToObsMap.size()];
     	dateToObsMap.keySet().toArray(keys);
     	Arrays.sort(keys);
     	
     	List<List<T>> drugOrdersSorted = new ArrayList<List<T>>(dateToObsMap.size());
     	for(String key : keys) {
     		drugOrdersSorted.add(0,dateToObsMap.get(key));
     	}
 		return drugOrdersSorted;
 	}
 	
 	public int getAge()
 	{
 		return patient.getAge();
 	}
 
 	/**
 	 * Format the given date according to the type ('short', 'long', 'ymd')
 	 * 
 	 * @param type format to use
 	 * @param d Date to format
 	 * @return a String with the formatted date
 	 */
 	private String formatDate(String type, Date d) {
 		if (d == null)
 			return "";
 
 		DateFormat df = new SimpleDateFormat(type);
 		return df.format(d);
 	}
 }
