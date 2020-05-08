 package com.pangratz.oeffinpc.rest;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.ext.freemarker.TemplateRepresentation;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.ResourceException;
 
 import com.pangratz.oeffinpc.model.NetworkPlan;
 import com.pangratz.oeffinpc.model.NetworkPlanEntry;
 
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.template.Configuration;
 
 public class NetworkPlanEntriesResource extends OeffiNpcServerResource {
 
 	private Long mNetworkPlanId;
 
 	@Override
 	protected void doInit() throws ResourceException {
		super.doInit();

 		String stringVal = (String) getRequest().getAttributes().get("networkPlanId");
 		System.out.println("NetworkPlanEntriesResource#stringVal = " + stringVal);
 		this.mNetworkPlanId = Long.valueOf(stringVal);
 
 		getVariants(Method.GET).add(new Variant(MediaType.TEXT_CSV));
 		getVariants(Method.GET).add(new Variant(MediaType.APPLICATION_JSON));
 	}
 
 	@Override
 	protected Representation get(Variant variant) throws ResourceException {
 		NetworkPlan networkPlan = mModelUtils.getNetworkPlan(mNetworkPlanId);
 		if (networkPlan == null) {
 			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
 			return createErrorRepresentation("no network plan with id " + mNetworkPlanId);
 		}
 
 		List<NetworkPlanEntry> entries = mModelUtils.getNetworkPlanEntries(mNetworkPlanId);
 
 		if (MediaType.TEXT_CSV.equals(variant.getMediaType())) {
 			String templateName = "template.tfl";
 			Configuration config = new Configuration();
 			ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/com/pangratz/oeffinpc");
 			config.setTemplateLoader(ctl);
 			Map<String, Object> model = new HashMap<String, Object>();
 			model.put("networkPlan", networkPlan);
 			model.put("entries", entries);
 			return new TemplateRepresentation(templateName, config, model, MediaType.TEXT_CSV);
 		}
 
 		JSONArray entriesArr = new JSONArray(entries);
 		return new JsonRepresentation(entriesArr);
 	}
 
 }
