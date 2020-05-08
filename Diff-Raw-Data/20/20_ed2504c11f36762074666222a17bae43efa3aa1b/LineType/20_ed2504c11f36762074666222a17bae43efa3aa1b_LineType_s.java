 package com.bitresolution.ledger.core.files;
 
 import org.apache.commons.lang3.StringUtils;
 
 enum LineType {
     PERIOD_OF_REPORT("CONFORMED PERIOD OF REPORT:"),
     REPORT_FILING_DATE("FILED AS OF DATE:"),
     ENTRY(""),
     SKIPPABLE("");
 
     private final String prefix;
 
     LineType(String prefix) {
         this.prefix = prefix;
     }
 
    public String getPrefix() {
         return prefix;
     }
 
    public int getPrefixLength() {
         return prefix.length();
     }
 
     public static LineType of(String line) {
         if(StringUtils.isBlank(line)
                 || line.startsWith(" ")
                 || line.startsWith("NAME OF ISSUER") //column header
                 || line.startsWith("<S>")
                 || line.startsWith("</TABLE>")
                 || line.startsWith("</TEXT>")
                 || line.startsWith("</DOCUMENT>")
                 || line.startsWith("</SEC-DOCUMENT>")) {
             return SKIPPABLE;
         }
         else if(line.startsWith(PERIOD_OF_REPORT.getPrefix())) {
             return PERIOD_OF_REPORT;
         }
         else if(line.startsWith(REPORT_FILING_DATE.getPrefix())) {
             return REPORT_FILING_DATE;
         }
         else {
             return ENTRY;
         }
     }
 }
