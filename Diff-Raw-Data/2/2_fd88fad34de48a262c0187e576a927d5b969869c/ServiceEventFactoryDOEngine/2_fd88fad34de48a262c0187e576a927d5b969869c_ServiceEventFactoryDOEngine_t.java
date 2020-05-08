 package eu.trentorise.smartcampus.domain.discovertrento;
 
 import it.sayservice.platform.core.domain.common.exception.DomainDataHandlerException;
 import it.sayservice.platform.core.domain.DomainConst.DOMAIN_OBJECT_EVENT_TYPE;
 import it.sayservice.platform.core.domain.DomainRelationTarget;
 import it.sayservice.platform.core.domain.bundle.DomainEvent;
 import it.sayservice.platform.core.domain.ext.AbstractDOEngineImpl;
 import it.sayservice.platform.core.domain.ext.ActionInvoke;
 import it.sayservice.platform.core.domain.ext.DomainObjectWrapper;
 import it.sayservice.platform.core.domain.ext.LanguageHelper;
 import it.sayservice.platform.core.domain.ext.Tuple;
 import it.sayservice.platform.core.domain.rules.DomainSubscriptionRule;
 import it.sayservice.platform.core.domain.rules.EvaluableDomainSubscriptionRule;
 import it.sayservice.platform.core.domain.rules.EvaluableDomainOperation;
 import it.sayservice.platform.core.domain.rules.DomainEventDescriptor;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.mongodb.DBObject;
 import com.mongodb.QueryBuilder;
 
 public class ServiceEventFactoryDOEngine extends AbstractDOEngineImpl {
 
     public ServiceEventFactoryDOEngine() {
         super();
     }
 
     private static String[] actions = new String[]{
             "transformEvents",
     };
 
     protected String[] getSortedActions() {
         return actions;
     }
 
     private static Map<String,Collection<java.io.Serializable>> extensions = new HashMap<String,Collection<java.io.Serializable>>();
     static {
     }
 
      public  Collection<java.io.Serializable> getExtensionValues(String property) {
         return extensions.get(property);
      }  
     
     protected Object executeAction(String actionName, DomainObjectWrapper obj, Tuple t, Set<DomainEvent> outEvents, Set<EvaluableDomainOperation> ops, String securityToken, String bundleId) throws DomainDataHandlerException {
         if ("transformEvents".equals(actionName)) {
             return transformEvents(t, obj, outEvents, ops, securityToken, bundleId);
         }
         return null;
     }
 
     private Object transformEvents(Tuple tuple, DomainObjectWrapper obj, Set<DomainEvent> evts, Set<EvaluableDomainOperation> ops, String securityToken, String bundleId) throws DomainDataHandlerException {
         {
 eu.trentorise.smartcampus.domain.discovertrento.GenericEvent[] list = (eu.trentorise.smartcampus.domain.discovertrento.GenericEvent[]) tuple.get("list");
 {
 for (eu.trentorise.smartcampus.domain.discovertrento.GenericEvent e:list){
 {
 java.lang.Boolean found = false;
 if (getDomainObjectHandler().getVar("events",obj,eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.class,bundleId) != null){
 {
 java.lang.Boolean contained = eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.containsEvent(getDomainObjectHandler().getVar("events",obj,eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.class,bundleId),e);
 if (contained){
 {
 found = true;
 {
 Tuple body = new Tuple();
  body.put("id",e.getId());
  body.put("event",e);
 getDomainObjectHandler().publishCustomEvent("updateEvent", body, obj, evts, bundleId);}
}
 }
 }
 }
 if (!(found)){
 {
 {
 Tuple body = new Tuple();
  body.put("data",e);
  body.put("id",e.getId());
 getDomainObjectHandler().create(null, "eu.trentorise.smartcampus.domain.discovertrento.ServiceEventObject", body, evts, ops, securityToken, bundleId);}
 getDomainObjectHandler().setVar("events", obj, eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.addEvent(getDomainObjectHandler().getVar("events",obj,eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.class,bundleId),e), evts, bundleId);}
 }
 }
 }
 java.lang.String[] toRemove = eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.findEventsToDelete(getDomainObjectHandler().getVar("events",obj,eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.class,bundleId),list);
 for (java.lang.String id:toRemove){
 {
 List<DomainObjectWrapper> objs = _query___query_0(obj, securityToken, bundleId, id);
 if ((objs != null) && (LanguageHelper.count(objs)>0)){
 {
 DomainObjectWrapper o = objs.get(0);
 getDomainObjectHandler().setVar("events", obj, eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.deleteEvent(getDomainObjectHandler().getVar("events",obj,eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.class,bundleId),getDomainObjectHandler().getVar("data",o,eu.trentorise.smartcampus.domain.discovertrento.GenericEvent.class,bundleId).getSource(),getDomainObjectHandler().getVar("id",o,java.lang.String.class,bundleId)), evts, bundleId);{
 Tuple body = new Tuple();
  body.put("id",getDomainObjectHandler().getVar("id",o,java.lang.String.class,bundleId));
 getDomainObjectHandler().publishCustomEvent("deleteEvent", body, obj, evts, bundleId);}
 }
 }
 }
 }
 }
 return null;
 }
 
     }
     
     
     
     private List<DomainObjectWrapper> _query___query_0(DomainObjectWrapper obj, String securityToken, String bundleId, java.lang.String id) throws DomainDataHandlerException {
         List<DomainObjectWrapper> result = new ArrayList<DomainObjectWrapper>();
 List<DomainObjectWrapper> _final = new ArrayList<DomainObjectWrapper>();
 List<DBObject> queryList = new ArrayList<DBObject>();
     if (true) {
         queryList.add(QueryBuilder.start().and("content.id").is(id).and("content.data.fromTime").greaterThan(java.lang.System.currentTimeMillis()).get());
     }
 	try{
     	result = getDomainObjectHandler().query("eu.trentorise.smartcampus.domain.discovertrento.ServiceEventObject",QueryBuilder.start().or(queryList.toArray(new DBObject[]{})).get(), securityToken, bundleId);
 	} catch(Exception e) {
     	result = getDomainObjectHandler().query("eu.trentorise.smartcampus.domain.discovertrento.ServiceEventObject", (DBObject)null, securityToken, bundleId);
     }
     for(DomainObjectWrapper w : result) {
         if (_matches___query_0(w, obj, bundleId, id)) {
             _final.add(w);
         }
     }
     return _final;
     }
     private boolean _matches___query_0(DomainObjectWrapper target, DomainObjectWrapper obj, String bundleId, java.lang.String id) throws DomainDataHandlerException {
         return (getDomainObjectHandler().getVar("id",target,java.lang.String.class,bundleId).equals(id)) && (getDomainObjectHandler().getVar("data",target,eu.trentorise.smartcampus.domain.discovertrento.GenericEvent.class,bundleId).getFromTime()>java.lang.System.currentTimeMillis());
     }
     
     public void handleObjectRelUpdate(String rName, DomainObjectWrapper obj, Set<DomainEvent> evts, String bundleId) throws DomainDataHandlerException {
     }
     public void handleObjectVarUpdate(String vName, DomainObjectWrapper obj, Set<DomainEvent> evts, String bundleId) throws DomainDataHandlerException {
     }
 
     public void handleObjectCreate(DomainObjectWrapper obj, Set<DomainEvent> outEvents, Set<EvaluableDomainOperation> ops, String bundleId) throws DomainDataHandlerException {
             getDomainObjectHandler().setVar("events", obj, eu.trentorise.smartcampus.domain.discovertrento.CurrentEvents.getInstance(), outEvents, bundleId);
         __initialize(new Tuple(), obj, outEvents, ops, obj.getDomainObject().getSecurityToken(),bundleId);
     }
 
     private Object __initialize(Tuple tuple, DomainObjectWrapper obj, Set<DomainEvent> evts, Set<EvaluableDomainOperation> ops, String securityToken, String bundleId) throws DomainDataHandlerException {
         return null;
     }
 
     protected ActionInvoke ruleApplies(EvaluableDomainSubscriptionRule rule, DomainObjectWrapper obj, String bundleId) throws DomainDataHandlerException {
         if ("declared_subscription_sub_0".equals(rule.getRule().getEngineName())) {
             Tuple payload = (Tuple) rule.getEvent().getPayload();
             eu.trentorise.smartcampus.domain.discovertrento.GenericEvent[] data = (eu.trentorise.smartcampus.domain.discovertrento.GenericEvent[]) payload.get("data");
             String name = (String) payload.get("name");
             if (true){
                 Tuple tuple = new Tuple();
                 tuple.put("list",data);
                 return new ActionInvoke("transformEvents", tuple);
             }
         }
         return null;
     }
 
 
     @Override
     public boolean isRelation(String key) {
         return false
         ;    
     }
 
     @Override
     public String getType() {
         return "eu.trentorise.smartcampus.domain.discovertrento.ServiceEventFactory";
     }
 
     @Override
     public boolean isStatic() {
         return !false;
     }
 
     private static Collection<String> dependencies = new java.util.HashSet<String>();
     static {
         dependencies.add("eu.trentorise.smartcampus.domain.discovertrento.EventService");
     }
 
     public Collection<String> getDependencies() {
         return dependencies;
     }
 
     public Collection<DomainSubscriptionRule> getSubscriptions(DomainObjectWrapper obj, String bundleId) throws DomainDataHandlerException {
         List<DomainSubscriptionRule> rules = new ArrayList<DomainSubscriptionRule>();
         
         {
             DomainSubscriptionRule 
             rule = new DomainSubscriptionRule();
             rule.setName("declared_subscription_sub_0");
             rule.setSourceId(null);
             rule.setSourceType("eu.trentorise.smartcampus.domain.discovertrento.EventService");
             rule.setTargetId(obj.getId());
             rule.setTargetType(obj.getType());
             rule.setEngineName("declared_subscription_sub_0");
             rule.setEventType(DOMAIN_OBJECT_EVENT_TYPE.CUSTOM .toString());
             rule.setEventSubtype("update");
             rules.add(rule);
         }
         return rules;
     }
 
     
     public it.sayservice.platform.domain.model.interfaces.DomainTypeInterface getInterface() throws it.sayservice.platform.core.domain.common.exception.DomainDataHandlerException {
         return eu.trentorise.smartcampus.domain.discovertrento .ServiceEventFactoryInterface .getInstance();
     }
     
 
     public List<DomainSubscriptionRule> findSubscriptionRules(DomainEvent event, String securityToken, String bundleId) throws DomainDataHandlerException {
         List<DomainSubscriptionRule> result = new ArrayList<DomainSubscriptionRule>();
         List<DomainSubscriptionRule> tmp = null;
         return result;
     }
     
     public Collection<DomainEventDescriptor> getEventsToQuery() {
         List<DomainEventDescriptor> result = new ArrayList<DomainEventDescriptor>();
         return result;
     }
     
 }
 
 
