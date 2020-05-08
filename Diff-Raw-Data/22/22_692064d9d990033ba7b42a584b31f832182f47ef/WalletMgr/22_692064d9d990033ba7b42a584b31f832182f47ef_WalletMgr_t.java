 package org.bitprivacy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import asg.cliche.Command;
 import asg.cliche.Param;
 import asg.cliche.Shell;
 import asg.cliche.ShellDependent;
 import asg.cliche.ShellFactory;
 
 import com.dustyneuron.txmarket.bitcoin.WalletUtils;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Block;
 import com.google.bitcoin.core.BlockChain;
 import com.google.bitcoin.core.CheckpointManager;
 import com.google.bitcoin.core.ECKey;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.core.Peer;
 import com.google.bitcoin.core.PeerGroup;
 import com.google.bitcoin.core.ScriptException;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;
 import com.google.bitcoin.core.TransactionOutput;
 import com.google.bitcoin.core.Utils;
 import com.google.bitcoin.core.VerificationException;
 import com.google.bitcoin.core.Wallet;
 import com.google.bitcoin.core.Wallet.SendRequest;
 import com.google.bitcoin.discovery.DnsDiscovery;
import com.google.bitcoin.params.TestNet3Params;
 import com.google.bitcoin.store.BlockStore;
 import com.google.bitcoin.store.SPVBlockStore;
