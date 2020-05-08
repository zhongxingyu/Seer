 package com.petrifiednightmares.singularityChess.io;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.petrifiednightmares.singularityChess.logic.Game;
 import com.petrifiednightmares.singularityChess.logic.Square;
 import com.petrifiednightmares.singularityChess.pieces.AbstractPiece;
 import com.petrifiednightmares.singularityChess.pieces.Bishop;
 import com.petrifiednightmares.singularityChess.pieces.King;
 import com.petrifiednightmares.singularityChess.pieces.Knight;
 import com.petrifiednightmares.singularityChess.pieces.Pawn;
 import com.petrifiednightmares.singularityChess.pieces.Queen;
 import com.petrifiednightmares.singularityChess.pieces.Rook;
 
 public class PieceSaveable implements Saveable
 {
 
 	boolean isWhite, isAlive;
 
 	AbstractPiece.PieceType pieceType;
 
 	SquareSaveable location;
 
 	// for resuming
 	AbstractPiece p;
 	Game _game;
 
 	public AbstractPiece getPiece()
 	{
 		return p;
 	}
 
 	// Empty constructor for reading
 	public PieceSaveable(Game g)
 	{
 		this._game = g;
 	}
 
 	// full constructor for writing
 	public PieceSaveable(AbstractPiece p)
 	{
 		isWhite = p.isWhite();
 		isAlive = p.isAlive();
 
		if (p.getLocation() != null)
			this.location = new SquareSaveable(p.getLocation());

 		this.pieceType = p.getType();
 	}
 
 	// ****************************Saveable ****************************/
 
 	// NOTE: This method has a side effect. it will modify _game's squares to
 	// add the pieces.
 	public void deserialize(InputStream in) throws IOException
 	{
 		DataInputStream dataIn = new DataInputStream(in);
 
 		int type = dataIn.readInt();
 		boolean isWhite = dataIn.readBoolean();
 		boolean isAlive = dataIn.readBoolean();
 		boolean hasLocation = dataIn.readBoolean();
 
 		SquareSaveable locationSaveable;
 		Square location;
 		if (hasLocation)
 		{
 			locationSaveable = new SquareSaveable();
 			locationSaveable.deserialize(in);
 			location = _game.getBoard().getSquares().get(locationSaveable.getIndex());
 		} else
 		{
 			location = null;
 		}
 
 		switch (AbstractPiece.PieceType.values()[type])
 		{
 		case King:
 			p = new King(_game, location, isWhite);
 			p.setIsAlive(isAlive);
 			break;
 
 		case Queen:
 			p = new Queen(_game, location, isWhite);
 			p.setIsAlive(isAlive);
 			break;
 		case Bishop:
 			p = new Bishop(_game, location, isWhite);
 			p.setIsAlive(isAlive);
 			break;
 		case Knight:
 			p = new Knight(_game, location, isWhite);
 			p.setIsAlive(isAlive);
 			break;
 		case Pawn:
 			p = new Pawn(_game, location, isWhite);
 			p.setIsAlive(isAlive);
 			break;
 		case Rook:
 			p = new Rook(_game, location, isWhite);
 			p.setIsAlive(isAlive);
 			break;
 		default:
 			break;
 		}
 		if (location != null)
 		{
 			location.addPiece(p);
 		}
 
 	}
 
 	public void serialize(OutputStream out) throws IOException
 	{
 		DataOutputStream dataOut = new DataOutputStream(out);
 
 		dataOut.writeInt((int) pieceType.getValue());
 		dataOut.writeBoolean(isWhite);
 		dataOut.writeBoolean(isAlive);
 
 		if (location != null)
 		{
 			dataOut.writeBoolean(true);
 			location.serialize(dataOut);
 		} else
 		{
 			dataOut.writeBoolean(false);
 		}
 	}
 }
