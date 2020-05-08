 /**
  * 
  */
 package de.xwic.cube.functions;
 
 import java.util.List;
 
 import de.xwic.cube.ICell;
 import de.xwic.cube.ICube;
 import de.xwic.cube.IDimension;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.IMeasure;
 import de.xwic.cube.IMeasureFunction;
 import de.xwic.cube.Key;
 
 /**
  * This function returns the data of the last leaf element for the specified Dimension.
  * 
  * Sample: If setup for the Time dimension, a query to the key 
  * [GEO:EMEA][JobCategorye:PSE][Time:2010/Q1] will return the value for
  * [GEO:EMEA][JobCategorye:PSE][Time:2010/Q1/Mar]
  * 
  * The function is searching for the last leaf combination that has data, looking up the childs in reverse order.
  * 
  * @author lippisch
  */
 public class LastLeafFunction implements IMeasureFunction {
 
 	private IDimension forceLeafDim;
 	private IMeasure baseMeasure;
 	private int dimIdx = -1;
 	private int measureIdx = -1;
 	
 	private class Result {
 		Double value = null;;
 		boolean found = false;
 	}
 	
 	/**
 	 * @param forceLeafDim
 	 */
 	public LastLeafFunction(IDimension forceLeafDim, IMeasure baseMeasure) {
 		super();
 		this.forceLeafDim = forceLeafDim;
 		this.baseMeasure = baseMeasure;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IMeasureFunction#computeValue(de.xwic.cube.ICube, de.xwic.cube.Key, de.xwic.cube.ICell, de.xwic.cube.IMeasure)
 	 */
 	public Double computeValue(ICube cube, Key key, ICell cell, IMeasure measure) {
 	
 		if (dimIdx == -1) {
 			dimIdx = cube.getDimensionIndex(forceLeafDim);
 			measureIdx = cube.getMeasureIndex(baseMeasure);
 		}
 		
 		IDimensionElement elm = key.getDimensionElement(dimIdx);
 		if (elm.isLeaf()) {
 			return cell != null ? cell.getValue(measureIdx) : null;
 		}
 		
 		// search the last leaf that has a value
 		Key searchKey = key.clone();
 		// reset all other dimensions (i.e. make a [GEO:*] out of a [GEO:EMEA]
 		for (int i = 0, size = key.getDimensionElements().size(); i < size; i++) {
 			if (i != dimIdx) {
 				searchKey.setDimensionElement(i, 
 					searchKey.getDimensionElement(i).getDimension()		
 				);
 			}
 		}
 	 	return findLastLeaf(cube, key.clone(), searchKey, elm).value;
 	}
 
 	/**
 	 * This method searches for the last leaf. This is done by clearing out the other
 	 * dimensions, to find the last dimension element that is not "empty" at all. This 
 	 * way we make sure that the last element is the same for all keys. 
 	 * @param cube
 	 * @param key
 	 * @param searchKey
 	 * @param elm 
 	 */
 	private Result findLastLeaf(ICube cube, Key key, Key searchKey, IDimensionElement elm) {
 		
 		Result result = new Result();
 		
 		List<IDimensionElement> childs = elm.getDimensionElements();
 		for (int i = childs.size() - 1; i >= 0; i--) {
 			IDimensionElement child = childs.get(i);
 			if (child.isLeaf()) {
 				searchKey.setDimensionElement(dimIdx, child);
 				ICell cell = cube.getCell(searchKey);
 				Double value = cell != null ? cell.getValue(measureIdx) : null;
 				
 				//RPF: added second null check -> caused nullpointer on different views!
 				if (value != null) {
 					key.setDimensionElement(dimIdx, child);
 					
					if (cube.getCell(key) != null) {
						result.value = cube.getCell(key).getValue(measureIdx);
						result.found = true;
 					}
 				}
 			} else {
 				result = findLastLeaf(cube, key, searchKey, child);
 			}
 			if (result.found) {
 				break;
 			}
 		}
 		
 		return result;
 	}
 
 }
