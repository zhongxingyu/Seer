 package ops;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 
 public class OPS
 {
   private List<MemoryElement> _wm = new ArrayList<MemoryElement>();
   private List<Rule> _rules = new ArrayList<Rule>();
   private List<PreparedRule> _preparedRules = new ArrayList<PreparedRule>();
   private Map<String, MemoryElement> _templates = new HashMap<String, MemoryElement>();
 
   ExecutorService _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
 
   private ConcurrentLinkedQueue<MemoryElement> _memoryInQueue = new ConcurrentLinkedQueue<MemoryElement>();
 
   private boolean _debug = true;
   private boolean _halt = false;
 
   private boolean _waitForWork = false;
 
   public void reset()
   {
     _halt = false;
     _rules.clear();
     _templates.clear();
     _wm.clear();
   }
 
   public void shutdown()
   {
     _threadPool.shutdown();
   }
 
   public void waitForWork(boolean wait)
   {
     _waitForWork = wait;
   }
 
   public void halt()
   {
     _halt = true;
   }
 
   public void literalize(MemoryElement template)
   {
     _templates.put(template.Type, template);
   }
 
   public void insert(MemoryElement element)
   {
     _wm.add(element);
   }
 
   public void remove(MemoryElement element)
   {
     _wm.remove(element);
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
 //    notify();
 
       return newElement;
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
 
     return newElement;
   }
 
   public void drainInMemoryQueue()
   {
     while(!_memoryInQueue.isEmpty())
     {
       _wm.add(_memoryInQueue.remove());
     }
   }
 
   public void run()
   {
     run(Integer.MAX_VALUE);
   }
 
   public void run(int steps)
   {
     _halt = false;
 
     while (steps-- > 0 && !_halt)
     {
       drainInMemoryQueue();
 
       Match match = match();
       if (match == null)
       {
         if (!_memoryInQueue.isEmpty())
         {
           continue;
         }
 
         break;
 /*
         if (!_waitForWork) break;
 
         try
         {
           wait();
           continue;
         } catch (InterruptedException e)
         {
           break;
         }
 */
       }
 
       final CommandContext context = new CommandContext(this, match.Elements, match.Vars);
 
       for (final ProductionSpec production : match.Rule.Productions)
       {
         final Object[] args = new Object[production.Params.length];
 
         for (int i = 0; i < args.length; i++)
         {
           args[i] = context.resolveValue(production.Params[i]);
         }
 
         try
         {
           if (production.Command instanceof AsyncCommand)
           {
             submit(new Runnable()
             {
               @Override
               public void run()
               {
                 try
                 {
                   production.Command.exec(context, args);
                 }
                 catch (Exception e)
                 {
                   e.printStackTrace();
                 }
               }
             });
           }
           else
           {
             production.Command.exec(context, args);
           }
         }
         catch (Exception e)
         {
           System.err.println(production.toString());
           e.printStackTrace();
         }
       }
 
 //      if (_debug) dumpWorkingMemory();
     }
 
     _halt = true;
   }
 
   private void dumpWorkingMemory()
   {
     for (MemoryElement me : _wm)
     {
       System.out.println(me.toString());
     }
   }
 
   public void addRules(List<Rule> rules)
   {
     _rules.addAll(rules);
     prepareQueries();
   }
 
   public void addRule(Rule rule)
   {
     _rules.add(rule);
     prepareQueries();
   }
 
   private void prepareQueries()
   {
     _preparedRules.clear();
 
     for (Rule rule : _rules)
     {
       PreparedRule preparedRule = new PreparedRule();
       preparedRule.Rule = rule;
       preparedRule.Specificity = computeSpecificity(rule);
       _preparedRules.add(preparedRule);
     }
 
     Collections.sort(_preparedRules, new Comparator<PreparedRule>()
     {
       @Override
       public int compare(PreparedRule preparedRule, PreparedRule preparedRule1)
       {
         return preparedRule1.Specificity.compareTo(preparedRule.Specificity);
       }
     });
 
     _rules.clear();
     for (PreparedRule preparedRule : _preparedRules)
     {
       _rules.add(preparedRule.Rule);
     }
   }
 
   private Integer computeSpecificity(Rule rule)
   {
     Integer specificity = 0;
 
     for (QueryElement element : rule.Query)
     {
       Integer elementSpecificity = 0;
 
       for (QueryPair queryPair : element.QueryPairs)
       {
         if (!(queryPair.Value instanceof String)) continue;
         String strVal = (String) queryPair.Value;
         if (!strVal.startsWith("$")) continue;
         elementSpecificity++;
       }
 
       specificity += elementSpecificity;
     }
 
     return specificity;
   }
 
   public void submit(Runnable runnable)
   {
   }
 
   private class PreparedRule
   {
     Rule Rule;
     Integer Specificity;
   }
 
   private Match match()
   {
     Match match = null;
 
     for (Rule rule : _rules)
     {
       List<MemoryElement> elements = new ArrayList<MemoryElement>();
       Map<String, Object> vars = new HashMap<String, Object>();
 
       for (QueryElement qe : rule.Query)
       {
         boolean haveMatch = false;
 
         for (MemoryElement me : _wm)
         {
           if (elements.contains(me)) continue;
           Map<String, Object> tmpVars = new HashMap<String, Object>(vars);
           haveMatch = compare(qe, me, tmpVars);
           if (haveMatch)
           {
             vars = tmpVars;
             elements.add(me);
             break;
           }
         }
 
         if (!haveMatch)
         {
           break;
         }
       }
 
       if (elements.size() == rule.Query.size())
       {
         match = new Match(rule, elements, vars);
         break;
       }
     }
 
     return match;
   }
 
   private boolean compare(QueryElement qe, MemoryElement me, Map<String, Object> vars)
   {
     if (!(me.Type.equals(qe.Type))) return false;
 
     for (QueryPair qp : qe.QueryPairs)
     {
       Object val = me.Values.containsKey(qp.Key) ? me.Values.get(qp.Key) : null;
 
       if (qp.Value == null)
       {
         if (val == null)
         {
           // match
         }
         else
         {
           return false;
         }
       }
       else
       {
         if (qp.Value instanceof String)
         {
           String strQpVal = (String)qp.Value;
           if (strQpVal.startsWith("$"))
           {
             if (vars.containsKey(strQpVal))
             {
               if (!vars.get(strQpVal).equals(val))
               {
                 return false;
               }
             }
             else
             {
               // variable matches everything
               vars.put(strQpVal, val);
             }
           }
           else
           {
             if (!strQpVal.equals(val))
             {
               return false;
             }
           }
         }
         else
         {
           if (!qp.Value.equals(val))
           {
             return false;
           }
         }
       }
     }
 
     return true;
   }
 
   private class Match
   {
     public Rule Rule;
     public List<MemoryElement> Elements;
     public Map<String, Object> Vars;
 
     public Match(Rule rule, List<MemoryElement> elements, Map<String, Object> vars)
     {
       Rule = rule;
       Elements = elements;
       Vars = vars;
     }
   }
 }
