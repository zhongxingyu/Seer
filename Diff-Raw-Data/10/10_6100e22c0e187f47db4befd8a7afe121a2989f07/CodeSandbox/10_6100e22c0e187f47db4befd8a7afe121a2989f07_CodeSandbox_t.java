 package common;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.WordUtils;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.NotFoundException;
 import play.Logger;
 
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
 import com.marketo.mktows.client.MktServiceException;
 import com.marketo.mktows.client.MktowsClientException;
 import com.marketo.mktows.client.MktowsUtil;
 import com.marketo.mktows.wsdl.ArrayOfAttribute;
 import com.marketo.mktows.wsdl.LeadRecord;
 import com.marketo.mktows.wsdl.SyncStatus;
 
 public class CodeSandbox {
 
 	private MktowsClient client;
 	private String munchkinAccountId;
 	private Long campaignId;
 
 	public CodeSandbox(String accessKey, String secretKey,
 			String munchkinAccountId, Long campaignId) {
 		this.munchkinAccountId = munchkinAccountId;
 		this.campaignId = campaignId;
 		String hostName = munchkinAccountId + ".mktoapi.com";
 		client = new MktowsClient(accessKey, secretKey, hostName);
 	}
 
 	/**
 	 * Makes the first character uppercase
 	 * 
 	 * @param inflightList
 	 * @param fieldNames
 	 * @return
 	 */
 	public List<LeadRecord> mktoProperCaseField(List<LeadRecord> inflightList,
 			String[] fieldNames) {
 		List<LeadRecord> retList = new ArrayList<LeadRecord>();
 		LeadRecord retLead = null;
 		for (LeadRecord lr : inflightList) {
 			retLead = null;
 			retLead = mktoProperCaseLeadFields(lr, fieldNames, false);
 			if (retLead != null) {
 				retList.add(retLead);
 			}
 		}
 		return retList;
 	}
 
 	public LeadRecord mktoProperCaseLeadFields(LeadRecord leadRecord,
 			String[] fieldNames, boolean syncImmediate) {
 		if (leadRecord == null) {
 			return null;
 		}
 		HashMap<String, String> newAttrs = new HashMap<String, String>();
 		boolean valueChanged = false;
 
 		String fld;
 		for (int i = 0; i < fieldNames.length; i++) {
 			fld = fieldNames[i].trim();
 			String fv = extractAttributeValue(leadRecord, fld);
 			String nfv = "";
 			if (fv != null) {
 				nfv = WordUtils.capitalize(fv);
 				newAttrs.put(fld, nfv);
 				Logger.debug("Capitalizing %s to : %s", fld, nfv);
 				if (!fv.equals(nfv)) {
 					Logger.debug("Value changed for lead id :%d",
 							leadRecord.getId());
 					valueChanged = true;
 				}
 			}
 		}
 		if (valueChanged) {
 			LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 					leadRecord.getId(), null, null, null, newAttrs);
 			try {
 				if (syncImmediate) {
 					client.syncLead(newLeadRecord, null, false);
 				}
 			} catch (MktowsClientException e) {
 				Logger.error("Unable to sync lead with capitalized name:%s",
 						e.getMessage());
 			} catch (MktServiceException e) {
 				Logger.error("Unable to sync lead with capitalized name:%s",
 						e.getMessage());
 			}
 			return newLeadRecord;
 		}
 		return null;
 	}
 
 	public List<LeadRecord> mktoCaseChange(List<LeadRecord> inflightList,
 			String formula, String[] fieldNames) {
 		List<LeadRecord> retList = new ArrayList<LeadRecord>();
 		LeadRecord retLead = null;
 		for (LeadRecord lr : inflightList) {
 			retLead = null;
 			retLead = mktoCaseChangeLeadFields(lr, formula, fieldNames, false);
 			if (retLead != null) {
 				retList.add(retLead);
 			}
 		}
 		return retList;
 	}
 
 	private LeadRecord mktoCaseChangeLeadFields(LeadRecord leadRecord,
 			String formula, String[] fieldNames, boolean syncImmediate) {
 		if (leadRecord == null) {
 			return null;
 		}
 		int toCase = Constants.UPPERCASE;
 		if (formula.startsWith(Constants.FORMULA_STRING_LOWER)) {
 			toCase = Constants.LOWERCASE;
 		}
 		HashMap<String, String> newAttrs = new HashMap<String, String>();
 		boolean valueChanged = false;
 
 		String fld;
 		for (int i = 0; i < fieldNames.length; i++) {
 			fld = fieldNames[i].trim();
 			String fv = extractAttributeValue(leadRecord, fld);
 			String nfv = "";
 			if (fv != null) {
 				if (toCase == Constants.UPPERCASE) {
 					nfv = fv.toUpperCase();
 				} else {
 					nfv = fv.toLowerCase();
 				}
 				newAttrs.put(fld, nfv);
 				Logger.debug("Changing %s to : %s", fld, nfv);
 				if (!fv.equals(nfv)) {
 					Logger.debug("Value changed for lead id :%d",
 							leadRecord.getId());
 					valueChanged = true;
 				}
 			}
 		}
 		if (valueChanged) {
 			LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 					leadRecord.getId(), null, null, null, newAttrs);
 			try {
 				if (syncImmediate) {
 					client.syncLead(newLeadRecord, null, false);
 				}
 			} catch (MktowsClientException e) {
 				Logger.error("Unable to sync lead with case changed value:%s",
 						e.getMessage());
 			} catch (MktServiceException e) {
 				Logger.error("Unable to sync lead with case changed value:%s",
 						e.getMessage());
 			}
 			return newLeadRecord;
 		}
 		return null;
 	}
 
 	public List<LeadRecord> mktoAddScores(List<LeadRecord> inflightList,
 			String storeNewValue, String score1, String score2) {
 		List<LeadRecord> retList = new ArrayList<LeadRecord>();
 		LeadRecord retLead = null;
 		for (LeadRecord lr : inflightList) {
 			retLead = null;
 			retLead = mktoAddLeadScores(lr, false, storeNewValue, score1,
 					score2);
 			if (retLead != null) {
 				retList.add(retLead);
 			}
 		}
 		return retList;
 	}
 
 	public LeadRecord mktoAddLeadScores(LeadRecord leadRecord,
 			boolean syncImmediate, String storeNewValue, String score1,
 			String score2) {
 		if (leadRecord == null) {
 			return null;
 		}
 
 		try {
 			Logger.debug("About to add scores %s and %s and store it in %s",
 					score1, score2, storeNewValue);
 			float sc1 = 0;
 			float sc2 = 0;
 			HashMap<String, String> newAttrs = new HashMap<String, String>();
 			String sc1Str = extractAttributeValue(leadRecord, score1);
 			String sc2Str = extractAttributeValue(leadRecord, score2);
 			String sc3Str = extractAttributeValue(leadRecord, storeNewValue);
 			if (sc1Str != null) {
 				Logger.debug("value of %s is %s", score1, sc1Str);
 				sc1 = Float.valueOf(sc1Str);
 			}
 			if (sc2Str != null) {
 				Logger.debug("value of %s is %s", score2, sc2Str);
 				sc2 = Float.valueOf(sc2Str);
 			}
 			float newScore = sc1 + sc2;
 			Logger.debug("New score is %f", newScore);
 
 			if (newScore != Float.valueOf(sc3Str)) {
 				newAttrs.put(storeNewValue, String.valueOf(newScore));
 				LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 						leadRecord.getId(), null, null, null, newAttrs);
 				if (syncImmediate) {
 					client.syncLead(newLeadRecord, null, false);
 				}
 				return newLeadRecord;
 			}
 
 		} catch (MktowsClientException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		} catch (MktServiceException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		}
 		return null;
 	}
 
 	public List<LeadRecord> mktoGeocodePhone(List<LeadRecord> inflightList,
 			String phoneField, String cityField, String regionField) {
 		List<LeadRecord> retList = new ArrayList<LeadRecord>();
 		LeadRecord retLead = null;
 		for (LeadRecord lr : inflightList) {
 			retLead = null;
 			retLead = mktoGeocodeLeadPhone(lr, false, phoneField, cityField,
 					regionField);
 			if (retLead != null) {
 				retList.add(retLead);
 			}
 		}
 		return retList;
 	}
 
 	private LeadRecord mktoGeocodeLeadPhone(LeadRecord leadRecord,
 			boolean syncImmediate, String phoneField, String cityField,
 			String regionField) {
 		if (leadRecord == null) {
 			return null;
 		}
 		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
 		PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder
 				.getInstance();
 		try {
 			Logger.debug("About to geolocate number in field %s", phoneField);
 			HashMap<String, String> newAttrs = new HashMap<String, String>();
 			String region = null;
 			String oldCity = extractAttributeValue(leadRecord, cityField);
 			String oldRegion = extractAttributeValue(leadRecord, regionField);
 			String pn = extractAttributeValue(leadRecord, phoneField);
 			Logger.debug("Phone number is %s", pn);
 			PhoneNumber phoneObj = phoneUtil.parse(pn, "US");
 			region = geocoder.getDescriptionForNumber(phoneObj, Locale.ENGLISH);
 			Logger.debug("Region for phone %s is %s.  old region is %s", pn,
 					region, oldRegion);
 			if (!region.equals("")) {
 				if (region.contains(",")) {
 					String[] values = region.split(",");
 					String cityValue = values[0].trim();
 					String regValue = values[1].trim();
 					String regValueShort = RegionUtil
 							.getStateShortCode(regValue);
 					if (regValueShort.equals("") || regValueShort == null) {
 						regValueShort = regValue;
 					}
 					if (oldCity == null || !oldCity.equalsIgnoreCase(cityValue)) {
 						Logger.debug("For Phone : %s, setting %s to %s", pn,
 								cityField, cityValue);
 						newAttrs.put(cityField, cityValue);
 					}
 					if (oldRegion == null
 							|| !oldRegion.equalsIgnoreCase(regValueShort)) {
 						Logger.debug("For Phone : %s, setting %s to %s", pn,
 								regionField, regValueShort);
 						newAttrs.put(regionField, regValueShort);
 					}
 				} else {
 					String regionCode = RegionUtil.getStateShortCode(region);
 					if (regionCode.equals("") || regionCode == null) {
 						regionCode = region;
 					}
					if (!regionCode.equals(oldRegion)) {
						if (regionCode.equals("US")) {
							Logger.debug(
									"For Phone : %s, clearing city and state because region=US",
									pn, cityField, regionCode);
							newAttrs.put(cityField, "");
							newAttrs.put(regionField, "");
						}
 						if (regionCode.contains("D.C")
 								|| regionCode.contains("DC")) {
 							Logger.debug(
 									"For Phone : %s, setting Washington DC",
 									pn, cityField, regionCode);
 							newAttrs.put(cityField, "Washington");
 							newAttrs.put(regionField, "DC");
 						} else {
 							Logger.debug("For Phone : %s, setting %s to %s",
 									pn, regionField, regionCode);
 							newAttrs.put(regionField, regionCode);
 						}
 					}
 				}
 				if (newAttrs.size() == 0) {
 					Logger.debug("No new attribs for Phone : %s", pn);
 					return null;
 				}
 				LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 						leadRecord.getId(), null, null, null, newAttrs);
 				if (syncImmediate) {
 					client.syncLead(newLeadRecord, null, false);
 				}
 				return newLeadRecord;
 			}
 		} catch (MktowsClientException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		} catch (MktServiceException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		} catch (NumberParseException e) {
 			Logger.error("Phone number parse Exception : %s", e.getMessage());
 		}
 		return null;
 	}
 
 	public List<LeadRecord> mktoPhoneFormat(List<LeadRecord> inflightList,
 			String phoneField, String formatType) {
 		List<LeadRecord> retList = new ArrayList<LeadRecord>();
 		LeadRecord retLead = null;
 		for (LeadRecord lr : inflightList) {
 			retLead = null;
 			retLead = mktoLeadPhoneFormat(lr, false, phoneField, formatType);
 			if (retLead != null) {
 				retList.add(retLead);
 			}
 		}
 		return retList;
 	}
 
 	private LeadRecord mktoLeadPhoneFormat(LeadRecord leadRecord,
 			boolean syncImmediate, String phoneField, String formatType) {
 		if (leadRecord == null) {
 			return null;
 		}
 		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
 		PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder
 				.getInstance();
 		try {
 			Logger.debug("About to format number in field %s", phoneField);
 			HashMap<String, String> newAttrs = new HashMap<String, String>();
 			String region = null;
 			String opn = extractAttributeValue(leadRecord, phoneField);
 			Logger.debug("Phone number is %s", opn);
 			PhoneNumber phoneObj = phoneUtil.parse(opn, "US");
 			String npn = "";
 			if (formatType.equalsIgnoreCase(Constants.PHONE_FORMAT_E164)) {
 				npn = phoneUtil.format(phoneObj, PhoneNumberFormat.E164);
 			} else if (formatType
 					.equalsIgnoreCase(Constants.PHONE_FORMAT_INTERNATIONAL)) {
 				npn = phoneUtil.format(phoneObj,
 						PhoneNumberFormat.INTERNATIONAL);
 			} else if (formatType
 					.equalsIgnoreCase(Constants.PHONE_FORMAT_NATIONAL)) {
 				npn = phoneUtil.format(phoneObj, PhoneNumberFormat.NATIONAL);
 			}
 			Logger.debug("Format for phone %s is %s.", opn, npn);
 			if (!npn.equals("") && !npn.equals(opn)) {
 				newAttrs.put(phoneField, npn);
 				LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 						leadRecord.getId(), null, null, null, newAttrs);
 				if (syncImmediate) {
 					client.syncLead(newLeadRecord, null, false);
 				}
 				return newLeadRecord;
 			}
 		} catch (MktowsClientException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		} catch (MktServiceException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		} catch (NumberParseException e) {
 			Logger.error("Phone number parse Exception : %s", e.getMessage());
 		}
 		return null;
 	}
 
 	private String extractAttributeValue(LeadRecord leadRecord, String fieldName) {
 		if (leadRecord == null) {
 			return null;
 		}
 		if (fieldName.equals(";")) {
 			Logger.debug("Dropping trailing ;");
 		}
 		Logger.debug("About to extract value of %s", fieldName);
 		Map<String, Object> attrMap = null;
 		ArrayOfAttribute aoAttribute = leadRecord.getLeadAttributeList();
 		if (aoAttribute != null) {
 			attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 			if (attrMap != null && !attrMap.isEmpty()) {
 				Set<String> keySet = attrMap.keySet();
 				if (keySet.contains(fieldName)) {
 					String fieldValue = attrMap.get(fieldName).toString();
 					Logger.debug("%s field is set to %s", fieldName, fieldValue);
 					return fieldValue;
 				}
 			}
 		}
 		return null;
 	}
 
 	public CtClass createClass(String className) {
 		ClassPool pool = ClassPool.getDefault();
 		CtClass mktoClass = null;
 		try {
 			pool.importPackage("com.marketo.mktows.wsdl");
 			pool.importPackage("com.marketo.mktows.client");
 			pool.importPackage("java.util");
 			mktoClass = pool.get(className);
 		} catch (NotFoundException e) {
 			Logger.debug("Did not find class :%s.  Will create new one",
 					className);
 			mktoClass = pool.makeClass(className);
 
 		}
 		return mktoClass;
 	}
 
 	public boolean methodExists(CtClass mktoClass, String methodName) {
 		CtMethod[] methods = mktoClass.getMethods();
 		for (CtMethod method : methods) {
 			if (method.getName().equals(methodName)) {
 				Logger.debug("Found method %s in class", methodName);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String addMethod(CtClass mktoClass, String methodDefinition) {
 		try {
 			// only call if method does NOT exist
 			String methodName = getMethodName();
 			String fullMethodDefn = "public LeadRecord " + methodName
 					+ "(LeadRecord leadRecord) {" + methodDefinition + "}";
 			Logger.debug("Adding new method to class:%s", fullMethodDefn);
 			mktoClass.addMethod(CtNewMethod.make(fullMethodDefn, mktoClass));
 			return methodName;
 		} catch (CannotCompileException e) {
 			Logger.error("Unable to compile : %s", e.getMessage());
 			return null;
 		}
 	}
 
 	public List<LeadRecord> executeMethod(Class clazz, String methodName,
 			List<LeadRecord> inflightList) {
 		List<LeadRecord> retList = new ArrayList<LeadRecord>();
 		LeadRecord retLead = null;
 		for (LeadRecord lr : inflightList) {
 			retLead = null;
 			retLead = executeMethod(clazz, methodName, lr);
 			if (retLead != null) {
 				retList.add(retLead);
 			}
 		}
 		return retList;
 	}
 
 	public LeadRecord executeMethod(Class clazz, String methodName,
 			LeadRecord leadRecord) {
 		try {
 			Object obj = clazz.newInstance();
 
 			Class[] formalParams = new Class[] { LeadRecord.class };
 			Method meth = clazz.getDeclaredMethod(methodName, formalParams);
 
 			Object[] actualParams = new Object[] { leadRecord };
 			LeadRecord result = ((LeadRecord) meth.invoke(obj, actualParams));
 			return result;
 
 		} catch (InstantiationException e) {
 			Logger.error("Unable to instantiate : %s", e.getMessage());
 		} catch (IllegalAccessException e) {
 			Logger.error("Illegal Access : %s", e.getMessage());
 		} catch (NoSuchMethodException e) {
 			Logger.error("No Such Method : %s", e.getMessage());
 		} catch (SecurityException e) {
 			Logger.error("Security Exception : %s", e.getMessage());
 		} catch (IllegalArgumentException e) {
 			Logger.error("Illegal Argument : %s", e.getMessage());
 		} catch (InvocationTargetException e) {
 			Logger.error("Invocation Target Exception : %s", e.getMessage());
 		}
 		return null;
 	}
 
 	public List<SyncStatus> syncMultipleLeads(
 			List<LeadRecord> processedLeadList, boolean dedupEnabled) {
 		try {
 			Logger.debug("About to sync %d leads", processedLeadList.size());
 			if (processedLeadList.size() == 0) {
 				return null;
 			}
 			List<SyncStatus> status = client.syncMultipleLeads(
 					processedLeadList, dedupEnabled);
 			Logger.debug("Finished sync-ing %d leads", processedLeadList.size());
 			return status;
 		} catch (MktowsClientException e) {
 			Logger.error("Marketo Client Exception : %s", e.getMessage());
 		} catch (MktServiceException e) {
 			Logger.error("Marketo Server Exception : %s", e.getMessage());
 		}
 		return null;
 	}
 
 	public String getMethodName() {
 		return ("eval" + this.munchkinAccountId + this.campaignId).replace("-",
 				"_");
 	}
 }
