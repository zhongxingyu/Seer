 package com.dustyneuron.bitprivacy.exchanger;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import com.dustyneuron.bitprivacy.exchanger.NetMixTest.BlindingType;
 import com.dustyneuron.bitprivacy.exchanger.NetMixTest.DHTType;
 import com.dustyneuron.bitprivacy.exchanger.NetMixTest.ExpectedResult;
 import com.dustyneuron.bitprivacy.exchanger.TestPartyData.Coin;
 
 public class MixTwo {
 	@Test
 	public void dhtmock_blindingnone_perm12() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.ONE), TestPartyData.party(Coin.TWO)),
 				DHTType.MOCK, BlindingType.NONE, ExpectedResult.COMPLETE);
 	}
 	
 	@Test
 	public void dhttom_blindingnone_perm12() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.ONE), TestPartyData.party(Coin.TWO)),
 				DHTType.TOMP2P, BlindingType.NONE, ExpectedResult.COMPLETE);
 	}
 
 	@Test
 	public void dhtmock_blindingmock_perm12() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.ONE), TestPartyData.party(Coin.TWO)),
 				DHTType.MOCK, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
/*
 	@Test
 	public void dhttom_blindingmock_perm12() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.ONE), TestPartyData.party(Coin.TWO)),
 				DHTType.TOMP2P, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
*/
 	@Test
 	public void dhtmock_blindingreal_perm12() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.ONE), TestPartyData.party(Coin.TWO)),
 				DHTType.MOCK, BlindingType.REAL, ExpectedResult.COMPLETE);
 	}
 /*
 	@Test
 	public void dhttom_blindingmock_perm12() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.ONE), TestPartyData.party(Coin.TWO)),
 				DHTType.TOMP2P, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
 	
 	@Test
 	public void dhttom_blindingmock_perm21() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.TWO), TestPartyData.party(Coin.ONE)),
 				DHTType.TOMP2P, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
 
 	@Test
 	public void dhttom_blindingmock_perm23() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.TWO), TestPartyData.party(Coin.THREE)),
 				DHTType.TOMP2P, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
 	
 	@Test
 	public void dhttom_blindingmock_perm31() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.THREE), TestPartyData.party(Coin.ONE)),
 				DHTType.TOMP2P, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
 
 
 	@Test
 	public void dhttom_blindingmock_perm32() throws Exception {
 		NetMixTest.harness(TestPartyData.coinValue,
 				Arrays.asList(TestPartyData.party(Coin.THREE), TestPartyData.party(Coin.TWO)),
 				DHTType.TOMP2P, BlindingType.MOCK, ExpectedResult.COMPLETE);
 	}
 	*/
 }
