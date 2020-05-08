 /**
  * 
  *   The caMOD Software License, Version 1.0
  *
  *   Copyright 2005-2006 SAIC. This software was developed in conjunction with the National Cancer
  *   Institute, and so to the extent government employees are co-authors, any rights in such works
  *   shall be subject to Title 17 of the United States Code, section 105.
  *
  *   Redistribution and use in source and binary forms, with or without modification, are permitted
  *   provided that the following conditions are met:
  *
  *   1. Redistributions of source code must retain the above copyright notice, this list of conditions
  *   and the disclaimer of Article 3, below.  Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the following disclaimer in the documentation and/or
  *   other materials provided with the distribution.
  *
  *   2.  The end-user documentation included with the redistribution, if any, must include the
  *   following acknowledgment:
  *
  *   "This product includes software developed by the SAIC and the National Cancer
  *   Institute."
  *
  *   If no such end-user documentation is to be included, this acknowledgment shall appear in the
  *   software itself, wherever such third-party acknowledgments normally appear.
  *
  *   3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or
  *   promote products derived from this software.
  *
  *   4. This license does not authorize the incorporation of this software into any third party proprietary
  *   programs.  This license does not authorize the recipient to use any trademarks owned by either
  *   NCI or SAIC.
  *
  *
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *   WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  *   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *   DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR
  *   THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  *   OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  *   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
 * $Id: QueryManagerImpl.java,v 1.98 2008-08-14 17:18:45 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.97  2008/08/14 16:37:55  pandyas
  * modified debug line to use log
  *
  * Revision 1.96  2008/08/13 17:41:38  pandyas
  * commented out is_induced_mutation_trigger until the database is updated with either 0 or 1 -  m ust be careful to write SQL
  *
  * Revision 1.95  2008/08/13 16:45:04  pandyas
  * uncommented code to build to dev
  *
  * Revision 1.94  2008/08/12 19:47:20  pandyas
  * Fixed #15053  	Search for models with transgenic or targeted modification on advanced search page confusing
  * Fixed #15041  	Why does keyword search not pick up MTB records?
  *
  * Revision 1.93  2008/07/17 18:50:36  pandyas
  * Reverted code back to version for security scan fixes
  *
  * Revision 1.91  2008/05/22 18:19:16  pandyas
  * Modified advanced search for Cell Line to prevent SQL injection
  * Modified query to return correct results
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.90  2008/05/21 19:03:56  pandyas
  * Modified advanced search to prevent SQL injection
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.89  2008/02/07 02:19:29  pandyas
  * Fixed cell line advanced search - the % % were not being properly added to search string
  *
  * Revision 1.88  2008/02/01 17:28:20  pandyas
  * Removed log for build
  *
  * Revision 1.87  2008/02/01 17:21:27  pandyas
  * The Hibernate upgrade to version 3.1.3 broke the query.list method in the QueryManagerImpl
  * Three methods were rewritten to use Search.query method instead
  *
  * Revision 1.86  2008/01/31 22:21:46  pandyas
  * remove log printouts now that bug is resolved
  *
  * Revision 1.85  2008/01/30 17:19:01  pandyas
  * The upgraded from Hibernate 3.0.2 to 3.1.3 to resolve caCORE32 IO error forced the rewrite of a few methods.
  * - query.list did not work so it was changed to Search.query for comments method
  * - the ResultSet closed before calling the SingletonManager so it was changed in  one method
  *
  * Revision 1.84  2008/01/16 18:30:22  pandyas
  * Renamed value to Transplant for #8290
  *
  * Revision 1.83  2007/12/17 17:59:52  pandyas
  * Modified graft name to transplant as per VCDE review suggestions
  *
  * Revision 1.82  2007/10/31 19:08:24  pandyas
  * Fixed #8188 	Rename UnctrlVocab items to text entries
  *
  * Revision 1.81  2007/10/18 18:34:24  pandyas
  * Forced the stmt.close() for all methods using the ResultSet
  *
  * Revision 1.80  2007/10/03 17:07:26  pandyas
  * Explicitly called and closed Statement for two methods (getIds and
  * getQueryOnlyPrincipalInvestigators) which displyed the following error:
  * Closing a statement you left open, please do your own housekeeping
  *
  * Note:  to monitor connections see JBoss documentation
  *
  * Revision 1.79  2007/09/12 19:36:03  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.78  2007/09/12 17:27:52  pandyas
  * XENOGRAFT_INVIVO_RESULT changed names to GRAFT_INVIVO_RESULT as per VCDE rcomments
  *
  * Revision 1.77  2007/09/07 15:11:58  pandyas
  * Fixed formating for the equals (=) model id in admin query
  *
  * Revision 1.76  2007/09/06 19:35:36  pandyas
  * Modified LIKE to = for model id admin query
  *
  * Revision 1.75  2007/08/27 15:39:13  pandyas
  * hide debug code printout
  *
  * Revision 1.74  2007/08/08 18:49:23  pandyas
  * Added Screener-assigned and Editor-assigned to admin filter to limit query by current state
  *
  * Revision 1.73  2007/08/07 18:43:31  pandyas
  * Renamed to GRAFT as per VCDE comments
  *
  * Revision 1.72  2007/08/07 15:35:50  pandyas
  * modified transient interference symbol - consistent
  * working on keyword search where clause
  *
  * Revision 1.71  2007/08/06 15:31:07  pandyas
  * Modified admin model id search where clause to be an AND statement instead of a WHERE statement
  *
  * Revision 1.70  2007/08/02 15:58:13  pandyas
  * Changed comment to comments and type to state as per VCDE review changes
  *
  * Revision 1.69  2007/07/31 12:02:21  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.68  2007/05/21 17:33:01  pandyas
  * Modified simple and adv search species drop down to pull from DB (approved model species only)
  *
  * Revision 1.67  2007/03/28 18:45:47  pandyas
  * Modifed debug statements - no longer need to see them all print to the output
  *
  * Revision 1.66  2007/03/28 18:43:37  pandyas
  * Modified for the following Test Track items:
  * #462 - Customized search for carcinogens for Jackson Lab data
  * #494 - Advanced search for Carcinogens for Jackson Lab data
  *
  * Revision 1.65  2006/12/28 16:02:25  pandyas
  * Reverted to previous version - changed CE on adv search page
  *
  * Revision 1.63  2006/11/27 19:09:24  pandyas
  * #483	select of organ after search in tree causes crash, was an oracle max elemnts in list error - implemented quick fix and should optimize a fix for next release
  *
  * Revision 1.62  2006/11/14 22:06:31  pandyas
  * #476 - Transient interference advanced search does not return correct results
  *
  * Revision 1.61  2006/10/23 14:21:59  pandyas
  * cleaned up - added image code back in
  *
  * Revision 1.60  2006/10/17 16:50:25  pandyas
  * modified during development of caMOD 2.2 - added methods for tool strain, external source, and images
  *
  * Revision 1.59  2006/07/31 21:12:51  pandyas
  * Returned to an earlier version of this class so it only included changes marked for the next bug fix.  Had to remove all changes that were slated for the 2.2 release.
  *
  * Revision 1.58  2006/07/27 14:53:33  pandyas
  * Defect#404:  added dash to separate species and strain in thrid column of results list
  *
  * Revision 1.57  2006/06/08 15:07:18  pandyas
  * Defect #404 - part 2, replaced the species.abreviation || strain.name with either the species.commonName or species.commonNameUnctrlVocab || strain.name or strain.nameUnctrlVocab depending on which is available
  *
  * Revision 1.56  2006/06/02 16:16:06  pandyas
  * Defect #404 part 1 - Fixed query so Pre-clinical trials return the proper data.  Working on substituting the values in the 3rd column
  *
  * Revision 1.55  2006/05/24 15:02:59  georgeda
  * Fixed error introduced in OM change
  *
  * Revision 1.54  2006/05/22 19:39:39  schroedn
  * Display criteria for AJAX search fields correctly
  *
  * Revision 1.53  2006/05/19 17:11:31  guptaa
  * added advance autocomplete search
  *
  * Revision 1.52  2006/05/19 15:01:00  guptaa
  * Fixed broken searching
  *
  * Revision 1.50  2006/05/19 12:33:34  guptaa
  * added organs
  *
  * Revision 1.49  2006/05/18 13:05:20  guptaa
  * added disease
  *
  * Revision 1.48  2006/05/17 21:16:08  guptaa
  * organ tree changes
  *
  * Revision 1.47  2006/05/15 13:35:16  georgeda
  * Cleaned up contact info management
  *
  * Revision 1.46  2006/05/12 12:49:33  georgeda
  * Changes for autocomplete
  *
  * Revision 1.45  2006/05/10 19:29:01  georgeda
  * Fixed error w/ Morphilino search
  *
  * Revision 1.44  2006/05/10 12:00:39  georgeda
  * Changes for searching on transient interfaces, and fixed a bug from 2.1 OM change
  *
  * Revision 1.43  2006/05/05 17:16:44  georgeda
  * Fixed one more error w/ SQL introduced during model change
  *
  * Revision 1.42  2006/05/05 16:57:02  georgeda
  * Fixed errors w/ SQL introduced during model change
  *
  * Revision 1.41  2006/04/28 19:19:46  schroedn
  * Defect # 261
  * Added queries to get saved queries by a given username
  *
  * Revision 1.40  2006/04/27 15:02:57  pandyas
  * Fixed SQLString as a result of failed testing
  *
  * Revision 1.39  2006/04/20 14:59:17  georgeda
  * Optimized performance of CI query
  *
  * Revision 1.38  2006/04/19 18:40:53  georgeda
  * Fixed the keyword search issue
  *
  * Revision 1.37  2006/04/19 13:31:47  georgeda
  * Fixed Taxon/TOC problem
  *
  * Revision 1.36  2006/04/19 12:47:33  georgeda
  * Fixed issue w/ advanced search page + cleaned up warnings
  *
  * Revision 1.35  2006/04/17 19:11:05  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.34  2005/12/19 14:01:33  georgeda
  * Defect #271 - Search issues
  *
  * Revision 1.33  2005/12/19 13:48:29  georgeda
  * Defect #271 - Search issues
  *
  * Revision 1.32  2005/11/21 18:38:31  georgeda
  * Defect #35.  Trim whitespace from items that are freeform text
  *
  * Revision 1.31  2005/11/17 22:31:02  georgeda
  * Defect #131, upper case the modelDescriptor when doing a keyword search
  *
  * Revision 1.30  2005/11/17 20:22:09  georgeda
  * Defect #158.  Fixed issue w/ author name being returned for first matching pmid instead of pub. associated with animal model.
  *
  * Revision 1.29  2005/11/17 19:40:02  georgeda
  * Defect #48.  Now searches for all CI's if no specific CI is selected
  *
  * Revision 1.28  2005/11/16 21:40:30  georgeda
  * Defect #46, added code to handle CI's correctly.
  *
  * Revision 1.27  2005/11/14 16:52:37  georgeda
  * Fixed admin problem
  *
  * Revision 1.26  2005/11/14 15:14:41  schroedn
  * Defect #18
  * Changed the order the Stage 2 - Dose Reponse for is sorted, now Strain / Dosage is in order. Problem was the Dosage is stored iin Treatment as a VARCHAR instead of an integer.
  *
  * Revision 1.25  2005/11/14 14:19:13  georgeda
  * Cleanup
  *
  * Revision 1.24  2005/11/10 22:07:36  georgeda
  * Fixed part of bug #21
  *
  * Revision 1.23  2005/11/10 17:08:58  schroedn
  * Defect #12 fix schroedln (11/9/05)
  * Added the administrative site to the, In Vivo Screening Data Summary. Edited SQL query getInvivoResults
  *
  * Revision 1.22  2005/11/03 13:58:40  georgeda
  * Added function to get an InvivoResults for a specific NSC number
  *
  * Revision 1.21  2005/10/28 20:16:21  georgeda
  * Added space
  *
  * Revision 1.20  2005/10/27 18:13:48  guruswas
  * Show all publications in the publications display page.
  *
  * Revision 1.19  2005/10/24 19:36:57  georgeda
  * Changed searching to case insensitive
  *
  * Revision 1.18  2005/10/20 19:28:58  georgeda
  * Added TOC functionality
  *
  * Revision 1.17  2005/10/18 16:24:31  georgeda
  * Added getModelsByUser
  *
  * Revision 1.16  2005/10/17 13:12:47  georgeda
  * Added new method
  *
  * Revision 1.15  2005/10/12 17:24:23  georgeda
  * Get species only from approved models
  *
  * Revision 1.14  2005/10/11 18:15:10  georgeda
  * More comment changes
  *
  * Revision 1.13  2005/10/10 14:09:41  georgeda
  * Changes for comment curation and performance improvement
  *
  * Revision 1.12  2005/10/05 20:27:59  guruswas
  * implementation of drug screening search page
  *
  * Revision 1.11  2005/10/05 15:23:34  georgeda
  * Changed the theraputic approaches query to return all models w/ TA's if the search string passed in was blank
  *
  * Revision 1.10  2005/10/05 13:04:47  georgeda
  * Completed advanced search
  *
  * Revision 1.9  2005/10/04 20:19:43  georgeda
  * Updates from search changes
  *
  * Revision 1.6  2005/09/27 16:46:59  georgeda
  * Added environmental factor dropdown query
  *
  * Revision 1.5  2005/09/26 14:04:14  georgeda
  * Use HQL instead of SQL
  *
  * Revision 1.4  2005/09/16 19:30:04  guruswas
  * Display invivo data (from DTP) in the therapuetic approaches page
  *
  * Revision 1.3  2005/09/16 15:52:57  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Agent;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Comments;
 import gov.nih.nci.camod.domain.Log;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.domain.Publication;
 import gov.nih.nci.camod.domain.Species;
 import gov.nih.nci.camod.util.DrugScreenResult;
 import gov.nih.nci.camod.webapp.form.CurationAssignmentData;
 import gov.nih.nci.camod.webapp.form.SearchData;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.exception.PersistenceException;
 import gov.nih.nci.common.persistence.hibernate.HQLParameter;
 import gov.nih.nci.common.persistence.hibernate.HibernateUtil;
 
 import java.sql.*;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Expression;
 
 /**
  * Implementation of a wrapper around the HQL/JDBC interface. Used for more
  * complex instances where QBE does not have sufficient power.
  */
 public class QueryManagerImpl extends BaseManager
 {
 
     /**
      * Return all NSC numbers
      * 
      * @return a list of NSC numbers
      * 
      * @throws PersistenceException
      */
     public List getNSCNumbers() throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getNSCNumbersAsStrings");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[0];
         String theHQLQuery = "select distinct(a.nscNumber) from Agent as a WHERE nsc_number is not null";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
         log.debug("Exiting QueryManagerImpl.getNSCNumbers");
 
         return theList;
     }
 
     /**
      * Return the list of animal model names that start with the pattern passed in
      * 
      * @param inPrefix
      *            the starting characters of the name
      * 
      * @return a sorted list of unique modelDescriptors
      * 
      * @throws PersistenceException
      */
     public List getMatchingAnimalModelNames(String inPrefix) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getMatchingAnimalModelNames");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[1];
         theParams[0] = new HQLParameter();
         theParams[0].setName("modelDescriptor");
         theParams[0].setValue(inPrefix.toUpperCase() + "%");
         theParams[0].setType(Hibernate.STRING);
 
         log.debug("inPrefix: " + inPrefix);
 
         String theHQLQuery = "select distinct am.modelDescriptor from AnimalModel as am where upper(am.modelDescriptor) like :modelDescriptor AND am.state = 'Edited-approved' order by am.modelDescriptor asc ";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
         log.debug("Exiting QueryManagerImpl.getMatchingAnimalModelNames");
 
         return theList;
     }
 
     /**
      * Return the list of organ object that start with the pattern passed in
      * 
      * @param inPrefix
      *            the starting characters of the name
      * 
      * @return a sorted list of unique organ object
      * 
      * @throws PersistenceException
      */
     public List getMatchingOrgans(String inPrefix) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getMatchingOrganNames");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[1];
         theParams[0] = new HQLParameter();
         theParams[0].setName("name");
         theParams[0].setValue(inPrefix.toUpperCase() + "%");
         theParams[0].setType(Hibernate.STRING);
         //HQLParameter[] theParams = new HQLParameter[0];
        
         log.debug("inPrefix: " + inPrefix);
 
         String theHQLQuery = "select distinct histo.organ from AnimalModel as am left outer join am.histopathologyCollection as histo where am.state = 'Edited-approved' and upper(histo.organ.name) like :name";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
         log.debug("Exiting QueryManagerImpl.getMatchingOrganNames");
 
         return theList;
     }
     /**
      * Return the list of tumor classification object that start with the pattern passed in
      * 
      * @param inPrefix
      *            the starting characters of the name
      * 
      * @return a sorted list of unique tumor classification object
      * 
      * @throws PersistenceException
      */
     public List getMatchingTumorClassifications(String inPrefix) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getMatchingTumorClassificationNames");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[1];
         theParams[0] = new HQLParameter();
         theParams[0].setName("name");
         theParams[0].setValue(inPrefix.toUpperCase() + "%");
         theParams[0].setType(Hibernate.STRING);
         //HQLParameter[] theParams = new HQLParameter[0];
        
         log.debug("inPrefix: " + inPrefix);
 
         String theHQLQuery = "select distinct histo.disease from AnimalModel as am left outer join am.histopathologyCollection as histo where am.state = 'Edited-approved' and upper(histo.disease.name) like :name";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
         log.debug("Exiting QueryManagerImpl.getMatchingTumorClassificationNames");
 
         return theList;
     }
 
     
     /**
      * Return the list of gene names that start with the pattern passed in
      * 
      * @param inPrefix
      *            the starting characters of the name
      * 
      * @return a sorted list of unique gene names
      * 
      * @throws PersistenceException
      */
     public List getMatchingGeneNames(String inPrefix) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getMatchingGeneNames");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[1];
         theParams[0] = new HQLParameter();
         theParams[0].setName("name");
         theParams[0].setValue(inPrefix.toUpperCase() + "%");
         theParams[0].setType(Hibernate.STRING);
 
         log.debug("inPrefix: " + inPrefix);
 
         String theHQLQuery = "select distinct genes.name from AnimalModel as am left outer join am.engineeredGeneCollection as genes where upper(genes.name) like :name AND am.state = 'Edited-approved' ";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
         log.debug("Exiting QueryManagerImpl.getMatchingGeneNames");
 
         return theList;
     }
 
 	/**
 	 * Return the list of agent names that match the agent type passed in
 	 * 
 	 * @param inPrefix
 	 *            the starting characters of the name
 	 * 
 	 * @return a sorted list of unique modelDescriptors
 	 * 
 	 * @throws PersistenceException
 	 */
 
 	public List getMatchingAgents(String inAgentType)
 			throws PersistenceException {
 		log.debug("Entering QueryManagerImpl.getMatchingAgents");
 		ResultSet theResultSet = null;
 		List<String> theEnvFactors = new ArrayList<String>();
 
 		try {
 			// Format the query
 			String theSQLQuery = "SELECT ef.name, ef.name_altern_entry "
 					+ "FROM environmental_factor ef "
 					+ "WHERE ef.type LIKE ? "
 					+ "  AND ef.name IS NOT null "
 					+ "  UNION "
 					+ "SELECT ef.name_altern_entry "
 					+ "FROM environmental_factor ef "
 					+ "WHERE ef.TYPE_altern_entry LIKE ? "
 					+ "  AND ef.name_altern_entry IS NOT null "
 					+ "AND am.abs_cancer_model_id = ce.abs_cancer_model_id AND am.state = 'Edited-approved') ORDER BY ef.name asc ";
 
 			Object[] theParams = new Object[1];
 			theParams[0] = inAgentType;
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				String theName = theResultSet.getString(1);
 				String theUncontrolledName = theResultSet.getString(2);
 
 				if (theName != null && theName.length() > 0
 						&& !theEnvFactors.contains(theName)) {
 					theEnvFactors.add(theName);
 				} else if (theUncontrolledName != null
 						&& theUncontrolledName.length() > 0
 						&& !theEnvFactors.contains(theUncontrolledName)) {
 					theEnvFactors.add(theName);
 				}
 			}
 
 			Collections.sort(theEnvFactors);
 
 			log.debug("Exiting QueryManagerImpl.getMatchingAgents");
 		} catch (Exception e) {
 			log.error("Exception in getMatchingAgents", e);
 			throw new PersistenceException("Exception in getMatchingAgents: "
 					+ e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theEnvFactors;
 	}
 
 	/**
 	 * Return the list of Carcinogenic Agent (environmental factor types) names
 	 * 
 	 * @return a sorted list of unique types
 	 * @throws PersistenceException
 	 */
 	public List getEnvironmentalFactorAgentTypes() throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getCarcinogenicAgents");
 		
 		ResultSet theResultSet = null;
 		List<String> theEFAgentTypesList = new ArrayList<String>();
 
 		try {
 			// Format the query
 			String theSQLQuery = "SELECT ef.type, ef.type_altern_entry "
 				+ "FROM environmental_factor ef ";			
 
 			// Format the query			
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				String theType = theResultSet.getString(1);
 				String theUncontrolledType = theResultSet.getString(2);
 
 				// Remove ADMIN and Not Specified in appropriate lists
 				if (theType != null && theType.length() > 0
 						&& !theEFAgentTypesList.contains(theType)) {
 					if (!theType.equals("ADMIN")) {
 					theEFAgentTypesList.add(theType);
 					}
 				} else if (theUncontrolledType != null
 						&& theUncontrolledType.length() > 0
 						&& !theEFAgentTypesList.contains(theUncontrolledType)) {
 					if (!theUncontrolledType.equals("Not Specified")) {
 					theEFAgentTypesList.add(theUncontrolledType);
 					}
 				}
 			}
 
 			Collections.sort(theEFAgentTypesList);
 
 			log.debug("Exiting QueryManagerImpl.getMatchingAgents");
 		} catch (Exception e) {
 			log.error("Exception in getMatchingAgents", e);
 			throw new PersistenceException("Exception in getMatchingAgents: "
 					+ e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theEFAgentTypesList;
 	}	
 
 	/**
 	 * Return the list of Cell Line Names
 	 * 
 	 * @return a sorted list of unique names
 	 * @throws PersistenceException
 	 */
 	public List getCellLineNames() throws PersistenceException {
 
 		log.info("Entering QueryManagerImpl.getCellLineNames");
 		
 		ResultSet theResultSet = null;
 		List<String> theCellLineList = new ArrayList<String>();
 
 		try {
 			// Format the query
 			String theSQLQuery = "SELECT distinct cl.name "
 				+ "FROM cell_line cl, abs_cancer_model am "
 				+ "			WHERE cl.abs_cancer_model_id = am.abs_cancer_model_id "
 				+ "     	AND am.state = 'Edited-approved'";			
 
 			// Format the query			
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				String theCellLine = theResultSet.getString(1);
 				theCellLineList.add(theCellLine);				
 			}
 
 			Collections.sort(theCellLineList);
 
 			log.debug("Exiting QueryManagerImpl.getCellLineNames");
 		} catch (Exception e) {
 			log.error("Exception in getCellLineNames", e);
 			throw new PersistenceException("Exception in getCellLineNames: "
 					+ e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theCellLineList;
 	}	
 
 	/**
 	 * Return the list of Drug Names
 	 * 
 	 * @return a sorted list of unique names
 	 * @throws PersistenceException
 	 */
 	public List getTherapeuticDrugNames() throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getTherapeuticDrugNames");
 		
 		ResultSet theResultSet = null;
 		List<String> theTherapeuticDrugList = new ArrayList<String>();
 
 		try {
 			// Format the query
 			String theSQLQuery = "SELECT distinct a.name "
 				+ "FROM agent a, abs_cancer_model am, therapy t "
 				+ "			WHERE a.agent_id = t.agent_id "
 				+ "			AND t.abs_cancer_model_id = am.abs_cancer_model_id "
 				+ "     	AND am.state = 'Edited-approved'";			
 
 			// Format the query			
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				String theDrugName = theResultSet.getString(1);
 				theTherapeuticDrugList.add(theDrugName);				
 			}
 
 			Collections.sort(theTherapeuticDrugList);
 
 			log.debug("Exiting QueryManagerImpl.getTherapeuticDrugNames");
 		} catch (Exception e) {
 			log.error("Exception in getTherapeuticDrugNames", e);
 			throw new PersistenceException("Exception in getTherapeuticDrugNames: "
 					+ e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theTherapeuticDrugList;
 	}		
 	
 	/**
 	 * Return the list of Clone Designators (Engineered Gene table) for Genomic Segment Designator
 	 * 
 	 * @return a sorted list of unique names
 	 * @throws PersistenceException
 	 */
 	public List getGenomicSegmentDesignators() throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getGenomicSegmentDesignators");
 		
 		ResultSet theResultSet = null;
 		List<String> theGenSegDesList = new ArrayList<String>();
 
 		try {
 			// Format the query
 			String theSQLQuery = "SELECT distinct eg.clone_designator "
 				+ "FROM engineered_gene eg, abs_cancer_model am "
 				+ "			WHERE eg.abs_cancer_model_id = am.abs_cancer_model_id "
 				+ "     	AND am.state = 'Edited-approved'";			
 
 			log.debug("After SQLQuery");
 			
 			// Format the query			
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				String theGenSegDesignator = theResultSet.getString(1);
 				log.debug("theGenSegDesignator: " + theGenSegDesignator);				
 				if(theGenSegDesignator != null && theGenSegDesignator.length() > 0){
 					theGenSegDesList.add(theGenSegDesignator);
 				}
 			}
 			Collections.sort(theGenSegDesList);
 
 			log.debug("Exiting QueryManagerImpl.getGenomicSegmentDesignators");
 		} catch (Exception e) {
 			log.error("Exception in getGenomicSegmentDesignators", e);
 			throw new PersistenceException("Exception in getGenomicSegmentDesignators: "
 					+ e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theGenSegDesList;
 	}	
 	
 	
 	/**
 	 * Return the list of environmental factor names
 	 * 
 	 * @param inType
 	 *            the type of environmental factor
 	 * 
 	 * @return a sorted list of unique environmental factors
 	 * @throws PersistenceException
 	 */
 
 	public List getEnvironmentalFactors(String inType)
 			throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getEnvironmentalFactors");
 
 		// Format the query
 		HQLParameter[] theParams = new HQLParameter[1];
 		theParams[0] = new HQLParameter();
 		theParams[0].setName("type");
 		theParams[0].setValue(inType);
 		theParams[0].setType(Hibernate.STRING);
 
 		log.debug("inType: " + inType);
 
 		String theHQLQuery = "select distinct ef.name from EnvironmentalFactor as ef where ef.type = :type and ef.name is not null order by ef.name asc ";
 
 		List theList = Search.query(theHQLQuery, theParams);
 
 		log.debug("Found matching items: " + theList.size());
 
 		log.debug("Exiting QueryManagerImpl.getEnvironmentalFactors");
 		return theList;
 	}
 
 
 	/**
 	 * Return the list of environmental factor names
 	 * 
 	 * @param inType
 	 *            the type of environmental factor
 	 * 
 	 * @return a sorted list of unique environmental factors
 	 * @throws PersistenceException
 	 */
 	public List getQueryOnlyEnvironmentalFactors(String inType)
 			throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getQueryOnlyEnvironmentalFactors");
 		log.debug("<getQueryOnlyEnvironmentalFactors> inType: " + inType);
 		
 		ResultSet theResultSet = null;
 		List<String> theEFNameList = new ArrayList<String>();
 
 		try {
 			// Format the query
 			String theSQLQuery = "SELECT ef.name, ef.name_altern_entry "
 					+ "FROM environmental_factor ef "
 					+ "WHERE (ef.type = ? OR ef.type_altern_entry = ?)"
 					+ "  AND ef.environmental_factor_id IN (SELECT ce.environmental_factor_id "
 					+ "     FROM carcinogen_exposure ce, abs_cancer_model am "
 					+ "			WHERE ef.environmental_factor_id = ce.environmental_factor_id "
 					//+ "     	AND ef.is_induced_mutation_trigger = 0 "
 					+ "     	AND am.abs_cancer_model_id = ce.abs_cancer_model_id AND am.state = 'Edited-approved')";
 			
 			log.debug("theSQLQuery: " + theSQLQuery.toString());
 
 			Object[] theParams = new Object[2];
 			theParams[0] = inType;
 			theParams[1] = inType;			
 			theResultSet = Search.query(theSQLQuery, theParams);
 			
 			while (theResultSet.next()) {
 				String theName = theResultSet.getString(1);
 				String theUncontrolledName = theResultSet.getString(2);
 				log.debug("theName: " + theName + "\ttheUncontrolledName: " +theUncontrolledName);
 
 				if (theName != null  && theName.length() > 0
 						&& !theEFNameList.contains(theName)) {
 					theEFNameList.add(theName);
 					log.debug("Added theName: " + theName + " Bringing total to: " +theEFNameList.size());
 				} else if (theUncontrolledName != null  
 						&& theUncontrolledName.length() > 0
 						&& !theEFNameList.contains(theUncontrolledName)) {
 					theEFNameList.add(theUncontrolledName);
 					log.debug("Added theUncontrolledName: " + theUncontrolledName + " Bringing total to: " +theEFNameList.size());
 				}
 
 			}
 
 			log.debug("Exiting QueryManagerImpl.getQueryOnlyEnvironmentalFactors");
 		} catch (Exception e) {
 			log.error("Exception in getQueryOnlyEnvironmentalFactors", e);
 			throw new PersistenceException(
 					"Exception in getQueryOnlyEnvironmentalFactors: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
  		Collections.sort(theEFNameList);
 		return theEFNameList;
 	}
 	
 	/**
 	 * Return the list of gene names (targeted modification)
 	 * 
 	 * @return a sorted list of unique gene names
 	 * @throws PersistenceException
 	 */
 	public List getGeneNames()
 			throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getGeneNames");
 		log.debug("<getGeneNames>  " );
 
 		ResultSet theResultSet = null;
 		List<String> theGeneNameList = new ArrayList<String>();
 
 		try {
 			String theSQLQuery = "SELECT distinct eg.name "
 					+ "FROM engineered_gene eg, abs_cancer_model ac "
 					+ "WHERE eg.engineered_gene_type = 'TM' " 
 					+ "AND ac.state = 'Edited-approved'	";
 			
 			log.debug("theSQLQuery: " + theSQLQuery.toString());
 
 			// Format the query - use a bind variable for the first pass when list is empty only
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 
 			while (theResultSet.next()) {
 				String theGeneName = theResultSet.getString(1);
 				if(theGeneName != null && theGeneName.length() > 0){
 					theGeneNameList.add(theGeneName);
 				}
 			}
 			
 			Collections.sort(theGeneNameList, String.CASE_INSENSITIVE_ORDER);
 
 			log.debug("Exiting QueryManagerImpl.getGeneNames");
 		} catch (Exception e) {
 			log.error("Exception in getGeneNames", e);
 			throw new PersistenceException(
 					"Exception in getGeneNames: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theGeneNameList;
 	}
 	
 	/**
 	 * Return the list of gene names (transgene)
 	 * 
 	 * @param inType
 	 *            the type of genetic description (transgene)
 	 * 
 	 * @return a sorted list of unique transgene names
 	 * @throws PersistenceException
 	 */
 	public List getTransgeneNames()
 			throws PersistenceException {
 
 		log.debug("Entering QueryManagerImpl.getTransgeneNames");
 		log.debug("<getTransgeneNames> : " );
 
 		ResultSet theResultSet = null;
 		List<String> theGeneNameList = new ArrayList<String>();
 
 		try {
 			// Set engineered_gene_type = ? for first pass when list is empty 
 			String	theSQLQuery = "SELECT distinct eg.name "
 				+ "FROM engineered_gene eg, abs_cancer_model ac "
 				+ "WHERE eg.engineered_gene_type = 'T' " 
 				+ "AND ac.state = 'Edited-approved'	";	
 			
 			log.debug("theSQLQuery: " + theSQLQuery.toString());
 
 			// Format the query 
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 
 			while (theResultSet.next()) {
 				String theGeneName = theResultSet.getString(1);
 				if(theGeneName != null && theGeneName.length() > 0){
 					theGeneNameList.add(theGeneName);
 				}
 			}
 			
 			Collections.sort(theGeneNameList, String.CASE_INSENSITIVE_ORDER);
 
 			log.debug("Exiting QueryManagerImpl.getGeneNames");
 		} catch (Exception e) {
 			log.error("Exception in getGeneNames", e);
 			throw new PersistenceException(
 					"Exception in getGeneNames: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theGeneNameList;
 	}	
 
     /**
      * Return the list of environmental factor names which were used to induce a
      * mutation
      * 
      * @return a sorted list of unique environmental factors
      * @throws PersistenceException
      */
     public List getQueryOnlyInducedMutationAgents() throws PersistenceException
     {
 
         log.debug("Entering QueryManagerImpl.getQueryOnlyInducedMutationAgents");
         ResultSet theResultSet = null;
         List<String> theAgents = new ArrayList<String>();
         try
         {
 
             /* Format the query */
 			String theSQLQuery = "SELECT distinct ef.name "
 				+ " FROM environmental_factor ef, engineered_gene eg "
 				+ " WHERE ef.environmental_factor_id = eg.environmental_factor_id "
 				//+ "  AND ef.is_induced_mutation_trigger = 1 "
 				+ "     	AND ef.name IS NOT null" ; 
 			           
 
             Object[] theParams = new Object[0];
             theResultSet = Search.query(theSQLQuery, theParams);
 
             while (theResultSet.next())
             {
                 theAgents.add(theResultSet.getString(1));
             }
 
             log.debug("Exiting QueryManagerImpl.getQueryOnlyInducedMutationAgents");
         }
         catch (Exception e)
         {
             log.error("Exception in getQueryOnlyInducedMutationAgents", e);
             throw new PersistenceException("Exception in getQueryOnlyInducedMutationAgents: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
         return theAgents;
     }
 
     /**
      * Return the list of species associated with animal models
      * 
      * @return a sorted list of unique species
      * 
      * @throws PersistenceException
      */
     public List getQueryOnlySpecies(HttpServletRequest inRequest) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getQueryOnlySpecies");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[0];
         String theHQLQuery = "from Species where scientificName is not null order by scientificName asc";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
 
         log.debug("Exiting QueryManagerImpl.getQueryOnlySpecies");
         return theList;
     }
 
     public List getApprovedSpecies() throws PersistenceException
     {
 
         log.debug("Entering QueryManagerImpl.getApprovedSpecies");
         ResultSet theResultSet = null;  
         List<String> theNames = new ArrayList<String>(); 
         List<Species> theSpeciesList = new ArrayList<Species>();
        
         try
         {
             // Format the query - SQL allows more complex query
 			String theSQLQuery = "SELECT distinct sp.scientific_name "
 				+ "FROM species sp, abs_cancer_model am, strain st "
 				+ "WHERE am.strain_id = st.strain_id "
 				+ "  AND st.species_id = sp.species_id "
 				+ " AND am.state = 'Edited-approved' ORDER BY sp.scientific_name asc ";
             
             log.debug("theSQLQuery: " + theSQLQuery);
 			
             Object[] theParams = new Object[0];
             theResultSet = Search.query(theSQLQuery, theParams);
 
             // Rewrote this method during the upgrade to Hibernate
             // Had to copy ResultSet to a list since the ResultSet was closed before the 
             // while loop was able to get all the Species objects from the ManagerSingleton
             while (theResultSet.next())
             {
                 try {
                     theNames.add(theResultSet.getString(1));
                 } catch (Exception e)  {
                     log.error("Exception in getApprovedSpecies", e);
                     throw new PersistenceException("Exception in getApprovedSpecies: " + e);
                 }
             }                
 
             for (int i = 0; i < theNames.size(); i++)
             {
                 Species theSpecies = SpeciesManagerSingleton.instance().getByName(theNames.get(i));
                 theSpeciesList.add(theSpecies);
             }            
             log.debug("Exiting QueryManagerImpl.getApprovedSpecies");
         }
         catch (Exception e)
         {
             log.error("Exception in getApprovedSpecies", e);
             throw new PersistenceException("Exception in getApprovedSpecies: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 log.debug("theResultSet != null: "  );
                 try
                 {
                     Statement stmt = theResultSet.getStatement();                    
                     theResultSet.close();
                     stmt.close();
                     
                 }
                 catch (Exception e)
                 {}
             } 
         } 
         return theSpeciesList;
     }
 
     
     
     /**
      * Return the list of PI's sorted by last name
      * 
      * @return a sorted list of People objects
      * @throws PersistenceException
      */
     public List getPrincipalInvestigators() throws PersistenceException
     {
 
         log.debug("Entering QueryManagerImpl.getPrincipalInvestigators");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[0];
         String theHQLQuery = "from Person where is_principal_investigator = 1 order by last_name asc";
 
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
 
         log.debug("Exiting QueryManagerImpl.getPrincipalInvestigators");
         return theList;
     }
 
     /**
      * Return the list of PI's sorted by last name
      * 
      * @return a sorted list of People objects
      * @throws PersistenceException
      */
     public List getPeopleByRole(String inRole) throws PersistenceException
     {
 
         log.debug("Entering QueryManagerImpl.getPeopleByRole");
 
         String theHQLQuery = "from Person where username is not null ";
 
         if (!inRole.equals(Constants.Admin.Roles.ALL))
         {
             theHQLQuery += " AND party_id in (" + getPartyIdsForRole(inRole) + ") ";
         }
 
         // Complete the query
         theHQLQuery += " order by last_name asc";
 
         HQLParameter[] theParams = new HQLParameter[0];
         List theList = Search.query(theHQLQuery, theParams);
 
         log.debug("Found matching items: " + theList.size());
 
         log.debug("Exiting QueryManagerImpl.getPeopleByRole");
         return theList;
     }
 
     /**
      * Get the model id's for any model that has a histopathology with a parent
      * histopathology
      * 
      * @return a list of matching model ids
      * 
      * @throws PersistenceException
      */
     private String getPartyIdsForRole(String inRole) throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct pr.party_id FROM party_role pr " + "WHERE pr.role_id IN (SELECT role_id FROM role r " + "     WHERE r.name = ?)";
 
         Object[] theParams = new Object[1];
         theParams[0] = inRole;
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Return the list of PIs associated with animal models
      * 
      * @return a sorted list of unique PIs
      * 
      * @throws PersistenceException
      */
     public List getQueryOnlyPrincipalInvestigators() throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getQueryOnlyPrincipalInvestigators");
 
         // Format the query
         String theSQLString = "SELECT last_name, first_name " + "FROM party " + "WHERE is_principal_investigator = 1 " + "  AND first_name IS NOT NULL " + "  AND last_name IS NOT NULL " + "  AND party_id IN (SELECT DISTINCT principal_investigator_id FROM abs_cancer_model WHERE state = 'Edited-approved')" + "ORDER BY last_name ASC";
 
         ResultSet theResultSet = null;
 
         List<String> thePIList = new ArrayList<String>();
 
         try
         {
             log.debug("getQueryOnlyPrincipalInvestigators - SQL: " + theSQLString);
 
             Object[] params = new Object[0];
             theResultSet = Search.query(theSQLString, params);
 
             while (theResultSet.next())
             {
                 String thePIEntry = theResultSet.getString(1) + ", " + theResultSet.getString(2);
                 thePIList.add(thePIEntry);
             }
 
         }
         catch (Exception e)
         {
             log.error("Exception in getQueryOnlyPrincipalInvestigators", e);
             throw new PersistenceException("Exception in getQueryOnlyPrincipalInvestigators: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {                
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
 
                 }
                 catch (Exception e)
                 {}
             }
         }
         return thePIList;
     }
 
     /**
      * Return the list of Editors associated with animal models
      * 
      * @return a sorted list of unique Editors
      * 
      * @throws PersistenceException
      */
     public List getQueryOnlyEditors() throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getQueryOnlyEditors");
 
         // Format the query
         String theSQLString = "SELECT last_name, first_name " + "FROM party " + "WHERE is_principal_investigator = 1 " + "  AND first_name IS NOT NULL " + "  AND last_name IS NOT NULL " + "  AND party_id IN (SELECT DISTINCT principal_investigator_id FROM abs_cancer_model WHERE state = 'Edited-approved')" + "ORDER BY last_name ASC";
 
         ResultSet theResultSet = null;
 
         List<String> theEditorList = new ArrayList<String>();
 
         try
         {
             log.debug("getQueryOnlyEditors - SQL: " + theSQLString);
 
             Object[] params = new Object[0];
             theResultSet = Search.query(theSQLString, params);
 
             while (theResultSet.next())
             {
                 String theEditorEntry = theResultSet.getString(1) + ", " + theResultSet.getString(2);
                 theEditorList.add(theEditorEntry);
             }
 
         }
         catch (Exception e)
         {
             log.error("Exception in getQueryOnlyEditors", e);
             throw new PersistenceException("Exception in getQueryOnlyEditors: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
         return theEditorList;
     }    
     
     /**
      * Return the latest log for an animal model
      * 
      * @param inModel
      *            the animal model to get the latest log for
      * 
      * @return the current log for an animal model
      * @throws PersistenceException
      */
     public Log getCurrentLog(AnimalModel inModel) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getCurrentLog");
 
         HQLParameter[] theParams = new HQLParameter[1];
         theParams[0] = new HQLParameter();
         theParams[0].setName("abs_cancer_model_id");
         theParams[0].setValue(inModel.getId());
         theParams[0].setType(Hibernate.LONG);
 
         String theHQLQuery = "from Log where abs_cancer_model_id = :abs_cancer_model_id order by timestamp desc";
         log.debug("The HQL query: " + theHQLQuery);
         List theLogs = Search.query(theHQLQuery, theParams);
 
         Log theLog = null;
         if (theLogs != null && theLogs.size() > 0)
         {
             theLog = (Log) theLogs.get(0);
             log.debug("Found a matching object: " + theLog.getId());
         }
         else
         {
             log.debug("No object found.");
         }
 
         log.debug("Exiting QueryManagerImpl.getCurrentLog");
 
         return theLog;
     }
 
     /**
      * Return the latest log for an animal model/user combo
      * 
      * @param inModel
      *            the animal model to get the latest log for
      * @param inUser
      *            the user to get the latest log for
      * 
      * @return the current log for an animal model/user combination
      * @throws PersistenceException
      */
     public Log getCurrentLogForUser(AnimalModel inModel,
                                     Person inUser) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getCurrentLogForUser");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[2];
         theParams[0] = new HQLParameter();
         theParams[0].setName("abs_cancer_model_id");
         theParams[0].setValue(inModel.getId());
         theParams[0].setType(Hibernate.LONG);
         theParams[1] = new HQLParameter();
         theParams[1].setName("party_id");
         theParams[1].setValue(inUser.getId());
         theParams[1].setType(Hibernate.LONG);
 
         String theHQLQuery = "from Log where abs_cancer_model_id = :abs_cancer_model_id and party_id = :party_id " + "and comments_id is null order by timestamp desc";
         log.debug("the HQL Query: " + theHQLQuery);
         List theLogs = Search.query(theHQLQuery, theParams);
 
         Log theLog = null;
         if (theLogs != null && theLogs.size() > 0)
         {
             theLog = (Log) theLogs.get(0);
             log.debug("Found a matching object: " + theLog.getId());
         }
         else
         {
             log.debug("No object found.");
         }
 
         log.debug("Exiting QueryManagerImpl.getCurrentLogForUser");
         return theLog;
     }
 
     /**
      * Return the latest log for a comment/user combo
      * 
      * @param inComments
      *            the comments to get the latest log for
      * @param inUser
      *            the user to get the latest log for
      * 
      * @return the current log for an comments/user combination
      * 
      * @throws PersistenceException
      */
     public Log getCurrentLogForUser(Comments inComments,
                                     Person inUser) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getCurrentLogForUser");
 
         // Format the query
         HQLParameter[] theParams = new HQLParameter[3];
         theParams[0] = new HQLParameter();
         theParams[0].setName("abs_cancer_model_id");
         theParams[0].setValue(inComments.getAbstractCancerModel().getId());
         theParams[0].setType(Hibernate.LONG);
         theParams[1] = new HQLParameter();
         theParams[1].setName("party_id");
         theParams[1].setValue(inUser.getId());
         theParams[1].setType(Hibernate.LONG);
         theParams[2] = new HQLParameter();
         theParams[2].setName("comments_id");
         theParams[2].setValue(inComments.getId());
         theParams[2].setType(Hibernate.LONG);
 
         String theHQLQuery = "from Log where abs_cancer_model_id = :abs_cancer_model_id and party_id = :party_id " + "and comments_id = :comments_id order by timestamp desc";
         log.debug("the HQL Query: " + theHQLQuery);
         List theLogs = Search.query(theHQLQuery, theParams);
 
         Log theLog = null;
         if (theLogs != null && theLogs.size() > 0)
         {
             theLog = (Log) theLogs.get(0);
             log.debug("Found a matching object: " + theLog.getId());
         }
         else
         {
             log.debug("No object found.");
         }
 
         log.debug("Exiting QueryManagerImpl.getCurrentLogForUser");
         return theLog;
     }
 
     /**
      * Return all of the comments associated with a person that match the state
      * passed in
      * 
      * @param inState
      *            the state of the comment
      * 
      * @param inPerson
      *            the person to match
      * 
      * @return a list of matching comments
      * 
      * @throws PersistenceException
      */
     public List getCommentsBySection(String inSection,
                                      Person inPerson,
                                      AnimalModel inModel) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getCommentsBySection");
         String theHQLQuery = null;
 
         HQLParameter[] theParams = new HQLParameter[3];
         
         // Format the query and define the query
         // Only query for party if the passed in party wasn't null
         // If person is null only retrieve Screened-approved models        
         if (inPerson != null){
 
             theParams[0] = new HQLParameter();
             theParams[0].setName("abs_cancer_model_id");
             theParams[0].setValue(inModel.getId());
             theParams[0].setType(Hibernate.LONG);
             theParams[1] = new HQLParameter();
             theParams[1].setName("party_id");
             theParams[1].setValue(inPerson.getId());
             theParams[1].setType(Hibernate.LONG);
             theParams[2] = new HQLParameter();
             theParams[2].setName("name");
             theParams[2].setValue(inSection);
             theParams[2].setType(Hibernate.STRING);
             
             theHQLQuery = "from Comments as c where (c.state = 'Screened-approved'or c.submitter = :party_id) and c.abstractCancerModel in (" + "from AnimalModel as am where am.id = :abs_cancer_model_id) and c.modelSection in (from ModelSection where name = :name)";
 
         } else {
             theParams = new HQLParameter[2];
 	        theParams[0] = new HQLParameter();
 	        theParams[0].setName("abs_cancer_model_id");
 	        theParams[0].setValue(inModel.getId());
 	        theParams[0].setType(Hibernate.LONG);
 	        theParams[1] = new HQLParameter();
 	        theParams[1].setName("name");
 	        theParams[1].setValue(inSection);
 	        theParams[1].setType(Hibernate.STRING);
 	        
         	theHQLQuery = "from Comments as c where c.state = 'Screened-approved' and c.abstractCancerModel in (" + "from AnimalModel as am where am.id = :abs_cancer_model_id) and c.modelSection in (from ModelSection where name = :name)";
             
         }
         log.debug("the HQL Query: " + theHQLQuery);
         List theComments = Search.query(theHQLQuery, theParams);
 
         if (theComments == null)
         {
         	log.debug("theComments == null: " );
             theComments = new ArrayList();
         }
 
         log.debug("Exiting QueryManagerImpl.getCommentsBySection");
 
         return theComments;
     }
 
     /**
      * Return all of the comments associated with a person that match the state
      * passed in
      * 
      * @param inState
      *            the state of the comment
      * 
      * @param inPerson
      *            the person to match
      * 
      * @return a list of matching comments
      * 
      * @throws PersistenceException
      */
     public List getCommentsByStateForPerson(String inState,
                                             Person inPerson) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getCommentsByStateForPerson");
         String theHQLQuery = null;
 
         HQLParameter[] theParams = new HQLParameter[2];     
 
         // Format the query and define the query
         // Only query for party if the passed in party wasn't null
         // If person is null only retrieve Screened-approved models        
         if (inPerson != null){
             theParams[0] = new HQLParameter();
             theParams[0].setName("state");
             theParams[0].setValue(inState);
             theParams[0].setType(Hibernate.STRING);
             theParams[1] = new HQLParameter();
             theParams[1].setName("party_id");
             theParams[1].setValue(inPerson.getId());
             theParams[1].setType(Hibernate.LONG);
             
             theHQLQuery = "from Comments as c where c.state = :state and c.id in (select l.comments from Log as l where l.submitter = :party_id and l.state = :state)"; 
         }
         else
         {
             theParams = new HQLParameter[1];
             theParams[0] = new HQLParameter();
             theParams[0].setName("state");
             theParams[0].setValue(inState);
             theParams[0].setType(Hibernate.STRING);
             
             theHQLQuery = "from Comments as c where c.state = :state and c.id in (select l.comments from Log as l where l.state = :state)";
         }
 
         log.debug("the HQL Query: " + theHQLQuery);
         List theComments = Search.query(theHQLQuery, theParams);
 
         if (theComments == null)
         {
             log.debug("theComments == null: " );
             theComments = new ArrayList();
         }
 
         log.debug("Exiting QueryManagerImpl.getCommentsBySection");
 
         return theComments;
     }
 
     /**
      * Return all of the models associated with a person that match the state
      * passed in
      * 
      * @param inState
      *            the state of the comment
      * 
      * @param inPerson
      *            the person to match
      * 
      * @return a list of matching models
      * 
      * @throws PersistenceException
      */
     public List getModelsByStateForPerson(String inState,
                                           Person inPerson) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getCurrentLog");
 
         //String theHQLQuery = "from AnimalModel as am where am.state = :state and am.id in (";
         String theHQLQuery = null;
 
         HQLParameter[] theParams = new HQLParameter[2];
         
         // Format the query and define the query
         // Only query for party if the passed in party wasn't null
         // If person is null only retrieve Screened-approved models 
         if (inPerson != null){
             theParams[0] = new HQLParameter();
             theParams[0].setName("state");
             theParams[0].setValue(inState);
             theParams[0].setType(Hibernate.STRING);
             theParams[1] = new HQLParameter();
             theParams[1].setName("party_id");
             theParams[1].setValue(inPerson.getId());
             theParams[1].setType(Hibernate.LONG);
             
            theHQLQuery = "from AnimalModel as am where am.state = :state and am.id in (select l.abstractCancerModel from Log as l where l.submitter = :party_id and l.state = :state)";          
         } else {
             theParams = new HQLParameter[1];            
             theParams[0] = new HQLParameter();
             theParams[0].setName("state");
             theParams[0].setValue(inState);
             theParams[0].setType(Hibernate.STRING);
             
             theHQLQuery = "from AnimalModel as am where am.state = :state and am.id in (select l.abstractCancerModel from Log as l where l.state = :state)";
             
         }
         
         log.debug("the HQL Query: " + theHQLQuery);
         List theComments = Search.query(theHQLQuery, theParams);
 
         if (theComments == null)
         {
             log.debug("theComments == null: " );
             theComments = new ArrayList();
         }
 
         log.debug("Exiting QueryManagerImpl.getCommentsBySection");
 
         return theComments;
     }
 
     /**
      * Return all of the models associated which have the passed in user as
      * either the submitter _or_ the PI
      * 
      * @param inUsername
      * 
      * @return a list of matching models
      * 
      * @throws PersistenceException
      */
     public List getModelsByUser(String inUsername) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getModelsByUser");
 
         String theHQLQuery = "from AnimalModel as am where " + " am.submitter in (from Person where username = :username) or" + " am.principalInvestigator in (from Person where username = :username) " + " order by model_descriptor";
 
         log.debug("The HQL query: " + theHQLQuery);
 
         Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 
 
         theQuery.setParameter("username", inUsername);
 
         log.debug("Entering QueryManagerImpl.getModelsByUser here");
         List theComments = new ArrayList();
 
         // breaks before this line
         theComments = theQuery.list();
 
         if (theComments == null)
         {
             theComments = new ArrayList();
         }
 
         return theComments;
     }
 
     /**
      * Return all of InvivoResult objects with a specific NSC number
      * 
      * @param inNscNumber
      * 
      * @return a list of matching InvivoResult objects
      * 
      * @throws PersistenceException
      */
     public List getInvivoResultCollectionByNSC(String inNSCNumber,
                                                String inModelId) throws PersistenceException
     {
         log.debug("Entering QueryManagerImpl.getInvivoResultCollectionByNSC");
 
         String theHQLQuery = "from InvivoResult as ir where " + " ir.id in (" + getInvivoIdsForNSCNumberAndModel(inNSCNumber, inModelId) + ")";
         log.debug("The HQL query: " + theHQLQuery);
 
         Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 
         List theResults = theQuery.list();
 
         if (theResults == null)
         {
             theResults = new ArrayList();
         }
         return theResults;
     }
 
     /**
      * Get the InvivoResult id's for for a given NSC number and model
      * 
      * @return a list of matching ids
      * 
      * @throws PersistenceException
      */
     private String getInvivoIdsForNSCNumberAndModel(String inNSCNumber,
                                                     String inModelId) throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct trans_inv.invivo_result_id FROM transplant_invivo_result trans_inv " + "WHERE trans_inv.invivo_result_id IN (SELECT ir.invivo_result_id FROM invivo_result ir, agent ag " + "     WHERE ir.agent_id = ag.agent_id and ag.nsc_number = ?) and trans_inv.abs_cancer_model_id = ?";
 
         Object[] theParams = new Object[2];
         theParams[0] = inNSCNumber;
         theParams[1] = inModelId;
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get yeast screen result data (ave inibition etc.) for a given Agent
      * (drug)
      * 
      * @param agent
      *            refers to the compound that was used in the yeast experiment
      * @param stage
      *            is the stage of the experiment (0, 1, or 2)
      * 
      * @return the results
      * @throws PersistenceException
      */
     public DrugScreenResult getYeastScreenResults(Agent agent,
                                                   String stage,
                                                   boolean useNscNumber) throws PersistenceException
     {
         DrugScreenResult dsr = new DrugScreenResult();
 
         ResultSet theResultSet = null;
         try
         {
 
             String theSQLString = "select st.name," + "\n" + "       t.dosage," + "\n" + "       sr.aveinh aveinh," + "\n" + "       sr.diffinh diffinh" + "\n" + "  from screening_Result sr," + "\n" + "       agent ag," + "\n" + "       strain st," + "\n" + "       YST_MDL_SCRNING_RESULT ymsr," + "\n" + "       abs_cancer_model acm," + "\n" + "       treatment t," + "\n" + "   species sp" + "\n" + " where sr.agent_id = ag.agent_id" + "\n" + "   and sr.screening_result_id = ymsr.screening_result_id" + "\n" + "   and sr.treatment_id = t.treatment_id" + "\n" + "   and ymsr.abs_cancer_model_id = acm.abs_cancer_model_id" + "\n" + "   and acm.strain_id = st.strain_id" + "\n" + "   and st.species_id = sp.species_id" + "\n" + "   and sr.stage = ?" + "\n";
 
             // We query for the agent id on the drug screen search pages and the nsc number on
             // the theraputic approaches pages
             Long theParam;
             if (useNscNumber == true)
             {
                 theSQLString += "   and ag.nsc_number = ?" + "\n";
                 theParam = agent.getNscNumber();
             }
             else
             {
                 theSQLString += "   and ag.agent_id = ?" + "\n";
                 theParam = agent.getId();
             }
 
             theSQLString += " order by st.name, to_number(t.dosage)";
 
             log.debug("getYeastScreenResults - SQL: " + theSQLString);
 
             Object[] params = new Object[2];
             params[0] = stage;
             params[1] = theParam;
             theResultSet = Search.query(theSQLString, params);
             while (theResultSet.next())
             {
                 final String strain = theResultSet.getString(1);
                 final String dosage = theResultSet.getString(2);
                 final float aveinh = theResultSet.getFloat(3);
                 final float diffinh = theResultSet.getFloat(4);
                 dsr.addEntry(strain, dosage, aveinh, diffinh);
             }
             log.debug("Got " + dsr.strainCount + " strains");
         }
         catch (Exception e)
         {
             log.error("Exception in getYeastScreenResults", e);
             throw new PersistenceException("Exception in getYeastScreenResults: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
         return dsr;
     }
 
     /**
      * Get the invivo (Transplant) data (from DTP) for a given Agent.nscNumber
      * (drug)
      * 
      * @param agent
      *            refers to the compound that was used in the Transplant
      *            experiment
      * 
      * @return the results (list of abs_cancer_model_id, model_descriptor, and #
      *         of records
      * @throws PersistenceException
      */
     public List getInvivoResults(Agent agent,
                                  boolean useNscNumber) throws PersistenceException
     {
         List<String[]> results = new ArrayList<String[]>();
         int cc = 0;
         ResultSet theResultSet = null;
         log.debug("Env factor_id=" + agent.getId());
         try
         {
 
             String theSQLString = "select acm.abs_cancer_model_id," + "\n" + "       acm.model_descriptor," 
             + "\n" + "       st.name," + "\n" + "       acm.administrative_site," + "\n" 
             + "       count(*)" + "\n" + "  from invivo_Result sr," + "\n" + "       agent a," + "\n" 
             + "       TRANSPLANT_INVIVO_RESULT ymsr," + "\n" + "       abs_cancer_model acm," + "\n" 
             + "       treatment t," + "\n" + "       strain st," + "\n" + "       species sp" + "\n" 
             + " where sr.agent_id = a.agent_id" + "\n" + "   and sr.invivo_result_id = ymsr.invivo_result_id" 
             + "\n" + "   and sr.treatment_id = t.treatment_id" + "\n" 
             + "   and ymsr.abs_cancer_model_id = acm.abs_cancer_model_id" + "\n" 
             + "   and acm.strain_id = st.strain_id" + "\n" + "   and st.species_id = sp.species_id" + "\n";
 
             Long theParam;
             if (useNscNumber == true)
             {
                 theSQLString += "   and a.nsc_number = ?" + "\n";
                 theParam = agent.getNscNumber();
             }
             else
             {
                 theSQLString += "   and a.agent_id = ?" + "\n";
                 theParam = agent.getId();
             }
 
             theSQLString += " group by acm.abs_cancer_model_id, acm.model_descriptor, st.name, acm.administrative_site" + "\n" + " order by 3, 2";
 
             log.debug("getInvivoResults - SQL: " + theSQLString);
 
             Object[] params = new Object[1];
             params[0] = theParam;
             theResultSet = Search.query(theSQLString, params);
             while (theResultSet.next())
             {
                 String[] item = new String[5];
                 item[0] = theResultSet.getString(1); // the id
                 item[1] = theResultSet.getString(2); // model descriptor
                 item[2] = theResultSet.getString(3); // strain
                 item[3] = theResultSet.getString(4); // administrative site
                 item[4] = theResultSet.getString(5); // record count
                 results.add(item);
                 cc++;
             }
             log.debug("Got " + cc + " Transplant models");
         }
         catch (Exception e)
         {
             log.error("Exception in getYeastScreenResults", e);
             throw new PersistenceException("Exception in getYeastScreenResults: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
         return results;
     }
 
     /**
      * Get the model id's for any model that has a strain associated w/ the species
      * 
      * @param inSpecies
      *            the species to search for
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getStrainIdsForSpecies(String inSpecies) throws PersistenceException
     {
         String theSQLString = "SELECT distinct strain.strain_id FROM strain WHERE strain.species_id IN (SELECT species.species_id FROM species WHERE species.scientific_name like ? or species.scientific_name_altern_entry like ?) ";
 
         log.debug("getStrainIdsForSpecies theSQLString: " + theSQLString);
         Object[] theParams = new Object[2];
         theParams[0] = inSpecies;
         theParams[1] = theParams[0];
 
         log.debug("The params: " + theParams[0]);
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has a histopathology associated
      * with a specific organ.
      * 
      * @param inConceptCodes
      *            the concept codes  and organ tissue name to search for
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForHistopathologyOrgan(String inConceptCodes, String inOrganTissueName) throws PersistenceException {
 
 		String theConceptCodeList = "";
 
 		 // Format the query
         Object[] theParams = null;
         		
 		String theSQLString = "SELECT distinct hist.abs_cancer_model_id FROM histopathology hist "
 			+ "WHERE hist.abs_cancer_model_id IS NOT null "
 			+ "AND hist.histopathology_id IN (SELECT h.histopathology_id "
 			+ "     FROM histopathology h, organ o "
 			+ "     WHERE h.organ_id = o.organ_id ";
 			
 		if (inOrganTissueName != null && inOrganTissueName.length() > 0 && inConceptCodes == null || inConceptCodes.trim().length() == 0) {
 
 			theParams = new Object[1];
 	        theParams[0] = "%"+inOrganTissueName.toUpperCase()+ "%";
 	        theSQLString += " AND upper(o.name) like ? )";
 
 		} else if (inConceptCodes.trim().length() > 0) {
             
             StringTokenizer theTokenizer = new StringTokenizer(inConceptCodes, ",");
 
 			theParams = new Object[0];
 			while (theTokenizer.hasMoreElements()) {
 				theConceptCodeList += "'" + theTokenizer.nextToken() + "'";
 
 				// Only tack on a , if it's not the last element
 				if (theTokenizer.hasMoreElements()) {
 					theConceptCodeList += ",";
 				}
 			}
 	        theSQLString += " AND o.concept_code IN (" + theConceptCodeList + "))";
 		}
 
 		return getIds(theSQLString, theParams);
 	}
 
 
     /**
      * Get the model id's for any model that has a histopathology with a parent
      * histopathology
      * 
      * @return a list of matching model ids
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForHistoMetastasis() throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct hist.abs_cancer_model_id FROM histopathology hist " + "WHERE hist.abs_cancer_model_id IS NOT null " + "AND hist.histopathology_id IN (SELECT h.parent_histopathology_id FROM histopathology h " + "     WHERE h.parent_histopathology_id IS NOT NULL)";
 
         Object[] theParams = new Object[0];
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has a Transplant
      *  
      * @return a list of matching model ids
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForTransplant() throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct par_abs_can_model_id FROM abs_cancer_model ";
 
         Object[] theParams = new Object[0];
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has associated microarray data
      * 
      * @return a list of matching model ids
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForMicroArrayData() throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct abs_cancer_model_id FROM micro_array_data";
 
         Object[] theParams = new Object[0];
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has associated transient interface data
      * 
      * @return a list of matching model ids
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForTransientInterference() throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct abs_cancer_model_id FROM transient_interference";
 
         Object[] theParams = new Object[0];
         return getIds(theSQLString, theParams);
 
     }
     
     /**
      * Get the model id's for any model that has a cellline w/ a matching name
      * 
      * @param inCellLineName
      *            the text to search for in the cell-line
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForCellLine(String inCellLineName) throws PersistenceException
     {
         String theSQLString = "SELECT distinct abs_cancer_model_id " + "FROM cell_line c " + "WHERE upper(c.name) like ?";
     	
         String theSQLCellLine = "%";
         if (inCellLineName != null && inCellLineName.trim().length() > 0)
         {
         	theSQLCellLine = "%" + inCellLineName.trim().toUpperCase() + "%";
         }    	
         Object[] theParams = new Object[1];
         theParams[0] = "%" + theSQLCellLine + "%";
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has a histopathology associated
      * with a specific organ.
      * 
      * @param inDisease
      *            the disease to search for
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForTherapeuticApproach(String inTherapeuticApproach) throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct ther.abs_cancer_model_id " + "FROM therapy ther " + "WHERE ther.therapy_id IN (SELECT t.therapy_id FROM therapy t, agent ag " + "     WHERE t.agent_id = ag.agent_id " + "         AND upper(ag.name) like ?)";
 
         String theSQLTheraputicApproach = "%";
         if (inTherapeuticApproach != null && inTherapeuticApproach.trim().length() > 0)
         {
             theSQLTheraputicApproach = "%" + inTherapeuticApproach.trim().toUpperCase() + "%";
         }
 
         Object[] theParams = new Object[1];
         theParams[0] = theSQLTheraputicApproach;
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has a transient interference associated
      * with a specific organ.
      * 
      * @param inTransientInterference
      *            the transient interface to search for
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForTransientInterference(String inTransientInterference) throws PersistenceException
     {
 
         String theSQLString = "SELECT distinct t.abs_cancer_model_id " + "FROM transient_interference t WHERE upper(t.targeted_region) like ?";
 
         String theSQLTheraputicApproach = "%";
         if (inTransientInterference != null && inTransientInterference.trim().length() > 0)
         {
             theSQLTheraputicApproach = "%" + inTransientInterference.trim().toUpperCase() + "%";
         }
 
         Object[] theParams = new Object[1];
         theParams[0] = theSQLTheraputicApproach;
         return getIds(theSQLString, theParams);
 
     }
 
     /**
      * Get the model id's for any model that has a histopathology associated
      * with a specific disease.
      * 
      * @param inDisease
      *            the concept code and disease name to search for
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForHistopathologyDisease(String inConceptCodes, String inDiseaseName) throws PersistenceException {
 
 		String theConceptCodeList = "";
 
 		 // Format the query
         Object[] theParams = null;
         		
 		String theSQLString = 
 			"SELECT distinct hist.abs_cancer_model_id " 
 			+ "FROM histopathology hist " 
 			+ "WHERE hist.histopathology_id IS NOT null " 
 			+ "AND hist.histopathology_id IN (SELECT h.histopathology_id " 
 			+ "     FROM histopathology h, disease d " 
 			+ "     WHERE h.disease_id = d.disease_id ";
 
 		if (inDiseaseName != null && inDiseaseName.trim().length() > 0 && inConceptCodes == null || inConceptCodes.trim().length() == 0 || inConceptCodes.equalsIgnoreCase("000000") && inConceptCodes.trim().length() > 0) 
         {		
 			theParams = new Object[1];
 	        theParams[0] = "%"+inDiseaseName.toUpperCase()+ "%";
 	        theSQLString += " AND upper(d.name) like ? )";
 		} 
         else if (inConceptCodes.trim().length() > 0) 
         {		
             StringTokenizer theTokenizer = new StringTokenizer(inConceptCodes, ",");
             
 			theParams = new Object[0];
 			while (theTokenizer.hasMoreElements()) {
 				theConceptCodeList += "'" + theTokenizer.nextToken() + "'";
 
 				// Only tack on a , if it's not the last element
 				if (theTokenizer.hasMoreElements()) {
 					theConceptCodeList += ",";
 				}
 			}
 	        theSQLString += " AND d.concept_code IN (" + theConceptCodeList + "))";			
 		}  
         
 		return getIds(theSQLString, theParams);
 	}
 
     /**
      * Get the models with the associated engineered gene
      * 
      * @param inGeneName
      *            the name of the transgene or targeted modification
      * 
      * @param isEngineeredTransgene
      *            are we looking for a transgene?
      * 
      * @param isEngineeredTransgene
      *            are we looking for an induced mutation?
      * 
      * @param inGenomicSegDesignator
      *            the name of the genomic segment designator
      * 
      * @param inInducedMutationAgent
      *            the name of Agent which induced the mutation. Exact match.
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      * 
      */
     
     private String getModelIdsForEngineeredGenes(String inGeneName, String inTransgeneName,
                                                  boolean isEngineeredTransgene,
                                                  boolean isTargetedModification,
                                                  String inGenomicSegDesignator,
                                                  String inInducedMutationAgent) throws PersistenceException
     {
 
         List<Object> theList = new ArrayList<Object>();
 
         String theSQLString = "SELECT eg.abs_cancer_model_id " + "FROM engineered_gene eg WHERE ";
 
         String OR = " ";
 
         if (isEngineeredTransgene == true)
         {
         	if(inTransgeneName != null && inTransgeneName.trim().length() > 0){
 	            theSQLString += OR + " eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " 
 	                         + " FROM engineered_gene WHERE upper(name) LIKE ? AND engineered_gene_type = 'T')";
 	            OR = " OR ";
 	            theList.add("%" + inTransgeneName.trim().toUpperCase() + "%");
         	} else {
                 theSQLString += OR + " eg.engineered_gene_type = 'T'";
         	}
         }
         if (isTargetedModification == true )
         {
         	if(inGeneName != null && inGeneName.trim().length() > 0){
             theSQLString += OR + " eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " 
                          + " FROM engineered_gene WHERE upper(name) LIKE ? AND engineered_gene_type = 'TM')";
             OR = " OR ";
             theList.add("%" + inGeneName.trim().toUpperCase() + "%");
         	} else {
                 theSQLString += OR + " eg.engineered_gene_type = 'TM'";        		
         	}
         }
         if (inInducedMutationAgent != null && inInducedMutationAgent.trim().length() > 0)
         {
             theSQLString += OR + " eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " 
                          + " FROM engineered_gene WHERE engineered_gene_id IN (" 
                          + " SELECT distinct eg.engineered_gene_id FROM environmental_factor ef, engineered_gene eg " 
                          + " WHERE ef.name = ? " + " AND ef.environmental_factor_id = eg.environmental_factor_id) AND engineered_gene_type = 'IM')";
             OR = " OR ";
             theList.add(inInducedMutationAgent.trim());
         }
         if (inGenomicSegDesignator != null && inGenomicSegDesignator.trim().length() > 0)
         {
             theSQLString += OR + " eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " 
                          + " FROM engineered_gene WHERE upper(clone_designator) LIKE ? AND engineered_gene_type = 'GS')";
             theList.add(inGenomicSegDesignator.trim().toUpperCase());
         }
 
         // Convert the params
         Object[] theParams = new Object[theList.size()];
         for (int i = 0; i < theList.size(); i++)
         {
             theParams[i] = theList.get(i);
         }
 
         return getIds(theSQLString, theParams);
     }
 
     /**
      * Get the models with the associated engineered gene
      * 
      * @param inKeyword
      *            the keyword to search for
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      * 
      */
     private String getModelIdsForAnyEngineeredGene(String inKeyword) throws PersistenceException
     {
         String theSQLString = "SELECT distinct eg.abs_cancer_model_id " + "FROM engineered_gene eg WHERE ";
 
         theSQLString += " eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " + " FROM engineered_gene WHERE upper(name) LIKE ?)";
         theSQLString += " OR eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " + " FROM engineered_gene WHERE engineered_gene_id IN (" + " SELECT distinct egg.engineered_gene_id FROM environmental_factor ef, engineered_gene egg " + " WHERE upper(ef.name) like ? " + " AND ef.environmental_factor_id = egg.environmental_factor_id) AND engineered_gene_type = 'IM')";
         theSQLString += " OR eg.engineered_gene_id IN (SELECT distinct engineered_gene_id " + " FROM engineered_gene WHERE upper(clone_designator) LIKE ? AND engineered_gene_type = 'GS')";
 
         // Convert the params
         Object[] theParams = new Object[3];
         theParams[0] = inKeyword;
         theParams[1] = inKeyword;
         theParams[2] = inKeyword;
 
         return getIds(theSQLString, theParams);
     }
 
     /**
      * Get the model id's that have an carcinogen_exposure
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForAnyEnvironmentalFactor() throws PersistenceException
     {
         String theSQLString = "SELECT distinct ce.abs_cancer_model_id FROM carcinogen_exposure ce ";
 
         Object[] theParams = new Object[0];
         return getIds(theSQLString, theParams);
     }
 
 	/**
 	 * Get the model id's that have a matching environmental factor
 	 * 
 	 * @param inType
 	 *            the EF type
 	 * @param inName
 	 *            the name to look for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForEnvironmentalFactor(String inType,
 			String inName) throws PersistenceException {
 		log.debug("<getModelIdsForEnvironmentalFactor> inType: " + inType);
 		log.debug("<getModelIdsForEnvironmentalFactor> inName: " + inName);		
 		
 		String theSQLString = "SELECT distinct ce.abs_cancer_model_id FROM carcinogen_exposure ce "
 				+ "WHERE ce.environmental_factor_id IN (SELECT ef.environmental_factor_id FROM carcinogen_exposure ce, environmental_factor ef"
 				+ "     WHERE ce.environmental_factor_id = ef.environmental_factor_id AND (ef.name = ? OR ef.name_altern_entry = ?)  " 
 				+ "AND (ef.type = ? OR ef.type_altern_entry = ?) )";
 
 		Object[] theParams = new Object[4];
 		theParams[0] = inName;
 		theParams[1] = inName;
 		theParams[2] = inType;
 		theParams[3] = inType;		
 		return getIds(theSQLString, theParams);
 	}
 
 	/**
 	 * Get the model id's for any model that has a type of env factor
 	 * 
 	 * @param inType
 	 *            the type to look for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 		private String getModelIdsForAnEnvironmentalFactorByType(String inType) throws PersistenceException {
 			
 			log.debug("In getModelIdsForAnEnvironmentalFactorByType");
 			String theSQLString = "SELECT distinct ce.abs_cancer_model_id FROM carcinogen_exposure ce "
 					+ "WHERE ce.environmental_factor_id IN (SELECT ef.environmental_factor_id FROM carcinogen_exposure ce, environmental_factor ef"
 					+ "     WHERE ce.environmental_factor_id = ef.environmental_factor_id AND (ef.type = ? OR ef.type_altern_entry = ?) )";
 
 			Object[] theParams = new Object[2];
 			theParams[0] = inType;
 			theParams[1] = inType;			
 			return getIds(theSQLString, theParams);
 		}		
 		
 	
 	/**
 	 * Get the model id's for any model has a keyword match in the env factor
 	 * 
 	 * @param inKeyword
 	 *            the name to look for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForAnyEnvironmentalFactor(String inKeyword)
 			throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ce.abs_cancer_model_id FROM carcinogen_exposure ce "
 			+ "WHERE ce.environmental_factor_id IN (SELECT ef.environmental_factor_id FROM carcinogen_exposure ce, environmental_factor ef"
 			+ "     WHERE ce.environmental_factor_id = ef.environmental_factor_id AND upper(ef.name) LIKE ? OR upper(ef.name_altern_entry) LIKE ? )";
 		
 
 		Object[] theParams = new Object[2];
 		theParams[0] = "%"+inKeyword.toUpperCase()+ "%";
 		theParams[1] = "%"+inKeyword.toUpperCase()+ "%";		
 		return getIds(theSQLString, theParams);
 	}
 
     public List searchForAnimalModels(SearchData inSearchData) throws Exception
     {
 
         log.info("Entering searchForAnimalModels");
 
         List theAnimalModels = null;
 
         String theFromClause = "from AnimalModel as am where am.state = 'Edited-approved' AND am.availability.releaseDate < sysdate ";
         String theOrderByClause = " ORDER BY am.modelDescriptor asc";
 
         if (inSearchData.getKeyword() != null && inSearchData.getKeyword().length() > 0)
         {
             log.info("Doing a keyword search: " + inSearchData.getKeyword());
             theAnimalModels = keywordSearch(theFromClause, theOrderByClause, inSearchData.getKeyword());
         }
         else
         {
             log.info("Doing a criteria search");
             theAnimalModels = criteriaSearch(theFromClause, theOrderByClause, inSearchData);
         }
         log.info("Exiting searchForAnimalModels");
 
         return theAnimalModels;
     }
     
     public List searchForAdminAnimalModels(CurationAssignmentData inCurationAssignmentData) throws Exception
     {
 
         log.debug("Entering searchForAdminAnimalModels");
 
         List theAnimalModels = null;
 
         String theFromClause = "from AnimalModel as am where am.modelDescriptor IS NOT NULL ";
         String theOrderByClause = " ORDER BY am.modelDescriptor asc";
 
         if (inCurationAssignmentData.getModelId() != null && inCurationAssignmentData.getModelId().length() > 0)
         {
             log.debug("Doing a model id search: " + inCurationAssignmentData.getModelId());
             theAnimalModels = adminModelIdSearch(theFromClause, inCurationAssignmentData);
         }
         else
         {
             log.debug("Doing a criteria admin search");
             theAnimalModels = criteriaAdminSearch(theFromClause, theOrderByClause, inCurationAssignmentData);
         }
         log.debug("theAnimalModels.size() from QM.searchForAdminAnimalModels: " + theAnimalModels.size());
         log.debug("Exiting searchForAdminAnimalModels");
 
         return theAnimalModels;
     }    
 
     public int countMatchingAnimalModels(SearchData inSearchData) throws Exception
     {
 
         log.debug("Entering countMatchingAnimalModels");
 
         String theFromClause = "select count (am) from AnimalModel as am where am.state = 'Edited-approved' AND am.availability.releaseDate < sysdate ";
         List theCountResults = null;
 
         if (inSearchData.getKeyword() != null && inSearchData.getKeyword().trim().length() > 0)
         {
             log.debug("Doing a keyword search: " + inSearchData.getKeyword());
             String theWhereClause = buildKeywordSearchWhereClause(inSearchData.getKeyword());
 
             try
             {
                 String theHQLQuery = theFromClause + theWhereClause;
 
                 log.debug("HQL Query: " + theHQLQuery);
 
                 String theKeyword = "%" + inSearchData.getKeyword().toUpperCase().trim() + "%";
                 Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
                 theQuery.setParameter("keyword", theKeyword);
 
                 theCountResults = theQuery.list();
 
             }
             catch (Exception e)
             {
                 log.error("Exception occurred searching for models", e);
                 throw e;
             }
         }
         else
         {
             log.debug("Doing a criteria search");
             String theWhereClause = buildCriteriaSearchWhereClause(inSearchData);
 
             try
             {
                 String theHQLQuery = theFromClause + theWhereClause;
 
                 log.debug("HQL Query: " + theHQLQuery);
 
                 Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
                 theCountResults = theQuery.list();
 
             }
             catch (Exception e)
             {
                 log.error("Exception occurred searching for models", e);
                 throw e;
             }
         }
 
         int theCount = -1;
         if (theCountResults != null && theCountResults.size() > 0)
         {
             Integer theInt = (Integer) theCountResults.get(0);
             theCount = theInt.intValue();
         }
         log.debug("Exiting searchForAnimalModels");
 
         return theCount;
     }
 
     // Build the where clause for the search and the count
     private String buildKeywordSearchWhereClause(String inKeyword) throws Exception
     {
         String theKeyword = "%" + inKeyword.toUpperCase().trim() + "%";
 
         String theWhereClause = "";
 
         theWhereClause += " AND (upper(am.modelDescriptor) like :keyword ";
 
         theWhereClause += " OR am.strain IN (" + getStrainIdsForSpecies(inKeyword) + ")";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForHistopathologyOrgan(theKeyword, "") + ")";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForHistopathologyDisease(theKeyword, "") + ")";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForAnyEnvironmentalFactor(inKeyword) + ")";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForAnyEngineeredGene(inKeyword) + ")";
 
         theWhereClause += " OR am.phenotype IN (from Phenotype as p where upper(p.description) like :keyword )";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForCellLine(inKeyword) + ")";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForTherapeuticApproach(inKeyword) + "))";
 
         theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForTransientInterference(inKeyword) + "))";
 
         log.debug("KeyWordSearchWhereClause: " + theWhereClause.toString());
         return theWhereClause;
     }
 
     private List keywordSearch(String inFromClause,
                                String inOrderByClause,
                                String inKeyword) throws Exception
     {
 
         String theWhereClause = buildKeywordSearchWhereClause(inKeyword);
 
         List theAnimalModels = null;
 
         try
         {
             String theHQLQuery = inFromClause + theWhereClause + inOrderByClause;
 
             log.debug("<keywordSearch> HQL Query: " + theHQLQuery);
 
             String theKeyword = "%" + inKeyword.toUpperCase() + "%";
             Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theQuery.setParameter("keyword", theKeyword);
 
             theAnimalModels = theQuery.list();
         }
         catch (Exception e)
         {
             log.error("Exception occurred searching for models", e);
             throw e;
         }
 
         return theAnimalModels;
     }
     
     private List adminModelIdSearch(String inFromClause, CurationAssignmentData inCurationAssignmentData) throws Exception
      {
 
         log.debug("adminModelIdSearch Entered");
         
         String theWhereClause = buildAdminModelIdSearchWhereClause(inCurationAssignmentData);
         log.debug("theWhereClause: " + theWhereClause);
 
         List theAnimalModels = null;
 
         try
         {
             String theHQLQuery = inFromClause + theWhereClause;
 
             log.debug("HQL Query: " + theHQLQuery);
 
             Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theAnimalModels = theQuery.list();
         }
         catch (Exception e)
         {
             log.error("Exception occurred searching for models", e);
             throw e;
         }
 
         return theAnimalModels;     
      }
 
     private String buildCriteriaSearchWhereClause(SearchData inSearchData) throws Exception
     {
         String theWhereClause = "";
 
         // PI criteria
         if (inSearchData.getPiName() != null && inSearchData.getPiName().length() > 0)
         {
 
             StringTokenizer theTokenizer = new StringTokenizer(inSearchData.getPiName());
             String theLastName = theTokenizer.nextToken(",").trim();
             String theFirstName = theTokenizer.nextToken().trim();
 
             theWhereClause += " AND am.principalInvestigator IN (from Person as p where p.lastName like '%" + theLastName + "%' AND p.firstName like '%" + theFirstName + "%')";
         }
 
 		// Model descriptor criteria
 		if (inSearchData.getModelDescriptor() != null
 				&& inSearchData.getModelDescriptor().trim().length() > 0) {
 
             theWhereClause += " AND upper(am.modelDescriptor) like '%" + inSearchData.getModelDescriptor().toUpperCase().trim() + "%'";
         }
 
         // Species criteria
         if (inSearchData.getSpecies() != null && inSearchData.getSpecies().length() > 0)
         {
 
             theWhereClause += " AND am.strain IN (" + getStrainIdsForSpecies(inSearchData.getSpecies()) + ")";
         }
 
         // Search for organ
         if (inSearchData.getOrganTissueCode() != null && inSearchData.getOrganTissueCode().length() > 0 || inSearchData.getOrgan()!= null && inSearchData.getOrgan().length() > 0)
         {
             theWhereClause += " AND abs_cancer_model_id IN (" + getModelIdsForHistopathologyOrgan(inSearchData.getOrganTissueCode(),inSearchData.getOrgan() ) + ")";
         }
 
 		// Search for disease
 		if (inSearchData.getDiagnosisCode() != null
 				&& inSearchData.getDiagnosisCode().trim().length() > 0
 				|| inSearchData.getTumorClassification() != null
 				&& inSearchData.getTumorClassification().length() > 0) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForHistopathologyDisease(inSearchData
 							.getDiagnosisCode(), inSearchData
 							.getTumorClassification()) + ")";
 		}
 		
 		// Search for external source
 		if (inSearchData.getExternalSource() != null
 				&& inSearchData.getExternalSource().length() > 0) {
 			log.debug("In Search inSearchData.getExternalSource: "
 					+ inSearchData.getExternalSource());
 			theWhereClause += " AND upper(am.externalSource) like '%"
 					+ inSearchData.getExternalSource().toUpperCase().trim()
 					+ "%'";
 		}		
 
 		// ///////////////////////////////////////
 		// Carcinogenic interventions
 		// ///////////////////////////////////////
 
 		if (inSearchData.getCarcinogenicIntervention() != null
 				&& inSearchData.getCarcinogenicIntervention().trim().length() > 0) {
 			
 			if (inSearchData.getAgentName() != null
 					&& inSearchData.getAgentName().trim().length() > 0) {
 				log.debug("<QueryManagerImpl> Searching for Carcinogenic Interventions by agent type and name  - selected by user");
 			// Search for a CE by agent type
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor(inSearchData.getCarcinogenicIntervention(),
 								inSearchData.getAgentName().trim()) + ")";
 			} else {
 				log.debug("<QueryManagerImpl> Searching for Carcinogenic Interventions by agent type only - selected by user");
 				// Search for any model w/ an agent type 
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForAnEnvironmentalFactorByType(inSearchData.getCarcinogenicIntervention()) + ")";
 			}
 		}
 
 		// call if any of the data is set for Genetic Desription section
 		if (inSearchData.isSearchEngineeredTransgene()
 				|| inSearchData.isSearchTargetedModification()
 				|| (inSearchData.getGenomicSegDesignator() != null && inSearchData
 						.getGenomicSegDesignator().trim().length() > 0)
 				|| (inSearchData.getInducedMutationAgent() != null && inSearchData
 						.getInducedMutationAgent().trim().length() > 0)) {
 
 			// Search for engineered genes
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForEngineeredGenes(inSearchData.getGeneName(), inSearchData.getTransgeneName(),
 							inSearchData.isSearchEngineeredTransgene(), inSearchData
 									.isSearchTargetedModification(), inSearchData
 									.getGenomicSegDesignator(), inSearchData
 									.getInducedMutationAgent()) + ")";
 		}
 
 		// Search for phenotype
 		if (inSearchData.getPhenotype() != null
 				&& inSearchData.getPhenotype().trim().length() > 0) {
 			theWhereClause += " AND am.phenotype IN (from Phenotype as p where upper(p.description) like '%"
 					+ inSearchData.getPhenotype().trim().toUpperCase() + "%')";
 		}
 
 		// Search for cellline
 		if (inSearchData.getCellLine() != null
 				&& inSearchData.getCellLine().trim().length() > 0) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForCellLine(inSearchData.getCellLine().trim())
 					+ ")";
 		}
 
 		// Search for therapeutic approaches
 		if (inSearchData.isSearchTherapeuticApproaches()) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForTherapeuticApproach(inSearchData
 							.getTherapeuticApproach().trim()) + ")";
 		}
 
 		// Search for metastasis
 		if (inSearchData.isSearchHistoMetastasis()) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForHistoMetastasis() + ")";
 		}
 
 		// Search for microarray data
 		if (inSearchData.isSearchMicroArrayData()) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForMicroArrayData() + ")";
 		}
 
 		// Search for image data
 		if (inSearchData.isSearchImageData()) {
 			log.debug("In theWhereClause for image data");
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForImageData() + ")";
 		}
 
 		// Search for Transient Interference
 		if (inSearchData.isSearchTransientInterference()) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForTransientInterference() + ")";
 		}
 
 		// Search for Transplant
 		if (inSearchData.isSearchTransplant()) {
 			log.debug("In Search inSearchData.isSearchTransplant(): "
 					+ inSearchData.isSearchTransplant());
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForTransplant() + ")";
 		}
 		
 		// Search for Transient Interference
 		if (inSearchData.isSearchTransientInterference()) {
 			log.debug("In Search inSearchData.isSearchTransientInterference(): "
 					+ inSearchData.isSearchTransientInterference());
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForTransientInterference() + ")";
 		}
 
 		// Search for tool strains
 		if (inSearchData.isSearchToolStrain()) {
 			log.debug("In Search inSearchData.isSearchToolStrain(): "
 					+ inSearchData.isSearchToolStrain());
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForToolStrain() + ")";
 		}		
 		log.info("QueryMgrImpl theWhereClause: " + theWhereClause.toString());
         return theWhereClause;
 
     }
 
     private String buildAdminCriteriaSearchWhereClause(CurationAssignmentData inCurationAssignmentData) throws Exception
     {
     	String theWhereClause = "";
         log.debug("<buildAdminCriteriaSearchWhereClause> following Characteristics:" 
         		+ "\n\t  State: " + inCurationAssignmentData.getCurrentState()
                 + "\n\t Editor: " + inCurationAssignmentData.getEditor()         		
                 + "\n\t External Source: " + inCurationAssignmentData.getExternalSource() 
                 + "\n\t Model Descriptor: " + inCurationAssignmentData.getModelDescriptor() 
                 + "\n\t PI: " + inCurationAssignmentData.getPrincipalInvestigator() 
                 + "\n\t Species: " + inCurationAssignmentData.getSpecies()                
                 + "\n\t Screener: " + inCurationAssignmentData.getScreener());    	
   	
 
         // Current State criteria
         if (inCurationAssignmentData.getCurrentState() != null && inCurationAssignmentData.getCurrentState().length() > 0)
         {
         	log.debug("inCurationAssignmentData.getCurrentState() != null loop");
             theWhereClause += " AND upper(am.state) like '%" + inCurationAssignmentData.getCurrentState().toUpperCase().trim() + "%'";
         }   	
     	
 		// External source criteria - sets where clause for null (caMOD) models
 		if (inCurationAssignmentData.getExternalSource() != null && inCurationAssignmentData.getExternalSource().length() > 0) {
 			log.debug("inCurationAssignmentData.getExternalSource() != null loop " );
 			theWhereClause += " AND upper(am.externalSource) like '%" + inCurationAssignmentData.getExternalSource().toUpperCase().trim()+ "%'";
 		} else {
 			log.debug("inCurationAssignmentData.getExternalSource() else loop " );
 			theWhereClause += " AND upper(am.externalSource) IS NULL ";
 		}
 		
 		// Model descriptor criteria
 		if (inCurationAssignmentData.getModelDescriptor() != null
 				&& inCurationAssignmentData.getModelDescriptor().trim().length() > 0) 
 		{
 			log.debug("inCurationAssignmentData.getModelDescriptor() != null loop " );
             theWhereClause += " AND upper(am.modelDescriptor) like '%" + inCurationAssignmentData.getModelDescriptor().toUpperCase().trim() + "%'";
         }
 		
         // Species criteria
         if (inCurationAssignmentData.getSpecies() != null && inCurationAssignmentData.getSpecies().length() > 0)
         {
             theWhereClause += " AND am.strain IN (" + getStrainIdsForSpecies(inCurationAssignmentData.getSpecies()) + ")";
         }		
 
         // PI criteria
         if (inCurationAssignmentData.getPrincipalInvestigator() != null && inCurationAssignmentData.getPrincipalInvestigator().length() > 0)
         {
 
             StringTokenizer theTokenizer = new StringTokenizer(inCurationAssignmentData.getPrincipalInvestigator());
             String theLastName = theTokenizer.nextToken(",").trim();
             String theFirstName = theTokenizer.nextToken().trim();
 
             theWhereClause += " AND am.principalInvestigator IN (from Person as p where p.lastName like '%" + theLastName + "%' AND p.firstName like '%" + theFirstName + "%')";
         }        
 		
 		// Editor criteria
         if (inCurationAssignmentData.getEditor() != null && inCurationAssignmentData.getEditor().length() > 0)
         {
             StringTokenizer theTokenizer = new StringTokenizer(inCurationAssignmentData.getEditor());
             String theLastName = theTokenizer.nextToken(",").trim();
             String theFirstName = theTokenizer.nextToken().trim();
             
             theWhereClause += " " + "AND am.id in (select l.abstractCancerModel from Log as l where l.state = 'Editor-assigned'  AND l.submitter IN (from Person as p where p.lastName like '%" + theLastName + "%' AND p.firstName like '%" + theFirstName + "%'))";
         }        
 		
 		// Screener criteria
         if (inCurationAssignmentData.getScreener() != null && inCurationAssignmentData.getScreener().length() > 0)
         {
             StringTokenizer theTokenizer = new StringTokenizer(inCurationAssignmentData.getScreener());
             String theLastName = theTokenizer.nextToken(",").trim();
             String theFirstName = theTokenizer.nextToken().trim();
             
            theWhereClause += " " + "AND am.id in (select l.abstractCancerModel from Log as l where l.state = 'Screener-assigned'  AND l.submitter IN (from Person as p where p.lastName like '%" + theLastName + "%' AND p.firstName like '%" + theFirstName + "%' ))";
         }       
         
         // Species criteria
         if (inCurationAssignmentData.getSpecies() != null && inCurationAssignmentData.getSpecies().length() > 0)
         {
 
             theWhereClause += " AND am.strain IN (" + getStrainIdsForSpecies(inCurationAssignmentData.getSpecies()) + ")";
         }
 		
 		log.debug("buildAdminCriteriaSearchWhereClause theWhereClause: " + theWhereClause.toString());
         return theWhereClause;
 
     }
     
 
     private String buildAdminModelIdSearchWhereClause(CurationAssignmentData inCurationAssignmentData) throws Exception
     {
     	String theWhereClause = "";
         log.debug("<buildAdminCriteriaSearchWhereClause> following Characteristics:" 
         		+ "\n\t  ModelId: " + inCurationAssignmentData.getModelId()); 
         
         // ModelId criteria
         if (inCurationAssignmentData.getModelId() != null && inCurationAssignmentData.getModelId().length() > 0)
         {
         	log.debug("inCurationAssignmentData.getCurrentState() != null loop");
         	theWhereClause += " " + "AND am.id = " + inCurationAssignmentData.getModelId().trim();
         }        
         
 		log.debug("buildAdminModelIdSearchWhereClause theWhereClause: " + theWhereClause.toString());
         return theWhereClause;
 
     }       
     
     private List criteriaSearch(String inFromClause,
                                 String inOrderByClause,
                                 SearchData inSearchData) throws Exception
     {
         String theWhereClause = buildCriteriaSearchWhereClause(inSearchData);
         log.info("theWhereClause: " + theWhereClause);
         List theAnimalModels = null;
 
         try
         {
             String theHQLQuery = inFromClause + theWhereClause + inOrderByClause;
 
             log.info("HQL Query: " + theHQLQuery);
 
             Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theAnimalModels = theQuery.list();
         }
         catch (Exception e)
         {
             log.error("Exception occurred searching for models", e);
             throw e;
         }
 
         return theAnimalModels;
     }
     
     private List criteriaAdminSearch(String inFromClause,
     								 String inOrderByClause,
     								 CurationAssignmentData inCurationAssignmentData) throws Exception
      {
         log.debug("criteriaAdminSearch Entered");
         
         String theWhereClause = buildAdminCriteriaSearchWhereClause(inCurationAssignmentData);
         log.debug("theWhereClause: " + theWhereClause);
 
         List theAnimalModels = null;
 
         try
         {
             String theHQLQuery = inFromClause + theWhereClause + inOrderByClause;
 
             log.debug("HQL Query: " + theHQLQuery);
 
             Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theAnimalModels = theQuery.list();
         }
         catch (Exception e)
         {
             log.error("Exception occurred searching for models", e);
             throw e;
         }
 
         return theAnimalModels;    	
      }
 
     /**
      * Extract the ID's for an sql query.
      * 
      * @param inSQLString
      *            the SQL string that returns a set of IDs
      * @param inParameters
      *            the parameters to bind in the query
      * 
      * @return a list of matching ids
      * 
      * @throws PersistenceException
      */
     private String getIds(String inSQLString,
                           Object inParameters[]) throws PersistenceException
     {
 
         log.debug("In getIds");
 
         String theModelIds = "";
 
         ResultSet theResultSet = null;
         try
         {
 
             log.debug("getIds - SQL: " + inSQLString);
 
             theResultSet = Search.query(inSQLString, inParameters);
 
             if (theResultSet.next()) {
                 theModelIds += theResultSet.getString(1);
             }
             int elementCount = 0;
             while (theResultSet.next() && elementCount < 999)
             {
                 theModelIds += "," + theResultSet.getString(1);
                 elementCount++;
             }
 
         }
         catch (Exception e)
         {
             log.error("Exception in getIds", e);
             throw new PersistenceException("Exception in getIds: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
 
         if (theModelIds.equals(""))
         {
             theModelIds = "-1";
         }
         return theModelIds;
     }
 
 
     //no nscNumber in environmental_factor to test if this is correct
     public List getModelsForThisCompound(Long nscNumber) throws PersistenceException
     {
         List<String[]> models = new ArrayList<String[]>();
         int cc = 0;
         ResultSet theResultSet = null;
         try
         {
             String theSQLString = "select acm.abs_cancer_model_id, " + "\n" 
             + "       acm.model_descriptor," + "\n"
             + "       sp.common_name," + "\n"
             + "       sp.common_name_altern_entry," + "\n"
             + "       st.name," + "\n"
             + "       st.name_altern_entry" + "\n"            
             + "  from abs_cancer_model acm," + "\n" 
             + "       therapy t,"   + "\n" 
             + "       strain st,"   + "\n" 
             + "       species sp,"   + "\n"             
             + "       agent a" + "\n" 
             + " where acm.abs_cancer_model_id = t.abs_cancer_model_id" + "\n" 
             + "   and acm.abs_cancer_model_type = 'AM'" + "\n"
             + "   and acm.state = 'Edited-approved'" + "\n"            
             + "   and t.agent_id = a.agent_id" + "\n" 
             + "   and acm.strain_id = st.strain_id" + "\n" 
             + "   and st.species_id = sp.species_id" + "\n"             
             + "   and a.nsc_number = ?";
 
             log.debug("\n getModelsForThisCompound - SQL: " + theSQLString);
 
             Object[] params = new Object[1];
             params[0] = nscNumber;
             theResultSet = Search.query(theSQLString, params);
             String speciesName = "";
             String stainName = "";
             while (theResultSet.next())
             {
                 String[] item = new String[3];
                 item[0] = theResultSet.getString(1); // the id
                 item[1] = theResultSet.getString(2); // model descriptor
                 
                 String theSpeciesName = theResultSet.getString(3);  // species common name
                 String theSpeciesUnctrlName = theResultSet.getString(4);
                 String theStrainName = theResultSet.getString(5);  // strain name
                 String theStrainUnctrlName = theResultSet.getString(6); 
                 
                 if (theSpeciesName != null && theSpeciesName.length() > 0 )
                 {
                 	speciesName = theResultSet.getString(3); // species common name
                 }
                 else if (theSpeciesUnctrlName != null && theSpeciesUnctrlName.length() > 0 )
                 {
                 	speciesName = theResultSet.getString(4); // species Unctrl common name
                 }                
                 if (theStrainName != null && theStrainName.length() > 0 )
                 {
                 	stainName = theResultSet.getString(5); // strain name
                 }
                 else if (theStrainUnctrlName != null && theStrainUnctrlName.length() > 0 )
                 {
                 	stainName = theResultSet.getString(6); // strain Unctrl name
                 }                
                 
                 item[2] = speciesName + "  -  " + stainName; // result of column
                 models.add(item);
                 cc++;
             }
             log.debug("Got " + cc + " animal models");
         }
         catch (Exception e)
         {
             log.error("Exception in getModelsForThisCompound", e);
             throw new PersistenceException("Exception in getModelsForThisCompound: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
         return models;
     }
 
     public List getAllPublications(long absCancerModelId) throws PersistenceException
     {
         List<Publication> publications = new ArrayList<Publication>();
         int cc = 0;
         ResultSet theResultSet = null;
         List<String> thePids = new ArrayList<String>();
         
         try
         {
             String theSQLString = "select publication_id, year, authors" + "\n" + "  from publication" + "\n" + "  where publication_id in (" + "\n" + "	  select min(publication_id) publication_id" + "\n" + "	  from (" + "\n" + "		select p.pmid, p.publication_id" + "\n" + "		  from therapy th," + "\n" + "		       therapy_publication tp," + "\n" + "		       publication p" + "\n" + "		  where th.abs_cancer_model_id = ?" + "\n" + "		   and th.therapy_id = tp.therapy_id" + "\n" + "		   and tp.publication_id = p.publication_id" + "\n" + "		union" + "\n" + "		select p.pmid, p.publication_id" + "\n" + "		  from cell_line cl," + "\n" + "		       cell_line_publication cp," + "\n" + "		       publication p" + "\n" + "		 where cl.abs_cancer_model_id = ?" + "\n" + "		   and cl.cell_line_id = cp.cell_line_id" + "\n" + "		   and cp.publication_id = p.publication_id" + "\n" + "		union" + "\n" + "		select p.pmid, p.publication_id" + "\n" + "		  from abs_can_mod_publication acmp," + "\n" + "		       publication p" + "\n" + "		 where acmp.abs_cancer_model_id = ?" + "\n" + "		   and acmp.publication_id = p.publication_id )" + "\n" + "	 group by pmid )" + "\n" + " order by year desc, authors" + "\n";
 
             log.debug("getAllPublications - SQL: " + theSQLString);
             Object[] params = new Object[3];
             params[0] = params[1] = params[2] = String.valueOf(absCancerModelId);
             theResultSet = Search.query(theSQLString, params);
             
             // Rewrote this method during the upgrade to Hibernate
             // Had to copy ResultSet to a list since the ResultSet was closed before the 
             // while loop was able to get all the Species objects from the ManagerSingleton
             while (theResultSet.next())
             {
                 try {
                     thePids.add(theResultSet.getString(1));
                 } catch (Exception e)  {
                     log.error("Exception in getApprovedSpecies", e);
                     throw new PersistenceException("Exception in getApprovedSpecies: " + e);
                 }
             }                
 
             for (int i = 0; i < thePids.size(); i++)
             {
                 String pid = thePids.get(i); // the publication_id
                 Publication p = (Publication) get(pid, Publication.class);
                 publications.add(p);
                 cc++;     
             }            
             
 
             log.debug("Got " + cc + " publications");
         }
         catch (Exception e)
         {
             log.error("Exception in getAllPublications", e);
             throw new PersistenceException("Exception in getAllPublications: " + e);
         }
         finally
         {
             if (theResultSet != null)
             {
                 try
                 {
                     Statement stmt = theResultSet.getStatement();
                     theResultSet.close();
                     stmt.close();
                 }
                 catch (Exception e)
                 {}
             }
         }
         return publications;
     }
 
 
     public List getSavedQueriesByParty(String inUsername) throws PersistenceException
     {
 
         log.trace("Entering QueryManagerImpl.getModelsByUser");
 
         String theHQLQuery = "from SavedQuery as sq where " + " sq.user in (from Person where username = :username) " + " and sq.isSaved = :savedValue " + " order by query_name";
 
         log.debug("The HQL query: " + theHQLQuery);
 
         org.hibernate.Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
         theQuery.setParameter("username", inUsername);
        theQuery.setParameter("savedValue", "1");
 
         List theSavedQueries = theQuery.list();
 
         if (theSavedQueries == null)
         {
             theSavedQueries = new ArrayList();
         }
         return theSavedQueries;
     }
 
     public List getQueriesByParty(String inUsername) throws PersistenceException
     {
 
         log.trace("Entering QueryManagerImpl.getModelsByUser");
 
         String theHQLQuery = "from SavedQuery as sq where " + " sq.user in (from Person where username = :username) " + " and sq.isSaved = :savedValue " + " order by query_execute_timestamp DESC";
 
         log.debug("The HQL query: " + theHQLQuery);
 
         org.hibernate.Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
         theQuery.setParameter("username", inUsername);
        theQuery.setParameter("savedValue", "0");
 
         List theSavedQueries = theQuery.list();
 
         if (theSavedQueries == null)
         {
             theSavedQueries = new ArrayList();
         }
         return theSavedQueries;
     }
 
     public List getResultSettingsByUsername(String inUsername) throws PersistenceException
     {
         log.trace("Entering QueryManagerImpl.getResultSettingsByUsername");
 
         String theHQLQuery = "from ResultSettings as sq where sq.user in (from Person where username = :username) ";
 
         org.hibernate.Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
         theQuery.setParameter("username", inUsername);
 
         List theResultSettings = theQuery.list();
 
 		return theResultSettings;
 	}
 	
 	
 	/**
 	 * Return the list of External Source names
 	 * 
 	 * @return a sorted list of unique eExternal Sources
 	 * @throws PersistenceException
 	 */
 	public List getExternalSources() throws PersistenceException {
 		
 		log.debug("Entering QueryManagerImpl.getExternalSources");
 
 		// Format the query
 		HQLParameter[] theParams = new HQLParameter[0];
 
 		String theHQLQuery = "select distinct(am.externalSource) FROM AnimalModel as am WHERE external_source is not null";
 
 		List theList = Search.query(theHQLQuery, theParams);
 
 		log.debug("Found matching items: " + theList.size());
 
 		log.debug("Exiting QueryManagerImpl.getExternalSources");
 		return theList;
 	}
 	
 	/**
 	 * Get the model id's for any model that is from an external source
 	 * 
 	 * @param inExternalSource
 	 *            the externalSource to search for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException )
 	 */
 	 private String getModelIdsForExternalSource(String inExternalSource)
 	 throws PersistenceException {
 	  
 	  log.debug("Entering QueryManagerImpl.getModelIdsExternalSource Enter");
 	  
 	  String theSQLString = "SELECT distinct abs_cancer_model_id FROM abs_cancer_model WHERE upper(acm.external_source) like ?";
 	  
 	  
 	  Object[] theParams = new Object[2]; theParams[0] = inExternalSource;
 	  theParams[1] = theParams[0];
 	  
 	  log.debug("The params: " + theParams[0]); 
       return
 	  getIds(theSQLString, theParams);
 	  
 	  }	
 	
 	/**
 	 * Get the model id's for any model that is indicated as a tool strain
 	 * 
 	 * @return a list of matching model ids
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForToolStrain() throws PersistenceException {
 
 		String theSQLString = "SELECT distinct abs_cancer_model_id FROM abs_cancer_model "
 				+ "WHERE is_tool_strain = 1 "
 				+ " AND state = 'Edited-approved'";
 
 		Object[] theParams = new Object[0];
 		return getIds(theSQLString, theParams);
 
 	}
 	
     /**
      * Get the model id's for any model that has associated image data
      * 
      * @return a list of matching model ids
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForImageData() throws PersistenceException
     {
     	log.debug("In getModelIdsForImageData");
         String theSQLString = "SELECT distinct abs_cancer_model_id FROM image";             
 
         Object[] theParams = new Object[0];
         return getIds(theSQLString, theParams);
 
     }	
 
 	/**
 	 * Static method to copy an existing List to another List (collection type)
 	 * 
 	 */	
 	static <T> void fromArrayToCollection(T[] a, Collection<T> c) {
 	    for (T o : a) {
 	        c.add(o); // Correct
 	    }
 	}
 	
 	public static void main(String[] inArgs) {
 		try {
 			List theList = QueryManagerSingleton.instance()
 					.getMatchingTumorClassifications("a");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
