 package org.lttng.flightbox.dep;
 
import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.lttng.flightbox.model.SymbolTable;
 import org.lttng.flightbox.model.SystemModel;
 import org.lttng.flightbox.model.Task;
 import org.lttng.flightbox.model.Task.TaskState;
 import org.lttng.flightbox.model.state.SoftIRQInfo;
 import org.lttng.flightbox.model.state.StateInfo;
 import org.lttng.flightbox.model.state.SyscallInfo;
 
 public class BlockingItem implements Comparable<BlockingItem> {
 
 	private Task task;
 	private StateInfo wakeUp;
 	private Task wakeUpTask;
 	private SyscallInfo waitingSyscall;
 	private long startTime;
 	private long endTime;
 	
 	public Task getTask() {
 		return task;
 	}
 	public void setTask(Task task) {
 		this.task = task;
 	}
 	public StateInfo getWakeUp() {
 		return wakeUp;
 	}
 	public void setWakeUp(StateInfo wakeUp) {
 		this.wakeUp = wakeUp;
 	}
 	public SyscallInfo getWaitingSyscall() {
 		return waitingSyscall;
 	}
 	public void setWaitingSyscall(SyscallInfo waitingSyscall) {
 		this.waitingSyscall = waitingSyscall;
 	}
 	public long getStartTime() {
 		return startTime;
 	}
 	public void setStartTime(long startTime) {
 		this.startTime = startTime;
 	}
 	public long getEndTime() {
 		return endTime;
 	}
 	public void setEndTime(long endTime) {
 		this.endTime = endTime;
 	}
 
 	public TreeSet<BlockingItem> getChildren(SystemModel model) {
 		TreeSet<BlockingItem> result = new TreeSet<BlockingItem>();
 		if (wakeUp == null)
 			return result;
 		
 		BlockingModel bm = model.getBlockingModel();
 		if (wakeUp.getTaskState() == TaskState.EXIT) {
 			populateSubBlocking(bm, result, wakeUp.getTask());
 		} else if (wakeUp.getTaskState() == TaskState.SOFTIRQ) {
 			SoftIRQInfo softirq = (SoftIRQInfo) wakeUp;
 			// wakeup from a received packet
 			if (softirq.getSoftirqId() == SymbolTable.NET_RX_ACTION) {
 				// get the task associated with this socket
 				//softirq.getField();
 				System.out.println("Wakeup by incoming packet!");
 			}
 		}
 		return result;
 	}
 
 	public void populateSubBlocking(BlockingModel blockingModel, TreeSet<BlockingItem> subBlock, Task subTask) {
		if (startTime >= endTime) {
			System.out.println("prob: startTime=" + startTime + " endTime=" + endTime + " diff=" + (endTime - startTime));
		}
		TreeSet<BlockingItem> items = blockingModel.getBlockingItemsForTask(subTask);
		BlockingItem fromElement = new BlockingItem();
		fromElement.setStartTime(startTime);
		BlockingItem toElement = new BlockingItem();
		toElement.setStartTime(endTime);
		SortedSet<BlockingItem> subSet = items.subSet(fromElement, toElement);
		subBlock.addAll(subSet);
 	}
 	
 	@Override
 	public int compareTo(BlockingItem o) {
 		final int BEFORE = -1;
 		final int EQUAL = 0;
 		final int AFTER = 1;
 		if (o == this) return EQUAL;
 		if (this.startTime < o.startTime) return BEFORE;
 		if (this.startTime > o.startTime) return AFTER;
 		return EQUAL;
 	}
 	public void setWakeUpTask(Task wakeUpTask) {
 		this.wakeUpTask = wakeUpTask;
 	}
 	public Task getWakeUpTask() {
 		return wakeUpTask;
 	}
 }
