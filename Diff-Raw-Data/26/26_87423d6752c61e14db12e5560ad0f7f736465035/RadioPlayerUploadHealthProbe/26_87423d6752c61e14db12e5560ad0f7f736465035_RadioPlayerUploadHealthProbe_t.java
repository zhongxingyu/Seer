 package org.atlasapi.feeds.radioplayer.upload;
 
 import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.FAILURE;
 import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.INFO;
 import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.SUCCESS;
 import static org.atlasapi.feeds.radioplayer.upload.FileType.OD;
 import static org.atlasapi.feeds.radioplayer.upload.FileType.PI;
 import static org.atlasapi.feeds.upload.FileUploadResult.DATE_ORDERING;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.atlasapi.feeds.upload.FileUploadResult;
 import org.atlasapi.feeds.upload.FileUploadResult.FileUploadResultType;
 import org.joda.time.LocalDate;
 
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.health.ProbeResult;
 import com.metabroadcast.common.health.ProbeResult.ProbeResultEntry;
 import com.metabroadcast.common.health.ProbeResult.ProbeResultType;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.DayRange;
 import com.metabroadcast.common.time.DayRangeGenerator;
 
 public class RadioPlayerUploadHealthProbe implements HealthProbe {
 
     protected static final String DATE_TIME = "dd/MM/yy HH:mm:ss";
 
     private final String remoteServiceId;
     protected final RadioPlayerUploadResultStore store;
     private final RadioPlayerService service;
     protected final DayRangeGenerator rangeGenerator;
 
     public RadioPlayerUploadHealthProbe(String remoteServiceId, RadioPlayerUploadResultStore store, RadioPlayerService service, DayRangeGenerator dayRangeGenerator) {
         this.remoteServiceId = remoteServiceId;
         this.store = store;
         this.service = service;
         this.rangeGenerator = dayRangeGenerator;
     }
 
     @Override
     public ProbeResult probe() {
         ProbeResult result = new ProbeResult(service.getName());
         
         DayRange dayRange = rangeGenerator.generate(new LocalDate(DateTimeZones.UTC));
         
         for (LocalDate day : dayRange) {
             result.addEntry(entryFor(day, PI, store.resultsFor(PI, remoteServiceId, service, day)));
             result.addEntry(entryFor(day, OD, store.resultsFor(OD, remoteServiceId, service, day)));
         }
 
         result.addEntry(uploadAllPi());
 
         return result;
     }
 
     private ProbeResultEntry entryFor(LocalDate day, FileType type, Iterable<? extends FileUploadResult> results) {
         String filename = linkedFilename(type, day) + uploadButton(type, day);
         
         if (Iterables.isEmpty(results)) {
             return new ProbeResultEntry(INFO, filename, "No Data");
         }
         
         return new ProbeResultEntry(entryResultType(mostRecentResult(results), day), filename, buildEntryValue(results));
     }
 
     private FileUploadResult mostRecentResult(Iterable<? extends FileUploadResult> results) {
         return DATE_ORDERING.reverse().immutableSortedCopy(results).get(0);
     }
 
     private String linkedFilename(FileType type, LocalDate day) {
         return String.format("<a style=\"text-decoration:none\" href=\"/feeds/ukradioplayer/%1$s_%2$s_%3$s.xml\">%1$s_%2$s_%3$s.xml</a>", day.toString("yyyyMMdd"), service.getRadioplayerId(), type.name());
     }
 
     private ProbeResultType entryResultType(FileUploadResult mostRecent, LocalDate day) {
         if (FileUploadResultType.FAILURE.equals(mostRecent.remoteProcessingResult())) {
             return FAILURE;
         }
         switch (mostRecent.type()) {
         case SUCCESS:
             if (day.isEqual(new LocalDate(DateTimeZones.UTC)) && mostRecent.uploadTime().plusMinutes(20).isBeforeNow()) {
                 return FAILURE;
             }
             if (mostRecent.uploadTime().plusHours(4).isBeforeNow()) {
                 return FAILURE;
             }
             return SUCCESS;
         case FAILURE:
             if (day.isAfter(mostRecent.uploadTime().toLocalDate().plusDays(1)) || RadioPlayerServices.untracked.contains(service)) {
                 return INFO;
             } else {
                 return FAILURE;
             }
         default:
             return INFO;
         }
     }
 
     protected String buildEntryValue(Iterable<? extends FileUploadResult> results) {
         StringBuilder builder = new StringBuilder("<table>");
         for (FileUploadResult result : Iterables.limit(results,2)) {
             appendResult(builder, result);
         }
         return builder.append("</table>").toString();
     }
 
     private void appendResult(StringBuilder builder, FileUploadResult result) {
         builder.append("<tr><td>Last ");
         builder.append(result.type().toNiceString());
         builder.append(": ");
         builder.append(result.uploadTime().toString(DATE_TIME));
         builder.append("</td><td>");
         if (result.message() != null) {
             builder.append(result.message());
         } else {
             if (FileUploadResultType.SUCCESS == result.type()) {
                 builder.append("File uploaded successfully");
             } else if (result.exceptionSummary() != null && result.exceptionSummary().message() != null) {
                 builder.append(result.exceptionSummary().message());
             }
         }
         builder.append("</td><td>");
         FileUploadResultType processSuccess = result.remoteProcessingResult() == null ? FileUploadResultType.UNKNOWN : result.remoteProcessingResult();
         builder.append("Processing Result: " + processSuccess.toNiceString());
         builder.append("</td></tr>");
     }
 
     private String uploadButton(FileType type, LocalDate day) {
         String postTarget = String.format("/feeds/ukradioplayer/upload/%s/%s", type.name(), service.getRadioplayerId());
         if(day != null) {
             postTarget += day.toString("/yyyyMMdd");
         }
         return "<form style=\"text-align:center\" action=\""+postTarget+"\" method=\"post\"><input type=\"submit\" value=\"Update\"/></form>";
     }
 
     private ProbeResultEntry uploadAllPi() {
        return new ProbeResultEntry(INFO, "Update All PI files", uploadButton(PI, null));
     }
 
     @Override
     public String title() {
         return service.getName();
     }
 
     @Override
     public String slug() {
         return String.format("ukrp-%s-%s", remoteServiceId, service.getName());
     }
 
 }
