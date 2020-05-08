 package com.madavan.bridge.support;
 
 public enum Command {
	BID, // Tells the user/server the most recent (and highest) bid (PLAYER, BID)
	DEAL, // Tells the user that a card has been dealt to them (CARD)
	PLAY_TURN, // Tells a user that it is his/her turn to play ("")
	BID_TURN, // Tells a user that it is his/her turn to bid ("")
	CARD; // Tells a user/server a card has been played (PLAYER, CARD)
 }
