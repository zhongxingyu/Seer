 package gov.nih.nci.evs.browser.bean;
 
 
 import java.util.*;
 import java.net.URI;
 import java.io.*;
 
 
 import javax.faces.context.*;
 import javax.faces.event.*;
 import javax.faces.model.*;
 import javax.servlet.http.*;
 
 import gov.nih.nci.evs.browser.utils.*;
 import gov.nih.nci.evs.browser.utils.*;
 import gov.nih.nci.evs.browser.properties.*;
 import gov.nih.nci.evs.browser.common.*;
 
 import org.LexGrid.LexBIG.DataModel.Collections.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.LexBIGService.*;
 import org.LexGrid.LexBIG.Utility.*;
 import org.LexGrid.codingSchemes.*;
 import org.LexGrid.naming.*;
 import org.LexGrid.LexBIG.Impl.Extensions.GenericExtensions.*;
 import org.apache.log4j.*;
 import javax.faces.event.ValueChangeEvent;
 
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.commonTypes.Source;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.lexgrid.valuesets.dto.ResolvedValueSetDefinition;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import javax.servlet.ServletOutputStream;
 import org.LexGrid.concepts.*;
 import org.lexgrid.valuesets.dto.ResolvedValueSetCodedNodeSet;
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
 import org.LexGrid.concepts.Definition;
 import org.LexGrid.commonTypes.PropertyQualifier;
 import org.LexGrid.commonTypes.Property;
 
 
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
 
 public class ValueSetBean {
     private static Logger _logger = Logger.getLogger(ValueSetBean.class);
     private static List _rela_list = null;
     private static List _association_name_list = null;
     private static List _property_name_list = null;
     private static List _property_type_list = null;
     private static List _source_list = null;
 
     private static Vector _value_set_uri_vec = null;
     private static Vector _coding_scheme_vec = null;
     private static Vector _concept_domain_vec = null;
 
 
 
     public void ValueSetBean() {
 
 	}
 
 
 	private String selectedConceptDomain = null;
 	private List conceptDomainList = null;
 	private Vector<String> conceptDomainListData = null;
 
 
 	public String getSelectedConceptDomain() {
 		return this.selectedConceptDomain;
 	}
 
 	public void setSelectedConceptDomain(String selectedConceptDomain) {
 		this.selectedConceptDomain = selectedConceptDomain;
 	}
 
 	public List getConceptDomainList() {
 		if (conceptDomainList != null) return conceptDomainList;
 		conceptDomainListData = DataUtils.getConceptDomainNames();
 		conceptDomainList = new ArrayList();
 		for (int i=0; i<conceptDomainListData.size(); i++) {
 			String t = (String) conceptDomainListData.elementAt(i);
 			conceptDomainList.add(new SelectItem(t));
 		}
 
 		if (conceptDomainList.size() > 0) {
 			setSelectedConceptDomain(((SelectItem) conceptDomainList.get(0)).getLabel());
 		}
 		return conceptDomainList;
 	}
 
 
 
 
 	private String selectedOntology = null;
 	private List ontologyList = null;
 	private Vector<String> ontologyListData = null;
 
 
 	public String getSelectedOntology() {
 		return this.selectedOntology;
 	}
 
 	public void setSelectedOntology(String selectedOntology) {
 		this.selectedOntology = selectedOntology;
 	}
 
 
 	public List getOntologyList() {
 		if (ontologyList != null) return ontologyList;
 		Vector v = new Vector();
 		HashSet hset = new HashSet();
 		ontologyList = new ArrayList();
 
 /*
 		Vector vsd_vec = DataUtils.getValueSetDefinitionMetadata();
 		HashMap csURN2ValueSetMetadataHashMap = DataUtils.getCodingSchemeURN2ValueSetMetadataHashMap(vsd_vec);
 		Iterator it = csURN2ValueSetMetadataHashMap.keySet().iterator();
         while (it.hasNext()) {
            String cs = (String) it.next();
            if (!hset.contains(cs)) {
 		   	   v.add(cs);
 			   hset.add(cs);
 		   }
 	    }
 */
 
 
         if (ValueSetHierarchy._valueSetParticipationHashSet == null) return null;
         Iterator it = ValueSetHierarchy._valueSetParticipationHashSet.iterator();
         while (it.hasNext()) {
 			String cs = (String) it.next();
 			System.out.println(cs);
 			String formalName = DataUtils.getFormalName(cs);
 			String codingSchemeName = DataUtils.uri2CodingSchemeName(formalName);
 
 			System.out.println("ValueSet cs root: " + codingSchemeName);
 			v.add(codingSchemeName);
 		}
 
 		v = SortUtils.quickSort(v);
 
 		String display_name = "ALL";
 		ontologyList.add(new SelectItem(display_name));
 
 		for (int j = 0; j < v.size(); j++) {
             display_name = (String) v.elementAt(j);
 			ontologyList.add(new SelectItem(display_name));
 		}
 
 		if (ontologyList.size() > 0) {
 			setSelectedOntology(((SelectItem) ontologyList.get(0)).getLabel());
 		}
 		return ontologyList;
 
 	}
 
 /*
 	public List getOntologyList() {
 		if (ontologyList != null) return ontologyList;
 		List ontology_list = DataUtils.getOntologyList();
 		if (ontology_list == null) {
 			_logger.warn("??????????? ontology_list == null");
 		}
 		int num_vocabularies = ontology_list.size();
 
 		ontologyListData = DataUtils.getCodingSchemeFormalNames();
 		ontologyList = new ArrayList();
 		Vector v = new Vector();
 
 		HashSet hset = new HashSet();
 
 		for (int i = 0; i < ontology_list.size(); i++) {
 			SelectItem item = (SelectItem) ontology_list.get(i);
 			String value = (String) item.getValue();
 			String label = (String) item.getLabel();
 
 			String scheme = DataUtils.key2CodingSchemeName(value);
 	        if (!hset.contains(scheme)) {
 				String version = DataUtils.key2CodingSchemeVersion(value);
                 boolean isMapping = DataUtils.isMapping(scheme, version);
                 if (!isMapping) {
 					String display_name = DataUtils.getMetadataValue(scheme, version, "display_name");
 
 					if (display_name == null || display_name.compareTo("null") == 0) {
 						display_name = DataUtils.getLocalName(scheme);
 					}
 					v.add(display_name);
 					hset.add(scheme);
 			    }
 		    }
 		}
 
 
 		v = SortUtils.quickSort(v);
 
 		String display_name = "ALL";
 		ontologyList.add(new SelectItem(display_name));
 
 		for (int j = 0; j < v.size(); j++) {
             display_name = (String) v.elementAt(j);
 			ontologyList.add(new SelectItem(display_name));
 		}
 
 		if (ontologyList.size() > 0) {
 			setSelectedOntology(((SelectItem) ontologyList.get(0)).getLabel());
 		}
 		return ontologyList;
 	}
 */
 
 
 	private String selectedValueSetURI = null;
 	private List valueSetURIList = null;
 	private Vector<String> valueSetURIListData = null;
 
 
 	public String getSelectedValueSetURI() {
 		return this.selectedValueSetURI;
 	}
 
 	public void setSelectedValueSetURI(String selectedValueSetURI) {
 		this.selectedValueSetURI = selectedValueSetURI;
 		System.out.println("setSelectedValueSetURI: " + selectedValueSetURI);
 	}
 
 	public List getValueSetURIList() {
 		if (valueSetURIList != null) return valueSetURIList;
 		valueSetURIListData = DataUtils.getValueSetURIs();
 
 		valueSetURIList = new ArrayList();
 		for (int i=0; i<valueSetURIListData.size(); i++) {
 			String t = (String) valueSetURIListData.elementAt(i);
 			valueSetURIList.add(new SelectItem(t));
 		}
 
 		if (valueSetURIList.size() > 0) {
 			setSelectedValueSetURI(((SelectItem) valueSetURIList.get(0)).getLabel());
 		}
 		return valueSetURIList;
 	}
 
 
 	public void selectValueSetSearchOptionChanged(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
         request.getSession().setAttribute("selectValueSetSearchOption", newValue);
 	}
 
     public void valueSetURIChangedEvent(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
         setSelectedValueSetURI(newValue);
 	}
 
     public void ontologyChangedEvent(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
         setSelectedOntology(newValue);
 	}
 
     public void conceptDomainChangedEvent(ValueChangeEvent event) {
         if (event.getNewValue() == null)
             return;
         String newValue = (String) event.getNewValue();
         setSelectedConceptDomain(newValue);
 	}
 
 
 
 
     public String valueSetSearchAction() {
 		java.lang.String valueSetDefinitionRevisionId = null;
 		String msg = null;
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         String VSD_view = (String) request.getParameter("view");
         request.getSession().setAttribute("view", VSD_view);
         System.out.println("view: " + VSD_view);
 
 
         String selectValueSetSearchOption = (String) request.getParameter("selectValueSetSearchOption");
 
         String selectURI = (String) request.getParameter("selectedValueSetURI");
         if (selectURI == null) {
 			selectURI = getSelectedValueSetURI();
 		}
 		System.out.println("(*) valueSetSearchAction selectURI: " + selectURI);
 
         String selectCodingScheme = getSelectedOntology(); //(String) request.getParameter("selectedOntology");
         System.out.println("valueSetSearchAction selectCodingScheme: " + selectCodingScheme);
         String selectConceptDomain = getSelectedConceptDomain(); //(String) request.getParameter("selectConceptDomain");
 
 		System.out.println("(*) valueSetSearchAction selectValueSetSearchOption: " + selectValueSetSearchOption);
 		request.getSession().setAttribute("selectValueSetSearchOption", selectValueSetSearchOption);
 
 
         String algorithm = (String) request.getParameter("valueset_search_algorithm");
         request.getSession().setAttribute("valueset_search_algorithm", algorithm);
 
         String matchText = (String) request.getParameter("matchText");
 
 
         if (selectValueSetSearchOption.compareTo("CodingScheme") != 0) {
 			matchText = matchText.trim();
 
 			if (matchText.length() == 0) {
 				String message = "Please enter a search string.";
 				request.getSession().setAttribute("message", message);
 				// request.getSession().removeAttribute("matchText");
 				// request.removeAttribute("matchText");
 				return "message";
 			}
 			request.getSession().setAttribute("matchText_VSD", matchText);
 	    }
 
 
         Vector v = new Vector();
         LexEVSValueSetDefinitionServices vsd_service = null;
         vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 
 
         if (matchText != null) matchText = matchText.trim();
 		if (selectValueSetSearchOption.compareTo("Code") == 0) {
             String uri = null;
 
 			try {
 
 				AbsoluteCodingSchemeVersionReferenceList csVersionList = ValueSetHierarchy.getAbsoluteCodingSchemeVersionReferenceList();
 				String versionTag = "PRODUCTION";
 				List list = vsd_service.listValueSetsWithEntityCode(matchText, null, csVersionList, versionTag);
 
 				if (list != null) {
 					System.out.println("valueSetSearchAction listValueSetsWithEntityCode returns " + list.size() + " VSD URIs.");
 
 					for (int j=0; j<list.size(); j++) {
 						uri = (String) list.get(j);
 						System.out.println(uri);
 
 						try {
 							ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), null);
 							if (vsd == null) {
 								msg = "Unable to find any value set with URI " + selectURI + ".";
 								request.getSession().setAttribute("message", msg);
 								return "message";
 							}
 
 							String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 							if (metadata != null) {
 								v.add(metadata);
 							}
 
 						} catch (Exception ex) {
 							ex.printStackTrace();
 							msg = "Unable to find any value set with URI " + selectURI + ".";
 							request.getSession().setAttribute("message", msg);
 							return "message";
 						}
 
 					}
 				}
 
 				request.getSession().setAttribute("matched_vsds", v);
 				if (v.size() == 0) {
 					msg = "No match found.";
 					request.getSession().setAttribute("message", msg);
 					return "message";
 				} else if (v.size() == 1) {
 					request.getSession().setAttribute("vsd_uri", uri);
 				}
 
 				return "value_set";
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				System.out.println("vsd_service.listValueSetsWithEntityCode throws exceptions???");
 			}
 
 			msg = "Unexpected errors encountered; search by code failed.";
 			request.getSession().setAttribute("message", msg);
 
 			return "message";
 
 		} else if (selectValueSetSearchOption.compareTo("Name") == 0) {
             String uri = null;
 			try {
 
 				Vector uri_vec = DataUtils.getValueSetURIs();
                 for (int i=0; i<uri_vec.size(); i++) {
 					uri = (String) uri_vec.elementAt(i);
 					AbsoluteCodingSchemeVersionReferenceList csVersionList = null;
 					Vector cs_ref_vec = DataUtils.getCodingSchemeReferencesInValueSetDefinition(uri, "PRODUCTION");
 					if (cs_ref_vec != null) {
 						csVersionList = DataUtils.vector2CodingSchemeVersionReferenceList(cs_ref_vec);
 					}
 
 					ResolvedValueSetCodedNodeSet rvs_cns = null;
 					SortOptionList sortOptions = null;
 					LocalNameList propertyNames = null;
 					CodedNodeSet.PropertyType[] propertyTypes = null;
 
 					try {
 						rvs_cns = vsd_service.getValueSetDefinitionEntitiesForTerm(matchText, algorithm, new URI(uri), csVersionList, null);
 
 						if (rvs_cns != null) {
 							CodedNodeSet cns = rvs_cns.getCodedNodeSet();
 							ResolvedConceptReferencesIterator itr = cns.resolve(sortOptions, propertyNames, propertyTypes);
 							if (itr != null && itr.numberRemaining() > 0) {
  								AbsoluteCodingSchemeVersionReferenceList ref_list = rvs_cns.getCodingSchemeVersionRefList();
 								if (ref_list.getAbsoluteCodingSchemeVersionReferenceCount() > 0) {
 									try {
 										ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), null);
 										if (vsd == null) {
 											msg = "Unable to find any value set with name " + matchText + ".";
 											request.getSession().setAttribute("message", msg);
 											return "message";
 										}
 
 
 										String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 										if (metadata != null) {
 											v.add(metadata);
 										}
 
 									} catch (Exception ex) {
 										ex.printStackTrace();
 										msg = "Unable to find any value set with name " + matchText + ".";
 										request.getSession().setAttribute("message", msg);
 										return "message";
 									}
 								}
 						    }
 						}
 
 
 				    } catch (Exception ex) {
 					    System.out.println("WARNING: getValueSetDefinitionEntitiesForTerm throws exception???");
 						msg = "getValueSetDefinitionEntitiesForTerm throws exception -- search by \"" + matchText + "\" failed.";
 						request.getSession().setAttribute("message", msg);
 						return "message";
 					}
 				}
 
 				request.getSession().setAttribute("matched_vsds", v);
 
 				if (v.size() == 0) {
 					msg = "No match found.";
 					request.getSession().setAttribute("message", msg);
 					return "message";
 				} else if (v.size() == 1) {
 					request.getSession().setAttribute("vsd_uri", uri);
 				}
 
 				return "value_set";
 
 			} catch (Exception ex) {
 				//ex.printStackTrace();
 				System.out.println("vsd_service.getValueSetDefinitionEntitiesForTerm throws exceptions???");
 			}
 
 			msg = "Unexpected errors encountered; search by name failed.";
 			request.getSession().setAttribute("message", msg);
 			return "message";
 
 		}
 
 		else if (selectValueSetSearchOption.compareTo("CodingScheme") == 0) {
             String uri = null;
 			if (selectCodingScheme != null) {
 
 				if (selectCodingScheme.compareTo("ALL") != 0) {
 					String cs_name = DataUtils.uri2CodingSchemeName(DataUtils.getFormalName(selectCodingScheme));
 					if (cs_name != null) {
 						selectCodingScheme = cs_name;
 					}
 			    }
 
 				if (selectCodingScheme.compareTo("ALL") == 0) {
 					request.getSession().setAttribute("view", "terminology");
 					return "all_value_sets";
 				}
 
 				try {
                     List uri_list = vsd_service.getValueSetDefinitionURIsWithCodingScheme(selectCodingScheme,
                                                                            DataUtils.codingSchemeName2URI(selectCodingScheme));
 					for (int i=0; i<uri_list.size(); i++) {
 						uri = (String) uri_list.get(i);
 						ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), valueSetDefinitionRevisionId);
 						String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 						if (metadata != null) {
 							v.add(metadata);
 						}
 					}
 					if(uri_list.size() == 0) {
 						msg = "Unable to find any value set in which the coding scheme " + selectCodingScheme + " participates.";
 						request.getSession().setAttribute("message", msg);
 						return "message";
 					}
 
 
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					msg = "Unable to find any value set in which the coding scheme " + selectCodingScheme + " participates.";
 					request.getSession().setAttribute("message", msg);
 					return "message";
 				}
 		    }
 
 		    if (v.size() == 1) {
 				request.getSession().setAttribute("vsd_uri", uri);
 			}
 
 			request.getSession().setAttribute("matched_vsds", v);
 			return "value_set";
 
 		} else if (selectValueSetSearchOption.compareTo("Source") == 0) {
 			request.getSession().removeAttribute("selected_vsd");
 
 			String uri = null;
 
 			ValueSetDefinition selected_vsd = null;
 			v = new Vector();
 			try {
 				String supportedTag = "supportedSource";
 				supportedTag = "Source";
 				String value = matchText;
 				String source_uri = null;
 
 System.out.println("(*) selectValueSetSearchOption source: " + value);
 Vector schemes = new Vector();
 Vector versions = new Vector();
 schemes.add(ValueSetHierarchy.SOURCE_SCHEME);
 versions.add(ValueSetHierarchy.SOURCE_VERSION);
 
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new SearchUtils()
                         .searchByName(schemes, versions, matchText, null,
                             "exactMatch", false, 1);
                 if (wrapper != null) {
                     ResolvedConceptReferencesIterator iterator = wrapper.getIterator();
                     while (iterator.hasNext()) {
 						ResolvedConceptReference rcr = (ResolvedConceptReference) iterator.next();
 						value = rcr.getConceptCode();
 						break;
 					}
 				}
 
 Vector w = ValueSetHierarchy.getValueSetDefinitionsWithSource(value);
 SortUtils.quickSort(w);
 
 				if (w != null) {
 
 					for (int i=0; i<w.size(); i++) {
 						ValueSetDefinition vsd = (ValueSetDefinition) w.elementAt(i);
 						selected_vsd = vsd;
 						String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 						if (metadata != null) {
 							v.add(metadata);
 						}
 					}
 				}
 
 
 			} catch (Exception ex) {
 				System.out.println("WARNING: getValueSetDefinitionsWithSource throws exception???");
 				msg = "getValueSetDefinitionsWithSource throws exception -- search by \"" + matchText + "\" failed.";
 				request.getSession().setAttribute("message", msg);
 				return "message";
 			}
 
 			//request.getSession().setAttribute("matched_vsds", w);
 			request.getSession().setAttribute("matched_vsds", v);
 
 
 			if (v.size() == 0) {
 				msg = "No match found.";
 				request.getSession().setAttribute("message", msg);
 				return "message";
 			} else if (v.size() == 1) {
 				request.getSession().setAttribute("vsd_uri", uri);
 
 				request.getSession().setAttribute("vsd_uri", uri);
 				request.getSession().setAttribute("selected_vsd", selected_vsd);
 
 			}
 
 			return "value_set";
 /*
 		} else if (selectValueSetSearchOption.compareTo("ConceptDomain") == 0) {
 			System.out.println("valueSetSearchAction selectConceptDomain: " + selectConceptDomain);
 
 			if (selectConceptDomain != null) {
 
 				try {
                     List uri_list = vsd_service.getValueSetDefinitionURIsWithConceptDomain(selectConceptDomain,
                                                                            "http://lexevs.org/codingscheme/conceptdomain");
 
 System.out.println("(********) getValueSetDefinitionURIsWithConceptDomain returns " + uri_list.size());
 
 
 					for (int i=0; i<uri_list.size(); i++) {
 						String uri = (String) uri_list.get(i);
 						ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), valueSetDefinitionRevisionId);
 						String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 
 System.out.println("(********) metadata " + metadata);
 
 
 						if (metadata != null) {
 							v.add(metadata);
 						}
 					}
 
 					if(uri_list.size() == 0) {
 						msg = "Unable to find any value set with concept domain " + selectConceptDomain;
 						request.getSession().setAttribute("message", msg);
 						return "message";
 					}
 
 
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					msg = "Unable to find any value set with concept domain " + selectConceptDomain;
 					request.getSession().setAttribute("message", msg);
 					return "message";
 				}
 		    }
 			request.getSession().setAttribute("matched_vsds", v);
 			if (v.size() == 0) {
 				msg = "No match found.";
 				request.getSession().setAttribute("message", msg);
 				return "message";
 			}
 
 			return "value_set";
 */
 
 		}
 
 
 		msg = "WARNING: Unexpected error encountered.";
 		request.getSession().setAttribute("message", msg);
 		request.getSession().setAttribute("selectValueSetSearchOption",  selectValueSetSearchOption);
         return "message";
 	}
 
     public String selectCSVersionAction() {
 
  System.out.println("(************* ) selectCSVersionAction ");
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         String selectedvalueset = null;
         String uri = null;
         String multiplematches = (String) request.getParameter("multiplematches");
         if (multiplematches != null) {
 			selectedvalueset = (String) request.getParameter("valueset");
 			uri = selectedvalueset;
 		} else {
 			uri = (String) request.getParameter("vsd_uri");
 			if (uri.indexOf("|") != -1) {
 				Vector u = DataUtils.parseData(uri);
 				uri = (String) u.elementAt(1);
 			}
 		}
         //String uri = (String) request.getParameter("valueset");
 
         System.out.println("selectCSVersionAction: selected value set " + uri);
 		//request.getSession().setAttribute("selectedvalueset", uri);
 		request.getSession().setAttribute("vsd_uri", uri);
 
 
 /*
         String selectedvalueset = (String) request.getParameter("valueset");
 
 
         String[] valuesets = (String[]) request.getParameterValues("valueset");
         if (valuesets != null && valuesets.length > 0) {
             selectedvalueset = valuesets[0];
 		}
 
         System.out.println("selectCSVersionAction: selected value set " + selectedvalueset);
 		request.getSession().setAttribute("selectedvalueset", selectedvalueset);
 
 */
         return "select_value_set";
 	}
 
     public String resolveValueSetAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
         String selectedvalueset = null;
         //String selectedvalueset = (String) request.getParameter("valueset");
         String multiplematches = (String) request.getParameter("multiplematches");
         if (multiplematches != null) {
 			selectedvalueset = (String) request.getParameter("valueset");
 		} else {
 			selectedvalueset = (String) request.getParameter("vsd_uri");
 			if (selectedvalueset != null && selectedvalueset.indexOf("|") != -1) {
 				Vector u = DataUtils.parseData(selectedvalueset);
 				selectedvalueset = (String) u.elementAt(1);
 			}
 	    }
         String vsd_uri = selectedvalueset;
 		request.getSession().setAttribute("selectedvalueset", selectedvalueset);
 		/*
         String vsd_uri = null;
         if (selectedvalueset != null) {
 			vsd_uri = selectedvalueset;
 		} else {
 			vsd_uri = (String) request.getParameter("vsd_uri");
 			if (vsd_uri == null) {
 				vsd_uri = (String) request.getSession().getAttribute("vsd_uri");
 			}
 		}
 		*/
 
 String key = vsd_uri;
 
         request.getSession().setAttribute("vsd_uri", vsd_uri);
         String[] coding_scheme_ref = null;
 
