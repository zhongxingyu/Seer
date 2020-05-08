 /**
  * Copyright (c) 2013 Christian Pelster.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Christian Pelster - initial API and implementation
  */
 package de.pellepelster.myadmin.client.gwt.modules.dictionary.editor;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import de.pellepelster.gwt.commons.client.HumanizedMessagePopup;
 import de.pellepelster.gwt.commons.client.HumanizedMessagePopup.MESSAGE_TYPE;
 import de.pellepelster.myadmin.client.base.db.vos.IBaseVO;
 import de.pellepelster.myadmin.client.gwt.GwtStyles;
 import de.pellepelster.myadmin.client.gwt.modules.IGwtModuleUI;
 import de.pellepelster.myadmin.client.gwt.modules.dictionary.ActionBar;
 import de.pellepelster.myadmin.client.gwt.modules.dictionary.BaseDictionaryModuleUI;
 import de.pellepelster.myadmin.client.gwt.modules.dictionary.DictionaryEditorPanel;
 import de.pellepelster.myadmin.client.web.MyAdmin;
 import de.pellepelster.myadmin.client.web.modules.dictionary.editor.DictionaryEditorModule;
 import de.pellepelster.myadmin.client.web.modules.dictionary.editor.IEditorUpdateListener;
 
 /**
  * UI for the navigation module
  * 
  * @author pelle
  * 
  */
public class DictionaryEditorModuleUI<VOType extends IBaseVO> extends BaseDictionaryModuleUI<DictionaryEditorModule<VOType>>
 {
 	private final VerticalPanel verticalPanel;
 
 	private static final String DICTIONARY_SAVE_BUTTON_DEBUG_ID = "DictionarySaveButton";
 
 	private static final String DICTIONARY_BACK_BUTTON_DEBUG_ID = "DictionaryBackButton";
 
 	private static final String DICTIONARY_REFRESH_BUTTON_DEBUG_ID = "DictionaryRefreshButton";
 
 	@SuppressWarnings("rawtypes")
 	public DictionaryEditorModuleUI(DictionaryEditorModule<VOType> editorModule, final IGwtModuleUI previousModuleUI)
 	{
 		super(editorModule);
 
 		verticalPanel = new VerticalPanel();
 
 		verticalPanel.addStyleName(GwtStyles.DEBUG_BORDER);
 		verticalPanel.ensureDebugId(DictionaryEditorModule.MODULE_ID + "-" + editorModule.getModuleName());
 		verticalPanel.setWidth("100%");
 
 		// - action panel ------------------------------------------------------
 		ActionBar actionBar = new ActionBar();
 		verticalPanel.add(actionBar);
 
 		// - title -------------------------------------------------------------
		final HTML editorTitle = new HTML(editorModule.getTitle());
 		editorTitle.addStyleName(GwtStyles.EDITOR_TITLE);
 		verticalPanel.add(editorTitle);
 
 		DictionaryEditorPanel<VOType> dictionaryEditorPanel = new DictionaryEditorPanel<VOType>(getModule());
 		verticalPanel.add(dictionaryEditorPanel);
 
 		if (previousModuleUI != null)
 		{
 
 			actionBar.addSingleButton(MyAdmin.RESOURCES.back(), MyAdmin.MESSAGES.editorBack(), new ClickHandler()
 			{
 				/** {@inheritDoc} */
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					MyAdmin.getInstance().getLayoutFactory().showModuleUI(previousModuleUI);
 				}
 			}, DictionaryEditorModule.MODULE_ID + "-" + getModule().getDictionaryModel().getName() + "-" + DICTIONARY_BACK_BUTTON_DEBUG_ID);
 
 			actionBar.addSpacer(2);
 		}
 
 		actionBar.addSingleButton(MyAdmin.RESOURCES.editorSave(), MyAdmin.MESSAGES.editorSave(), new ClickHandler()
 		{
 			/** {@inheritDoc} */
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				save();
 			}
 		}, DictionaryEditorModule.MODULE_ID + "-" + getModule().getDictionaryModel().getName() + "-" + DICTIONARY_SAVE_BUTTON_DEBUG_ID);
 
 		final Button refreshButton = actionBar.addSingleButton(MyAdmin.RESOURCES.editorRefresh(), MyAdmin.MESSAGES.editorRefresh(), new ClickHandler()
 		{
 			/** {@inheritDoc} */
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				getModule().getDictionaryEditor().refresh();
 			}
 
 		}, DictionaryEditorModule.MODULE_ID + "-" + getModule().getDictionaryModel().getName() + "-" + DICTIONARY_REFRESH_BUTTON_DEBUG_ID);
 
 		// refreshButton.setEnabled(false);
 
		getModule().addUpdateListener(new IEditorUpdateListener()
		{

			@Override
			public void onUpdate()
			{
				// refreshButton.setEnabled(!baseVO.isNew());
				editorTitle.setText(getModule().getTitle());
			}
		});
 
 	}
 
 	private void save()
 	{
 		if (getModule().getDictionaryEditor().getValidationMessages().hasErrors())
 		{
 			HumanizedMessagePopup.showMessageAndFadeAfterMouseMove(MyAdmin.MESSAGES.editorContainsErrors(), MESSAGE_TYPE.ERROR);
 		}
 		else
 		{
 			getModule().getDictionaryEditor().save();
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean close()
 	{
 
 		if (getModule().getDictionaryEditor().isDirty())
 		{
 			return Window.confirm(MyAdmin.MESSAGES.editorClose());
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Panel getContainer()
 	{
 		return verticalPanel;
 	}
 
 	@Override
 	public String getTitle()
 	{
 		return getModule().getTitle();
 	}
 
 }
