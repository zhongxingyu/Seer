 package com.google.bitcoin.core;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Future;
 
 import static org.easymock.EasyMock.*;
 
 import org.easymock.Capture;
 import org.easymock.IAnswer;
 import org.easymock.IMocksControl;
 import org.junit.Before;
 import org.junit.Test;
 
 
 import com.google.bitcoin.store.MemoryBlockStore;
 
 public class PeerTest {
 
     private Peer peer;
     private IMocksControl control;
     private NetworkConnection conn;
     private NetworkParameters unitTestParams;
     private MemoryBlockStore blockStore;
     private BlockChain blockChain;
 
     @Before
     public void setUp() throws Exception {
         control = createStrictControl();
         control.checkOrder(true);
         unitTestParams = NetworkParameters.unitTests();
         blockStore = new MemoryBlockStore(unitTestParams);
         blockChain = new BlockChain(unitTestParams, new Wallet(unitTestParams), blockStore);
         PeerAddress address = new PeerAddress(InetAddress.getLocalHost());
         //peer = new Peer(unitTestParams, address , testNetChain );
         conn = createMockBuilder(NetworkConnection.class)
             .addMockedMethod("getVersionMessage")
             .addMockedMethod("readMessage")
             .addMockedMethod("writeMessage")
             .addMockedMethod("shutdown")
             .addMockedMethod("toString")
             .createMock(control);
         peer = new Peer(unitTestParams, address , blockChain);
         peer.setConnection(conn);
     }
 
     @Test
     public void testAddEventListener() {
         PeerEventListener listener = new AbstractPeerEventListener();
         peer.addEventListener(listener);
         assertTrue(peer.removeEventListener(listener));
         assertFalse(peer.removeEventListener(listener));
     }
     
     @Test
     public void testRun_exception() throws Exception {
         expect(conn.readMessage()).andThrow(new IOException("done"));
         conn.shutdown();
         expectLastCall();
 
         control.replay();
 
         try {
             peer.run();
             fail("did not throw");
         } catch (PeerException e) {
             // expected
            assert(e.getCause() instanceof IOException);
         }
         
         control.verify();
         
         control.reset();
         expect(conn.readMessage()).andThrow(new ProtocolException("proto"));
         conn.shutdown();
         expectLastCall();
 
         control.replay();
 
         try {
             peer.run();
             fail("did not throw");
         } catch (PeerException e) {
             // expected
            assert(e.getCause() instanceof PeerException);
         }
         
         control.verify();
     }
 
     @Test
     public void testRun_normal() throws Exception {
         expectPeerDisconnect();
 
         control.replay();
 
         peer.run();
         control.verify();
     }
 
     @Test
     public void testRun_unconnected_block() throws Exception {
         PeerEventListener listener = control.createMock(PeerEventListener.class);
         peer.addEventListener(listener);
 
         Block b1 = TestUtils.createFakeBlock(unitTestParams, blockStore).block;
         blockChain.add(b1);
 
         Block prev = TestUtils.makeSolvedTestBlock(unitTestParams, blockStore);
         final Block block = TestUtils.makeSolvedTestBlock(unitTestParams, prev);
         
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 return block;
             }
         });
         Capture<GetBlocksMessage> message = new Capture<GetBlocksMessage>();
 
         conn.writeMessage(capture(message));
         expectLastCall();
 
         expectPeerDisconnect();
 
         control.replay();
 
         peer.run();
         control.verify();
         
         List<Sha256Hash> expectedLocator = new ArrayList<Sha256Hash>();
         expectedLocator.add(b1.getHash());
         expectedLocator.add(unitTestParams.genesisBlock.getHash());
         
         assertEquals(message.getValue().getLocator(), expectedLocator);
         assertEquals(message.getValue().getStopHash(), block.getHash());
     }
 
     @Test
     public void testRun_inv_tickle() throws Exception {
         PeerEventListener listener = control.createMock(PeerEventListener.class);
         peer.addEventListener(listener);
 
         Block b1 = TestUtils.createFakeBlock(unitTestParams, blockStore).block;
         blockChain.add(b1);
 
         Block prev = TestUtils.makeSolvedTestBlock(unitTestParams, blockStore);
         final Block block = TestUtils.makeSolvedTestBlock(unitTestParams, prev);
         
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 return block;
             }
         });
         
         conn.writeMessage(anyObject(Message.class));
         expectLastCall();
 
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 InventoryMessage inv = new InventoryMessage(unitTestParams);
                 InventoryItem item = new InventoryItem(InventoryItem.Type.Block, block.getHash());
                 inv.addItem(item);
                 return inv;
             }
         });
 
         Capture<GetBlocksMessage> message = new Capture<GetBlocksMessage>();
         conn.writeMessage(capture(message));
         expectLastCall();
         
         expectPeerDisconnect();
 
         control.replay();
 
         peer.run();
         control.verify();
         
         List<Sha256Hash> expectedLocator = new ArrayList<Sha256Hash>();
         expectedLocator.add(b1.getHash());
         expectedLocator.add(unitTestParams.genesisBlock.getHash());
         
         assertEquals(message.getValue().getLocator(), expectedLocator);
         assertEquals(message.getValue().getStopHash(), block.getHash());
     }
 
     @Test
     public void testRun_inv_block() throws Exception {
         PeerEventListener listener = control.createMock(PeerEventListener.class);
         peer.addEventListener(listener);
 
         Block b1 = TestUtils.createFakeBlock(unitTestParams, blockStore).block;
         blockChain.add(b1);
 
         Block prev = TestUtils.makeSolvedTestBlock(unitTestParams, blockStore);
         final Block b2 = TestUtils.makeSolvedTestBlock(unitTestParams, prev);
         final Block b3 = TestUtils.makeSolvedTestBlock(unitTestParams, b2);
         
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 return b2;
             }
         });
         
         conn.writeMessage(anyObject(Message.class));
         expectLastCall();
 
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 InventoryMessage inv = new InventoryMessage(unitTestParams);
                 InventoryItem item = new InventoryItem(InventoryItem.Type.Block, b3.getHash());
                 inv.addItem(item);
                 return inv;
             }
         });
 
         Capture<GetDataMessage> message = new Capture<GetDataMessage>();
         conn.writeMessage(capture(message));
         expectLastCall();
         
         expectPeerDisconnect();
 
         control.replay();
 
         peer.run();
         control.verify();
         
         List<InventoryItem> items = message.getValue().getItems();
         assertEquals(1, items.size());
         assertEquals(b3.getHash(), items.get(0).hash);
         assertEquals(InventoryItem.Type.Block, items.get(0).type);
     }
 
     @Test
     public void testStartBlockChainDownload() throws Exception {
         PeerEventListener listener = control.createMock(PeerEventListener.class);
         peer.addEventListener(listener);
 
         Block b1 = TestUtils.createFakeBlock(unitTestParams, blockStore).block;
         blockChain.add(b1);
 
         expect(conn.getVersionMessage()).andStubReturn(new VersionMessage(unitTestParams, 100));
 
         listener.onChainDownloadStarted(peer, 99);
         expectLastCall();
 
         Capture<GetBlocksMessage> message = new Capture<GetBlocksMessage>();
         conn.writeMessage(capture(message));
         expectLastCall();
         
         control.replay();
 
         peer.startBlockChainDownload();
         control.verify();
         
         List<Sha256Hash> expectedLocator = new ArrayList<Sha256Hash>();
         expectedLocator.add(b1.getHash());
         expectedLocator.add(unitTestParams.genesisBlock.getHash());
         
         assertEquals(message.getValue().getLocator(), expectedLocator);
         assertEquals(message.getValue().getStopHash(), Sha256Hash.ZERO_HASH);
     }
 
     @Test
     public void testGetBlock() throws Exception {
         PeerEventListener listener = control.createMock(PeerEventListener.class);
         peer.addEventListener(listener);
 
         Block b1 = TestUtils.createFakeBlock(unitTestParams, blockStore).block;
         blockChain.add(b1);
 
         Block prev = TestUtils.makeSolvedTestBlock(unitTestParams, blockStore);
         final Block b2 = TestUtils.makeSolvedTestBlock(unitTestParams, prev);
 
         expect(conn.getVersionMessage()).andStubReturn(new VersionMessage(unitTestParams, 100));
 
         Capture<GetDataMessage> message = new Capture<GetDataMessage>();
         conn.writeMessage(capture(message));
         expectLastCall();
 
         expect(conn.readMessage()).andReturn(b2);
         
         expectPeerDisconnect();
 
         control.replay();
 
         Future<Block> resultFuture = peer.getBlock(b2.getHash());
         peer.run();
         
         assertEquals(b2.getHash(), resultFuture.get().getHash());
         
         control.verify();
         
         List<Sha256Hash> expectedLocator = new ArrayList<Sha256Hash>();
         expectedLocator.add(b1.getHash());
         expectedLocator.add(unitTestParams.genesisBlock.getHash());
         
         List<InventoryItem> items = message.getValue().getItems();
         assertEquals(1, items.size());
         assertEquals(b2.getHash(), items.get(0).hash);
         assertEquals(InventoryItem.Type.Block, items.get(0).type);
     }
 
     @Test
     public void testRun_new_block() throws Exception {
         PeerEventListener listener = control.createMock(PeerEventListener.class);
         peer.addEventListener(listener);
 
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 return TestUtils.makeSolvedTestBlock(unitTestParams, blockStore); 
             }
         });
         expect(conn.getVersionMessage()).andReturn(new VersionMessage(unitTestParams, 100));
         listener.onBlocksDownloaded(eq(peer), anyObject(Block.class), eq(99));
         expectLastCall();
         expectPeerDisconnect();
 
         control.replay();
 
         peer.run();
         control.verify();
     }
 
     private void expectPeerDisconnect() throws IOException, ProtocolException {
         expect(conn.readMessage()).andAnswer(new IAnswer<Message>() {
             public Message answer() throws Throwable {
                 peer.disconnect();
                 throw new IOException("done");
             }
         });
         conn.shutdown();
         expectLastCall().times(2);
     }
 }
