 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package ch.heigvd.gamification.rest;
 
 import ch.heigvd.gamification.exceptions.AuthentificationFailedException;
 import ch.heigvd.gamification.exceptions.EntityNotFoundException;
 import ch.heigvd.gamification.model.Application;
 import ch.heigvd.gamification.model.Rule;
 import ch.heigvd.gamification.services.crud.ApplicationsManagerLocal;
 import ch.heigvd.gamification.services.crud.BadgesManagerLocal;
 import ch.heigvd.gamification.services.crud.RulesManagerLocal;
 import ch.heigvd.gamification.services.to.BadgesTOServiceLocal;
 import ch.heigvd.gamification.services.to.RulesTOServiceLocal;
 import ch.heigvd.gamification.to.PublicRuleTO;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 /**
  *
  * @author Jean-Luc
  */
 @Stateless
 @Path("applications/{apiKey}/{apiSecret}/rules")
 public class RuleResource {
 
     @Context
     private UriInfo context;
 
     @EJB
     RulesTOServiceLocal rulesTOService;
    
    @EJB
    RulesManagerLocal rulesManager;
 
     @EJB
     ApplicationsManagerLocal applicationsManager;
 
     @EJB
     BadgesManagerLocal badgesManager;
 
     @EJB
     BadgesTOServiceLocal badgesTOService;
 
     public RuleResource() {
     }
 
     @POST
     @Consumes({"application/json"})
     public Response createResource(@PathParam("apiKey") String apiKey, @PathParam("apiSecret") String apiSecret, PublicRuleTO newRuleTO) throws AuthentificationFailedException, EntityNotFoundException, MalformedURLException {
         Application app = applicationsManager.checkApiSecret(apiKey, apiSecret);
         Rule newRule = new Rule();
         newRule.setApplication(app);
         newRule.setBadge(badgesManager.findById(newRuleTO.getBadge().getId()));
         rulesTOService.updateRuleEntity(newRule, newRuleTO);
         long newRuleId = rulesManager.create(newRule);
         app.getRules().add(rulesManager.findById(newRuleId));
         URI createdURI = context.getAbsolutePathBuilder().path(Long.toString(newRuleId)).build();
         return Response.created(createdURI).build();
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public List<PublicRuleTO> getResourceList(@PathParam("apiKey") String apiKey, @PathParam("apiSecret") String apiSecret) throws AuthentificationFailedException, EntityNotFoundException {
         Application app = applicationsManager.checkApiSecret(apiKey, apiSecret);
         List<Rule> rules = new ArrayList<>(app.getRules());
         List<PublicRuleTO> result = new LinkedList<>();
         for (Rule rule : rules) {
             result.add(rulesTOService.buildPublicRuleTo(rule));
         }
         return result;
     }
 
     @GET
     @Path("{ruleId}")
     @Produces(MediaType.APPLICATION_JSON)
     public PublicRuleTO getResource(@PathParam("apiKey") String apiKey, @PathParam("apiSecret") String apiSecret, @PathParam("ruleId") Long ruleId) throws EntityNotFoundException, AuthentificationFailedException {
         Application app = applicationsManager.checkApiSecret(apiKey, apiSecret);
         Rule rule = rulesManager.findById(ruleId);
         if (app.getRules().contains(rule)) {
             PublicRuleTO ruleTO = rulesTOService.buildPublicRuleTo(rule);
             return ruleTO;
         }
         return new PublicRuleTO();
     }
 
     @PUT
     @Path("{ruleId}")
     @Consumes({"application/json"})
     public Response Resource(PublicRuleTO updatedRuleTO, @PathParam("ruleId") Long ruleId, @PathParam("apiKey") String apiKey, @PathParam("apiSecret") String apiSecret) throws EntityNotFoundException, AuthentificationFailedException {
         Application app = applicationsManager.checkApiSecret(apiKey, apiSecret);
         Rule ruleToUpdate = rulesManager.findById(ruleId);
         if (app.getRules().contains(ruleToUpdate)) {
             ruleToUpdate.setBadge(badgesManager.findById(ruleToUpdate.getBadge().getId()));
             rulesTOService.updateRuleEntity(ruleToUpdate, updatedRuleTO);
             rulesManager.update(ruleToUpdate);
             return Response.ok().build();
         }
         return Response.notModified().build();
     }
 
     @DELETE
     @Path("{ruleId}")
     public Response deleteResource(@PathParam("apiKey") String apiKey, @PathParam("apiSecret") String apiSecret, @PathParam("ruleId") Long ruleId) throws EntityNotFoundException, AuthentificationFailedException {
         Application app = applicationsManager.checkApiSecret(apiKey, apiSecret);
         Rule ruleToDelete = rulesManager.findById(ruleId);
         if (app.getRules().contains(ruleToDelete)) {
             app.getRules().remove(ruleToDelete);
             rulesManager.delete(ruleId);
             return Response.ok().build();
         }
         return Response.notModified().build();
     }
 }
