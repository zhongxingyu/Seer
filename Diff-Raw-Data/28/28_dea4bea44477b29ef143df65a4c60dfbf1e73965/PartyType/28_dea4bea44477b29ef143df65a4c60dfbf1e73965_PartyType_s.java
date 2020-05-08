 package net.aetherteam.aether.party;
 
 import java.io.Serializable;
 
 public enum PartyType implements Serializable
 {
    OPEN(6750054), CLOSED(16711680), PRIVATE(0);
 
     public int displayColor;
 
     private PartyType(int displayColor)
     {
         this.displayColor = displayColor;
     }
 
     public int getDisplayColor()
     {
         return this.displayColor;
     }
 
     public static PartyType getTypeFromString(String name)
     {
         for (PartyType type : values())
         {
             if (type.name().equalsIgnoreCase(name))
             {
                 return type;
             }
         }
 
         return null;
     }
 }

/* Location:           D:\Dev\Mc\forge_orl\mcp\jars\bin\aether.jar
 * Qualified Name:     net.aetherteam.aether.party.PartyType
 * JD-Core Version:    0.6.2
 */
