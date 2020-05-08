 package com.dustyneuron.bitprivacy.exchanger;
 
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 
 import org.spongycastle.crypto.AsymmetricCipherKeyPair;
 import org.spongycastle.crypto.CryptoException;
 import org.spongycastle.crypto.generators.RSAKeyPairGenerator;
 import org.spongycastle.crypto.params.RSAKeyGenerationParameters;
 import org.spongycastle.crypto.params.RSAKeyParameters;
 import org.spongycastle.util.Arrays;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Blinded;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SignedBlinded;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SignedUnblinded;
 import com.google.protobuf.ByteString;
 
 public class MockBlinding implements Blinding {
 	
 	private SecureRandom getSecureRandom() {
 		InsecureRandomTest.initInsecureRandom();
 		try {
 			return SecureRandom.getInstance("InsecureRandom");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public AsymmetricCipherKeyPair generateKeyPair() {
 		// Can't return null as public key is extracted from it 
 		RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
 		RSAKeyGenerationParameters params =
				new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), getSecureRandom(), 512, 12);
 		gen.init(params);
 		return gen.generateKeyPair();
 	}
 	
 	@Override
 	public BigInteger generateBlindingFactor(RSAKeyParameters keyPair) {
 		return new BigInteger(16, getSecureRandom());
 	}
 	
 	@Override
 	public Blinded blind(DataItem privateData, BigInteger blindingFactor, RSAKeyParameters publicKey) throws CryptoException {
 		BigInteger val = new BigInteger(privateData.getAddress().getBytes()).add(blindingFactor);
 		
 		return Blinded.newBuilder()
 			.setReference(privateData.getReference())
 			.setValue(privateData.getValue())
 			.setData(ByteString.copyFrom(val.toByteArray()))
 			.build();
 	}
 
 	@Override
 	public SignedBlinded sign(Blinded a, AsymmetricCipherKeyPair keyPair) {
 		BigInteger val = new BigInteger(a.getData().toByteArray());
 		val = val.add(new BigInteger("100"));
 		
 		return SignedBlinded.newBuilder()
 				.setReference(a.getReference())
 				.setValue(a.getValue())
 				.setData(ByteString.copyFrom(val.toByteArray()))
 				.build();
 	}
 
 	@Override
 	public SignedUnblinded unblind(SignedBlinded s, DataItem privateData, BigInteger blindingFactor, RSAKeyParameters publicKey) {
 		BigInteger val = new BigInteger(s.getData().toByteArray());
 		val = val.subtract(blindingFactor);
 		
 		return SignedUnblinded.newBuilder()
 				.setReference(s.getReference())
 				.setValue(s.getValue())
 				.setAddress(ByteString.copyFrom(privateData.getAddress().getBytes()))
 				.setSignature(ByteString.copyFrom(val.toByteArray()))
 				.build();
 	}
 
 	@Override
 	public DataItem getValue(SignedUnblinded s) {		
 		return DataItem.newBuilder()
 				.setReference(s.getReference())
 				.setValue(s.getValue())
 				.setBlindedAddress(true)
 				.setAddress(new String(s.getAddress().toByteArray()))
 				.build();
 	}
 
 	@Override
 	public boolean verify(SignedUnblinded s, DataItem privateData, RSAKeyParameters publicKey) {
 		byte[] msg = s.getAddress().toByteArray();
 		byte[] original = privateData.getAddress().getBytes();
 		if (!Arrays.areEqual(msg, original)) {
 			return false;
 		}
 
 		BigInteger val = new BigInteger(s.getSignature().toByteArray());
 		val = val.subtract(new BigInteger("100"));
 		
 		byte[] fromSig = val.toByteArray();
 
 		return Arrays.areEqual(msg, fromSig);
 	}
 }
