 package edu.nrao.dss.client.widget.explorers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.SplitButton;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.Period;
 import edu.nrao.dss.client.data.PeriodType;
 import edu.nrao.dss.client.util.DynamicHttpProxy;
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 import edu.nrao.dss.client.widget.PeriodGroupInfoPanel;
 
 // This class is responsible for handling CRUD on the periods of a given Window.
 // This class breaks the pattern that we'd had so far:
 // One type of  Explorer -> REST url -> one type of Nell Resource.
 // Here we create a different explorer that shares the PeriodResource w/ the PeriodExplorer.
 
 // This abstract class is implemented by WindowedPeriodExplorer and ElectivePeriodExplorer
 
 public abstract class GroupPeriodExplorer extends Explorer {
 	
 	protected int periodGroupId;
 	protected String sessionHandle;
 
 	// the parent panel who needs to be updated frequently
 	protected PeriodGroupInfoPanel pg;
 
 	protected abstract void showDlg(Period period);
 	protected abstract String getFilterUrl(int id);
 
 	// NOTE: we are getting the column definitions passed in from the child class
 	public GroupPeriodExplorer(int periodGroupId, String sessionHandle, ColumnType[] columnTypes) {
 		super("/scheduler/periods/UTC", "", new PeriodType(columnTypes));
 	    this.periodGroupId = periodGroupId;
 	    this.sessionHandle = sessionHandle;    
 		setShowColumnsMenu(false);
 		setAutoHeight(true);
 		setCreateFilterToolBar(false);
 		initLayout(initColumnModel(columnTypes), true);
 		filterByPeriodGroup(periodGroupId);
 		viewItem.setVisible(true);
 	}
 	
 	// make sure we pull in only periods belonging to this elective
 	private void filterByPeriodGroup(int id) {
 		String url = getFilterUrl(id);
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 		DynamicHttpProxy<BasePagingLoadResult<BaseModelData>> proxy = getProxy();
 		proxy.setBuilder(builder);
 	}
 	
 	// override Explorer's loadData so that when it get's called in the parent class, we get periods for just this elective 
 	public void loadData() {
 		filterByPeriodGroup(periodGroupId);
 		loader.load(0, getPageSize());
 	}	
 
 	// we need to override Explorer's initColumnModel because we are being passed in our columnTypes
 	private ColumnModel initColumnModel(ColumnType[] columnTypes) {
 		configs = new ArrayList<ColumnConfig>();
 		CheckColumnConfig checkColumn;
 		for (ColumnType ct : columnTypes) {
 			if (ct.getClasz() != Boolean.class) {
 			    configs.add(new PeriodColConfig(ct.getId(), ct.getName(), ct.getLength(), ct.getClasz()));
 			} else {
 				checkColumn = new CheckColumnConfig(ct.getId(), ct.getName(), ct.getLength());
 			    checkColumn.setEditor(new CellEditor(new CheckBox()));
 			    configs.add(checkColumn);
 			    checkBoxes.add(checkColumn);
 			}
 		}
 	    return new ColumnModel(configs);
 	}
 	
 	// when the view button gets pressed, show the Period Summary Dialog.
 	public void viewObject() {
 		// get the id of the period selected
 		BaseModelData bd = getGrid().getSelectionModel().getSelectedItem();
 		if (bd == null) {
 			return;
 		}
 		Double bid = bd.get("id");
 		int periodId = bid.intValue();
 		// use this to get it's JSON, which includes things like time accounting
 	    JSONRequest.get("/scheduler/periods/UTC/" + Integer.toString(periodId) , new JSONCallbackAdapter() {
             @Override
             public void onSuccess(JSONObject json) {
             	// display the dialog
              	Period period = Period.parseJSON(json.get("period").isObject());
              	// abstract method to be implemented by child so that the 
              	// resulting dialog updates the correct parent panel of this explorer
              	showDlg(period);
             }
         });		
 	}
 	
 	public void viewPeriodSummaryDlg(JSONObject jsonPeriod) {
 	}
 	
 	public ColumnConfig getSessionConfig() {
 		return configs.get(0);
 	}
 	
 	private List<ColumnConfig> configs;
 
 	public void registerObservers(PeriodGroupInfoPanel pg) {
 		    this.pg = pg;
 	}
 
 	// reload all the elective info when a change gets made to the elective's periods
 	public void updateObservers() {
 		pg.getPeriodGroup();
 	}
 	
	// override this method from the parent class so that we can enforce that only pending
	// periods are removed from this explorer
	protected void setRemoveItemListener() {
		removeItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				String state = getGrid().getSelectionModel().getSelectedItem().get("state");
				// if pending, do this
				if (state.compareTo("P") == 0) {
					removeDialog.show();
				} else {
				    // if not, let them know they can't
					MessageBox.alert("Not Allowed To Delete This Period", "Please use the Period Summary Dialog to delete a non-Pending Period.", null);
				}	
			}
		});
	}	
	
 }	
 	
