 package gov.nih.nci.evs.valueseteditor.beans;
 
 import gov.nih.nci.evs.valueseteditor.utilities.*;
 
 import java.io.*;
 import java.util.*;
 import java.util.ResourceBundle;
 
 import javax.faces.component.html.HtmlSelectBooleanCheckbox;
 
 import org.apache.log4j.Logger;
 
 import javax.faces.context.*;
 import javax.faces.event.*;
 import javax.faces.model.*;
 import javax.servlet.http.*;
 
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
 import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 import org.lexgrid.valuesets.impl.LexEVSValueSetDefinitionServicesImpl;
 import javax.servlet.ServletOutputStream;
 
 
 
 import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
 import org.LexGrid.LexBIG.Utility.Constructors;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 import org.LexGrid.naming.Mappings;
 import org.LexGrid.naming.SupportedCodingScheme;
 import org.LexGrid.util.PrintUtility;
 import org.LexGrid.valueSets.DefinitionEntry;
 import org.LexGrid.valueSets.EntityReference;
 import org.LexGrid.valueSets.PropertyMatchValue;
 import org.LexGrid.valueSets.PropertyReference;
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.valueSets.types.DefinitionOperator;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 import org.lexgrid.valuesets.dto.ResolvedValueSetDefinition;
 import org.LexGrid.valueSets.ValueSetDefinitionReference;
 import org.LexGrid.commonTypes.EntityDescription;
 import org.LexGrid.valueSets.CodingSchemeReference;
 
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 
 import org.LexGrid.LexBIG.DataModel.Collections.SortOptionList;
 import org.LexGrid.LexBIG.Exceptions.LBException;
 
 import org.lexgrid.valuesets.dto.ResolvedValueSetCodedNodeSet;
 
 
 import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
 import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
 
 
 import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
 import org.LexGrid.codingSchemes.CodingScheme;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.LexBIG.DataModel.Core.types.CodingSchemeVersionStatus;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
 
 import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
 
 import org.LexGrid.LexBIG.Exceptions.LBInvocationException;
 import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
 
 import org.LexGrid.commonTypes.Source;
 import org.LexGrid.commonTypes.Properties;
 
 import org.LexGrid.naming.Mappings;
 import org.LexGrid.naming.SupportedCodingScheme;
 import org.LexGrid.naming.SupportedNamespace;
 import org.LexGrid.naming.SupportedConceptDomain;
 import org.LexGrid.naming.SupportedSource;
 
 import org.LexGrid.valueSets.CodingSchemeReference;
 
 import org.LexGrid.valueSets.ValueSetDefinitionReference;
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
 import org.LexGrid.concepts.Definition;
 import org.LexGrid.commonTypes.PropertyQualifier;
 import org.LexGrid.commonTypes.Property;
 import org.LexGrid.concepts.*;
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
  * Action bean for value set operations
  *
  * @author kim.ong@ngc.com, garciawa2
  */
 public class ValueSetBean {
 
 	// Local class variables
     private static Logger _logger = Logger.getLogger(ValueSetBean.class);
     private ResourceBundle resource = ResourceBundle.getBundle("gov.nih.nci.evs.valueseteditor.resources.Resources");
     private HashMap<String, ValueSetObject> _cart = null;
     private String _message = null;
 
     // Internal maps
     private String _selectedConceptDomain = null;
     //private Map<String,String> _selectedConceptDomainList = null;
     private String _selectedOntology = null;
 
 
     private boolean _isNotEmpty = false;
 
    //private Map<String,String> _selectedOntologyList = null;
 
     private List _selectedOntologyList = null;
 
     private List _ontologyList = null;
 
     private List _conceptDomainList = null;
     private List _sourceList = null;
     private List _isActiveList = null;
 
     // Metadata variables
     private String _uri = null;
     private String _name = null;
     private String _description = null;
 
     private String _owner = "NCI";
     private String _isActive = "true";
     private String _status = "1";
     private String _organizations = null;
 
     private String _sources = null;
     private String _selectedSource = null;
 
     private String _leafOnly = "false";
     private String _referenceAssociation = null;
     private String _targetToSource = null;
     private String _transitiveClosure = null;
 
     private String _expression = null;
     private String _selectValueSetReference = null;
 
     private boolean _new_vsd = true;
     private boolean _hasComponent = false;
 
     // Error messages
     static public final String NO_VALUE_SETS = "No value set has been created.";
     static public final String NOTHING_SELECTED = "No value sets selected.";
     static public final String DUPLICATE_VS = "Duplicate URI.";
 
 
     /**
      * Class constructor
      * @throws Exception
      */
     public ValueSetBean() throws Exception {
     	//ValueSetSearchUtil util = new ValueSetSearchUtil();
     	/*
     	DataUtils util = new DataUtils();
     	if (_selectedConceptDomainList == null)
     		_selectedConceptDomainList = util.getConceptDomainNames();
         */
 
     	if (_cart == null) _init();
 	}
 
 
 
 
     public ValueSetBean(ValueSetDefinition vsd) {
 
 
 	}
 
 
 
 
     public ComponentObject instantiateComponentObject() {
 		ComponentObject co = new ComponentObject();
 		return co;
 	}
 
 
 
 	public List getConceptDomainList() {
 		if (_conceptDomainList != null) return _conceptDomainList;
 		String codingSchemeName = "ConceptDomainCodingScheme";
 		String version = null;
 
 		Vector v = DataUtils.getConceptEntityNamesInCodingScheme(codingSchemeName, version);
 		if (v == null || v.size() == 0) {
 			v = new Vector();
 			v.add("Intellectual Product");
 		}
 		_conceptDomainList = new ArrayList();
 		for (int i=0; i<v.size(); i++) {
 			String t = (String) v.elementAt(i);
 			_conceptDomainList.add(new SelectItem(t));
 		}
 		return _conceptDomainList;
 	}
 
 	public List getSourceList() {
 		if (_sourceList != null) return _sourceList;
 		String codingSchemeName = "Terminology_Value_Set";
 		String version = null;
 
         String blank_str = "";
 		Vector v = DataUtils.getConceptEntityNamesInCodingScheme(codingSchemeName, version);
 		if (v == null || v.size() == 0) {
 			v = new Vector();
 			v.add("FDA");
 		}
 		_sourceList = new ArrayList();
 		_sourceList.add(new SelectItem(blank_str));
 		for (int i=0; i<v.size(); i++) {
 			String t = (String) v.elementAt(i);
 			_sourceList.add(new SelectItem(t));
 		}
 		return _sourceList;
 	}
 
 
 
 
     // ========================================================
     // ====               Getters & Setters                 ===
     // ========================================================
 
     public String getSelectValueSetReference() {
         return _selectValueSetReference;
     }
 
     public void setSelectValueSetReference(String reference) {
         _selectValueSetReference = reference;
     }
 
 
     public String getExpression() {
         return _expression;
     }
 
     public void setExpression(String expression) {
         _expression = expression;
     }
 
 
     public int getCount() {
         if (_cart == null) return 0;
         return _cart.size();
     }
 
     public Collection<ValueSetObject> getValueSetList() {
         if (_cart == null) _init();
         return _cart.values();
     }
 
     public ValueSetObject getValueSet(String uri) {
         if (_cart == null) {
         	_init();
         	return null;
         }
         if (_uri == null || _uri.length() < 1)
         	return null;
         return _cart.get(uri);
     }
 
 
     public ValueSetObject getCurrentValueSet() {
         if (_cart == null) {
         	_init();
         	return null;
         }
         if (_uri == null || _uri.length() < 1)
         	return null;
         return _cart.get(_uri);
     }
 
     public String getMessage() {
         return _message;
     }
 
     public String getOrganizations() {
         return _organizations;
     }
 
     public void setOrganizations(String organizations) {
         _organizations = organizations;
     }
 
     public String getOwner() {
         return _owner;
     }
 
     public void setOwner(String owner) {
         _owner = owner;
     }
 
     public String getStatus() {
         return _status;
     }
 
     public void setStatus(String status) {
         _status = status;
     }
 
     public String getIsActive() {
         return _isActive;
     }
 
     public void setIsActive(String isActive) {
         _isActive = isActive;
     }
 
     public String getUri() {
         return _uri;
     }
 
     public void setUri(String uri) {
         _uri = uri;
     }
 
     public String getName() {
         return _name;
     }
 
     public void setName(String name) {
         _name = name;
     }
 
 
         public String getDescription() {
             return this._description;
         }
 
         public void setDescription(String description) {
             this._description = description;
         }
 
 
     public boolean getHasComponent() {
     	if (_uri == null || _uri.length() < 1) return false;
     	int count = _cart.get(_uri).getCompListSize();
 
     	_hasComponent = false;
     	if (count > 0) {
 			_hasComponent = true;
 		}
 		return _hasComponent;
     }
 
 
 
     public int getComponentCount() {
     	if (_uri == null || _uri.length() < 1) return 0;
     	return _cart.get(_uri).getCompListSize();
     }
 
     public Collection<ComponentObject> getComponentObjectList() {
     	if (_uri == null || _uri.length() < 1) return null;
     	return _cart.get(_uri)._compList.values();
     }
 
     // =======================
 
 	public String getSelectedConceptDomain() {
 		return _selectedConceptDomain;
 	}
 
 	public void setSelectedConceptDomain(String selectedConceptDomain) {
 		this._selectedConceptDomain = selectedConceptDomain;
 	}
 
 /*
 	public Map<String,String> getConceptDomainList() {
 		return _selectedConceptDomainList;
 	}
 */
 
 	// =======================
 
 	public List getIsActiveList() {
 		if (_isActiveList != null) return _isActiveList;
 		_isActiveList = new ArrayList();
 		_isActiveList.add(new SelectItem("true"));
 		_isActiveList.add(new SelectItem("false"));
 		return _isActiveList;
 	}
 
 
 	public String getSelectedOntology() {
 		if (_selectedOntology == null) {
 			_selectedOntology = "NCI Thesaurus";
 		}
 		return _selectedOntology;
 	}
 
 	public void setSelectedOntology(String selectedOntology) {
 		_selectedOntology = selectedOntology;
 	}
 
 	public List getOntologyList() {
 		if (_ontologyList != null) return _ontologyList;
 		try {
 			_ontologyList = DataUtils.getOntologyList();
 		} catch (Exception ex) {
 
 		}
 		return _ontologyList;
 	}
 
 	// =======================
 
 	public String getSelectedSource() {
 		if (_selectedSource == null) {
 			_selectedSource = "";
 		}
 		return _selectedSource;
 	}
 
 	public void setSelectedSource(String selectedSource) {
 		this._selectedSource = selectedSource;
 	}
 
 
 
     // =======================
 
     /**
      * Get metadata sources
      * @return
      */
     public String getSources() {
         return _sources;
     }
 
     /**
      * Set metadata sources
      * @param sources
      */
     public void setSources(String sources) {
         _sources = sources;
     }
 
 
     public boolean getIsNewVSD() {
 		return _new_vsd;
 	}
 
 
     public boolean getIsNotEmpty() {
 		if (getCount() == 0) {
 			_isNotEmpty = false;
 		} else {
 			_isNotEmpty = true;
 		}
 		return _isNotEmpty;
 	}
 
     // ========================================================
     // ====                 Action Methods                  ===
     // ========================================================
 
 
     public String closeAction() {
 
 		String vs_uri = FacesUtil.getRequestParameter("uri");
 		System.out.println("(******** resolved_value_set.jsp closeAction ...vs_uri: " + vs_uri);
 
         if (vs_uri != null) {
 			setUri(vs_uri);
 		}
 
 	    return "close";
     }
 
 
     public String saveCopyAction() {
 		_selectValueSetReference = FacesUtil.getRequestParameter("selectValueSetReference");
 System.out.println("saveCopyAction _selectValueSetReference: " + _selectValueSetReference);
 
 
         ValueSetDefinition vsd = ValueSetUtils.getValueSetDefinitionByURI(_selectValueSetReference);
 
         if (vsd == null) {
 System.out.println("vsd == null???: ");
 
 		} else {
 System.out.println("vsd found: " + vsd.getValueSetDefinitionURI());
 System.out.println("vsd found: " + vsd.getValueSetDefinitionName());
 		}
 
     	_message = null;
 
      	_uri = FacesUtil.getRequestParameter("uri");
 
 			HttpServletRequest request =
 				(HttpServletRequest) FacesContext.getCurrentInstance()
 					.getExternalContext().getRequest();
 
         String selectValueSetReference = FacesUtil.getRequestParameter("selectValueSetReference");
         if (_uri == null || _uri.length() < 1) {
             _message = resource.getString("error_missing_uri");
 
 
 			request.setAttribute("selectValueSetReference", selectValueSetReference);
 			System.out.println("selectValueSetReference: " + selectValueSetReference);
 			request.setAttribute("uri_value", _uri);
 
 
             request.setAttribute("message", _message);
 
             _logger.debug("URI is null.");
             return "error";
         } else {
 			System.out.println("_uri: " + _uri);
 		}
 
 		boolean retval = DataUtils.validateVSDURI(_uri);
 		if (!retval) {
 			request.setAttribute("uri_value", _uri);
 			request.setAttribute("selectValueSetReference", selectValueSetReference);
 			_message = "WARNING: A value Set Definition with the same URI, " + _uri + ", already exists on the server.";
 			request.setAttribute("message", _message);
 			 return "error";
 		}
 
 
 		retval = validateVSDURI(_uri);
 		if (!retval) {
 			request.setAttribute("uri_value", _uri);
 			request.setAttribute("selectValueSetReference", selectValueSetReference);
 			_message = "WARNING: A value Set Definition with the same URI, " + _uri + ", already exists.";
 			request.setAttribute("message", _message);
 			 return "error";
 		}
 
         ValueSetObject item = new ValueSetObject();
 
     	item.setUri(_uri);
 
      	_name = vsd.getValueSetDefinitionName();
     	item.setUri(_uri);
     	item.setName(_name);
     	item.setStatus(vsd.getStatus());
     	item.setOwner(vsd.getOwner());
 
     	_organizations = "";
     	Mappings mappings = vsd.getMappings();
     	SupportedSource[] supportedSources = mappings.getSupportedSource();
     	for (int i=0; i<supportedSources.length; i++) {
 			SupportedSource supportedSource = supportedSources[i];
 
     	    System.out.println("supportedSources " + supportedSource.getContent());
     	    if (i == 0) {
 				_organizations = supportedSource.getContent();
 			} else {
 				_organizations = ";" + supportedSource.getContent();
 			}
 		}
     	item.setOrganizations(_organizations);
 
      	java.lang.Boolean isActive = vsd.isIsActive();
      	if (isActive != null && isActive.equals(Boolean.TRUE)) {
 			item.setIsActive("true");
 		} else {
 			item.setIsActive("false");
 		}
 
 		_description = "";
 		if (vsd.getEntityDescription() != null) {
 			_description = vsd.getEntityDescription().getContent();
 			if (_description == null) _description = "";
     		item.setDescription(_description);
 		}
 
 
 		System.out.println("_description: " + _description);
 
     	item.setConceptDomain(vsd.getConceptDomain());
     	System.out.println("ConceptDomain: " + vsd.getConceptDomain());
 
     	item.setCodingScheme(vsd.getDefaultCodingScheme());
 
     	System.out.println("DefaultCodingScheme: " + vsd.getDefaultCodingScheme());
 
         String cs_nm = DataUtils.getFormalName(vsd.getDefaultCodingScheme(), null);
     	setSelectedOntology(cs_nm);
 
     	System.out.println("DefaultCodingScheme (formal name): " + _selectedOntology);
 
         Source[] sources = vsd.getSource();
     	for (int i=0; i<vsd.getSourceCount(); i++) {
 			Source source = sources[i];
 			_selectedSource = source.getContent();
     	    item.setSources(_selectedSource);
     	    System.out.println("source: " + _selectedSource);
 		}
 
     	DefinitionEntry[] entries = vsd.getDefinitionEntry();
     	System.out.println("Number of DefinitionEntry: " + vsd.getDefinitionEntryCount());
     	System.out.println("entries.length(): " + entries.length);
 
         String component_label = null;
     	for (int i=0; i<entries.length; i++) {
 			DefinitionEntry entry = entries[i];
 
 			ComponentObject ob = definitionEntry2ComponentObject(entry);
 			int j = i+1;
 			String label = "C" + j;
 			component_label = label;
 			ob.setLabel(label);
 			ob.setDescription(label);
     	    item._compList.put(label, ob);
 		}
 		if (item.getCompListSize() == 0) {
 			item.setExpression(component_label);
 		}
 
         _cart.put(_uri,item);
         _new_vsd = false;
 
 		return "save_copy";
 	}
 
 	public ComponentObject definitionEntry2ComponentObject(DefinitionEntry entry) {
 		if (entry == null) return null;
 		ComponentObject ob = new ComponentObject();
 
 		DefinitionOperator operator = entry.getOperator();
 		System.out.println("operator: " + operator.toString());  //AND, OR, SUBTRACT
 
 
         java.lang.Long ruleOrder = entry.getRuleOrder();
         System.out.println("ruleOrder: " + ruleOrder);
 
 
 		CodingSchemeReference cs_ref = entry.getCodingSchemeReference();
 		if (cs_ref != null) {
 			ob.setType("EntireVocabulary");
 			ob.setVocabulary(cs_ref.getCodingScheme());
 		}
 
 		EntityReference entity_ref = entry.getEntityReference();
 		if (entity_ref != null) {
 			ob.setType("Relationship");
 			ob.setVocabulary(entity_ref.getEntityCodeNamespace());
 			ob.setFocusConceptCode(entity_ref.getEntityCode());
 
 			if (entity_ref.getReferenceAssociation() != null) {
 
 				ob.setRel_search_association(entity_ref.getReferenceAssociation());
 				Boolean isLeafOnly = entity_ref.isLeafOnly();
 				if (isLeafOnly != null && isLeafOnly.equals(Boolean.TRUE)) {
 					ob.setInclude_focus_node("false");
 				} else {
 					ob.setInclude_focus_node("true");
 				}
 				Boolean isTransitiveClosure = entity_ref.isTransitiveClosure();
 				if (isTransitiveClosure != null && isTransitiveClosure.equals(Boolean.TRUE)) {
 					ob.setTransitivity("true");
 				} else {
 					ob.setTransitivity("false");
 				}
 				Boolean isTargetToSource = entity_ref.isTargetToSource();
 				if (isTargetToSource != null && isTargetToSource.equals(Boolean.TRUE)) {
 					ob.setSelectedDirection("backward");
 				} else {
 					ob.setSelectedDirection("forward");
 				}
 			} else {
 				ob.setType("Code");
 			}
 		}
 
         ValueSetDefinitionReference vsd_ref = entry.getValueSetDefinitionReference();
         if (vsd_ref != null) {
 			ob.setType("ValueSetReference");
 			ob.setValueSetReference(vsd_ref.getValueSetDefinitionURI());
 		}
 
         PropertyReference prop_ref = entry.getPropertyReference();
         if (prop_ref != null) {
 			ob.setType("Property");
 			ob.setPropertyName(prop_ref.getPropertyName());
 			PropertyMatchValue pmv = prop_ref.getPropertyMatchValue();
 
 			ob.setAlgorithm(pmv.getMatchAlgorithm());
 			ob.setMatchText(pmv.getContent());
 		}
 
 /*
  java.lang.String 	getEntityCode()
           Returns the value of field 'entityCode'.
  java.lang.String 	getEntityCodeNamespace()
           Returns the value of field 'entityCodeNamespace'.
  java.lang.Boolean 	getLeafOnly()
           Returns the value of field 'leafOnly'.
  java.lang.String 	getReferenceAssociation()
           Returns the value of field 'referenceAssociation'.
  java.lang.Boolean 	getTargetToSource()
           Returns the value of field 'targetToSource'.
  java.lang.Boolean 	getTransitiveClosure()
           Returns the value of field 'transitiveClosure'.
  java.lang.Boolean 	isLeafOnly()
           Returns the value of field 'leafOnly'.
  java.lang.Boolean 	isTargetToSource()
           Returns the value of field 'targetToSource'.
  java.lang.Boolean 	isTransitiveClosure()
           Returns the value of field 'transitiveClosure'.
  */
 
 
 
 
 
 		// determine type
 
 
 
 		return ob;
 	}
 
     public String cancelResolveValueSetAction() throws Exception {
 
 		String vs_uri = FacesUtil.getRequestParameter("vs_uri");
 		System.out.println("(******** cancelComponentSubsetAction ...vs_uri: " + vs_uri);
 
         if (vs_uri != null) {
 			setUri(vs_uri);
 		}
 
 		return "cancel";
 	}
 
 
     public String cancelCopyAction() {
 
 		return "cancel_copy";
 	}
 
 
     public String copyFromServerAction() {
 
 		return "copyvalueset";
 	}
 
     public String selectMetadataTemplateAction() {
 
 		return "template";
 	}
 
     public String resolveVSDAction() {
 _logger.debug("===== resolveValueSetAction");
 		String resolved_vs_key = "";
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
 
         String expression = null;
 		expression = FacesUtil.getRequestParameter("expression");
 		if (expression != null) {
 			expression = expression.trim();
 		}
 
 
         request.getSession().removeAttribute("message");
     	_message = null;
     	String curr_uri = FacesUtil.getRequestParameter("uri");
     	_logger.debug("Resolving value set: " + curr_uri);
 
         ValueSetObject item = null;
         if (_cart.containsKey(curr_uri)) {
         	item = _cart.get(curr_uri);
 			System.out.println("URI: " + curr_uri);
 			if (item.getCompListSize() > 1) {
 				if (expression == null || expression.compareTo("") == 0) {
 					String msg = "WARNING: Please complete the value set expression.";
 					request.setAttribute("message", msg);
 					return "error";
 				}
 			}
 
 			item.setExpression(expression);
 			setExpression(expression);
 
 			resolved_vs_key = resolved_vs_key + curr_uri;
 
 			System.out.println("ConceptDomain: " + item.getConceptDomain());
 			System.out.println("DefaultCodingScheme: " + item.getCodingScheme());
 			System.out.println("Sources: " + item.getSources());
 			Map<String,ComponentObject> map = item.getCompList();
 			Iterator it = map.keySet().iterator();
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				ComponentObject ob = (ComponentObject) map.get(key);
 				System.out.println("(*) Label: " + ob.getLabel());
 				resolved_vs_key = resolved_vs_key + "$" + ob.getLabel();
 				System.out.println("\t Description: " + ob.getDescription());
 				System.out.println("\t Vocabulary: " + ob.getVocabulary());
 
 _logger.debug("\t Vocabulary: " + ob.getVocabulary());
 
 				System.out.println("\t Type: " + ob.getType());
 				System.out.println("\t MatchText: " + ob.getMatchText());
 				System.out.println("\t Algorithm: " + ob.getAlgorithm());
 				System.out.println("\n");
 			}
 		}
 
   /*      //expression
         String infixExpression = null;
         try {
 			infixExpression = FacesUtil.getRequestParameter("expression");
 			if (infixExpression != null) {
 				System.out.println("\ninfixExpression: " + infixExpression);
 				RPN rpn = new RPN(infixExpression);
 				Vector v = rpn.convertToPostfixExpression(infixExpression);
 				for (int i=0; i<v.size(); i++) {
 					String s = (String) v.elementAt(i);
 					System.out.println(s);
 				}
 				item.setExpression(infixExpression);
 				setExpression(infixExpression);
 			}
 	    } catch (Exception e) {
 			//e.printStackTrace();
 			System.out.println("infixExpression is null???");
 		}
 
 
     	_logger.debug("infixExpression: " + infixExpression);
 */
 		_logger.debug("Resolving value set: Step 1 ");
 
 				// To be implemented.
 
 		_logger.debug("Resolving value set: convertToValueSetDefinition infixExpression: " + expression);
 				ValueSetDefinition vsd = convertToValueSetDefinition(item, expression);
 
         int componentCount = item.getCompListSize();
         if (componentCount == 0) {
 
 						String msg = "WARNING: No component subset has been defined.";
 						request.setAttribute("message", msg);
 						return "error";
 		}
 
         if (componentCount > 1 && (expression == null || expression.length() == 0)) {
 
 						String msg = "WARNING: No value set expression has been defined.";
 						request.setAttribute("message", msg);
 						return "error";
 		}
 
 
 		if (vsd == null) {
 
 						String msg = "WARNING: Unable to convert the given expression to a value set definition: " + item.getExpression();
 						request.setAttribute("message", msg);
 						return "error";
 
 		} else {
 			_logger.debug("Value set definition uri: " + vsd.getValueSetDefinitionURI());
 			_logger.debug("Value set definition name: " + vsd.getValueSetDefinitionName());
 			_logger.debug("Value set definition: definitionentrycount: " + vsd.getDefinitionEntryCount());
 
 
 			 DefinitionEntry[] entries = vsd.getDefinitionEntry();
 			 for (int k=0; k<vsd.getDefinitionEntryCount(); k++) {
 				 DefinitionEntry entry = entries[k];
 				 PropertyReference prop_ref = entry.getPropertyReference();
 				 if (prop_ref != null) {
 					_logger.debug("Value set definition: prop_ref != null." );
 
 				 } else {
 					_logger.debug("Value set definition: prop_ref == null ???" );
 
 				 }
 
 			 }
 
 
 		}
 
 /*
 _logger.debug("Resolving value set: Step 2 ");
 
         HashMap<String, ValueSetDefinition> referencedVSDs = null;
         ResolvedConceptReferencesIterator iterator = ValueSetUtils.resolveValueSetDefinition(vsd, referencedVSDs);
 
 		if (iterator == null) {
 			String msg = "WARNING: Unable to resolve the value set definition.";
 			request.setAttribute("message", msg);
 			return "error";
 
 		}
 
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
 
 		String key = (String) request.getSession().getAttribute("resolved_vs_key");
 		if (key != null && key.compareTo("null") != 0) {
 			IteratorBean it_bean = iteratorBeanManager.removeIteratorBean(key);
 			ResolvedConceptReferencesIterator it = it_bean.getIterator();
 			try {
 				it.release();
 			} catch (Exception ex) {
 
 			}
 		}
 
 
 		key = resolved_vs_key;//vsd.getValueSetDefinitionURI();
 System.out.println("resolveValueSetAction key " + key);
 
 		iteratorBean = new IteratorBean(iterator);
 		iteratorBean.setKey(key);
 		iteratorBeanManager.addIteratorBean(iteratorBean);
 
 		int size = iteratorBean.getSize();
 
 System.out.println("resolveValueSetAction iteratorBean.getSize() " + size);
 
 		if (size > 0) {
 			request.getSession().setAttribute("value_set_object", item);
 			String match_size = Integer.toString(size);
 			request.getSession().setAttribute("match_size", match_size);
 			request.getSession().setAttribute("page_string", "1");
 			request.getSession().setAttribute("resolved_vs_key", key);
 
 
             System.out.println("search_results -- numbe of matches: " + size);
 			return "coding_scheme_references";
 		}
 
         String message = "No match found.";
 
         System.out.println("result: " + message);
 
         request.getSession().setAttribute("message", message);
         //request.getSession().setAttribute("dictionary", scheme);
         return "message";
 */
 
         request.setAttribute("uri", curr_uri);
         return "coding_scheme_references";
     }
 
 
 
 
     public String continueResolveValueSetAction() {
 _logger.debug("===== continueResolveValueSetAction");
 
 		String resolved_vs_key = "";
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
         request.getSession().removeAttribute("message");
     	_message = null;
     	String curr_uri = FacesUtil.getRequestParameter("uri");
     	_logger.debug("***************************** continueResolveValueSetAction Resolving value set: " + curr_uri);
 
 		//String expression = FacesUtil.getRequestParameter("expression");
 		//if (expression != null) expression = expression.trim();
 		String infixExpression = null;
 
         ValueSetObject item = null;
         if (_cart.containsKey(curr_uri)) {
         	item = _cart.get(curr_uri);
 
 			infixExpression = item.getExpression();;
 
 			System.out.println("continueResolveValueSetAction infixExpression: " + infixExpression);
 
 
 			System.out.println("URI: " + curr_uri);
 			if (item.getCompListSize() > 1) {
 				if (infixExpression == null || infixExpression.compareTo("") == 0) {
 					String msg = "WARNING: Please complete the value set expression.";
 					request.setAttribute("message", msg);
 					return "error";
 				}
 			}
 
 			resolved_vs_key = resolved_vs_key + curr_uri;
 
 			System.out.println("ConceptDomain: " + item.getConceptDomain());
 			System.out.println("DefaultCodingScheme: " + item.getCodingScheme());
 			System.out.println("Sources: " + item.getSources());
 			Map<String,ComponentObject> map = item.getCompList();
 			Iterator it = map.keySet().iterator();
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				ComponentObject ob = (ComponentObject) map.get(key);
 				System.out.println("(*) Label: " + ob.getLabel());
 				resolved_vs_key = resolved_vs_key + "$" + ob.getLabel();
 				System.out.println("\t Description: " + ob.getDescription());
 				System.out.println("\t Vocabulary: " + ob.getVocabulary());
 
 _logger.debug("\t Vocabulary: " + ob.getVocabulary());
 
 				System.out.println("\t Type: " + ob.getType());
 				System.out.println("\t MatchText: " + ob.getMatchText());
 				System.out.println("\t Algorithm: " + ob.getAlgorithm());
 				System.out.println("\n");
 			}
 		}
 
         //expression
 
         try {
 
 			if (infixExpression != null) {
 				System.out.println("\ninfixExpression: " + infixExpression);
 				RPN rpn = new RPN(infixExpression);
 				Vector v = rpn.convertToPostfixExpression(infixExpression);
 				for (int i=0; i<v.size(); i++) {
 					String s = (String) v.elementAt(i);
 					System.out.println(s);
 				}
 				item.setExpression(infixExpression);
 				setExpression(infixExpression);
 			}
 	    } catch (Exception e) {
 			//e.printStackTrace();
 			System.out.println("infixExpression is null???");
 		}
 
 
     	_logger.debug("infixExpression: " + infixExpression);
 
 		_logger.debug("Resolving value set: Step 1 ");
 
 				// To be implemented.
 
 		_logger.debug("Resolving value set: convertToValueSetDefinition infixExpression: " + infixExpression);
 				ValueSetDefinition vsd = convertToValueSetDefinition(item, infixExpression);
 
         int componentCount = item.getCompListSize();
         if (componentCount == 0) {
 
 						String msg = "WARNING: No component subset has been defined.";
 						request.setAttribute("message", msg);
 						return "error";
 		}
 
         if (componentCount > 1 && (infixExpression == null || infixExpression.length() == 0)) {
 
 						String msg = "WARNING: No value set expression has been defined.";
 						request.setAttribute("message", msg);
 						return "error";
 		}
 
 
 		if (vsd == null) {
 
 						String msg = "WARNING: Unable to convert the given expression to a value set definition: " + item.getExpression();
 						request.setAttribute("message", msg);
 						return "error";
 
 		} else {
 			_logger.debug("Value set definition uri: " + vsd.getValueSetDefinitionURI());
 			_logger.debug("Value set definition name: " + vsd.getValueSetDefinitionName());
 			_logger.debug("Value set definition: definitionentrycount: " + vsd.getDefinitionEntryCount());
 		}
 
 
 _logger.debug("Resolving value set: Step 2 ");
 
         HashMap<String, ValueSetDefinition> referencedVSDs = null;
 
 
 //////////////////////////////////////////////////////////////////
 
         //String vsd_uri = curr_uri;
         //request.getSession().setAttribute("vsd_uri", curr_uri);
 
 
     	String codingSchemeNames = FacesUtil.getRequestParameter("codingSchemeNames");
     	_logger.debug("codingSchemeNames: " + codingSchemeNames);
 
 // to be modified:
 
 		Vector cs_name_vec = DataUtils.parseData(codingSchemeNames);
 		AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
 		Vector ref_vec = new Vector();
 		//String key = vsd_uri;
 		String cs_ref_key = "";
 		int lcv = 0;
 
         for (int i=0; i<cs_name_vec.size(); i++) {
 			String cs_name = (String) cs_name_vec.elementAt(i);
 			String version = (String) request.getParameter(cs_name);
 			if (version != null) {
 				csvList.addAbsoluteCodingSchemeVersionReference(Constructors.createAbsoluteCodingSchemeVersionReference(cs_name, version));
 				//ref_vec.add(cs_name + "$" + version);
 				//key = key + "|" + cs_name + "$" + version;
 				if (lcv > 0) {
 					cs_ref_key = cs_ref_key + "|";
 				}
 				cs_ref_key = cs_ref_key + cs_name + "$" + version;
 				lcv++;
 		    }
 		}
 
 
         ResolvedConceptReferencesIterator iterator = ValueSetUtils.resolveValueSetDefinition(vsd, csvList, referencedVSDs);
 
 		if (iterator == null) {
 			String msg = "WARNING: Unable to resolve the value set definition.";
 			request.setAttribute("message", msg);
 			return "error";
 
 		}
 
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
 
 		String key = (String) request.getSession().getAttribute("resolved_vs_key");
 		if (key != null && key.compareTo("null") != 0) {
 			IteratorBean it_bean = iteratorBeanManager.removeIteratorBean(key);
 			ResolvedConceptReferencesIterator it = it_bean.getIterator();
 			try {
 				it.release();
 			} catch (Exception ex) {
 ex.printStackTrace();
 			}
 		}
 
 
 		key = resolved_vs_key;//vsd.getValueSetDefinitionURI();
 System.out.println("resolveValueSetAction key " + key);
 
 		iteratorBean = new IteratorBean(iterator);
 		iteratorBean.setKey(key);
 		iteratorBeanManager.addIteratorBean(iteratorBean);
 
 		int size = iteratorBean.getSize();
 
 System.out.println("resolveValueSetAction iteratorBean.getSize() " + size);
 
 		if (size > 0) {
 			request.getSession().setAttribute("value_set_object", item);
 			String match_size = Integer.toString(size);
 			request.getSession().setAttribute("match_size", match_size);
 			request.getSession().setAttribute("page_string", "1");
 			request.getSession().setAttribute("resolved_vs_key", key);
 
 
             System.out.println("search_results -- numbe of matches: " + size);
 
             request.setAttribute("cs_ref_key", cs_ref_key);
 
 			return "resolve";
 		}
 
         String message = "No match found.";
         System.out.println("result: " + message);
         request.getSession().setAttribute("message", message);
         return "message";
 
     }
 
 
 
 
     public String exportVSDToXMLAction() {
 		String expression = FacesUtil.getRequestParameter("expression");
 		if (expression != null) {
 			expression = expression.trim();
 			System.out.println("\nexpression: " + expression);
 		}
 
 		HttpServletRequest request =
 			(HttpServletRequest) FacesContext.getCurrentInstance()
 				.getExternalContext().getRequest();
         request.getSession().removeAttribute("message");
 
     	_message = null;
     	_logger.debug("Exporting value.");
 
     	String curr_uri = FacesUtil.getRequestParameter("uri");
     	_logger.debug("Exporting value set: " + curr_uri);
 
 System.out.println("exportVSDToXMLAction curr_uri: " + curr_uri);
 
         ValueSetObject item = null;
         if (_cart.containsKey(curr_uri)) {
         	item = _cart.get(curr_uri);
 			if (item.getCompListSize() > 1 && expression.compareTo("") == 0) {
 				String msg = "WARNING: Please complete the value set expression.";
 				request.setAttribute("message", msg);
 				return "error";
 			}
 
 			item.setExpression(expression);
 			setExpression(expression);
 
 			System.out.println("URI: " + curr_uri);
 			System.out.println("ConceptDomain: " + item.getConceptDomain());
 			System.out.println("DefaultCodingScheme: " + item.getCodingScheme());
 			System.out.println("Sources: " + item.getSources());
 			Map<String,ComponentObject> map = item.getCompList();
 			Iterator it = map.keySet().iterator();
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				ComponentObject ob = (ComponentObject) map.get(key);
 				System.out.println("(*) Label: " + ob.getLabel());
 				System.out.println("\t Description: " + ob.getDescription());
 				System.out.println("\t Vocabulary: " + ob.getVocabulary());
 				System.out.println("\t Type: " + ob.getType());
 				System.out.println("\t MatchText: " + ob.getMatchText());
 				System.out.println("\t Algorithm: " + ob.getAlgorithm());
 				System.out.println("\n");
 			}
 		} else {
 System.out.println("????? exportVSDToXMLAction curr_uri not found: " + curr_uri);
 
 		}
 
 /*
         try {
 			String infixExpression = FacesUtil.getRequestParameter("expression");
 			if (infixExpression != null) {
 				infixExpression = infixExpression.trim();
 				System.out.println("\ninfixExpression: " + infixExpression);
 				RPN rpn = new RPN(infixExpression);
 				Vector v = rpn.convertToPostfixExpression(infixExpression);
 				for (int i=0; i<v.size(); i++) {
 					String s = (String) v.elementAt(i);
 					System.out.println(s);
 				}
 
 
 		    }
 	    } catch (Exception e) {
 			//e.printStackTrace();
 			System.out.println("RPN error...");
 		}
 */
 
         // To be implemented.
 		ValueSetDefinition vsd = convertToValueSetDefinition(item, expression);
 		if (vsd == null) {
 			String msg = "ERROR: Unable to construct ValueSetDefinition based on the expression.";
 			request.setAttribute("message", msg);
 			return "error";
 		}
 
 
 
         try {
         	LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 			StringBuffer sb = vsd_service.exportValueSetDefinition(vsd);
 
 			HttpServletResponse response = (HttpServletResponse) FacesContext
 					.getCurrentInstance().getExternalContext().getResponse();
 			response.setContentType("text/xml");
 
 			String vsd_name = item.getName();
 			vsd_name = vsd_name.replaceAll(" ", "_");
 			vsd_name = vsd_name + ".xml";
 
 			response.setHeader("Content-Disposition", "attachment; filename="
 					+ vsd_name);
 
 			response.setContentLength(sb.length());
 			ServletOutputStream ouputStream = response.getOutputStream();
 			ouputStream.write(sb.toString().getBytes(), 0, sb.length());
 			ouputStream.flush();
 			ouputStream.close();
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
         FacesContext.getCurrentInstance().responseComplete();
 		return "export";
 	}
 
 
     public String editValueSetExpressionAction() {
 
 		return "expression";
 	}
 
 
 
     public String saveMetadataAction() {
     	_message = null;
         _logger.debug("Saving metadata to cart.");
 
         // Validate input
         if (_uri == null || _uri.length() < 1) {
             _message = resource.getString("error_missing_uri");
             _logger.debug("URI is null.");
             return "error";
         }
         /*
 		boolean retval = validateVSDURI(_uri);
 		if (!retval) {
 			_message = "WARNING: A value Set Definition with URI " + _uri + " already exists.";
 			request.setAttribute("message", _message);
 			 return "error";
 		}
 		*/
 
         ValueSetObject item = null;
         if (_cart.containsKey(_uri)) {
         	item = _cart.get(_uri);
         	_logger.debug("Updating value set.");
         } else {
         	item = new ValueSetObject();
         	_logger.debug("Adding value set to cart.");
         }
 
     	item.setUri(_uri);
     	item.setName(_name);
     	item.setStatus(_status);
     	item.setOwner(_owner);
 
     	item.setOrganizations(_organizations);
 
      	_isActive = FacesUtil.getRequestParameter("isActive");
     	item.setIsActive(_isActive);
 
      	_description = FacesUtil.getRequestParameter("vsd_description");
     	item.setDescription(_description);
 
     	item.setConceptDomain(_selectedConceptDomain);
     	item.setCodingScheme(_selectedOntology);
 
     	item.setSources(_selectedSource);
 
         _cart.put(_uri,item);
         _message = resource.getString("action_saved");
 
         _new_vsd = false;
 
         return "success";
     }
 
     // *** Value Set List Buttons
 
     public String newValueSetAction() {
     	_message = null;
     	_logger.debug("Creating new value set.");
 
     	clear();
     	return "newvalueset";
     }
 
     public boolean validateVSDURI(String uri) {
 		if (uri == null) return false;
 
 		if (getCount() == 0) return true;
        for (Iterator<ValueSetObject> i = getValueSetList().iterator(); i.hasNext();) {
             ValueSetObject item = (ValueSetObject)i.next();
 
             if (uri.compareToIgnoreCase(item.getUri()) == 0) {
 				return false;
             }
         }
         return true;
 	}
 
 
     public String removeFromCartAction() {
         int knt = 0;
     	_message = null;
         for (Iterator<ValueSetObject> i = getValueSetList().iterator(); i.hasNext();) {
             ValueSetObject item = (ValueSetObject)i.next();
             if (item.getCheckbox().isSelected()) {
 				knt++;
                 if (_cart.containsKey(item._uri)) {
                     i.remove();
 
                     setExpression("");
 				}
             }
         }
 
         if (knt == 0) {
 
 			HttpServletRequest request =
 				(HttpServletRequest) FacesContext.getCurrentInstance()
 					.getExternalContext().getRequest();
 
 			String message = "No item is selected.";
 			request.setAttribute("message", message);
 			return "error";
 		}
 
         return "removevalueset";
     }
 
     // Value Item Action Methods
 
     public String editValueSetAction() {
 
     	_message = null;
     	/*
     	String uriParam = FacesUtil.getRequestParameter("uriParam");
     	_logger.debug("Editing value set: " + uriParam);
 
     	loadUI(uriParam);
     	*/
      	String uri = FacesUtil.getRequestParameter("uri");
     	_logger.debug("Editing value set: " + uri);
 
     	loadUI(uri);
 
     	return "editvalueset";
     }
 
 
     // Component Action Methods
     public String addComponentAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
     	request.getSession().removeAttribute("vs_uri");
     	request.getSession().removeAttribute("vocabulary");
     	request.getSession().removeAttribute("matchText");
     	request.getSession().removeAttribute("editAction");
 
 
         String vs_uri = FacesUtil.getRequestParameter("vs_uri");
         request.getSession().setAttribute("vs_uri", vs_uri);
 
     	_message = null;
     	_logger.debug("Add component.");
 
         return "addcomponent";
     }
 
 
     public String editComponentAction() {
 
         HttpServletRequest request =
             (HttpServletRequest) FacesContext.getCurrentInstance()
                 .getExternalContext().getRequest();
 
     	_message = null;
     	//String labelParam = FacesUtil.getRequestParameter("labelParam");
         String vs_uri = FacesUtil.getRequestParameter("vs_uri");
     	String labelParam = FacesUtil.getRequestParameter("component_label");
 
 
     	_logger.debug("Edit component: " + labelParam);
 
         if (_cart == null) {
         	_init();
         	return "error";
         }
         if (_uri == null || _uri.length() < 1)
         	return "error";
         ValueSetObject vs_obj = _cart.get(vs_uri);
         ComponentObject component_obj = vs_obj.getComponent(labelParam);
 
         request.setAttribute("vs_uri", vs_uri);
         request.setAttribute("component_object", component_obj);
         request.setAttribute("component_label", labelParam);
 
     	return "editcomponent";
     }
 
     public String removeComponentAction() {
 
     	_message = null;
     	_logger.debug("Removing component.");
 
         return "removecomponent";
     }
 
     // ******************** Internal Class Methods ************************
 
     /**
      * Initialize the cart container
      */
     private void _init() {
         if (_cart == null) _cart = new HashMap<String, ValueSetObject>();
     }
 
     /**
      * Clear UI data
      */
     private void clear() {
     	_uri = null;
     	_name = null;
     	_description = null;
         _sources = null;
         _organizations = null;
         _selectedSource = null;
         _selectedConceptDomain = null;
         _selectedOntology = null;
         _selectValueSetReference = null;
         _new_vsd = true;
 
     }
 
     /**
      * Load UI with Value set data
      * @param uriParam
      */
     private void loadUI(String uriParam) {
     	ValueSetObject vso = _cart.get(uriParam);
     	_uri = vso.getUri();
         _sources = vso.getSources();
         _selectedConceptDomain = vso.getConceptDomain();
         _selectedOntology = vso.getCodingScheme();
     }
 
     /**
      * Subclass to hold contents of the cart
      * @author garciawa2
      */
     public class ValueSetObject {
 
 		private String _uri = null;
 		private String _name = null;
 		private String _description = null;
         private String _conceptDomain = null;
         private String _codingScheme = null;
         private String _organizations = null;
         private String _sources = null;
         private HtmlSelectBooleanCheckbox _checkbox = null;
         private Map<String,ComponentObject> _compList = null;
 
 		private String _owner = null;
 		private String _isActive = null;
 		private String _status = null;
 
 		private String _leafOnly = "false";
 		private String _referenceAssociation = null;
 		private String _targetToSource = null;
 		private String _transitiveClosure = null;
 
 		private String _expression = null;
 		private boolean _isNotEmpty = true;
 
         /**
          * Constructor
          */
         public ValueSetObject() {
         	_compList = new HashMap<String,ComponentObject>();
 		}
 
 
         public boolean getIsNotEmpty() {
 			if (getCompListSize() == 0) {
 				_isNotEmpty = false;
 			} else {
 				_isNotEmpty = true;
 			}
 			return _isNotEmpty;
 	    }
 
 
         // Getters & setters
 		public String getExpression() {
 			return _expression;
 		}
 
 		public void setExpression(String expression) {
 			_expression = expression;
 		}
 
 		public String getOwner() {
 			return _owner;
 		}
 
 		public void setOwner(String owner) {
 			_owner = owner;
 		}
 
 		public String getStatus() {
 			return _status;
 		}
 
 		public void setStatus(String status) {
 			_status = status;
 		}
 
 		public String getIsActive() {
 			return _isActive;
 		}
 
 		public void setIsActive(String isActive) {
 			_isActive = isActive;
 		}
 
 		public String getOrganizations() {
 			return _organizations;
 		}
 
 		public void setOrganizations(String organizations) {
 			_organizations = organizations;
 		}
 
 
 
         public String getUri() {
             return this._uri;
         }
 
         public void setUri(String uri) {
             this._uri = uri;
         }
 
         public String getName() {
             return this._name;
         }
 
         public void setName(String name) {
             this._name = name;
         }
 
         public String getDescription() {
             return this._description;
         }
 
         public void setDescription(String description) {
             this._description = description;
         }
 
 
         public String getConceptDomain() {
             return this._conceptDomain;
         }
 
         public void setConceptDomain(String conceptDomain) {
             this._conceptDomain = conceptDomain;
         }
 
         public String getCodingScheme() {
             return this._codingScheme;
         }
 
         public void setCodingScheme(String codingScheme) {
             this._codingScheme = codingScheme;
         }
 
         public String getSources() {
             return this._sources;
         }
 
         public void setSources(String sources) {
             this._sources = sources;
         }
 
         public HtmlSelectBooleanCheckbox getCheckbox() {
             return _checkbox;
         }
 
         public void setCheckbox(HtmlSelectBooleanCheckbox checkbox) {
             _checkbox = checkbox;
         }
 
         public Map<String,ComponentObject> getCompList() {
             return this._compList;
         }
 
         public int getCompListSize() {
             if (_compList == null) return 0;
             return _compList.size();
         }
 
         public ComponentObject getComponent(String component_label) {
 			if (_compList == null) return null;
 			return (ComponentObject) _compList.get(component_label);
 		}
 
         public void removeComponent(String component_label) {
 			if (_compList != null) {
 				_compList.remove(component_label);
 		    }
 		}
 
         // *** Private Methods ***
 
         private boolean getSelected() {
             return _checkbox.isSelected();
         }
 
 
         public void addComponent(String label, ComponentObject co) {
             if (_compList == null)  {
 				_compList = new HashMap<String,ComponentObject>();
 			}
             _compList.put(label, co);
 		}
 
         public boolean addComponent(ComponentObject co) {
             if (_compList == null)  {
 				_compList = new HashMap<String,ComponentObject>();
 			}
 			if (co.getLabel() != null) {
                 _compList.put(co.getLabel(), co);
                 return true;
 			} else {
 				return false;
 			}
 		}
 
     } // End of Concept
 
 
     public static void dumpComponentObject(ComponentObject ob) {
         System.out.println("Type: " + ob.getType());
         System.out.println("Label: " + ob.getLabel());
         System.out.println("Description: " + ob.getDescription());
         System.out.println("Vocabulary: " + ob.getVocabulary());
         System.out.println("PropertyName: " + ob.getPropertyName());
         System.out.println("MatchText: " + ob.getMatchText());
         System.out.println("Algorithm: " + ob.getAlgorithm());
         System.out.println("FocusConceptCode: " + ob.getFocusConceptCode());
         System.out.println("Rel_search_association: " + ob.getRel_search_association());
         System.out.println("Include_focus_node: " + ob.getInclude_focus_node());
         System.out.println("Transitivity: " + ob.getTransitivity());
         System.out.println("SelectedDirection: " + ob.getSelectedDirection());
         System.out.println("ValueSetReference: " + ob.getValueSetReference());
         System.out.println("Codes: " + ob.getCodes());
 
 
 
 	}
 
     public class ComponentObject {
         private String _label = null;
         private String _description = null;
 
         private String _vocabulary = null;
 
         private String _type = null;
 
         private String _propertyName = null;
 
         private String _matchText = null;
         private String _algorithm = null;
 
         private boolean _checkbox = false;
 
         private String _codes = null;
 
 
     private String _focusConceptCode = null;
     private String _rel_search_association = null;
     private String _include_focus_node = null;
     private String _transitivity = null;
 	private String _selectedDirection = null;
 	private String _selectValueSetReference = null;
	private String _selectCodingSchemeReference = null;
 
 
         public ComponentObject() {
 
 		}
 
         public String getValueSetReference() {
         	return _selectValueSetReference;
         }
 
         public void setValueSetReference(String selectValueSetReference) {
         	this._selectValueSetReference = selectValueSetReference;
         }
 
        public String getCodingSchemeReference() {
        	return _selectCodingSchemeReference;
        }
 
        public void setCodingSchemeReference(String selectCodingSchemeReference) {
        	this._selectCodingSchemeReference = selectCodingSchemeReference;
        }
 
 
         public String getCodes() {
         	return _codes;
         }
 
         public void setCodes(String codes) {
         	this._codes = codes;
         }
 
         public String getPropertyName() {
         	return _propertyName;
         }
 
         public void setPropertyName(String propertyName) {
         	this._propertyName = propertyName;
         }
 
         public String getFocusConceptCode() {
         	return _focusConceptCode;
         }
 
         public void setFocusConceptCode(String focusConceptCode) {
         	this._focusConceptCode = focusConceptCode;
         }
 
         public String getRel_search_association() {
          	return _rel_search_association;
         }
 
         public void setRel_search_association(String rel_search_association) {
         	this._rel_search_association = rel_search_association;
         }
 
 
         public String getInclude_focus_node() {
           	return _include_focus_node;
         }
 
         public void setInclude_focus_node(String include_focus_node) {
           	this._include_focus_node = include_focus_node;
         }
 
 
         public String getTransitivity() {
          	return _transitivity;
         }
 
         public void setTransitivity(String transitivity) {
          	this._transitivity = transitivity;
         }
 
         public String getSelectedDirection() {
         	return _selectedDirection;
         }
 
         public void setSelectedDirection(String selectedDirection) {
         	this._selectedDirection = selectedDirection;
         }
 
 
 
 
         // Getters & setters
         public boolean getCheckbox() {
         	return _checkbox;
         }
 
         public void getCheckbox(boolean checked) {
         	_checkbox = checked;
         }
 
         public String getLabel() {
         	return _label;
         }
 
         public void setLabel(String label) {
         	this._label = label;
         }
 
         public String getDescription() {
         	return _description;
         }
 
         public void setDescription(String description) {
         	this._description = description;
         }
 
 
         public String getVocabulary() {
         	return _vocabulary;
         }
 
         public void setVocabulary(String vocabulary) {
         	this._vocabulary = vocabulary;
         }
 
         public String getType() {
         	return _type;
         }
 
         public void setType(String type) {
         	this._type = type;
         }
 
         public String getMatchText() {
         	return _matchText;
         }
 
         public void setMatchText(String matchText) {
         	this._matchText = matchText;
         }
 
         public String getAlgorithm() {
         	return _algorithm;
         }
 
         public void setAlgorithm(String algorithm) {
         	this._algorithm = algorithm;
         }
 
         public String toString() {
 			return _label;
 		}
 
     } // End of ComponentObject
 
     //**
     //* Utility methods
     //**
 
     /**
      * Dump contents of cart object
      * (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("Listing cart contents...\n");
 
         if (_cart != null && _cart.size() > 0) {
             sb.append("\tCart:\n");
             for (Iterator<ValueSetObject> i = getValueSetList().iterator(); i.hasNext();) {
                 ValueSetObject item = (ValueSetObject)i.next();
                 sb.append("\t                URI  = " + item._uri + "\n");
                 sb.append("\t      Concept Domain = " + item._conceptDomain + "\n");
                 sb.append("\t       Coding Scheme = " + item._codingScheme + "\n");
                 sb.append("\t             Sources = " + item._sources + "\n");
                 //sb.append("\t            Selected = " + item.getSelected() + "\n");
                 sb.append("\t Component list size = " + item.getCompListSize() + "\n");
             }
         } else {
             sb.append("Cart is empty.");
         }
 
         return sb.toString();
     }
 
 
 
     // To be implemented
     public ValueSetOperand ComponentObject2ValueSetOperand(ComponentObject obj) {
 		ValueSetOperand operand = new ValueSetOperand();
 
 
 		return operand;
 
 	}
 
 
 //for testing only
       public ValueSetDefinition createNCItValueSetDefinition() {
 			ValueSetDefinition def = new ValueSetDefinition();
 			def.setDefaultCodingScheme("NCI_Thesaurus");
 			def.setValueSetDefinitionName("testName");
 			def.setValueSetDefinitionURI("testUri");
 
 			def.setMappings(new Mappings());
 			SupportedCodingScheme scs = new SupportedCodingScheme();
 			scs.setLocalId("NCI_Thesaurus");
 			scs.setUri("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#");
 			def.getMappings().addSupportedCodingScheme(scs);
 
 			DefinitionEntry entry = new DefinitionEntry();
 			entry.setRuleOrder(0l);
 			entry.setOperator(DefinitionOperator.OR);
 
 			EntityReference ref = new EntityReference();
 			ref.setEntityCode("C3262");
 			ref.setReferenceAssociation("subClassOf");
 			ref.setTransitiveClosure(true);
 			ref.setLeafOnly(false);
 			ref.setTargetToSource(true);
 			entry.setEntityReference(ref);
 			def.addDefinitionEntry(entry);
 			return def;
       }
 
 
     public DefinitionEntry componentObject2DefinitionEntry(ComponentObject ob) {
 
 		DefinitionEntry entry = new DefinitionEntry();
         //EntityReference entity_ref = new EntityReference();
         //entry.setEntityReference(entity_ref);
 
 		String type = ob.getType();
 
 		//Code, Name, Property, Relationship, EnumerationOfCodes, EntireVocabulary
 		if (type.compareTo("Code") == 0) {
 			EntityReference entity_ref = new EntityReference();
 			entity_ref.setEntityCode(ob.getFocusConceptCode());
 			String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 			entity_ref.setEntityCodeNamespace(cs_name);
 			entry.setEntityReference(entity_ref);
 
 		} else if (type.compareTo("Property") == 0) {
 			PropertyReference pr = new PropertyReference();
 			String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 			pr.setCodingScheme(cs_name);
 			pr.setPropertyName(ob.getPropertyName());
 
 			PropertyMatchValue pmv = new PropertyMatchValue();
 			pmv.setMatchAlgorithm(ob.getAlgorithm());
 			pmv.setContent(ob.getMatchText());
 
 			pr.setPropertyMatchValue(pmv);
 			entry.setPropertyReference(pr);
 
 		} else if (type.compareTo("Relationship") == 0) {
 			EntityReference entity_ref = new EntityReference();
 			entity_ref.setEntityCode(ob.getFocusConceptCode());
 			String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 			entity_ref.setEntityCodeNamespace(cs_name);
 
             if (ob.getInclude_focus_node() != null && ob.getInclude_focus_node().compareToIgnoreCase("true") == 0) {
 				entity_ref.setLeafOnly(Boolean.FALSE);
 			} else {
 				entity_ref.setLeafOnly(Boolean.TRUE);
 			}
 			entity_ref.setReferenceAssociation(ob.getRel_search_association());
 
             if (ob.getTransitivity() != null && ob.getTransitivity().compareToIgnoreCase("true") == 0) {
 				entity_ref.setTransitiveClosure(Boolean.TRUE);
 			} else {
 				entity_ref.setTransitiveClosure(Boolean.FALSE);
 			}
 
             if (ob.getSelectedDirection() != null && ob.getSelectedDirection().compareToIgnoreCase("forward") == 0) {
 				entity_ref.setTargetToSource(Boolean.FALSE);
 			} else {
 				entity_ref.setTargetToSource(Boolean.TRUE);
 			}
 			entry.setEntityReference(entity_ref);
 
 		} else if (type.compareTo("EntireVocabulary") == 0) {
             CodingSchemeReference codingSchemeReference = new CodingSchemeReference();
            codingSchemeReference.setCodingScheme(ob.getCodingSchemeReference());
             entry.setCodingSchemeReference(codingSchemeReference);
 
 		} else if (type.compareTo("ValueSetReference") == 0) {
             ValueSetDefinitionReference valueSetDefinitionReference = new ValueSetDefinitionReference();
             valueSetDefinitionReference.setValueSetDefinitionURI (ob.getValueSetReference());
 
             System.out.println("(* testing VSD URI *) ValueSetReference: " + ob.getValueSetReference());
 
             entry.setValueSetDefinitionReference(valueSetDefinitionReference);
         }
 
 		return entry;
 	}
 
 
     public Mappings constructVSDMappings(ValueSetObject vs_obj) {
 		Mappings mappings = new Mappings();
 
 		Vector w = findSupportedCodingSchemes(vs_obj);
 		if (w != null) {
 			for (int i=0; i<w.size(); i++) {
 				SupportedCodingScheme scs = (SupportedCodingScheme) w.elementAt(i);
 				mappings.addSupportedCodingScheme(scs);
 			}
 		} else {
             System.out.println("ValueSetBean constructVSDMappings findSupportedCodingSchemes returns NULL??? ");
 
 		}
 
 		String conceptDomain = vs_obj.getConceptDomain();
 		if (conceptDomain != null) {
 			SupportedConceptDomain scd = new SupportedConceptDomain();
 			scd.setLocalId(conceptDomain);
 			mappings.addSupportedConceptDomain(scd);
 		}
 
 		String organizations = vs_obj.getOrganizations();
 		String delimiter = null;
 		if (organizations.indexOf(",") != -1) {
 			delimiter = ",";
 			Vector org_vec = DataUtils.parseData(organizations, delimiter);
 			if (org_vec != null) {
 				for (int k=0; k<org_vec.size(); k++) {
 					String s = (String) org_vec.elementAt(k);
 					s = s.trim();
 					SupportedSource src = new SupportedSource();
 					src.setLocalId(s);
 					mappings.addSupportedSource(src);
 				}
 			}
 		}
 		else if (organizations.indexOf("|") != -1) {
 			delimiter = "|";
 			Vector org_vec = DataUtils.parseData(organizations, delimiter);
 			if (org_vec != null) {
 				for (int k=0; k<org_vec.size(); k++) {
 					String s = (String) org_vec.elementAt(k);
 					s = s.trim();
 					SupportedSource src = new SupportedSource();
 					src.setLocalId(s);
 					mappings.addSupportedSource(src);
 				}
 			}
 		} else {
 			String s = organizations;
 			s = s.trim();
 			SupportedSource src = new SupportedSource();
 			src.setLocalId(s);
 			mappings.addSupportedSource(src);
 		}
 
 
 		for (int i=0; i<w.size(); i++) {
 			SupportedCodingScheme scs = (SupportedCodingScheme) w.elementAt(i);
 
 			String localId = scs.getLocalId();
 			String uri = scs.getUri();
 			SupportedNamespace ns = new SupportedNamespace();
 			ns.setLocalId(localId);
 			ns.setUri(uri);
 			ns.setEquivalentCodingScheme(localId);
 			mappings.addSupportedNamespace(ns);
 		}
 		return mappings;
 	}
 
 
 	public AbsoluteCodingSchemeVersionReferenceList getAbsoluteCodingSchemeVersionReferenceListForValueSetDefinition(ValueSetObject vs_obj) {
 		Vector codingSchemeName_vec = findSupportedCodingSchemes(vs_obj);
 		if (codingSchemeName_vec == null) return null;
         return DataUtils.getAbsoluteCodingSchemeVersionReferenceListForValueSetDefinition(codingSchemeName_vec);
     }
 
 
     // to be modified KLO
 	public Vector findParticipatingCodingSchemes(ValueSetObject vs_obj) {
 
 System.out.println("Calling ValuSetBean.findParticipatingCodingSchemes...");
 
 		Vector v = new Vector();
 		if (vs_obj == null) {
 			System.out.println("WARNING: vs_obj is null???");
 			v.add("NCI_Thesaurus");
 			return v;
 		}
 
 		HashSet hset = new HashSet();
 
 System.out.println("vs_obj.getCodingScheme(): " + vs_obj.getCodingScheme());
 
 		if (vs_obj.getCodingScheme() != null) {
 
 String cs_name = DataUtils.getCodingSchemeName(vs_obj.getCodingScheme(), null);
 System.out.println("cs_name 1 " + cs_name);
 
 			hset.add(cs_name);
 			v.add(cs_name);
 		}
 
 		Map<String,ComponentObject> map = vs_obj.getCompList();
 		Iterator it = map.keySet().iterator();
 		while (it.hasNext()) {
 			String key = (String) it.next();
 			ComponentObject ob = (ComponentObject) map.get(key);
 
 			if (ob.getType().compareTo("ValueSetReference") != 0) {
 				String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 				System.out.println("cs_name 2 " + cs_name);
 
 				if (!hset.contains(cs_name)) {
 					hset.add(cs_name);
 					v.add(cs_name);
 				}
 		    }
 		}
 
 		hset.clear();
 		return v;
 	}
 
 
 
 
 	public Vector findSupportedCodingSchemes(ValueSetObject vs_obj) {
 		if (vs_obj == null) return null;
 		HashSet hset = new HashSet();
 		Vector v = new Vector();
 		if (vs_obj.getCodingScheme() != null) {
 
 String cs_name = DataUtils.getCodingSchemeName(vs_obj.getCodingScheme(), null);
 System.out.println("cs_name 1 " + cs_name);
 
 			hset.add(cs_name);
 			v.add(cs_name);
 		}
 
 		Map<String,ComponentObject> map = vs_obj.getCompList();
 		Iterator it = map.keySet().iterator();
 		while (it.hasNext()) {
 			String key = (String) it.next();
 			ComponentObject ob = (ComponentObject) map.get(key);
 
 			if (ob.getType().compareTo("ValueSetReference") != 0) {
 				String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 				System.out.println("cs_name 2 " + cs_name);
 
 				if (!hset.contains(cs_name)) {
 					hset.add(cs_name);
 					v.add(cs_name);
 				}
 		    }
 		}
 
 		hset.clear();
 		Vector w = new Vector();
 		try {
 			for (int i=0; i<v.size(); i++) {
 				String codingSchemeName = (String) v.elementAt(i);
 				System.out.println("codingSchemeName: " + codingSchemeName);
 
 				String cs_uri = DataUtils.getCodingSchemeURI(codingSchemeName, null);
 
 System.out.println("cs_uri: " + cs_uri);
 
 				//Mappings mappings = DataUtils.getCodingSchemeMappings(codingSchemeName, null);
 				Mappings mappings = DataUtils.getCodingSchemeMappings(cs_uri, null);
 				if (mappings != null) {
 					int knt1 = mappings.getSupportedCodingSchemeCount();
 					System.out.println("getSupportedCodingSchemeCount: " + knt1); //1
 
 					SupportedCodingScheme[] cs_array = mappings.getSupportedCodingScheme();
 					for (int j=0; j<knt1; j++) {
 						SupportedCodingScheme cs = cs_array[j];
 						if (cs != null) {
 							String key = cs.getLocalId() + "|" + cs.getUri() + "|" + cs.getIsImported();
 							System.out.println(key);
 							if (!hset.contains(key)) {
 
 								System.out.println("adding cs to vector ..." + cs.getUri());
 								w.add(cs);
 							}
 					    }
 					}
 				}
 
 			}
 
 			System.out.println("w size: " + w.size());
 			return w;
 		} catch (Exception ex) {
 			//ex.printStackTrace();
 			System.out.println("ValueSetBean findSupportedCodingSchemes throws exception???");
 		}
 		return null;
 
 	}
 
 
     public ValueSetDefinition convertToValueSetDefinition(ValueSetObject vs_obj, String expression) {
         ValueSetDefinition vsd = new ValueSetDefinition();
 
         vsd.setOwner(vs_obj.getOwner());
         String isActive = vs_obj.getIsActive();
         vsd.setIsActive(Boolean.FALSE);
         if (isActive.compareTo("true") == 0) {
 			vsd.setIsActive(Boolean.TRUE);
 		}
         vsd.setStatus(vs_obj.getStatus());
 
 String cs_name = DataUtils.getCodingSchemeName(vs_obj.getCodingScheme(), null);
 
 		vsd.setDefaultCodingScheme(cs_name);
 		vsd.setValueSetDefinitionName(vs_obj.getName());
 
 		String description = vs_obj.getDescription();
 		EntityDescription ed = new EntityDescription();
 
 		ed.setContent(description);
 		vsd.setEntityDescription(ed);
 
 		vsd.setValueSetDefinitionName(vs_obj.getName());
 		vsd.setValueSetDefinitionURI(vs_obj.getUri());
         vsd.setConceptDomain(vs_obj.getConceptDomain());
 
         Mappings mappings = constructVSDMappings(vs_obj);
         vsd.setMappings(mappings);
 
         org.LexGrid.commonTypes.Source source = new org.LexGrid.commonTypes.Source();
         source.setContent(vs_obj.getSources());
         vsd.addSource(source);
 
         org.LexGrid.commonTypes.Properties properties = new org.LexGrid.commonTypes.Properties();
         vsd.setProperties(properties);
 
         int count = vs_obj.getCompListSize();
 
         Long ruleOrderCount = new Long(0);
 
 		System.out.println("Component count: " + count);
 		System.out.println("expression: " + expression);
 		try {
 
 			List list = translateValueSetExpression(vs_obj, expression);
 			vsd = generateValueSetDefinition(list);
 			if (vsd == null) {
 
 				System.out.println("generateValueSetDefinition returns null???");
 				return null;
 			}
 
 			vsd.setOwner(vs_obj.getOwner());
 			isActive = vs_obj.getIsActive();
 			vsd.setIsActive(Boolean.FALSE);
 			if (isActive.compareTo("true") == 0) {
 				vsd.setIsActive(Boolean.TRUE);
 			}
 			vsd.setStatus(vs_obj.getStatus());
 
 cs_name = DataUtils.getCodingSchemeName(vs_obj.getCodingScheme(), null);
 
 			vsd.setDefaultCodingScheme(cs_name);
 			vsd.setValueSetDefinitionName(vs_obj.getName());
 
 			description = vs_obj.getDescription();
 			ed = new EntityDescription();
 
 			ed.setContent(description);
 			vsd.setEntityDescription(ed);
 
 			vsd.setValueSetDefinitionName(vs_obj.getName());
 			vsd.setValueSetDefinitionURI(vs_obj.getUri());
 			vsd.setConceptDomain(vs_obj.getConceptDomain());
 
 			mappings = constructVSDMappings(vs_obj);
 			vsd.setMappings(mappings);
 
 			source = new org.LexGrid.commonTypes.Source();
 			source.setContent(vs_obj.getSources());
 			vsd.addSource(source);
 
 			properties = new org.LexGrid.commonTypes.Properties();
 			vsd.setProperties(properties);
 
 
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 
 		return vsd;
 
 	}
 
 	public List translateValueSetExpression(ValueSetObject vs_obj, String expression) {
 		System.out.println("\t\ttranslateValueSetExpression expression: " + expression);
 
 		List list = new ArrayList();
 		if (expression == null) {
 			Map<String,ComponentObject> map = vs_obj.getCompList();
 			Iterator it = map.keySet().iterator();
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				ComponentObject ob = (ComponentObject) map.get(key);
 				list.add(ob);
 			}
 			return list;
 		}
 
 		//HashMap hmap = new HashMap();
 		if (vs_obj == null) return null;
 
 		HashMap hmap = (HashMap) vs_obj.getCompList();
 		int count = vs_obj.getCompListSize();
 		list = new ArrayList();
 
 		if (count == 0) {
 			System.out.println("WARNING: ValueSetObject " + vs_obj.getUri() + " " + vs_obj.getName() + " has no component.");
 			System.out.println("\t\ttranslateValueSetExpression returns null.");
 			return null;
 		}
 
 		try {
 			Vector postfix_vec = new RPN().convertToPostfixExpression(expression);
 			for (int i=0; i<postfix_vec.size(); i++) {
 				String t = (String) postfix_vec.elementAt(i);
 				System.out.println("postfix_vec: " + t);
 				t = t.trim();
                 if (t.length() > 0) {
 					if (t.compareTo("UNION") == 0) {
 						list.add(t);
 					} else if (t.compareTo("INTERSECTION") == 0) {
 						list.add(t);
 					} else if (t.compareTo("DIFFERENCE") == 0) {
 						list.add(t);
 					} else {
 						ComponentObject obj = (ComponentObject) hmap.get(t);
 						if (obj != null) {
 							list.add(obj);
 						} else {
 							System.out.println("WARNING: ComponentObject " + t + " not found.");
 						}
 					}
 			    }
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return list;
 	}
 
 
 
 
       public static DefinitionOperator string2DefinitionOperator(String operator) {
 		  if (operator == null) return null;
 		  operator = operator.trim();
 
 		  if (operator.compareTo("UNION") == 0) {
 			  return DefinitionOperator.OR;
 		  } else if (operator.compareTo("INTERSECTION") == 0) {
 			  return DefinitionOperator.AND;
 		  } else if (operator.compareTo("DIFFERENCE") == 0 || operator.compareTo("SUBTRACT") == 0) {
 			  return DefinitionOperator.SUBTRACT;
 		  }
 
 		  return null;//DefinitionOperator.OR;
 	  }
 
 
 
 
 	  public ValueSetDefinition generateValueSetDefinition(List list) {
 		  Stack stack = new Stack();
 		  System.out.println("\ngenerateValueSetDefinition ...");
 		  DefinitionEntry vDefinitionEntry = null;
 
 		  // Stoppping rule -- list is empty and stack is empty
 		  for (int i=0; i<list.size(); i++) {
 			  Object obj = list.get(i);
 
 			  if (obj instanceof ComponentObject) {
 				  // Operand
 				  ComponentObject co = (ComponentObject) obj;
                   stack.push(obj);
 
 			  } else if (obj instanceof ValueSetDefinition) {
 				  // Operand
 				  ValueSetDefinition def = (ValueSetDefinition) obj;
                   stack.push(def);
 
 
 			  }	else if (obj instanceof String) {
 				  // Operator
 			      System.out.println("Set operator: " + obj.toString());
 			      String operator = obj.toString();
 
 			      if (stack.size() < 2) {
 					  System.out.println("Invalid postfix expression -- operation aborts. stack.size() = " + stack.size());
 					  return null;
 				  } else {
 					  Object operand_2_obj = stack.pop();
 					  Object operand_1_obj = stack.pop();
 					  // four different cases:
 					  // case 1: operand_1_obj is ValueSetOperand && operand_2_obj is ValueSetOperand
 					  // create a VSD with two DefinitionEntry to hold both ValueSetOperand
 					  if (operand_1_obj instanceof ComponentObject && operand_2_obj instanceof ComponentObject) {
 						  ValueSetDefinition new_vsd = new ValueSetDefinition();
 						  ComponentObject operand_1 = (ComponentObject) operand_1_obj;
 						  ComponentObject operand_2 = (ComponentObject) operand_2_obj;
 
 System.out.println("Calculating  " + operand_1.getLabel() + " " + operand_1.getType() + " " + obj.toString()
                              + " " + operand_2.getLabel() + " " + operand_2.getType());
 
 
 System.out.println("processing #1 " + operand_1.getLabel());
 
 						  if(operand_1.getType().compareTo("EnumerationOfCodes") == 0) {
 							    Long ruleOrderCount = new Long(new_vsd.getDefinitionEntryCount());
 								String codes = operand_1.getCodes();
 								String lines[] = codes.split("\\n");
 								for(int j = 0; j < lines.length; j++) {
 									String code = lines[j];
 									code = code.trim();
 									DefinitionEntry entry = new DefinitionEntry();
 									EntityReference entity_ref = new EntityReference();
 									entity_ref.setEntityCode(code);
 
 									String cs_name = DataUtils.getCodingSchemeName(operand_1.getVocabulary(), null);
 									//entity_ref.setEntityCodeNamespace(operand_1.getVocabulary());
 									entity_ref.setEntityCodeNamespace(cs_name);
 									entry.setEntityReference(entity_ref);
 									entry.setRuleOrder(ruleOrderCount);
 									entry.setOperator(DefinitionOperator.OR);
 									new_vsd.addDefinitionEntry(entry);
 									ruleOrderCount++;
 								}
 
 						  } else if(operand_1.getType().compareTo("Code") == 0) {
 							    Long ruleOrderCount = new Long(new_vsd.getDefinitionEntryCount());
 								String code = operand_1.getMatchText();
 								code = code.trim();
 								DefinitionEntry entry = new DefinitionEntry();
 								EntityReference entity_ref = new EntityReference();
 								entity_ref.setEntityCode(code);
 									String cs_name = DataUtils.getCodingSchemeName(operand_1.getVocabulary(), null);
 								entity_ref.setEntityCodeNamespace(cs_name);
 								entry.setEntityReference(entity_ref);
 								entry.setRuleOrder(ruleOrderCount);
 								entry.setOperator(DefinitionOperator.OR);
 								new_vsd.addDefinitionEntry(entry);
 								ruleOrderCount++;
 
 
 						  } else {
 							  vDefinitionEntry = componentObject2DefinitionEntry(operand_1);
 							  vDefinitionEntry.setOperator(DefinitionOperator.OR);
 							  vDefinitionEntry.setRuleOrder(new Long(new_vsd.getDefinitionEntryCount()));
 							  new_vsd.addDefinitionEntry(vDefinitionEntry);
 						  }
 
 System.out.println("processing #2 " + operand_2.getLabel());
 
 						  if(operand_2.getType().compareTo("EnumerationOfCodes") == 0) {
 							    Long ruleOrderCount = new Long(new_vsd.getDefinitionEntryCount());
 								String codes = operand_2.getCodes();
 								String lines[] = codes.split("\\n");
 								for(int j = 0; j < lines.length; j++) {
 									String code = lines[j];
 									code = code.trim();
 									DefinitionEntry entry = new DefinitionEntry();
 									EntityReference entity_ref = new EntityReference();
 									entity_ref.setEntityCode(code);
 									String cs_name = DataUtils.getCodingSchemeName(operand_2.getVocabulary(), null);
 									//entity_ref.setEntityCodeNamespace(operand_1.getVocabulary());
 
 									entity_ref.setEntityCodeNamespace(cs_name);
 									entry.setEntityReference(entity_ref);
 									entry.setRuleOrder(ruleOrderCount);
 									entry.setOperator(string2DefinitionOperator(operator));
 									new_vsd.addDefinitionEntry(entry);
 									ruleOrderCount++;
 								}
 
 						  } else if(operand_2.getType().compareTo("Code") == 0) {
 							    Long ruleOrderCount = new Long(new_vsd.getDefinitionEntryCount());
 								String code = operand_2.getMatchText();
 								code = code.trim();
 								DefinitionEntry entry = new DefinitionEntry();
 								EntityReference entity_ref = new EntityReference();
 								entity_ref.setEntityCode(code);
 								String cs_name = DataUtils.getCodingSchemeName(operand_2.getVocabulary(), null);
 								entity_ref.setEntityCodeNamespace(cs_name);
 								entry.setEntityReference(entity_ref);
 								entry.setRuleOrder(ruleOrderCount);
 								entry.setOperator(string2DefinitionOperator(operator));
 								new_vsd.addDefinitionEntry(entry);
 								ruleOrderCount++;
 
 
 						  } else {
 							  vDefinitionEntry = componentObject2DefinitionEntry(operand_2);
 
 System.out.println("string2DefinitionOperator(operator) " + operator + " " + string2DefinitionOperator(operator).toString());
 
 							  vDefinitionEntry.setOperator(string2DefinitionOperator(operator));
 							  vDefinitionEntry.setRuleOrder(new Long (new_vsd.getDefinitionEntryCount()));
 							  new_vsd.addDefinitionEntry(vDefinitionEntry);
 						  }
 
 						  String new_label = "(" + operand_1.getLabel() + " " + operator + " " + operand_2.getLabel() + ")";
 						  EntityDescription entityDescription = new EntityDescription();
 						  entityDescription.setContent(new_label);
 						  new_vsd.setEntityDescription(entityDescription);
 
                           // push new_vsd to stack
 
  System.out.println("stack.push " + new_label);
 
 
 						  stack.push(new_vsd);
 					  } else if (operand_1_obj instanceof ValueSetDefinition && operand_2_obj instanceof ComponentObject) {
 						  //ValueSetDefinition new_vsd = new ValueSetDefinition();
 
 						  ValueSetDefinition new_vsd = (ValueSetDefinition) operand_1_obj;
 						  ComponentObject operand_2 = (ComponentObject) operand_2_obj;
 
  System.out.println("operand_1_obj instanceof ValueSetDefinition && operand_2_obj instanceof ComponentObject " );
  System.out.println("\t" + new_vsd.getValueSetDefinitionURI() + " " + string2DefinitionOperator(operator) + " " +
                          operand_2.getLabel() );
 
 
                           if(operand_2.getType().compareTo("EnumerationOfCodes") == 0) {
 							    Long ruleOrderCount = new Long(new_vsd.getDefinitionEntryCount());
 								String codes = operand_2.getCodes();
 								String lines[] = codes.split("\\n");
 								for(int j = 0; j < lines.length; j++) {
 									String code = lines[j];
 									DefinitionEntry entry = new DefinitionEntry();
 									EntityReference entity_ref = new EntityReference();
 									entity_ref.setEntityCode(code);
 									entity_ref.setEntityCodeNamespace(operand_2.getVocabulary());
 									entry.setEntityReference(entity_ref);
 									entry.setRuleOrder(ruleOrderCount);
 									entry.setOperator(string2DefinitionOperator(operator));
 									new_vsd.addDefinitionEntry(entry);
 									ruleOrderCount++;
 
 								}
 
 						  } else if(operand_2.getType().compareTo("Code") == 0) {
 							    Long ruleOrderCount = new Long(new_vsd.getDefinitionEntryCount());
 								String code = operand_2.getMatchText();
 								code = code.trim();
 								DefinitionEntry entry = new DefinitionEntry();
 								EntityReference entity_ref = new EntityReference();
 								entity_ref.setEntityCode(code);
 								String cs_name = DataUtils.getCodingSchemeName(operand_2.getVocabulary(), null);
 								entity_ref.setEntityCodeNamespace(cs_name);
 								entry.setEntityReference(entity_ref);
 								entry.setRuleOrder(ruleOrderCount);
 								entry.setOperator(string2DefinitionOperator(operator));
 								new_vsd.addDefinitionEntry(entry);
 								ruleOrderCount++;
 
 
 						  } else {
 
 System.out.println("Add DefinitionEntry operand_2 to operand_1 value set definition");
 
 							  vDefinitionEntry = componentObject2DefinitionEntry(operand_2);
 							  vDefinitionEntry.setOperator(string2DefinitionOperator(operator));
 							  vDefinitionEntry.setRuleOrder(new Long(new_vsd.getDefinitionEntryCount()));
 							  new_vsd.addDefinitionEntry(vDefinitionEntry);
 						  }
 
 						  String new_label = "(" + new_vsd.getValueSetDefinitionURI() + " " + operator + " " + operand_2.getLabel() + ")";
 						  EntityDescription entityDescription = new EntityDescription();
 						  entityDescription.setContent(new_label);
 						  new_vsd.setEntityDescription(entityDescription);
 
                           // push new_vsd to stack
 						  stack.push(new_vsd);
 
 					  } else if (operand_2_obj instanceof ValueSetDefinition && operand_1_obj instanceof ComponentObject) {
 						 System.out.println("WARNING: Not supported.");
 
 					  } else {
 
 						  System.out.println("Not implemented.");
 					  }
 
 				  }
 			  }
 		  }
 
 		  if (stack.size() == 0) {
 			  return null;
 		  }
 
 		  Object final_ob = stack.pop();
 
 		  if (final_ob instanceof ValueSetDefinition) {
 
 
 			  ValueSetDefinition vsd = (ValueSetDefinition) final_ob;
 			  System.out.println("returning final: " + vsd.getValueSetDefinitionURI());
 
 			  return vsd;
 		  } else if (final_ob instanceof ComponentObject) {
 
 
 System.out.println("(*) final_ob instanceof ComponentObject): " );
 
 
 			ComponentObject ob = (ComponentObject) final_ob;
 			ValueSetDefinition vsd = new ValueSetDefinition();
 			Long ruleOrderCount = new Long(0);
 			if(ob.getType().compareTo("EnumerationOfCodes") == 0) {
 
 				String codes = ob.getCodes();
 				String lines[] = codes.split("\\n");
 				for(int i = 0; i < lines.length; i++) {
 					String code = lines[i];
 					code = code.trim();
 					DefinitionEntry entry = new DefinitionEntry();
 					EntityReference entity_ref = new EntityReference();
 					entity_ref.setEntityCode(code);
 
 String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 					entity_ref.setEntityCodeNamespace(cs_name);
 
 					entry.setEntityReference(entity_ref);
 					entry.setRuleOrder(ruleOrderCount);
 					entry.setOperator(DefinitionOperator.OR);
 					vsd.addDefinitionEntry(entry);
 					ruleOrderCount++;
 				}
 
 			} else if(ob.getType().compareTo("Code") == 0) {
 				String code = ob.getMatchText();
 				code = code.trim();
 
 
 				DefinitionEntry entry = new DefinitionEntry();
 				EntityReference entity_ref = new EntityReference();
 				entity_ref.setEntityCode(code);
 				String cs_name = DataUtils.getCodingSchemeName(ob.getVocabulary(), null);
 				entity_ref.setEntityCodeNamespace(cs_name);
 				entry.setEntityReference(entity_ref);
 				entry.setRuleOrder(ruleOrderCount);
 				entry.setOperator(DefinitionOperator.OR);
 				vsd.addDefinitionEntry(entry);
 				ruleOrderCount++;
 
 
 			} else {
 
 				DefinitionEntry entry = componentObject2DefinitionEntry(ob);
 				entry.setRuleOrder(ruleOrderCount);
 				entry.setOperator(DefinitionOperator.OR);
 				vsd.addDefinitionEntry(entry);
 			}
 
 			return vsd;
 
 		  }
 
           return null;
 
 	  }
 
 	public AbsoluteCodingSchemeVersionReferenceList constructAbsoluteCodingSchemeVersionReferenceList(String cs_ref_key) {
 		AbsoluteCodingSchemeVersionReferenceList csvList = new AbsoluteCodingSchemeVersionReferenceList();
 		Vector v = DataUtils.parseData(cs_ref_key);
 		for (int i=0; i<v.size(); i++) {
 			String nm_version = (String) v.elementAt(i);
 			Vector u = DataUtils.parseData(nm_version, "$");
 			String cs_uri = (String) u.elementAt(0);
 			String cs_version = (String) u.elementAt(1);
 			csvList.addAbsoluteCodingSchemeVersionReference(Constructors.createAbsoluteCodingSchemeVersionReference(cs_uri, cs_version));
 		}
 		return csvList;
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
 
 
 
     public String exportResolvedVSDToCSVAction() {
 		HttpServletRequest request =
 			(HttpServletRequest) FacesContext.getCurrentInstance()
 				.getExternalContext().getRequest();
         request.getSession().removeAttribute("message");
 
 
         String cs_ref_key = FacesUtil.getRequestParameter("cs_ref_key");
         AbsoluteCodingSchemeVersionReferenceList csvList = constructAbsoluteCodingSchemeVersionReferenceList(cs_ref_key);
 
     	_message = null;
     	_logger.debug("Exporting value.");
 
     	String curr_uri = FacesUtil.getRequestParameter("uri");
     	_logger.debug("Exporting value set: " + curr_uri);
 
         ValueSetObject item = null;
         if (_cart.containsKey(curr_uri)) {
         	item = _cart.get(curr_uri);
 		}
 
 		if (item == null) {
 			String msg = "ERROR: Unable to identify the value set definition with URI " + curr_uri;
 			request.setAttribute("message", msg);
 			return "error";
 		}
 
 
         String infixExpression = item.getExpression();
 		ValueSetDefinition vsd = convertToValueSetDefinition(item, infixExpression);
 		if (vsd == null) {
 			String msg = "ERROR: Unable to construct ValueSetDefinition based on the given expression.";
 			request.setAttribute("message", msg);
 			return "error";
 		}
 
         HashMap<String, ValueSetDefinition> referencedVSDs = null;
         StringBuffer sb = new StringBuffer();
         try {
         	LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
             ResolvedConceptReferencesIterator itr = ValueSetUtils.resolveValueSetDefinition(vsd, csvList, referencedVSDs);
 
 			sb.append("Code,");
 			sb.append("Name,");
 			sb.append("Terminology,");
 			sb.append("Version,");
 			sb.append("Namespace,");
 			//sb.append("Definition");
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
 /*
 					String definition = getNCIDefinition(ref);
 					if (definition == null) definition = "";
 					sb.append("\"" + definition + "\"");
 */
 					sb.append("\r\n");
 				}
 			}
 		} catch (Exception ex)	{
 			sb.append("WARNING: Export to CVS action failed.");
 		}
 
 
 		String vsd_uri = vsd.getValueSetDefinitionName();//.valueSetDefiniionURI2Name(vsd_uri);
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
 		return null;
 	}
 
 
     public String exportResolvedVSDToXMLAction() {
 		HttpServletRequest request =
 			(HttpServletRequest) FacesContext.getCurrentInstance()
 				.getExternalContext().getRequest();
         request.getSession().removeAttribute("message");
 
 
         String cs_ref_key = FacesUtil.getRequestParameter("cs_ref_key");
         AbsoluteCodingSchemeVersionReferenceList csvList = constructAbsoluteCodingSchemeVersionReferenceList(cs_ref_key);
 
 		//request.getSession().setAttribute("expression", expression);
 
     	_message = null;
     	_logger.debug("Exporting value.");
     	//<input type="hidden" id="uri" name="uri" value="<%=curr_uri%>" />
 
     	String curr_uri = FacesUtil.getRequestParameter("uri");
     	_logger.debug("Exporting value set: " + curr_uri);
 
         ValueSetObject item = null;
         if (_cart.containsKey(curr_uri)) {
         	item = _cart.get(curr_uri);
 		}
 
 		if (item == null) {
 			String msg = "ERROR: Unable to identify the value set definition with URI " + curr_uri;
 			request.setAttribute("message", msg);
 			return "error";
 		}
 
 
         String infixExpression = item.getExpression();
 		ValueSetDefinition vsd = convertToValueSetDefinition(item, infixExpression);
 		if (vsd == null) {
 			String msg = "ERROR: Unable to construct ValueSetDefinition based on the given expression.";
 			request.setAttribute("message", msg);
 			return "error";
 		}
 
 
         try {
         	LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 
             StringBuffer buf = null;
             InputStream reader = null;
 
     	String codingSchemeNames = FacesUtil.getRequestParameter("codingSchemeNames");
     	_logger.debug("codingSchemeNames: " + codingSchemeNames);
 
             try {
 	    		reader = vsd_service.exportValueSetResolution(vsd, null, csvList, null, false);
 
 				if (reader != null) {
 					buf = new StringBuffer();
 					for (int c = reader.read(); c != -1; c = reader.read()) {
 						buf.append((char) c);
 					}
 				} else {
 					buf = new StringBuffer("<error>exportValueSetResolution returned null.</error>");
 				}
 
             } catch (Exception e) {
 				buf = new StringBuffer("<error>The VSD export service is not supported by your current LexEVS setup.</error>");
 				buf.append("<!-- " + e.getMessage() + " -->");
 			} finally {
 				try {
 					reader.close();
 				} catch (Exception e) {
 					new StringBuffer("<error>" + e.getMessage() + "</error>");
 				}
 			}
 
             // Send export XML string to browser
 
             HttpServletResponse response = (HttpServletResponse) FacesContext
                     .getCurrentInstance().getExternalContext().getResponse();
 
 			response.setContentType("text/xml");
 
 			String vsd_name = item.getName();
 			vsd_name = vsd_name.replaceAll(" ", "_");
 			vsd_name = vsd_name + ".xml";
 
 			response.setHeader("Content-Disposition", "attachment; filename="
 					+ vsd_name);
 
             response.setContentLength(buf.length());
             ServletOutputStream ouputStream = response.getOutputStream();
             ouputStream.write(buf.toString().getBytes(), 0, buf.length());
             ouputStream.flush();
             ouputStream.close();
 
             // Don't allow JSF to forward to cart.jsf
             FacesContext.getCurrentInstance().responseComplete();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 
 } // End of ValueSetBean
