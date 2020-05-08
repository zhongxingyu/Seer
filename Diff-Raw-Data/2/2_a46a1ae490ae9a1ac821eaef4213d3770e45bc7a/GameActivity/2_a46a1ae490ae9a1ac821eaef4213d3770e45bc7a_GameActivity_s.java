 package com.hmml.knightwatch_android;
 
 import java.util.ArrayList;
 
 import com.example.knightwatch_android.R;
 import com.hmml.knightwatch_android.BoardCell.ECellPlayer;
 import com.hmml.knightwatch_android.BoardCell.ECellType;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.GridLayout;
 import android.widget.GridView;
 
 
 public class GameActivity extends Activity {
 	//private BoardCell[][] boardArray; 
 	private ArrayList<BoardCell> boardArrayList = new ArrayList<BoardCell>();
 	private GridView gridView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		gridView = (GridView) findViewById(R.id.boardGridView);
 		initBoard();
 		gridView.setAdapter(new GridViewBoardAdapter(this,0,boardArrayList ));
 	}
 	
 	private void initBoard()
 	{
 		ECellPlayer eCellPlayer = ECellPlayer.White;
		for (int i = 0; i < 32; i++)
 		{
 			if ( i == 16)
 				eCellPlayer = ECellPlayer.Black;
 			if ( i == 1 || i == 4)
 				boardArrayList.add(i, new BoardCell( eCellPlayer, ECellType.Knight ));
 			else if (i > 5 && i < 8)
 				boardArrayList.add(i, new BoardCell( eCellPlayer , ECellType.Pawn ));
 			else
 				boardArrayList.add(i, new BoardCell( ECellPlayer.None , ECellType.None ));		
 		}
 		
 		/*
 		for (int k = 0; k < 2; k++)
 		{
 			for(int i = 0; i < 5; i++)
 			{
 				if ( k == 0) // If first row check if cell will be none or knight 
 				{
 					ECellType currentType;
 					if (i == 1 || i == 4) // Knight or none in column
 						currentType = ECellType.Knight;
 					else
 						currentType = ECellType.None;
 					boardArray[k][i] = new BoardCell( ECellPlayer.White , currentType );
 					boardArray[k+5][i] = new BoardCell( ECellPlayer.Black , currentType );
 				}
 				else if (k == 1) // if second row set cell to pawn
 				{
 					boardArray[k][i] = new BoardCell( ECellPlayer.White , ECellType.Pawn );
 					boardArray[k+2][i] = new BoardCell( ECellPlayer.Black , ECellType.Pawn );
 				}
 				else // set the cell to empty
 				{
 					boardArray[k][i] = new BoardCell( ECellPlayer.White , ECellType.None );
 					boardArray[k+1][i] = new BoardCell( ECellPlayer.Black , ECellType.None );
 				}
 			}
 			
 		} */
 	}
 	private void checkMove ()
 	{
 		
 		
 	}
 	
 }
