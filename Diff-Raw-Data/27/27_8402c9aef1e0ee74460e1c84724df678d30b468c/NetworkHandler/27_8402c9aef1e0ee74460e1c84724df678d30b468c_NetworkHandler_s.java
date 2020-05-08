 package org.renci.databridge.mhandling;
 
 import com.rabbitmq.client.*;
 import org.renci.databridge.database.*;
 import org.renci.databridge.util.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import cern.colt.matrix.impl.RCDoubleMatrix2D;
 
 public class NetworkHandler implements BaseHandler {
 
   public void handle(String msg, Channel channel, String LOG_QUEUE) throws Exception{
 
     String fileLoc = msg;
 
     channel.basicPublish("", LOG_QUEUE, null, new String("Handler: file location determined").getBytes());
 
     NetworkData retriever = new NetworkData();
     try{
       retriever.populateFromURL(fileLoc);
     }
     catch (IOException e){
       channel.basicPublish("", LOG_QUEUE, null, new String("Handler: ERROR - Invalid filename").getBytes());
       System.exit(0);
     }
 
     DBWriter dbw = new DBWriterNeo4j();
     ArrayList<Dataset> datasets = retriever.getDatasets();
     int i = 0;
     for(Dataset d : datasets){
      dbw.writeNode(DBNode n = new DBNode(i++, "dataset", d.getDbID(), makeProps(d.getProperties())));
     }
     String[][] edgeProps = makeProps(retriever.getProperties());
     String edgeID = retriever.getDbID();
     RCDoubleMatrix2D similMx = retriever.getSimilarityMatrix();
     for(int y = 0; y < similMx.columns(); y++){
       for(int x = 0; x < similMx.rows(); x++){
	dbw.writeEdge(dbEdge e = new DBEdge(x, y, "distance", edgeID, edgeProps));
       }
     }
 
     
 
     channel.basicPublish("", LOG_QUEUE, null, new String("Handler: complete").getBytes());
 
   }
 
  private String[][] makeProps(HashMap<String, String> propMap){
     String[][] propArray = new String[propMap.keySet().size()][2];
     int i = 0;
     for(Map.Entry<String, String> e : propMap.entrySet()){
       propArray[i][0] = e.getKey();
       propArray[i++][1] = e.getValue();
     }
     return propArray;
   }
 
 }
