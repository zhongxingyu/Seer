 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */
 package edu.ua.eng.software.clonerank.importing;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
import edu.ua.eng.software.clonerank.CodeFragment;
 
 
 /**
  * Usage:
  * ------
  * File cloneFile = new File(cloneResultXML);
  * SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
  * NiCadHandler niCadHandler = new NiCadHandler();
  * saxParser.parse(cloneFile, niCadHandler);
  * ArrayList<ArrayList<CodeFragment>> clones = niCadHandler.getClones();
  * ------
  *
  * @author      Blake Bassett <rbbassett@crimson.ua.edu>
  */
 public class NiCadHandler extends DefaultHandler
 {
     public void startElement (String uri, String localName, String qname, Attributes attributes) throws SAXException {
         if(qname.equals("clone")) {
             cloneClass = new ArrayList<CodeFragment>();
             clones.add(cloneClass);
         } else if(qname.equals("source")) {
             CodeFragment fragment = new CodeFragment(
                 attributes.getValue("file"),
                 Integer.parseInt(attributes.getValue("startline")),
                 Integer.parseInt(attributes.getValue("endline"))
             );
             cloneClass.add(fragment);
         }
     }
     
     public ArrayList<ArrayList<CodeFragment>> getClones() { return clones; }
     
     ArrayList<ArrayList<CodeFragment>> clones = new ArrayList<ArrayList<CodeFragment>>();
     ArrayList<CodeFragment> cloneClass;
 }
