 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class provides general utility methods used in the UI implementation
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.utils;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.ISourceReference;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.mylyn.reviews.notifications.core.IMeetingData;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDelta;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EMeetingData;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ETextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ResourceUpdater;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileRevisionEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileRevisionTypedElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileTypedElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIComment;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedAnomaly;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUITextPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class MailServicesProxy {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field LINE_FEED_MSG_PART.
 	 */
 	private static final String LINE_FEED_MSG_PART = System.getProperty("line.separator");
 
 	/**
 	 * Field TAB_MSG_PART. (value is ""\t"")
 	 */
 	private static final String TAB_MSG_PART = "\t";
 
 	/**
 	 * Field SUBJECT_MSG_HEADER. (value is "" Review "")
 	 */
 	private static final String SUBJECT_MSG_HEADER = " Review ";
 
 	/**
 	 * Field INTRO_MSG_BODY. (value is ""Hi,"")
 	 */
 	private static final String INTRO_MSG_BODY = "Hi,";
 
 	/**
 	 * Field OUTRO_MSG_BODY. (value is ""Best Regards,"")
 	 */
 	private static final String OUTRO_MSG_BODY = "Best Regards,";
 
 	/**
 	 * Field ITEMS_READY_MSG_BODY. (value is ""The following Review Item(s) and Files are Ready for you to Review"")
 	 */
 	private static final String ITEMS_READY_MSG_BODY = "The following Review Item(s) and Files are Ready for you to Review";
 
 	/**
 	 * Field MEETING_REQUEST_MSG_BODY. (value is ""This invitation is for the decision phase." + LINE_FEED_MSG_PART +
 	 * "Please review the included items prior to the meeting."")
 	 */
 	private static final String MEETING_REQUEST_MSG_BODY = "This invitation is for the decision phase."
 			+ LINE_FEED_MSG_PART + "Please review the included items prior to the meeting.";
 
 	/**
 	 * Field ADDED_ITEMS_MSG_BODY. (value is ""The following Review Item(s) and Files have been Added." +
 	 * LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open"")
 	 */
 	private static final String ADDED_ELEMENTS_MSG_BODY = "The following Review Element(s) have been Added."
 			+ LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open";
 
 	/**
 	 * Field REMOVED_ITEMS_MSG_BODY. (value is ""The following Review Item(s) and Files have been Removed." +
 	 * LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open"")
 	 */
 	private static final String REMOVED_ELEMENTS_MSG_BODY = "The following Element(s) have been Removed."
 			+ LINE_FEED_MSG_PART + "Please Refresh your Review if it is currently Open";
 
 	/**
 	 * Field PROGRESS_MESSAGE. (value is ""Progress Update: " + LINE_FEED_MSG_PART")
 	 */
 	private static final String PROGRESS_MESSAGE = "Progress Update: " + LINE_FEED_MSG_PART;
 
 	/**
 	 * Field COMPLETION_MESSAGE. (value is ""I have Completed this Review, see Details below: " + LINE_FEED_MSG_PART")
 	 */
 	private static final String COMPLETION_MESSAGE = "I have Completed this Review, see Details below: "
 			+ LINE_FEED_MSG_PART;
 
 	/**
 	 * Field QUESTION_MSG_BODY. (value is ""I have a Question concerning the Following "")
 	 */
 	private static final String QUESTION_MSG_BODY = "I have a Question concerning the Following Elements: "
 			+ LINE_FEED_MSG_PART + LINE_FEED_MSG_PART;
 
 	/**
 	 * Field DEFAULT_MEETING_DURATION. (value is "60")
 	 */
 	private static final Integer DEFAULT_MEETING_DURATION = new Integer(60);
 
 	/**
 	 * Field DEFAULT_MEETING_LOCATION. (value is """")
 	 */
 	private static final String DEFAULT_MEETING_LOCATION = "";
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	//Notifications
 
 	/**
 	 * Method sendItemsReadyNotification
 	 * 
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendItemsReadyNotification() throws CoreException, ResourceHandlingException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			final String messageSubject = createSubject() + " - Items Ready for Review";
 			final String messageBody = createItemsReadyNotificationMessage(false);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendItemsAddedNotification
 	 * 
 	 * @param aAddedElements
 	 *            List<R4EReviewComponent>
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendItemsAddedNotification(List<R4EReviewComponent> aAddedElements) throws CoreException,
 			ResourceHandlingException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			final String messageSubject = createSubject() + " - Items Added for Review";
 			final String messageBody = createUpdatedItemsNotificationMessage(aAddedElements, true);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendItemsRemovedNotification
 	 * 
 	 * @param aRemovedElements
 	 *            List<R4EReviewComponent>
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendItemsRemovedNotification(List<R4EReviewComponent> aRemovedElements) throws CoreException,
 			ResourceHandlingException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 			final String[] messageDestinations = createItemsUpdatedDestinations();
 			final String messageSubject = createSubject() + " - Items Removed from Review";
 			final String messageBody = createUpdatedItemsNotificationMessage(aRemovedElements, false);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendProgressNotification
 	 * 
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendProgressNotification() throws CoreException, ResourceHandlingException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 			final String[] messageDestinations = createProgressDestinations();
 			final String messageSubject = createSubject() + " - Participant Progress";
 			final String messageBody = createProgressNotification(PROGRESS_MESSAGE);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendCompletionNotification
 	 * 
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendCompletionNotification() throws CoreException, ResourceHandlingException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 			final String[] messageDestinations = createProgressDestinations();
 			final String messageSubject = createSubject() + " - Participant Progress (Completed)";
 			final String messageBody = createProgressNotification(COMPLETION_MESSAGE);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendQuestion
 	 * 
 	 * @param aSource
 	 *            Object
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendQuestion(Object aSource) throws CoreException, ResourceHandlingException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 			String[] messageDestinations = null;
 			messageDestinations = createQuestionDestinations();
 			final String messageSubject = createSubject() + " - Question regarding review ";
 			final String messageBody = createQuestionMessage(aSource);
 			sendMessage(messageDestinations, messageSubject, messageBody);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method sendMessage
 	 * 
 	 * @param aDestinations
 	 *            String[]
 	 * @param aSubject
 	 *            String
 	 * @param aBody
 	 *            String
 	 * @throws CoreException
 	 * @throws ResourceHandlingException
 	 */
 	public static void sendMessage(String[] aDestinations, String aSubject, String aBody) throws CoreException,
 			ResourceHandlingException {
 		final R4EParticipant user = R4EUIModelController.getActiveReview().getParticipant(
 				R4EUIModelController.getReviewer(), false);
 		String originatorEmail = null;
 		if (null != user) {
 			originatorEmail = user.getEmail();
 		} else {
 			//If current user is not part of the review, get email from preferences
 			final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 			originatorEmail = store.getString(PreferenceConstants.P_USER_EMAIL);
 		}
 		R4EUIDialogFactory.getInstance()
 				.getMailConnector()
 				.sendEmailGraphical(originatorEmail, aDestinations, aSubject, aBody, null, null);
 	}
 
 	/**
 	 * Method createSubject
 	 * 
 	 * @return String
 	 */
 	private static String createSubject() {
 		final StringBuilder subject = new StringBuilder();
 		subject.append("[r4e-mail] ");
 		subject.append(SUBJECT_MSG_HEADER);
 		subject.append(R4EUIModelController.getActiveReview().getName());
 		return subject.toString();
 	}
 
 	/**
 	 * Method isEmailValid
 	 * 
 	 * @param aParticipant
 	 *            R4EParticipant
 	 * @return boolean
 	 */
 	private static boolean isEmailValid(R4EParticipant aParticipant) {
 		if (aParticipant.isEnabled() && !R4EUIModelController.getReviewer().equals(aParticipant.getId())) {
 			String emailStr = aParticipant.getEmail();
 			if (null != emailStr && !("".equals(emailStr))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method createItemsUpdatedDestinations
 	 * 
 	 * @return String[]
 	 */
 	private static String[] createItemsUpdatedDestinations() {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		final List<R4EParticipant> participants = R4EUIModelController.getActiveReview().getParticipants();
 		for (R4EParticipant participant : participants) {
			if (participant.isEnabled() && isEmailValid(participant)) {
 				//All active participants should receive this email
 				destinations.add(participant.getEmail());
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createProgressDestinations
 	 * 
 	 * @return String[]
 	 */
 	private static String[] createProgressDestinations() {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		final List<R4EParticipant> participants = R4EUIModelController.getActiveReview().getParticipants();
 		for (R4EParticipant participant : participants) {
			if (participant.isEnabled() && isEmailValid(participant)) {
 				if (!(R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL))) {
 					destinations.add(participant.getEmail());
 				} else {
 					//If this is a formal review, only send mail if we have the proper role
 					if ((participant.getRoles().contains(R4EUserRole.R4E_ROLE_LEAD)
 							|| participant.getRoles().contains(R4EUserRole.R4E_ROLE_ORGANIZER) || participant.getRoles()
 							.contains(R4EUserRole.R4E_ROLE_AUTHOR))) {
 						destinations.add(participant.getEmail());
 					}
 				}
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createQuestionDestinations
 	 * 
 	 * @return String[]
 	 */
 	private static String[] createQuestionDestinations() {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		final List<R4EParticipant> participants = R4EUIModelController.getActiveReview().getParticipants();
 		for (R4EParticipant participant : participants) {
			if (participant.isEnabled() && isEmailValid(participant)) {
 				if (!(R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL))) {
 					destinations.add(participant.getEmail());
 				} else {
 					//If this is a formal review, only send mail if we have the proper role
 					if ((participant.getRoles().contains(R4EUserRole.R4E_ROLE_LEAD)
 							|| participant.getRoles().contains(R4EUserRole.R4E_ROLE_ORGANIZER) || participant.getRoles()
 							.contains(R4EUserRole.R4E_ROLE_AUTHOR))) {
 						destinations.add(participant.getEmail());
 					}
 				}
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 
 	/**
 	 * Method createAnomalyCreatorDestination
 	 * 
 	 * @param aAnomaly
 	 *            R4EUIAnomalyBasic
 	 * @return String
 	 */
 	/*  TODO not used for now, could be added later to narrow down destinations for anomaly questions
 	private static String[] createAnomalyCreatorDestination(R4EUIAnomalyBasic aAnomaly) {
 		final ArrayList<String> destinations = new ArrayList<String>();
 		if (!R4EUIModelController.getReviewer().equals(aAnomaly.getAnomaly().getUser().getId())) {
 			R4EParticipant participant = (R4EParticipant) aAnomaly.getAnomaly().getUser();
			if (participant.isEnabled() && isEmailValid(participant)) {
 				destinations.add(participant.getEmail());
 			}
 		}
 		return destinations.toArray(new String[destinations.size()]);
 	}
 	 */
 
 	/**
 	 * Method createItemsReadyNotificationMessage
 	 * 
 	 * @param aMeetingRequestIncluded
 	 *            boolean
 	 * @return String
 	 */
 	private static String createItemsReadyNotificationMessage(boolean aMeetingRequestIncluded) {
 		final StringBuilder msgBody = new StringBuilder();
 
 		msgBody.append(createIntroPart());
 		if (aMeetingRequestIncluded) {
 			msgBody.append(MEETING_REQUEST_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		} else {
 			msgBody.append(ITEMS_READY_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		}
 		final List<R4EUIReviewItem> items = R4EUIModelController.getActiveReview().getReviewItems();
 		for (R4EUIReviewItem item : items) {
 			if (item.isEnabled()) {
 				if (null != item.getItem().getDescription()) {
 					msgBody.append("Review Item -> " + item.getItem().getDescription() + LINE_FEED_MSG_PART);
 				} else {
 					//Always use the name of the file for Resource review items
 					msgBody.append("Review Item -> " + item.getChildren()[0].getName() + " (Resource)"
 							+ LINE_FEED_MSG_PART);
 				}
 				msgBody.append("Eclipse Project: File Path (Repository | Project)[: Line range]" + LINE_FEED_MSG_PART);
 				R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 				for (R4EUIFileContext context : contexts) {
 					R4EFileVersion fileVersion = context.getTargetFileVersion();
 					if (context.isEnabled() && null != fileVersion) {
 						IResource resource = fileVersion.getResource();
 						msgBody.append(TAB_MSG_PART);
 						//Project
 						if (null != resource) {
 							msgBody.append(resource.getProject());
 						}
 						//Path
 						String path = fileVersion.getRepositoryPath();
 						if (null != path && !(path.equals(""))) {
 							msgBody.append(": " + path + " (Repository)");
 						} else if (null != resource) {
 							msgBody.append(": " + resource.getProjectRelativePath() + " (Project)");
 						}
 						//Line Range
 						if (null != context.getContentsContainerElement()) {
 							R4EUIContent[] contents = (R4EUIContent[]) context.getContentsContainerElement()
 									.getChildren();
 							msgBody.append(": ");
 							for (R4EUIContent content : contents) {
 								msgBody.append(content.getPosition().toString() + ", ");
 							}
 						}
 						msgBody.append(LINE_FEED_MSG_PART);
 					}
 				}
 				msgBody.append(LINE_FEED_MSG_PART);
 			}
 		}
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createRemovedItemsNotificationMessage
 	 * 
 	 * @param aElements
 	 *            List<R4EReviewComponent>
 	 * @param aIsAdded
 	 *            boolean
 	 * @return String
 	 */
 	private static String createUpdatedItemsNotificationMessage(List<R4EReviewComponent> aElements, boolean aIsAdded) {
 		final StringBuilder msgBody = new StringBuilder();
 
 		msgBody.append(createIntroPart());
 		if (aIsAdded) {
 			msgBody.append(ADDED_ELEMENTS_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		} else {
 			msgBody.append(REMOVED_ELEMENTS_MSG_BODY + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 		}
 		boolean legendAppended = false;
 		for (R4EReviewComponent component : aElements) {
 			if (component instanceof R4EItem) {
 				if (null != ((R4EItem) component).getDescription()) {
 					msgBody.append("Review Item -> " + ((R4EItem) component).getDescription() + LINE_FEED_MSG_PART);
 				}
 				msgBody.append("Eclipse Project: File Path (Repository | Project)[: Line range]" + LINE_FEED_MSG_PART);
 				EList<R4EFileContext> contexts = ((R4EItem) component).getFileContextList();
 				for (R4EFileContext context : contexts) {
 					R4EFileVersion fileVersion = context.getTarget();
 					if (null != fileVersion) {
 						IResource resource = fileVersion.getResource();
 						msgBody.append(TAB_MSG_PART);
 						//Project
 						if (null != resource) {
 							msgBody.append(resource.getProject());
 						}
 						//Path
 						String path = fileVersion.getRepositoryPath();
 						if (null != path && !(path.equals(""))) {
 							msgBody.append(": " + path + " (Repository)");
 						} else if (null != resource) {
 							msgBody.append(": " + resource.getProjectRelativePath() + " (Project)");
 						}
 						//Line Range
 						if (context.getDeltas().size() > 0) {
 							msgBody.append(": ");
 							EList<R4EDelta> deltas = context.getDeltas();
 							for (R4EDelta delta : deltas) {
 								msgBody.append(buildLineTag(delta) + ", ");
 							}
 						}
 					}
 					msgBody.append(LINE_FEED_MSG_PART);
 				}
 				msgBody.append(LINE_FEED_MSG_PART);
 			} else if (component instanceof R4EDelta) {
 				if (!legendAppended) {
 					msgBody.append("Eclipse Project: File Path (Repository | Project)[: Line range]"
 							+ LINE_FEED_MSG_PART);
 					legendAppended = true;
 				}
 				R4EFileContext context = (R4EFileContext) ((R4EDelta) component).eContainer();
 				R4EFileVersion fileVersion = context.getTarget();
 				if (null != fileVersion) {
 					IResource resource = fileVersion.getResource();
 					msgBody.append(TAB_MSG_PART);
 					//Project
 					if (null != resource) {
 						msgBody.append(resource.getProject());
 					}
 					//Path
 					String path = fileVersion.getRepositoryPath();
 					if (null != path && !(path.equals(""))) {
 						msgBody.append(": " + path + " (Repository)");
 					} else if (null != resource) {
 						msgBody.append(": " + resource.getProjectRelativePath() + " (Project)");
 					}
 					msgBody.append(": " + buildLineTag((R4EDelta) component) + ", ");
 				}
 			}
 			msgBody.append(LINE_FEED_MSG_PART);
 		}
 
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createProgressNotification
 	 * 
 	 * @param aHeader
 	 *            String
 	 * @return String
 	 */
 	private static String createProgressNotification(String aHeader) {
 		final StringBuilder msgBody = new StringBuilder();
 
 		msgBody.append(createIntroPart());
 		msgBody.append(aHeader + LINE_FEED_MSG_PART);
 
 		//First count the number of elements to add to the message 
 		int numReviewedFiles = 0;
 		int numTotalFiles = 0;
 		int numTotalAnomalies = 0;
 		final List<R4EUIReviewItem> items = R4EUIModelController.getActiveReview().getReviewItems();
 		for (R4EUIReviewItem item : items) {
 			R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 			for (R4EUIFileContext context : contexts) {
 				if (context.isUserReviewed()) {
 					++numReviewedFiles;
 				}
 				++numTotalFiles;
 				if (null != (R4EUIAnomalyContainer) context.getAnomalyContainerElement()) {
 					R4EUIAnomalyBasic[] anomalies = (R4EUIAnomalyBasic[]) ((R4EUIAnomalyContainer) context.getAnomalyContainerElement()).getChildren();
 					for (R4EUIAnomalyBasic anomaly : anomalies) {
 						if (anomaly.getAnomaly().getUser().getId().equals(R4EUIModelController.getReviewer())) {
 							++numTotalAnomalies; //Specific anomalies
 						}
 					}
 				}
 			}
 		}
 		final R4EUIAnomalyBasic[] globalAnomalies = (R4EUIAnomalyBasic[]) R4EUIModelController.getActiveReview()
 				.getAnomalyContainer()
 				.getChildren();
 		for (R4EUIAnomalyBasic anomaly : globalAnomalies) {
 			if (anomaly.getAnomaly().getUser().getId().equals(R4EUIModelController.getReviewer())) {
 				++numTotalAnomalies; //Global Anomalies
 			}
 		}
 
 		//Add current review progress
 		msgBody.append("Files Reviewed: " + numReviewedFiles + TAB_MSG_PART);
 		msgBody.append("Files Total: " + numTotalFiles + TAB_MSG_PART);
 		final double progress = (numReviewedFiles / new Integer(numTotalFiles).doubleValue()) * 100;
 		final DecimalFormat fmt = new DecimalFormat("#");
 		msgBody.append("Progress: " + fmt.format(progress) + "%");
 		msgBody.append(LINE_FEED_MSG_PART);
 
 		//Add anomalies created by current reviewer
 		msgBody.append("Anomalies Created by: " + R4EUIModelController.getReviewer() + LINE_FEED_MSG_PART);
 		msgBody.append("Count: " + numTotalAnomalies + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 
 		R4EUIFileContext context;
 		R4EUIAnomalyBasic anomaly;
 		for (R4EUIReviewItem item : items) {
 			R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 			for (int i = 0; i < contexts.length; i++) {
 				context = contexts[i];
 				if (0 == i) {
 					//Add format line
 					msgBody.append("FileContext: " + TAB_MSG_PART + "Eclipse Project: File Path (Repository | Project)"
 							+ LINE_FEED_MSG_PART);
 				}
 				if (null != (R4EUIAnomalyContainer) context.getAnomalyContainerElement()) {
 					R4EUIAnomalyBasic[] anomalies = (R4EUIAnomalyBasic[]) ((R4EUIAnomalyContainer) context.getAnomalyContainerElement()).getChildren();
 
 					R4EFileVersion fileVersion = context.getTargetFileVersion();
 					if (context.isEnabled() && null != fileVersion) {
 						IResource resource = fileVersion.getResource();
 						msgBody.append(TAB_MSG_PART);
 						//Project
 						if (null != resource) {
 							msgBody.append(resource.getProject());
 						}
 						//Path
 						String path = fileVersion.getRepositoryPath();
 						if (null != path && !(path.equals(""))) {
 							msgBody.append(": " + path + " (Repository)");
 						} else if (null != resource) {
 							msgBody.append(": " + resource.getProjectRelativePath() + " (Project)");
 						}
 						msgBody.append(LINE_FEED_MSG_PART);
 					}
 
 					for (int j = 0; j < anomalies.length; j++) {
 						anomaly = anomalies[j];
 						if (0 == j) {
 							//Add format line
 							msgBody.append(TAB_MSG_PART + "Anomaly: " + "Line Range: Title: Description"
 									+ LINE_FEED_MSG_PART);
 						}
 						if (anomaly.getAnomaly().getUser().getId().equals(R4EUIModelController.getReviewer())) {
 							//Add anomaly
 							msgBody.append(TAB_MSG_PART + TAB_MSG_PART + "   " + anomaly.getPosition().toString()
 									+ ": " + anomaly.getAnomaly().getTitle() + ": "
 									+ anomaly.getAnomaly().getDescription() + LINE_FEED_MSG_PART);
 
 							//Also add child comments
 							R4EUIComment[] comments = (R4EUIComment[]) anomaly.getChildren();
 							for (R4EUIComment comment : comments) {
 								msgBody.append(TAB_MSG_PART + TAB_MSG_PART + TAB_MSG_PART + "Comment: "
 										+ comment.getComment().getDescription() + LINE_FEED_MSG_PART);
 							}
 						}
 					}
 				}
 			}
 		}
 		msgBody.append(LINE_FEED_MSG_PART);
 
 		//Add global anomalies
 		if (globalAnomalies.length > 0) {
 			msgBody.append("Global Anomalies: " + LINE_FEED_MSG_PART);
 		}
 		for (R4EUIAnomalyBasic globalAnomaly : globalAnomalies) {
 			if (globalAnomaly.getAnomaly().getUser().getId().equals(R4EUIModelController.getReviewer())) {
 				//Add anomaly
 				msgBody.append(globalAnomaly.getAnomaly().getTitle() + ": "
 						+ globalAnomaly.getAnomaly().getDescription() + LINE_FEED_MSG_PART);
 
 				//Also add child comments
 				R4EUIComment[] globalComments = (R4EUIComment[]) globalAnomaly.getChildren();
 				for (R4EUIComment globalComment : globalComments) {
 					msgBody.append(TAB_MSG_PART + "Comment: " + globalComment.getComment().getDescription()
 							+ LINE_FEED_MSG_PART);
 				}
 			}
 		}
 
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method createQuestionMessage
 	 * 
 	 * @param aSource
 	 *            Object
 	 * @return String
 	 */
 	private static String createQuestionMessage(Object aSource) {
 		final StringBuilder msgBody = new StringBuilder();
 		msgBody.append(createIntroPart());
 		msgBody.append(QUESTION_MSG_BODY);
 
 		if (aSource instanceof List) {
 			for (Object sourceElement : (List<?>) aSource) {
 				addElementInfo(msgBody, sourceElement);
 				msgBody.append(LINE_FEED_MSG_PART);
 			}
 		} else if (aSource instanceof IR4EUIModelElement) {
 			addElementInfo(msgBody, aSource);
 		} else if (aSource instanceof TextSelection) {
 			addElementInfo(msgBody, aSource);
 		}
 
 		msgBody.append(LINE_FEED_MSG_PART);
 		msgBody.append(createReviewInfoPart());
 		msgBody.append(createOutroPart());
 		return msgBody.toString();
 	}
 
 	/**
 	 * Method addElementInfo
 	 * 
 	 * @param aMsgBody
 	 *            StringBuilder
 	 * @param aSource
 	 *            Object
 	 */
 	private static void addElementInfo(StringBuilder aMsgBody, Object aSource) {
 		if (aSource instanceof R4EUIPostponedAnomaly) {
 			final R4EFileVersion file = ((R4EUIFileContext) ((R4EUIPostponedAnomaly) aSource).getParent()).getTargetFileVersion();
 			if (null != file) {
 				String path = file.getRepositoryPath();
 				if (null != path && !(path.equals(""))) {
 					aMsgBody.append("Postponed File Path (Repository): " + path + LINE_FEED_MSG_PART);
 				} else if (null != file.getResource()) {
 					aMsgBody.append("Postponed File Path (Project): " + file.getResource().getProject() + ": "
 							+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				}
 				aMsgBody.append("Postponed File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				aMsgBody.append("Postponed File (Local): "
 						+ ((R4EUIFileContext) ((R4EUIPostponedAnomaly) aSource).getParent()).getName()
 						+ LINE_FEED_MSG_PART);
 			}
 			aMsgBody.append("Postponed Anomaly Line(s): " + ((R4EUIPostponedAnomaly) aSource).getPosition().toString()
 					+ LINE_FEED_MSG_PART);
 			aMsgBody.append("Postponed Anomaly Title: " + ((R4EUIPostponedAnomaly) aSource).getAnomaly().getTitle()
 					+ LINE_FEED_MSG_PART);
 			aMsgBody.append("Postponed Anomaly Description: "
 					+ ((R4EUIPostponedAnomaly) aSource).getAnomaly().getDescription() + LINE_FEED_MSG_PART);
 		} else if (aSource instanceof R4EUIAnomalyBasic) {
 			final IR4EUIModelElement parent = ((R4EUIAnomalyBasic) aSource).getParent().getParent();
 			if (parent instanceof R4EUIFileContext) {
 				//This is an anomaly tied to specific content
 				final R4EFileVersion file = ((R4EUIFileContext) parent).getTargetFileVersion();
 				if (null != file) {
 					String path = file.getRepositoryPath();
 					if (null != path && !(path.equals(""))) {
 						aMsgBody.append("File Path (Repository): " + path + LINE_FEED_MSG_PART);
 					} else if (null != file.getResource()) {
 						aMsgBody.append("File Path (Project): " + file.getResource().getProject() + ": "
 								+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 					}
 					aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 				} else {
 					aMsgBody.append("File (Local): "
 							+ ((R4EUIFileContext) ((R4EUIAnomalyBasic) aSource).getParent().getParent()).getName()
 							+ LINE_FEED_MSG_PART);
 				}
 				aMsgBody.append("Anomaly Line(s): " + ((R4EUIAnomalyBasic) aSource).getPosition().toString()
 						+ LINE_FEED_MSG_PART);
 			}
 			aMsgBody.append("Anomaly Title: " + ((R4EUIAnomalyBasic) aSource).getAnomaly().getTitle()
 					+ LINE_FEED_MSG_PART);
 			aMsgBody.append("Anomaly Description: " + ((R4EUIAnomalyBasic) aSource).getAnomaly().getDescription()
 					+ LINE_FEED_MSG_PART);
 		} else if (aSource instanceof R4EUIComment) {
 			final IR4EUIModelElement parent = ((R4EUIComment) aSource).getParent().getParent().getParent();
 			if (parent instanceof R4EUIFileContext) {
 				final R4EFileVersion file = ((R4EUIFileContext) parent).getTargetFileVersion();
 				if (null != file) {
 					String path = file.getRepositoryPath();
 					if (null != path && !(path.equals(""))) {
 						aMsgBody.append("File Path (Repository): " + path + LINE_FEED_MSG_PART);
 					} else if (null != file.getResource()) {
 						aMsgBody.append("File Path (Project): " + file.getResource().getProject() + ": "
 								+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 					}
 					aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 				} else {
 					aMsgBody.append("File (Local): "
 							+ ((R4EUIFileContext) ((R4EUIComment) aSource).getParent().getParent().getParent()).getName()
 							+ LINE_FEED_MSG_PART);
 				}
 				aMsgBody.append("Anomaly Line(s): "
 						+ ((R4EUIAnomalyBasic) ((R4EUIComment) aSource).getParent()).getPosition().toString()
 						+ LINE_FEED_MSG_PART);
 			}
 			aMsgBody.append("Anomaly Title: "
 					+ ((R4EUIAnomalyBasic) ((R4EUIComment) aSource).getParent()).getAnomaly().getTitle()
 					+ LINE_FEED_MSG_PART);
 			aMsgBody.append("Anomaly Description: "
 					+ ((R4EUIAnomalyBasic) ((R4EUIComment) aSource).getParent()).getAnomaly().getDescription()
 					+ LINE_FEED_MSG_PART);
 
 			aMsgBody.append("Anomaly Comment: " + ((R4EUIComment) aSource).getComment().getDescription()
 					+ LINE_FEED_MSG_PART);
 		} else if (aSource instanceof R4EUIReviewBasic) {
 			aMsgBody.append("Review: " + ((R4EUIReviewBasic) aSource).getReview().getName() + LINE_FEED_MSG_PART);
 
 		} else if (aSource instanceof R4EUIReviewItem) {
 			final String description = ((R4EUIReviewItem) aSource).getItem().getDescription();
 			if (null != description) {
 				aMsgBody.append("Review Item Description: " + description + LINE_FEED_MSG_PART);
 			} else {
 				//This is a ressource review item, so put the resource filename
 				aMsgBody.append("Review Item Resource: "
 						+ ((R4EUIReviewItem) aSource).getFileContexts().get(0).getName() + LINE_FEED_MSG_PART);
 			}
 
 		} else if (aSource instanceof R4EUIFileContext) {
 			final R4EFileVersion targetFile = ((R4EUIFileContext) aSource).getTargetFileVersion();
 			if (null != targetFile) {
 				String path = targetFile.getRepositoryPath();
 				if (null != path && !(path.equals(""))) {
 					aMsgBody.append("Target File Path (Repository): " + path + LINE_FEED_MSG_PART);
 				} else if (null != targetFile.getResource()) {
 					aMsgBody.append("Target File Path (Project): " + targetFile.getResource().getProject() + ": "
 							+ targetFile.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				}
 				aMsgBody.append("Target File Version: " + targetFile.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				aMsgBody.append("Target File (Local): " + ((R4EUIFileContext) aSource).getName() + LINE_FEED_MSG_PART);
 				aMsgBody.append("Target File Version: None" + LINE_FEED_MSG_PART);
 			}
 			final R4EFileVersion baseFile = ((R4EUIFileContext) aSource).getBaseFileVersion();
 			if (null != baseFile) {
 				String path = baseFile.getRepositoryPath();
 				if (null != path && !(path.equals(""))) {
 					aMsgBody.append("Base File Path (Repository): " + path + LINE_FEED_MSG_PART);
 				} else if (null != baseFile.getResource()) {
 					aMsgBody.append("Base File Path (Project): " + baseFile.getResource().getProject() + ": "
 							+ baseFile.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				}
 				aMsgBody.append("Base File Version: " + baseFile.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				aMsgBody.append("Base File (Local): " + ((R4EUIFileContext) aSource).getName() + LINE_FEED_MSG_PART);
 				aMsgBody.append("Base File Version: None" + LINE_FEED_MSG_PART);
 			}
 		} else if (aSource instanceof R4EUIContent) {
 			final R4EFileVersion file = ((R4EUIFileContext) ((R4EUIContent) aSource).getParent().getParent()).getTargetFileVersion();
 			if (null != file) {
 				String path = file.getRepositoryPath();
 				if (null != path && !(path.equals(""))) {
 					aMsgBody.append("File Path (Repository): " + path + LINE_FEED_MSG_PART);
 				} else if (null != file.getResource()) {
 					aMsgBody.append("File Path (Project): " + file.getResource().getProject() + ": "
 							+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				}
 				aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 			} else {
 				aMsgBody.append("File (Local): "
 						+ ((R4EUIFileContext) ((R4EUIContent) aSource).getParent().getParent()).getName()
 						+ LINE_FEED_MSG_PART);
 			}
 			aMsgBody.append("Content Line(s): " + ((R4EUIContent) aSource).getPosition().toString()
 					+ LINE_FEED_MSG_PART);
 
 		} else if (aSource instanceof ITextEditor) {
 			//Get the information from the text editor
 			final IRegion region = ((ITextEditor) aSource).getHighlightRange();
 			final IEditorInput input = ((ITextEditor) aSource).getEditorInput();
 			final TextSelection selectedText = new TextSelection(((ITextEditor) aSource).getDocumentProvider()
 					.getDocument(input), region.getOffset(), region.getLength());
 			((ITextEditor) aSource).getSelectionProvider().setSelection(selectedText);
 			R4EFileVersion file = null;
 			if (input instanceof R4EFileRevisionEditorInput) {
 				file = ((R4EFileRevisionEditorInput) input).getFileVersion();
 				aMsgBody.append("File: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 			} else if (input instanceof R4EFileEditorInput) {
 				file = ((R4EFileEditorInput) input).getFileVersion();
 				aMsgBody.append("File: " + file.getResource().getProject() + ": "
 						+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 			}
 			aMsgBody.append(LINE_FEED_MSG_PART);
 			aMsgBody.append("Position in File: " + CommandUtils.getPosition(selectedText).toString()
 					+ LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			aMsgBody.append("Contents :" + LINE_FEED_MSG_PART);
 			aMsgBody.append(selectedText.getText());
 
 		} else if (aSource instanceof TextSelection) {
 			final IEditorPart editorPart = PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow()
 					.getActivePage()
 					.getActiveEditor();
 			final IEditorInput input = editorPart.getEditorInput();
 			if (input instanceof R4ECompareEditorInput) {
 				//TODO: For now we give the file version of the file on the left side, regardless of whose side the selected input was.
 				//		Later we want to refine this.
 				final ITypedElement element = ((R4ECompareEditorInput) input).getLeftElement();
 				if (element instanceof R4EFileTypedElement) {
 					final R4EFileVersion file = ((R4EFileTypedElement) element).getFileVersion();
 					aMsgBody.append("File: " + file.getResource().getProject() + ": "
 							+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 					aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 				} else if (element instanceof R4EFileRevisionTypedElement) {
 					final R4EFileVersion file = ((R4EFileRevisionTypedElement) element).getFileVersion();
 					aMsgBody.append("File Path: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 					aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 				}
 			} else if (input instanceof R4EFileRevisionEditorInput) {
 				final R4EFileVersion file = ((R4EFileRevisionEditorInput) input).getFileVersion();
 				aMsgBody.append("File Path: " + file.getRepositoryPath() + LINE_FEED_MSG_PART);
 				aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 			} else if (input instanceof R4EFileEditorInput) {
 				final R4EFileVersion file = ((R4EFileEditorInput) input).getFileVersion();
 				aMsgBody.append("File: " + file.getResource().getProject() + ": "
 						+ file.getResource().getProjectRelativePath() + LINE_FEED_MSG_PART);
 				aMsgBody.append("File Version: " + file.getVersionID() + LINE_FEED_MSG_PART);
 			} else if (input instanceof FileEditorInput) {
 				final IFile file = ((FileEditorInput) input).getFile();
 				aMsgBody.append("File: " + file.getProject() + ": " + file.getProjectRelativePath()
 						+ LINE_FEED_MSG_PART);
 				aMsgBody.append("File Version: (None available)" + LINE_FEED_MSG_PART);
 			}
 			final TextSelection selectedText = (TextSelection) aSource;
 			aMsgBody.append(LINE_FEED_MSG_PART);
 			aMsgBody.append("Position in File: " + CommandUtils.getPosition(selectedText).toString()
 					+ LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			aMsgBody.append("Contents: " + LINE_FEED_MSG_PART);
 			aMsgBody.append(selectedText.getText());
 		} else if (R4EUIPlugin.isJDTAvailable() && aSource instanceof ISourceReference) {
 			//NOTE:  This is always true because all elements that implement ISourceReference
 			//       also implement IJavaElement.  The resource is always an IFile
 			final IFile file = (IFile) ((IJavaElement) aSource).getResource();
 			aMsgBody.append("File: " + file.getProject() + ": " + file.getProjectRelativePath() + LINE_FEED_MSG_PART);
 			aMsgBody.append("File Version: (None available)" + LINE_FEED_MSG_PART);
 			try {
 				final R4EUITextPosition position = CommandUtils.getPosition((ISourceReference) aSource, file);
 				aMsgBody.append("Position in File: " + position.toString() + LINE_FEED_MSG_PART + LINE_FEED_MSG_PART);
 			} catch (CoreException e) {
 				// Ignore
 			}
 		} else if (R4EUIPlugin.isCDTAvailable() && aSource instanceof org.eclipse.cdt.core.model.ISourceReference) {
 			//NOTE:  This is always true because all elements that implement ISourceReference
 			//       also implement ICElement.  The resource is always an IFile
 			IFile file = null;
 			if (aSource instanceof org.eclipse.cdt.core.model.ITranslationUnit) {
 				file = (IFile) ((org.eclipse.cdt.core.model.ICElement) aSource).getResource();
 			} else if (aSource instanceof org.eclipse.cdt.core.model.ICElement) {
 				file = (IFile) ((org.eclipse.cdt.core.model.ICElement) aSource).getParent().getResource();
 			}
 			if (null != file) {
 				aMsgBody.append("File: " + file.getProject() + ": " + file.getProjectRelativePath()
 						+ LINE_FEED_MSG_PART);
 				aMsgBody.append("File Version: (None available)" + LINE_FEED_MSG_PART);
 				try {
 					final R4EUITextPosition position = CommandUtils.getPosition(
 							(org.eclipse.cdt.core.model.ISourceReference) aSource, file);
 					aMsgBody.append("Position in File: " + position.toString() + LINE_FEED_MSG_PART
 							+ LINE_FEED_MSG_PART);
 				} catch (CoreException e) {
 					// Ignore
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method createReviewInfoPart
 	 * 
 	 * @return String
 	 */
 	private static String createReviewInfoPart() {
 		final StringBuilder msgReviewInfo = new StringBuilder();
 
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Review Information");
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Group: " + TAB_MSG_PART + TAB_MSG_PART
 				+ R4EUIModelController.getActiveReview().getParent().getName() + LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Review: " + TAB_MSG_PART + R4EUIModelController.getActiveReview().getReview().getName()
 				+ LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Components: " + TAB_MSG_PART);
 		final List<String> components = R4EUIModelController.getActiveReview().getReview().getComponents();
 		for (String component : components) {
 			msgReviewInfo.append(component + ", ");
 		}
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Project: " + TAB_MSG_PART
 				+ R4EUIModelController.getActiveReview().getReview().getProject() + LINE_FEED_MSG_PART);
 		msgReviewInfo.append("Participants: " + TAB_MSG_PART);
 		final List<String> participants = R4EUIModelController.getActiveReview().getParticipantIDs();
 		for (String participant : participants) {
 			msgReviewInfo.append(participant + ", ");
 		}
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 		msgReviewInfo.append(LINE_FEED_MSG_PART);
 
 		return msgReviewInfo.toString();
 	}
 
 	/**
 	 * Method createIntroPart
 	 * 
 	 * @return String
 	 */
 	private static String createIntroPart() {
 		final StringBuilder msgIntro = new StringBuilder();
 		msgIntro.append(LINE_FEED_MSG_PART);
 		msgIntro.append(INTRO_MSG_BODY);
 		msgIntro.append(LINE_FEED_MSG_PART);
 		msgIntro.append(LINE_FEED_MSG_PART);
 		return msgIntro.toString();
 	}
 
 	/**
 	 * Method createOutroPart
 	 * 
 	 * @return String
 	 */
 	private static String createOutroPart() {
 		final StringBuilder msgOutro = new StringBuilder();
 		msgOutro.append(OUTRO_MSG_BODY + LINE_FEED_MSG_PART);
 		msgOutro.append(R4EUIModelController.getReviewer());
 		return msgOutro.toString();
 	}
 
 	//Meetings
 
 	/**
 	 * Method sendMeetingRequest
 	 * 
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @throws CoreException
 	 */
 	public static void sendMeetingRequest() throws ResourceHandlingException, OutOfSyncException {
 		if (null != R4EUIDialogFactory.getInstance().getMailConnector()) {
 
 			boolean meetingInfoFound = false;
 
 			//Check if a meeting request already exist.  If so, update it.  Otherwise create a new one
 			R4EMeetingData r4eMeetingData = R4EUIModelController.getActiveReview().getReview().getActiveMeeting();
 			IMeetingData meetingData = null;
 			if (null != r4eMeetingData) {
 				meetingData = R4EUIDialogFactory.getInstance()
 						.getMailConnector()
 						.openAndUpdateMeeting(createMeetingData(r4eMeetingData),
 								R4EUIModelController.getActiveReview().getReview().getStartDate());
 				if (null != meetingData) {
 					meetingInfoFound = true;
 				}
 			}
 
 			if (!meetingInfoFound) {
 				final String[] messageDestinations = createItemsUpdatedDestinations();
 
 				//Notify user if request cannot be sent when no valid destinations defined
 				if (messageDestinations.length == 0) {
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"Cannot Send Meeting Request", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID,
 									"No valid destinations for participants defined", null), IStatus.ERROR);
 					dialog.open();
 					return;
 				}
 
 				String messageSubject = null;
 				if (null != R4EUIModelController.getActiveReview().getReview().getActiveMeeting()) {
 					messageSubject = createSubject() + " - Decision Meeting Request Updated";
 				} else {
 					messageSubject = createSubject() + " - Items Ready for Review & Decision Meeting Request";
 				}
 				final String messageBody = createItemsReadyNotificationMessage(true);
 
 				try {
 					meetingData = R4EUIDialogFactory.getInstance()
 							.getMailConnector()
 							.createMeetingRequest(messageSubject, messageBody, messageDestinations,
 									getDefaultStartTime(), DEFAULT_MEETING_DURATION);
 				} catch (CoreException e) {
 					R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 					R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e);
 					return;
 				}
 			}
 			R4EUIModelController.getActiveReview().setMeetingData(meetingData);
 		} else {
 			showNoEmailConnectorDialog();
 		}
 	}
 
 	/**
 	 * Method getDefaultStartTime
 	 * 
 	 * @return Long
 	 */
 	public static Long getDefaultStartTime() {
 
 		// Make sure we leave 3 days to review and don't set a meeting on the week-end
 		final GregorianCalendar meetingDate = new GregorianCalendar();
 		switch (meetingDate.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.TUESDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 518400000);
 			break;
 		case Calendar.WEDNESDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 691200000);
 			break;
 		case Calendar.THURSDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 604800000);
 			break;
 		case Calendar.FRIDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 518400000);
 			break;
 		case Calendar.SATURDAY:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 432000000);
 			break;
 		default:
 			meetingDate.setTimeInMillis(meetingDate.getTimeInMillis() + 345600000);
 			break;
 		}
 
 		// Set start time at 10 AM
 		meetingDate.set(Calendar.HOUR_OF_DAY, 10);
 		meetingDate.set(Calendar.MINUTE, 0);
 		meetingDate.set(Calendar.SECOND, 0);
 		// Add the current time zone offset
 		meetingDate.setTimeInMillis(meetingDate.getTimeInMillis()
 				+ TimeZone.getDefault().getOffset(System.currentTimeMillis()));
 		return Long.valueOf(meetingDate.getTimeInMillis());
 	}
 
 	/**
 	 * Method showNoEmailConnectorDialog
 	 */
 	private static void showNoEmailConnectorDialog() {
 		final ErrorDialog dialog = new ErrorDialog(
 				null,
 				R4EUIConstants.DIALOG_TITLE_WARNING,
 				"No Email connector detected"
 						+ "Take note that no Automatic Email can be sent because no Mail Services Connector is Present",
 				new Status(IStatus.WARNING, R4EUIPlugin.PLUGIN_ID, 0, null, null), IStatus.WARNING);
 		dialog.open();
 	}
 
 	/**
 	 * Method buildLineTag
 	 * 
 	 * @param aDelta
 	 *            R4EDelta
 	 * @return String
 	 */
 	private static String buildLineTag(R4EDelta aDelta) {
 		if (null != aDelta.getTarget() && null != aDelta.getTarget().getLocation()) {
 			final int startLine = ((R4ETextPosition) aDelta.getTarget().getLocation()).getStartLine();
 			final int endLineLine = ((R4ETextPosition) aDelta.getTarget().getLocation()).getEndLine();
 			final StringBuilder buffer = new StringBuilder(R4EUIConstants.DEFAULT_LINE_TAG_LENGTH);
 			if (startLine == endLineLine) {
 				buffer.append(R4EUIConstants.LINE_TAG + startLine);
 			} else {
 				buffer.append(R4EUIConstants.LINES_TAG + startLine + "-" + endLineLine);
 			}
 			return buffer.toString();
 		}
 		return "";
 	}
 
 	private static IMeetingData createMeetingData(final R4EMeetingData aR4EMeetingData) {
 
 		final ResourceUpdater resUpdater = R4EUIModelController.FResourceUpdater;
 		return new IMeetingData() {
 
 			public int getSentCounter() {
 				return aR4EMeetingData.getSentCount();
 			}
 
 			public void incrementSentCounter() {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setSentCount(aR4EMeetingData.getSentCount() + 1);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public void clearSentCounter() {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setSentCount(0);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getCustomID() {
 				return aR4EMeetingData.getId();
 			}
 
 			public void setCustomID(String aId) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setId(aId);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getSubject() {
 				return aR4EMeetingData.getSubject();
 			}
 
 			public void setSubject(String aSubject) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setSubject(aSubject);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getBody() {
 				return aR4EMeetingData.getBody();
 			}
 
 			public void setBody(String aBody) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setBody(aBody);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getLocation() {
 				return aR4EMeetingData.getLocation();
 			}
 
 			public void setLocation(String aLocation) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setLocation(aLocation);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public Long getStartTime() {
 				return Long.valueOf(aR4EMeetingData.getStartTime());
 			}
 
 			public void setStartTime(Long aStartTime) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setStartTime(aStartTime.longValue());
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public Integer getDuration() {
 				return Integer.valueOf(aR4EMeetingData.getDuration());
 			}
 
 			public void setDuration(Integer aDuration) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setDuration(aDuration.intValue());
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String getSender() {
 				return aR4EMeetingData.getSender();
 			}
 
 			public void setSender(String aSender) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.setSender(aSender);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public String[] getReceivers() {
 				final EList<String> recieversL = aR4EMeetingData.getReceivers();
 				final String[] receivers = recieversL.toArray(new String[recieversL.size()]);
 				return receivers;
 			}
 
 			public void clearReceivers() {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.getReceivers().clear();
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public void addReceiver(String aReceiver) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.getReceivers().add(aReceiver);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			public void removeReceiver(String aReceiver) {
 				try {
 					final Long bookNum = resUpdater.checkOut(aR4EMeetingData, R4EUIModelController.getReviewer());
 					aR4EMeetingData.getReceivers().remove(aReceiver);
 					resUpdater.checkIn(bookNum);
 				} catch (ResourceHandlingException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				} catch (OutOfSyncException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 		};
 	}
 }
