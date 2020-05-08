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
 
     // Application version
     public static final int MAJOR_VER = 1;
     public static final int MINOR_VER = 0;
     public static final String CONFIG_FILE = "NCItBrowserProperties.xml";
     public static final String CODING_SCHEME_NAME = "NCI Thesaurus";
     public static final String NCI_THESAURUS = "NCI Thesaurus";
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
 
     public static final String ERROR_ENCOUNTERED_TRY_NARROW_QUERY =
         "Unable to perform search successfully. Please narrow your query.";
     public static final String ERROR_REQUIRE_MORE_SPECIFIC_QUERY_STRING =
         "Please provide a more specific search criteria.";
 
     public static final String EXACT_SEARCH_ALGORITHM = "exactMatch";// "literalSubString";//"subString";
     public static final String STARTWITH_SEARCH_ALGORITHM = "startsWith";// "literalSubString";//"subString";
     public static final String CONTAIN_SEARCH_ALGORITHM =
         "nonLeadingWildcardLiteralSubString";// "literalSubString";//"subString";
     public static final String LICENSE_STATEMENT = "license_statement";// "literalSubString";//"subString";
 
     public static final int SEARCH_BOTH_DIRECTION = 0;
     public static final int SEARCH_SOURCE = 1;
     public static final int SEARCH_TARGET = 2;
 
     public static final String TREE_ACCESS_ALLOWED = "tree_access_allowed";
 
     public static String TYPE_ROLE = "type_role";
     public static String TYPE_ASSOCIATION = "type_association";
     public static String TYPE_SUPERCONCEPT = "type_superconcept";
     public static String TYPE_SUBCONCEPT = "type_subconcept";
     public static String TYPE_INVERSE_ROLE = "type_inverse_role";
     public static String TYPE_INVERSE_ASSOCIATION = "type_inverse_association";
 
 
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
