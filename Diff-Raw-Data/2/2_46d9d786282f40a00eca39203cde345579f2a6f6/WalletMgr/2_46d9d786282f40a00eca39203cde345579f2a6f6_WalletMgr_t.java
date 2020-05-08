 package com.dustyneuron.bitprivacy.bitcoin;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.codec.binary.Hex;
 import org.spongycastle.crypto.params.KeyParameter;
 
 import asg.cliche.Command;
 import asg.cliche.Param;
 import asg.cliche.Shell;
 import asg.cliche.ShellDependent;
 import asg.cliche.ShellFactory;
 
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Block;
 import com.google.bitcoin.core.BlockChain;
 import com.google.bitcoin.core.CheckpointManager;
 import com.google.bitcoin.core.ECKey;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.core.Peer;
 import com.google.bitcoin.core.PeerGroup;
 import com.google.bitcoin.core.Script;
 import com.google.bitcoin.core.ScriptException;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.TransactionConfidence;
 import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;
 import com.google.bitcoin.core.TransactionInput;
 import com.google.bitcoin.core.TransactionOutPoint;
 import com.google.bitcoin.core.TransactionOutput;
 import com.google.bitcoin.core.UnsafeByteArrayOutputStream;
 import com.google.bitcoin.core.Utils;
 import com.google.bitcoin.core.VerificationException;
 import com.google.bitcoin.core.Wallet;
 import com.google.bitcoin.core.Transaction.SigHash;
 import com.google.bitcoin.core.Wallet.SendRequest;
 import com.google.bitcoin.discovery.DnsDiscovery;
 import com.google.bitcoin.store.BlockStore;
 import com.google.bitcoin.store.SPVBlockStore;
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.util.concurrent.ListenableFuture;
 
 
 public class WalletMgr implements ShellDependent {
 	private String nodeName;
 	
 	private PeerGroup peerGroup;
 	private BlockChain chain;
 	private BlockStore blockStore;
 	private File walletFile;
 	private Wallet wallet;
 	
     public NetworkParameters params;
     
     private TxCommands txCommands;
     
     private int maxConnections = 2;
     private double defaultFee = 0.005;
 
     public WalletMgr(String file) throws Exception {
     	init(file);
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
     
     public <R> R doNetworkAction(Function<Object, R> action) throws Exception {
     	peerGroup.setMaxConnections(maxConnections);
     	peerGroup.startAndWait();
         System.out.println("Waiting for " + peerGroup.getMaxConnections() + " connections...");
         peerGroup.waitForPeers(peerGroup.getMaxConnections()).get();
 
         R result = action.apply(null);
 		        
     	peerGroup.setMaxConnections(0);
         peerGroup.stopAndWait();
         wallet.saveToFile(walletFile);
         
         return result;
     }
     
 	public void broadcast(final Transaction t) throws Exception {
 		doNetworkAction(new Function<Object, Object>() {
 			@Override
 			public Object apply(Object o) {
 		        System.out.println("Syncing...");
 		        peerGroup.downloadBlockChain();
 				ListenableFuture<Transaction> future = peerGroup.broadcastTransaction(t, peerGroup.getMaxConnections());
 				System.out.print("Broadcasting...");
 				try {
 					future.get();
 				} catch (InterruptedException | ExecutionException e) {
 					throw new RuntimeException(e);
 				}
 				System.out.println("...done!");
 				return null;
 			}
 		});
 	}	
 
 	public Transaction newSignedInputsIfPossible(Transaction t) throws Exception {
 		return newSignedInputsIfPossible(t, Transaction.SigHash.ALL, wallet, null);
 	}
 	
     synchronized Transaction newSignedInputsIfPossible(Transaction t, SigHash hashType, Wallet wallet, KeyParameter aesKey) throws Exception {
     	List<TransactionInput> inputs = t.getInputs();
     	List<TransactionOutput> outputs = t.getOutputs();
     	
         Preconditions.checkState(inputs.size() > 0);
         Preconditions.checkState(outputs.size() > 0);
 
         // I don't currently have an easy way to test other modes work, as the official client does not use them.
         Preconditions.checkArgument(hashType == SigHash.ALL, "Only SIGHASH_ALL is currently supported");
 
         // The transaction is signed with the input scripts empty except for the input we are signing. In the case
         // where addInput has been used to set up a new transaction, they are already all empty. The input being signed
         // has to have the connected OUTPUT program in it when the hash is calculated!
         //
         // Note that each input may be claiming an output sent to a different key. So we have to look at the outputs
         // to figure out which key to sign with.
 
         byte[][] signatures = new byte[inputs.size()][];
         ECKey[] signingKeys = new ECKey[inputs.size()];
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
             	
 	            TransactionOutPoint outpoint = getConnectedOutPoint(input);
 	            if (outpoint == null) {
 	            	throw new Exception("null outpoint for " + input);
 	            }
 	
 	            ECKey key = outpoint.getConnectedKey(wallet);
 	            if (key == null) {
 	            	System.out.println("we do not have the key for " + input.getOutpoint());
 	            }
 	            else {
 		            // Keep the key around for the script creation step below.
 		            signingKeys[i] = key;
 		            // The anyoneCanPay feature isn't used at the moment.
 		            boolean anyoneCanPay = false;
 		            byte[] connectedPubKeyScript = getConnectedOutput(input).getScriptBytes();
 		            Sha256Hash hash = t.hashTransactionForSignature(i, connectedPubKeyScript, hashType, anyoneCanPay);
 		
 		            // Now sign for the output so we can redeem it. We use the keypair to sign the hash,
 		            // and then put the resulting signature in the script along with the public key (below).
 		            try {
 		                // Usually 71-73 bytes.
 		                ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(73);
 		                bos.write(key.sign(hash, aesKey).encodeToDER());
 		                bos.write((hashType.ordinal() + 1) | (anyoneCanPay ? 0x80 : 0));
 		                signatures[i] = bos.toByteArray();
 		                bos.close();
 		            } catch (IOException e) {
 		                throw new RuntimeException(e);  // Cannot happen.
 		            }
 	            }
             }
         }
 
         // Now we have calculated each signature, go through and create the scripts. Reminder: the script consists:
         // 1) For pay-to-address outputs: a signature (over a hash of the simplified transaction) and the complete
         //    public key needed to sign for the connected output. The output script checks the provided pubkey hashes
         //    to the address and then checks the signature.
         // 2) For pay-to-key outputs: just a signature.
         
         Transaction newTx = new Transaction(params);
         newTx.getConfidence().setSource(TransactionConfidence.Source.SELF);
         for (TransactionOutput o : t.getOutputs()) {
         	newTx.addOutput(o);
         }
         
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
 	            ECKey key = signingKeys[i];
 	            if (key == null) {
 	            	newTx.addInput(input);
 	            }
 	            else {
 		            Script scriptPubKey = getConnectedOutput(input).getScriptPubKey();
 		            byte[] scriptBytes;
 		            if (scriptPubKey.isSentToAddress()) {
 		            	scriptBytes = Script.createInputScript(signatures[i], key.getPubKey());
 		            } else if (scriptPubKey.isSentToRawPubKey()) {
 		            	scriptBytes = Script.createInputScript(signatures[i]);
 		            } else {
 		                // Should be unreachable - if we don't recognize the type of script we're trying to sign for, we should
 		                // have failed above when fetching the key to sign with.
 		                throw new RuntimeException("Do not understand script type: " + scriptPubKey);
 		            }
 		            //input.setScriptBytes(scriptBytes);
 		            newTx.addInput(new TransactionInput(params, newTx, scriptBytes, input.getOutpoint()));
 	            }
             }
             else {
             	newTx.addInput(input);
             }
         }
         return newTx;
     }
     
     TransactionOutPoint getConnectedOutPoint(TransactionInput input) throws Exception {
     	Sha256Hash id = input.getOutpoint().getHash();
     	Transaction t = getTransaction(id);
     	if (t == null) {
     		throw new Exception("tx not found in wallet");
     	}
     	return new TransactionOutPoint(params, (int) input.getOutpoint().getIndex(), t);
     }
 
     TransactionOutput getConnectedOutput(TransactionInput input) throws Exception {
     	int idx = (int) input.getOutpoint().getIndex();
     	Sha256Hash txId = input.getOutpoint().getHash();
     	return getTransaction(txId).getOutput(idx);
     }
     
     Block downloadBlock(final Sha256Hash b) throws Exception {
     	
     	Block foundBlock = doNetworkAction(new Function<Object, Block>() {
     		@Override
 			public Block apply(Object o) {
 		    	for (Peer peer : peerGroup.getConnectedPeers()) {
 		    		//System.out.println("downloadBlock(" + b + ") - trying peer " + peer);
 		    		Block block = null;
 					try {
 						block = peer.getBlock(b).get();
 					} catch (InterruptedException | ExecutionException
 							| IOException e) {
 						throw new RuntimeException(e);
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
     
     public String transactionToString(Transaction tx) throws Exception {
 		if (tx == null) {
 			throw new Exception("given a null transaction object");
 		}
     	String output = tx.toString(null) + "\n";
     	BigInteger totalInput = new BigInteger("0");
     	boolean inputAmountsUnknown = false;
     	int numSigned = 0;
     	for (TransactionInput i : tx.getInputs()) {
     		if (i.getScriptBytes().length > 0) {
     			++numSigned;
     		}
     		Sha256Hash prevTxId = i.getOutpoint().getHash();
     		Transaction prevTx = getTransaction(prevTxId);
     		if (prevTx == null) {
     			inputAmountsUnknown = true;
     		}
     		else
     		{
     			totalInput = totalInput.add(prevTx.getOutput((int) i.getOutpoint().getIndex()).getValue());
     		}
     	}
     	BigInteger totalOutput = new BigInteger("0");
     	for (TransactionOutput o : tx.getOutputs()) {
     		totalOutput = totalOutput.add(o.getValue());
     	}
     	output += "Inputs: " + Utils.bitcoinValueToFriendlyString(totalInput) + (inputAmountsUnknown ? "???" : "") + " Outputs: " + Utils.bitcoinValueToFriendlyString(totalOutput) + "\n";
     	output += Integer.toString(numSigned) + "/" + tx.getInputs().size() + " inputs have been signed\n";
     	output += "Confidence " + tx.getConfidence() + "\n";
     	output += "Raw tx hex data\n";
     	output += new String(Hex.encodeHex(tx.bitcoinSerialize())) + "\n";
         return output;
     }
     
     @Command
     public void clearTransactions() {
     	wallet.clearTransactions(0);
     	System.out.println("done");
     }
     
     @Command
     public void history() throws Exception {
         String output = "Tx history:\n";
         List<Transaction> transactions = wallet.getTransactionsByTime();
         for (Transaction t : transactions) {
         	output += transactionToString(t) + "\n";
         }
 		
 		System.out.print(output);
     }
     
     @Command
     public void sync() throws Exception {
     	doNetworkAction(new Function<Object, Object>() {
     		@Override
 			public Object apply(Object o) {
     	        System.out.println("Syncing...");
     	        peerGroup.downloadBlockChain();
 				return null;
 			}
 		});
     }
     
     @Command(description="Manually construct/manipulate transactions")
     public void tx() throws IOException {
     	ShellFactory.createSubshell("tx", theShell, "Tx Shell", txCommands).commandLoop();
     }
     
     @Command
     public void pay(
     		@Param(name="address", description="Destination address")
     		String address,
     		@Param(name="amount", description="BTC amount")
     		double amount) throws Exception {
     	BigInteger btc = Utils.toNanoCoins(Double.toString(amount));
     	final SendRequest request = SendRequest.to(new Address(params, address), btc);
     	request.fee = Utils.toNanoCoins(Double.toString(defaultFee));
     	
     	doNetworkAction(new Function<Object, Object>() {
     		@Override
 			public Object apply(Object o) {
     	        System.out.println("Paying...");
     	        try {
     	        	wallet.sendCoins(peerGroup, request).broadcastComplete.get();
     	        } catch (Exception e) {
     	        	throw new RuntimeException(e);
     	        }
 				return null;
 			}
 		});    	
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
     	
     	BigInteger estimateTotalBalance = new BigInteger("0");
     	Iterator<ECKey> it = wallet.getKeys().iterator();
     	while (it.hasNext()) {
     		ECKey key = it.next();
     		byte[] pubKeyHash = key.getPubKeyHash();
     		Address address = new Address(params, pubKeyHash);
     		String output = "address: " + address.toString() + "\n";
     		
     		List<Transaction> transactions = wallet.getTransactionsByTime();
     		BigInteger estimateAddressBalance = new BigInteger("0");
     		int worstDepth = Integer.MAX_VALUE;
             for (Transaction t : transactions) {
             	for (TransactionOutput o : t.getOutputs()) {
             		if (o.isAvailableForSpending()) {
 	            		if (o.getScriptPubKey().getToAddress().equals(address)) {
 	            			estimateAddressBalance = estimateAddressBalance.add(o.getValue());
 	            			if (t.getConfidence().getConfidenceType() == ConfidenceType.BUILDING) {
 		            			int depth = t.getConfidence().getDepthInBlocks();
 		            			worstDepth = (depth < worstDepth ? depth : worstDepth);
 	            			}
 	            		}
             		}
             	}
             }
             estimateTotalBalance = estimateTotalBalance.add(estimateAddressBalance);
             output += "  balance estimate: " + Utils.bitcoinValueToFriendlyString(estimateAddressBalance);
             if (worstDepth != Integer.MAX_VALUE) {
             	output += " (worst depth " + worstDepth + ")";
             }
             
             System.out.println(output);
     	}
     	System.out.println("Total balance estimate: " + Utils.bitcoinValueToFriendlyString(estimateTotalBalance) + ", Total available: " + Utils.bitcoinValueToFriendlyString(wallet.getBalance()));
     }
     
     public void commitToWallet(Transaction tx) throws VerificationException, IOException {
     	wallet.commitTx(tx);
     	wallet.saveToFile(walletFile);
     }
     
     public void init(String n) throws Exception {
 
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
 
        chain = new BlockChain(params, wallet, blockStore);
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
         
         txCommands = new TxCommands(this);
     }
     
     
     private Shell theShell;
 
     public void cliSetShell(Shell theShell) {
         this.theShell = theShell;
     }
 
 }
