 package org.palaso.languageforge.client.lex.configure.presenter;
 
 import org.palaso.languageforge.client.lex.common.IPersistable;
 import org.palaso.languageforge.client.lex.model.CurrentEnvironmentDto;
 import org.palaso.languageforge.client.lex.model.ProjectDto;
 import org.palaso.languageforge.client.lex.model.UserDto;
 import org.palaso.languageforge.client.lex.common.I18nConstants;
 import org.palaso.languageforge.client.lex.configure.ConfigureEventBus;
 import org.palaso.languageforge.client.lex.configure.view.ConfigureSettingPropertiesView;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = ConfigureSettingPropertiesView.class)
 public class ConfigureSettingPropertiesPresenter
 		extends
 		BasePresenter<ConfigureSettingPropertiesPresenter.IConfigureSettingPropertiesView, ConfigureEventBus>
 		implements IPersistable {
 	@Inject
 	public ILexService lexService;
 
 	public void bind() {
 
 	}
 
 	public void onAttachPropertiesView(SimplePanel simplePanel) {
 		simplePanel.clear();
 		simplePanel.add(view.getWidget());
 		eventBus.addSubControl(simplePanel, this);
 		refersh();
 	}
 
 	protected void refersh() {
 		view.getNameTextBox().setText(
 				CurrentEnvironmentDto.getCurrentProject().getProjectTitle());
 		view.getTypeTextBox().setText(
 				CurrentEnvironmentDto.getCurrentProject().getProjectType());
 	}
 
 	public interface IConfigureSettingPropertiesView {
 		public Widget getWidget();
 
 		public TextBox getNameTextBox();
 
 		public TextBox getTypeTextBox();
 	}
 
 	@Override
 	public boolean persistData(UserDto user) {
 		 
 		if (view.getNameTextBox().getText().trim().isEmpty()) {
 			eventBus.displayMessage(I18nConstants.STRINGS.ConfigureSettingPropertiesPresenter_Project_name_can_not_be_empty());
 			return false;
 		}
 
 		AsyncCallback<ProjectDto> asyncCallback = new AsyncCallback<ProjectDto>() {
 
 			@Override
 			public void onSuccess(ProjectDto result) {
				CurrentEnvironmentDto.getCurrentProject().setProjectTitle(
						result.getProjectTitle());
 				eventBus.displayMessage(I18nConstants.STRINGS.ConfigureSettingPropertiesPresenter_Name_changed_you_may_need_to_reload_page_to_see_changes());
 				//eventBus.reloadLex();			
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				eventBus.handleError(caught);
 			}
 		};
 		lexService.updateProjectName(view.getNameTextBox().getText().trim(),
 				CurrentEnvironmentDto.getCurrentProject().getProjectId(),
 				asyncCallback);
 		return true;
 	}
 
 	@Override
 	public boolean isMultiUserSupported() {
 		return false;
 	}
 
 	@Override
 	public boolean isPersistDataByParentSupported() {
 		return true;
 	}
 }
