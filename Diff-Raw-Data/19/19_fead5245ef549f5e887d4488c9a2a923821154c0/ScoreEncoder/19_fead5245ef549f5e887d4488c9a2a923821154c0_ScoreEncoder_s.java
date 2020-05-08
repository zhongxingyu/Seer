 package eval;
 
 /** encoder for score, margin, and any flags*/
 public final class ScoreEncoder {
 	public final static int scoreBits = 19;
 	public final static int marginBits = 10;
 	public final static int flagBits = 3;
 	
 	private final static int scoreMask;
 	private final static int marginMask;
 	private final static int flagMask;
 	private final static int scoreMaskOffset;
 	private final static int marginMaskOffset;
 	private final static int flagMaskOffset;
 	
 	static{
 		scoreMaskOffset = 0;
 		marginMaskOffset = scoreBits;
 		flagMaskOffset = scoreBits + marginBits;
 		
 		{
 			int temp = 0;
			for(int a = 0; a < scoreBits; a++) temp |= 1 << (scoreMaskOffset + a);
 			scoreMask = temp;
 		}
 		{
 			int temp = 0;
 			for(int a = 0; a < marginBits; a++) temp |= 1 << (marginMaskOffset + a);
 			marginMask = temp;
 		}
 		{
 			int temp = 0;
 			for(int a = 0; a < flagBits; a++) temp |= 1 << (flagMaskOffset + a);
 			flagMask = temp;
 		}
 	}
 	
 	public static int getScore(final int scoreEncoding){
		return scoreEncoding & scoreMask;
 	}
 	
 	public static int getMargin(final int scoreEncoding){
 		return (scoreEncoding & marginMask) >>> marginMaskOffset;
 	}
 	
 	public static int getFlags(final int scoreEncoding){
 		return (scoreEncoding & flagMask) >>> flagMaskOffset;
 	}
 	
 	public static int encode(final int score, final int margin, final int flags){
 		assert score < 1<<scoreBits;
 		assert margin < 1<<marginBits;
 		assert flags < 1<<flagBits;
 		
		return score | (margin << marginMaskOffset) | (flags << flagMaskOffset);
 	}
 }
