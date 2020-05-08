 // Copyright (C) 2011 Associated Universities, Inc. Washington DC, USA.
 // 
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 // 
 // This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // General Public License for more details.
 // 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 // 
 // Correspondence concerning GBT software should be addressed as follows:
 //       GBT Operations
 //       National Radio Astronomy Observatory
 //       P. O. Box 2
 //       Green Bank, WV 24944-0002 USA
 
 package edu.nrao.dss.client.widget;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.WindowEvent;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Window;
 
 import edu.nrao.dss.client.Period;
 import edu.nrao.dss.client.Refresher;
 import edu.nrao.dss.client.Schedule;
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 
 public class PeriodSummaryDlg extends Dialog {
 	
 	private Period period;
 	private Dialog removeDialog;
 	private ArrayList<String> sess_handles;
 	
 	private Button delete = new Button();
 	private Button publish = new Button();
 	private Button change = new Button();
 	private Button shift = new Button();
 	
 	private Button removeApproval = new Button();
 	private  PeriodSummaryPanel periodPanel = new PeriodSummaryPanel(null);
 	
 	private Refresher parent;
 	
 
 	public PeriodSummaryDlg(Period period, ArrayList<String> sess_handles, Refresher parent) {
 		//super();
 		this.parent = parent;
 		this.period = period;
 		this.sess_handles = sess_handles;
         initLayout();
         initListeners();
 	}
 	
 	private void initLayout() {
 		
		setLayout(new FlowLayout());
 		
 
 		// Basic Dialog settings
 		String txt = "Summary for Period " + period.getHandle();
 		setHeading(txt);
 		setButtons(Dialog.CANCEL);
 
 		// make all the buttons run horizontally
 	    FormPanel buttonFp = new FormPanel();
 		buttonFp.setLayout(new RowLayout(Orientation.HORIZONTAL));
		buttonFp.setHeight(45);
 		buttonFp.setHeaderVisible(false);
 		buttonFp.setBodyBorder(false);
 
 		// Publish the Period?
 		publish.setToolTip("Click this button to publish this Period.");
		publish.setText("Publish Period");
		buttonFp.add(publish, new RowData(0.25, 1, new Margins(0, 4, 0, 4)));
 		
 		// Delete the Period?
 		delete.setToolTip("Click this button to delete this Period.");
 		delete.setText("Delete Period");
		buttonFp.add(delete, new RowData(0.25, 1, new Margins(0, 4, 0, 4)));
 	    
 		// Insert a Period?
 		change.setToolTip("Click this button to insert a period into the schedule, with the correct time accounting.");
 		change.setText("Insert Period");
		buttonFp.add(change, new RowData(0.25, 1, new Margins(0, 4, 0, 4)));
 	    
 	    // shift period boundary?
 		shift.setToolTip("Click this button to shift one of the boundaries of the period (start or end), with the correct time accounting.");
 		shift.setText("Shift Period Boundary");
		buttonFp.add(shift, new RowData(0.25, 1, new Margins(0, 4, 0, 4)));

	    add(buttonFp);
 	    
 		// display summary info
 	    periodPanel.setPeriod(period);
 		periodPanel.hidePeriodPicker();
 		add(periodPanel);
 
 		Button cancel = getButtonById(Dialog.CANCEL);
 		cancel.setText("Close");
 		cancel.setToolTip("Close this Dialog");
 
 		// for confirming deletion of period
 		removeDialog = new Dialog();
 		removeDialog.setHeading("Confirmation");
 		removeDialog.addText("Are you sure you want to delete this Period?");
 		removeDialog.setButtons(Dialog.YESNO);
 		removeDialog.setHideOnButtonClick(true);
 		removeApproval = removeDialog.getButtonById(Dialog.YES);
 
 		removeDialog.hide();
 		
 		setWidth(700);
 		setAutoHeight(true);
 	}	
 	
 	private void initListeners() {
 	    delete.addListener(Events.OnClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 	    		tryDeletePeriod();
 	    	}
 	    });
 	    publish.addListener(Events.OnClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 	    		publishPeriod();
 	    	}
 	    });	    
 	    shift.addListener(Events.OnClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 	    		ShiftPeriodBndDlg dlg = new ShiftPeriodBndDlg(period, (Schedule) parent); //period, sess_handles, sc);
 	    		hide();
 	    	}
 	    });	
 	    change.addListener(Events.OnClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 	    		GWT.log("Change Click", null);
 	    		ChangeScheduleDlg dlg = new ChangeScheduleDlg(period, sess_handles, (Schedule) parent);
 	    		hide();
 	    	}
 	    });		    
 		// listener for close w/ out saving changes confirmation
 		final Listener<MessageBoxEvent> cancelListener = new Listener<MessageBoxEvent>() {
             public void handleEvent(MessageBoxEvent ce) {
             	Button b = (Button) ce.getButtonClicked();
             	if (b.getItemId().compareTo("yes") == 0) {
             		// they really want to exit w/ out saving changes
             		hide();
             	}
             }
         };	
 		Button cancel = getButtonById(Dialog.CANCEL);        
 		cancel.addListener(Events.OnClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 				// if they have unsaved changes, double check that they really want to exit
 				if (periodPanel.hasChanged()) {
 					MessageBox.confirm("Period Summary", "You have unsaved changes.  Exit anyways?", cancelListener);
 				} else {
 					hide();
 				}
 			}
 		});		    
 		removeApproval.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				deletePeriod();
 			}
 		});	
 		
 	}
 	
 	public void setPeriod(Period period) {
 		this.period = period;
 	}
 	
 	// some of things in this dialog are only good for the context of
 	// scheduling, so hide them if we don't want them
 	public void notForScheduling() {
 		delete.setVisible(false);
 		shift.setVisible(false);
 		change.setVisible(false);
 	}
 	
 	private void publishPeriod() {
 		String url = "/scheduler/periods/publish/" + Integer.toString(period.getId());
 		GWT.log("publish period: " + url, null);
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		JSONRequest.post(url, keys,
 				new JSONCallbackAdapter() {
 					public void onSuccess(JSONObject json) {
 						if (parent != null) {
 							parent.refresh();
 						}
 						hide();
 					}
 				});		
 	}
 	
 	// A Period can be deleted if it's not in the scheduled state, or it's
 	// time accounting has been reconciled so that there is no Time Billed.
 	private final boolean periodCanBeDeleted(Period period) {
 		if (!period.isScheduled()) {
 			return true; // it's pending, who cares?
 		} else {
 			if (period.getBilled() == 0.0) {
 				// somebody must have taken care of the time accounting
 				return true;
 			} else {
 				// sorry, go back and fix the time accounting first
 				return false;
 			}
 		}
 	}
 
 	// Retrieves the latest value of the Period from the server, checks if it
 	// can be deleted, then asks for confirmation before zapping it.
 	private void tryDeletePeriod() {
 	    // get this period again - it's time accounting might have changed
         String periodUrl = "/scheduler/periods/UTC/" + Integer.toString(this.period.getId());
 	    JSONRequest.get(periodUrl, new JSONCallbackAdapter() {
             @Override
             public void onSuccess(JSONObject json) {
             	// JSON period -> JAVA period
              	period = Period.parseJSON(json.get("period").isObject());
              	
 	    		if (!periodCanBeDeleted(period)) {
 	    			// can't delete it yet!
     	    		Window.alert("This Scheduled Period must first have it's Time Accounting adjusted so that it's Time Billed is 0.0");
           		} else {
           			// confirm deletion
           		    removeDialog.show();	
     		    }
              	
             }
 	    });    
 	}
 	
 	// makes call to server for deletion, then updates calendar
 	private final void deletePeriod() {
 		String rootURL = "/scheduler/periods/UTC";
 		JSONRequest.delete(rootURL + "/" + this.period.getId(), 
 			new JSONCallbackAdapter() {
 				public void onSuccess(JSONObject json) {
 					if (parent != null) {
 					    parent.refresh();
 					}
 					hide();
 				}
 			});		
 	}
 	
 }
