 /*
  * investovator, Stock Market Gaming Framework
  *     Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.investovator.controller.command.dataplayback;
 
 import org.investovator.controller.command.exception.CommandExecutionException;
 import org.investovator.core.data.api.utils.StockTradingData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.core.data.exeptions.DataNotFoundException;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class GetDataUpToTodayCommand extends DataPlaybackGameCommand {
 
     //stores the results
     StockTradingData result;
 
     //stores the required parameters
     String stock;
     Date startingDate;
     ArrayList<TradingDataAttribute> attributes;
 
     public GetDataUpToTodayCommand(String stock, Date startingDate, ArrayList<TradingDataAttribute> attributes) {
         this.stock = stock;
         this.startingDate = startingDate;
         this.attributes = attributes;
     }
 
     @Override
     public void execute() throws CommandExecutionException {
         try {
             result=facade.getDataUpToToday(stock,startingDate,attributes);
         } catch (DataAccessException e) {
             e.printStackTrace();
             throw new CommandExecutionException(e.getMessage());
         } catch (DataNotFoundException e) {
             e.printStackTrace();
             throw new CommandExecutionException(e.getMessage());
         }
 
     }

    public StockTradingData getResult(){
        return result;
    }
 }
