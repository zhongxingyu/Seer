 package com.lavida.swing.handler;
 
 import com.google.gdata.util.ServiceException;
 import com.lavida.service.UserService;
 import com.lavida.service.entity.ArticleJdo;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.dialog.*;
 import com.lavida.swing.exception.LavidaSwingRuntimeException;
 import com.lavida.swing.form.MainForm;
 import com.lavida.swing.form.component.FileChooserComponent;
 import com.lavida.swing.service.ArticleServiceSwingWrapper;
 import com.lavida.service.ArticleUpdateInfo;
 import com.lavida.swing.service.ArticlesTableModel;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.bind.JAXBException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * MainFormHandler
  * <p/>
  * Created: 21:14 09.08.13
  *
  * @author Pavel
  */
 @Component
 public class MainFormHandler {
 
     @Resource
     private MessageSource messageSource;
 
     @Resource
     private LocaleHolder localeHolder;
 
     @Resource
     private ArticleServiceSwingWrapper articleServiceSwingWrapper;
 
     @Resource
     private MainForm form;
 
     @Resource
     private SellDialog sellDialog;
 
     @Resource
     private SoldProductsDialog soldProductsDialog;
 
     @Resource
     private AddNewProductsDialog addNewProductsDialog;
 
     @Resource
     private ColumnsViewSettingsDialog columnsViewSettingsDialog;
 
     @Resource
     private AllDiscountCardsDialog allDiscountCardsDialog;
 
     @Resource
     private AddNewDiscountCardsDialog addNewDiscountCardsDialog;
 
     @Resource(name = "notSoldArticleTableModel")
     private ArticlesTableModel tableModel;
 
     @Resource
     private UserService userService;
 
     @Resource
     private FileChooserComponent fileChooser;
 
 
     /**
      * The ActionListener for refreshButton component.
      * When button is clicked data for ArticleJdo List loads from Google spreadsheet
      * and saves to database, then it goes through filterSold() for deleting all sold
      * goods and renders to the articleTable.
      */
     public void refreshButtonClicked() {
         try {
             List<ArticleJdo> articles = articleServiceSwingWrapper.loadArticlesFromRemoteServer();
             ArticleUpdateInfo informer = articleServiceSwingWrapper.updateDatabaseFromRemote(articles);
             showUpdateInfoMessage(informer);
             form.update();    // repaint MainForm in some time
 
         } catch (IOException e) {
             throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_IO_EXCEPTION, e, form);
         } catch (ServiceException e) {
             throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_SERVICE_EXCEPTION, e, form);
         }
     }
 
     /**
      * Shows a JOptionPane message with the information about the articles updating  process.
      *
      * @param informer the ArticleUpdateInfo to be shown.
      */
     private void showUpdateInfoMessage(ArticleUpdateInfo informer) {
         StringBuilder messageBuilder = new StringBuilder();
         messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.added",
                 null, localeHolder.getLocale()));
         messageBuilder.append(informer.getAddedCount());
         messageBuilder.append(". \n");
         messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.updated",
                 null, localeHolder.getLocale()));
         messageBuilder.append(informer.getUpdatedCount());
         messageBuilder.append(". \n");
         messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.deleted",
                 null, localeHolder.getLocale()));
         messageBuilder.append(informer.getDeletedCount());
         messageBuilder.append(". \n");
 
         form.showMessageBox("mainForm.panel.refresh.message.title", new String(messageBuilder));
     }
 
     public void sellButtonClicked() {
         if (tableModel.getSelectedArticle() != null) {
             sellDialog.initWithArticleJdo(tableModel.getSelectedArticle());
             sellDialog.show();
 
         } else {
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.handler.sold.article.not.chosen");
         }
     }
 
     public void recommitPostponedItemClicked() {
         List<ArticleJdo> articles = articleServiceSwingWrapper.getAll();
         for (ArticleJdo articleJdo : articles) {
             if (articleJdo.getPostponedOperationDate() != null) {
                 try {
                     if (articleJdo.getSold() != null) {   //recommit selling
                         articleServiceSwingWrapper.updateToSpreadsheet(articleJdo, new Boolean(true));
                     } else if (articleJdo.getSold() == null && articleJdo.getRefundDate() != null) {  // recommit refunding
                         articleServiceSwingWrapper.updateToSpreadsheet(articleJdo, new Boolean(false));
                         articleJdo.setSaleDate(null);
                     } else {
                         articleServiceSwingWrapper.updateToSpreadsheet(articleJdo, null); // recommit other changes
                     }
                     articleJdo.setPostponedOperationDate(null);
                     articleServiceSwingWrapper.update(articleJdo);
                     showPostponedOperationsMessage();
                 } catch (IOException e) {
                     form.showMessage("mainForm.exception.message.dialog.title", "sellDialog.handler.sold.article.not.saved.to.worksheet");
                     showPostponedOperationsMessage();
                     throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_IO_EXCEPTION, e, form);
                 } catch (ServiceException e) {
                     form.showMessage("mainForm.exception.message.dialog.title", "sellDialog.handler.sold.article.not.saved.to.worksheet");
                     showPostponedOperationsMessage();
                     throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_SERVICE_EXCEPTION, e, form);
                 }
             }
         }
         form.update();
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
         if (count > 0) {
             form.getPostponedMessage().setText(String.valueOf(count));
             form.getPostponedMessage().setVisible(true);
             form.getPostponedOperations().setVisible(true);
             form.getSavePostponedItem().setEnabled(true);
             form.getRecommitPostponedItem().setEnabled(true);
             form.getDeletePostponedItem().setEnabled(true);
         } else {
             form.getPostponedMessage().setText(String.valueOf(count));
             form.getPostponedMessage().setVisible(false);
             form.getPostponedOperations().setVisible(false);
             form.getSavePostponedItem().setEnabled(false);
             form.getRecommitPostponedItem().setEnabled(false);
             form.getDeletePostponedItem().setEnabled(false);
         }
     }
 
     public void showSoldProductsButtonClicked() {
         soldProductsDialog.show();
     }
 
     /**
      * Saves the List{@code <}{@link ArticleJdo}{@code >} with postponed operation to chosen xml file.
      *
      * @param file the chosen xml file.
      */
     public void savePostponed(File file) {
         List<ArticleJdo> articlesPostponed = new ArrayList<ArticleJdo>();
         List<ArticleJdo> articlesAll = articleServiceSwingWrapper.getAll();
         for (ArticleJdo articleJdo : articlesAll) {
             if (articleJdo.getPostponedOperationDate() != null) {
                 articlesPostponed.add(articleJdo);
             }
         }
         try {
             articleServiceSwingWrapper.saveToXml(articlesPostponed, file);
         } catch (JAXBException e) {
             e.printStackTrace();
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.exception.xml.JAXB.message");
         } catch (IOException e) {
             e.printStackTrace();
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.exception.io.xml.file");
         }
     }
 
     /**
      * Shows a dialog for choosing the file for saving postponed operations.
      */
     public void savePostponedItemClicked() {
         fileChooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
         fileChooser.setSelectedFile(new File("postponed_" +
                 new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".xml"));
         File file;
         while (true) {
             int choice = fileChooser.showSaveDialog(form.getForm());
             if (choice == JFileChooser.APPROVE_OPTION) {
                 file = fileChooser.getSelectedFile();
                 if (!fileChooser.isValidFile(file)) {
                     fileChooser.showWarningMessage("mainForm.exception.message.dialog.title",
                             "mainForm.handler.fileChooser.fileName.format.message");
                     fileChooser.setSelectedFile(new File("postponed_" +
                             new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".xml"));
                     continue;
                 }
 
                 if (!fileChooser.isFileFilterSelected()) {
                     fileChooser.showWarningMessage("mainForm.exception.message.dialog.title",
                             "mainForm.handler.fileChooser.fileFilter.selection.message");
                     continue;
                 }
 
                 file = fileChooser.improveFileExtension(file);
 
                 if (file.exists()) {
                     int result = fileChooser.showConfirmDialog("mainForm.handler.fileChooser.file.exists.dialog.title",
                             "mainForm.handler.fileChooser.file.exists.dialog.message");
                     switch (result) {
                         case JOptionPane.YES_OPTION:
                             break;
                         case JOptionPane.NO_OPTION:
                             continue;
                         case JOptionPane.CLOSED_OPTION:
                             continue;
                         case JOptionPane.CANCEL_OPTION:
                             fileChooser.setSelectedFile(new File("postponed_" +
                                     new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".xml"));
                             fileChooser.cancelSelection();
                             return;
                     }
                 }
                 break;
             } else {
                 fileChooser.setSelectedFile(new File("postponed_" +
                         new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".xml"));
                 fileChooser.cancelSelection();
                 return;
             }
         }
         savePostponed(file);
         form.update();
         fileChooser.setSelectedFile(new File("postponed_" +
                 new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".xml"));
     }
 
     /**
      * Deletes all postponed operations from the database.
      */
     public void deletePostponedItemClicked() {
         int result = form.showConfirmDialog("mainForm.menu.postponed.delete.message.title",
                 "mainForm.menu.postponed.delete.message.body");
         switch (result) {
             case JOptionPane.YES_OPTION:
                 deletePostponedOperations();
                 form.update();
             case JOptionPane.NO_OPTION:
                 return;
             case JOptionPane.CLOSED_OPTION:
                 return;
         }
 
     }
 
     /**
      * Deletes all postponed operations from the database and updates the PostponedOperationsMessage label of the
      * mainForm.
      */
     private void deletePostponedOperations() {
         List<ArticleJdo> allArticles = articleServiceSwingWrapper.getAll();
         for (ArticleJdo articleJdo : allArticles) {
             if (articleJdo.getPostponedOperationDate() != null) {
                 articleJdo.setPostponedOperationDate(null);
                 articleServiceSwingWrapper.update(articleJdo);
             }
         }
         showPostponedOperationsMessage();
     }
 
     /**
      * Shows a dialog for choosing the xml file with postponed operations and loads them to the database.
      */
     public void loadPostponedItemClicked() {
         fileChooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
         fileChooser.setSelectedFile(null);
         File file;
         while (true) {
             int choice = fileChooser.showOpenDialog(form.getForm());
             if (choice == JFileChooser.APPROVE_OPTION) {
                 file = fileChooser.getSelectedFile();
                 if (!fileChooser.isValidFile(file)) {
                     fileChooser.showWarningMessage("mainForm.exception.message.dialog.title",
                             "mainForm.handler.fileChooser.fileName.format.message");
                     fileChooser.setSelectedFile(null);
                     continue;
                 }
 
                 if (!fileChooser.isFileFilterSelected()) {
                     fileChooser.showWarningMessage("mainForm.exception.message.dialog.title",
                             "mainForm.handler.fileChooser.fileFilter.selection.message");
                     continue;
                 }
                 break;
             } else {
                 fileChooser.setSelectedFile(null);
                 fileChooser.cancelSelection();
                 return;
             }
         }
         loadPostponed(file);
         form.update();
         fileChooser.setSelectedFile(null);
     }
 
     /**
      * Loads postponed operations to the database.
      *
      * @param file the chosen xml file with postponed operations to be loaded.
      */
     private void loadPostponed(File file) {
         List<ArticleJdo> loadedArticles = null;
         try {
             loadedArticles = articleServiceSwingWrapper.loadFromXml(file);
         } catch (JAXBException e) {
             e.printStackTrace();
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.exception.xml.JAXB.message");
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.exception.io.xml.file");
         }
         List<ArticleJdo> forUpdateArticles = articleServiceSwingWrapper.mergePostponedWithDatabase(loadedArticles);
         if (loadedArticles.size() > 0) {
             for (ArticleJdo articleJdo : forUpdateArticles) {
                 articleServiceSwingWrapper.update(articleJdo);
             }
             showPostponedOperationsMessage();
         } else {
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.handler.postponed.articles.not.exist.message");
         }
     }
 
     /**
      * Opens s dialog for adding new  products.
      */
     public void addNewProductsItemClicked() {
         addNewProductsDialog.show();
     }
 
     public void articleColumnsViewItemClicked() {
         columnsViewSettingsDialog.show();
     }
 
     public ColumnsViewSettingsDialog getColumnsViewSettingsDialog() {
         return columnsViewSettingsDialog;
     }
 
     public void addNewDiscountCardItemClicked() {
         addNewDiscountCardsDialog.show();
     }
 
     public void allDiscountCardsItemClicked() {
         allDiscountCardsDialog.show();
     }
 }
