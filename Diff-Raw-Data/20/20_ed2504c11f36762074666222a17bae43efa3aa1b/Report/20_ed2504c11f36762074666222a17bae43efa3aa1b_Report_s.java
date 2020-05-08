 package com.bitresolution.ledger.core.ledger;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Report {
     private final DateTime periodOfReport;
     private final DateTime filingDate;
     private final List<Entry> entries;
 
     public Report(DateTime periodOfReport, DateTime filingDate, List<Entry> entries) {
         this.periodOfReport = periodOfReport;
         this.filingDate = filingDate;
         this.entries = ImmutableList.copyOf(entries);
     }
 
     public DateTime getPeriodOfReport() {
         return periodOfReport;
     }
 
     public DateTime getFilingDate() {
         return filingDate;
     }
 
     public List<Entry> getEntries() {
         return entries;
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(periodOfReport, filingDate, entries);
     }
 
     @Override
     public boolean equals(Object obj) {
         if(this == obj) {
             return true;
         }
         if(obj == null || getClass() != obj.getClass()) {
             return false;
         }
         final Report other = (Report) obj;
         return Objects.equal(this.periodOfReport, other.periodOfReport)
                 && Objects.equal(this.filingDate, other.filingDate)
                 && Objects.equal(this.entries, other.entries);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                 .add("periodOfReport", periodOfReport)
                 .add("filingDate", filingDate)
                 .add("entries", entries)
                 .toString();
     }
 
     public static class Builder {
         private DateTime periodOfReport;
         private DateTime filingDate;
        private List<Entry> entries;
 
         public Builder() {
             this.entries = new ArrayList<Entry>();
         }
 
         public Builder setPeriodOfReport(DateTime periodOfReport) {
             this.periodOfReport = periodOfReport;
             return this;
         }
 
         public Builder setFilingDate(DateTime filingDate) {
             this.filingDate = filingDate;
             return this;
         }
 
         public Builder addEntry(Entry entry) {
             entries.add(entry);
             return this;
         }
 
         public Report build() {
             return new Report(periodOfReport, filingDate, entries);
         }
     }
 }
