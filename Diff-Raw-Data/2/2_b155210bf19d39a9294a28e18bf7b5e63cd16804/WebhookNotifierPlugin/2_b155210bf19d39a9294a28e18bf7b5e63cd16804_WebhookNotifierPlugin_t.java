 package org.wickedsource.hooked.plugins.webhook;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.wickedsource.hooked.plugins.api.notifier.CommitData;
 import org.wickedsource.hooked.plugins.api.notifier.NotifierPlugin;
 
 import java.util.List;
 import java.util.Properties;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public class WebhookNotifierPlugin implements NotifierPlugin {
 
     private Logger logger = LoggerFactory.getLogger(WebhookNotifierPlugin.class);
 
     private WebhookNotifierPluginProperties properties;
 
     @Override
     public void notify(CommitData data) {
         JsonMapper mapper = new JsonMapper();
         String json = mapper.mapToJson(data);
         logger.debug(String.format("Converted commit data to JSON: %s", json));
 
         WebhookCaller caller = new WebhookCaller();
 
         List<WebHookParameters> webhookParams = properties.getWebHookParameters();
 
         if (webhookParams.isEmpty()) {
             logger.warn(String.format("No target URLs configured for plugin %s! Please check your configuration!",
                     getClass().getSimpleName()));
         }
 
         for (WebHookParameters params : webhookParams) {
            logger.debug(String.format("Sending JSON data to URL %s", params.getUrl()));
             caller.callWebhook(params, json);
         }
 
     }
 
     @Override
     public void init(Properties properties) {
         this.properties = new WebhookNotifierPluginProperties(properties);
     }
 }
