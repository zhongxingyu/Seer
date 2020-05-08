 package com.chess.genesis;
 
 import android.content.Context;
 import android.widget.ImageView;
 
public class PlaceButton extends ImageView
 {
 	private static final int[][] pieceImages = {
 		{R.drawable.black_king_1,	R.drawable.black_king_0},
 		{R.drawable.black_queen_1,	R.drawable.black_queen_0},
 		{R.drawable.black_rook_2,	R.drawable.black_rook_1,	R.drawable.black_rook_0},
 		{R.drawable.black_bishop_2,	R.drawable.black_bishop_1,	R.drawable.black_bishop_0},
 		{R.drawable.black_knight_2,	R.drawable.black_knight_1,	R.drawable.black_knight_0},
 		{R.drawable.black_pawn_8,	R.drawable.black_pawn_7,	R.drawable.black_pawn_6,
 		R.drawable.black_pawn_5,	R.drawable.black_pawn_4,	R.drawable.black_pawn_3,
 		R.drawable.black_pawn_2,	R.drawable.black_pawn_1,	R.drawable.black_pawn_0},
 		{0},
 		{R.drawable.white_pawn_8,	R.drawable.white_pawn_7,	R.drawable.white_pawn_6,
 		R.drawable.white_pawn_5,	R.drawable.white_pawn_4,	R.drawable.white_pawn_3,
 		R.drawable.white_pawn_2,	R.drawable.white_pawn_1,	R.drawable.white_pawn_0},
 		{R.drawable.white_knight_2,	R.drawable.white_knight_1,	R.drawable.white_knight_0},
 		{R.drawable.white_bishop_2,	R.drawable.white_bishop_1,	R.drawable.white_bishop_0},
 		{R.drawable.white_rook_2,	R.drawable.white_rook_1,	R.drawable.white_rook_0},
 		{R.drawable.white_queen_1,	R.drawable.white_queen_0},
 		{R.drawable.white_king_1,	R.drawable.white_king_0} };
 
 	private static final int[][] pieceImagesH = {
 		{R.drawable.black_king_1h,	R.drawable.black_king_0h},
 		{R.drawable.black_queen_1h,	R.drawable.black_queen_0h},
 		{R.drawable.black_rook_2h,	R.drawable.black_rook_1h,	R.drawable.black_rook_0h},
 		{R.drawable.black_bishop_2h,	R.drawable.black_bishop_1h,	R.drawable.black_bishop_0h},
 		{R.drawable.black_knight_2h,	R.drawable.black_knight_1h,	R.drawable.black_knight_0h},
 		{R.drawable.black_pawn_8h,	R.drawable.black_pawn_7h,	R.drawable.black_pawn_6h,
 		R.drawable.black_pawn_5h,	R.drawable.black_pawn_4h,	R.drawable.black_pawn_3h,
 		R.drawable.black_pawn_2h,	R.drawable.black_pawn_1h,	R.drawable.black_pawn_0h},
 		{0},
 		{R.drawable.white_pawn_8h,	R.drawable.white_pawn_7h,	R.drawable.white_pawn_6h,
 		R.drawable.white_pawn_5h,	R.drawable.white_pawn_4h,	R.drawable.white_pawn_3h,
 		R.drawable.white_pawn_2h,	R.drawable.white_pawn_1h,	R.drawable.white_pawn_0h},
 		{R.drawable.white_knight_2h,	R.drawable.white_knight_1h,	R.drawable.white_knight_0h},
 		{R.drawable.white_bishop_2h,	R.drawable.white_bishop_1h,	R.drawable.white_bishop_0h},
 		{R.drawable.white_rook_2h,	R.drawable.white_rook_1h,	R.drawable.white_rook_0h},
 		{R.drawable.white_queen_1h,	R.drawable.white_queen_0h},
 		{R.drawable.white_king_1h,	R.drawable.white_king_0h} };
 
 	private static final int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};
 
 	private int type;
 	private int count;
 	private int maxCount;
 
 	private boolean isHighlighted = false;
 
 	public PlaceButton(Context context, int Type)
 	{
 		super(context);
 
 		type = Type;
 		maxCount = typeCounts[Math.abs(type)];
 		count = maxCount;
 		
 		setPieceImage();
 		setId(type + 100);
		setAdjustViewBounds(true);
 		setLayoutParams(PlaceLayout.linearparams);
 	}
 
 	private void setPieceImage()
 	{
 		int image = (isHighlighted)?
 			pieceImagesH[type + 6][maxCount - count] :
 			pieceImages[type + 6][maxCount - count];
 
 		setImageResource(image);
 	}
 	
 	public void reset()
 	{
 		isHighlighted = false;
 		count = maxCount;
 
 		setPieceImage();
 	}
 
 	public int getPiece()
 	{
 		return type;
 	}
 
 	public int getCount()
 	{
 		return count;
 	}
 
 	public void setCount(int Count)
 	{
 		count = Count;
 
 		setPieceImage();
 	}
 
 	public void minusPiece()
 	{
 		count--;
 
 		setPieceImage();
 	}
 	
 	public void plusPiece()
 	{
 		count++;
 
 		setPieceImage();
 	}
 
 	public void setHighlight(boolean mode)
 	{
 		isHighlighted = mode;
 
 		setPieceImage();
 	}
 }
