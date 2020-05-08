 package ops;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 
 public class WorkingMemory
 {
   private HashMap<String, List<MemoryElement>> _wm = new HashMap<String, List<MemoryElement>>();
   private Map<String, MemoryElement> _templates = new HashMap<String, MemoryElement>();
   private ConcurrentLinkedQueue<MemoryElement> _memoryInQueue = new ConcurrentLinkedQueue<MemoryElement>();
 
   private final boolean _waitForItems;
 
   public WorkingMemory() {
     this(false);
   }
 
   public WorkingMemory(boolean waitForItems) {
     _waitForItems = waitForItems;
   }
 
   public void reset()
   {
     _templates.clear();
     _wm.clear();
     notifyDrain();
   }
 
   public List<MemoryElement> get(String key) {
     return _wm.get(key);
   }
   
   public void literalize(MemoryElement template)
   {
     _templates.put(template.Type, template);
   }
 
   public void literalize(String type, Object... values)
   {
     literalize(new MemoryElement(type, values));
   }
 
   public void insert(MemoryElement element)
   {
     if (!_wm.containsKey(element.Type))
     {
       _wm.put(element.Type, new ArrayList<MemoryElement>());
     }
     _wm.get(element.Type).add(element);
 
     notifyDrain();
   }
 
   public void remove(MemoryElement element)
   {
     List<MemoryElement> wme = _wm.get(element.Type);
     if (wme == null) return;
     wme.remove(element);
   }
 
   private void notifyDrain() {
     if (_waitForItems) {
       synchronized (_memoryInQueue) {
         _memoryInQueue.notify();
       }
     }
   }
 
   public MemoryElement make(String type, Object... args)
   {
     return make(new MemoryElement(type, args));
   }
 
   public MemoryElement make(MemoryElement element)
   {
     MemoryElement newElement = null;
 
     try
     {
       if (!_templates.containsKey(element.Type))
       {
         throw new IllegalArgumentException(String.format("memory element type %s not literalized", element.Type));
       }
 
       newElement = _templates.get(element.Type).make(element.Values);
 
       _memoryInQueue.add(newElement);
 
       notifyDrain();
 
       return newElement;
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
 
     return newElement;
   }
 
  public boolean drainInMemoryQueueBlockable()
   {
     if (_waitForItems) {
       synchronized (_memoryInQueue) {
         if (_memoryInQueue.size() == 0) {
           try
           {
             _memoryInQueue.wait();
           }
           catch (InterruptedException e)
           {
           }
         }
       }
     }
 
    return drainInMemoryQueue();
  }

  public boolean drainInMemoryQueue()
  {
    int count = 0;
     while(!_memoryInQueue.isEmpty())
     {
       insert(_memoryInQueue.remove());
      count++;
     }
    return count > 0;
   }
 
   public boolean HasQueuedItems()
   {
     return !_memoryInQueue.isEmpty();
   }
 }
