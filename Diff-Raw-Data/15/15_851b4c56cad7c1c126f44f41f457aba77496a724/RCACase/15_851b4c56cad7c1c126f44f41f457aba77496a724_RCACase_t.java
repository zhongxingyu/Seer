 /*
  * Copyright (C) 2012 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
  * Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus,
  * Timo Lehtinen, Jaakko Harjuhahto
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package models;
 
 import models.enums.CompanySize;
 import models.ClassificationDimension;
 import models.enums.RCACaseType;
 import models.ClassificationTable;
 import models.events.CauseStream;
 import models.events.Event;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 import play.cache.Cache;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.libs.F.IndexedEvent;
 import play.libs.F.Promise;
 import utils.IdComparableModel;
 
 import javax.persistence.*;
 import java.util.*;
 
 import static models.ClassificationTable.*;
 
 /**
  * This class represents an RCA case in the application.
  *
  * @author Eero Laukkanen
  */
 @PersistenceUnit(name = "maindb")
 @Entity(name = "rcacase")
 public class RCACase extends IdComparableModel {
 
     private static final String CAUSE_STREAM_NAME_IN_CACHE = "causeStream";
     private static final int NUMBER_OF_EVENTS_STORED_IN_EVENT_STREAM = 100;
     private static final String EXPIRATION_TIME_FOR_CAUSE_STREAM_IN_CACHE = "30mn";
 
     /**
     * the name of the rca case
     */
     @Required
     @MaxSize(value = 255)
     @Column(name = "name")
     public String caseName;
 
     /**
     * the tpy of the rca case
     */
     public Integer caseTypeValue;
 
     /*
     * Goals of the rca case
     */
     @Lob
     public String caseGoals;
 
     /**
     * The size of the company
     */
     public Integer companySizeValue;
 
     /**
     * The description of the rca case
     */
     public String description;
 
     /**
     * Is the compnay multinational
     */
     public boolean isMultinational;
 
     /**
     * The name of the company
     */
     @MaxSize(value = 255)
     public String companyName;
 
     /**
     * Description of the products of the company
     */
     @Lob
     public String companyProducts;
 
     /**
     * Is the rca case public for everyone
     */
     public boolean isCasePublic;
 
     /**
     * Id of the owner user
     */
     public Long ownerId;
 
     /**
     * Id of the root problem "cause"
     */
     @OneToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "problemId")
     public Cause problem;
 
     /**
     * All causes that belong to this rca case
     */
     @OneToMany(mappedBy = "rcaCase", cascade = CascadeType.ALL)
     @Sort(type = SortType.NATURAL)
     public SortedSet<Cause> causes;
 
     /**
     * The creation date of the rca case
     */
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "created", nullable = false)
     public Date created;
 
     /**
     * The update date of the rca case
     */
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "updated", nullable = false)
     public Date updated;
 
     /**
     * This method is called when the rca case is created
     */
     @PrePersist
     protected void onCreate() {
         Date current = new Date();
         updated = current;
         created = current;
     }
 
     /**
      * Basic constructor
      *
      * @param owner User who created this case
      */
     public RCACase(User owner) {
         this.ownerId = owner.id;
         this.causes = new TreeSet<Cause>();
     }
 
     /**
      * Constructor for the form in create.html.
      *
      * @param caseName         The name of the RCA case
      * @param caseTypeValue    The type of the RCA case. Enums are found in models/enums/RCACaseType.
      * @param caseGoals        Goals of the RCA case
      * @param description      The description of the RCA case
      * @param isMultinational  The boolean value whether the company related to the RCA case is multinational.
      * @param companyName      The name of the company related to the RCA case.
      * @param companySizeValue The size of the company related to the RCA case. Enums are found in
      *                         models/enums/CompanySize.
      * @param companyProducts  Products of the company
      * @param isCasePublic     The boolean value whether the RCA is public.
      * @param owner            The User who owns the case.
      *                         <p/>
      *                         ownerId The ID of the user who creates the case.
      *                         problem The Cause object that represents the problem of the RCA case.
      */
     public RCACase(String caseName, int caseTypeValue, String caseGoals, String description, boolean isMultinational,
                    String companyName, int companySizeValue, String companyProducts, boolean isCasePublic, User owner) {
         this.caseName = caseName;
         this.caseTypeValue = caseTypeValue;
         this.caseGoals = caseGoals;
         this.description = description;
         this.isMultinational = isMultinational;
         this.companyName = companyName;
         this.companySizeValue = companySizeValue;
         this.companyProducts = companyProducts;
         this.isCasePublic = isCasePublic;
         this.ownerId = owner.id;
         this.causes = new TreeSet<Cause>();
         System.out.println("RCACase constructor called");
     }
 
     /**
      * Returns the owner of the case.
      *
      * @return the owner of the case.
      */
     public User getOwner() {
         return User.findById(ownerId);
     }
 
     /**
      * Calls for a "Promise"-job that returns list of events that have been sent after the parameter.
      *
      * @param lastReceived the id of the last message that has been received.
      *
      * @return asynchronous Promise job that can be run with await() that returns the list of events
      */
     public Promise<List<IndexedEvent<Event>>> nextMessages(long lastReceived) {
         CauseStream stream = this.getCauseStream();
         return stream.getStream().nextEvents(lastReceived);
     }
 
     /**
      * Returns the size of the company of the RCA case.
      *
      * @return company size enumeration
      */
     public CompanySize getCompanySize() {
         return CompanySize.valueOf(companySizeValue);
     }
 
     /**
      * Sets the companySize value.
      *
      * @param companySize the size to be setted
      */
     public void setCompanySize(CompanySize companySize) {
         this.companySizeValue = companySize.value;
     }
 
     /**
      * Returns the type of the RCA case.
      *
      * @return type of the RCA case
      */
     public RCACaseType getRCACaseType() {
         return RCACaseType.valueOf(caseTypeValue);
     }
 
     /**
      * Sets the type of the RCA case.
      *
      * @param rcaCaseType the type to be set
      */
     public void setRCACaseType(RCACaseType rcaCaseType) {
         this.caseTypeValue = rcaCaseType.value;
     }
 
     /**
      * Returns the stream that handles the messages of one RCA case. Streams are stored in the Cache class. If stream
      * has not been created yet, it will be created in this method. Calling this method also updates the stream in
      * the Cache, increasing the time when the stream is saved.
      *
      * @return the stream that has the messages
      */
     public CauseStream getCauseStream() {
         CauseStream stream = Cache.get(CAUSE_STREAM_NAME_IN_CACHE + this.id, CauseStream.class);
         if (stream == null) {
             stream = new CauseStream(NUMBER_OF_EVENTS_STORED_IN_EVENT_STREAM);
         }
         Cache.set(CAUSE_STREAM_NAME_IN_CACHE + this.id, stream, EXPIRATION_TIME_FOR_CAUSE_STREAM_IN_CACHE);
         return stream;
     }
 
     /**
      * Deletes a cause in an RCA case. The main problem cannot be deleted this way.
      *
      * @param cause cause to be deleted.
      */
     public void deleteCause(Cause cause) {
         if (cause.equals(this.problem)) {
             return;
         }
         this.causes.remove(cause);
         cause.deleteCause();
         cause.delete();
         this.save();
     }
 
     @Override
     public String toString() {
         return caseName + " (id: " + id + ")";
     }
 
     /**
      * Sets the problem of this RCA case.
      *
      * @param cause the problem
      */
     public void setProblem(Cause cause) {
         if (cause.rcaCase.equals(this)) {
             this.problem = cause;
             if (this.causes == null) {
                 this.causes = new TreeSet<Cause>();
             }
             this.causes.add(cause);
             this.save();
         }
     }
 
 
     /**
      * Returns all classifications of the case
      * @return list of Classification objects
      */
     public List<Classification> getClassifications() {
         return Classification.find(
             "SELECT c FROM classification AS c WHERE rcaCaseId=?",
             this.id
         ).fetch();
     }
 
 
     /**
      * Returns classifications of the case that are of the given dimension
      * @param classificationDimension the dimension of the classifications
      * @return list of Classification objects
      */
     public List<Classification> getClassifications(int classificationDimension) {
         return Classification.find(
             "SELECT c FROM classification AS c WHERE rcaCaseId=? AND classificationDimension=?",
             this.id, classificationDimension
         ).fetch();
     }
 
 	/**
 	 * Returns and calculates ClassificationTable. Initially it creates ClassificationTable and initializes
 	 * row and column names. Then Causes and each ClassificationPair for them are looped through and table
 	 * values are updated accordingly. Finally percentages are calculated and table is returned.
 	 * @return the classification table
 	 * @see ClassificationTable
 	 */
 	public ClassificationTable getClassificationTable() {
		List<Classification> parentDimension = this.getClassifications(ClassificationDimension.SECOND_DIMENSION_ID);
		List<Classification> childDimension = this.getClassifications(ClassificationDimension.FIRST_DIMENSION_ID);

		if (parentDimension.size() == 0 || childDimension.size() == 0) {
			return null;
		}
 
 		ClassificationTable table = new ClassificationTable(parentDimension.size(), childDimension.size());
 
 		// Set row and column names as classification names
 		for (Classification c : parentDimension) {
 			table.rowNames.add(c.name);
 		}
 
 		for (Classification c : childDimension) {
 			table.colNames.add(c.name);
 		}
 
 		for (Cause cause : this.causes) {
 			SortedSet<ClassificationPair> pairs = cause.getClassifications();
 			// Loop through all classification pairs for each cause
 			for (ClassificationPair pair : pairs) {
 				int i = parentDimension.indexOf(pair.parent);
 				int j = childDimension.indexOf(pair.child);
 				ClassificationTable.TableCellObject object = table.tableCells[i][j];
 
 				// Increase the cause counter for the classification pair
 				object.numberOfCauses++;
 
 				// Increase the proposed counter for the classification pair if cause is liked
 				if (cause.countLikes() > 0) {
 					object.numberOfProposedCauses++;
 				}
 
 				// Increase the correction counter for the classification pair if cause has correction proposal
 				if (cause.corrections.size() > 0) {
 					object.numberOfCorrectionCauses++;
 				}
 			}
 		}
 		calculateClassificationTablePercentages(table);
 
 		return table;
 	}
 
 	/**
 	 * Calculates percentages for numberOfCauses, numberOfProposedCauses and numberOfCorrectionCauses
 	 * that are used in the Classification Table
 	 * @param table ClassificationTable where percentages should be calculated
 	 */
 	private void calculateClassificationTablePercentages(ClassificationTable table) {
 		for (int i = 0; i < table.tableCells.length; i++) {
 			for (int j = 0; j < table.tableCells[i].length; j++) {
 				ClassificationTable.TableCellObject object = table.tableCells[i][j];
				object.percentOfCauses = (double)object.numberOfCauses / this.causes.size() * 100.0;
				object.percentOfProposedCauses = (double)object.numberOfProposedCauses / this.causes.size() * 100.0;
				object.percentOfCorrectionCauses = (double)object.numberOfCorrectionCauses / this.causes.size() * 100.0;
 			}
 		}
 	}
 
 	/**
 	 * Return dimensions
 	 * @return list of classification dimensions
 	 */
 	public List<ClassificationDimension> getClassificationDimensions() {
 		return ClassificationDimension.find("SELECT c FROM dimension AS c").fetch();
 	}
 }
