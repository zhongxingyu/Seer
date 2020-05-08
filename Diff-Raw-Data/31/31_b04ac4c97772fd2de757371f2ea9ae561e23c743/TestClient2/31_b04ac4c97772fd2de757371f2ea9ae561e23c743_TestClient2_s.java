 package test;
 
 import org.ojim.iface.IClient;
 import org.ojim.server.OjimServer;
 
 public class TestClient2 implements IClient {
 
 	public int id;
 	@SuppressWarnings("unused")
 	private OjimServer server;
 	public String lastMessage;
 	public boolean lastMessagePrivate;
 	
 	public TestClient2(OjimServer server) {
 		this.server = server;
 		id = server.addPlayer(this);
 	}
 	
 	public TestClient2() {
 	}
 	
 	@Override
 	public String getName() {
 		return "aName";
 	}
 
 	@Override
 	public String getLanguage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void informStartGame(int[] ids) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informTurn(int player) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informDiceValues(int[] diceValues) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informCashChange(int player, int cashChange) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informBuy(int player, int position) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informConstruct(int street) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informDestruct(int street) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informMortgageToogle(int street) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informCardPull(String text, boolean communityCard) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informBankruptcy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informMessage(String text, int sender, boolean privateMessage) {
 		this.lastMessage = text;
 		this.lastMessagePrivate = privateMessage;
 
 	}
 
 	@Override
	public void informTrade(int actingPlayer, int partnerPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
 	public void informAuction(int auctionState) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informMove(int playerId, int position) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informNewPlayer(int playerId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informPlayerLeft(int playerId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informBuyEvent(int player, int position) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void informCanEndTurn(int player) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void informFreeParkingChange(int freeParkingField, int newPot) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void informTrade() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void informGameOver(int playeID) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
