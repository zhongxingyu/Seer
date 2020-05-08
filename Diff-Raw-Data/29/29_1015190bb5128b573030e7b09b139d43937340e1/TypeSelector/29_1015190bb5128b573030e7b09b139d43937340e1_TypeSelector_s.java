 package net.sf.beezle.ssass.scss;
 
 public class TypeSelector implements BaseSelector {
    public static final TypeSelector UNIVERSAL = new TypeSelector(null, "*");
     public static final String NAMESPACE_STAR = "*";
 
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
