 /*
     This file is part of the object-browser.
 
     The object-browser is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     The object-browser is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with the object-browser.  If not, see <http://www.gnu.org/licenses/>.
 
     Copyright 2009 Johannes Rudolph
 */
 
 package net.virtualvoid.android.browser;
 
 import java.io.File;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.virtualvoid.android.browser.ObjectBrowser.HistoryItem;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class ItemFactory {
     static abstract class MappedItemList<T> implements ItemList{
         private CharSequence name;
 
         public MappedItemList(CharSequence name) {
             this.name = name;
         }
 
         protected abstract T getOriginal(int position);
         protected abstract Item map(T object);
         @Override
         public Item get(int position) {
             return map(getOriginal(position));
         }
         @Override
         public CharSequence getName() {
             return name;
         }
         /*@Override
         public Item byPathSegment(String str) {
             try{
                 return map(getOriginal(Integer.valueOf(str)));
             } catch(NumberFormatException e){
                 return null;
             }
         }*/
     }
     static abstract class MappedRAItemList<T> extends MappedItemList<T>{
         private RA<T> list;
         public MappedRAItemList(CharSequence name,RA<T> list){
             super(name);
             this.list = list;
         }
         @Override
         public int size() {
             return list.size();
         }
         @Override
         protected T getOriginal(int pos) {
             return list.get(pos);
         }
     }
     static abstract class MappedListItemList<T> extends MappedItemList<T>{
         private List<T> items;
 
         public MappedListItemList(String name, List<T> items) {
             super(name);
             this.items = items;
         }
 
         @Override
         public int size() {
             return items.size();
         }
         @Override
         protected T getOriginal(int index) {
             return items.get(index);
         }
     }
     static abstract class MappedArrayItemList<T> extends MappedItemList<T>{
         private T[] items;
 
         public MappedArrayItemList(String name, T[] items) {
             super(name);
             this.items = items;
         }
         @Override
         public int size() {
             return items.length;
         }
         @Override
         protected T getOriginal(int arg0) {
             return items[arg0];
         }
     }
     private static Item findByPath(ItemList list,String path){
         int len = list.size();
         for (int i=0;i<len;i++) {
             Item item = list.get(i);
             if (item.getPath().equals(path))
                 return item;
         }
         return null;
     }
     private static ItemList fromArray(final String name,final Item...is){
         return new MappedArrayItemList<Item>(name,is){
             @Override
             protected Item map(Item item) {
                 return item;
             }
             @Override
             public Item byPathSegment(String str) {
                 return findByPath(this, str);
             }
         };
     }
     private static ItemList join(final String name,final ItemList list1,final ItemList list2){
         return new ItemList(){
             @Override
             public Item get(int position) {
                 int len1 = list1.size();
                 if (position < len1)
                     return list1.get(position);
                 else
                     return list2.get(position-len1);
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public int size() {
                 return list1.size()+list2.size();
             }
             @Override
             public Item byPathSegment(String str) {
                 Item i = list1.byPathSegment(str);
                 return i != null ? i : list2.byPathSegment(str);
             }
         };
     }
 
     private static Item materialize(final MetaItem metaItem,final Object o){
         assert metaItem != null;
         return new Item(){
             @Override
             public Object get() {
                 return metaItem.get(o);
             }
             @Override
             public CharSequence getName() {
                 return metaItem.getName();
             }
             @Override
             public Class<?> getReturnType() {
                 return metaItem.getReturnType();
             }
             @Override
             public String getPath() {
                 return metaItem.getPath();
             }
         };
     }
     private static ItemList materialize(final MetaItemList metaList,final Object o){
         return new MappedRAItemList<MetaItem>(metaList.getName(),metaList){
             @Override
             protected Item map(MetaItem arg0) {
                 return materialize(arg0, o);
             }
             @Override
             public Item byPathSegment(String path) {
                 MetaItem meta = metaList.byPathSegment(path);
                 return meta != null ? map(meta) : null;
             }
         };
     }
     private final static ItemList emptyList = new ItemList(){
         @Override
         public String getName() {
             return "<empty>";
         }
         @Override
         public int size() {
             return 0;
         }
         @Override
         public Item get(int position) {
             throw new NoSuchElementException("no such position "+position);
         }
         @Override
         public Item byPathSegment(String str) {
             return null;
         }
     };
     private static ItemList singleton(final String name,final String path,final Object o){
         final Item item = single(name,path,o);
         return o == null ? emptyList : new ItemList(){
             @Override
             public Item get(int position) {
                 if (position == 0)
                     return item;
                 else
                     throw new NoSuchElementException("At position "+position);
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public int size() {
                 return 1;
             }
             @Override
             public Item byPathSegment(String str) {
                 return path.equals(str) ? item : null;
             }
         };
     }
     private static Item single(final String name,final String path,final Object o){
         return new Item(){
             @Override
             public Object get() {
                 return o;
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public Class<?> getReturnType() {
                 return o.getClass();
             }
             @Override
             public String getPath() {
                 return path;
             }
         };
     }
     private static ItemList elementsOfArray(final Object array){
         final int len = Array.getLength(array);
         final Item[] items = new Item[len];
         return new ItemList(){
             @Override
             public Item get(final int index) {
                 Item item = items[index];
                 if (item == null){
                     item = new Item(){
                         @Override
                         public Object get() {
                             return Array.get(array, index);
                         }
                         @Override
                         public String getName() {
                             return Integer.toString(index);
                         }
                         @Override
                         public Class<?> getReturnType() {
                             Object val = get();
                             return val!=null ? val.getClass() : array.getClass().getComponentType();
                         }
                         @Override
                         public String getPath() {
                             return Integer.toString(index);
                         }
                     };
                     items[index] = item;
                 }
                 return item;
             }
             @Override
             public String getName() {
                 return "Elements";
             }
             @Override
             public int size() {
                 return len;
             }
             @Override
             public Item byPathSegment(String str) {
                 try{
                     return get(Integer.valueOf(str));
                 } catch(NumberFormatException e){
                     return null;
                 }
             }
         };
     }
     private static ItemList elementsOfMap(final Map<?,?> map){
         final ArrayList<Object> keys = new ArrayList<Object>();
         for (Object o:map.keySet())
             keys.add(o);
         return new MappedListItemList<Object>("Values",keys){
             @Override
             protected Item map(final Object key) {
                 return new Item(){
                     @Override
                     public Object get() {
                         return map.get(key);
                     }
                     @Override
                     public String getName() {
                         return ItemFactory.toString(key);
                     }
                     @Override
                     public Class<?> getReturnType() {
                         Object val = get();
                         return val!=null ? val.getClass() : Object.class;
                     }
                     @Override
                     public String getPath() {
                         return ItemFactory.toString(key);
                     }
                 };
             }
             @Override
             public Item byPathSegment(String str) {
                 Object key = map.get(str);
                 if (key != null)
                     return map(key);
                 else {
                     for (Object k:keys){
                         Item i = map(k);
                         if (i.getPath().equals(str))
                             return i;
                     }
                     return null;
                 }
             }
         };
     }
     private static final int MAX_ELEMENTS = 100;
     private static ItemList elementsOfIterable(final Iterable<?> i){
         final ArrayList<Object> res = new ArrayList<Object>(MAX_ELEMENTS/2);
 
         int num = 0;
         for (Object o:i)
             if (num < MAX_ELEMENTS){
                 res.add(o);
                 num++;
             }
             else
                 break;
         final int numElements = num;
         return join("Elements"
                    ,singleton("Size","size",num >= MAX_ELEMENTS?">= "+MAX_ELEMENTS:num)
                    ,new ItemList(){
                         @Override
                         public Item get(final int position) {
                             return new Item(){
                                 @Override
                                 public Object get() {
                                     return res.get(position);
                                 }
                                 @Override
                                 public String getName() {
                                     return Integer.toString(position);
                                 }
                                 @Override
                                 public Class<?> getReturnType() {
                                     Object val = get();
                                     return val!=null ? val.getClass() : Object.class;
                                 }
                                 @Override
                                 public String getPath() {
                                     return getName();
                                 }
                             };
                         }
                         @Override
                         public String getName() {
                             return "Elements";
                         }
                         @Override
                         public int size() {
                             return numElements;
                         }
                         @Override
                         public Item byPathSegment(String str) {
                             try {
                                 return get(Integer.valueOf(str));
                             } catch (NumberFormatException nfe){
                                 return null;
                             }
                         }
                    });
     }
     private static ItemList contentsOfDirectory(final File dir){
         return join(
                 "Contents",
                 singleton("..","parent",dir.getParentFile())
                 ,new MappedArrayItemList<File>("Contents",dir.listFiles()){
                     @Override
                     protected Item map(final File f) {
                         return new Item(){
                             @Override
                             public Object get() {
                                 return f;
                             }
                             @Override
                             public String getName() {
                                 return f.getName();
                             }
                             @Override
                             public Class<?> getReturnType() {
                                 return File.class;
                             }
                             @Override
                             public String getPath() {
                                 return f.getName();
                             }
                         };
                     }
                     @Override
                     public Item byPathSegment(String str) {
                         File f = new File(dir.getAbsolutePath()+File.separator+str);
                         return f.exists() ? map(f) : null;
                     }
                 });
     }
     private static ItemList tablesOfDb(final String dbPath){
         final SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
         final Cursor cursor = db.rawQuery("select name from sqlite_master where type='table'", null);
         return new MappedItemList<String>("Tables"){
             @Override
             protected String getOriginal(int position) {
                 cursor.moveToPosition(position);
                 return cursor.getString(0);
             }
             @Override
             protected Item map(final String table) {
                 return new Item(){
                     @Override
                     public Object get() {
                         return db.rawQuery("select * from "+table,null);
                     }
                     @Override
                     public CharSequence getName() {
                         return table;
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return Cursor.class;
                     }
                     @Override
                     public String getPath() {
                         return table;
                     }
                 };
             }
             @Override
             public int size() {
                 return cursor.getCount();
             }
             @Override
             public Item byPathSegment(String str) {
                 return findByPath(this, str);
             }
         };
     }
     private static ItemList resultsOfQuery(final Cursor cursor){
         return new ItemList(){
             @Override
             public Item get(final int position) {
                 return new Item(){
                     @Override
                     public Object get() {
                         cursor.moveToPosition(position);
                         return DBTools.rowAsMap(cursor);
                     }
                     @Override
                     public CharSequence getName() {
                         return Integer.toString(position);
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return Map.class;
                     }
                     @Override
                     public String getPath() {
                         return Integer.toString(position);
                     }
                 };
             }
             @Override
             public CharSequence getName() {
                 return "Results";
             }
             @Override
             public int size() {
                 return cursor.getCount();
             }
             @Override
             public Item byPathSegment(String str) {
                 try{
                     return get(Integer.valueOf(str));
                 } catch(NumberFormatException nfe){
                     return null;
                 }
             }
         };
     }
     private static MappedListItemList<PackageInfo> packagesFromPM(final PackageManager pm) {
         return new MappedListItemList<PackageInfo>("Installed Packages",pm.getInstalledPackages(0xffffffff)){
             @Override
             protected Item map(final PackageInfo info) {
                 return new Item(){
                     @Override
                     public Object get() {
                         return info;
                     }
                     @Override
                     public CharSequence getName() {
                         return pm.getApplicationLabel(info.applicationInfo);
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return ApplicationInfo.class;
                     }
                     @Override
                     public String getPath() {
                         return info.packageName.replace('.', '#');
                     }
                 };
             }
             @Override
             public Item byPathSegment(String path) {
                 try {
                     String packageName = path.replaceAll("\\#", ".");
                     PackageInfo pi = pm.getPackageInfo(packageName, 0xffffffff);
                     return pi != null ? map(pi) : null;
                 } catch (NameNotFoundException e) {
                     return null;
                 }
             }
         };
     }
     private static ItemList informationFor(Home home,final HistoryItem o){
         List<Item> els = Arrays.asList(single("String representation","toString",o.toString())
                      ,single("Class","class",o.getClass())
                      ,single("Path","path",o.path));
 
         if (Settings.showSelfFromPath.get())
             els.add(single("Self by path","self",fromPath(home, o.path)));
 
         return fromArray("This",els.toArray(new Item[els.size()]));
     }
     static abstract class MappedArray implements ItemLists{
         List<MetaItemList> metaLists;
         Class<?> currentReturnType;
         public MappedArray(Class<?> clazz) {
             this.metaLists = MetaItemFactory.metaItemsFor(clazz);
             this.currentReturnType = clazz;
         }
         @Override
         public int size() {
             return metaLists.size() + 1;
         }
         @Override
         public ItemList get(int pos){
             if (pos == 0)
                 return new ItemList(){
                     @Override
                     public CharSequence getName() {
                         return "Current Mapping";
                     }
                     @Override
                     public Item get(int pos) {
                         final Object orig = originalValueAt(pos);
                         final Object mapped = mappedValueAt(pos);
                         return new Item(){
                             @Override
                             public Object get() {
                                 return mapped;
                             }
                             @Override
                             public String getName() {
                                 return ItemFactory.toString(orig);
                             }
                             @Override
                             public Class<?> getReturnType() {
                                 return currentReturnType;
                             }
                             @Override
                             public String getPath() {
                                 return getName();
                             }
                         };
                     }
                     @Override
                     public int size() {
                         return numValues();
                     }
                     @Override
                     public Item byPathSegment(String str) {
                         return findByPath(this, str);
                     }
                 };
 
             final MetaItemList metaList = metaLists.get(pos-1);
             return new ItemList(){
                 @Override
                 public CharSequence getName() {
                     return "Map with "+metaList.getName();
                 }
                 @Override
                 public int size() {
                     return metaList.size();
                 }
                 public Item get(int position) {
                     final MetaItem item = metaList.get(position);
                     return fromMetaItem(item);
                 }
                 private Item fromMetaItem(final MetaItem item) {
                     return new Item(){
                         @Override
                         public Object get() {
                             return new MappedArray(item.getReturnType()){
                                 @Override
                                 protected Object mappedValueAt(int position) {
                                     Object val = MappedArray.this.mappedValueAt(position);
                                     return val == null ? null : item.get(val);
                                 }
                                 @Override
                                 protected int numValues() {
                                     return MappedArray.this.numValues();
                                 }
                                 @Override
                                 protected Object originalValueAt(int pos) {
                                     return MappedArray.this.originalValueAt(pos);
                                 }
                                 @Override
                                 protected Class<?> originalClass() {
                                     return MappedArray.this.originalClass();
                                 }
                             };
                         }
                         @Override
                         public CharSequence getName() {
                             return item.getName();
                         }
                         @Override
                         public Class<?> getReturnType() {
                             return MappedArray.class;
                         }
                         @Override
                         public String getPath() {
                             return "*"+item.getPath();
                         }
                     };
                 }
                 @Override
                 public Item byPathSegment(String str) {
                     if (!str.startsWith("*"))
                         return null;
 
                     str = str.substring(1);
 
                     MetaItem i = metaList.byPathSegment(str);
                     return i != null ? fromMetaItem(i) : null;
                 }
             };
         }
 
         protected abstract Object mappedValueAt(int pos);
         protected abstract Object originalValueAt(int pos);
         protected abstract int numValues();
         protected abstract Class<?> originalClass();
         @Override
         public String toString() {
             StringBuffer buffer = new StringBuffer();
             int num = numValues();
             int length = Math.min(num,MAX_ARRAY_ELEMENTS);
             for (int i = 0;i<length;i++){
                 if (buffer.length() != 0)
                     buffer.append("<br/>");
                 buffer.append(ItemFactory.toString(originalValueAt(i)))
                     .append(" → ")
                     .append(ItemFactory.toString(mappedValueAt(i)));
             }
             int moreValues = num - length;
             if (moreValues > 0)
                 buffer.append("\n... <i>(+ ")
                     .append(moreValues)
                     .append(" more")
                     .append(")</i>");
             return buffer.toString();
         }
         public Object[] getArray(){
             int length = numValues();
             Object []res = new Object[length];
             for (int i=0;i<length;i++)
                 res[i] = mappedValueAt(i);
             return res;
         }
         public Map<Object,Object> getMap(){
             HashMap<Object, Object> res = new HashMap<Object, Object>();
 
             int length = numValues();
 
             for (int i=0;i<length;i++)
                 res.put(originalValueAt(i),mappedValueAt(i));
             return res;
         }
         public MappedArray getSwapped(){
             final MappedArray outer = this;
             return new MappedArray(originalClass()){
                 @Override
                 protected Object mappedValueAt(int pos) {
                     return outer.originalValueAt(pos);
                 }
                 @Override
                 protected int numValues() {
                     return outer.numValues();
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return outer.mappedValueAt(pos);
                 }
                 @Override
                 protected Class<?> originalClass() {
                     return outer.currentReturnType;
                 }
             };
         }
         private void putIntoMap(Map<Object,List<Object>> map,Object value,Object key){
             List<Object> os = map.get(value);
             if (os == null){
                 os = new ArrayList<Object>();
                 map.put(value,os);
             }
             os.add(key);
         }
         public MappedArray getGroupedByValue(){
             final Map<Object,List<Object>> res = new HashMap<Object, List<Object>>();
 
             int len = numValues();
             for (int i=0;i<len;i++){
                 Object key = originalValueAt(i);
                 Object value = mappedValueAt(i);
 
                 if (value == null)
                     continue;
 
                 if (currentReturnType.isArray()){
                     int num = Array.getLength(value);
                     for (int j=0;j<num;j++)
                         putIntoMap(res,Array.get(value,j),key);
                 }
                 else
                     putIntoMap(res,value,key);
             }
 
             final Object[]values = new Object[res.size()];
             int i=0;
             for (Object v:res.keySet()){
                 values[i] = v;
                 i++;
             }
 
             return new MappedArray(Array.newInstance(originalClass(),0).getClass()){
                 @Override
                 protected Object mappedValueAt(int pos) {
                    List<Object> list = res.get(values[pos]);
                    return list.toArray((Object[])Array.newInstance(Reflection.boxed(MappedArray.this.originalClass()), list.size()));
                 }
                 @Override
                 protected int numValues() {
                     return values.length;
                 }
                 @Override
                 protected Class<?> originalClass() {
                     return MappedArray.this.currentReturnType;
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return values[pos];
                 }
             };
         }
     }
     private static ItemLists mappedArray(final Object o){
         if (o instanceof Object[]){
             final Object[] array = (Object[]) o;
             return new MappedArray(array.getClass().getComponentType()){
                 @Override
                 protected Object mappedValueAt(int pos) {
                     return array[pos];
                 }
                 @Override
                 protected int numValues() {
                     return array.length;
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return array[pos];
                 }
                 @Override
                 protected Class<?> originalClass() {
                     return array.getClass().getComponentType();
                 }
             };
         }
         else if (o instanceof List){
             final List<?> list = (List<?>) o;
             return new MappedArray(Object.class){
                 @Override
                 protected Object mappedValueAt(int pos) {
                     return list.get(pos);
                 }
                 @Override
                 protected int numValues() {
                     return list.size();
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return list.get(pos);
                 }
                 @Override
                 protected Class<?> originalClass() {
                     return Object.class;
                 }
             };
         }
         else
             return new MappedArray(o.getClass().getComponentType()){
                 @Override
                 protected Object mappedValueAt(int pos) {
                     return Array.get(o, pos);
                 }
                 @Override
                 protected int numValues() {
                     return Array.getLength(o);
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return Array.get(o, pos);
                 }
                 @Override
                 protected Class<?> originalClass() {
                     return o.getClass().getComponentType();
                 }
             };
     }
     private static void add(ArrayList<ItemList> list,ItemList il){
         if (il.size() > 0)
             list.add(il);
     }
     public static ArrayList<ItemList> itemsFor(Home home,HistoryItem it){
         ArrayList<ItemList> res = new ArrayList<ItemList>();
         add(res,informationFor(home,it));
 
         itemsFor(it.object,res);
 
         return res;
     }
     public static ArrayList<ItemList> itemsFor(Object o,ArrayList<ItemList> res){
         if (o.getClass().isArray()){
             add(res,elementsOfArray(o));
 
             Item[] actions = o.getClass().getComponentType().isPrimitive()?
                     new Item[]{single("Mapped array","mapped",mappedArray(o))} :
                     new Item[]{single("Mapped array","mapped",mappedArray(o))
                               ,single("Narrowed","narrow",Reflection.narrow((Object[]) o))};
 
             add(res,fromArray("Actions",actions));
         }
         else if (o instanceof Map)
             add(res,elementsOfMap((Map<?, ?>) o));
         else if (o instanceof File && ((File) o).isDirectory())
             add(res,contentsOfDirectory((File) o));
         else if (o instanceof Iterable)
             add(res,elementsOfIterable((Iterable<?>) o));
         else if (o instanceof PackageManager)
             add(res,packagesFromPM((PackageManager) o));
         else if (o instanceof File && ((File)o).getName().endsWith(".db"))
             add(res,tablesOfDb(((File) o).getPath()));
         else if (o instanceof Cursor)
             add(res,resultsOfQuery((Cursor) o));
 
         if (o instanceof ItemList)
             add(res,(ItemList) o);
         if (o instanceof ItemLists){
             ItemLists lists = (ItemLists) o;
             for (int i=0;i<lists.size();i++)
                 add(res,lists.get(i));
         }
 
         for (MetaItemList meta:MetaItemFactory.metaItemsFor(o.getClass()))
             add(res,materialize(meta,o));
 
         if (Settings.listItemLists.get())
             add(res,elementsOfIterable(res));
 
         return res;
     }
     public static Object fromPath(Home home,String path){
         String[] els = path.split("\\.");
 
         Object o = null;
 outer:  for (String segment:els){
             if ("Home".equals(segment))
                 o = home;
             else {
                 for (ItemList list:itemsFor(o,new ArrayList<ItemList>(10))){
                     // allow navigation into an ItemList
                     if (list.getName().equals(segment)){
                         o = list;
                         continue outer;
                     }
 
                     Item item = list.byPathSegment(segment);
                     if (item != null){
                         o = item.get();
                         continue outer;
                     }
                 }
                 throw new RuntimeException("In path "+path+" couldn't find object for "+segment);
             }
         }
         return o;
     }
 
     private static String capitalized(String str){
         return str.substring(0,1).toUpperCase()+str.substring(1);
     }
     private static Method getMethod(Class<?> clazz,String name){
         try {
             return clazz.getMethod(name);
         } catch (SecurityException e) {
             throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
             return null;
         }
     }
     private static Object getFieldOf(String field,Object o){
         try {
             Class<? extends Object> clazz = o.getClass();
             Method m = getMethod(clazz,"get"+capitalized(field));
             if (m == null)
                 m = getMethod(clazz,field);
 
             if (m != null)
                 return m.invoke(o);
             else{
                 Field f = clazz.getDeclaredField(field);
                 f.setAccessible(true);
                 return f.get(o);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     private static String eval(String path,Object o){
         String[] parts = path.split("\\.");
 
         for(String part:parts)
             o = getFieldOf(part,o);
         return toString(o);
     }
     private static Pattern splish = Pattern.compile("#(\\w+(?:\\.\\w+)*)");
     private static String format(String format,Object o){
         Matcher m = splish.matcher(format);
         StringBuffer res = new StringBuffer();
         while(m.find())
             m.appendReplacement(res, eval(m.group(1),o));
 
         m.appendTail(res);
         return res.toString();
     }
     private static final int MAX_ARRAY_ELEMENTS = 5;
     private static String arrayToString(Object o){
         StringBuffer buffer = new StringBuffer();
         buffer.append('[');
 
         int len = Array.getLength(o);
         for (int i=0;i<Math.min(len,MAX_ARRAY_ELEMENTS);i++){
             if (i != 0)
                 buffer.append(", ");
             buffer.append(toString(Array.get(o,i)));
         }
         int remaining = len - MAX_ARRAY_ELEMENTS;
         if (remaining > 0)
             buffer.append(", ... (<i>").append(remaining).append(" more</i>)");
 
         buffer.append(']');
         return buffer.toString();
     }
     public static String toString(Object o){
         if (o == null)
             return "null";
         else {
             Class<? extends Object> clazz = o.getClass();
             Textual text = clazz.getAnnotation(Textual.class);
             if (text != null)
                 return format(text.value(),o);
             else if (clazz.isArray())
                 return arrayToString(o);
             else if (o instanceof String && Settings.quoteStrings.get())
                 return "\""+o+"\"";
             else
                 return String.valueOf(o);
         }
     }
 }
