 
 package it.wolfed.model;
 
 import it.wolfed.util.Constants;
 import it.wolfed.util.IterableNodeList;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * Transition.
  */
 public class TransitionVertex extends Vertex
 {
     /**
      * TransitionVertex Constructor
      * 
      * @param parent
      * @param id
      * @param value
      * @param x
      * @param y
      */
     public TransitionVertex(Object parent, String id, Object value, double x, double y)
     {
         super(
             parent,
             id,
             value,
             x, y, 40, 40,
             Constants.STYLE_TRANSITION
         );
     }
     
     /**
      * Generate a new {@link TransitionVertex} from a pnml valid dom node.
      * 
      * <transition id="t1"> 
      * 	<name> 
      * 		<text>t1</text> 
      * 		<graphics> 
      * 			<offset x="120" y="110"/> 
      * 		</graphics> 
      * 	</name> 
      * 	<graphics> 
      * 		<position x="125" y="70"/>
      * 		<dimension x="40" y="40"/> 
      * 	</graphics> 
      * 	<toolspecific tool="WoPeD" version="1.0"> 
      * 		<time>0</time> 
      * 		<timeUnit>1</timeUnit>
      * 		<orientation>1</orientation> 
      * 	</toolspecific> 
      * </transition>
      *
      * @param parent
      * @param dom
      * @return TransitionVertex
      * @see <a href="http://www.pnml.org/">http://www.pnml.org/</a>
      */
     public static TransitionVertex factory(Object parent, Node dom)
     {
         String id, value = "";
         double x = 0, y = 0;
         
         id = dom.getAttributes().getNamedItem(Constants.PNML_ID).getNodeValue();
 
         for (final Node childNode : new IterableNodeList(dom.getChildNodes()))
         {
             if (childNode.getNodeType() == Node.ELEMENT_NODE)
             {
                 switch (childNode.getNodeName())
                 {
                     // @note pnml "name" will be mapped to "value" property
                     case Constants.PNML_NAME:
                     {
                         value = childNode.getTextContent().trim();
                         break;
                     }
                         
                     /** 
                     * Set the geometric aspect of the Vertex
                     * 
                     *  <graphics> 
                     *       <position x="200" y="70"/>
                     * 	    <dimension x="40" y="40"/> 
                     * 	</graphics> 
                     */
                     case Constants.PNML_GRAPHICS:
                     {
                         for (final Node graphNode : new IterableNodeList(childNode.getChildNodes()))
                         {
                             if (graphNode.getNodeType() == Node.ELEMENT_NODE)
                             {
                                 switch(graphNode.getNodeName())
                                 {
                                     case Constants.PNML_GRAPHICS_POSITION :
                                     {
                                         x = Double.valueOf(graphNode.getAttributes().getNamedItem(Constants.PNML_GRAPHICS_POSITION_X).getNodeValue());
                                         y = Double.valueOf(graphNode.getAttributes().getNamedItem(Constants.PNML_GRAPHICS_POSITION_Y).getNodeValue());
                                         break;
                                     }
                                 }
                             }
                         }
                         
                         break;
                     } 
                 }
             }
         }
         
         return new TransitionVertex(parent, id, value, x, y);
     }
     
     /**
      * Export PNML Transition.
      * 
      * @return 
      */
     public Element exportPNML(Document doc ) throws ParserConfigurationException 
     {
         /** <transition id="t3"> */
 	Element transition = doc.createElement(Constants.PNML_TRANSITION);
 	transition.setAttribute(Constants.PNML_ID, getId());
 
         /**    <name> */
 	Element name = doc.createElement(Constants.PNML_NAME);
         transition.appendChild(name);
 
         /**         <text>p6</text> */
 	Element text = doc.createElement(Constants.PNML_TEXT);
 	text.setTextContent(getValue().toString());
         name.appendChild(text);
        
 	 /**
 	 *              <graphics>
 	 *                  <position x="275" y="140"/>
 	 *                  <dimension x="40" y="40"/>
 	 *              </graphics>
 	 *              <toolspecific tool="WoPeD" version="1.0">
 	 *                  <time>0</time>
 	 *                  <timeUnit>1</timeUnit>
 	 *                  <orientation>1</orientation>
 	 *              </toolspecific>
 	 */
         Element graphics = doc.createElement(Constants.PNML_GRAPHICS);
         Element position = doc.createElement(Constants.PNML_GRAPHICS_POSITION);
         position.setAttribute(Constants.PNML_GRAPHICS_POSITION_X, String.valueOf(getGeometry().getX()));
         position.setAttribute(Constants.PNML_GRAPHICS_POSITION_Y, String.valueOf(getGeometry().getY()));
 	graphics.appendChild(position);
         
         transition.appendChild(graphics);
         
         /**  </transition>  */
         return transition;
     }
     
     /**
      * Export DOT Transition.
      * 
      * @return 
      */
       public String exportDOT() 
       {
        return "\n "+ this.getId() + " [label=\"" + getValue().toString() + "\", shape=\"box\" ];";
     }
 }
