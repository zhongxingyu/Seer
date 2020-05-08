 /**
  * 
  */
 package pt.utl.ist.fenix.tools.codeGenerator;
 
 import java.io.IOException;
 import java.util.Formatter;
 import java.util.Iterator;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.utl.ist.fenix.tools.util.FileUtils;
 import dml.DomainClass;
 import dml.Slot;
 
 /**
  * @author - Shezad Anavarali (shezad@ist.utl.pt)
  * 
  */
 public class DeprecatedDatesGettersAndSettersGenerator extends DomainObjectGenerator {
 
     public void appendMethodsInTheRootDomainObject() throws IOException {
 
         for (Iterator<DomainClass> iter = getModel().getClasses(); iter.hasNext();) {
             DomainClass domainClass = iter.next();
             StringBuilder resultSourceCode = buildMethods(domainClass);
             if (resultSourceCode.length() > 0) {
 
                 String domainClassSourceCodeFilePath = outputFolder + "/"
                         + domainClass.getFullName().replace('.', '/') + sourceSuffix;
                 String domainClassSourceCode = FileUtils.readFile(domainClassSourceCodeFilePath);
 
                 int lastBrace = domainClassSourceCode.lastIndexOf('}');
                 if (lastBrace > 0) {
                     resultSourceCode.insert(0, domainClassSourceCode.substring(0, lastBrace));
                     resultSourceCode.append("\n\n}\n");
                     FileUtils.writeFile(domainClassSourceCodeFilePath, resultSourceCode.toString(), false);
                 }
             }
         }
     }
 
     private StringBuilder buildMethods(DomainClass domainClass) {
         StringBuilder resultSourceCode = new StringBuilder();
         Formatter methods = new Formatter(resultSourceCode);
 
         for (Iterator<Slot> iter = domainClass.getSlots(); iter.hasNext();) {
             Slot slot = iter.next();
             if (slot.getType().equals("org.joda.time.YearMonthDay")) {
 
                 String originalSlotName = StringUtils.capitalize(slot.getName());
                 if (originalSlotName.endsWith("YearMonthDay")) {
                     String dateSlotName = originalSlotName.replaceFirst("YearMonthDay", "");
 
                     methods.format("\n\t@Deprecated\n\tpublic java.util.Date get%s(){\n", dateSlotName);
                     methods.format("\t\torg.joda.time.YearMonthDay ymd = get%s();\n", originalSlotName);
                     methods
                             .format("\t\treturn (ymd == null) ? null : new java.util.Date(ymd.getYear() - 1900, ymd.getMonthOfYear() - 1, ymd.getDayOfMonth());\n");
                     methods.format("\t}\n");
 
                     methods.format("\n\t@Deprecated\n\tpublic void set%s(java.util.Date date){\n", dateSlotName);
                     methods.format("\t\tif(date == null) set%s(null);\n", originalSlotName);
                     methods.format("\t\telse set%s(org.joda.time.YearMonthDay.fromDateFields(date));\n",
                             originalSlotName);
                     methods.format("\t}\n");
                 }
 
             } else if (slot.getType().equals("org.joda.time.DateTime")) {
 
                 String originalSlotName = StringUtils.capitalize(slot.getName());
                 if (originalSlotName.endsWith("DateTime")) {
 
                     String dateSlotName = originalSlotName.replaceFirst("DateTime", "");
 
                     methods.format("\n\t@Deprecated\n\tpublic java.util.Date get%s(){\n", dateSlotName);
                     methods.format("\t\torg.joda.time.DateTime dt = get%s();\n", originalSlotName);
                     methods
                             .format("\t\treturn (dt == null) ? null : new java.util.Date(dt.getMillis());\n");
                     methods.format("\t}\n");
 
                     methods.format("\n\t@Deprecated\n\tpublic void set%s(java.util.Date date){\n", dateSlotName);
                     methods.format("\t\tif(date == null) set%s(null);\n", originalSlotName);
                     methods.format("\t\telse set%s(new org.joda.time.DateTime(date.getTime()));\n",
                             originalSlotName);
                     methods.format("\t}\n");
                 }
 
             } else if (slot.getType().equals("net.sourceforge.fenixedu.util.HourMinuteSecond")) {
 
                 String originalSlotName = StringUtils.capitalize(slot.getName());
                 if (originalSlotName.endsWith("HourMinuteSecond")) {
 
                     String dateSlotName = originalSlotName.replaceFirst("HourMinuteSecond", "");
 
                     methods.format("\n\t@Deprecated\n\tpublic java.util.Date get%s(){\n", dateSlotName);
                     methods.format(
                             "\t\tnet.sourceforge.fenixedu.util.HourMinuteSecond hms = get%s();\n",
                             originalSlotName);
                     methods
                             .format("\t\treturn (hms == null) ? null : new java.util.Date(0, 0, 1, hms.getHour(), hms.getMinuteOfHour(), hms.getSecondOfMinute());\n");
                     methods.format("\t}\n");
 
                     methods.format("\n\t@Deprecated\n\tpublic void set%s(java.util.Date date){\n", dateSlotName);
                     methods.format("\t\tif(date == null) set%s(null);\n", originalSlotName);
                     methods
                             .format(
                                     "\t\telse set%s(net.sourceforge.fenixedu.util.HourMinuteSecond.fromDateFields(date));\n",
                                     originalSlotName);
                     methods.format("\t}\n");
                 }
             }
         }
 
         return resultSourceCode;
 
     }
 
     public static void main(String[] args) {
        process(args, new DeprecatedDatesGettersAndSettersGenerator());
         System.exit(0);
     }
 
 }
