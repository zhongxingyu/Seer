 package pt.isel.adeetc.meic.pdm.common;
 
 public class GenericEventArgs<T> implements IEventHandlerArgs<T>
 {
     private final T _data;
     private final Exception _error;
 
     public GenericEventArgs(T result, Exception error)
     {
 
         this._data = result;
         _error = error;
     }
 
     public Exception getError()
     {
         return _error;
     }
 
     public T getData() throws Exception
     {
 
        if(_error != null)
             throw _error;
 
         return _data;
     }
 
     public boolean errorOccurred()
     {
         return _error != null;
     }
 }
