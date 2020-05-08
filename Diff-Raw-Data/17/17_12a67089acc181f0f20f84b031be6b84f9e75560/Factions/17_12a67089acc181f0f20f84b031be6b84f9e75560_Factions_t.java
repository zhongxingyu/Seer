 import java.io.File;
 import java.util.logging.Logger;
 
 public class Factions extends Plugin{
     fListener listener = new fListener(this);
     public static final Logger log = Logger.getLogger("Minecraft");
 
   String name = "[Player made factions";
   String maker = "D3athrow,";
   String version = "Version 0.9.1b] ";
   String author = "By D3athrow";
   static String plugin = "Factions";
   public File propsDir = new File("plugins/config/");
   public static PropertiesFile tpp = new PropertiesFile("plugins/config/" + plugin + ".properties");
   public boolean mysql = tpp.getBoolean("Use-MySQL");
   FactionsH fhook = new FactionsH(this);
   boolean economy = false;
   boolean dconomy = tpp.getBoolean("use-dConomy");
   public String currency;
   public int max = tpp.getInt("Maximum-Factions");
   public int cost = tpp.getInt("CostofFaction");
   public boolean paytocreate = tpp.getBoolean("PayToCreate");
 
   
     
   public void enable(){
  etc.getInstance().addCommand("/factions", " - Lists all available factions commands.");
     
     props();
     if(mysql)
         listener.makeTables();
     }
   public void disable(){
      
   }
   
   public void props(){
         if (!this.propsDir.exists()) {
       this.propsDir.mkdirs();
     }
         if(!tpp.containsKey("Maximum-Factions")){
             tpp.setInt("Maximum-Factions", -1);
         }
         if(!tpp.containsKey("Signatures-To-Create-Faction")){
             tpp.setInt("Signatures-To-Create-Faction", 5);
         }
         if(!tpp.containsKey("Max-Team-Size")){
             tpp.setInt("Max-Team-Size", 5);
         }
         if(!tpp.containsKey("PayToCreate")){
             tpp.setBoolean("PayToCreate", false);
         }
         if(!tpp.containsKey("use-dConomy")){
             tpp.setBoolean("use-dConomy", false);
         }
         if(!tpp.containsKey("CostofFaction")){
             tpp.setInt("CostofFaction", 0);
         }
         if(!tpp.containsKey("useCanaryDb")){
             tpp.setBoolean("useCanaryDb", false);
         }
         
         if(!tpp.containsKey("Use-MySQL")){
             tpp.setBoolean("Use-MySQL", true);
         }
         if(!tpp.containsKey("username")){
             tpp.setString("username","User");
         }
         if(!tpp.containsKey("password")){
             tpp.setString("password", "Password");
         }
         if(!tpp.containsKey("MySQL-Driver")){
             tpp.setString("MySQL-Driver", "com.mysql.jdbc.Driver");
         }
         if(!tpp.containsKey("db")){
             tpp.setString("db","jdbc:mysql://localhost:3306/minecraft");
         }
         
         
         
         
     }
   
   public void initialize(){
       this.log.info(this.name + " " + this.version + " " + this.author + " seems to be loaded");
       etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
       etc.getLoader().addCustomListener(fhook.fh);
       economy = Hooked.getBoolean("iSettings", new Object[] { "loaded" });
       if(!economy){
           economy = dconomy;
           if(dconomy){
               log.info("Factions - Found dConomy!");
               currency = Hooked.getString("dCBalance", new Object[] { "MoneyName" });
           }
       }
       else{
           log.info("Factions - Found iConomy!");
           currency = Hooked.getString("iSettings", new Object[] { "Currency" });
           dconomy = false;
       }
       
       
   }
   
 
 }
