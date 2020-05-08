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
 package fr.peralta.mycellar.application.wine.impl;
 
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import fr.peralta.mycellar.application.wine.WineService;
 import fr.peralta.mycellar.domain.shared.repository.CountEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.domain.wine.Wine;
 import fr.peralta.mycellar.domain.wine.WineColorEnum;
 import fr.peralta.mycellar.domain.wine.WineTypeEnum;
 import fr.peralta.mycellar.domain.wine.repository.WineOrder;
 import fr.peralta.mycellar.domain.wine.repository.WineRepository;
 
 /**
  * @author speralta
  */
 @Service
 public class WineServiceImpl implements WineService {
 
     private WineRepository wineRepository;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long countWines(SearchForm searchForm) {
         return wineRepository.countWines(searchForm);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Wine> getWines(SearchForm searchForm, WineOrder orders, int first, int count) {
         return wineRepository.getWines(searchForm, orders, first, count);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Map<WineTypeEnum, Long> getTypes(SearchForm searchForm, CountEnum count) {
        return wineRepository.getTypes(searchForm, count);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Map<WineColorEnum, Long> getColors(SearchForm searchForm, CountEnum count) {
        return wineRepository.getColors(searchForm, count);
     }
 
     /**
      * @param wineRepository
      *            the wineRepository to set
      */
     @Autowired
     public void setWineRepository(WineRepository wineRepository) {
         this.wineRepository = wineRepository;
     }
 
 }
