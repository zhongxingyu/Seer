 package org.sizzle.rpg.core;
 
 import org.openide.util.Lookup;
 import org.openide.util.lookup.AbstractLookup;
 import org.openide.util.lookup.InstanceContent;
 
 /**
  *
  * @author Jason
  */
 public abstract class AbstractAvatar implements IAvatar {
 
     @Override
     public Lookup getLookup() {
         return propertyLookup;
     }
     protected InstanceContent propertyContent = new InstanceContent();
     protected Lookup propertyLookup = new AbstractLookup(propertyContent);
     protected InstanceContent stateContent = new InstanceContent();
     protected Lookup stateLookup = new AbstractLookup(stateContent);
 
     @Override
     public <C extends IProperty<T>, T> C find(Class<C> propertyClass) {
         C property = propertyLookup.lookup(propertyClass);
         return property;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> IProperty<T> find(String slug) {
         IProperty<T> result = null;
         for (IProperty<?> property : propertyLookup.lookupAll(IProperty.class)) {
             if (property.hasAlias(slug)) {
                 try {
                     result = (IProperty<T>) property;
                 } catch (ClassCastException cce) {
                     //cce.printStackTrace();
                     throw new ClassCastException(String.format("There is no property aliased %1$ with a return type of %2$", result, slug));
                 }
             }
         }
         return result;
     }
 
     @Override
     public <C extends IProperty<T>, T> T findValueOf(Class<C> propertyClass) {
         IProperty<T> property = find(propertyClass);
         T value = property.getValue();
         return value;
     }
 
     @Override
     public <T> T findValueOf(String slug) {
         IProperty<T> property = find(slug);
         T value = property.getValue();
         return value;
     }
 
     @Override
     public boolean hasState(Class<? extends IState> stateClass) {
         return null != stateLookup.lookup(stateClass);
     }
     
     @Override
     public <T> void addProperty(IProperty<T> property) {
         this.propertyContent.add(property);
        property.setAvatar(this);
     }
 
     /**
      *
      * @param <T>
      * @param properties
      */
     @Override
     public <T> void addProperties(IProperty<T>... properties) {
         for (IProperty<T> iProperty : properties) {
             addProperty(iProperty);
         }
     }
 
     @Override
     public <T> void removeProperty(IProperty<T> property) {
         @SuppressWarnings("unchecked")
         Class<IProperty<T>> type = (Class<IProperty<T>>) property.getType();
         String id = "[id]";
         Lookup.Template<IProperty<T>> templatedProperty = new Lookup.Template<IProperty<T>>(type, id, property);
         Lookup.Item<IProperty<T>> item = this.propertyLookup.lookupItem(templatedProperty);
         this.propertyContent.remove(item.getInstance());
     }
 
     @Override
     public <T> void removeAllProperties(Class<? extends IProperty<T>> propertyClass) {
         for (IProperty<?> property : this.propertyLookup.lookupAll(propertyClass)) {
             this.propertyContent.remove(property);
         }
     }
 
     //<editor-fold defaultstate="collapsed" desc="Events">
     @Override
     public void firePropertyChanged() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     //</editor-fold>
 }
