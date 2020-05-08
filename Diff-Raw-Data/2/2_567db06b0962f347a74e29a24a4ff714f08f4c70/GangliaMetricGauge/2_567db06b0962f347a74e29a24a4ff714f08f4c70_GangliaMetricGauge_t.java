 /**
  * This file is part of gauge.
  *
  * gauge is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * gauge is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with gauge.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mashti.gauge.ganglia;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.mashti.gauge.Gauge;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import static org.apache.commons.io.IOUtils.closeQuietly;
 
 /** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
 public class GangliaMetricGauge implements Gauge<String> {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GangliaMetricGauge.class);
     private static final int DEFAULT_GMETAD_INTERACTIVE_PORT = 8652;
     private static final String LOCALHOST = "localhost";
     private static final String METRIC_TAG_NAME = "METRIC";
     private static final String METRIC_NAME_ATTRIBUTE = "NAME";
     private static final String METRIC_VALUE_ATTRIBUTE = "VAL";
     private static final String DELIMITER = "/";
     private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
     private final String host_name;
     private final int port;
     private final String metric_name;
     private final String query;
     private final MetricValueHandler handler;
     private final XMLReader xml_reader;
     private final SAXParser parser;
 
     public GangliaMetricGauge(String cluster_name, String node_name, String metric_name) throws ParserConfigurationException, SAXException {
 
         this(LOCALHOST, DEFAULT_GMETAD_INTERACTIVE_PORT, cluster_name, node_name, metric_name);
     }
 
     public GangliaMetricGauge(String host_name, int gmetad_interactive_port, String cluster_name, String node_name, String metric_name) throws ParserConfigurationException, SAXException {
 
         this.host_name = host_name;
         this.metric_name = metric_name;
         port = gmetad_interactive_port;
         query = constructQuery(cluster_name, node_name, metric_name);
         handler = new MetricValueHandler();
         parser = SAX_PARSER_FACTORY.newSAXParser();
         xml_reader = parser.getXMLReader();
         xml_reader.setContentHandler(handler);
     }
 
     @Override
     public String get() {
 
         String result;
         Socket connection = null;
         try {
             connection = connect();
             writeQuery(connection);
             result = readResult(connection);
         }
         catch (Exception e) {
             LOGGER.error("failed to query {} on host {}:{}. cause: {}", query, host_name, port, e);
            LOGGER.debug("failure to query ganglia", e);
             result = null;
         }
         finally {
             closeQuietly(connection);
         }
 
         return result;
     }
 
     private String readResult(final Socket connection) throws IOException, SAXException {
 
         xml_reader.parse(new InputSource(connection.getInputStream()));
         return handler.value;
     }
 
     private void writeQuery(final Socket connection) throws IOException {
 
         final PrintWriter writer = new PrintWriter(connection.getOutputStream());
         writer.println(query);
         writer.flush();
     }
 
     private Socket connect() throws IOException {
 
         return new Socket(host_name, port);
     }
 
     private String constructQuery(final String cluster_name, final String node_name, final String metric_name) {
 
         return new StringBuilder().append(DELIMITER).append(cluster_name).append(DELIMITER).append(node_name).append(DELIMITER).append(metric_name).toString();
     }
 
     private class MetricValueHandler extends DefaultHandler {
 
         private String value;
 
         @Override
         public void startElement(final String uri, final String local_name, final String q_name, final Attributes attributes) throws SAXException {
 
             if (q_name.equals(METRIC_TAG_NAME)) {
                 String name = attributes.getValue(METRIC_NAME_ATTRIBUTE);
                 if (name.equals(metric_name)) {
                     value = attributes.getValue(METRIC_VALUE_ATTRIBUTE);
                 }
             }
         }
     }
 }
