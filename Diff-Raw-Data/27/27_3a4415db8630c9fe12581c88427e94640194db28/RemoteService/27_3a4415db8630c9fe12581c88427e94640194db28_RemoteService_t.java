 package com.lavida.service.remote;
 
 import com.google.gdata.data.spreadsheet.CellEntry;
 import com.google.gdata.data.spreadsheet.CellFeed;
 import com.google.gdata.util.ServiceException;
 import com.lavida.service.entity.ArticleJdo;
 import com.lavida.service.remote.google.GoogleCellEntriesIterator;
 import com.lavida.service.remote.google.GoogleCellsTransformer;
 import com.lavida.service.remote.google.GoogleSpreadsheetWorker;
 import com.lavida.service.settings.SettingsHolder;
 import com.lavida.utils.ReflectionUtils;
 import org.springframework.stereotype.Service;
 import org.springframework.util.StringUtils;
 
 import javax.annotation.Resource;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * RemoteService
  * Created: 12:43 05.08.13
  *
  * @author Pavel
  */
 @Service
 public class RemoteService {
 
     @Resource
     private SettingsHolder settingsHolder;
 
     @Resource
     private GoogleCellsTransformer cellsTransformer;
 
     public List<ArticleJdo> loadArticles() throws ServiceException, IOException {
         GoogleSpreadsheetWorker worker = new GoogleSpreadsheetWorker(settingsHolder.getSettings());
         CellFeed cellFeed = worker.getWholeDocument();
 
         GoogleCellEntriesIterator cellEntriesIterator = new GoogleCellEntriesIterator(cellFeed.getEntries());
         Map<Integer, String> headers = cellsTransformer.cellsToColNumsAndItsValueMap(cellEntriesIterator.getNextLine());
         cellsTransformer.cellsToColNumsAndItsValueMap(cellEntriesIterator.getNextLine());   // header titles.
 
         List<ArticleJdo> articles = new ArrayList<ArticleJdo>();
         while (cellEntriesIterator.hasNext()) {
             ArticleJdo articleJdo = cellsTransformer.cellsToArticleJdo(cellEntriesIterator.getNextLine(), headers);
             articles.add(articleJdo);
         }
         return articles;
     }
 
     public void updateArticle(ArticleJdo articleJdo, Boolean isSold) throws IOException, ServiceException {
         GoogleSpreadsheetWorker spreadsheetWorker = new GoogleSpreadsheetWorker(settingsHolder.getSettings());
 
         // take headers
         CellFeed headersFeed = spreadsheetWorker.getRow(1);
         GoogleCellEntriesIterator cellEntriesIterator = new GoogleCellEntriesIterator(headersFeed.getEntries());
         Map<Integer, String> headers = cellsTransformer.cellsToColNumsAndItsValueMap(cellEntriesIterator.getNextLine());
 
         // take articles feed and check (by code and size) if all correct
         CellFeed articleFeed = spreadsheetWorker.getRow(articleJdo.getSpreadsheetRow());
         String codeColumnHeader = ReflectionUtils.getFieldAnnotation(ArticleJdo.class, "code", SpreadsheetColumn.class).column();
         String codeColumnValue = cellsTransformer.cellToValue(articleFeed, headers, codeColumnHeader);
         String sizeColumnHeader = ReflectionUtils.getFieldAnnotation(ArticleJdo.class, "size", SpreadsheetColumn.class).column();
         String sizeColumnValue = cellsTransformer.cellToValue(articleFeed, headers, sizeColumnHeader);
         String soldColumnHeader = ReflectionUtils.getFieldAnnotation(ArticleJdo.class, "sold", SpreadsheetColumn.class).column();
         String soldColumnValue = cellsTransformer.cellToValue(articleFeed, headers, soldColumnHeader);
 //        String nameColumnHeader = ReflectionUtils.getFieldAnnotation(ArticleJdo.class, "name", SpreadsheetColumn.class).column();
 //        String nameColumnValue = cellsTransformer.cellToValue(articleFeed, headers, nameColumnHeader);
 
         // ArticleJdo.spreadsheetRow has incorrect value -> search for correct
         if (isSold != null && isSold.booleanValue()) {    // for selling
            if ((codeColumnValue != null) ? !codeColumnValue.equals(articleJdo.getCode()) :
                    articleJdo.getCode() != null
                            || (sizeColumnValue != null) ? !sizeColumnValue.equals(articleJdo.getSize()) :
                            articleJdo.getSize() != null
                                    || soldColumnValue != null && !StringUtils.isEmpty(soldColumnValue)
                     ) {
                 Integer newSpreadsheetRowIndex = null;
                 List<ArticleJdo> realArticles = loadArticles();
                 for (ArticleJdo realArticleJdo : realArticles) {
                    if ((realArticleJdo.getCode() != null)? realArticleJdo.getCode().equals(articleJdo.getCode()) :
                            articleJdo.getCode() == null
                            && (realArticleJdo.getSize() != null)? realArticleJdo.getSize().equals(articleJdo.getSize()):
                                    articleJdo.getSize() == null
                             && StringUtils.isEmpty(realArticleJdo.getSold())) {
                         newSpreadsheetRowIndex = realArticleJdo.getSpreadsheetRow();
                     }
                 }
 
                 if (newSpreadsheetRowIndex != null) {
                     articleJdo.setSpreadsheetRow(newSpreadsheetRowIndex);
 
                 } else {
                     throw new RuntimeException("Updating article doesn't exist anymore!");
                 }
             } else if (isSold != null && !isSold.booleanValue()) {  //for refunding  , code != null
                 if ((codeColumnValue != null) ? !codeColumnValue.equals(articleJdo.getCode()) :
                         articleJdo.getCode() != null
                                 || (sizeColumnValue != null) ? !sizeColumnValue.equals(articleJdo.getSize()) :
                                 articleJdo.getSize() != null
                                         || soldColumnValue == null && StringUtils.isEmpty(soldColumnValue)
                         ) {
                     Integer newSpreadsheetRowIndex = null;
                     List<ArticleJdo> realArticles = loadArticles();
                     for (ArticleJdo realArticleJdo : realArticles) {
                         if ((realArticleJdo.getCode() != null) ? realArticleJdo.getCode().equals(articleJdo.getCode()) :
                                 articleJdo.getCode() == null
                                         && (realArticleJdo.getSize() != null) ? realArticleJdo.getSize().equals(articleJdo.getSize()) :
                                         articleJdo.getSize() == null
                                                 && !StringUtils.isEmpty(realArticleJdo.getSold())) {
                             newSpreadsheetRowIndex = realArticleJdo.getSpreadsheetRow();
                         }
                     }
                     if (newSpreadsheetRowIndex != null) {
                         articleJdo.setSpreadsheetRow(newSpreadsheetRowIndex);
                     } else {
                         throw new RuntimeException("Updating article doesn't exist anymore!");
                     }
                 }
             } else {   // no selling nor refunding, but simple changing
                 if ((codeColumnValue != null) ? !codeColumnValue.equals(articleJdo.getCode()) :
                         articleJdo.getCode() != null
                                 || (sizeColumnValue != null) ? !sizeColumnValue.equals(articleJdo.getSize()) :
                                 articleJdo.getSize() != null
                         ) {
                     Integer newSpreadsheetRowIndex = null;
                     List<ArticleJdo> realArticles = loadArticles();
                     for (ArticleJdo realArticleJdo : realArticles) {
                         if ((realArticleJdo.getCode() != null) ? realArticleJdo.getCode().equals(articleJdo.getCode()) :
                                 articleJdo.getCode() == null
                                         && (realArticleJdo.getSize() != null) ? realArticleJdo.getSize().equals(articleJdo.getSize()) :
                                         articleJdo.getSize() == null
                                                && (realArticleJdo.getSold() != null) ? realArticleJdo.getSold().equals(articleJdo.getSold()) :
                                                articleJdo.getSold() == null
                                 ) {
                             newSpreadsheetRowIndex = realArticleJdo.getSpreadsheetRow();
                         }
                     }
                     if (newSpreadsheetRowIndex != null) {
                         articleJdo.setSpreadsheetRow(newSpreadsheetRowIndex);
                     } else {
                         throw new RuntimeException("Updating article doesn't exist anymore!");
                     }
                 }
             }
         }
         // transform and update.
         List<CellEntry> cellEntriesForUpdate = cellsTransformer.articleToCellEntriesForUpdate(articleJdo, headers, articleFeed);
         spreadsheetWorker.saveOrUpdateCells(cellEntriesForUpdate);
     }
 
 }
