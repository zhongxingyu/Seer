 package org.atlasapi.feeds.xmltv.upload;
 
 import static com.metabroadcast.common.media.MimeType.TEXT_PLAIN;
 import static org.atlasapi.feeds.upload.FileUpload.fileUpload;
 
 import java.io.ByteArrayOutputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Executors;
 
 import org.atlasapi.feeds.upload.FileUpload;
 import org.atlasapi.feeds.upload.FileUploadResult;
 import org.atlasapi.feeds.upload.persistence.FileUploadResultStore;
 import org.atlasapi.feeds.xmltv.XmlTvChannelLookup.XmlTvChannel;
 import org.atlasapi.feeds.xmltv.XmlTvChannelsCompiler;
 import org.atlasapi.feeds.xmltv.XmlTvFeedCompiler;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
import com.google.common.collect.Ranges;
 import com.google.common.util.concurrent.AsyncFunction;
 import com.google.common.util.concurrent.ForwardingListenableFuture;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class XmlTvUploadTask extends ScheduledTask {
 
     private final AsyncFileUploadService uploadService;
     private final Map<Integer,XmlTvChannel> channelLookup;
     private final XmlTvFeedCompiler feedCompiler;
     private final FileUploadResultStore resultStore;
     private final XmlTvChannelsCompiler channelCompiler;
     private final AdapterLog log;
 
     public XmlTvUploadTask(AsyncFileUploadService uploadService, FileUploadResultStore resultStore, XmlTvFeedCompiler feedCompiler, Map<Integer,XmlTvChannel> channelLookup, AdapterLog log) {
         this.uploadService = uploadService;
         this.resultStore = resultStore;
         this.feedCompiler = feedCompiler;
         this.channelLookup = channelLookup;
         this.log = log;
         this.channelCompiler = new XmlTvChannelsCompiler(channelLookup);
     }
     
     @Override
     protected void runTask() {
         LocalDate startDay = new LocalDate(DateTimeZones.LONDON);
         
         ListeningExecutorService compilerService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("xmltv-compile-%s").build()));
 
         List<XmlTvUploadResult> uploadResults = ImmutableList.<XmlTvUploadResult> builder()
                 .add(uploadChannelsDat("channels.dat", compilerService))
                 .addAll(uploadChannels(startDay, compilerService))
                 .build();
         
         for (XmlTvUploadResult uploadResult : uploadResults) {
             try {
                 FileUploadResult result = uploadResult.get();
                 resultStore.store(result.filename(), result);
             } catch (Exception e) {
                 resultStore.store(uploadResult.filename(), failedUpload(uploadResult.filename(), e));
                 log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception uploading XMLTV feeds"));
                 throw Throwables.propagate(e);
             }
         }
         
         compilerService.shutdown();
     }
 
     private Iterable<XmlTvUploadResult> uploadChannels(final LocalDate startDay, final ListeningExecutorService compilerService) {
         return Iterables.transform(channelLookup.entrySet(), new Function<Entry<Integer, XmlTvChannel>, XmlTvUploadResult>() {
             @Override
             public XmlTvUploadResult apply(Entry<Integer, XmlTvChannel> channel) {
                 return upload(String.format("%s.dat", channel.getKey()), startDay, channel, compilerService);
             }
         });
     }
 
     private XmlTvUploadResult uploadChannelsDat(final String filename, ListeningExecutorService compilerService) {
         final ListenableFuture<FileUpload> upload = compilerService.submit(new Callable<FileUpload>() {
             @Override
             public FileUpload call() throws Exception {
                 ByteArrayOutputStream writeTo = new ByteArrayOutputStream();
                 channelCompiler.compileChannelsFeed(writeTo);
                 return fileUpload(filename, writeTo.toByteArray()).withContentType(TEXT_PLAIN).build();
             }
         });
         return chainUpload(filename, upload);
     }
 
     private XmlTvUploadResult chainUpload(final String filename, final ListenableFuture<FileUpload> upload) {
         return new XmlTvUploadResult(Futures.transform(upload, new AsyncFunction<FileUpload, FileUploadResult>() {
 
             @Override
             public ListenableFuture<FileUploadResult> apply(FileUpload input) {
                 return uploadService.upload(input);
             }
 
         }),filename);
     }
 
     private XmlTvUploadResult upload(final String filename, final LocalDate startDay, final Entry<Integer, XmlTvChannel> channel, ListeningExecutorService compilerService) {
         final ListenableFuture<FileUpload> upload = compilerService.submit(new Callable<FileUpload>() {
             @Override
             public FileUpload call() throws Exception {
                 ByteArrayOutputStream writeTo = new ByteArrayOutputStream();
                feedCompiler.compileChannelFeed(Ranges.closed(startDay, startDay.plusWeeks(2)), channel.getValue().channel(), writeTo);
                 return fileUpload(filename, writeTo.toByteArray()).withContentType(TEXT_PLAIN).build();
             }
         });
         return chainUpload(filename, upload);
     }
 
     private FileUploadResult failedUpload(String filename, Exception e) {
         return FileUploadResult.failedUpload(uploadService.serviceName(), filename).withCause(e).withMessage(e.getMessage());
     }
 
     private class XmlTvUploadResult extends ForwardingListenableFuture<FileUploadResult> {
 
         private final ListenableFuture<FileUploadResult> delegate;
         private final String filename;
 
         public XmlTvUploadResult(ListenableFuture<FileUploadResult> delegate, String filename) {
             this.delegate = delegate;
             this.filename = filename;
         }
         
         @Override
         protected ListenableFuture<FileUploadResult> delegate() {
             return delegate;
         }
 
         public String filename() {
             return filename;
         }
         
     }
     
 }
