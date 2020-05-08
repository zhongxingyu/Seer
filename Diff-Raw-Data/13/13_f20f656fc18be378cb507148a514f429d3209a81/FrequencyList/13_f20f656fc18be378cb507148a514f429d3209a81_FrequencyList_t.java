 package Statistics;
 
import Root.BaseObject;
import Root.SimpleTypes.ValueInt;
import TheoryOfSet.Alphabet;
 
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alex
  * Date: 11.12.2010
  * Time: 1:47:53
  */
 public class FrequencyList extends Alphabet {
     private ArrayList<Long> pFrequency = new ArrayList<Long>();
 
     @Override
     public BaseObject get(int index)
     {
         return new DictionaryEntryBase(new ValueInt(pFrequency.get(index)), vault.get(index));
     }
 
     @Override
     public Object clone() {
         FrequencyList frecList = new FrequencyList();
         frecList.pFrequency = (ArrayList<Long>) this.pFrequency.clone();
         frecList.vault = (ArrayList) this.vault.clone();
         return frecList;
     }
 
     @Override
     public boolean equals(Object obj) {
         return false;  //TODO: "Fill method"
     }
 
     public int getCount() {
         int temp = 0;
         for (Long value : pFrequency)
         {
             temp += value;
         }
         return temp;
     }
 
     public int add(BaseObject o) throws Exception {
         int result = indexOf(o);
 
         if (result == -1)
         {
             result = super.add(o);
             pFrequency.add(1l);
         }
         else pFrequency.set(result, pFrequency.get(result) + 1);
         return result;
     }
 
     public void sum(FrequencyList intervals) throws Exception {
         for (int i = 0; i < intervals.getPower(); i++)
         {
             BaseObject value = ((DictionaryEntryBase)intervals.get(i)).getKey();
             long valueCount = ((ValueInt) ((DictionaryEntryBase)intervals.get(i)).getValue()).getValue();
             if (!isContains(value))
             {
                 add(value);
                 valueCount = valueCount - 1;
             }
             pFrequency.set(indexOf(value), pFrequency.get(indexOf(value)) + valueCount);
         }
     }
 }
