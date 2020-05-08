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
 import fr.peralta.mycellar.domain.wine.Region;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.AjaxTool;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.autocomplete.SimpleIdentifiedEntityTypeahead;
 import fr.peralta.mycellar.interfaces.client.web.shared.FilterEnumHelper;
 import fr.peralta.mycellar.interfaces.facades.wine.WineServiceFacade;
 
 /**
  * @author speralta
  */
 public class RegionSimpleTypeahead extends SimpleIdentifiedEntityTypeahead<Region> {
 
     private static final long serialVersionUID = 201107252130L;
 
     private static final String COUNTRY_COMPONENT_ID = "country";
 
     @SpringBean
     private WineServiceFacade wineServiceFacade;
 
     private final CountrySimpleTypeahead countrySimpleTypeahead;
 
     /**
      * @param id
      * @param label
      * @param searchFormModel
      * @param filters
      */
     public RegionSimpleTypeahead(String id, IModel<String> label,
             IModel<SearchForm> searchFormModel, FilterEnum... filters) {
        super(id, label, searchFormModel, filters);
         add(countrySimpleTypeahead = new CountrySimpleTypeahead(COUNTRY_COMPONENT_ID,
                 new StringResourceModel("country", this, null), searchFormModel,
                 FilterEnumHelper.removeFilter(filters, FilterEnum.REGION)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Region> getChoices(String term) {
         return wineServiceFacade.getRegionsLike(term, getSearchFormModel().getObject(),
                 getFilters());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean isReadyToSelect() {
         return countrySimpleTypeahead.isValued();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Region createDefaultObject() {
         Region region = new Region();
         region.setCountry(countrySimpleTypeahead.getModelObject());
         return region;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onModelChanged(IEvent<?> event) {
         getSearchFormModel().getObject().replaceSet(FilterEnum.COUNTRY,
                 countrySimpleTypeahead.getModelObject());
         if (!countrySimpleTypeahead.isValued()) {
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
         return FilterEnum.REGION;
     }
 
 }
