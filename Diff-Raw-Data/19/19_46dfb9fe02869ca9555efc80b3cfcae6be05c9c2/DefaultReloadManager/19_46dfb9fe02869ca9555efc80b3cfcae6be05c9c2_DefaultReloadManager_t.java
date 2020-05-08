 package org.wings;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.plaf.ComponentCG;
 import org.wings.plaf.Update;
 import org.wings.util.SStringBuilder;
 
 /**
  * Default implementation of the reload manager.
  *
  * @author Stephan Schuster
  */
 public class DefaultReloadManager implements ReloadManager {
 
     private final transient static Log log = LogFactory.getLog(DefaultReloadManager.class);
 
     private int updateCount = 0;
 
     private boolean updateMode = false;
 
     private boolean acceptChanges = true;
 
    private final Map<SComponent, PotentialUpdate> fullReplaceUpdates = new HashMap<SComponent, PotentialUpdate>(256);
 
    private final Map<SComponent, Set<PotentialUpdate>> fineGrainedUpdates = new HashMap<SComponent, Set<PotentialUpdate>>(64);
 
    /** 
     * All the components to reload. A LinkedHashSet to have an ordered
     * sequence that allows for fast lookup.
     */
    private final Set<SComponent> componentsToReload = new LinkedHashSet<SComponent>();
    
     public void reload(SComponent component) {
         if (component == null)
             throw new IllegalArgumentException("Component must not be null!");
 
         if (updateMode) {
             addUpdate(component, null);
         } else {
             if (!componentsToReload.contains(component)) {
                 componentsToReload.add(component);
             }
         }
     }
 
     public void addUpdate(SComponent component, Update update) {
         if (component == null)
             throw new IllegalArgumentException("Component must not be null!");
 
         if (update == null) {
             update = component.getCG().getComponentUpdate(component);
             if (update == null) {
                 SFrame frame = component.getParentFrame();
                 if (frame != null)
                     fullReplaceUpdates.put(frame, null);
                 return;
             }
         }
 
         component = update.getComponent();
 
         if (acceptChanges) {
             PotentialUpdate potentialUpdate = new PotentialUpdate(update);
 
             if ((update.getProperty() & Update.FULL_REPLACE_UPDATE) == Update.FULL_REPLACE_UPDATE) {
                 fullReplaceUpdates.put(component, potentialUpdate);
             } else {
                 Set<PotentialUpdate> potentialUpdates = getFineGrainedUpdates(component);
                 potentialUpdates.remove(potentialUpdate);
                 potentialUpdates.add(potentialUpdate);
                 fineGrainedUpdates.put(component, potentialUpdates);
             }
         } else if (log.isDebugEnabled()) {
             //log.debug("Component " + component + " changed after invalidation of frames.");
         }
     }
 
     @SuppressWarnings({"unchecked"})
     public List<Update> getUpdates() {
         if (!componentsToReload.isEmpty()) {
             for (SComponent aComponentsToReload : componentsToReload) {
                 boolean tmp = acceptChanges;
                 acceptChanges = true;
                 addUpdate(aComponentsToReload, null);
                 acceptChanges = tmp;
             }
         }
 
         filterUpdates();
 
         List<PotentialUpdate> filteredUpdates = new ArrayList<PotentialUpdate>(fullReplaceUpdates.values());
         for (Set<PotentialUpdate> updates : fineGrainedUpdates.values()) {
             filteredUpdates.addAll(updates);
         }
         Collections.sort(filteredUpdates, getUpdateComparator());
 
         return (List) filteredUpdates;
     }
 
     public Set<SComponent> getDirtyComponents() {
         final Set<SComponent> dirtyComponents = new HashSet<SComponent>();
         dirtyComponents.addAll(fullReplaceUpdates.keySet());
         dirtyComponents.addAll(fineGrainedUpdates.keySet());
         dirtyComponents.addAll(componentsToReload);
         return dirtyComponents;
     }
 
     public Set<SFrame> getDirtyFrames() {
         final Set<SFrame> dirtyFrames = new HashSet<SFrame>(5);
         for (Object o : getDirtyComponents()) {
             SFrame parentFrame = ((SComponent) o).getParentFrame();
             if (parentFrame != null) {
                 dirtyFrames.add(parentFrame);
             }
         }
         return dirtyFrames;
     }
 
     public void invalidateFrames() {
         Iterator i = getDirtyFrames().iterator();
         while (i.hasNext()) {
             ((SFrame) i.next()).invalidate();
             i.remove();
         }
         acceptChanges = false;
     }
 
     public void notifyCGs() {
         for (SComponent component : getDirtyComponents()) {
             ComponentCG componentCG = component.getCG();
             if (componentCG != null) {
                 componentCG.componentChanged(component);
             }
         }
     }
 
     public void clear() {
         updateCount = 0;
         updateMode = false;
         acceptChanges = true;
         fullReplaceUpdates.clear();
        fineGrainedUpdates.clear();        
         componentsToReload.clear();
     }
 
     public boolean isUpdateMode() {
         return updateMode;
     }
 
     public void setUpdateMode(boolean updateMode) {
         this.updateMode = updateMode;
     }
 
     public boolean isReloadRequired(SFrame frame) {
         if (updateMode)
             return fullReplaceUpdates.containsKey(frame);
         else
             return true;
     }
 
     protected Set<PotentialUpdate> getFineGrainedUpdates(SComponent component) {
         Set<PotentialUpdate> potentialUpdates = fineGrainedUpdates.get(component);
         if (potentialUpdates == null) {
             potentialUpdates = new HashSet<PotentialUpdate>(5);
         }
         return potentialUpdates;
     }
 
     protected void filterUpdates() {
         if (log.isDebugEnabled())
             printAllUpdates("Potential updates:");
 
         fineGrainedUpdates.keySet().removeAll(fullReplaceUpdates.keySet());
 
         SortedMap<String, SComponent> componentHierarchy = new TreeMap<String, SComponent>(new PathComparator());
 
         for (SComponent component : getDirtyComponents()) {
             if ((!component.isRecursivelyVisible() && !(component instanceof SMenu)) ||
                     component.getParentFrame() == null) {
                 fullReplaceUpdates.remove(component);
                 fineGrainedUpdates.remove(component);
             } else {
                 componentHierarchy.put(getPath(component).toString(), component);
             }
         }
 
         for (Iterator i = componentHierarchy.keySet().iterator(); i.hasNext();) {
             String topPath = (String) i.next();
             if (fullReplaceUpdates.containsKey(componentHierarchy.get(topPath))) {
                 while (i.hasNext()) {
                     String subPath = (String) i.next();
                     if (subPath.startsWith(topPath + "/")) {
                         fullReplaceUpdates.remove(componentHierarchy.get(subPath));
                         fineGrainedUpdates.remove(componentHierarchy.get(subPath));
                         i.remove();
                     }
                 }
             }
             i = componentHierarchy.tailMap(topPath + "\0").keySet().iterator();
         }
 
         if (log.isDebugEnabled())
             printAllUpdates("Effective updates:");
     }
 
     private SStringBuilder getPath(SComponent component) {
         if (component == null) {
             return new SStringBuilder();
         } else {
             if (component instanceof SMenuItem) {
                 SMenuItem menuItem = (SMenuItem) component;
                 return getPath(menuItem.getParentMenu()).append("/").append(component.getName());
             } else if (component instanceof SSpinner.DefaultEditor) {
                 SSpinner.DefaultEditor defaultEditor = (SSpinner.DefaultEditor) component;
                 return getPath(defaultEditor.getSpinner()).append("/").append(component.getName());
             } else {
                 return getPath(component.getParent()).append("/").append(component.getName());
             }
         }
     }
 
     private void printAllUpdates(String header) {
         log.debug(header);
         int numberOfUpdates = 0;
         SStringBuilder output = new SStringBuilder(512);
         for (SComponent component : getDirtyComponents()) {
             output.setLength(0);
             output.append("    ").append(component + ":");
             if (fullReplaceUpdates.containsKey(component)) {
                 output.append(" " + fullReplaceUpdates.get(component));
                 if (fullReplaceUpdates.get(component) == null) {
                     output.append(" [no component update supported --> reload frame!!!]");
                 }
                 ++numberOfUpdates;
             }
             for (PotentialUpdate potentialUpdate : getFineGrainedUpdates(component)) {
                 output.append(" " + potentialUpdate);
                 ++numberOfUpdates;
             }
             log.debug(output.toString());
         }
         log.debug("    --> " + numberOfUpdates + " updates");
     }
 
     private final class PotentialUpdate implements Update {
 
         private Update update;
         private int position;
 
         public PotentialUpdate(Update update) {
             this.update = update;
             this.position = updateCount++;
         }
 
         public SComponent getComponent() {
             return update.getComponent();
         }
 
         public Handler getHandler() {
             return update.getHandler();
         }
 
         public int getProperty() {
             return update.getProperty();
         }
 
         public int getPriority() {
             return update.getPriority();
         }
 
         public int getPosition() {
             return position;
         }
 
         @Override
         public boolean equals(Object object) {
             if (object == this)
                 return true;
             if (object == null || object.getClass() != this.getClass())
                 return false;
 
             PotentialUpdate other = (PotentialUpdate) object;
 
             return update.equals(other.update);
         }
 
         @Override
         public int hashCode() {
             return update.hashCode();
         }
 
         @Override
         public String toString() {
             String clazz = update.getClass().getName();
             int index = clazz.lastIndexOf("$");
             if (index < 0)
                 index = clazz.lastIndexOf(".");
             return clazz.substring(++index) + "[" + getPriority() + "|" + getPosition() + "]";
         }
 
     }
 
     private Comparator<PotentialUpdate> getUpdateComparator() {
         return
             new CombinedComparator<PotentialUpdate>(
                 new InverseComparator<PotentialUpdate>(new PriorityComparator()),
                 new PositionComparator()
             );
     }
 
     private static class PathComparator implements Comparator<String> {
 
         public int compare(String path1, String path2) {
             int depthOfPath1 = (new StringTokenizer(path1, ":")).countTokens();
             int depthOfPath2 = (new StringTokenizer(path2, ":")).countTokens();
             if (depthOfPath1 < depthOfPath2) return -1;
             if (depthOfPath1 > depthOfPath2) return 1;
             return path1.compareTo(path2);
         }
 
     }
 
     private static class PositionComparator implements Comparator<PotentialUpdate> {
 
         public int compare(PotentialUpdate object1, PotentialUpdate object2) {
             if (object1.getPosition() < object2.getPosition()) return -1;
             if (object1.getPosition() > object2.getPosition()) return 1;
             return 0;
         }
 
     }
 
     private static class PriorityComparator implements Comparator<PotentialUpdate> {
 
         public int compare(PotentialUpdate object1, PotentialUpdate object2) {
             if (object1.getPriority() < object2.getPriority()) return -1;
             if (object1.getPriority() > object2.getPriority()) return 1;
             return 0;
         }
 
     }
 
     private static class CombinedComparator<T> implements Comparator<T> {
 
         private Comparator<T> comparator1;
         private Comparator<T> comparator2;
 
         public CombinedComparator(Comparator<T> c1, Comparator<T> c2) {
             this.comparator1 = c1;
             this.comparator2 = c2;
         }
 
         public int compare(T object1, T object2) {
             int result = comparator1.compare(object1, object2);
             if (result == 0)
                 return comparator2.compare(object1, object2);
             else
                 return result;
         }
     }
 
     private static class InverseComparator<T> implements Comparator<T> {
 
         private Comparator<T> comparator;
 
         public InverseComparator(Comparator<T> c) {
             this.comparator = c;
         }
 
         public int compare(T object1, T object2) {
             return -comparator.compare(object1, object2);
         }
     }
 
 }
