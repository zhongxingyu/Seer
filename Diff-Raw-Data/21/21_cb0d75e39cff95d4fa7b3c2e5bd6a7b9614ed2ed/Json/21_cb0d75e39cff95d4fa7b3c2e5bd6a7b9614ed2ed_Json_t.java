 /*
  * Copyright (C) 2011 Miami-Dade County.
  * Copyright (C) 2012 John Pritchard, Gap Data
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Note: this file incorporates source code from 3d party entities. Such code 
  * is copyrighted by those entities as indicated below.
  */
 package json;
 
 /**
  * <h3><a href="http://www.json.org">JSON</a> network format</h3>
  * 
  * 
  * @author Borislav Iordanov
  * @author John Pritchard
  * @version Gap-Data
  */
 public abstract class Json
     extends Object
     implements Cloneable,
                Comparable<Json>
 {
     public static Json Decode(String json){
 
         return (Json)((new Reader()).read(json));
     }
     public static String Encode(Json json){ 
 
         return json.toString();
     }
     public static Json Wrap(Object object){
         if (null == object)
             return NullJson.Instance;
 
         else if (object instanceof Json)
             return (Json)object;
 
         else if (object instanceof String)
             return new StringJson((String)object);
 
         else if (object instanceof Boolean)
             return new BooleanJson((Boolean)object);
 
         else if (object instanceof Number)
             return new NumberJson((Number)object);
 
         else if (object instanceof Builder)
             return ((Builder)object).toJson();
 
         else if (object instanceof Iterable)
             return new ArrayJson( (Iterable)object);
 
         else if (object.getClass().isArray())
             return new ArrayJson((Object[])object);
 
         else if (object instanceof lxl.Map)
             return new ObjectJson( (lxl.Map)object);
 
         else if (object instanceof java.util.Map)
             return new ObjectJson( (java.util.Map)object);
 
         else {
             final Primitive primitive = Primitive.For(object.getClass());
             if (Json.Use(primitive))
                 return new StringJson( Strings.ToString(primitive, object));
             else {
                 /*
                  * Inert string coder protocol
                  * 
                  * (String)toString <-> ctor(String)
                  */
                 return new StringJson( object.toString());
             }
         }
     }
 		
 
 
 	
     protected Json() {
         super();
     }
 
 	
 	
     /**
      * Return a clone (a duplicate) of this <code>Json</code>
      * entity.  Cloning is deep in array and objects. 
      */
     public Json dup() {
         return this.clone();
     }
     public Json clone(){
         try {
             return (Json)super.clone();
         }
         catch (CloneNotSupportedException exc){
             throw new InternalError();
         }
     }
 	
     /**
      * <p>Return the <code>Json</code> element at the specified index of this
      * <code>Json</code> array. This method applies only to Json arrays.
      * </p>
      * 
      * @param index The index of the desired element.
      */
     public Json at(int index) { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Return the specified property of a <code>Json</code> object or <code>null</code>
      * if there's no such property. This method applies only to Json objects.  
      * </p>
      */
     public Json at(String property)	{ throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Return the specified property of a <code>Json</code> object if it exists.
      * If it doesn't, then create a new property with value the <code>def</code> 
      * parameter and return that parameter. 
      * </p>
      * 
      * @param property The property to return.
      * @param def The default value to set and return in case the property doesn't exist.
      */
     public final Json at(String property, Json def)	
     {
         Json x = at(property);
         if (x == null)
             {
                 set(property, def);
                 return def;
             }
         else
             return x; 
     }	
 	
     /**
      * <p>
      * Return the specified property of a <code>Json</code> object if it exists.
      * If it doesn't, then create a new property with value the <code>def</code> 
      * parameter and return that parameter. 
      * </p>
      * 
      * @param property The property to return.
      * @param def The default value to set and return in case the property doesn't exist.
      */
     public final Json at(String property, Object def)
     {
         return at(property, Json.Wrap(def));
     }
 	
     /**
      * <p>
      * Return true if this <code>Json</code> object has the specified property
      * and false otherwise. 
      * </p>
      * 
      * @param property The name of the property.
      */
     public boolean has(String property)	{ throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Return <code>true</code> if and only if this <code>Json</code> object has a property with
      * the specified value. In particular, if the object has no such property <code>false</code> is returned. 
      * </p>
      * 
      * @param property The property name.
      * @param value The value to compare with. Comparison is done via the equals method. 
      * If the value is not an instance of <code>Json</code>, it is first converted to
      * such an instance. 
      * @return
      */
     public boolean is(String property, Object value) { throw new UnsupportedOperationException(); }
 
     /**
      * <p>
      * Return <code>true</code> if and only if this <code>Json</code> array has an element with
      * the specified value at the specified index. In particular, if the array has no element at
      * this index, <code>false</code> is returned. 
      * </p>
      * 
      * @param property The property name.
      * @param value The value to compare with. Comparison is done via the equals method. 
      * If the value is not an instance of <code>Json</code>, it is first converted to
      * such an instance. 
      * @return
      */
     public boolean is(int index, Object value) { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Add the specified <code>Json</code> element to this array. 
      * </p>
      * 
      * @return this
      */
     public Json add(Json el) { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Add an arbitrary Java object to this <code>Json</code> array. The object
      * is first converted to a <code>Json</code> instance by calling the static
      * {@link #Wrap} method.
      * </p>
      * 
      * @param object Any Java object that can be converted to a Json instance.
      * @return this
      */
     public final Json add(Object object) { return add(Json.Wrap(object)); }
 	
     /**
      * <p>
      * Remove the specified property from a <code>Json</code> object and return
      * that property.
      * </p>
      * 
      * @param property The property to be removed.
      * @return The property value or <code>null</code> if the object didn't have such
      * a property to begin with.
      */
     public Json atDel(String property)	{ throw new UnsupportedOperationException(); }
 
     /**
      * <p>
      * Remove the element at the specified index from a <code>Json</code> array and return
      * that element.
      * </p>
      * 
      * @param index The index of the element to delete.
      * @return The element value.
      */
     public Json atDel(int index)	{ throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Delete the specified property from a <code>Json</code> object.
      * </p>
      * 
      * @param property The property to be removed.
      * @return this
      */
     public Json delAt(String property)	{ throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Remove the element at the specified index from a <code>Json</code> array.
      * </p>
      * 
      * @param index The index of the element to delete.
      * @return this
      */
     public Json delAt(int index) { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Remove the specified element from a <code>Json</code> array.
      * </p>
      * 
      * @param el The element to delete.
      * @return this
      */
     public Json remove(Json el)	{ throw new UnsupportedOperationException(); }
 	
     /**
      * <p>
      * Remove the specified Java object (converted to a Json instance) 
      * from a <code>Json</code> array. This is equivalent to 
      * <code>remove({@link #Wrap(object)})</code>.
      * </p>
      * 
      * @param object The object to delete.
      * @return this
      */
     public final Json remove(Object object) { return remove(Json.Wrap(object)); }
 	
     /**
      * <p>
      * Set a <code>Json</code> objects's property.
      * </p>
      * 
      * @param property The property name.
      * @param value The value of the property.
      * @return this
      */
     public Json set(String property, Json value) { throw new UnsupportedOperationException();	}
 	
     /**
      * <p>
      * Set a <code>Json</code> objects's property.
      * </p>
      * 
      * @param property The property name.
      * @param value The value of the property, converted to a <code>Json</code> representation
      * with {@link #Wrap}.
      * @return this
      */
     public final Json set(String property, Object value) { return set(property, Json.Wrap(value)); }
 	
     /**
      * <p>
      * Combine this object or array with the passed in object or array. The types of 
      * <code>this</code> and the <code>object</code> argument must match. If both are
      * <code>Json</code> objects, all properties of the parameter are added to <code>this</code>.
      * If both are arrays, all elements of the parameter are appended to <code>this</code> 
      * </p>
      * @param object The object or array whose properties or elements must be added to this
      * Json object or array.
      * @return this
      */
     public Json with(Json object) { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>Return the underlying value of this <code>Json</code> entity. The actual value will 
      * be a Java Boolean, String, Number, Map, List or null. For complex entities (objects
      * or arrays), the method will perform a deep copy and extra underlying values recursively 
      * for all nested elements.</p>
      */
     public <T> T getValue() { throw new UnsupportedOperationException(); }
 
     public <T> T getValue(Class<T> clas){
         Object value = this.getValue();
         if (null == value || clas.isAssignableFrom(value.getClass()))
             return (T)value;
         else {
             final Primitive primitive = Primitive.For(clas);
             if (Json.Use(primitive)){
                 if (value instanceof String)
                     return Strings.FromString(primitive, (String)value);
                 else
                     return Objects.From(primitive, value);
             }
             else if (Builder.class.isAssignableFrom(clas)){
 
                return Builder.Immutable.ConstructJson( clas, this);
             }
             else if (value instanceof String)
                return Builder.Immutable.ConstructString( clas, (String)value);
             else
                return Builder.Immutable.ConstructObject( clas, value);
         }
     }
     public <T> T getValue(String name){ throw new UnsupportedOperationException(); }
 
     public <T> T getValue(String name, Class<T> clas){ throw new UnsupportedOperationException(); }
 
     public Json setValue(String name, Object value){ throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the boolean value of a boolean <code>Json</code> instance. Call
      * {@link #isBoolean()} first if you're not sure this instance is indeed a
      * boolean.</p>
      */
     public boolean asBoolean() { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>Return the string value of a string <code>Json</code> instance. Call
      * {@link #isString()} first if you're not sure this instance is indeed a
      * string.</p>
      */
     public String asString() { throw new UnsupportedOperationException(); }
 	
     /**
      * <p>Return the integer value of a number <code>Json</code> instance. Call
      * {@link #isNumber()} first if you're not sure this instance is indeed a
      * number.</p>
      */
     public int asInteger() { throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the float value of a float <code>Json</code> instance. Call
      * {@link #isNumber()} first if you're not sure this instance is indeed a
      * number.</p>
      */
     public float asFloat() { throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the double value of a number <code>Json</code> instance. Call
      * {@link #isNumber()} first if you're not sure this instance is indeed a
      * number.</p>
      */
     public double asDouble() { throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the long value of a number <code>Json</code> instance. Call
      * {@link #isNumber()} first if you're not sure this instance is indeed a
      * number.</p>
      */
     public long asLong() { throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the short value of a number <code>Json</code> instance. Call
      * {@link #isNumber()} first if you're not sure this instance is indeed a
      * number.</p>
      */
     public short asShort() { throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the byte value of a number <code>Json</code> instance. Call
      * {@link #isNumber()} first if you're not sure this instance is indeed a
      * number.</p>
      */	
     public byte asByte() { throw new UnsupportedOperationException(); }
 
     /**
      * <p>Return the first character of a string <code>Json</code> instance. Call
      * {@link #isString()} first if you're not sure this instance is indeed a
      * string.</p>
      */	
     public char asChar() { throw new UnsupportedOperationException(); }		
 
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> null entity 
      * and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isNull() { return false; }
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> string entity 
      * and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isString() { return false; }	
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> number entity 
      * and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isNumber() { return false; }	
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> boolean entity 
      * and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isBoolean() { return false;	}	
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> array (i.e. list) entity 
      * and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isArray() { return false; }	
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> object entity 
      * and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isObject(){ return false; }	
     /**
      * <p>Return <code>true</code> if this is a <code>Json</code> primitive entity 
      * (one of string, number or boolean) and <code>false</code> otherwise.
      * </p> 
      */
     public boolean isPrimitive() { return isString() || isNumber() || isBoolean(); }
 
     public final String toString(){
 
         return this.toString(0);
     }
     public final String toString(final int d){
 
         return this.toString(false,d);
     }
     public abstract String toString(final boolean child, final int d);
 
     public abstract int compareTo(Json json);
 
     public final static boolean Use(Primitive p){
         return (null != p);
     }
 }
