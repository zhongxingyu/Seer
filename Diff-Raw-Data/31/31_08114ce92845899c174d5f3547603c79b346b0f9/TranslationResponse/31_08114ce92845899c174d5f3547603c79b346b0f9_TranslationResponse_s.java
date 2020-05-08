 package tetrix.entities.rotations;
 
 import tetrix.entities.shapes.Shape;
 
 
 /**
  * Representation of a set of {@link Block} translations that can be used in response to
  * a {@link Shape} rotation request.  A <code>TranslationResponse</code> is composed of
  * four pairs of column-row offsets, one pair for each of the four {@link Block}s that
  * make up a {@link Shape}.  Each column-row offset pair describes how to translate one
  * of the blocks in order to simulate a rotation.  Translation offsets use
  * the following convention:<br>
  * Assuming k is an integer > 0, then
  * <li>colOffset = k, represents a translation to the Right by k columns.</li>
  * <li>colOffset = -k, represents a translation to the Left by k columns.</li>
  * <li>rowOffset = k,  represents a translation Up by k rows.</li>
  * <li>rowOffset = -k,  represents a translation Down by k rows.</li>
  * <br>
  * 
  * @author Dustin Biser
  *
  */
 public class TranslationResponse {
 	private int aColOffset, aRowOffset;
 	private int bColOffset, bRowOffset;
 	private int cColOffset, cRowOffset;
 	private int dColOffset, dRowOffset;
 	
 	
 	/**
 	 * Constructs a TranslationReponse with no translations.  Translations can be
 	 * added to this object by calling any of the following methods in any order:
 	 * <li>translateA(int aColOffset, int aRowOffset)</li>
 	 * <li>translateB(int bColOffset, int bRowOffset)</li>
 	 * <li>translateC(int cColOffset, int cRowOffset)</li>
 	 * <li>translateD(int dColOffset, int dRowOffset)</li>
 	 */
 	public TranslationResponse(){
 		
 	}
 	
 	/**
 	 * Constructs a TranslationResponse representing how to translate a
 	 * {@link Shape}'s A-Block.<br>
 	 * 
 	 * @param aColOffset - number of columns to shift Block-A by.
 	 * @param aRowOffset - number of rows to shift Block-A by.
 	 */
 	public TranslationResponse translateA(int aColOffset, int aRowOffset){
 		this.aColOffset = aColOffset;
 		this.aRowOffset = aRowOffset;
 		
 		return this;
 	}
 	
 	/**
 	 * Constructs a TranslationResponse representing how to translate a
 	 * {@link Shape}'s B-Block.<br>
 	 * 
	 * @param aColOffset - number of columns to shift Block-B by.
	 * @param aRowOffset - number of rows to shift Block-B by.
 	 */
 	public TranslationResponse translateB(int bColOffset, int bRowOffset){
 		this.bColOffset = bColOffset;
 		this.bRowOffset = bRowOffset;
 		
 		return this;
 	}
 	
 	/**
 	 * Constructs a TranslationResponse representing how to translate a
 	 * {@link Shape}'s C-Block.<br>
 	 * 
	 * @param aColOffset - number of columns to shift Block-C by.
	 * @param aRowOffset - number of rows to shift Block-C by.
 	 */
 	public TranslationResponse translateC(int cColOffset, int cRowOffset){
 		this.cColOffset = cColOffset;
 		this.cRowOffset = cRowOffset;
 		
 		return this;
 	}
 	
 	/**
 	 * Constructs a TranslationResponse representing how to translate a
 	 * {@link Shape}'s D-Block.<br>
 	 * 
	 * @param aColOffset - number of columns to shift Block-D by.
	 * @param aRowOffset - number of rows to shift Block-D by.
 	 */
 	public TranslationResponse translateD(int dColOffset, int dRowOffset){
 		this.dColOffset = dColOffset;
 		this.dRowOffset = dRowOffset;
 		
 		return this;
 	}
 	
 	
 	/**
 	 * Translates each {@link Block} of {@link Shape} s by using the translations
 	 * that define this {@link TranslationResponse}.
 	 * @param s - Shape to be translated.
 	 */
 	public void translate(Shape s){
 		s.translateA(aColOffset, aRowOffset);
 		s.translateB(bColOffset, bRowOffset);
 		s.translateC(cColOffset, cRowOffset);
 		s.translateD(dColOffset, dRowOffset);
 	}
 	
 	/**
 	 * Returns a new TranslationResponse representing the inverse of this TranslationResponse.
 	 * If a {@link Shape} is translated using a TranslationResponse <code>t</code>,
 	 * its inverse, <code>t.getInverse()</code> can be used to translate the shape
 	 * back to its original position.
 	 * @return TranslationReponse
 	 */
 	public TranslationResponse getInverse(){
 		return new TranslationResponse().translateA(-1 * aColOffset, -1 * aRowOffset)
 				                        .translateB(-1 * bColOffset, -1 * bRowOffset)
 				                        .translateC(-1 * cColOffset, -1 * cRowOffset)
 				                        .translateD(-1 * dColOffset, -1 * dRowOffset);
 	}
 }
