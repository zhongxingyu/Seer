 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Abstract combination parser that accepts the results of the first parser
  * that accepts each message 
  */
 
 public class GroupBestParser extends MsgParser {
   
   private MsgParser[] parsers;
   
   private String dispFilter;
   
   public GroupBestParser(MsgParser ... parsers) {
     super(parsers[0].getDefaultCity(), parsers[0].getDefaultState());
     
     this.parsers = parsers;
     
     // Build a display filter by concatenating all of the filters of
     // our constituent parsers.  As the name implies, this will never be
     // used to do any real filtering, but it will give us something to 
     // display in the settings menu
     StringBuilder sb = new StringBuilder();
     for (MsgParser parser : parsers) {
       String filter = parser.getFilter();
       if (filter.length() > 0) {
         if (sb.length() > 0) sb.append(',');
         sb.append(filter);
       }
     }
     dispFilter = sb.toString();
   }
   
   @Override
   public String getFilter() {
     return dispFilter;
   }
 
   @Override
   protected Data parseMsg(Message msg, int parserFlags) {
     
    // If caller has requested general alerts and postively identified this as
    // coming form dispatch, we will have to handle the general alerts ourselves
    // Passing a positive ID flag to sub-parsers might cause them to return 
    // an inappropriate general alert status for a message that could be handled
    // by one of the other subparsers.
    int mask = (PARSE_FLG_GEN_ALERT | PARSE_FLG_POSITIVE_ID);
    boolean genAlert = (parserFlags & mask) == mask;
    
    parserFlags &= ~PARSE_FLG_POSITIVE_ID;
     
     int bestScore = -1;
     Data bestData = null;
     
     for (MsgParser parser : parsers) {
       Data tmp = parser.parseMsg(msg, parserFlags);
       if (tmp != null) {
         int newScore = tmp.score();
         if (newScore > bestScore) {
           bestData = tmp;
           bestScore = newScore;
         }
       }
     }
     
     if (bestData != null) return bestData;
     
    // If this isn't a valid CAD page, see if we should treat it as a general alert
    // If not then return failure
     if (genAlert) {
       return ManageParsers.getInstance().getAlertParser().parseMsg(msg, parserFlags);
     }
     
     return null;
   }
 
   // We have to override this to satisfy abstract requirements, but it will
   // never be called and doesn't have to do anything
   @Override
   protected boolean parseMsg(String strMessage, Data data) {
     return false;
   }
 }
