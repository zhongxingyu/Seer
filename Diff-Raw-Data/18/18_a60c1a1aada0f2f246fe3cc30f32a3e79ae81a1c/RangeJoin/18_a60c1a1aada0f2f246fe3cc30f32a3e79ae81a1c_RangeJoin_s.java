 package com.yahoo.dtf.range;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.yahoo.dtf.range.Range;
 import com.yahoo.dtf.range.RangeJoin;
 import com.yahoo.dtf.exception.RangeException;
 import com.yahoo.dtf.logger.DTFLogger;
 
 public class RangeJoin extends Range {
     
     private DTFLogger _logger = DTFLogger.getLogger(RangeJoin.class);
    
     private ArrayList<Range> _ranges = null;
     private String[] elems = null;
     
     public RangeJoin() {
         _ranges = new ArrayList<Range>();
     }
     
     public synchronized void addRange(Range range)  { 
         _ranges.add(range);
     }
 
     public boolean hasMoreElements() {
         boolean hasNoMore = true;
        
         for (int i = 0; i < _ranges.size(); i++) 
             hasNoMore &= !_ranges.get(i).hasMoreElements();
         
         return !hasNoMore;
     }
 
     /**
      * Push the range group up by 1 starting from the least significant range
      * 
      * @param index
      */
     private void nextElement(int index) throws RangeException { 
         Range range = (Range) _ranges.get(index);
 
         if (!range.hasMoreElements()) { 
             if (index != 0) {
                 range.reset();
                 nextElement(index-1);
             } 
         }
       
         elems[index] = range.nextElement();
     }
     
     public String nextElement() throws RangeException {
         if (elems == null) { 
             elems = new String[_ranges.size()];
             // initialize
             for (int i = 0; i < _ranges.size(); i++) {
                 if ( _ranges.get(i).hasMoreElements() ) {
                     elems[i] = _ranges.get(i).nextElement();
                 }
             }
         } else  {
             nextElement(_ranges.size()-1);
         }
         
         StringBuffer result = new StringBuffer();
         for (int i = 0; i < _ranges.size(); i++) { 
             result.append(elems[i]);
         }
         
         return result.toString();
     }
 
     public void reset() throws RangeException {
          for (int i = 0; i < _ranges.size(); i++)
             ((Range)_ranges.get(i)).reset();
     }
     
     public int size() {
        int result = 0;
        
        for (int i = 0; i < _ranges.size(); i++)
            result += ((Range) _ranges.get(i)).size();
         
         return result;
     }
     
     @Override
     public void restoreState(DataInputStream dis) throws RangeException {
         try { 
             int ranges = dis.readInt();
             _ranges = new ArrayList<Range>();
             
             for (int i = 0; i < ranges; i++) { 
                 String classname = dis.readUTF();
 	            Object obj = Class.forName(classname).newInstance();
     	            
 	            if ( !(obj instanceof Range) )  {
 	                throw new RangeException("Serious issue, this isn't even a Range ["
 	                                         + obj.getClass().getName() + "]");
 	            }
     	        Range range = (Range)obj;
     	        range.restoreState(dis);
 	            _ranges.add(range);
             }
         } catch (InstantiationException e) {
             throw new RangeException("Error restoring range.",e);
         } catch (IllegalAccessException e) {
             throw new RangeException("Error restoring range.",e);
         } catch (ClassNotFoundException e) {
             throw new RangeException("Error restoring range.",e);
         } catch (IOException e) {
             throw new RangeException("Error restoring range.",e);
         }
     }
     
     @Override
     public void suspendState(DataOutputStream dos) throws RangeException {
         try { 
 	        dos.writeInt(_ranges.size());
 	        for (int i = 0; i < _ranges.size(); i++) {
 	            Range range = _ranges.get(i);
 	            dos.writeUTF(range.getClass().getName());
 	            _ranges.get(i).suspendState(dos);
 	        }
 	        
 	        if ( elems == null ) { 
 	            dos.writeInt(0);
 	        } else { 
 		        dos.writeInt(elems.length);
 		        for (int i = 0; i < elems.length; i++)
 		            dos.writeUTF(elems[i]);
 	        }
         } catch (IOException e) { 
             throw new RangeException("Error suspending range.",e);
         }
     }
 }
