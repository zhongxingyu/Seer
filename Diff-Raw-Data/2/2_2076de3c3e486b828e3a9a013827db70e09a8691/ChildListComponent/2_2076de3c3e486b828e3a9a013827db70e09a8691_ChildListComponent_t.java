 package com.seitenbau.micgwaf.component;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * A Component consisting of a list of child components.
  */
 public class ChildListComponent<T extends Component> extends Component
 {
   /** serial Version UID. */
   private static final long serialVersionUID = 1L;
 
   public final ChildList<T> children = new ChildList<T>();
   
   public ChildListComponent(Component parent)
   {
     super(null, parent);
   }
 
   public ChildListComponent(String id, Component parent)
   {
     super(id, parent);
   }
 
   public ChildListComponent(String id, Component parent, T child)
   {
     super(id, parent);
     children.add(child);
   }
 
   public List<T> getChildren()
   {
     return children;
   }
 
   @Override
   public void render(Writer writer) throws IOException
   {
     for (Component child : getChildren())
     {
       child.render(writer);
     }
   }
   
   public static class ChildList<C extends Component> extends ArrayList<C>
   {
     /** serial Version UID. */
     private static final long serialVersionUID = 1L;
 
     public ChildList()
     {
     }
     
     public ChildList(Collection<? extends C> toCopy)
     {
      super.addAll(toCopy);
     }
     
     @Override
     public boolean add(C component)
     {
       component.inLoop(size());
       return super.add(component);
     }
 
     @Override
     public void add(int index, C element)
     {
       // Currently there is no way to shift the index
       // this would lead to duplicate index variables, so disallow
       throw new UnsupportedOperationException(
           "Currently the index of child components in a ChildListComponent cannot be changed");
     }
 
     @Override
     public boolean addAll(Collection<? extends C> componentList)
     {
       for (C component : componentList)
       {
         add(component);
       }
       return componentList.size() > 0;
     }
 
     @Override
     public boolean addAll(int index, Collection<? extends C> c)
     {
       // Currently there is no way to shift the index
       // this would lead to duplicate index variables, so disallow
       throw new UnsupportedOperationException(
           "Currently the index of child components in a ChildListComponent cannot be changed");
     }
 
     @Override
     public boolean remove(Object o)
     {
       // Currently there is no way to shift the index
       // this would lead to gaps in the index variables, so disallow
       // TODO check whether this is a necessary restriction
       throw new UnsupportedOperationException(
           "Currently the index of child components in a ChildListComponent cannot be changed");
     }
 
     @Override
     public C remove(int index)
     {
       // Currently there is no way to shift the index
       // this would lead to gaps in the index variables, so disallow
       // TODO check whether this is a necessary restriction
       throw new UnsupportedOperationException(
           "Currently the index of child components in a ChildListComponent cannot be changed");
     }
 
     @Override
     public boolean removeAll(Collection<?> c)
     {
       // Currently there is no way to shift the index
       // this would lead to gaps in the index variables, so disallow
       // TODO check whether this is a necessary restriction
       throw new UnsupportedOperationException(
           "Currently the index of child components in a ChildListComponent cannot be changed");
     }
 
     @Override
     public boolean retainAll(Collection<?> c)
     {
       // Currently there is no way to shift the index
       // this would lead to gaps in the index variables, so disallow
       // TODO check whether this is a necessary restriction
       throw new UnsupportedOperationException(
           "Currently the index of child components in a ChildListComponent cannot be changed");
     }
 
     @Override
     public C set(int index, C component)
     {
       component.inLoop(size());
       return super.set(index, component);
     }
     
     public ChildList<C> copy()
     {
       return new ChildList<C>(this);
     }
   }
 }
