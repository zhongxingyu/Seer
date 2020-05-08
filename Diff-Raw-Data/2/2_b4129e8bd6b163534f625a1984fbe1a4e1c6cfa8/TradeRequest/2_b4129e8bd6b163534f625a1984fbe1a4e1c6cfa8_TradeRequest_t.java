 package com.dustyneuron.bitprivacy.exchanger.messages;
 
 import java.math.BigInteger;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.VerificationException;
 
 public class TradeRequest {
 	final public String type = "trade";
 	public Date time;
 	public BigInteger btc;
 	public UnspentOutput source;
 	public String output;
 	
 	public TradeRequest() {		
 	}
 	
 	public TradeRequest(Transaction tx, int unspentOutputIdx, Address dest) throws Exception {
 		time = new Date();
 		source = new UnspentOutput(tx, unspentOutputIdx);
 		btc = tx.getOutput(unspentOutputIdx).getValue();
 		output = dest.toString();
 	}
 	
 	public void verify(WalletMgr walletMgr) throws Exception {
 		
 		if (time.after(new Date())) {
 			throw new VerificationException("Time is in the future");
 		}
 		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 		c.add(Calendar.DAY_OF_YEAR, -7);
 		if (c.after(time)) {
 			throw new VerificationException("Time is older than one week");
 		}
 		
 		Transaction foundTx = source.getTransaction(walletMgr);
 		if (foundTx == null) {
 			throw new VerificationException("No such tx");
 		}
 		
 		BigInteger amount = foundTx.getOutput(source.outputIdx).getValue();
		if (amount.compareTo(btc) != 0) {
 			throw new VerificationException("BTC value incorrect: " + amount + " != " + btc);
 		}
 	}
 }
