 package edu.berkeley.cs.cs162.Writable;
 
 public class MessageProtocol {
 
     // StoneColor definitions
     public static final byte STONE_BLACK = (byte) 0;
     public static final byte STONE_WHITE = (byte) 1;
     public static final byte STONE_NONE  = (byte) 2;
 
     // PlayerType definitions
     public static final byte TYPE_HUMAN = (byte) 0;
     public static final byte TYPE_MACHINE = (byte) 1;
     public static final byte TYPE_OBSERVER = (byte) 2;
 
     // MoveType definitions
     public static final byte MOVE_STONE = (byte) 0;
     public static final byte MOVE_PASS = (byte) 1;
     public static final byte MOVE_FORFEIT = (byte) 2;
 
     // Client to server message opcodes
     public static final byte OP_TYPE_CONNECT     = (byte) 10;
     public static final byte OP_TYPE_DISCONNECT  = (byte) 11;
     public static final byte OP_TYPE_LISTGAMES   = (byte) 12;
     public static final byte OP_TYPE_JOIN        = (byte) 13;
     public static final byte OP_TYPE_LEAVE       = (byte) 14;
     public static final byte OP_TYPE_WAITFORGAME = (byte) 15;
     public static final byte OP_TYPE_REGISTER    = (byte) 16;
     public static final byte OP_TYPE_CHANGEPW    = (byte) 17;
 
     // Server to client message opcodes
     public static final byte OP_TYPE_GAMESTART     = (byte) 20;
     public static final byte OP_TYPE_GAMEOVER      = (byte) 21;
     public static final byte OP_TYPE_MAKEMOVE      = (byte) 22;
     public static final byte OP_TYPE_GETMOVE       = (byte) 23;
 
     // Status return codes
     public static final byte OP_STATUS_OK          = (byte) 10;
     public static final byte OP_STATUS_RESUME      = (byte) 11; 
 
     // Error return codes
     public static final byte OP_ERROR_REJECTED     = (byte) 20;
     public static final byte OP_ERROR_INVALID_GAME = (byte) 21;
     public static final byte OP_ERROR_BAD_AUTH     = (byte) 22;
     public static final byte OP_ERROR_UNCONNECTED  = (byte) 23;
 
     // gameOver status codes
     public static final byte GAME_OK = (byte) 10;
 
     // gameOver error codes
     public static final byte PLAYER_INVALID_MOVE  = (byte) 20;
     public static final byte PLAYER_KO_RULE       = (byte) 22;
     public static final byte PLAYER_FORFEIT       = (byte) 24;
 }
