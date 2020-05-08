 /**
  * 
  */
 package de.xwic.cube.webui.viewer;
 
 import de.xwic.cube.ICube;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.Key;
 
 /**
  * @author Lippisch
  *
  */
 public abstract class AbstractCubeDataProvider implements ICubeDataProvider {
 
 	protected int priority = 1;
 	
 	/**
 	 * 
 	 */
 	public AbstractCubeDataProvider() {
 		super();
 	}
 	
 	public AbstractCubeDataProvider(int priority) {
 		this.priority = priority;
 	}
 
 	public Key createCursor(CubeViewerModel model, ContentInfo row, ContentInfo col) {
 		ICube cube = model.getCube();
 		Key cursor = model.createCursor();
 		
 		if (row != null) {
 			for (IDimensionElement elm : row.getElements()) {
				int idx = cube.getDimensionIndex(elm.getDimension());
				cursor.setDimensionElement(idx, elm);
 			}
 		}
 		
 		if (col != null) {
 			for (IDimensionElement elm : col.getElements()) {
				int idx = cube.getDimensionIndex(elm.getDimension());
				cursor.setDimensionElement(idx, elm);
 			}
 		}
 		
 		return cursor;
 	}
 
 	public int getPriority() {
 		return priority;
 	}
 
 	/**
 	 * @param priority the priority to set
 	 */
 	public void setPriority(int priority) {
 		this.priority = priority;
 	}
 
 }