System.out.println("(*) KLO resolveValueSetAction vsd_uri: " + vsd_uri);


         Vector w = DataUtils.getCodingSchemeReferencesInValueSetDefinition(vsd_uri, "PRODUCTION");
         if (w != null) {
 			coding_scheme_ref = new String[w.size()];
 			for (int i=0; i<w.size(); i++) {
 				String s = (String) w.elementAt(i);
 				coding_scheme_ref[i] = s;
 
 			}
 		}
 
         if (coding_scheme_ref == null || coding_scheme_ref.length == 0) {
 			String msg = "No PRODUCTION version of coding scheme is available.";
 			request.getSession().setAttribute("message", msg);
 			return "resolve_value_set";
 		}
 
 		AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
 
         for (int i=0; i<coding_scheme_ref.length; i++) {
 			String t = coding_scheme_ref[i];
 
 			System.out.println("(*) coding_scheme_ref: " + t);
 			String delim = "|";
 			if (t.indexOf("|") == -1) {
 				delim = "$";
 			}
 			Vector u = DataUtils.parseData(t, delim);
 			String uri = (String) u.elementAt(0);
 			String version = (String) u.elementAt(1);
 			if (version == null || version.compareTo("null") == 0) {
 				version = DataUtils.getVocabularyVersionByTag(uri, "PRODUCTION");
 			}
 			key = key + "|" + uri + "$" + version;
             csvList.addAbsoluteCodingSchemeVersionReference(Constructors.createAbsoluteCodingSchemeVersionReference(uri, version));
 		}
 
         request.getSession().setAttribute("coding_scheme_ref", coding_scheme_ref);
 
         long time = System.currentTimeMillis();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 		ResolvedValueSetDefinition rvsd = null;
 		int lcv = 0;
 		try {
 			ValueSetDefinition vsd = DataUtils.findValueSetDefinitionByURI(vsd_uri);
 			rvsd = vsd_service.resolveValueSetDefinition(vsd, csvList, null, null);
 			if(rvsd != null) {
 				ResolvedConceptReferencesIterator itr = rvsd.getResolvedConceptReferenceIterator();
 
 
 				IteratorBeanManager iteratorBeanManager = (IteratorBeanManager) FacesContext.getCurrentInstance().getExternalContext()
 				.getSessionMap().get("iteratorBeanManager");
 
 				if (iteratorBeanManager == null) {
 					iteratorBeanManager = new IteratorBeanManager();
 					request.getSession().setAttribute("iteratorBeanManager", iteratorBeanManager);
 				}
 
 				request.getSession().setAttribute("ResolvedConceptReferencesIterator", itr);
 
 				IteratorBean iteratorBean = iteratorBeanManager.getIteratorBean(key);
 				if (iteratorBean == null) {
 					iteratorBean = new IteratorBean(itr);
 					iteratorBean.initialize();
 					iteratorBean.setKey(key);
 					iteratorBeanManager.addIteratorBean(iteratorBean);
 				}
 
                 request.getSession().setAttribute("coding_scheme_ref", coding_scheme_ref);
 				request.getSession().setAttribute("ResolvedConceptReferencesIterator", itr);
 				request.getSession().setAttribute("resolved_vs_key", key);
 				return "resolved_value_set";
 		    }
 		} catch (Exception ex) {
 			System.out.println("??? vds.resolveValueSetDefinition throws exception");
 		}
 
 		String msg = "Unable to resolve the value set " + vsd_uri;
 		request.getSession().setAttribute("message", msg);
         return "resolved_value_set";
 	}
 
     // radio button implementation
     public String continueResolveValueSetAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         String vsd_uri = (String) request.getParameter("vsd_uri");
         if (vsd_uri == null) {
 			vsd_uri = (String) request.getSession().getAttribute("vsd_uri");
 		}
 
         request.getSession().setAttribute("vsd_uri", vsd_uri);
 
 
 Vector coding_scheme_ref_vec = DataUtils.getCodingSchemeReferencesInValueSetDefinition(vsd_uri);
 if (coding_scheme_ref_vec == null) {
 			String msg = "WARNING: No coding scheme version reference is available.";
 			request.getSession().setAttribute("message", msg);
 			return "resolve_value_set";
 }
 
 Vector cs_name_vec = DataUtils.getCodingSchemeURNsInValueSetDefinition(vsd_uri);
 
 		AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
 Vector ref_vec = new Vector();
 
 
 String key = vsd_uri;
 
 
         for (int i=0; i<cs_name_vec.size(); i++) {
 			String cs_name = (String) cs_name_vec.elementAt(i);
 
 			String version = (String) request.getParameter(cs_name);
 
 			if (version != null) {
 				csvList.addAbsoluteCodingSchemeVersionReference(Constructors.createAbsoluteCodingSchemeVersionReference(cs_name, version));
 				ref_vec.add(cs_name + "$" + version);
 				key = key + "|" + cs_name + "$" + version;
 		    }
 		}
 
 
         long time = System.currentTimeMillis();
 		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 		ResolvedValueSetDefinition rvsd = null;
 		int lcv = 0;
 		try {
 			ValueSetDefinition vsd = DataUtils.findValueSetDefinitionByURI(vsd_uri);
 
 			rvsd = vsd_service.resolveValueSetDefinition(vsd, csvList, null, null);
 			if(rvsd != null) {
 				ResolvedConceptReferencesIterator itr = rvsd.getResolvedConceptReferenceIterator();
 
 				IteratorBeanManager iteratorBeanManager = (IteratorBeanManager) FacesContext.getCurrentInstance().getExternalContext()
 				.getSessionMap().get("iteratorBeanManager");
 
 				if (iteratorBeanManager == null) {
 					iteratorBeanManager = new IteratorBeanManager();
 					request.getSession().setAttribute("iteratorBeanManager", iteratorBeanManager);
 				}
 
 				request.getSession().setAttribute("ResolvedConceptReferencesIterator", itr);
 
 				IteratorBean iteratorBean = iteratorBeanManager.getIteratorBean(key);
 				if (iteratorBean == null) {
 					iteratorBean = new IteratorBean(itr);
 					iteratorBean.initialize();
 					iteratorBean.setKey(key);
 					iteratorBeanManager.addIteratorBean(iteratorBean);
 				}
 				//request.getSession().setAttribute("ResolvedConceptReferencesIterator", itr);
 			}
 
 			String[] coding_scheme_ref = new String[ref_vec.size()];
 			for (int j=0; j<ref_vec.size(); j++) {
 				coding_scheme_ref[j] = (String) ref_vec.elementAt(j);
 			}
             request.getSession().setAttribute("coding_scheme_ref", coding_scheme_ref);
 
             request.getSession().setAttribute("resolved_vs_key", key);
 			return "resolved_value_set";
 
 
 		} catch (Exception ex) {
 			System.out.println("??? vds.resolveValueSetDefinition throws exception");
 		}
 
 		String msg = "Unable to resolve the value set " + vsd_uri;
 		request.getSession().setAttribute("message", msg);
         return "resolved_value_set";
 	}
 
 
 
 
     public String exportValueSetAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
         return "exported_value_set";
 
 	}
 
 
     public String valueSetDefinition2XMLString(String uri) {
 
         LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
         String s = null;
         String valueSetDefinitionRevisionId = null;
         try {
 			URI valueSetDefinitionURI = new URI(uri);
 			StringBuffer buf = vsd_service.exportValueSetDefinition(valueSetDefinitionURI, valueSetDefinitionRevisionId);
             s = buf.toString();
         } catch (Exception ex) {
            ex.printStackTrace();
         }
 		return s;
 	}
 
 
 
     public void exportVSDToXMLAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
         String selectedvalueset = null;
        String multiplematches = (String) request.getParameter("multiplematches");
         if (multiplematches != null) {
 			selectedvalueset = (String) request.getParameter("valueset");
 		} else {
 			selectedvalueset = (String) request.getParameter("vsd_uri");
 			if (selectedvalueset != null && selectedvalueset.indexOf("|") != -1) {
 				Vector u = DataUtils.parseData(selectedvalueset);
 				selectedvalueset = (String) u.elementAt(1);
 			}
 	    }
         String uri = selectedvalueset;
 		request.getSession().setAttribute("selectedvalueset", uri);
 
 		/*
         String uri = (String) request.getParameter("vsd_uri");
         if (uri == null) {
             uri = (String) request.getSession().getAttribute("vsd_uri");
 		}
 		*/
 
 
         String xml_str = valueSetDefinition2XMLString(uri);
 
 		try {
 			HttpServletResponse response = (HttpServletResponse) FacesContext
 					.getCurrentInstance().getExternalContext().getResponse();
 			response.setContentType("text/xml");
 
 			String vsd_name = DataUtils.valueSetDefiniionURI2Name(uri);
 			vsd_name = vsd_name.replaceAll(" ", "_");
 			vsd_name = vsd_name + ".xml";
 
 		    response.setHeader("Content-Disposition", "attachment; filename="
 					+ vsd_name);
 
 			response.setContentLength(xml_str.length());
 
 			ServletOutputStream ouputStream = response.getOutputStream();
 			ouputStream.write(xml_str.getBytes(), 0, xml_str.length());
 			ouputStream.flush();
 			ouputStream.close();
 
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 
 		FacesContext.getCurrentInstance().responseComplete();
 
 	}
 
 
     public void exportToXMLAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 		String valueSetDefinitionRevisionId = null;
         String[] coding_scheme_ref = (String[]) request.getSession().getAttribute("coding_scheme_ref");
 
         String uri = (String) request.getParameter("vsd_uri");
         if (uri == null) {
             uri = (String) request.getSession().getAttribute("vsd_uri");
 		}
 
         if (coding_scheme_ref == null || coding_scheme_ref.length == 0) {
 			String msg = "No coding scheme reference is selected.";
 			request.getSession().setAttribute("message", msg);
 			System.out.println(msg);
 			return;// "resolve_value_set";
 		}
 
 		AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
         for (int i=0; i<coding_scheme_ref.length; i++) {
 			String t = coding_scheme_ref[i];
 			//System.out.println("coding_scheme_ref " + t);
 			Vector u = DataUtils.parseData(t, "$");
 			String url = (String) u.elementAt(0);
 			String version = (String) u.elementAt(1);
             csvList.addAbsoluteCodingSchemeVersionReference(Constructors.createAbsoluteCodingSchemeVersionReference(url, version));
 		}
 
 		String csVersionTag = null;
 		boolean failOnAllErrors = false;
 
         try {
         	LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 
 			InputStream reader =  vsd_service.exportValueSetResolution(new URI(uri), valueSetDefinitionRevisionId,
 			   csvList, csVersionTag, failOnAllErrors);
 
 			if (reader != null) {
 				StringBuffer sb = new StringBuffer();
 				try {
 					for(int c = reader.read(); c != -1; c = reader.read()) {
 						sb.append((char)c);
 					}
 
 					HttpServletResponse response = (HttpServletResponse) FacesContext
 							.getCurrentInstance().getExternalContext().getResponse();
 					response.setContentType("text/xml");
 
 					String vsd_name = DataUtils.valueSetDefiniionURI2Name(uri);
 					vsd_name = vsd_name.replaceAll(" ", "_");
 					vsd_name = "resolved_" + vsd_name + ".xml";
 
 					response.setHeader("Content-Disposition", "attachment; filename="
 							+ vsd_name);
 
 
 					response.setContentLength(sb.length());
 					ServletOutputStream ouputStream = response.getOutputStream();
 					ouputStream.write(sb.toString().getBytes(), 0, sb.length());
 					ouputStream.flush();
 					ouputStream.close();
 
 				} catch(IOException e) {
 					throw e;
 				} finally {
 					try {
 						reader.close();
 					} catch(Exception e) {
 						// ignored
 					}
 			    }
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
         FacesContext.getCurrentInstance().responseComplete();
 	}
 
 
     public String getNCIDefinition(ResolvedConceptReference ref) {
 		if (ref == null) return null;
 		Entity concept = ref.getReferencedEntry();
 		if (concept == null) return null;
 		Definition[] definitions = concept.getDefinition();
 		Vector v = new Vector();
 		if (definitions == null) return null;
 		for (int i=0; i<definitions.length; i++) {
 			Definition definition = definitions[i];
 
 			System.out.println(definition.getPropertyName());
 			System.out.println(definition.getValue().getContent());
 
 			Source[] sources = definition.getSource();
 			for (int j=0; j<sources.length; j++)
 			{
 				Source src = sources[j];
 				String src_name = src.getContent();
 				System.out.println("\tsrc_name: " + src_name);
 				if (src_name.compareTo("NCI") == 0) {
 					v.add(definition.getValue().getContent());
 				}
 			}
 
 			PropertyQualifier[] qualifiers = definition.getPropertyQualifier();
 			for (int j=0; j<qualifiers.length; j++)
 			{
 				System.out.println("\tQualifier name: " + qualifiers[j].getPropertyQualifierName());
 				System.out.println("\tQualifier value: " + qualifiers[j].getValue().getContent());
 				String qualifier_value = qualifiers[j].getValue().getContent();
 				if (qualifier_value.compareTo("NCI") == 0) {
 					v.add(definition.getValue().getContent());
 				}
 			}
 		}
 		if (v.size() == 0) return null;
 		if (v.size() == 1) return (String) v.elementAt(0);
 
 		String def_str = "";
 		for (int i=0; i<v.size(); i++) {
 			String def = (String) v.elementAt(i);
 			if (i == 0) {
 				def_str = def;
 			} else {
 				def_str = def_str + "|" + def;
 			}
 		}
         return def_str;
 	}
 
 
 
     public void exportToCSVAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         ResolvedConceptReferencesIterator itr = null;
         String[] coding_scheme_ref = (String[]) request.getSession().getAttribute("coding_scheme_ref");
         String vsd_uri = (String) request.getSession().getAttribute("vsd_uri");
         if (coding_scheme_ref == null || coding_scheme_ref.length == 0) {
 			String msg = "No coding scheme reference is selected.";
 			request.getSession().setAttribute("message", msg);
 			return;// "resolve_value_set";
 		}
 
 		AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
         for (int i=0; i<coding_scheme_ref.length; i++) {
 			String t = coding_scheme_ref[i];
 			//System.out.println("(*) coding_scheme_ref: " + t);
 			Vector u = DataUtils.parseData(t, "$");
 			String uri = (String) u.elementAt(0);
 			String version = (String) u.elementAt(1);
             csvList.addAbsoluteCodingSchemeVersionReference(Constructors.createAbsoluteCodingSchemeVersionReference(uri, version));
 		}
 
    		StringBuffer sb = new StringBuffer();
 		try {
 			LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 			String valueSetDefinitionRevisionId = null;
 			String csVersionTag = null;
 
 			ResolvedValueSetCodedNodeSet rvs_cns = null;
 			rvs_cns = vsd_service.getCodedNodeSetForValueSetDefinition(new URI(vsd_uri),
 																		valueSetDefinitionRevisionId,
 																		csvList,
 																		csVersionTag);
 			CodedNodeSet cns = rvs_cns.getCodedNodeSet();
 
             SortOptionList sortOptions = null;
             LocalNameList filterOptions = null;
             LocalNameList propertyNames = null;//new LocalNameList();
             CodedNodeSet.PropertyType[] propertyTypes = new CodedNodeSet.PropertyType[1];
             propertyTypes[0] = CodedNodeSet.PropertyType.DEFINITION;
             boolean resolveObjects = true;
 
 			itr = cns.resolve(sortOptions, filterOptions, propertyNames, propertyTypes, resolveObjects);
 
 			sb.append("Code,");
 			sb.append("Name,");
 			sb.append("Terminology,");
 			sb.append("Version,");
 			sb.append("Namespace,");
 			sb.append("Definition");
 			sb.append("\r\n");
 
 			while (itr != null && itr.hasNext()) {
 				ResolvedConceptReference[] refs = itr.next(100).getResolvedConceptReference();
 				for (ResolvedConceptReference ref : refs) {
 					String entityDescription = "<NOT ASSIGNED>";
 					if (ref.getEntityDescription() != null) {
 						entityDescription = ref.getEntityDescription().getContent();
 					}
 
 					sb.append("\"" + ref.getConceptCode() + "\",");
 					sb.append("\"" + entityDescription + "\",");
 					sb.append("\"" + ref.getCodingSchemeName() + "\",");
 					sb.append("\"" + ref.getCodingSchemeVersion() + "\",");
 					sb.append("\"" + ref.getCodeNamespace() + "\",");
 
 					String definition = getNCIDefinition(ref);
 					if (definition == null) definition = "";
 					sb.append("\"" + definition + "\"");
 					sb.append("\r\n");
 				}
 			}
 		} catch (Exception ex)	{
 			sb.append("WARNING: Export to CVS action failed.");
 		}
 
 
 		vsd_uri = DataUtils.valueSetDefiniionURI2Name(vsd_uri);
 		vsd_uri = vsd_uri.replaceAll(" ", "_");
 		vsd_uri = "resolved_" + vsd_uri + ".txt";
 
 		HttpServletResponse response = (HttpServletResponse) FacesContext
 				.getCurrentInstance().getExternalContext().getResponse();
 		response.setContentType("text/csv");
 		response.setHeader("Content-Disposition", "attachment; filename="
 				+ vsd_uri);
 
 		response.setContentLength(sb.length());
 
 		try {
 			ServletOutputStream ouputStream = response.getOutputStream();
 			ouputStream.write(sb.toString().getBytes(), 0, sb.length());
 			ouputStream.flush();
 			ouputStream.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			sb.append("WARNING: Export to CVS action failed.");
 		}
 		FacesContext.getCurrentInstance().responseComplete();
 	}
 
 
 	public String searchAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
 		return "search_results";
 	}
 
 
 
     public String resolvedValueSetSearchAction() {
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         String vsd_uri = (String) request.getParameter("vsd_uri");
         if (vsd_uri == null) {
 			vsd_uri = (String) request.getSession().getAttribute("vsd_uri");
 		}
 
         request.getSession().setAttribute("vsd_uri", vsd_uri);
         String matchText = (String) request.getParameter("matchText");
         request.getSession().setAttribute("matchText_RVS", matchText);
 
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
 
         //Vector<org.LexGrid.concepts.Entity> v = null;
 
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
 
         Vector schemes = new Vector();
         schemes.add(vsd_uri);
 
         int maxToReturn = -1;
 
         String key =
             iteratorBeanManager.createIteratorKey(schemes, matchText,
                 searchTarget, matchAlgorithm, maxToReturn);
 
         if (searchTarget.compareTo("code") == 0) {
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new ValueSetSearchUtils().searchByCode(
 				        vsd_uri, matchText, maxToReturn);
 
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     }
                 }
             }
 
         } else if (searchTarget.compareTo("names") == 0) {
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new ValueSetSearchUtils().searchByName(
 				        vsd_uri, matchText, matchAlgorithm, maxToReturn);
 
                 if (wrapper != null) {
                     iterator = wrapper.getIterator();
                     if (iterator != null) {
                         iteratorBean = new IteratorBean(iterator);
                         iteratorBean.setKey(key);
                         iteratorBeanManager.addIteratorBean(iteratorBean);
                     }
                 }
             }
 
         } else if (searchTarget.compareTo("properties") == 0) {
             if (iteratorBeanManager.containsIteratorBean(key)) {
                 iteratorBean = iteratorBeanManager.getIteratorBean(key);
                 iterator = iteratorBean.getIterator();
             } else {
 				boolean excludeDesignation = true;
                 ResolvedConceptReferencesIteratorWrapper wrapper =
                     new ValueSetSearchUtils().searchByProperties(
 				        vsd_uri, matchText, excludeDesignation, matchAlgorithm, maxToReturn);
 
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
 
 		request.getSession().setAttribute("key", key);
 
 		System.out.println("(*************) setAttribute key: " + key);
 
         if (iterator != null) {
             request.getSession().setAttribute("key", key);
             //request.setAttribute("key", key);
             //System.out.println("(*************) setAttribute key: " + key);
 
             int numberRemaining = 0;
             try {
                 numberRemaining = iterator.numberRemaining();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             int size = iteratorBean.getSize();
 /*
             if (size > 1) {
                 request.getSession().setAttribute("search_results", v);
                 String match_size = Integer.toString(size);
                 request.getSession().setAttribute("match_size", match_size);
                 request.getSession().setAttribute("page_string", "1");
                 return "search_results";
 
             } else if (size == 1) {
                 request.getSession().setAttribute("singleton", "true");
 //                request.getSession().setAttribute("dictionary", scheme);// Constants.CODING_SCHEME_NAME);
                 int pageNumber = 1;
                 List list = iteratorBean.getData(1);
                 ResolvedConceptReference ref =
                     (ResolvedConceptReference) list.get(0);
 
 System.out.println("ref.getCodingSchemeURI(): " + ref.getCodingSchemeURI());
 System.out.println("ref.getCodingSchemeVersion(): " + ref.getCodingSchemeVersion());
 System.out.println("ref.getEntityDescription().getContent(): " + ref.getEntityDescription().getContent());
 System.out.println("ref.getConceptCode(): " + ref.getConceptCode());
 
                 Entity c = null;
                 if (ref == null) {
                     String msg =
                         "Error: Null ResolvedConceptReference encountered.";
                     request.getSession().setAttribute("message", msg);
 
 //                    request.getSession().setAttribute("dictionary", scheme);
                     return "message";
 
                 } else {
                     if (ref.getConceptCode() == null) {
                         String message =
                             "Code has not been assigned to the concept matches with '"
                                 + matchText + "'";
                         _logger.warn("WARNING: " + message);
                         request.getSession().setAttribute("message", message);
 
                        // request.getSession().setAttribute("dictionary", scheme);
                         return "message";
                     } else {
                         request.getSession().setAttribute("code",
                             ref.getConceptCode());
                     }
 
                     c = ref.getReferencedEntry();
 
                     if (c == null) {
 
 System.out.println("ref.getConceptCode(): " + ref.getConceptCode());
 
 
                            // to be modified
                            c = DataUtils.getConceptByCode(ref.getCodingSchemeURI(), null, null,
                                ref.getConceptCode());
                         if (c == null) {
                             String message =
                                 "Unable to find the concept with a code '"
                                     + ref.getConceptCode() + "'";
                             _logger.warn("WARNING: " + message);
                             request.getSession().setAttribute("message",
                                 message);
                            // request.getSession().setAttribute("dictionary",
                            //     scheme);
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
             */
             if (size > 0) {
                 //request.getSession().setAttribute("search_results", v);
                 String match_size = Integer.toString(size);
                 request.getSession().setAttribute("match_size", match_size);
                 request.getSession().setAttribute("page_string", "1");
                 request.getSession().setAttribute("vsd_uri", vsd_uri);
 
                 return "search_results";
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
         return "message";
     }
 }
