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
 
 package org.investovator.ann.data.datanormalizing;
 
 import org.investovator.ann.config.ConfigReceiver;
 import org.investovator.ann.nngaming.util.GameTypes;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 
 /**
  * @author: Hasala Surasinghe
  * @version: ${Revision}
  */
 public class NormalizationModelSerializerTest {
 
     @Before
     public void setUp() throws Exception {
 
         ConfigReceiver configReceiver = new ConfigReceiver();
         configReceiver.setBasePath("src/test");
 
     }
 
     @After
     public void tearDown() throws Exception {
 
     }
 
     @Test
     public void testSaveModel() throws Exception {
         NormalizationModelSerializer modelSerializer = new NormalizationModelSerializer();
         NormalizationModel model = new NormalizationModel();
         modelSerializer.saveModel(model, TradingDataAttribute.CLOSING_PRICE.toString(),"SAMP", GameTypes.TRADING_GAME);
        assert (new File("resources/SAMP").exists());
     }
 
     @Test
     public void testReadModel() throws Exception {
         NormalizationModelSerializer modelSerializer = new NormalizationModelSerializer();
         NormalizationModel model = modelSerializer.readModel(TradingDataAttribute.CLOSING_PRICE.toString(),"SAMP",GameTypes.TRADING_GAME);
         assert (model instanceof NormalizationModel);
     }
 }
