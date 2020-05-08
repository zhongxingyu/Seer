 /*   
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package dk.dbc.opensearch.common.compass;
 
 
 import dk.dbc.opensearch.common.compass.CompassEntityResolver;
 import dk.dbc.opensearch.common.config.CompassConfig;
 import dk.dbc.opensearch.common.helpers.XMLUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
 import org.xml.sax.SAXException;
 
 
 public class CPMAlias
 {
     Logger log = Logger.getLogger( CPMAlias.class );
 
 
     String xsemFile;
     NodeList cpmNodeList;
 
     
     public CPMAlias() throws ParserConfigurationException, SAXException, IOException, ConfigurationException
     {
         log.debug( "Entering CPMAlias constructor" );
         xsemFile = CompassConfig.getXSEMPath();
         
         String publicUrl = CompassConfig.getHttpUrl();
         String dtdPath = CompassConfig.getDTDPath();
         String systemUrl = "file://" + dtdPath;
        EntityResolver cer = new CompassEntityResolver( publicUrl, systemUrl );
            
         cpmNodeList = XMLUtils.getNodeList( xsemFile, "xml-object", cer );
         log.debug( "XMLUtils returned node list" );
     }
 
 
     public boolean isValidAlias( String alias ) throws ParserConfigurationException, SAXException, IOException
     {        
         for( int i = 0; i < cpmNodeList.getLength(); i++ )
         {
             Element aliasNode = (Element)cpmNodeList.item( i );
             String aliasValue = aliasNode.getAttribute( "alias" );
             if ( aliasValue.equals( alias ) )
             {
                 return true;
             }
         }
 
         return false;
     }
 }
