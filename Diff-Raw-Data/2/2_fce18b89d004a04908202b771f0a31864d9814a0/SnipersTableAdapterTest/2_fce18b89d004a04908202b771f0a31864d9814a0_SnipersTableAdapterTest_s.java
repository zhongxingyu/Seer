 package com.yskang.auctionsniper.unittest;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 import com.yskang.auctionsniper.Column;
 import com.yskang.auctionsniper.SniperSnapshot;
 import com.yskang.auctionsniper.SniperState;
 import com.yskang.auctionsniper.SnipersTableAdapter;
 
 public class SnipersTableAdapterTest extends AndroidTestCase {
 	private final Mockery context = new Mockery();
 	private AuctionSnipersObserver observer = context
 			.mock(AuctionSnipersObserver.class);
 	private AuctionSnipersDataSetObserver auctionSnipersObserver = new AuctionSnipersDataSetObserver(
 			observer);
 	private SnipersTableAdapter snipersTableAdapter;
 
 	public void testSetsSniperValuesInColumns() {
 
 		context.checking(new Expectations() {
 			{
 				atLeast(1).of(observer).dataChanged();
 			}
 		});
 
 		snipersTableAdapter = new SnipersTableAdapter(getContext());
 		snipersTableAdapter.registerDataSetObserver(auctionSnipersObserver);
 
 		SniperSnapshot joining = SniperSnapshot.joining("item id");
 		SniperSnapshot bidding = joining.bidding(555, 666);
 
 		snipersTableAdapter.addSniper(joining);
 		snipersTableAdapter.sniperStateChanged(bidding);
 
 		assertEquals("item id", snipersTableAdapter.getItem(0).getItemId());
 		assertEquals(555, snipersTableAdapter.getItem(0).getLastPrice());
 		assertEquals(666, snipersTableAdapter.getItem(0).getLastBid());
 		assertEquals(SniperState.BIDDING, snipersTableAdapter.getItem(0)
 				.getStatus());
 
 		context.assertIsSatisfied();
 	}
 
 	public void testNotifiesListenersWhenAddingASniper() {
 		SniperSnapshot joining = SniperSnapshot.joining("item123");
 		context.checking(new Expectations() {
 			{
 				one(observer).dataChanged();
 			}
 		});
 
 		snipersTableAdapter = new SnipersTableAdapter(getContext());
 		snipersTableAdapter.registerDataSetObserver(auctionSnipersObserver);
 
 		assertEquals(0, snipersTableAdapter.getRowCount());
 		snipersTableAdapter.addSniper(joining);
 		assertEquals(1, snipersTableAdapter.getRowCount());
 		assertEquals(snipersTableAdapter.getItem(0), joining);
 
 		context.assertIsSatisfied();
 	}
 
 	public void testHoldsSnipersInAdditionOrder() {
 		context.checking(new Expectations() {
 			{
 				ignoring(observer);
 			}
 		});
 		
 		snipersTableAdapter = new SnipersTableAdapter(getContext());
 		
 		snipersTableAdapter.addSniper(SniperSnapshot.joining("item 0"));
 		snipersTableAdapter.addSniper(SniperSnapshot.joining("item 1"));
 		
 		assertEquals("item 0", snipersTableAdapter.getItem(0).getItemId());
 		assertEquals("item 1", snipersTableAdapter.getItem(1).getItemId());
 		
 		context.assertIsSatisfied();
 	}
 	
 	public void testUpdatesCorrectRowForSniper(){
 		context.checking(new Expectations() {
 			{
 				ignoring(observer);
 			}
 		});
 		
 		snipersTableAdapter = new SnipersTableAdapter(getContext());
 		
 		SniperSnapshot item0 = SniperSnapshot.joining("item 0");
 		SniperSnapshot item1 = SniperSnapshot.joining("item 1");
 		SniperSnapshot item2 = SniperSnapshot.joining("item 2");
 		
 		snipersTableAdapter.addSniper(item0);
 		snipersTableAdapter.addSniper(item1);
 		snipersTableAdapter.addSniper(item2);
 		
 		SniperSnapshot item1Modified = item1.bidding(41, 74);
 		snipersTableAdapter.sniperStateChanged(item1Modified);
 
 		assertEquals("item 0", snipersTableAdapter.getItem(0).getItemId());
 		assertEquals(0, snipersTableAdapter.getItem(0).getLastPrice());
 		assertEquals(0, snipersTableAdapter.getItem(0).getLastBid());
 		assertEquals(SniperState.JOINING, snipersTableAdapter.getItem(0).getStatus());
 		
 		assertEquals("item 1", snipersTableAdapter.getItem(1).getItemId());
 		assertEquals(41, snipersTableAdapter.getItem(1).getLastPrice());
 		assertEquals(74, snipersTableAdapter.getItem(1).getLastBid());
 		assertEquals(SniperState.BIDDING, snipersTableAdapter.getItem(1).getStatus());
 
 		assertEquals("item 2", snipersTableAdapter.getItem(2).getItemId());
 		assertEquals(0, snipersTableAdapter.getItem(2).getLastPrice());
 		assertEquals(0, snipersTableAdapter.getItem(2).getLastBid());
 		assertEquals(SniperState.JOINING, snipersTableAdapter.getItem(2).getStatus());
 		
 		context.assertIsSatisfied();
 	}
 	
	void testThrowsDefectIfNoExistingSniperForAnUpdate(){
 		context.checking(new Expectations() {
 			{
 				ignoring(observer);
 			}
 		});
 		
 		snipersTableAdapter = new SnipersTableAdapter(getContext());
 		
 		SniperSnapshot item0 = SniperSnapshot.joining("item 0");
 		SniperSnapshot item1 = SniperSnapshot.joining("item 1");
 		SniperSnapshot item2 = SniperSnapshot.joining("item 2");
 		SniperSnapshot item4 = SniperSnapshot.joining("item 4");
 		
 		snipersTableAdapter.addSniper(item0);
 		snipersTableAdapter.addSniper(item1);
 		snipersTableAdapter.addSniper(item2);
 		
 		SniperSnapshot item4Modified = item4.bidding(41, 74);
 		
 		try{
 			snipersTableAdapter.sniperStateChanged(item4Modified);
 			fail("Cannot find match for ");
 		}
 		catch(Exception e){
 
 		}
 		
 		context.assertIsSatisfied();
 	}
 }
