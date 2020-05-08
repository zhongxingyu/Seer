 package org.ibcb.xnat.redaction;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.TimeZone;
 
 import org.dcm4che2.data.DicomObject;
 import org.ibcb.xnat.redaction.config.CheckoutRuleset;
 import org.ibcb.xnat.redaction.config.Configuration;
 import org.ibcb.xnat.redaction.config.DICOMSchema;
 import org.ibcb.xnat.redaction.config.XNATSchema;
 import org.ibcb.xnat.redaction.database.*;
 import org.ibcb.xnat.redaction.exceptions.CompileException;
 import org.ibcb.xnat.redaction.exceptions.PipelineServiceException;
 import org.ibcb.xnat.redaction.interfaces.XNATExperiment;
 import org.ibcb.xnat.redaction.interfaces.XNATProject;
 import org.ibcb.xnat.redaction.interfaces.XNATRestAPI;
 import org.ibcb.xnat.redaction.interfaces.XNATScan;
 import org.ibcb.xnat.redaction.interfaces.XNATSubject;
 import org.ibcb.xnat.redaction.synchronization.Globals;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 public class WebappLoader {
 	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	
 	static int uk_id = 0;
 	
 	// log_flags:  errors = errors to log file or email or both
 	//             redaction = redaction information to log, email or both
 	//             redaction_warnings = redaction warnings to log, email or both
 	//			   
 	
 	public static void Load(HashMap<String,String> loaderSetting){
 		/*String project_id = args[0];
 		String dest_project_id = args[1];
 		
 		String co_user_id = args[2];
 		String co_admin_id = args[3];
 		
 		String request_fields = args[4];
 		*/
 		LinkedList<String> req_field_names = new LinkedList<String>();
 		
 		// initialize logging and email handling
 		
 		long current_time_stamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
 		String human_date = df.format(Calendar.getInstance().getTime());
 		
 		boolean email_errors=Configuration.instance().getProperty("errors").contains("email");
 		boolean log_errors=Configuration.instance().getProperty("errors").contains("log");
 		
 		boolean email_warnings=Configuration.instance().getProperty("redaction_warnings").contains("email");
 		boolean log_warnings=Configuration.instance().getProperty("redaction_warnings").contains("log");
 		
 		boolean email_redaction=Configuration.instance().getProperty("redaction").contains("email");
 		boolean log_redaction=Configuration.instance().getProperty("redaction").contains("log");
 		
 		if(log_errors){
 			Globals.application_log.enableFlags("e");
 		}else
 			Globals.application_log.disableFlags("e");
 		
 		if(log_redaction){
 			Globals.application_log.enableFlags("r");
 		}else
 			Globals.application_log.disableFlags("r");
 		
 		if(log_warnings){
 			Globals.application_log.enableFlags("w");
 		}else
 			Globals.application_log.disableFlags("w");
 		
 		
 		try{
 
 			// load schemas and extractors
 			
 			DICOMExtractor dext = DICOMExtractor.instance();
 			dext.initialize();
 			
 			XNATExtractor xext = XNATExtractor.instance();
 			xext.initialize();
 			
 			LinkedList<String> complete_field_names = new LinkedList<String>();
 			
 			for(String f : DICOMSchema.instance().getMappedFields()){
 				if(!complete_field_names.contains(f))
 					complete_field_names.add(f);
 			}
 			
 			for(String f : XNATSchema.instance().getMappedFields()){
 				if(!complete_field_names.contains(f))
 					complete_field_names.add(f);
 			}
 			
 			for(String item : loaderSetting.get("request_fields").split(",")){
 				if(!item.trim().equals(""))
 				{
 					System.out.println("processing "+item.trim());
 					req_field_names.add(XNATSchema.instance().getXnatFieldName("xnat:"+item.trim()));
 				}
 			}
 			
 			// load redaction rules
 
 			
 			// load checkout ruleset and checkout system
 			CheckoutRuleset cr = new CheckoutRuleset();
 			cr.setFields(Configuration.instance().getProperty("filter_fields").split(","));
 			cr.loadRuleSet(Configuration.instance().getProperty("checkout_rules"));
 			
 			Checkout.instance().initialize();
 			
 			XNATRestAPI api = XNATRestAPI.instance();
 			
 			// download project and subject ids
 			XNATProject project = new XNATProject();
 			project.setID(loaderSetting.get("source_project"));
 
 			Checkout.instance().downloadProjectXML(project);
 			
 			// ***Possibly Canceled Feature*** upload new project -Matt
 			
 			// Get target project
 			
 			XNATProject target = new XNATProject();
 			target.setID(loaderSetting.get("destination_project"));
 			api.retreiveProject(target);
 			
 			//init DB manager
 			DBManager db;
			if (loaderSetting.get("credential").contains("preconig"))
 				db=new DBManager(Configuration.instance().getProperty("database_hostname"),Configuration.instance().getProperty("database_name"),Configuration.instance().getProperty("database_user"),Configuration.instance().getProperty("database_pass"));
 			else
 			{
 				//System.out.println("use credentials from webpage:\n"+loaderSetting.get("db_addr")); 
 				db=new DBManager(loaderSetting.get("db_addr"),loaderSetting.get("db_name"),loaderSetting.get("db_username"),loaderSetting.get("db_password"));
 				XNATRestAPI.instance().url=loaderSetting.get("xnat_addr");
 				XNATRestAPI.instance().user=loaderSetting.get("xnat_username");
 				XNATRestAPI.instance().pass=loaderSetting.get("xnat_password");
 			}
 				//init a new request
 			Date dt=new Date();
 			//leave affected subjectids blank for now
 			System.out.println("check out field "+loaderSetting.get("request_fields"));
 			RequestInfo r_info=new RequestInfo(loaderSetting.get("co_user_id"),dt.toString(),loaderSetting.get("co_admin_id"),"",req_field_names);
 			BigDecimal requestId=db.getNextRequestID();
 			r_info.setRequestid(requestId);
 			HashMap<String,HashMap<String,String>> overallCheckoutInfo=db.getUserCheckOutInfo(loaderSetting.get("co_user_id"));
 			
 			
 			// for each user in the project
 			for(String subject_id : project.subject_ids){
 				
 				// download subject information and redact
 				Checkout.instance().downloadSubjectXML(project, subject_id);
 				Checkout.instance().downloadSubjectFiles(project, subject_id);
 				// redact XNATSubject demographics
 				
 				XNATSubject subject = project.subjects.get(subject_id);
 				HashMap<String, String> xnat_demographics = XNATExtractor.instance().extractNameValuePairs(subject.getXML(), true);
 				
 				HashMap<String, LinkedList<String>> dicom_demographics = new HashMap<String, LinkedList<String>>();
 				
 				for(String experiment_id : subject.experiment_ids){
 					for(String scan_id : subject.scan_ids.get(experiment_id)){
 					// redact DICOMFiles
 						XNATScan scan = subject.scans.get(scan_id);
 						for(String file : scan.localFiles){
 							String input = scan.tmp_folder+"/"+file;
 							File f = new File(input);
 							
 							if(f.isFile() && f.getName().endsWith("dcm")){
 								System.out.println("Processing: " + input);
 								
 								DicomObject obj = dext.loadDicom(input);
 								
 								HashMap<String,String> hs = dext.extractNameValuePairs(obj, req_field_names);
 								
 								// Store hs data in dicom map
 								
 								for(String key : hs.keySet()){
 									String val = hs.get(key);
 									
 									if(!dicom_demographics.containsKey(key))
 										dicom_demographics.put(key, new LinkedList<String>());
 									if(!dicom_demographics.get(key).contains(val))
 										dicom_demographics.get(key).add(val);
 								}
 								
 								File dir = new File(scan.tmp_folder+"/redacted");
 								if(!dir.exists()){
 									dir.mkdirs();
 								}
 								String nfilename = scan.tmp_folder+"/redacted/"+file;
 								dext.writeDicom(nfilename, obj);
 								
 	//							DicomObject obj2_test = dext.loadDicom(nfilename);							
 	//							hs = dext.extractNameValuePairs(obj2_test, ruleset);
 							}
 						}
 					}
 				}
 				
 				HashMap<String, String> combined_demographics = new HashMap<String, String>();
 				
 				for(String key : xnat_demographics.keySet()){
 					combined_demographics.put(key, xnat_demographics.get(key));
 				}
 				
 				for(String key : dicom_demographics.keySet()){
 					if(!combined_demographics.containsKey(key)){
 						combined_demographics.put(key, dicom_demographics.get(key).getFirst());
 					}
 				}
 				
 				// download checkout user information from our database -Liang
 				String uniSubjectid=null;
 				if (db.lookupSubjectid(subject_id)!=null)
 				uniSubjectid=db.lookupSubjectid(subject_id).toString();
 				HashMap<String,String> subjectCheckoutInfo=null;
 				if (uniSubjectid!=null)
 				subjectCheckoutInfo=overallCheckoutInfo.get(uniSubjectid);
 				// populate map of checkout fields -Liang
 				HashMap<String, String> requesting_user_data = subjectCheckoutInfo;
 				HashMap<String, String> filter_data = new HashMap<String,String>();
 				int checkoutCount=0;
 				for (String field : complete_field_names)
 				{
 						String requestName="request_"+field;
 						filter_data.put(requestName, "0");						
 				}
 				
 				if (subjectCheckoutInfo!=null){
 					for (String key:subjectCheckoutInfo.keySet())
 					{
 						if (subjectCheckoutInfo.get(key).equals(new String("1")))
 						{
 							checkoutCount++;
 							String requestName="request_"+key;
 							filter_data.put(requestName, "1");
 						}					
 					}
 					for (String key:requesting_user_data.keySet())
 					{
 						if (requesting_user_data.get(key).equals(new String("1")))
 						{
 							String requestName="request_"+key;
 							filter_data.put(requestName, "1");
 						}
 						
 					}
 				}
 				for (String fieldName : req_field_names)
 				{
 					String key="request_"+fieldName;
 					if (filter_data.containsKey(key) && !filter_data.get(key).equals(new String("1")))
 							{
 								filter_data.remove(key);
 								filter_data.put(key, "1");
 								checkoutCount++;
 							}					
 				}
 				String phi_checked="phi_checked_out";
 				filter_data.put(phi_checked, Integer.toString(checkoutCount));
 				System.out.println("Check out map for subject "+subject_id);
 				for(String key : filter_data.keySet())
 				{
 					System.out.println(key+"  |  "+filter_data.get(key));					
 				}
 				// Using the above data, along with req_field_names and insert resulting data into the filter_data hashmap
 				// example:
 				// user has already checked out Age previously, and is requesting to check out Race now
 				// filter_data looks like this:
 				// phi_checked_out		2
 				// request_PatientAge	1
 				// request_PatientRace	1
 				// request_...			0
 				// ...					0
 				// 
 				// 
 				
 				
 				// run permissions checks against checkout ruleset information
 				
 				subject.passed = cr.filter(filter_data);
 				// upload redacted information to database -Liang			
 				// PatientAge = [31, 32]
 				// subject_id, field, values
 				
 				// update information about checked out PHI to database -Liang
 				
 				// don't forget to store the destination ids for tracking our redacted data: subject.destination_id
 				// xnat_demographics and dicom_demographics applies to the current subject object
 				
 				
 				// upload subject information -Matt
 				if(subject.passed){
 					//Create a subject info for passed subject
 					//System.out.println("try to insert "+subject.getXML());
 					if (combined_demographics.containsKey("PatientBirthdate") && combined_demographics.containsKey("PatientName"))
 					{
 						String req_ID=requestId.toPlainString()+";";
 						SubjectInfo s_info=new SubjectInfo(null,SubjectInfo.transphiData(combined_demographics),loaderSetting.get("source_project"),req_ID,combined_demographics.get("PatientName"),combined_demographics.get("PatientBirthdate"));
 						//System.out.println("phi = "+combined_demographics.toString()+" request id = "+requestId.toPlainString());
 						BigDecimal db_subjectid=db.insertSubjectInfo(s_info);		
 						db.insertSubjectidMap(db_subjectid, subject_id);
 						subject.setNewLabel(db_subjectid.toString());
 						//System.out.println("new id "+db_subjectid);
 						if (db_subjectid!=null)
 						{
 							String newAffectedIDs=r_info.getaffectedsubjectstext()+db_subjectid+";";
 							r_info.setaffectedsubjects(newAffectedIDs);
 						}
 					}
 					else{
 						subject.setNewLabel("unknown_"+(uk_id++));
 					}
 					
 					// reinsert requested, authorized information into XNAT and DICOM -Matt			
 					for(String field : loaderSetting.get("request_fields").split(",")){
 						if(combined_demographics.containsKey(XNATSchema.instance().getXnatFieldName("xnat:"+field))){
 							System.out.println("Reinserting: " + "xnat:"+field+" value " + combined_demographics.get(XNATSchema.instance().getXnatFieldName("xnat:"+field)));
 							//System.out.println("raw xml "+subject.subject_xml);
 							xext.insertData(subject.getXML(), "xnat:"+field, combined_demographics.get(XNATSchema.instance().getXnatFieldName("xnat:"+field)));
 						}
 					}
 					//temporally commented out setDestinationID
 					String response = api.postSubject(target);
 					subject.setDestinationID(response.substring(response.lastIndexOf('/')+1));
 					
 					if(!api.putSubject(target, subject)){
 						throw new PipelineServiceException("Unable to upload subject: " + subject.getID());
 					}
 
 					// upload experiment information -Matt
 					for(String eid : subject.experiment_ids){
 						XNATExperiment experiment = project.experiments.get(eid);
 						
 						response = api.postExperiment(target, subject, experiment);
 						experiment.setDestinationID(response.substring(response.lastIndexOf('/')+1));
 
 						
 						// upload scans -Matt
 						for(String scan_id : subject.scan_ids.get(eid)){
 							XNATScan scan = subject.scans.get(scan_id);
 							
 							response = api.postScan(target, subject, experiment, scan);
 							scan.setDestinationID(response.substring(response.lastIndexOf('/')+1));
 						
 
 							// upload DICOM files -Matt
 							api.uploadDICOMFiles(target, subject, experiment, scan);
 						}
 					}
 				}else{
 					System.out.println("Subject: " + subject.getID() + " filtered");
 					
 				}
 			}
 			db.insertRequestInfo(r_info);
 		}catch(PipelineServiceException pse){
 			pse.printStackTrace();
 			
 			if(email_errors){
 				String error_text = "["+human_date+"]: Pipeline Service Exception encountered in XNATRedaction engine, contact your system administrator";
 			
 				// upload error message to database
 			}
 			
 			Globals.application_log.write("e", "Pipeline Service Exception Encountered: " + pse.getMessage());
 			Globals.application_log.write("e", Globals.stackTraceConvert(pse));
 		}catch(CompileException ce){
 			ce.printStackTrace();
 			
 			if(email_errors){
 				String error_text = "["+human_date+"]: Compiler Exception encountered in XNATRedaction engine, contact systema administrator";
 
 				
 				// upload error message to database
 			}
 			
 			Globals.application_log.write("e", "Compiler Exception Encountered: " + ce.getMessage());
 			Globals.application_log.write("e", Globals.stackTraceConvert(ce));
 		}
 	}
 
 }
