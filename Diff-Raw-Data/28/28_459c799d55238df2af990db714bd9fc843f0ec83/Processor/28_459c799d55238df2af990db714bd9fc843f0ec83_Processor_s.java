 package pl.kaczanowski.model;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.annotation.Nonnull;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pl.kaczanowski.algorithm.HeightAlgorithm;
 import pl.kaczanowski.model.ModulesGraph.Task;
 
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class Processor {
 
 	private static class TaskRank implements Comparable<TaskRank> {
 
 		private final Task task;
 
 		private final int rank;
 
 		public TaskRank(final Task task, final int rank) {
 			this.task = task;
 			this.rank = rank;
 		}
 
 		@Override
 		public int compareTo(final TaskRank o) {
 			return ComparisonChain.start().compare(o.rank, rank).compare(task.getId(), o.task.getId()).result();
 		}
 
 	}
 
 	private static final Logger LOG = LoggerFactory.getLogger(Processor.class);
 
 	private final int id;
 
 	private int reservedTillTime;
 
 	private int actualTime;
 
 	private Task activeTask;
 
 	private final ImmutableSet<Task> tasksToExecute;
 
 	Processor(final int id, final Collection<Task> tasksToExecute) {
 		this.id = id;
 		reservedTillTime = 0;
 		actualTime = 0;
 		this.tasksToExecute =
 				tasksToExecute == null ? ImmutableSet.<Task> of() : ImmutableSet.<Task> copyOf(tasksToExecute);
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Processor other = (Processor) obj;
 		if (id != other.id) {
 			return false;
 		}
 		return true;
 	}
 
 	public void executeNext(@Nonnull final Task nextTask, @Nonnull final ProcessorsGraph processorsGraph,
			final Map<Integer, Set<Task>> tasksParial) {
 		checkState(isFree(), "Now executed is other task: " + activeTask);
 
 		checkState(tasksToExecute.contains(nextTask), "Cannot assign task " + nextTask + " on processor " + this);
 
 		int time = nextTask.getCost();
 
 		if (!Iterables.isEmpty(nextTask.getParentTasks())) {
 			for (Task parentTask : nextTask.getParentTasks()) {
				time += processorsGraph.getChangeCost(getFromProcessorId(parentTask, tasksParial), id);
 			}
 		}
 		this.reservedTillTime = this.actualTime + time;
 
 		this.activeTask = nextTask;
 
 	}
 
 	public int getActualTime() {
 		return actualTime;
 	}
 
 	private Task getBestTask(final List<Task> candidates, final HeightAlgorithm heightAlgorithm) {
 
 		if (Iterables.isEmpty(candidates)) {
 			return null;
 		}
 		if (candidates.size() == 1) {
 			return Iterables.getOnlyElement(candidates);
 		}
 		TreeSet<TaskRank> taskRanks = Sets.newTreeSet();
 		for (Task task : candidates) {
 			taskRanks.add(new TaskRank(task, heightAlgorithm.getCost(task.getId())));
 		}
 
 		return taskRanks.first().task;
 
 	}
 
 	private Integer getFromProcessorId(final Task parentTask, final Map<Integer, Set<Task>> tasksParial) {
 		for (Entry<Integer, Set<Task>> entry : tasksParial.entrySet()) {
 			if (entry.getValue().contains(parentTask)) {
 				return entry.getKey();
 			}
 
 		}
 		return null;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public Task getNextTask(final Collection<Task> allEndedTasks, final HeightAlgorithm heightAlgorithm) {
 
 		List<Task> candidates = Lists.newArrayList();
 		for (Task task : tasksToExecute) {
 			if (!task.isEnded() && allEndedTasks.containsAll(task.getParentTasks())) {
 				candidates.add(task);
 			}
 		}
 
 		return getBestTask(candidates, heightAlgorithm);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
 		return result;
 	}
 
 	public boolean isFree() {
 		return reservedTillTime <= actualTime;
 	}
 
 	public void tick() {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("tick on processor " + this);
 		}
 		this.actualTime++;
 		if (isFree() && this.activeTask != null) {
 			this.activeTask.end();
 			this.activeTask = null;
 		}
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Processor [id=").append(id).append(", reservedTillTime=").append(reservedTillTime)
				.append(", actualTime=").append(actualTime).append(", activeTask=").append(activeTask)
				.append(", tasksToExecute=").append(tasksToExecute).append("]");
 		return builder.toString();
 	}
 
 }
