 package org.eclipse.nebula.visualization.xygraph.figures;
 
 public interface IAnnotationLabelProvider {
 
 	/**
 	 * Return a string to be used on the annotation label.
 	 * @param xValue
 	 * @param yValue
 	 * @return null to use normal labelling, "" to have no label, or a string to be the label.
 	 */
	public String getInfoText(double xValue, double yValue, boolean showName, boolean showSample, boolean showPosition);
 
 }
