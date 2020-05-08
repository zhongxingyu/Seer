 package org.softwareFm.collections.explorer.internal;
 
 import org.softwareFm.card.card.ICardHolderForTests;
 import org.softwareFm.card.configuration.CardConfig;
 import org.softwareFm.card.dataStore.CardDataStoreFixture;
 import org.softwareFm.collections.explorer.IExplorer;
 import org.softwareFm.collections.explorer.IMasterDetailSocial;
 import org.softwareFm.display.swt.SwtTest;
 import org.softwareFm.display.timeline.IPlayListGetter;
 import org.softwareFm.utilities.services.IServiceExecutor;
 
 public class IExplorerTest extends SwtTest {
 
 	private IServiceExecutor service;
 
 	public void testConstructorCreatesExplorerAndCardHolderIsSetUp() {
		CardConfig cardConfig = CardDataStoreFixture.syncCardConfig(display);
 		String rootUrl = "rootUrl";
 		IPlayListGetter playListGetter = IPlayListGetter.Utils.noPlayListGetter();
 		IMasterDetailSocial masterDetailSocial = IMasterDetailSocial.Utils.masterDetailSocial(shell);
 		Explorer explorer = (Explorer) IExplorer.Utils.explorer(masterDetailSocial, cardConfig, rootUrl, playListGetter, service);
 		ICardHolderForTests cardHolder = (ICardHolderForTests) explorer.cardHolder;
 		assertEquals(rootUrl, cardHolder.getRootUrl());
 		assertEquals(cardConfig, cardHolder.getCardConfig());
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		service = IServiceExecutor.Utils.defaultExecutor();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		service.shutdown();
 		super.tearDown();
 	}
 
 }
