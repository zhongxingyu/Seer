 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.pircbotx.Configuration;
 import org.pircbotx.PircBotX;
 
 import bot.Bot;
 import bot.BotConfig;
import bot.BotHandler;
 import bot.config.EnvironmentVars;
import bot.di.Module;

import com.google.inject.Guice;
import com.google.inject.Injector;
 
 public class Main extends HttpServlet {
 
     private static final long serialVersionUID = -7490600326997334112L;
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         resp.getWriter().print(
                 "I'm just alive so the bot counts as a webapp :)");
     }
 
     public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new Module());
        BotConfig botConfig = new BotConfig(injector.getInstance(BotHandler.class));

         Configuration<PircBotX> config = botConfig.createConfig();
 
         PircBotX pircbotx = new PircBotX(config);
 
         Bot bot = new Bot(pircbotx);
 
         bot.start();
     }
 
     private static void launchServer() throws Exception, InterruptedException {
         Server server = new Server(Integer.valueOf(System
                 .getenv(EnvironmentVars.PORT)));
         ServletContextHandler context = new ServletContextHandler(
                 ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new Main()), "/*");
         server.start();
         server.join();
     }
 }
