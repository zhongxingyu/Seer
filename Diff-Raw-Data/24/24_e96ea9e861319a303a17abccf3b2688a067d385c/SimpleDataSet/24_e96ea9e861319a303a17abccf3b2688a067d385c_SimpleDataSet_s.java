 // License: GPL. For details, see LICENSE file.
 package org.openstreetmap.beboj.shared.data.osm;
 
 import java.util.ArrayList;
 
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.data.osm.Node;
 import org.openstreetmap.josm.data.osm.NodeData;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.data.osm.PrimitiveData;
 import org.openstreetmap.josm.data.osm.Relation;
 import org.openstreetmap.josm.data.osm.RelationData;
 import org.openstreetmap.josm.data.osm.Way;
 import org.openstreetmap.josm.data.osm.WayData;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * Just the plain data from a DataSet; for RPC transfer.
  */
 public class SimpleDataSet implements IsSerializable {
 
     public ArrayList<NodeData> nodes;
     public ArrayList<WayData> ways;
     public ArrayList<RelationData> relations;
     public String version;
 
 //    /**
 //     * All data sources of this DataSet.
 //     */
 //    public Collection<DataSource> dataSources = new LinkedList<DataSource>();
 
     public SimpleDataSet() {
         this("0.6");
     }
 
     public SimpleDataSet(String version) {
         this.version = version;
         nodes = new ArrayList<NodeData>();
         ways = new ArrayList<WayData>();
         relations = new ArrayList<RelationData>();
     }
 
     public static SimpleDataSet fromDataSet(DataSet ds) {
         SimpleDataSet s = new SimpleDataSet();
 
         for (Node n : ds.getNodes()) {
             NodeData nd = n.save();
             s.nodes.add(nd);
         }
         for (Way w : ds.getWays()) {
             WayData wd = w.save();
             s.ways.add(wd);
         }
         for (Relation r : ds.getRelations()) {
             RelationData rd = r.save();
             s.relations.add(rd);
         }
 
         s.version = ds.getVersion();
 
         return s;
     }
 
     public DataSet toDataSet() {
         DataSet ds = new DataSet();
         for (NodeData nd : nodes) {
             Node n = new Node(nd.getUniqueId(), true);
             n.load(nd);
             ds.addPrimitive(n);
         }
         for (WayData wd : ways) {
             Way w = new Way(wd.getUniqueId(), true);
             ds.addPrimitive(w);
             w.load(wd);
         }
         for (RelationData rd : relations) {
             Relation r = new Relation(rd.getUniqueId(), true);
             ds.addPrimitive(r);
             r.load(rd);
         }
         return ds;
     }
 
     public void add(PrimitiveData osm) {
         if (osm instanceof NodeData) {
             nodes.add((NodeData) osm);
         } else if (osm instanceof WayData) {
             ways.add((WayData) osm);
         } else if (osm instanceof RelationData) {
             relations.add((RelationData) osm);
         } else
             throw new AssertionError();
     }
 
     public void addForUpload(DataSet ds) {
         for (OsmPrimitive osm : ds.allPrimitives()) {
             if ((osm.isDeleted() && !osm.isNew() && osm.isModified() && osm.isVisible())
                     || (!osm.isDeleted() && osm.isModified())) {
                 add(osm.save());
             }
         }
     }
 
     public void addAll(Iterable<OsmPrimitive> primitives) {
         for (OsmPrimitive osm : primitives) {
             add(osm.save());
         }
     }
 
     @Override
     public String toString() {
         return version+nodes.size()+"|";
     }
 }
