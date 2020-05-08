 package org.picketlink.idm.impl.tree;
 
 import org.infinispan.AdvancedCache;
 import org.infinispan.Cache;
 import org.infinispan.atomic.AtomicMap;
 import org.infinispan.atomic.AtomicMapLookup;
 import org.infinispan.batch.AutoBatchSupport;
 import org.infinispan.config.ConfigurationException;
 import org.infinispan.tree.Fqn;
 import org.infinispan.util.logging.Log;
 import org.infinispan.util.logging.LogFactory;
 
 /**
  * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
  */
 public class IDMTreeCacheImpl extends AutoBatchSupport implements TreeCache
 {
    private final AdvancedCache<Fqn, Object> cache;
 
    private static final Log log = LogFactory.getLog(IDMTreeCacheImpl.class);
 
    private static final Integer PLACEHOLDER = 123456;
 
    public IDMTreeCacheImpl(Cache<?, ?> cache)
    {
       this(cache.getAdvancedCache());
    }
 
    public IDMTreeCacheImpl(AdvancedCache<?, ?> cache)
    {
       this.cache = (AdvancedCache<Fqn, Object>)cache;
       this.batchContainer = cache.getBatchContainer();
       if (cache.getConfiguration().isIndexingEnabled())
       {
          throw new ConfigurationException("TreeCache cannot be used with a Cache instance configured to use indexing!");
       }
       createRoot();
    }
 
    public boolean exists(Fqn f)
    {
       return cache.containsKey(f);
    }
 
    public Node addLeafNode(Fqn nodeFqn)
    {
       startAtomic();
       try
       {
          createNodeInCache(nodeFqn, true);
          return new IDMNodeImpl(nodeFqn, cache, this);
       }
       finally
       {
          endAtomic();
       }
    }
 
    public Node getNode(Fqn nodeFqn)
    {
       if (exists(nodeFqn))
       {
          return new IDMNodeImpl(nodeFqn, cache, this);
       }
       else
       {
          return null;
       }
    }
 
    public boolean removeNode(Fqn nodeFqn)
    {
       if (nodeFqn.isRoot())
       {
          return false;
       }
 
       startAtomic();
       try
       {
          Object cacheObject = cache.get(nodeFqn);
          if (cacheObject != null && cacheObject instanceof AtomicMap)
          {
             // Don't remove node itself, but remove only it's child nodes
             Node myNode = getNode(nodeFqn);
             myNode.removeChildren();
             return true;
          }
          else
          {
             Node parentNode = getNode(nodeFqn.getParent());
             return (parentNode != null && parentNode.removeChild(nodeFqn.getLastElement()));
          }
       }
       finally
       {
          endAtomic();
       }
    }
 
    public boolean removeNode(String fqnString)
    {
       return removeNode(Fqn.fromString(fqnString));
    }
 
    public Cache getCache()
    {
       return cache;
    }
 
    private void createRoot()
    {
       if (!exists(Fqn.ROOT))
       {
          createNodeInCache(Fqn.ROOT, false);
       }
    }
 
    private boolean createNodeInCache(Fqn fqn, boolean isLeafNode)
    {
       if (cache.containsKey(fqn))
       {
          return false;
       }
 
       Fqn parent = fqn.getParent();
       if (!fqn.isRoot())
       {
          if (!exists(parent))
          {
             createNodeInCache(parent, false);
          }
          AtomicMap<Object, Fqn> parentStructure = getStructure(parent);
          parentStructure.put(fqn.getLastElement(), fqn);
       }
 
       if (isLeafNode)
       {
          // TODO: is this needed? Maybe we can put null
          cache.put(fqn, PLACEHOLDER);
       }
       else
       {
          getStructure(fqn);
       }
 
       if (log.isTraceEnabled())
       {
          log.tracef("Created node %s", fqn);
       }
 
       return true;
    }
 
    AtomicMap<Object, Fqn> getStructure(Fqn fqn)
    {
       return AtomicMapLookup.getAtomicMap(cache, fqn);
    }
 
 
 
    /**
     * Visual representation of a tree
     *
     * @return String rep
     */
    public String printTree()
    {
       StringBuilder sb = new StringBuilder();
       sb.append("\n\n");
 
       // walk tree
       sb.append("+ ").append(Fqn.SEPARATOR);
      Object rootNodeContent = cache.get(Fqn.ROOT);

      if (rootNodeContent == null)
      {
         sb.append("NULL_CONTENT\n\n");
         return sb.toString();
      }

      if (rootNodeContent instanceof AtomicMap)
       {
          sb.append("  NO_DATA");
       }
       else
       {
         sb.append("  ").append(rootNodeContent);
       }
       sb.append("\n");
       printChildren(getNode(Fqn.ROOT), 1, sb);
       return sb.toString();
    }
 
    private void printChildren(Node node, int depth, StringBuilder sb)
    {
       AtomicMap<Object, Fqn> structure = getStructure(node.getFqn());
 
       for (Fqn childFqn : structure.values())
       {
          for (int i = 0; i < depth; i++) sb.append("  "); // indentations
          sb.append("+ ");
          sb.append(childFqn.getLastElementAsString()).append(Fqn.SEPARATOR);
 
          if (cache.get(childFqn) instanceof AtomicMap)
          {
            sb.append("  NO_DATA\n");
             Node n = new IDMNodeImpl(childFqn, cache, this);
             printChildren(n, depth + 1, sb);
          }
          else
          {
             sb.append("  ").append(cache.get(childFqn));
          }
          sb.append("\n");
       }
    }
 
 }
