 package br.ufmg.dcc.vod.spiderpig;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
import java.util.concurrent.CountDownLatch;
 
 import org.apache.log4j.Logger;
 
 import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDClientActor;
 import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverActor;
 import br.ufmg.dcc.vod.spiderpig.master.Master;
 import br.ufmg.dcc.vod.spiderpig.master.ResultActor;
 import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.CrawlFinishedListener;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
 
 public class Crawler {
 
 	private static final Logger LOG = Logger.getLogger(Crawler.class);
 	
 	private final QueueService service;
 	private final ProcessorActor processorActor;
 	private final Master master;
 	private final StopCondition stopCondition;
 	private final int numThreads;
 
 	private final ResultActor resultActor;
 	private final FileSaverActor fileSaverActor;
 	private final FDClientActor fd;
 	
 	Crawler(ProcessorActor processorActor, 
 			QueueService service, Master master, 
 			ResultActor resultActor, FileSaverActor fileSaverActor, 
 			FDClientActor fd, int numThreads) {
 		this.processorActor = processorActor;
 		this.service = service;
 		this.master = master;
 		this.stopCondition = master.getStopCondition();
 		this.numThreads = numThreads;
 		this.resultActor = resultActor;
 		this.fileSaverActor = fileSaverActor;
 		this.fd = fd;
 	}
 	
 	public void addSeed(List<String> seed) {
 		CrawlID.Builder builder = CrawlID.newBuilder();
 		List<CrawlID> seedList = new ArrayList<>();
 		for (String crawlID : seed)
 			seedList.add(builder.setId(crawlID).build());
 		master.addSeed(seedList);
 	}
 
 	public void addSeed(String... seed) {
 		addSeed(Arrays.asList(seed));
 	}
 	
 	public void crawl() {
 		LOG.info("Starting Crawler");
 
 		//Starting Processors
 		resultActor.startProcessors(numThreads);
 		fileSaverActor.startProcessors(numThreads);
 		fd.startProcessors(1);
 		fd.startTimer();
 		processorActor.startProcessors(numThreads);
 		
 		//Waiting until crawl ends
 		LOG.info("Waiting until crawl ends");
 		
		final CountDownLatch latch = new CountDownLatch(1);
 		stopCondition.addCrawlFinishedListener(new CrawlFinishedListener() {
 			@Override
 			public void crawlDone() {
				latch.countDown();
 			}
 		});
 		
 		try {
			latch.await();
			service.waitUntilWorkIsDoneAndStop(1);
 			fd.stopTimer();
 		} catch (InterruptedException e) {
 		}
 		
 		LOG.info("Crawl done!");
 	}
 }
