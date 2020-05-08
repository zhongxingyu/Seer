 /**
  * 
  */
 package org.esa.beam.dataViewer3D.data.source;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Data source producing random data. Mainly for development purposes.
  * 
  * @author Martin Pecka
  * @param <N>
  */
 public abstract class RandomDataSource<N extends Number> extends AbstractDataSource<N> implements DataSource<N>
 {
 
     protected final List<N> data; // TODO change to a list that can have more than MAXINT items
     protected final int     size;
     protected final N       min, max;
 
    public RandomDataSource(int size, N min, N max)
     {
         data = new ArrayList<N>(size);
         this.size = size;
         this.min = min;
         this.max = max;
     }
 
     @Override
     public Iterator<N> iterator()
     {
         return new Iterator<N>() {
             private int i = 0;
 
             @Override
             public boolean hasNext()
             {
                return i < size - 1;
             }
 
             @Override
             public N next()
             {
                if (data.size() - 1 < i || data.get(i) == null)
                     data.add(getRandomValue());
                 return data.get(i++);
             }
 
             @Override
             public void remove()
             {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     protected abstract N getRandomValue();
 
     @Override
     public long size()
     {
         return size;
     }
 
 }
