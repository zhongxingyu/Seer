 package com.dustyneuron.bitprivacy.schemas;
 
 
 import java.lang.reflect.Array;
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TimeZone;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element.ElementType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element.VariableType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Expression;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Formula;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.FormulaStatement;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.FormulaStatement.Comparator;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.IOTypeReference;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.IOTypeReference.IndexReferenceType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.LimitType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.PartyType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Specifier;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSchema;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.InputType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.OutputType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.BtcLimit;
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Utils;
 import com.google.bitcoin.core.VerificationException;
 import com.google.protobuf.ByteString;
 
 
 public class SimpleMix {
 	static Map<TransactionSchema, BigInteger> schemas = new HashMap<TransactionSchema, BigInteger>();
 	
 	static public Sha256Hash getSchemaKey(TransactionSchema schema) {
 		return Sha256Hash.create(schema.toByteArray());
 	}
 
 	static public void verify(WalletMgr walletMgr, Trade trade) throws Exception {
 		
 		Date time = new Date(trade.getTimestamp());
 		
 		if (time.after(new Date())) {
 			throw new VerificationException("Time is in the future");
 		}
 		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 		c.add(Calendar.DAY_OF_YEAR, -7);
 		if (c.after(time)) {
 			throw new VerificationException("Time is older than one week");
 		}
 		
 		BigInteger value =
 				new BigInteger(trade.getSchema().getInputTypes(0).getBtcLimits(0).getValue().toByteArray());
 		
 		if (!Arrays.equals(trade.getSchema().toByteArray(), getSchema(value).toByteArray())) {
 			throw new Exception("schemas differ!");
 		}
 		
 		if (trade.getAllPartiesDataCount() != 1) {
 			throw new Exception("trade had " + trade.getAllPartiesDataCount() + " parties data, should have been 1");
 		}
 		SinglePartyData partyData = trade.getAllPartiesData(0);
 		if (partyData.getPartyIdx() != 0) {
 			throw new Exception("wrong party idx");
 		}
 		
 		String s = partyData.getData(0).getBlockId();
 		if (s.isEmpty()) {
 			throw new Exception("no block id");
 		}
 		Sha256Hash blockId = new Sha256Hash(s);
 
 		s = partyData.getData(0).getTxId();
 		if (s.isEmpty()) {
 			throw new Exception("no tx id");
 		}
 		Sha256Hash tx = new Sha256Hash(s);
 
 		int outputIdx = partyData.getData(0).getOutputIndex();
 		
 		Transaction foundTx = walletMgr.getExternalTransaction(blockId, tx);
 		if (foundTx == null) {
 			throw new VerificationException("No such tx");
 		}
 		
 		BigInteger amount = foundTx.getOutput(outputIdx).getValue();
 		if (amount.compareTo(value) != 0) {
 			throw new VerificationException("BTC value incorrect: " + amount + " != " + value);
 		}
 		
 		if (isTradeFinished(trade)) {
 			throw new Exception("cannot list a finished trade");
 		}
 	}
 	
 	static public boolean canAddTrade(WalletMgr walletMgr, Trade original, Trade newTrade) throws Exception {
 		
 		verify(walletMgr, newTrade);
 		
 		for (SinglePartyData p : newTrade.getAllPartiesDataList()) {
 			if (!canAddParty(walletMgr, original, p)) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	static public boolean isTradeFinished(Trade original) throws Exception {
 		return false;
 	}
 	
 	static public Trade combineTrades(Trade a, Trade b) throws Exception {
 		Trade.Builder newTrade = Trade.newBuilder(a);
 		for (SinglePartyData p : b.getAllPartiesDataList()) {
 			newTrade.addAllPartiesData(p);
 		}
 		return newTrade.build();
 	}
 
 	
 	static boolean canAddParty(WalletMgr walletMgr, Trade original, SinglePartyData newParty) throws Exception {
 		// Is newParty valid according to the schema?
 		//	is it a valid party type
 		//  and does it set all + only all the vars specified
 		
 		// Then, if we add this new party,
 		// do we break any formulae?
 		
 		// think that's it???
 		
 		return true;
 	}
 
 	
 	public static Trade createTrade(Transaction tx, int unspentOutputIdx, BigInteger value, Address dest) {
 		TransactionSchema schema = getSchema(value);
 		Trade t = Trade.newBuilder()
 				.setSchema(schema)
				.setTimestamp(new Date().getTime())
 				.addAllPartiesData(SinglePartyData.newBuilder()
 						.setPartyIdx(0)
 						.addData(DataItem.newBuilder()
 								.setReference(IOTypeReference.newBuilder()
 										.setRefType(IndexReferenceType.INPUT)
 										.setRefIdx(0)
 										.build())
 								.setBlockId(tx.getAppearsInHashes().iterator().next().toString())
 								.setTxId(tx.getHash().toString())
 								.setOutputIndex(unspentOutputIdx)
 								.build())
 						.addData(DataItem.newBuilder()
 								.setReference(IOTypeReference.newBuilder()
 										.setRefType(IndexReferenceType.OUTPUT)
 										.setRefIdx(0)
 										.build())
 								.setAddress(dest.toString())
 								.build())
 						.build())
 				.build();
 		
 		return t;
 	}
 	
 	static TransactionSchema getSchema(BigInteger v) {
 		for (TransactionSchema s : schemas.keySet()) {
 			if (schemas.get(s).compareTo(v) == 0) {
 				return s;
 			}
 		}
 		
 		TransactionSchemaProtos.BigInteger btcValue = TransactionSchemaProtos.BigInteger.newBuilder()
 				.setData(ByteString.copyFrom(v.toByteArray()))
 				.build();
 		
 		// TODO: fix/make clearer protocol so can say '5 inputs all of the same btc value'
 		
     	TransactionSchema schema = 
     			TransactionSchema.newBuilder()
     				.addInputTypes(InputType.newBuilder()
     						.addBtcLimits(BtcLimit.newBuilder()
     								.setLimitType(LimitType.FIXED)
     								.setValue(btcValue)
     								.build())
     						.build())
     				.addOutputTypes(OutputType.newBuilder()
     						.addBtcLimits(BtcLimit.newBuilder()
     								.setLimitType(LimitType.FIXED)
     								.setValue(btcValue)
     								.build())
     						.build())
 					.addFormulae(Formula.newBuilder()
 							.addStatements(FormulaStatement.newBuilder()
 									.setComparator(Comparator.EQ)
 									.setLhs(Expression.newBuilder()
 											.addElements(Element.newBuilder()
 													.setElementType(ElementType.VARIABLE)
 													.setVariableType(VariableType.COUNT)
 													.setVariable(IOTypeReference.newBuilder()
 															.setRefType(IndexReferenceType.INPUT)
 															.setRefIdx(0)
 															.build())
 													.build())
 											.build())
 									.setRhs(Expression.newBuilder()
 											.addElements(Element.newBuilder()
 													.setElementType(ElementType.VARIABLE)
 													.setVariableType(VariableType.COUNT)
 													.setVariable(IOTypeReference.newBuilder()
 															.setRefType(IndexReferenceType.OUTPUT)
 															.setRefIdx(0)
 															.build())
 													.build())
 											.build())
 									.build())
 							.build())
 					.addPartyTypes(PartyType.newBuilder()
 							.addSpecifiers(Specifier.newBuilder()
 									.setReference(IOTypeReference.newBuilder()
 											.setRefType(IndexReferenceType.INPUT)
 											.setRefIdx(0)
 											.build())
 									.setFixAddress(true)
 									.setSign(true)
 									.build())
 							.addSpecifiers(Specifier.newBuilder()
 									.setReference(IOTypeReference.newBuilder()
 											.setRefType(IndexReferenceType.OUTPUT)
 											.setRefIdx(0)
 											.build())
 									.setFixAddress(true)
 									.build())
 							.build())
 					.build();
     	
     	schemas.put(schema, v);
     	
     	//System.out.println(Hex.encodeHex(schema.toByteArray()));
     	//System.out.println();
     	//System.out.println(schema.toString());
     	return schema;
 	}
 }
