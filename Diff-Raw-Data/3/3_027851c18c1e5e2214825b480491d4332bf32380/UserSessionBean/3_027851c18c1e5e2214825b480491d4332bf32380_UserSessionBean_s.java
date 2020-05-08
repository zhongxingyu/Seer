 package gov.nih.nci.evs.browser.bean;
 
 import java.util.*;
 import java.net.URI;
 
 import javax.faces.context.*;
 import javax.faces.event.*;
 import javax.faces.model.*;
 import javax.servlet.http.*;
 
 import org.LexGrid.concepts.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.Utility.Iterators.*;
 
 import gov.nih.nci.evs.browser.utils.*;
 import gov.nih.nci.evs.browser.properties.*;
 import gov.nih.nci.evs.browser.common.*;
 import gov.nih.nci.evs.searchlog.*;
 import org.apache.log4j.*;
 
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.commonTypes.Source;
 
 
 
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  *
  *          Modification history Initial implementation kim.ong@ngc.com
  *
  */
 
 public class UserSessionBean extends Object {
     private static Logger _logger = Logger.getLogger(UserSessionBean.class);
 
     private static String _contains_warning_msg =
         "(WARNING: Only a subset of results may appear due to current limits in the terminology server (see Known Issues on the Help page).)";
     private String _selectedQuickLink = null;
     private List _quickLinkList = null;
 
     public List<SelectItem> _ontologyList = null;
     public List<String> _ontologiesToSearchOn = null;
 
     public String contextPath = null;
 
     public UserSessionBean() {
         _ontologiesToSearchOn = new ArrayList<String>();
         contextPath = getContextPath();
     }
 
 
     public String getContextPath() {
         if (contextPath == null) {
 			HttpServletRequest request =
 				(HttpServletRequest) FacesContext.getCurrentInstance()
 					.getExternalContext().getRequest();
 
 			contextPath = request.getContextPath();
 		}
 		return contextPath;
     }
 
 
     public void setSelectedQuickLink(String selectedQuickLink) {
         _selectedQuickLink = selectedQuickLink;
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         request.getSession().setAttribute("selectedQuickLink",
             selectedQuickLink);
     }
 
     public String getSelectedQuickLink() {
         return _selectedQuickLink;
     }
 
     public void quickLinkChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
 
         // _logger.debug("quickLinkChanged; " + newValue);
         setSelectedQuickLink(newValue);
 
         HttpServletResponse response =
             (HttpServletResponse) FacesContext.getCurrentInstance()
                 .getExternalContext().getResponse();
 
         String targetURL = null;// "http://nciterms.nci.nih.gov/";
         if (_selectedQuickLink.compareTo("NCI Terminology Browser") == 0) {
             targetURL = "http://nciterms.nci.nih.gov/";
         }
         try {
             response.sendRedirect(response.encodeRedirectURL(targetURL));
         } catch (Exception ex) {
             ex.printStackTrace();
             // send error message
         }
 
     }
 
     public List getQuickLinkList() {
         _quickLinkList = new ArrayList();
         _quickLinkList.add(new SelectItem("Quick Links"));
         _quickLinkList.add(new SelectItem("NCI Terminology Browser"));
         _quickLinkList.add(new SelectItem("NCI MetaThesaurus"));
         _quickLinkList.add(new SelectItem("EVS Home"));
         _quickLinkList.add(new SelectItem("NCI Terminology Resources"));
         return _quickLinkList;
     }
 
 
 
     public String searchAction() {
 
 System.out.println("(*******************) SearchAction");
 
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         String matchText = (String) request.getParameter("matchText");
         if (matchText != null)
             matchText = matchText.trim();
 
         // [#19965] Error message is not displayed when Search Criteria is not
         // proivded
         if (matchText == null || matchText.length() == 0) {
             String message = "Please enter a search string.";
             request.getSession().setAttribute("message", message);
             // request.getSession().removeAttribute("matchText");
 
             request.removeAttribute("matchText");
 
             return "message";
         }
         request.getSession().setAttribute("matchText", matchText);
 
         String matchAlgorithm = (String) request.getParameter("algorithm");
         String searchTarget = (String) request.getParameter("searchTarget");
 
         request.getSession().setAttribute("searchTarget", searchTarget);
         request.getSession().setAttribute("algorithm", matchAlgorithm);
 
         boolean ranking = true;
 
         String scheme = request.getParameter("scheme");
 
 
 
 
         String searchaction_dictionary = request.getParameter("dictionary");
 
         if (scheme == null) {
             scheme = (String) request.getAttribute("scheme");
         }
         if (scheme == null) {
             scheme = (String) request.getParameter("dictionary");
         }
         if (scheme == null) {
             scheme = Constants.CODING_SCHEME_NAME;
         }
 
         String version = (String) request.getParameter("version");
         if (version == null) {
             version = DataUtils.getVocabularyVersionByTag(scheme, "PRODUCTION");
 		}
 
 
 
 
 
 
 	    request.setAttribute("version", version);
 
         _logger.debug("UserSessionBean scheme: " + scheme);
         _logger.debug("searchAction version: " + version);
 
         // KLO, 012610
         if (searchTarget.compareTo("relationships") == 0
             && matchAlgorithm.compareTo("contains") == 0) {
             String text = matchText.trim();
             if (text.length() < NCItBrowserProperties
                 .getMinimumSearchStringLength()) {
                 String msg = Constants.ERROR_REQUIRE_MORE_SPECIFIC_QUERY_STRING;
                 request.getSession().setAttribute("message", msg);
                 request.getSession().setAttribute("vocabulary", scheme);
                 return "message";
             }
         }
 
         request.getSession().setAttribute("ranking", Boolean.toString(ranking));
         String source = (String) request.getParameter("source");
         if (source == null) {
             source = "ALL";
         }
 
         if (NCItBrowserProperties._debugOn) {
             try {
                 _logger.debug(Utils.SEPARATOR);
                 _logger.debug("* criteria: " + matchText);
                 // _logger.debug("* matchType: " + matchtype);
                 _logger.debug("* source: " + source);
                 _logger.debug("* ranking: " + ranking);
                 // _logger.debug("* sortOption: " + sortOption);
             } catch (Exception e) {
             }
         }
 
         Vector schemes = new Vector();
         schemes.add(scheme);
 
         //String version = null;
         Vector versions = new Vector();
         versions.add(version);
 
         String max_str = null;
         int maxToReturn = -1;// 1000;
         try {
             max_str =
                 NCItBrowserProperties.getInstance().getProperty(
                     NCItBrowserProperties.MAXIMUM_RETURN);
             maxToReturn = Integer.parseInt(max_str);
         } catch (Exception ex) {
 
         }
         Utils.StopWatch stopWatch = new Utils.StopWatch();
         Vector<org.LexGrid.concepts.Entity> v = null;
 
         boolean excludeDesignation = true;
         boolean designationOnly = false;
 
         // check if this search has been performance previously through
         // IteratorBeanManager
         IteratorBeanManager iteratorBeanManager =
             (IteratorBeanManager) FacesContext.getCurrentInstance()
                 .getExternalContext().getSessionMap()
                 .get("iteratorBeanManager");
 
         if (iteratorBeanManager == null) {
             iteratorBeanManager = new IteratorBeanManager();
             FacesContext.getCurrentInstance().getExternalContext()
                 .getSessionMap()
                 .put("iteratorBeanManager", iteratorBeanManager);
         }
 
         IteratorBean iteratorBean = null;
         ResolvedConceptReferencesIterator iterator = null;
         String key =
             iteratorBeanManager.createIteratorKey(schemes, matchText,
                 searchTarget, matchAlgorithm, maxToReturn);
         if (searchTarget.compareTo("names") == 0) {
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new SearchUtils()
                         .searchByName(schemes, versions, matchText, source,
                             matchAlgorithm, ranking, maxToReturn);
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
 
 
  System.out.println("(**************** iterator = wrapper.getIterator()");
 
 
 
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     } else {
  System.out.println("(**************** iterator == null???");
 
 					}
 
                 }
             }
 
         } else if (searchTarget.compareTo("properties") == 0) {
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new SearchUtils().searchByProperties(schemes, versions,
                         matchText, source, matchAlgorithm, excludeDesignation,
                         ranking, maxToReturn);
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     }
                 }
             }
 
         } else if (searchTarget.compareTo("relationships") == 0) {
             designationOnly = true;
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new SearchUtils().searchByAssociations(schemes, versions,
                         matchText, source, matchAlgorithm, designationOnly,
                         ranking, maxToReturn);
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     }
                 }
             }
         }
 
         request.getSession().setAttribute("vocabulary", scheme);
         request.getSession().removeAttribute("neighborhood_synonyms");
         request.getSession().removeAttribute("neighborhood_atoms");
         request.getSession().removeAttribute("concept");
         request.getSession().removeAttribute("code");
         request.getSession().removeAttribute("codeInNCI");
         request.getSession().removeAttribute("AssociationTargetHashMap");
         request.getSession().removeAttribute("type");
 
 		request.setAttribute("key", key);
 		System.out.println("(*************) setAttribute key: " + key);
 
 
         if (iterator == null) {
 System.out.println("(*******************) SearchAction iterator == null???");
 
 		}
 
 
         if (iterator != null) {
 
             // request.getSession().setAttribute("key", key);
             //request.setAttribute("key", key);
             //System.out.println("(*************) setAttribute key: " + key);
 
             int numberRemaining = 0;
             try {
                 numberRemaining = iterator.numberRemaining();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             int size = iteratorBean.getSize();
 
             if (size > 1) {
                 request.getSession().setAttribute("search_results", v);
                 String match_size = Integer.toString(size);
                 request.getSession().setAttribute("match_size", match_size);
                 request.getSession().setAttribute("page_string", "1");
 
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
 
                 request.getSession().setAttribute("dictionary", scheme);
 
                 _logger
                     .debug("UserSessionBean request.getSession().setAttribute dictionary: "
                         + scheme);
 
 
 				boolean isMapping = DataUtils.isMapping(scheme, version);
 				if (isMapping) {
 
 	System.out.println("(*************) creating mappingIteratorBean");
 
 
 					  MappingIteratorBean mappingIteratorBean = new MappingIteratorBean(
 						iterator,
 						numberRemaining, // number remaining
 						0,    // istart
 						50,   // iend,
 						numberRemaining, // size,
 						0,    // pageNumber,
 						1);   // numberPages
 
 
 request.getSession().setAttribute("mapping_search_results", mappingIteratorBean);
 
 	System.out.println("(*************) returning mapping_search_results");
 
 
 
 					return "mapping_search_results";
 				}
 
                 return "search_results";
             } else if (size == 1) {
                 request.getSession().setAttribute("singleton", "true");
                 request.getSession().setAttribute("dictionary", scheme);// Constants.CODING_SCHEME_NAME);
                 int pageNumber = 1;
                 List list = iteratorBean.getData(1);
                 ResolvedConceptReference ref =
                     (ResolvedConceptReference) list.get(0);
                 Entity c = null;
                 if (ref == null) {
                     String msg =
                         "Error: Null ResolvedConceptReference encountered.";
                     request.getSession().setAttribute("message", msg);
 
                     request.getSession().setAttribute("dictionary", scheme);
                     return "message";
 
                 } else {
                     if (ref.getConceptCode() == null) {
                         String message =
                             "Code has not been assigned to the concept matches with '"
                                 + matchText + "'";
                         _logger.warn("WARNING: " + message);
                         request.getSession().setAttribute("message", message);
                         request.getSession().setAttribute("dictionary", scheme);
                         return "message";
                     } else {
                         request.getSession().setAttribute("code",
                             ref.getConceptCode());
                     }
 
                     c = ref.getReferencedEntry();
 
                     if (c == null) {
 
                         c =
                             DataUtils.getConceptByCode(scheme, null, null, ref
                                 .getConceptCode());
                         if (c == null) {
                             String message =
                                 "Unable to find the concept with a code '"
                                     + ref.getConceptCode() + "'";
                             _logger.warn("WARNING: " + message);
                             request.getSession().setAttribute("message",
                                 message);
                             request.getSession().setAttribute("dictionary",
                                 scheme);
                             return "message";
                         }
 
                     } else {
                         request.getSession().setAttribute("code",
                             c.getEntityCode());
                     }
                 }
 
                 request.getSession().setAttribute("concept", c);
                 request.getSession().setAttribute("type", "properties");
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
                 return "concept_details";
             }
         }
 
         String message = "No match found.";
         int minimumSearchStringLength =
             NCItBrowserProperties.getMinimumSearchStringLength();
 
         if (matchAlgorithm.compareTo(Constants.EXACT_SEARCH_ALGORITHM) == 0) {
             message = Constants.ERROR_NO_MATCH_FOUND_TRY_OTHER_ALGORITHMS;
         }
 
         else if (matchAlgorithm.compareTo(Constants.STARTWITH_SEARCH_ALGORITHM) == 0
             && matchText.length() < minimumSearchStringLength) {
             message = Constants.ERROR_ENCOUNTERED_TRY_NARROW_QUERY;
         }
 
         request.getSession().setAttribute("message", message);
         request.getSession().setAttribute("dictionary", scheme);
         return "message";
     }
 
     private String _selectedResultsPerPage = null;
     private List _resultsPerPageList = null;
 
 
     public static List getResultsPerPageValues() {
         List resultsPerPageList = new ArrayList();
         resultsPerPageList.add("10");
         resultsPerPageList.add("25");
         resultsPerPageList.add("50");
         resultsPerPageList.add("75");
         resultsPerPageList.add("100");
         resultsPerPageList.add("250");
         resultsPerPageList.add("500");
         return resultsPerPageList;
     }
 
 
     public List getResultsPerPageList() {
         _resultsPerPageList = new ArrayList();
         _resultsPerPageList.add(new SelectItem("10"));
         _resultsPerPageList.add(new SelectItem("25"));
         _resultsPerPageList.add(new SelectItem("50"));
         _resultsPerPageList.add(new SelectItem("75"));
         _resultsPerPageList.add(new SelectItem("100"));
         _resultsPerPageList.add(new SelectItem("250"));
         _resultsPerPageList.add(new SelectItem("500"));
 
         _selectedResultsPerPage =
             ((SelectItem) _resultsPerPageList.get(2)).getLabel(); // default to
                                                                  // 50
         return _resultsPerPageList;
     }
 
     public void setSelectedResultsPerPage(String selectedResultsPerPage) {
         if (selectedResultsPerPage == null) {
 			System.out.println("(*) selectedResultsPerPage == null ??? ");
             return;
 		}
         _selectedResultsPerPage = selectedResultsPerPage;
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         request.getSession().setAttribute("selectedResultsPerPage",
             selectedResultsPerPage);
 
 
         System.out.println("(*) request.getSession().setAttribute selectedResultsPerPage " + selectedResultsPerPage);
     }
 
     public String getSelectedResultsPerPage() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         String s =
             (String) request.getSession()
                 .getAttribute("selectedResultsPerPage");
         if (s != null) {
             _selectedResultsPerPage = s;
         } else {
             _selectedResultsPerPage = "50";
             request.getSession().setAttribute("selectedResultsPerPage", "50");
         }
         return _selectedResultsPerPage;
     }
 
     public void resultsPerPageChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null) {
 			System.out.println("(*) UserSessionBean event.getNewValue() == null??? ");
             return;
         }
         String newValue = (String) event.getNewValue();
 
         System.out.println("(*) UserSessionBean resultsPerPageChanged newValue: " + newValue);
 
 
         setSelectedResultsPerPage(newValue);
     }
 
     public String linkAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         return "";
     }
 
     private String _selectedAlgorithm = null;
     private List _algorithmList = null;
 
     public List getAlgorithmList() {
         _algorithmList = new ArrayList();
         _algorithmList.add(new SelectItem("exactMatch", "exactMatch"));
         _algorithmList.add(new SelectItem("startsWith", "Begins With"));
         _algorithmList.add(new SelectItem("contains", "Contains"));
         _selectedAlgorithm = ((SelectItem) _algorithmList.get(0)).getLabel();
         return _algorithmList;
     }
 
     public void algorithmChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
         setSelectedAlgorithm(newValue);
     }
 
     public void setSelectedAlgorithm(String selectedAlgorithm) {
         _selectedAlgorithm = selectedAlgorithm;
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         request.getSession().setAttribute("algorithm", selectedAlgorithm);
     }
 
     public String getSelectedAlgorithm() {
         return _selectedAlgorithm;
     }
 
     public String contactUs() throws Exception {
         String msg = "Your message was successfully sent.";
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         try {
             String subject = request.getParameter("subject");
             String message = request.getParameter("message");
             String from = request.getParameter("emailaddress");
             String recipients[] = MailUtils.getRecipients();
             MailUtils.postMail(from, recipients, subject, message);
         } catch (UserInputException e) {
             msg = e.getMessage();
             request.setAttribute("errorMsg", Utils.toHtml(msg));
             request.setAttribute("errorType", "user");
             return "error";
         } catch (Exception e) {
             msg = "System Error: Your message was not sent.\n";
             msg += "    (If possible, please contact NCI systems team.)\n";
             msg += "\n";
             msg += e.getMessage();
             request.setAttribute("errorMsg", Utils.toHtml(msg));
             request.setAttribute("errorType", "system");
             e.printStackTrace();
             return "error";
         }
 
         request.getSession().setAttribute("message", Utils.toHtml(msg));
         return "message";
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////
     // ontologies
 
     public List getOntologiesToSearchOn() {
         if (_ontologyList == null) {
             _ontologyList = DataUtils.getOntologyList();
             SelectItem item = (SelectItem) _ontologyList.get(0);
             _ontologiesToSearchOn.add(item.getLabel());
         } else if (_ontologiesToSearchOn.size() == 0) {
             SelectItem item = (SelectItem) _ontologyList.get(0);
             _ontologiesToSearchOn.add(item.getLabel());
         }
         return _ontologiesToSearchOn;
     }
 
     public List getOntologyList() {
         if (_ontologyList == null) {
             _ontologyList = DataUtils.getOntologyList();
         }
         return _ontologyList;
     }
 
     public void setOntologiesToSearchOn(List<String> newValue) {
         _ontologiesToSearchOn = new ArrayList<String>();
         for (int i = 0; i < newValue.size(); i++) {
             Object obj = newValue.get(i);
             _ontologiesToSearchOn.add((String) obj);
         }
     }
 
     public void ontologiesToSearchOnChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null) {
             return;
         }
 
         List newValue = (List) event.getNewValue();
         setOntologiesToSearchOn(newValue);
     }
 
     public List<SelectItem> _ontologySelectionList = null;
     public String _ontologyToSearchOn = null;
 
     public List getOntologySelectionList() {
         if (_ontologySelectionList != null)
             return _ontologySelectionList;
         List ontologies = getOntologyList();
         _ontologySelectionList = new ArrayList<SelectItem>();
         String label = "Switch to another vocabulary (select one)";
         _ontologySelectionList.add(new SelectItem(label, label));
         for (int i = 0; i < ontologies.size(); i++) {
             SelectItem item = (SelectItem) _ontologyList.get(i);
             _ontologySelectionList.add(item);
         }
         return _ontologySelectionList;
     }
 
     public void ontologySelectionChanged(ValueChangeEvent event) {
 
         if (event.getNewValue() == null) {
             // _logger.warn("ontologySelectionChanged; event.getNewValue() == null ");
             return;
         }
         String newValue = (String) event.getNewValue();
 
         HttpServletResponse response =
             (HttpServletResponse) FacesContext.getCurrentInstance()
                 .getExternalContext().getResponse();
 
         String targetURL = null;// "http://nciterms.nci.nih.gov/";
         targetURL = "http://nciterms.nci.nih.gov/";
         try {
             response.sendRedirect(response.encodeRedirectURL(targetURL));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public String getOntologyToSearchOn() {
         if (_ontologySelectionList == null) {
             _ontologySelectionList = getOntologySelectionList();
             SelectItem item = (SelectItem) _ontologyList.get(1);
             _ontologyToSearchOn = item.getLabel();
         }
         return _ontologyToSearchOn;
     }
 
     public void setOntologyToSearchOn(String newValue) {
         _ontologyToSearchOn = newValue;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////
 
     private String[] getSelectedVocabularies(String ontology_list_str) {
         Vector v = DataUtils.parseData(ontology_list_str);
         String[] ontology_list = new String[v.size()];
         for (int i = 0; i < v.size(); i++) {
             String s = (String) v.elementAt(i);
             ontology_list[i] = s;
         }
         return ontology_list;
     }
 
     public String acceptLicenseAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         String dictionary = (String) request.getParameter("dictionary");
         String code = (String) request.getParameter("code");
 
         if (dictionary != null && code != null) {
             LicenseBean licenseBean =
                 (LicenseBean) request.getSession().getAttribute("licenseBean");
             if (licenseBean == null) {
                 licenseBean = new LicenseBean();
             }
             licenseBean.addLicenseAgreement(dictionary);
             request.getSession().setAttribute("licenseBean", licenseBean);
 
             Entity c =
                 DataUtils.getConceptByCode(dictionary, null, null, code);
             request.getSession().setAttribute("code", code);
             request.getSession().setAttribute("concept", c);
             request.getSession().setAttribute("type", "properties");
 
             return "concept_details";
         } else {
             String message = "Unidentifiable vocabulary name, or code";
             request.getSession().setAttribute("warning", message);
             return "message";
         }
     }
 
 
 
 
     public String multipleSearchAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         String scheme = (String) request.getParameter("scheme");
         String version = (String) request.getParameter("version");
 
 		String navigation_type = request.getParameter("nav_type");
 		if (navigation_type == null || navigation_type.equals("null")) {
 			navigation_type = "terminologies";
 		}
 
         // Called from license.jsp
         LicenseBean licenseBean =
             (LicenseBean) request.getSession().getAttribute("licenseBean");
         if (scheme != null && version != null) {
 
             if (licenseBean == null) {
                 licenseBean = new LicenseBean();
             }
             licenseBean.addLicenseAgreement(scheme);
             request.getSession().setAttribute("licenseBean", licenseBean);
         }
 
         String matchText = (String) request.getParameter("matchText");
         if (matchText != null) {
             matchText = matchText.trim();
             request.getSession().setAttribute("matchText", matchText);
         } else {
             matchText = (String) request.getSession().getAttribute("matchText");
         }
         String multiple_search_error =
             (String) request.getSession().getAttribute(
                 "multiple_search_no_match_error");
         request.getSession().removeAttribute("multiple_search_no_match_error");
 
         String matchAlgorithm = (String) request.getParameter("algorithm");
         request.getSession().setAttribute("algorithm", matchAlgorithm);
         String searchTarget = (String) request.getParameter("searchTarget");
         request.getSession().setAttribute("searchTarget", searchTarget);
 
         String initial_search = (String) request.getParameter("initial_search");
 
         String[] ontology_list = null;
         //if (navigation_type == null || navigation_type.compareTo("terminologies") == 0) {
             ontology_list = request.getParameterValues("ontology_list");
 		//}
 
         List list = new ArrayList<String>();
 
         String ontologiesToSearchOnStr = null;
         String ontology_list_str = null;
         List<String> ontologiesToSearchOn = null;
         int knt = 0;
 
         // process mappings
         if (initial_search != null) { // from home page
 			if (multiple_search_error != null) {
 				ontologiesToSearchOn = new ArrayList<String>();
 				ontologiesToSearchOnStr =
 					(String) request.getSession().getAttribute(
 						"ontologiesToSearchOn");
 
 				if (ontologiesToSearchOnStr != null) {
 					Vector ontologies_to_search_on =
 						DataUtils.parseData(ontologiesToSearchOnStr);
 					ontology_list = new String[ontologies_to_search_on.size()];
 					knt = ontologies_to_search_on.size();
 					for (int k = 0; k < ontologies_to_search_on.size(); k++) {
 						String s =
 							(String) ontologies_to_search_on.elementAt(k);
 						ontology_list[k] = s;
 						ontologiesToSearchOn.add(s);
 					}
 				}
 			}
 
             if (ontology_list == null || ontology_list.length == 0) {
 
                 String message = Constants.ERROR_NO_VOCABULARY_SELECTED;// "Please select at least one vocabulary.";
                 request.getSession().setAttribute("warning", message);
                 request.getSession().setAttribute("message", message);
                 request.getSession().removeAttribute("ontologiesToSearchOn");
 
                 // String defaultOntologiesToSearchOnStr =
                 // ontologiesToSearchOnStr;
                 request.getSession().setAttribute(
                     "defaultOntologiesToSearchOnStr", "|");
                 request.getSession().setAttribute("matchText",
                     HTTPUtils.convertJSPString(matchText));
 
                 return "multiple_search";
             } else {
                 ontologiesToSearchOn = new ArrayList<String>();
 
                 ontologiesToSearchOnStr = "|";
                 for (int i = 0; i < ontology_list.length; ++i) {
                     list.add(ontology_list[i]);
                     ontologiesToSearchOn.add(ontology_list[i]);
                     ontologiesToSearchOnStr =
                         ontologiesToSearchOnStr + ontology_list[i] + "|";
                 }
 
                 if (ontology_list_str == null) {
                     ontology_list_str = "";
                     for (int i = 0; i < ontology_list.length; ++i) {
                         ontology_list_str =
                             ontology_list_str + ontology_list[i];
                         if (i < ontology_list.length - 1) {
                             ontology_list_str = ontology_list_str + "|";
                         }
                     }
                 }
                 request.getSession().setAttribute("ontologiesToSearchOn",
                     ontologiesToSearchOnStr);
             }
         } else {
 
 			ontologiesToSearchOn = new ArrayList<String>();
 			ontologiesToSearchOnStr =
 				(String) request.getSession().getAttribute(
 					"ontologiesToSearchOn");
 			if (ontologiesToSearchOnStr != null) {
 				Vector ontologies_to_search_on =
 					DataUtils.parseData(ontologiesToSearchOnStr);
 				ontology_list = new String[ontologies_to_search_on.size()];
 				knt = ontologies_to_search_on.size();
 				for (int k = 0; k < ontologies_to_search_on.size(); k++) {
 					String s = (String) ontologies_to_search_on.elementAt(k);
 					ontology_list[k] = s;
 					ontologiesToSearchOn.add(s);
 				}
 			}
 
         }
 
 
         String hide_ontology_list = "false";
         // [#19965] Error message is not displayed when Search Criteria is not
         // proivided
         if (matchText == null || matchText.length() == 0) {
             String message = Constants.ERROR_NO_SEARCH_STRING_ENTERED;
 
             if (initial_search == null) {
                 hide_ontology_list = "true";
             }
             request.getSession().setAttribute("hide_ontology_list",
                 hide_ontology_list);
             request.getSession().setAttribute("warning", message);
             request.getSession().setAttribute("message", message);
             request.getSession().setAttribute("matchText",
                 HTTPUtils.convertJSPString(matchText));
 
             return "multiple_search";
         }
 
         // KLO, 012610
         else if (searchTarget.compareTo("relationships") == 0
             && matchAlgorithm.compareTo("contains") == 0) {
             String text = matchText.trim();
             if (text.length() < NCItBrowserProperties
                 .getMinimumSearchStringLength()) {
                 String msg = Constants.ERROR_REQUIRE_MORE_SPECIFIC_QUERY_STRING;
                 request.getSession().setAttribute("warning", msg);
                 request.getSession().setAttribute("message", msg);
                 request.getSession().setAttribute("matchText",
                     HTTPUtils.convertJSPString(matchText));
                 return "multiple_search";
             }
         }
 
         boolean ranking = true;
         String source = (String) request.getParameter("source");
         if (source == null) {
             source = "ALL";
         }
 
         if (NCItBrowserProperties._debugOn) {
             try {
                 _logger.debug(Utils.SEPARATOR);
                 _logger.debug("* criteria: " + matchText);
                 _logger.debug("* source: " + source);
                 _logger.debug("* ranking: " + ranking);
                 _logger.debug("* ontology_list: ");
                 for (int i = 0; i < ontology_list.length; ++i) {
                     _logger.debug("  " + i + ") " + ontology_list[i]);
                 }
             } catch (Exception e) {
             }
         }
 
         if (ontology_list == null) {
             ontology_list_str =
                 (String) request.getParameter("ontology_list_str"); // from
                                                                     // multiple_search_results
                                                                     // (hidden
                                                                     // variable)
             if (ontology_list_str != null) {
                 ontology_list = getSelectedVocabularies(ontology_list_str);
             }
 
         } else {
             knt = ontology_list.length;
             if (knt == 0) {
                 String message = Constants.ERROR_NO_VOCABULARY_SELECTED;// "Please select at least one vocabulary.";
                 request.getSession().setAttribute("warning", message);
                 request.getSession().setAttribute("message", message);
                 request.getSession().setAttribute("hide_ontology_list", "true");
                 request.getSession().removeAttribute("ontologiesToSearchOn");
 
                 // String defaultOntologiesToSearchOnStr =
                 // ontologiesToSearchOnStr;
                 request.getSession().setAttribute(
                     "defaultOntologiesToSearchOnStr", "|");
                 request.getSession().setAttribute("matchText",
                     HTTPUtils.convertJSPString(matchText));
 
                 return "multiple_search";
             }
         }
 
         Vector schemes = new Vector();
         Vector versions = new Vector();
 
         ontologiesToSearchOn = new ArrayList<String>();
 
         ontologiesToSearchOnStr = "|";
         for (int i = 0; i < ontology_list.length; ++i) {
             list.add(ontology_list[i]);
             ontologiesToSearchOn.add(ontology_list[i]);
             ontologiesToSearchOnStr =
                 ontologiesToSearchOnStr + ontology_list[i] + "|";
         }
 
         if (ontology_list_str == null) {
             ontology_list_str = "";
             for (int i = 0; i < ontology_list.length; ++i) {
                 ontology_list_str = ontology_list_str + ontology_list[i];
                 if (i < ontology_list.length - 1) {
                     ontology_list_str = ontology_list_str + "|";
                 }
             }
         }
 
         scheme = null;
         version = null;
         String t = "";
         if (ontologiesToSearchOn.size() == 0) {
             String message = Constants.ERROR_NO_VOCABULARY_SELECTED;// "Please select at least one vocabulary.";
             request.getSession().setAttribute("warning", message);
             request.getSession().setAttribute("message", message);
             request.getSession().removeAttribute("ontologiesToSearchOn");
 
             request.getSession().setAttribute("matchText",
                 HTTPUtils.convertJSPString(matchText));
 
             return "multiple_search";
         } else {
             request.getSession().setAttribute("ontologiesToSearchOn",
                 ontologiesToSearchOnStr);
 
             // [#25270] Set "all but NCIm selected" as the default for the TB
             // home page.
             String defaultOntologiesToSearchOnStr = ontologiesToSearchOnStr;
             request.getSession().setAttribute("defaultOntologiesToSearchOnStr",
                 defaultOntologiesToSearchOnStr);
 
             for (int k = 0; k < ontologiesToSearchOn.size(); k++) {
                 String key = (String) list.get(k);
                 if (key != null) {
                     scheme = DataUtils.key2CodingSchemeName(key);
                     version = DataUtils.key2CodingSchemeVersion(key);
                     if (scheme != null) {
                         schemes.add(scheme);
                         // to be modified (handling of versions)
                         versions.add(version);
                         t = t + scheme + " (" + version + ")" + "\n";
                         boolean isLicensed =
                             LicenseBean.isLicensed(scheme, version);
                         if (licenseBean == null) {
                             licenseBean = new LicenseBean();
                             request.getSession().setAttribute("licenseBean",
                                 licenseBean);
                         }
 
                         boolean accepted =
                             licenseBean.licenseAgreementAccepted(scheme);
                         if (isLicensed && !accepted) {
                             request.getSession().setAttribute("matchText",
                                 matchText);
 
                             request.setAttribute("searchTarget", searchTarget);
                             request.setAttribute("algorithm", matchAlgorithm);
                             request.setAttribute("ontology_list_str",
                                 ontology_list_str);
                             request.setAttribute("scheme", scheme);
                             request.setAttribute("version", version);
                             return "license";
                         }
                     } else {
                         _logger.warn("Unable to identify " + key);
                     }
                 }
             }
         }
 
         String max_str = null;
         int maxToReturn = -1;// 1000;
         try {
             max_str =
                 NCItBrowserProperties
                     .getProperty(NCItBrowserProperties.MAXIMUM_RETURN);
             maxToReturn = Integer.parseInt(max_str);
         } catch (Exception ex) {
             // Do nothing
         }
         boolean designationOnly = false;
         boolean excludeDesignation = true;
         ResolvedConceptReferencesIterator iterator = null;
         if (searchTarget.compareTo("names") == 0) {
             long ms = System.currentTimeMillis();
             long delay = 0;
             _logger.debug("Calling SearchUtils().searchByName " + matchText);
             ResolvedConceptReferencesIteratorWrapper wrapper =
                 new SearchUtils().searchByName(schemes, versions, matchText,
                     source, matchAlgorithm, ranking, maxToReturn);
             if (wrapper != null) {
                 iterator = wrapper.getIterator();
             }
             delay = System.currentTimeMillis() - ms;
             _logger.debug("searchByName delay (millisec.): " + delay);
 
         } else if (searchTarget.compareTo("properties") == 0) {
             ResolvedConceptReferencesIteratorWrapper wrapper =
                 new SearchUtils().searchByProperties(schemes, versions,
                     matchText, source, matchAlgorithm, excludeDesignation,
                     ranking, maxToReturn);
             if (wrapper != null) {
                 iterator = wrapper.getIterator();
             }
         } else if (searchTarget.compareTo("relationships") == 0) {
             designationOnly = true;
             ResolvedConceptReferencesIteratorWrapper wrapper =
                 new SearchUtils().searchByAssociations(schemes, versions,
                     matchText, source, matchAlgorithm, designationOnly,
                     ranking, maxToReturn);
             if (wrapper != null) {
                 iterator = wrapper.getIterator();
            }
         }
 
         request.getSession().setAttribute("vocabulary", scheme);
         request.getSession().setAttribute("searchTarget", searchTarget);
         request.getSession().setAttribute("algorithm", matchAlgorithm);
 
         request.getSession().removeAttribute("neighborhood_synonyms");
         request.getSession().removeAttribute("neighborhood_atoms");
         request.getSession().removeAttribute("concept");
         request.getSession().removeAttribute("code");
         request.getSession().removeAttribute("codeInNCI");
         request.getSession().removeAttribute("AssociationTargetHashMap");
         request.getSession().removeAttribute("type");
 
         if (iterator != null) {
             IteratorBean iteratorBean =
                 (IteratorBean) FacesContext.getCurrentInstance()
                     .getExternalContext().getSessionMap().get("iteratorBean");
 
             if (iteratorBean == null) {
                 iteratorBean = new IteratorBean(iterator);
 
                 FacesContext.getCurrentInstance().getExternalContext()
                     .getSessionMap().put("iteratorBean", iteratorBean);
             } else {
                 iteratorBean.setIterator(iterator);
             }
 
             int size = iteratorBean.getSize();
             if (size == 1) {
 
                 int pageNumber = 1;
                 list = iteratorBean.getData(1);
                 ResolvedConceptReference ref =
                     (ResolvedConceptReference) list.get(0);
 
                 String coding_scheme = ref.getCodingSchemeName();
                 if (coding_scheme.compareToIgnoreCase("NCI Metathesaurus") == 0) {
                     String match_size = Integer.toString(size);
                     ;// Integer.toString(v.size());
                     request.getSession().setAttribute("match_size", match_size);
                     request.getSession().setAttribute("page_string", "1");
                     request.getSession().setAttribute("new_search",
                         Boolean.TRUE);
                     // route to multiple_search_results.jsp
                     return "search_results";
                 }
 
                 request.getSession().setAttribute("singleton", "true");
                 request.getSession().setAttribute("dictionary", coding_scheme);
                 Entity c = null;
                 if (ref == null) {
                     String msg =
                         "Error: Null ResolvedConceptReference encountered.";
                     request.getSession().setAttribute("message", msg);
                     request.getSession().setAttribute("matchText",
                         HTTPUtils.convertJSPString(matchText));
                     return "message";
 
                 } else {
                     c = ref.getReferencedEntry();
                     if (c == null) {
                         c =
                             DataUtils.getConceptByCode(coding_scheme, null,
                                 null, ref.getConceptCode());
                     }
                 }
 
                 request.getSession().setAttribute("code", ref.getConceptCode());
                 request.getSession().setAttribute("concept", c);
                 request.getSession().setAttribute("type", "properties");
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
 
                 request.setAttribute("algorithm", matchAlgorithm);
                 coding_scheme =
                     (String) DataUtils._localName2FormalNameHashMap
                         .get(coding_scheme);
 
                 String convertJSPString = HTTPUtils.convertJSPString(matchText);
                 request.getSession()
                     .setAttribute("matchText", convertJSPString);
 
                 request.setAttribute("dictionary", coding_scheme);
                 return "concept_details";
             } else if (size > 0) {
                 String match_size = Integer.toString(size);
                 ;// Integer.toString(v.size());
                 request.getSession().setAttribute("match_size", match_size);
                 request.getSession().setAttribute("page_string", "1");
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
                 // route to multiple_search_results.jsp
                 request.getSession().setAttribute("matchText",
                     HTTPUtils.convertJSPString(matchText));
 
                 _logger.debug("Start to render search_results ... ");
                 return "search_results";
             }
         }
 
         int minimumSearchStringLength =
             NCItBrowserProperties.getMinimumSearchStringLength();
         if (ontologiesToSearchOn.size() == 0) {
             request.getSession().removeAttribute("vocabulary");
         } else if (ontologiesToSearchOn.size() == 1) {
             String msg_scheme = (String) ontologiesToSearchOn.get(0);
             request.getSession().setAttribute("vocabulary", msg_scheme);
         } else {
             request.getSession().removeAttribute("vocabulary");
         }
 
         String message = Constants.ERROR_NO_MATCH_FOUND;
         if (matchAlgorithm.compareTo(Constants.EXACT_SEARCH_ALGORITHM) == 0) {
             message = Constants.ERROR_NO_MATCH_FOUND_TRY_OTHER_ALGORITHMS;
         }
 
         else if (matchAlgorithm.compareTo(Constants.STARTWITH_SEARCH_ALGORITHM) == 0
             && matchText.length() < minimumSearchStringLength) {
             message = Constants.ERROR_ENCOUNTERED_TRY_NARROW_QUERY;
         }
 
         hide_ontology_list = "false";
         /*
          * if (initial_search == null) { hide_ontology_list = "true"; }
          */
         request.getSession().setAttribute("hide_ontology_list",
             hide_ontology_list);
         request.getSession().setAttribute("warning", message);
         request.getSession().setAttribute("message", message);
         request.getSession().setAttribute("ontologiesToSearchOn",
             ontologiesToSearchOnStr);
         request.getSession().setAttribute("multiple_search_no_match_error",
             "true");
 
         request.getSession().setAttribute("matchText",
             HTTPUtils.convertJSPString(matchText));
         return "multiple_search";
     }
 
     public String acceptLicenseAgreement() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         // update LicenseBean
         String dictionary = (String) request.getParameter("dictionary");
         String version = (String) request.getParameter("version");
 
         LicenseBean licenseBean =
             (LicenseBean) request.getSession().getAttribute("licenseBean");
         if (licenseBean == null) {
             licenseBean = new LicenseBean();
         }
         licenseBean.addLicenseAgreement(dictionary);
         request.getSession().setAttribute("licenseBean", licenseBean);
 
         request.getSession().setAttribute("dictionary", dictionary);
         request.getSession().setAttribute("scheme", dictionary);
         request.getSession().setAttribute("version", version);
         return "vocabulary_home";
     }
 
     public String advancedSearchAction() {
 
         ResolvedConceptReferencesIteratorWrapper wrapper = null;
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         String scheme = (String) request.getParameter("dictionary");
         String version = (String) request.getParameter("version");
 
 System.out.println("advancedSearchAction version: " + version);
 
 
         SearchStatusBean bean =
             (SearchStatusBean) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequestMap().get("searchStatusBean");
 
         if (bean == null) {
             bean = new SearchStatusBean(scheme, version);
             request.setAttribute("searchStatusBean", bean);
         }
 
         String matchType = (String) request.getParameter("adv_search_type");
 
         bean.setSearchType(matchType);
 
         String matchAlgorithm =
             (String) request.getParameter("adv_search_algorithm");
         bean.setAlgorithm(matchAlgorithm);
 
         String source = (String) request.getParameter("adv_search_source");
         bean.setSelectedSource(source);
 
         String selectSearchOption =
             (String) request.getParameter("selectSearchOption");
         bean.setSelectedSearchOption(selectSearchOption);
 
         String selectProperty = (String) request.getParameter("selectProperty");
         bean.setSelectedProperty(selectProperty);
 
         String rel_search_association =
             (String) request.getParameter("rel_search_association");
         bean.setSelectedAssociation(rel_search_association);
 
         String rel_search_rela =
             (String) request.getParameter("rel_search_rela");
         bean.setSelectedRELA(rel_search_rela);
 
         FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
             .put("searchStatusBean", bean);
         request.setAttribute("searchStatusBean", bean);
 
         String searchTarget = (String) request.getParameter("searchTarget");
 
         String matchText = (String) request.getParameter("matchText");
         if (matchText == null || matchText.length() == 0) {
             String message = "Please enter a search string.";
             // request.getSession().setAttribute("message", message);
             request.setAttribute("message", message);
             return "message";
         }
         matchText = matchText.trim();
         bean.setMatchText(matchText);
 
         if (NCItBrowserProperties._debugOn) {
             _logger.debug(Utils.SEPARATOR);
             _logger.debug("* criteria: " + matchText);
             _logger.debug("* source: " + source);
         }
 
         // String scheme = Constants.CODING_SCHEME_NAME;
         Vector schemes = new Vector();
         schemes.add(scheme);
 
         String max_str = null;
         int maxToReturn = -1;// 1000;
         try {
             max_str =
                 NCItBrowserProperties.getInstance().getProperty(
                     NCItBrowserProperties.MAXIMUM_RETURN);
             maxToReturn = Integer.parseInt(max_str);
         } catch (Exception ex) {
         }
         Utils.StopWatch stopWatch = new Utils.StopWatch();
         Vector<org.LexGrid.concepts.Entity> v = null;
 
         boolean excludeDesignation = true;
         boolean designationOnly = false;
 
         // check if this search has been performance previously through
         // IteratorBeanManager
         IteratorBeanManager iteratorBeanManager =
             (IteratorBeanManager) FacesContext.getCurrentInstance()
                 .getExternalContext().getSessionMap()
                 .get("iteratorBeanManager");
 
         if (iteratorBeanManager == null) {
             iteratorBeanManager = new IteratorBeanManager();
             FacesContext.getCurrentInstance().getExternalContext()
                 .getSessionMap()
                 .put("iteratorBeanManager", iteratorBeanManager);
         }
 
         IteratorBean iteratorBean = null;
         ResolvedConceptReferencesIterator iterator = null;
         boolean ranking = true;
 
         SearchFields searchFields = null;
         String key = null;
 
         String searchType = (String) request.getParameter("selectSearchOption");
         _logger.debug("SearchUtils.java searchType: " + searchType);
 
         if (searchType != null && searchType.compareTo("Property") == 0) {
             /*
              * _logger.debug("Advanced Search: "); _logger.debug("searchType: "
              * + searchType); _logger.debug("matchText: " + matchText);
              * _logger.debug("adv_search_algorithm: " + adv_search_algorithm);
              * _logger.debug("adv_search_source: " + adv_search_source);
              */
 
             String property_type =
                 (String) request.getParameter("selectPropertyType");
             if (property_type != null && property_type.compareTo("ALL") == 0) {
                 property_type = null;
             }
 
             String property_name = selectProperty;
             if (property_name != null) {
                 property_name = property_name.trim();
                 // if (property_name.length() == 0) property_name = null;
                 if (property_name.compareTo("ALL") == 0)
                     property_name = null;
             }
 
             searchFields =
                 SearchFields.setProperty(schemes, matchText, searchTarget,
                     property_type, property_name, source, matchAlgorithm,
                     maxToReturn);
             key = searchFields.getKey();
 
             _logger.debug("advancedSearchAction " + key);
 
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 String[] property_types = null;
                 if (property_type != null)
                     property_types = new String[] { property_type };
                 String[] property_names = null;
                 if (property_name != null)
                     property_names = new String[] { property_name };
                 excludeDesignation = false;
                 wrapper =
                     new SearchUtils().searchByProperties(scheme, version,
                         matchText, property_types, property_names, source,
                         matchAlgorithm, excludeDesignation, ranking,
                         maxToReturn);
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                 }
                 if (iterator != null) {
                     iteratorBean = new IteratorBean(iterator);
                     iteratorBean.setKey(key);
                     iteratorBean.setMatchText(matchText);
                     iteratorBeanManager.addIteratorBean(iteratorBean);
                 }
             }
 
         } else if (searchType != null
             && searchType.compareTo("Relationship") == 0) {
             if (rel_search_association != null
                 && rel_search_association.compareTo("ALL") == 0)
                 rel_search_association = null;
 
             if (rel_search_rela != null) {
                 rel_search_rela = rel_search_rela.trim();
                 if (rel_search_rela.length() == 0)
                     rel_search_rela = null;
             }
 
             /*
              * String rel_search_direction = (String)
              * request.getParameter("rel_search_direction");
              *
              * //boolean direction = false; int search_direction =
              * Constants.SEARCH_BOTH_DIRECTION; if (rel_search_direction != null
              * && rel_search_direction.compareTo("source") == 0) {
              * search_direction = Constants.SEARCH_SOURCE; //direction = true; }
              * else if (rel_search_direction != null &&
              * rel_search_direction.compareTo("target") == 0) { search_direction
              * = Constants.SEARCH_TARGET; //direction = true; }
              */
 
             int search_direction = Constants.SEARCH_SOURCE;
 
             _logger.debug("AdvancedSearchAction search_direction "
                 + search_direction);
 
 
 
             searchFields =
                 SearchFields.setRelationship(schemes, matchText, searchTarget,
                     rel_search_association, rel_search_rela, source,
                     matchAlgorithm, maxToReturn);
             key = searchFields.getKey();
 
             _logger.debug("AdvancedSearchAction key " + key);
 
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
 
                 String[] associationsToNavigate = null;
                 String[] association_qualifier_names = null;
                 String[] association_qualifier_values = null;
 
                 if (rel_search_association != null) {
 					/*
                     associationsToNavigate =
                         new String[] { rel_search_association };
                     */
 					String assocName = OntologyBean.convertAssociationName(scheme, null, rel_search_association);
 					//_logger.debug("Converting " + rel_search_association + " to " + assocName);
                     associationsToNavigate =
                         new String[] { assocName };
 
 
                 } else {
                     _logger.debug("(*) associationsToNavigate == null");
                 }
 
                 if (rel_search_rela != null) {
                     association_qualifier_names = new String[] { "rela" };
                     association_qualifier_values =
                         new String[] { rel_search_rela };
 
                     if (associationsToNavigate == null) {
                         Vector w = OntologyBean.getAssociationNames(scheme);
                         if (w == null || w.size() == 0) {
                             _logger
                                 .warn("OntologyBean.getAssociationNames() returns null, or nothing???");
                         } else {
                             associationsToNavigate = new String[w.size()];
                             for (int i = 0; i < w.size(); i++) {
                                 String nm = (String) w.elementAt(i);
                                 associationsToNavigate[i] = nm;
                             }
                         }
                     }
 
                 } else {
                     _logger.warn("(*) qualifiers == null");
                 }
 
                 wrapper =
                     new SearchUtils().searchByAssociations(scheme, version,
                         matchText, associationsToNavigate,
                         association_qualifier_names,
                         association_qualifier_values, search_direction, source,
                         matchAlgorithm, excludeDesignation, ranking,
                         maxToReturn);
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                 }
                 if (iterator != null) {
                     iteratorBean = new IteratorBean(iterator);
                     iteratorBean.setKey(key);
                     iteratorBean.setMatchText(matchText);
                     iteratorBeanManager.addIteratorBean(iteratorBean);
                 }
             }
 
         } else if (searchType != null && searchType.compareTo("Name") == 0) {
             searchFields =
                 SearchFields.setName(schemes, matchText, searchTarget, source,
                     matchAlgorithm, maxToReturn);
             key = searchFields.getKey();
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 wrapper =
                     new SearchUtils().searchByName(scheme, version, matchText,
                         source, matchAlgorithm, ranking, maxToReturn,
                         SearchUtils.NameSearchType.Name);
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBean.setMatchText(matchText);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     }
                 }
             }
 
         } else if (searchType != null && searchType.compareTo("Code") == 0) {
             searchFields =
                 SearchFields.setCode(schemes, matchText, searchTarget, source,
                     matchAlgorithm, maxToReturn);
             key = searchFields.getKey();
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 /*
                  * wrapper = new SearchUtils().searchByName(scheme, version,
                  * matchText, source, matchAlgorithm, ranking, maxToReturn,
                  * SearchUtils.NameSearchType.Code);
                  */
 
                 wrapper =
                     new SearchUtils().searchByCode(scheme, version, matchText,
                         source, matchAlgorithm, ranking, maxToReturn);
 
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBean.setMatchText(matchText);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     }
                 }
             }
         }
 
         request.setAttribute("key", key);
         request.getSession().setAttribute("matchText", matchText);
 
         request.getSession().removeAttribute("neighborhood_synonyms");
         request.getSession().removeAttribute("neighborhood_atoms");
         request.getSession().removeAttribute("concept");
         request.getSession().removeAttribute("code");
         request.getSession().removeAttribute("codeInNCI");
         request.getSession().removeAttribute("AssociationTargetHashMap");
         request.getSession().removeAttribute("type");
 
         if (iterator != null) {
             int size = iteratorBean.getSize();
 
             _logger.debug("AdvancedSearchActon size: " + size);
 
             // Write a search log entry
             SearchLog.writeEntry(searchFields, size, HTTPUtils
                 .getRefererParmDecode(request));
 
             if (size > 1) {
                 request.getSession().setAttribute("search_results", v);
 
                 String match_size = Integer.toString(size);
                 ;// Integer.toString(v.size());
                 request.getSession().setAttribute("match_size", match_size);
                 request.getSession().setAttribute("page_string", "1");
 
                 request.setAttribute("version", version);
 
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
                 return "search_results";
             } else if (size == 1) {
                 request.getSession().setAttribute("singleton", "true");
                 request.getSession().setAttribute("dictionary", scheme);
                 // Concept c = (Concept) v.elementAt(0);
                 int pageNumber = 1;
                 List list = iteratorBean.getData(1);
                 ResolvedConceptReference ref =
                     (ResolvedConceptReference) list.get(0);
 
                 Entity c = null;
                 if (ref == null) {
                     String msg =
                         "Error: Null ResolvedConceptReference encountered.";
                     request.getSession().setAttribute("message", msg);
                     return "message";
 
                 } else {
                     c = ref.getReferencedEntry();
                     if (c == null) {
                         c =
                             DataUtils.getConceptByCode(scheme, null, null, ref
                                 .getConceptCode());
                     }
                 }
 
                 request.getSession().setAttribute("code", ref.getConceptCode());
                 request.getSession().setAttribute("concept", c);
                 request.getSession().setAttribute("type", "properties");
 
                 request.getSession().setAttribute("version", version);
 
 
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
                 return "concept_details";
             }
         }
 
         String message = "No match found.";
         if (matchAlgorithm.compareTo("exactMatch") == 0) {
             message = Constants.ERROR_NO_MATCH_FOUND_TRY_OTHER_ALGORITHMS;
         }
         request.setAttribute("message", message);
         return "no_match";
 
     }
 
 
 	//resolveValueSetAction
 
     public String resolveValueSetAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         return "resolve_value_set";
 
 	}
 
 
     public String continueResolveValueSetAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
         return "resolved_value_set";
 
 	}
 
 
     public String exportValueSetAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
         return "exported_value_set";
 
 	}
 
 
 }
