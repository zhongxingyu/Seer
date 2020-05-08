 package in.mDev.MiracleM4n.DragonSpawn;
 
 import java.util.List;
 import java.util.Random;
 
 public class API {
     DragonSpawn plugin;
 
     public API(DragonSpawn plugin) {
         this.plugin = plugin;
     }
 
     public Boolean isPercent(Integer percent) {
         Random random = new Random();
         Integer minValue = 0;
         Integer maxValue = 100;
         
         Integer rand = random.nextInt(maxValue - minValue + 1) + minValue;
 
         return rand <= percent;
     }
 
    String getRandomString(List list) {
         if (list.isEmpty())
             return "";
 
         Random random = new Random();
 
         Integer minValue = 0;
         Integer maxValue = list.size()-1;
 
         Integer rand = random.nextInt(maxValue - minValue + 1) + minValue;
 
         try {
            return list.get(rand).toString();
         } catch (ArrayIndexOutOfBoundsException ignored) {}
 
         return "";
     }
 
     public String addColour(String string) {
         string = string.replace("`e", "")
                 .replace("`r", "\u00A7c")           .replace("`R", "\u00A74")
                 .replace("`y", "\u00A7e")           .replace("`Y", "\u00A76")
                 .replace("`g", "\u00A7a")           .replace("`G", "\u00A72")
                 .replace("`a", "\u00A7b")           .replace("`A", "\u00A73")
                 .replace("`b", "\u00A79")           .replace("`B", "\u00A71")
                 .replace("`p", "\u00A7d")           .replace("`P", "\u00A75")
                 .replace("`k", "\u00A70")           .replace("`s", "\u00A77")
                 .replace("`S", "\u00A78")           .replace("`w", "\u00A7f");
 
         string = string.replace("<r>", "")
                 .replace("<black>", "\u00A70")      .replace("<navy>", "\u00A71")
                 .replace("<green>", "\u00A72")      .replace("<teal>", "\u00A73")
                 .replace("<red>", "\u00A74")        .replace("<purple>", "\u00A75")
                 .replace("<gold>", "\u00A76")       .replace("<silver>", "\u00A77")
                 .replace("<gray>", "\u00A78")       .replace("<blue>", "\u00A79")
                 .replace("<lime>", "\u00A7a")       .replace("<aqua>", "\u00A7b")
                 .replace("<rose>", "\u00A7c")       .replace("<pink>", "\u00A7d")
                 .replace("<yellow>", "\u00A7e")     .replace("<white>", "\u00A7f");
 
         string = string.replaceAll("(§([a-fA-F0-9]))", "\u00A7$2");
 
         string = string.replaceAll("(&([a-fA-F0-9]))", "\u00A7$2");
 
         return string.replace("&&", "&");
     }
 
     public void log(Object loggedObject) {
         System.out.println(loggedObject);
     }
 }
