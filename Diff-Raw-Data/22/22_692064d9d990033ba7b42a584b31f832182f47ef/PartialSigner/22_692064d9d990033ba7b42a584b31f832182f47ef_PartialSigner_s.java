 package com.dustyneuron.txmarket.schema;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Hex;
 import org.spongycastle.crypto.params.KeyParameter;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItemContent;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ReferenceType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.txmarket.bitcoin.WalletUtils;
 import com.google.bitcoin.core.ECKey;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.script.Script;
 import com.google.bitcoin.core.ScriptException;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Transaction.SigHash;
 import com.google.bitcoin.core.TransactionConfidence;
 import com.google.bitcoin.core.TransactionInput;
 import com.google.bitcoin.core.TransactionOutPoint;
 import com.google.bitcoin.core.TransactionOutput;
 import com.google.bitcoin.core.UnsafeByteArrayOutputStream;
 import com.google.bitcoin.core.Wallet;
 import com.google.common.base.Preconditions;
 
 public class PartialSigner {
 
     public static Transaction doSignRequest(Transaction t,
             List<SinglePartyData> myData, Wallet wallet) throws ScriptException {
 
         final SigHash hashType = Transaction.SigHash.ALL;
         final KeyParameter aesKey = null;
 
         List<TransactionInput> inputs = t.getInputs();
         List<TransactionOutput> outputs = t.getOutputs();
 
         Preconditions.checkState(inputs.size() > 0);
         Preconditions.checkState(outputs.size() > 0);
 
         List<DataItem> inputsData = new ArrayList<DataItem>();
         for (SinglePartyData party : myData) {
             for (DataItem d : party.getDataList()) {
                 if (d.getHeader().getReference().getRefType() == ReferenceType.INPUT) {
                     inputsData.add(d);
                 }
             }
         }
         if (inputsData.size() == 0) {
             System.out
                     .println("This trade doesn't actually need us to sign anything, returning the same tx");
             return t;
         }
 
         final NetworkParameters params = wallet.getNetworkParameters();
 
         byte[][] signatures = new byte[inputs.size()][];
         ECKey[] signingKeys = new ECKey[inputs.size()];
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
                 TransactionOutPoint outpoint = WalletUtils
                         .getConnectedOutPoint(input, wallet);
                 if (outpoint == null) {
                     System.out.println("null outpoint for input #" + i
                             + ", assuming we don't have key anyway");
                 } else {
                     DataItem matchingItem = null;
                     for (DataItem d : inputsData) {
                         DataItemContent content = d.getContent();
                         if (outpoint.getHash().equals(
                                 new Sha256Hash(Hex.encodeHexString(content
                                         .getTxId().toByteArray())))
                                 && (outpoint.getIndex() == content
                                         .getOutputIndex())) {
                             matchingItem = d;
                             break;
                         }
                     }
 
                     if (matchingItem != null) {
                         inputsData.remove(matchingItem);
 
                         ECKey key = outpoint.getConnectedKey(wallet);
                         if (key == null) {
                             System.out
                                     .println("we do not have the key for input #"
                                             + i);
                         } else {
                             // Keep the key around for the script creation step
                             // below.
                             signingKeys[i] = key;
                             // The anyoneCanPay feature isn't used at the
                             // moment.
                             boolean anyoneCanPay = false;
                             byte[] connectedPubKeyScript = WalletUtils
                                     .getConnectedOutput(input, wallet)
                                     .getScriptBytes();
                            Sha256Hash hash = t.hashTransactionForSignature(i,
                                     connectedPubKeyScript, hashType,
                                     anyoneCanPay);
 
                             // Now sign for the output so we can redeem it. We
                             // use the keypair to sign the hash,
                             // and then put the resulting signature in the
                             // script along with the public key (below).
                             try {
                                 // Usually 71-73 bytes.
                                 ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(
                                         73);
                                 bos.write(key.sign(hash, aesKey).encodeToDER());
                                 bos.write((hashType.ordinal() + 1)
                                         | (anyoneCanPay ? 0x80 : 0));
                                 signatures[i] = bos.toByteArray();
                                 bos.close();
                             } catch (IOException e) {
                                 throw new RuntimeException(e); // Cannot happen.
                             }
                         }
                     }
                 }
             }
         }
 
         if (inputsData.size() != 0) {
             System.err
                     .println("We expected to sign more inputs than this... bogus transaction");
             return null;
         }
 
         // Now we have calculated each signature, go through and create the
         // scripts. Reminder: the script consists:
         // 1) For pay-to-address outputs: a signature (over a hash of the
         // simplified transaction) and the complete
         // public key needed to sign for the connected output. The output script
         // checks the provided pubkey hashes
         // to the address and then checks the signature.
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
                 } else {
                     Script scriptPubKey = WalletUtils.getConnectedOutput(input,
                             wallet).getScriptPubKey();
                     byte[] scriptBytes;
                     if (scriptPubKey.isSentToAddress()) {
                         scriptBytes = Script.createInputScript(signatures[i],
                                 key.getPubKey());
                     } else if (scriptPubKey.isSentToRawPubKey()) {
                         scriptBytes = Script.createInputScript(signatures[i]);
                     } else {
                         // Should be unreachable - if we don't recognize the
                         // type of script we're trying to sign for, we should
                         // have failed above when fetching the key to sign with.
                         throw new RuntimeException(
                                 "Do not understand script type: "
                                         + scriptPubKey);
                     }
                     // input.setScriptBytes(scriptBytes);
                     newTx.addInput(new TransactionInput(params, newTx,
                             scriptBytes, input.getOutpoint()));
                 }
             } else {
                 newTx.addInput(input);
             }
         }
         return newTx;
     }
 
     /*
      * Danger: this just signs as many inputs as possible
      */
     public static Transaction dangerousSignMany(Transaction t, Wallet wallet)
             throws Exception {
 
         final SigHash hashType = Transaction.SigHash.ALL;
         final KeyParameter aesKey = null;
 
         List<TransactionInput> inputs = t.getInputs();
         List<TransactionOutput> outputs = t.getOutputs();
 
         Preconditions.checkState(inputs.size() > 0);
         Preconditions.checkState(outputs.size() > 0);
 
         final NetworkParameters params = wallet.getNetworkParameters();
 
         // I don't currently have an easy way to test other modes work, as the
         // official client does not use them.
         Preconditions.checkArgument(hashType == SigHash.ALL,
                 "Only SIGHASH_ALL is currently supported");
 
         // The transaction is signed with the input scripts empty except for the
         // input we are signing. In the case
         // where addInput has been used to set up a new transaction, they are
         // already all empty. The input being signed
         // has to have the connected OUTPUT program in it when the hash is
         // calculated!
         //
         // Note that each input may be claiming an output sent to a different
         // key. So we have to look at the outputs
         // to figure out which key to sign with.
 
         byte[][] signatures = new byte[inputs.size()][];
         ECKey[] signingKeys = new ECKey[inputs.size()];
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
 
                 TransactionOutPoint outpoint = WalletUtils
                         .getConnectedOutPoint(input, wallet);
                 if (outpoint == null) {
                     System.out.println("null outpoint for input #" + i
                             + ", assuming we don't have key anyway");
                 } else {
                     ECKey key = outpoint.getConnectedKey(wallet);
                     if (key == null) {
                         System.out.println("we do not have the key for input #"
                                 + i);
                     } else {
                         // Keep the key around for the script creation step
                         // below.
                         signingKeys[i] = key;
                         // The anyoneCanPay feature isn't used at the moment.
                         boolean anyoneCanPay = false;
                         byte[] connectedPubKeyScript = WalletUtils
                                 .getConnectedOutput(input, wallet)
                                 .getScriptBytes();
                        Sha256Hash hash = t.hashTransactionForSignature(i,
                                 connectedPubKeyScript, hashType, anyoneCanPay);
 
                         // Now sign for the output so we can redeem it. We use
                         // the keypair to sign the hash,
                         // and then put the resulting signature in the script
                         // along with the public key (below).
                         try {
                             // Usually 71-73 bytes.
                             ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(
                                     73);
                             bos.write(key.sign(hash, aesKey).encodeToDER());
                             bos.write((hashType.ordinal() + 1)
                                     | (anyoneCanPay ? 0x80 : 0));
                             signatures[i] = bos.toByteArray();
                             bos.close();
                         } catch (IOException e) {
                             throw new RuntimeException(e); // Cannot happen.
                         }
                     }
                 }
             }
         }
 
         // Now we have calculated each signature, go through and create the
         // scripts. Reminder: the script consists:
         // 1) For pay-to-address outputs: a signature (over a hash of the
         // simplified transaction) and the complete
         // public key needed to sign for the connected output. The output script
         // checks the provided pubkey hashes
         // to the address and then checks the signature.
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
                 } else {
                     Script scriptPubKey = WalletUtils.getConnectedOutput(input,
                             wallet).getScriptPubKey();
                     byte[] scriptBytes;
                     if (scriptPubKey.isSentToAddress()) {
                         scriptBytes = Script.createInputScript(signatures[i],
                                 key.getPubKey());
                     } else if (scriptPubKey.isSentToRawPubKey()) {
                         scriptBytes = Script.createInputScript(signatures[i]);
                     } else {
                         // Should be unreachable - if we don't recognize the
                         // type of script we're trying to sign for, we should
                         // have failed above when fetching the key to sign with.
                         throw new RuntimeException(
                                 "Do not understand script type: "
                                         + scriptPubKey);
                     }
                     // input.setScriptBytes(scriptBytes);
                     newTx.addInput(new TransactionInput(params, newTx,
                             scriptBytes, input.getOutpoint()));
                 }
             } else {
                 newTx.addInput(input);
             }
         }
         return newTx;
     }
 }
