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
 
 
 package dk.dbc.opensearch.plugins;
 
 
 import dk.dbc.opensearch.common.pluginframework.ICreateCargoContainer;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.components.datadock.DatadockJob;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 
 
 public class DCHarvester implements ICreateCargoContainer
 {
     Logger log = Logger.getLogger( DCHarvester.class );
 

    private String submitter;
    private String format;
    private byte[] data;
     private Document referenceData;
 
     private PluginType pluginType = PluginType.HARVEST;
 
 
     public CargoContainer getCargoContainer( DatadockJob job, byte[] data ) throws PluginException
     {
 
         return createCargoContainerFromFile( job, data );
     }
 
 
     /**
      *
      * @return the CargoContainer from
      * @throws IOException if the data cannot be read
      */
     private CargoContainer createCargoContainerFromFile( DatadockJob job, byte[] data ) throws PluginException
     {
        String submitter = job.getSubmitter();
        String format = job.getFormat();

         CargoContainer cargo = new CargoContainer();
 
         /** \todo: hardcoded values for mimetype, langugage and data type */
         String mimetype = "text/xml";
         String lang = "da";
         // String dataMimetype = "application/pdf";
         DataStreamType dataStreamName = DataStreamType.OriginalData;
 
         try
         {
            cargo.add( dataStreamName, format, submitter, lang, mimetype, IndexingAlias.DC, data );
         }
         catch (IOException ioe)
         {
             String error = String.format( "Failed to add DublinCore metadata to CargoContainer", ioe.getMessage() );
             log.error( error );
             throw new PluginException( error, ioe );
         }
 
         log.debug(String.format("num of objects in cargo: %s", cargo.getCargoObjectCount()) );
         return cargo;
     }
 
 
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
     private byte[] getDCData( byte[] referenceData )
     {
         return null;
     }
 
 }
