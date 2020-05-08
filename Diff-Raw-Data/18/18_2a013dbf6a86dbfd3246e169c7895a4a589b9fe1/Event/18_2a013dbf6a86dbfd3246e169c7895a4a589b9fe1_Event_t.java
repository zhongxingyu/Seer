 package cha.event;
 
 public enum Event {
 	MakeBet,	//Is done when player presses a tile t make a bet.
	ShowBet,	//Done. Malla.
 	StartMission, //Done Johan
 	TimeTick,
	MissionDone, 
 	TimeOver,
 	MissionSuccess, //Done Johan
 	MissionFail, //Done Johan
 	NextPlayer,	//Done. Malla.
 	OldPosition,//DONE. Only removes piece when moved. Called from 2 methods in Piece.
 	NewPosition,//DONE. Only adds piece when moved. And repaints. Called from 2 methods in Piece.
 	CreateBoard,
 	NewGame,
 	GameOver,
 	ShowGameRules,
 	ShowStartPanel,
 	NextCard, 
 	ContinueGame, 
 	Challenge 	//Done. Malla.
 }
