 /**
  * 
  * Class: SVGPainting
  * Description: Parsing <paint> in SVG documents and convert to respective type and color to be drawn on canvas
  * Refer to: <a href="http://www.w3.org/TR/SVG/painting.html#SpecifyingPaint"/>
  * @author: Cheow Yeong Chi
  * @date: 16/03/2013
  * 
  */
 
 package rocks6205.svg.adt;
 
 public class SVGPainting {
 	/*
 	 * PROPERTIES
 	 */
 	SVGColorScheme paintColor;
 	SVGPaintingType paintType;
 
 	/*
 	 * CONSTRUCTORS
 	 */
 	public SVGPainting(SVGPaintingType type) {
 		setPaintType(type);
 	}
 
 	public SVGPainting(SVGColorScheme c){
 		setPaint(c,SVGPaintingType.COLOR);
 	}
 
 	/*
 	 * MUTATOR
 	 */
 	public final void setPaintColor(SVGColorScheme c){
 		if(c!= null)
 			this.paintColor = c;
 	}
 
 	public final void setPaintType(SVGPaintingType type){
		if (type != null)   this.paintType = type;
		if (type == SVGPaintingType.NONE)   this.paintColor = new SVGColorScheme(0, 0, 0, 0);
 	}
 
 	/*
 	 * ACCESSOR
 	 */
 	public SVGColorScheme getPaintColor(){
 		return this.paintColor;
 	}
 
 	public SVGPaintingType getPaintType(){
 		return this.paintType;
 	}
 
 	/*
 	 * METHODS
 	 */
 
 	/**
 	 * Parsing paint attribute into types and color that matches the defined types that matches.
 	 * 
 	 * @param paintAttributeString
 	 * @return Painting object that contains respective type and color to be drawn on canvas
 	 */
 	public static SVGPainting parse(String paintAttributeString){
 		paintAttributeString = paintAttributeString.trim();
 
 		if (paintAttributeString.equalsIgnoreCase(SVGPaintingType.NONE.name())) {
 			return new SVGPainting(SVGPaintingType.NONE);
 		} else if (paintAttributeString.equalsIgnoreCase(SVGPaintingType.CURRENTCOLOR.name())) {
 			return new SVGPainting(SVGPaintingType.CURRENTCOLOR);
 		} else {
 			SVGColorScheme color = SVGColorScheme.parse(paintAttributeString);
 			if (color != null)	{return new SVGPainting(color);}
 		}
 		return null;
 	}
 
 
 	/**
 	 * Setting the type and color of the current paint type.
 	 * @param c
 	 * @param type 
 	 */
 	public final void setPaint(SVGColorScheme c, SVGPaintingType type){
 		setPaintType(type);
 		if (type == SVGPaintingType.COLOR) {setPaintColor(c);}
 	}
 
 	/**
 	 * Variant. Setting the color of paint.
 	 * 
 	 * @param c Color type object of paint
 	 */
 	public final void setPaint(SVGColorScheme c){
 		setPaint(c, SVGPaintingType.COLOR);
 	}
 }
