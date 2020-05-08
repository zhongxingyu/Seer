 /* Copyright 2011 Massimo Gengarelli <gengarel@cs.unibo.it>
  * This file is part of Floz Configurator.
  * Floz Configurator is free software: you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or 
  * (at your option) any later version.
  *
  * Floz Configurator is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License 
  * along with Floz Configurator. If not, see http://www.gnu.org/licenses/.
  */
 
 package it.unibo.cs.v2.client;
 
 import it.unibo.cs.v2.servlets.CreateMachine;
 import it.unibo.cs.v2.servlets.CreateMachineAsync;
 import it.unibo.cs.v2.servlets.GetIsoList;
 import it.unibo.cs.v2.servlets.GetIsoListAsync;
 import it.unibo.cs.v2.shared.MachineInfo;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class NewMachineWizard extends HTMLPanel implements AsyncCallback<LinkedList<HashMap<String, String>>>, ClickHandler {
 	private final GetIsoListAsync getIsoListProxy = (GetIsoListAsync) GWT.create(GetIsoList.class);
 	private final CreateMachineAsync createMachineProxy = (CreateMachineAsync) GWT.create(CreateMachine.class);
 	
 	private final ListBox isoListBox = new ListBox();
 	private final Button submitButton = new Button("Create");
 	
 	private final DisclosurePanel description = new DisclosurePanel("Image Description");
 	private final HTML descriptionLabel = new HTML();
 	
 	private final HTML errorLabel = new HTML();
 	
 	private LinkedList<HashMap<String, String>> isos;
 	
 	/* stuff for the new machine */
 	private final TextBox name = new TextBox();
 	private final TextBox descriptionVM = new TextBox();
 	
 	/* buttons for the two disks */
 	private final String rb1group = "rb1group";
 	private final String rb2group = "rb2group";
 	
 	private final TextBox disk1name = new TextBox();
 	private final RadioButton rb12 = new RadioButton(rb1group, "2GB");
 	private final RadioButton rb14 = new RadioButton(rb1group, "4GB");
 	private final RadioButton rb18 = new RadioButton(rb1group, "8GB");
 	private final RadioButton rb116 = new RadioButton(rb1group, "16GB");
 	private final RadioButton[] rb1 = new RadioButton[] {rb12, rb14, rb18, rb116};
 	
 	private final TextBox disk2name = new TextBox();
 	private final CheckBox secondDisk = new CheckBox("Enable the second disk");
 	private final RadioButton rb22 = new RadioButton(rb2group, "2GB");
 	private final RadioButton rb24 = new RadioButton(rb2group, "4GB");
 	private final RadioButton rb28 = new RadioButton(rb2group, "8GB");
 	private final RadioButton rb216 = new RadioButton(rb2group, "16GB");
 	private final RadioButton[] rb2 = new RadioButton[] {rb22, rb24, rb28, rb216};
 	
 	/* networking stuff */
 	private final CheckBox virtuaCluster = new CheckBox("Connect to the VirtuaCluster");
 	private final CheckBox addNetworkIf = new CheckBox("Create a secondary network interface");
 	private final TextBox socketName = new TextBox();
 	private final TextBox secondMacAddr = new TextBox();
 
 	private boolean finished = false;
 	
 	public NewMachineWizard() {
 		super("<h2>New machine wizard</h2>");
 		getIsoListProxy.getIsoList(this);
 		submitButton.addClickHandler(this);
 		
 		description.setContent(descriptionLabel);
 		description.setAnimationEnabled(true);
 		
 		
 		/**
 		 * Show a description of the selected ISO.
 		 */
 		isoListBox.addChangeHandler(new ChangeHandler() {
 			
 			@Override
 			public void onChange(ChangeEvent event) {
 				String imageName = isoListBox.getValue(isoListBox.getSelectedIndex());
 				for (HashMap<String, String> h : isos) {
 					if (!h.get("name").equals(imageName))
 						continue;
 					else
 						descriptionLabel.setHTML("<b>" + h.get("name") + " " + h.get("version") + "</b><br />" + 
 								h.get("description") + "<br />" + 
 								"<a href=\"" + h.get("web") + "\">Visit the official website</a>" + "<br />" + 
 								"The file is located at: " + h.get("iso"));
 				}
 			}
 		});
 
 		
 		/***
 		 * Basic stuff (iso image, name and description)
 		 */
 		add(new HTML("<h3>Basic settings</h3>"));
 		
 		FlexTable table = new FlexTable();
 		table.setWidget(0, 0, new HTML("Select the installation disc for your VM: "));
 		table.setWidget(0, 1, isoListBox);
 		
 		add(table);
 		add(description);
 		
 		table = new FlexTable();
 		table.setWidget(0, 0, new HTML("Choose a name for your new VM: "));
 		table.setWidget(0, 1, name);
 		table.setWidget(1, 0, new HTML("Enter a short description of it: "));
 		table.setWidget(1, 1, descriptionVM);
 		
 		
 		add(table);
 		
 		/***
 		 * Storage (hard disks)
 		 */
 		add(new HTML("<h3>Storage settings</h3>"));
 		add(secondDisk);
 		
 		table = new FlexTable();
 
 		table.setWidget(0, 0, new HTML("Choose a name for the primary disk: "));
 		table.setWidget(0, 1, disk1name);
 		table.setWidget(1, 0, new HTML("Choose a size for the primary disk: "));
 		
 		HorizontalPanel tempHP = new HorizontalPanel();
 		tempHP.add(rb12);
 		tempHP.add(rb14);
 		tempHP.add(rb18);
 		tempHP.add(rb116);
 		table.setWidget(1, 1, tempHP);
 
 		table.setWidget(2, 0, new HTML("Choose a name for the secondary disk<span style=\"color: pink;\">*</span>: "));
 		table.setWidget(2, 1, disk2name);
 		table.setWidget(3, 0, new HTML("Choose a size for the secondary disk<span style=\"color: pink;\">*</span>: "));
 		
 		tempHP = new HorizontalPanel();
 		tempHP.add(rb22);
 		tempHP.add(rb24);
 		tempHP.add(rb28);
 		tempHP.add(rb216);
 		table.setWidget(3, 1, tempHP);
 		
 		add(table);
 		
 		secondDisk.setValue(false);
 		
 		/**
 		 * by default, the secondary disk is not enabled
 		 */
 		disk2name.setEnabled(false);
 
 		rb22.setEnabled(false);
 		rb24.setEnabled(false);
 		rb28.setEnabled(false);
 		rb216.setEnabled(false);
 
 		rb18.setValue(true);
 		rb28.setValue(true);
 		
 		secondDisk.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				disk2name.setEnabled(secondDisk.getValue());
 				rb22.setEnabled(secondDisk.getValue());
 				rb24.setEnabled(secondDisk.getValue());
 				rb28.setEnabled(secondDisk.getValue());
 				rb216.setEnabled(secondDisk.getValue());
 			}
 		});
 		
 		table = new FlexTable();
 		add(new HTML("<h3>Networking</h3>"));
 		table = new FlexTable();
 		
 		table.setWidget(0, 0, virtuaCluster);
 		table.setWidget(1, 0, addNetworkIf);
 		table.setWidget(2, 0, new HTML("Macaddress for the network interface<span style=\"color: red;\">*</span>: "));
 		table.setWidget(2, 1, secondMacAddr);
 		table.setWidget(3, 0, new HTML("Relative path for the new socket<span style=\"color: blue;\">*</span>: "));
 		table.setWidget(3, 1, socketName);
 		
 		
 		virtuaCluster.setValue(true);
 		addNetworkIf.setValue(false);
 		secondMacAddr.setEnabled(false);
 		socketName.setEnabled(false);
 		
 		addNetworkIf.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				secondMacAddr.setEnabled(addNetworkIf.getValue());
 				socketName.setEnabled(addNetworkIf.getValue());
 			}
 		});
 		
 		add(table);
 		add(new HTML("<h3>Notes</h3>"));
 		add(new HTML("<span style=\"color: red;\">* </span>Leave blank to use the QEMU's default one."));
 		add(new HTML("<span style=\"color: blue;\">* </span>Will be created in your home."));
 		add(new HTML("<span style=\"color: pink;\">* </span>Extension '.img' will be automatically added."));
 		
 		add(errorLabel);
 		add(submitButton);
 	}
 	
 	public void updateList() {
 		getIsoListProxy.getIsoList(this);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		DialogBox errorDialog = new DialogBox();
 		errorDialog.setAnimationEnabled(true);
 		errorDialog.setWidget(new HTML("An error occured while querying the RPC server, please refresh the page."));
 		errorDialog.show();
 	}
 
 	@Override
 	public void onSuccess(LinkedList<HashMap<String, String>> result) {
 		this.isos = result;
 		isoListBox.clear();
 		for (HashMap<String, String> h : result) 
 			isoListBox.addItem(h.get("name"));
 		
 		descriptionLabel.setHTML("Select an image to show its description!");
 	}
 
 	private void showSuccess(String msg) {
 		errorLabel.getElement().getStyle().setColor("green");
 		errorLabel.getElement().getStyle().setFontSize(1.4, Unit.EM);
 	
 		errorLabel.setHTML(msg);
 		errorLabel.setVisible(true);
 	}
 	
 	private void showError(String error) {
 		errorLabel.getElement().getStyle().setColor("red");
 		errorLabel.getElement().getStyle().setFontSize(1.4, Unit.EM);
 		
 		errorLabel.setHTML(error);
 		errorLabel.setVisible(true);
 	}
 	
 	private void hideError() {
 		errorLabel.setVisible(false);
 	}
 	
 	@Override
 	public void onClick(ClickEvent event) {
 		final MachineInfo machineInfo = new MachineInfo();
 		hideError();
 		
 		if (name.getText().equals("")) {
 			showError("Machine name can't be blank");
 			return;
 		}
 		
 		machineInfo.setName(name.getText());
 		machineInfo.setDescription(descriptionVM.getText());
 		
 		for (HashMap<String, String> hm : isos) {
 			if (hm.get("name").equals(isoListBox.getValue(isoListBox.getSelectedIndex()))) {
 				machineInfo.setIso(hm.get("iso"));
 				break;
 			}
 		}
 		
 		if (disk1name.getText().equals("")) {
 			showError("Disk name can't be blank");
 			return;
 		}
 		
 		machineInfo.setHda(disk1name.getText());
 		for (RadioButton b : rb1) {
 			if (!b.getValue())
 				continue;
 			machineInfo.setHdaSize(b.getText());
 		}
 		
 		if (disk2name.isEnabled() && disk2name.getText().equals((""))) {
 			showError("Disk name can't be blank");
 			return;
 		}
 		
 		machineInfo.setHdbEnabled(disk2name.isEnabled());
 		if (disk2name.isEnabled()) {
 			machineInfo.setHdb(disk2name.getText());
 			for (RadioButton b: rb2) {
 				if (!b.getValue())
 					continue;
 				machineInfo.setHdbSize(b.getText());
 			}
 		}
 		
 		machineInfo.setVirtuacluster(virtuaCluster.getValue());
 		machineInfo.setSecondNetwork(addNetworkIf.getValue());
 		
 		if (addNetworkIf.getValue() && socketName.getText().equals("")) {
 			showError("Socket name can't be blank");
 			return;
 		}
 		
 		machineInfo.setSocketPath(socketName.getText());
 		machineInfo.setMacAddress(secondMacAddr.getText());
 		
 		createMachineProxy.createMachine(machineInfo, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				clear();
 				add(errorLabel);
 				
 				if (result) {
 					showSuccess("Machine successfully created");
 					finished = true;
 				}
 				else
 					showError("Error while creating the machine. This is programmers' fault, not yours. " +
 							"Please reload the page and retry.");
 			}
 		});
 	}
 	
 	public boolean hasFinished() {
 		return finished;
 	}
 }
