 package org.apache.myfaces.scripting.core.dependencyScan.filter;
 
 import org.apache.myfaces.scripting.core.dependencyScan.api.ClassFilter;
 
 /**
  * a filter which works on the scan identifiers
  * only classes which trigger on the same identifier
  * are allowed to be passed through
  */
 public class ScanIdentifierFilter implements ClassFilter {
 
    private final Integer _engineType;
 
    public ScanIdentifierFilter(Integer engineType) {
         _engineType = engineType;
     }
 
     public boolean isAllowed(Integer identifier, String clazz) {
        return _engineType.equals(identifier);  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
