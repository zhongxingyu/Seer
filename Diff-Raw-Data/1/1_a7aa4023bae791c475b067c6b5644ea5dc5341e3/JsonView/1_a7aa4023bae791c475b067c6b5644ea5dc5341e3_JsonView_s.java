 package org.cc.exception;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.cc.response.CloudErrorResponse;
 import org.cc.util.LogUtil;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.web.servlet.View;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.OutputStream;
 import java.util.Map;
 
 /**
  * View is intended for rendering error response objects.
  *
  * Daneel Yaitskov
  */
 public class JsonView implements View {
 
     private static final Logger logger = LogUtil.get();
     public static final String DATA_FIELD_NAME = "data";
 
     @Resource
     private ObjectMapper mapper;
 
     @Override
     public String getContentType() {
         return "application/json";
     }
 
     @Override
     public void render(Map<String, ?> model, HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
         Object data = model.get(DATA_FIELD_NAME);
         OutputStream ostream = response.getOutputStream();
         if (data == null) {
             String err = "Model doesn't have field '" + DATA_FIELD_NAME + "'";
             logger.error(err);
             CloudErrorResponse cer = new CloudErrorResponse("JsonViewError", err);
             mapper.writeValue(ostream, cer);
         } else {
             mapper.writeValue(ostream, data);
         }
     }
 }
