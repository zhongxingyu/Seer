 package com.narmnevis.range;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.narmnevis.range.config.RangeConfig;
 
 /**
  * @author nobeh
  * @since 1.1
  * 
  */
 public class ConcurrentRangeContext extends SimpleRangeContext {
 
 	private final ExecutorService executor;
 
 	public ConcurrentRangeContext(RangeConfig config) {
 		super(config);
 		executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("range-context-%d").build());
 	}
 
 	@Override
 	protected Collection<Datum> generate(List<String> names, Map<String, Generator> generators) {
 		Integer size = getSize();
		Integer batches = Double.valueOf(Math.sqrt(size) / 4).intValue();
 		if (batches ==  0) {
 			return super.generate(names, generators);
 		}
 		
 		Integer batchSize = size / batches;
 
 		logger.info("Starting generation in batch mode with {} batches and each batch with size {}", batches, batchSize);
 		CompletionService<Collection<Datum>> cs = new ExecutorCompletionService<Collection<Datum>>(executor);
 		for (int i = 1; i < batches; ++i) {
 			GenerationTask task = new GenerationTask(i, batchSize, names, generators);
 			cs.submit(task);
 		}
 		cs.submit(new GenerationTask(batches, size - (batchSize * (batches - 1)), names, generators));
 
 		Collection<Datum> datums = new ArrayList<>();
 		for (int count = 0; count < batches; ++count) {
 			try {
 				Future<Collection<Datum>> result = cs.take();
 				Collection<Datum> resultDatums = result.get();
 				datums.addAll(resultDatums);
 			} catch (ExecutionException e) {
 				logger.error("Failed to get generated datums from batch {}", (count + 1), e);
 			} catch (InterruptedException e) {
 				logger.error("Failed to get generated datums from batch {}", (count + 1), e);
 			}
 		}
 		executor.shutdownNow();
 		return datums;
 	}
 
 	private class GenerationTask implements Callable<Collection<Datum>> {
 
 		private final Integer id;
 		private final Integer size;
 		private final List<String> names;
 		private final Map<String, Generator> generators;
 
 		public GenerationTask(Integer id, Integer size, List<String> names, Map<String, Generator> generators) {
 			this.id = id;
 			this.size = size;
 			this.names = names;
 			this.generators = generators;
 		}
 
 		@Override
 		public Collection<Datum> call() throws Exception {
 			final Collection<Datum> datums = new ArrayList<>();
 			for (int i = 1; i <= size; ++i) {
 				Datum datum = generateDatum(names, generators);
 				datums.add(datum);
 			}
 			logger.info("Batch generator {} completed with batch size: {}", id, size);
 			return datums;
 		}
 
 	}
 
 }
