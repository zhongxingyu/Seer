 package org.misera.android.cavenav.graph;
 
 import java.util.Comparator;
 
 public class VertexComparator implements Comparator<Vertex> {
 	
     public int compare(Vertex v1, Vertex v2) {
        return v1.f > v2.f ? 1 : -1;
     }
 }
