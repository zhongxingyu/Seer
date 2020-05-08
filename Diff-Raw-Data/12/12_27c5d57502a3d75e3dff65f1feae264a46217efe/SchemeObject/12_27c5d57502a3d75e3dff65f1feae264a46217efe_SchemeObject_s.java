 package com.undi.javascheme;
 
 import java.util.HashMap;
 import java.util.Vector;
 import com.undi.util.HashCodeUtil;
 import com.undi.util.Reflector;
 
 //This class works almost like a union
 public class SchemeObject {
   public static enum type {
     NUMBER, BOOLEAN, CHARACTER, STRING, SYMBOL, PAIR, EMPTY_LIST, NATIVE_PROC, 
     COMPOUND_PROC, VECTOR, HASH_MAP,
     JAVA_OBJ, JAVA_METHOD, JAVA_CONSTRUCTOR, JAVA_STATIC_METHOD, NUM_TYPES
   };
   
   private type mType;
   private Object mData;
 
   public static final SchemeObject TRUE = SchemeObject.createBoolean(true);
   public static final SchemeObject FALSE = SchemeObject.createBoolean(false);
   public static final SchemeObject THE_EMPTY_LIST = SchemeObject.createEmptyList();
 
   public static SchemeObject symbolTable = SchemeObject.THE_EMPTY_LIST;
   public static final SchemeObject QUOTE_SYMBOL = SchemeObject.makeSymbol("quote");
   public static final SchemeObject DEFINE_SYMBOL = SchemeObject.makeSymbol("define");
   public static final SchemeObject SET_SYMBOL = SchemeObject.makeSymbol("set!");
   public static final SchemeObject OK_SYMBOL = SchemeObject.makeSymbol("ok");
   public static final SchemeObject IF_SYMBOL = SchemeObject.makeSymbol("if");
   public static final SchemeObject LAMBDA_SYMBOL = SchemeObject.makeSymbol("lambda");
   public static final SchemeObject BEGIN_SYMBOL = SchemeObject.makeSymbol("begin");
   public static final SchemeObject COND_SYMBOL = SchemeObject.makeSymbol("cond");
   public static final SchemeObject ELSE_SYMBOL = SchemeObject.makeSymbol("else");
   public static final SchemeObject LET_SYMBOL = SchemeObject.makeSymbol("let");
   public static final SchemeObject LOAD_SYMBOL = SchemeObject.makeSymbol("load");
   public static final SchemeObject OR_SYMBOL = SchemeObject.makeSymbol("or");
   public static final SchemeObject AND_SYMBOL = SchemeObject.makeSymbol("and");
   public static final SchemeObject APPLY_SYMBOL = SchemeObject.makeSymbol("apply");
   public static final SchemeObject WHILE_SYMBOL = SchemeObject.makeSymbol("while");
   public static final SchemeObject EVAL_SYMBOL = SchemeObject.makeSymbol("eval");
 
   public type getType() {
     return this.mType;
   }
   
   // TODO: This doesn't work
   /**
    * Polymorphic check for equality of the values of two schemeObjects
    * @param other the object to check agains
    * @return 
    */
   public boolean valueEqual(SchemeObject other) {
     if (other.mType != this.mType) {
       return false;
     }
     switch (this.mType) {
     case NUMBER:
       return this.getNumber() == other.getNumber();
     case STRING:
       return (new String(this.getString()))
           .equals(new String(other.getString()));
     case CHARACTER:
       return this.getCharacter() == other.getCharacter();
     case SYMBOL:
       return this.getSymbol().equals(other.getSymbol());
     case BOOLEAN:
       return this.getBoolean() == other.getBoolean();
     default:
       return this.mData == other.mData;
     }
   }
 
   //Java Methods/constructors
   public static SchemeObject makeJavaConstructor(String className){
     SchemeObject obj = new SchemeObject();
     obj.mType = type.JAVA_CONSTRUCTOR;
     obj.mData = className;
     return obj;
   }
   public Object callJavaConstructor(SchemeObject args){
     if(this.mType != type.JAVA_CONSTRUCTOR){
       throw new SchemeException("Object Isn't a Java Constructor!");
     }
     Object[] argsArray = prepJavaArgs(args);
 		try{
 			return Reflector.invokeStaticMethod((String)this.mData, "new", argsArray);
 		}catch(Exception e){
 				throw new SchemeException("Unable to call constructor -- " + e.getClass().getName() + " - " + e.getMessage(), e);
 		}
   }
   public boolean isJavaMethod(){
     return this.mType == type.JAVA_METHOD;
   }
   public boolean isJavaConstructor(){
     return this.mType == type.JAVA_CONSTRUCTOR;
   }
   
   public static SchemeObject makeJavaMethod(String methodName){
     SchemeObject obj = new SchemeObject();
     obj.mType = type.JAVA_METHOD;
     obj.mData = methodName;
     return obj;
   }
   
   public Object[] prepJavaArgs(SchemeObject args){
     int numArgs = (int)SchemeNatives.length.getNativeProc().call(
         SchemeObject.cons(args, SchemeObject.THE_EMPTY_LIST)).getNumber();
     Object[] retArgs = new Object[numArgs];
     int index = 0;
     while(!args.isEmptyList()){
       //TODO: handle odd data types like lists, vectors, etc
       SchemeObject curArg = args.getCar();
       if(curArg.isString()){
         retArgs[index] = new String(curArg.getString());
       }else{
         retArgs[index] = curArg.mData;
       }
       
       index++;
       args = args.getCdr();
     }
     return retArgs;
   }
   
   public SchemeObject callJavaMethod(SchemeObject args){
     if(this.mType != type.JAVA_METHOD){
       throw new SchemeException("Object Isn't a Java Method!");
     }
     Object target = args.getCar().getJavaObj();
     Object[] argsArray = prepJavaArgs(args.getCdr());
     //TODO - cast this to the appropriate SchemeObject
     Object retObj = Reflector.invokeInstanceMethod(target, (String) this.mData, argsArray);
     return makeJavaObj(retObj);
   }
   
   public static SchemeObject makeJavaStaticMethod(String className, String methodName){
     SchemeObject obj = new SchemeObject();
     obj.mType = type.JAVA_STATIC_METHOD;
     String[] data = new String[2];
     data[0] = className;
     data[1] = methodName;
     obj.mData = data;
     return obj;
   }
   public boolean isJavaStaticMethod(){
     return this.mType == type.JAVA_STATIC_METHOD;
   }
   public SchemeObject callJavaStaticMethod(SchemeObject args){
 		if(!this.isJavaStaticMethod()){
       throw new SchemeException("Object Isn't a Java Static Method!");
     }
     Object[] argsArray = prepJavaArgs(args);
     String className = ((String[]) this.mData)[0];
     String methodName = ((String[]) this.mData)[1];
     //TODO - cast this to the appropriate SchemeObject
    Object retObj = Reflector.invokeStaticMethod(className, methodName, argsArray);
     return makeJavaObj(retObj);
   }
   
   //Java Objects
   /**
    * @param className - the class name for the object to create
    * @param params - parameters to pass to the constructor
    */
   public static SchemeObject makeJavaObj(Object data){
     SchemeObject obj = new SchemeObject();
     obj.mType = type.JAVA_OBJ;
     obj.mData = data;
     return obj;
   }
   
   public Object getJavaObj(){
     if(this.mType != type.JAVA_OBJ){
       throw new SchemeException("Object Isn't a Java Object!");
     }
     return this.mData;
   }
   
   // Compound Proc
   public static SchemeObject makeCompoundProc(SchemeObject params,
       SchemeObject body, SchemeObject env) {
     SchemeObject obj = new SchemeObject();
     obj.mType = type.COMPOUND_PROC;
     SchemeObject[] newData = new SchemeObject[3];
     newData[0] = params;
     newData[1] = body;
     newData[2] = env;
     obj.mData = newData;
     return obj;
   }
 
   public SchemeObject getCompoundProcParams() {
     if (this.mType != type.COMPOUND_PROC) {
       throw new SchemeException("Object Isn't a Compound Proc!");
     }
     SchemeObject[] data = (SchemeObject[]) this.mData;
     return data[0];
   }
 
   public SchemeObject getCompoundProcBody() {
     if (this.mType != type.COMPOUND_PROC) {
       throw new SchemeException("Object Isn't a Compound Proc!");
     }
     SchemeObject[] data = (SchemeObject[]) this.mData;
     return data[1];
   }
 
   public SchemeObject getCompoundProcEnv() {
     if (this.mType != type.COMPOUND_PROC) {
       throw new SchemeException("Object Isn't a Compound Proc!");
     }
     SchemeObject[] data = (SchemeObject[]) this.mData;
     return data[2];
   }
 
   public boolean isCompoundProc() {
     return this.mType == type.COMPOUND_PROC;
   }
 
   // Native Proc
   public static SchemeObject makeNativeProc(SchemeNatives.NativeProc proc) {
     SchemeObject obj = new SchemeObject();
     obj.mType = type.NATIVE_PROC;
     obj.mData = proc;
     return obj;
   }
 
   public boolean isNativeProc() {
     return this.mType == type.NATIVE_PROC;
   }
 
   public SchemeNatives.NativeProc getNativeProc() {
     if (this.mType != type.NATIVE_PROC) {
       throw new SchemeException("Object Isn't a Native Proc!");
     }
     return (SchemeNatives.NativeProc) this.mData;
   }
 
   // Pairs
   public static SchemeObject makePair(SchemeObject car, SchemeObject cdr) {
     if (car == null && cdr == null) {
       return THE_EMPTY_LIST;
     }
     SchemeObject obj = new SchemeObject();
     obj.mType = type.PAIR;
     obj.mData = new SchemeObject[2];
     obj.setCar(car);
     obj.setCdr(cdr);
     return obj;
   }
 
   public boolean isPair() {
     return this.mType == type.PAIR;
   }
 
   public void setCar(SchemeObject car) {
     SchemeObject[] data = (SchemeObject[]) this.mData;
     data[0] = car;
   }
 
   public void setCdr(SchemeObject cdr) {
     SchemeObject[] data = (SchemeObject[]) this.mData;
     data[1] = cdr;
   }
 
   public SchemeObject getCar() {
     if (this.mType == type.PAIR) {
       return ((SchemeObject[]) this.mData)[0];
     } else {
       return this;
     }
   }
 
   public SchemeObject getCdr() {
     if (this.mType == type.PAIR) {
       return ((SchemeObject[]) this.mData)[1];
     } else {
       return THE_EMPTY_LIST;
     }
   }
   
   public int getListLength(){
     int length = 0;
     SchemeObject subLst = this;
     while(!subLst.isEmptyList()){
       subLst = subLst.getCdr();
       length++;
     }
     return length;
   }
 
   public static SchemeObject car(SchemeObject obj) {
     return obj.getCar();
   }
 
   public static SchemeObject cdr(SchemeObject obj) {
     return obj.getCdr();
   }
 
   public static SchemeObject caar(SchemeObject obj) {
     return car(car(obj));
   }
 
   public static SchemeObject cadr(SchemeObject obj) {
     return car(cdr(obj));
   }
 
   public static SchemeObject cdar(SchemeObject obj) {
     return cdr(car(obj));
   }
 
   public static SchemeObject cddr(SchemeObject obj) {
     return cdr(cdr(obj));
   }
 
   public static SchemeObject caddr(SchemeObject obj) {
     return car(cdr(cdr(obj)));
   }
 
   public static SchemeObject cdadr(SchemeObject obj) {
     return cdr(car(cdr(obj)));
   }
 
   public static SchemeObject caadr(SchemeObject obj) {
     return car(car(cdr(obj)));
   }
 
   public static SchemeObject cadddr(SchemeObject obj) {
     return car(cdr(cdr(cdr(obj))));
   }
 
   public static SchemeObject caaddr(SchemeObject obj) {
     return car(car(cdr(cdr(obj))));
   }
 
   public SchemeObject[] getPair() {
     if (this.mType != type.PAIR) {
       throw new SchemeException("Object Isn't a Pair!");
     }
     return (SchemeObject[]) this.mData;
   }
 
   public static SchemeObject cons(SchemeObject car, SchemeObject cdr) {
     return SchemeObject.makePair(car, cdr);
   }
 
   public static SchemeObject concatList(SchemeObject list, SchemeObject item) {
     if (list.isEmptyList()) {
       return item;
     } else {
       return cons(list.getCar(), concatList(list.getCdr(), item));
     }
   }
 
   public static boolean isFalse(SchemeObject obj) {
     return obj == FALSE;
   }
 
   public static boolean isTrue(SchemeObject obj) {
     return !isFalse(obj);
   }
 
   /**
    * Prints a list, modifies input tempString
    * 
    * @param tempString
    */
   private void writePair(StringBuilder tempString) {
     SchemeObject[] pair = getPair();
     SchemeObject carObj = pair[0];
     SchemeObject cdrObj = pair[1];
     tempString.append(carObj);
     if (cdrObj.mType == type.PAIR) {
       tempString.append(' ');
       cdrObj.writePair(tempString);
     } else if (cdrObj.isEmptyList()) {
       return;
     } else {
       tempString.append(" . ");
       tempString.append(cdrObj);
     }
   }
 
   // Numbers
   public static SchemeObject makeNumber(double value) {
     SchemeObject obj = new SchemeObject();
     obj.setNumber(value);
     return obj;
   }
 
   public boolean isNumber() {
     return this.mType == type.NUMBER;
   }
 
   public double getNumber() {
     if (this.mType != type.NUMBER) {
       throw new SchemeException("Object Isn't a Number!");
     }
     return (Double) this.mData;
   }
 
   public void setNumber(double value) {
     this.mType = type.NUMBER;
     this.mData = value;
   }
 
   // Booleans
   private static SchemeObject createBoolean(boolean value) {
     SchemeObject obj = new SchemeObject();
     obj.setBoolean(value);
     return obj;
   }
 
   public boolean isBoolean() {
     return this.mType == type.BOOLEAN;
   }
 
   public static SchemeObject makeBoolean(boolean value) {
     return (value) ? SchemeObject.TRUE : SchemeObject.FALSE;
   }
 
   private void setBoolean(boolean value) {
     this.mType = type.BOOLEAN;
     this.mData = value;
   }
 
   public boolean getBoolean() {
     if (this.mType != type.BOOLEAN) {
       throw new SchemeException("Object Isn't a Boolean!");
     }
     return (Boolean) this.mData;
   }
 
   // Characters
   public static SchemeObject makeCharacter(short value) {
     SchemeObject obj = new SchemeObject();
     obj.setCharacter(value);
     return obj;
   }
 
   public boolean isCharacter() {
     return this.mType == type.CHARACTER;
   }
 
   public short getCharacter() {
     if (this.mType != type.CHARACTER) {
       throw new SchemeException("Object Isn't a Character!");
     }
     return (Short) this.mData;
   }
 
   public void setCharacter(short value) {
     this.mType = type.CHARACTER;
     this.mData = value;
   }
 
   // Strings
   public static SchemeObject makeString(String value) {
     SchemeObject obj = new SchemeObject();
     obj.setString(value);
     return obj;
   }
 
   public boolean isString() {
     return this.mType == type.STRING;
   }
 
   public char[] getString() {
     if (this.mType != type.STRING) {
       throw new SchemeException("Object Isn't a String!");
     }
     return (char[]) this.mData;
   }
 
   public void setString(String value) {
     this.mType = type.STRING;
     this.mData = value.toCharArray();
   }
 
   // Empty list
   private static SchemeObject createEmptyList() {
     SchemeObject obj = new SchemeObject();
     obj.mData = null;
     obj.mType = type.EMPTY_LIST;
     return obj;
   }
 
   public static SchemeObject makeEmptyList() {
     return SchemeObject.THE_EMPTY_LIST;
   }
 
   public boolean isEmptyList() {
     return this.mType == type.EMPTY_LIST;
   }
 
   // Symbols
   public static SchemeObject makeSymbol(String value) {
     // See if this symbol is in the symbol table
     SchemeObject elt = symbolTable;
     while (!elt.isEmptyList()) {
       if (car(elt).getSymbol().equals(value)) {
         return car(elt);
       }
       elt = cdr(elt);
     }
     SchemeObject obj = new SchemeObject();
     obj.setSymbol(value);
     symbolTable = cons(obj, symbolTable);
     return obj;
   }
 
   public boolean isSymbol() {
     return this.mType == type.SYMBOL;
   }
 
   public String getSymbol() {
     if (this.mType != type.SYMBOL) {
       throw new SchemeException("Object Isn't a Symbol!");
     }
     return (String) this.mData;
   }
 
   public void setSymbol(String value) {
     this.mType = type.SYMBOL;
     this.mData = value;
   }
 
   // Vectors
   public static SchemeObject makeVector(SchemeObject contents) {
     SchemeObject obj = new SchemeObject();
     obj.mType = type.VECTOR;
 
     obj.mData = new Vector<SchemeObject>();
     while (!contents.isEmptyList()) {
       obj.addToVector(contents.getCar());
       contents = contents.getCdr();
     }
 
     return obj;
   }
 
   @SuppressWarnings("unchecked")
   public Vector<SchemeObject> getVector() {
     if (!this.isVector()) {
       throw new SchemeException("Object Isn't a Vector!");
     }
     return (Vector<SchemeObject>) this.mData;
   }
 
   public SchemeObject addToVector(SchemeObject obj) {
     // If it's a vector, we know it has a vector as data
     Vector<SchemeObject> myVec = this.getVector();
     myVec.add(obj);
     return OK_SYMBOL;
   }
 
   public boolean isVector() {
     return this.mType == type.VECTOR;
   }
 
   // Hash Tables
   public boolean isHashMap() {
     return this.mType == type.HASH_MAP;
   }
 
   public static SchemeObject makeHashMap(SchemeObject elts) {
     SchemeObject obj = new SchemeObject();
     obj.mType = type.HASH_MAP;
 
     obj.mData = new HashMap<SchemeObject, SchemeObject>();
 
     // elts is a list of key value pairs, so (1 "one" 2 "two") -> {1 => "one", 2
     // => "two"}
     SchemeObject newKey;
     SchemeObject newVal;
     while (!elts.isEmptyList()) {
       newKey = SchemeObject.car(elts);
       newVal = SchemeObject.cadr(elts);
 
       obj.setHashMap(newKey, newVal);
 
       // skip to the next pair
       elts = SchemeObject.cddr(elts);
     }
 
     return obj;
   }
 
   public SchemeObject setHashMap(SchemeObject key, SchemeObject val) {
     this.getHashMap().put(key, val);
     return OK_SYMBOL;
   }
 
   @SuppressWarnings("unchecked")
   public HashMap<SchemeObject, SchemeObject> getHashMap() {
     if (!this.isHashMap()) {
       throw new SchemeException("Object Isn't a HashMap!");
     }
     return (HashMap<SchemeObject, SchemeObject>) this.mData;
   }
 
   /**
    * Returns the object as a string **Writer in repl**
    */
   @Override
   public String toString() {
     StringBuilder tempString = new StringBuilder();
     // tempString.append("Type: " + this.mType.name());
 
     // tempString.append(" Value: ");
     switch (this.mType) {
     case NUMBER:
       tempString.append(getNumber());
       break;
     case STRING:
       tempString.append('"');
       tempString.append(getString());
       tempString.append('"');
       break;
     case CHARACTER:
       tempString.append((char) getCharacter());
       break;
     case BOOLEAN:
       tempString.append(getBoolean() ? "true" : "false");
       break;
     case SYMBOL:
       tempString.append(getSymbol());
       break;
     case PAIR:
       tempString.append('(');
       this.writePair(tempString);
       tempString.append(')');
       break;
     case EMPTY_LIST:
       tempString.append("()");
       break;
     case COMPOUND_PROC:
       tempString.append("#<interpreted procedure>");
       break;
     case NATIVE_PROC:
       tempString.append("#<native procedure>");
       break;
     case VECTOR:
       tempString.append("#(");
       Vector<SchemeObject> data = this.getVector();
       int elts = data.size();
       for (int i = 0; i < elts; i++) {
         tempString.append(data.get(i));
         if (elts - i > 1) {
           tempString.append(' ');
         }
       }
       tempString.append(")");
       break;
     case HASH_MAP:
       tempString.append("{");
 
       SchemeObject nextEntry;
       HashMap<SchemeObject, SchemeObject> myMap = this.getHashMap();
       for (SchemeObject key : myMap.keySet()) {
         nextEntry = myMap.get(key);
         if(nextEntry.isHashMap() || nextEntry.isPair()){
           tempString.append(key + " => *Hash Map or List*, ");
         }else{
           tempString.append(key + " => " + nextEntry + ", ");
         }
       }
       // Clear out the last ", "
       tempString.delete(tempString.lastIndexOf(", "), tempString.length());
 
       tempString.append("}");
       break;
     case JAVA_OBJ:
       tempString.append("<Java Obj: ");
       
       tempString.append(this.getJavaObj());
       
       tempString.append(">");
       break;
     }
 
     // tempString.append(">");
     return tempString.toString();
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj.getClass() != SchemeObject.class) {
       return super.equals(obj);
     }
     SchemeObject sObj = (SchemeObject) obj;
     if (this.mType != sObj.mType) {
       return false;
     }
     switch (this.mType) {
     case NUMBER:
       return this.getNumber() == sObj.getNumber();
     case CHARACTER:
       return this.getCharacter() == sObj.getCharacter();
     case STRING:
       return new String(this.getString()).equals(new String(sObj.getString()));
     case SYMBOL:
       return this.getSymbol().equals(sObj.getSymbol());
     }
     return false;
   }
 
   @Override
   public int hashCode() {
     switch (this.mType) {
     case NUMBER:
       return HashCodeUtil.hash(HashCodeUtil.SEED, (long) this.getNumber());
     case CHARACTER:
       return HashCodeUtil.hash(HashCodeUtil.SEED, this.getCharacter());
     case STRING:
       return HashCodeUtil.hash(HashCodeUtil.SEED, new String(this.getString()));
     case SYMBOL:
       HashCodeUtil.hash(HashCodeUtil.SEED, this.getSymbol());
     }
     return HashCodeUtil.hash(HashCodeUtil.SEED, this.mData);
   }
 }
