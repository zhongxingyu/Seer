 package org.palaso.languageforge.client.lex.addinfo.presenter;
 
 import java.util.Collection;
 
 import org.palaso.languageforge.client.lex.addinfo.AddInfoEventBus;
 import org.palaso.languageforge.client.lex.addinfo.view.IncompleteWordListView;
 import org.palaso.languageforge.client.lex.common.Constants;
 import org.palaso.languageforge.client.lex.common.enums.EntryFieldType;
 import org.palaso.languageforge.client.lex.common.enums.SettingTaskNameType;
 import org.palaso.languageforge.client.lex.controls.JSNIJQueryWrapper;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 import org.palaso.languageforge.client.lex.model.DashboardActivitiesDto;
 import org.palaso.languageforge.client.lex.model.FieldSettings;
 import org.palaso.languageforge.client.lex.model.LexiconListDto;
 import org.palaso.languageforge.client.lex.model.LexiconListEntry;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksDashboardSettings;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksTaskElementDto;
 
 import com.github.gwtbootstrap.client.ui.Bar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.inject.Inject;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 // TODO: should eliminate code redundancy with classes in .lex.browse.edit but then we have cross-module references 
 
 @Presenter(view = IncompleteWordListView.class)
 public class IncompleteWordListPresenter extends
 		BasePresenter<IncompleteWordListPresenter.IView, AddInfoEventBus> {
 	/**
 	 * Interface created for MissInfoListView controls , these functions must be
 	 * implemented in MissInfoListView class
 	 */
 	public interface IView {
 
 		HasClickHandlers getTableClickHandlers();
 
 		void clearList();
 
 		void setRow(int row, String word, String meaning);
 
 		int getClickedRow(ClickEvent event);
 
 		void selectRow(int row, boolean selected);
 
 		void applyDataRowStyles();
 
 		SimplePanel getScrollPanel();
 
 		Bar getProgressLabel();
 	}
 
 	private int startIndex, selectedRow = -1;
 
 	private boolean isScrollbarAdded = false;
 
 	public int getSelectedRow() {
 		return selectedRow;
 	}
 
 	public void setSelectedRow(int selectedRow) {
 		this.selectedRow = selectedRow;
 	}
 
 	/**
 	 * Interface created for to apply the styles in MissInfoListView controls
 	 * 
 	 */
 	interface SelectionStyle extends CssResource {
 		String selectedRow();
 	}
 
 	private static EntryFieldType entryFieldType;
 
 	public static EntryFieldType getEntryFieldType() {
 		return entryFieldType;
 	}
 
 	public void setEntryFieldType(EntryFieldType entryFieldType) {
 		IncompleteWordListPresenter.entryFieldType = entryFieldType;
 	}
 
 	private FieldSettings fieldSettings = FieldSettings.fromWindow();
 	private int totalCount;
 	private String ids[];
 	@Inject
 	public ILexService LexService;
 
 	/**
 	 * Bind the button controls from loading by the EventBus
 	 * 
 	 */
 	public void bind() {
 		eventBus.setNavStatus(0, 0, 0);
 
 		view.getTableClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				int row = view.getClickedRow(event);
 				if (row != -1) {
 					selectRow(row);
 					eventBus.wordSelected(this.getRowId(row));
 				}
 			}
 
 			private String getRowId(int row) {
 				return ids[row].toString();
 			}
 		});
 
 	}
 
 	/**
 	 * Loading by the eventbus on start up of the project
 	 * 
 	 */
 	public void onStartModule(EntryFieldType entryFieldType) {
 		setEntryFieldType(entryFieldType);
 		selectRow(0);
 		startIndex = 0;
 		update(true);
 	}
 
 	/**
 	 * To go backward to previous set of entry list from server
 	 * 
 	 */
 	public void onPrevious() {
 		// Move back a page.
 		startIndex -= Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		if (startIndex < 0) {
 			startIndex = 0;
 		} else {
 			styleRow(selectedRow, false);
 			selectedRow = -1;
 			paginate();
 		}
 	}
 
 	/**
 	 * To go forward to next set of entry list from server
 	 * 
 	 */
 	public void onNext() {
 		// Move forward a page.
 		startIndex += Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		if (startIndex >= totalCount) {
 			startIndex -= Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		} else {
 			styleRow(selectedRow, false);
 			selectedRow = -1;
 			paginate();
 		}
 	}
 
 	/**
 	 * To go to first page
 	 * 
 	 */
 	public void onFirst() {
 		// Move to 1st page.
 		startIndex = 0;
 		paginate();
 	}
 
 	/**
 	 * To go to last page
 	 * 
 	 */
 	public void onLast() {
 		// Move to last page.
 		startIndex = totalCount
 				- (totalCount % Constants.VISIBLE_WORDS_PER_PAGE_COUNT);
 		paginate();
 	}
 
 	/**
 	 * To apply the style for selected row in the grid
 	 * 
 	 */
 	private void selectRow(int row) {
 		styleRow(selectedRow, false);
 		styleRow(row, true);
 		selectedRow = row;
 	}
 
 	/**
 	 * To populate the list with pagination bar
 	 * 
 	 */
 	public void onResultListDTO(LexiconListDto result) {
 		JsArray<LexiconListEntry> entries = result.getEntries();
 		int count = entries.length();
 
 		totalCount = result.getEntryCount();
 		ids = new String[count];
 		view.clearList();
 		for (int i = 0; i < count; ++i) {
 
 			LexiconListEntry entry = entries.get(i);
 			ids[i] = entry.getId();
 
 			if (i == 0) {
 
 				eventBus.wordSelected(this.getRowId(0));
 			}
 			view.setRow(i, entry.getWordFirstForm(),
 					entry.getMeaningFirstForm());
 
 		}
 		if (selectedRow == -1) {
 			selectRow(0);
 		}
 		view.applyDataRowStyles();
 
 		int max = startIndex + Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		if (max > totalCount) {
 			max = totalCount;
 		}
 		if (totalCount == 0) {
 			eventBus.setNavStatus(0, max, totalCount);
 		} else {
 			eventBus.setNavStatus(startIndex + 1, max, totalCount);
 		}
 	}
 
 	/**
 	 * To get the row id used when select any from the list
 	 * 
 	 */
 	private String getRowId(int i) {
 		return ids[i].toString();
 	}
 
 	/**
 	 * re-fill the list to apply the changes happened in cache
 	 */
 	public void onClientDataRefresh(EntryFieldType i) {
 		update(false);
 	}
 
 	/**
 	 * To create a call to json-rpc service to load the entry list in grid view
 	 * 
 	 */
 
 	private void update(boolean force) {
 		view.getProgressLabel().setPercent(0);
 		view.getProgressLabel().setText("");
 		// for startup time to get server and add the list to cache
 		LexService.getEntriesWithMissingFieldsAsList(getEntryFieldType(),
 				force, new AsyncCallback<Collection<LexiconListEntry>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						eventBus.handleError(caught);
 					}
 
 					@Override
 					public void onSuccess(
 							final Collection<LexiconListEntry> result) {
 						paginate();
 
 						// // so now we need to know how many words in server
 						// // for update progress bar
 						// LexService
 						// .getWordCountInDatabase(new
 						// AsyncCallback<ResultDto>() {
 						//
 						// @Override
 						// public void onSuccess(ResultDto resultDto) {
 						// double serverCount = Double
 						// .parseDouble(resultDto
 						// .getCode());
 						//
 						// int countPercent = (int) (((serverCount - (double)
 						// result
 						// .size()) / serverCount) * 100);
 						// view.getProgressLabel().setPercent(
 						// (int) countPercent);
 						// view.getProgressLabel().setText(
 						// "Add Infomation "
 						// + countPercent
 						// + " % Complete");
 						// }
 						//
 						// @Override
 						// public void onFailure(Throwable caught) {
 						// eventBus.handleError(caught);
 						// }
 						// });
 					}
 				});
 
 		// use dashboard data to show the progress bar
 		LexService.getDashboardData(1,
 				new AsyncCallback<DashboardActivitiesDto>() {
 
 					@Override
 					public void onSuccess(DashboardActivitiesDto result) {
 						SettingTasksDashboardSettings dashboardSpeData = getDashboardSettings();
 						// calculate percent
 						int targetWordCount = dashboardSpeData
 								.getTargetWordCount();
 
 						int statsWordCountPercent = (result.getStatsWordCount() * 100)
 								/ targetWordCount;
 						int statsPercent = 0;
 
 						switch (getEntryFieldType()) {
 						case DEFINITION:
 							statsPercent = statsWordCountPercent == 0 ? result
 									.getStatsMeanings() : (result
 									.getStatsMeanings() * 100)
 									/ result.getStatsWordCount();
 							break;
 						case POS:
 							statsPercent = statsWordCountPercent == 0 ? result
 									.getStatsPos()
 									: (result.getStatsPos() * 100)
 											/ result.getStatsWordCount();
 							break;
 						case EXAMPLESENTENCE:
 							statsPercent = statsWordCountPercent == 0 ? result
 									.getStatsExamples() : (result
 									.getStatsExamples() * 100)
 									/ result.getStatsWordCount();
 							break;
 						default:
 							throw new RuntimeException();
 						}
 
 						view.getProgressLabel().setText(statsPercent + "% Complete");
 						
 						statsPercent = statsPercent>100 ? 100 : statsPercent;
 						
 						view.getProgressLabel().setPercent((int) statsPercent);
 
 
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						eventBus.handleError(caught);
 					}
 				});
 	}
 
 	private void paginate() {
 		// bind the cached list to result
 		LexiconListDto result1 = LexiconListDto
 				.createFromSettings(fieldSettings);
 
 		Object[] obj = LexService.getEntryCollection().toArray();
 		int entriesCount = LexService.getEntriesRecordCount() < startIndex
 				+ Constants.VISIBLE_WORDS_PER_PAGE_COUNT ? LexService
 				.getEntriesRecordCount() : startIndex
 				+ Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		if (isScrollbarAdded) {
			JSNIJQueryWrapper.removeJQueryScrollbars(view.getScrollPanel()
					.getElement());
 			isScrollbarAdded = false;
 		}
 		if (entriesCount == 0) {
 			eventBus.noWordsToDisplay();
 			view.clearList();
 			eventBus.setNavStatus(0, 0, 0);
 			return;
 		}
 
 		for (int i = startIndex; i < entriesCount; i++) {
 			result1.addEntry((LexiconListEntry) obj[i]);
 		}
 
 		result1.setEntryCount(LexService.getEntriesRecordCount());
 		onResultListDTO(result1);
 
		JSNIJQueryWrapper.addJQueryScrollbars(GWT.getModuleBaseURL());
 		isScrollbarAdded = true;
 
 	}
 
 	private void styleRow(int row, boolean selected) {
 		if (row != -1) {
 			view.selectRow(row, selected);
 		}
 	}
 
 	public void onPreviousWord() {
 		if (getSelectedRow() - 1 > -1) {
 			setSelectedRow(getSelectedRow() - 1);
 			styleRow(getSelectedRow() + 1, false);
 			selectRow(getSelectedRow());
 			eventBus.wordSelected(this.getRowId(getSelectedRow()));
 		}
 	}
 
 	public void onNextWord() {
 		if (getSelectedRow() + 1 < totalCount - startIndex
 				&& getSelectedRow() + 1 < Constants.VISIBLE_WORDS_PER_PAGE_COUNT) {
 			setSelectedRow(getSelectedRow() + 1);
 			styleRow(getSelectedRow() - 1, false);
 			selectRow(getSelectedRow());
 			eventBus.wordSelected(this.getRowId(getSelectedRow()));
 		}
 	}
 
 	public void onSetNavigationButtonStatus() {
 		if (totalCount <= 1) {
 			// have only one or none
 			eventBus.setPrevButtonEnabled(false);
 			eventBus.setNextButtonEnabled(false);
 		} else {
 			// have more then one.
 			if (getSelectedRow() < 1) {
 				eventBus.setPrevButtonEnabled(false);
 				eventBus.setNextButtonEnabled(true);
 			} else if ((getSelectedRow() + 1) >= Constants.VISIBLE_WORDS_PER_PAGE_COUNT) {
 				eventBus.setPrevButtonEnabled(true);
 				eventBus.setNextButtonEnabled(false);
 			} else {
 				eventBus.setPrevButtonEnabled(true);
 				eventBus.setNextButtonEnabled(true);
 			}
 		}
 	}
 
 	private SettingTasksDashboardSettings getDashboardSettings() {
 		JsArray<SettingTasksTaskElementDto> tasksDto = SettingTasksDto
 				.getCurrentUserSetting().getEntries();
 		if (tasksDto.length() > 0) {
 			for (int i = 0; i < tasksDto.length(); i++) {
 				SettingTasksTaskElementDto taskElementDto = tasksDto.get(i);
 
 				if (taskElementDto.getTaskName() == SettingTaskNameType.DASHBOARD) {
 					SettingTasksDashboardSettings taskDashboardSpeData = SettingTasksDashboardSettings
 							.<SettingTasksDashboardSettings> decode(taskElementDto
 									.getTaskSpecifiedData());
 					return taskDashboardSpeData;
 				}
 			}
 		}
 		return SettingTasksDashboardSettings.getNew();
 	}
 
 }
