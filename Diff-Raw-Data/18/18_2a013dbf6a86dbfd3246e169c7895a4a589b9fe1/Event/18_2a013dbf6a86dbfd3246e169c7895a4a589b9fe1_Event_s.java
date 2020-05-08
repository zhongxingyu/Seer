 package cha.event;
 
 public enum Event {
 	MakeBet,	//Is done when player presses a tile t make a bet.
<<<<<<< HEAD
	ShowBet,
 	StartMission, //Done Johan
 	TimeTick,
	MissionDone,
 	TimeOver,
 	MissionSuccess, //Done Johan
 	MissionFail, //Done Johan
	NextPlayer,// Is called when mission(Y/N button) or challenge is over. Only repaints the GUI.
=======
	ShowBet,	//Done. Malla.
	StartMission,
	TimeTick,
	MissionDone,
	TimeOver,
	MissionSuccess,
	MissionFail,
 	NextPlayer,	//Done. Malla.
>>>>>>> 0d724f666831c91631c48d4e54ea098ee79e8c2f
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
