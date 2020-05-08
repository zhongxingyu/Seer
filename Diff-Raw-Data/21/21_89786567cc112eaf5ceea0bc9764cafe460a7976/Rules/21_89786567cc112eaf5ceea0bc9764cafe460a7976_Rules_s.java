 package net.cyklotron.cms.modules.rest.accesslimits;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.coral.web.rest.RequireCoralRole;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.ratelimit.impl.RuleFactory;
 import org.objectledge.web.ratelimit.rules.ParseException;
 
 import net.cyklotron.cms.accesslimits.ProtectedItemResource;
 import net.cyklotron.cms.accesslimits.ProtectedItemResourceImpl;
 import net.cyklotron.cms.accesslimits.RuleResource;
 import net.cyklotron.cms.accesslimits.RuleResourceImpl;
 
 @Path("/accesslimits/rules")
 @RequireCoralRole("cms.administrator")
 public class Rules
 {
     private static final String RULES_ROOT = "/cms/accesslimits/rules";
 
     private final CoralSession coralSession;
 
     private final UriInfo uriInfo;
 
     private static final Object ITEM_NAME_LOCK = new Object();
 
     @Inject
     public Rules(CoralSessionFactory coralSessionFactory, UriInfo uriInfo)
     {
         this.uriInfo = uriInfo;
         this.coralSession = coralSessionFactory.getCurrentSession();
     }
 
     @GET
     @Path("/items")
     @Produces(MediaType.APPLICATION_JSON)
     public Response retrieveProtectedItems()
     {
         Resource[] actions = coralSession.getStore().getResourceByPath(RULES_ROOT + "/*");
         return Response.ok(ProtectedItemDao.create(actions)).build();
     }
 
     @POST
     @Path("/items")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response createProctectedItem(ProtectedItemDao item)
     {
         try
         {
             validateProtectedItem(item);
         }
         catch(PatternSyntaxException | ParseException e)
         {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
         }
 
         try
         {
             String name;
             ProtectedItemResource itemResource;
             synchronized(ITEM_NAME_LOCK)
             {
                 Resource parent = coralSession.getStore().getUniqueResourceByPath(RULES_ROOT);
                 name = nextName(parent.getChildren());
                 itemResource = ProtectedItemResourceImpl.createProtectedItemResource(coralSession,
                     name, parent, item.getUrlPattern());
             }
             int n = 1;
             for(RuleDao rule : item.getRules())
             {
                 RuleResourceImpl.createRuleResource(coralSession, Integer.toString(n),
                     itemResource, n, rule.getRuleDefinition());
                 n++;
             }
             return Response.created(uriInfo.getRequestUri().resolve(itemResource.getIdString()))
                 .header("X-Item-Id", itemResource.getIdString()).build();
         }
         catch(EntityDoesNotExistException | AmbigousEntityNameException | ValueRequiredException
                         | InvalidResourceNameException e)
         {
             return Response.status(Status.INTERNAL_SERVER_ERROR)
                 .entity(new StackTrace(e).toString()).build();
         }
     }
 
     private String nextName(Resource[] children)
     {
         int max = 0;
         for(Resource child : children)
         {
             int n = Integer.parseInt(child.getName());
             max = n > max ? n : max;
         }
         return Integer.toString(max + 1);
     }
 
     @GET
     @Path("/items/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response retrieveProtectedItem(@PathParam("id") long id)
     {
         try
         {
             Resource res = coralSession.getStore().getResource(id);
             return Response.ok(new ProtectedItemDao((ProtectedItemResource)res)).build();
         }
         catch(EntityDoesNotExistException | ClassCastException e)
         {
             return Response.status(Status.NOT_FOUND).build();
         }
     }
 
     @PUT
     @Path("/items/{id}")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response updateProtectedItem(@PathParam("id") long id, ProtectedItemDao item)
     {
         try
         {
             validateProtectedItem(item);
         }
         catch(PatternSyntaxException | ParseException e)
         {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
         }
 
         try
         {
             ProtectedItemResource res = (ProtectedItemResource)coralSession.getStore().getResource(
                 id);
             try
             {
                 res.setUrlPattern(item.getUrlPattern());
                 res.update();
                 Resource[] cur = res.getChildren();
                 if(item.getRules() != null && item.getRules().size() > 0)
                 {
                     int p = 1;
                     for(RuleDao rule : item.getRules())
                     {
                         rule.setPriority(p++);
                     }
                     int n = cur.length + 1;
                     for(RuleDao rule : item.getRules())
                     {
                         RuleResource curRule = rule.getId() != null ? getRuleResource(cur,
                             rule.getId()) : null;
                         if(curRule == null)
                         {
                             RuleResourceImpl.createRuleResource(coralSession,
                                 Integer.toString(n++), res, rule.getPriority(),
                                 rule.getRuleDefinition());
                         }
                         else
                         {
                             curRule.setPriority(rule.getPriority());
                             curRule.setRuleDefinition(rule.getRuleDefinition());
                             curRule.update();
                         }
                     }
                     for(Resource curRule : cur)
                     {
                         if(getRuleDao(item.getRules(), curRule.getId()) == null)
                         {
                             coralSession.getStore().deleteResource(curRule);
                         }
                     }
                 }
                 else
                 {
                     // no rules array in item - delete all rules from db
                     for(Resource curRule : cur)
                     {
                         coralSession.getStore().deleteResource(curRule);
                     }
                 }
                 return Response.noContent().build();
             }
             catch(Exception e)
             {
                 return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity(new StackTrace(e).toString()).build();
             }
         }
         catch(EntityDoesNotExistException | ClassCastException e)
         {
             return Response.status(Status.NOT_FOUND).build();
         }
     }
 
     private RuleResource getRuleResource(Resource[] rs, long id)
     {
         for(Resource r : rs)
         {
             if(r.getId() == id)
             {
                 return (RuleResource)r;
             }
         }
         return null;
     }
 
     private RuleDao getRuleDao(List<RuleDao> rules, long id)
     {
         for(RuleDao rule : rules)
         {
             if(rule.getId() != null && rule.getId() == id)
             {
                 return rule;
             }
         }
         return null;
     }
 
     @DELETE
     @Path("/items/{id}")
     public Response deleteProtectedItem(@PathParam("id") long id)
     {
         try
         {
             Resource res = coralSession.getStore().getResource(id);
             try
             {
                 synchronized(ITEM_NAME_LOCK)
                 {
                     coralSession.getStore().deleteTree(res);
                 }
                 return Response.noContent().build();
             }
             catch(EntityInUseException e)
             {
                 return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity(new StackTrace(e).toString()).build();
             }
         }
         catch(EntityDoesNotExistException | ClassCastException e)
         {
             return Response.status(Status.NOT_FOUND).build();
         }
     }
 
     @POST
     @Path("/validate/urlPattern")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response validateUrlPattern(ValidationRequest request)
     {
         try
         {
             validateUrlPattern(request.getText());
             return Response.ok(new ValidationResponse(true)).build();
         }
         catch(PatternSyntaxException e)
         {
             return Response.ok(new ValidationResponse(false, e.getMessage())).build();
         }
     }
 
     @POST
     @Path("/validate/rule")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response validateRuleDefinition(ValidationRequest request)
     {
         try
         {
             validateRuleDefinition(request.getText());
             return Response.ok(new ValidationResponse(true)).build();
         }
         catch(ParseException e)
         {
             return Response.ok(new ValidationResponse(false, e.getMessage())).build();
         }
     }
 
     private void validateUrlPattern(String urlPattern)
         throws PatternSyntaxException
     {
         Pattern.compile(urlPattern);
     }
 
     private void validateRuleDefinition(String rule)
         throws ParseException
     {
         RuleFactory.getInstance().validateRule(rule);
     }
 
     private void validateProtectedItem(ProtectedItemDao item)
         throws PatternSyntaxException, ParseException
     {
         validateUrlPattern(item.getUrlPattern());
         if(item.getRules() != null)
         {
             for(RuleDao rule : item.getRules())
             {
                 validateRuleDefinition(rule.getRuleDefinition());
             }
         }
     }
 
     public static class ValidationRequest
     {
         private String text;
 
         public String getText()
         {
             return text;
         }
 
         public void setText(String text)
         {
             this.text = text;
         }
     }
 
     public static class ValidationResponse
     {
         private boolean valid;
 
         private String error;
 
         public ValidationResponse(boolean valid)
         {
             this.valid = valid;
         }
 
         public ValidationResponse(boolean valid, String error)
         {
             this.valid = valid;
             this.error = error;
         }
 
         public boolean isValid()
         {
             return valid;
         }
 
         public void setValid(boolean valid)
         {
             this.valid = valid;
         }
 
         public String getError()
         {
             return error;
         }
 
         public void setError(String error)
         {
             this.error = error;
         }
     }
 
     public static class ProtectedItemDao
     {
         private long id;
 
         private String urlPattern;
 
         private List<RuleDao> rules;
 
         public ProtectedItemDao()
         {
         }
 
         public ProtectedItemDao(ProtectedItemResource resource)
         {
             this.id = resource.getId();
             this.urlPattern = resource.getUrlPattern();
             this.rules = RuleDao.create(resource.getChildren());
         }
 
         public long getId()
         {
             return id;
         }
 
         public void setId(long id)
         {
             this.id = id;
         }
 
         public String getUrlPattern()
         {
             return urlPattern;
         }
 
         public void setUrlPattern(String urlPattern)
         {
             this.urlPattern = urlPattern;
         }
 
         public List<RuleDao> getRules()
         {
             return rules;
         }
 
         public void setRules(List<RuleDao> rules)
         {
             this.rules = rules;
         }
 
         private static final Comparator<ProtectedItemDao> BY_ID = new Comparator<ProtectedItemDao>()
             {
                 @Override
                 public int compare(ProtectedItemDao o1, ProtectedItemDao o2)
                 {
                     return o1.id > o2.id ? 1 : (o1.id < o2.id ? -1 : 0);
                 }
             };
 
         public static List<ProtectedItemDao> create(Resource[] items)
         {
             List<ProtectedItemDao> result = new ArrayList<ProtectedItemDao>(items.length);
             for(Resource item : items)
             {
                 result.add(new ProtectedItemDao((ProtectedItemResource)item));
             }
             Collections.sort(result, BY_ID);
             return result;
         }
     }
 
     public static class RuleDao
     {
         private Long id;
 
         private int priority;
 
         private String ruleDefinition;
 
         public RuleDao()
         {
         }
 
         public RuleDao(RuleResource resource)
         {
             this.id = resource.getId();
             this.priority = resource.getPriority();
             this.ruleDefinition = resource.getRuleDefinition();
         }
 
         public Long getId()
         {
             return id;
         }
 
         public void setId(Long id)
         {
             this.id = id;
         }
 
         public int getPriority()
         {
             return priority;
         }
 
         public void setPriority(int priority)
         {
             this.priority = priority;
         }
 
         public String getRuleDefinition()
         {
             return ruleDefinition;
         }
 
         public void setRuleDefinition(String ruleDefinition)
         {
             this.ruleDefinition = ruleDefinition;
         }
 
         private static final Comparator<RuleDao> BY_PRIORITY = new Comparator<RuleDao>()
             {
                 @Override
                 public int compare(RuleDao o1, RuleDao o2)
                 {
                     return o1.priority - o2.priority;
                 }
             };
 
         public static List<RuleDao> create(Resource[] rules)
         {
             List<RuleDao> result = new ArrayList<RuleDao>(rules.length);
             for(Resource rule : rules)
             {
                 result.add(new RuleDao((RuleResource)rule));
             }
             Collections.sort(result, BY_PRIORITY);
             return result;
         }
     }
 }
