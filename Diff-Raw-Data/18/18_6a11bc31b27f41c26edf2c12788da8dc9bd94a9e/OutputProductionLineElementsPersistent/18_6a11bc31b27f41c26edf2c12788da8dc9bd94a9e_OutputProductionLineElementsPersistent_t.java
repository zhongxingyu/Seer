 package persistence;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import model.production.Direction;
 import model.production.elements.OutputProductionLineElement;
 import model.utils.StringUtils;
 import model.warehouse.Position;
 import model.warehouse.Wall;
 
 import org.dom4j.Element;
 
 import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
 
 public class OutputProductionLineElementsPersistent {
 
 	static String TAG_NAME="Outputs";
 	static String TAG_INNER="OutputElement";
 	static String TAG_ATR_ROWS="rows";
 	static String TAG_ATR_COLS="cols";
 	static String TAG_ATR_DIRECTION="direction";
 	
 	@SuppressWarnings("unchecked")
 	static public List<OutputProductionLineElementPositioned> buildFromXML(Element element)
 	throws InvalidTagException{
 		
 		if (!element.getName().equals(OutputProductionLineElementsPersistent.TAG_NAME))
 			throw new InvalidTagException();
 		
 		List<OutputProductionLineElementPositioned> list=
 			new ArrayList<OutputProductionLineElementPositioned>();
 		
 		Iterator<Element> iter=element.elementIterator();
 		
 		while(iter.hasNext()){
			Element childElem = iter.next();
			if (!childElem.getName().equals(
 					OutputProductionLineElementsPersistent.TAG_INNER)){
 				throw new InvalidTagException();
 			}
 			
 
			Direction direction= (childElem.attributeValue(TAG_ATR_DIRECTION)!=null?
 					Direction.getDirectionByString(StringUtils.
							toChar(childElem.attributeValue(TAG_ATR_DIRECTION))):
 					Direction.NORTH);
 			
 			OutputProductionLineElement outputElement = 
 				new OutputProductionLineElement(direction);	
 			
 			list.add(new OutputProductionLineElementPositioned(
						new Position(Integer.valueOf(childElem.attributeValue(
 									WallsPersistent.TAG_CHILD_ATR_ROWS)),
									Integer.valueOf(childElem.attributeValue(
 											WallsPersistent.TAG_CHILD_ATR_COLS)))
 						,outputElement));
 			
 		}
 		
 		return list;
 	}	
 }
