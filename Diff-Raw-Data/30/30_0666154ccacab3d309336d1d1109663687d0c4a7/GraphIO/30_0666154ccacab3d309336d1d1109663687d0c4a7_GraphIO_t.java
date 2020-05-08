 /*
  *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
  * 
  *  MONGKIE is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  MONGKE is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  * 
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package kobic.prefuse.data.io;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.openide.util.Exceptions;
 import prefuse.data.Graph;
 import prefuse.data.Tuple;
 
 /**
  *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 public class GraphIO {
 
     private static final Logger logger = Logger.getLogger(GraphIO.class.getName());
 
     public static Graph readSerializableGraph(InputStream from) {
         Graph g = null;
         ObjectInputStream in = null;
         try {
             in = new ObjectInputStream(from);
             g = new Graph(((SerializableTable) in.readObject()).getTable(),
                     ((SerializableTable) in.readObject()).getTable(),
                     in.readBoolean(), (String) in.readObject(), (String) in.readObject(), (String) in.readObject());
             g.setNodeLabelField((String) in.readObject());
             g.setEdgeLabelField((String) in.readObject());
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
         }
 
         if (g != null) {
             logger.log(Level.INFO, "Graph is loaded successfully");
         } else {
             logger.log(Level.SEVERE, "Graph can not be loaded from the input stream");
         }
         return g;
     }
 
     public static boolean writeSerializableGraph(Graph g, OutputStream to) {
         ObjectOutputStream out = null;
         try {
             out = new ObjectOutputStream(to);
             SerializableTable serializableNodeTable = new SerializableTable(g.getNodeTable());
             out.writeObject(serializableNodeTable);
             SerializableTable serializableEdgeTable = new SerializableTable(g.getEdgeTable());
             String nodeKey = g.getNodeKeyField();
             String sourceKey = g.getEdgeSourceField();
             String targetKey = g.getEdgeTargetField();
             if (nodeKey == null) {
                 for (Iterator<Tuple> edgesIter = serializableEdgeTable.getTable().tuples(); edgesIter.hasNext();) {
                     Tuple edge = edgesIter.next();
                     edge.setInt(sourceKey, serializableNodeTable.getRow(edge.getInt(sourceKey)));
                     edge.setInt(targetKey, serializableNodeTable.getRow(edge.getInt(targetKey)));
                 }
             }
            out.writeObject(serializableEdgeTable);
             out.writeBoolean(g.isDirected());
             out.writeObject(nodeKey);
             out.writeObject(sourceKey);
             out.writeObject(targetKey);
             out.writeObject(g.getNodeLabelField());
             out.writeObject(g.getEdgeLabelField());
         } catch (IOException ex) {
             Exceptions.printStackTrace(ex);
             return false;
         } finally {
             if (out != null) {
                 try {
                     out.flush();
                     out.close();
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                     return false;
                 }
             }
         }
 
         logger.log(Level.INFO, "Graph is written successfully");
         return true;
     }
 }
