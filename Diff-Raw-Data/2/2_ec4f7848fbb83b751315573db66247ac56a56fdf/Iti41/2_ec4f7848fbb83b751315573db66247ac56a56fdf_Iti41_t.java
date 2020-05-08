 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.rsna.isn.transfercontent.ihe;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.dcm4che2.data.UID;
 import org.dcm4che2.util.UIDUtils;
 import org.openhealthtools.ihe.atna.auditor.XDSAuditor;
 import org.openhealthtools.ihe.common.hl7v2.CX;
 import org.openhealthtools.ihe.common.hl7v2.Hl7v2Factory;
 import org.openhealthtools.ihe.common.hl7v2.SourcePatientInfoType;
 import org.openhealthtools.ihe.common.hl7v2.XCN;
 import org.openhealthtools.ihe.common.hl7v2.XPN;
 import org.openhealthtools.ihe.utils.IHEException;
 import org.openhealthtools.ihe.xds.document.DocumentDescriptor;
 import org.openhealthtools.ihe.xds.document.XDSDocument;
 import org.openhealthtools.ihe.xds.document.XDSDocumentFromByteArray;
 import org.openhealthtools.ihe.xds.metadata.AuthorType;
 import org.openhealthtools.ihe.xds.metadata.CodedMetadataType;
 import org.openhealthtools.ihe.xds.metadata.DocumentEntryType;
 import org.openhealthtools.ihe.xds.metadata.InternationalStringType;
 import org.openhealthtools.ihe.xds.metadata.LocalizedStringType;
 import org.openhealthtools.ihe.xds.metadata.MetadataFactory;
 import org.openhealthtools.ihe.xds.metadata.SubmissionSetType;
 import org.openhealthtools.ihe.xds.response.XDSResponseType;
 import org.openhealthtools.ihe.xds.response.XDSStatusType;
 import org.openhealthtools.ihe.xds.source.B_Source;
 import org.openhealthtools.ihe.xds.source.SubmitTransactionData;
 import org.rsna.isn.dao.ConfigurationDao;
 import org.rsna.isn.domain.Author;
 import org.rsna.isn.domain.DicomKos;
 import org.rsna.isn.domain.DicomObject;
 import org.rsna.isn.domain.DicomSeries;
 import org.rsna.isn.domain.DicomStudy;
 import org.rsna.isn.domain.Exam;
 import org.rsna.isn.domain.RsnaDemographics;
 import org.rsna.isn.util.Constants;
 import org.rsna.isn.util.Environment;
 
 /**
  *
  * @author wtellis
  */
 public class Iti41
 {
 	private static final Logger logger = Logger.getLogger(Iti41.class);
 
 	private static final String sourceId;
 
 	private static final String repositoryUniqueId;
 
 	private static final MetadataFactory xdsFactory = MetadataFactory.eINSTANCE;
 
 	private static final Hl7v2Factory hl7Factory = Hl7v2Factory.eINSTANCE;
 
 	private static final String DICOM_UID_REG_UID = "1.2.840.10008.2.6.1";
 
 	private static final DocumentDescriptor KOS_DESCRIPTOR =
 			new DocumentDescriptor("KOS", "application/dicom-kos");
 
 	private static final DocumentDescriptor TEXT_DESCRIPTOR =
 			new DocumentDescriptor("TEXT", "text/plain");
 
 	private static final URI endpoint;
 
 	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 
 	private final DicomStudy study;
 
 	private final Exam exam;
 
 	private final RsnaDemographics rsna;
 
 	static
 	{
 		try
 		{
 			ConfigurationDao dao = new ConfigurationDao();
 
 			repositoryUniqueId = dao.getConfiguration("iti41-repository-unique-id");
 			if (StringUtils.isBlank(repositoryUniqueId))
 				throw new ExceptionInInitializerError("iti41-repository-unique-id is blank");
 
 			sourceId = dao.getConfiguration("iti41-source-id");
 			if (StringUtils.isBlank(sourceId))
 				throw new ExceptionInInitializerError("iti41-source-id is blank");
 
 			String url = dao.getConfiguration("iti41-endpoint-url");
 			if (StringUtils.isBlank(url))
 				throw new ExceptionInInitializerError("iti41-endpoint-url");
 			endpoint = new URI(url);
 
 
 			XDSAuditor.getAuditor().getConfig().setAuditorEnabled(false);
 
 			//
 			// Load Axis 2 configuration (there has got be a better way)
 			//
 			Class cls = Iti41.class;
 			String pkg = cls.getPackage().getName().replace('.', '/');
 			String path = "/" + pkg + "/axis2.xml";
 			InputStream in = cls.getResourceAsStream(path);
 
 
 			File tmpDir = Environment.getTmpDir();
 			File axis2Xml = new File(tmpDir, "axis2.xml");
 			FileOutputStream out = new FileOutputStream(axis2Xml);
 			IOUtils.copy(in, out);
 			in.close();
 			out.close();
 
 			System.setProperty("axis2.xml", axis2Xml.getCanonicalPath());
 		}
 		catch (Exception ex)
 		{
 			throw new ExceptionInInitializerError(ex);
 		}
 	}
 
 	public Iti41(DicomStudy study)
 	{
 		this.study = study;
 
 		exam = study.getExam();
 
 		rsna = exam.getRsnaDemographics();
 		if (rsna == null)
 			throw new IllegalArgumentException("No RSNA ID associated with patient: " + exam.getMrn());
 	}
 
 	public void submitDocuments(File debugFile) throws Exception
 	{
 		SubmitTransactionData tx = new SubmitTransactionData();
 
 
 
 		//
 		// Add entry for report
 		//
 
 		String report = exam.getReport();
 		if (report != null)
 		{
 			XDSDocument reportDoc =
 					new XDSDocumentFromByteArray(TEXT_DESCRIPTOR, report.getBytes("UTF-8"));
 
 			String reportUuid = tx.addDocument(reportDoc);
 			DocumentEntryType reportEntry = tx.getDocumentEntry(reportUuid);
 			initDocEntry(reportEntry);
 
 
 			CodedMetadataType reportFmt = xdsFactory.createCodedMetadataType();
 			reportFmt.setCode("TEXT");
 			reportFmt.setSchemeName("RSNA-ISN");
 			reportEntry.setFormatCode(reportFmt);
 
 			CodedMetadataType reportEventCode = xdsFactory.createCodedMetadataType();
 			reportEventCode.setCode("REPORT");
 			reportEntry.getEventCode().add(reportEventCode);
 
 			reportEntry.setMimeType(TEXT_DESCRIPTOR.getMimeType());
 
 			reportEntry.setUniqueId(UIDUtils.createUID());
 		}
 
 
 
 
 
 
 		//
 		// Add entry for KOS
 		//
 
 		DicomKos kos = study.getKos();
 		File kosFile = kos.getFile();
 		XDSDocument kosDoc = new LazyLoadedXdsDocument(KOS_DESCRIPTOR, kosFile);
 		String kosUuid = tx.addDocument(kosDoc);
 		DocumentEntryType kosEntry = tx.getDocumentEntry(kosUuid);
 		initDocEntry(kosEntry);
 
 		CodedMetadataType kosFmt = xdsFactory.createCodedMetadataType();
 		kosFmt.setCode(UID.KeyObjectSelectionDocumentStorage);
 		kosFmt.setSchemeName("DCM");
 		kosFmt.setSchemeUUID(DICOM_UID_REG_UID);
 		kosEntry.setFormatCode(kosFmt);
 
 		CodedMetadataType kosEventCode = xdsFactory.createCodedMetadataType();
 		kosEventCode.setCode("KO");
 		kosEventCode.setSchemeName("DCM");
 		kosEventCode.setSchemeUUID(DICOM_UID_REG_UID);
 		kosEntry.getEventCode().add(kosEventCode);
 
 		kosEntry.setMimeType(KOS_DESCRIPTOR.getMimeType());
 
 		kosEntry.setUniqueId(kos.getSopInstanceUid());
 
 
 
 
 		//
 		// Add entries for images
 		//
 
 		for (DicomSeries series : study.getSeries().values())
 		{
 			for (DicomObject object : series.getObjects().values())
 			{
 				File dcmFile = object.getFile();
 
 				XDSDocument dcmDoc = new LazyLoadedXdsDocument(DocumentDescriptor.DICOM, dcmFile);
 				String dcmUuid = tx.addDocument(dcmDoc);
 				DocumentEntryType dcmEntry = tx.getDocumentEntry(dcmUuid);
 				initDocEntry(dcmEntry);
 
 				CodedMetadataType dcmFmt = xdsFactory.createCodedMetadataType();
 				dcmFmt.setCode(object.getSopClassUid());
 				dcmFmt.setSchemeName("DCM");
 				dcmFmt.setSchemeUUID(DICOM_UID_REG_UID);
 				dcmEntry.setFormatCode(dcmFmt);
 
 				CodedMetadataType dcmEventCode = xdsFactory.createCodedMetadataType();
 				dcmEventCode.setCode(series.getModality());
 				dcmEventCode.setSchemeName("DCM");
 				dcmEventCode.setSchemeUUID(DICOM_UID_REG_UID);
 				dcmEntry.getEventCode().add(dcmEventCode);
 
 				dcmEntry.setMimeType(DocumentDescriptor.DICOM.getMimeType());
 
 				dcmEntry.setUniqueId(object.getSopInstanceUid());
 			}
 		}
 
 
 
 
 		//
 		// Initialize submission set metadata
 		//
 		SubmissionSetType subSet = tx.getSubmissionSet();
 		AuthorType author = getAuthor();
 		if (author != null)
 			subSet.setAuthor(author);
 
 		CodedMetadataType contentType = xdsFactory.createCodedMetadataType();
 		contentType.setCode("Imaging Exam");
 		contentType.setDisplayName(inStr("Imaging Exam"));
 		contentType.setSchemeName("RSNA-ISN");
 		subSet.setContentTypeCode(contentType);
 
 		subSet.setPatientId(getRsnaId());
 		subSet.setSourceId(sourceId);
 		subSet.setSubmissionTime(getGmt(new Date()));
 		subSet.setTitle(inStr(study.getStudyDescription()));
 		subSet.setUniqueId(UIDUtils.createUID());
 
 		if(debugFile != null)
 		  tx.saveMetadataToFile(debugFile.getCanonicalPath());
 
 
 		B_Source reg = new B_Source(endpoint);
 		XDSResponseType resp = reg.submit(tx);
 
 		XDSStatusType status = resp.getStatus();
 		int code = status.getValue();
 
 		if (code != XDSStatusType.SUCCESS)
 		{
 			throw new IHEException("Unable to submit documents for study "
 					+ study.getStudyUid()
					+ ". Remote site returned error: " + status.getLiteral());
 
 		}
 	}
 
 	private AuthorType getAuthor()
 	{
 		XCN legalAuthenticator = getLegalAuthenticator();
 		if (legalAuthenticator != null)
 		{
 			AuthorType author = xdsFactory.createAuthorType();
 
 			author.setAuthorPerson(legalAuthenticator);
 
 
 			return author;
 		}
 		else
 		{
 			return null;
 		}
 
 	}
 
 	private XCN getLegalAuthenticator()
 	{
 		Author signer = exam.getSigner();
 		if (signer != null)
 		{
 			XCN legalAuthenticator = hl7Factory.createXCN();
 
 			legalAuthenticator.setFamilyName(signer.getLastName());
 			legalAuthenticator.setGivenName(signer.getFirstName());
 			legalAuthenticator.setIdNumber(signer.getId());
 
 			return legalAuthenticator;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	private CX getRsnaId()
 	{
 		CX rsnaId = hl7Factory.createCX();
 		rsnaId.setIdNumber(rsna.getId());
 		rsnaId.setAssigningAuthorityName(Constants.NAMESPACE_ID);
 		rsnaId.setAssigningAuthorityUniversalId(Constants.UNIVERSAL_ID);
 		rsnaId.setAssigningAuthorityUniversalIdType(Constants.UNIVERSAL_ID_TYPE);
 
 		return rsnaId;
 	}
 
 	private SourcePatientInfoType getSrcPatInfo()
 	{
 		XPN rsnaPatName = hl7Factory.createXPN();
 		rsnaPatName.setFamilyName(rsna.getLastName());
 		rsnaPatName.setGivenName(rsna.getFirstName());
 
 		SourcePatientInfoType srcPatInfo = hl7Factory.createSourcePatientInfoType();
 		srcPatInfo.getPatientIdentifier().add(getRsnaId());
 		srcPatInfo.getPatientName().add(rsnaPatName);
 
 		return srcPatInfo;
 	}
 
 	private CodedMetadataType getClassCode()
 	{
 		CodedMetadataType classCode = xdsFactory.createCodedMetadataType();
 		classCode.setCode("Image Exam Result");
 		classCode.setDisplayName(inStr("Image Exam Result"));
 		classCode.setSchemeName("RSNA-ISN");
 
 		return classCode;
 	}
 
 	private CodedMetadataType getConfidentialityCode()
 	{
 		CodedMetadataType confidentialityCode = xdsFactory.createCodedMetadataType();
 		confidentialityCode.setCode("GRANT");
 		confidentialityCode.setSchemeName("RSNA-ISN");
 
 		return confidentialityCode;
 	}
 
 	private String getGmt(Date date)
 	{
 		return sdf.format(date);
 	}
 
 	private CodedMetadataType getHealthcareFacilityTypeCode()
 	{
 		CodedMetadataType healthcareFacilityTypeCode = xdsFactory.createCodedMetadataType();
 		healthcareFacilityTypeCode.setCode("GEN");
 		healthcareFacilityTypeCode.setDisplayName(inStr("General Hospital"));
 		healthcareFacilityTypeCode.setSchemeName("RSNA-ISN");
 
 		return healthcareFacilityTypeCode;
 	}
 
 	private CodedMetadataType getPracticeSettingCode()
 	{
 		CodedMetadataType practiceSettingCode = xdsFactory.createCodedMetadataType();
 		practiceSettingCode.setCode("Radiology");
 		practiceSettingCode.setDisplayName(inStr("Radiology"));
 		practiceSettingCode.setSchemeName("RSNA-ISN");
 
 		return practiceSettingCode;
 	}
 
 	private CodedMetadataType getTypeCode()
 	{
 		CodedMetadataType typeCode = xdsFactory.createCodedMetadataType();
 		typeCode.setCode(study.getStudyDescription());
 		typeCode.setDisplayName(inStr(study.getStudyDescription()));
 		typeCode.setSchemeName("RSNA-ISN");
 
 		return typeCode;
 	}
 
 	private void initDocEntry(DocumentEntryType docEntry)
 	{
 		AuthorType author = getAuthor();
 		if (author != null)
 			docEntry.setAuthor(author);
 
 		docEntry.setClassCode(getClassCode());
 		docEntry.getConfidentialityCode().add(getConfidentialityCode());
 		docEntry.setCreationTime(getGmt(new Date()));
 		docEntry.setHealthCareFacilityTypeCode(getHealthcareFacilityTypeCode());
 		docEntry.setLanguageCode("en-US");
 
 		XCN legalAuthenticator = getLegalAuthenticator();
 		if (legalAuthenticator != null)
 			docEntry.setLegalAuthenticator(legalAuthenticator);
 
 		docEntry.setPatientId(getRsnaId());
 		docEntry.setPracticeSettingCode(getPracticeSettingCode());
 		docEntry.setRepositoryUniqueId(repositoryUniqueId);
 		docEntry.setServiceStartTime(getGmt(study.getStudyDateTime()));
 		docEntry.setSourcePatientId(getRsnaId());
 		docEntry.setSourcePatientInfo(getSrcPatInfo());
 		docEntry.setTitle(inStr(study.getStudyDescription()));
 		docEntry.setTypeCode(getTypeCode());
 	}
 
 	private static InternationalStringType inStr(String value)
 	{
 		LocalizedStringType lzStr = xdsFactory.createLocalizedStringType();
 		lzStr.setCharset("UTF-8");
 		lzStr.setLang("en-US");
 		lzStr.setValue(value);
 
 		InternationalStringType inStr = xdsFactory.createInternationalStringType();
 		inStr.getLocalizedString().add(lzStr);
 
 		return inStr;
 	}
 
 }
