 package wtfdb.core.data;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class DataMap extends Data<Map<String, Data<?>>> implements Iterable<Entry<String, Data<?>>>
 {
     public DataMap()
     {
         super(new LinkedHashMap<String, Data<?>>());
     }
 
     @SuppressWarnings("unchecked")
     private <T> T get(String k)
     {
         Data<T> v = (Data<T>) value.get(k);
         
         if (v == null) return null;
         else return v.value;
     }
 
     @Override
     public void accept(DataVisitor visitor)
     {
         visitor.visit(this);
     }
     
     public void clear()
     {
         value.clear();
     }
     
     @Override
     public boolean equals(Object o)
     {
         if (o == this) return true;
         if (!(o instanceof DataMap)) return false;
         
         DataMap that = (DataMap) o;
         
         return this.value.equals(that.value);
     }
 
     public Boolean getBoolean(String k)
     {
         return get(k);
     }
 
     public Byte getByte(String k)
     {
         return get(k);
     }
 
     public Short getShort(String k)
     {
         return get(k);
     }
     
     public Integer getInteger(String k)
     {
         return get(k);
     }
     
     public Long getLong(String k)
     {
         return get(k);
     }
     
     public Float getFloat(String k)
     {
         return get(k);
     }
     
     public Double getDouble(String k)
     {
         return get(k);
     }
     
     public Character getChar(String k)
     {
         return get(k);
     }
     
     public String getString(String k)
     {
         return get(k);
     }
     
     public byte[] getByteArray(String k)
     {
         return get(k);
     }
     
     public Date getDate(String k)
     {
         return get(k);
     }
     
     public DataArray getDataArray(String k)
     {
         return (DataArray) value.get(k);
     }
     
     public DataMap getDataMap(String k)
     {
         return (DataMap) value.get(k);
     }
     
     public void remove(String k)
     {
         value.remove(k); 
     }
 
     public void set(String k, Data<?> v)
     {
        v.parent = v;
         value.put(k, v);
     }
 
     public void set(String k, boolean v)
     {
         set(k, new DataBoolean(v));
     }
 
     public void set(String k, byte v)
     {
         set(k, new DataByte(v));
     }
 
     public void set(String k, short v)
     {
         set(k, new DataShort(v));
     }
 
     public void set(String k, int v)
     {
         set(k, new DataInteger(v));
     }
 
     public void set(String k, long v)
     {
         set(k, new DataLong(v));
     }
 
     public void set(String k, float v)
     {
         set(k, new DataFloat(v));
     }
 
     public void set(String k, double v)
     {
         set(k, new DataDouble(v));
     }
 
     public void set(String k, char v)
     {
         set(k, new DataChar(v));
     }
 
     public void set(String k, String v)
     {
         set(k, new DataString(v));
     }
 
     public void set(String k, byte[] v)
     {
         set(k, new DataByteArray(v));
     }
 
     public void set(String k, Date v)
     {
         set(k, new DataDate(v));
     }
 
     public void set(String k, DataArray v)
     {
         v.parent = this;
         this.value.put(k, v);
     }
 
     public void set(String k, DataMap v)
     {
         v.parent = this;
         this.value.put(k, v);
     }
 
     public int size()
     {
         return value.size();
     }
     
     @Override
     public String toString()
     {
         return value == null? "{}" : value.toString();  
     }
 
     @Override
     public Iterator<Entry<String, Data<?>>> iterator()
     {
         return value.entrySet().iterator();
     }
 }
