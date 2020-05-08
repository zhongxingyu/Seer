 package net.shrine.webclient.shared.domain;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.codehaus.jackson.annotate.JsonCreator;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import net.shrine.webclient.client.util.Util;
 
 /**
  * 
  * @author clint
  * @date Mar 23, 2012
  */
 public final class Term implements Andable {
     @JsonProperty
     private final String path;
 
     @JsonProperty
     private final String category;
 
     @JsonProperty
     private final String simpleName;
 
     public Term(final String value, final String category) {
         this(value, category, "");
     }
 
     @JsonCreator
    public Term(@JsonProperty("path") final String path, @JsonProperty("category") final String category, @JsonProperty("simpleName") final String simpleName) {
         super();
 
        Util.requireNotNull(path);
         Util.requireNotNull(category);
         Util.requireNotNull(simpleName);
 
        this.path = path;
         this.category = category;
         this.simpleName = simpleName;
     }
 
     @Override
     public Collection<Term> getTerms() {
         return Collections.singletonList(this);
     }
 
     @Override
     public String toString() {
         return "Term(" + category + ":'" + path + "')";
     }
 
     @Override
     public String toXmlString() {
         return "<term>" + path + "</term>";
     }
 
     public String getPath() {
         return path;
     }
 
     public String getCategory() {
         return category;
     }
 
     public String getSimpleName() {
         return simpleName;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (path == null ? 0 : path.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Term other = (Term) obj;
         if (path == null) {
             if (other.path != null) {
                 return false;
             }
         } else if (!path.equals(other.path)) {
             return false;
         }
         return true;
     }
 
 }
