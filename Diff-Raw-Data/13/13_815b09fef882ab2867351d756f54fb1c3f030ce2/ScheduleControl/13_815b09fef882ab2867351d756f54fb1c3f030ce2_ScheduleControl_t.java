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
 
 package edu.nrao.dss.client;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.core.El;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 import edu.nrao.dss.client.util.TimeUtils;
 import edu.nrao.dss.client.widget.EmailDialogBox;
 import edu.nrao.dss.client.widget.FactorsDlg;
 
 public class ScheduleControl extends FormPanel {
 	
 	private Schedule schedule;
 	//private LabelField scheduleAverage, currentAverage, unscheduledTime;
 	private boolean schedulePressed;
 	int dataSize;
 	private NumberFormat scoreFormat = NumberFormat.getFormat("0.00");
 	private String origBackground;
 	private String origBackgroundColor;
 	
 	public FactorsDlg factorsDlg;
 	
 	private Button scheduleButton;
 	private Button emailButton;
 	private Button publishButton;
 	private Button restoreScheduleBtn;
 	private Button factorsButton;
 	private Dialog deletePendingDialog;
 	
 	public ScheduleControl(Schedule sched) {
 		schedule = sched;
 		initLayout();
 		initListeners();
 	}
 	
 	// Fails on DST boundaries! Story: https://www.pivotaltracker.com/story/show/14225261
 	public void setScheduleSummary(List<BaseModelData> data) {
 		if (!schedulePressed) {
 			dataSize = data.size();
 		}
 		if (data.size() <= 0) {
 			return;
 		}
 
 		// find the average score and gaps in the schedule
 		double[] results = analyzeSchedule(data);
 		double currentAverageValue = results[0];
 		double total_empty = results[1];
 		
 		String heading = "Schedule Control (";
 		if (schedulePressed && dataSize != data.size()) {
 			//scheduleAverage.setValue("Schedule Average Score: " + scoreFormat.format(currentAverageValue));
 			heading += "Schedule Average Score: " + scoreFormat.format(currentAverageValue) + " ";
 			schedulePressed = false;
 			dataSize = data.size();
 		}
 		//currentAverage.setValue("Current Average Score: " + scoreFormat.format(currentAverageValue));
 		//unscheduledTime.setValue("Unscheduled Time: " + TimeUtils.min2sex((int)total_empty));
 		heading += "Current Average Score: " + scoreFormat.format(currentAverageValue);
 		heading += " Unscheduled Time: " + TimeUtils.min2sex((int)total_empty) + ")";
 		setHeading(heading);
 		// do we need to highlight red?
 		checkForDST();
 	}
 
 	// find the average score of the schedule, and any gaps
 	public double[] analyzeSchedule(List<BaseModelData> data) {
 		// Note: time computation done in minutes
 		long msecPerMinute = 60*1000;
 		double total_scheduled = 0.0;
 		long total_empty = 0;
 		double total_score = 0.0;
 		
 		BaseModelData init = data.get(0);
 		Date t = TimeUtils.toDate(init);
 		long   end      = t.getTime()/msecPerMinute;
 		double dur      = 0.0; //init.get("duration");
 		long   duration = 0; //Math.round(60.*dur);
 		long   start    = 0;
 		double score    = 0.0;
 		
 		for (BaseModelData datum : data) {
 			t = TimeUtils.toDate(datum);
 			start = t.getTime()/msecPerMinute;
 			Object value = datum.get("duration");
 			// This is needed because newly entered values from the user are of type
 			// String, but only become Double upon being returned from the server
 			if (value.getClass() == String.class) {
 				dur = Double.valueOf(value.toString());
 			} else {
 				dur = datum.get("duration");
 			}
 			duration = Math.round(60.*dur);
 			score = datum.get("cscore");
 			total_scheduled += duration;
 			total_score += duration*score;
 			total_empty += start - end;
 			end = start + duration;
 		}
 		double currentAverageValue = total_score/total_scheduled;		
 		
 		// wrap up the results
 		double[] results = new double[2];
 		results[0] = currentAverageValue;
 		results[1] = total_empty;
 	    return results;	
 	}
 
 
 	// if we're displaying a DST boundary, make sure we mark this header so that
 	// users know not to trust the calculations.
 	private void checkForDST() {
 		El e = el();
 		if (e != null) {
 			El child = e.firstChild();
 			if (child != null & isRendered()) {
 				if (origBackground == null) {
 					origBackground = child.getStyleAttribute("background");
 					origBackgroundColor = child.getStyleAttribute("background-color");
 				}
 				if (schedule.hasDSTBoundary()) {
 					// warn the user of DST!
 					setHeading(getHeading() + " DST!");
 					child.setStyleAttribute("background", "none");
 					child.setStyleAttribute("background-color", "red");
 				} else {
 					// make sure we reset the original appearence.
 					child.setStyleAttribute("background", origBackground);
 					child.setStyleAttribute("background-color", origBackgroundColor);
 					
 				}
 
 			}
 		}
 //		}
 		
 	}
 	
 	private void initLayout() {
 		setHeading("Schedule Control");
 		setBorders(true);
 		
 		String col1Width = "50px";
 		String col2Width = "100px";
 		String labelFontSize = "11";
 		
 		TableLayout tb = new TableLayout(2);
 		tb.setWidth("100%");
 		tb.setBorder(0);
 		setLayout(tb);
 		
         final FormPanel left = new FormPanel();
 		left.setHeaderVisible(false);
 		left.setBorders(false);
 		left.setLayout(new RowLayout(Orientation.HORIZONTAL));
 		left.setSize(350, 50);
 		
 		// Auto schedules the current calendar
 		scheduleButton = new Button("Schedule");
 		schedulePressed = false;
 		scheduleButton.setToolTip("Generate a schedule for free periods over the specified calendar range");
 		left.add(scheduleButton, new RowData(-1, -1, new Margins(0, 4, 0, 4)));
 		
 		// deletes all pending periods currently displayed (state moved from pending to deleted)
 		restoreScheduleBtn = new Button("Restore Schedule");
 		restoreScheduleBtn.setToolTip("Deletes all the currently visible Open Periods and non-default Windowed Periods in the Pending (P) state; then it restores any Elective and Windowed Periods in the Deleted (D) state.");
 		left.add(restoreScheduleBtn, new RowData(-1, -1, new Margins(0, 4, 0, 4)));		
 		
 		deletePendingDialog = new Dialog();
 		deletePendingDialog.setHeading("Confirmation");
 		deletePendingDialog.addText("Are you sure you want to delete pending periods?");
 		deletePendingDialog.setButtons(Dialog.YESNO);
 		deletePendingDialog.setHideOnButtonClick(true);
 		deletePendingDialog.hide();
 		
 		// Factors
 		factorsButton = new Button("Factors");
 		factorsButton.setToolTip("Provides access to individual score factors for selected session and time range");
 		factorsDlg = new FactorsDlg(schedule);
 		factorsDlg.hide();
 		left.add(factorsButton, new RowData(-1, -1, new Margins(0, 4, 0, 4)));
 		
 		// publishes all periods currently displayed (state moved from pending to scheduled)
 		publishButton = new Button("Publish");
 		publishButton.setToolTip("Publishes all the currently visible Periods: state is moved from Pending (P) to Scheduled (S) and become visible to Observer.");
 		left.add(publishButton, new RowData(-1, -1, new Margins(0, 4, 0, 4)));
 		
 		emailButton = new Button("Email");
 		emailButton.setToolTip("Emails a schedule to staff and observers starting now and covering the next two days");
 		left.add(emailButton, new RowData(-1, -1, new Margins(0, 4, 0, 4)));
 		        
 		TableData tdLeft = new TableData();
 		tdLeft.setVerticalAlign(VerticalAlignment.TOP);
 		tdLeft.setWidth(col2Width);
 		
         add(left, tdLeft);
 	}
 	
 	private HashMap<String, Object> getTimeRange() {
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(schedule.startCalendarDay) + " 00:00:00";
 
 		keys.put("start", startStr);
 		keys.put("duration", schedule.numCalendarDays);
 		keys.put("tz", schedule.timezone);
 		return keys;
 	}
 	
 	private void initListeners() {
 		
 		scheduleButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				schedulePressed = true;
 				HashMap<String, Object> keys = getTimeRange();
 				String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(schedule.startCalendarDay) + " 00:00:00";
 	    		Integer numScheduleDays = schedule.numCalendarDays < 2 ? 1 : (schedule.numCalendarDays -1); 
 				String msg = "Scheduling from " + startStr + " (" + schedule.timezone + ")" + " until " + numScheduleDays.toString() + " days later at 8:00 (ET).";
 				final MessageBox box = MessageBox.wait("Calling Scheduling Algorithm", msg, "Be Patient ...");
 				JSONRequest.post("/runscheduler", keys,
 						new JSONCallbackAdapter() {
 							public void onSuccess(JSONObject json) {
 								schedule.updateCalendar();
 								box.close();
 							}
							public void onError(String error, JSONObject json) {
								box.close();
								super.onError(error, json);
							}							
 						});
 			}
 		});
 		
 		emailButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				String msg = "Generating scheduling email for observations over the next two days";
 				final MessageBox box = MessageBox.wait("Getting Email Text", msg, "Be Patient ...");
 				
 				// Must set keys here somehow to transmit proper time range.  What is the time range?
 				HashMap<String, Object> keys = getTimeRange();
 				
 				JSONRequest.get("/scheduler/schedule/email", keys,
 						new JSONCallbackAdapter() {
 							public void onSuccess(JSONObject json) {
 								String addr[] = new String[3];
 								String subject[] = new String[3];
 								String body[] = new String[3];
 								String address_key[] = {"observer_address", "changed_address", "staff_address"};
 								String subject_key[] = {"observer_subject", "changed_subject", "staff_subject"};
 								String body_key[] = {"observer_body", "changed_body", "staff_body"};
 								                   
 								for (int j = 0; j < 3; ++j)
 								{
 									JSONArray emails = json.get(address_key[j]).isArray();
 									//String addr = "";
 									addr[j] = "";
 									
 									for (int i = 0; i < emails.size(); ++i)
 									{
 										addr[j] += i == 0 ? "" : ",";
 										addr[j] += emails.get(i).isString().stringValue(); // + ", ";
 										
 									}
 	
 									//addr[j] = addr[j].substring(0, addr[j].length() - 2); // Get rid of last comma.
 									subject[j] = json.get(subject_key[j]).isString().stringValue();
 									body[j] = json.get(body_key[j]).isString().stringValue();
 								}
 								
 								// get the periods in the scheduling section
 								JSONArray obs_periods = json.get("obs_periods").isArray();
 								int obsPeriodIds[] = new int[obs_periods.size()];
 								for (int k = 0; k < obs_periods.size(); k++) {
 									obsPeriodIds[k] = (int) obs_periods.get(k).isNumber().doubleValue();
 									GWT.log(Integer.toString(obsPeriodIds[k]));
 								}								
 								// now get the changed periods
 								JSONArray changed_periods = json.get("changed_periods").isArray();
 								int chgPeriodIds[] = new int[changed_periods.size()];
 								for (int k = 0; k < changed_periods.size(); k++) {
 									chgPeriodIds[k] = (int) changed_periods.get(k).isNumber().doubleValue();
 									GWT.log(Integer.toString(chgPeriodIds[k]));
 								}
 								
 								EmailDialogBox dlg = new EmailDialogBox(addr, subject, body, obsPeriodIds, chgPeriodIds);
 								dlg.show();
 								box.close();
 							}
 							
 							public void onError(String error, JSONObject json)
 							{
 								box.close();
 								super.onError(error, json);
 							}
 						});
 			}
 		});
 		
 		publishButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				// make the JSON request for the periods so we can make appointments
 				// we need the same url in a different format
 				//final MessageBox box = MessageBox.confirm("Publish Pending Periods", "r u sure?", l);
 				HashMap<String, Object> keys = getTimeRange();
 				JSONRequest.post("/scheduler/periods/publish", keys,
 						new JSONCallbackAdapter() {
 							public void onSuccess(JSONObject json) {
 								schedule.updateCalendar();
 							}
 						});
 			}
 		});
 
 		restoreScheduleBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				deletePendingDialog.show();
 			}
 		});
 		
 		deletePendingDialog.getButtonById(Dialog.YES).addSelectionListener(
 				new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				// make the JSON request for the periods so we can make appointments
 				// we need the same url in a different format
 				HashMap<String, Object> keys = getTimeRange();
 				//final MessageBox box = MessageBox.confirm("Publish Pending Periods", "r u sure?", l);
 				JSONRequest.post("/scheduler/periods/restore_schedule", keys,
 						new JSONCallbackAdapter() {
 							public void onSuccess(JSONObject json) {
 								schedule.updateCalendar();
 							}
 						});
 			}
 		});
 		
 		factorsButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				BaseModelData selectedItem = schedule.scheduleExplorer.pe.getGrid()
 				        .getSelectionModel().getSelectedItem();
 				if (selectedItem != null) {
 					HashMap<String, Object> periodValues =
 						new HashMap<String, Object>(selectedItem.getProperties());
 					factorsDlg.initValues(periodValues);
 				}
 				
 				factorsDlg.show();
 			}
 		});		
 	}
 }
