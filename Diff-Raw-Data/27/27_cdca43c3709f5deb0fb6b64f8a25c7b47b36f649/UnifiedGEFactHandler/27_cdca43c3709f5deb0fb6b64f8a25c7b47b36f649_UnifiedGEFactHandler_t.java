 package gov.nih.nci.rembrandt.queryservice.queryprocessing.ge;
 
 import gov.nih.nci.rembrandt.dbbean.*;
 import gov.nih.nci.rembrandt.dto.query.GeneExpressionQuery;
 import gov.nih.nci.rembrandt.dto.query.UnifiedGeneExpressionQuery;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.AllGenesCritValidator;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.CommonFactHandler;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.DBEvent;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.QueryHandler;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ThreadController;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 import gov.nih.nci.rembrandt.util.ThreadPool;
 import gov.nih.nci.caintegrator.dto.critieria.GeneIDCriteria;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.Query;
 import org.apache.ojb.broker.query.QueryFactory;
 
 /**
  * @author BhattarR
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 abstract public class UnifiedGEFactHandler {
     private static Logger logger = Logger.getLogger(UnifiedGEFactHandler.class);
     Map geneExprObjects = Collections.synchronizedMap(new HashMap());
     private final static int VALUES_PER_THREAD = 50;
     abstract void addToResults(Collection results);
 
     abstract ResultSet[] executeFactQuery(UnifiedGeneExpressionQuery query) throws Exception;
 
     protected void executeQuery(final Class targetFactClass, String geneSymbolAttr, UnifiedGeneExpressionQuery geQuery ) throws Exception {
                 GeneIDCriteria geneIDCrit = geQuery.getGeneIDCrit();
                 final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                 pb.clearCache();
 
                 // 1. add Gene ID (symbol) criteria
                 ArrayList arrayIDs = GeneIDCriteriaHandler.getGeneIDValues(geneIDCrit);
                 Criteria c = new Criteria();
                 c.addIn(geneSymbolAttr, arrayIDs);
 
                 // 2. add Institution criteria
                 CommonFactHandler.addAccessCriteria(geQuery, targetFactClass, c);
 
                 Query factQuery = QueryFactory.newQuery(targetFactClass,c, false);
                 assert(factQuery  != null);
                 Collection exprObjects =  pb.getCollectionByQuery(factQuery);
                 addToResults(exprObjects);
                 pb.close();
     }
 
     public final static class SingleHandler extends UnifiedGEFactHandler {
 
         public ResultSet[] executeFactQuery(UnifiedGeneExpressionQuery query) throws Exception {
 
             // 1. first execute against fact table
             executeQuery(DiffExpressionGeneSFact.class, DiffExpressionGeneSFact.GENE_SYMBOL, query);
 
             // 2. format it to results (by now geneExprObjects must have been populated with facts
             UnifiedGeneExpr.UnifiedGeneExprSingle[] results = new UnifiedGeneExpr.UnifiedGeneExprSingle[geneExprObjects.size()];
             Collection facts = geneExprObjects.values();
             int count = 0;
             for (Iterator iterator = facts.iterator(); iterator.hasNext();) {
                 UnifiedGeneExpr.UnifiedGeneExprSingle obj = (UnifiedGeneExpr.UnifiedGeneExprSingle) iterator.next();
                 results[count++] = obj;
             }
 
             return results;
         }
 
         void addToResults(Collection exprObjects) {
             for (Iterator iterator = exprObjects.iterator(); iterator.hasNext();) {
                 DiffExpressionGeneSFact exprObj = (DiffExpressionGeneSFact) iterator.next();
                 UnifiedGeneExpr.UnifiedGeneExprSingle singleExprObj = new UnifiedGeneExpr.UnifiedGeneExprSingle();
                 copyTo(singleExprObj, exprObj);
                 geneExprObjects.put(singleExprObj.getID(), singleExprObj);
                 exprObj = null;
             }
         }
         public static void copyTo(UnifiedGeneExpr.UnifiedGeneExprSingle singleExprObj, DiffExpressionGeneSFact exprObj) {
             singleExprObj.setExpressionRatio(exprObj.getExpressionRatio());
             singleExprObj.setGeneSymbol(exprObj.getGeneSymbol());
             singleExprObj.setID(exprObj.getDegsId());
             singleExprObj.setNormalIntensity(exprObj.getNormalIntensity());
             singleExprObj.setSampleId(exprObj.getSampleId());
             singleExprObj.setBiospecimenId(exprObj.getBiospecimenId());
             singleExprObj.setSampleIntensity(exprObj.getSampleIntensity());
             singleExprObj.setUnifiedGeneID(exprObj.getUnifiedGene());
             singleExprObj.setDiseaseType(exprObj.getDiseaseType());
         }
     }
     public final static class GroupHandler extends UnifiedGEFactHandler {
 
         public ResultSet[] executeFactQuery(UnifiedGeneExpressionQuery query) throws Exception {
             // 1. first execute against fact table
             executeQuery(DiffExpressionGeneGFact.class, DiffExpressionGeneGFact.GENE_SYMBOL, query);
 
             // 2. format it to results (by now geneExprObjects must have been populated with facts
             UnifiedGeneExpr.UnifiedGeneExprGroup[] results = new UnifiedGeneExpr.UnifiedGeneExprGroup[geneExprObjects.size()];
             Collection facts = geneExprObjects.values();
             int count = 0;
             for (Iterator iterator = facts.iterator(); iterator.hasNext();) {
                 UnifiedGeneExpr.UnifiedGeneExprGroup obj = (UnifiedGeneExpr.UnifiedGeneExprGroup) iterator.next();
                 results[count++] = obj;
             }
 
             return results;
         }
 
         void addToResults(Collection exprObjects) {
             for (Iterator iterator = exprObjects.iterator(); iterator.hasNext();) {
                 DiffExpressionGeneGFact exprObj = (DiffExpressionGeneGFact) iterator.next();
                 UnifiedGeneExpr.UnifiedGeneExprGroup singleExprObj = new UnifiedGeneExpr.UnifiedGeneExprGroup();
                 copyTo(singleExprObj, exprObj);
                 geneExprObjects.put(singleExprObj.getID(), singleExprObj);
                 exprObj = null;
             }
         }
        public static void copyTo(UnifiedGeneExpr.UnifiedGeneExprGroup groupExprObj, DiffExpressionGeneGFact exprObj) {
        	groupExprObj.setExpressionRatio(exprObj.getExpressionRatio());
        	groupExprObj.setGeneSymbol(exprObj.getGeneSymbol());
        	groupExprObj.setID(exprObj.getDeggId());
        	groupExprObj.setNormalIntensity(exprObj.getNormalIntensity());
        	groupExprObj.setSampleIntensity(exprObj.getSampleIntensity());
        	groupExprObj.setUnifiedGeneID(exprObj.getUnifiedGene());
        	groupExprObj.setDiseaseType(exprObj.getDiseaseType());
        	groupExprObj.setRatioPval(exprObj.getRatioPval());
        	groupExprObj.setStandardDeviation(exprObj.getRatioStd());
         }
     }
 }
 
