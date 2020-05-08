 package net.jonp.sorm;
 
 /**
  * A wrapper around {@link Iterable} to return a {@link SormIterator}.
  * 
  * @param <T> The type of data returned by the {@link SormIterator}.
  */
 public interface SormIterable<T>
     extends Iterable<T>
 {
    @Override
     public SormIterator<T> iterator();
 }
