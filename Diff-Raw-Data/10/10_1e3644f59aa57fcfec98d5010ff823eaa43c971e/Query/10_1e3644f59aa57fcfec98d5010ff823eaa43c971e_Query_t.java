 package gov.nih.nci.caintegrator.dto.query;
 
 import gov.nih.nci.caintegrator.dto.critieria.Criteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
import gov.nih.nci.caintegrator.dto.view.Viewable;
 import gov.nih.nci.caintegrator.exceptions.ValidationException;
 import gov.nih.nci.caintegrator.query.Validatable;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.QueryHandler;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 import org.apache.log4j.Logger;
 
 abstract public class Query implements Queriable, Serializable, Cloneable, Validatable{
 	/**
 	 * IMPORTANT! This class has a clone method! This requires that any new data
 	 * field that is added to this class also be cloneable and be added to clone
 	 * calls in the clone method.If you do not do this, you will not seperate 
 	 * the references of at least one data field when we generate a copy of this
 	 * object.This means that if the data field ever changes in one copy or the 
 	 * other it will affect both instances... this will be hell to track down if
 	 * you aren't ultra familiar with the code base, so add those methods now!
 	 * 
 	 * (Not necesary for primitives.)
 	 */
 	
     private Logger logger = Logger.getLogger(Query.class);
     
     //This attribute required for caching mechanism
     private String sessionId = null;
 
 	private String queryName;
 	//Added so that we can still distinguish where we have added a result set
 	//and that can know what result set we applied
 	private String appliedResultSet;
 
 	private boolean isComplete = false;
 	private Viewable associatedView;
     
     protected DiseaseOrGradeCriteria diseaseOrGradeCriteria;
 
     protected SampleCriteria sampleIDCrit;
 
 	public abstract QueryHandler getQueryHandler() throws Exception;
     
     public abstract QueryType getQueryType() throws Exception;
 
     public abstract String toString();
     
     public DiseaseOrGradeCriteria getDiseaseOrGradeCriteria() {
 		return diseaseOrGradeCriteria;
 	}
 
 	public void setDiseaseOrGradeCrit(DiseaseOrGradeCriteria diseaseOrGradeCriteria) {
 		this.diseaseOrGradeCriteria = diseaseOrGradeCriteria;
 	}
 
 	public SampleCriteria getSampleIDCrit() {
 		return sampleIDCrit;
 	}
 
 	public void setSampleIDCrit(SampleCriteria sampleIDCrit) {
 		this.sampleIDCrit = sampleIDCrit;
 	}
 	
 	//TODO: The following method checks if a given Query is empty
 	public boolean isEmpty() {
 		try {
 
 			String currObjectName = this.getClass().getName();
 			Class currClass = Class.forName(currObjectName);
 			Method[] allPublicmethods = currClass.getMethods();
 
 			for (int i = 0; i < allPublicmethods.length; i++) {
 				Method currMethod = allPublicmethods[i];
 				String currMethodString = currMethod.getName();
 				if ((currMethodString.toUpperCase().startsWith("GET"))
 						&& (currMethod.getModifiers() == Modifier.PUBLIC)) {
 					Object thisObj = currMethod.invoke(this, null);
 
 					if (thisObj != null) {
 						if (Criteria.class.isInstance(thisObj)) {
 							Criteria thisCriteria = (Criteria) thisObj;
 							if (!thisCriteria.isEmpty()) {
 								return false;
 							}
 						}
 					}
 				}
 			}
 
 		} catch (Throwable e) {
 			logger.error(e);
        	}
 		return true;
 	}
 
 
 	/*
 	 * public Collection getCriteriaCol() { return criteriaCol; }
 	 * 
 	 * public void setCriteriaCol(Collection criteriaCol) { this.criteriaCol =
 	 * criteriaCol; }
 	 */
 
 	public String getQueryName() {
 		return queryName;
 	}
 
 	public void setQueryName(String queryName) {
 		this.queryName = queryName;
 	}
 
 	public Viewable getAssociatedView() {
 		return associatedView;
 	}
 
 	public void setAssociatedView(Viewable associatedViewObj) {
 		this.associatedView = associatedViewObj;
 	}
 
 	public Query[] getAssociatiedQueries() {
 		Query[] queries = { this };
 		return queries;
 	}
     
     public String getSessionId() {
         return sessionId;
     }
 
     public void setSessionId(String sessionId) {
         this.sessionId = sessionId;
     }
     public Object clone() {
     	Query myClone = null;
     	try {
     		myClone = (Query)super.clone();
             if(associatedView != null){
                 myClone.associatedView = (Viewable)associatedView.clone();
             }
             if(diseaseOrGradeCriteria != null){
                 myClone.diseaseOrGradeCriteria = (DiseaseOrGradeCriteria)diseaseOrGradeCriteria.clone();
             }
             if(sampleIDCrit != null){
                 myClone.sampleIDCrit = (SampleCriteria)sampleIDCrit.clone();
             }
     	}catch(CloneNotSupportedException cnse) {
         		/*
         		 * This is meaningless as it will still perform
         		 * the memcopy, and then let you know that
         		 * the object did not implement the Cloneable inteface.
         		 * Kind of a stupid implementation if you ask me...
         		 */
         	}
        	return myClone;
     }
 	/**
 	 * @return Returns the appliedResultSet.
 	 */
 	public String getAppliedResultSet() {
 		return appliedResultSet;
 	}
 	/**
 	 * @param appliedResultSet The appliedResultSet to set.
 	 */
 	public void setAppliedResultSet(String appliedResultSet) {
 		this.appliedResultSet = appliedResultSet;
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.caintegrator.query.Validatable#validate()
 	 */
 	public boolean validate() throws ValidationException {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	/** for analysis type large queries that run asynchronously
 	 * 
 	 * 
 	 */
 	public boolean isTaskComplete() {
 		return isComplete;
 	}
 	public void setIsTaskComplete(boolean status) {
 		isComplete = status;
 		
 	}
 }
