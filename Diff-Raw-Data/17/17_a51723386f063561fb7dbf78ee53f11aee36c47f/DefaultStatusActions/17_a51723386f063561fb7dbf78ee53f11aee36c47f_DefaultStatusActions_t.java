 //=============================================================================
 //===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
 //===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 //===	and United Nations Environment Programme (UNEP)
 //===
 //===	This program is free software; you can redistribute it and/or modify
 //===	it under the terms of the GNU General Public License as published by
 //===	the Free Software Foundation; either version 2 of the License, or (at
 //===	your option) any later version.
 //===
 //===	This program is distributed in the hope that it will be useful, but
 //===	WITHOUT ANY WARRANTY; without even the implied warranty of
 //===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 //===	General Public License for more details.
 //===
 //===	You should have received a copy of the GNU General Public License
 //===	along with this program; if not, write to the Free Software
 //===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 //===
 //===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 //===	Rome - Italy. email: geonetwork@osgeo.org
 //==============================================================================
 
 package org.fao.geonet.services.metadata;
 
 import jeeves.resources.dbms.Dbms;
 import jeeves.server.UserSession;
 import jeeves.server.context.ServiceContext;
 import org.apache.commons.lang.StringUtils;
 import org.fao.geonet.GeonetContext;
 import org.fao.geonet.constants.Geonet;
 import org.fao.geonet.constants.Params;
 import org.fao.geonet.kernel.AccessManager;
 import org.fao.geonet.kernel.DataManager;
 import org.fao.geonet.kernel.MdInfo;
 import org.fao.geonet.kernel.setting.SettingManager;
 import org.fao.geonet.util.ISODate;
 import org.fao.geonet.util.MailSender;
 import org.jdom.Document;
 import org.jdom.Element;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class DefaultStatusActions implements StatusActions {
 
 	private String host, port, from, fromDescr, replyTo, replyToDescr;
 	private ServiceContext context;
 	private AccessManager am;
 	private DataManager dm;
 	private Dbms dbms;
 	private String siteUrl;
 	private UserSession session;
 	private boolean emailNotes = true;
 
 	private String allGroup = "1";
 
 	/**
 		* Constructor.
 		*/
 	public DefaultStatusActions() {}
 
 	/** 
 	  * Initializes the StatusActions class with external info from GeoNetwork.
 	  *
 		* @param context
 		* @param dbms
 		*/
 	public void init(ServiceContext context, Dbms dbms) {
 	
 		this.context = context;
 		this.dbms = dbms;
 
 		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
 		SettingManager sm = gc.getSettingManager();
 		am = gc.getAccessManager();
 
 		host = sm.getValue("system/feedback/mailServer/host");
 		port = sm.getValue("system/feedback/mailServer/port");
 		from = sm.getValue("system/feedback/email");
 
 		if (host.length() == 0) {
 			context.error("Mail server host not configured, email notifications won't be sent.");
 			emailNotes = false;
 		}
 		
 		if (port.length() == 0) {
 			context.error("Mail server port not configured, email notifications won't be sent.");
 			emailNotes = false;
 		}
 		
 		if (from.length() == 0) {
 			context.error("Mail feedback address not configured, email notifications won't be sent.");
 			emailNotes = false;
 		}
 
 		fromDescr = "Metadata Workflow";
 
 		session = context.getUserSession();
 		replyTo = session.getEmailAddr();
 		if (replyTo != null) {
 			replyToDescr = session.getName() + " " + session.getSurname();
 		} else {
 			replyTo = from; replyToDescr = fromDescr;
 		}
 
 
 		dm = gc.getDataManager();
 		siteUrl = dm.getSiteURL();
 	}
 
     /**
      * Called when a metadata is created.
      *
      * @param id the id of the metadata
      */
     public void onCreate(String id) throws Exception {
         String changeMessage = "GeoNetwork gebruiker "+session.getUserId()+" ("+session.getUsername()+")heeft metadata record met id "+id + " gecrëeerd.";
         dm.setStatus(context, dbms, id, new Integer(Params.Status.JUSTCREATED), new ISODate().toString(), changeMessage);
     }
 
 	/** 
 	  * Called when a record is edited to set/reset status.
 	  *
 		* @param id The metadata id that has been edited.
 		* @param minorEdit If true then the edit was a minor edit.
 		*/
 	public void onEdit(String id, boolean minorEdit) throws Exception {
 
         // AGIV change
         // if (!minorEdit && dm.getCurrentStatus(dbms, id).equals(Params.Status.APPROVED)) {
         if (!minorEdit && !dm.getCurrentStatus(dbms, id).equals(Params.Status.DRAFT)) {
 
             //String changeMessage = "GeoNetwork user "+session.getUserId()+" ("+session.getUsername()+") edited metadata record "+id;
             String changeMessage = "GeoNetwork gebruiker "+session.getUserId()+" ("+session.getUsername()+")heeft metadata record met id "+id + " bewerkt.";
             //unsetAllOperations(id);
 			dm.setStatus(context, dbms, id, new Integer(Params.Status.DRAFT), new ISODate().toString(), changeMessage);
 		}
         else if(minorEdit) {
             //System.out.println("*** minorEdit, not setting status to DRAFT");
         }
         else {
             //System.out.println("*** current status is not APPROVED: "+ dm.getCurrentStatus(dbms, id));
         }
 	}
 
     public void onCancelEdit(String id) throws Exception {
 
         if (dm.getCurrentStatus(dbms, id).equals(Params.Status.DRAFT)) {
 
             //String changeMessage = "GeoNetwork user "+session.getUserId()+" ("+session.getUsername()+") canceled edit session for metadata record "+id;
             String changeMessage = "GeoNetwork gebruiker "+session.getUserId()+" ("+session.getUsername()+") heeft de editeer sessie van metadata record met id "+id + " geannuleerd.";
             //unsetAllOperations(id);
             String revertToThisStatus = dm.getLastBeforeCurrentStatus(dbms, id);
             if(StringUtils.isEmpty(revertToThisStatus)) {
                 revertToThisStatus = Params.Status.UNKNOWN;
             }
             dm.setStatus(context, dbms, id, new Integer(revertToThisStatus), new ISODate().toString(), changeMessage);
         }
 
     }
 
 	/** 
 	  * Called when need to set status on a set of metadata records.
 	  *
 		* @param status The status to set.
 		* @param metadataIds The set of metadata ids to set status on.
 		* @param changeDate The date the status was changed.
 		* @param changeMessage The message explaining why the status has changed.
 		*/
 	public Set<String> statusChange(String status, Set<String> metadataIds, String changeDate, String changeMessage) throws Exception {
         System.out.println("Starting statusChange to " + status + " for #" + metadataIds.size() + " metadata");
         try {
 
             Set<String> unchanged = new HashSet<String>();
 
             //-- process the metadata records to set status
             for (String mid : metadataIds) {
                 String currentStatus = dm.getCurrentStatus(dbms, mid);
 
                 //--- if the status is already set to value of status then do nothing
                 if (status.equals(currentStatus)) {
                     if(context.isDebug())
                         context.debug("Metadata "+mid+" already has status "+mid);
                         unchanged.add(mid);
                 }
 
                 // check if this is a template
                 MdInfo mdInfo = dm.getMetadataInfo(dbms, mid);
                 boolean isTemplate = mdInfo.template.equals(MdInfo.Template.TEMPLATE) || mdInfo.template.equals(MdInfo.Template.SUBTEMPLATE);
 
                 // templates need not be valid, other md must be valid to change status to approved
                 if(isTemplate || statusChangeAllowed(currentStatus, status, mid)) {
                     System.out.println("Change status of metadata " + mid + " from " + currentStatus + " to " + status);
                     if (status.equals(Params.Status.APPROVED)) {
                         setAllOperations(mid); //- this is a short cut that could be enabled
                         dm.moveFromWorkspaceToMetadata(context, dbms, mid);
                     }
                     else if (status.equals(Params.Status.DRAFT) || status.equals(Params.Status.REJECTED)) {
                         //unsetAllOperations(mid);
                     }
 
                     //--- set status, indexing is assumed to take place later
                     // heikki: why later ?!
                     //dm.setStatusExt(context, dbms, mid, new Integer(status), changeDate, changeMessage);
                     dm.setStatus(context, dbms, mid, new Integer(status), new ISODate().toString(), changeMessage);
 
                     //--- inform content reviewers if the status is submitted
                     if (status.equals(Params.Status.SUBMITTED)) {
                         informContentReviewers(metadataIds, changeDate, changeMessage);
                     }
                     //--- inform owners if status is approved
                     else if (status.equals(Params.Status.APPROVED)) {
                         informOwnersApprovedOrRejected(metadataIds, changeDate, changeMessage, true);
                     }
                     //--- inform owners if status is rejected
                     else if (status.equals(Params.Status.REJECTED)) {
                         informOwnersApprovedOrRejected(metadataIds, changeDate, changeMessage, false);
                     }
                 }
                 else {
                     unchanged.add(mid);
                     System.out.println("Status change not allowed for metadata " + mid + " from " + currentStatus + " to " + status);
                 }
             }
             return unchanged;
         }
         catch(Throwable x) {
             System.out.println("ERROR in statusChange " + x.getMessage());
             x.printStackTrace();
             throw new Exception(x);
         }
 	}
 
     /**
      *
      * @param currentStatus
      * @param status
      * @param mid
      * @return
      * @throws Exception
      */
     private boolean statusChangeAllowed(String currentStatus, String status, String mid) throws Exception {
         //
         // AGIV: trigger validation when status is changed. If one of the validations fails, the change is not allowed (depending on which logical node this is).
         //
         // heikki: use non-boolean doValidate, to get errorreport in session (we need to know which validation failed exactly)
         // OR use the other doValidate and then here retrieve validationresult from db <-- better because errorreport is too detailed, we want the summary here
 
         // the workspace md should be validated if the current status is draft or rejected and the status to be set is submitted; OR
         // if the current status is submitted and the status to be set is approved
         boolean workspace;
         if ((currentStatus.equals(Params.Status.DRAFT) || currentStatus.equals(Params.Status.REJECTED) && (status.equals(Params.Status.SUBMITTED))) ||
                 (currentStatus.equals(Params.Status.SUBMITTED)) && (status.equals(Params.Status.APPROVED)) ){
             workspace = true;
         }
         else {
             workspace =false;
         }
         // retrieve md as Document
         Document doc ;
         Element mdE;
         if(workspace) {
             mdE = dm.getMetadataFromWorkspaceNoInfo(context, mid);
         }
         else {
             mdE = dm.getMetadataNoInfo(context, mid);
         }
         doc = new Document(mdE);
 
         // the md schema
         String schema = dm.getMetadataSchema(dbms, mid);
         // validate
         boolean valid = dm.doValidate(dbms, schema, mid, doc, context.getLanguage(), workspace);
 
         boolean statusChangeAllowed = true;
 
         // if the md is not valid, analyze why not and allow or disallow status change depending on what exactly was not valid and which logical node this is
         if(!valid) {
             // retrieve validationreport from db
             // this is a list of <record> Elements with <valtype>, <status>, <tested>, and <failed> child elements
             List<Element> validationStatus = validationStatus = dm.getValidationStatus(dbms, mid, workspace);
             // if this is AGIV Edit or the AGIV View Node:
             // Only when the metadata validates against the ISO schemas and schematrons, it can be set to
             // "Submitted to AGIV",
             // "Submitted to the Principal Editor", and
             // "Approved by AGIV" states.
             // In case these validations do not succeed, the metadata is returned to the "Draft" state and the
             // user informed.
             // Even if a metadata record does not validate against the INSPIRE and GDI-Vlaanderen Best Practices
             // schematrons, it can be set to the above mentioned states (these both checks are not blocking).
 
             // GDI Vlaanderen Node:
             // Only be set to "Submitted" or "Approved" when the metadata validates against the ISO schemas and
             // schematrons.  If a metadata record's status is changed, the metadata validation (all measures)
             // will be triggered. Metadata records only get the "Approved" or "Submitted" status if the ISO XSD
             // and schematron validation was performed OK.
             // INSPIRE and GDI-Vlaanderen Best Practices validity are given for information only, metadata can
             // be saved and set to the above mentioned states even in case that the validation for these 2
             // measures was not successful.
 
             // INSPIRE Node:
             // only gets its metadata through Harvesting. INSPIRE compliance is enforced by the validation in
             // the CSW INSPIRE harvester.
 
             // heikki: so in short, the status change is only allowed if ISO XSD and schematron validation has
             // passed; the same for all logical nodes !
 
             for(Element record : validationStatus) {
                 // check that xsd validation succeeded
                 if(record.getChildText("valtype").equals("xsd")) {
                     if(!record.getChildText("status").equals("1") &&
                             (status.equals(Params.Status.SUBMITTED) || status.equals(Params.Status.APPROVED)) ) {
                         System.out.println("Metadata with id " + mid + " failed XSD validation: status change not " +
                                 "allowed");
                         return false;
                     }
                 }
                 // check that iso schematron validation succeeded
                 if(record.getChildText("valtype").equals("schematron-rules-iso")) {
                     if(!record.getChildText("status").equals("1")) {
                         System.out.println("Metadata with id " + mid + " failed ISO Schematron validation: status change not allowed");
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
 	//-------------------------------------------------------------------------
 	// Private methods
 	//-------------------------------------------------------------------------
 
 	/**
     * Set all operations on 'All' Group. Used when status changes from submitted to approved.
     *
     * @param mdId The metadata id to set privileges on
     */
   private void setAllOperations(String mdId) throws Exception {
     String allGroup = "1";
     dm.setOperation(context, dbms, mdId, allGroup, AccessManager.OPER_VIEW);
     dm.setOperation(context, dbms, mdId, allGroup, AccessManager.OPER_DOWNLOAD);
     dm.setOperation(context, dbms, mdId, allGroup, AccessManager.OPER_NOTIFY);
     dm.setOperation(context, dbms, mdId, allGroup, AccessManager.OPER_DYNAMIC);
     dm.setOperation(context, dbms, mdId, allGroup, AccessManager.OPER_FEATURED);
   }
 
   /**
     * Unset all operations on 'All' Group. Used when status changes from approved to something else. 
     *
     * @param mdId The metadata id to unset privileges on
     */
   private void unsetAllOperations(String mdId) throws Exception {
     // for AGIV / new workflow: md status does not impact visibility
     /*
     String allGroup = "1";
     dm.unsetOperation(context, dbms, mdId+"", allGroup, AccessManager.OPER_VIEW);
     dm.unsetOperation(context, dbms, mdId+"", allGroup, AccessManager.OPER_DOWNLOAD);
     dm.unsetOperation(context, dbms, mdId+"", allGroup, AccessManager.OPER_NOTIFY);
     dm.unsetOperation(context, dbms, mdId+"", allGroup, AccessManager.OPER_DYNAMIC);
     dm.unsetOperation(context, dbms, mdId+"", allGroup, AccessManager.OPER_FEATURED);
     */
   }
 		
 	/**
 		* Inform content reviewers of metadata records in list that they need
 		* to review the record.
 		*
 		* @param metadata The selected set of metadata records
 		* @param changeDate The date that of the change in status
 		* @param changeMessage Message supplied by the user that set the status
 		*/
 	private void informContentReviewers(Set<String> metadata, String changeDate, String changeMessage) throws Exception {
 
 		//--- get content reviewers (sorted on content reviewer userid)
 		Element contentRevs = am.getContentReviewers(dbms, metadata);
 
         String subject;
         if(metadata.size() == 1) {
             subject = "Metadata record INGEDIEND door "+replyTo+" ("+replyToDescr+") op "+changeDate;
 
         }
         else {
             //String subject = "Metadata records SUBMITTED by "+replyTo+" ("+replyToDescr+") on "+changeDate;
             subject = "Metadata records INGEDIEND door "+replyTo+" ("+replyToDescr+") op "+changeDate;
         }
         boolean multiple = metadata.size() > 1;
 		processList(contentRevs, subject, Params.Status.SUBMITTED, changeDate, changeMessage, metadata);
 	}
 
 	/**
 		* Inform owners of metadata records that the records have approved
 		* or rejected.
 		*
 		* @param metadata The selected set of metadata records
 		* @param changeDate The date that of the change in status
 		* @param changeMessage Message supplied by the user that set the status
 		*/
 	private void informOwnersApprovedOrRejected(Set<String> metadata, String changeDate, String changeMessage, boolean approved) throws Exception {
 
 		//--- get metadata owners (sorted on owner userid)
 		Element owners = am.getOwners(dbms, metadata);
 
         String subject;
         if(metadata.size() == 1) {
             //String subject = "Metadata records APPROVED";
             subject = "Metadata record GOEDGEKEURD";
         }
         else {
             subject = "Metadata records GOEDGEKEURD";
         }
 
 
 		String status = Params.Status.APPROVED;
 		if (!approved) {
             if(metadata.size() == 1) {
                 subject = "Metadata record AFGEWEZEN";
             }
             else {
                 //subject = "Metadata records REJECTED";
                 subject = "Metadata records AFGEWEZEN";
             }
             status = Params.Status.REJECTED;
 		}
         //subject += " by "+replyTo+" ("+replyToDescr+") on "+changeDate;
         subject += " door "+replyTo+" ("+replyToDescr+") op "+changeDate;
 
         boolean multiple = metadata.size() > 1;
         processList(owners, subject, status, changeDate, changeMessage, metadata);
 
 	}
 
 	/**
 		* Process the users and metadata records for emailing notices.
 		*
 		* @param users The selected set of users
 		* @param subject Subject to be used for email notices
 		* @param status The status being set
 		* @param changeDate Datestamp of status change
 		* @param changeMessage The message indicating why the status has changed
 		*/
 	private void processList(Element users, String subject, String status, String changeDate, String changeMessage, Set<String> metadataIds) throws Exception {
 
 		List<Element> userList = users.getChildren();
 
 		List<String> mIds = new ArrayList<String>();
 		boolean first = true;
 		String lastUserId = "-100";
 		String email = "";
 		String id = "";
 
 		for (Element user : userList) {
 			String mid = user.getChildText("metadataid");
 			id  = user.getChildText("userid");
 			email = user.getChildText("email");
 			if (!id.equals(lastUserId) && !first) {	// send out list
 				sendEmail(email, subject, status, changeDate, changeMessage, metadataIds);
 				mIds = new ArrayList<String>();
 				lastUserId = id;
 			}
 			mIds.add(mid);
 			first = false;
 		}
 
 		if (mIds.size() > 0) { // send out the last one
 			sendEmail(email, subject, status, changeDate, changeMessage, metadataIds);
 		}
 	}
 
 	/**
 		* Send the email message about change of status on a group of metadata
 		* records.
 		*
 		* @param sendTo The recipient email address
 		* @param subject Subject to be used for email notices
 		* @param status The status being set on the records
 		* @param changeDate Datestamp of status change
 		* @param changeMessage The message indicating why the status has changed
 		*/
 	private void sendEmail(String sendTo, String subject, String status, String changeDate, String changeMessage, Set<String> metadataIds) throws Exception {
 
         String message = "";
 
         for(String metadataId : metadataIds) {
             message = changeMessage+"\n\nHet metadatarecord is beschikbaar via de volgende URL:\n"+buildMetadataLink(metadataId);
         }
 
         if (!emailNotes) {
 			context.info("Would send email with message:\n"+message);
 		} else {
 			MailSender sender = new MailSender(context);
 			sender.sendWithReplyTo(host, Integer.parseInt(port), from, fromDescr, sendTo, null, replyTo, replyToDescr, subject, message);
 		}
 	}
 
 	/**
 		* Build search link to metadata that has had a change of status.
 		*
 		* @param metadataId The id of the metadata
 		* @return string Search link to metadata
 		*/
 	private String buildMetadataLink(String metadataId) {
 		// TODO: hack voor AGIV
		GeonetContext  gc = (GeonetContext) this.context.getHandlerContext(Geonet.CONTEXT_NAME);
        String protocol = gc.getSettingManager().getValue(Geonet.Settings.SERVER_PROTOCOL);
		String host    = gc.getSettingManager().getValue(Geonet.Settings.SERVER_HOST);
		String port    = gc.getSettingManager().getValue(Geonet.Settings.SERVER_PORT);
		return protocol + "://" + host + (port == "80" ? "" : ":" + port) + "/zoekdienst/apps/tabsearch/index_login.html?id="+metadataId;
 	}
 
 
     /**
      * Build search link to metadata that has had a change of status.
      *
      * @param status The status of the metadata
      * @param changeDate The date the status has been set on the metadata
      * @return string Search link to metadata
      */
     private String buildMetadataSearchLink(String status, String metadataId, String changeDate) {
         // FIXME : hard coded link to main.search
         return siteUrl+"/main.search?_status="+status+"&_statusChangeDate="+changeDate;
     }
 
 
 }
