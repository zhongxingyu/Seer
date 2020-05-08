 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Harris County ESD #1, TX
 Contact: glenn chisholm <glenn1736@gmail.com>
 Sender: varies
 System: Tritech
 
 67B01U / GRASS/WOODS FIRE / 2418 LILY LN / APT: / BLD: / KM: 460T / XSTRTS: OLEANDER ST/WISTERIA WAY / Box #: 1705
 Unknown Problem / 916 Jones Rd / APT: / BLD: / KM: 460S / XSTRTS: MIZELL ST/STEELE RD / Box #: 1704
 29D02p / /Trans Incidents F/O / E Wallisville Rd / Thompson Rd / APT: / BLD: / KM: 460P / XSTRTS: / Box #: 1705
 / 10C01 / Pain (Non-Traumatic) / 9600 N MAIN ST / APT: / BLD: / KM: 461L / XSTRTS: E WALLISVILLE RD/FM 1942 / Box #: 1706
 / Unknown Problem / 916 Jones Rd / APT: / BLD: / KM: 460S / XSTRTS: MIZELL ST/STEELE RD / Box #: 1704
 / 28C09X / Stroke (CVA) / 2726 Waco / APT: / BLD: / KM: 460Y / XSTRTS: DALLAS ST/EAST ST / Box #: 1703
 / 31D03 /Fainting F/O / 409 W Wallisville Rd / APT: / BLD: / KM: 459Q / XSTRTS: N 5TH ST/N 4TH ST / Box #: 1705
 / 60B01 / /ODOR (NAT/LP GASES) / 617 Golden Bend Dr / APT: / BLD: / KM: 459M / XSTRTS: RED HAVEN DR/Dead End / Box #: 1701
 /11-05-15429 / 52C03S / 52O1 ALARMS / 16102 E Ih 10 / APT: / BLD: OLD RIVER TERRANCE M / KM: 498G / XSTRTS:IH 10 EAST BAYOU RAMP/IH 10 SH/ Box #:2002
 
 */
 
 
 public class TXHarrisCountyESD1Parser extends FieldProgramParser {
   
  private static final Pattern DELIM = Pattern.compile(" *(?: /)+ *");
 
   public TXHarrisCountyESD1Parser() {
     super("HARRIS COUNTY", "TX",
           "ID? CODE? CALL ADDR! ADDR2 APT:APT BLD:APT KM:MAP XSTRTS:X Box:BOX");
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.startsWith("/")) body = body.substring(1).trim();
     body = body.replace("Box #:", "Box:");
     return parseFields(DELIM.split(body), data);
   }
   
   // ID Field has to fit specific pattern
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d\\d-\\d\\d-\\d{5}"));
     }
   }
   
   // Code field has to fit specific pattern
   private class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(Pattern.compile("\\d\\d[A-Z]\\d\\d[A-Za-z]?"));
     }
   }
   
   // ADDR2 is a second address field, intersections are reported as two
   // different fields that have to be put back together
   private class MyAddress2Field extends AddressField {
     @Override
     public void parse(String field, Data data) {
       data.strAddress = append(data.strAddress, " & ", field);
     }
   }
   
   // There are two fields, APT and BLD mapped to the apartment field.  But they
   // occur in reverse order, if both are specified, we want to to put bldg first.
   private class MyAptField extends AptField {
     @Override
     public void parse(String field, Data data) {
       data.strApt = append(field, "-", data.strApt);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("CODE")) return new MyCodeField();
     if (name.equals("ADDR2")) return new MyAddress2Field();
     if (name.equals("APT")) return new MyAptField();
     return super.getField(name);
   }
   
 }
