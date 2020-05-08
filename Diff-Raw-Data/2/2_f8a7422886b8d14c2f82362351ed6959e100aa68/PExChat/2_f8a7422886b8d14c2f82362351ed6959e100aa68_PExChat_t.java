 /*     */ package com.voidteam.PExChat;
 /*     */ 
 /*     */ import java.io.File;
 /*     */ import java.io.IOException;
 /*     */ import java.text.SimpleDateFormat;
 /*     */ import java.util.ArrayList;
 /*     */ import java.util.Date;
 /*     */ import java.util.HashMap;
 /*     */ import java.util.HashSet;
 /*     */ import java.util.List;
 /*     */ import java.util.Set;
 /*     */ import java.util.logging.Logger;
 /*     */ import java.util.regex.Matcher;
 /*     */ import java.util.regex.Pattern;
 /*     */ import org.bukkit.command.Command;
 /*     */ import org.bukkit.command.CommandSender;
 /*     */ import org.bukkit.configuration.file.FileConfiguration;
 /*     */ import org.bukkit.configuration.file.YamlConfiguration;
 /*     */ import org.bukkit.entity.Player;
 /*     */ import org.bukkit.plugin.PluginManager;
 /*     */ import org.bukkit.plugin.java.JavaPlugin;
 /*     */ import ru.tehkode.permissions.PermissionManager;
 /*     */ import ru.tehkode.permissions.bukkit.PermissionsEx;
 /*     */ 
 /*     */ public class PExChat extends JavaPlugin
 /*     */ {
 /*  55 */   public PermissionManager permissions = null;
 /*     */ 
 /*  57 */   private playerListener pListener = new playerListener(this);
 /*     */   private PluginManager pm;
 /*  62 */   public Logger console = null;
 /*     */   FileConfiguration config;
 /*     */   File configFile;
 /*  67 */   public String censorChar = "*";
 /*  68 */   public boolean censorColored = false;
 /*  69 */   public String censorColor = "&f";
 /*  70 */   public String chatColor = "&f";
 /*  71 */   public List<String> censorWords = new ArrayList<String>();
 /*  72 */   public String chatFormat = "[+prefix+group+suffix&f] +name: +message";
 /*  73 */   public String multigroupFormat = "[+prefix+group+suffix&f]";
 /*  74 */   public String meFormat = "* +name +message";
 /*  75 */   public String dateFormat = "HH:mm:ss";
 /*  76 */   public List<Track> tracks = new ArrayList<Track>();
 /*  77 */   public HashMap<String, String> aliases = new HashMap<String, String>();
 /*     */ 
 /*  80 */   public static PExChat pexchat = null;
 /*     */ 
 /*     */   public void onEnable() {
 /*  83 */     this.pm = getServer().getPluginManager();
 /*  84 */     this.console = Logger.getLogger("Minecraft");
 /*     */ 
 /*  88 */     if (this.pm.isPluginEnabled("PermissionsEx")) {
 /*  89 */       this.permissions = PermissionsEx.getPermissionManager();
 /*     */     }
 /*     */     else {
 /*  92 */       this.console.info("[PExChat] Permissions plugin not found or wrong version. Disabling");
 /*  93 */       this.pm.disablePlugin(this);
 /*  94 */       return;
 /*     */     }
 /*     */ 
 /*  98 */     this.configFile = new File(getDataFolder() + "/config.yml");
 /*  99 */     if (!this.configFile.exists()) {
 /* 100 */       defaultConfig();
 /*     */     }
 /* 102 */     loadConfig();
 /*     */ 
 /* 105 */     this.pm.registerEvents(this.pListener, this);
 /* 106 */     this.pm.registerEvents(this.pListener, this);
 /*     */ 
 /* 109 */     pexchat = this;
 /*     */ 
 /* 111 */     this.console.info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " enabled");
 /*     */   }
 /*     */ 
 /*     */   public void onDisable() {
 /* 115 */     this.console.info("[" + getDescription().getName() + "] PExChat Disabled");
 /*     */   }
 /*     */ 
 /*     */   private void loadConfig() {
 /* 119 */     this.config = YamlConfiguration.loadConfiguration(this.configFile);
 /* 120 */     this.censorChar = this.config.getString("censor-char");
 /* 121 */     this.censorColored = this.config.getBoolean("censor-colored");
 /* 122 */     this.censorColor = this.config.getString("censor-color");
 /* 123 */     this.chatColor = this.config.getString("censor-string-color");
 /* 124 */     this.censorWords = this.config.getStringList("censor-list");
 /* 125 */     this.chatFormat = this.config.getString("message-format");
 /* 126 */     this.multigroupFormat = this.config.getString("multigroup-format");
 /* 127 */     this.dateFormat = this.config.getString("date-format");
 /* 128 */     this.meFormat = this.config.getString("me-format");
 /* 129 */     Set<String> tracknames = new HashSet<String>();
 /* 130 */     tracknames = this.config.getConfigurationSection("tracks").getKeys(false);
 /*     */     Track loadtrack;
 /* 132 */     if (tracknames != null) {
 /* 133 */       for (String track : tracknames) {
 /* 134 */         loadtrack = new Track();
 /* 135 */         loadtrack.groups = this.config.getStringList("tracks." + track + ".groups");
 /* 136 */         loadtrack.priority = Integer.valueOf(this.config.getInt("tracks." + track + ".priority", 0));
 /* 137 */         loadtrack.name = track;
 /* 138 */         this.tracks.add(loadtrack);
 /*     */       }
 /*     */     }
 /* 141 */     Set<String> tmpaliases = new HashSet<String>();
 /* 142 */     tmpaliases = this.config.getConfigurationSection("aliases").getKeys(false);
 /* 143 */     if (tmpaliases != null)
 /* 144 */       for (String alias : tmpaliases)
 /* 145 */         this.aliases.put(alias, this.config.getString("aliases." + alias));
 /*     */   }
 /*     */ 
 /*     */   private void defaultConfig()
 /*     */   {
 /* 151 */     this.config = YamlConfiguration.loadConfiguration(this.configFile);
 /* 152 */     this.config.set("censor-char", this.censorChar);
 /* 153 */     this.config.set("censor-colored", Boolean.valueOf(this.censorColored));
 /* 154 */     this.config.set("censor-color", this.censorColor);
 /* 155 */     this.config.set("censor-string-color", this.chatColor);
 /* 156 */     this.config.set("censor-list", this.censorWords);
 /* 157 */     this.config.set("message-format", this.chatFormat);
 /* 158 */     this.config.set("multigroup-format", this.multigroupFormat);
 /* 159 */     this.config.set("date-format", this.dateFormat);
 /* 160 */     this.config.set("me-format", this.meFormat);
 /* 161 */     HashMap<String, String> aliases = new HashMap<String, String>();
 /* 162 */     aliases.put("Admin", "A");
 /* 163 */     List<String> track = new ArrayList<String>();
 /* 164 */     track.add("Admin");
 /* 165 */     track.add("Moderator");
 /* 166 */     track.add("Builder");
 /* 167 */     this.config.set("tracks.default.groups", track);
 /* 168 */     this.config.set("tracks.default.priority", Integer.valueOf(1));
 /* 169 */     this.config.set("aliases", aliases);
 /*     */     try {
 /* 171 */       this.config.save(this.configFile);
 /*     */     } catch (IOException e) {
 /* 173 */       e.printStackTrace();
 /*     */     }
 /*     */   }
 /*     */ 
 /*     */   public String parseVars(String format, Player p)
 /*     */   {
 /* 181 */     Pattern pattern = Pattern.compile("\\+\\{(.*?)\\}");
 /* 182 */     Matcher matcher = pattern.matcher(format);
 /* 183 */     StringBuffer sb = new StringBuffer();
 /* 184 */     while (matcher.find()) {
 /* 185 */       String var = getVariable(p, matcher.group(1));
 /* 186 */       matcher.appendReplacement(sb, Matcher.quoteReplacement(var));
 /*     */     }
 /* 188 */     matcher.appendTail(sb);
 /* 189 */     return sb.toString();
 /*     */   }
 /*     */ 
 /*     */   public String replaceVars(String format, String[] search, String[] replace)
 /*     */   {
 /* 196 */     if (search.length != replace.length) return "";
 /* 197 */     for (int i = 0; i < search.length; i++) {
 /* 198 */       if (search[i].contains(",")) {
 /* 199 */         for (String s : search[i].split(","))
 /* 200 */           if ((s != null) && (replace[i] != null))
 /* 201 */             format = format.replace(s, replace[i]);
 /*     */       }
 /*     */       else {
 /* 204 */         format = format.replace(search[i], replace[i]);
 /*     */       }
 /*     */     }
 /* 207 */     return format.replaceAll("(&([a-f0-9]))", "§$2");
 /*     */   }
 /*     */ 
 /*     */   public String censor(Player p, String msg)
 /*     */   {
 /* 214 */     if ((this.censorWords == null) || (this.censorWords.size() == 0)) {
 /* 215 */       if (!hasPerm(p, "pexchat.color")) {
 /* 216 */         return msg.replaceAll("(&([a-f0-9]))", "");
 /*     */       }
 /* 218 */       return msg;
 /*     */     }
 /* 220 */     String[] split = msg.split(" ");
 /* 221 */     StringBuilder out = new StringBuilder();
 /*     */ 
 /* 223 */     for (String word : split) {
 /* 224 */       for (String cen : this.censorWords) {
 /* 225 */         if (word.equalsIgnoreCase(cen)) {
 /* 226 */           word = star(word);
 /* 227 */           if (!this.censorColored) break;
 /* 228 */           word = this.censorColor + word + this.chatColor;
 /*     */ 
 /* 230 */           break;
 /*     */         }
 /*     */       }
 /* 233 */       out.append(word).append(" ");
 /*     */     }
 /* 235 */     if (!hasPerm(p, "pexchat.color")) {
 /* 236 */       return out.toString().replaceAll("(&([a-f0-9]))", "").trim();
 /*     */     }
 /* 238 */     return out.toString().trim();
 /*     */   }
 /*     */   private String star(String word) {
 /* 241 */     StringBuilder out = new StringBuilder();
 /* 242 */     for (int i = 0; i < word.length(); i++)
 /* 243 */       out.append(this.censorChar);
 /* 244 */     return out.toString();
 /*     */   }
 /*     */ 
 /*     */   public String parseChat(Player p, String msg, String chatFormat)
 /*     */   {
 /* 255 */     String prefix = getPrefix(p);
 /* 256 */     String suffix = getSuffix(p);
 /* 257 */     String group = getGroup(p);
 /* 258 */     if (prefix == null) prefix = "";
 /* 259 */     if (suffix == null) suffix = "";
 /* 260 */     if (group == null) group = "";
 /* 261 */     String healthbar = healthBar(p);
 /* 262 */     String health = String.valueOf(p.getHealth());
 /* 263 */     String world = p.getWorld().getName();
 /*     */ 
 /* 265 */     Date now = new Date();
 /* 266 */     SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
 /* 267 */     String time = dateFormat.format(now);
 /*     */ 
 /* 270 */     msg = msg.replaceAll("%", "%%");
 /*     */ 
 /* 272 */     msg = censor(p, msg);
 /*     */ 
 /* 275 */     String format = parseVars(chatFormat, p);
 /*     */ 
 /* 278 */     String groups = "";
 /* 279 */     if (format.contains("+groups")) {
 /* 280 */       groups = parseGroups(p, this.multigroupFormat);
 /*     */     }
 /*     */ 
 /* 284 */     ArrayList<String> searchlist = new ArrayList<String>();
 /* 285 */     ArrayList<String> replacelist = new ArrayList<String>();
 /*     */ 
 /* 288 */     String[] playergroups = this.permissions.getUser(p).getGroupsNames();
 /*     */ 
 /* 292 */     for (Track track : this.tracks) {
 /* 293 */       Boolean found = Boolean.valueOf(false);
 /*     */ 
 /* 295 */       for (String playergroup : playergroups) {
 /* 296 */         if (track.groups.contains(playergroup))
 /*     */         {
 /* 298 */           searchlist.add("+prefix." + track.name);
 /* 299 */           searchlist.add("+suffix." + track.name);
 /* 300 */           searchlist.add("+group." + track.name);
 /*     */ 
 /* 302 */           replacelist.add(getGroupPrefix(playergroup, p.getWorld().getName()));
 /* 303 */           replacelist.add(getGroupSuffix(playergroup, p.getWorld().getName()));
 /* 304 */           replacelist.add(getAlias(playergroup));
 /*     */ 
 /* 306 */           found = Boolean.valueOf(true);
 /*     */         }
 /*     */       }
 /* 309 */       if (found.equals(Boolean.valueOf(false))) {
 /* 310 */         searchlist.add("+prefix." + track.name);
 /* 311 */         searchlist.add("+suffix." + track.name);
 /* 312 */         searchlist.add("+group." + track.name);
 /* 313 */         replacelist.add("");
 /* 314 */         replacelist.add("");
 /* 315 */         replacelist.add("");
 /*     */       }
 /*     */ 
 /*     */     }
 /*     */ 
 /* 321 */     String[] search = { "+suffix,+s", "+prefix,+p", "+groups,+gs", "+group,+g", "+healthbar,+hb", "+health,+h", "+world,+w", "+time,+t", "+name,+n", "+displayname,+d", "+message,+m" };
 /* 322 */     String[] replace = { suffix, prefix, groups, group, healthbar, health, world, time, p.getName(), p.getDisplayName(), msg };
 /* 323 */     for (int i = 0; i < search.length; i++) {
 /* 324 */       searchlist.add(search[i]);
 /*     */     }
 /* 326 */     for (int i = 0; i < replace.length; i++) {
 /* 327 */       replacelist.add(replace[i]);
 /*     */     }
 /*     */ 
 /* 331 */     search = (String[])searchlist.toArray(new String[searchlist.size()]);
 /* 332 */     replace = (String[])replacelist.toArray(new String[replacelist.size()]);
 /*     */ 
 /* 334 */     return replaceVars(format, search, replace);
 /*     */   }
 /*     */ 
 /*     */   public String parseChat(Player p, String msg)
 /*     */   {
 /* 344 */     return parseChat(p, msg, this.chatFormat);
 /*     */   }
 /*     */ 
 /*     */   public String parseGroups(Player p, String mgFormat)
 /*     */   {
 /* 356 */     String[] groups = this.permissions.getUser(p).getGroupsNames();
 /*     */ 
 /* 358 */     String output = "";
 /* 359 */     HashMap<Integer, String> unparsedGroups = new HashMap<Integer, String>();
 /* 360 */     int max = 0;
 /* 361 */     int key = 0;
 /*     */ 
 /* 364 */     for (String group : groups)
 /*     */     {
 /* 366 */       for (Track track : this.tracks)
 /*     */       {
 /* 368 */         if (track.priority.intValue() >= 1)
 /*     */         {
 /* 372 */           for (String trackgroup : track.groups) {
 /* 373 */             if (trackgroup.equalsIgnoreCase(group))
 /*     */             {
 /* 375 */               key = track.priority.intValue();
 /* 376 */               while (unparsedGroups.containsKey(Integer.valueOf(key))) {
 /* 377 */                 key++;
 /*     */               }
 /* 379 */               unparsedGroups.put(Integer.valueOf(key), group);
 /* 380 */               if (key > max) {
 /* 381 */                 max = key;
 /*     */               }
 /*     */             }
 /*     */           }
 /*     */         }
 /*     */       }
 /*     */     }
 /*     */ 
 /* 389 */     String format = parseVars(mgFormat, p);
 /*     */ 
 /* 392 */     for (int i = 0; i <= max; i++) {
 /* 393 */       if (unparsedGroups.containsKey(Integer.valueOf(i))) {
 /* 394 */         String groupname = (String)unparsedGroups.get(Integer.valueOf(i));
 /* 395 */         String prefix = getGroupPrefix(groupname, p.getWorld().getName());
 /* 396 */         if (prefix == null) {
 /* 397 */           prefix = "";
 /*     */         }
 /* 399 */         String suffix = getGroupSuffix(groupname, p.getWorld().getName());
 /* 400 */         if (suffix == null) {
 /* 401 */           suffix = "";
 /*     */         }
 /* 403 */         groupname = getAlias(groupname);
 /*     */ 
 /* 406 */         String[] search = { "+suffix,+s", "+prefix,+p", "+group,+g" };
 /* 407 */         String[] replace = { suffix, prefix, groupname };
 /* 408 */         output = output + replaceVars(format, search, replace);
 /*     */       }
 /*     */     }
 /*     */ 
 /* 412 */     return output;
 /*     */   }
 /*     */ 
 /*     */   private String getAlias(String group)
 /*     */   {
 /* 421 */     if (this.aliases.containsKey(group)) {
 /* 422 */       return (String)this.aliases.get(group);
 /*     */     }
 /* 424 */     return group;
 /*     */   }
 /*     */ 
 /*     */   public String healthBar(Player player)
 /*     */   {
 /* 434 */     float maxHealth = 20.0F;
 /* 435 */     float barLength = 10.0F;
/* 436 */     float health = (float) player.getHealth();
 /* 437 */     int fill = Math.round(health / maxHealth * barLength);
 /* 438 */     String barColor = "&2";
 /*     */ 
 /* 440 */     if (fill <= 4) barColor = "&4";
 /* 441 */     else if (fill <= 7) barColor = "&e"; else {
 /* 442 */       barColor = "&2";
 /*     */     }
 /* 444 */     StringBuilder out = new StringBuilder();
 /* 445 */     out.append(barColor);
 /* 446 */     for (int i = 0; i < barLength; i++) {
 /* 447 */       if (i == fill) out.append("&8");
 /* 448 */       out.append("|");
 /*     */     }
 /* 450 */     out.append("&f");
 /* 451 */     return out.toString();
 /*     */   }
 /*     */ 
 /*     */   public boolean hasPerm(Player player, String perm)
 /*     */   {
 /* 461 */     if (this.permissions.has(player, perm)) {
 /* 462 */       return true;
 /*     */     }
 /* 464 */     return player.isOp();
 /*     */   }
 /*     */ 
 /*     */   public String getPrefix(Player player)
 /*     */   {
 /* 474 */     if (this.permissions != null) {
 /* 475 */       return this.permissions.getUser(player).getPrefix(player.getWorld().getName());
 /*     */     }
 /* 477 */     this.console.severe("[ There is no Permissions module, why are we running?!??!?");
 /* 478 */     return null;
 /*     */   }
 /*     */ 
 /*     */   public String getSuffix(Player player)
 /*     */   {
 /* 487 */     if (this.permissions != null) {
 /* 488 */       return this.permissions.getUser(player).getSuffix(player.getWorld().getName());
 /*     */     }
 /* 490 */     this.console.severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
 /* 491 */     return null;
 /*     */   }
 /*     */ 
 /*     */   public String getGroupPrefix(String group, String worldname)
 /*     */   {
 /* 501 */     if (this.permissions != null) {
 /* 502 */       return this.permissions.getGroup(group).getPrefix(worldname);
 /*     */     }
 /* 504 */     this.console.severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
 /* 505 */     return null;
 /*     */   }
 /*     */ 
 /*     */   public String getGroupSuffix(String group, String worldname)
 /*     */   {
 /* 515 */     if (this.permissions != null) {
 /* 516 */       return this.permissions.getGroup(group).getSuffix(worldname);
 /*     */     }
 /* 518 */     this.console.severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
 /* 519 */     return null;
 /*     */   }
 /*     */ 
 /*     */   public String getGroup(Player player)
 /*     */   {
 /* 528 */     if (this.permissions != null) {
 /* 529 */       String[] groups = this.permissions.getUser(player).getGroupsNames(player.getWorld().getName());
 /* 530 */       return groups[0];
 /*     */     }
 /* 532 */     this.console.severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
 /* 533 */     return null;
 /*     */   }
 /*     */ 
 /*     */   public String getVariable(Player player, String variable)
 /*     */   {
 /* 543 */     if (this.permissions != null)
 /*     */     {
 /* 545 */       String userVar = this.permissions.getUser(player).getOption(variable);
 /* 546 */       if ((userVar != null) && (!userVar.isEmpty())) {
 /* 547 */         return userVar;
 /*     */       }
 /*     */ 
 /* 550 */       String group = this.permissions.getGroup(getGroup(player)).getName();
 /*     */ 
 /* 552 */       if (group == null) return "";
 /* 553 */       String groupVar = this.permissions.getGroup(group).getOption(variable);
 /*     */ 
 /* 555 */       if (groupVar == null) return "";
 /* 556 */       return groupVar;
 /*     */     }
 /* 558 */     this.console.severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
 /* 559 */     return "";
 /*     */   }
 /*     */ 
 /*     */   public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 /*     */   {
 /* 566 */     if (!command.getName().equalsIgnoreCase("pexchat")) return false;
 /* 567 */     if (((sender instanceof Player)) && (!hasPerm((Player)sender, "pexchat.reload"))) {
 /* 568 */       sender.sendMessage("[PExChat] Permission Denied");
 /* 569 */       return true;
 /*     */     }
 /* 571 */     if (args.length != 1) return false;
 /* 572 */     if (args[0].equalsIgnoreCase("reload")) {
 /* 573 */       this.aliases.clear();
 /* 574 */       this.tracks.clear();
 /* 575 */       loadConfig();
 /* 576 */       sender.sendMessage("[PExChat] Config Reloaded");
 /* 577 */       return true;
 /*     */     }
 /* 579 */     return false;
 /*     */   }
 /*     */ 
 /*     */   public final class Track
 /*     */   {
 /*  50 */     public String name = "";
 /*  51 */     public Integer priority = Integer.valueOf(0);
 /*  52 */     public List<String> groups = new ArrayList<String>();
 /*     */ 
 /*     */     public Track()
 /*     */     {
 /*     */     }
 /*     */   }
 /*     */ }
 
 /* Location:           /Users/jklink/Desktop/PExChat.jar
  * Qualified Name:     com.Sleelin.PExChat.PExChat
  * JD-Core Version:    0.6.2
  */
