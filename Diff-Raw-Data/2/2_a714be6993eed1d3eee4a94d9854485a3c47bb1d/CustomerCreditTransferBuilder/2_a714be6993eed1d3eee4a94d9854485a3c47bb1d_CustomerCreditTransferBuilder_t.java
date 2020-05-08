 package lt.itdbaltics.unifi.gateway.pain;
 
 import iso.std.iso._20022.tech.xsd.pain_001_001.AccountIdentification4Choice;
 import iso.std.iso._20022.tech.xsd.pain_001_001.ActiveOrHistoricCurrencyAndAmount;
 import iso.std.iso._20022.tech.xsd.pain_001_001.AmountType3Choice;
 import iso.std.iso._20022.tech.xsd.pain_001_001.BranchAndFinancialInstitutionIdentification4;
 import iso.std.iso._20022.tech.xsd.pain_001_001.CashAccount16;
 import iso.std.iso._20022.tech.xsd.pain_001_001.CreditTransferTransactionInformation10;
 import iso.std.iso._20022.tech.xsd.pain_001_001.CustomerCreditTransferInitiationV03;
 import iso.std.iso._20022.tech.xsd.pain_001_001.FinancialInstitutionIdentification7;
 import iso.std.iso._20022.tech.xsd.pain_001_001.GenericOrganisationIdentification1;
 import iso.std.iso._20022.tech.xsd.pain_001_001.GenericPersonIdentification1;
 import iso.std.iso._20022.tech.xsd.pain_001_001.GroupHeader32;
 import iso.std.iso._20022.tech.xsd.pain_001_001.OrganisationIdentification4;
 import iso.std.iso._20022.tech.xsd.pain_001_001.OrganisationIdentificationSchemeName1Choice;
 import iso.std.iso._20022.tech.xsd.pain_001_001.Party6Choice;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PartyIdentification32;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PaymentIdentification1;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PaymentInstructionInformation3;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PaymentMethod3Code;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PaymentTypeInformation19;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PersonIdentification5;
 import iso.std.iso._20022.tech.xsd.pain_001_001.PersonIdentificationSchemeName1Choice;
 import iso.std.iso._20022.tech.xsd.pain_001_001.Purpose2Choice;
 import iso.std.iso._20022.tech.xsd.pain_001_001.RemittanceInformation5;
 import iso.std.iso._20022.tech.xsd.pain_001_001.ServiceLevel8Choice;
 
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import lt.lba.xmlns._2011._11.unifi.customercredittransferinitiation.v03.SignableDocumentType;
 
 /**
  * 
  * Build <code>SignableDocument</code> JAXB type
  * out of the provided list of LITAS-ESIS v1.2 Mokesis payments.
  * 
  * XAdES Signature part of the <code>SignableDocument</code> is not supported. 
  * Only <code>CustomerCreditTransferInitiationV03</code> part (Document) is support currently.
  * 
  * Each payment in the <code>List</code> corresponds to the 
  * <code>PaymentInformation</code> item in the resulting JAXB graph.  
  * 
  * @author vgoldin
  *
  */
 public class CustomerCreditTransferBuilder {
 	private static final String SVCLVL_CODE_URGENT = "URGP";
 	private static final String SVCLVL_CODE_NONURGENT = "NURG";
 
 	private static final String PERSON_ID_CODE_CUST = "CUST";
 	private static final String ULTIMATE_PARTY_ID_CODE_IBAN = "IBAN";
 	private static final String ESIS_DATE_FORMAT = "yyyyMMdd";
 
 	public static SignableDocumentType convertToSignableDocumentType(List<LitasPaymentDto> rows) {
 		if (rows == null || rows.size() < 1) {
 			throw new IllegalArgumentException("List of LITAS-ESIS payments should be provided with at least one payment in the list."); 
 		}
 		
 		XMLGregorianCalendar createDate = createXMLGregorianCalendar(new Date());
 
 		GroupHeader32 header = new GroupHeader32();
 		header.setMsgId(UUID.randomUUID().toString().replaceAll("-", ""));
 		header.setCreDtTm(createDate);
 		header.setNbOfTxs(Integer.toString(rows.size()));
 
 		CustomerCreditTransferInitiationV03 credit = new CustomerCreditTransferInitiationV03();
 		credit.setGrpHdr(header);
 
 		Iterator<LitasPaymentDto> it = rows.iterator();
 
 		while (it.hasNext()) {
 			LitasPaymentDto row = it.next();
 
 			PaymentTypeInformation19 type = new PaymentTypeInformation19();
 			ServiceLevel8Choice svcLvl = new ServiceLevel8Choice();
 
 			int priority = Integer.valueOf(row.getPriority());
 			if (priority == 1) {
 				svcLvl.setCd(SVCLVL_CODE_NONURGENT);
 			} else if (priority == 2) {
 				svcLvl.setCd(SVCLVL_CODE_URGENT);
 			}
 			type.setSvcLvl(svcLvl);
 
 			// -- create new transfer payment instruction
 			PaymentInstructionInformation3 instr = new PaymentInstructionInformation3();
 			instr.setPmtInfId(UUID.randomUUID().toString().replaceAll("-", ""));
 			instr.setPmtMtd(PaymentMethod3Code.TRF);
 			instr.setPmtTpInf(type);
 
 			// -- set requested execution date
 			String reqdExctnDtStr = row.getValueDate();
 			if (hasValue(reqdExctnDtStr)) {
 				instr.setReqdExctnDt(getReqdExctnDt(reqdExctnDtStr));
 			}
 
 			// -- set debtor account
 			AccountIdentification4Choice dbtrAcctIdIBAN = new AccountIdentification4Choice();
 			dbtrAcctIdIBAN.setIBAN(row.getPayerAccount());
 
 			CashAccount16 dbtrAcct = new CashAccount16();
 			dbtrAcct.setId(dbtrAcctIdIBAN);
 			instr.setDbtrAcct(dbtrAcct);
 
 			// -- set debtor
 			PartyIdentification32 dbtr = createParty(row.getPayerName(), row.getPayerId(), row.getCustomerCodeInBeneficiaryIs(), PERSON_ID_CODE_CUST);
 			instr.setDbtr(dbtr);
 
 			// -- set debtor agent
 			String dbtrAgtFinInstnIdBICStr = row.getPayerCreditInstitution();
 			if (hasValue(dbtrAgtFinInstnIdBICStr)) {
 				instr.setDbtrAgt(getFinInstnId(dbtrAgtFinInstnIdBICStr));
 			}
 
 			// -- set ultimate debtor
 			PartyIdentification32 ultmtDbtr = createParty(row.getOriginatorName(), row.getOriginatorId(), row.getOriginatorAccount(), ULTIMATE_PARTY_ID_CODE_IBAN);
 			if (ultmtDbtr != null) {
 				instr.setUltmtDbtr(ultmtDbtr);
 			}
 
 			// -- set creditor account
 			AccountIdentification4Choice cdtrAcctIdIBAN = new AccountIdentification4Choice();
 			cdtrAcctIdIBAN.setIBAN(row.getBeneficiaryAccount());
 
 			CreditTransferTransactionInformation10 cdtTrfTxInf = new CreditTransferTransactionInformation10();
 
 			// -- set credit transfer number
 			String cdtTrfTxInfPmtIdStr = row.getDocumentNumber();
 			PaymentIdentification1 pmtId = new PaymentIdentification1();
 			pmtId.setInstrId(cdtTrfTxInfPmtIdStr);
 			pmtId.setEndToEndId(cdtTrfTxInfPmtIdStr);
 			cdtTrfTxInf.setPmtId(pmtId);
 
 			// -- set creditor
 			PartyIdentification32 cdtr = createParty(row.getBenificiaryName(), row.getBenificiaryId(), row.getCustomerCodeInPayerIs(), PERSON_ID_CODE_CUST);
 			cdtTrfTxInf.setCdtr(cdtr);
 
 			// -- set creditor agent
 			String cdtrAgtFinInstnIdBICStr = row.getBenificiaryCreditInsitution();
 			if (hasValue(cdtrAgtFinInstnIdBICStr)) {
 				cdtTrfTxInf.setCdtrAgt(getFinInstnId(cdtrAgtFinInstnIdBICStr));
 			}
 
 			// -- set ultimate creditor
			PartyIdentification32 ultmtCdtr = createParty(row.getBenificiaryPartyName(), row.getBenificiaryPartyId(),
 					row.getBeneficiaryPartyAccount(), ULTIMATE_PARTY_ID_CODE_IBAN);
 			if (ultmtCdtr != null) {
 				cdtTrfTxInf.setUltmtCdtr(ultmtCdtr);
 			}
 
 			// -- set amount
 			AmountType3Choice amt = createAmount(row.getAmount(), row.getCurrencyCode());
 			cdtTrfTxInf.setAmt(amt);
 
 			// -- set purpose code (purpose is not the same as remittance!
 			// Remittance is to identify particular instruction relation to the
 			// particular invoice, whenever purpose is a static value, defining
 			// the category of the payment)
 			String purpPrtryStr = row.getReferenceNo();
 			if (hasValue(purpPrtryStr)) {
 				Purpose2Choice purp = new Purpose2Choice();
 				purp.setPrtry(purpPrtryStr);
 
 				cdtTrfTxInf.setPurp(purp);
 			}
 
 			// -- set unstructured remittance information
 			RemittanceInformation5 rmtInf = createRmtInf(row.getPaymentDetails());
 			cdtTrfTxInf.setRmtInf(rmtInf);
 
 			// -- add credit transfer transaction information (credit part) to
 			// the instruction
 			instr.getCdtTrfTxInf().add(cdtTrfTxInf);
 
 			// -- add instruction to the list of instructions in the current
 			// document
 			credit.getPmtInf().add(instr);
 		}
 
 		iso.std.iso._20022.tech.xsd.pain_001_001.Document document = new iso.std.iso._20022.tech.xsd.pain_001_001.Document();
 		document.setCstmrCdtTrfInitn(credit);
 
 		SignableDocumentType signableType = new SignableDocumentType();
 		signableType.setDocument(document);
 		return signableType;
 	}
 
 	private static XMLGregorianCalendar createXMLGregorianCalendar(Date currentDateTime) {
 		GregorianCalendar calendar = new GregorianCalendar();
 		calendar.setTime(currentDateTime);
 
 		XMLGregorianCalendar createDate;
 		try {
 			createDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
 		} catch (DatatypeConfigurationException e) {
 			throw new RuntimeException(e);
 		}
 
 		return createDate;
 	}
 
 	private static RemittanceInformation5 createRmtInf(String string) {
 		RemittanceInformation5 rmtInf = new RemittanceInformation5();
 
 		// -- maximum allowed length is 300, splitting the string into chunks of
 		// 140 each
 		if (string.length() > 140) {
 			String s1 = string.substring(0, 140);
 			rmtInf.getUstrd().add(s1);
 
 			String s2 = string.substring(140);
 			rmtInf.getUstrd().add(s2.substring(0, 140));
 
 			if (s2.length() > 140) {
 				String s3 = s2.substring(140);
 				if (s3.length() > 140) {
 					s3 = s3.substring(0, 140);
 				}
 
 				rmtInf.getUstrd().add(s3);
 			}
 		} else {
 			rmtInf.getUstrd().add(string);
 		}
 
 		return rmtInf;
 	}
 
 	private static AmountType3Choice createAmount(String instdAmtStr, String instdAmtCcy) {
 		String d = instdAmtStr.substring(0, instdAmtStr.length() - 2) + "." + instdAmtStr.substring(instdAmtStr.length() - 2);
 
 		BigDecimal amount = BigDecimal.valueOf(Double.valueOf(d));
 
 		AmountType3Choice amt = new AmountType3Choice();
 		ActiveOrHistoricCurrencyAndAmount instdAmt = new ActiveOrHistoricCurrencyAndAmount();
 		instdAmt.setValue(amount);
 		instdAmt.setCcy(instdAmtCcy);
 
 		amt.setInstdAmt(instdAmt);
 
 		return amt;
 	}
 
 	private static PartyIdentification32 createParty(String partyNm, String partyMainId, String partyAdditionalId, String partyAdditionalIdCode) {
 		PartyIdentification32 party = null;
 
 		if (hasValue(partyNm) || hasValue(partyMainId) || hasValue(partyAdditionalId)) {
 			party = new PartyIdentification32();
 
 			if (hasValue(partyNm)) {
 				party.setNm(partyNm);
 			}
 
 			Party6Choice partyId = createPartyId(partyMainId, partyAdditionalId, partyAdditionalIdCode);
 			if (partyId != null) {
 				party.setId(partyId);
 			}
 		}
 
 		return party;
 	}
 
 	private static BranchAndFinancialInstitutionIdentification4 getFinInstnId(String finInstnIdBICStr) {
 		BranchAndFinancialInstitutionIdentification4 finInstn = new BranchAndFinancialInstitutionIdentification4();
 		FinancialInstitutionIdentification7 finInstnIdBIC = new FinancialInstitutionIdentification7();
 		finInstnIdBIC.setBIC(finInstnIdBICStr);
 		finInstn.setFinInstnId(finInstnIdBIC);
 
 		return finInstn;
 	}
 
 	private static Party6Choice createPartyId(String partyIdOtherIdStr, String partyIdOtherIdAdditionalStr, String additionalSchmeNmCodeCdStr) {
 
 		Party6Choice partyIdOtherId = null;
 
 		if (hasValue(partyIdOtherIdStr) || hasValue(partyIdOtherIdAdditionalStr)) {
 			partyIdOtherId = new Party6Choice();
 
 			if (hasValue(partyIdOtherIdStr) && partyIdOtherIdStr.length() == 11) {
 				PersonIdentification5 prvtId = new PersonIdentification5();
 				GenericPersonIdentification1 otherId = new GenericPersonIdentification1();
 				otherId.setId(partyIdOtherIdStr);
 
 				prvtId.getOthr().add(otherId);
 
 				if (hasValue(partyIdOtherIdAdditionalStr)) {
 					GenericPersonIdentification1 otherCustId = getPersonAdditionalId(partyIdOtherIdAdditionalStr, additionalSchmeNmCodeCdStr);
 
 					prvtId.getOthr().add(otherCustId);
 				}
 
 				partyIdOtherId.setPrvtId(prvtId);
 			} else {
 				OrganisationIdentification4 orgId = new OrganisationIdentification4();
 
 				if (hasValue(partyIdOtherIdStr)) {
 					GenericOrganisationIdentification1 otherId = new GenericOrganisationIdentification1();
 					otherId.setId(partyIdOtherIdStr);
 
 					orgId.getOthr().add(otherId);
 				}
 
 				if (hasValue(partyIdOtherIdAdditionalStr)) {
 					GenericOrganisationIdentification1 otherCustId = getOrganisationCustId(partyIdOtherIdAdditionalStr, additionalSchmeNmCodeCdStr);
 
 					orgId.getOthr().add(otherCustId);
 				}
 
 				partyIdOtherId.setOrgId(orgId);
 			}
 		}
 
 		return partyIdOtherId;
 	}
 
 	private static boolean hasValue(String str) {
 		return str != null && !str.isEmpty();
 	}
 
 	private static GenericOrganisationIdentification1 getOrganisationCustId(String partyIdOtherIdCustStr, String schmeNmCodeCdStr) {
 		GenericOrganisationIdentification1 otherCustId = new GenericOrganisationIdentification1();
 		otherCustId.setId(partyIdOtherIdCustStr);
 
 		OrganisationIdentificationSchemeName1Choice schmeNmCodeCd = new OrganisationIdentificationSchemeName1Choice();
 
 		if (!isProprietaryCode(schmeNmCodeCdStr)) {
 			schmeNmCodeCd.setCd(schmeNmCodeCdStr);
 		} else {
 			schmeNmCodeCd.setPrtry(schmeNmCodeCdStr);
 		}
 
 		otherCustId.setSchmeNm(schmeNmCodeCd);
 
 		return otherCustId;
 	}
 
 	private static GenericPersonIdentification1 getPersonAdditionalId(String otherIdAdditionalStr, String schmeNmCodeCdStr) {
 		GenericPersonIdentification1 otherAdditionalId = new GenericPersonIdentification1();
 		otherAdditionalId.setId(otherIdAdditionalStr);
 
 		PersonIdentificationSchemeName1Choice schmeNmCodeCd = new PersonIdentificationSchemeName1Choice();
 
 		if (!isProprietaryCode(schmeNmCodeCdStr)) {
 			schmeNmCodeCd.setCd(schmeNmCodeCdStr);
 		} else {
 			schmeNmCodeCd.setPrtry(schmeNmCodeCdStr);
 		}
 
 		otherAdditionalId.setSchmeNm(schmeNmCodeCd);
 
 		return otherAdditionalId;
 	}
 
 	private static boolean isProprietaryCode(String schmeNmCodeCdStr) {
 		// -- in the future we can create a list of used Codes from external
 		// list and check against the list instead
 		return !schmeNmCodeCdStr.equals(PERSON_ID_CODE_CUST);
 	}
 
 	private static XMLGregorianCalendar getReqdExctnDt(String reqdExctnDtStr) {
 		XMLGregorianCalendar reqdExctnDt = null;
 		try {
 			DateFormat formatter = new SimpleDateFormat(ESIS_DATE_FORMAT);
 			Date date = (Date) formatter.parse(reqdExctnDtStr);
 
 			GregorianCalendar calendar = new GregorianCalendar();
 			calendar.setTime(date);
 
 			reqdExctnDt = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		} catch (DatatypeConfigurationException e) {
 			throw new RuntimeException(e);
 		}
 
 		return reqdExctnDt;
 	}
 }
