 package net.canarymod.api.entity;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.canarymod.Canary;
 import net.canarymod.CanaryServer;
 import net.canarymod.Colors;
 import net.canarymod.Logman;
 import net.canarymod.api.NetServerHandler;
 import net.canarymod.api.Packet;
 import net.canarymod.api.inventory.Inventory;
 import net.canarymod.api.inventory.Item;
 import net.canarymod.api.world.CanaryDimension;
 import net.canarymod.api.world.Dimension;
 import net.canarymod.api.world.position.Direction;
 import net.canarymod.api.world.position.Location;
 import net.canarymod.api.world.position.Vector3D;
 import net.canarymod.commands.CanaryCommand;
 import net.canarymod.hook.command.PlayerCommandHook;
 import net.canarymod.hook.player.ChatHook;
 import net.canarymod.permissionsystem.PermissionProvider;
 import net.canarymod.user.Group;
 import net.minecraft.server.OAchievementList;
 import net.minecraft.server.OChunkCoordinates;
 import net.minecraft.server.OEntityPlayerMP;
 import net.minecraft.server.OMinecraftServer;
 import net.minecraft.server.OStatBase;
 
 /**
  * Canary Player wrapper.
  * @author Chris
  *
  */
 public class CanaryPlayer extends CanaryEntityLiving implements Player {
     private Pattern badChatPattern = Pattern.compile("[\u00a7\u2302\u00D7\u00AA\u00BA\u00AE\u00AC\u00BD\u00BC\u00A1\u00AB\u00BB]");
     private Group group; 
     private PermissionProvider permissions;
     private String prefix = null;
     private boolean muted;
     private String[] allowedIPs;
     
     public CanaryPlayer(OEntityPlayerMP entity) {
         super(entity);
         String[] data = Canary.usersAndGroups().getPlayerData(getName());
         group = Canary.usersAndGroups().getGroup(data[1]); 
         permissions = Canary.permissionManager().getPlayerProvider(getName());
         
         if(data[0] != null && (!data[0].isEmpty() && !data[0].equals(" "))) {
             prefix = data[0];
         }
         
         if(data[2] != null && !data[2].isEmpty()) {
             allowedIPs = data[2].split(",");
         }
     }
 
     /**
      * CanaryMod: Get player handle
      */
     @Override
     public OEntityPlayerMP getHandle() {
         return (OEntityPlayerMP) entity;
     }
     @Override
     public String getName() {
     	return ((OEntityPlayerMP)entity).v;
     }
     
     @Override
     public void chat(String message) {
         if (message.length() > 100) {
             kick("Message too long!");
         } 
         message = message.trim();
         Matcher m = badChatPattern.matcher(message);
         String out = message;
         
         if (m.find() && !this.canIgnoreRestrictions()) {
             out = message.replaceAll(m.group(), "");
         }
         message = out;
         
         //TODO: Add configuration for spam protection?
         
         if(message.startsWith("/")) {
             executeCommand(message.split(" "));
         }
         else {
             if(isMuted()) {
                 notify("You are currently muted!");
             }
             else {
                 String prefix = "<" + getColor() + getName() + Colors.White + "> ";
                 ArrayList<Player> receivers = (ArrayList<Player>) Canary.getServer().getPlayerList();
                 ChatHook hook = (ChatHook) Canary.hooks().callCancelableHook(new ChatHook(this, prefix, message, receivers));
                 if(hook.isCancelled()) {
                     return;
                 }
                 receivers = hook.getReceiverList();
                 for(Player player : receivers) {
                     if (hook.getPrefix().length() + hook.getMessage().length() >= 100) {
                         player.sendMessage(hook.getPrefix());
                         player.sendMessage(hook.getMessage().toString());
                     } else {
                         player.sendMessage(hook.getPrefix()+hook.getMessage().toString());
                     }
                 }
             }
         }
 
     }
 
     @Override
     public void sendMessage(String message) {
         getNetServerHandler().sendMessage(message);
     }
 
     @Override
     public void addExhaustion(float exhaustion) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void removeExhaustion(float exhaustion) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public float getExhaustionLevel() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public void setHunger(int hunger) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public int getHunger() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public void addExperience(int experience) {
       //TODO: Requires work in OEntityPlayer about experience management
         
     }
 
     @Override
     public void removeExperience(int experience) {
         //TODO: Requires work in OEntityPlayer about experience management
         
     }
 
     @Override
     public int getExperience() {
         return ((OEntityPlayerMP)entity).N;
     }
 
     @Override
     public boolean isSleeping() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void setSleeping(boolean sleeping) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void destroyItemHeld() {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public Item getItemHeld() {
        // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void dropItem(Item item) {
         getDimension().dropItem((int)getX(), (int)getY(), (int)getZ(), item);
     }
 
     @Override
     public Location getSpawnPosition() {
         Location spawn = Canary.getServer().getDefaultWorld().getNormal().getSpawnLocation();
         OChunkCoordinates loc = ((OEntityPlayerMP)entity).ab();
         if (loc != null) {
             spawn = new Location(Canary.getServer().getDefaultWorld().getNormal(), loc.a, loc.b, loc.c, 0.0F, 0.0F);
         }
         return spawn;
     }
 
     @Override
     public void setSpawnPosition(Location spawn) {
         OChunkCoordinates loc = new OChunkCoordinates((int)spawn.getX(), (int)spawn.getY(), (int)spawn.getZ());
         ((OEntityPlayerMP)entity).a(loc);
     }
 
     @Override
     public String getIP() {
         String ip = ((OEntityPlayerMP)entity).a.b.c().toString();
         return ip.substring(1,ip.lastIndexOf(":"));
     }
 
     @Override
     public boolean executeCommand(String[] command) {
         try {
             String cmd = command[0];
             if (cmd.startsWith("/#") && hasPermission("canary.commands.vanilla."+cmd.replace("/#", ""))) {
                 Canary.getServer().consoleCommand(Canary.glueString(command, 0, " ").replace("/#", ""), this);
                 return true;
             }
             
             PlayerCommandHook hook = (PlayerCommandHook) Canary.hooks().callCancelableHook(new PlayerCommandHook(this, command));
             if (hook.isCancelled()) {
                 return true;
             } // No need to go on, commands were parsed.
             
             //Check for canary permissions
             
             CanaryCommand toExecute = CanaryCommand.fromString(cmd.replace("/", ""));
             if(hasPermission("canary.command.tphere")) {
                 sendMessage(Colors.LightGreen + "You can do it!");
             }
             if(toExecute == null) {
                 sendMessage(Colors.Rose + "Unknown command!");
                 return false;
             }
             else {
                 if(!toExecute.execute(this, command)) {
                     sendMessage(Colors.Rose + "Permission denied!");
                     return false;
                 }
                 return true;
             }
             
         } catch (Throwable ex) {
             Logman.logStackTrace("Exception in command handler: ", ex);
             if (isAdmin()) {
                 sendMessage(Colors.Rose + "Exception occured. "+ex.getMessage());
             }
             return false;
         }
     }
 
     @Override
     public boolean canFly() {
         return hasPermission("canary.player.canFly");
     }
 
     @Override
     public boolean isFlying() {
         return ((OEntityPlayerMP)entity).L.b;
     }
 
     @Override
     public void setFlying(boolean flying) {
         ((OEntityPlayerMP)entity).L.b = flying;
     }
 
     @Override
     public void sendPacket(Packet packet) {
         getDimension().getEntityTracker().sendPacketToTrackedPlayer(this, packet);
     }
 
     @Override
     public Group getGroup() {
         return group;
     }
 
     @Override
     public Group[] getPlayerGroups() {
         return group.parentsToList().toArray(new Group[]{});
     }
 
     @Override
     public void setGroup(Group group) {
          this.group = group;
          Canary.usersAndGroups().addOrUpdatePlayerData(this);
     }
 
     @Override
     public boolean hasPermission(String permission) {
         if(!group.hasPermission(permission)) {
             return permissions.queryPermission(permission);
         }
         return true;
     }
 
     @Override
     public boolean isAdmin() {
         if(!permissions.queryPermission("canary.player.administrator")) {
             return group.isAdministratorGroup();
         }
         return true;
     }
 
     @Override
     public boolean canBuild() {
         if(!group.canBuild()) {
             return permissions.queryPermission("canary.world.build");
         }
         return true;
     }
 
     @Override
     public void setCanBuild(boolean canModify) {
         permissions.addPermission("canary.world.build", canModify, -1);
     }
 
     @Override
     public boolean canIgnoreRestrictions() {
         if(!group.canIgnorerestrictions()) {
             return permissions.queryPermission("canary.player.ignoreRestrictions");
         }
         return true;
     }
 
     @Override
     public void setCanIgnoreRestrictions(boolean canIgnore) {
         permissions.addPermission("canary.player.ignoreRestrictions", canIgnore, -1);
     }
 
     @Override
     public boolean isMuted() {
         return muted;
     }
 
     @Override
     public void setMuted(boolean muted) {
          this.muted = muted;
     }
 
     @Override
     public PermissionProvider getPermissionProvider() {
         return permissions;
     }
 
     @Override
     public Location getLocation() {
         return new Location(entity.bi.getCanaryDimension(), getX(), getY(), getZ(), getPitch(), getRotation());
     }
     
     @Override
     public Vector3D getPosition() {
         return new Vector3D(getX(), getY(), getZ());
     }
 
     @Override
     public Inventory getInventory() {
         return ((OEntityPlayer)entity).getInventory();
     }
 
     @Override
     public void giveItem(Item item) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public boolean isInGroup(Group group, boolean parents) {
         if(this.group.name.equals(group.name)) {
             return true;
         }
         else {
             if(parents) {
                 return this.group.parentsToList().contains(group);
             }
             return false;
         }
     }
 
     @Override
     public void teleportTo(double x, double y, double z) {
         teleportTo(x, y, z, 0.0F, 0.0F);
     }
 
     public void teleportTo(Vector3D position) {
         teleportTo(position.getX(), position.getY(), position.getZ(), 0.0f, 0.0f);
     }
     @Override
     public void teleportTo(double x, double y, double z, Dimension dim) {
         if (!(getDimension().hashCode() == dim.hashCode())) {
             switchWorlds(dim);
         }
         teleportTo(x, y, z, 0.0F, 0.0F);
         
     }
 
     @Override
     public void teleportTo(double x, double y, double z, float pitch, float rotation, Dimension dim) {
         if (!(getDimension().hashCode() == dim.hashCode())) {
             switchWorlds(dim);
         }
         teleportTo(x, y, z, pitch, rotation);
     }
 
     @Override
     public void teleportTo(double x, double y, double z, float pitch, float rotation) {
         OEntityPlayerMP player = (OEntityPlayerMP) entity;
         // If player is in vehicle - eject them before they are teleported.
         if (player.bh != null) {
             player.b(player.bh);
         }
         player.a.a(x, y, z, rotation, pitch);
         
     }
 
     @Override
     public void teleportTo(Location location) {
         if (!(getDimension().hashCode() == location.getCanaryDimension().hashCode())) {
             switchWorlds(location.getCanaryDimension());
         }
         teleportTo(location.getX(),location.getY(), location.getZ(),location.getPitch(), location.getRotation());
     }
 
     @Override
     public void kick(String reason) {
         ((OEntityPlayerMP)entity).a.a(reason);
     }
 
     @Override
     public void notify(String message) {
         sendMessage(Colors.Rose+message);
         
     }
 
     @Override
     public String getColor() {
         if(prefix != null) {
             return Colors.Marker+prefix;
         }
         return Colors.Marker+group.prefix;
     }
 
     @Override
     public NetServerHandler getNetServerHandler() {
         return ((OEntityPlayerMP)entity).getServerHandler();
     }
 
     @Override
     public boolean isInGroup(String group, boolean parents) {
         if(parents) {
             ArrayList<Group> groups = this.group.parentsToList();
             for(Group g : groups) {
                 if(g.name.equals(group)) {
                     return true;
                 }
             }
         }
         else {
             if(this.group.name.equals(group)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public String[] getAllowedIPs() {
         return allowedIPs;
     }
 
     @Override
     public Direction getCardinalDirection() {
         double degrees = ((getRotation() - 90) % 360);
 
         if (degrees < 0) {
             degrees += 360.0;
         }
         
         if (0 <= degrees && degrees < 22.5) {
             return Direction.EAST;
         } else if (22.5 <= degrees && degrees < 67.5) {
             return Direction.SOUTHEAST;
         } else if (67.5 <= degrees && degrees < 112.5) {
             return Direction.SOUTH;
         } else if (112.5 <= degrees && degrees < 157.5) {
             return Direction.SOUTHWEST;
         } else if (157.5 <= degrees && degrees < 202.5) {
             return Direction.WEST;
         } else if (202.5 <= degrees && degrees < 247.5) {
             return Direction.NORTHWEST;
         } else if (247.5 <= degrees && degrees < 292.5) {
             return Direction.NORTH;
         } else if (292.5 <= degrees && degrees < 337.5) {
             return Direction.NORTHEAST;
         } else if (337.5 <= degrees && degrees < 360.0) {
             return Direction.EAST;
         } else {
             return Direction.ERROR;
         }
     }
     
     public void switchWorlds(Dimension dim) {
         OMinecraftServer mcServer = ((CanaryServer) Canary.getServer()).getHandle();
         OEntityPlayerMP ent = (OEntityPlayerMP) entity;
         
         // Nether is not allowed, so shush
         if (dim.getType() == Dimension.Type.NETHER && !mcServer.d.a("allow-nether", true)) {
             return;
         }
         // The End is not allowed, so shush
         if (dim.getType() == Dimension.Type.END && !mcServer.d.a("allow-end", true)) {
             return;
         }
         // Dismount first or get buggy
         if (ent.bh != null) {
             ent.b(ent.bh);
         }
 
         //Collect world switch achievement ?
         ent.a((OStatBase) OAchievementList.B);
         
         //switch world if needed
         if(!(dim.hashCode() == ent.bi.getCanaryDimension().hashCode())) {
             Dimension oldWorld = ent.bi.getCanaryDimension();
             //remove player from entity tracker
             oldWorld.getEntityTracker().untrackPlayerSymmetrics(this);
             oldWorld.getEntityTracker().untrackEntity(this);
             //remove player from old worlds entity list
             oldWorld.removePlayerFromWorld(this);
             
             //Remove player from player manager for the old world
             oldWorld.getPlayerManager().removePlayer(this);
             
             //Change players world reference
             ent.bi = ((CanaryDimension) dim).getHandle();
             //Add player back to the new world
             dim.addPlayerToWorld(this);
             dim.getEntityTracker().trackEntity(this);
         }
         //Get chunk coordinates...
 //        OChunkCoordinates var2 = mcServer.getWorld(ent.bi.getCanaryDimension().getName(), dim.getType().getId()).d();
         OChunkCoordinates var2 = ent.bi.d();
 
         if (var2 != null) {
             ent.a.a((double) var2.a, (double) var2.b, (double) var2.c, 0.0F, 0.0F);
         }
 
         mcServer.h.switchDimension(ent, dim.getType().getId(), false);
         
 //        refreshCreativeMode();
     }
     
 //    public void refreshCreativeMode() {
 //        if (getMode() || etc.getMCServer().d.a("gamemode", 0) == 1) {
 //            getEntity().c.a(1);
 //        } else {
 //            getEntity().c.a(0);
 //        }
 //    }
 
 }