import com.google.bitcoin.store.UnreadableWalletException;
 import com.google.common.base.Function;
 import com.google.common.util.concurrent.ListenableFuture;
 
 public class WalletMgr implements ShellDependent, BitcoinNetwork {
     private String nodeName;
 
     private BlockChain chain;
     private BlockStore blockStore;
     private File walletFile;
     private Wallet wallet;
 
     public NetworkParameters params;
 
     private TxCommands txCommands;
 
     private int maxConnections = 2;
     private double defaultFee = 0.0005;
 
     public WalletMgr(String file) throws Exception {
         init(file);
     }
 
     public synchronized void shutdown() {
         if (wallet == null) {
             return;
         }
         try {
             System.out.print("Shutting down wallet... ");
             wallet.saveToFile(walletFile);
             wallet = null;
             walletFile = null;
             blockStore.close();
             blockStore = null;
             chain = null;
             System.out.println("...done ");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public synchronized <R> R doNetworkAction(Function<PeerGroup, R> action)
             throws Exception {
 
         PeerGroup peerGroup = new PeerGroup(params, chain);
         peerGroup.addPeerDiscovery(new DnsDiscovery(params));
         peerGroup.addWallet(wallet);
 
         peerGroup.setMaxConnections(maxConnections);
         peerGroup.startAndWait();
         System.out.println("Waiting for " + peerGroup.getMaxConnections()
                 + " connections...");
         peerGroup.waitForPeers(peerGroup.getMaxConnections()).get();
 
         R result = action.apply(peerGroup);
 
         peerGroup.setMaxConnections(0);
         peerGroup.stopAndWait();
 
         return result;
     }
 
     @Override
     public Transaction broadcast(final Transaction t) throws Exception {
         return doNetworkAction(new Function<PeerGroup, Transaction>() {
             @Override
             public Transaction apply(PeerGroup peerGroup) {
                 System.out.println("Syncing...");
                 peerGroup.downloadBlockChain();
                 ListenableFuture<Transaction> future = peerGroup
                         .broadcastTransaction(t, 1);
                 System.out.print("Broadcasting...");
                 Transaction result = null;
                 try {
                     result = future.get(30, TimeUnit.SECONDS);
                     System.out.println("...done!");
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                 } catch (ExecutionException e) {
                     throw new RuntimeException(e);
                 } catch (TimeoutException e) {
                     e.printStackTrace();
                 }
                 return result;
             }
         });
     }
 
     public Block downloadBlock(final Sha256Hash b) throws Exception {
 
         Block foundBlock = doNetworkAction(new Function<PeerGroup, Block>() {
             @Override
             public Block apply(PeerGroup peerGroup) {
                 for (Peer peer : peerGroup.getConnectedPeers()) {
                     // System.out.println("downloadBlock(" + b +
                     // ") - trying peer " + peer);
                     Block block = null;
                     try {
                         block = peer.getBlock(b).get(10, TimeUnit.SECONDS);
                     } catch (InterruptedException e) {
                         throw new RuntimeException(e);
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     } catch (ExecutionException e) {
                         throw new RuntimeException(e);
                     } catch (TimeoutException e) {
                         e.printStackTrace();
                     }
 
                     if (block != null) {
                         return block;
                     }
                 }
                 return null;
             }
         });
 
         if (foundBlock == null) {
             throw new Exception("block not found");
         }
         foundBlock.verify();
         return foundBlock;
     }
 
     public Transaction getExternalTransaction(Sha256Hash block, Sha256Hash tx)
             throws Exception {
         Block b = downloadBlock(block);
         return WalletUtils.findTransaction(b, tx);
 
     }
 
     public boolean isTransactionOutputMine(TransactionOutput o) {
         return o.isMine(wallet);
     }
 
     public Transaction getTransaction(Sha256Hash txHash) {
         return wallet.getTransaction(txHash);
     }
 
     @Command
     public synchronized void setMaxConnections(int i) {
         maxConnections = i;
     }
 
     @Command
     public int getMaxConnections() {
         return maxConnections;
     }
 
     public Wallet getWallet() {
         return wallet;
     }
 
     @Command
     public synchronized void clearTransactions() {
         wallet.clearTransactions(0);
         System.out.println("done");
     }
 
     @Command
     public void history() throws Exception {
         String output = "Tx history:\n";
         List<Transaction> transactions = wallet.getTransactionsByTime();
         for (int i = transactions.size() - 1; i >= 0; --i) {
             Transaction t = transactions.get(i);
             output += WalletUtils.transactionToString(t, wallet) + "\n\n";
         }
 
         System.out.print(output);
     }
 
     @Command
     public void sync() throws Exception {
         doNetworkAction(new Function<PeerGroup, Object>() {
             @Override
             public Object apply(PeerGroup peerGroup) {
                 System.out.println("Syncing...");
                 peerGroup.downloadBlockChain();
                 return null;
             }
         });
     }
 
     @Command(description = "Manually construct/manipulate transactions")
     public void tx() throws IOException {
         ShellFactory
                 .createSubshell(
                         "tx",
                         theShell,
                         "Tx Shell - Type '?l' for available commands, 'exit' to exit shell",
                         txCommands).commandLoop();
     }
 
     @Command
     public void pay(
             @Param(name = "address", description = "Destination address") String address,
             @Param(name = "amount", description = "BTC amount") double amount)
             throws Exception {
         BigInteger btc = Utils.toNanoCoins(Double.toString(amount));
         final SendRequest request = SendRequest.to(
                 new Address(params, address), btc);
         request.fee = Utils.toNanoCoins(Double.toString(defaultFee));
 
         doNetworkAction(new Function<PeerGroup, Object>() {
             @Override
             public Object apply(PeerGroup peerGroup) {
                 System.out.println("Paying...");
                 try {
                     wallet.sendCoins(peerGroup, request).broadcastComplete
                             .get();
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                 return null;
             }
         });
     }
 
     @Command
     public synchronized void generate() throws IOException, ScriptException {
         wallet.addKey(new ECKey());
 
         addresses();
     }
 
     @Command
     public void privateKeys() {
         Iterator<ECKey> it = wallet.getKeys().iterator();
         while (it.hasNext()) {
             ECKey key = it.next();
             byte[] pubKeyHash = key.getPubKeyHash();
             Address address = new Address(params, pubKeyHash);
 
             String output = "address: " + address.toString() + "\n";
             output += "  key:" + key.getPrivateKeyEncoded(params) + "\n";
             System.out.print(output);
         }
     }
 
     @Command
     public void addresses() throws ScriptException {
 
         BigInteger estimateTotalBalance = new BigInteger("0");
         Iterator<ECKey> it = wallet.getKeys().iterator();
         while (it.hasNext()) {
             ECKey key = it.next();
             byte[] pubKeyHash = key.getPubKeyHash();
             Address address = new Address(params, pubKeyHash);
             String output = "address: " + address.toString() + "\n";
 
             List<Transaction> transactions = wallet.getTransactionsByTime();
             BigInteger estimateAddressBalance = new BigInteger("0");
             BigInteger confirmedAddressBalance = new BigInteger("0");
             int worstDepth = Integer.MAX_VALUE;
             for (Transaction t : transactions) {
                 for (TransactionOutput o : t.getOutputs()) {
                     if (o.isAvailableForSpending()) {
                         if (Arrays.equals(o.getScriptPubKey().getPubKeyHash(), address.getHash160())) {
                             estimateAddressBalance = estimateAddressBalance
                                     .add(o.getValue());
                             if (t.getConfidence().getConfidenceType() == ConfidenceType.BUILDING) {
                                 int depth = t.getConfidence()
                                         .getDepthInBlocks();
                                 worstDepth = (depth < worstDepth ? depth
                                         : worstDepth);
                                 confirmedAddressBalance = confirmedAddressBalance
                                         .add(o.getValue());
                             }
                         }
                     }
                 }
             }
             estimateTotalBalance = estimateTotalBalance
                     .add(estimateAddressBalance);
             output += "  balance estimate: "
                     + Utils.bitcoinValueToFriendlyString(estimateAddressBalance)
                     + "  confirmed: "
                     + Utils.bitcoinValueToFriendlyString(confirmedAddressBalance);
             if (worstDepth != Integer.MAX_VALUE) {
                 output += " (worst depth " + worstDepth + ")";
             }
 
             System.out.println(output);
         }
         System.out.println("Total balance estimate: "
                 + Utils.bitcoinValueToFriendlyString(estimateTotalBalance)
                 + ", Total available: "
                 + Utils.bitcoinValueToFriendlyString(wallet.getBalance()));
     }
 
     public synchronized void commitToWallet(Transaction tx)
             throws VerificationException, IOException {
         wallet.commitTx(tx);
     }
 
     public synchronized void init(String n) throws Exception {
 
         nodeName = n;
 
        params = TestNet3Params.get();
 
         walletFile = new File(nodeName + ".wallet");
         try {
             wallet = Wallet.loadFromFile(walletFile);
             System.out.println("Opened wallet " + walletFile.getAbsolutePath());
        } catch (UnreadableWalletException e) {
             wallet = new Wallet(params);
             wallet.addKey(new ECKey());
             System.out.println("Created new wallet "
                     + walletFile.getAbsolutePath());
         }
         wallet.autosaveToFile(walletFile, 1, TimeUnit.SECONDS, null);
 
         // System.out.println("Reading block store from disk");
 
         File file = new File(nodeName + ".spvchain");
         boolean chainExistedAlready = file.exists();
         blockStore = new SPVBlockStore(params, file);
         if (!chainExistedAlready) {
             File checkpointsFile = new File("checkpoints");
             if (checkpointsFile.exists()) {
                 ECKey key = wallet.getKeys().iterator().next();
                 FileInputStream stream = new FileInputStream(checkpointsFile);
                 CheckpointManager.checkpoint(params, stream, blockStore,
                         key.getCreationTimeSeconds());
             }
         }
 
         chain = new BlockChain(params, wallet, blockStore);
 
         // make sure that we shut down cleanly!
         final WalletMgr walletMgr = this;
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 walletMgr.shutdown();
             }
         });
 
         txCommands = new TxCommands(this);
     }
 
     private Shell theShell;
 
     public void cliSetShell(Shell theShell) {
         this.theShell = theShell;
     }
 
 }
