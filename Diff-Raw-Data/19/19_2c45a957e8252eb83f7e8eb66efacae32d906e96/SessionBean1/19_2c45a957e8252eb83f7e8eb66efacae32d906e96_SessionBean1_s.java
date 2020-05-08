 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
  
 package cnery;
 
 import cnerydb.HibernateUtil;
 import cnerydb.Scene;
 import cnerydb.SceneAction;
 import cnerydb.SceneActionCondition;
 import cnerydb.SceneActivation;
 import cnerydb.SceneCondition;
 import com.daveoxley.cbus.Application;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cbus.Network;
 import com.daveoxley.cbus.Project;
 import com.sun.data.provider.impl.ObjectListDataProvider;
 import com.sun.rave.web.ui.appbase.AbstractSessionBean;
 import com.sun.webui.jsf.model.Option;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.faces.FacesException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 /**
  * <p>Session scope data bean for your application.  Create properties
  *  here to represent cached data that should be made available across
  *  multiple HTTP requests for an individual user.</p>
  *
  * <p>An instance of this class will be created for you automatically,
  * the first time your application evaluates a value binding expression
  * or method binding expression that references a managed bean using
  * this class.</p>
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class SessionBean1 extends AbstractSessionBean {
 
     private final static Log log = LogFactory.getLog(SessionBean1.class);
 
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     // </editor-fold>
 
     /**
      * <p>Construct a new session data bean instance.</p>
      */
     public SessionBean1() {
     }
 
     /**
      * <p>This method is called when this bean is initially added to
      * session scope.  Typically, this occurs as a result of evaluating
      * a value binding or method binding expression, which utilizes the
      * managed bean facility to instantiate this bean and store it into
      * session scope.</p>
      * 
      * <p>You may customize this method to initialize and cache data values
      * or resources that are required for the lifetime of a particular
      * user session.</p>
      */
     @Override
     public void init() {
         // Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
         
         // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("SessionBean1 Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
 
         // Fill in the sceneOptions[]
         updateScenes();
     }
 
     /**
      * <p>This method is called when the session containing it is about to be
      * passivated.  Typically, this occurs in a distributed servlet container
      * when the session is about to be transferred to a different
      * container instance, after which the <code>activate()</code> method
      * will be called to indicate that the transfer is complete.</p>
      * 
      * <p>You may customize this method to release references to session data
      * or resources that can not be serialized with the session itself.</p>
      */
     @Override
     public void passivate() {
     }
 
     /**
      * <p>This method is called when the session containing it was
      * reactivated.</p>
      * 
      * <p>You may customize this method to reacquire references to session
      * data or resources that could not be serialized with the
      * session itself.</p>
      */
     @Override
     public void activate() {
     }
 
     /**
      * <p>This method is called when this bean is removed from
      * session scope.  Typically, this occurs as a result of
      * the session timing out or being terminated by the application.</p>
      * 
      * <p>You may customize this method to clean up resources allocated
      * during the execution of the <code>init()</code> method, or
      * at any later time during the lifetime of the application.</p>
      */
     @Override
     public void destroy() {
     }
     
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected ApplicationBean1 getApplicationBean1() {
         return (ApplicationBean1) getBean("ApplicationBean1");
     }
 
     void updateScenes() {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             Query q = session.createQuery("from Scene");
             List<Scene> _scenes = (List<Scene>)q.list();
             setScenes(_scenes);
 
             sceneOptions = new Option[_scenes.size()];
 
             int i=0;
             String sceneDebugMessage = "updateScenes - ";
             boolean first = true;
             for(Scene scene : _scenes) {
                 Option opt = new Option(scene.getId(), scene.getName());
                 if (!first)
                     sceneDebugMessage += ", ";
                 first = false;
                 sceneDebugMessage += "Scene: " + scene.getId() + ", " + scene.getName();
                 sceneOptions[i++] = opt;
             }
             log.debug(sceneDebugMessage);
         } catch(Exception e) {
             new CGateException(e);
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     public void updateSceneConditions()
     {
         if (scene == null) {
             setSceneCondition(null);
             setSceneConditions(new ArrayList<SceneCondition>());
             return;
         }
 
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             Query q = session.createQuery("from SceneCondition where scene_id=:id").setString("id", scene.getId());
             List<SceneCondition> _sceneConditions = (List<SceneCondition>)q.list();
             setSceneConditions(_sceneConditions);
 
             String sceneDebugMessage = "updateSceneConditions - ";
             boolean first = true;
             for(SceneCondition sceneCondition : _sceneConditions) {
                 if (!first)
                     sceneDebugMessage += ", ";
                 first = false;
                 sceneDebugMessage += "SceneCondition: " + sceneCondition.getId();
             }
             log.debug(sceneDebugMessage);
         } catch(Exception e) {
             new CGateException(e);
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     public void updateSceneActivations()
     {
         if (scene == null) {
             setSceneActivation(null);
             setSceneActivations(new ArrayList<SceneActivation>());
             return;
         }
 
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             Query q = session.createQuery("from SceneActivation where scene_id=:id").setString("id", scene.getId());
             List<SceneActivation> _sceneActivations = (List<SceneActivation>)q.list();
             setSceneActivations(_sceneActivations);
 
             String sceneDebugMessage = "updateSceneActivations - ";
             boolean first = true;
             for(SceneActivation sceneActivation : _sceneActivations) {
                 if (!first)
                     sceneDebugMessage += ", ";
                 first = false;
                 sceneDebugMessage += "SceneActivation: " + sceneActivation.getId();
             }
             log.debug(sceneDebugMessage);
         } catch(Exception e) {
             new CGateException(e);
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     public void updateSceneActions()
     {
         if (scene == null) {
             setSceneAction(null);
             setSceneActions(new ArrayList<SceneAction>());
             return;
         }
 
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             Query q = session.createQuery("from SceneAction where scene_id=:id").setString("id", scene.getId());
             List<SceneAction> _sceneActions = (List<SceneAction>)q.list();
             setSceneActions(_sceneActions);
 
             String sceneDebugMessage = "updateSceneActions - ";
             boolean first = true;
             for(SceneAction sceneAction : _sceneActions) {
                 if (!first)
                     sceneDebugMessage += ", ";
                 first = false;
                 sceneDebugMessage += "SceneAction: " + sceneAction.getId() + ", " + sceneAction.getAddress() + ", " + sceneAction.getDelay();
             }
             log.debug(sceneDebugMessage);
         } catch(Exception e) {
             new CGateException(e);
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     public void updateSceneActionConditions()
     {
         if (sceneAction == null) {
             setSceneActionCondition(null);
             setSceneActionConditions(new ArrayList<SceneActionCondition>());
             return;
         }
 
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             Query q = session.createQuery("from SceneActionCondition where scene_action_id=:id").setString("id", sceneAction.getId());
             List<SceneActionCondition> _sceneActionConditions = (List<SceneActionCondition>)q.list();
             setSceneActionConditions(_sceneActionConditions);
 
             String sceneDebugMessage = "updateSceneActionConditions - ";
             boolean first = true;
             for(SceneActionCondition sceneActionCondition : _sceneActionConditions) {
                 if (!first)
                     sceneDebugMessage += ", ";
                 first = false;
                 sceneDebugMessage += "SceneActionCondition: " + sceneActionCondition.getId();
             }
             log.debug(sceneDebugMessage);
         } catch(Exception e) {
             new CGateException(e);
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     Project project;
 
     Network network;
 
     Application application;
 
     private Option[] sceneOptions;
 
     /**
      * Get the value of sceneOptions
      *
      * @return the value of sceneOptions
      */
     public Option[] getSceneOptions() {
         return sceneOptions;
     }
 
     /**
      * Set the value of sceneOptions
      *
      * @param sceneOptions new value of sceneOptions
      */
     public void setSceneOptions(Option[] sceneOptions) {
         this.sceneOptions = sceneOptions;
     }
 
     private final ObjectListDataProvider scenes = new ObjectListDataProvider(Scene.class);
 
     /**
      * Get the value of scenes
      *
      * @return the value of scenes
      */
     public ObjectListDataProvider getScenes() {
         return scenes;
     }
 
     /**
      * Set the value of scenes
      *
      * @param scenes new value of scenes
      */
     public void setScenes(List<Scene> scenes) {
         Collections.sort(scenes);
         this.scenes.setList(scenes);
     }
 
     private Scene scene;
 
     public Scene getScene() {
         return scene;
     }
 
     public void setScene(Scene scene) {
         this.scene = scene;
         updateSceneActions();
     }
 
     private Scene scene2;
 
     public Scene getScene2() {
         return scene2;
     }
 
     public void setScene2(Scene scene2) {
         this.scene2 = scene2;
     }
 
     public String getSelectedSceneId() {
         return scene == null ? "" : scene.getId();
     }
 
     public void setSelectedSceneId(String sceneId) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
            setScene((Scene)session.get(Scene.class, sceneId));
         } catch(Exception e) {
            e.printStackTrace();
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     public String getSelectedSceneId2() {
         return scene2 == null ? "" : scene2.getId();
     }
 
     public void setSelectedSceneId2(String sceneId) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
            setScene2((Scene)session.get(Scene.class, sceneId));
         } catch(Exception e) {
            e.printStackTrace();
         } finally {
             if (tx != null) tx.commit();
         }
     }
 
     private final ObjectListDataProvider sceneConditions = new ObjectListDataProvider(SceneCondition.class);
 
     /**
      * Get the value of scenes
      *
      * @return the value of scenes
      */
     public ObjectListDataProvider getSceneConditions() {
         return sceneConditions;
     }
 
     /**
      * Set the value of scenes
      *
      * @param scenes new value of scenes
      */
     public void setSceneConditions(List<SceneCondition> sceneConditions) {
         this.sceneConditions.setList(sceneConditions);
     }
 
     private SceneCondition sceneCondition;
 
     public SceneCondition getSceneCondition() {
         return sceneCondition;
     }
 
     public void setSceneCondition(SceneCondition sceneCondition) {
         this.sceneCondition = sceneCondition;
     }
 
     private final ObjectListDataProvider sceneActivations = new ObjectListDataProvider(SceneActivation.class);
 
     /**
      * Get the value of scenes
      *
      * @return the value of scenes
      */
     public ObjectListDataProvider getSceneActivations() {
         return sceneActivations;
     }
 
     /**
      * Set the value of scenes
      *
      * @param scenes new value of scenes
      */
     public void setSceneActivations(List<SceneActivation> sceneActivations) {
         Collections.sort(sceneActivations);
         this.sceneActivations.setList(sceneActivations);
     }
 
     private SceneActivation sceneActivation;
 
     public SceneActivation getSceneActivation() {
         return sceneActivation;
     }
 
     public void setSceneActivation(SceneActivation sceneActivation) {
         this.sceneActivation = sceneActivation;
     }
 
     private final ObjectListDataProvider sceneActions = new ObjectListDataProvider(SceneAction.class);
 
     /**
      * Get the value of scenes
      *
      * @return the value of scenes
      */
     public ObjectListDataProvider getSceneActions() {
         return sceneActions;
     }
 
     /**
      * Set the value of scenes
      *
      * @param scenes new value of scenes
      */
     public void setSceneActions(List<SceneAction> sceneActions) {
         Collections.sort(sceneActions);
         this.sceneActions.setList(sceneActions);
     }
 
     private SceneAction sceneAction;
 
     public SceneAction getSceneAction() {
         return sceneAction;
     }
 
     public void setSceneAction(SceneAction sceneAction) {
         this.sceneAction = sceneAction;
         updateSceneActionConditions();
     }
 
     private final ObjectListDataProvider sceneActionConditions = new ObjectListDataProvider(SceneActionCondition.class);
 
     public ObjectListDataProvider getSceneActionConditions() {
         return sceneActionConditions;
     }
 
     public void setSceneActionConditions(List<SceneActionCondition> sceneActionConditions) {
         this.sceneActionConditions.setList(sceneActionConditions);
     }
 
     private SceneActionCondition sceneActionCondition;
 
     public SceneActionCondition getSceneActionCondition() {
         return sceneActionCondition;
     }
 
     public void setSceneActionCondition(SceneActionCondition sceneActionCondition) {
         this.sceneActionCondition = sceneActionCondition;
     }
 
     private Option[] allGroupsOptions;
 
     public synchronized Option[] getAllGroupsOptions() {
         try {
             if (allGroupsOptions == null) {
                 ArrayList<Option> options = new ArrayList<Option>();
                 for (Network network : Network.listAll(ApplicationBean1.getCGateSession(), true)) {
                     for (Application application : network.getApplications(true)) {
                         for (Group group : application.getGroups(true)) {
                             options.add(new Option(group.getAddress(), group.getAddress() + " : " + group.getName()));
                         }
                     }
                 }
                 allGroupsOptions = options.toArray(new Option[]{});
             }
         } catch(Exception e) {
             new CGateException(e);
         }
         return allGroupsOptions;
     }
 }
