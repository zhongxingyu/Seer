 package net.craftminecraft.bungee.bungeeyaml;
 
 import com.ning.http.client.Response;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 
 import com.thebuzzmedia.sjxp.rule.IRule;
 import com.thebuzzmedia.sjxp.rule.DefaultRule;
 import com.thebuzzmedia.sjxp.rule.IRule.Type;
 import com.thebuzzmedia.sjxp.XMLParser;
 import net.craftminecraft.bungee.bungeeyaml.pluginapi.ConfigurablePlugin;
 
 public class BungeeYAML extends ConfigurablePlugin {
     private Metrics metrics;
     private static final String updaterelurl = "http://teamcity.craftminecraft.net/guestAuth/app/rest/builds/?locator=pinned:true,buildType:bt7,sinceBuild:number:%d,count:1";
     private static final String updatedevurl = "http://teamcity.craftminecraft.net/guestAuth/app/rest/builds/?locator=buildType:bt7,sinceBuild:number:%d,count:1";
     private static final String getartifactrelurl = "http://teamcity.craftminecraft.net/guestAuth/repository/download/bt7/.lastPinned/teamcity-ivy.xml";
     private static final String getartifactdevurl = "http://teamcity.craftminecraft.net/guestAuth/repository/download/bt7/.lastSuccessful/teamcity-ivy.xml";
     private static final String downloadrelurl = "http://teamcity.craftminecraft.net/guestAuth/repository/download/bt7/.lastPinned/%s.jar";
     private static final String downloaddevurl = "http://teamcity.craftminecraft.net/guestAuth/repository/download/bt7/.lastSuccessful/%s.jar";
     
     private String buildnumber;
     @SuppressWarnings("unchecked")
     @Override
     public void onLoading() {
         // Set version
         String[] version = this.getDescription().getVersion().split("-");
         buildnumber = version[version.length-1];
         
         // Save a copy of the default config.yml if one is not there
         this.saveDefaultConfig();
 
         // If we don't autoupdate, then GTFO.
         if (!this.getConfig().getBoolean("autoupdate", true)) {
             return;
         }
 
         try {
             Response resp;
             final boolean dev;
             
             // Should we get a dev or a release build ?
             switch (this.getConfig().getString("updatechannel", "rel")) {
             case "development":
             case "dev":
                 dev = true;
                 resp = this.getProxy().getHttpClient().prepareGet(String.format(updatedevurl, Integer.parseInt(buildnumber))).execute().get();
                 break;
             default:
                 dev = false;
                 resp = this.getProxy().getHttpClient().prepareGet(String.format(updaterelurl, Integer.parseInt(buildnumber))).execute().get();
                 break;
             }
             IRule<BungeeYAML> versionrule = new DefaultRule<BungeeYAML>(Type.ATTRIBUTE, "/builds/build", "number") {
                 @Override
                 public void handleParsedAttribute(XMLParser<BungeeYAML> parser, int index, String value, BungeeYAML plugin) {
                     // We are looking for attribute called "number"
                     if (!getAttributeNames()[index].equals("number")) {
                         return;
                     }
                     try {
                         // If the attribute value is not the same, trigger "plugin.update" with correct URI.
                         if (!value.equals(plugin.buildnumber)) {
                             if (dev) {
                                 String filename = plugin.getArtifactUrl(getartifactdevurl);
                                 plugin.update(String.format(downloaddevurl,filename));
                             } else {
                                 String filename = plugin.getArtifactUrl(getartifactrelurl);
                                 plugin.update(String.format(downloadrelurl,filename));
                             }
                             // Updating once is enough...
                             parser.stop();
                         }
                     } catch (Exception ex) {plugin.getProxy().getLogger().log(Level.WARNING, "[BungeeYAML] Update of BungeeYAML failed.",ex);return;}
                 }
             };
             // Start the update process.
             XMLParser<BungeeYAML> parser = new XMLParser<BungeeYAML>(versionrule);
             parser.parse(resp.getResponseBodyAsStream(), this);
         } catch (NumberFormatException ex) {this.getProxy().getLogger().log(Level.INFO, "[BungeeYAML] Using custom version of BungeeYAML. Will not autoupdate.");return;
        } catch (Exception ex) {this.getProxy().getLogger().log(Level.WARNING, "[BungeeYAML] Update of BungeeYAML failed.",ex);return;
        } catch (ExceptionInInitializerError err) {this.getProxy().getLogger().log(Level.WARNING, "[BungeeYAML] Update of BungeeYAML failed.",err);return;}
     }
 
     @SuppressWarnings("unchecked")
     private String getArtifactUrl(String url) throws Exception {
         IRule<StringBuilder> artifactrule = new DefaultRule<StringBuilder>(Type.ATTRIBUTE, "/ivy-module/publications/artifact", "name") {
             @Override
             public void handleParsedAttribute(XMLParser<StringBuilder> parser, int index, String value, StringBuilder returnvalue) {
                 if (!getAttributeNames()[index].equals("name")) {
                     return;
                 }
                 returnvalue.append(value);
                 parser.stop();
             }
         };
         StringBuilder returnvalue = new StringBuilder();
         Response resp = this.getProxy().getHttpClient().prepareGet(url).execute().get();
         XMLParser<StringBuilder> artifactparser = new XMLParser<>(artifactrule);
         artifactparser.parse(resp.getResponseBodyAsStream(), returnvalue);
         return returnvalue.toString();
     }
     
     private void update(String url) throws Exception {
         Response resp = this.getProxy().getHttpClient().prepareGet(url).execute().get();
         File bungeeyamlfile;
         try {
             bungeeyamlfile = this.getFile();
         } catch (NoSuchMethodError ex) { // If we are running on a slightly older version of BungeeCord.
             bungeeyamlfile = new File(this.getProxy().getPluginsFolder(), "BungeeYAML.jar"); // Should add a this.getFile();
         }
         FileOutputStream bungeeyamlstream = new FileOutputStream(bungeeyamlfile);
         bungeeyamlstream.write(resp.getResponseBodyAsBytes());
         bungeeyamlstream.close();
         this.getProxy().getPluginManager().loadPlugin(bungeeyamlfile);
         getProxy().getLogger().log(Level.INFO, "BungeeYAML has been auto-updated.");
     }
 
 	public void onEnable() {
 		try {
 			metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 
 		}
 	}
 	
 	public void onDisable() {
 		
 	}
 }
