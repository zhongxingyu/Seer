 /*==============================================================================
  * File $Id$
  * Project: ProWim
  *
  * $LastChangedDate: 2010-07-01 09:16:47 +0200 (Do, 01 Jul 2010) $
  * $LastChangedBy: wardi $
  * $HeadURL: https://repository.ebcot.info/wivu/trunk/prowim-server/src/de/ebcot/prowim/utils/ServerProperties.java $
  * $LastChangedRevision: 4188 $
  *------------------------------------------------------------------------------
  * (c) 03.06.2009 Ebcot Business Solutions GmbH. More info: http://www.ebcot.de
  * All rights reserved. Use is subject to license terms.
  *==============================================================================
  *
  *This file is part of ProWim.
 
 ProWim is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 ProWim is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with ProWim.  If not, see <http://www.gnu.org/licenses/>.
 
 Diese Datei ist Teil von ProWim.
 
 ProWim ist Freie Software: Sie können es unter den Bedingungen
 der GNU General Public License, wie von der Free Software Foundation,
 Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
 veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
 ProWim wird in der Hoffnung, dass es nützlich sein wird, aber
 OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
 Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 Siehe die GNU General Public License für weitere Details.
 
 Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
  */
 package org.prowim.services.ejb;
 
 import org.prowim.datamodel.dms.DMSException;
 import org.prowim.jca.connector.algernon.OntologyErrorException;
 
 
 /**
  * Properties and constants for the proxy.
  * 
  * @author Saad Wardi
  * 
  * @version $Revision: 4188 $
  */
 public final class ServiceConstants
 {
 
     /**
      * The Context root.
      */
     public static final String CONTEXT_ROOT                     = "ProWimServices";
 
     /* ****************** ProwimActivityService ********************************************* */
 
     /**
      * The package name.
      */
     public static final String PROWIM_ACTIVITY_PACKAGE          = "org.prowim.services.ejb.activity";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_ACTIVITY_REMOTE           = "ActivityRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_ACTIVITY_SEI              = PROWIM_ACTIVITY_PACKAGE + "." + PROWIM_ACTIVITY_REMOTE;
     /**
      * The Activity bean class name.
      */
     public static final String ACTIVITY_BEAN                    = "ActivityBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_ACTIVITY_NAMESPACE        = "http://activity.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_ACTIVITY_SERVICE_NAME     = "ActivityService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_ACTIVITY_SERVICE_PORT     = "ActivityServicePort";
 
     /* ********************* ProwimKnowledgeService************************************************** */
 
     /**
      * The package name.
      */
     public static final String PROWIM_KNOWLEDGE_PACKAGE         = "org.prowim.services.ejb.knowledge";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_KNOWLEDGE_REMOTE          = "KnowledgeRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_KNOWLEDGE_SEI             = PROWIM_KNOWLEDGE_PACKAGE + "." + PROWIM_KNOWLEDGE_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String KNOWLEDGE_BEAN                   = "KnowledgeBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_KNOWLEDGE_NAMESPACE       = "http://knowledge.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_KNOWLEDGE_SERVICE_NAME    = "KnowledgeService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_KNOWLEDGE_SERVICE_PORT    = "KnowledgeServicePort";
 
     /* ********************* DocumentsService************************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_DOCUMENTS_PACKAGE         = "org.prowim.services.ejb.documents";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_DOCUMENTS_REMOTE          = "DocumentRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_DOCUMENTS_SEI             = PROWIM_DOCUMENTS_PACKAGE + "." + PROWIM_DOCUMENTS_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String DOCUMENTS_BEAN                   = "DocumentBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_DOCUMENTS_NAMESPACE       = "http://documents.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_DOCUMENTS_SERVICE_NAME    = "DocumentService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_DOCUMENTS_SERVICE_PORT    = "DocumentServicePort";
 
     /* *************************************** OntologyWSAccessService********************** */
 
     /**
      * The package name.
      */
     public static final String ONTOLOGYWSACCESS_PACKAGE         = "org.prowim.services.ejb.ontology";
     /**
      * The remote interface name.
      */
     public static final String ONTOLOGYWSACCESS_REMOTE          = "OntologyWSAccessRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String ONTOLOGYWSACCESS_SEI             = ONTOLOGYWSACCESS_PACKAGE + "." + ONTOLOGYWSACCESS_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String ONTOLOGYWSACCESS_BEAN            = "ProWimOntologyBean";
 
     /**
      * The name space.
      */
     public static final String ONTOLOGYWSACCESS_NAMESPACE       = "http://ontology.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String ONTOLOGYWSACCESS_SERVICE_NAME    = "OntologyWSAccessService";
 
     /**
      * The Service Port.
      */
     public static final String ONTOLOGYWSACCESS_SERVICE_PORT    = "OntologyWSAccessServicePort";
 
     /* *********************** ProwimSystemInfoService **************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_SYSTEMINFO_PACKAGE        = "org.prowim.services.ejb.systeminfo";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_SYSTEMINFO_REMOTE         = "SystemInfoRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_SYSTEMINFO_SEI            = PROWIM_SYSTEMINFO_PACKAGE + "." + PROWIM_SYSTEMINFO_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String PROWIM_SYSTEMINFO_BEAN           = "SystemInfoBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_SYSTEMINFO_NAMESPACE      = "http://systeminfo.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_SYSTEMINFO_SERVICE_NAME   = "SystemInfoService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_SYSTEMINFO_SERVICE_PORT   = "SystemInfoServicePort";
 
     /* ********************************ProcessService**************************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_PROCESS_PACKAGE           = "org.prowim.services.ejb.process";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_PROCESS_REMOTE            = "ProcessRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_PROCESS_SEI               = PROWIM_PROCESS_PACKAGE + "." + PROWIM_PROCESS_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String PROWIM_PROCESS_BEAN              = "ProcessBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_PROCESS_NAMESPACE         = "http://process.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_PROCESS_SERVICE_NAME      = "ProcessService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_PROCESS_SERVICE_PORT      = "ProcessServicePort";
 
     /* *********************************************************************************************** */
 
     /* ********************************WorkflowService**************************************************** */
 
     /**
      * The package name.
      */
     public static final String PROWIM_WORKFLOW_PACKAGE          = "org.prowim.services.ejb.workflow";
 
     /**
      * The remote interface name.
      */
     public static final String PROWIM_WORKFLOW_REMOTE           = "WorkflowRemote";
 
     /**
      * The workflow bean name.
      */
     public static final String PROWIM_WORKFLOW_BEAN             = "WorkflowBean";
 
     /**
      * The name space for workflow.
      */
     public static final String PROWIM_WORKFLOW_NAMESPACE        = "http://workflow.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_WORKFLOW_SERVICE_NAME     = "WorkflowService";
 
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_WORKFLOW_SEI              = PROWIM_WORKFLOW_PACKAGE + "." + PROWIM_WORKFLOW_REMOTE;
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_WORKFLOW_SERVICE_PORT     = "WorkflowServicePort";
 
     /* *********************************************************************************************** */
 
     /* ********************************EditorService**************************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_EDITOR_PACKAGE            = "org.prowim.services.ejb.editor";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_EDITOR_REMOTE             = "EditorRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_EDITOR_SEI                = PROWIM_EDITOR_PACKAGE + "." + PROWIM_EDITOR_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String PROWIM_EDITOR_BEAN               = "EditorBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_EDITOR_NAMESPACE          = "http://editor.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_EDITOR_SERVICE_NAME       = "EditorService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_EDITOR_SERVICE_PORT       = "EditorServicePort";
 
     /* *********************************************************************************************** */
     /* ********************************EditorService**************************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_COMMON_PACKAGE            = "org.prowim.services.ejb.commons";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_COMMON_REMOTE             = "CommonRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_COMMON_SEI                = PROWIM_COMMON_PACKAGE + "." + PROWIM_COMMON_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String PROWIM_COMMON_BEAN               = "CommonBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_COMMON_NAMESPACE          = "http://commons.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_COMMON_SERVICE_NAME       = "CommonService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_COMMON_SERVICE_PORT       = "CommonServicePort";
 
     /* *********************************************************************************************** */
     /* ********************************EditorService**************************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_ORGANIZATION_PACKAGE      = "org.prowim.services.ejb.organization";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_ORGANIZATION_REMOTE       = "OrganizationRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_ORGANIZATION_SEI          = PROWIM_ORGANIZATION_PACKAGE + "." + PROWIM_ORGANIZATION_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String PROWIM_ORGANIZATION_BEAN         = "OrganizationBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_ORGANIZATION_NAMESPACE    = "http://organization.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_ORGANIZATION_SERVICE_NAME = "OrganizationService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_ORGANIZATION_SERVICE_PORT = "OrganizationServicePort";
 
     /* *********************************************************************************************** */
     /* ********************************AdminService**************************************************** */
     /**
      * The package name.
      */
     public static final String PROWIM_ADMIN_PACKAGE             = "org.prowim.services.ejb.admin";
     /**
      * The remote interface name.
      */
     public static final String PROWIM_ADMIN_REMOTE              = "AdminRemote";
     /**
      * The remote service endpoint interface.
      */
     public static final String PROWIM_ADMIN_SEI                 = PROWIM_ADMIN_PACKAGE + "." + PROWIM_ADMIN_REMOTE;
     /**
      * The KnowledgeBean name.
      */
     public static final String PROWIM_ADMIN_BEAN                = "AdminBean";
 
     /**
      * The name space.
      */
     public static final String PROWIM_ADMIN_NAMESPACE           = "http://admin.ejb.services.prowim.org/";
 
     /**
      * The Service name.
      */
     public static final String PROWIM_ADMIN_SERVICE_NAME        = "AdminService";
 
     /**
      * The Service Port.
      */
     public static final String PROWIM_ADMIN_SERVICE_PORT        = "AdminServicePort";
 
     /**
      * The Namespace for the {@link OntologyErrorException ontology error}
      */
    public static final String ONTOLOGY_ERROR_NAMESPACE         = "http://algernon.connector.jca.ebcot.de/";
 
     /**
      * The {@link OntologyErrorException ontology error}
      */
     public static final String ONTOLOGY_ERROR                   = "OntologyErrorException";
 
     /**
      * The name of {@link DMSException DMS exception}
      */
     public static final String DMS_EXCEPTION_NAME               = "DMSException";
 
     /**
      * The namespace for the {@link DMSException DMS Exception}
      */
     public static final String DMS_EXCEPTION_NAMESPACE          = "http://dms.datamodel.prowim.org";
 
     /* *********************************************************************************************** */
 
     private ServiceConstants()
     {
 
     }
 
 }
