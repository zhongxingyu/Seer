 package demo.edge.java.lang;
 
 import au.net.netstorm.boost.nursery.autoedge.StaticEdge;
 
public interface ClassStatic extends StaticEdge<java.lang.Class<?>> {
     Class<?> forName(String name);
 }
