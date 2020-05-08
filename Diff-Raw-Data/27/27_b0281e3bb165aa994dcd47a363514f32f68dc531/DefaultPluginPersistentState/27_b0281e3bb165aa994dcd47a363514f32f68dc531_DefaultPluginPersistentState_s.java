 package com.atlassian.plugin.manager;
 
 import com.atlassian.plugin.util.concurrent.CopyOnWriteMap;
 import com.atlassian.plugin.manager.PluginPersistentState;
 import com.atlassian.plugin.PluginRestartState;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.ModuleDescriptor;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * <p/>
  * <p>The state stored in this object represents only the <i>differences</i> between the desired state
  * and the default state configured in the plugin. So if "getPluginState()" or "getPluginModuleState()" return
  * null, then the manager should assume that the default state applies instead.
  * <p>
  * Please note that this class is not threadsafe.  Access to instances should be synchronised.
  */
 public class DefaultPluginPersistentState implements Serializable, PluginPersistentState
 {
     private final Map<String, Boolean> map;
     private static final String RESTART_STATE_SEPARATOR = "--";
 
     public DefaultPluginPersistentState()
     {
         this(Collections.<String, Boolean>emptyMap());
     }
 
     public DefaultPluginPersistentState(final Map<String, Boolean> map)
     {
         this.map = CopyOnWriteMap.newHashMap(map);
     }
 
     public DefaultPluginPersistentState(final PluginPersistentState state)
     {
         map = CopyOnWriteMap.newHashMap(state.getMap());
     }
 
     /* (non-Javadoc)
      * @see com.atlassian.plugin.PluginPersistentState#getState(java.lang.String)
      */
     public Boolean getState(final String key)
     {
         return map.get(key);
     }
 
     /* (non-Javadoc)
      * @see com.atlassian.plugin.PluginPersistentState#getMap()
      */
     public Map<String, Boolean> getMap()
     {
         return Collections.unmodifiableMap(map);
     }
 
     /* (non-Javadoc)
      * @see com.atlassian.plugin.PluginPersistentState#isEnabled(com.atlassian.plugin.Plugin)
      */
     public boolean isEnabled(final Plugin plugin)
     {
         final Boolean bool = getState(plugin.getKey());
         return (bool == null) ? plugin.isEnabledByDefault() : bool.booleanValue();
     }
 
     /* (non-Javadoc)
      * @see com.atlassian.plugin.PluginPersistentState#isEnabled(com.atlassian.plugin.ModuleDescriptor)
      */
     public boolean isEnabled(final ModuleDescriptor<?> pluginModule)
     {
         if (pluginModule == null)
         {
             return false;
         }
 
         final Boolean bool = getState(pluginModule.getCompleteKey());
         return (bool == null) ? pluginModule.isEnabledByDefault() : bool.booleanValue();
     }
 
     public void setEnabled(final ModuleDescriptor<?> pluginModule, boolean isEnabled)
     {
         setEnabled(pluginModule.getCompleteKey(), pluginModule.isEnabledByDefault(), isEnabled);
     }
 
     public void setEnabled(final Plugin plugin, boolean isEnabled)
     {
         setEnabled(plugin.getKey(), plugin.isEnabledByDefault(), isEnabled);
     }
     
     private void setEnabled(final String completeKey, boolean enabledByDefault, boolean isEnabled)
     {
         if (isEnabled == enabledByDefault)
         {
             map.remove(completeKey);
         }
         else
         {
             map.put(completeKey, isEnabled);
         }
     }
 
     /**
      * reset all plugin's state.
      */
     public void setState(final PluginPersistentState state)
     {
         map.clear();
         map.putAll(state.getMap());
     }
 
     /**
      * Add the plugin state.
      */
     public void addState(final Map<String, Boolean> state)
     {
         map.putAll(state);
     }
 
     /**
      * Remove a plugin's state.
      */
     public void removeState(final String key)
     {
         map.remove(key);
     }
 
     /* (non-Javadoc)
      * @see com.atlassian.plugin.PluginPersistentState#getPluginStateMap(com.atlassian.plugin.Plugin)
      */
     public Map<String, Boolean> getPluginStateMap(final Plugin plugin)
     {
         final Map<String, Boolean> state = new HashMap<String, Boolean>(getMap());
         CollectionUtils.filter(state.keySet(), new StringStartsWith(plugin.getKey()));
         return state;
     }
 
     public PluginRestartState getPluginRestartState(String pluginKey)
     {
         for (PluginRestartState state : PluginRestartState.values())
         {
             if (map.containsKey(buildStateKey(pluginKey, state)))
             {
                 return state;
             }
         }
         return PluginRestartState.NONE;
     }
 
     public void setPluginRestartState(String pluginKey, PluginRestartState state)
     {
        if (state == PluginRestartState.NONE)
         {
            for (PluginRestartState st : PluginRestartState.values())
            {
                map.remove(buildStateKey(pluginKey, st));
            }
         }
        else
         {
             map.put(buildStateKey(pluginKey, state), true);
         }
     }
 
     private static String buildStateKey(String pluginKey, PluginRestartState state)
     {
         StringBuilder sb = new StringBuilder();
         sb.append(state.name());
         sb.append(RESTART_STATE_SEPARATOR);
         sb.append(pluginKey);
         return sb.toString();
     }
 
     public void clearPluginRestartState()
     {
         Set<String> keys = new HashSet<String>(getMap().keySet());
         for (String key : keys)
         {
             if (key.contains(RESTART_STATE_SEPARATOR))
             {
                 map.remove(key);
             }
         }
     }
 
     private static class StringStartsWith implements Predicate
     {
         private final String prefix;
 
         public StringStartsWith(final String keyPrefix)
         {
             prefix = keyPrefix;
         }
 
         public boolean evaluate(final Object object)
         {
             final String str = (String) object;
             return str.startsWith(prefix);
         }
     }
 }
