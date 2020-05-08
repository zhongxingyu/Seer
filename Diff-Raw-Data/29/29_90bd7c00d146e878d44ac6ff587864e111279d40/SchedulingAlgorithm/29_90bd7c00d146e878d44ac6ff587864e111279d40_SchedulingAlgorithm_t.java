 package pl.kaczanowski.algorithm;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.annotation.Nonnull;
 
 import org.testng.collections.Maps;
 import org.testng.internal.annotations.Sets;
 
 import pl.kaczanowski.model.ModulesGraph;
 import pl.kaczanowski.model.ModulesGraph.Task;
 import pl.kaczanowski.model.Processor;
 import pl.kaczanowski.model.ProcessorsGraph;
 
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.inject.Inject;
 
 /**
  * Algorithm for scheduling.
  * @author kaczanowskip
  */
 public class SchedulingAlgorithm {
 
 	public static class Factory {
 
 		private final DynamicHeightAlgorithm.Factory heightAlgorithm;
 
 		@Inject
 		public Factory(@Nonnull final DynamicHeightAlgorithm.Factory heightAlgorithm) {
 			this.heightAlgorithm = heightAlgorithm;
 		}
 
 		public SchedulingAlgorithm create(@Nonnull final ModulesGraph modulesGraph,
 				@Nonnull final ProcessorsGraph processorsGraph) {
 			return new SchedulingAlgorithm(heightAlgorithm, modulesGraph, processorsGraph);
 		}
 
 	}
 
 	// private final Logger log = LoggerFactory.getLogger(SchedulingAlgorithm.class);
 	private final DynamicHeightAlgorithm.Factory heightAlgorithmFactory;
 	private final ModulesGraph modulesGraph;
 
 	private final ProcessorsGraph processorsGraph;
 
 	private SchedulingAlgorithm(final DynamicHeightAlgorithm.Factory heightAlgorithmFactory,
 			final ModulesGraph modulesGraph, final ProcessorsGraph processorsGraph) {
 		this.heightAlgorithmFactory = heightAlgorithmFactory;
 		this.modulesGraph = modulesGraph;
 		this.processorsGraph = processorsGraph;
 	}
 
 	public int getExecutionTime(final Map<Integer, Set<Integer>> processorsPartial) {
 
 		int time = 0;
 
 		List<Task> tasks = modulesGraph.getTasksCopy();
 
 		Map<Integer, Set<Task>> tasksPartial = getTasksPartial(processorsPartial, tasks);
 		List<Processor> processors = processorsGraph.getProcessorCopies(tasksPartial);
 
		while (Iterables.any(tasks, Task.isNotEndedFn())) {
			Collection<Task> ended = Collections2.filter(tasks, Task.isEndedFn());
 			for (Processor processor : processors) {
 				if (processor.isFree()) {
 					// log.debug("processor is free " + processor);
 					Task nextTask =
 							processor.getNextTask(ended,
 									heightAlgorithmFactory.create(modulesGraph, processorsGraph, processorsPartial));
 					if (nextTask != null) {
 						// log.debug("EXEC time=" + time + " task=" + nextTask.getId() + " on processor="
 						// + processor.getId());
 						processor.executeNext(nextTask, processorsGraph, tasksPartial, modulesGraph);
 					}
 				}
 			}
 			for (Processor processor : processors) {
 				processor.tick();
 			}
 
 			++time;
 			// log.debug("tick= " + time);
 		}
 		return time;
 
 	}
 
 	private Map<Integer, Set<Task>> getTasksPartial(final Map<Integer, Set<Integer>> processorsPartial,
 			final List<Task> tasks) {
 		Map<Integer, Set<Task>> result = Maps.newHashMap();
 
 		for (Entry<Integer, Set<Integer>> processorEntry : processorsPartial.entrySet()) {
 			Set<Task> tasksOnProcessor = Sets.newHashSet();
 
 			for (Integer taskId : processorEntry.getValue()) {
 				tasksOnProcessor.add(tasks.get(taskId));
 			}
 
 			result.put(processorEntry.getKey(), tasksOnProcessor);
 		}
 
 		return result;
 	}
 
 }
