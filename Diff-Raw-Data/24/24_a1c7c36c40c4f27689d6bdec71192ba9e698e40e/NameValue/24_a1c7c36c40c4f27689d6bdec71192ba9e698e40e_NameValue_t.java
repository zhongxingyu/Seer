 package org.stringtree.http;
 
 public class NameValue {
 
    protected final String name;
    protected final String value;
 
     public NameValue(String name, String value) {
         this.name = name;
         this.value = value;
     }
 
     public String getName() {
         return name;
     }
 
     public String getValue() {
         return value;
     }
     
     public String toString() {
     	return name + ": " + value;
     }

    public boolean equals(Object other) {
        if (!(other instanceof NameValue)) return false;
        return this.name.equals(((NameValue)other).name);
    }

	@Override public int hashCode() {
		return ("NV:" + name).hashCode();
	}
 }
