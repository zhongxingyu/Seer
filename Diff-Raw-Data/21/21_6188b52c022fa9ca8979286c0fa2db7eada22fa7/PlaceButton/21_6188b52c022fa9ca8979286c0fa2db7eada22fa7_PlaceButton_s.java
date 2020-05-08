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
 
 public class PlaceButton extends PieceImg
 {
 	protected final static int outerLight = MColors.GREY_LIGHT;
 	protected final static int outerDark = MColors.BLUE_NAVY_DARK;
 	protected final static int innerDark = MColors.BLUE_NAVY;
 	protected final static int innerLight = Color.WHITE;
 	protected final static int innerSelect = MColors.GREEN_TEAL;
 
 	protected final static int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};
 
 	protected final Paint paint = new Paint();
 	protected final RectF inSquare = new RectF();
 
 	protected boolean isHighlighted = false;
 
 	public PlaceButton(final Context context, final PieceImgCache _cache, final int Type)
 	{
 		super(context, _cache);
 		type = Type;
 		count = typeCounts[Math.abs(type)];
 		setId(type + 1000);
 	}
 
 	@Override
 	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
 	{
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		final int size = getMeasuredHeight();
 		inSquare.set((float)(size * 0.09), (float)(size * 0.09), (float)(size * 0.91), (float)(size * 0.91));
 	}
 
 	@Override
 	protected void onDraw(final Canvas canvas)
 	{
 		drawBackground(canvas);
 		super.onDraw(canvas);
 	}
 
 	protected void drawBackground(final Canvas canvas)
 	{
 		// Draw outer square
 		if (type % 2 == 0)
			canvas.drawColor(outerLight);
 		else
			canvas.drawColor(outerDark);
 
 		// Draw inner square
 		final int innerColor =
 			isHighlighted?
 				innerSelect :
 			((type % 2 == 0)?
 				innerLight :
 				innerDark);
 		paint.setColor(innerColor);
 		canvas.drawRect(inSquare, paint);
 	}
 
 	public void reset()
 	{
 		isHighlighted = false;
 		count = typeCounts[Math.abs(type)];
 		invalidate();
 	}
 
 	public void setHighlight(final boolean mode)
 	{
 		isHighlighted = mode;
 		invalidate();
 	}
 }
