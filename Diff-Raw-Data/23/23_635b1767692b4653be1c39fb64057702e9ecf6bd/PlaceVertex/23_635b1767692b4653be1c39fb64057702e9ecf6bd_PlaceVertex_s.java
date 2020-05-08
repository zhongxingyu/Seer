 
 package it.wolfed.model;
 
 import it.wolfed.util.Constants;
 import it.wolfed.util.IterableNodeList;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * Place.
  */
 public class PlaceVertex extends Vertex
 {
     /**
      * Number of tokens on the place.
      * 
      * Pnml mapped from {@link Constants#PNML_INITIALMARKING}
      */
     private int tokens = 0;
     
     /**
      * {@link PlaceVertex} Constructor.
      * 
      * @param parent
      * @param id
      * @param value
      * @param x
      * @param y  
      */
     public PlaceVertex(Object parent, String id, Object value, double x, double y)
     {
         super(
             parent,
             id,
             value,
             x, y, 40, 40,
             Constants.STYLE_PLACE
         );
     }
     
     /**
      * Generate a new {@link PlaceVertex} from a pnml valid dom node.
      * 
      * <place id="p2"> 
      * 	<name> 
      * 		<text>p2</text> 
      * 		<graphics> 
      * 			<offset x="200" y="110"/> 
      * 		</graphics> 
      * 	</name> 
      * 	<graphics> 
      * 		<position x="200" y="70"/>
      * 		<dimension x="40" y="40"/> 
      * 	</graphics> 
      * 	<initialMarking> 
      * 		<text>2</text>
      * 	</initialMarking> 
      * </place>
      * 
      * @param parent
      * @param dom
      * @return  PlaceVertex
      * @see <a href="http://www.pnml.org/">http://www.pnml.org/</a>
      */  
     public static PlaceVertex factory(Object parent, Node dom)
     {
         String id, value = "";
         int tokens = 0;
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
                         
                     // @note pnml "initialmarking" will be mapped to "tokens" property
                     case Constants.PNML_INITIALMARKING:
                     {
                         tokens = Integer.parseInt(childNode.getTextContent().trim());
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
 
         PlaceVertex place = new PlaceVertex(parent, id, value, x, y);
         place.setTokens(tokens);
         return place;
     };
     
     /**
      * Sets place tokens number.
      * 
      * @param tokens 
      */
     public void setTokens(int tokens)
     {
         this.tokens = tokens;
     }
     
     /**
      * Get place tokens number.
      * 
      * @return the tokens
      */
     public int getTokens()
     {
         return tokens;
     }
 
     public Element exportPNML(Document doc) {
         /**
 	 *  <place id="p6">
 	 *      <name>
 	 *        <text>p6</text>
 	 *        <graphics>
 	 *          <offset x="650" y="110"/>
 	 *        </graphics>
 	 *      </name>
 	 *      <graphics>
 	 *        <position x="650" y="70"/>
 	 *        <dimension x="40" y="40"/>
 	 *      </graphics>
 	 *			
 	 *      <initialMarking>
 	 *        <text>2</text>
 	 *      </initialMarking>
 	 *    </place>
 	 */
         Element placeAsXML = doc.createElement(Constants.PNML_PLACE);
 	placeAsXML.setAttribute(Constants.PNML_ID, getId());
 	Element nameAsXML = doc.createElement(Constants.PNML_NAME);
 	Element textAsXML = doc.createElement(Constants.PNML_TEXT);
 	textAsXML.setTextContent(getValue().toString());
 	nameAsXML.appendChild(textAsXML);
 	placeAsXML.appendChild(nameAsXML);
         
          // the geometric aspect of the Vertex
         
         Element graphics = doc.createElement(Constants.PNML_GRAPHICS);
         Element position = doc.createElement(Constants.PNML_GRAPHICS_POSITION);
         position.setAttribute(Constants.PNML_GRAPHICS_POSITION_X, String.valueOf(getGeometry().getX()));
         position.setAttribute(Constants.PNML_GRAPHICS_POSITION_Y, String.valueOf(getGeometry().getY()));
 	graphics.appendChild(position);
         
         
         placeAsXML.appendChild(graphics);
         
 	if(getTokens() > 0)
         {
             Element initialMarkingAsXML = doc.createElement(Constants.PNML_INITIALMARKING);
             Element initialMarkingTextAsXML = doc.createElement(Constants.PNML_TEXT);
             initialMarkingTextAsXML.setTextContent(String.valueOf(getTokens()));
             initialMarkingAsXML.appendChild(initialMarkingTextAsXML);
             placeAsXML.appendChild(initialMarkingAsXML);
 	}
         return placeAsXML;
     }
 
    /**
      * 
      * @return 
      */
       public String exportDOT() {
         
          
          
          return "\n "+this.getId()+" [=\""+getValue().toString()+", shape=circle]; ";
     }
 
     public String exportDOT(int preSet, int postSet) {
         
         StringBuilder dotBuilder = new StringBuilder();
         String shape = "circle , color=\"green\" ]";
         
         dotBuilder.append("\n "+this.getId()+" [label=\""+getValue().toString()+"\", shape=");
         
         
         
         
         switch(getStyle()){
             case Constants.STYLE_PLACE_VALID:
                 shape = "circle , color=\"green\" ]";
                 break;
             case Constants.STYLE_PLACE_SPECIAL_VALID:
                 shape = "doublecircle , color=\"green\" ]";
                 break;
         }
         
         dotBuilder.append(shape);
         
        return dotBuilder.toString();
     }
 }
