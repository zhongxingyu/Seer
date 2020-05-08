 package org.palaso.languageforge.client.lex.configure.presenter;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.palaso.languageforge.client.lex.controls.ExtendedComboBox;
 import org.palaso.languageforge.client.lex.common.MessageFormat;
 import org.palaso.languageforge.client.lex.common.SettingTaskNameType;
 import org.palaso.languageforge.client.lex.common.I18nConstants;
 import org.palaso.languageforge.client.lex.configure.ConfigureEventBus;
 import org.palaso.languageforge.client.lex.common.IPersistable;
 import org.palaso.languageforge.client.lex.configure.view.ConfigureSettingsView;
 import org.palaso.languageforge.client.lex.model.CurrentEnvironmentDto;
 import org.palaso.languageforge.client.lex.model.IanaBaseDataDto;
 import org.palaso.languageforge.client.lex.model.IanaDto;
 import org.palaso.languageforge.client.lex.model.UserDto;
 import org.palaso.languageforge.client.lex.model.UserListDto;
 import org.palaso.languageforge.client.lex.model.UserSettingsDto;
 import org.palaso.languageforge.client.lex.model.settings.inputsystems.SettingInputSystemsDto;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.touch.client.Point;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = ConfigureSettingsView.class)
 public class ConfigureSettingsPresenter
 		extends
 		BasePresenter<ConfigureSettingsPresenter.IConfigureSettingsView, ConfigureEventBus> {
 	@Inject
 	public ILexService lexService;
 
	protected Map<String, UserDto> userDtoList = null;
 	protected SortedMap<String, IanaBaseDataDto> languagesData = null;
 	protected SortedMap<String, IanaBaseDataDto> regionsData = null;
 	protected SortedMap<String, IanaBaseDataDto> scriptsData = null;
 	protected SortedMap<String, IanaBaseDataDto> variantsData = null;
 
 	protected HashMap<SimplePanel, IPersistable> subControls = new HashMap<SimplePanel, IPersistable>();
 
 	protected SettingInputSystemsDto settingInputSystemsDto = null;
 
 	public void onStartSettings(SimplePanel panel) {
 		panel.setWidget(view.getWidget());
 	}
 
 	public void onAddSubControl(SimplePanel panel, IPersistable control) {
 		subControls.put(panel, control);
 	}
 
 	public void bind() {
 		view.getTabPanel().selectTab(0);
 		view.getUserListBoxPanel().setVisible(false);
 		lexService.getUsersListInProject(new AsyncCallback<UserListDto>() {
 
 			@Override
 			public void onSuccess(UserListDto result) {
 				userDtoList = convertListDtoToUserHasMap(result);
 				fillUserListIntoListBox();
 				checkIsDataLoadAndGo();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				eventBus.handleError(caught);
 			}
 		});
 
 		lexService.getIanaData(new AsyncCallback<IanaDto>() {
 
 			@Override
 			public void onSuccess(IanaDto result) {
 				languagesData = convertIanaBaseDataDtoListToSortedMap(result
 						.getLanguages());
 
 				regionsData = convertIanaBaseDataDtoListToSortedMap(result
 						.getRegions());
 
 				scriptsData = convertIanaBaseDataDtoListToSortedMap(result
 						.getScripts());
 
 				variantsData = convertIanaBaseDataDtoListToSortedMap(result
 						.getVariants());
 
 				checkIsDataLoadAndGo();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				eventBus.handleError(caught);
 			}
 		});
 
 		lexService
 				.getSettingInputSystems(new AsyncCallback<SettingInputSystemsDto>() {
 
 					@Override
 					public void onSuccess(SettingInputSystemsDto result) {
 						settingInputSystemsDto = result;
 						checkIsDataLoadAndGo();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						eventBus.handleError(caught);
 
 					}
 				});
 
 		SettingTaskNameType dd = SettingTaskNameType.ADDMISSINGINFO;
 
 		String tt = dd.getValue();
 
 		dd = SettingTaskNameType.getFromValue(tt);
 
 		view.getApplyClickedHandlers().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 
 				IPersistable currentControl = subControls
 						.get(getSelectedTabInnetPanel(view.getTabPanel()
 								.getSelectedIndex()));
 
 				if (currentControl.isPersistDataByParentSupported()) {
 					if (currentControl.isMultiUserSupported()) {
 						// can save per user based
 						Point btnMoreLoc = view.getBtnApplyLocation();
 						PopupPanel popupPanel = new PopupPanel(true);
 						popupPanel.addStyleName("popup");
 
 						createMenuBarFromApplyToUser(popupPanel);
 
 						popupPanel.setPopupPosition(
 								(int) btnMoreLoc.getX() - 60,
 								(int) btnMoreLoc.getY() + 40);
 						popupPanel.show();
 					} else {
 						// gobal setting
 						currentControl.persistData(null);
 					}
 				}
 
 			}
 		});
 
 		view.getTabPanel().addSelectionHandler(new SelectionHandler<Integer>() {
 
 			@Override
 			public void onSelection(SelectionEvent<Integer> event) {
 				IPersistable currentControl = subControls
 						.get(getSelectedTabInnetPanel(view.getTabPanel()
 								.getSelectedIndex()));
 				if (currentControl.isPersistDataByParentSupported()) {
 					if (currentControl.isMultiUserSupported()) {
 						// can save per user based
 						view.getUserListBoxPanel().setVisible(true);
 						view.setApplyButtonVisible(true);
 					} else {
 						// gobal setting
 						view.getUserListBoxPanel().setVisible(false);
 						view.setApplyButtonVisible(true);
 					}
 				} else {
 					view.getUserListBoxPanel().setVisible(false);
 					view.setApplyButtonVisible(false);
 				}
 			}
 		});
 
 		view.getUserListBox().addChangeHandler(new ChangeHandler() {
 
 			@Override
 			public void onChange(ChangeEvent event) {
 
 				for (UserDto userDto : userDtoList.values()) {
 					if (view.getUserListBox()
 							.getItemText(
 									view.getUserListBox().getSelectedIndex())
 							.equals(userDto.getName())) {
 
 						userSelectionChanged(userDto);
 						break;
 					}
 				}
 			}
 		});
 
 	}
 
 	private void userSelectionChanged(UserDto userDto) {
 		lexService.getSettingUserSettings(userDto.getId(),
 				new AsyncCallback<UserSettingsDto>() {
 
 					@Override
 					public void onSuccess(UserSettingsDto result) {
 						eventBus.fillFields(result.getFieldEntries());
 						eventBus.fillTasks(result.getTaskEntries());
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						eventBus.handleError(caught);
 					}
 				});
 	}
 
 	private SimplePanel getSelectedTabInnetPanel(int index) {
 
 		if (index == 0) {
 			// "input System Tab
 			return view.getInputSystemsPanel();
 		} else if (index == 1) {
 			// "Fields Tab
 			return view.getFieldsPanel();
 		} else if (index == 2) {
 			// "tasks Tab
 			return view.getTasksPanel();
 		} else if (index == 3) {
 			// "tasks Tab
 			return view.getPropertiesPanel();
 		} else if (index == 4) {
 			// "tasks Tab
 			return view.getMembersPanel();
 		}
 		return null;
 	}
 
 	public void onSetSettingInputSystems(SettingInputSystemsDto dto) {
 		settingInputSystemsDto = dto;
 	}
 
 	public void onGetSettingInputSystems(List<SettingInputSystemsDto> dtoInList) {
 		dtoInList.add(settingInputSystemsDto);
 	}
 
 	// public Map<Integer, UserDto> getUsersInProject() {
 	// return userDtoList;
 	// }
 
	public void onGetUserInProjectList(Map<String, UserDto> list) {
 		list.putAll(userDtoList);
 	}
 
 	public void onUpdateUserInProjectList(UserListDto listDto) {
 		userDtoList = convertListDtoToUserHasMap(listDto);
 		fillUserListIntoListBox();
 	}
 
 	public void onGetIanaLanguages(SortedMap<String, IanaBaseDataDto> list) {
 		list.putAll(languagesData);
 	}
 
 	public void onGetIanaRegions(SortedMap<String, IanaBaseDataDto> list) {
 		list.putAll(regionsData);
 	}
 
 	public void onGetIanaScripts(SortedMap<String, IanaBaseDataDto> list) {
 		list.putAll(scriptsData);
 	}
 
 	public void onGetIanaVariants(SortedMap<String, IanaBaseDataDto> list) {
 		list.putAll(variantsData);
 	}
 
 	private void checkIsDataLoadAndGo() {
 		// if all required data loaded and then show sub view!
 		if (userDtoList != null && languagesData != null && regionsData != null
 				&& scriptsData != null && variantsData != null
 				&& settingInputSystemsDto != null) {
 
 			eventBus.attachInputSystemsView(view.getInputSystemsPanel());
 			eventBus.attachFieldsView(view.getFieldsPanel());
 			eventBus.attachTasksView(view.getTasksPanel());
 
 			eventBus.attachPropertiesView(view.getPropertiesPanel());
 			eventBus.attachMembersView(view.getMembersPanel());
 			selectCurrentUser();
 		}
 	}
 
 	private void createMenuBarFromApplyToUser(final PopupPanel panel) {
 		MenuBar popupMenuBar = new MenuBar(true);
 
 		MenuItem applyForAllItem = new MenuItem(I18nConstants.STRINGS.ConfigureSettingsPresenter_Apply_to_All(), true,
 				new Command() {
 					@Override
 					public void execute() {
 						panel.hide();
 						subControls.get(
 								getSelectedTabInnetPanel(view.getTabPanel()
 										.getSelectedIndex())).persistData(null);
 					}
 				});
 
 		applyForAllItem.addStyleName("popup-item");
 		applyForAllItem.addStyleName("popup-item-top-divider");
 
 		for (final UserDto userDto : userDtoList.values()) {
 			String message = MessageFormat.format(I18nConstants.STRINGS.ConfigureSettingsPresenter_Apply_to_X()
 					, new Object[]{userDto.getName()});
 			MenuItem applyForUserItem = new MenuItem(message, true, new Command() {
 				@Override
 				public void execute() {
 					panel.hide();
 
 					subControls.get(
 							getSelectedTabInnetPanel(view.getTabPanel()
 									.getSelectedIndex())).persistData(userDto);
 
 				}
 			});
 			applyForUserItem.addStyleName("popup-item");
 			popupMenuBar.addItem(applyForUserItem).setWidth("100%");
 		}
 
 		// this should always be last one!!!
 		popupMenuBar.addItem(applyForAllItem).setWidth("100%");
 
 		popupMenuBar.setVisible(true);
 		panel.setWidget(popupMenuBar);
 	}
 
 	private void fillUserListIntoListBox() {
 		view.getUserListBox().clear();
 		view.getUserListBox().beginUpdateItem();
 		for (UserDto userDto : userDtoList.values()) {
 			view.getUserListBox().addItem(userDto.getName());
 		}
 		view.getUserListBox().endUpdateItem();
 	}
 
 	private void selectCurrentUser() {
 		if (userDtoList.containsKey(CurrentEnvironmentDto.getCurrentUser()
 				.getId())) {
 			UserDto userDto = CurrentEnvironmentDto.getCurrentUser();
 
 			for (int i = 0; i < view.getUserListBox().getItemCount(); i++) {
 				if (view.getUserListBox().getItemText(i)
 						.equalsIgnoreCase(userDto.getName())) {
 					view.getUserListBox().setSelectedIndex(i);
 					userSelectionChanged(userDto);
 					break;
 				}
 			}
 		}
 	}
 
	private Map<String, UserDto> convertListDtoToUserHasMap(
 			UserListDto userListDto) {
 		JsArray<UserDto> listEntry = userListDto.getEntries();
		Map<String, UserDto> userList = new HashMap<String, UserDto>();
 
 		for (int i = 0; i < listEntry.length(); i++) {
			userList.put(listEntry.get(i).getId(),
 					listEntry.get(i));
 		}
 		return userList;
 	}
 
 	private SortedMap<String, IanaBaseDataDto> convertIanaBaseDataDtoListToSortedMap(
 			JsArray<IanaBaseDataDto> jsArr) {
 		SortedMap<String, IanaBaseDataDto> IanaObjHashMap = new TreeMap<String, IanaBaseDataDto>();
 		for (int i = 0; i < jsArr.length(); i++) {
 			IanaBaseDataDto dto = jsArr.get(i);
 			IanaObjHashMap.put(dto.getSubtag(), dto);
 		}
 		return IanaObjHashMap;
 	}
 
 	public interface IConfigureSettingsView {
 		public Widget getWidget();
 
 		public SimplePanel getInputSystemsPanel();
 
 		public SimplePanel getFieldsPanel();
 
 		public SimplePanel getTasksPanel();
 
 		public SimplePanel getPropertiesPanel();
 
 		public SimplePanel getMembersPanel();
 
 		public HasClickHandlers getApplyClickedHandlers();
 
 		public Point getBtnApplyLocation();
 
 		public ExtendedComboBox getUserListBox();
 
 		public TabLayoutPanel getTabPanel();
 
 		public SimplePanel getUserListBoxPanel();
 
 		public void setApplyButtonVisible(boolean visible);
 	}
 
 	public void onReloadIana() {
 		eventBus.fieldSettingsReloadIana();
 	}
 }
