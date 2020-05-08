 package org.atlasapi.feeds.lakeview.upload;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.atlasapi.feeds.lakeview.LakeviewContentFetcher;
 import org.atlasapi.feeds.lakeview.LakeviewFeedCompiler;
 import org.atlasapi.feeds.lakeview.XmlFeedOutputter;
 import org.atlasapi.feeds.upload.FileUpload;
 import org.atlasapi.feeds.upload.FileUploader;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.metabroadcast.common.time.Clock;
 
 public class LakeviewFileUpdater extends ScheduledTask {
 
 	private LakeviewContentFetcher contentFetcher;
 	private LakeviewFeedCompiler feedCompiler;
 	private XmlFeedOutputter feedOutputter;
 	private String filenameProviderID;
 	private Clock clock;
 	private FileUploader uploader;
 	private String schemaVersion;
 	private AdapterLog log;
 	
 	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd.HH-mm-ss");
 
 	public LakeviewFileUpdater(LakeviewContentFetcher contentFetcher, LakeviewFeedCompiler feedCompiler, XmlFeedOutputter feedOutputter, String filenameProviderId, String schemaVersion, FileUploader uploader, Clock clock, AdapterLog log) {
 		this.contentFetcher = contentFetcher;
 		this.feedCompiler = feedCompiler;
 		this.feedOutputter = feedOutputter;
 		this.filenameProviderID = filenameProviderId;
 		this.uploader = uploader;
 		this.schemaVersion = schemaVersion;
 		this.clock = clock;
 		this.log = log;
 	}
 
 	@Override
 	protected void runTask() {
         try {
         	ByteArrayOutputStream bos = new ByteArrayOutputStream();
         	OutputStream gzippedStream = new GZIPOutputStream(bos);
         	String filename = filenameProviderID + "." + clock.now().toString(DATE_FORMATTER) + ".full.Lakeview_v" + schemaVersion +".gz";
 	
 			feedOutputter.outputTo(feedCompiler.compile(contentFetcher.fetchContent(Publisher.C4_PMLSD)), gzippedStream);
 			gzippedStream.close();
 			FileUpload ftpUpload = new FileUpload(filename, bos.toByteArray());
 			uploader.upload(ftpUpload);
 		} catch (Exception e) {
 			log.record(new AdapterLogEntry(Severity.ERROR).withDescription("Problem uploading C4 file to Azure").withCause(e));
 		}
 	}
 	 
 }
