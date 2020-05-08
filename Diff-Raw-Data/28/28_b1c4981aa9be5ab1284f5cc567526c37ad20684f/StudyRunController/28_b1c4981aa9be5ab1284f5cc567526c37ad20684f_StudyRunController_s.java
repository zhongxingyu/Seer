 package com.mamykin.psytest.client;
 
 import java.util.Collection;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.HasValue;
 import com.mamykin.psytest.client.model.SlideElementWidget;
 import com.mamykin.psytest.client.model.StudyInfo;
 import com.mamykin.psytest.client.model.StudyRun;
 import com.mamykin.psytest.client.model.StudySlide;
 
 public class StudyRunController {
 
 	interface StartView {
 		HasText getStudyName();
 
 		void setGroupsList(Collection<String> groups);
 
 		String getSelectedGroup();
 
 		void setCasesList(Collection<String> cases);
 
 		String getSelectedCase();
 
 		HasValue<String> getParticipantName();
 
 		HasClickHandlers getStartButton();
 
 		void setVisible(Boolean visible);
 	}
 
 	interface RunView {
 		void setContent(List<SlideElementWidget> elements);
 
 		HasClickHandlers getContinueButton();
 
 		void setVisible(Boolean visible);
 
 		boolean contentIsValid();
 
 		void recordResults();
 	}
 
 	interface FinishView {
 		HasClickHandlers getFinishButton();
 
 		void setVisible(Boolean visible);
 	}
 
 	interface ErrorsNotification {
 		void displayError(String message);
 	}
 
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 */
 	// private static final String SERVER_ERROR = "An error occurred while "
 	// + "attempting to contact the server. Please check your network "
 	// + "connection and try again.";
 	private StartView startView;
 	private RunView runView;
 	private FinishView finishView;
 	private StudyServiceAsync studyService;
 	private ErrorsNotification errorsNotification;
 	private StudyRun currentRun;
 
 	public StudyRunController(StartView startView, RunView runView,
 			FinishView finishView, ErrorsNotification errorsNotification,
 			StudyServiceAsync studyService) {
 		this.startView = startView;
 		this.runView = runView;
 		this.finishView = finishView;
 		this.errorsNotification = errorsNotification;
 		this.studyService = studyService;
 		wireEvents();
 	}
 
 	public void start() {
 		startView.setVisible(true);
 		runView.setVisible(false);
 		finishView.setVisible(false);
 		retrieveStudy();
 	}
 
 	private void retrieveStudy() {
 		studyService.getStudyInfo(new AsyncCallback<StudyInfo>() {
 
 			public void onFailure(Throwable caught) {
 				errorsNotification.displayError(caught.getMessage());
 			}
 
 			public void onSuccess(StudyInfo result) {
 				displayStudy(result);
 			}
 		});
 	}
 
 	private void displayStudy(StudyInfo info) {
 		startView.getStudyName().setText(info.getName());
 		startView.setGroupsList(info.getGroups());
 		startView.setCasesList(info.getCases());
 		startView.getParticipantName().setValue("");
 	}
 
 	private void wireEvents() {
 		startView.getStartButton().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				startButtonPressed();
 			}
 		});
 
 		runView.getContinueButton().addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				nextSlideButtonPressed();
 			}
 
 		});
 
 		finishView.getFinishButton().addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				finishButtonPressed();
 			}
 
 		});
 	}
 
 	private void startButtonPressed() {
 		retrieveStudyRunSlides();
 		startView.setVisible(false);
 		runView.setVisible(true);
 	}
 
 	private void retrieveStudyRunSlides() {
 		studyService.getStudyRun(startView.getSelectedGroup(), startView
 				.getSelectedCase(), startView.getParticipantName().getValue(),
 				new AsyncCallback<StudyRun>() {
 
 					public void onFailure(Throwable caught) {
 						errorsNotification.displayError(caught.getMessage());
 					}
 
 					public void onSuccess(StudyRun result) {
 						displayStudyRun(result);
 					}
 				});
 	}
 
 	protected void displayStudyRun(StudyRun run) {
 		this.currentRun = run;
 		displayCurrentSlide();
 	}
 
 	protected void nextSlideButtonPressed() {
 		if (runView.contentIsValid()) {
 			runView.recordResults();
 
 			if (currentRun.hasMoreSlides()) {
 				currentRun.moveToNextSlide();
 				displayCurrentSlide();
 			} else {
 				runView.setVisible(false);
 				finishView.setVisible(true);
 			}
 		}
 	}
 
 	private void displayCurrentSlide() {
 		StudySlide currentSlide = currentRun.getCurrentSlide();
 		runView.setContent(currentSlide.createUIElements());
		if(currentSlide.isTimed()){
			Timer timer = new Timer(){

				@Override
				public void run() {
					nextSlideButtonPressed();
				}
				
			};
 			timer.schedule(currentSlide.getTimeLimitInMillis());
 		}
 	}
 
 	private void finishButtonPressed() {
 		recordStudyResults();
 	}
 
 	private void recordStudyResults() {
 		studyService.recordRunResults(currentRun.getResults(),
 				new AsyncCallback<Boolean>() {
 
 					public void onFailure(Throwable caught) {
 						errorsNotification.displayError(caught.getMessage());
 					}
 
 					public void onSuccess(Boolean result) {
 						start();
 					}
 				});
 	}
 
 }
