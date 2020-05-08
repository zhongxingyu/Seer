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
     
    // OK, this gets complicated
    // If all three flags are on (which is what PARSE_FLG_FORCE is, we can't
    // pass them directly to the subparsers.  If we do, they will all return
    // a general alert status, possibly masking a real positive parser hit from
    // one of the other parsers.  In this case, and this case only, we turn off
    // the general alert flag passed to subparsers and handle it ourselves
    boolean genAlert = (parserFlags & MsgParser.PARSE_FLG_FORCE) == MsgParser.PARSE_FLG_FORCE;
    if (genAlert) parserFlags &= ~PARSE_FLG_GEN_ALERT;
     
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
     
    // If non of our subparsers found this to be valid page, and the original
    // flags forced a general alert, handle that now
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
