 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.business.indexing;
 
 import java.util.regex.Pattern;
 
 /**
  * Constants for Search and Browse.
  *
  * @author Michael Hoppe
  */
 public final class Constants {
 
     /**
      * Private constructor to avoid instantiation.
      */
     private Constants() {
     }
 
     /**
      * Http-Request-Timeout.
      */
     //3 hrs, because of optimize
     public static final int REQUEST_TIMEOUT = 3 * 60 * 60 * 1000;
 
     public static final String INDEXING_ERROR_LOGFILE = "indexer-errors";
 
     /**
      * common gsearch Constants.
      */
     public static final String GSEARCH_INDEX_NAMES_PROPERTY = "fedoragsearch.indexNames";
 
     private static final String ESCIDOC_FEDORA_REPOSITORY = "escidocrepository";
 
     public static final String GSEARCH_CREATE_EMPTY_INDEX_PARAMS =
         "?operation=updateIndex&action=createEmpty&repositoryName=" + ESCIDOC_FEDORA_REPOSITORY
             + "&indexName=${INDEX_NAME}";
 
     public static final String GSEARCH_UPDATE_INDEX_PARAMS =
         "?operation=updateIndex&action=fromPid&repositoryName=" + ESCIDOC_FEDORA_REPOSITORY
             + "&indexName=${INDEX_NAME}&value=${VALUE}&commitWrite=${COMMIT_WRITE}";
 
     public static final String GSEARCH_DELETE_INDEX_PARAMS =
         "?operation=updateIndex&action=deletePid&repositoryName=" + ESCIDOC_FEDORA_REPOSITORY
             + "&indexName=${INDEX_NAME}&value=${VALUE}&commitWrite=${COMMIT_WRITE}";
 
     public static final String GSEARCH_STYLESHEET_PARAMS =
         "&indexDocXslt=(SUPPORTED_MIMETYPES=${SUPPORTED_MIMETYPES}"
             + ",PID_VERSION_IDENTIFIER=${PID_VERSION_IDENTIFIER}"
            + ",INDEX_FULLTEXT_VISIBILITIES=${INDEX_FULLTEXT_VISIBILITIES}"
            + ",ESCIDOC_SERVLET_CONTEXT_NAME=${ESCIDOC_SERVLET_CONTEXT_NAME})";
 
     public static final String GSEARCH_GET_INDEX_CONFIGURATION_PARAMS = "?operation=getIndexConfigInfo";
 
     public static final String GSEARCH_GET_REPOSITORY_INFO_PARAMS = "?operation=getRepositoryInfo";
 
     public static final String GSEARCH_OPTIMIZE_INDEX_PARAMS =
         "?operation=updateIndex&action=optimize&repositoryName=" + ESCIDOC_FEDORA_REPOSITORY
             + "&indexName=${INDEX_NAME}";
 
     public static final String GSEARCH_COMMIT_WRITES_PARAMS =
         "?operation=updateIndex&action=commitIndexWrites&repositoryName=" + ESCIDOC_FEDORA_REPOSITORY
             + "&indexName=${INDEX_NAME}";
 
     /**
      * common srw Constants.
      */
     public static final String SRW_SCAN_PARAMS =
         "?operation=scan&scanClause=PID%3D%22${TERM}%22" + "&maximumTerms=${MAXIMUM_TERMS}&responsePosition=0";
 
     public static final int SRW_MAXIMUM_SCAN_TERMS = 10000;
 
     public static final Pattern INDEX_NAME_PATTERN = Pattern.compile("\\$\\{INDEX_NAME\\}");
 
     public static final Pattern VALUE_PATTERN = Pattern.compile("\\$\\{VALUE\\}");
 
     public static final Pattern COMMIT_WRITE_PATTERN = Pattern.compile("\\$\\{COMMIT_WRITE\\}");
 
     public static final Pattern SUPPORTED_MIMETYPES_PATTERN = Pattern.compile("\\$\\{SUPPORTED_MIMETYPES\\}");
 
     public static final Pattern PID_VERSION_IDENTIFIER_PATTERN = Pattern.compile("\\$\\{PID_VERSION_IDENTIFIER\\}");
 
     public static final Pattern PID_VERSION_IDENTIFIER_TOTAL_PATTERN =
         Pattern.compile(",PID_VERSION_IDENTIFIER=\\$\\{PID_VERSION_IDENTIFIER\\}");
 
     public static final Pattern INDEX_FULLTEXT_VISIBILITIES_PATTERN =
         Pattern.compile("\\$\\{INDEX_FULLTEXT_VISIBILITIES\\}");
 
     public static final Pattern INDEX_FULLTEXT_VISIBILITIES_TOTAL_PATTERN =
         Pattern.compile(",INDEX_FULLTEXT_VISIBILITIES=\\$\\{INDEX_FULLTEXT_VISIBILITIES\\}");
 
    public static final Pattern ESCIDOC_SERVLET_CONTEXT_NAME_PATTERN =
        Pattern.compile("\\$\\{ESCIDOC_SERVLET_CONTEXT_NAME\\}");

    public static final Pattern ESCIDOC_SERVLET_CONTEXT_NAME_TOTAL_PATTERN =
        Pattern.compile(",ESCIDOC_SERVLET_CONTEXT_NAME=\\$\\{ESCIDOC_SERVLET_CONTEXT_NAME\\}");

     public static final Pattern DOC_COUNT_PATTERN =
         Pattern.compile(".*?docCount=\"(.*?)\".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 
     public static final Pattern SRW_TERM_PATTERN = Pattern.compile("\\$\\{TERM\\}");
 
     public static final Pattern SRW_MAXIMUM_TERMS_PATTERN = Pattern.compile("\\$\\{MAXIMUM_TERMS\\}");
 
     public static final Pattern EXCEPTION_PATTERN =
         Pattern.compile(".*Exception.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 
     public static final Pattern NO_INDEX_DIR_PATTERN =
         Pattern.compile(".*?(no segments|not a directory|NoSuchDirectoryException).*", Pattern.DOTALL
             | Pattern.CASE_INSENSITIVE);
 
     public static final Pattern NO_INDEX_DIR_INDEX_NAME_PATTERN =
         Pattern.compile(".*?indexName=([^\\s]*).*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 
     public static final Pattern LOCK_OBTAIN_TIMEOUT_PATTERN =
         Pattern.compile(".*Lock obtain timed out.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 
     /**
      * Constants.
      */
     public static final int DO_DELETE = -1;
 
     public static final int DO_UPDATE = 1;
 
     public static final int DO_NOTHING = 0;
 
     public static final String LATEST_VERSION_PID_SUFFIX = "LV";
 
     public static final String LATEST_RELEASE_PID_SUFFIX = "LR";
 
 }
