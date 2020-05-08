 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.xml;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.morphoss.acal.Constants;
 
 import android.util.Log;
 
 public class DavXmlTreeBuilder {
 
 	private DavNode root;
 	public static String lastXMLParsed = "";
 	public static final String TAG = "aCal DavXMLTreeBuilder";
 
 	public DavXmlTreeBuilder (Document dom) {
 		try {
 		NodeList nl = dom.getChildNodes();
 		HashMap<String,String> nameSpaces = new HashMap<String,String>();
 		root = new DavNode();
 		for (int i = 0; i < nl.getLength(); i++) {
 			Node item = nl.item(i);
 			if (item.getNodeType() == Node.ELEMENT_NODE) root.addChild(getSubTree(item, null,nameSpaces, root));
 		}
 		} catch (Exception e) {
 			Log.e(TAG, "Error occured creating XML tree."+e);
 		}
 	}
 
 	public DavNode getRoot() {
 		return this.root;
 	}
 
 	public DavNode getSubTree(Node n, String ns, Map<String,String> spaces, DavNode parent) {
 		try {
 		DavNode root = null;
 		if (n.getNodeName().toLowerCase().contains("calendar-timezone")) {
 			root = new CalendarTimeZoneNode(n,ns,spaces,parent);
 		} else
 			root = new DavNode(n,ns,spaces,parent);
 		if (root.getNameSpace() != null) ns = root.getNameSpace();
 		NodeList nl = n.getChildNodes();
 		for (int i = 0; i < nl.getLength(); i++) {
 			Node item = nl.item(i);
 			if (item.getNodeType() == Node.ELEMENT_NODE) root.addChild(getSubTree(item,ns,spaces,root));
 		}
 		return root;
 		} catch (Exception e) {
 			Log.e(TAG, "Error occured creating XML tree."+e);
 			return null;
 		}
 	}
 
 	public static DavNode buildTreeFromXml(InputStream in) {
 		long start = System.currentTimeMillis();
 
		if ( Constants.LOG_VERBOSE )
			Log.v(TAG,"Building DOM from XML input stream");
 
		try {
 			//Build XML Tree
 			DocumentBuilderFactory dof = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dob = dof.newDocumentBuilder();
 			Document dom = dob.parse(in);
 			DavXmlTreeBuilder dxtb = new DavXmlTreeBuilder(dom);
 			DavNode root = dxtb.getRoot();
			if (Constants.LOG_VERBOSE)
				Log.v(TAG,"Build DOM from XML completed in "+(System.currentTimeMillis()-start)+"ms");
 			return root;
 
 		} catch (Exception e) {
			Log.d(TAG,Log.getStackTraceString(e));
 		}
 		return null;
 		
 	}
 
 
 	public static DavNode buildTreeFromXml(String xml) {
 		long start = System.currentTimeMillis();
 		lastXMLParsed = xml;
 
 		try {
 			InputStream in = new ByteArrayInputStream(xml.getBytes());
 			//Build XML Tree
 			DocumentBuilderFactory dof = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dob = dof.newDocumentBuilder();
 			Document dom = dob.parse(in);
 			DavXmlTreeBuilder dxtb = new DavXmlTreeBuilder(dom);
 			DavNode root = dxtb.getRoot();
 
 			if (Constants.LOG_VERBOSE) Log.v(TAG,"Build DOM from XML completed in "+(System.currentTimeMillis()-start)+"ms");
 			return root;
 
 		} catch (Exception e) {
 			Log.e(TAG,"Error occured while building XML tree."+e.getMessage());
 		}
 		return null;
 	}
 }
