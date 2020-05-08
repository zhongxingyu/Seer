 package org.neo4j.laboratory.env;
 
 import org.neo4j.graphdb.*;
 
 import java.util.*;
 
 public class RmatGenerator implements GraphGenerator
 {
     private final long flushInterval = 10000;
 
     private GraphDatabaseService graphdb;
     private RelationshipType relType;
     private long scale;
 
     /**
      * Generates a graph following the recursive matrix (R-MAT) model, as described in:
      * http://www.siam.org/proceedings/datamining/2004/dm04_043chakrabartid.pdf.
      *
      * @param scale   The scale of the resulting graph, such that the
      *                number of nodes is 2^scale
      * @param relType the type of relationships between Nodes
      * @return Number of edges generated
      */
     private RmatGenerator( GraphDatabaseService graphdb, RelationshipType relType, long scale )
     {
         this.graphdb = graphdb;
         this.relType = relType;
         this.scale = scale;
     }
 
     public static RmatGenerator createRmatGenerator( GraphDatabaseService graphdb, RelationshipType relType, long scale )
     {
         return new RmatGenerator( graphdb, relType, scale );
     }
 
     public Iterable<Node> generate( Node origin )
     {
         // first, create all the nodes. NodeGenerator only returns the last node created, which should be the highest ID
         Iterable<Node> returnNodes = NodeGenerator.createNodeGenerator( graphdb, 1 << scale, flushInterval ).generate( null );
         Node highestNode = returnNodes.iterator().next();
         long offsetid = highestNode.getId() - (1<<scale);
 
         Random r = new Random( 3 );
         HashSet<Long> links = new HashSet<Long>();
 
         long N = 1 << scale;
         long E = N * 8;
 
         long edges = 0;
         Transaction tx = graphdb.beginTx();
         try
         {
             while ( E-- > 0 )
             {
                 long u = 1;
                 long v = 1;
                 long step = N / 2;
 
                 double av = 0.6;
                 double bv = 0.15;
                 double cv = 0.15;
                 double dv = 0.1;
 
                 double p = r.nextDouble();
                 if ( p < av )
                 {
                     // do nothing
                 }
                 else if ( (p >= av) && (p < av + bv) )
                 {
                     v += step;
                 }
                 else if ( (p >= av + bv) && (p < av + bv + cv) )
                 {
                     u += step;
                 }
                 else
                 {
                     u += step;
                     v += step;
                 }
 
                 for ( long j = 1; j < scale; j++ )
                 {
                     step = step / 2;
 
                     // Vary a,b,c,d by up to 10%
                     double var = 0.1;
                     av *= 0.95 + var * r.nextDouble();
                     bv *= 0.95 + var * r.nextDouble();
                     cv *= 0.95 + var * r.nextDouble();
                     dv *= 0.95 + var * r.nextDouble();
 
                     double S = av + bv + cv + dv;
                     av = av / S;
                     bv = bv / S;
                     cv = cv / S;
                     dv = dv / S;
 
                     // Choose partition
                     p = r.nextDouble();
                     if ( p < av )
                     {
                         // Do nothing
                     }
                     else if ( (p >= av) && (p < av + bv) )
                     {
                         v += step;
                     }
                     else if ( (p >= av + bv) && (p < av + bv + cv) )
                     {
                         u += step;
                     }
                     else
                     {
                         u += step;
                         v += step;
                     }
                 }
 
                 if ( u != v )
                 {
                     edges++;
                     long k = u;
                     k <<= 32;
                     k += v;

                         Node head = graphdb.getNodeById( u + offsetid );
                         Node tail = graphdb.getNodeById( v + offsetid );
 
                        if (!relationShipExistsFrom(head, tail))
                        {
                            Relationship rel = head.createRelationshipTo( tail, relType );
                            rel.setProperty( "v", (1 + r.nextInt( (int) N )) );
                        }
 
                     if ( (edges % flushInterval) == 0 )
                     {
                         tx.success();
                         tx.finish();
                         tx = graphdb.beginTx();
                     }
 
                 }
             }
             tx.success();
         } finally
         {
             tx.finish();
         }
 
         return returnNodes;
     }
 
    private boolean relationShipExistsFrom( Node head, Node tail )
    {
        boolean relationshipDoesExist = false;
        for (Relationship rel : head.getRelationships())
        {
            if (rel.getEndNode().equals( tail ))
            {
                relationshipDoesExist = true;
                break;
            }
        }
        return relationshipDoesExist;
    }

 }
