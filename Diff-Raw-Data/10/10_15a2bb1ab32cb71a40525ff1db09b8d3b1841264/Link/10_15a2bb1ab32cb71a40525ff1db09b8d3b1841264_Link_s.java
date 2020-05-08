 package model;
 
 /**
  * The link class represents a communications link between two networked nodes
  * (vertices) in a plane.
  * 
  * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
  * @version 1.0
  * @since 2011-09-10
  */
 public class Link implements WeightedEdgeInterface {
 
 	private String name = null;
 	private float weight = 0f;
 
 	/**
 	 * Basic constructor for a Link object.
 	 * 
 	 * @param name
 	 *            the name of the Link.
 	 */
 	public Link(String name) {
 
 		this.name = name;
 	}
 
 	/**
 	 * Constructor for a Link object.
 	 * 
 	 * @param name
 	 *            the name of the link.
 	 * @param weight
 	 *            the weight of the link.
 	 */
 	public Link(String name, float weight) {
 
 		this(name);
 		this.weight = weight;
 	}
 
 	@Override
 	public int compareTo(WeightedEdgeInterface o) {
 
		return (int) (getWeight() - o.getWeight());
 	}
 
 	@Override
 	public float getWeight() {
 		return weight;
 	}
 
 	@Override
 	public void setWeight(float weight) {
 		this.weight = weight;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 }
