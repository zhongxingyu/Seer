 package gov.nih.nci.evs.browser.bean;
 
 import gov.nih.nci.evs.browser.utils.MailUtils;
 import gov.nih.nci.evs.browser.utils.SearchUtils;
 import gov.nih.nci.evs.browser.utils.UserInputException;
 import gov.nih.nci.evs.browser.utils.Utils;
 import gov.nih.nci.evs.browser.properties.NCItBrowserProperties;
 import gov.nih.nci.evs.browser.common.Constants;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.LexGrid.concepts.Concept;
 
 import gov.nih.nci.evs.browser.utils.DataUtils;
 import gov.nih.nci.evs.browser.utils.SortOption;
 
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction with the National Cancer Institute,
  * and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
  * in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  * "This product includes software developed by NGIT and the National Cancer Institute."
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
  * wherever such third-party acknowledgments normally appear.
  * 3. The names "The National Cancer Institute", "NCI" and "NGIT" must not be used to endorse or promote products derived from this software.
  * 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
  * the recipient to use any trademarks owned by either NCI or NGIT
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  * NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  *
  * Modification history
  *     Initial implementation kim.ong@ngc.com
  *
  */
 
 public class UserSessionBean extends Object {
     private static String contains_warning_msg = "(WARNING: Only a subset of results may appear due to current limits in the terminology server (see Known Issues on the Help page).)";
     private String selectedQuickLink = null;
     private List quickLinkList = null;
 
 
     public List<SelectItem> ontologyList = null;
     public List<String> ontologiesToSearchOn = null;
 
     public UserSessionBean() {
         ontologiesToSearchOn = new ArrayList<String>();
     }
 
 
     public void setSelectedQuickLink(String selectedQuickLink) {
         this.selectedQuickLink = selectedQuickLink;
         HttpServletRequest request = (HttpServletRequest) FacesContext
                 .getCurrentInstance().getExternalContext().getRequest();
         request.getSession().setAttribute("selectedQuickLink",
                 selectedQuickLink);
     }
 
     public String getSelectedQuickLink() {
         return this.selectedQuickLink;
     }
 
     public void quickLinkChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
 
         //System.out.println("quickLinkChanged; " + newValue);
         setSelectedQuickLink(newValue);
 
         HttpServletResponse response = (HttpServletResponse) FacesContext
                 .getCurrentInstance().getExternalContext().getResponse();
 
         String targetURL = null;//"http://nciterms.nci.nih.gov/";
         if (selectedQuickLink.compareTo("NCI Terminology Browser") == 0) {
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
         quickLinkList = new ArrayList();
         quickLinkList.add(new SelectItem("Quick Links"));
         quickLinkList.add(new SelectItem("NCI Terminology Browser"));
         quickLinkList.add(new SelectItem("NCI MetaThesaurus"));
         quickLinkList.add(new SelectItem("EVS Home"));
         quickLinkList.add(new SelectItem("NCI Terminology Resources"));
         return quickLinkList;
     }
 
 
     public String searchAction() {
         HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 
         String matchText = (String) request.getParameter("matchText");
         if (matchText != null) matchText = matchText.trim();
         //[#19965] Error message is not displayed when Search Criteria is not proivded
         if (matchText == null || matchText.length() == 0)
         {
             String message = "Please enter a search string.";
             request.getSession().setAttribute("message", message);
             request.getSession().removeAttribute("matchText");
             return "message";
         }
         request.getSession().setAttribute("matchText", matchText);
 
         String matchAlgorithm = (String) request.getParameter("algorithm");
         String searchTarget = (String) request.getParameter("searchTarget");
 
 System.out.println("============ searchTarget: " + searchTarget);
 
 
         request.getSession().setAttribute("searchTarget", searchTarget);
         request.getSession().setAttribute("algorithm", matchAlgorithm);
 
         boolean ranking = true;
 
         request.getSession().setAttribute("ranking", Boolean.toString(ranking));
         String source = (String) request.getParameter("source");
         if (source == null) {
             source = "ALL";
         }
 
         if (NCItBrowserProperties.debugOn) {
             try {
                 System.out.println(Utils.SEPARATOR);
                 System.out.println("* criteria: " + matchText);
                 //System.out.println("* matchType: " + matchtype);
                 System.out.println("* source: " + source);
                 System.out.println("* ranking: " + ranking);
                // System.out.println("* sortOption: " + sortOption);
             } catch (Exception e) {
             }
         }
 
         String scheme = request.getParameter("scheme");
         if (scheme == null) {
             scheme = (String) request.getAttribute("scheme");
         }
 
         if (scheme == null) {
             scheme = (String) request.getParameter("dictionary");
         }
 
         if (scheme == null) scheme = Constants.CODING_SCHEME_NAME;
 
 		Vector schemes = new Vector();
 		schemes.add(scheme);
 
 		String version = null;
 		Vector versions = new Vector();
 		versions.add(version);
 
         String max_str = null;
         int maxToReturn = -1;//1000;
         try {
             max_str = NCItBrowserProperties.getInstance().getProperty(NCItBrowserProperties.MAXIMUM_RETURN);
             maxToReturn = Integer.parseInt(max_str);
         } catch (Exception ex) {
 
         }
         Utils.StopWatch stopWatch = new Utils.StopWatch();
         Vector<org.LexGrid.concepts.Concept> v = null;
 
         boolean designationOnly = false;
 
         // check if this search has been performance previously through IteratorBeanManager
 		IteratorBeanManager iteratorBeanManager = (IteratorBeanManager) FacesContext.getCurrentInstance().getExternalContext()
 			.getSessionMap().get("iteratorBeanManager");
 
 		if (iteratorBeanManager == null) {
 			iteratorBeanManager = new IteratorBeanManager();
 			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("iteratorBeanManager", iteratorBeanManager);
 		}
 
         IteratorBean iteratorBean = null;
         ResolvedConceptReferencesIterator iterator = null;
         String key = iteratorBeanManager.createIteratorKey(schemes, matchText, searchTarget, matchAlgorithm, maxToReturn);
         if (searchTarget.compareTo("names") == 0) {
 			if (iteratorBeanManager.containsIteratorBean(key)) {
 				iteratorBean = iteratorBeanManager.getIteratorBean(key);
 				iterator = iteratorBean.getIterator();
 			} else {
        	    	iterator = new SearchUtils().searchByName(schemes, versions, matchText, source, matchAlgorithm, ranking, maxToReturn);
        	    	if (iterator != null) {
 					iteratorBean = new IteratorBean(iterator);
 					iteratorBean.setKey(key);
 					iteratorBeanManager.addIteratorBean(iteratorBean);
 				}
 			}
 
 		} else if (searchTarget.compareTo("properties") == 0) {
 			if (iteratorBeanManager.containsIteratorBean(key)) {
 				iteratorBean = iteratorBeanManager.getIteratorBean(key);
 				iterator = iteratorBean.getIterator();
 			} else {
                 iterator = new SearchUtils().searchByProperties(schemes, versions, matchText, source, matchAlgorithm, designationOnly, ranking, maxToReturn);
        	    	if (iterator != null) {
 					iteratorBean = new IteratorBean(iterator);
 					iteratorBean.setKey(key);
 					iteratorBeanManager.addIteratorBean(iteratorBean);
 				}
 			}
 
 		} else if (searchTarget.compareTo("relationships") == 0) {
 			designationOnly = true;
 			if (iteratorBeanManager.containsIteratorBean(key)) {
 				iteratorBean = iteratorBeanManager.getIteratorBean(key);
 				iterator = iteratorBean.getIterator();
 			} else {
                 iterator = new SearchUtils().searchByAssociations(schemes, versions, matchText, source, matchAlgorithm, designationOnly, ranking, maxToReturn);
        	    	if (iterator != null) {
 					iteratorBean = new IteratorBean(iterator);
 					iteratorBean.setKey(key);
 					iteratorBeanManager.addIteratorBean(iteratorBean);
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
 
         if (iterator != null) {
 
			request.getSession().setAttribute("key", key);
 
 			int numberRemaining = 0;
 			try {
 				numberRemaining = iterator.numberRemaining();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 
 /*
 
             IteratorBean iteratorBean = (IteratorBean) FacesContext.getCurrentInstance().getExternalContext()
                 .getSessionMap().get("iteratorBean");
 
             if (iteratorBean == null) {
                 iteratorBean = new IteratorBean(iterator);
                 FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("iteratorBean", iteratorBean);
             } else {
                 iteratorBean.setIterator(iterator);
             }
 */
             int size = iteratorBean.getSize();
             if (size > 1) {
                 request.getSession().setAttribute("search_results", v);
                 String match_size = Integer.toString(size);
                 request.getSession().setAttribute("match_size", match_size);
                 request.getSession().setAttribute("page_string", "1");
 
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
                 request.getSession().setAttribute("dictionary", scheme);
                 return "search_results";
 
             } else if (size == 1) {
                 request.getSession().setAttribute("singleton", "true");
                 request.getSession().setAttribute("dictionary", scheme);//Constants.CODING_SCHEME_NAME);
                 int pageNumber = 1;
                 List list = iteratorBean.getData(1);
                 ResolvedConceptReference ref = (ResolvedConceptReference) list.get(0);
                 Concept c = null;
                 if (ref == null) {
                     String msg = "Error: Null ResolvedConceptReference encountered.";
                     request.getSession().setAttribute("message", msg);
                     return "message";
 
                 } else {
 					if (ref.getConceptCode() == null) {
 						String message = "Code has not been assigned to the concept matches with '" + matchText + "'";
 						System.out.println("WARNING: " + message);
 						request.getSession().setAttribute("message", message);
 						request.getSession().setAttribute("dictionary", scheme);
 						return "message";
 					} else {
 						request.getSession().setAttribute("code", ref.getConceptCode());
 					}
 
                     c = ref.getReferencedEntry();
 
                     if (c == null) {
                         c = DataUtils.getConceptByCode(scheme, null, null, ref.getConceptCode());
                         if (c == null) {
 							String message = "Unable to find the concept with a code '" + ref.getConceptCode() + "'";
 							System.out.println("WARNING: " + message);
 							request.getSession().setAttribute("message", message);
 							request.getSession().setAttribute("dictionary", scheme);
 							return "message";
 					    }
                     } else {
 						request.getSession().setAttribute("code", c.getEntityCode());
 
 					}
                 }
                 request.getSession().setAttribute("concept", c);
                 request.getSession().setAttribute("type", "properties");
                 request.getSession().setAttribute("new_search", Boolean.TRUE);
                 /*
                 if (scheme.compareTo("NCI Thesaurus") == 0 || scheme.compareTo("NCI%20Thesaurus") == 0) {
                    return "concept_details";
 
                 } else if (scheme.indexOf("NCI Thesaurus") != -1 || scheme.indexOf("NCI%20Thesaurus") != -1 ) {
                     return "concept_details";
 
                 } else {
                     return "concept_details_other_term";
                 }
                 */
                 return "concept_details";
             }
         }
 
         String message = "No match found.";
         if (matchAlgorithm.compareTo(Constants.EXACT_SEARCH_ALGORITHM) == 0) {
             message = Constants.ERROR_NO_MATCH_FOUND_TRY_OTHER_ALGORITHMS;
         }
 
         else if (matchAlgorithm.compareTo(Constants.STARTWITH_SEARCH_ALGORITHM) == 0 && matchText.length() <= 2) {
             message = Constants.ERROR_ENCOUNTERED_TRY_NARROW_QUERY;
         }
 
         request.getSession().setAttribute("message", message);
         request.getSession().setAttribute("dictionary", scheme);
         return "message";
     }
 
 
     private String selectedResultsPerPage = null;
     private List resultsPerPageList = null;
 
     public List getResultsPerPageList() {
         resultsPerPageList = new ArrayList();
         resultsPerPageList.add(new SelectItem("10"));
         resultsPerPageList.add(new SelectItem("25"));
         resultsPerPageList.add(new SelectItem("50"));
         resultsPerPageList.add(new SelectItem("75"));
         resultsPerPageList.add(new SelectItem("100"));
         resultsPerPageList.add(new SelectItem("250"));
         resultsPerPageList.add(new SelectItem("500"));
 
         selectedResultsPerPage = ((SelectItem) resultsPerPageList.get(2))
                 .getLabel(); // default to 50
         return resultsPerPageList;
     }
 
     public void setSelectedResultsPerPage(String selectedResultsPerPage) {
         if (selectedResultsPerPage == null)
             return;
         this.selectedResultsPerPage = selectedResultsPerPage;
         HttpServletRequest request = (HttpServletRequest) FacesContext
                 .getCurrentInstance().getExternalContext().getRequest();
         request.getSession().setAttribute("selectedResultsPerPage",
                 selectedResultsPerPage);
     }
 
     public String getSelectedResultsPerPage() {
         HttpServletRequest request = (HttpServletRequest) FacesContext
                 .getCurrentInstance().getExternalContext().getRequest();
         String s = (String) request.getSession().getAttribute(
                 "selectedResultsPerPage");
         if (s != null) {
             this.selectedResultsPerPage = s;
         } else {
             this.selectedResultsPerPage = "50";
             request.getSession().setAttribute("selectedResultsPerPage", "50");
         }
         return this.selectedResultsPerPage;
     }
 
     public void resultsPerPageChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null) {
             return;
         }
         String newValue = (String) event.getNewValue();
         setSelectedResultsPerPage(newValue);
     }
 
     public String linkAction() {
         HttpServletRequest request = (HttpServletRequest) FacesContext
                 .getCurrentInstance().getExternalContext().getRequest();
         return "";
     }
 
     private String selectedAlgorithm = null;
     private List algorithmList = null;
 
     public List getAlgorithmList() {
         algorithmList = new ArrayList();
         algorithmList.add(new SelectItem("exactMatch", "exactMatch"));
         algorithmList.add(new SelectItem("startsWith", "Begins With"));
         algorithmList.add(new SelectItem("contains", "Contains"));
         selectedAlgorithm = ((SelectItem) algorithmList.get(0)).getLabel();
         return algorithmList;
     }
 
     public void algorithmChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
         setSelectedAlgorithm(newValue);
     }
 
     public void setSelectedAlgorithm(String selectedAlgorithm) {
         this.selectedAlgorithm = selectedAlgorithm;
         HttpServletRequest request = (HttpServletRequest) FacesContext
                 .getCurrentInstance().getExternalContext().getRequest();
         request.getSession().setAttribute("algorithm", selectedAlgorithm);
     }
 
     public String getSelectedAlgorithm() {
         return this.selectedAlgorithm;
     }
 
     public String contactUs() throws Exception {
         String msg = "Your message was successfully sent.";
         HttpServletRequest request = (HttpServletRequest) FacesContext
                 .getCurrentInstance().getExternalContext().getRequest();
 
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
 
 ////////////////////////////////////////////////////////////////////////////////////////////
 // ontologies
 
     public List getOntologiesToSearchOn() {
         if (ontologyList == null) {
             ontologyList = DataUtils.getOntologyList();
             SelectItem item = (SelectItem) ontologyList.get(0);
             ontologiesToSearchOn.add(item.getLabel());
         } else if (ontologiesToSearchOn.size() == 0) {
             SelectItem item = (SelectItem) ontologyList.get(0);
             ontologiesToSearchOn.add(item.getLabel());
         }
         return ontologiesToSearchOn;
     }
 
 
     public List getOntologyList() {
         if (ontologyList == null) {
             ontologyList = DataUtils.getOntologyList();
         }
         return ontologyList;
     }
 
     public void setOntologiesToSearchOn(List<String> newValue) {
         ontologiesToSearchOn = new ArrayList<String>();
         for (int i=0; i<newValue.size(); i++)
         {
              Object obj = newValue.get(i);
              ontologiesToSearchOn.add((String) obj);
         }
     }
 
     public void ontologiesToSearchOnChanged(ValueChangeEvent event) {
          if (event.getNewValue() == null) {
             return;
          }
 
          List newValue = (List)event.getNewValue();
          setOntologiesToSearchOn(newValue);
     }
 
 
     public List<SelectItem> ontologySelectionList = null;
     public String ontologyToSearchOn = null;
 
     public List getOntologySelectionList() {
         if (ontologySelectionList != null) return ontologySelectionList;
         List ontologies = getOntologyList();
         ontologySelectionList = new ArrayList<SelectItem>();
         String label = "Switch to another vocabulary (select one)";
         ontologySelectionList.add(new SelectItem(label, label));
         for (int i=0; i<ontologies.size(); i++)
         {
             SelectItem item = (SelectItem) ontologyList.get(i);
             ontologySelectionList.add(item);
         }
         return ontologySelectionList;
     }
 
 
     public void ontologySelectionChanged(ValueChangeEvent event) {
 
         if (event.getNewValue() == null) {
             //System.out.println("ontologySelectionChanged; event.getNewValue() == null ");
             return;
         }
         String newValue = (String) event.getNewValue();
 
         HttpServletResponse response = (HttpServletResponse) FacesContext
                 .getCurrentInstance().getExternalContext().getResponse();
 
         String targetURL = null;//"http://nciterms.nci.nih.gov/";
         targetURL = "http://nciterms.nci.nih.gov/";
         try {
             response.sendRedirect(response.encodeRedirectURL(targetURL));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public String getOntologyToSearchOn() {
         if (ontologySelectionList == null) {
             ontologySelectionList = getOntologySelectionList();
             SelectItem item = (SelectItem) ontologyList.get(1);
             ontologyToSearchOn = item.getLabel();
         }
         return ontologyToSearchOn;
     }
 
     public void setOntologyToSearchOn(String newValue) {
         ontologyToSearchOn = newValue;
     }
 
 
 ////////////////////////////////////////////////////////////////////////////////////////
 
    private String[] getSelectedVocabularies(String ontology_list_str) {
        Vector v = DataUtils.parseData(ontology_list_str);
        String[] ontology_list = new String[v.size()];
        for (int i=0; i<v.size(); i++) {
            String s = (String) v.elementAt(i);
            ontology_list[i] = s;
        }
        return ontology_list;
    }
 
    public String acceptLicenseAction() {
         HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		String dictionary = (String) request.getParameter("dictionary");
 		String code = (String) request.getParameter("code");
 
 		if (dictionary != null && code != null) {
 			LicenseBean licenseBean = (LicenseBean) request.getSession().getAttribute("licenseBean");
 			if (licenseBean == null) {
 				licenseBean = new LicenseBean();
 			}
 			licenseBean.addLicenseAgreement(dictionary);
 			request.getSession().setAttribute("licenseBean", licenseBean);
 
             Concept c = DataUtils.getConceptByCode(dictionary, null, null, code);
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
 
 
         HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		String scheme = (String) request.getParameter("scheme");
 		String version = (String) request.getParameter("version");
 
 		// Called from license.jsp
 		LicenseBean licenseBean = (LicenseBean) request.getSession().getAttribute("licenseBean");
 		if (scheme != null && version != null) {
 
 			if (licenseBean == null) {
 				licenseBean = new LicenseBean();
 			}
 			licenseBean.addLicenseAgreement(scheme);
 			request.getSession().setAttribute("licenseBean", licenseBean);
 			//request.getSession().removeAttribute("scheme");
 			//request.getSession().removeAttribute("version");
 		}
 
 String matchText = (String) request.getParameter("matchText");
 /*
 String calledFromLicense = (String) request.getParameter("calledFromLicense");
 if (calledFromLicense != null && calledFromLicense.compareTo("true") == 0) {
 	matchText = (String) request.getSession().getAttribute("matchText");
 } else {
 	matchText = (String) request.getParameter("matchText");
 }
 */
 
         if (matchText != null) {
 			matchText = matchText.trim();
 			request.getSession().setAttribute("matchText", matchText);
 		} else {
 			matchText = (String) request.getSession().getAttribute("matchText");
 		}
 
 		String multiple_search_error = (String) request.getSession().getAttribute("multiple_search_no_match_error");
 		request.getSession().removeAttribute("multiple_search_no_match_error");
 
         String matchAlgorithm = (String) request.getParameter("algorithm");
 		request.getSession().setAttribute("algorithm", matchAlgorithm);
         String searchTarget = (String) request.getParameter("searchTarget");
         request.getSession().setAttribute("searchTarget", searchTarget);
         System.out.println("searchTarget: " + searchTarget);
 
 	    String initial_search = (String) request.getParameter("initial_search");
         String[] ontology_list = request.getParameterValues("ontology_list");
 
         List list = new ArrayList<String>();
 
         String ontologiesToSearchOnStr = null;
         String ontology_list_str = null;
         List<String> ontologiesToSearchOn = null;
         int knt = 0;
 
         if (initial_search != null) { // from home page
             if (multiple_search_error != null) {
 				ontologiesToSearchOn = new ArrayList<String>();
 				ontologiesToSearchOnStr = (String) request.getSession().getAttribute("ontologiesToSearchOn");
 
 				if (ontologiesToSearchOnStr != null) {
 					Vector ontologies_to_search_on = DataUtils.parseData(ontologiesToSearchOnStr);
 					ontology_list = new String[ontologies_to_search_on.size()];
 					knt = ontologies_to_search_on.size();
 					for (int k=0; k<ontologies_to_search_on.size(); k++) {
 						String s = (String) ontologies_to_search_on.elementAt(k);
 						ontology_list[k] = s;
 						ontologiesToSearchOn.add(s);
 					}
 				}
 			}
 
 			if (ontology_list == null || ontology_list.length == 0) {
 				String message = Constants.ERROR_NO_VOCABULARY_SELECTED;//"Please select at least one vocabulary.";
 				request.getSession().setAttribute("warning", message);
 				request.getSession().setAttribute("message", message);
 				request.getSession().removeAttribute("ontologiesToSearchOn");
 				return "multiple_search";
 			} else {
 				ontologiesToSearchOn = new ArrayList<String>();
 
 				ontologiesToSearchOnStr = "|";
 				for (int i = 0; i < ontology_list.length; ++i) {
 					list.add(ontology_list[i]);
 					ontologiesToSearchOn.add(ontology_list[i]);
 					ontologiesToSearchOnStr = ontologiesToSearchOnStr + ontology_list[i] + "|";
 				}
 
 				if (ontology_list_str == null) {
 					ontology_list_str = "";
 					for (int i = 0; i < ontology_list.length; ++i) {
 						ontology_list_str = ontology_list_str + ontology_list[i];
 						if (i < ontology_list.length-1) {
 							ontology_list_str = ontology_list_str + "|";
 						}
 					}
 				}
 				request.getSession().setAttribute("ontologiesToSearchOn", ontologiesToSearchOnStr);
 			}
 		} else {
 			ontologiesToSearchOn = new ArrayList<String>();
             ontologiesToSearchOnStr = (String) request.getSession().getAttribute("ontologiesToSearchOn");
             if (ontologiesToSearchOnStr != null) {
                 Vector ontologies_to_search_on = DataUtils.parseData(ontologiesToSearchOnStr);
                 ontology_list = new String[ontologies_to_search_on.size()];
                 knt = ontologies_to_search_on.size();
                 for (int k=0; k<ontologies_to_search_on.size(); k++) {
                     String s = (String) ontologies_to_search_on.elementAt(k);
                     ontology_list[k] = s;
                     ontologiesToSearchOn.add(s);
                 }
             }
 
 		}
 
         String hide_ontology_list = "false";
         //[#19965] Error message is not displayed when Search Criteria is not proivided
         if (matchText == null || matchText.length() == 0)
         {
             String message = Constants.ERROR_NO_SEARCH_STRING_ENTERED;
 
             if (initial_search == null) {
 				hide_ontology_list = "true";
 			}
 			request.getSession().setAttribute("hide_ontology_list", hide_ontology_list);
             request.getSession().setAttribute("warning", message);
             request.getSession().setAttribute("message", message);
             return "multiple_search";
         }
 
         boolean ranking = true;
         String source = (String) request.getParameter("source");
         if (source == null) {
             source = "ALL";
         }
 
         if (NCItBrowserProperties.debugOn) {
             try {
                 System.out.println(Utils.SEPARATOR);
                 System.out.println("* criteria: " + matchText);
                 System.out.println("* source: " + source);
                 System.out.println("* ranking: " + ranking);
                 System.out.println("* ontology_list: ");
                 for (int i=0; i<ontology_list.length; ++i) {
                     System.out.println("  " + i + ") " + ontology_list[i]);
                 }
             } catch (Exception e) {
             }
         }
         if (ontology_list == null) {
             ontology_list_str = (String) request.getParameter("ontology_list_str"); // from multiple_search_results (hidden variable)
             if (ontology_list_str != null) {
                 ontology_list = getSelectedVocabularies(ontology_list_str);
 			}
 
         } else {
             knt = ontology_list.length;
 			if (knt == 0) {
 				String message = Constants.ERROR_NO_VOCABULARY_SELECTED;//"Please select at least one vocabulary.";
 				request.getSession().setAttribute("warning", message);
 				request.getSession().setAttribute("message", message);
 				request.getSession().setAttribute("hide_ontology_list", "true");
 	            request.getSession().removeAttribute("ontologiesToSearchOn");
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
             ontologiesToSearchOnStr = ontologiesToSearchOnStr + ontology_list[i] + "|";
         }
 
         if (ontology_list_str == null) {
             ontology_list_str = "";
             for (int i = 0; i < ontology_list.length; ++i) {
                 ontology_list_str = ontology_list_str + ontology_list[i];
                 if (i < ontology_list.length-1) {
                     ontology_list_str = ontology_list_str + "|";
                 }
             }
         }
 
         scheme = null;
         version = null;
 
         String t = "";
         if (ontologiesToSearchOn.size() == 0) {
             String message = Constants.ERROR_NO_VOCABULARY_SELECTED;//"Please select at least one vocabulary.";
             request.getSession().setAttribute("warning", message);
             request.getSession().setAttribute("message", message);
             request.getSession().removeAttribute("ontologiesToSearchOn");
             return "multiple_search";
         } else {
             request.getSession().setAttribute("ontologiesToSearchOn", ontologiesToSearchOnStr);
             for (int k=0; k<ontologiesToSearchOn.size(); k++) {
                 String key = (String) list.get(k);
                 if (key != null) {
                     scheme = DataUtils.key2CodingSchemeName(key);
                     version = DataUtils.key2CodingSchemeVersion(key);
                     if (scheme != null) {
                         schemes.add(scheme);
                         // to be modified (handling of versions)
                         versions.add(version);
                         t = t + scheme + " (" + version + ")" + "\n";
                         boolean isLicensed = LicenseBean.isLicensed(scheme, version);
                         if (licenseBean == null) {
                             licenseBean = new LicenseBean();
                             request.getSession().setAttribute("licenseBean", licenseBean);
                         }
 
                         boolean accepted = licenseBean.licenseAgreementAccepted(scheme);
                         if (isLicensed && !accepted) {
                             request.setAttribute("matchText", matchText);
                             request.setAttribute("searchTarget", searchTarget);
                             request.setAttribute("algorithm", matchAlgorithm);
                             request.setAttribute("ontology_list_str", ontology_list_str);
                             request.setAttribute("scheme", scheme);
                             request.setAttribute("version", version);
                             return "license";
                         }
                     } else {
                         System.out.println("Unable to identify " + key);
                     }
                 }
             }
         }
 
         String max_str = null;
         int maxToReturn = -1;//1000;
         try {
             max_str = NCItBrowserProperties
                     .getProperty(NCItBrowserProperties.MAXIMUM_RETURN);
             maxToReturn = Integer.parseInt(max_str);
         } catch (Exception ex) {
             // Do nothing
         }
         boolean designationOnly = false;
         ResolvedConceptReferencesIterator iterator = null;
         if (searchTarget.compareTo("names") == 0) {
        	    iterator = new SearchUtils().searchByName(schemes, versions, matchText, source, matchAlgorithm, ranking, maxToReturn);
 		} else if (searchTarget.compareTo("properties") == 0) {
             iterator = new SearchUtils().searchByProperties(schemes, versions, matchText, source, matchAlgorithm, designationOnly, ranking, maxToReturn);
 		} else if (searchTarget.compareTo("relationships") == 0) {
 			designationOnly = true;
             iterator = new SearchUtils().searchByAssociations(schemes, versions, matchText, source, matchAlgorithm, designationOnly, ranking, maxToReturn);
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
 			IteratorBean iteratorBean = (IteratorBean) FacesContext.getCurrentInstance().getExternalContext()
 				.getSessionMap().get("iteratorBean");
 
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
 				ResolvedConceptReference ref = (ResolvedConceptReference) list.get(0);
 
 				String coding_scheme = ref.getCodingSchemeName();
 				if (coding_scheme.compareToIgnoreCase("NCI Metathesaurus") == 0) {
 					String match_size = Integer.toString(size);;//Integer.toString(v.size());
 					request.getSession().setAttribute("match_size", match_size);
 					request.getSession().setAttribute("page_string", "1");
 					request.getSession().setAttribute("new_search", Boolean.TRUE);
 					//route to multiple_search_results.jsp
 					return "search_results";
 				}
 
 				request.getSession().setAttribute("singleton", "true");
 				request.getSession().setAttribute("dictionary", coding_scheme);
 				Concept c = null;
 				if (ref == null) {
 					String msg = "Error: Null ResolvedConceptReference encountered.";
 					request.getSession().setAttribute("message", msg);
 					return "message";
 
 				} else {
 					c = ref.getReferencedEntry();
 					if (c == null) {
 						c = DataUtils.getConceptByCode(coding_scheme, null, null, ref.getConceptCode());
 					}
 				}
 
 				request.getSession().setAttribute("code", ref.getConceptCode());
 				request.getSession().setAttribute("concept", c);
 				request.getSession().setAttribute("type", "properties");
 				request.getSession().setAttribute("new_search", Boolean.TRUE);
 
 				request.setAttribute("algorithm", matchAlgorithm);
 				coding_scheme = (String) DataUtils.localName2FormalNameHashMap.get(coding_scheme);
 
 				request.setAttribute("dictionary", coding_scheme);
 				return "concept_details";
 			}
 			else if (size > 0) {
 				String match_size = Integer.toString(size);;//Integer.toString(v.size());
 				request.getSession().setAttribute("match_size", match_size);
 				request.getSession().setAttribute("page_string", "1");
 				request.getSession().setAttribute("new_search", Boolean.TRUE);
 				//route to multiple_search_results.jsp
 				return "search_results";
 			}
         }
 
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
 
         else if (matchAlgorithm.compareTo(Constants.STARTWITH_SEARCH_ALGORITHM) == 0 && matchText.length() <= 2) {
             message = Constants.ERROR_ENCOUNTERED_TRY_NARROW_QUERY;
         }
 
 		hide_ontology_list = "false";
 /*
 		if (initial_search == null) {
 			hide_ontology_list = "true";
 		}
 */
 		request.getSession().setAttribute("hide_ontology_list", hide_ontology_list);
 		request.getSession().setAttribute("warning", message);
 		request.getSession().setAttribute("message", message);
 		request.getSession().setAttribute("ontologiesToSearchOn", ontologiesToSearchOnStr);
 		request.getSession().setAttribute("multiple_search_no_match_error", "true");
 
 		return "multiple_search";
     }
 
 
     public String acceptLicenseAgreement() {
         HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
         // update LicenseBean
         String dictionary = (String) request.getParameter("dictionary");
         String version = (String) request.getParameter("version");
 
         LicenseBean licenseBean = (LicenseBean) request.getSession().getAttribute("licenseBean");
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
 }
