 package uk.co.sromo.blister;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniel
  * Date: 10-Aug-2010
  * Time: 21:45:45
  * To change this template use File | Settings | File Templates.
  */
 public class BPDict extends BPExpandableItem implements Map<BPString, BPItem> {
     private final int[] keyOffsets;
     private final int[] valueOffsets;
 
     private final Map<BPString, BPItem> map = new LinkedHashMap<BPString, BPItem>();
 
     BPDict(int[] keyOffsets, int[] valueOffsets) {
         this.keyOffsets = keyOffsets;
         this.valueOffsets = valueOffsets;
     }
 
     public BPDict() {
         keyOffsets = EMPTY;
         valueOffsets = EMPTY;
     }
 
     @Override
     protected void doExpand(BinaryPlistDecoder decoder) throws BinaryPlistException {
         for (int i=0; i< keyOffsets.length; i++) {
             int keyOffset = keyOffsets[i];
             int valueOffset = valueOffsets[i];
             BPItem key = decoder.getItemAtIndex(keyOffset);
             if (key.getType() != Type.String) {
                 throw new BinaryPlistException("Dictionary key wasn't a string, was " + key.getType());
             }
             BPItem value = decoder.getItemAtIndex(valueOffset);
             map.put((BPString)key, value);
             log.debug(key.toString() + " = " + value.toString());
         }
     }
 
     public BPDict with(BPString key, BPItem value) {
         put(key, value);
         return this;
     }
 
     public BPDict with(String key, BPItem value) {
         return with(BPString.get(key), value);
     }
 
     public BPDict with(String key, String value) {
         return with(key, BPString.get(value));
     }
 
     public BPDict with(String key, int value) {
         return with(key, BPInt.get(value));
     }
 
     public BPDict with(String key, boolean value) {
         return with(key, BPBoolean.get(value));
     }
 
     public <S extends Enum<S>> BPDict with(S key, BPItem value) {
         return with(key.toString(), value);
     }
 
     public <T extends Enum<T>> BPDict with(String key, Enum<T> value) {
         return with(key, value.toString());
     }
 
     public <S extends Enum<S>> BPDict with(S key, String value) {
         return with(key.toString(), BPString.get(value));
     }
 
     public <S extends Enum<S>> BPDict with(S key, int value) {
         return with(key.toString(), BPInt.get(value));
     }
 
     public <S extends Enum<S>> BPDict with(S key, boolean value) {
         return with(key.toString(), BPBoolean.get(value));
     }
 
     public <S extends Enum<S>, T extends Enum<T>> BPDict with(S key, Enum<T> value) {
         return with(key, value.toString());
     }
 
     public BPDict with(BPString key, String value) {
         return with(key, BPString.get(value));
     }
 
     public BPDict with(BPString key, int value) {
         return with(key, BPInt.get(value));
     }
 
     public BPDict with(BPString key, boolean value) {
         return with(key, BPBoolean.get(value));
     }
 
     public <T extends Enum<T>> BPDict with(BPString key, Enum<T> value) {
         return with(key, value.toString());
     }
 
     public String get(String key, String fallback) throws BinaryPlistException {
         BPString bpKey = new BPString(key);
         if (!containsKey(bpKey)) return fallback;
         BPItem value = get(bpKey);
         if (value.getType() != BPItem.Type.String) {
             throw new BinaryPlistException("Not a string");
         }
         return ((BPString) value).getValue();
     }
 
     public int get(BPString bpKey, int fallback) throws BinaryPlistException {
         if (!containsKey(bpKey)) return fallback;
         BPItem value = get(bpKey);
         if (value.getType() != BPItem.Type.Int) {
             throw new BinaryPlistException("Not an int");
         }
         return ((BPInt) value).getValue();
     }
 
     public boolean get(BPString bpKey, boolean fallback) throws BinaryPlistException {
         if (!containsKey(bpKey)) return fallback;
         BPItem value = get(bpKey);
         if (value.getType() != BPItem.Type.Boolean) {
             throw new BinaryPlistException("Not a boolean");
         }
         return ((BPBoolean) value).getValue();
     }
 
     public double get(BPString bpKey, double fallback) throws BinaryPlistException {
         if (!containsKey(bpKey)) return fallback;
         BPItem value = get(bpKey);
         if (value.getType() != BPItem.Type.Real) {
             throw new BinaryPlistException("Not a real");
         }
         return ((BPReal) value).getValue();
     }
 
     public <T extends Enum<T>> T get(BPString bpKey, T fallback) throws BinaryPlistException {
         if (!containsKey(bpKey)) return fallback;
         BPItem value = get(bpKey);
         if (value.getType() != BPItem.Type.String) {
             throw new BinaryPlistException("Not a string");
         }
         return Enum.valueOf(fallback.getDeclaringClass(), ((BPString)value).getValue());
     }
 
     public String get(BPString bpKey, String fallback) throws BinaryPlistException {
         if (!containsKey(bpKey)) return fallback;
         BPItem value = get(bpKey);
         if (value.getType() != BPItem.Type.String) {
             throw new BinaryPlistException("Not a string");
         }
         return ((BPString) value).getValue();
     }
 
     public int get(String key, int fallback) throws BinaryPlistException {
         return get(new BPString(key), fallback);
     }
 
     public boolean get(String key, boolean fallback) throws BinaryPlistException {
         return get(new BPString(key), fallback);
     }
 
     public double get(String key, double fallback) throws BinaryPlistException {
         return get(new BPString(key), fallback);
     }
 
     public <T extends Enum<T>> T get(String key, T fallback) throws BinaryPlistException {
         return get(new BPString(key), fallback);
     }
 
     public <S extends Enum<S>> String get(S key, String fallback) throws BinaryPlistException {
         return get(key.toString(), fallback);
     }
 
     public <S extends Enum<S>> int get(S key, int fallback) throws BinaryPlistException {
         return get(key.toString(), fallback);
     }
 
     public <S extends Enum<S>> boolean get(S key, boolean fallback) throws BinaryPlistException {
         return get(key.toString(), fallback);
     }
 
     public <S extends Enum<S>> double get(S key, double fallback) throws BinaryPlistException {
         return get(key.toString(), fallback);
     }
 
     public <S extends Enum<S>, T extends Enum<T>> T get(S key, T fallback) throws BinaryPlistException {
         return get(key.toString(), fallback);
     }
 
     public void clear() {
         map.clear();
     }
 
     public boolean containsKey(Object key) {
         return containsKey(key.toString());
     }
 
     public boolean containsKey(String key) {
         return containsKey(new BPString(key));
     }
 
     public <S extends Enum> boolean containsKey(S key) {
        return containsKey(key.toString());
     }
 
     public boolean containsKey(BPString key) {
         return map.containsKey(key);
     }
 
     public boolean containsValue(Object value) {
         return map.containsValue(value);
     }
 
     public Set<Entry<BPString, BPItem>> entrySet() {
         return map.entrySet();
     }
 
     @Override
     public boolean equals(Object o) {
         return map.equals(o);
     }
 
     public BPItem get(Object key) {
         return get(key.toString());
     }
 
     public <S extends Enum> BPItem get(S key) {
         return get(key.toString());
     }
 
     public BPItem get(String key) {
         return get(new BPString(key));
     }
 
     public BPItem get(BPString key) {
         return map.get(key);
     }
 
     @Override
     public int hashCode() {
         return map.hashCode();
     }
 
     public boolean isEmpty() {
         return map.isEmpty();
     }
 
     public Set<BPString> keySet() {
         return map.keySet();
     }
 
     public BPItem put(BPString key, BPItem value) {
         return map.put(key, value);
     }
 
     public void putAll(Map<? extends BPString, ? extends BPItem> m) {
         map.putAll(m);
     }
 
     public BPItem remove(Object key) {
         return remove(key.toString());
     }
 
     public <S extends Enum> BPItem remove(S key) {
         return remove(key.toString());
     }
 
     public BPItem remove(String key) {
         return remove(new BPString(key));
     }
 
     public BPItem remove(BPString key) {
         return map.remove(key);
     }
 
     public int size() {
         return map.size();
     }
 
     public Collection<BPItem> values() {
         return map.values();
     }
 
     @Override
     public String toString() {
         return "BPDict{" +
                 "map=" + map +
                 '}';
     }
 
     @Override
     public Type getType() {
         return Type.Dict;
     }
 
     @Override
     public void accept(BPVisitor visitor) {
         visitor.visit(this);
     }
 
 }
