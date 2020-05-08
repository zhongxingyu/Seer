 package com.dustyneuron.bitprivacy.bitcoin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import asg.cliche.Command;
 import asg.cliche.Shell;
 import asg.cliche.ShellDependent;
 import asg.cliche.ShellFactory;
 
 import com.google.bitcoin.core.AbstractWalletEventListener;
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
 import com.google.bitcoin.core.TransactionConfidence;
 import com.google.bitcoin.core.TransactionOutput;
 import com.google.bitcoin.core.Utils;
 import com.google.bitcoin.core.Wallet;
 import com.google.bitcoin.discovery.DnsDiscovery;
 import com.google.bitcoin.store.BlockStore;
 import com.google.bitcoin.store.SPVBlockStore;
 import com.google.common.util.concurrent.ListenableFuture;
 
 public class WalletMgr implements ShellDependent {
 	private String nodeName;
 	
 	private PeerGroup peerGroup;
 	private BlockChain chain;
 	private BlockStore blockStore;
 	private File walletFile;
 	private Wallet wallet;
 	
     public NetworkParameters params;
     
     private Tx tx;
     private int maxConnections = 2;
 
     public WalletMgr(String file) throws Exception {
     	Init(file);
     }
         
     public void shutdown() {
     	if (peerGroup == null) {
     		return;
     	}
         try {
             System.out.print("Shutting down wallet and peergroup ... ");
             peerGroup.stopAndWait();
             peerGroup = null;
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
     
 	public void broadcast(Transaction t) throws InterruptedException, ExecutionException, IOException {
     	peerGroup.setMaxConnections(maxConnections);
     	peerGroup.startAndWait();
         System.out.println("Waiting for " + peerGroup.getMaxConnections() + " connections...");
         peerGroup.waitForPeers(peerGroup.getMaxConnections()).get();
 
         System.out.println("Syncing...");
         peerGroup.downloadBlockChain();
 		
 		ListenableFuture<Transaction> future = peerGroup.broadcastTransaction(t, peerGroup.getMaxConnections());
 		System.out.print("Broadcasting...");
 
 		future.get();
 		System.out.println("...done!");
     	peerGroup.setMaxConnections(0);
         peerGroup.stopAndWait();
         wallet.saveToFile(walletFile);
 	}	
 
 	public void sign(Transaction t) throws ScriptException {
 		t.signInputs(Transaction.SigHash.ALL, wallet, null);
         t.getConfidence().setSource(TransactionConfidence.Source.SELF);
 	}
     
     Block downloadBlock(Sha256Hash b) throws Exception {
     	peerGroup.setMaxConnections(maxConnections);
     	peerGroup.startAndWait();
         System.out.println("Waiting for " + peerGroup.getMaxConnections() + " connections...");
         peerGroup.waitForPeers(peerGroup.getMaxConnections()).get();
     	
         Block foundBlock = null;
     	for (Peer peer : peerGroup.getConnectedPeers()) {
     		System.out.println("downloadBlock(" + b + ") - trying peer " + peer);
     		foundBlock = peer.getBlock(b).get();
     		if (foundBlock != null) {
     			break;
     		}
     	}
     	System.out.println("Shutting down peerGroup...");
     	peerGroup.setMaxConnections(0);
         peerGroup.stopAndWait();
         if (foundBlock == null) {
         	throw new Exception("block not found");
         }
         return foundBlock;
     }
     
     public Transaction getExternalTransaction(Sha256Hash block, Sha256Hash tx) throws Exception {
 		Block b = downloadBlock(block);
 		b.verify();
 		
 		Transaction foundTx = null;
 		for (Transaction t : b.getTransactions()) {
 			if (t.getHash().equals(tx)) {
 				foundTx = t;
 			}
 		}
 		return foundTx;
 
     }
     
     public boolean isTransactionOutputMine(TransactionOutput o) {
     	return o.isMine(wallet);
     }
     
     public Transaction getTransaction(Sha256Hash txHash) {
     	return wallet.getTransaction(txHash);
     }
     
     @Command
     public void setMaxConnections(int i) {
     	maxConnections = i;
     }
     @Command
     public int getMaxConnections() {
     	return maxConnections;
     }
     
     @Command
     public void history() {
         String output = "Tx history:\n";
         List<Transaction> transactions = wallet.getTransactionsByTime();
         for (Transaction t : transactions) {
         	output += t.toString(null) + "\n";
         }
 		
 		System.out.print(output);
     }
     
     @Command
     public void sync() throws Exception {
     	peerGroup.setMaxConnections(maxConnections);
     	peerGroup.startAndWait();
         System.out.println("Waiting for " + peerGroup.getMaxConnections() + " connections...");
         peerGroup.waitForPeers(peerGroup.getMaxConnections()).get();
 
         System.out.println("Syncing...");
         peerGroup.downloadBlockChain();
         System.out.println("Shutting down peerGroup...");
         peerGroup.stopAndWait();
         wallet.saveToFile(walletFile);
     }
     
     @Command
     public void tx() throws IOException {
     	ShellFactory.createSubshell("tx", theShell, "Tx Shell", tx).commandLoop();
     }
     
     @Command
     public void generate() throws IOException {
         wallet.keychain.add(new ECKey());
         wallet.saveToFile(walletFile);
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
     	
     	Iterator<ECKey> it = wallet.getKeys().iterator();
     	while (it.hasNext()) {
     		ECKey key = it.next();
     		byte[] pubKeyHash = key.getPubKeyHash();
     		Address address = new Address(params, pubKeyHash);
     		String output = "address: " + address.toString() + "\n";
     		
     		List<Transaction> transactions = wallet.getTransactionsByTime();
     		BigInteger estimateAddressBallance = new BigInteger("0");
             for (Transaction t : transactions) {
            	estimateAddressBallance = estimateAddressBallance.add(t.getValue(wallet));
             }
             output += "  ballance estimate: " + Utils.bitcoinValueToFriendlyString(estimateAddressBallance) + "\n";
             System.out.print(output);
     	}
     }
     
     public void Init(String n) throws Exception {
 
     	nodeName = n;
     	
         params = NetworkParameters.testNet();
         
         walletFile = new File(nodeName + ".wallet");
         try {
         	wallet = Wallet.loadFromFile(walletFile);
         	System.out.println("Read wallet from file " + walletFile);
         } catch (IOException e) {
         	wallet = new Wallet(params);
             wallet.keychain.add(new ECKey());
             wallet.saveToFile(walletFile);
             System.out.println("Created new wallet file " + walletFile);
         }
         	        
         // Load the block chain, if there is one stored locally. If it's going to be freshly created, checkpoint it.
         System.out.println("Reading block store from disk");
         
         File file = new File(nodeName + ".spvchain");
         boolean chainExistedAlready = file.exists();
         blockStore = new SPVBlockStore(params, file);
         if (!chainExistedAlready) {
             File checkpointsFile = new File("checkpoints");
             if (checkpointsFile.exists()) {
             	ECKey key = wallet.getKeys().iterator().next();
                 FileInputStream stream = new FileInputStream(checkpointsFile);
                 CheckpointManager.checkpoint(params, stream, blockStore, key.getCreationTimeSeconds());
             }
         }
         
      // We want to know when the balance changes.
         wallet.addEventListener(new AbstractWalletEventListener() {
             public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
                 // Running on a peer thread.
             	BigInteger value = tx.getValueSentToMe(w);
             	try {
 					System.out.println("Received " + Utils.bitcoinValueToFriendlyString(value) + " from " + tx.getInput(0).getFromAddress().toString());
 				} catch (ScriptException e) {
 					throw new RuntimeException(e);
 				}
             	System.out.println("Estimated ballance is " + Utils.bitcoinValueToFriendlyString(w.getBalance(Wallet.BalanceType.ESTIMATED)));
             }
         });
 
         chain = new BlockChain(params, blockStore);
         
         peerGroup = new PeerGroup(params, chain);
         peerGroup.addPeerDiscovery(new DnsDiscovery(params));
         peerGroup.addWallet(wallet);
         
         // make sure that we shut down cleanly!
         final WalletMgr walletMgr = this;
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
             	walletMgr.shutdown();
             }
         });
         
         tx = new Tx(this);
     }
     
     
     private Shell theShell;
 
     public void cliSetShell(Shell theShell) {
         this.theShell = theShell;
     }
 
 }
