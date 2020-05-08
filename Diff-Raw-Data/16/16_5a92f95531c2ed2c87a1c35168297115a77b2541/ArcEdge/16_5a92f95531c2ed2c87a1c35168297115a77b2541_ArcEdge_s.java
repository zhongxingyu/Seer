 
 package it.wolfed.model;
 
 import it.wolfed.util.Constants;
import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * Arcs are the edges type in a PetriNets.
  */
 public class ArcEdge extends Edge
 {
     /**
      * {@link ArcEdge} Constructor. 
      * 
      * @param parent
      * @param id
      * @param value 
      */
     public ArcEdge(Object parent, String id, Object value)
     {
         super(
             parent,
             id,
             value,
             null,
             null
         );  
     }
     
     /**
     * Generate a new {@link ArcEdge} from a pnml valid dom node.
     * 
     *  <arc id="a31" source="t7" target="p7">
     *    <inscription>
     *       <text>1</text>
     *     </inscription>
     *     <graphics/>
     *     <toolspecific tool="WoPeD" version="1.0">
     *        <probability>1.0</probability>
     *        <displayProbabilityOn>false</displayProbabilityOn>
     *        <displayProbabilityPosition x="500.0" y="0.0"/>
     *     </toolspecific>
     *  </arc>
      * 
      * @param parent
      * @param dom
      * @param graph 
      * @return ArcEdge
      */
     public static ArcEdge factory(Object parent, Node dom, PetriNetGraph graph)
     {
         String id = dom.getAttributes().getNamedItem(Constants.PNML_ID).getNodeValue();
         String sourceId = dom.getAttributes().getNamedItem(Constants.PNML_SOURCE).getNodeValue();
         String targetId = dom.getAttributes().getNamedItem(Constants.PNML_TARGET).getNodeValue();
         
         ArcEdge arc = new ArcEdge(parent, id, null);
         arc.setSource(graph.getVertexById(sourceId));
         arc.setTarget(graph.getVertexById(targetId));
         
         return arc;
     };
 }
