 package org.otherobjects.cms.jcr;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.springframework.util.Assert;
 
 /**
  * Selects a range of items from a List. Currently supports:
  * 
  * <ul>
  * <li>{n} = first <i>n</i> items 
  * <li>{n..m} = items <i>n</i> to <i>m</i>
  * <li>{%n} = <i>n</i> random items (random items will not contain duplicates)
  * </ul>
  * 
  * <p>Note, the indexes are one-based since they will be used by end users not just developers.
  * 
  * @author rich
  *
  */
 public class RangeSelector<T>
 {
     private int start = -1;
     private int end = -1;
     private boolean random = false;
     private final List<T> items;
 
     public RangeSelector(String selector, List<T> items)
     {
 
         this.items = items;
         Pattern p = Pattern.compile("\\{(%?)(\\d*)(\\.\\.)?(\\d+)\\}");
         Matcher m = p.matcher(selector);
 
         Assert.isTrue(m.matches(), "Invalid selector pattern: " + selector);
 
         if (selector.contains("%"))
             random = true;
 
         this.random = m.group(1).length() > 0;
         this.start = m.group(2).length() > 0 ? Integer.parseInt(m.group(2)) : 1;
         this.end = Integer.parseInt(m.group(4));
 
        Assert.isTrue(this.end >= this.start, "Invalid selector pattern. End index must be greater than or equal to start index: " + selector);
         Assert.isTrue(!(this.random && this.start != 1), "Invalid selector pattern. Random sublists not supported: " + selector);
     }
 
     public List<T> getSelected()
     {
         if (this.random)
         {
             List<T> results = new ArrayList<T>();
             List<Integer> randomIntArray = randomIntArray(this.end, this.items.size());
             for (int i : randomIntArray)
             {
                 results.add(items.get(i));
             }
             return results;
         }
         else
             return this.items.subList(this.start - 1, this.end);
     }
 
     /**
      * From: http://www.acuras.co.uk/articles/21-java-random-array-generator
      * 
      * @param length
      * @param max
      * @return
      */
     public List<Integer> randomIntArray(int length, int max)
     {
         // FIXME Can we replace this with a fast but more random generator?
         List<Integer> randomInts = new ArrayList<Integer>(length);
         Random generator = new Random();
         for (int i = 0; i < length; i++)
         {
             // Don't allow duplicates
             int r;
             do
             {
                 r = generator.nextInt(max);
             }
             while (randomInts.contains(r));
             randomInts.add(r);
         }
         return randomInts;
     }
 
     public int getStart()
     {
         return start;
     }
 
     public void setStart(int start)
     {
         this.start = start;
     }
 
     public int getEnd()
     {
         return end;
     }
 
     public void setEnd(int end)
     {
         this.end = end;
     }
 
     public boolean isRandom()
     {
         return random;
     }
 
     public void setRandom(boolean random)
     {
         this.random = random;
     }
 
 }
