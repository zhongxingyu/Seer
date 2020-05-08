 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.client.web.components.stock.tab;
 
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.event.IEventSource;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.peralta.mycellar.domain.shared.exception.BusinessException;
 import fr.peralta.mycellar.domain.shared.repository.CountEnum;
 import fr.peralta.mycellar.domain.shared.repository.FilterEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.stock.Cellar;
 import fr.peralta.mycellar.domain.stock.CellarShare;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.Action;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.ActionLink;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.AjaxTool;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.SearchFormModel;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.data.AdvancedTable;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.tab.TabAdvancedTablePanel;
 import fr.peralta.mycellar.interfaces.client.web.components.stock.data.CellarShareDataView;
 import fr.peralta.mycellar.interfaces.client.web.components.stock.form.CellarShareForm;
 import fr.peralta.mycellar.interfaces.client.web.shared.FormValidationHelper;
 import fr.peralta.mycellar.interfaces.client.web.shared.LoggingHelper;
 import fr.peralta.mycellar.interfaces.facades.stock.StockServiceFacade;
 
 /**
  * @author speralta
  */
 public class CellarShareTabPanel extends TabAdvancedTablePanel<CellarShare> {
 
     private static final long serialVersionUID = 201201241804L;
     private static final Logger logger = LoggerFactory.getLogger(CellarShareTabPanel.class);
     private static final String CELLAR_SHARE_COMPONENT_ID = "cellarShare";
 
     @SpringBean
     private StockServiceFacade stockServiceFacade;
 
     private CellarShareForm cellarShareForm;
 
     private final SearchFormModel searchFormModel;
 
     /**
      * @param id
      * @param cellarModel
      */
     public CellarShareTabPanel(String id, IModel<Cellar> cellarModel) {
         super(id, cellarModel);
         searchFormModel = new SearchFormModel(new SearchForm());
         setOutputMarkupId(true);
         add(cellarShareForm = new CellarShareForm(CELLAR_SHARE_COMPONENT_ID, searchFormModel,
                new CellarShare(), CountEnum.WINE));
        cellarShareForm.hideForm();
     }
 
     @SuppressWarnings("unchecked")
     public IModel<Cellar> getModel() {
         return (IModel<Cellar>) getDefaultModel();
     }
 
     public Cellar getModelObject() {
         return (Cellar) getDefaultModelObject();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected AdvancedTable<CellarShare> createAdvancedTable(String tableId) {
         return new CellarShareDataView(tableId, new SearchFormModel(new SearchForm().addToSet(
                 FilterEnum.CELLAR, getModelObject())));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onEvent(IEvent<?> event) {
         LoggingHelper.logEventReceived(logger, event);
         if (event.getPayload() instanceof Action) {
             Action action = (Action) event.getPayload();
             IEventSource source = event.getSource();
             switch (action) {
             case ADD:
                 if (source instanceof ActionLink) {
                     cellarShareForm.displayForm();
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             case DELETE:
                 if (source instanceof ActionLink) {
                     CellarShare cellarShare = (CellarShare) ((ActionLink) source).getParent()
                             .getDefaultModelObject();
                     stockServiceFacade.deleteCellarShare(cellarShare);
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             case SAVE:
                 if (cellarShareForm == source) {
                     CellarShare cellarShareToSave = cellarShareForm.getModelObject();
                     cellarShareToSave.setCellar(getModelObject());
                     try {
                         stockServiceFacade.saveCellarShare(cellarShareToSave);
                         cellarShareForm = new CellarShareForm(CELLAR_SHARE_COMPONENT_ID,
                                searchFormModel, new CellarShare(), CountEnum.WINE);
                        cellarShareForm.hideForm();
                         replace(cellarShareForm);
                     } catch (BusinessException e) {
                         FormValidationHelper.error(cellarShareForm, e);
                     }
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             case SELECT:
                 if (source instanceof ActionLink) {
                     CellarShare cellarShare = (CellarShare) ((ActionLink) source).getParent()
                             .getDefaultModelObject();
                     cellarShareForm = new CellarShareForm(CELLAR_SHARE_COMPONENT_ID,
                             searchFormModel, cellarShare, CountEnum.WINE);
                     cellarShareForm.displayForm();
                     replace(cellarShareForm);
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             case CANCEL:
                 if (cellarShareForm.isCancelButton(source)) {
                     cellarShareForm = new CellarShareForm(CELLAR_SHARE_COMPONENT_ID,
                            searchFormModel, new CellarShare(), CountEnum.WINE);
                    cellarShareForm.hideForm();
                     replace(cellarShareForm);
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             default:
                 break;
             }
         }
         LoggingHelper.logEventProcessed(logger, event);
     }
 
 }
