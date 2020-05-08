 package ia;
 
 import org.jgrapht.UndirectedGraph;
 import org.jgrapht.graph.DefaultEdge;
 
 /**
  * 
 * @author Anderson Queiroz, Fernando Zucatelli, João Coutinho, Tiago Queiroz
  *
  */
 public class PriceStationLink extends DefaultEdge 
 {
 	private static int count = 0;
 	protected UndirectedGraph<Station, PriceStationLink> graph;
 	public int my_number;
 	protected double price;
 
 	/**
 	 * @param graph
 	 * @param price
 	 */
 	public PriceStationLink(UndirectedGraph<Station, PriceStationLink> graph,
 			double price)
 	{
 		super();
 		this.graph = graph;
 		this.price = price;
 		this.my_number = this.count++;
 	}
 	
 	public double getPrice()
 	{
 		return price;
 	}
 
 	public void setPrice(double price)
 	{
 		this.price = price;
 	}
 	
 	@Override
 	protected Station getSource()
 	{
 		return graph.getEdgeSource(this);
 	}
 
 	@Override
 	protected Station getTarget()
 	{
 		return graph.getEdgeTarget(this);
 	}
 
 	@Override
 	public String toString()
 	{
 		return new String ("N:" + my_number + " " + this.getSource() + " <-- " + "R$ " + price + " --> " + this.getTarget());
 	}
 }
