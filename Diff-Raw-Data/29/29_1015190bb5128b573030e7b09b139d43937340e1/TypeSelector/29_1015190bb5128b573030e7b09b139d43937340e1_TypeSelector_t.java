 package net.sf.beezle.ssass.scss;
 
 public class TypeSelector implements BaseSelector {
     public static final String NAMESPACE_STAR = "*";
    public static final String NAMESPACE_EMPTY = "";

    public static TypeSelector universal(String namespace) {
        return new TypeSelector(namespace, "*");
    }
 
     private final String namespace;
     private final String elementName;
 
     public TypeSelector(String one, String two) {
         if (two == null) {
             this.namespace = null;
             this.elementName = one;
         } else {
             this.namespace = one;
             this.elementName = two;
         }
     }
 
     @Override
     public void toCss(Output output) {
         if (namespace != null) {
             output.string(namespace);
             output.string("|");
         }
         output.string(elementName);
     }
 }
