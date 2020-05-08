 package pl.kaczanowski.algorithm;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.collections.Maps;
 import org.testng.internal.annotations.Sets;
 
 import pl.kaczanowski.model.ModulesGraph;
 import pl.kaczanowski.model.ModulesGraph.Task;
 import pl.kaczanowski.model.Processor;
 import pl.kaczanowski.model.ProcessorsGraph;
 
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 
 /**
  * Algorithm for scheduling.
  * @author kaczanowskip
  */
 public class SchedulingAlgorithm {
 
 	private final Logger log = LoggerFactory.getLogger(SchedulingAlgorithm.class);
 
 	private final HeightAlgorithm heightAlgorithm;
 	private ModulesGraph modulesGraph;
 	private ProcessorsGraph processorsGraph;
 
 	public SchedulingAlgorithm(final HeightAlgorithm heightAlgorithm, final ModulesGraph modulesGraph,
 			final ProcessorsGraph processorsGraph) {
 		this.heightAlgorithm = heightAlgorithm;
 		this.modulesGraph = modulesGraph;
 		this.processorsGraph = processorsGraph;
 	}
 
 	public int getExecutionTime(final Map<Integer, Set<Integer>> processorsPartial) {
 
 		int time = 0;
 
 		List<Task> tasks = modulesGraph.getTasksCopy();
 
 		Map<Integer, Set<Task>> tasksPartial = getTasksPartial(processorsPartial, tasks);
 		List<Processor> processors = processorsGraph.getProcessorCopies(tasksPartial);
 
 		while (Iterables.any(tasks, Task.IS_TASK_NOT_ENDED)) {
 			Collection<Task> ended = Collections2.filter(tasks, Task.IS_TASK_ENDED);
 			for (Processor processor : processors) {
 				if (processor.isFree()) {
 					log.debug("processor is free " + processor);
 					Task nextTask = processor.getNextTask(ended, heightAlgorithm);
 					if (nextTask != null) {
						log.debug("EXEC task " + nextTask.getId() + " on processor " + processor.getId());
						processor.executeNext(nextTask, processorsGraph, tasksPartial);
 					}
 				}
 			}
 			for (Processor processor : processors) {
 				processor.tick();
 			}
 
 			++time;
 			log.debug("tick= " + time);
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
