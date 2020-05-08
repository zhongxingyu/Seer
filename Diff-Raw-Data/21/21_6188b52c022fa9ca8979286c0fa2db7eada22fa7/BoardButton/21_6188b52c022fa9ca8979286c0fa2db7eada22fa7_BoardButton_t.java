 /*	GenesisChess, an Android chess application
 	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 	http://apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 */
 
 package com.chess.genesis.view;
 
 import android.content.*;
 import android.graphics.*;
 import com.chess.genesis.data.*;
 import com.chess.genesis.engine.*;
 
 public class BoardButton extends PlaceButton
 {
 	protected final static int innerCheck = MColors.RED_LIGHT;
 	protected final static int innerLast = MColors.PURPLE_LIGHT;
 	protected final static int WHITE = 0;
 	protected final static int BLACK = 1;
 
 	private final int squareColor;
 	private final int squareIndex;
 
 	private boolean isCheck = false;
 	private boolean isLast = false;
 
 	public BoardButton(final Context context, final PieceImgCache _cache, final int index)
 	{
 		super(context, _cache, Piece.NONE);
 
 		squareIndex = index;
 		squareColor = ((index / 16) % 2 == 1)?
 				((index % 2 == 1)? BLACK : WHITE) :
 				((index % 2 == 1)? WHITE : BLACK);
 		setId(squareIndex);
 	}
 
 	@Override
 	protected void onDraw(final Canvas canvas)
 	{
 		drawBackground(canvas);
 		drawPiece(canvas);
 	}
 
 	@Override
 	protected void drawBackground(final Canvas canvas)
 	{
 		// Draw outer square
 		if (squareColor == WHITE)
			paint.setColor(outerLight);
 		else
			paint.setColor(outerDark);
		canvas.drawRect(outSquare, paint);
 
 		// Draw inner square
 		final int innerColor =
 			isHighlighted?
 				innerSelect :
 			(isLast?
 				innerLast :
 			(isCheck?
 				innerCheck :
 			((squareColor == WHITE)?
 				innerLight :
 				innerDark)));
 		paint.setColor(innerColor);
 		canvas.drawRect(inSquare, paint);
 	}
 
 	@Override
 	public void reset()
 	{
 		isHighlighted = false;
 		isCheck = false;
 		setPiece(Piece.EMPTY);
 	}
 
 	public int getIndex()
 	{
 		return squareIndex;
 	}
 
 	public void setCheck(final boolean mode)
 	{
 		isCheck = mode;
 		invalidate();
 	}
 
 	public void setLast(final boolean mode)
 	{
 		isLast = mode;
 		invalidate();
 	}
 }
