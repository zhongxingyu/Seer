 /**
  * Copyright 2011 multibit.org
  *
  * Licensed under the MIT license (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://opensource.org/licenses/mit-license.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.multibit.network;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.security.SecureRandom;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.SimpleTimeZone;
 import java.util.Stack;
 
 import javax.swing.SwingWorker;
 
 import org.multibit.MultiBit;
 import org.multibit.controller.MultiBitController;
 import com.google.bitcoin.crypto.EncrypterDecrypter;
 import com.google.bitcoin.crypto.EncrypterDecrypterScrypt;
 import com.google.bitcoin.crypto.ScryptParameters;
 import org.multibit.file.FileHandlerException;
 import org.multibit.file.WalletSaveException;
 import com.google.bitcoin.core.WalletVersionException;
 import org.multibit.message.Message;
 import org.multibit.message.MessageManager;
 import org.multibit.model.MultiBitModel;
 import org.multibit.model.PerWalletModelData;
 import org.multibit.model.StatusEnum;
 import org.multibit.model.WalletInfo;
 import com.google.bitcoin.core.WalletMajorVersion;
 import org.multibit.store.ReplayableBlockStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.spongycastle.crypto.params.KeyParameter;
 
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.AddressFormatException;
 import com.google.bitcoin.core.Block;
 import com.google.bitcoin.core.ECKey;
 import com.google.bitcoin.core.MultiBitBlockChain;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.core.PeerAddress;
 import com.google.bitcoin.core.PeerGroup;
 import com.google.bitcoin.core.ProtocolException;
 import com.google.bitcoin.core.ScriptException;
 import com.google.bitcoin.core.StoredBlock;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Utils;
 import com.google.bitcoin.core.VerificationException;
 import com.google.bitcoin.core.Wallet;
 import com.google.bitcoin.discovery.IrcDiscovery;
 import com.google.bitcoin.store.BlockStoreException;
 
 /**
  * <p>
  * MultiBitService encapsulates the interaction with the bitcoin netork
  * including: o Peers o Block chain download o sending / receiving bitcoins
 
  * The testnet can be slow or flaky as it's a shared resource. You can use the
  * <a href="http://sourceforge
  * .net/projects/bitcoin/files/Bitcoin/testnet-in-a-box/">testnet in a box</a>
  * to do everything purely locally.
  * </p>
  */
 public class MultiBitService {
     private static final Logger log = LoggerFactory.getLogger(MultiBitService.class);
 
     private static final int NUMBER_OF_MILLISECOND_IN_A_SECOND = 1000;
     public static final int MAXIMUM_EXPECTED_LENGTH_OF_ALTERNATE_CHAIN = 6;
 
     public static final String MULTIBIT_PREFIX = "multibit";
     public static final String TESTNET_PREFIX = "testnet";
     public static final String TESTNET3_PREFIX = "testnet3";
     public static final String SEPARATOR = "-";
 
     public static final String BLOCKCHAIN_SUFFIX = ".blockchain";
     public static final String WALLET_SUFFIX = ".wallet";
 
     public static final String IRC_CHANNEL_TEST = "#bitcoinTEST";
     public static final String IRC_CHANNEL_TESTNET3 = "#bitcoinTEST3";
 
     static boolean restartListenerHasBeenAddedToPeerGroup = false;
 
     public Logger logger = LoggerFactory.getLogger(MultiBitService.class.getName());
 
     private Wallet wallet;
 
     private MultiBitPeerGroup peerGroup;
 
     private String blockchainFilename;
 
     private MultiBitBlockChain blockChain;
 
     private ReplayableBlockStore blockStore;
 
     private MultiBitController controller;
 
     private final NetworkParameters networkParameters;
     
     private SecureRandom secureRandom = new SecureRandom();
 
     public static Date genesisBlockCreationDate;
     
     static {
         try {
             java.text.SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             java.util.Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
             format.setCalendar(cal);
             genesisBlockCreationDate = format.parse("2009-01-03 18:15:05");
         } catch (ParseException e) {
             // Will never happen.
             e.printStackTrace();
         }
     }
 
     /**
      * 
      * @param controller
      *            MutliBitController
      */
     public MultiBitService(MultiBitController controller) {
         this(controller.getModel().getUserPreference(MultiBitModel.WALLET_FILENAME), controller);
     }
 
     /**
      * 
      * @param walletFilename
      *            filename of current wallet
      * @param controller
      *            MutliBitController
      */
     public MultiBitService(String walletFilename, MultiBitController controller) {
         this.controller = controller;
         
         if (controller == null) {
             throw new IllegalStateException("controller cannot be null");
         }
 
         if (controller.getModel() == null) {
             throw new IllegalStateException("controller.getModel() cannot be null");
         }
         
         if (controller.getApplicationDataDirectoryLocator() == null) {
             throw new IllegalStateException("controller.getApplicationDataDirectoryLocator() cannot be null");
         }
         
         if (controller.getFileHandler() == null) {
             throw new IllegalStateException("controller.getFileHandler() cannot be null");
         }
 
         networkParameters = controller.getModel().getNetworkParameters();
         log.debug("Network parameters = " + networkParameters);
         
         try {
             // Load the block chain.
             String filePrefix = getFilePrefix();
             log.debug("filePrefix = " + filePrefix);
 
             if ("".equals(controller.getApplicationDataDirectoryLocator().getApplicationDataDirectory())) {
                 blockchainFilename = filePrefix + BLOCKCHAIN_SUFFIX;
             } else {
                 blockchainFilename = controller.getApplicationDataDirectoryLocator().getApplicationDataDirectory() + File.separator
                         + filePrefix + BLOCKCHAIN_SUFFIX;
             }
 
             // Check to see if the user has a blockchain and copy over the sinstalled one if they do not.
             controller.getFileHandler().copyBlockChainFromInstallationDirectory(blockchainFilename, false);
 
             log.debug("Reading block store '{}' from disk", blockchainFilename);
 
             blockStore = new ReplayableBlockStore(networkParameters, new File(blockchainFilename), false);
 
             log.debug("Creating blockchain ...");
             blockChain = new MultiBitBlockChain(networkParameters, blockStore);
             log.debug("Created blockchain '" + blockChain + "'");
 
             log.debug("Creating peergroup ...");
             peerGroup = createNewPeerGroup();
             log.debug("Created peergroup '" + peerGroup + "'");
 
             log.debug("Starting peergroup ...");
             peerGroup.start();
             log.debug("Started peergroup.");
         } catch (BlockStoreException e) {
             handleError(e);
         } catch (FileHandlerException e) {
             handleError(e);    
         } catch (Exception e) {
             handleError(e);
         }
     }
     
     private void handleError(Exception e) {
         controller.setOnlineStatus(StatusEnum.ERROR);
         MessageManager.INSTANCE.addMessage(new Message(controller.getLocaliser().getString("multiBitService.couldNotLoadBlockchain", 
                 new Object[]{blockchainFilename, e.getClass().getName() + " " + e.getMessage()})));
         log.error("Error creating MultiBitService " + e.getClass().getName() + " " + e.getMessage());        
     }
 
     private MultiBitPeerGroup createNewPeerGroup() {
         MultiBitPeerGroup peerGroup = new MultiBitPeerGroup(controller, networkParameters, blockChain);
         peerGroup.setFastCatchupTimeSecs(0); // genesis block
         peerGroup.setUserAgent("MultiBit", controller.getLocaliser().getVersionNumber());
 
         String singleNodeConnection = controller.getModel().getUserPreference(MultiBitModel.SINGLE_NODE_CONNECTION);
         if (singleNodeConnection != null && !singleNodeConnection.equals("")) {
             try {
                 peerGroup.addAddress(new PeerAddress(InetAddress.getByName(singleNodeConnection)));
                 peerGroup.setMaxConnections(1);
             } catch (UnknownHostException e) {
                 log.error(e.getMessage(), e);
             }
         } else {
             // Use DNS for production, IRC for test.
             if ("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943".equals(controller.getModel().getNetworkParameters().genesisBlock.getHashAsString())){
                 peerGroup.addPeerDiscovery(new IrcDiscovery(IRC_CHANNEL_TESTNET3));
             } else if (NetworkParameters.testNet().equals(controller.getModel().getNetworkParameters())){
                 peerGroup.addPeerDiscovery(new IrcDiscovery(IRC_CHANNEL_TEST));
             } else {
                 peerGroup.addPeerDiscovery(new MultiBitDnsDiscovery(networkParameters));
             }
         }
         // Add the controller as a PeerEventListener.
         peerGroup.addEventListener(controller);
         return peerGroup;
     }
 
     public static String getFilePrefix() {
         MultiBitController controller = MultiBit.getController();
         MultiBitModel model = controller.getModel();
         // testnet3
         if ("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943".equals(model.getNetworkParameters().genesisBlock.getHashAsString())) {
             return MULTIBIT_PREFIX + SEPARATOR + TESTNET3_PREFIX;
         } else if (NetworkParameters.testNet().equals(model.getNetworkParameters())) {
             return MULTIBIT_PREFIX + SEPARATOR + TESTNET_PREFIX;
         } else {
             return MULTIBIT_PREFIX;
         }
     }
 
     /**
      * Initialize wallet from the wallet filename.
      * 
      * @param walletFilename
      * @return perWalletModelData
      */
     public PerWalletModelData addWalletFromFilename(String walletFilename) throws IOException {
         PerWalletModelData perWalletModelDataToReturn = null;
 
         File walletFile = null;
         boolean walletFileIsADirectory = false;
         boolean newWalletCreated = false;
 
         if (walletFilename != null) {
             walletFile = new File(walletFilename);
             if (walletFile.isDirectory()) {
                 walletFileIsADirectory = true;
             } else {
 
                 perWalletModelDataToReturn = controller.getFileHandler().loadFromFile(walletFile);
                 if (perWalletModelDataToReturn != null) {
                     wallet = perWalletModelDataToReturn.getWallet();
                 }
 
             }
         }
 
         if (wallet == null || walletFilename == null || walletFilename.equals("") || walletFileIsADirectory) {
             // Use default wallet name - create if does not exist.
             if ("".equals(controller.getApplicationDataDirectoryLocator().getApplicationDataDirectory())) {
                 walletFilename = getFilePrefix() + WALLET_SUFFIX;
             } else {
                 walletFilename = controller.getApplicationDataDirectoryLocator().getApplicationDataDirectory() + File.separator
                         + getFilePrefix() + WALLET_SUFFIX;
             }
 
             walletFile = new File(walletFilename);
 
             if (walletFile.exists()) {
                 // Wallet file exists with default name.
                 perWalletModelDataToReturn = controller.getFileHandler().loadFromFile(walletFile);
                 if (perWalletModelDataToReturn != null) {
                     wallet = perWalletModelDataToReturn.getWallet();
                 }
 
                 newWalletCreated = true;
             } else {
                 // Create a brand new wallet - by default protobuf and scrypt encryptable with random salt.
                 byte[] salt = new byte[ScryptParameters.SALT_LENGTH];
                 secureRandom.nextBytes(salt);
                 ScryptParameters scryptParameters = new ScryptParameters(salt);
                 EncrypterDecrypter encrypterDecrypter = new EncrypterDecrypterScrypt(scryptParameters);
                 wallet = new Wallet(networkParameters, encrypterDecrypter);
                 ECKey newKey = new ECKey();
                 wallet.keychain.add(newKey);
 
                 perWalletModelDataToReturn = controller.getModel().addWallet(wallet, walletFile.getAbsolutePath());
 
                 // Create a wallet info.
                 WalletInfo walletInfo = new WalletInfo(walletFile.getAbsolutePath(), WalletMajorVersion.PROTOBUF);
                 perWalletModelDataToReturn.setWalletInfo(walletInfo);
 
                 // Set a default description.
                 String defaultDescription = controller.getLocaliser().getString("createNewWalletSubmitAction.defaultDescription");
                 perWalletModelDataToReturn.setWalletDescription(defaultDescription);
 
                 try {
                     controller.getFileHandler().savePerWalletModelData(perWalletModelDataToReturn, true);
 
                     newWalletCreated = true;
                 } catch (WalletSaveException wse) {
                     log.error(wse.getClass().getCanonicalName() + " " + wse.getMessage());
                     MessageManager.INSTANCE.addMessage(new Message(wse.getClass().getCanonicalName() + " " + wse.getMessage()));
                 } catch (WalletVersionException wve) {
                     log.error(wve.getClass().getCanonicalName() + " " + wve.getMessage());
                     MessageManager.INSTANCE.addMessage(new Message(wve.getClass().getCanonicalName() + " " + wve.getMessage()));
                 }
             }
         }
 
         if (wallet != null) {
             // Add the keys for this wallet to the address book as receiving addresses.
             ArrayList<ECKey> keys = wallet.keychain;
             if (keys != null) {
                 if (!newWalletCreated) {
                     perWalletModelDataToReturn = controller.getModel().getPerWalletModelDataByWalletFilename(walletFilename);
                 }
                 if (perWalletModelDataToReturn != null) {
                     WalletInfo walletInfo = perWalletModelDataToReturn.getWalletInfo();
                     if (walletInfo != null) {
                         for (ECKey key : keys) {
                             if (key != null) {
                                 Address address = key.toAddress(networkParameters);
                                 walletInfo.addReceivingAddressOfKey(address);
                             }
                         }
                     }
                 }
             }
 
             // Add wallet to blockchain.
             if (blockChain != null) {
                 blockChain.addWallet(wallet);
             } else {
                 log.error("Could not add wallet '" + walletFilename + "' to the blockChain as the blockChain is missing.\n"
                         + "This is bad. MultiBit is currently looking for a blockChain at '" + blockchainFilename + "'");
             }
 
             // Add wallet to PeerGroup - this is done in a background thread as it is slow.
             @SuppressWarnings("rawtypes")
             SwingWorker worker = new SwingWorker() {
                 @Override
                 protected Object doInBackground() throws Exception {
                     peerGroup.addWallet(wallet);
                     return null; // not used
                 }
             };
             worker.execute();
         }
 
         return perWalletModelDataToReturn;
     }
 
     /**
      * Replay blockchain.
      * 
      * @param dateToReplayFrom
      *            the date on the blockchain to replay from - if missing replay
      *            from genesis block
      */
     public void replayBlockChain(Date dateToReplayFrom) throws BlockStoreException {
         boolean restartPeerGroup = true;
         
         MessageManager.INSTANCE.addMessage(new Message (controller.getLocaliser().getString("resetTransactionsSubmitAction.startReplay")));
 
         // Work out whether to use CacheManager.
         String useCacheManagerString = controller.getModel().getUserPreference(MultiBitModel.USE_CACHE_MANAGER);
         boolean useCacheManager = false;
         if (Boolean.TRUE.toString().equalsIgnoreCase(useCacheManagerString)) {
             useCacheManager = true;
             CacheManager cacheManager = CacheManager.INSTANCE;
             log.debug("CacheManager = " + cacheManager);
             cacheManager.setController(controller);
             cacheManager.initialise();
         }
         
         // Navigate backwards in the blockchain to work out how far back in time to go.
         log.debug("Starting replay of blockchain from date = '" + dateToReplayFrom + "', useCacheManager = " + useCacheManager);
         StoredBlock storedBlock = null;
 
         Stack<StoredBlock> blockStack = new Stack<StoredBlock>();
 
         if (dateToReplayFrom == null || genesisBlockCreationDate.after(dateToReplayFrom)) {
             // Go back to the genesis block.
             if (!useCacheManager) {
                 try {
                     blockChain.setChainHeadClearCachesAndTruncateBlockStore(new StoredBlock(networkParameters.genesisBlock,
                             networkParameters.genesisBlock.getWork(), 0));
                 } catch (VerificationException e) {
                     throw new BlockStoreException(e);
                 }
             } else {
                 // Make sure the genesis block is cached - this will be the starting point for the replay from cache.
                 CacheManager.INSTANCE.writeFile(networkParameters.genesisBlock);
 
                 File blockFile = CacheManager.INSTANCE.getBlockCacheFile(networkParameters.genesisBlock);
 
                 log.debug("Actual Satoshi genesis exists = "
                         + (new File(CacheManager.INSTANCE.getBlockCacheDirectory() + File.separator
                                 + "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f.block")).exists());
 
                 if (blockFile.exists()) {
                     log.debug("Can replay blocks from the cache from  the genesis block");
 
                     // Put the whole chain on the stack.
                     storedBlock = blockStore.getChainHead();
                     assert storedBlock != null;
 
                     blockStack.push(storedBlock);
 
                     while (true) {
                         if (storedBlock == null) {
                             // Null result of previous get previous - have
                             // reached genesis block.
                             break;
                         }
                         Block header = storedBlock.getHeader();
                         if (header == null) {
                             log.debug("No header for stored block " + storedBlock.getHeight());
                             break;
                         }
 
                         try {
                             StoredBlock previousBlock = storedBlock.getPrev(blockStore);
                             if (previousBlock == null) {
                                 log.debug("Could not navigate backwards form storedBlock " + storedBlock.getHeight());
                                 break;
                             } else {
                                 storedBlock = previousBlock;
                                 blockStack.push(storedBlock);
                             }
                         } catch (BlockStoreException e) {
                             e.printStackTrace();
                             // We have to stop navigating backwards.
                             break;
                         }
                     }
                     restartPeerGroup();
                     restartPeerGroup = false;
                     storedBlock = CacheManager.INSTANCE.replayFromBlockCache(blockStack);
                 }
             }
         } else {
             // Not a replay from the genesis block.
             storedBlock = blockStore.getChainHead();
             assert storedBlock != null;
 
             boolean haveGoneBackInTimeEnough = false;
             int numberOfBlocksGoneBackward = 0;
             blockStack.push(storedBlock);
 
             while (!haveGoneBackInTimeEnough) {
                 if (storedBlock == null) {
                     // Null result of previous get previous - will have to stop
                     // navigating backwards.
                     break;
                 }
                 Block header = storedBlock.getHeader();
                 if (header == null) {
                     log.debug("No header for stored block " + storedBlock.getHeight());
                     break;
                 }
 
                 long headerTimeInSeconds = header.getTimeSeconds();
                 if (headerTimeInSeconds < (dateToReplayFrom.getTime() / NUMBER_OF_MILLISECOND_IN_A_SECOND)) {
                     haveGoneBackInTimeEnough = true;
                 } else {
                     try {
                         StoredBlock previousBlock = storedBlock.getPrev(blockStore);
                         if (previousBlock == null) {
                             log.debug("Could not navigate backwards form storedBlock " + storedBlock.getHeight());
                             break;
                         } else {
                             storedBlock = previousBlock;
                             blockStack.push(storedBlock);
                         }
                         numberOfBlocksGoneBackward++;
                     } catch (BlockStoreException e) {
                         e.printStackTrace();
                         // We have to stop navigating backwards.
                         break;
                     }
                 }
             }
 
             // In case the chain head was on an alternate fork go back more
             // blocks to ensure back on the main chain.
             while (numberOfBlocksGoneBackward < MAXIMUM_EXPECTED_LENGTH_OF_ALTERNATE_CHAIN) {
                 try {
                     StoredBlock previousBlock = storedBlock.getPrev(blockStore);
                     if (previousBlock == null) {
                         log.debug("Could not navigate backwards form storedBlock.1 " + storedBlock.getHeight());
                         break;
                     } else {
                         storedBlock = previousBlock;
                         blockStack.push(storedBlock);
                     }
                     numberOfBlocksGoneBackward++;
                 } catch (BlockStoreException e) {
                     e.printStackTrace();
                     // We have to stop - fail.
                     break;
                 }
             }
 
             if (useCacheManager) {
                 // Go back some more blocks to ensure there is an overlap of blocks downloaded.
                 for (int i = 0; i < MAXIMUM_EXPECTED_LENGTH_OF_ALTERNATE_CHAIN; i++) {
                     try {
                         StoredBlock previousBlock = storedBlock.getPrev(blockStore);
                         if (previousBlock == null) {
                             log.debug("Could not navigate backwards form storedBlock.2 " + storedBlock.getHeight());
                             break;
                         } else {
                             storedBlock = previousBlock;
                         }
                     } catch (BlockStoreException e) {
                         e.printStackTrace();
                         // We have to stop - fail.
                         break;
                     }
                 }
                 
                 // See if block is already cached. If it is, replay the cached blocks.
                 File blockFile = CacheManager.INSTANCE.getBlockCacheFile(storedBlock);
 
                 if (blockFile != null && blockFile.exists()) {
                     restartPeerGroup();
                     restartPeerGroup = false;
                     log.debug("Can replay blocks from the cache from storedBlock height = " + storedBlock.getHeight());
                     storedBlock = CacheManager.INSTANCE.replayFromBlockCache(blockStack);
                 }
             }
 
             // Set the block chain head to the earliest block that we need to download.
             blockChain.setChainHeadClearCachesAndTruncateBlockStore(storedBlock);
         }
 
         // Restart peerGroup and download rest of blockchain.
         Message message;
         if (dateToReplayFrom != null && storedBlock != null  && storedBlock.getHeader() != null) {
             message = new Message(controller.getLocaliser().getString(
                     "resetTransactionSubmitAction.replayingBlockchain",
                     new Object[] { DateFormat.getDateInstance(DateFormat.MEDIUM, controller.getLocaliser().getLocale()).format(
                             storedBlock.getHeader().getTime()) }), false);
         } else {
             message = new Message(controller.getLocaliser().getString(
                     "resetTransactionSubmitAction.replayingBlockchain",
                     new Object[] { DateFormat.getDateInstance(DateFormat.MEDIUM, controller.getLocaliser().getLocale()).format(
                             genesisBlockCreationDate) }), false);
         }
         MessageManager.INSTANCE.addMessage(message);
 
 
         if (restartPeerGroup) {
             restartPeerGroup();
         }
 
         downloadBlockChain();
     }
     
     public void restartPeerGroup() {
         // Restart peerGroup and download.
         Message message = new Message(controller.getLocaliser().getString("multiBitService.stoppingBitcoinNetworkConnection"),
                 false, 0);
         MessageManager.INSTANCE.addMessage(message);
 
         peerGroup.stop();
 
         // Reset UI to zero peers.
         controller.onPeerDisconnected(null, 0);
  
         peerGroup = createNewPeerGroup();
         peerGroup.start();
     }
 
     /**
      * Download the block chain.
      */
     public void downloadBlockChain() {
         @SuppressWarnings("rawtypes")
         SwingWorker worker = new SwingWorker() {
             @Override
             protected Object doInBackground() throws Exception {
                 logger.debug("Downloading blockchain");
                 peerGroup.downloadBlockChain();
                 return null; // return not used
             }
         };
         worker.execute();
     }
 
     /**
      * Send bitcoins from the active wallet.
      * 
      * @param sendAddressString
      *            the address to send to, as a String
      * @param fee
      *            fee to pay in nanocoin
      * @param amount
      *            the amount to send to, in BTC, as a String
      * @return The sent transaction (may be null if there were insufficient
      *         funds for send)
      */
 
     public Transaction sendCoins(PerWalletModelData perWalletModelData, String sendAddressString, String amount, BigInteger fee, boolean decryptBeforeSigning, char[] password)
             throws java.io.IOException, AddressFormatException {
         // Send the coins
         Address sendAddress = new Address(networkParameters, sendAddressString);
 
         log.debug("MultiBitService#sendCoins - Just about to send coins");
         KeyParameter aesKey = null;
        if (perWalletModelData.getWallet().getEncrypterDecrypter() != null) {
             aesKey = perWalletModelData.getWallet().getEncrypterDecrypter().deriveKey(password);
         }
         Transaction sendTransaction = perWalletModelData.getWallet().sendCoinsAsync(peerGroup, sendAddress,
                 Utils.toNanoCoins(amount), fee, decryptBeforeSigning, aesKey);
         log.debug("MultiBitService#sendCoins - Sent coins has completed");
 
         assert sendTransaction != null; 
         // We should never try to send more
         // coins than we have!
         // throw an exception if sendTransaction is null - no money.
         if (sendTransaction != null) {
             log.debug("MultiBitService#sendCoins - Sent coins. Transaction hash is {}", sendTransaction.getHashAsString());
 
             try {
                 controller.getFileHandler().savePerWalletModelData(perWalletModelData, false);
             } catch (WalletSaveException wse) {
                 log.error(wse.getClass().getCanonicalName() + " " + wse.getMessage());
                 MessageManager.INSTANCE.addMessage(new Message(wse.getClass().getCanonicalName() + " " + wse.getMessage()));
             } catch (WalletVersionException wse) {
                 log.error(wse.getClass().getCanonicalName() + " " + wse.getMessage());
                 MessageManager.INSTANCE.addMessage(new Message(wse.getClass().getCanonicalName() + " " + wse.getMessage()));
             }
             
             try {
                 // Notify other wallets of the send (it might be a send to or from them).
                 List<PerWalletModelData> perWalletModelDataList = controller.getModel().getPerWalletModelDataList();
 
                 if (perWalletModelDataList != null) {
                     for (PerWalletModelData loopPerWalletModelData : perWalletModelDataList) {
                         if (!perWalletModelData.getWalletFilename().equals(loopPerWalletModelData.getWalletFilename())) {
                             Wallet loopWallet = loopPerWalletModelData.getWallet();
                             if (loopWallet.isTransactionRelevant(sendTransaction, true)) {
                                 // The perWalletModelData is marked as dirty.
                                 if (perWalletModelData.getWalletInfo() != null) {
                                     synchronized(perWalletModelData.getWalletInfo()) {
                                         perWalletModelData.setDirty(true);
                                     }
                                 } else {
                                     perWalletModelData.setDirty(true);
                                 }
                                 if (loopWallet.getTransaction(sendTransaction.getHash()) == null) {
                                     log.debug("MultiBit adding a new pending transaction for the wallet '"
                                             + perWalletModelData.getWalletDescription() + "'\n" + sendTransaction.toString());
                                     loopWallet.receivePending(sendTransaction);
                                 }
                             }  
                         }
                     }
                 }
             } catch (ScriptException e) {
                 e.printStackTrace();
             } catch (VerificationException e) {
                 e.printStackTrace();
             }
         } 
         return sendTransaction;
     }
 
     public PeerGroup getPeerGroup() {
         return peerGroup;
     }
 
     public MultiBitBlockChain getChain() {
         return blockChain;
     }
 
     public ReplayableBlockStore getBlockStore() {
         return blockStore;
     }
 
     public SecureRandom getSecureRandom() {
         return secureRandom;
     };
 }
