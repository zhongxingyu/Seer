 package com.refnil.uqcard.data;
 
 import com.refnil.uqcard.event.BeginTurnEvent;
 import com.refnil.uqcard.event.Event;
 import com.refnil.uqcard.event.Event_Type;
 
 import android.util.Log;
 
 public class ServerBoard extends Board {
 	
 	final private static String TAG = "ServerBoard";
 	
 	public ServerBoard()
 	{
		
 	}
 	
 	@Override
 	public void receiveEvent(Event event) {
 		Log.i(TAG,"TPTP "+ event.type.toString());
 		if (event.type == Event_Type.BEGIN_GAME) {
 			Log.i(TAG, "Game begins");
 			tell(event);
 		}
 
 		if (event.type == Event_Type.BEGIN_TURN) {
 			
 		}
 
 		if (event.type == Event_Type.END_TURN) {
 			Log.i(TAG, "Turn " + this.getTour() + " ends");
 			tell(new BeginTurnEvent());
 		}
 
 		if (event.type == Event_Type.END_GAME) {
 			Log.i(TAG, "Game ends");
 			tell(event);
 		}
 
 		/*if (event instanceof AttackEvent) {
 			AttackEvent ae = (AttackEvent) event;
 			Card opp = ae.opponent.getCard();
 			Card pl = ae.player.getCard();
 			int index = opponentBoardCards.indexOf(opp);
 			int index1 = playerBoardCards.indexOf(pl);
 			((CreatureCard) opponentBoardCards.get(index))
 					.setHp(((CreatureCard) opponentBoardCards.get(index))
 							.getHp()
 							- ((CreatureCard) playerBoardCards.get(index1))
 									.getAtk());
 			ae.opponent.setCard((Card)opponentBoardCards.get(index));
 			tell(ae);
 		}*/
 	}
 
 }
