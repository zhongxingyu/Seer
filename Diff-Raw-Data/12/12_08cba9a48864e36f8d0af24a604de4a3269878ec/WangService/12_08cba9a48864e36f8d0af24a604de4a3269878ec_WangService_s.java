 package com.robonobo.core.wang;
 
 import static com.robonobo.common.util.TimeUtil.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.doomdark.uuid.UUID;
 import org.doomdark.uuid.UUIDGenerator;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.proto.CoreApi.Node;
 import com.robonobo.core.service.AbstractService;
 import com.robonobo.wang.WangException;
 import com.robonobo.wang.beans.CoinList;
 import com.robonobo.wang.client.WangClient;
 import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
 
 public class WangService extends AbstractService implements CurrencyClient {
 	RobonoboWangConfig config;
 	WangClient client;
 	Log log = LogFactory.getLog(getClass());
 	double cachedBankBalance = 0;
 	Date nextCheckBalanceTime = new Date(0);
 	boolean clientStarted = false;
 
 	public WangService() {
 		addHardDependency("core.users");
 		addHardDependency("core.tasks");
 	}
 
 	public String getName() {
 		return "Wang banking service";
 	}
 
 	public String getProvides() {
 		return "core.wang";
 	}
 
 	@Override
 	public void startup() throws Exception {
 		config = (RobonoboWangConfig) getRobonobo().getConfig("wang");
 		File coinStoreDir = new File(getRobonobo().getHomeDir(), "coins");
 		coinStoreDir.mkdirs();
 		config.setCoinStoreDir(coinStoreDir.getAbsolutePath());
 		// Start client on successful login
 		getRobonobo().getEventService().addUserPlaylistListener(new LoginListener());
 	}
 
 	private class StartClientTask extends Task {
 		public StartClientTask() {
 			title = "Starting wang currency client";
 		}
 		
 		@Override
 		public void runTask() throws Exception {
 			if (client != null)
 				client.stop();
 
 			User me = getRobonobo().getUsersService().getMyUser();
 			config.setAccountEmail(me.getEmail());
 			config.setAccountPwd(me.getPassword());
 			statusText = "Initializing client";
 			fireUpdated();
 			client = new WangClient(config);
 			client.start();
 			clientStarted = true;
 			completion = 0.5f;
 			statusText = "Updating account balance";
 			fireUpdated();
 			updateBalanceIfNecessary(true);
 			completion = 1f;
 			statusText = "Done.";
 			fireUpdated();
			getRobonobo().getEventService().fireWangBalanceChanged(cachedBankBalance);
 		}
 	}
 	
 	@Override
 	public void shutdown() throws Exception {
 		clientStarted = false;
 		if (client != null)
 			client.stop();
 	}
 
 	public boolean isReady() {
 		return clientStarted;
 	}
 	
 	public String getAcceptPaymentMethods() {
 		// TODO Add in escrow nodes ("escrow:<nodeid>,escrow:<nodeid>") here
 		return "upfront";
 	}
 	
 	public Node[] getTrustedEscrowNodes() {
 		return new Node[0];
 	}
 	
 	public String currencyUrl() {
 		return config.getCurrencyUrl();
 	}
 
 	public double getBidIncrement() {
 		return Math.pow(2,config.getBidIncrement());
 	}
 
 	public double getMinBid() {
 		return Math.pow(2,config.getMinBid());
 	}
 
 	public int getMinTopRate() {
 		return config.getMinTopRate();
 	}
 
 	public double getOpeningBalance() {
 		return Math.pow(2, config.getOpeningBalance());
 	}
 
 	public double getMaxBid(StreamVelocity sv) {
 		switch (sv) {
 		case LowestCost:
 			return Math.pow(2, config.getLowestCostMaxBid());
 		case MaxRate:
 			return Math.pow(2, config.getMaxRateMaxBid());
 		default:
 			throw new SeekInnerCalmException();
 		}
 	}
 
 	public double getBankBalance() throws CurrencyException {
 		return getBankBalance(false);
 	}
 
 	public double getBankBalance(boolean forceUpdate) throws CurrencyException {
 		updateBalanceIfNecessary(forceUpdate);
 		return cachedBankBalance;
 	}
 
 	public double getOnHandBalance() throws CurrencyException {
 		return client.getOnHandBalance();
 	}
 	
 	public double depositToken(byte[] token, String narration) throws CurrencyException {
 		try {
 			CoinListMsg coins = CoinListMsg.parseFrom(token);
 			client.putCoins(coins);
 			double result = CoinList.totalValue(coins);
 			synchronized (this) {
 				cachedBankBalance += result;
 			}
 			fireAccountActivity(result, narration);
 			return result;
 		} catch (WangException e) {
 			throw new CurrencyException(e);
 		} catch (IOException e) {
 			throw new CurrencyException(e);
 		}
 	}
 
 	public byte[] withdrawToken(double value, String narration) throws CurrencyException {
 		try {
 			CoinListMsg coins = client.getCoins(value);
 			synchronized (this) {
 				cachedBankBalance -= CoinList.totalValue(coins);
 			}
 			fireAccountActivity(0 - value, narration);
 			return coins.toByteArray();
 		} catch (WangException e) {
 			throw new CurrencyException(e);
 		}
 	}
 
 	private void fireAccountActivity(final double creditValue, final String narration) {
 		getRobonobo().getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				updateBalanceIfNecessary(false);
 				getRobonobo().getEventService().fireWangAccountActivity(creditValue, narration);
				getRobonobo().getEventService().fireWangBalanceChanged(cachedBankBalance);
 			}
 		});		
 	}
 	
 	private void updateBalanceIfNecessary(boolean forceUpdate) {
 		synchronized (this) {
 			if (forceUpdate || now().after(nextCheckBalanceTime)) {
 				try {
 					cachedBankBalance = client.getAccurateBankBalance();
 				} catch (WangException e) {
 					log.error("Error updating bank balance", e);
 				}
 				nextCheckBalanceTime = timeInFuture(config.getBankBalanceCacheTime() * 1000);
 			}
 		}
 	}
 
 	class LoginListener implements UserPlaylistListener {
 		public void loggedIn() {
 			getRobonobo().getTaskService().runTask(new StartClientTask());
 		}
 
 		public void playlistChanged(Playlist p) {
 			// Do nothing
 		}
 
 		public void userChanged(User u) {
 			// Do nothing
 		}
 		
 		public void libraryUpdated(long userId, Library lib) {
 			// Do nothing
 		}
 		
 		public void libraryChanged(Library lib) {
 			// Do nothing
 		}
 		
 		@Override
 		public void allUsersAndPlaylistsUpdated() {
 			// Do nothing
 		}
 		
 		@Override
 		public void userConfigChanged(UserConfig cfg) {
 			// Do nothing
 		}
 	}
 }
