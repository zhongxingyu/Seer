 package org.eclipse.jst.jsf.designtime.internal.view.model.jsp.registry;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jst.jsf.common.internal.policy.OrderedListProvider;
 import org.eclipse.jst.jsf.common.internal.policy.OrderedListProvider.OrderableObject;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.DefaultJSPTagResolver;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TagIntrospectingStrategy;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.persistence.PersistedDataTagStrategy;
 
 /**
  * Preferences model for the TLD registry
  * 
  * @author cbateman
  * 
  */
 public class TLDRegistryPreferences
 {
     private final IPreferenceStore             _prefStore;
 
     private final static String                KEY_STRATEGY_ID_ORDER = "org.eclipse.jst.jsf.designtime.jsp.registry.StrategyIDOrder";
 
     private final static List<OrderableObject> DEFAULT_STRATEGY_ORDER;
 
     static
     {
         final List<OrderableObject> list = new ArrayList<OrderableObject>();
         list.add(new OrderableObject(new StrategyIdentifier(PersistedDataTagStrategy.DISPLAY_NAME,
                 PersistedDataTagStrategy.ID), true));
         list.add(new OrderableObject(new StrategyIdentifier(DefaultJSPTagResolver.DISPLAY_NAME,
                 DefaultJSPTagResolver.ID), true));
         list.add(new OrderableObject(new StrategyIdentifier(TagIntrospectingStrategy.DISPLAY_NAME,
                 TagIntrospectingStrategy.ID), true));
         // list.add(UnresolvedJSPTagResolvingStrategy.ID);
         DEFAULT_STRATEGY_ORDER = Collections.unmodifiableList(list);
     }
 
     private List<OrderableObject>              _ids;
     private List<OrderableObject>              _originalIds;
 
     /**
      * Constructor
      * 
      * @param prefStore
      */
     public TLDRegistryPreferences(final IPreferenceStore prefStore)
     {
         _prefStore = prefStore;
         _ids = new ArrayList<OrderableObject>();
     }
 
     /**
      * IPreferenceStore The default preference loader
      */
     public void load()
     {
         load(_prefStore);
     }
 
     /**
      * @return the ordered list provider for the strategy id ordering
      */
     public OrderedListProvider getOrderedListProvider()
     {
         return new MyOrderedListProvider();
     }
 
     /**
      * @return the strategy id ordering
      */
     public List<OrderableObject> getStrategyIdOrdering()
     {
         return _ids;
     }
 
     /**
      * @param ids
      */
     public void setStrategyIdOrdering(final List<OrderableObject> ids)
     {
         _ids = ids;
     }
 
     /**
      * @return the list of strategy ids in the order they should be consulted
      */
     public List<String> getEnabledIds()
     {
         final List<String> strategies = new ArrayList<String>();
 
         for (final OrderableObject id : _ids)
         {
             if (id.isEnabled())
             {
                 StrategyIdentifier strategyId = (StrategyIdentifier) id.getObject();
                 strategies.add(strategyId.getId());
             }
         }
         return strategies;
     }
 
     /**
      * Loads preferences from prefStore
      * 
      * @param prefStore
      */
     private void load(final IPreferenceStore prefStore)
     {
         if (!prefStore.contains(KEY_STRATEGY_ID_ORDER))
         {
             prefStore.setDefault(KEY_STRATEGY_ID_ORDER,
                     serialize(DEFAULT_STRATEGY_ORDER));
         }
         List<OrderableObject> ids = deserialize(prefStore
                 .getString(KEY_STRATEGY_ID_ORDER));
         if (ids == null)
         {
             ids = deserialize(serialize(DEFAULT_STRATEGY_ORDER));
         }
         _ids = ids;
        final List<OrderableObject> originalList = new ArrayList<OrderableObject>();
        for (final OrderableObject id : _ids)
        {
            final OrderableObject copy = id.clone();
            originalList.add(copy);
        }
         _originalIds = Collections.unmodifiableList(originalList);
     }
 
     private String serialize(final List<OrderableObject> ids)
     {
         final StringBuffer buffer = new StringBuffer();
 
         for (final OrderableObject id : ids)
         {
             StrategyIdentifier strategyId = (StrategyIdentifier) id.getObject();
             buffer.append(strategyId.getDisplayName());
             buffer.append(",");
             buffer.append(strategyId.getId());
             buffer.append(",");
             buffer.append(id.isEnabled());
             buffer.append(",");
         }
         return buffer.toString();
     }
 
     private List<OrderableObject> deserialize(final String serializedList)
     {
         final List<OrderableObject> list = new ArrayList<OrderableObject>();
         final String[] ids = serializedList.split(",");
         if ((ids.length % 3) != 0)
         {
             return new ArrayList<OrderableObject>();
         }
 
         for (int i = 0; i < ids.length; i += 3)
         {
             final String displayName = ids[i];
             final String id = ids[i + 1];
             final String enabled = ids[i + 2];
 
             final StrategyIdentifier strategyIdentifier = new StrategyIdentifier(
                     displayName, id);
             list.add(new OrderableObject(strategyIdentifier
                     , Boolean.valueOf(enabled).booleanValue()));
         }
         return list;
     }
 
     /**
      * Commits but does not store the preferences
      * 
      * @param prefStore
      */
     public void commit(final IPreferenceStore prefStore)
     {
         prefStore.setValue(KEY_STRATEGY_ID_ORDER,
                 serialize(getStrategyIdOrdering()));
     }
 
     /**
      * Reverts the model to it's defaults. Does not commit to pref store.
      */
     public void setDefaults()
     {
          setStrategyIdOrdering(new ArrayList<OrderableObject>(
                 DEFAULT_STRATEGY_ORDER));
     }
 
     /**
      * @return true if this preference object's properties have
      * changed since load() was last called.
      */
     public boolean isDirty()
     {
         // dirty if the current list is not equal to the original list
         // generated at load time.
         return !(_ids.equals(_originalIds));
     }
 
     /**
      * Used as the model for sorting and enabling strategy identifiers.
      * 
      */
     public static class StrategyIdentifier
     {
         private final String _id;
         private final String _displayName;
 
         StrategyIdentifier(final String displayName, final String id)
         {
             _displayName = displayName;
             _id = id;
         }
 
         /**
          * @return the id
          */
         public String getId()
         {
             return _id;
         }
 
         /**
          * @return the display name for the strategy
          */
         public String getDisplayName()
         {
             return _displayName;
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj instanceof StrategyIdentifier)
             {
                 return _id.equals(((StrategyIdentifier)obj)._id);
             }
             return false;
         }
 
         @Override
         public int hashCode()
         {
             return _id.hashCode();
         }
     }
 
     private class MyOrderedListProvider extends OrderedListProvider
     {
         @Override
         protected List<OrderableObject> createAndPopulateOrderedObjects()
         {
             return _ids;
         }
     }
 
 }
