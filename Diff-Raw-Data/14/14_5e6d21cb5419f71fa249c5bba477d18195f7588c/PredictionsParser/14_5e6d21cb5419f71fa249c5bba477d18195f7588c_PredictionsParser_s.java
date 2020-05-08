 package info.yasskin.droidmuni;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 /**
  * Parses an XML feed like
  * http://www.nextmuni.com/s/COM.NextBus.Servlets.XMLFeed
  * ?command=predictionsForMultiStops&a=sf-muni&stops=38||6648&stops=6||6648
  */
 public class PredictionsParser extends Parser {
   public List<Db.Prediction> getPredictions() {
     return predictions;
   }
 
   public HashMap<String, String> getDirectionTag2Title() {
     return direction_tag2title;
   }
 
   private final List<Db.Prediction> predictions =
       new ArrayList<Db.Prediction>();
   private final HashMap<String, String> direction_tag2title =
       new HashMap<String, String>();
 
   @Override
   protected void parseBody() throws XmlPullParserException, IOException {
     parser.require(XmlPullParser.START_TAG, null, "body");
     this.copyright = getAttr("copyright");
 
     while (parser.nextTag() == XmlPullParser.START_TAG) {
       if (parser.getName().equals("keyForNextTime")) {
         parser.nextText();
         continue;
       }
      parsePredictions();
     }
     parser.require(XmlPullParser.END_TAG, null, "body");
     this.result_state = ResultState.SUCCESS;
   }
 
   /**
    * Parse a <predictions> tag, which corresponds to one route stopping at the
    * requested stop, and may contain several <direction> blocks.
    */
  private void parsePredictions() throws XmlPullParserException, IOException {
     parser.require(XmlPullParser.START_TAG, null, "predictions");
     String no_predictions_title =
         parser.getAttributeValue(null, "dirTitleBecauseNoPredictions");
     if (no_predictions_title == null) {
       final String route_tag = parser.getAttributeValue(null, "routeTag");
       while (parser.nextTag() == XmlPullParser.START_TAG) {
         if (parseDirection(route_tag)) {
         } else {
           skipToEndOfTag();
         }
       }
     } else {
       skipToEndOfTag();
     }
     parser.require(XmlPullParser.END_TAG, null, "predictions");
   }
 
   /**
    * Parse a <direction> tag, which contains several <prediction> tags.
    * 
    * @return true if we successfully parsed a <direction> tag.
    */
   private boolean parseDirection(final String route_tag)
       throws XmlPullParserException, IOException {
     if (XmlPullParser.START_TAG != parser.getEventType()
         || !"direction".equals(parser.getName())) {
       return false;
     }
     parser.require(XmlPullParser.START_TAG, null, "direction");
     String direction_title = getAttr("title");
     while (parser.nextTag() != XmlPullParser.END_TAG) {
       parser.require(XmlPullParser.START_TAG, null, "prediction");
       long epochTime = Long.parseLong(getAttr("epochTime"), 10);
       boolean isDeparture = Boolean.parseBoolean(getAttr("isDeparture"));
       String dirTag = getAttr("dirTag");
       String block = getAttr("block");
       predictions.add(new Db.Prediction(route_tag, epochTime, isDeparture,
           dirTag, block));
       // Record the names of the direction tags too.
       direction_tag2title.put(dirTag, direction_title);
       parser.nextText();
     }
     parser.require(XmlPullParser.END_TAG, null, "direction");
     return true;
   }
 }
