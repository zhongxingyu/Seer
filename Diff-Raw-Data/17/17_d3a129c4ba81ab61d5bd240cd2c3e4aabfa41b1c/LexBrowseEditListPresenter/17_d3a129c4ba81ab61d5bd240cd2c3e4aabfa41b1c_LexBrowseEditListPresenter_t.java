 package org.palaso.languageforge.client.lex.browse.edit.presenter;
 
 import java.util.Collection;
 
 import org.palaso.languageforge.client.lex.browse.edit.BrowseAndEditEventBus;
 import org.palaso.languageforge.client.lex.browse.edit.view.LexBrowseEditListView;
 import org.palaso.languageforge.client.lex.common.Constants;
 import org.palaso.languageforge.client.lex.controls.JSNIJQueryWrapper;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 import org.palaso.languageforge.client.lex.model.FieldSettings;
 import org.palaso.languageforge.client.lex.model.LexiconListDto;
 import org.palaso.languageforge.client.lex.model.LexiconListEntry;
 
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
 
 @Presenter(view = LexBrowseEditListView.class)
 public class LexBrowseEditListPresenter extends BasePresenter<LexBrowseEditListPresenter.IView, BrowseAndEditEventBus> {
 	/**
 	 * Interface to be implemented by the view of this presenter
 	 * (LexBrowseEditListView)
 	 */
 	public interface IView {
 
 		HasClickHandlers getTableClickHandlers();
 
 		void clearList();
 
 		void setRow(int row, String word, String meaning);
 
 		int getClickedRow(ClickEvent event);
 
 		void selectRow(int row, boolean selected);
 
 		void applyDataRowStyles();
 
 		void setSuggestBoxLabel(String text);
 
 		SimplePanel getScrollPanel();
 	}
 
 	private int startIndex, selectedRow = -1;
 
 	private boolean isScrollBarAdded = false;
 
 	/**
 	 * Interface created for to apply the styles in LexListView controls
 	 * 
 	 */
 	interface SelectionStyle extends CssResource {
 		String selectedRow();
 	}
 
 	private FieldSettings fieldSettings = FieldSettings.fromWindow();
 	private int totalCount;
 	private String ids[];
 	@Inject
 	public ILexService LexService;
 
 	/**
 	 * Called when the module loads
 	 */
 	public void onStart() {
 		view.setSuggestBoxLabel(org.palaso.languageforge.client.lex.common.Constants.getFirstWordAbbreviation());
 		eventBus.setNavStatus(0, 0, 0);
 	}
 
 	@Override
 	public void bind() {
 		view.getTableClickHandlers().addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				int row = view.getClickedRow(event);
 				if (row != -1) {
 					selectRow(row);
 					eventBus.wordSelected(this.getRowId(row));
 				} else {
 					eventBus.setDeleteButtonVisible(false);
 				}
 			}
 			
 
 			private String getRowId(int row) {
 				return ids[row].toString();
 			}
 		});
 
 	}
 
 	/**
 	 * Previous clicked. Move back one page if we are not only on the first
 	 * page.
 	 */
 	public void onPrevious() {
 		startIndex -= Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		if (startIndex < 0) {
 			startIndex = 0;
 		} else {
 			selectRowInternal(selectedRow, false);
 			selectedRow = -1;
 			update(false, startIndex);
 		}
 	}
 
 	/**
 	 * Next clicked. Move forward one page if we are not already on the last
 	 * page.
 	 */
 	public void onNext() {
 		startIndex += Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		if (startIndex >= totalCount) {
 			startIndex -= Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		} else {
 			selectRowInternal(selectedRow, false);
 			selectedRow = -1;
 
 			// will trigger a fetch from the server if needed or read from the
 			// cache if already present.
 			update(false, startIndex);
 		}
 	}
 
 	/**
 	 * First clicked. Move to first page.
 	 */
 	public void onFirst() {
 		startIndex = 0;
 		update(false, 0);
 	}
 
 	/**
 	 * Last clicked. Move to last page.
 	 */
 	public void onLast() {
 
 		startIndex = LexService.getEntryCountAvailableOnServer()
 				- (LexService.getEntryCountAvailableOnServer() % Constants.VISIBLE_WORDS_PER_PAGE_COUNT);
 
 		// Notes: If the user hits "Previous" then we read from the cache. This
 		// means that we have to fetch ALL words from the server if we go to the
 		// last page.
 
 		update(true, -1);
 	}
 
 	/**
 	 * Entries received from the server. Populates the list.
 	 */
 	public void onResultListDTO(LexiconListDto result) {
 		JsArray<LexiconListEntry> entries = result.getEntries();
 		int count = entries.length();
 		totalCount = result.getEntryCount();
 		ids = new String[count];
 		view.clearList();
 		if (count <= 0) {
 			eventBus.setDeleteButtonVisible(false);
 		}
 		for (int i = 0; i < count; i++) {
 
 			LexiconListEntry entry = entries.get(i);
 			if (entry != null) {
 				ids[i] = entry.getId();
 
 				if (i == 0) {
 					eventBus.wordSelected(this.getRowId(0));
 				}
 				view.setRow(i, entry.getWordFirstForm(), entry.getMeaningFirstForm());
 			}
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
 	 * Reload event received. Refresh the list.
 	 */
 	public void onReloadList() {
 		reloadInternal();
 	}
 
 	/**
 	 * To get the row id used when select any from the list
 	 */
 	private String getRowId(int i) {
 		return ids[i].toString();
 	}
 
 	/**
 	 * Re-fill the list to apply the changes happened in cache
 	 */
 	public void onClientDataRefresh(boolean isUpdate, boolean resetPager) {
 		if (resetPager)
 		{
 			startIndex = 0;
 			selectedRow = -1;
 		}
 		update(!isUpdate, isUpdate, startIndex);
 	}
 
 	/**
 	 * To create a call to json-rpc service to load the entry list
 	 */
 	private void update(boolean force, int rangeBegin) {
 		update(force,false,rangeBegin);
 	}
 	/**
 	 * To create a call to json-rpc service to load the entry list
 	 */
 	private void update(boolean force, final boolean isUpdate, int rangeBegin) {
 		eventBus.setDeleteButtonVisible(false);
 		final int oldSelectedRow = selectedRow;
 		LexService.getAllEntriesAsList(force, rangeBegin, new AsyncCallback<Collection<LexiconListEntry>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				eventBus.handleError(caught);
 			}
 
 			@Override
 			public void onSuccess(Collection<LexiconListEntry> result) {
 				reloadInternal();
 				if (isUpdate) {
 					if (oldSelectedRow > 0) {
 						selectRow(oldSelectedRow);
 						eventBus.wordSelected(getRowId(oldSelectedRow));
 					}
 				}
 			}
 		});
 
 	}
 
 	private void reloadInternal() {
 		// bind the cached words to the list
 		LexiconListDto wordList = LexiconListDto.createFromSettings(fieldSettings);
 
 		Object[] obj = LexService.getEntryCollection().toArray();
 		int wordsOnPageCount = LexService.getEntryCountAvailableOnServer() < startIndex
 				+ Constants.VISIBLE_WORDS_PER_PAGE_COUNT ? LexService.getEntryCountAvailableOnServer() : startIndex
 				+ Constants.VISIBLE_WORDS_PER_PAGE_COUNT;
 		for (int i = startIndex; i < wordsOnPageCount; i++) {
 			wordList.addEntry((LexiconListEntry) obj[i]);
 		}
 		wordList.setEntryCount(LexService.getEntryCountAvailableOnServer());
 		onResultListDTO(wordList);
 
 		if (isScrollBarAdded) {
			//JSNIJQueryWrapper.removeJQueryScrollbars(view.getScrollPanel().getElement());
 			isScrollBarAdded = false;
 		}
		//JSNIJQueryWrapper.addJQueryScrollbars(GWT.getModuleBaseURL());
 		isScrollBarAdded = true;
 	}
 
 	/**
 	 * Selects the row and applies the appropriate style.
 	 */
 	private void selectRow(int row) {
 		// remove selected style from old row
 		selectRowInternal(selectedRow, false);
 
 		// set selected style to new row
 		selectRowInternal(row, true);
 
 		// keep last selected row
 		selectedRow = row;
 	}
 
 	private void selectRowInternal(int row, boolean selected) {
 		if (row != -1) {
 			view.selectRow(row, selected);
 		}
 	}
 
 }
