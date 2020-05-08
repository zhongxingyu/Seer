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
 package fr.peralta.mycellar.interfaces.facades.stock;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.peralta.mycellar.application.stock.StockService;
import fr.peralta.mycellar.domain.stock.Cellar;
 import fr.peralta.mycellar.domain.stock.Input;
 import fr.peralta.mycellar.interfaces.facades.shared.MapperServiceFacade;
 
 /**
  * @author speralta
  */
 public class StockServiceFacadeImpl implements StockServiceFacade {
 
     private StockService stockService;
 
     private MapperServiceFacade mapperServiceFacade;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void arrival(Arrival arrival) {
         List<Input> inputs = new ArrayList<Input>();
         float unitCharges = arrival.getOtherCharges() / arrival.getBottles().size();
        for (Bottle bottleDto : arrival.getBottles()) {
            inputs.add(new Input(arrival.getDate(), (Cellar) null, mapperServiceFacade.map(
                    bottleDto, fr.peralta.mycellar.domain.stock.Bottle.class), bottleDto
                    .getQuantity(), bottleDto.getPrice(), arrival.getSource(), unitCharges));
         }
         stockService.stock(inputs);
     }
 
     /**
      * @param stockService
      *            the stockService to set
      */
     public void setStockService(StockService stockService) {
         this.stockService = stockService;
     }
 
     /**
      * @param mapperServiceFacade
      *            the mapperServiceFacade to set
      */
     public void setMapperServiceFacade(MapperServiceFacade mapperServiceFacade) {
         this.mapperServiceFacade = mapperServiceFacade;
     }
 
 }
