 package plugins.adufour.ezplug;
 
 import icy.util.XMLUtil;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import plugins.adufour.vars.lang.Var;
 
 /**
  * Class allowing EzPlug to load/save parameters from/to disk in XML format
  * 
  * @author Alexandre Dufour
  * 
  */
 class EzVarIO
 {
     /**
      * Legacy method that saves a set of parameters to XML file
      * 
      * @param ezVarMap
      * @param f
      */
     static synchronized void save(HashMap<String, EzVar<?>> ezVarMap, File f)
     {
         Document xml = XMLUtil.createDocument(false);
         Element root = XMLUtil.createRootElement(xml, "Parameters");
 
         for (String id : ezVarMap.keySet())
         {
             EzVar<?> var = ezVarMap.get(id);
 
             try
             {
                 Element parameterNode = XMLUtil.addElement(root, "parameter");
                 XMLUtil.setAttributeValue(parameterNode, Var.XML_KEY_ID, id);
                 var.getVariable().saveToXML(parameterNode);
             }
             catch (UnsupportedOperationException e)
             {
                 System.err.println("Warning: variable " + id + " has not been saved (unsupported variable type)");
             }
         }
 
         if (!XMLUtil.saveDocument(xml, f)) throw new EzException("unable to save parameters", true);
     }
 
     /**
      * Legacy method that loads a parameter set from a XML file
      * 
      * @param f
      * @param ezVarMap
      * @throws EzException
      */
     static synchronized void load(File f, HashMap<String, EzVar<?>> ezVarMap) throws EzException
     {
         Document xml = XMLUtil.loadDocument(f);
 
         if (xml == null) throw new EzException("unable to load parameter file", true);
 
         Element root = XMLUtil.getRootElement(xml);
        ArrayList<Element> elements = XMLUtil.getElements(root);
         for (Element element : elements)
         {
             String id = XMLUtil.getAttributeValue(element, Var.XML_KEY_ID, null);
             if (id != null && ezVarMap.containsKey(id))
             {
                 ezVarMap.get(id).getVariable().loadFromXML(element);
             }
         }
     }
 }
