 package packets;
 
 // For all packet classes
 import util.BnetPacket;
 import constants.PacketConstants;
 import exceptions.*;
 import callback_interfaces.PublicExposedFunctions;
 
 // For this class only
 import java.util.Date;
 
 import util.ServerSignature;
 
 import versioning.CheckRevisionResults;
 import versioning.Versioning;
 
 import constants.ErrorLevelConstants;
 
 /*
  * Created on Sep 24, 2009 by joe
  */
 
 /**
  * Handles all things SID_AUTH_CHECK 
  */
 public class SidAuthCheck
 {
 
     public static BnetPacket getOutgoing(PublicExposedFunctions pubFuncs,
             BnetPacket SidAuthInfo) throws LoginException, PluginException
     {       
         pubFuncs.putLocalVariable("loginType",  SidAuthInfo.removeDWord());
         pubFuncs.putLocalVariable("serverToken", SidAuthInfo.removeDWord());
         pubFuncs.putLocalVariable("udpValue",   SidAuthInfo.removeDWord());
         pubFuncs.putLocalVariable("mpqTime",    SidAuthInfo.removeLong());
         pubFuncs.putLocalVariable("mpqName",    SidAuthInfo.removeNTString());
         pubFuncs.putLocalVariable("verFormula", SidAuthInfo.removeNtByteArray());
         
         // test signature, will throw exception if invalid
        ServerSignature.checkSignature(pubFuncs, SidAuthInfo);
         
         // Generate the client token, which is a random value
         pubFuncs.putLocalVariable("clientToken", (
             (int) (Math.random() * 0x7FFFFFFF) ^
             (int) (Math.random() * 0x7FFFFFFF) ^
             (int) (Math.random() * 0x7FFFFFFF)));
 
         // Display the variables if we're debugging, and because it looks cool
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] Logon type:   0x" +
                 Integer.toHexString((Integer)pubFuncs.getLocalVariable("loginType")));
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] Server token: 0x" +
                 Integer.toHexString((Integer)pubFuncs.getLocalVariable("serverToken")));
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] Client token: 0x" +
                 Integer.toHexString((Integer)pubFuncs.getLocalVariable("clientToken")));
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] MPQ filetime: 0x" +
                 Long.toHexString((Long)pubFuncs.getLocalVariable("mpqTime")));
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] MPQ filename: " +
                 (String)pubFuncs.getLocalVariable("mpqName"));
         
         
         // END PARSING SID_AUTH_INFO
         // -------------------------------------------------
         // CONSTRUCTING SID_AUTH_CHECK
         
         String game = pubFuncs.getLocalSetting("Battle.net Login Plugin", "game");
         
         // Run check revision
         CheckRevisionResults crev = Versioning.CheckRevision(game, pubFuncs,
                 (String)pubFuncs.getLocalVariable("mpqName"),
                 (byte[])pubFuncs.getLocalVariable("verFormula"),
                 (Long)pubFuncs.getLocalVariable("mpqTime"));
         
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] Version Hash: 0x" +
                 Integer.toHexString(crev.verhash));
         pubFuncs.systemMessage(ErrorLevelConstants.DEBUG, "[BNET] Checksum:     0x" +
                 Integer.toHexString(crev.checksum));
         
         // Build the SID_AUTH_CHECK packet
         BnetPacket authCheck = new BnetPacket(PacketConstants.SID_AUTH_CHECK);
         // (DWORD) Client Token
         authCheck.addDWord((Integer)pubFuncs.getLocalVariable("clientToken"));
         // (DWORD) EXE Version
         authCheck.addDWord(crev.verhash);
         // (DWORD) EXE Hash
         authCheck.addDWord(crev.checksum);
         // (VOID) CD-Key Block
         authCheck.add(Versioning.CDKeyBlock(game,
             (Integer)pubFuncs.getLocalVariable("clientToken"),
             (Integer)pubFuncs.getLocalVariable("serverToken"),
             pubFuncs.getLocalSetting("Battle.net Login Plugin", "cdkey"),
             pubFuncs.getLocalSetting("Battle.net Login Plugin", "cdkey2")));
         // (STRING) EXE Statstring */
         authCheck.addNtByteArray(crev.statstring);
         // (STRING) CD-Key Owner  */
         authCheck.addNTString(pubFuncs.getLocalSetting("Battle.net Login Plugin", "username"));
 
         return authCheck;
     }
     
     public static void checkIncoming(PublicExposedFunctions pubFuncs,
             BnetPacket SidAuthCheck) throws LoginException
     {
         //  (DWORD) Result
         int result = SidAuthCheck.removeDWord();
         // (STRING) Additional Information
         String info = SidAuthCheck.removeNTString();
 
         switch(result)
         {
             case 0x000: // 0x000: Passed challenge
                 break;
             case 0x100: // 0x100: Old game version
                 throw new LoginException("[BNET] Old game version (new MPQ: " +
                     info + ")");
             case 0x101: // 0x101: Invalid version
                 throw new LoginException("[BNET] Invalid game version");
             case 0x102: // 0x102: Game version must be downgraded 
                 throw new LoginException("[BNET] Game must be downgraded");
             case 0x200: // 0x200: Invalid CD key 
                 throw new LoginException("[BNET] Invalid CD Key");
             case 0x201: // 0x201: CD key in use
                 throw new LoginException("[BNET] CD key in use by " + info);
             case 0x202: // 0x202: Banned key
                 throw new LoginException("[BNET] Your CD Key is banned");
             case 0x203: // 0x203: Wrong product
                 throw new LoginException("[BNET] Wrong product value for your CD Key");
             case 0x210: // 0x200 + 0x10: Invalid expansion CD-Key
                 throw new LoginException("[BNET] Invalid expansion CD Key");
             case 0x211: // 0x201 + 0x10: Expansion CD key in use
                 throw new LoginException("[BNET] Expansion CD key in use by " + info);
             case 0x212: // 0x202 + 0x10: Banned expansion key
                 throw new LoginException("[BNET] Your expansion CD Key is banned");
             case 0x213: // 0x203 + 0x10: Wrong product (expansion)
                 throw new LoginException("[BNET] Wrong product value for your expansion CD Key");
             default:
                 throw new LoginException("[BNET] Unknown status code in SID_AUTH_CHECK: " +
                     "0x" + Integer.toHexString(result));
         }
     }
 }
