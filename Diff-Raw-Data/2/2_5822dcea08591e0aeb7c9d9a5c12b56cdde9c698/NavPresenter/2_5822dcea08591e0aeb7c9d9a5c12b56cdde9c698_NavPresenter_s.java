 package org.palaso.languageforge.client.lex.main.presenter;
 
 import org.palaso.languageforge.client.lex.common.ConsoleLog;
 import org.palaso.languageforge.client.lex.common.PermissionManager;
 import org.palaso.languageforge.client.lex.common.enums.DomainPermissionType;
 import org.palaso.languageforge.client.lex.common.enums.EntryFieldType;
 import org.palaso.languageforge.client.lex.common.enums.OperationPermissionType;
 import org.palaso.languageforge.client.lex.main.MainEventBus;
 import org.palaso.languageforge.client.lex.model.CurrentEnvironmentDto;
 import org.palaso.languageforge.client.lex.model.UserDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksTaskElementDto;
 import org.palaso.languageforge.client.lex.main.view.NavPanel;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = NavPanel.class)
 public class NavPresenter extends
 		BasePresenter<NavPresenter.INavView, MainEventBus> {
 
 	public interface INavView {
 
 		// HasClickHandlers getGatherFromOtherLanguageClicker();
 
 		HasClickHandlers getGatherFromTextsClickHandlers();
 
 		HasClickHandlers getGatherFromSemanticDomainsClickHandlers();
 
 		HasClickHandlers getGatherFromWordListClickHandlers();
 
 		HasClickHandlers getReviewRecentChangesClickHandlers();
 
 		//
 		// HasClickHandlers getReviewToDo();
 		HasClickHandlers getDashboardClickHandlers();
 		
 		HasClickHandlers getReviewBrowseEditClickHandlers();
 
 		HasClickHandlers getAddMeaningClickHandlers();
 
 		HasClickHandlers getAddExampleClickHandlers();
 
 		HasClickHandlers getAddGrammaticalClickHandlers();
 
 		HasClickHandlers getSettingsClickHandlers();
 
 		HasClickHandlers getContributeClickHandlers();
 
 		void setBoldStyleAddGrammatical(boolean bold);
 
 		void setBoldStyleAddMeaning(boolean bold);
 
 		void setBoldStyleAddExample(boolean bold);
 
 		void setBoldStyleReviewBrowseEdit(boolean bold);
 
 		// void setBoldStyleGatherFromOtherLanguage(boolean bold);
 
 		void setBoldStyleReviewRecentChanges(boolean bold);
 
 		void setBoldStyleGatherFromTexts(boolean bold);
 
 		void setBoldStyleGatherFromSemanticDomains(boolean bold);
 
 		void setBoldStyleGatherFromWordList(boolean bold);
 
 		void setBoldStyleSettings(boolean bold);
 
 		void setBoldStyleDashboard(boolean bold);
 		
 		// group
 		void setReviewRecentChangesVisible(boolean visible);
 
 		boolean getReviewRecentChangesVisible();
 
 		void setGatherWordsMenuVisible(boolean visible);
 
 		void setGatherWordsMenuExpended(boolean expended);
 
 		void setContributeMenuVisible(boolean visible);
 
 		void setContributeMenuExpended(boolean expended);
 
 		void setConfigureMenuVisible(boolean visible);
 
 		boolean getConfigureMenuVisible();
 
 		void setConfigureMenuExpended(boolean expended);
 
 		void setAddInformationMenuVisible(boolean visible);
 
 		void setAddInformationMenuExpended(boolean expended);
 
 		
 		boolean getDashboardMenuVisible();
 
 		void setDashboardMenuExpended(boolean expended);
 		
 		void setDashboardMenuVisible(boolean visible);
 		// group-elements
 
 		void setGatherFromSemanticDomainsVisible(boolean visible);
 
 		boolean getGatherFromSemanticDomainsVisible();
 
 		void setGatherFromTextsVisible(boolean visible);
 
 		boolean getGatherFromTextsVisible();
 
 		void setGatherFromWordListVisible(boolean visible);
 
 		boolean getGatherFromWordListVisible();
 
 		void setReviewBrowseEditVisible(boolean visible);
 
 		boolean getReviewBrowseEditVisible();
 
 		void setAddMeaningVisible(boolean visible);
 
 		boolean getAddMeaningVisible();
 
 		void setAddGrammaticalPanelVisible(boolean visible);
 
 		boolean getAddGrammaticalPanelVisible();
 
 		void setAddExamplePanelVisible(boolean visible);
 
 		boolean getAddExamplePanelVisible();
 
 	}
 
 
 	private class UrlCmdClickEvent extends ClickEvent {
 	};
 
 
 	@Override
 	public void bind() {
 		// view.getGatherFromOtherLanguageClicker().addClickHandler(
 		// new ClickHandler() {
 		// public void onClick(ClickEvent event) {
 		// eventBus.displayMessage("Gather From Other Language clicked");
 		// }
 		// });
 
 		view.getGatherFromSemanticDomainsClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToGatherFromSemanticDomains();
 				resetSelection();
 				view.setBoldStyleGatherFromSemanticDomains(true);
 			}
 		});
 
 		view.getGatherFromTextsClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToGatherFromTexts();
 				resetSelection();
 				view.setBoldStyleGatherFromTexts(true);
 			}
 		});
 
 		view.getGatherFromWordListClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToGatherFromWordList();
 				resetSelection();
 				view.setBoldStyleGatherFromWordList(true);
 			}
 		});
 
 		view.getReviewRecentChangesClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToReviewRecentChanges();
 				resetSelection();
 				view.setBoldStyleReviewRecentChanges(true);
 			}
 		});
 		//
 		// view.getReviewToDo().addClickHandler(new ClickHandler() {
 		// public void onClick(ClickEvent event) {
 		// eventBus.displayMessage("Review To Do clicked");
 		// }
 		// });
 
 		view.getReviewBrowseEditClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToLexDicBrowseAndEdit();
 				resetSelection();
 				view.setBoldStyleReviewBrowseEdit(true);
 			}
 		});
 
 		view.getAddMeaningClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToLexMissingInfo(EntryFieldType.DEFINITION);
 				resetSelection();
 				view.setBoldStyleAddMeaning(true);
 			}
 		});
 		view.getAddGrammaticalClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToLexMissingInfo(EntryFieldType.POS);
 				resetSelection();
 				view.setBoldStyleAddGrammatical(true);
 			}
 		});
 		view.getAddExampleClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToLexMissingInfo(EntryFieldType.EXAMPLESENTENCE);
 				resetSelection();
 				view.setBoldStyleAddExample(true);
 			}
 		});
 		view.getSettingsClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				eventBus.goToConfigureSettings();
 				resetSelection();
 				view.setBoldStyleSettings(true);
 			}
 		});
 
 		view.getContributeClickHandlers().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				String newUrl = "http://"
 						+ Window.Location.getHost()
 						+ "/project/subscribe/"
 						+ CurrentEnvironmentDto.getCurrentProject()
 								.getProjectId();
 				//Window.Location.assign(newUrl);
 				topWindowRedirect(newUrl);
 			}
 		});
 		view.getDashboardClickHandlers().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				eventBus.goToDashboard();
 				resetSelection();
 				view.setBoldStyleDashboard(true);
 			}
 		});
 		
 	}
 
 	private void resetSelection() {
 		view.setBoldStyleReviewRecentChanges(false);
 		view.setBoldStyleDashboard(false);
 		view.setBoldStyleAddGrammatical(false);
 		view.setBoldStyleAddMeaning(false);
 		view.setBoldStyleAddExample(false);
 		view.setBoldStyleReviewBrowseEdit(false);
 		// view.setBoldStyleGatherFromOtherLanguage(false);
 		view.setBoldStyleGatherFromSemanticDomains(false);
 		view.setBoldStyleGatherFromTexts(false);
 		view.setBoldStyleGatherFromWordList(false);
 		view.setBoldStyleSettings(false);
 		eventBus.clearMessageBox();
 	}
 
 	public void onStart() {
 		resetSelection();
 		onTaskSettingChanged();		
 		if (!urlCommand())
 		{
 			//default Page!
 			view.getDashboardClickHandlers().fireEvent(new UrlCmdClickEvent());
 		}
 	}
 
 	private boolean urlCommand() {
 		RootPanel rootPanel = RootPanel.get("GWTContent");
 		if (rootPanel==null)
 		{
 			return false;
 		}
 		String urlCmdName =rootPanel.getElement().getAttribute("targetpage");
 		if (urlCmdName == null) {
 			return false;
 		}
 		urlCmdName = urlCmdName.trim().toLowerCase();
 
 		if (urlCmdName.equals("fromtexts")) {
 			if (view.getGatherFromTextsVisible()) {
 				view.setGatherWordsMenuExpended(true);
 				view.getGatherFromTextsClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		} else if (urlCmdName.equals("fromwordlist")) {
 			if (view.getGatherFromWordListVisible()) {
 				view.setGatherWordsMenuExpended(true);
 				view.getGatherFromWordListClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		} else if (urlCmdName.equals("browseandedit")) {
 			if (view.getReviewBrowseEditVisible()) {
 				view.setGatherWordsMenuExpended(true);
 				view.getReviewBrowseEditClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		} else if (urlCmdName.equals("addmeanings")) {
 			if (view.getAddMeaningVisible()) {
 				view.setAddInformationMenuExpended(true);
 				view.getAddMeaningClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		} else if (urlCmdName.equals("addgrammaticalusage")) {
 			if (view.getAddGrammaticalPanelVisible()) {
 				view.setAddInformationMenuExpended(true);
 				view.getAddGrammaticalClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		} else if (urlCmdName.equals("addexamples")) {
 			if (view.getAddExamplePanelVisible()) {
 				view.setAddInformationMenuExpended(true);
 				view.getAddExampleClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		} else if (urlCmdName.equals("setting")) {
 			if (view.getConfigureMenuVisible()) {
 				view.getSettingsClickHandlers().fireEvent(new UrlCmdClickEvent());
 			}
 		}else
 		{
 			return false;
 		}
 		return true;
 
 	}
 
 	public void onTaskSettingChanged() {
 		// hide all of them
 		// group
 		// view.setGatherWordsMenuVisible(false);
 		// view.setContributeMenuVisible(false);
 		// view.setConfigureMenuVisible(false);
 		// view.setAddInformationMenuVisible(false);
 
 		// group-elements
 		view.setDashboardMenuVisible(true);
 		
 		view.setReviewRecentChangesVisible(false);
 		view.setGatherFromTextsVisible(false);
 		view.setGatherFromWordListVisible(false);
 		view.setGatherFromSemanticDomainsVisible(false);
 		view.setReviewBrowseEditVisible(false);
 		view.setAddMeaningVisible(false);
 		view.setAddGrammaticalPanelVisible(false);
 		view.setAddExamplePanelVisible(false);
 		view.setConfigureMenuVisible(false);
 		view.setContributeMenuVisible(false);
 		
 		JsArray<SettingTasksTaskElementDto> tasksDto = SettingTasksDto
 				.getCurrentUserSetting().getEntries();
 		if (tasksDto.length() > 0) {
 			for (int i = 0; i < tasksDto.length(); i++) {
 				SettingTasksTaskElementDto taskElementDto = tasksDto.get(i);
 
 				switch (taskElementDto.getTaskName()) {
 				case NOTESBROWSER:
 				case ADVANCEDHISTORY:
 					continue;
 				case DASHBOARD:
 					view.setDashboardMenuVisible(taskElementDto.getVisible());
 					continue;
 				case ADDMISSINGINFO:
 					if (taskElementDto.getField()
 							.equalsIgnoreCase("definition")) {
 						view.setAddMeaningVisible(taskElementDto.getVisible());
 					} else if (taskElementDto.getField()
 							.equalsIgnoreCase("POS")) {
 						view.setAddGrammaticalPanelVisible(taskElementDto
 								.getVisible());
 					} else if (taskElementDto.getField().equalsIgnoreCase(
 							"ExampleSentence")) {
 						view.setAddExamplePanelVisible(taskElementDto
 								.getVisible());
 					}
 					continue;
 				case GATHERWORDLIST:
 					if (taskElementDto.getWordListFileName().equals("SILCAWL")
 							&& taskElementDto.getLongLabel().isEmpty()) {
 						view.setGatherFromWordListVisible(taskElementDto
 								.getVisible());
 					} else if (taskElementDto.getWordListFileName().isEmpty()
 							&& taskElementDto.getLongLabel().equalsIgnoreCase(
 									"from texts")) {
 						view.setGatherFromTextsVisible(taskElementDto
 								.getVisible());
 					}
 					continue;
 				case DICTIONARY:
 					view.setReviewBrowseEditVisible(taskElementDto.getVisible());
 					continue;
 				case GATHERWORDSBYSEMANTICDOMAINS:
 					view.setGatherFromSemanticDomainsVisible(taskElementDto
 							.getVisible());
 					continue;
 				case CONFIGURESETTINGS:
 					view.setConfigureMenuVisible(taskElementDto.getVisible());
 					continue;
 				case REVIEW:
 					view.setReviewRecentChangesVisible(taskElementDto
 							.getVisible());
 					continue;
 				default:
 					continue;
 				}
 			}
 		}
 	
 		UserDto user = CurrentEnvironmentDto.getCurrentUser();
 		if (user == null) {
 			ConsoleLog.log("Work with out user logged in!");
 			view.setContributeMenuVisible(true);
 			view.setConfigureMenuVisible(false);
 		}else
 		{
 			ConsoleLog.log("Work with user!");
			view.setConfigureMenuVisible(true);
 			if (PermissionManager.getPermission(DomainPermissionType.DOMAIN_PROJECTS, OperationPermissionType.CAN_EDIT_OTHER)) {
 				view.setConfigureMenuVisible(true);
 			}
 		}
 	}
 
 	protected static native void topWindowRedirect(String url) /*-{
 			window.top.location=url;
 	}-*/;
 
 }
