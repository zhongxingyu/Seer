 package com.dustyneuron.bitprivacy.exchanger;
 
 import static org.junit.Assert.*;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.bitcoin.WalletUtils;
 import com.dustyneuron.bitprivacy.schemas.SchemaUtils;
 import com.dustyneuron.bitprivacy.schemas.SimpleMix;
 import com.dustyneuron.bitprivacy.schemas.WalletHarness;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Utils;
 
 public class NetMixTest {
 	final static NetworkParameters params = NetworkParameters.testNet();
 	
 	public class Party {
 		String privKey;
 		String blockId;
 		String txId;
 		int txOutput;
 		
 		WalletHarness wallet;
 		Address toAddress;
 		Trade trade;
 		MixClient client;
 		boolean haveSigned;
 		
 		public Party(String privKey, String blockId, String txId, int txOutput) throws Exception {
 			this.privKey = privKey;
 			this.blockId = blockId;
 			this.txId = txId;
 			this.txOutput = txOutput;
 		}
 		
 		public void init(TradeDHT dht) throws Exception {
 			wallet = new WalletHarness(params);
 			wallet.importPrivateKey(privKey);
 			toAddress = null;
 			trade = null;
 			client = new MixClient(dht, wallet.getWallet(), wallet, false);
 			haveSigned = false;
 		}
 		
 		public Trade createTrade(BigInteger mixAmount, int numParties) throws Exception {
 			wallet.getWallet().commitTx(
 					WalletUtils.findTransaction(wallet.downloadBlock(new Sha256Hash(blockId)), new Sha256Hash(txId)));
 			toAddress = wallet.generate();
 			trade = SimpleMix.createTrade(
 					blockId,
 					txId,
 					txOutput,
 					mixAmount,
 					toAddress,
 					numParties);
 			assertNotNull(trade);
 			assertTrue(SchemaUtils.isInitialListingValid(trade.getSchema(), trade.getAllPartiesDataList()));
 			return trade;
 		}
 	}
 	
 	public void harness(String btcValue, List<Party> parties) throws Exception {
 		harness(btcValue, parties, true, -1);
 		harness(btcValue, parties, false, -1);
 	}
 		
 	public void harness(String btcValue, List<Party> parties, boolean mockDHT, int disrupterIdx) throws Exception {
 		BigInteger mixAmount = Utils.toNanoCoins(btcValue);
 		
 		MockDHTStore mockStore = null;
 		if (mockDHT) {
 			mockStore = new MockDHTStore();
 		}
 		
 		int numParties = parties.size();
 		if (disrupterIdx != -1) {
 			--numParties;
 		}
 		
 		for (int i = 0; i < parties.size(); ++i) {
 			Party p = parties.get(i);
 			TradeDHT dht = null;
 			if (mockDHT) {
 				dht = new MockDHT(mockStore, i);
 			} else {
 				dht = new TomP2PDHT();
 			}
 			p.init(dht);
 			p.createTrade(mixAmount, numParties);
 		}
 
 		for (int i = 0; i < parties.size(); ++i) {
 			Party p = parties.get(i);
 			if (i == 0) {
 				p.client.connect();
 			} else {
 				p.client.connect("localhost");
 			}
 			if (i != disrupterIdx) {
 				Transaction tx = p.client.tradeRequest(p.trade);
 				if (parties.size() > 1) {
 					assertNull("trade completed too early", tx);
				}
				
				if (disrupterIdx != -1) {
					parties.get(disrupterIdx).client.tryDisrupt(p.trade.getSchema());
				}
 			}
 		}
 				
 		for (int j = 0; j < parties.size(); ++j) {
 			for (int i = 0; i < parties.size(); ++i) {
 				Party p = parties.get(i);
 				if (!p.client.areAllTradesComplete()) {
 					if (i != disrupterIdx) {
						p.client.pollTrades();
						
 						if (disrupterIdx != -1) {
 							parties.get(disrupterIdx).client.tryDisrupt(p.trade.getSchema());
 						}
 					}
 				}
 			}
 		}
 		
 		for (int i = 0; i < parties.size(); ++i) {
 			if (i != disrupterIdx) {
 				Party p = parties.get(i);
 				assertTrue("Peer " + p.client.peerIdToString() + " has incomplete trade(s)", p.client.areAllTradesComplete());
 				p.client.disconnect();
 			}
 		}
 	}
 	
 	@Test
 	public void mixTwo() throws Exception {
 		harness("0.1", Arrays.asList(
 				new Party(
 						"cVWQR3kHXhfD7FX9ELAwzzjvwsihwekLTxKDH3VbDg7E883v99dD",
 						"0000000071762d682cbe381ce20203303dacc370126bc25b5abb025b47c34a2a",
 						"2fdf5e1d94ae9e7d651938ff3902f2d917954a004a949398a8ef270300da64c4",
 						0),
 				new Party(
 						"cVPw8hYiYg3Kb5duYYNK7oEjT1SFB9J7nAC5fBo8hifHZCKQfEn2",
 						"0000000000a1317058ae8c8730fac13a294fc7bc20f3e098633837fe9f799d9b",
 						"718471262dc9f5e4f69f78a00c7e5f09df9782835f5274d85401edf901a098f3",
 						0)
 			));
 	}
 	
 	@Test
 	public void mixTwoAlt() throws Exception {
 		harness("0.1", Arrays.asList(
 				new Party(
 						"cVPw8hYiYg3Kb5duYYNK7oEjT1SFB9J7nAC5fBo8hifHZCKQfEn2",
 						"0000000000a1317058ae8c8730fac13a294fc7bc20f3e098633837fe9f799d9b",
 						"718471262dc9f5e4f69f78a00c7e5f09df9782835f5274d85401edf901a098f3",
 						0),
 				new Party(
 						"cVWQR3kHXhfD7FX9ELAwzzjvwsihwekLTxKDH3VbDg7E883v99dD",
 						"0000000071762d682cbe381ce20203303dacc370126bc25b5abb025b47c34a2a",
 						"2fdf5e1d94ae9e7d651938ff3902f2d917954a004a949398a8ef270300da64c4",
 						0)
 			));
 	}
 
 	
 	@Test
 	public void mixThree() throws Exception {
 		harness("0.1", Arrays.asList(
 				new Party(
 						"cVWQR3kHXhfD7FX9ELAwzzjvwsihwekLTxKDH3VbDg7E883v99dD",
 						"0000000071762d682cbe381ce20203303dacc370126bc25b5abb025b47c34a2a",
 						"2fdf5e1d94ae9e7d651938ff3902f2d917954a004a949398a8ef270300da64c4",
 						0),
 				new Party(
 						"cVPw8hYiYg3Kb5duYYNK7oEjT1SFB9J7nAC5fBo8hifHZCKQfEn2",
 						"0000000000a1317058ae8c8730fac13a294fc7bc20f3e098633837fe9f799d9b",
 						"718471262dc9f5e4f69f78a00c7e5f09df9782835f5274d85401edf901a098f3",
 						0),
 				new Party(
 						"cVdahi99xh86rxFPBdQW2zcsAzKxxknd3k2RGKCioj119QqUDLX7",
 						"00000000014475f074027aeacd94e37df59a2e9f27854913b2bca82a3a0bd14f",
 						"9a646db92123ebe8e45456b76b1ad18870f3f0b2b3aaffc2192501f83f016a2c",
 						0)
 			));
 	}
 
 	@Test
 	public void mixDisrupter() throws Exception {
 		harness("0.1", Arrays.asList(
 				new Party(
 						"cVWQR3kHXhfD7FX9ELAwzzjvwsihwekLTxKDH3VbDg7E883v99dD",
 						"0000000071762d682cbe381ce20203303dacc370126bc25b5abb025b47c34a2a",
 						"2fdf5e1d94ae9e7d651938ff3902f2d917954a004a949398a8ef270300da64c4",
 						0),
 				new Party(
 						"cVPw8hYiYg3Kb5duYYNK7oEjT1SFB9J7nAC5fBo8hifHZCKQfEn2",
 						"0000000000a1317058ae8c8730fac13a294fc7bc20f3e098633837fe9f799d9b",
 						"718471262dc9f5e4f69f78a00c7e5f09df9782835f5274d85401edf901a098f3",
 						0),
 				new Party(
 						"cVdahi99xh86rxFPBdQW2zcsAzKxxknd3k2RGKCioj119QqUDLX7",
 						"00000000014475f074027aeacd94e37df59a2e9f27854913b2bca82a3a0bd14f",
 						"9a646db92123ebe8e45456b76b1ad18870f3f0b2b3aaffc2192501f83f016a2c",
 						0)
 			),
 			false,
 			2);
 	}
 }
