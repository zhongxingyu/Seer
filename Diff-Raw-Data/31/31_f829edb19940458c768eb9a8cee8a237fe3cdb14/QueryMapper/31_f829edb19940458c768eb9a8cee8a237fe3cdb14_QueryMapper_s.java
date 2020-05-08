 package com.imaginea.qctree.hadoop;
 
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import com.imaginea.qctree.Cell;
 import com.imaginea.qctree.hadoop.QCTree.QCNode;
 import com.imaginea.qctree.measures.Aggregates;
 
 public class QueryMapper extends
     Mapper<NullWritable, QCTree, NullWritable, Aggregates> {
 
   private static final Log LOG = LogFactory.getLog(QueryMapper.class);
   private Cell query;
 
   private NullWritable KEY = NullWritable.get();
 
   @Override
   protected void setup(Context context) throws IOException,
       InterruptedException {
     String query = context.getConfiguration().get("query");
     LOG.info("processing query : " + query);
     // FIXME hard coded, need to come up with better approach?
    String[] split = query.split(" ");
     this.query = new Cell(split);
   };
 
   @Override
   protected void map(NullWritable key, QCTree tree, Context context)
       throws IOException, InterruptedException {
     QCNode node = tree.getRoot();
 
     for (int idx = 0; idx < query.getDimensions().length; ++idx) {
      node = searchRoute(node, idx, query.getDimensionAt(idx));
      if (node == null) {
        break;
       }
     }
 
     if (node == null) {
       LOG.info("query failed.");
     }
    while (!node.isLeaf()) {
      node = node.getLastChild();
    }
    context.write(KEY, node.getAggregates());
   }
 
   private QCNode searchRoute(QCNode node, int dimIdx, String dimVal) {
     if (node.isLeaf()) {
       return node;
     }
 
     // node has a child with given dim name and value.
     for (QCNode c : node.getChildren()) {
       if (c.getDimIdx() == dimIdx && c.getDimValue().equals(dimVal)) {
         return c;
       }
     }
 
     // node has a ddlink with given dim name and value.
     for (QCNode c : node.getDDLinks()) {
       if (c.getDimIdx() == dimIdx && c.getDimValue().equals(dimVal)) {
         return c;
       }
     }
 
     QCNode lastChild = node.getLastChild();
     if (lastChild.getDimIdx() < dimIdx) {
      searchRoute(node, dimIdx, dimVal);
     }
    return null;
   }

 }
