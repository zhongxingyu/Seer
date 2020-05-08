 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.common;
 
 /**
  * Holds the list of application roles, used to define access permissions on various methods.
  */
 public class RolesConstants {
 
     // DASHBOARD
     public static final String DASHBOARD_VIEW_ASSIGNED_APPS = "DashbrdViewAssign";
     public static final String DASHBOARD_VIEW_UNASSIGNED_APPS = "DashbrdViewUnassign";
     public static final String DASHBOARD_VIEW_OWN_APPS = "DashbrdViewOwn";
 
     // APPLICATION
     public static final String APPLICATION_VIEW_APPS = "ApplnView";
     public static final String APPLICATION_CREATE_APPS = "ApplnCreate";
     public static final String APPLICATION_EDIT_APPS = "ApplnEdit";
     public static final String APPLICATION_PRINT_STATUS_REPORT = "ApplnStatus";
     public static final String APPLICATION_ASSIGN_TO_YOURSELF = "ApplnAssignSelf";
     public static final String APPLICATION_ASSIGN_TO_OTHERS = "ApplnAssignOthers";
     public static final String APPLICATION_UNASSIGN_FROM_YOURSELF = "ApplnUnassignSelf";
     public static final String APPLICATION_UNASSIGN_FROM_OTHERS = "ApplnUnassignOthers";
     public static final String APPLICATION_SERVICE_START = "StartService";
     public static final String APPLICATION_SERVICE_COMPLETE = "CompleteService";
     public static final String APPLICATION_SERVICE_CANCEL = "CancelService";
     public static final String APPLICATION_SERVICE_REVERT = "RevertService";
     public static final String APPLICATION_REQUISITE = "ApplnRequistion";
     public static final String APPLICATION_RESUBMIT = "ApplnResubmit";
     public static final String APPLICATION_LAPSE = "ApplnLapse";
     public static final String APPLICATION_APPROVE = "ApplnApprove";
     public static final String APPLICATION_WITHDRAW = "ApplnWithdraw";
     public static final String APPLICATION_REJECT = "ApplnReject";
     public static final String APPLICATION_VALIDATE = "ApplnValidate";
    public static final String APPLICATION_DISPATCH = "ApplnDispatch";
     public static final String APPLICATION_ARCHIVE = "ApplnArchive";
 
     // ADMINISTRATIVE
     public static final String ADMINISTRATIVE_BA_UNIT_SAVE = "BaunitSave";
     public static final String ADMINISTRATIVE_RRR_SAVE = "BaunitrrrSave";
     public static final String ADMINISTRATIVE_PARCEL_SAVE = "BaunitParcelSave";
     public static final String ADMINISTRATIVE_NOTATION_SAVE = "BaunitNotnSave";
     public static final String ADMINISTRATIVE_BA_UNIT_PRINT_CERT = "BaunitCertificate";
     public static final String ADMINISTRATIVE_BA_UNIT_SEARCH = "BaunitSearch";
 
     // TRANSACTIONAL DOCUMENTS
     public static final String SOURCE_TRANSACTIONAL = "TransactionCommit";
 
     // DOCUMENTS
     public static final String SOURCE_SAVE = "SourceSave";
     public static final String SOURCE_SEARCH = "SourceSearch";
     public static final String SOURCE_PRINT = "SourcePrint";
 
     // GIS
     public static final String GIS_VIEW_MAP = "ViewMap";
     public static final String GIS_PRINT = "GISprint";
 
     // CADASTRE
     public static final String CADASTRE_PARCEL_SAVE = "ParcelSave";
 
     // PARTY
     public static final String PARTY_SAVE = "PartySave";
     public static final String PARTY_RIGHTHOLDERS_SAVE = "RHSave";
 
     // REPORTS
     public static final String REPORTS_VIEW = "ReportGen";
     
     // ARCHIVE
     public static final String ARCHIVE_ARCHIVE_APPS = "ArchiveApps";
     
     // ADMIN
     public static final String ADMIN_MANAGE_SECURITY = "ManageSecurity";
     public static final String ADMIN_MANAGE_REFDATA = "ManageRefdata";
     public static final String ADMIN_MANAGE_SETTINGS = "ManageSettings";
     public static final String ADMIN_MANAGE_BR = "ManageBR";
 }
