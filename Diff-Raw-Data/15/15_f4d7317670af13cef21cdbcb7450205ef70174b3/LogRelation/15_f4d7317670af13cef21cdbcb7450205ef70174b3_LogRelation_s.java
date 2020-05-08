 package polyglot.ext.esj.tologic;
 
 import polyglot.ext.esj.primitives.*;
 
 import java.util.Hashtable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.AbstractCollection;
 import java.util.Iterator;
 import java.io.CharArrayWriter;
 
 public class LogRelation extends Hashtable {
 
     static int RelCtr = 0;
 
     // id instVar domain range fixedSize isUnknown isaCollectionInstVar isaListInstVar subRels
     protected String id;
     protected String instVar;
     protected Class domain;
     protected Class range;
     protected ArrayList subRels;
     protected int fixedSize;
     protected boolean isResultVar;
     protected boolean isUnknown;
     protected boolean isaCollectionInstVar;
     protected boolean isaListInstVar;
     protected boolean isRangeEnum;
 
     public LogRelation(String instVar, Class domain, Class range) {
 	this(instVar, domain, range, false, false, false, false, 0);
     }
 
     public LogRelation(String instVar, Class domain, Class range, boolean isaCollectionInstVar, boolean isaListInstVar){
 	this(instVar, domain, range, isaCollectionInstVar, isaListInstVar, false, false, 0);
     }
 
     public LogRelation(String instVar, Class domain, Class range, boolean isaCollectionInstVar, boolean isaListInstVar, boolean isUnknown){
 	this(instVar, domain, range, isaCollectionInstVar, isaListInstVar, isUnknown, false, 0);
     }
 
     public LogRelation(String instVar, Class domain, Class range, boolean isaCollectionInstVar, boolean isaListInstVar, boolean isUnknown, boolean isResultVar){
 	this(instVar, domain, range, isaCollectionInstVar, isaListInstVar, isUnknown, isResultVar, 0);
     }
 
     public LogRelation(String instVar, Class domain, Class range, boolean isaCollectionInstVar, 
 		       boolean isaListInstVar, boolean isUnknown, boolean isResultVar, 
 		       int fixedSize) {
 	super();
 	this.instVar = instVar;
 	this.domain = domain;
 	this.range = range;
 	this.fixedSize = fixedSize;
 	this.isUnknown = isUnknown;
 	this.isResultVar = isResultVar;
 	this.isaCollectionInstVar = isaCollectionInstVar;
 	this.isaListInstVar = isaListInstVar;
 	this.id = (isaListInstVar ? "m3_" : (isResultVar ? "s" : "r")) + this.RelCtr++;
 	this.isRangeEnum = range.isEnum();
 	if (LogMap.SolverOpt_debug1())
 	    System.out.println("new relation " + this.id + " " + instVar + " old: " + !isUnknown);
     }
 
     public String instVar() { return instVar; }
     //public String id() { return id; }
     public String id() { LogMap.addAsProblemRel(this,id); return id; }
     public Class domain() { return domain; }
     public Class range() { return range; }
     public ArrayList subRels() { return subRels; }
     public int fixedSize() { return fixedSize; }
     public boolean isUnknown() { return isUnknown; }
     public boolean isaCollectionInstVar() { return isaCollectionInstVar; }
     public boolean isaListInstVar() { return isaListInstVar; }
     public boolean isResultVar() { return isResultVar; }
     public boolean isRangeEnum() { return isRangeEnum; }
     public boolean hasFixedSize() { return fixedSize != 0; }
     public void fixedSize(int s) { fixedSize = s; }
     public void incrFixedSize() { fixedSize++; }
     public void range(Class range) { this.range = range; }
 
     public boolean isModifiable(HashMap modifiableFields) {
 	return modifiableFields == null || isResultVar ||
 	    modifiableFields.containsKey(domain.getName() + "." + instVar);
     }
 
     public String domain_log() { 
 	return isaCollectionInstVar ? ESJInteger.zeroTo_log(fixedSize).string() : LogMap.bounds_log(domain, false, false).string();
     }
 
     public String range_log(boolean isBoundsDef, Class resultVarType) { 
 	// have to add 'null' to the set of possible values for the ref field
	return LogMap.bounds_log(resultVarType != null ? resultVarType : range, !isRangeEnum, isBoundsDef).string();
     }
 
     public String fullDomainRange(Class resultVarType) {
 	return (isResultVar ? "" : domain_log() + "->") + (isaListInstVar ? listInstVarDomain_log() : "") + range_log(true, resultVarType);
     }
 
     // FIXME
     public String listInstVarDomain_log() {
 	return ESJInteger.zeroTo_log(fixedSize-1).string();
     }
    
     public void put_log(Object key, Object value) {
 	if (LogMap.SolverOpt_debug2())
 	    System.out.println("trying put key " + key + " value " + value + " ");
 	if (value instanceof AbstractCollection) {
 	    ArrayList l = new ArrayList();
 	    for(Object v : (AbstractCollection) value) {
 		l.add(LogMap.get1(v));
 	    }
 	    put(LogMap.get1(key), l);
 	} else {
 	    put(LogMap.get1(key), LogMap.get1(value));
 	}
 	if (LogMap.SolverOpt_debug2())
 	    System.out.println("id: " + id + " contents: " + this);
     }
 
     /*
     public void atIntIdxPut_log(int index, Object value) {
 	put_log(index,value);
     }
     */
 
     public String lowerBound_log(HashSet<?> modifiableObjects) {
 	LogRelation r = this;
 	boolean filterObjects = false;
 	Set s = r.keySet();
 	if (isUnknown) {
 	    if (modifiableObjects == null)
 		return "{}";	    
 	    filterObjects = true;
 	    r = LogMap.instVarRelOld_log(this);
 	    HashSet sNew = new HashSet();
 	    for (Object k : r.keySet())
 		if (!modifiableObjects.contains(k))
 		    sNew.add(k);
 	    s = sNew;
 	}
 	CharArrayWriter o = new CharArrayWriter();
 	o.append("{");
 	Iterator itr = s.iterator();
 	for (int i=0;i<s.size()-1;i++) {
 	    Object k = itr.next();
 	    Object v = r.get(k);
 	    if (v instanceof ArrayList) {
 		if (isaListInstVar) {
 		    ArrayList lv = (ArrayList) v;
 		    int lvs = lv.size() - 1;
 		    for(int c = 0; c <= lvs; c++)
 			o.append("[A" + k + ", A" + ESJInteger.log(c) + ", A" + lv.get(c) + "], ");
 		} else {
 		    for(Object e : (ArrayList) v)
 			o.append("[A" + k + ", A" + e + "], ");
 		}
 	    } else
 		o.append("[A" + k + ", A" + v + "], ");
 	}
 	if (itr.hasNext()) {
 	    Object k = itr.next();
 	    Object v = r.get(k);
 	    if (v instanceof ArrayList) {
 		ArrayList lv = (ArrayList) v;
 		int lvs = lv.size() - 1;
 		if (isaListInstVar) {
 		    for(int c = 0; c < lvs; c++)
 			o.append("[A" + k + ", A" + ESJInteger.log(c) + ", A" + lv.get(c) + "],");
 		    o.append("[A" + k + ", A" + ESJInteger.log(lvs) + ", A" + lv.get(lvs) + "]");
 		} else {
 		    for(int c = 0; c < lvs; c++) {
 			o.append("[A" + k + ", A" + lv.get(c) + "],");
 		    }
 		    o.append("[A" + k + ", A" + lv.get(lvs) + "]");
 		}
 	    } else
 		o.append("[A" + k + ", A" + v + "]");
 	}
 	o.append("}");
 	return o.toString();
     }
 
     public String log(HashSet<?> modifiableObjects, Class resultVarType) {
 	String lower = lowerBound_log(modifiableObjects);
 	if (isUnknown()) {
 	    CharArrayWriter o = new CharArrayWriter();
 	    o.append("[" + lower + ", " + fullDomainRange(resultVarType) + "]");
 	    if (isaListInstVar) {
 		for (LogRelation s : (ArrayList<LogRelation>) subRels) {
 		    o.append(" bounds " + s + ": [{}, " +  listInstVarDomain_log() + "->" + range() + "] ");
 		}
 	    }
 	    return o.toString();
 	}
 	return lower;
     }
 
     public String funDef_log() {
 	if (isResultVar)
 	    return "one " + id() + " && ";
 	CharArrayWriter o = new CharArrayWriter();
 	String r = range_log(false, null);
 	if (isaListInstVar) {
 	    String d = listInstVarDomain_log();	    
 	    for (LogRelation s : (ArrayList<LogRelation>) subRels) {
 		o.append("FUNCTION(" + s.id() + ", " + d + "->one " + r + ") && ");
 	    }
 	} else {
 	    String d = domain_log();	    
 	    o.append("FUNCTION(" + id + ", " + d + "->one " + r + ") && ");
 	}
 	return o.toString();
     }
 
 }
