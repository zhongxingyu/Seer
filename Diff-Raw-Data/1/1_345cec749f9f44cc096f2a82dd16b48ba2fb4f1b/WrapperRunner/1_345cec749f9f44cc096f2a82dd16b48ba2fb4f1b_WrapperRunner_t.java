 /* * Copyright 2012 Oregon State University.
  * All Rights Reserved. 
  *  
  * Permission to use, copy, modify, and distribute this software and its 
  * documentation for educational, research and non-profit purposes, without fee, 
  * and without a written agreement is hereby granted, provided that the above 
  * copyright notice, this paragraph and the following three paragraphs appear in 
  * all copies. 
  *
  * Permission to incorporate this software into commercial products may be 
  * obtained by contacting OREGON STATE UNIVERSITY Office for 
  * Commercialization and Corporate Development.
  *
  * This software program and documentation are copyrighted by OREGON STATE
  * UNIVERSITY. The software program and documentation are supplied "as is", 
  * without any accompanying services from the University. The University does 
  * not warrant that the operation of the program will be uninterrupted or errorfree. 
  * The end-user understands that the program was developed for research 
  * purposes and is advised not to rely exclusively on the program for any reason. 
  *
  * IN NO EVENT SHALL OREGON STATE UNIVERSITY BE LIABLE TO ANY PARTY 
  * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
  * DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS 
  * SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE OREGON STATE  
  * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
  * OREGON STATE UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE AND ANY 
  * STATUTORY WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE PROVIDED 
  * HEREUNDER IS ON AN "AS IS" BASIS, AND OREGON STATE UNIVERSITY HAS 
  * NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
  * ENHANCEMENTS, OR MODIFICATIONS. 
  * 
  */
 package cgrb.eta.client.tabs;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import cgrb.eta.client.ETA;
 import cgrb.eta.client.FileSelector;
 import cgrb.eta.client.ItemSelector;
 import cgrb.eta.client.MyAsyncCallback;
 import cgrb.eta.client.WrapperService;
 import cgrb.eta.client.WrapperServiceAsync;
 import cgrb.eta.client.button.Button;
 import cgrb.eta.client.button.CheckButton;
 import cgrb.eta.client.button.ImgButton;
 import cgrb.eta.client.button.LabelButton;
 import cgrb.eta.client.button.LargeSeprator;
 import cgrb.eta.client.button.MenuButton;
 import cgrb.eta.client.button.SeperatorButton;
 import cgrb.eta.client.button.Seprator;
 import cgrb.eta.client.button.SimpleButton;
 import cgrb.eta.client.button.SimpleLabel;
 import cgrb.eta.client.button.ValueListener;
 import cgrb.eta.client.tabset.ETATab;
 import cgrb.eta.client.window.FileBrowser;
 import cgrb.eta.client.window.MultipleUserSelect;
 import cgrb.eta.client.window.SC;
 import cgrb.eta.client.wrapperrunner.Inputs;
 import cgrb.eta.client.wrapperrunner.JobOptions;
 import cgrb.eta.shared.etatype.Cluster;
 import cgrb.eta.shared.etatype.File;
 import cgrb.eta.shared.etatype.Job;
 import cgrb.eta.shared.etatype.User;
 import cgrb.eta.shared.wrapper.Input;
 import cgrb.eta.shared.wrapper.Output;
 import cgrb.eta.shared.wrapper.Wrapper;
 
 /**
  * 
  * The class which literally runs our wrappers.
  * 
  * It is also the information viewed when you open the runwrappers button from the left. It displays information about the selected wrapper and loads them into our contentpane. It also is the file which communicates with our server. All client->server wrapper computation runs through here.
  * 
  * @see WrapperServiceAsync.java
  * 
  * @author Alexander Boyd
  * 
  */
 public class WrapperRunner extends ETATab implements ValueChangeHandler<Wrapper> {
 
 	private HorizontalPanel bar;
 	private VerticalPanel pane;
 	private JobOptions options;
 	private Inputs inputs;
 	private Wrapper wrapper;
 	private MultipleUserSelect usersNot;
 	private SimpleLabel workingFolder;
 	private int wrapperId;
 	private LabelButton jobName;
 	private CheckButton saveStd;
 	private SimpleLabel commandRun;
 	ListBox clustersBox;
 	private int waitingFor = 0;
 	private FlowPanel jobOptions;
 
 	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
 	private CheckButton notifyMe;
 
 	public WrapperRunner(int wrapper) {
 		super("Wrapper Runner");
 		wrapperId = wrapper;
 		pane = new VerticalPanel();
 		bar = new HorizontalPanel();
 		setPane(pane);
 		jobOptions = new FlowPanel();
 		this.setAnimatedPanel(jobOptions);
 		if (wrapper == 0)
 			return;
 		wrapperService.getWrapperFromId(wrapper, new MyAsyncCallback<Wrapper>() {
 			@Override
 			public void success(Wrapper result) {
 				setup(result);
 			}
 		});
 	}
 
 	public WrapperRunner(Wrapper wrapper) {
 		super("Wrapper Runner");
 		wrapperId = wrapper.getId();
 		pane = new VerticalPanel();
 		bar = new HorizontalPanel();
 		setPane(pane);
 		this.setAnimatedPanel(jobOptions);
 		setup(wrapper);
 	}
 
 	public WrapperRunner(Job job) {
 		super("Wrapper Runner");
 		wrapperId = job.getWrapper().getId();
 		pane = new VerticalPanel();
 		bar = new HorizontalPanel();
 		setPane(pane);
 		setup(job.getWrapper());
 		this.setAnimatedPanel(jobOptions);
 		workingFolder.setText(job.getWorkingDir());
 	}
 
 	public void setup(Wrapper wrapper) {
 		this.wrapper = wrapper;
 		bar.clear();
 		pane.clear();
 		options = new JobOptions();
		jobName = new LabelButton(wrapper.getName());
 		// make the bar
 		usersNot = new MultipleUserSelect();
 		Label descTitle = new Label("Description:");
 		Label descText = new Label(wrapper.getDescription());
 		descTitle.setStyleName("simple-label-desc");
 		descText.setStyleName("simple-label-desc");
 
 		// setup hidden options
 		FlowPanel description = new FlowPanel();
 		description.setStyleName("animated-options-floats");
 		FlowPanel folder = new FlowPanel();
 		folder.setStyleName("animated-options-floats");
 		FlowPanel wrapOptions = new FlowPanel();
 		wrapOptions.setStyleName("animated-options-floats");
 		FlowPanel notifications = new FlowPanel();
 		notifications.setStyleName("animated-options-floats");
 
 		// description pane
 		description.add(new SimpleLabel("Program: " + wrapper.getProgram()));
 		Button change = new Button("Change Title").setClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				SC.ask("What do you want to name this job?", new ValueListener<String>() {
 					public void returned(String ret) {
 						jobName.setText(ret);
 					}
 				});
 			}
 		});
 		change.setStyleDependentName("-animate-button", true);
 
 		description.add(change);
 		description.add(descTitle);
 		description.add(descText);
 
 		// wrapperOptions pane
 		Button sge = new Button("SGE Options").setClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				options.show();
 			}
 		});
 		sge.setStyleDependentName("-animate-button", true);
 		wrapOptions.add(sge);
 		saveStd = new CheckButton("Save STD");
 		saveStd.setStyleDependentName("-animate-check", true);
 		wrapOptions.add(saveStd);
 
 		// Folder pane
 		SimpleLabel workingDir = new SimpleLabel("Working Dir");
 		folder.add(workingDir);
 		workingFolder = new SimpleLabel((FileBrowser.lastFolder) != "" ? FileBrowser.lastFolder : "No working folder.");
 		folder.add(workingFolder);
 		Button changebutt = new Button("Change").setClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				new FileSelector(new ItemSelector() {
 					public void itemSelected(String[] items) {
 						if (items != null && items.length > 0) {
 							workingFolder.setText(items[0]);
 						}
 					}
 				}, FileBrowser.FOLDER_SELECT);
 			}
 		});
 		changebutt.setStyleDependentName("-animate-button", true);
 		folder.add(changebutt);
 
 		jobOptions.add(description);
 		jobOptions.add(new LargeSeprator());
 		jobOptions.add(notifications);
 		jobOptions.add(wrapOptions);
 		jobOptions.add(new LargeSeprator());
 		jobOptions.add(folder);
 		jobOptions.add(new LargeSeprator());
 		jobOptions.add(notifications);
 		jobOptions.add(new LargeSeprator());
 
 		// notifypane
 		SimpleLabel notifyMenu = new SimpleLabel("Notifications");
 		notifyMe = new CheckButton("Notify me");
 		notifyMe.setStyleDependentName("-animate-button", true);
 		notifications.add(notifyMenu);
 		notifications.add(notifyMe);
 
 		Button noteothers = new Button("Notify Others").setClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				SC.ask("Select the users you want to notify", usersNot, null);
 			}
 		});
 		noteothers.setStyleDependentName("-animate-button", true);
 		notifications.add(noteothers);
 
 		bar.add(new SimpleLabel("Wrapper created by: " + wrapper.getCreator()).setColor("white").setFontSize(10));
 		bar.add(new Seprator());
 
 		// the panels
 		HorizontalPanel top = new HorizontalPanel();
 		top.setStyleName("wr-top");
 		SimpleLabel nameHeader = new SimpleLabel("Name");
 		SimpleLabel descHeader = new SimpleLabel("Description");
 		SimpleLabel value = new SimpleLabel("Value");
 		FlowPanel middle = new FlowPanel();
 		top.add(nameHeader);
 		top.add(descHeader);
 		top.add(value);
 		top.setWidth("100%");
 		top.setCellWidth(nameHeader, "150px");
 		top.setCellWidth(value, "250px");
 
 		inputs = new Inputs(wrapper);
 
 		middle.add(inputs);
 		middle.setStyleName("wr-middle");
 		middle.setHeight("100%");
 		middle.getElement().getStyle().setOverflowY(Overflow.AUTO);
 		top.setHeight("20px");
 		pane.add(top);
 		pane.setCellHeight(top, "20px");
 		pane.add(middle);
 		pane.setCellHeight(middle, "100%");
 		HorizontalPanel bottom = new HorizontalPanel();
 		SimpleLabel temp = new SimpleLabel("Command being ran:");
 		bottom.add(temp);
 		commandRun = new SimpleLabel("");
 		bottom.add(commandRun.setSelectable());
 		SimpleButton submit = new SimpleButton("Submit").addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				submit();
 			}
 		});
 
 		bottom.setWidth("100%");
 		bottom.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_RIGHT);
 		pane.add(bottom);
 
 		Vector<Cluster> clusters = ETA.getInstance().getUser().getClusters();
 		if (clusters.size() > 0) {
 			clustersBox = new ListBox();
 			clustersBox.addItem("", "");
 			for (Cluster cluster : clusters) {
 				clustersBox.addItem(cluster.getCompany(), "" + cluster.getId());
 			}
 			clustersBox.setStyleName("eta-input");
 			bottom.add(clustersBox);
 			bottom.setCellHorizontalAlignment(clustersBox, HasHorizontalAlignment.ALIGN_RIGHT);
 			bottom.setCellWidth(clustersBox, "120px");
 
 		}
 		bottom.add(submit);
 		bottom.setCellWidth(submit, "90px");
 		bottom.setCellWidth(temp, "120px");
 		bottom.setStyleName("wr-bottom");
 		pane.setCellHeight(bottom, "30px");
 		pane.setHeight("100%");
 		inputs.setHandler(this);
 		onValueChange(null);
 	}
 
 	@Override
 	public Widget getBar() {
 		return bar;
 	}
 
 	@Override
 	public String getId() {
 		return "wr#" + wrapperId;
 	}
 
 	private void submit() {
 		Vector<Input> inputs = wrapper.getInputs();
 		for (Input input : inputs) {
 			if (input.isRequired() && (input.getValue() == null || input.getValue().equals(""))) {
 				SC.alert("Can't submit", "Sorry all required inputs must have a value to submit this job.");
 				return;
 			}
 		}
 		Job job = new Job();
 		job.setWrapper(wrapper);
 		job.setName(jobName.getText());
 		job.setWorkingDir(workingFolder.getTitle());
 		job.setUserId(ETA.getInstance().getUser().getId());
 		job.setSpecs("");
 		job.setStatus("Submitted");
 		job.setSaveStd(saveStd.getValue());
 		if (clustersBox != null) {
 			int index = clustersBox.getSelectedIndex();
 			if (index > 0) {
 				job.setGlobalCluster(Integer.parseInt(clustersBox.getValue(index)));
 			}
 		}
 		job.setWaitingFor(waitingFor);
 		wrapperService.runJob(job, new MyAsyncCallback<Integer>() {
 			@Override
 			public void success(Integer result) {
 				if (result > 0) {
 					closeMe();
 					ETA.getInstance().addTab("jv");
 					setupNotifications(result);
 				} else {
 					SC.alert("Error", "Sorry for some reason this job can't be submitted.");
 				}
 			}
 		});
 	}
 
 	private void setupNotifications(int job) {
 		if (notifyMe.getValue()) {
 			wrapperService.addNotification(ETA.getInstance().getUser().getId(), job, new MyAsyncCallback<Void>() {
 				@Override
 				public void success(Void result) {
 				}
 			});
 		}
 		Vector<User> users = usersNot.getUsers();
 		for (User user : users) {
 			wrapperService.addNotification(user.getId(), job, new MyAsyncCallback<Void>() {
 				@Override
 				public void success(Void result) {
 				}
 			});
 		}
 	}
 
 	private void closeMe() {
 		ETA.getInstance().removeTab(this);
 
 	}
 
 	public void onValueChange(ValueChangeEvent<Wrapper> event) {
 		commandRun.setText(wrapper.getCMD());
 	}
 
 	public void matchInputs(Job job) {
 		Vector<File> outputFiles = job.getOutputFiles();
 		Vector<Output> outputs = job.getWrapper().getOutputs();
 		HashMap<String, File> map = new HashMap<String, File>();
 		for (int i = 0; i < outputs.size(); i++) {
 			map.put(outputs.get(i).getType(), outputFiles.get(i + 2));
 		}
 
 		for (Input in : wrapper.getInputs()) {
 			if (in.getType().startsWith("File:")) {
 				if (map.containsKey(in.getType().replaceAll("File:", ""))) {
 					in.setDefaultValue(map.get(in.getType().replaceAll("File:", "")).getPath());
 				}
 			}
 		}
 		setup(wrapper);
 		if (!job.getStatus().equals("Finished")) {
 			waitingFor = job.getId();
 		}
 	}
 
 }
