 /**
  * 
  */
 package net.lahwran.mcclient.capsystem;
 
 /**
  * @author lahwran
  *
  */
 public class ChatProcessor {
     private static final String cap = Capsystem.colorchar + "0" + Capsystem.colorchar + "0" + 
                                       Capsystem.colorchar + "0" + Capsystem.colorchar + "0";
     private static final String comm = Capsystem.colorchar + "0" + Capsystem.colorchar + "0";
     
     private static int colorDecode(String encoded)
     {
         StringBuilder hex = new StringBuilder();
         char colorchar = Capsystem.colorchar; //shouldn't it be a char to begin with?
         for(int i=0; i<encoded.length(); i++)
         {
             if(encoded.charAt(i) != colorchar)
             {
                 hex.append(encoded.charAt(i));
             }
         }
         return Integer.parseInt(hex.toString(), 16);
     }
     
     public static boolean processChat(String chat)
     {
         if(chat.startsWith(cap))
         {
             chat = chat.substring(cap.length());
             if(Capsystem.instance.connected && !Capsystem.instance.capableServer)
             {
                 Capsystem.instance._initCaps(colorDecode(chat));
             }
             else if (chat.equals("done"))
             {
                 Commsystem._ready();
             }
             else
             {
                String[] split = chat.trim().split(" ");
                 for (String s:split)
                 {
                     Capsystem.instance._addCap(s);
                 }
             }
             return true;
         }
         else if(chat.startsWith(comm))
         {
             chat = chat.substring(comm.length());
             Commsystem.dispatch(chat);
             return true;
         }
         return false;
     }
     
     static {
         Commsystem.instance.equals(null);
         Capsystem.instance.equals(null);
     }
 }
