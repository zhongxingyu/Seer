 package net.aetherteam.aether;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.aetherteam.aether.donator.Donator;
 import net.aetherteam.aether.donator.DonatorChoice;
 import net.aetherteam.aether.donator.DonatorTexture;
 import net.aetherteam.aether.donator.EnumChoiceType;
 import net.aetherteam.aether.donator.SyncDonatorList;
 import net.aetherteam.aether.donator.choices.MoaChoice;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.potion.Potion;
 
 public class AetherMoaColour
 {
     public int ID;
     public int colour;
     public int jumps;
     public int chance;
     public String name;
    public String texturename;
     public static List names = new ArrayList();
     private static int totalChance;
     public static List colours = new ArrayList();
     private static Random random = new Random();
 
    public AetherMoaColour(int i, int j, int k, int l, String s, String texture)
     {
         this.ID = i;
         this.colour = j;
         this.jumps = k;
         this.chance = l;
         totalChance += l;
         this.name = s;
        this.texturename = texture;
         colours.add(this);
         names.add(this.name);
     }
 
     public String getTexture(boolean saddled)
     {
        return "/net/aetherteam/aether/client/sprites/moa/" + (saddled ? "saddle_" : "moa_") + this.texturename + ".png";
     }
 
     public String getTexture(boolean saddled, EntityPlayer player)
     {
         if ((Aether.syncDonatorList.isDonator(player.username)) && (saddled))
         {
             Aether.getInstance();
             Donator donator = Aether.syncDonatorList.getDonator(player.username);
             boolean hasChoice = donator.containsChoiceType(EnumChoiceType.MOA);
             DonatorChoice choice = null;
 
             if (hasChoice)
             {
                 choice = (MoaChoice) donator.getChoiceFromType(EnumChoiceType.MOA);
 
                 return choice.textureFile.localURL;
             }
         }
 
         return "/net/aetherteam/aether/client/sprites/moa/" + ((saddled) && (!player.isPotionActive(Potion.invisibility)) ? "saddle_" : "moa_") + this.name + ".png";
     }
 
     public static AetherMoaColour pickRandomMoa()
     {
         int i = random.nextInt(totalChance);
         for (int j = 0; j < colours.size(); j++)
         {
             if (i < ((AetherMoaColour) colours.get(j)).chance)
             {
                 return (AetherMoaColour) colours.get(j);
             }
             i -= ((AetherMoaColour) colours.get(j)).chance;
         }
         return (AetherMoaColour) colours.get(0);
     }
 
     public static AetherMoaColour getColour(int ID)
     {
         for (int i = 0; i < colours.size(); i++)
         {
             if (((AetherMoaColour) colours.get(i)).ID == ID)
             {
                 return (AetherMoaColour) colours.get(i);
             }
         }
         return (AetherMoaColour) colours.get(0);
     }
 
     public static String[] getNames()
     {
         String[] namesArray = new String[names.size()];
         namesArray = (String[]) names.toArray(namesArray);
         return namesArray;
     }
 
     static
     {
        new AetherMoaColour(0, 7829503, 3, 100, "蓝色", "Blue");
        new AetherMoaColour(1, 16777215, 4, 20, "白色", "White");
        new AetherMoaColour(2, 2236962, 8, 5, "黑色", "Black");
     }
 }
