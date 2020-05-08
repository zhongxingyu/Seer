 package fws_master;
 
 import java.awt.Font;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 
 
 @SuppressWarnings("unused")
 public class ViewStation {
 	private Shell shell;
 	private StationController station_controller;
 	private ParameterController parameter_controller;
 	private Station selected_station;
 	private Parameter selected_parameter;
 	
 
 	private List station_list,parameter_list;
 	private Text nameText,ipText,pollingText,addressText,valueText,plotText;
 	private Button boxActive;
 
 	
 	public ViewStation(Shell shell,StationController sc,ParameterController p) {
 		this.shell = shell;
 		this.station_controller = sc;
 		this.parameter_controller = p;
 		this.InitView();
 		this.enableParams(false);
 		this.select_first();
 		
 	}
 	private void enableText(boolean enabled) {
 		this.nameText.setEnabled(enabled);
 		this.ipText.setEnabled(enabled);
 		this.pollingText.setEnabled(enabled);
 	}
 	private void enableParams(boolean enabled) {
 		this.addressText.setEnabled(enabled);
 		this.valueText.setEnabled(enabled);
 		this.plotText.setEnabled(enabled);
 		this.boxActive.setEnabled(enabled);
 	}
 	private void select_first() {
 		if(this.station_list.getItemCount() >0) {
 			this.station_list.select(0);
 			this.list_selected();
 		}
 		else {
 			this.enableText(false);
 		}
 	}
 	private void InitView() {
 		GridLayout gridLayout = new GridLayout(3,false);
 		shell.setLayout(gridLayout);
 
 		station_list = new List(this.shell,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);	
 		GridData gridData = new GridData();
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.verticalSpan = 5;
 		gridData.widthHint = 150;
 		station_list.setLayoutData(gridData);
 		station_list.addListener(SWT.Selection, new ListListener());
 		loadList();
 		
 		
 		Label nameLabel = new Label(shell, SWT.NONE);
 		nameLabel.setText("Name:");
 		
 		nameText = new Text(shell, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		nameText.setLayoutData(gridData);
 		nameText.setText("");
 		
 		Label ipLabel = new Label(shell, SWT.NONE);
 		ipLabel.setText("IP:");
 		
 		ipText = new Text(shell, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		ipText.setLayoutData(gridData);
 		ipText.setText("");
 		
 		Label pollingLabel = new Label(shell, SWT.NONE);
 		pollingLabel.setText("Intervall:");
 		
 		pollingText = new Text(shell, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		pollingText.setLayoutData(gridData);
 		pollingText.setText("");
 		
 		Button saveButton = new Button(shell, SWT.PUSH);
 		saveButton.setText("Speichern");
 		saveButton.addSelectionListener(new ButtonListener());
 		gridData = new GridData();
 		//gridData.horizontalSpan=2;
 		saveButton.setLayoutData(gridData);
 		
 		Button uploadButton = new Button(shell, SWT.PUSH);
 		uploadButton.setText("Upload");
 		uploadButton.setToolTipText("Die aktuellen Parameterwerte an das Device hochladen");
 		uploadButton.addSelectionListener(new ButtonListener());
 		gridData = new GridData();
 		uploadButton.setLayoutData(gridData);
 		
 		Composite params = new Composite(shell, SWT.BORDER);
 		
 		params.setLayout(new GridLayout(3,false));
 		gridData = new GridData();
 		gridData.grabExcessVerticalSpace = true;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		params.setLayoutData(gridData);
 		
 		
 		
 		this.parameter_list = new List(params,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
 		gridData = new GridData();
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.verticalSpan = 6;
 		gridData.widthHint = 150;
 		parameter_list.setLayoutData(gridData);
 		parameter_list.addListener(SWT.Selection, new PListListener());
 		load_params_list();
 		
 		Label parmLabel = new Label(params, SWT.NONE);
 		parmLabel.setText("Parameter Zuordung");
 		
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan =2;
 		parmLabel.setLayoutData(gridData);
 		
 		Label addressLabel = new Label(params, SWT.NONE);
 		addressLabel.setText("Adresse:");
 		
 		addressText = new Text(params, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		addressText.setLayoutData(gridData);
 		addressText.setText("");
 		
 		
 		
 		Label valueLabel = new Label(params, SWT.NONE);
 		valueLabel.setText("Wert:");
 		
 		valueText = new Text(params, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		valueText.setLayoutData(gridData);
 		valueText.setText("");
 		
 		
 		Label plotLabel = new Label(params, SWT.NONE);
 		plotLabel.setText("PlotConfig:");
 		
 		plotText = new Text(params, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		plotText.setLayoutData(gridData);
 		plotText.setText("");
 		
 		this.boxActive = new Button(params,SWT.CHECK);
 		this.boxActive.setText("Aktiv");
 		gridData = new GridData();
 		gridData.horizontalSpan=2;
 		boxActive.setLayoutData(gridData);
 		
 		Button saveButton2 = new Button(params, SWT.PUSH);
 		saveButton2.setText("Zuordnen");
 		saveButton2.addSelectionListener(new ButtonListener());
 		gridData = new GridData();
 		gridData.horizontalSpan=2;
 		saveButton2.setLayoutData(gridData);
 	}
 
 	private void load_params_list() {
 		this.parameter_list.removeAll();
 		for(Parameter p:this.parameter_controller.getParameters()) {
 			this.parameter_list.add(p.getName());
 		}
 		
 	}
 
 	private void loadList() {
 		this.station_list.removeAll();
 		for (Station s:this.station_controller.getStations()) {
 			station_list.add(s.getStationName());
 		}
 	}
 	
 	public void list_selected() {
 		try {
 			this.selected_station = this.station_controller.findStation((this.station_list.getSelection()[0]));
 			this.nameText.setText(selected_station.getStationName());
 			this.ipText.setText(selected_station.getIpAddress());
 			this.pollingText.setText(""+selected_station.getPollingInterval());
 			this.enableText(true);
 			this.enableParams(false);
 			//this.load_params_list();
 			this.parameter_list.setEnabled(true);
 			this.parameter_list.select(0);
 			this.plist_selected();
 			
 		} catch (Exception e) 
 		{
 			this.nameText.setText("");
 			this.ipText.setText("");
 			this.pollingText.setText("");
 			this.selected_station = null;
 			this.parameter_list.setEnabled(false);
 			this.enableParams(false);
 			this.enableText(false);
 		}
 	}
 	
 	private void saveClicked() {
 		if (this.selected_station != null) {
 			String name,ip,polling;
 			name = this.nameText.getText();
 			ip = this.ipText.getText();
 			polling = this.pollingText.getText();
 			int tmp_p;
 			if (name.equals("") || ip.equals("") || polling.equals("")) {
 				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
 				messageBox.setMessage("Es müssen alle Felder ausgefüllt sein!");
 				messageBox.open();
 				return;
 			}
 			
 			try {
 				tmp_p = Integer.parseInt(polling);
 			} catch (Exception e) {
 				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
 				messageBox.setMessage("Keine gültige Zahl bei Polling Intervall eingegeben!");
 				messageBox.open();
 				return;
 			}
 			
 			if (this.station_controller.findStation(name) != null && this.station_controller.findStation(name)!=this.selected_station) {
 				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
 				messageBox.setMessage("Der Name muss eindeutig sein");
 				messageBox.open();
 				return;
 			}
 			this.selected_station.setStationName(name);
 			this.selected_station.setPollingInterval(tmp_p);
 			
 			this.selected_station.changeIPAddress(ip);
 			int tmp_sel = this.station_list.getSelectionIndex();
 			this.loadList();
 			this.station_list.setSelection(tmp_sel);
 			this.list_selected();
 		}
 	}
 	
 	private void bindClicked() {
 		Binding current_binding = null;
 		for(Binding b:this.selected_parameter.getStationsBindings()) {
 			if (b.getStation() == this.selected_station) {
 				current_binding = b;
 				break;
 			}
 		}
 		if (current_binding == null)
 			return;
 		
 		int address = 0;
 		try {
 
 			address = Integer.parseInt(this.addressText.getText());
 		} catch (Exception ex) {return;}
 		current_binding.setAddress(address);
 		current_binding.setActive(this.boxActive.getSelection());
 		if (this.selected_parameter instanceof ConfigParameter) {
 			int value;
 			try {
 				value = Integer.parseInt(this.valueText.getText());
 	
 				
 			} catch (Exception ex) {return;}
 			
 			StationConfigBinding cfg = (StationConfigBinding)current_binding;
 			cfg.setTransfered(false);
 			
 			cfg.setValue(value);
 			
 		} else {
 			
 			StationInputBinding ip = (StationInputBinding)current_binding;
 			
 			String plotConfig = this.plotText.getText();
 			
 			if (!ip.setPlotConfig(plotConfig)) {
 				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
 				messageBox.setMessage("Ungültige Plot Konfiguration: Gültige Werte: d24;...!");
 				messageBox.open();
 				return;
 			}
 			
 		}
 		
 	}
 	public void plist_selected() {
 		try {
 			this.selected_parameter = this.parameter_controller.findParameter((this.parameter_list.getSelection()[0]));
 			
 			this.enableParams(true);
 			Binding current_binding = null;
 			for(Binding b:this.selected_parameter.getStationsBindings()) {
 				if (b.getStation() == this.selected_station) {
 					current_binding = b;
 					break;
 				}
 			}
 			if (current_binding==null) {
 				if (this.selected_parameter instanceof ConfigParameter) {
 					current_binding = new StationConfigBinding(this.selected_station,(ConfigParameter)this.selected_parameter,-1,false,false);
 				}
 				else {
					current_binding = new StationInputBinding(this.selected_station,(InputParameter)this.selected_parameter,-1,"h24",false);
 				}
 			}
 			
 			this.addressText.setText(""+current_binding.getAddress());
 			this.boxActive.setSelection(current_binding.isActive());
 			
 			if (this.selected_parameter instanceof ConfigParameter) {
 				this.plotText.setEnabled(false);
 				this.plotText.setText("");
 				StationConfigBinding cfg = (StationConfigBinding)current_binding;
 				this.valueText.setText(""+cfg.getValue());	
 				
 			} else {
 				this.valueText.setEnabled(false);
 				this.valueText.setText("");
 				StationInputBinding ip = (StationInputBinding)current_binding;
 				this.plotText.setText(ip.getPlotConfig());
 				
 			}
 		} catch (Exception e) 
 		{
 			this.selected_parameter = null;
 			this.enableParams(false);
 		}
 		
 	}
 	
 	public void uploadClicked() {
 		this.selected_station.uploadParamsConfig();
 	}
 	
 	
 	class ListListener implements Listener{
 		public void handleEvent(Event e) {
 			list_selected();
 		}
 	}
 	
 	class PListListener implements Listener{
 		public void handleEvent(Event e) {
 			plist_selected();
 		}
 	}
 	
 	class ButtonListener extends SelectionAdapter {
 		public void widgetSelected(SelectionEvent event) {
 			if (((Button) event.widget).getText().equals("Speichern")) {
 				saveClicked();
 			}
 			if (((Button) event.widget).getText().equals("Zuordnen")) {
 				bindClicked();
 			}
 			if (((Button) event.widget).getText().equals("Upload")) {
 				uploadClicked();
 			}
 		}
 
 		
 	}
 
 	
 
 	
 }
