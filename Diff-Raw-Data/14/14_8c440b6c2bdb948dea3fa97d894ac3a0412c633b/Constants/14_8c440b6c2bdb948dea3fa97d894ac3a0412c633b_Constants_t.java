 package gov.nih.nci.evs.browser.common;
 
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
  * Application constants class
  *
  * @author garciawa2
  */
 public class Constants {
 
     public static final String TELEPHONE = "240-276-5541 or Toll-Free: 1-888-478-4423";
     public static final String MAIL_TO = "ncicbiit@mail.nih.gov";
     public static final String NCICB_URL = "http://ncicb.nci.nih.gov/support";
 
     // Application version
     public static final int MAJOR_VER = 1;
     public static final int MINOR_VER = 0;
     public static final String CONFIG_FILE = "NCItBrowserProperties.xml";
     public static final String CODING_SCHEME_NAME = "NCI Thesaurus";
     public static final String NCI_THESAURUS = "NCI Thesaurus";
     public static final String NCIT_CS_NAME = "NCI_Thesaurus";
     public static final String NCI_METATHESAURUS = "NCI Metathesaurus";
 
     // Application constants
     public static final String NA = "N/A";
     public static final String TRUE = "true";
     public static final String FALSE = "false";
     public static final String EMPTY = "";
 
     public static final String ALL = "ALL";
 
     // Application error constants
     public static final String INIT_PARAM_ERROR_PAGE = "errorPage";
     public static final String ERROR_MESSAGE = "systemMessage";
     public static final String ERROR_UNEXPECTED =
         "Warning: An unexpected processing error has occurred.";
 
     public static final int DEFAULT_PAGE_SIZE = 50;
 
     public static final String ERROR_NO_VOCABULARY_SELECTED =
         "Please select at least one terminology.";
     public static final String ERROR_NO_SEARCH_STRING_ENTERED =
         "Please enter a search string.";
     public static final String ERROR_NO_MATCH_FOUND = "No match found.";
     public static final String ERROR_NO_MATCH_FOUND_TRY_OTHER_ALGORITHMS =
         "No match found. Please try 'Begins With' or 'Contains' search instead.";
 
     public static final String ERROR_NO_MATCH_FOUND_CODE_IS_CASESENSITIVE =
         "No match found. Please note that code search is case sensitive.";
 
     public static final String ERROR_ENCOUNTERED_TRY_NARROW_QUERY =
         "Unable to perform search successfully. Please narrow your query.";
     public static final String ERROR_REQUIRE_MORE_SPECIFIC_QUERY_STRING =
         "Please provide a more specific search criteria.";
 
     public static final String EXACT_SEARCH_ALGORITHM = "exactMatch";// "literalSubString";//"subString";
     public static final String STARTWITH_SEARCH_ALGORITHM = "startsWith";// "literalSubString";//"subString";
     public static final String CONTAIN_SEARCH_ALGORITHM =
         "nonLeadingWildcardLiteralSubString";// "literalSubString";//"subString";
     public static final String LICENSE_STATEMENT = "license_statement";// "literalSubString";//"subString";
 
     public static final String DEFAULT_SEARCH_ALGORITHM = "contains";
 
 
     public static final int SEARCH_BOTH_DIRECTION = 0;
     public static final int SEARCH_SOURCE = 1;
     public static final int SEARCH_TARGET = 2;
 
     public static final String TREE_ACCESS_ALLOWED = "tree_access_allowed";
 
     public static final String TYPE_ROLE = "type_role";
     public static final String TYPE_ASSOCIATION = "type_association";
     public static final String TYPE_SUPERCONCEPT = "type_superconcept";
     public static final String TYPE_SUBCONCEPT = "type_subconcept";
     public static final String TYPE_INVERSE_ROLE = "type_inverse_role";
     public static final String TYPE_INVERSE_ASSOCIATION = "type_inverse_association";
 
     public static final int  STANDARD_VIEW = 1;
     public static final int  TERMINOLOGY_VIEW = 2;
 
     public static final String  TERMINOLOGY_VALUE_SET = "Terminology Value Set";
     public static final String  TERMINOLOGY_VALUE_SET_NAME = "Terminology_Value_Set.owl";
 
     public static final long MILLISECONDS_PER_MINUTE = 60L * 1000;
 
     public static final String PLEASE_COMPLETE_DATA_ENTRIES = "Please complete data entries.";
     public static final String INVALID_EMAIL_ADDRESS = "Invalid email address.";
 
 
     public static final String CODE = "codes";
     public static final String NAME = "names";
     public static final String PROPERTY = "properties";
     public static final String RELATIONSHIP = "relationahips";
 
     //public static final String[] VALID_CAPTCHA_OTPIONS = new String[] {"default","audio"};
     public static final String DEFAULT_CAPTCHA_OTPION = "default";
     public static final String AUDIO_CAPTCHA_OTPION = "audio";
 
     public static final String INVALID_CAPTCHA_OTPION = "WARNING: Invalid CAPTCHA option.";
 
     public static final String UNIDENTIFIABLE_VOCABULARY = "WARNING: Unidentifiable vocabulary name.";
 
 
     public static final String ROLE_LABEL = "Role Relationships";
     public static final String ROLE_LABEL_2 = "pointing from the current concept to other concepts";
     public static final String ROLE_DESCRIPTION_LABEL = "(True for the current concept and its descendants, may be inherited from parent(s).)";
 
     public static final String ASSOCIATION_LABEL = "Associations";
     public static final String ASSOCIATION_LABEL_2 = "pointing from the current concept to other concepts";
     public static final String ASSOCIATION_DESCRIPTION_LABEL = "(True for the current concept.)";
 
     public static final String INVERSE_ROLE_LABEL = "Incoming Role Relationships";
     public static final String INVERSE_ROLE_LABEL_2 = "pointing from other concepts to the current concept";
     public static final String INVERSE_ROLE_DESCRIPTION_LABEL = "(True for the other concept and its descendants, may be inherited from its parent(s).)";
 
     public static final String INVERSE_ASSOCIATION_LABEL = "Incoming Associations";
     public static final String INVERSE_ASSOCIATION_LABEL_2 = "pointing from other concepts to the current concept";
     //public static final String INVERSE_ASSOCIATION_DESCRIPTION_LABEL = "(True for the other concepts relationship to the current concept.)";
 
     public static final String INVERSE_ASSOCIATION_DESCRIPTION_LABEL = "(True for the other concept's relationship to the current concept.)";
 
     public static final String NCBO_PURL = "http://purl.bioontology.org/ontology/";
     public static final String NCIT_NCBO_ID = "1032";
     public static final String NCIT = "NCIT";
     public static final String NCIT_NAMESPACE = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
     public static final String NCBO_WIDGET_QUERY_STRING = "http://bioportal.bioontology.org/widgets/visualization?ontology=";
 
     public static final String DEFAULT_NCBO_WIDGET_INFO = "NCI_Thesaurus|1032|NCIT;";
 
    public static final String[] NAV_TYPE_VALUES = {"terminologies", "mappings", "valuesets"};

 /*
 Role Relationships pointing from the current concept to other concepts:
 (True for the current concept and its descendants, may be inherited from parent(s).)
 
 Associations pointing from the current concept to other concepts:
 (True for the current concept.)
 
 Incoming Role Relationships pointing from other concepts to the current concept:
 (True for the other concept and its descendants, may be inherited from its parent(s).)
 
 Incoming Associations pointing from other concepts to the current concept:
 (True for the other concepts relationship to the current concept.)
 
 */
 
 
     /**
      * Constructor
      */
     private Constants() {
         // Prevent class from being explicitly instantiated
     }
 
     public static String getCodingSchemeName() {
         return CODING_SCHEME_NAME.replaceAll(" ", "%20");
     }
 
 } // Class Constants
