 package com.cee.news.client.content;
 
 import com.cee.news.client.error.ErrorHandler;
 import com.cee.news.client.list.ListChangedEvent;
 import com.cee.news.client.list.ListChangedHandler;
 import com.cee.news.model.EntityKey;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.view.client.MultiSelectionModel;
 
 public class SourceSelectionPresenter {
 
     public SourceSelectionPresenter(final SourceSelectionView sourceSelectionView, final SiteListContentModel sitesOfWorkingSetModel, ErrorHandler errorHandler) {
         final MultiSelectionCellListPresenter<EntityKey> siteListPresenter = new MultiSelectionCellListPresenter<EntityKey>(sourceSelectionView.getCellListSites(), sitesOfWorkingSetModel, sitesOfWorkingSetModel);
         siteListPresenter.addErrorHandler(errorHandler);
         final MultiSelectionModel<EntityContent<EntityKey>> selectionModel = siteListPresenter.getSelectionModel();
         sourceSelectionView.getSelectAllButton().addClickHandler(new ClickHandler() {
             
             @Override
             public void onClick(ClickEvent event) {
                 for (EntityKey key : sitesOfWorkingSetModel.getKeys()) {
                     selectionModel.setSelected(new EntityContent<EntityKey>(key, null), true);
                 }
             }
         });
         sourceSelectionView.getSelectNoneButton().addClickHandler(new ClickHandler() {
             
             @Override
             public void onClick(ClickEvent event) {
                 selectionModel.clear();
             }
         });
         sitesOfWorkingSetModel.addListChangedHandler(new ListChangedHandler<EntityKey>() {
             
             @Override
             public void onContentListChanged(ListChangedEvent<EntityKey> event) {
                 for (EntityKey key : sitesOfWorkingSetModel.getKeys()) {
                     selectionModel.setSelected(new EntityContent<EntityKey>(key, null), true);
                 }
             }
         });
     }
     
 }
