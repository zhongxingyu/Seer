 package fr.liglab.adele.cube.autonomicmanager.resolver;
 
 import fr.liglab.adele.cube.AutonomicManager;
 import fr.liglab.adele.cube.autonomicmanager.*;
 import fr.liglab.adele.cube.autonomicmanager.comm.CommunicationException;
 import fr.liglab.adele.cube.autonomicmanager.comm.TimeOutException;
 import fr.liglab.adele.cube.extensions.ResolverExtensionPoint;
 import fr.liglab.adele.cube.extensions.core.model.Component;
 import fr.liglab.adele.cube.extensions.core.model.Master;
 import fr.liglab.adele.cube.extensions.core.model.Node;
 import fr.liglab.adele.cube.extensions.core.model.Scope;
 import fr.liglab.adele.cube.metamodel.*;
 import fr.liglab.adele.cube.util.model.ModelUtils;
 import fr.liglab.adele.cube.util.perf.ResolutionMeasure;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: debbabi
  * Date: 9/19/13
  * Time: 11:49 AM
  */
 public class ArchetypeResolverImpl implements ArchetypeResolver {
 
     private AutonomicManager am;
 
     private Map<String, ResolverExtensionPoint> resolvers;
 
     public ArchetypeResolverImpl(AutonomicManager autonomicManager) {
         this.am = autonomicManager;
         if (autonomicManager == null)
             throw new NullPointerException();
         if (autonomicManager != null) {
             autonomicManager.getRuntimeModelController().getRuntimeModel().addListener(this);
             resolvers = new HashMap<String, ResolverExtensionPoint>();
         }
     }
 
     //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     ///// FUNCTIONING ////////////////////////////////////////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     public void update(RuntimeModel rm, Notification notification) {
         switch (notification.getNotificationType()) {
             case RuntimeModelListener.UPDATED_RUNTIMEMODEL: {
                 for (ManagedElement me : am.getRuntimeModelController().getRuntimeModel().getElements(ManagedElement.INVALID)) {
                     resolveUncheckedInstance(me);
                 }
             } break;
         }
     }
 
     public void resolveUncheckedInstance(ManagedElement instance) {
         if (instance == null) return;
         if (instance.getState() == ManagedElement.INVALID) {
             //System.out.println(getAutonomicManager().getUri()+ " resolving "+instance.getName()+" ...");
             info("resolving INVALID "+instance.getName()+" '"+instance.getUUID()+"'...");
 
             ResolutionGraph rg = new ResolutionGraph(this);
 
             MultiValueVariable root = new MultiValueVariable(rg);
             rg.setRoot(root);
 
             try {
                 ManagedElement desc = (ManagedElement) instance.clone();
                 root.setDescription(desc);
                 root.addValue(instance.getUUID());
             } catch (CloneNotSupportedException e) {
                 e.printStackTrace();
             }
             ResolutionMeasure m = new ResolutionMeasure(getAutonomicManager().getUri(), instance.getName());
             if (instance.getName().equalsIgnoreCase(Component.NAME)) {
                 m.setComment(instance.getAttribute(Component.CORE_COMPONENT_TYPE));
             } else if (instance.getName().equalsIgnoreCase(Node.NAME)) {
                 m.setComment(instance.getAttribute(Node.CORE_NODE_TYPE));
             } else if (instance.getName().equalsIgnoreCase(Scope.NAME)) {
                 m.setComment(instance.getAttribute(Scope.CORE_SCOPE_ID));
             } else if (instance.getName().equalsIgnoreCase(Master.NAME)) {
                 m.setComment(instance.getAttribute(Master.NAME));
             }
             m.start();
             if (rg.resolve()) {
                 m.end();
                 m.setResolved(true);
                 info(instance.getName()+" '"+instance.getUUID()+"' is resolved!");
                 if (validateSolution(rg)) {
                     //am.getRuntimeModelController().getRuntimeModel().removeUnmanagedElements();
                     //am.getRuntimeModelController().getRuntimeModel().refresh();
                     am.getRuntimeModelController().getRuntimeModel().refresh();
                 }
             } else {
                 m.end();
                 m.setResolved(false);
                 //am.getRuntimeModelController().getRuntimeModel().removeUnmanagedElements();
                 info("no solution found for "+instance.getName()+": " + instance.getUUID());
             }
             m.calculate();
             getAutonomicManager().getAdministrationService().getPerformanceChecker().addResolutionMeasure(m);
             //instance.setInResolution(false);
             //am.getRuntimeModelController().getRuntimeModel().refresh();
         }
     }
 
     private boolean validateSolution(ResolutionGraph rg) {
         // TODO should check "am" attribute to check if it should added here or in another runtime model part
         Variable root = rg.getRoot();
         boolean changed = false;
         for (Constraint c : root.getConstraints()) {
             if (c instanceof GoalConstraint) {
                 String related = ((GoalConstraint) c).getCurrentSolution();
                 if (am.getRuntimeModelController().isLocalInstance(related)) {
 
                     ManagedElement me = am.getRuntimeModelController().getRuntimeModel().getManagedElement(related);
                     if (me != null) {
                         if (me.getAutonomicManager() != null && !me.getAutonomicManager().equalsIgnoreCase(am.getUri())) {
                             // should be in another am!
                             // 1. add to remote hash map
                             am.getExternalInstancesHandler().addExternalInstance(related, me.getAutonomicManager());
                             // 2. prepare table of references/ams - to be sent with the ME
                             // 3. move the ME
                             moveManagedElement(me, me.getAutonomicManager());
                             // 4. remove local instance
                             am.getRuntimeModelController().removeManagedElement(related);
 
                             //System.out.println("#### this instance "+related+" should be moved to and validated at a remote am!");
                             //am.getRuntimeModelController().getRuntimeModel().removeUnmanagedElements();
 
                         } else {
                             int state = am.getRuntimeModelController().getState(related);
                             if (state == ManagedElement.UNMANAGED) {
                                 am.getRuntimeModelController().getRuntimeModel().manage(related);
                                 changed = true;
                             }
                         }
                     }
                 } else {
                     //TODO validate the instance of the remote am!
                     //System.out.println("##### This instance "+related+" should be validated at a remote am!");
                    String am_uri = getAutonomicManager().getExternalInstancesHandler().getAutonomicManagerOfExternalInstance(related);
                    if (am_uri != null) {
                        refreshRemoteAM(am_uri);
                    } else {
                        System.out.println("WARNING: remote instance referenced but no am uri is found in the local AM!");
                    }
                 }
             }
         }
         String uuid = ((MultiValueVariable)root).getDescription().getUUID();
         ManagedElement me = am.getRuntimeModelController().getRuntimeModel().getManagedElement(uuid);
         if (me != null) {
             if (me.getState() == ManagedElement.INVALID) {
                 me.setState(ManagedElement.VALID);
                 changed = true;
             }
         }
         return changed;
     }
 
     //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     ///// HELPERS ////////////////////////////////////////////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     public boolean moveManagedElement(ManagedElement me, String am_uri){
         CMessage msg = new CMessage();
         msg.setTo(am_uri);
         msg.setReplyTo(am.getUri());
         msg.setFrom(am.getUri());
         msg.setAttachment(me);
         msg.setObject("resolution");
         msg.setBody("moveManagedElement");
         try {
             this.am.getCommunicator().sendMessage(msg);
         } catch (Exception e) {
         }
         return true;
     }
 
    public void refreshRemoteAM(String am_uri) {
        CMessage msg = new CMessage();
        msg.setTo(am_uri);
        msg.setReplyTo(am.getUri());
        msg.setFrom(am.getUri());
        msg.setObject("resolution");
        msg.setBody("refreshRemoteAM");
        try {
            this.am.getCommunicator().sendMessage(msg);
        } catch (Exception e) {
        }
    }

     public List<String> findFromRuntimeModel(ManagedElement description) {
 
         List<String> result = new ArrayList<String>();
         if (description != null) {
             if (description.getAutonomicManager() != null && !description.getAutonomicManager().equalsIgnoreCase(am.getUri())) {
                 //System.out.println("///// remote find RM");
                 String to = description.getAutonomicManager();
                 CMessage msg = new CMessage();
                 msg.setTo(to);
                 msg.setReplyTo(am.getUri());
                 msg.setFrom(am.getUri());
                 msg.setAttachment(description);
                 msg.setObject("resolution");
                 msg.setBody("findFromRuntimeModel");
                 //System.out.println("///// " + msg.toString());
                 try {
                     CMessage resultmsg = sendAndWait(msg);
                     if (resultmsg != null) {
                         //info("////////: receiving find from RM: "+resultmsg.toString());
                         if (resultmsg.getBody() != null) {
                             String[] tmp = resultmsg.getBody().toString().split(",");
                             for (int i=0; i<tmp.length; i++) {
                                 if (tmp[i] != null && tmp[i].length()>0) {
                                     String[] tmp2 = tmp[i].split("###");
                                     String agenturi = tmp2[0];
                                     String elementuuid = tmp2[1];
                                     if (!agenturi.equalsIgnoreCase(am.getUri())) {
 
                                         this.am.getExternalInstancesHandler().addExternalInstance(elementuuid, agenturi);
                                     }
                                     //System.out.println("************* adding "+ elementuuid);
                                     result.add(elementuuid);
                                 }
                             }
                         }
                     }
                 } catch (TimeOutException e) {
                     e.printStackTrace();
                 }
                 return result;
             }
             RuntimeModel rm = am.getRuntimeModelController().getRuntimeModel();
             for (ManagedElement mes : rm.getElements(description.getNamespace(), description.getName(), ManagedElement.VALID)) {
                 if (mes.isInResolution() == false) {
                     int compResult = ModelUtils.compareTwoManagedElements(description, mes);
                     if (compResult == 0) {
                         result.add(mes.getUUID());
                     } else {
 
                     }
                 }
             }
         }
         return result;
     }
 
     public List<String> findUsingArchetypeProperty(String archetypePropertyName, String uuid, ManagedElement description) {
         info( archetypePropertyName + ".find("+uuid+", "+description.getName()+")...");
         List<String> result = new ArrayList<String>();
         if (am.getRuntimeModelController().isLocalInstance(uuid) == true) {
             ManagedElement me = am.getRuntimeModelController().getRuntimeModel().getManagedElement(uuid);
             ResolverExtensionPoint r = am.getArchetypeResolver().getResolver(archetypePropertyName);
             if (r != null) {
                 result =  r.find(me, description);
             } else {
                 info(" WARNING! findUsingArchetypeProperty: no specific resolver was found for the archetype property '" + archetypePropertyName + "'!");
             }
         } else if (am.getRuntimeModelController().isRemoteInstance(uuid) == true) {
             String to = getAutonomicManager().getExternalInstancesHandler().getAutonomicManagerOfExternalInstance(uuid);
             CMessage msg = new CMessage();
             msg.setTo(to);
             msg.setReplyTo(am.getUri());
             msg.setFrom(am.getUri());
             msg.setAttachment(description);
             msg.addHeader("pname", archetypePropertyName);
             msg.addHeader("uuid", uuid);
             msg.setObject("resolution");
             msg.setBody("findUsingArchetypeProperty");
             //info("//////////////// find using archetype property: " + msg.toString());
             try {
                 CMessage resultmsg = sendAndWait(msg);
                 if (resultmsg != null) {
                     if (resultmsg.getBody() != null) {
                         String[] tmp = resultmsg.getBody().toString().split(",");
                         for (int i=0; i<tmp.length; i++) {
                             if (tmp[i] != null && tmp[i].length()>0) {
                                 String[] tmp2 = tmp[i].split("###");
                                 String agenturi = tmp2[0];
                                 String elementuuid = tmp2[1];
                                 if (!agenturi.equalsIgnoreCase(am.getUri())) {
                                     this.am.getExternalInstancesHandler().addExternalInstance(elementuuid, agenturi);
                                 }
                                 //System.out.println("************* adding "+ elementuuid);
                                 result.add(elementuuid);
                             }
                         }
                     }
                 }
             } catch (TimeOutException e) {
                 e.printStackTrace();
             }
             return result;
         }
         return result;
     }
 
 
     public boolean checkProperty(String archetypePropertyName, ManagedElement managedElement, String value) {
         ResolverExtensionPoint r = am.getArchetypeResolver().getResolver(archetypePropertyName);
         if (r != null) {
             boolean res =  r.check(managedElement, value);
             return res;
         } else {
             System.out.println("[WARNING] ArchetypeResolver: no specific resolver was found for the archetype property '"+archetypePropertyName+"'!");
         }
         return true;
     }
 
     public boolean verifyProperty(String archetypePropertyName, String uuid, String value) {
         info( archetypePropertyName + ".verify("+uuid+", "+value+")...");
         boolean result = false;
         if (am.getRuntimeModelController().isLocalInstance(uuid) == true) {
             ManagedElement me = am.getRuntimeModelController().getRuntimeModel().getManagedElement(uuid);
             ResolverExtensionPoint r = am.getArchetypeResolver().getResolver(archetypePropertyName);
             if (r != null) {
                 result =  r.check(me, value);
             } else {
                 info(" WARNING! verifyProperty: no specific resolver was found for the archetype property '" + archetypePropertyName + "'!");
             }
         } else if (am.getRuntimeModelController().isRemoteInstance(uuid) == true) {
             String to = getAutonomicManager().getExternalInstancesHandler().getAutonomicManagerOfExternalInstance(uuid);
             CMessage msg = new CMessage();
             msg.setTo(to);
             msg.setReplyTo(am.getUri());
             msg.setFrom(am.getUri());
             msg.addHeader("pname", archetypePropertyName);
             msg.addHeader("uuid", uuid);
             msg.addHeader("value", value);
             msg.setObject("resolution");
             msg.setBody("verifyProperty");
             try {
                 CMessage resultmsg = sendAndWait(msg);
                 if (resultmsg != null) {
                     if (resultmsg.getBody() != null) {
                         if (resultmsg.getBody().toString().equalsIgnoreCase("true")) {
                             return true;
                         } else {
                             return false;
                         }
                     }
                 }
             } catch (TimeOutException e) {
                 e.printStackTrace();
             }
         }
         return result;
     }
 
     public boolean performProperty(String archetypePropertyName, ManagedElement managedElement, String value) {
         ResolverExtensionPoint r = am.getArchetypeResolver().getResolver(archetypePropertyName);
         if (r != null) {
             return r.perform(managedElement, value);
         } else {
             System.out.println("[WARNING] ArchetypeResolver: no specific resolver was found for the archetype property '"+archetypePropertyName+"'!");
         }
         return false;
     }
 
     public boolean performProperty(String archetypePropertyName, String uuid, String value) {
         if (uuid != null) {
             if (am.getRuntimeModelController().isLocalInstance(uuid)) {
                 ManagedElement managedElement = am.getRuntimeModelController().getRuntimeModel().getManagedElement(uuid);
                 if (managedElement != null) {
                     ResolverExtensionPoint r = am.getArchetypeResolver().getResolver(archetypePropertyName);
                     if (r != null) {
                         return r.perform(managedElement, value);
                     } else {
                         System.out.println("[WARNING] ArchetypeResolver: no specific resolver was found for the archetype property '"+archetypePropertyName+"'!");
                     }
                 }
             } else {
                 System.out.println("\n[WARNING] ArchetypeResolver.performProperty: not implemented the case of remote instances!\n");
             }
         }
         return false;
     }
 
     public String createUsingDescription(ManagedElement description) {
         if (description != null) {
             Properties p = new Properties();
             for (Attribute a : description.getAttributes()) {
                 p.put(a.getName(), a.getValue());
             }
             ManagedElement me = null;
             try {
 
                 me = getAutonomicManager().getRuntimeModelController().newManagedElement(description.getNamespace(), description.getName(), p, true);
                 //System.out.println("#### me: " + me.getDocumentation());
             } catch (NotFoundManagedElementException e) {
                 e.printStackTrace();
             } catch (InvalidNameException e) {
                 e.printStackTrace();
             } catch (PropertyExistException e) {
                 e.printStackTrace();
             }
             if (me != null) {
                 return me.getUUID();
             }
         }
         return null;
     }
 
 
 
     //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     ///// UTILS //////////////////////////////////////////////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     public AutonomicManager getAutonomicManager() {
         return this.am;
     }
 
     private void info(String msg) {
         if (this.am.getConfiguration().isDebug() == true) {
             System.out.println("[RESOLVER:"+this.am.getUri()+":"+this.hashCode()+"] " + msg);
         }
     }
 
     public void addSpecificResolver(ResolverExtensionPoint sr) {
         this.resolvers.put(sr.getExtension().getExtensionFactory().getNamespace().toLowerCase()+":"+sr.getName().toLowerCase(), sr);
     }
 
     public ResolverExtensionPoint getResolver(String fullname) {
         return this.resolvers.get(fullname.toLowerCase());
     }
 
     //////////////////// HANDLE MESSAGES //////////////////////////////
 
     public CMessage sendAndWait(CMessage msg) throws TimeOutException {
         if (msg != null) {
             msg.setFrom(this.am.getUri());
             msg.setReplyTo(this.am.getUri());
             /*if (msg.getTo() != null && !msg.getTo().contains("/ext/"))
                 msg.setTo(msg.getTo() + "/ext/" + getExtensionFactory().getExtensionId());*/
             //String to = msg.getTo();
             msg.setCorrelation(++correlation);
             waitingCorrelation = msg.getCorrelation();
             //System.out.println(msg.toString());
             try {
                 this.waitingMessage = null;
 
                 this.am.getCommunicator().sendMessage(msg);
             } catch (Exception e) {
                 //this.getAutonomicManager().getLogger().warning("The Extension could not send a message to " + to + "!");
             }
             try {
                 long initialTime = System.currentTimeMillis();
                 long currentTime = initialTime;
                 long waitingTime = TIMEOUT;
                 synchronized (csplock) {
                     while (((currentTime < (initialTime + TIMEOUT)) && waitingTime > 1)
                             && (this.waitingMessage == null)) {
                         csplock.wait(waitingTime);
                         currentTime = System.currentTimeMillis();
                         waitingTime = waitingTime - (currentTime - initialTime);
                     }
                 }
             } catch (InterruptedException e) {
                 //this.getAutonomicManager().getLogger().warning("The Extension waits for a response message from " + to + " but no answer! timeout excedded!");
             }
             return this.waitingMessage;
         } else {
             return null;
         }
     }
 
     public void receiveMessage(CMessage msg) {
         if (msg.getCorrelation() == waitingCorrelation) {
             this.waitingMessage = msg;
             if (csplock != null) {
                 synchronized (csplock) {
                     csplock.notify();
                 }
             }
             waitingCorrelation = -1;
         }
         try {
             handleMessage(msg);
         }  catch(Exception ex) {
             //getAutonomicManager().getLogger().error("[AbstractExtension.receiveMessage] " + ex.getMessage());
         }
     }
 
     protected void handleMessage(CMessage msg) throws Exception {
 
         if (msg != null) {
             if (msg.getBody() != null) {
                if (msg.getBody().toString().equalsIgnoreCase("refreshRemoteAM")) {
                    getAutonomicManager().getRuntimeModelController().getRuntimeModel().refresh();
                } else
                 if (msg.getBody().toString().equalsIgnoreCase("moveManagedElement")) {
                     ManagedElement me = msg.getAttachment();
                     if (me != null) {
                         am.getRuntimeModelController().addManagedElement(me);
                     }
                 } else
                 if (msg.getBody().toString().equalsIgnoreCase("findFromRuntimeModel")) {
                     ManagedElement me = msg.getAttachment();
                     String resultat = "";
                     if (me != null ) {
                         List<String> res = findFromRuntimeModel(me);
                         for (String r : res) {
                             resultat += this.am.getRuntimeModelController().getAutonomicManagerOf(r) + "###" + r + ",";
                         }
                     }
                     CMessage resmsg = new CMessage();
                     resmsg.setFrom(am.getUri());
                     resmsg.setTo(msg.getFrom());
                     resmsg.setCorrelation(msg.getCorrelation());
                     resmsg.setObject(msg.getObject());
                     resmsg.setBody(resultat);
 
                     try {
                         am.getCommunicator().sendMessage(resmsg);
                     } catch (CommunicationException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 } else if (msg.getBody().toString().equalsIgnoreCase("findUsingArchetypeProperty")) {
                     //info("received.msg:\n"+msg.toString());
                     ManagedElement me = msg.getAttachment();
                     Object pname = msg.getHeader("pname");
                     Object uuid = msg.getHeader("uuid");
                     String resultat = "";
                     if (pname != null && uuid != null && me != null) {
                         List<String> res = findUsingArchetypeProperty(pname.toString(), uuid.toString(), me);
                         for (String r : res) {
                             resultat += this.am.getRuntimeModelController().getAutonomicManagerOf(r) + "###" + r + ",";
                         }
                     }
                     CMessage resmsg = new CMessage();
                     resmsg.setTo(msg.getFrom());
                     resmsg.setCorrelation(msg.getCorrelation());
                     resmsg.setObject(msg.getObject());
                     resmsg.setBody(resultat);
 
                     try {
                         am.getCommunicator().sendMessage(resmsg);
                     } catch (CommunicationException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 } else if (msg.getBody().toString().equalsIgnoreCase("verifyProperty")) {
                     Object pname = msg.getHeader("pname");
                     Object uuid = msg.getHeader("uuid");
                     Object value = msg.getHeader("value");
 
                     boolean p = false;
                     if (uuid != null && value != null) {
                         p = verifyProperty(pname.toString(), uuid.toString(), value.toString());
                     }
                     CMessage resmsg = new CMessage();
                     resmsg.setTo(msg.getFrom());
                     resmsg.setCorrelation(msg.getCorrelation());
                     resmsg.setObject(msg.getObject());
                     if (p == true)
                         resmsg.setBody("true");
                     else
                         resmsg.setBody("false");
                     try {
                         am.getCommunicator().sendMessage(resmsg);
                     } catch (CommunicationException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
 
     private CMessage waitingMessage = null;
     private long TIMEOUT = 3000;
     private Object csplock = new Object();
     private static long correlation = 1;
     private long waitingCorrelation = -1;
 }
