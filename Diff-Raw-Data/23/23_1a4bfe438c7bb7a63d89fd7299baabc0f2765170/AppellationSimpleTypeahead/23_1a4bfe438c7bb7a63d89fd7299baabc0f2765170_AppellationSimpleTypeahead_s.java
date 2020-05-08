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
 package fr.peralta.mycellar.interfaces.client.web.components.wine.autocomplete;
 
 import java.util.List;
 
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import fr.peralta.mycellar.domain.shared.repository.FilterEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.wine.Appellation;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.AjaxTool;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.autocomplete.SimpleIdentifiedEntityTypeahead;
 import fr.peralta.mycellar.interfaces.client.web.shared.FilterEnumHelper;
 import fr.peralta.mycellar.interfaces.facades.wine.WineServiceFacade;
 
 /**
  * @author speralta
  */
 public class AppellationSimpleTypeahead extends SimpleIdentifiedEntityTypeahead<Appellation> {
 
     private static final long serialVersionUID = 201107252130L;
 
     private static final String REGION_COMPONENT_ID = "region";
 
     @SpringBean
     private WineServiceFacade wineServiceFacade;
 
     private final RegionSimpleTypeahead regionSimpleTypeahead;
 
     /**
      * @param id
      * @param label
      * @param searchFormModel
      * @param filters
      */
     public AppellationSimpleTypeahead(String id, IModel<String> label,
             IModel<SearchForm> searchFormModel, FilterEnum... filters) {
        super(id, label, searchFormModel);
         add(regionSimpleTypeahead = new RegionSimpleTypeahead(REGION_COMPONENT_ID,
                 new StringResourceModel("region", this, null), searchFormModel,
                 FilterEnumHelper.removeFilter(filters, FilterEnum.REGION)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Appellation> getChoices(String term) {
         return wineServiceFacade.getAppellationsLike(term, getSearchFormModel().getObject(),
                 getFilters());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean isReadyToSelect() {
         return regionSimpleTypeahead.isValued();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Appellation createDefaultObject() {
         Appellation region = new Appellation();
         region.setRegion(regionSimpleTypeahead.getModelObject());
         return region;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onModelChanged(IEvent<?> event) {
         getSearchFormModel().getObject().replaceSet(FilterEnum.REGION,
                 regionSimpleTypeahead.getModelObject());
         if (!regionSimpleTypeahead.isValued()) {
             markAsNonValued();
         }
         AjaxTool.ajaxReRender(this);
         event.stop();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected FilterEnum getFilterToReplace() {
         return FilterEnum.APPELLATION;
     }
 
 }
