 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.external.cadsr;
 
 import gov.nih.nci.cadsr.domain.AdministeredComponent;
 import gov.nih.nci.cadsr.domain.EnumeratedValueDomain;
 import gov.nih.nci.cadsr.domain.PermissibleValue;
 import gov.nih.nci.cadsr.domain.ValueDomainPermissibleValue;
 import gov.nih.nci.cadsr.freestylesearch.util.Search;
 import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
 import gov.nih.nci.cadsr.freestylesearch.util.SearchResults;
 import gov.nih.nci.caintegrator2.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.CommonDataElement;
 import gov.nih.nci.caintegrator2.domain.annotation.DatePermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.NumericPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.StringPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.ValueDomain;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 
 /**
  * Implements the CaDSRFacade interface.
  */
 @SuppressWarnings({ "PMD.CyclomaticComplexity" }) // See retrieveValueDomainForDataElement
 public class CaDSRFacadeImpl implements CaDSRFacade {
     
     
     private Search search;
     private String dataDescription;
     private CaDSRApplicationServiceFactory caDsrApplicationServiceFactory;
     private String caDsrUrl;
 
     /**
      * {@inheritDoc}
      */
     public List<CommonDataElement> retreiveCandidateDataElements(List<String> keywords) {
         
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < keywords.size(); ++i) {
             sb.append(keywords.get(i));
             if (i != keywords.size() - 1) {
                 sb.append(' ');
             }
           }
 
         String keywordsString = sb.toString();
         search.setDataDescription(dataDescription);
         // Restrict results to only Data Elements
         search.restrictResultsByType(SearchAC.DE);
         search.excludeWorkflowStatusRetired(true);
         search.excludeTest(true);
         search.excludeTraining(true);
         return convertSearchResultsToDataElements(search.findReturningSearchResults(keywordsString));
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({ "PMD.ExcessiveMethodLength", "PMD.CyclomaticComplexity" }) // Type checking and casting
     public ValueDomain retrieveValueDomainForDataElement(Long dataElementId) throws ConnectionException {
         ValueDomain valueDomain = new ValueDomain();
         try {
             // Won't have to provide a URL when it goes from stage to production.
             ApplicationService cadsrApi = caDsrApplicationServiceFactory.retrieveCaDsrApplicationService(caDsrUrl);

             gov.nih.nci.cadsr.domain.DataElement cadsrDataElement = new gov.nih.nci.cadsr.domain.DataElement();
             cadsrDataElement.setPublicID(dataElementId);
             List<Object> cadsrDataElements = cadsrApi.search(
                     gov.nih.nci.cadsr.domain.DataElement.class, cadsrDataElement);
             
             if (!cadsrDataElements.isEmpty()) {
                 gov.nih.nci.cadsr.domain.DataElement cadsrRetrievedDataElement = 
                     (gov.nih.nci.cadsr.domain.DataElement) cadsrDataElements.get(0);
                 gov.nih.nci.cadsr.domain.ValueDomain cadsrValueDomain = cadsrRetrievedDataElement.getValueDomain();
                 // Default will be String, Character, Alphanumeric, and Alpha DVG
                 AnnotationTypeEnum annotationType = AnnotationTypeEnum.STRING;
                 String cadsrDataType = cadsrValueDomain.getDatatypeName();
                 if (cadsrDataType.matches("DATE")) {
                     annotationType = AnnotationTypeEnum.DATE;
                 }
                 if (cadsrDataType.matches("NUMBER")) {
                     annotationType = AnnotationTypeEnum.NUMERIC;
                 }
                 valueDomain.setDataType(annotationType.getValue());
                 valueDomain.setLongName(cadsrValueDomain.getLongName());
                 valueDomain.setPublicID(cadsrValueDomain.getPublicID());
                 valueDomain.setValueDomainType(ValueDomainType.NON_ENUMERATED.getValue());
                 if (cadsrValueDomain instanceof EnumeratedValueDomain) {
                     valueDomain.setValueDomainType(ValueDomainType.ENUMERATED.getValue());
                     valueDomain.setPermissibleValueCollection(new HashSet<AbstractPermissibleValue>());
                     retrievePermissibleValues(valueDomain, cadsrValueDomain);
                 }
             }
             return valueDomain;
         } catch (Exception e) {
             throw new ConnectionException("Couldn't connect to the caDSR server", e);
         }
     }
 
     private void retrievePermissibleValues(ValueDomain valueDomain,
                                            gov.nih.nci.cadsr.domain.ValueDomain cadsrValueDomain) {
         EnumeratedValueDomain enumeratedValueDomain = (EnumeratedValueDomain) cadsrValueDomain;
         Collection<ValueDomainPermissibleValue> valueDomainPermissibleValues = 
                                 enumeratedValueDomain.getValueDomainPermissibleValueCollection();
         AnnotationTypeEnum annotationType = AnnotationTypeEnum.getByValue(valueDomain.getDataType());
         for (ValueDomainPermissibleValue valueDomainPermissibleValue : valueDomainPermissibleValues) {
             PermissibleValue cadsrPermissibleValue = valueDomainPermissibleValue.getPermissibleValue();
             String stringValue = cadsrPermissibleValue.getValue();
             AbstractPermissibleValue abstractValue = new AbstractPermissibleValue();
             if (AnnotationTypeEnum.STRING.equals(annotationType)) {
                 StringPermissibleValue stringPermissibleValue = new StringPermissibleValue();
                 stringPermissibleValue.setStringValue(stringValue);
                 abstractValue = stringPermissibleValue;
             } else if (AnnotationTypeEnum.NUMERIC.equals(annotationType)) {
                 abstractValue = retrieveNumericPermissibleValue(
                         cadsrPermissibleValue, stringValue);
             } else if (AnnotationTypeEnum.DATE.equals(annotationType)) {
                 // Larry Hebel mentioned that DATE doesn't have any caDSR instances of Permissible Values at this time.
                 DatePermissibleValue datePermissibleValue = new DatePermissibleValue();
                 abstractValue = datePermissibleValue;
             }
             if (cadsrPermissibleValue.getValueMeaning() != null) {
                 abstractValue.setDescription(cadsrPermissibleValue.getValueMeaning().getDescription());
             }
             valueDomain.getPermissibleValueCollection().add(abstractValue);
         }
     }
 
     private NumericPermissibleValue retrieveNumericPermissibleValue(PermissibleValue cadsrPermissibleValue,
             String stringValue) {
         NumericPermissibleValue numericPermissibleValue = new NumericPermissibleValue();
         numericPermissibleValue.setNumericValue(Double.valueOf(stringValue));
         if (cadsrPermissibleValue.getHighValueNumber() != null) {
             numericPermissibleValue.setHighValue(Double.valueOf(cadsrPermissibleValue.getHighValueNumber()));
         }
         if (cadsrPermissibleValue.getLowValueNumber() != null) {
             numericPermissibleValue.setLowValue(Double.valueOf(cadsrPermissibleValue.getLowValueNumber()));    
         }
         return numericPermissibleValue;
     }
     
     private List<CommonDataElement> convertSearchResultsToDataElements(List<SearchResults> searchResults) {
         List<CommonDataElement> dataElements = new ArrayList<CommonDataElement>();
         CommonDataElement de;
         for (SearchResults sr : searchResults) {
             de = new CommonDataElement();
             de.setDefinition(sr.getPreferredDefinition());
             de.setLongName(sr.getLongName());
             de.setPublicID(Long.valueOf(sr.getPublicID()));
             de.setContextName(sr.getContextName());
             de.setPreferredName(sr.getPreferredName());
             de.setRegistrationStatus(sr.getRegistrationStatus());
             de.setType(sr.getType().getName());
             de.setVersion(sr.getVersion());
             de.setWorkflowStatus(sr.getWorkflowStatus());
             dataElements.add(de);
         }
         return dataElements;
     }
     
     @SuppressWarnings({"PMD.UnusedPrivateMethod", "unused" }) // This might be used later, but might not.
     private List<CommonDataElement> convertAdministeredComponentsToDataElements(List<AdministeredComponent> acs) {
         List<CommonDataElement> dataElements = new ArrayList<CommonDataElement>();
         CommonDataElement de;
         for (AdministeredComponent ac : acs) {
             de = new CommonDataElement();
             de.setDefinition(ac.getPreferredDefinition());
             de.setLongName(ac.getLongName());
             de.setPublicID(Long.valueOf(ac.getPublicID()));
             de.setContextName(ac.getContext().getName());
             de.setPreferredName(ac.getPreferredName());
             de.setRegistrationStatus(ac.getRegistrationStatus());
             de.setVersion(String.valueOf(ac.getVersion()));
             de.setWorkflowStatus(ac.getWorkflowStatusName());
             dataElements.add(de);
         }
         return dataElements;
     }
 
     /**
      * @param search the search to set
      */
     public void setSearch(Search search) {
         this.search = search;
     }
 
     /**
      * @param dataDescription the dataDescription to set
      */
     public void setDataDescription(String dataDescription) {
         this.dataDescription = dataDescription;
     }
 
     /**
      * @param caDsrUrl the caDsrUrl to set
      */
     public void setCaDsrUrl(String caDsrUrl) {
         this.caDsrUrl = caDsrUrl;
     }
 
     /**
      * @param caDsrApplicationServiceFactory the caDsrApplicationServiceFactory to set
      */
     public void setCaDsrApplicationServiceFactory(CaDSRApplicationServiceFactory caDsrApplicationServiceFactory) {
         this.caDsrApplicationServiceFactory = caDsrApplicationServiceFactory;
     }
     
     
 
     
 }
