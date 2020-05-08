 /*
 * $Id: GenomicSegmentManagerImpl.java,v 1.30 2007-07-31 12:02:29 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.29  2007/04/04 13:17:49  pandyas
  * modified names for mutation identifier fields (number changed to id)
  *
  * Revision 1.28  2007/03/27 18:37:40  pandyas
  * Modified code to trim identifiers - cleaner for display link
  *
  * Revision 1.27  2007/03/26 12:01:11  pandyas
  * caMOd 2.3 enhancements for Zebrafish support
  *
  * Revision 1.26  2006/08/17 18:26:52  pandyas
  * Defect# 410: Externalize properties files - Code Changes to send mail method
  *
  * Revision 1.25  2006/05/23 14:08:20  pandyas
  * uncommented the comment save code so it runs again
  *
  * Revision 1.24  2006/05/22 17:21:11  pandyas
  * Must set locationOfIntegration to null during editing from targeted back to random
  *
  * Revision 1.23  2006/05/22 16:52:47  pandyas
  * modified/fixed isRandom to make consistent between transgene and genomic segment
  *
  * Revision 1.22  2006/04/20 14:58:51  georgeda
  * Fixed targeted/random
  *
  * Revision 1.21  2006/04/17 19:11:06  pandyas
  * caMod 2.1 OM changes
  *
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.GenomicSegment;
 import gov.nih.nci.camod.domain.Image;
 import gov.nih.nci.camod.domain.MutationIdentifier;
 import gov.nih.nci.camod.domain.SegmentType;
 import gov.nih.nci.camod.service.GenomicSegmentManager;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.GenomicSegmentData;
 import gov.nih.nci.camod.webapp.form.ImageForm;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpServletRequest;
 
 public class GenomicSegmentManagerImpl extends BaseManager implements
 		GenomicSegmentManager {
 
 	public List getAll() throws Exception {
 		log.trace("In GenomicSegmentManagerImpl.getAll");
 		return super.getAll(GenomicSegment.class);
 	}
 
 	public GenomicSegment get(String id) throws Exception {
 		log.trace("In GenomicSegmentManagerImpl.get");
 		return (GenomicSegment) super.get(id, GenomicSegment.class);
 	}
 
 	public void save(GenomicSegment GenomicSegment) throws Exception {
 		log.trace("In GenomicSegmentManagerImpl.save");
 		super.save(GenomicSegment);
 	}
 
 	public void remove(String id, AnimalModel inAnimalModel) throws Exception {
 		log.trace("In GenomicSegmentManagerImpl.remove");
 		inAnimalModel.getEngineeredGeneCollection().remove(get(id));
 		super.save(inAnimalModel);
 	}
 
 	public GenomicSegment create(AnimalModel inAnimalModel,
 			GenomicSegmentData inGenomicSegmentData, HttpServletRequest request)
 			throws Exception {
 
 		log.trace("Entering GenomicSegmentManagerImpl.create");
 
 		GenomicSegment inGenomicSegment = new GenomicSegment();
 		populateGenomicSegment(inAnimalModel, inGenomicSegmentData,
 				inGenomicSegment, request);
 
 		log.trace("Exiting GenomicSegmentManagerImpl.create");
 
 		return inGenomicSegment;
 	}
 
 	public void update(AnimalModel inAnimalModel,
 			GenomicSegmentData inGenomicSegmentData,
 			GenomicSegment inGenomicSegment, HttpServletRequest request)
 			throws Exception {
 
 		log.trace("Entering GenomicSegmentManagerImpl.update");
 		log.debug("Updating GenomicSegmentForm: " + inGenomicSegment.getId());
 
 		// Populate w/ the new values and save
 		populateGenomicSegment(inAnimalModel, inGenomicSegmentData,
 				inGenomicSegment, request);
 		save(inGenomicSegment);
 
 		log.trace("Exiting GenomicSegmentManagerImpl.update");
 	}
 
 	private void populateGenomicSegment(AnimalModel inAnimalModel,
 			GenomicSegmentData inGenomicSegmentData,
 			GenomicSegment inGenomicSegment, HttpServletRequest request)
 			throws Exception {
 		log.info("Entering populateGenomicSegment");
 
 		if (inGenomicSegmentData.getIsRandom().equals("yes")) {
 			inGenomicSegment.setIsRandom(true);
 			// Set locationOfIntegration to null during editing
 			inGenomicSegment.setLocationOfIntegration(null);
 		} else {
 			inGenomicSegment.setIsRandom(false);
 			inGenomicSegment.setLocationOfIntegration(inGenomicSegmentData
 					.getLocationOfIntegration());
 		}
 		inGenomicSegment.setComments(inGenomicSegmentData.getComments());
 		inGenomicSegment.setSegmentSize(inGenomicSegmentData.getSegmentSize());
 		inGenomicSegment.setCloneDesignator(inGenomicSegmentData
 				.getCloneDesignator());
 
 		// SegmentType. Reuse if it's aready there, update otherwise.
 		SegmentType theSegmentType = null;
 		if (inGenomicSegment.getSegmentType() != null) {
 			theSegmentType = (SegmentType) inGenomicSegment.getSegmentType();
 		} else {
 			theSegmentType = new SegmentType();
 			inGenomicSegment.setSegmentType(theSegmentType);
 		}
 
 		theSegmentType.setName(inGenomicSegmentData.getSegmentName());
 
 		if (inGenomicSegmentData.getOtherSegmentName() != null) {
 			theSegmentType.setName(null);
 			theSegmentType.setNameUnctrlVocab(inGenomicSegmentData
 					.getOtherSegmentName());
 
 			log
 					.trace("Sending Notification eMail - new Administrative Route added");
 			sendEmail(inAnimalModel,
 					inGenomicSegmentData.getOtherSegmentName(), "SegmentName");
 		}
 
 		if (inGenomicSegment.getImage() != null) {
 			Image image = inGenomicSegment.getImage();
 			image.setTitle(inGenomicSegmentData.getTitle());
 			// image.setFileServerLocation( );
 			image.setDescription(inGenomicSegmentData
 					.getDescriptionOfConstruct());
 			inGenomicSegment.setImage(image);
 		}
 
 		// Upload Construct File location, Title of Construct, Description of
 		// Construct
 		// Check for exisiting Image for this GenomicSegment
 		if (inGenomicSegmentData.getFileLocation() != null)
 			if (inGenomicSegmentData.getFileLocation().getFileName() != null
 					&& !inGenomicSegmentData.getFileLocation().getFileName()
 							.equals("")) {
 
 				ImageForm inImageData = new ImageForm();
 
 				String inPath = request.getSession().getServletContext()
 						.getRealPath("/config/temp.jpg");
 
 				inImageData.setDescriptionOfConstruct(inGenomicSegmentData
 						.getDescriptionOfConstruct());
 				inImageData.setTitle(inGenomicSegmentData.getTitle());
 				inImageData.setUrl(inGenomicSegmentData
 						.getUrl());
 				inImageData.setFileLocation(inGenomicSegmentData
 						.getFileLocation());
 
 				Image image = ImageManagerSingleton.instance().create(
 						new AnimalModel(), inImageData, inPath,
 						Constants.CaImage.FTPGENCONSTORAGEDIRECTORY);
 
 				System.out.println("Image info: \ndescription:"
 						+ image.getDescription() + " \ntitle:"
 						+ image.getTitle() + " \nname:"
 						+ image.getUrl() + " \nid:"
 						+ image.getId());
 				inGenomicSegment.setImage(image);
 			}
 
 		// MGI Number
 		// Check for exisiting MutationIdentifier
 		MutationIdentifier inMutationIdentifier = null;
 		if (inGenomicSegment.getMutationIdentifier() != null){
 			inMutationIdentifier = inGenomicSegment.getMutationIdentifier();
 		} else {
 			inMutationIdentifier = new MutationIdentifier();
 		}
 
 		if (inGenomicSegmentData.getMgiId() != null) {
 
 			inMutationIdentifier.setMgiId(inGenomicSegmentData
 					.getMgiId().trim());
 			inGenomicSegment.setMutationIdentifier(inMutationIdentifier);
 		}
 		if (inGenomicSegmentData.getZfinId() != null) {
 
 			inMutationIdentifier.setZfinId(inGenomicSegmentData
 					.getZfinId().trim());
 			inGenomicSegment.setMutationIdentifier(inMutationIdentifier);
 		}
 		if (inGenomicSegmentData.getRgdId() != null) {
 
 			inMutationIdentifier.setRgdId(inGenomicSegmentData
 					.getRgdId().trim());
 			inGenomicSegment.setMutationIdentifier(inMutationIdentifier);
 		}		
 		
 
 		log.trace("Exiting populateGenomicSegment");
 	}
 
 	private void sendEmail(AnimalModel inAnimalModel,
 			String theUncontrolledVocab, String inType) {
 
 		// Get the e-mail resource
 		Properties camodProperties = new Properties();
 		String camodPropertiesFileName = null;
 
 		camodPropertiesFileName = System
 				.getProperty("gov.nih.nci.camod.camodProperties");
 
 		try {
 			FileInputStream in = new FileInputStream(camodPropertiesFileName);
 			camodProperties.load(in);
 
 		} catch (FileNotFoundException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();
 		}
 
 		// String recipients =
 		// theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_NOTIFY_KEY);
 		String recipients = UserManagerSingleton.instance()
 				.getEmailForCoordinator();
 
 		StringTokenizer st = new StringTokenizer(recipients, ",");
 		String inRecipients[] = new String[st.countTokens()];
 		for (int i = 0; i < inRecipients.length; i++) {
 			inRecipients[i] = st.nextToken();
 		}
 
 		// String inSubject =
 		// theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_SUBJECT_KEY);
 		String inSubject = camodProperties
 				.getProperty("model.new_unctrl_vocab_subject");
 		String inFrom = inAnimalModel.getSubmitter().getEmailAddress();
 
 		// gather message keys and variable values to build the e-mail
 		String[] messageKeys = { Constants.Admin.NONCONTROLLED_VOCABULARY };
 		Map<String, Object> values = new TreeMap<String, Object>();
 		values.put("type", inType);
 		values.put("value", theUncontrolledVocab);
 		values.put("submitter", inAnimalModel.getSubmitter());
 		values.put("model", inAnimalModel.getModelDescriptor());
 		values.put("modelstate", inAnimalModel.getState());
 
 		// Send the email
 		try {
 			MailUtil.sendMail(inRecipients, inSubject, "", inFrom, messageKeys,
 					values);
 		} catch (MessagingException e) {
 			log.error("Caught exception sending mail: ", e);
 			e.printStackTrace();
 		}
 
 	}
 }
