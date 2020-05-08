 package common;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import models.BlogCampaign;
 import models.FormulaCampaign;
 import models.GoogleCampaign;
 import models.Lead;
 import models.Rule;
 import models.SMSCampaign;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 import play.Logger;
 import play.db.jpa.Model;
 import play.libs.WS;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.marketo.mktows.client.MktServiceException;
 import com.marketo.mktows.client.MktowsClientException;
 import com.marketo.mktows.client.MktowsUtil;
 import com.marketo.mktows.wsdl.ArrayOfAttribute;
 import com.marketo.mktows.wsdl.Attrib;
 import com.marketo.mktows.wsdl.LeadKey;
 import com.marketo.mktows.wsdl.LeadKeyRef;
 import com.marketo.mktows.wsdl.LeadRecord;
 import com.marketo.mktows.wsdl.ResultGetMultipleLeads;
 import com.marketo.mktows.wsdl.ResultSyncLead;
 import com.twilio.sdk.TwilioRestException;
 
 public class MarketoUtility {
 
 	public static void main(String[] args) {
 		MarketoUtility mu = new MarketoUtility();
 		SMSCampaign sc = new SMSCampaign();
 		sc.munchkinAccountId = Constants.MUNCHKINID;
 		sc.soapUserId = Constants.SOAP_USER_ID;
 		sc.soapEncKey = Constants.SOAP_ENC_KEY;
 		sc.programName = Constants.PROG_NAME;
 		sc.campaignToLogOutgoingRequests = Constants.OUTBOUND_CAMP;
 		mu.requestCampaign(sc, sc.campaignToLogOutgoingRequests, 2,
 				Constants.SMS_OUTBOUND, "abcdefg");
 
 	}
 
 	public Model readSettings(String targetUrl, int campaignType) {
 		play.libs.WS.HttpResponse res = WS.url(targetUrl).get();
 		int status = res.getStatus();
 		if (status != 200) {
 			Logger.error("Unable to read settings from %s.  Got response %d",
 					targetUrl, status);
 			return null;
 		}
 		JsonElement retVal = res.getJson();
 		SMSCampaign sc = null;
 		GoogleCampaign gc = null;
 		FormulaCampaign fc = null;
 		BlogCampaign bc = null;
 		try {
 			Gson gson = new GsonBuilder().create();
 			switch (campaignType) {
 			case Constants.CAMPAIGN_SMS:
 				sc = gson.fromJson(retVal, SMSCampaign.class);
 				sc.campaignURL = targetUrl;
 				List<Rule> ruleList = extractRules(sc);
 				if (ruleList.size() == 0) {
 					Logger.error("Unable to extract rules from json");
 					return null;
 				}
 				sc.munchkinAccountId = sc.munchkinAccountId == null ? null
 						: sc.munchkinAccountId.toUpperCase();
 				return sc;
 
 			case Constants.CAMPAIGN_GOOG:
 				gc = gson.fromJson(retVal, GoogleCampaign.class);
 				gc.munchkinId = StringEscapeUtils.unescapeHtml(gc.munchkinId);
 				gc.munchkinId = gc.munchkinId == null ? null
 						: gc.munchkinId.toLowerCase();
 				Logger.debug("Read values from settings file : munchkinId[%s]",
 						gc.munchkinId);
 				gc.campaignURL = targetUrl;
 				return gc;
 
 			case Constants.CAMPAIGN_FORMULA:
 				fc = gson.fromJson(retVal, FormulaCampaign.class);
 				fc.soapUserId = StringEscapeUtils.unescapeHtml(fc.soapUserId);
 				fc.soapEncKey = StringEscapeUtils.unescapeHtml(fc.soapEncKey);
 				fc.munchkinAccountId = StringEscapeUtils
 						.unescapeHtml(fc.munchkinAccountId);
 				fc.munchkinAccountId = fc.munchkinAccountId == null ? null
						: fc.munchkinAccountId.toLowerCase();
 				Logger.debug("Read values from settings file : munchkinId[%s]",
 						fc.munchkinAccountId);
 				fc.campaignURL = targetUrl;
 				return fc;
 				
 			case Constants.CAMPAIGN_BLOG:
 				bc = gson.fromJson(retVal, BlogCampaign.class);
 				bc.soapUserId = StringEscapeUtils.unescapeHtml(bc.soapUserId);
 				bc.soapEncKey = StringEscapeUtils.unescapeHtml(bc.soapEncKey);
 				bc.munchkinAccountId = StringEscapeUtils
 						.unescapeHtml(bc.munchkinAccountId);
 				bc.munchkinAccountId = bc.munchkinAccountId == null ? null
 						: bc.munchkinAccountId.toUpperCase();
 				Logger.debug("Read values from settings file : munchkinId[%s]",
 						bc.munchkinAccountId);
 				bc.url = targetUrl;
 				return bc;
 			}
 
 		} catch (Exception e) {
 			Logger.error("Unable to parse %s into json", retVal);
 			Logger.error("Exception is %s", e.getMessage());
 			return null;
 		}
 		return null;
 	}
 
 	/*
 	 * Example definition "mktSmsOut(this kicks off the
 	 * campaign):::mktSmsIn(contains(stop,unsub)),mktSmsOut(we have unsubscribed
 	 * you):::mktSmsIn(contains(optin, start)), mktSmsOut(you are now
 	 * subscribed):::mktSmsIn(matches(votes for 1)),mktSmsOut(you voted for
 	 * choice 1):::mktSmsIn(*),mktSmsOut(this is a default mesg)"
 	 */
 	private ArrayList<Rule> extractRules(SMSCampaign sc) {
 		if (sc.smsCampaignDefinition == null
 				|| "".equals(sc.smsCampaignDefinition)) {
 			Logger.debug("campaign[%d] - Null or empty campaign definition",
 					sc.id);
 			return null;
 		}
 		String[] rulePair = sc.smsCampaignDefinition.split(":::");
 		ArrayList<Rule> retVal = new ArrayList<Rule>();
 		int cntr = 0;
 		for (String pair : rulePair) {
 			if (cntr == SMSCampaign.MAX_RULES) {
 				Logger.debug(
 						"campaign[%d] - Sorry, we will only accept the first %d rules",
 						SMSCampaign.MAX_RULES);
 				break;
 			}
 			int idxOut = pair.indexOf("mktSmsOut");
 			int idxIn = pair.indexOf("mktSmsIn");
 			String inRule = null;
 			String outRule = null;
 			if (idxIn != -1) {
 				if (idxOut > idxIn) {
 					int idxEndingParanthesis = pair.indexOf("),");
 					if (idxEndingParanthesis == -1) {
 						// something wrong if there is no closing paranthesis,
 						// ignore this
 						Logger.debug(
 								"campaign[%d] - Mismatched paranthesis in rule pair %s",
 								sc.id, pair);
 						continue;
 					}
 					inRule = pair.substring(idxIn + 9, idxEndingParanthesis);
 					Logger.debug("campaign[%d] - inRule[%d] %s", sc.id, cntr,
 							inRule);
 				} else {
 					/*
 					 * ignore this case where mktSmsOut is followed by mktSmsIn
 					 * because it can be accomplished using a new rulePair
 					 */
 					Logger.error(
 							"campaign[%d] will ignore %s - mktSmsIn must always preced mktSmsOut",
 							sc.id, pair);
 				}
 			}
 			if (idxOut != -1) {
 				int idxEndingParanthesis = pair.lastIndexOf(")");
 				if (idxEndingParanthesis == -1) {
 					// something wrong if there is no closing paranthesis,
 					// ignore this
 					Logger.debug(
 							"campaign[%d] - Mismatched paranthesis in rule pair %s",
 							sc.id, pair);
 					continue;
 				}
 				outRule = pair.substring(idxOut + 10, idxEndingParanthesis);
 				Logger.debug("campaign[%d] - outRule[%d] %s", sc.id, cntr,
 						outRule);
 
 			}
 
 			// if you got this far, we have an inRule and an outRule
 			Rule newRule = new Rule(sc, inRule, outRule);
 			sc.rules.add(newRule);
 			retVal.add(newRule);
 			cntr++;
 		}
 		Logger.info("campaign[%d] - parsed %d rules.  accepted %d", sc.id,
 				cntr, retVal.size());
 		return retVal;
 	}
 
 	public List<LeadRecord> fetchFromStaticList(String soapUserId,
 			String soapEncKey, String munchkinAccountId, Long campaignId,
 			String programName, String staticListName, String[] fields) {
 		Logger.info("campaign[%d] - trying to fetch leads from list %s.%s",
 				campaignId, programName, staticListName);
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		List<String> leadAttrs = new ArrayList<String>();
 
 		List<LeadRecord> leadRecords = new ArrayList<LeadRecord>();
 		;
 
 		try {
 			MktowsClient client = makeSoapConnection(campaignId, soapUserId,
 					soapEncKey, munchkinAccountId);
 
 			Logger.debug("campaign[%d] - get multiple leads from list :%s",
 					campaignId, staticListName);
 			String listName = programName + "." + staticListName;
 			String listFields = "";
 			for (int i = 0; i < fields.length; i++) {
 				leadAttrs.add(fields[i].trim());
 				listFields += fields[i] + ",";
 			}
 			Logger.debug("campaign[%d] - requesting attributes %s", campaignId,
 					listFields);
 			boolean tryAgain = false;
 			do {
 				tryAgain = false;
 				ResultGetMultipleLeads result = client.getMultipleLeads(
 						Constants.BATCH_SIZE, listName, posHolder, leadRecords,
 						leadAttrs);
 				if (result != null && result.getRemainingCount() > 0) {
 					Logger.debug(
 							"campaign[%d] - %d records remaining,  will try again",
 							campaignId, result.getRemainingCount());
 					tryAgain = true;
 				}
 
 			} while (tryAgain);
 			Logger.debug("campaign[%d] - returning total of %d records",
 					campaignId, leadRecords.size());
 			return leadRecords;
 		} catch (MktowsClientException e) {
 			Logger.error(
 					"campaign[%d] - Exception occurred while fetching leads from list: %s",
 					campaignId, e.getMessage());
 			return leadRecords;
 		} catch (MktServiceException e) {
 			Logger.error("campaign[%d] - Exception occurred: %s", campaignId,
 					e.getLongMessage());
 			return leadRecords;
 		}
 	}
 
 	/**
 	 * 
 	 * @param soapUserId
 	 * @param soapEncKey
 	 * @param munchkinAccountId
 	 * @param campaignId
 	 * @param programName
 	 * @param staticListName
 	 * @param fields
 	 * @param phoneNumFieldApiName
 	 * @return
 	 */
 	public List<Lead> fetchFromStaticListForSms(String soapUserId,
 			String soapEncKey, String munchkinAccountId, Long campaignId,
 			String programName, String staticListName, String[] fields,
 			String phoneNumFieldApiName) {
 		Logger.info("campaign[%d] - trying to fetch leads from list %s",
 				campaignId, staticListName);
 		List<LeadRecord> leadRecords = new ArrayList<LeadRecord>();
 		List<Lead> leadList = new ArrayList<Lead>();
 
 		leadRecords = fetchFromStaticList(soapUserId, soapEncKey,
 				munchkinAccountId, campaignId, programName, staticListName,
 				fields);
 		for (LeadRecord item : leadRecords) {
 			Lead newLead = new Lead();
 			newLead.munchkinId = munchkinAccountId;
 			newLead.leadId = item.getId();
 			// newLead.email = item.getEmail();
 			Logger.debug("processing lead with id : %d", newLead.leadId);
 
 			Map<String, Object> attrMap = null;
 			ArrayOfAttribute aoAttribute = item.getLeadAttributeList();
 			if (aoAttribute != null) {
 				attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 				if (attrMap != null && !attrMap.isEmpty()) {
 					Set<String> keySet = attrMap.keySet();
 					if (keySet.contains(phoneNumFieldApiName)) {
 						newLead.phoneNumber = attrMap.get(phoneNumFieldApiName)
 								.toString();
 						Logger.debug("lead with id : %d has %s set to %s",
 								newLead.leadId, phoneNumFieldApiName,
 								newLead.phoneNumber);
 					}
 					if (keySet.contains(Constants.COUNTRY_FIELD_NAME)) {
 						newLead.country = attrMap.get(
 								Constants.COUNTRY_FIELD_NAME).toString();
 						Logger.debug("lead with id : %d has country %s",
 								newLead.leadId, newLead.country);
 					} else {
 						newLead.country = "USA";
 						Logger.debug(
 								"lead with id : %d does not have country.  Using USA",
 								newLead.leadId);
 					}
 					if (keySet.contains(Constants.UNSUB_FIELD_NAME)) {
 						String unsubValue = attrMap.get(
 								Constants.UNSUB_FIELD_NAME).toString();
 						if (unsubValue.equals("1")) {
 							newLead.unsubscribed = true;
 						} else {
 							// should never come here
 							newLead.unsubscribed = false;
 						}
 					} else {
 						newLead.unsubscribed = false;
 					}
 					Logger.debug(
 							"lead with id : %d has sms unsubscribed set to %s",
 							newLead.leadId,
 							String.valueOf(newLead.unsubscribed));
 				}
 			}
 			newLead.save();
 			leadList.add(newLead);
 		}
 		Logger.debug("campaign[%d] - retrieved and parsed %d leads",
 				campaignId, leadRecords.size());
 
 		Logger.debug("campaign[%d] - returning %d leads", campaignId,
 				leadList.size());
 		return leadList;
 	}
 
 	public ExecStatus executeFormula(FormulaCampaign fc) {
 		boolean syncMultiple = false;
 		Logger.debug("campaign[%d] - In executeFormula for command set %s",
 				fc.id, fc.formula);
 
 		List<LeadRecord> inflightList = null;
 		List<LeadRecord> processedLeadList = new ArrayList<LeadRecord>();
 		MarketoUtility mu = new MarketoUtility();
 		CodeSandbox csb = new CodeSandbox(fc.soapUserId, fc.soapEncKey,
 				fc.munchkinAccountId, fc.id);
 
 		processedLeadList = new ArrayList<LeadRecord>();
 		Logger.debug("campaign[%d] - executing command %s", fc.id, fc.formula);
 		if (fc.formula.startsWith(Constants.FORMULA_STRING_PROPER)) {
 			int length = Constants.FORMULA_STRING_PROPER.length();
 			String[] vars = fc.formula.substring(length + 1).split("[(),]");
 			if (vars.length < 1) {
 				String errMsg = "Need at least one field name to capitalize, Got "
 						+ vars.length + " parameters";
 				Logger.error(errMsg);
 				return new ExecStatus(errMsg, 0);
 			}
 			inflightList = mu.fetchFromStaticList(fc.soapUserId, fc.soapEncKey,
 					fc.munchkinAccountId, fc.id, fc.programName, fc.leadList,
 					vars);
 
 			processedLeadList = csb.mktoProperCaseField(inflightList, vars);
 			syncMultiple = true;
 
 		} else if (fc.formula.startsWith(Constants.FORMULA_STRING_UPPER)
 				|| fc.formula.startsWith(Constants.FORMULA_STRING_LOWER)) {
 			int length = 0;
 			if (fc.formula.startsWith(Constants.FORMULA_STRING_UPPER)) {
 				length = Constants.FORMULA_STRING_UPPER.length();
 			} else if (fc.formula.startsWith(Constants.FORMULA_STRING_LOWER)) {
 				length = Constants.FORMULA_STRING_LOWER.length();
 			}
 			String[] vars = fc.formula.substring(length + 1).split("[(),]");
 			if (vars.length < 1) {
 				String errMsg = "Need at least one field name to capitalize, Got"
 						+ vars.length + " parameters";
 				Logger.error(errMsg);
 				return new ExecStatus(errMsg, 0);
 			}
 			inflightList = mu.fetchFromStaticList(fc.soapUserId, fc.soapEncKey,
 					fc.munchkinAccountId, fc.id, fc.programName, fc.leadList,
 					vars);
 
 			processedLeadList = csb.mktoCaseChange(inflightList, fc.formula,
 					vars);
 			syncMultiple = true;
 
 		} else if (fc.formula.startsWith(Constants.FORMULA_ADD)) {
 			int length = Constants.FORMULA_ADD.length();
 			String[] vars = fc.formula.substring(length + 1).split("[(),]");
 			if (vars.length != 3) {
 				String errMsg = "Need 3 fields to add scores and write back.  Got "
 						+ vars.length + " parameters";
 				Logger.error(errMsg);
 				return new ExecStatus(errMsg, 0);
 			}
 			inflightList = mu.fetchFromStaticList(fc.soapUserId, fc.soapEncKey,
 					fc.munchkinAccountId, fc.id, fc.programName, fc.leadList,
 					vars);
 
 			processedLeadList = csb.mktoAddScores(inflightList, vars[0].trim(),
 					vars[1].trim(), vars[2].trim());
 			syncMultiple = true;
 
 		} else if (fc.formula.startsWith(Constants.FORMULA_GEOCODE_PHONE)) {
 			int length = Constants.FORMULA_GEOCODE_PHONE.length();
 			String[] vars = fc.formula.substring(length + 1).split("[(),]");
 			if (vars.length != 3) {
 				String errMsg = "Need the phone number and city and region field names,   Got "
 						+ vars.length + " parameters";
 				Logger.error(errMsg);
 				return new ExecStatus(errMsg, 0);
 			}
 			inflightList = mu.fetchFromStaticList(fc.soapUserId, fc.soapEncKey,
 					fc.munchkinAccountId, fc.id, fc.programName, fc.leadList,
 					vars);
 
 			processedLeadList = csb.mktoGeocodePhone(inflightList,
 					vars[0].trim(), vars[1].trim(), vars[2].trim());
 			syncMultiple = true;
 
 		} else if (fc.formula.startsWith(Constants.FORMULA_PHONE_FORMAT)) {
 			int length = Constants.FORMULA_PHONE_FORMAT.length();
 			String[] vars = fc.formula.substring(length + 1).split("[(),]");
 			if (vars.length != 2) {
 				String errMsg = "Need the phone number and format type,   Got "
 						+ vars.length + " parameters";
 				Logger.error(errMsg);
 				return new ExecStatus(errMsg, 0);
 			}
 			inflightList = mu.fetchFromStaticList(fc.soapUserId, fc.soapEncKey,
 					fc.munchkinAccountId, fc.id, fc.programName, fc.leadList,
 					vars);
 
 			processedLeadList = csb.mktoPhoneFormat(inflightList,
 					vars[0].trim(), vars[1].trim());
 			syncMultiple = true;
 		} else { // custom code
 			// no-op for now
 			/*
 			 * String className = "MarketoSandBox" + campaignId; CtClass
 			 * mktoClass = csb.createClass("MarketoSandBox" + campaignId);
 			 * String methodName = csb.getMethodName(); if
 			 * (!csb.methodExists(mktoClass, methodName)) {
 			 * csb.addMethod(mktoClass, formula); } try { processedLeadList =
 			 * csb.executeMethod(mktoClass.toClass(), methodName, inflightList);
 			 * } catch (CannotCompileException e) {
 			 * Logger.error("Unable to execute method %s", methodName); return
 			 * null; } mktoClass.detach();
 			 */
 		}
 		if (syncMultiple) {
 			csb.syncMultipleLeads(processedLeadList, true);
 		}
 		return new ExecStatus("All Done", processedLeadList.size());
 	}
 
 	public ResultSyncLead createNewLead(SMSCampaign sc, String from) {
 		ResultSyncLead result = null;
 		try {
 			HashMap<String, String> attrs = new HashMap<String, String>();
 			attrs.put(sc.phoneNumFieldApiName, from);
 			LeadRecord leadRec = MktowsUtil.newLeadRecord(null, null, null,
 					null, attrs);
 
 			MktowsClient client = makeSoapConnection(sc.id, sc.soapUserId,
 					sc.soapEncKey, sc.munchkinAccountId);
 			Logger.debug(
 					"campaign[%d] - calling sync lead on lead with phone number %s",
 					sc.id, from);
 
 			result = client.syncLead(leadRec, null, true);
 		} catch (MktowsClientException e) {
 			Logger.error("campaign[%d] - Exception occurred: %s", sc.id,
 					e.getMessage());
 			return null;
 		} catch (MktServiceException e) {
 			Logger.error("campaign[%d] - Exception occurred: %s", sc.id,
 					e.getLongMessage());
 			return null;
 		}
 		return result;
 	}
 
 	public void deleteLead(SMSCampaign sc, ResultSyncLead dummyLead) {
 
 	}
 
 	public MktowsClient makeSoapConnection(Long scid, String soapUserId,
 			String soapEncKey, String munchkinAccountId) {
 		Logger.debug(
 				"campaign[%d] - making soap connection user:%s encKey:%s munchId:%s",
 				scid, soapUserId, soapEncKey, munchkinAccountId);
 		return new MktowsClient(soapUserId, soapEncKey, munchkinAccountId
 				+ ".mktoapi.com");
 	}
 
 	public void requestCampaign(SMSCampaign sc, String campaignName,
 			Integer leadId, Integer smsDirection, String body) {
 		// Request that lead(s) be added to the campaign
 		List<Attrib> tokenList = new ArrayList<Attrib>();
 		Attrib token = null;
 		token = MktowsUtil.objectFactory.createAttrib();
 		if (smsDirection == Constants.SMS_INBOUND) {
 			token.setName("my.inboundSMSText");
 		} else {
 			token.setName("my.outboundSMSText");
 		}
 		token.setValue(body);
 		tokenList.add(token);
 
 		LeadKey leadKey = MktowsUtil.objectFactory.createLeadKey();
 		leadKey.setKeyType(LeadKeyRef.IDNUM);
 		leadKey.setKeyValue(String.valueOf(leadId));
 		List<LeadKey> leadList = new ArrayList<LeadKey>();
 		leadList.add(leadKey);
 
 		boolean success = false;
 		try {
 			MktowsClient client = makeSoapConnection(sc.id, sc.soapUserId,
 					sc.soapEncKey, sc.munchkinAccountId);
 			Logger.debug(
 					"campaign[%d] - calling requestCampaign prog:%s campaign:%s #leads:%d token:%s",
 					sc.id, sc.programName, campaignName, leadList.size(),
 					token.getName());
 			success = client.requestCampaign(sc.programName, campaignName,
 					leadList, tokenList);
 		} catch (MktowsClientException e) {
 			Logger.error("campaign[%d] - Exception occurred: %s", sc.id,
 					e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			Logger.error("campaign[%d] - Exception occurred: %s", sc.id,
 					e.getLongMessage());
 			return;
 		}
 		if (success) {
 			Logger.info("campaign[%d] - Lead %d added to campaign %s", sc.id,
 					leadId, campaignName);
 		} else {
 			Logger.error("campaign[%d] - Failed to add lead %d to campaign %s",
 					sc.id, leadId, campaignName);
 		}
 	}
 
 	public boolean setLeadUnsubscribed(SMSCampaign sc, Integer leadId,
 			String value) {
 		Logger.debug("campaign[%d] - Fetching lead with id:%d", sc.id, leadId);
 		List<Lead> leadList = Lead.find("munchkinId = ? and leadId = ? ",
 				sc.munchkinAccountId, leadId).fetch();
 		if (leadList != null) {
 			for (Lead ld : leadList) {
 				HashMap<String, String> attrs = new HashMap<String, String>();
 				attrs.put(Constants.UNSUB_FIELD_NAME, value);
 				LeadRecord leadRec = MktowsUtil.newLeadRecord(leadId, null,
 						null, null, attrs);
 
 				MktowsClient client = makeSoapConnection(sc.id, sc.soapUserId,
 						sc.soapEncKey, sc.munchkinAccountId);
 				try {
 					Logger.info(
 							"campaign[%d] - Setting sms unsubscribed:%s for lead:",
 							sc.id, value, leadId);
 					client.syncLead(leadRec, null, true);
 				} catch (MktowsClientException e) {
 					Logger.error("campaign[%d] - Exception occurred: %s",
 							sc.id, e.getMessage());
 				} catch (MktServiceException e) {
 					Logger.error("campaign[%d] - Exception occurred: %s",
 							sc.id, e.getLongMessage());
 				}
 				// set the flag locally as well
 				ld.unsubscribed = Boolean.valueOf(value);
 				ld.save();
 				return ld.unsubscribed;
 			}
 		}
 		// do not send if lead not present
 		Logger.debug("Unable to find lead with leadId : %d in local database",
 				leadId);
 		return true;
 	}
 
 	public boolean getLeadSubscriptionSetting(SMSCampaign sc, Integer leadId) {
 		Logger.debug("campaign[%d] - Fetching lead with id:%d", sc.id, leadId);
 		List<Lead> leadList = Lead.find("munchkinId = ? and leadId = ? ",
 				sc.munchkinAccountId, leadId).fetch();
 		if (leadList != null) {
 			for (Lead ld : leadList) {
 				Logger.debug(
 						"campaign[%d] - Returning unsubscribed:%s for id:%d",
 						sc.id, String.valueOf(ld.unsubscribed), leadId);
 				return ld.unsubscribed;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param sc
 	 * @param leadList
 	 * @param rule
 	 * @param from
 	 */
 	public int performOutRule(SMSCampaign sc, Rule rule, List<Lead> leadList) {
 		Boolean subscribeUser = false;
 		Boolean unsubscribeUser = false;
 		Boolean operationalCampaign = false;
 		Boolean multiByteString = false;
 		int numSent = 0;
 		if (rule.outRule != null) {
 			String payload = null;
 			String[] keywords = rule.outRule.split("::");
 			for (String word : keywords) {
 				if (word.equals("mktUnsubscribe")) {
 					Logger.debug("campaign[%d] - will unsubscribe user", sc.id);
 					unsubscribeUser = true;
 				}
 				if (word.equals("mktSubscribe")) {
 					Logger.debug("campaign[%d] - will subscribe user", sc.id);
 					subscribeUser = true;
 				}
 
 				if (word.startsWith("operational(")) {
 					int idxEndingParanthesis = word.lastIndexOf(")");
 					if (idxEndingParanthesis == -1) {
 						Logger.error(
 								"campaign[%d] - no ending paranthesis for rule %s",
 								sc.id, word);
 					}
 					operationalCampaign = true;
 					Logger.debug("campaign[%d] - %s is an operational rule",
 							sc.id, rule.outRule);
 					payload = word.substring(12, idxEndingParanthesis);
 					Logger.debug("campaign[%d] - payload is %s", sc.id, payload);
 				} else {
 					payload = rule.outRule;
 					Logger.debug("campaign[%d] - payload is %s", sc.id, payload);
 				}
 				multiByteString = isPayloadMultiByte(payload);
 				Logger.debug(
 						"campaign[%d] - payload is using multi-byte string = [%s]",
 						sc.id, String.valueOf(multiByteString));
 				if (sc.smsFooter != null && !sc.smsFooter.equals("null")) {
 					payload = payload.concat(sc.smsFooter);
 					int maxlen = (multiByteString ? Constants.SMS_MAX_LEN / 2
 							: Constants.SMS_MAX_LEN);
 					if (payload.length() > maxlen) {
 						payload = payload.substring(0, maxlen); // max SMS
 																// length
 					}
 					Logger.debug(
 							"campaign[%d] - payload with footer is %s.  Length [%d] is less than max [%d]",
 							sc.id, payload, payload.length(), maxlen);
 				}
 			}
 			try {
 				for (Lead ld : leadList) {
 					if (unsubscribeUser) {
 						Logger.debug(
 								"campaign[%d] - Unsubscribing user with phone %s",
 								sc.id, ld.phoneNumber);
 						setLeadUnsubscribed(sc, ld.leadId, "true");
 						ld.unsubscribed = true;
 					}
 					if (subscribeUser) {
 						Logger.debug(
 								"campaign[%d] - Subscribing user with phone %s",
 								sc.id, ld.phoneNumber);
 						setLeadUnsubscribed(sc, ld.leadId, "false");
 						ld.unsubscribed = false;
 					}
 					if (operationalCampaign || ld.unsubscribed == false) {
 						Logger.debug(
 								"campaign[%d] - Sending message to %s : payload %s",
 								sc.id, ld.phoneNumber, payload);
 						String status = TwilioUtility.sendSMS(sc.smsGatewayID,
 								sc.smsGatewayPassword,
 								sc.smsGatewayPhoneNumber, ld.phoneNumber,
 								ld.country, payload);
 						MarketoUtility mu = new MarketoUtility();
 						Logger.debug(
 								"campaign[%d] - Requesting campaign %s for lead with id %d.  Status %s",
 								sc.id, sc.campaignToLogOutgoingRequests,
 								ld.leadId, status);
 						mu.requestCampaign(sc,
 								sc.campaignToLogOutgoingRequests, ld.leadId,
 								Constants.SMS_OUTBOUND, payload + ":" + status);
 						Logger.debug(
 								"campaign[%d] - Request campaign %s succeeded",
 								sc.id, sc.campaignToLogOutgoingRequests);
 						numSent++;
 					} else {
 						Logger.info(
 								"campaign[%d] - Not sending message to %s because lead has unsubscribed",
 								sc.id, ld.phoneNumber);
 					}
 				}
 			} catch (TwilioRestException e) {
 				Logger.error("campaign[%d] - Error talking to Twilio %s",
 						sc.id, e.getMessage());
 			}
 
 		}
 		return numSent;
 	}
 
 	private Boolean isPayloadMultiByte(String str) {
 		char[] c_array;
 		String c_string;
 		byte[] c_byte_array;
 		Boolean result = false;
 
 		c_array = str.toCharArray();
 		result = false;
 		for (char c : c_array) {
 			c_string = Character.toString(c);
 			try {
 				c_byte_array = c_string.getBytes("UTF-8");
 				if (c_byte_array.length > 1) {
 					Logger.debug(
 							"Detected a multibyte character in payload [%s]",
 							str);
 					result = true;
 					break;
 				}
 			} catch (UnsupportedEncodingException e) {
 				Logger.error(
 						"Unable to detect multibyte character due to exception [%s]",
 						e.getMessage());
 			}
 		}
 		return result;
 	}
 
 }
