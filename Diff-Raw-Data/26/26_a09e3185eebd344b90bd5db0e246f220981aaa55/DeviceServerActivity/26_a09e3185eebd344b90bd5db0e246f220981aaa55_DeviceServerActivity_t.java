 package com.eucalyptus.webui.client.activity.device;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.eucalyptus.webui.client.ClientFactory;
 import com.eucalyptus.webui.client.activity.AbstractSearchActivity;
 import com.eucalyptus.webui.client.activity.ActivityUtil;
 import com.eucalyptus.webui.client.place.SearchPlace;
 import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.client.view.FooterView;
 import com.eucalyptus.webui.client.view.HasValueWidget;
 import com.eucalyptus.webui.client.view.LogView;
 import com.eucalyptus.webui.client.view.FooterView.StatusType;
 import com.eucalyptus.webui.client.view.LogView.LogType;
 import com.eucalyptus.webui.client.view.device.DeviceServerDeviceAddView;
 import com.eucalyptus.webui.client.view.device.DeviceServerDeviceAddViewImpl;
 import com.eucalyptus.webui.client.view.device.DeviceServerModifyView;
 import com.eucalyptus.webui.client.view.device.DeviceServerModifyViewImpl;
 import com.eucalyptus.webui.client.view.device.DeviceServerView;
 import com.eucalyptus.webui.client.view.device.DeviceMirrorSearchResultTable.SearchResultRowMatcher;
 import com.eucalyptus.webui.client.view.device.DeviceServerView.MirrorModeType;
 import com.eucalyptus.webui.shared.checker.InvalidValueException;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class DeviceServerActivity extends AbstractSearchActivity implements DeviceServerView.Presenter {
 
 	private static final int LAN_SELECT = 1;
 
 	public static final String TITLE[] = {"Server", "服务器"};
 
 	private DeviceServerModifyView serverModifyView;
 	private DeviceServerDeviceAddView deviceAddView;
    
    public static final int TABLE_COL_INDEX_SERVER_ID = 0;
    public static final int TABLE_COL_INDEX_CHECKBOX = 1;
    public static final int TABLE_COL_INDEX_NO = 2;
    public static final int TABLE_COL_INDEX_STATE = 10;
 
 	public DeviceServerActivity(SearchPlace place, ClientFactory clientFactory) {
 		super(place, clientFactory);
 		serverModifyView = new DeviceServerModifyViewImpl();
 		serverModifyView.setPresenter(new DeviceServerModifyView.Presenter() {
 
 			@Override
 			public void onOK(SearchResultRow row, int state) {
 				handleModifyServer(row, state);
 				getView().getMirrorTable().clearSelection();
 			}
 
 			@Override
 			public void onCancel() {
 				getView().getMirrorTable().clearSelection();
 			}
 
 		});
 
 		deviceAddView = new DeviceServerDeviceAddViewImpl();
 		deviceAddView.setPresenter(new DeviceServerDeviceAddView.Presenter() {
 
 			@Override
 			public boolean onOK(String mark, String name, String conf, String ip, int bw, String sstate, String room) {
 				if (isEmpty(mark) || isEmpty(name)) {
 					StringBuilder sb = new StringBuilder();
 					sb.append(ADD_DEVICE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
 					sb.append("<mark='").append(mark).append("'").append(", ");
 					sb.append("name='").append(name).append("', ");
 					sb.append("conf='").append(conf).append("', ");
 					sb.append("ip='").append(ip).append("', ");
 					sb.append("bw='").append(bw).append("', ");
 					sb.append("state='").append(sstate).append("', ");
 					sb.append("room='").append(room).append("'>");
 					Window.alert(sb.toString());
 					return false;
 				}
 				int state = ServerState.ERROR.getValue();
 				try {
 					state = ServerState.getValue(sstate);
 				}
 				catch (Exception e) {
 				}
 				handleAddDevice(mark, name, conf, ip, bw, state, room);
 				return true;
 			}
 
 		});
 	}
 	
 	private boolean isEmpty(String s) {
 		return s == null || s.length() == 0;
 	}
 
 	private EucalyptusServiceAsync getBackendService() {
 		return clientFactory.getBackendService();
 	}
 
 	private FooterView getFooterView() {
 		return clientFactory.getShellView().getFooterView();
 	}
 
 	private LogView getLogView() {
 		return clientFactory.getShellView().getLogView();
 	}
 
 	private Session getSession() {
 		return clientFactory.getLocalSession().getSession();
 	}
 
 	private ServerState queryState = null;
 
 	private void log(String msg, Throwable caught) {
 		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
 		getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
 	}
 
 	private void showStatus(String msg) {
 		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
 		getLogView().log(LogType.ERROR, msg);
 	}
 
 	private void reloadLabels() {
 		getBackendService().getDeviceServerCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
 			}
 
 			@Override
 			public void onSuccess(Map<Integer, Integer> result) {
 				ServerState.reset();
 				for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
 					ServerState.setCount(ServerState.getServerState(entry.getKey()), entry.getValue());
 				}
 				getView().updateLabels();
 			}
 
 		});
 	}
 
 	@Override
 	protected void doSearch(String query, SearchRange range) {
 		getBackendService().lookupDeviceServer(getSession(), query, range, ServerState.getValue(queryState),
 		        new AsyncCallback<SearchResult>() {
 
 			        @Override
 			        public void onFailure(Throwable caught) {
 				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
 				        log(QUERY_TABLE_FAILURE[LAN_SELECT], caught);
 				        displayData(null);
 			        }
 
 			        @Override
 			        public void onSuccess(SearchResult result) {
 				        displayData(result);
 			        }
 
 		        });
 		reloadLabels();
 	}
 
 	private DeviceServerView getView() {
 		DeviceServerView view = (DeviceServerView)this.view;
 		if (view == null) {
 			view = clientFactory.getDeviceServerView();
 			view.setPresenter(this);
 			container.setWidget(view);
 			view.clear();
 			this.view = view;
 		}
 		return view;
 	}
 
 	@Override
 	protected void showView(SearchResult result) {
 		getView().showSearchResult(result);
 	}
 
 	@Override
 	public void onSelectionChange(Set<SearchResultRow> selections) {
 		/* do nothing */
 	}
 
 	@Override
 	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
 		/* do nothing */
 	}
 
 	@Override
 	protected String getTitle() {
 		return TITLE[LAN_SELECT];
 	}
 
 	public static class ServerState {
 
 		final private int value;
 		final private String[] STATE_VALUES;
 		private int count;
 
 		public static final ServerState INUSE = new ServerState(0);
 		public static final ServerState ERROR = new ServerState(1);
 		public static final ServerState STOP = new ServerState(2);
 
 		private ServerState(int value) {
 			assert (value >= 0);
 			this.value = value;
 			this.count = 0;
 			switch (value) {
 			default:
 				throw new InvalidValueException(Integer.toString(value));
 			case 0:
 				STATE_VALUES = new String[]{"NORMAL", "使用"};
 				return;
 			case 1:
 				STATE_VALUES = new String[]{"ERROR", "故障"};
 				return;
 			case 2:
 				STATE_VALUES = new String[]{"STOP", "停止"};
 				return;
 			}
 		}
 
 		public int getValue() {
 			return value;
 		}
 
 		public static int getValue(ServerState state) {
 			if (state == null) {
 				return -1;
 			}
 			return state.value;
 		}
 
 		public static int getValue(String state) {
 			if (state == null) {
 				return -1;
 			}
 			else if (state.equals(INUSE.toString())) {
 				return INUSE.getValue();
 			}
 			else if (state.equals(ERROR.toString())) {
 				return ERROR.getValue();
 			}
 			else if (state.equals(STOP.toString())) {
 				return STOP.getValue();
 			}
 			throw new InvalidValueException(state);
 		}
 
 		public static ServerState getServerState(int value) {
 			if (value == -1) {
 				return null;
 			}
 			else if (value == INUSE.value) {
 				return INUSE;
 			}
 			else if (value == ERROR.value) {
 				return ERROR;
 			}
 			else if (value == STOP.value) {
 				return STOP;
 			}
 			throw new InvalidValueException(Integer.toString(value));
 		}
 
 		public static int getCount(ServerState state) {
 			if (state != null) {
 				return state.count;
 			}
 			else {
 				return countTotal;
 			}
 		}
 
 		public static void setCount(ServerState state, int count) {
 			if (state != null) {
 				state.count = count;
 			}
 			else {
 				countTotal = count;
 			}
 		}
 
 		public static void reset() {
 			INUSE.count = 0;
 			ERROR.count = 0;
 			STOP.count = 0;
 			countTotal = 0;
 		}
 
 		private static int countTotal = 0;
 
 		@Override
 		public String toString() {
 			return STATE_VALUES[LAN_SELECT];
 		}
 
 	}
 
 	@Override
 	public void setQueryState(ServerState state) {
 		getView().clearSelection();
 		this.queryState = state;
 		this.range = new SearchRange(0, DeviceServerView.DEFAULT_PAGESIZE, -1, true);
 		reloadCurrentRange();
 	}
 
 	@Override
 	public ServerState getQueryState() {
 		return queryState;
 	}
 
 	@Override
 	public int getCounts(ServerState state) {
 		return ServerState.getCount(state);
 	}
 
 	private static final String[] QUERY_COUNT_FAILURE = {"", "获取资源失败"};
 	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
 	private static final String[] UPDATE_SERVER_FAILURE = {"", "更新服务失败"};
 	private static final String[] UPDATE_SERVER_SUCCESS = {"", "更新服务成功"};
 	private static final String[] ADD_DEVICE_SUCCESS = {"", "添加设备成功"};
 	private static final String[] ADD_DEVICE_FAILURE = {"", "添加设备失败"};
 	private static final String[] ADD_DEVICE_FAILURE_INVALID_ARGS = {"", "添加设备失败：无效的参数"};
 	private static final String[] MODIFY_STATE_CONFIRM = {"", "确认对所选择服务器进行操作？"};
 	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};
 	private static final String[] DELETE_DEVICE_FAILURE = {"", "删除设备失败"};
 	private static final String[] DELETE_DEVICE_SUCCESS = {"", "删除设备成功"};
 	private static final String[] DELETE_DELETE_CONFIRM = {"", "确认删除所选择的 设备？"};
 	private static final String[] DELETE_ALL_DEVICE_CONFIRM = {"", "确认删除所选择的 全部设备？"};
 
 	private void prepareModifyServerState(SearchResultRow row) {
 		if (!Window.confirm(MODIFY_STATE_CONFIRM[LAN_SELECT])) {
 			getView().getMirrorTable().clearSelection();
 			return;
 		}
 		int state = -1;
 		try {
			state = ServerState.getValue(row.getField(TABLE_COL_INDEX_STATE));
 		}
 		catch (Exception e) {
 		}
 		serverModifyView.popup(row, state);
 	}
 
 	private void prepareDeleteDevice(SearchResultRow row) {
 		if (!Window.confirm(DELETE_DELETE_CONFIRM[LAN_SELECT])) {
 			getView().getMirrorTable().clearSelection();
 			return;
 		}
 		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
 		list.add(row);
 		handleDeleteDevice(list);
 	}
 
 	@Override
 	public void onMirrorSelectRow(SearchResultRow row) {
 		if (row == null) {
 			return;
 		}
 		switch (getView().getMirrorModeType()) {
 		case MODIFY_STATE:
 			prepareModifyServerState(row);
 			return;
 		case DELETE_DEVICE:
 			prepareDeleteDevice(row);
 			return;
 		default:
 			return;
 		}
 	}
 
 	private void handleAddDevice(String mark, String name, String conf, String ip, int bw, int state, String room) {
 		getBackendService().addDeviceServer(getSession(), mark, name, conf, ip, bw, state, room,
 		        new AsyncCallback<Boolean>() {
 
 			        @Override
 			        public void onFailure(Throwable caught) {
 				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
 				        log(ADD_DEVICE_FAILURE[LAN_SELECT], caught);
 			        }
 
 			        @Override
 			        public void onSuccess(Boolean result) {
 				        if (!result) {
 					        showStatus(ADD_DEVICE_FAILURE[LAN_SELECT]);
 				        }
 				        else {
 					        showStatus(ADD_DEVICE_SUCCESS[LAN_SELECT]);
 				        }
 				        reloadCurrentRange();
 			        }
 
 		        });
 	}
 
 	private void handleModifyServer(SearchResultRow row, int state) {
 		assert (row != null && getView().getMirrorModeType() == MirrorModeType.MODIFY_STATE);
 		getBackendService().modifyDeviceServerState(getSession(), row, state, new AsyncCallback<SearchResultRow>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
 				log(UPDATE_SERVER_FAILURE[LAN_SELECT], caught);
 			}
 
 			@Override
 			public void onSuccess(SearchResultRow result) {
 				if (result != null) {
 					showStatus(UPDATE_SERVER_SUCCESS[LAN_SELECT]);
 					if (getView().isMirrorMode()) {
						final int col = TABLE_COL_INDEX_SERVER_ID;
						result.setField(TABLE_COL_INDEX_CHECKBOX, "+");
 						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {
 
 							@Override
 							public boolean match(SearchResultRow row0, SearchResultRow row1) {
 								return row0.getField(col).equals(row1.getField(col));
 							}
 
 						};
 						getView().getMirrorTable().updateRow(result, matcher);
 					}
 				}
 				else {
 					showStatus(UPDATE_SERVER_FAILURE[LAN_SELECT]);
 				}
 				reloadCurrentRange();
 			}
 
 		});
 	}
 
 	private void handleDeleteDevice(List<SearchResultRow> list) {
 		DeviceServerView view = getView();
 		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_DEVICE);
 		getBackendService().deleteDeviceServer(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
 				log(DELETE_DEVICE_FAILURE[LAN_SELECT], caught);
 			}
 
 			@Override
 			public void onSuccess(List<SearchResultRow> result) {
 				if (result != null) {
 					showStatus(DELETE_DEVICE_SUCCESS[LAN_SELECT]);
 					if (getView().isMirrorMode()) {
						final int col = TABLE_COL_INDEX_SERVER_ID;
 						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {
 
 							@Override
 							public boolean match(SearchResultRow row0, SearchResultRow row1) {
 								return row0.getField(col).equals(row1.getField(col));
 							}
 
 						};
 						for (SearchResultRow row : result) {
 							getView().getMirrorTable().deleteRow(row, matcher);
 						}
 					}
 				}
 				else {
 					showStatus(DELETE_DEVICE_FAILURE[LAN_SELECT]);
 				}
 				reloadCurrentRange();
 			}
 
 		});
 	}
 
 	private SearchResultRow copyRow(SearchResultRow row) {
 		SearchResultRow tmp = row.copy();
		tmp.setField(TABLE_COL_INDEX_CHECKBOX, "");
 		return tmp;
 	}
 
 	@Override
 	public void onModifyState() {
 		Set<SearchResultRow> selected = getView().getSelectedSet();
 		if (selected == null || selected.isEmpty()) {
 			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
 			return;
 		}
 		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
 		for (SearchResultRow row : selected) {
 			list.add(copyRow(row));
 		}
 		getView().openMirrorMode(MirrorModeType.MODIFY_STATE, sortSearchResultRow(list));
 	}
 
 	@Override
 	public void onDeleteDevice() {
 		Set<SearchResultRow> selected = getView().getSelectedSet();
 		if (selected == null || selected.isEmpty()) {
 			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
 			return;
 		}
 		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
 		for (SearchResultRow row : selected) {
 			list.add(copyRow(row));
 		}
 		getView().openMirrorMode(MirrorModeType.DELETE_DEVICE, sortSearchResultRow(list));
 	}
 
 	@Override
 	public void onAddDevice() {
 		final String[] stateValueList = new String[]{ServerState.INUSE.toString(), ServerState.STOP.toString(),
 		        ServerState.ERROR.toString()};
 		deviceAddView.popup(stateValueList);
 	}
 
 	@Override
 	public void onClearSelection() {
 		getView().clearSelection();
 	}
 
 	@Override
 	public void onMirrorBack() {
 		getView().closeMirrorMode();
 	}
 
 	@Override
 	public void onMirrorDeleteAll() {
 		List<SearchResultRow> data;
 		switch (getView().getMirrorModeType()) {
 		case DELETE_DEVICE:
 			data = getView().getMirrorTable().getData();
 			if (data != null && data.size() != 0) {
 				if (Window.confirm(DELETE_ALL_DEVICE_CONFIRM[LAN_SELECT])) {
 					handleDeleteDevice(data);
 				}
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	private List<SearchResultRow> sortSearchResultRow(List<SearchResultRow> list) {
 		Collections.sort(list, new Comparator<SearchResultRow>() {
 
 			@Override
 			public int compare(SearchResultRow arg0, SearchResultRow arg1) {
				String v0 = arg0.getField(TABLE_COL_INDEX_NO);
				String v1 = arg1.getField(TABLE_COL_INDEX_NO);
 				if (v0 == null) {
 					return v1 == null ? 0 : 1;
 				}
 				if (v1 == null) {
 					return -1;
 				}
 				try {
 					return Integer.parseInt(v0) - Integer.parseInt(v1);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				return 0;
 			}
 
 		});
 		return list;
 	}
 
 }
