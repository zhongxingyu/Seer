 package com.vdweem.webspotify.result;
 
 import org.apache.struts2.ServletActionContext;
 
 import com.google.gson.JsonObject;
 import com.opensymphony.xwork2.ActionInvocation;
 import com.opensymphony.xwork2.Result;
 
 public class JsonResult extends WebSpotifyResult {
 	public static final JsonResult SUCCESS = new JsonResult("{\"message\": \"success\"}");
 	private final String json;
 
 	public JsonResult(String json) {
 		this(json, false);
 	}
 	public JsonResult(String json, boolean isMessage) {
 		if (isMessage) {
 			JsonObject jso = new JsonObject();
 			jso.addProperty("message", json);
 			json = jso.toString();
 		}
 		this.json = json;
 	}
 
 	/**
 	 * Returns the JSON response if this was an Ajax request,
 	 * if not, then it returns the index page with an initializer.
 	 * @param json
 	 * @return
 	 */
 	public static Result onAjax(String json) {
 		if (isAjax()) {
 			return new JsonResult(json);
 		} else {
 			return new NotAjaxRedirect(json);
 		}
 	}
 
 	/**
 	 * Determines if the current request is an Ajax request.
 	 * @return
 	 */
 	public static boolean isAjax() {
 		return "XMLHttpRequest".equals(ServletActionContext.getRequest().getHeader("x-requested-with"));
 	}
 
 	@Override
 	public void execute(ActionInvocation invocation) throws Exception {
 		super.execute(invocation);
 		ServletActionContext.getResponse().setContentType("application/json");
 		ServletActionContext.getResponse().getWriter().print(json);
 	}
 
 }
