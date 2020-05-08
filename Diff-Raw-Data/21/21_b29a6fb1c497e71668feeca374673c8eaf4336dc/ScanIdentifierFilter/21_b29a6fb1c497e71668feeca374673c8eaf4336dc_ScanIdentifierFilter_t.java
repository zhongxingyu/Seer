 package org.apache.myfaces.scripting.core.dependencyScan.filter;
 
 import org.apache.myfaces.scripting.core.dependencyScan.api.ClassFilter;
 
 /**
  * a filter which works on the scan identifiers
  * only classes which trigger on the same identifier
  * are allowed to be passed through
  */
 public class ScanIdentifierFilter implements ClassFilter {
 
    private final int _engineType;
 
    public ScanIdentifierFilter(int engineType) {
         _engineType = engineType;
     }
 
     public boolean isAllowed(Integer identifier, String clazz) {
        return _engineType == identifier.intValue();  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
