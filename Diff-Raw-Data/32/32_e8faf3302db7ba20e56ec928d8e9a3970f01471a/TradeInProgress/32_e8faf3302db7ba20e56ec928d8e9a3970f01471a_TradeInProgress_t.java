 package com.dustyneuron.bitprivacy.exchanger;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ListedTrade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ListedTradePartyType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ReferenceType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TradeInfo;
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.dustyneuron.bitprivacy.schemas.SchemaUtils;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.protobuf.ByteString;
 
 public class TradeInProgress {
 	public Sha256Hash schemaHash;
 	public int tradeId;
 	private Trade trade;	
 	private final Map<ContactInfo, Boolean> haveSigned = new HashMap<ContactInfo, Boolean>();
 	private Transaction transaction;
 	
 	public TradeInProgress(Trade t, ContactInfo contact) {
 		trade = t;
 		schemaHash = SchemaUtils.getSchemaKey(t.getSchema());
 		haveSigned.put(contact, false);
 		tradeId = (int) Math.floor(Math.random() * (double) Integer.MAX_VALUE);
 	}
 	
 	public TradeInfo getTradeInfo() {
 		return TradeInfo.newBuilder()
 				.setSchemaHash(ByteString.copyFrom(schemaHash.getBytes()))
 				.setTradeId(tradeId)
 				.build();
 	}
 	
 	public ListedTrade getListedTrade() {
 		ListedTrade.Builder l = ListedTrade.newBuilder()
 				.setTradeInfo(getTradeInfo())
 				.setSchema(trade.getSchema());
 		
 		int numPartyTypes = trade.getSchema().getPartyTypesList().size();
 		int[] counts = new int[numPartyTypes];
 		for (int i = 0; i < numPartyTypes; ++i) {
 			counts[i] = 0;
 		}
 		for (SinglePartyData party : trade.getAllPartiesDataList()) {
 			counts[party.getPartyIdx()] += 1;
 		}
 		for (int i = 0; i < numPartyTypes; ++i) {
 			if (counts[i] > 0) {
 				l.addPartyTypes(ListedTradePartyType.newBuilder()
 						.setMembers(counts[i])
 						.setPartyIdx(i)
 						.build());
 			}
 		}
 		
 		return l.build();
 	}
 	
 	public void addParticipent(Trade updated, ContactInfo contact) throws Exception {
 		if (haveSigned.containsKey(contact)) {
 			throw new Exception("error already have this contact");
 		}
 		haveSigned.put(contact, false);
 		trade = updated;
 	}
 	
 	public Trade getTrade() {
 		return trade;
 	}
 	
 	public void createInitialTransaction(WalletMgr walletMgr) throws Exception {
 		if (transaction != null) {
 			throw new Exception("already have an in-progress transaction");
 		}
 		
 		transaction = new Transaction(walletMgr.params);
 		for (SinglePartyData party : trade.getAllPartiesDataList()) {
 			for (DataItem d : party.getDataList()) {
 				if (d.getReference().getRefType() == ReferenceType.INPUT) {
 					Transaction prevTx = walletMgr.getExternalTransaction(new Sha256Hash(d.getBlockId()), new Sha256Hash(d.getTxId()));
 					transaction.addInput(prevTx.getOutput(d.getOutputIndex()));
 				} else {
 					if (!d.hasValue()) {
 						throw new Exception("unimplemented... different party sets output address to the party that sets the value");
 					}
 					BigInteger outputValue = SchemaUtils.readBigInteger(d.getValue());
 					transaction.addOutput(outputValue, new Address(walletMgr.params, d.getAddress()));
 				}
 			}
 		}
 		//Sha256Hash key = tx.getHash();
 	}
 	
 	
 	public byte[] getTransactionBytes() {
 		return transaction.bitcoinSerialize();
 	}
 	
 	public String getTransactionToString(WalletMgr walletMgr) throws Exception {
 		return walletMgr.transactionToString(transaction);
 	}
 	
 	public void markParticipentSigned (WalletMgr walletMgr, ContactInfo contact, byte[] txData) throws Exception {
 		
 		Transaction tx = new Transaction(walletMgr.params, txData);
 		
 		transaction = tx;
 		
		ContactInfo foundContact = null;
		for (ContactInfo c : haveSigned.keySet()) {
			if (c.equals(contact)) {
				foundContact = c;
				c.setChannel(contact.getChannel());
				break;
			}
		}
		if (foundContact == null) {
 			throw new Exception("contact unknown");
 		}
		haveSigned.put(foundContact, true);		
 	}
 	
 	public ContactInfo getNextToSign() {
 		for (ContactInfo c : haveSigned.keySet()) {
 			if (!haveSigned.get(c)) {
 				return c;
 			}
 		}
 		// All signed!
 		return null;
 	}
 	
 	public Set<ContactInfo> getAllParticipents() {
 		return haveSigned.keySet();
 	}
 	
 	 @Override
 	public String toString() {
 		String s = "TradeInProgress " + schemaHash + " #" + tradeId + "\n";
 		s += trade.getSchema().toString();
 		for (SinglePartyData d : trade.getAllPartiesDataList()) {
 			s += "party #" + trade.getAllPartiesDataList().indexOf(d) + "\n";
 			s += d.toString();
 		}
 		s += "\n";
 
 		for (ContactInfo c : haveSigned.keySet()) {
 			s += "  " + c.toString() + " haveSigned = " + haveSigned.get(c) + '\n';
 		}
 		s += "\n\n";
 		return s;
 	}
 	
 }
