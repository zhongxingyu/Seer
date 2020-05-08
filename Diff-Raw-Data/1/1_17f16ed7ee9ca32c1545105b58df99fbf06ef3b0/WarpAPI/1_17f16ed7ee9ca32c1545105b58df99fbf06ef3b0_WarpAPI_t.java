 package com.legit2.Demigods.API;
 
 import com.legit2.Demigods.Demigods;
 import com.legit2.Demigods.Libraries.Objects.Altar;
 import com.legit2.Demigods.Libraries.Objects.PlayerCharacter;
 import com.legit2.Demigods.Libraries.Objects.SerialLocation;
 
 import java.util.ArrayList;
 
 public class WarpAPI
 {
     private static final Demigods API = Demigods.INSTANCE;
 
     public ArrayList<SerialLocation> getWarps(PlayerCharacter character)
     {
         if(character == null || API.data.getCharData(character.getID(), "warps") == null) return null;
         return (ArrayList<SerialLocation>) API.data.getCharData(character.getID(), "warps");
     }
 
     public boolean hasWarp(Altar altar, PlayerCharacter character)
     {
        if(getWarps(character) == null) return false;
         for(SerialLocation warp : getWarps(character))
         {
             if(API.zone.zoneAltar(warp.unserialize()) == altar) return true;
         }
         return false;
     }
 
     public boolean hasInvites(PlayerCharacter character)
     {
          return getInvites(character) != null;
     }
 
     public SerialLocation getInvite(PlayerCharacter inviting, PlayerCharacter invited)
     {
         if(hasInvites(invited))
         {
             for(SerialLocation invite : getInvites(invited))
             {
                 if(invite.getName().equalsIgnoreCase(inviting.getName())) return invite;
             }
         }
         return null;
     }
     public SerialLocation getInvite(PlayerCharacter character, String name)
     {
         if(hasInvites(character))
         {
             for(SerialLocation invite : getInvites(character))
             {
                 if(invite.getName().equalsIgnoreCase(name)) return invite;
             }
         }
         return null;
     }
 
     public boolean alreadyInvited(PlayerCharacter inviting, PlayerCharacter invited)
     {
         if(getInvite(inviting, invited) != null) return true;
         return false;
     }
 
     public ArrayList<SerialLocation> getInvites(PlayerCharacter character)
     {
         return (ArrayList<SerialLocation>) API.data.getCharData(character.getID(), "temp_invites");
     }
 
     public void addInvite(PlayerCharacter inviting, PlayerCharacter invited)
     {
         ArrayList<SerialLocation> invites;
         if(hasInvites(invited)) invites = getInvites(invited);
         else invites = new ArrayList<SerialLocation>();
         invites.add(new SerialLocation(inviting.getOwner().getPlayer().getLocation(), inviting.getName()));
         API.data.saveCharData(invited.getID(), "temp_invites", invites);
     }
 
     public void removeInvite(PlayerCharacter invited, SerialLocation invite)
     {
         ArrayList<SerialLocation> invites;
         if(hasInvites(invited)) invites = getInvites(invited);
         else return;
         invites.remove(invite);
         API.data.saveCharData(invited.getID(), "temp_invites", invites);
     }
 
     public void clearInvites(PlayerCharacter invited)
     {
         API.data.removeCharData(invited.getID(), "temp_invites");
     }
 }
