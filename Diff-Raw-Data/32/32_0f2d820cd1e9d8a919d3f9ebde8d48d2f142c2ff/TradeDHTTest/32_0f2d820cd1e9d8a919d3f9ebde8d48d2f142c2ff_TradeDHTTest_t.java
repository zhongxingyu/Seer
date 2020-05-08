 package org.bitprivacy;
 
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.bitprivacy.SimpleMix.BlindingValues;
 import org.junit.Test;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSchema;
 import com.dustyneuron.txmarket.TestPartyData;
 import com.dustyneuron.txmarket.TestPartyData.Coin;
 import com.dustyneuron.txmarket.TestPartyData.CoinData;
 import com.dustyneuron.txmarket.dht.MockDHT;
 import com.dustyneuron.txmarket.dht.MockDHTStore;
 import com.dustyneuron.txmarket.dht.TradeDHT;
 import com.dustyneuron.txmarket.dht.TradeListingKey;
 import com.dustyneuron.txmarket.schema.SchemaUtils;
 import com.dustyneuron.txmarket.schema.Trade;
 import com.dustyneuron.txmarket.schema.TradeListing;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.core.Utils;
 
 public class TradeDHTTest {
     final static NetworkParameters params = NetworkParameters.testNet();
     final static MockDHTStore mockStore = new MockDHTStore();
     
     TradeDHT getDHT(int i) throws Exception {
         return new MockDHT(mockStore, i);
     }
     
     Trade createTrade(String coinValue, Coin coin, int numParties) {
         CoinData coinData = TestPartyData.getCoinData(coin);
         Trade t1 = SimpleMix
                 .createTrade(
                         coinData.blockId,
                         coinData.txId,
                         coinData.txOutput,
                         Utils.toNanoCoins(TestPartyData.coinValue),
                         null,
                         numParties,
                         BlindingValues.OUTPUTS);
         return t1;
     }
 
     @Test
     public void initialListingValid() throws Exception {
         int numParties = 2;
 
         Trade t1 = createTrade(TestPartyData.coinValue, Coin.ONE, numParties);
         TransactionSchema s = t1.getSchema();
                 
         assertNotNull(t1);
         assertTrue(SchemaUtils.areIdentical(s, t1.getSchema()));
         
         TradeDHT dht = getDHT(0);
         
         TradeListing l1 = new TradeListing(t1.getAllPartiesData(), dht.getPeerId());
        assertTrue(SchemaUtils.areIdentical(l1.getData(), l1.createBlindListing().getData()));
         
        dht.addTradeListing(s, l1.createBlindListing());
         
         List<TradeListingKey> listingKeys = dht.getTradeListingKeys(s);
         assertTrue(listingKeys.size() == 1);
         
         TradeListing l = dht.getTradeListing(listingKeys.get(0));
         assertTrue(
                 "Trade listing from DHT invalid",
                 SchemaUtils.isInitialListingValid(s, l.getData().getAllPartiesDataList()));
     }
 
 }
 
