 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author user
  */
class ADTreeHelper {
     protected final int m; // The number of attributes
     protected final int[] airities;
     
     protected ADTreeHelper( int m ){
         this.m = m;
         airities = new int[m];
     }
     
     protected int count( int[] assignment, CountNode ptr ){
         
         if( null == ptr ) return 0;
         
         for( int i = ptr.attr-1; i >= 0 && ptr != null; i-- ){
             VaryNode vary = ptr.vary[i];
             if( assignment[i] >= 0 ){
                 if( assignment[i] == vary.mcv ){
                     int[] a = new int[m];
                     System.arraycopy(assignment, 0, a, 0, m);
                     a[i] = -1;
                     int count = count( a, ptr );
                     for( int v = 0; v < vary.values.length; v++ ) if( v != vary.mcv ){
                         a[i] = v;
                         count -= count( a, ptr );
                     }
                     return count;
                 }else
                     ptr = vary.values[assignment[i]];
             }
         }
         
         return null == ptr ? 0 : ptr.count;
     }
     
    protected class CountNode {
         private final int attr;
         protected final int count;
         protected final VaryNode[] vary;
         
         protected CountNode( final int attribute, final int[][] array ){
             attr = attribute;
             count = array.length;
             vary = new VaryNode[attr];
             for( int i=0; i<attr; i++ )
                 vary[i] = new VaryNode( i, array );
         }
     }
     
    protected class VaryNode{
         protected final CountNode[] values;
         protected int mcv = -1;
         
         private VaryNode( final int attr, final int[][] array ){
             
             final int airity = airities[attr];
             
             values = new CountNode[airity];
             
             List<List<Integer>> childArrayIndexes = new ArrayList<>(airity);
             for( int i=0; i<airity; i++ )
                 childArrayIndexes.add( new ArrayList<Integer>() );
             
             for( int r=0; r<array.length; r++ )
                 childArrayIndexes.get( array[r][attr] ).add(r);
             
             int maxCount = 0;
             for( int i=0; i<airity; i++ ){
                 int count = childArrayIndexes.get(i).size();
                 if( count > maxCount ){
                     maxCount = count;
                     mcv = i;
                 }
             }
             
             for( int i=0; i<airity; i++ ) if( i != mcv ){
                 List<Integer> indexes = childArrayIndexes.get(i);
                 if( indexes.size() > 0 ){
                     int[][] childArray = new int[indexes.size()][];
                     int j=0;
                     for( int index : indexes )
                         childArray[j++] = array[index];
                     
                     values[i] = new CountNode( attr, childArray );
                 }
             }
         }
     }
 
 }
