 package org.dawnsci.plotting.expression;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
 
 class ExpressionLazyDataset extends LazyDataset {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4008063310659517138L;
 
 	public ExpressionLazyDataset(String name, int dtype, int[] shape, ILazyLoader loader) {
 		super(name, dtype, shape, loader);
 	}
 	
 	public void setShapeSilently(final int[] shape) {
 		this.shape = shape;
 		try {
 			size = AbstractDataset.calcSize(shape);
 		} catch (IllegalArgumentException e) {
 			size = Integer.MAX_VALUE; // this indicates that the entire dataset cannot be read in! 
 		}
		if (lazyErrorDeligate!=null) lazyErrorDeligate.setShape(shape);
 	}
 
 }
