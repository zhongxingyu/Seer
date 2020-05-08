 package com.williamsinteractive.casino.wager.wiring;
 
 import com.williamsinteractive.casino.wager.WagerServiceConfiguration;
 import com.williamsinteractive.casino.wager.core.FakeMoneyService;
 import com.williamsinteractive.casino.wager.core.FakeWagerRoundArchiver;
 import com.williamsinteractive.casino.wager.core.MoneyService;
 import com.williamsinteractive.casino.wager.core.SynchronousWagerRoundManager;
 import com.williamsinteractive.casino.wager.core.WagerRoundArchiver;
 import com.williamsinteractive.casino.wager.core.VoltWagerRoundStateStore;
 import com.williamsinteractive.casino.wager.core.WagerRoundManager;
 import com.williamsinteractive.casino.wager.core.WagerRoundStateStore;
 import com.williamsinteractive.casino.wager.health.VoltDbHealthCheck;
 import com.williamsinteractive.casino.wager.resources.WagerResource;
 import dagger.Module;
 import dagger.Provides;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.voltdb.client.Client;
 import org.voltdb.client.ClientFactory;
 
 import java.io.IOException;
 
 /**
  * TODO: document!
  *
  * @author Petter Måhlén
  */
 @Module(
     entryPoints = {WagerResource.class,
                    VoltDbHealthCheck.class }
 )
 public class WagerServiceModule {
     private final Logger LOGGER = LoggerFactory.getLogger(WagerServiceModule.class);
     private final WagerServiceConfiguration configuration;
 
     public WagerServiceModule(WagerServiceConfiguration configuration) {
         this.configuration = configuration;
     }
 
     @Provides
     public WagerRoundManager wagerRoundManager(WagerRoundStateStore wagerRoundStateStore,
                                                MoneyService moneyService,
                                                WagerRoundArchiver archiver) {
         return new SynchronousWagerRoundManager(wagerRoundStateStore, moneyService, archiver);
     }
 
     @Provides
     public WagerRoundStateStore wagerRoundStateStore(Client client) {
         return new VoltWagerRoundStateStore(client);
     }
 
     @Provides
     public Client connectedVoltClient() {
         LOGGER.info("Creating volt client");
         Client client = ClientFactory.createClient();
         try {
             LOGGER.info("connecting to {}:{}", configuration.getVoltHost(), configuration.getVoltPort());
             client.createConnection(configuration.getVoltHost(), configuration.getVoltPort());
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
         return client;
     }
 
     @Provides
     public MoneyService moneyService() {
         return new FakeMoneyService(100);
     }
 
     @Provides
     public WagerRoundArchiver transactionArchiver() {
         return new FakeWagerRoundArchiver();
     }
 }
