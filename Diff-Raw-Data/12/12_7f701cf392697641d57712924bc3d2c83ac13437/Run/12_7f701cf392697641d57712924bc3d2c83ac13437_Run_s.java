 /*
  * Copyright (c) 2006 Andy Wood
  */
 package uk.co.mindtrains;
 
 public class Run extends Thread
 {
 	private Layout layout;
 	
 	public Run( Layout layout )
 	{
 		this.layout = layout;
 		start();
 	}
 
 	public void run()
 	{
 		for ( int i = 0; i < layout.getComponentCount(); i++ )
 			if ( layout.getComponent( i ) instanceof Piece.Label )
 				( (Piece.Label)layout.getComponent( i ) ).getPiece().reset();
 
 		Train train = new Train();
 		layout.add( train, 0 );
 		
 		Piece.Label now = layout.getMain();
 		Connector entry = null;
 		do
 		{
 			Connector exit = now.getPiece().travel( train, entry );
 			for ( int i = 0; i < layout.getComponentCount(); i++ )
 			{
 				if ( layout.getComponent( i ) instanceof Piece.Label )
 				{
 					Piece.Label piece = (Piece.Label)layout.getComponent( i );
 					if ( piece != now  )
 					{
 						entry = piece.getPiece().connected( exit );
 						if ( entry != null )
 						{
 							now = piece;
 							break;
 						}
 					}
 				}
 			}
 			try { Thread.sleep( 250 ); } catch ( InterruptedException e ) {}
 		}
 		while ( entry != null );
 
 		layout.remove( train );
 		layout.repaint();
 	}
 }
