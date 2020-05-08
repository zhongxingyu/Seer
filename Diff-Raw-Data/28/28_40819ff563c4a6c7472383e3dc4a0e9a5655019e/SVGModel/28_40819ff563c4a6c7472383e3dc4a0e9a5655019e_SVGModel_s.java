 /**
  * 
  * Class: SVGModel
  * Description: 
  * 
  * @author: Cheow Yeong Chi
  * @date: 14/03/2013
  * 
  */
 
 package rocks6205.svg.engine;
 
 import java.util.Observable;
 
 import rocks6205.svg.elements.SVGSVGElement;
 import rocks6205.svgFamily.SVGImageCanvas;
 
 public class SVGModel extends Observable{
 
 	private SVGSVGElement SVGElement;
 	private SVGImageCanvas canvas;
 
 	public SVGModel() {
 		// TODO Auto-generated constructor stub
 	}
 
 
 	/*
 	 * ACCESSOR
 	 */
 	public SVGSVGElement getSVGElement() {
 		return SVGElement;
 	}
 
 	public SVGImageCanvas getCanvas() {
 		return canvas;
 	}
 	/*
 	 * MUTATORS
 	 */
 	public void setCanvas(SVGImageCanvas canvas) {
 		this.canvas = canvas;
 	}
 
 	public void setSVGElement(SVGSVGElement sVGElement) {
 		SVGElement = sVGElement;
 	}
 
 	/*
 	 * METHOD
 	 */
 
 	/**
 	 * Renders SVG file by notifying Observer
 	 */
 	public void render() {
 		setCanvas(SVGElement.draw());
 		setChanged();
//		notifyObservers(this.canvas);
 	}
 }
