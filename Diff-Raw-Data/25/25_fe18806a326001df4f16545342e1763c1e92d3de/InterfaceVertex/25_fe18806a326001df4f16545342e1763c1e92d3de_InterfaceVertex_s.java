 
 package it.wolfed.model;
 
 import it.wolfed.util.Constants;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Interface.
  */
 public class InterfaceVertex extends Vertex
 {
     /**
      * {@link InterfaceVertex} Constructor
      * 
      * @param parent
      * @param id
      * @param value 
      */
     public InterfaceVertex(Object parent, String id, Object value)
     {
         super(
             parent,
             id,
             value,
             0, 0, 40, 40,
             Constants.STYLE_INTERFACE
         );
     }
 
     /**
      * Export PNML interface
      * 
      * @param doc
      * @return 
      */
     public Element exportPNML(Document doc) 
     {
         /**<interface id="i1"> */
         Element interf = doc.createElement(Constants.PNML_INTERFACE);
 	interf.setAttribute(Constants.PNML_ID, getId());
         
         return interf;
     }
     
     /**
      * Export DOT interface
      * 
      * @return 
      */
       public String exportDOT() 
       {
        return "\n "+this.getId()+" [label=\""+getValue().toString()+"\", shape=doublecircle, color=\"orange\" ]; ";
     }
 }
