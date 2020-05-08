 package com.google.gwt.audio.recorder.client;
 
 import com.google.gwt.audio.recorder.client.event.MicrophoneConnectedEvent;
 import com.google.gwt.audio.recorder.client.event.MicrophoneNotConnectedEvent;
 import com.google.gwt.audio.recorder.client.event.MicrophoneUserRequestEvent;
 import com.google.gwt.audio.recorder.client.event.NoMicrophoneFoundEvent;
 import com.google.gwt.audio.recorder.client.event.PlaybackStartedEvent;
 import com.google.gwt.audio.recorder.client.event.PlaybackStoppedEvent;
 import com.google.gwt.audio.recorder.client.event.PlayingEvent;
 import com.google.gwt.audio.recorder.client.event.RecorderReadyEvent;
 import com.google.gwt.audio.recorder.client.event.RecordingEvent;
 import com.google.gwt.audio.recorder.client.event.RecordingStoppedEvent;
 import com.google.gwt.audio.recorder.client.event.SaveFailedEvent;
 import com.google.gwt.audio.recorder.client.event.SavePressedEvent;
 import com.google.gwt.audio.recorder.client.event.SaveProgressEvent;
 import com.google.gwt.audio.recorder.client.event.SavedEvent;
 import com.google.gwt.audio.recorder.client.event.SavingEvent;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.FormElement;
 import com.google.gwt.dom.client.InputElement;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 
 public class Recorder extends Composite {
 
 	private final static int DEFAULT_WIDTH = 24;
 	private final static int DEFAULT_HEIGHT = 24;
 	private final static String DEFAULT_APPLICATION_NAME = "RECORDER_APPLICATION";
 	private final static String SWF_OBJECT = GWT.getModuleBaseURL() + "recorder.swf";
 
 	private final static String FLASH_CONTAINER_ID = "RECORDER_FLASH_CONTAINER";
 	private final static String UPLOAD_FORM_ID = "RECORDER_UPLOAD_FORM";
 	private final static String UPLOAD_FIELD_NAME = "RECORDER_UPLOAD_FILE";
 
 	private JavaScriptObject flashRecorder;
 	private int recorderOriginalWidth = DEFAULT_WIDTH;
 	private int recorderOriginalHeight = DEFAULT_HEIGHT;
 	private String uploadFormId = UPLOAD_FORM_ID;
 	private String uploadFieldName = UPLOAD_FIELD_NAME + "[filename]";
 
 	private String uploadImage;
 	private String uploadURL;
 
 	private String filename;
 
 	private FormElement form;
 
 	@UiConstructor
 	public Recorder(String uploadImage) {
 		this.uploadImage = uploadImage;
 		HTMLPanel mainContent = new HTMLPanel("");
 		// Flash container
 		DivElement flashContainer = Document.get().createDivElement();
 		flashContainer.setId(FLASH_CONTAINER_ID);
 		flashContainer.setInnerHTML("Your browser must have JavaScript enabled and the Adobe Flash Player installed.");
 		mainContent.getElement().appendChild(flashContainer);
 		// Form
 		form = Document.get().createFormElement();
 		form.setId(UPLOAD_FORM_ID);
 		form.setName(UPLOAD_FORM_ID);
 		InputElement fileInput = Document.get().createHiddenInputElement();
 		fileInput.setName(UPLOAD_FIELD_NAME + "[parent_id]");
 		fileInput.setValue("1");
 		form.appendChild(fileInput);
 		mainContent.getElement().appendChild(form);
 		initWidget(mainContent);
 	}
 
 	@Override
 	protected void onAttach() {
 		super.onAttach();
 		this.loadFlashRecorder(DEFAULT_WIDTH, DEFAULT_HEIGHT, uploadImage, DEFAULT_APPLICATION_NAME, SWF_OBJECT,
 				FLASH_CONTAINER_ID, UPLOAD_FORM_ID, UPLOAD_FIELD_NAME);
 	}
 
 	private native void loadFlashRecorder(int width, int height, String uploadImage, String applicationName,
 			String swfObject, String containerId, String uploadFormId, String uploadFieldname) /*-{
 		var instance = this;
 		// Event management
 		$wnd.microphone_recorder_events = function() {
 			switch (arguments[0]) {
 			case "ready":
 				var width = parseInt(arguments[1]);
 				var height = parseInt(arguments[2]);
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onRecorderReady(II)(width, height);
 				instance.@com.google.gwt.audio.recorder.client.Recorder::connect(Ljava/lang/String;I)(applicationName, 0);
 				break;
 
 			case "no_microphone_found":
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onNoMicrophoneFound()();
 				break;
 
 			case "microphone_user_request":
 				instance.@com.google.gwt.audio.recorder.client.Recorder::showPermissionWindow()();
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onMicrophoneUserRequest()();
 				break;
 
 			case "microphone_connected":
 				var mic = arguments[1];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::defaultSize()();
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onMicrophoneConnected(Ljava/lang/String;)(mic.name);
 				break;
 
 			case "microphone_not_connected":
 				instance.@com.google.gwt.audio.recorder.client.Recorder::defaultSize()();
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onMicrophoneNotConnected()();
 				break;
 
 			case "recording":
 				var name = arguments[1];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onRecording(Ljava/lang/String;)(name);
 				instance.@com.google.gwt.audio.recorder.client.Recorder::hide()();
 				break;
 
 			case "recording_stopped":
 				var name = arguments[1];
 				var duration = arguments[2];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.show();
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onRecordingStop(Ljava/lang/String;I)(name, duration);
 				break;
 
 			case "playing":
 				var name = arguments[1];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onPlaying(Ljava/lang/String;)(name);
 				break;
 
 			case "playback_started":
 				var name = arguments[1];
 				var latency = arguments[2];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onPlaybackStarted(Ljava/lang/String;I)(name, latency);
 				break;
 
 			case "stopped":
 				var name = arguments[1];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onPlaybackStopped(Ljava/lang/String;)(name);
 				break;
 
 			case "save_pressed":
 				instance.@com.google.gwt.audio.recorder.client.Recorder::updateForm()();
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onSavePressed()();
 				break;
 
 			case "saving":
 				var name = arguments[1];
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onSaving(Ljava/lang/String;)(name);
 				break;
 
 			case "saved":
 				var name = arguments[1];
 				//				var data = $.parseJSON(arguments[2]);
 				//				if (data.saved) {
 				instance.@com.google.gwt.audio.recorder.client.Recorder::onSaved(Ljava/lang/String;)(name);
 				break;
 
 			case "save_failed":
 				var name = arguments[1];
 				var errorMessage = arguments[2];
				// instance.@com.google.gwt.audio.recorder.client.Recorder::onSaveProgress(II)(bytesLoaded, bytesTotal);
 				break;
 			}
 		}
 
 		var appWidth = width;
 		var appHeight = height;
 		var flashvars = {
 			'event_handler' : 'microphone_recorder_events',
 			'upload_image' : uploadImage
 		};
 		var params = {};
 		var attributes = {
 			'id' : applicationName,
 			'name' : applicationName
 		};
 		$wnd.swfobject.embedSWF(swfObject, containerId, appWidth, appHeight, "10.1.0", "", flashvars, params, attributes);
 	}-*/;
 
 	private native void connect(String applicationName, int attempts) /*-{
 		var instance = this;
 		if (navigator.appName.indexOf("Microsoft") != -1) {
 			this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder = $wnd.window[applicationName];
 		} else {
 			this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder = $wnd.document[applicationName];
 		}
 		if (attempts >= 40) {
 			return;
 		}
 
 		// flash app needs time to load and initialize
 		if (this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				&& this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.init) {
 			this.@com.google.gwt.audio.recorder.client.Recorder::recorderOriginalWidth = this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.width;
 			this.@com.google.gwt.audio.recorder.client.Recorder::recorderOriginalHeight = this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.height;
 			if (this.@com.google.gwt.audio.recorder.client.Recorder::uploadFormId) {
 				var frm;
 				if (navigator.appName.indexOf("Microsoft") != -1) {
 					frm = $wnd.window[this.@com.google.gwt.audio.recorder.client.Recorder::uploadFormId];
 				} else {
 					frm = $wnd.document[this.@com.google.gwt.audio.recorder.client.Recorder::uploadFormId];
 				}
 				this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 						.init(
 								frm.getAttribute('action'),
 								this.@com.google.gwt.audio.recorder.client.Recorder::uploadFieldName);
 			}
 			return;
 		}
 		setTimeout(
 				function() {
 					instance.@com.google.gwt.audio.recorder.client.Recorder::connect(Ljava/lang/String;I)(applicationName, attempts + 1);
 				}, 100);
 	}-*/;
 
 	public native void play() /*-{
 		var filename = this.@com.google.gwt.audio.recorder.client.Recorder::filename;
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.playBack(filename);
 	}-*/;
 
 	public native void record() /*-{
 		var filename = this.@com.google.gwt.audio.recorder.client.Recorder::filename;
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.record(filename, filename);
 	}-*/;
 
 	public native void resize(int width, int height) /*-{
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.width = width
 				+ "px";
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.height = height
 				+ "px";
 	}-*/;
 
 	public native void defaultSize() /*-{
 		this
 				.@com.google.gwt.audio.recorder.client.Recorder::resize(II)
 				(
 						this.@com.google.gwt.audio.recorder.client.Recorder::recorderOriginalWidth,
 						this.@com.google.gwt.audio.recorder.client.Recorder::recorderOriginalHeight);
 	}-*/;
 
 	/**
 	 * show the save button
 	 */
 	public native void show() /*-{
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				.show();
 	}-*/;
 
 	/**
 	 * hide the save button
 	 */
 	public native void hide() /*-{
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				.hide();
 	}-*/;
 
 	/**
 	 * update the form data
 	 */
 	public native void updateForm() /*-{
 		// Not working
 		// var data = new Object;
 		// data.name = "id";
 		// data.value = this.@com.google.gwt.audio.recorder.client.Recorder::filename;
 		// this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.update(JSON.stringify(data));
 
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder.update();
 	}-*/;
 
 	/**
 	 * returns the duration of the recording
 	 * 
 	 * @param name
 	 *            name of the recording
 	 * @return
 	 */
 	public native int getDuration(String name) /*-{
 		return this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				.duration(name);
 	}-*/;
 
 	/**
 	 * show the permissions dialog for microphone access, make sure the flash
 	 * application is large enough for the dialog box before calling this
 	 * method. Must be at least 240x160.
 	 */
 	public native void showPermissionWindow() /*-{
 		var instance = this;
 		this.@com.google.gwt.audio.recorder.client.Recorder::resize(II)(240, 160);
 		// need to wait until app is resized before displaying permissions screen
 		setTimeout(
 				function() {
 					instance.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 							.permit();
 				}, 1);
 	}-*/;
 
 	/**
 	 * Configures microphone settings
 	 * 
 	 * @param rate
 	 *            at which the microphone captures sound, in kHz. default is 22.
 	 *            Currently we only support 44 and 22.
 	 * @param gain
 	 *            the amount by which the microphone should multiply the signal
 	 *            before transmitting it. default is 100
 	 * @param silenceLevel
 	 *            amount of sound required to activate the microphone and
 	 *            dispatch the activity event. default is 0
 	 * @param silenceTimeout
 	 *            number of milliseconds between the time the microphone stops
 	 *            detecting sound and the time the activity event is dispatched.
 	 *            default is 4000
 	 */
 	public native void configure(int rate, int gain, int silenceLevel, int silenceTimeout) /*-{
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				.configure(rate, gain, silenceLevel, silenceTimeout);
 	}-*/;
 
 	/**
 	 * use echo suppression
 	 * 
 	 * @param use
 	 */
 	private native void setUseEchoSuppression(boolean use) /*-{
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				.setUseEchoSuppression(value);
 	}-*/;
 
 	/**
 	 * routes audio captured by a microphone to the local speakers
 	 * 
 	 * @param loopBack
 	 */
 	private native void setLoopBack(boolean loopBack) /*-{
 		this.@com.google.gwt.audio.recorder.client.Recorder::flashRecorder
 				.setLoopBack(value);
 	}-*/;
 
 	private void onRecorderReady(int width, int height) {
 		this.fireEvent(new RecorderReadyEvent(width, height));
 	}
 
 	private void onNoMicrophoneFound() {
 		this.fireEvent(new NoMicrophoneFoundEvent());
 	}
 
 	private void onMicrophoneUserRequest() {
 		this.fireEvent(new MicrophoneUserRequestEvent());
 	}
 
 	private void onMicrophoneConnected(String name) {
 		this.fireEvent(new MicrophoneConnectedEvent(name));
 	}
 
 	private void onMicrophoneNotConnected() {
 		this.fireEvent(new MicrophoneNotConnectedEvent());
 	}
 
 	private void onRecording(String name) {
 		this.fireEvent(new RecordingEvent(name));
 	}
 
 	private void onRecordingStop(String name, int duration) {
 		this.fireEvent(new RecordingStoppedEvent(name, duration));
 	}
 
 	private void onPlaying(String name) {
 		this.fireEvent(new PlayingEvent(name));
 	}
 
 	private void onPlaybackStopped(String name) {
 		this.fireEvent(new PlaybackStoppedEvent(name));
 	}
 
 	private void onPlaybackStarted(String name, int latency) {
 		this.fireEvent(new PlaybackStartedEvent(name, latency));
 	}
 
 	private void onSavePressed() {
 		this.fireEvent(new SavePressedEvent());
 	}
 
 	private void onSaved(String name) {
 		this.fireEvent(new SavedEvent(name));
 	}
 
 	private void onSaveFailed(String name, String error) {
 		this.fireEvent(new SaveFailedEvent(name, error));
 	}
 
 	private void onSaveProgress(int byteLoaded, int bytesTotal) {
 		this.fireEvent(new SaveProgressEvent(byteLoaded, bytesTotal));
 	}
 
 	private void onSaving(String name) {
 		this.fireEvent(new SavingEvent(name));
 	}
 
 	/**
 	 * Adds this handler to the widget.
 	 * 
 	 * @param <H>
 	 *            the type of handler to add
 	 * @param type
 	 *            the event type
 	 * @param handler
 	 *            the handler
 	 * @return {@link HandlerRegistration} used to remove the handler
 	 */
 	public final <H extends EventHandler> HandlerRegistration addHandler(GwtEvent.Type<H> type, final H handler) {
 		return this.addHandler(handler, type);
 	}
 
 	public String getUploadURL() {
 		return uploadURL;
 	}
 
 	public void setUploadURL(String uploadURL) {
 		this.uploadURL = uploadURL;
 		this.form.setAction(this.uploadURL);
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 
 }
