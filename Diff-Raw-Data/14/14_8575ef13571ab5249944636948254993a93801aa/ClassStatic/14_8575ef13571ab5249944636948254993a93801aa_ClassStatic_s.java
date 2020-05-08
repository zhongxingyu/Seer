 package demo.edge.java.lang;
 
 import au.net.netstorm.boost.nursery.autoedge.StaticEdge;
 
public interface ClassStatic<T> extends StaticEdge<java.lang.Class<T>> {
     Class<?> forName(String name);
 }
