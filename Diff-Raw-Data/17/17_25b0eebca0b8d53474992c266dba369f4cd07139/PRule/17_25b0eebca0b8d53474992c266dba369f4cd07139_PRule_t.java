 package predict.singletarget;
 
 import mem.OneView;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Set;
 
 import com.pmstation.common.utils.CountingMap;
 import utils.Utils;
 
 /**
  * Created by IntelliJ IDEA.
  * User: adenysenko
  * Date: 22 жовт 2008
  * Time: 16:53:28
  */
 public class PRule extends RuleCond implements java.io.Serializable{
   private CountingMap resultCounts = new CountingMap();
   private CountingMap<Boolean> resultCountsEqPrev = new CountingMap<Boolean>();
   private Map<String,BoolCount> backRefs = new HashMap<String,BoolCount>();
 
   PRule(){
   }
 
   PRule(RuleCond c){
     cond = new HashMap<String, Object>(c.cond);
     negCond = new HashMap<String, Object>(c.negCond);
   }
 
   void addAttention(Set<String> keys){
     if( keys==null ){
       return;
     }
     for( String key : keys ){
       addAttention(key);
     }
   }
 
   void addAttention(String key){
     if( key==null ){
       return;
     }
     if( backRefs.put(key, new BoolCount())!=null ){
       throw new IllegalStateException();
     }
   }
 
   Set<String> attentionKeys(){
     return backRefs.keySet();
   }
 
   boolean convergent(){
     if( resultCounts.size()==1 || resultCountsEqPrev.getValOr0(Boolean.FALSE)==0 ){
       return true;
     }
     for( BoolCount b : backRefs.values() ){
       if( b.cFalse==0 ){
         return true;
       }
     }
     return false;
   }
 
   Map<Object,Double> normalizedResCounts(OneView vprev, OneViewToVal v2v){
     CountingMap resC = resultCounts;
     if( resC.size()>1 &&
         resultCountsEqPrev.size()==1 && resultCountsEqPrev.get(Boolean.FALSE)==null ){
       resC = new CountingMap();
       resC.increment( v2v.val(vprev.prev), resultCountsEqPrev.get(Boolean.TRUE) );
     }
     for( String k : backRefs.keySet() ){
       BoolCount c = backRefs.get(k);
       if( resC.size()>1 && c.cFalse==0 ){
         resC = new CountingMap();
         resC.increment( vprev.get(k), c.cTrue );
         break;
       }
     }
 
     double tot = resC.syncTotalCount();
     Map<Object,Double> ret = new HashMap<Object,Double>();
     for( Object v : resC.keySet() ){
       ret.put(v, resC.getValOr0(v)/tot);
     }
     return ret;
   }
 
   boolean useful(){
     return resultCounts.size()==1 ||
         resultCountsEqPrev.size()==1 && resultCountsEqPrev.get(Boolean.FALSE)==null;
   }
 
   void recordResult(Object val, OneView vprev, OneViewToVal v2v){
     boolean eqPrev = val.equals(v2v.val(vprev));
 //    if( "N".equals(cond.get("!")) && !eqPrev ){
 //      Utils.breakPoint();
 //    }
     resultCounts.increment(val);
     resultCountsEqPrev.increment(eqPrev);
 
    for( String k : backRefs.keySet() ){
      boolean eqRef = false;
      if( vprev!=null ){ smth. is wrong with this NULL
        val.equals(vprev.get(k));
       }
      backRefs.get(k).inc(eqRef);
     }
   }
 
 
 
 
 
 
 
   PRule(Map<String,Object> cond, Map<String,Object> negCond){
     this.cond=cond;
     this.negCond=negCond;
   }
 
   PRule(String attr, Object val, boolean positive){
     if( positive ){
       cond.put(attr, val);
     }else{
       negCond.put(attr, val);
     }
   }
 
   PRule negate(){
     PRule n = new PRule();
     n.cond.putAll(negCond);
     n.negCond.putAll(cond);
     return n;
   }
 
   PRule andRule(PRule r){
     PRule n = new PRule();
 
     n.cond.putAll(cond);
     n.cond.putAll(r.cond);
 
     n.negCond.putAll(negCond);
     n.negCond.putAll(r.negCond);
 
     return n;
   }
 
   boolean condWiderIn(PRule other){
     for( String s : other.cond.keySet() ){
       if( !other.cond.get(s).equals(cond.get(s)) ){
         return false; // we don't have a condition from 'other'
       }
     }
     for( String s : other.negCond.keySet() ){
       if( !other.negCond.get(s).equals(negCond.get(s)) ){
         return false; // we don't have a condition from 'other'
       }
     }
     return true; // all conditions from 'other' are present in 'this'
   }
 
 
   public String toString() {
     String ret = super.toString() + " => ";
     ret += resultCounts.mapSortedDesc();
     if( resultCountsEqPrev.size()==1 && resultCountsEqPrev.get(Boolean.FALSE)==null ){
       ret += " eqPrev";
     }
    for( String k : backRefs.keySet() ){
      BoolCount b = backRefs.get(k);
      if( b.cFalse==0 ){
        ret += " eq@"+k;
      }
    }
     return ret;
   }
 
   public String condToString(){
     return super.toString();
   }
 }
