 package filter;
 
 import java.util.List;
 import java.util.PropertyResourceBundle;
 import event.Event;
 import exception.TivooEventKeywordNotFound;
 
 
 public class FilterByKeyword extends FilterDecorator
 {
 
     private String myKeyword;
 
 
     public FilterByKeyword (String keyword)
     {
         super();
         myKeyword = keyword;
     }
 
 
     @Override
     public void filter (List<Event> list)
     {
         List<Event> decoratedList = decoratedFilterWork(list);
         for (Event entry : decoratedList)
         {
             try
             {
                if (entry.containsKeyword(entry.get("title"), myKeyword))
                 {
                     myFilteredList.add(entry);
                 }
             }
             catch (TivooEventKeywordNotFound e)
             {
                 myFilteredList.add(entry);
             }
         }
     }
 }
