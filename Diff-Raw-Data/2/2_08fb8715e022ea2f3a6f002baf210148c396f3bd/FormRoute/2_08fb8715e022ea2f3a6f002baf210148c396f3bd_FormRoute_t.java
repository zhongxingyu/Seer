 package com.baosight.bssim.routes;
 
 import com.baosight.bssim.helpers.SvnHelper;
 import com.baosight.bssim.models.TableModel;
 import com.google.gson.Gson;
 import org.apache.commons.lang3.StringUtils;
 import spark.Request;
 import spark.Response;
 
 import java.io.File;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import static spark.Spark.get;
 import static spark.Spark.post;
 
 public class FormRoute {
     public static void all() {
         get(new JSONResponseRoute("/form/:schemaTable/:config") {
             public Object handle(Request request, Response response) {
                 try {
                     Map result = new HashMap();
                     Map config = new Gson().fromJson(URLDecoder.decode(request.params(":config"), "UTF8"), HashMap.class);
                     config.put("firstModule", (config.get("firstModule")+"").toUpperCase());
                     config.put("secondModule", (config.get("secondModule")+"").toUpperCase());
                     config.put("jspName", config.get("formName")+"");
                     config.put("jsName", config.get("formName")+"");
                     config.put("serviceName", "Service"+config.get("formName"));
 
                    TableModel table = new TableModel(request.params(":schemaTable"));
                     result.put("jspCode", table.genJspCode(config));
                     result.put("jsCode", table.genJsCode(config));
                     result.put("serviceCode", table.genServiceCode(config));
 
                     return new HandleResult(true, "", result);
                 } catch (Exception e) {
                     return new HandleResult(false, e.getMessage(), null);
                 }
             }
         });
 
         post(new JSONResponseRoute("/form/:schemaTable/commit/:config") {
             @Override
             public Object handle(Request request, Response response) {
                 String id = request.params(":schemaTable");
 
                 try {
                     TableModel model = new TableModel(id);
 
                     Map config = new Gson().fromJson(URLDecoder.decode(request.params(":config"), "UTF8"), HashMap.class);
                     config.put("firstModule", (config.get("firstModule")+"").toUpperCase());
                     config.put("secondModule", (config.get("secondModule")+"").toUpperCase());
                     config.put("jspName", config.get("formName")+"");
                     config.put("jsName", config.get("formName")+"");
                     config.put("serviceName", "Service"+config.get("formName"));
 
                     String jspPath = "web/"+config.get("firstModule");
                     String jsPath = "web/"+config.get("firstModule");
                     String servicePath = "src/com/baosight/bssim/"+config.get("firstModule").toString().toLowerCase();
 
                     if(!StringUtils.isBlank(config.get("secondModule")+"")){
                         jspPath = jspPath + "/" + config.get("secondModule");
                         jsPath = jsPath + "/" + config.get("secondModule");
                         servicePath = servicePath + "/" + config.get("secondModule").toString().toLowerCase();
                     }
 
                     jspPath = jspPath + "/" + config.get("formName") + ".jsp";
                     jsPath = jsPath + "/" + config.get("formName") + ".js";
                     servicePath = servicePath + "/service/Service" + config.get("formName") + ".java";
 
                     Map commit = new HashMap();
                     commit.put(jspPath,  model.genJspCode(config));
                     commit.put(jsPath,  model.genJsCode(config));
                     commit.put(servicePath, model.genServiceCode(config));
 
                     String result = SvnHelper.commit(commit);
 
                     return new HandleResult(true, result, null);
                 } catch (Exception e) {
                     return new HandleResult(false, e.getMessage(), null);
                 }
             }
         });
 
     }
 }
