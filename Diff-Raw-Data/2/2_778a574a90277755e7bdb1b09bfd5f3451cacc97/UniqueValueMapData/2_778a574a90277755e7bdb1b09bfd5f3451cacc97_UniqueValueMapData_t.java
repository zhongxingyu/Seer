 package net.avh4.rpg.maptoolkit.data;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import net.avh4.util.Point;
 
 public class UniqueValueMapData<T> extends MapDataBase<T> {
     BiMap<Point, T> values = HashBiMap.create();
 
     public UniqueValueMapData(String name, int w, int h) {
        super(name, w, h);
     }
 
     @Override
     public T getData(int x, int y) {
         return getData(new Point(x, y));
     }
 
     @Override
     public T getData(Point p) {
         return values.get(p);
     }
 
     @Override
     public void setData(int x, int y, T value) {
         setData(new Point(x, y), value);
     }
 
     @Override
     public void setData(Point p, T value) {
         values.put(p, value);
     }
 
     @SuppressWarnings("ChainedMethodCall")
     public Point find(T value) {
         return values.inverse().get(value);
     }
 }
