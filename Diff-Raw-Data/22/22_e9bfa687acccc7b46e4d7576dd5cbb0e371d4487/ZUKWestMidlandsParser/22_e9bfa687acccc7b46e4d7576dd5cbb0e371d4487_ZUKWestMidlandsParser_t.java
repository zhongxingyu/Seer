 package net.anei.cadpage.parsers.ZUK;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 West Midlands, UK
Contact: richard smith <thebattyilike@hotmail.com>
Sender: +447624805974
 
 E:1699 (16:45) Unconscious DY8 4JA The Brambles Residential Home 69a Vicarage Road Amblecote Stourbridge (390070,285129)
 E:989 (12:13) Abdo Pain DY8 1HB Highbury House Nursing Home The Old Rectory Parkfield Road Stourbridge (390537,284075)
 E:1131 (13:07) Allergy DY8 4AT 15 Jardine Close Amblecote Stourbridge (390184,284942)
 E:1112 (12:20) UNK DIAGNOSIS DY9 9AE 37 Grove Road Wollescote Stourbridge (392960,283446)
 E:1481 (14:27) Burn DY8 3JS 14 Orwell Close Norton Stourbridge (388565,283699)
 E:1302 (14:00) UNK DIAGNOSIS DY8 3ER 10 Manor Lane Norton Stourbridge (388759,283434)
 E:1444 (14:53) Chest Pain DY6 9PG 19 Barnett Green Kingswinford (388879,287721)
 E:1294 (13:52) Convulsions DY8 4JU 11 Corbett Crescent Amblecote Stourbridge (390356,285008)
 E:1517 (15:24) Chest Pain DY6 8JG 15 Wellington Close Kingswinford (389335,287797)
 E:1642 (16:19) Falls DY9 0QE 17 Brook Crescent Hagley Stourbridge (390590,280007)
 E:1075 (11:55) UNK DIAGNOSIS DY8 5UN 56 Brooklands Wordsley Stourbridge (389821,286440)
 
 */
 public class ZUKWestMidlandsParser extends SmartAddressParser {
 
   private static final Pattern MASTER =
    Pattern.compile("(E:\\d+) +\\((\\d\\d:\\d\\d)\\) *(.*?) ([A-Z]{1,2}\\d{1,2}[A-Z]? \\d[A-Z]{2}) +(.*?) +\\((\\d+,\\d+)\\)");
 
   public ZUKWestMidlandsParser() {
     super(CITY_LIST, "", "West Midlands", CountryCode.UK);
   }
  
  @Override
  public String getFilter() {
    return "447624805974";
  }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
 
    data.strCallId = match.group(1);
     data.strTime = match.group(2);
     data.strCall = match.group(3);
     String sCode = match.group(4);
     String sAddr = match.group(5);
     data.strMap = match.group(6);
     parseAddress(StartType.START_PLACE, FLAG_ANCHOR_END, sAddr, data);
     if (data.strAddress.length() == 0) {
       data.strAddress = data.strPlace;
       data.strPlace = "";
     }
     data.strAddress = sCode + " " + data.strAddress;
     return true;
   }
 
   private static final String[] CITY_LIST = new String[]{
     "BIRMINGHAM",
     "BURTON-UPON-TRENT",
     "CANNOCK",
     "COVENTRY",
     "DUDLEY",
     "HALESOWEN",
     "HEREFORD",
     "KIDDERMINSTER",
     "KINGSWINFORD",
     "LEAMINGTON SPA",
     "LEEK",
     "LICHFIELD",
     "NEWCASTLE-UNDER-LYME",
     "NUNEATON",
     "OTHER NOTABLE",
     "REDDITCH",
     "RUGBY",
     "SHREWSBURY",
     "SOLIHULL",
     "STAFFORD",
     "STOKE-ON-TRENT",
     "STOURBRIDGE",
           "AMBLECOTE STOURBRIDGE",
           "HAGLEY STOURBRIDGE",
           "NORTON STOURBRIDGE",
           "WOLLESCOTE STOURBRIDGE",
           "WORDSLEY STOURBRIDGE",
     "STRATFORD-UPON-AVON",
     "SUTTON COLDFIELD",
     "TAMWORTH",
     "TELFORD",
     "WALSALL",
     "WARWICK",
     "WEST BROMWICH",
     "WOLVERHAMPTON",
     "WORCESTER"
   };
 }
