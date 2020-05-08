 package com.lavida.swing.handler;
 
 import com.google.gdata.util.ServiceException;
 import com.lavida.TaskProgressEvent;
 import com.lavida.service.*;
 import com.lavida.service.entity.*;
 import com.lavida.swing.exception.RemoteUpdateException;
 import com.lavida.swing.form.component.TablePrintPreviewComponent;
 import com.lavida.swing.preferences.UsersSettings;
 import com.lavida.swing.service.*;
 import com.lavida.service.settings.Settings;
 import com.lavida.service.settings.SettingsService;
 import com.lavida.swing.preferences.UsersSettingsHolder;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.dialog.*;
 import com.lavida.swing.exception.LavidaSwingRuntimeException;
 import com.lavida.swing.form.MainForm;
 import com.lavida.swing.form.component.ProgressComponent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import javax.swing.*;
 import javax.xml.bind.JAXBException;
 import java.awt.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.List;
 
 /**
  * MainFormHandler
  * <p/>
  * Created: 21:14 09.08.13
  *
  * @author Pavel
  */
 @Component
 public class MainFormHandler implements ApplicationContextAware {
     private static final Logger logger = LoggerFactory.getLogger(MainFormHandler.class);
 
     @Resource
     private MessageSource messageSource;
 
     @Resource
     private LocaleHolder localeHolder;
 
     @Resource
     private ArticleServiceSwingWrapper articleServiceSwingWrapper;
 
     @Resource
     private DiscountCardServiceSwingWrapper discountCardServiceSwingWrapper;
 
     @Resource
     private ChangedFieldServiceSwingWrapper changedFieldServiceSwingWrapper;
 
     @Resource
     private MainForm form;
 
     @Resource
     private SellDialog sellDialog;
 
     @Resource
     private PostponedChangesDialog postponedChangesDialog;
 
     @Resource
     private PostponedChangesDialogHandler postponedChangesDialogHandler;
 
     @Resource(name = "notSoldArticleTableModel")
     private ArticlesTableModel tableModel;
 
     @Resource
     private ProgressComponent progressComponent;
 
     @Resource
     private SettingsService settingsService;
 
     @Resource
     private UsersSettingsHolder usersSettingsHolder;
 
     @Resource
     private UserSettingsService userSettingsService;
 
     @Resource
     private ConcurrentOperationsService concurrentOperationsService;
 
 //    @Resource
 //    private UpdateInfoMessageDialog updateInfoMessageDialog;
 
     private ApplicationContext applicationContext;
 
     /**
      * The ActionListener for refreshButton component.
      * When button is clicked data for ArticleJdo List loads from Google spreadsheet
      * and saves to database, then it goes through filterSold() for deleting all sold
      * goods and renders to the articleTable.
      */
     public void refreshTableItemClicked() {
         final boolean needToLoadPostponed = hasPostponed();
         String postponedOperations = messageSource.getMessage("mainForm.handler.string.postponed.operations", null, localeHolder.getLocale());
         String filePath = System.getProperty("user.dir") + System.getProperty("file.separator") +
                 postponedOperations + ".xml";
         final File fileToLoad = new File(filePath);
         if (hasPostponed()) {
             postponedChangesDialogHandler.savePostponed(fileToLoad);
             postponedChangesDialogHandler.deletePostponedOperations();
         }
 
         concurrentOperationsService.startOperation("Synchronization.", new Runnable() {
             @Override
             public void run() {
                 form.setRefreshTableItemEnable(false);
                 ArticleUpdateInfo articleUpdateInfo;
                 DiscountCardsUpdateInfo discountCardsUpdateInfo;
                 try {
                     List<Long> refreshTaskTimes = Arrays.asList(6000L, 14000L, 8000L, 300L, 3000L, 100L);
                     String refreshTaskTimesString = settingsService.getSettings().getSheetRefreshTasksTimes();
                     if (refreshTaskTimesString != null && !refreshTaskTimesString.trim().isEmpty()) {
                         String[] refreshTaskTimesArray = refreshTaskTimesString.split(", ");
                         if (refreshTaskTimesArray.length == refreshTaskTimes.size()) {
                             try {
                                 List<Long> newRefreshTaskTimes = new ArrayList<>(refreshTaskTimes.size());
                                 for (String time : refreshTaskTimesArray) {
                                     newRefreshTaskTimes.add(Long.valueOf(time));
                                 }
                                 refreshTaskTimes = newRefreshTaskTimes;
                             } catch (NumberFormatException ignored) {
                             }
                         }
                     }
 
                     progressComponent.reinitialize(messageSource.getMessage("mainForm.progress.label.refresh", null, localeHolder.getLocale()))
                             .addWork(refreshTaskTimes.get(0), true).addWork(refreshTaskTimes.get(1), true).addWork(refreshTaskTimes.get(2), true)
                             .addWork(refreshTaskTimes.get(3), true).addWork(refreshTaskTimes.get(4), true).addWork(refreshTaskTimes.get(5), true)
                             .start();
                     List<ArticleJdo> articles = articleServiceSwingWrapper.loadArticlesFromRemoteServer();
                     articleUpdateInfo = articleServiceSwingWrapper.updateDatabaseFromRemote(articles);
                     applicationContext.publishEvent(new TaskProgressEvent(this, TaskProgressEvent.TaskProgressType.COMPLETE));
 
                     List<DiscountCardJdo> discountCardJdoList = discountCardServiceSwingWrapper.loadDiscountCardsFromRemoteServer();
                     applicationContext.publishEvent(new TaskProgressEvent(this, TaskProgressEvent.TaskProgressType.COMPLETE));
                     discountCardsUpdateInfo = discountCardServiceSwingWrapper.updateDatabaseFromRemote(discountCardJdoList);
                     applicationContext.publishEvent(new TaskProgressEvent(this, TaskProgressEvent.TaskProgressType.COMPLETE));
                 } catch (IOException | ServiceException e) {
                     Thread.currentThread().interrupt();
                     progressComponent.getProgressBar().setVisible(false);
                     progressComponent.getLabel().setVisible(false);
                     if (needToLoadPostponed) {
                         postponedChangesDialogHandler.loadPostponed(fileToLoad);
                     }
                     form.setRefreshTableItemEnable(true);
                     form.update();    // repaint MainForm in some time
                     throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_IO_EXCEPTION, e);
                 }
                 showUpdateInfoMessage(articleUpdateInfo, discountCardsUpdateInfo);
                 if (articleUpdateInfo.getChangedFieldJdoList() != null) {
                     changedFieldServiceSwingWrapper.update(articleUpdateInfo.getChangedFieldJdoList());
                 }
                 if (discountCardsUpdateInfo.getChangedFieldJdoList() != null) {
                     changedFieldServiceSwingWrapper.update(discountCardsUpdateInfo.getChangedFieldJdoList());
                 }
                 form.update();    // repaint MainForm in some time
                 form.setRefreshTableItemEnable(true);
 
                 Long[] correctedTaskTimes = progressComponent.getCorrectedTaskTimes();
                 StringBuilder correctedTimesBuilder = new StringBuilder();
                 for (long correctedTaskTime : correctedTaskTimes) {
                     correctedTimesBuilder.append(", ").append(correctedTaskTime);
                 }
                 Settings settings = settingsService.getSettings();
                 settings.setSheetRefreshTasksTimes(correctedTimesBuilder.toString().substring(2));
                 settingsService.saveSettings(settings);
 
                 if (needToLoadPostponed) {
                     postponedChangesDialogHandler.loadPostponed(fileToLoad);
                     showPostponedOperationsMessage();
                 }
             }
         });
     }
 
     private boolean hasPostponed() {
         for (ArticleJdo articleJdo : articleServiceSwingWrapper.getAll()) {
             if (articleJdo.getPostponedOperationDate() != null) return true;
         }
         for (DiscountCardJdo discountCardJdo : discountCardServiceSwingWrapper.getAll()) {
             if (discountCardJdo.getPostponedDate() != null) return true;
         }
         return false;
     }
 
     /**
      * Shows a JOptionPane message with the information about the articles updating  process.
      *
      * @param articleUpdateInfo       the ArticleUpdateInfo to be shown.
      * @param discountCardsUpdateInfo the DiscountCardsUpdateInfo to be shown.
      */
     private void showUpdateInfoMessage(ArticleUpdateInfo articleUpdateInfo, DiscountCardsUpdateInfo discountCardsUpdateInfo) {
         StringBuilder messageBuilder = new StringBuilder();
         if (articleUpdateInfo.getAddedCount() > 0 || discountCardsUpdateInfo.getAddedCount() > 0) {
             messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.added",
                     null, localeHolder.getLocale()));
             if (articleUpdateInfo.getAddedCount() > 0) {
                 messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.added",
                         null, localeHolder.getLocale()));
                 messageBuilder.append(articleUpdateInfo.getAddedCount());
                 messageBuilder.append(", ");
             }
             if (discountCardsUpdateInfo.getAddedCount() > 0) {
                 messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.discountCards.added",
                         null, localeHolder.getLocale()));
                 messageBuilder.append(discountCardsUpdateInfo.getAddedCount());
             }
             messageBuilder.append(". \n");
         }
         if (articleUpdateInfo.getUpdatedCount() > 0 || discountCardsUpdateInfo.getUpdatedCount() > 0) {
             messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.updated",
                     null, localeHolder.getLocale()));
             if (articleUpdateInfo.getUpdatedCount() > 0) {
                 messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.added",
                         null, localeHolder.getLocale()));
                 messageBuilder.append(articleUpdateInfo.getUpdatedCount());
                 messageBuilder.append(", ");
             }
             if (discountCardsUpdateInfo.getUpdatedCount() > 0) {
                 messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.discountCards.added",
                         null, localeHolder.getLocale()));
                 messageBuilder.append(discountCardsUpdateInfo.getUpdatedCount());
             }
             messageBuilder.append(". \n");
         }
 
         if (articleUpdateInfo.getDeletedCount() > 0 || discountCardsUpdateInfo.getDeletedCount() > 0) {
             messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.deleted",
                     null, localeHolder.getLocale()));
             if (articleUpdateInfo.getDeletedCount() > 0) {
                 messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.added",
                         null, localeHolder.getLocale()));
                 messageBuilder.append(articleUpdateInfo.getDeletedCount());
                 messageBuilder.append(", ");
             }
             if (discountCardsUpdateInfo.getDeletedCount() > 0) {
                 messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.discountCards.added",
                         null, localeHolder.getLocale()));
                 messageBuilder.append(discountCardsUpdateInfo.getDeletedCount());
             }
             messageBuilder.append(". \n");
         }
         String message = convertToMultiline(new String(messageBuilder));
 //        updateInfoMessageDialog.showUpdateInfoMessage(message);
         form.showInfoToolTip(message);
 //        form.showInformationMessage("mainForm.panel.refresh.message.title", message);
     }
 
     private String convertToMultiline(String orig) {
         return "<html>" + orig.replaceAll("\n", "<br>"); //+ "</html>"
     }
 
     public void sellButtonClicked() {
         if (tableModel.getSelectedArticle() != null) {
             sellDialog.initWithArticleJdo(tableModel.getSelectedArticle());
             sellDialog.show();
 
         } else {
             form.showWarningMessage("mainForm.exception.message.dialog.title", "mainForm.handler.sold.article.not.chosen");
         }
     }
 
     /**
      * Shows postponedOperationMessageLabel in the statusBarPanel of the mainForm.
      */
     public void showPostponedOperationsMessage() {
         int count = 0;
         List<ArticleJdo> articles = articleServiceSwingWrapper.getAll();
         for (ArticleJdo articleJdo : articles) {
             if (articleJdo.getPostponedOperationDate() != null) {
                 ++count;
             }
         }
         List<DiscountCardJdo> discountCardJdoList = discountCardServiceSwingWrapper.getAll();
         for (DiscountCardJdo discountCardJdo : discountCardJdoList) {
             if (discountCardJdo.getPostponedDate() != null) {
                 ++count;
             }
         }
         if (count > 0) {
             form.getPostponedMessage().setText(String.valueOf(count));
             form.getPostponedMessage().setVisible(true);
             form.getPostponedOperations().setVisible(true);
             postponedChangesDialog.getSavePostponedItem().setEnabled(true);
             postponedChangesDialog.getRecommitPostponedItem().setEnabled(true);
             postponedChangesDialog.getDeletePostponedItem().setEnabled(true);
         } else {
             form.getPostponedMessage().setText(String.valueOf(count));
             form.getPostponedMessage().setVisible(false);
             form.getPostponedOperations().setVisible(false);
             postponedChangesDialog.getSavePostponedItem().setEnabled(false);
             postponedChangesDialog.getRecommitPostponedItem().setEnabled(false);
             postponedChangesDialog.getDeletePostponedItem().setEnabled(false);
         }
     }
 
 
     public void printItemClicked() {
         TablePrintPreviewComponent tablePrintPreviewComponent = new TablePrintPreviewComponent();
         boolean done = tablePrintPreviewComponent.showPrintPreviewDialog(form.getForm(), form.getArticleTableComponent().getArticlesTable(),
                 messageSource, localeHolder);
         if (done) {
             form.showInformationMessage("mainForm.menu.table.print.message.title",
                     messageSource.getMessage("mainForm.menu.table.print.finished.message.body", null, localeHolder.getLocale()));
         } else {
             form.showInformationMessage("mainForm.menu.table.print.message.title",
                     messageSource.getMessage("mainForm.menu.table.print.cancel.message.body", null, localeHolder.getLocale()));
         }
         form.getArticleTableComponent().initTableColumnsEditors();
     }
 
     public void savePresetItemClicked() {
 //        String defaultPresetName = messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale());
 //        if (!defaultPresetName.equals(usersSettingsHolder.getPresetName())) {
             form.holdAllTables();
             UsersSettings usersSettings = userSettingsService.updatePresetSettings();
             try {
                 userSettingsService.saveSettings(usersSettings);
             } catch (IOException | JAXBException e) {
                 throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.JAXB_EXCEPTION, e);
             }
 //        }
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
     }
 
     public void fixTableDataItemClicked() {
 // todo fixTableData
     }
 
     public void moveToShopItemClicked() {
         String[] shopArray = new String[tableModel.getShopService().getAll().size()];
         List<ShopJdo> shopJdoList = tableModel.getShopService().getAll();
         for (int i = 0; i < shopJdoList.size(); ++i) {
             shopArray[i] = shopJdoList.get(i).getName();
         }
         Arrays.sort(shopArray);
         String shop = (String) (form.showInputDialog("mainForm.menu.selected.moveToShop.select.title", "mainForm.menu.selected.moveToShop.select.message",
                 null, shopArray, shopArray[0]));
         if (shop != null) {
             moveToShop(shop);
         }
     }
 
     private void moveToShop(final String shop) {
         concurrentOperationsService.startOperation("Moving articles to the " + shop, new Runnable() {
             @Override
             public void run() {
                 StringBuilder stringBuilder = new StringBuilder();
                 stringBuilder.append(messageSource.getMessage("mainForm.moveToShop.finished.message", null, localeHolder.getLocale()));
                 stringBuilder.append("\n");
                 boolean postponedExist = false;
                 RemoteUpdateException exception = null;
                 for (ArticleJdo articleJdo : form.getTableModel().getTableData()) {
                     if (articleJdo.isSelected()) {
                         ArticleJdo oldArticle;
                         try {
                             oldArticle = (ArticleJdo) articleJdo.clone();
                         } catch (CloneNotSupportedException e) {
                             throw new RuntimeException(e);
                         }
                         articleJdo.setShop(shop);
                         articleJdo.setSelected(false);
                         try {
                             articleServiceSwingWrapper.updateToSpreadsheet(oldArticle, articleJdo, null);
                         } catch (RemoteUpdateException e) {
                             exception = e;
                             postponedExist = true;
                         }
                     }
                 }
                 String message = convertToMultiline(new String(stringBuilder));
                 form.showInfoToolTip(message);
                 form.update();
                 if (postponedExist) {
                     throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_SERVICE_EXCEPTION, exception);
                 }
 
             }
         });
     }
 
     public void deselectArticlesItemClicked() {
         for (ArticleJdo articleJdo : form.getTableModel().getTableData()) {
             if (articleJdo.isSelected()) {
                 articleJdo.setSelected(false);
             }
         }
         form.update();
     }
 
     public void createDefaultPreset() {
         String defaultPresetName = messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale());
         usersSettingsHolder.setPresetName(defaultPresetName);
         form.holdAllTables();
         UsersSettings usersSettings = userSettingsService.updatePresetSettings();
         try {
             userSettingsService.saveSettings(usersSettings);
         } catch (IOException | JAXBException e) {
             throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.JAXB_EXCEPTION, e);
         }
 //        createPreset(defaultPresetName);
     }
 
 
 /*
     public void selectPresetItemClicked() {
         String currentPresetName = usersSettingsHolder.getPresetName();
         Object[] selectionValues = userSettingsService.getUserPresetNames().toArray();
         String presetName = (String) form.showInputDialog("mainForm.menu.settings.select.title", "mainForm.menu.settings.select.message",
                 null, selectionValues, currentPresetName);
         if (presetName != null) {
             usersSettingsHolder.setPresetName(presetName);
             form.initializeUserSettings();
             userSettingsService.getUserSettings().setLastPresetName(presetName);
             form.updatePresetNameField();
             try {
                 userSettingsService.saveSettings(usersSettingsHolder.getUsersSettings());
             } catch (IOException | JAXBException e) {
                 throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.JAXB_EXCEPTION, e);
             }
         }
     }
 */
 
     public void createPresetItemClicked() {
         String presetName = (String) form.showInputDialog("mainForm.menu.settings.create.title", "mainForm.menu.settings.create.message",
                 null, null, null);
         JRadioButtonMenuItem presetItem = createPreset(presetName);
         form.getPresetButtonGroup().clearSelection();
         presetItem.setSelected(true);
     }
 
     public JRadioButtonMenuItem createPreset(String presetName) {
         JRadioButtonMenuItem presetItem = createRadioButtonPresetMenuItem(presetName);
         form.getPresetMenuItemMap().put(presetName, presetItem);
         form.getSelectPresetMenu().add(presetItem);
         form.getPresetButtonGroup().add(presetItem);
 
         return presetItem;
     }
 
     private JRadioButtonMenuItem createRadioButtonPresetMenuItem(final String presetName) {
         JRadioButtonMenuItem presetItem = new JRadioButtonMenuItem(presetName);
         presetItem.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (e.getStateChange() == ItemEvent.DESELECTED) {
 
                 } else if (e.getStateChange() == ItemEvent.SELECTED) {
                     usersSettingsHolder.setPresetName(presetName);
                     form.initializeUserSettings();
                     userSettingsService.getUserSettings().setLastPresetName(presetName);
                     form.updatePresetNameField();
                     try {
                         userSettingsService.saveSettings(usersSettingsHolder.getUsersSettings());
                     } catch (IOException | JAXBException exception) {
                         throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.JAXB_EXCEPTION, exception);
                     }
 
                 }
             }
         });
         return presetItem;
     }
 
     public void deletePresetItemClicked() {
         String currentPresetName = usersSettingsHolder.getPresetName();
         String defaultPresetName = messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale());
         Object[] presetNames = userSettingsService.getUserPresetNames().toArray();
         if (presetNames.length < 2) {
             Toolkit.getDefaultToolkit().beep();
             form.showWarningMessage("mainForm.exception.message.dialog.title", "mainForm.handler.delete.usersSettings.error.message");
             return;
         }
         Object[] selectionValues;
         if (currentPresetName.equals(defaultPresetName)) {
             selectionValues = new Object[presetNames.length - 1];
         } else {
             selectionValues = new Object[presetNames.length - 2];
         }
         int order = 0;
         for (Object preset : presetNames) {
             String presetName = (String) preset;
             if (!(currentPresetName.equals(presetName) || defaultPresetName.equals(presetName))) {
                 selectionValues[order] = presetName;
                 ++order;
             }
         }
         String presetName = (String) form.showInputDialog("mainForm.menu.settings.delete.title", "mainForm.menu.settings.select.message",
                 null, selectionValues, selectionValues[0]);
         if (presetName != null) {
             userSettingsService.deletePresetSettings(presetName);
             try {
                 userSettingsService.saveSettings(usersSettingsHolder.getUsersSettings());
             } catch (IOException | JAXBException e) {
                 throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.JAXB_EXCEPTION, e);
             }
             JRadioButtonMenuItem deletedPreset = form.getPresetMenuItemMap().get(presetName);
             form.getSelectPresetMenu().remove(deletedPreset);
             form.getPresetButtonGroup().remove(deletedPreset);
             form.getPresetMenuItemMap().remove(presetName);
         }
 
     }
 
 
 }
