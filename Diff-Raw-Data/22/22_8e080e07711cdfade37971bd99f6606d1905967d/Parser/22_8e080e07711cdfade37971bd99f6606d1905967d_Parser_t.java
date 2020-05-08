 package com.ghostrun.controllers;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.ghostrun.model.MazeGraph;
 import com.ghostrun.model.MazeGraphPoint;
 import com.google.android.maps.GeoPoint;
 
 public class Parser {
    private Vector<Integer> ids;
     private Vector<Double> lats;
     private Vector<Double> lons;
     private Vector<Integer> src;
     private Vector<Integer> dest;
     private HashMap<Integer,MazeGraphPoint> hash_map;
     
     public Parser() {
        ids = new Vector<Integer>();
         lats = new Vector<Double>();
         lons = new Vector<Double>();
         src = new Vector<Integer>();
         dest = new Vector<Integer>();
         hash_map = new HashMap<Integer,MazeGraphPoint>();
     }
     
     /**
     * Construct a MazeGraph object from an input xml file.
     * @param fileName
      */
     public MazeGraph parse(InputStream instream) {
         MazeGraph g = new MazeGraph();
         try {
             DocumentBuilder doc_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document doc = doc_builder.parse(instream);
            parsingATag(doc,"Node");
             parsingATag(doc,"LatLng");
             parsingATag(doc,"Edge");
             
            for (int i=0;i<ids.size();++i) {
                 MazeGraphPoint point = g.addPoint(new GeoPoint((int)(lats.get(i).doubleValue()*1E6),
                                                                (int)(lons.get(i).doubleValue()*1E6)));
                hash_map.put(ids.get(i), point);
             }
             
             for (int i=0;i<src.size();++i) {
                 MazeGraphPoint srcVertex = hash_map.get(src.get(i));
                 MazeGraphPoint dstVertex = hash_map.get(dest.get(i));
                 g.addEdge(srcVertex,dstVertex);
             }
             
         } catch (Exception e) {
             System.out.print("Exception found in Parser.java: "+e.getMessage());
         }
         return g;
     }
     
     private void parsingATag(Document doc, String tagName) {
         NodeList node_list = doc.getElementsByTagName(tagName);
         if (node_list!=null) {
             for (int i=0;i<node_list.getLength();++i) {
                 Node tag_node = node_list.item(i);
                 //System.out.println(tag_node.getNodeName());
                 NamedNodeMap node_attributes = tag_node.getAttributes();
                 for (int j=0;j<node_attributes.getLength();++j) {
                     Node attribute = node_attributes.item(j);
                    if (attribute.getNodeName().equals("id")) {
                        ids.add(Integer.parseInt(attribute.getNodeValue()));
                    } else if (attribute.getNodeName().equals("lat")) {
                         lats.add(Double.parseDouble(attribute.getNodeValue()));
                     } else if (attribute.getNodeName().equals("lon")) {
                         lons.add(Double.parseDouble(attribute.getNodeValue()));
                     } else if (attribute.getNodeName().equals("source")) {
                         src.add(Integer.parseInt(attribute.getNodeValue()));
                     } else if (attribute.getNodeName().equals("dest")) {
                         dest.add(Integer.parseInt(attribute.getNodeValue()));
                     }
                     //System.out.println(attribute.getNodeName()+" "+attribute.getNodeValue());
                 }
             }
         }
     }
 }
