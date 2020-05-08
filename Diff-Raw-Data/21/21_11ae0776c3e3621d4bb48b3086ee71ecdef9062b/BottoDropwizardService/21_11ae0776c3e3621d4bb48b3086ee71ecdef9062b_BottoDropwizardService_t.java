 import botto.xmpp.service.*;
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 
 
 public class BottoDropwizardService extends Service<BottoDropwizardServiceConfiguration> {
 
     public static void main(String[] args) throws Exception {
         new BottoDropwizardService().run(args);
     }
 
     @Override
     public void initialize(Bootstrap<BottoDropwizardServiceConfiguration> bootstrap) {
         bootstrap.setName("botto-dropwizard-integration");
     }
 
     @Override
     public void run(BottoDropwizardServiceConfiguration dropwizardConfig, Environment dropwizardEnvironment) throws Exception {
 
         // create an instance of the simple Bot
         final SayHelloBot helloBot = new SayHelloBot();
 
         final CountUpBot countUpBot = new CountUpBot();
 
         // create an instance of the simple resource and reference the bot
         SayHelloResource resource = new SayHelloResource(helloBot);
 
         // configure the resource
         dropwizardEnvironment.addResource(resource);
 
         // create a botservice instance
         AbstractBotService botService = new AbstractBotService() {
             @Override
             protected void run(ServiceEnvironment environment) {
                 // create a new xmpp subdomain
                 SubdomainEnvironment subdomain = environment.getSubdomain("dropwizard");
 
                 // add the bot to this subdomain, and assigne the node "hello" to it
                 // so that bot will be addressable as hello@dropwizard.example.com
                 subdomain.addBot(helloBot, "hello");
 
                 // setup a single-node bot
                 BotEnvironment botEnv = environment.getBot("countup");
                 botEnv.setBot(countUpBot);
                 botEnv.setSecret("countup");
             }
         };
 
         // obtain bot service config from dropwizard config
         BotServiceConfiguration botServiceConfiguration = dropwizardConfig.getBotService();
 
         // run the service
        //botService.run(botServiceConfiguration);
     }
 
 }
