 package com.irr310.i3d;
 
 public class Bundle {
 
     private Object obj;
 
     public Bundle(Object obj) {
         this.obj = obj;
     }
 
     public Object getObject() {
         return obj;
     }
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((obj == null) ? 0 : obj.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bundle other = (Bundle) obj;
        if (this.obj == null) {
            if (other.obj != null)
                return false;
        } else if (!this.obj.equals(other.obj))
            return false;
        return true;
    }
 }
