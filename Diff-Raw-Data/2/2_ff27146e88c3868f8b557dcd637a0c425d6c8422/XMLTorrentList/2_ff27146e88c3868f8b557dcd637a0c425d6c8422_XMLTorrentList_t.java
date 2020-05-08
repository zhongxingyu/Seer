 /*
  * $Id$
  *
  * Copyright © 2008,2009 Bjørn Øivind Bjørnsen
  *
  * This file is part of Quash.
  *
  * Quash is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Quash is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Quash. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.tracker.backend.webinterface.xml;
 
 import com.tracker.backend.webinterface.TorrentList;
 import com.tracker.backend.webinterface.TorrentSearch;
 import com.tracker.backend.entity.Torrent;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  * Implements a searchable torrentlist with the results given in XML for use in
  * different frontends or applications.
  *
  * <p>The format of the XML-stream is like this (liable to change):</p>
  * &lt torrents &gt <br />
  *  &lt torrent id=1 &gt <br />
  *      &lt name &gt <br />
  *          Torrentname <br />
  *      &lt /name &gt <br />
  *      &lt numSeeders &gt <br />
  *          1 <br />
  *      &lt /numSeeders &gt <br />
  *      &lt numLeechers &gt <br />
  *          2 <br />
  *      &lt /numLeechers &gt <br />
  *      &lt numCompleted &gt <br />
  *          10 <br />
  *      &lt /numCompleted &gt <br />
  *      &lt dateAdded &gt <br />
  *          Thu Nov 13 20:06:20 CET 2008 <br />
  *      &lt /dateAdded &gt <br />
  *  &lt /torrent &gt <br />
  * &lt /torrents &gt <br />
  * And so on.
  * @author bo
  */
 public class XMLTorrentList implements TorrentList {
     
     Logger log = Logger.getLogger(XMLTorrentList.class.getName());
 
     /**
      * Implements printTorrentList from com.tracker.backend.webinterface.TorrentList
      * @see com.tracker.backend.webinterface.TorrentList
      */
     @Override
     public void printTorrentList(Map<String, String[]> requestMap, PrintWriter out) {
         try {
             Vector<Torrent> result = (Vector<Torrent>) TorrentSearch.getList(requestMap);
             Iterator itr = result.iterator();
 
            // Set-up JAXP + SAX
             StreamResult streamResult = new StreamResult(out);
             SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
 
             // SAX2.0 ContentHandler.
             TransformerHandler tHandler = tf.newTransformerHandler();
             Transformer serializer = tHandler.getTransformer();
 
             // set encoding and indenting
             serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
             serializer.setOutputProperty(OutputKeys.INDENT,"yes");
 
             tHandler.setResult(streamResult);
 
             // document start
             tHandler.startDocument();
             AttributesImpl attr = new AttributesImpl();
 
             // add results to XML
             /*
              * markup looks like this:
              * - torrents
              *   - torrent [id]
              *     - name
              *     - num seeders
              *     - num leechers
              *     - num completed
              *     - (data moved?)
              *     - date added
              *   - end torrent
              * - end torrents
              */
             
             tHandler.startElement("", "", "torrents", attr);
 
             itr = result.iterator();
             while(itr.hasNext()) {
                 Torrent t = (Torrent) itr.next();
 
                 attr.clear();
                 attr.addAttribute("", "", "id", "", t.getId().toString());
                 tHandler.startElement("", "", "torrent", attr);
 
                 attr.clear();
 
                 tHandler.startElement("", "", "name", attr);
                 tHandler.characters(t.getTorrentData().getName().toCharArray(),
                         0, t.getTorrentData().getName().length());
                 tHandler.endElement("", "", "name");
 
                 tHandler.startElement("", "", "numSeeders", attr);
                 tHandler.characters(t.getNumSeeders().toString().toCharArray(),
                         0, t.getNumSeeders().toString().length());
                 tHandler.endElement("", "", "numSeeders");
 
                 tHandler.startElement("", "", "numLeechers", attr);
                 tHandler.characters(t.getNumLeechers().toString().toCharArray(),
                         0, t.getNumLeechers().toString().length());
                 tHandler.endElement("", "", "numLeechers");
 
                 tHandler.startElement("", "", "numCompleted", attr);
                 tHandler.characters(t.getNumCompleted().toString().toCharArray(),
                         0, t.getNumCompleted().toString().length());
                 tHandler.endElement("", "", "numCompleted");
 
                 // data moved?
 
                 tHandler.startElement("", "", "dateAdded", attr);
                 tHandler.characters(t.getTorrentData().getAdded().toString().toCharArray(),
                         0, t.getTorrentData().getAdded().toString().length());
                 tHandler.endElement("", "", "dateAdded");
 
                 tHandler.endElement("", "", "torrent");
             }
 
             tHandler.endElement("", "", "torrents");
         } catch (SAXException ex) {
             log.log(Level.SEVERE, "SAX Exception caught", ex);
         } catch (TransformerConfigurationException ex) {
             log.log(Level.SEVERE, "Cannot configure XML transformer", ex);
         } catch (Exception ex) {
             log.log(Level.SEVERE, "Exception caught when trying to form XML", ex);
         }
     }
 }
