 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.javafx.listener;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javafx.collections.ListChangeListener;
 import javafx.collections.ObservableList;
 import javafx.event.EventHandler;
 import javafx.geometry.Point2D;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import org.eclipse.jubula.rc.common.AUTServerConfiguration;
 import org.eclipse.jubula.rc.common.exception.ComponentNotFoundException;
 import org.eclipse.jubula.rc.common.exception.ComponentNotManagedException;
 import org.eclipse.jubula.rc.common.exception.NoIdentifierForComponentException;
 import org.eclipse.jubula.rc.common.listener.BaseAUTListener;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.javafx.components.AUTJavaFXHierarchy;
 import org.eclipse.jubula.rc.javafx.components.CurrentStages;
 import org.eclipse.jubula.rc.javafx.components.FindJavaFXComponentBP;
 import org.eclipse.jubula.rc.javafx.components.JavaFXComponent;
 import org.eclipse.jubula.rc.javafx.listener.sync.IStageResizeSync;
 import org.eclipse.jubula.rc.javafx.listener.sync.StageResizeSyncFactory;
 import org.eclipse.jubula.rc.javafx.util.NodeBounds;
 import org.eclipse.jubula.tools.constants.TimingConstantsServer;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.ComponentClass;
 
 /**
  * This class is responsible for handling the components of the AUT. <br>
  *
  * The static methods for fetching an identifier for a component and getting the
  * component for an identifier delegates to this AUTHierarchy.
  *
  * @author BREDEX GmbH
  * @created 10.10.2013
  */
 public class ComponentHandler implements ListChangeListener<Stage>,
         BaseAUTListener {
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
             ComponentHandler.class);
 
     /** the Container hierarchy of the AUT */
     private static AUTJavaFXHierarchy hierarchy = new AUTJavaFXHierarchy();
 
     /** Businessprocess for getting components */
     private static FindJavaFXComponentBP findBP = new FindJavaFXComponentBP();
 
     /** used for synchronizing on stage resize events */
     private static IStageResizeSync stageResizeSync = 
             StageResizeSyncFactory.instance();
     
     /**
      * Constructor. Adds itself as ListChangeListener to the Stages-List
      */
     public ComponentHandler() {
         CurrentStages.addStagesListener(this);
     }
 
     @Override
     public void onChanged(Change<? extends Stage> change) {
         change.next();
 
         for (final Stage stage : change.getRemoved()) {
             hierarchy.removeComponentFromHierarchy(stage);
         }
         
         for (final Stage stage : change.getAddedSubList()) {
            stage.addEventFilter(WindowEvent.WINDOW_SHOWN,
                    new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        hierarchy.createHierarchyFrom(stage);
                        stageResizeSync.register(stage);
                        stage.removeEventFilter(WindowEvent.WINDOW_SHOWN, this);
                    }
                });
 
         }
     }
 
     @Override
     public long[] getEventMask() {
         return null;
     }
 
     /**
      * @return the Container hierarchy of the AUT
      */
     public static AUTJavaFXHierarchy getAutHierarchy() {
         return hierarchy;
     }
 
     /**
      * Searches the hierarchy-map for components that are assignable from the
      * given type
      *
      * @param type
      *            the type to look for
      *            
      * @param <T> component type
      * @return List
      */
     public static <T> List<? extends T> getAssignableFrom(Class<T> type) {
         Set<JavaFXComponent> keys = hierarchy.getHierarchyMap().keySet();
         List<T> result = new ArrayList<T>();
         for (JavaFXComponent object : keys) {
             if (type.isAssignableFrom(object.getRealComponentType())) {
                 result.add(type.cast(object.getRealComponent()));
             }
         }
         return result;
     }
 
     /**
      * Returns the node under the given point
      *
      * @param pos
      *            the point
      * @return the component
      */
     public static Node getComponentByPos(Point2D pos) {
         List<? extends Node> comps = getAssignableFrom(Node.class);
         List<Node> matches = new ArrayList<Node>();
         for (Node n : comps) {
             if (n.getScene() == null) {
                 continue;
             }
             Set supportetTypes = AUTServerConfiguration.getInstance().
                     getSupportedTypes();
             for (Object object : supportetTypes) {
                 if (((ComponentClass)object).getName().
                         equals(n.getClass().getName()) 
                         && NodeBounds.checkIfContains(pos, n)) {
                     matches.add(n);
                 }
             }
         }
         
         if (matches.size() == 0) {
             return null;
         }
         if (matches.size() == 1) {
             return matches.get(0);
         }
 
         // multiple matches, try filtering
         return filterMatches(matches);
     }
 
     /**
      * Filters out all parent in a list of matches
      * @param matches the matches
      * @return the filtered list
      */
     private static Node filterMatches(List<Node> matches) {
         
         List<Node> filteredMatches = filterOutUnfocussedNodes(matches);
         if (filteredMatches.size() == 1) {
             return filteredMatches.get(0);
         }
         
         Node firstCommonAncestor = findFirstCommonAncestor(filteredMatches);
         /* Always of type Parent */
         if (firstCommonAncestor != null) {
             return topMostDescendant(
                     (Parent)firstCommonAncestor, filteredMatches);
         }
         return null;
     }
     
     /**
      * Filters out nodes from unfocused windows from a given list
      * @param matches the list
      * @return list containing only the nodes of focused window
      */
     private static List<Node> filterOutUnfocussedNodes(List<Node> matches) {
         List<Node> filteredMatches = new ArrayList<Node>();
         for (Node match : matches) {
             if (match.getScene().getWindow().isFocused()) {
                 filteredMatches.add(match);
             }
         }
         return filteredMatches;
     }
 
     /**
      * Returns all instances of the type Node from a given list which are 
      * descendants of a given parent node
      * @param parent the parent
      * @param matches list of possible descendants
      * @return all descendants of the parent which also occur in the list
      */
     private static List<Node> filterDescendants(Parent parent, 
             List<Node> matches) {
         List<Node> descendants = new ArrayList<Node>();
         for (Node match : matches) {
             if (isDescendant(match, parent)) {
                 descendants.add(match);
             }
         }
         return descendants;
     }
     
     /**
      * Returns the descendant of a parent node from a given list which has 
      * the highest "z-coordinate".
      * @param parent the parent
      * @param matches the list of descendants
      * @return the top-most descendant from the list
      */
     private static Node topMostDescendant(Parent parent, List<Node> matches) {
         ObservableList<Node> children = parent.getChildrenUnmodifiable();
         ArrayList<Node> revertedChildren = new ArrayList<Node>(children);
         Collections.reverse(revertedChildren);
         // Start by checking if the last child of the StackPane is among the matches
         for (Node child : revertedChildren) {
             if (child instanceof Parent) {
                 List<Node> remainingMatches = 
                         filterDescendants((Parent)child, matches);
                 if (remainingMatches.size() == 1) {
                     return remainingMatches.get(0);
                 } else if (remainingMatches.size() > 1) {
                     return topMostDescendant(
                             ((Parent) child), remainingMatches);
                 }
             } 
             if (matches.contains(child)) {
                 return child;
             }
         }
         return null;
     }
 
     /**
      * Checks whether a node is a descendant of a node
      * @param candidate the possible descendant
      * @param node the node
      * @return whether the candidate is a descendant of the other node
      */
     private static boolean isDescendant(Node candidate, Node node) {
         if (candidate == null) {
             return false;
         } else if (candidate == node) {
             return true;
         }
         return isDescendant(candidate.getParent(), node);
     }
     
     /**
      * Finds the first common ancestor of a list of nodes
      * @param nodelist the list
      * @return the first common ancestor
      */
     private static Node findFirstCommonAncestor(List<Node> nodelist) {
         if (nodelist == null || nodelist.size() <= 0) {
             return null;
         } else if (nodelist.size() == 1) {
             return nodelist.get(0);
         } else {
             return findFirstCommonAncestor(nodelist.get(0), 
                     findFirstCommonAncestor(nodelist.subList(
                             1, nodelist.size())));
         }
     }
     
     /**
      * Finds the first common ancestor of two nodes
      * @param node1 first node
      * @param node2 second node
      * @return their first common ancestor
      */
     private static Node findFirstCommonAncestor(Node node1, Node node2) {
         if (node1 == null || node2 == null) {
             return null;
         }
         if (isDescendant(node1, node2)) {
             return node2;
         }
         return findFirstCommonAncestor(node1, node2.getParent());
     }
 
 
     /**
      * Investigates the given <code>component</code> for an identifier. It must
      * be distinct for the whole AUT. To obtain this identifier the AUTHierarchy
      * is queried.
      *
      * @param node
      *            the node to get an identifier for
      * @throws NoIdentifierForComponentException
      *             if an identifier could not created for <code>component</code>
      *             .
      * @return the identifier, containing the identification
      */
     public static IComponentIdentifier getIdentifier(Node node)
         throws NoIdentifierForComponentException {
 
         try {
             return hierarchy.getComponentIdentifier(node);
         } catch (ComponentNotManagedException cnme) {
             log.warn(cnme);
             throw new NoIdentifierForComponentException(
                     "unable to create an identifier for '" //$NON-NLS-1$
                             + node + "'", //$NON-NLS-1$
                     MessageIDs.E_COMPONENT_ID_CREATION);
         }
     }
 
     /**
      * Finds a Node by id
      *
      * @param id
      *            the id
      * @return the node ore null if there is nothing or something else than a
      *         node found
      */
     public static Node findNodeByID(IComponentIdentifier id) {
         Object comp = findBP.findComponent(id, hierarchy);
         if (comp != null && comp instanceof Node) {
             return (Node) comp;
         }
         return null;
     }
 
     /**
      * Searchs the component in the AUT, which belongs to the given
      * <code>componentIdentifier</code>.
      *
      * @param componentIdentifier
      *            the identifier of the component to search for
      * @param retry
      *            number of tries to get object
      * @param timeout
      *            timeout for retries
      * @throws ComponentNotFoundException
      *             if no component is found for the given identifier.
      * @throws IllegalArgumentException
      *             if the identifier is null or contains invalid data
      *             {@inheritDoc}
      * @return the found component
      */
     public static Object findComponent(
         IComponentIdentifier componentIdentifier, boolean retry, int timeout)
         throws ComponentNotFoundException, IllegalArgumentException {
 
         long start = System.currentTimeMillis();
         ReentrantLock lock = hierarchy.getLock();
         try {
             return hierarchy.findComponent(componentIdentifier);
         } catch (ComponentNotManagedException cnme) {
             if (retry) {
 
                 while (System.currentTimeMillis() - start < timeout) {
                     try {
                         lock.lock();
                         return hierarchy.findComponent(componentIdentifier);
                     } catch (ComponentNotManagedException e) { // NOPMD by zeb
                                                                // on 10.04.07
                                                                // 15:25
                         // OK, we will throw a corresponding exception later
                         // if we really can't find the component
                         lock.unlock();
                         try {
                             Thread.sleep(TimingConstantsServer.
                                     POLLING_DELAY_FIND_COMPONENT);
                         } catch (InterruptedException e1) {
                             // ok
                         }
                     } catch (InvalidDataException ide) { // NOPMD by zeb on
                                                          // 10.04.07 15:25
                         // OK, we will throw a corresponding exception later
                         // if we really can't find the component
                     }
                 }
             }
             throw new ComponentNotFoundException(cnme.getMessage(),
                     MessageIDs.E_COMPONENT_NOT_FOUND);
         } catch (IllegalArgumentException iae) {
             log.error(iae);
             throw iae;
         } catch (InvalidDataException ide) {
             log.error(ide);
             throw new ComponentNotFoundException(ide.getMessage(),
                     MessageIDs.E_COMPONENT_NOT_FOUND);
         } finally {
             if (lock.isHeldByCurrentThread()) {
                 lock.unlock();
             }
 
         }
     }
     
     /**
      * Blocks the calling thread until the Stage has been sufficiently resized
      * to deliver reliable component bounds. May not be called on the FX Thread.
      */
     public static void syncStageResize() {
         stageResizeSync.await();
     }
 }
