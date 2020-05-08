 package de.lere.vaad.animation.splaytree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.lere.vaad.animation.binarysearchtree.BinaryTreeSetup;
 import de.lere.vaad.treebuilder.events.TreeDeleteEvent;
 import de.lere.vaad.treebuilder.events.TreeEvent;
 import de.lere.vaad.treebuilder.events.TreeEventListener;
 import de.lere.vaad.treebuilder.events.TreeInsertEvent;
 import de.lere.vaad.treebuilder.events.TreeLeftRotateEvent;
 import de.lere.vaad.treebuilder.events.TreeModelChangeEventListenerForSplaytreeAnimations;
 import de.lere.vaad.treebuilder.events.TreeRightRotateEvent;
 import de.lere.vaad.treebuilder.events.TreeSearchEvent;
 
 public class SplayTreeModelChangeListener<T extends Comparable<T>> implements
 		TreeEventListener<T> {
 
 	public enum SUB_RECORDING_STATUS {
 		RECORDING_NOTHING, RECORDING_ZIG, RECORDING_ZIG_ZIG, RECORDING_ZIG_ZAG
 	}
 
 	private TreeModelChangeEventListenerForSplaytreeAnimations<T> animator;
 
 	private List<SplayTreeStepListener<T>> splayOperationListeners;
 
 	private List<SplayOperation<T>> operations;
 
 	private ArrayList<SplayStep<T>> steps;
 
 	private ArrayList<TreeEvent<T>> stepEvents;
 
 	private boolean recordingOperation;
 
 	private SUB_RECORDING_STATUS recordingStatus;
 
 	public SplayTreeModelChangeListener(BinaryTreeSetup<T> setup) {
 		animator = new TreeModelChangeEventListenerForSplaytreeAnimations<T>(
 				setup);
 		this.splayOperationListeners = new ArrayList<SplayTreeStepListener<T>>();
 		this.operations = new ArrayList<SplayOperation<T>>();
 		this.steps = new ArrayList<SplayStep<T>>();
 		this.stepEvents = new ArrayList<TreeEvent<T>>();
 		this.recordingStatus = SUB_RECORDING_STATUS.RECORDING_NOTHING;	
 	}
 
 	public void add(SplayTreeStepListener<T> listener) {
 		this.splayOperationListeners.add(listener);
 	}
 
 	public boolean remove(SplayTreeStepListener<T> listener) {
 		return this.splayOperationListeners.remove(listener);
 	}
 
 	@Override
 	public void update(TreeEvent<T> event) {
 		if (event instanceof SplayTreeEvent) {
 			if (event instanceof SplayStartedEvent) {
 				doHandleSplayStart();
 			} else if (event instanceof SplayEndedEvent) {
 				doHandleSplayEnded();
 			} else if (event instanceof SplayTreeStartEvent) {
 				handleSplayStepStarted((SplayTreeStartEvent<T>) event);
 			} else if (event instanceof SplayTreeEndEvent) {
 				handleSplayStepEnded();
 			}
 
		} else if (event instanceof TreeEvent) {
			// if (event instanceof TreeLeftRotateEvent) {
			// stepEvents.add(event);
			// } else if (event instanceof TreeRightRotateEvent) {
			// stepEvents.add(event);
			// } else {
			// if (event instanceof TreeSearchEvent) {
			// fireNodeFound(event);
			// } else if (event instanceof TreeDeleteEvent) {
			// fireNodeDelete(event);
			// } else if (event instanceof TreeInsertEvent) {
			// fireNodeInsert(event);
			// }
 			stepEvents.add(event);
 		}
 	}
 
 	private void fireNodeInsert(TreeEvent<T> event) {
 		for (SplayTreeStepListener<T> l : splayOperationListeners) {
 			l.nodeInserted((TreeInsertEvent<T>) event);
 		}
 	}
 
 	private void fireNodeDelete(TreeEvent<T> event) {
 		for (SplayTreeStepListener<T> l : splayOperationListeners) {
 			l.nodeDelete((TreeDeleteEvent<T>) event);
 		}
 	}
 
 	private void fireNodeFound(TreeEvent<T> event) {
 		for (SplayTreeStepListener<T> l : splayOperationListeners) {
 			l.nodeFound((TreeSearchEvent<T>) event);
 		}
 	}
 
 	private void handleSplayStepEnded() {
 		int sizeOfSteps = numberOfRotations(stepEvents);
 		if (recordingStatus == SUB_RECORDING_STATUS.RECORDING_NOTHING) {
 			throw new IllegalStateException("Must be in recording state.");
 		} else if (recordingStatus == SUB_RECORDING_STATUS.RECORDING_ZIG) {
 			if (sizeOfSteps == 1) {
 				SplayStep<T> step = new SplayStep<T>();
 				step.addAll(this.stepEvents);
 				steps.add(step);
 			} else {
 				throw new IllegalStateException(
 						"Number of Rotations in a Zig-Step must be 1. It is currently: "
 								+ sizeOfSteps);
 			}
 		} else if (recordingStatus == SUB_RECORDING_STATUS.RECORDING_ZIG_ZIG
 				|| recordingStatus == SUB_RECORDING_STATUS.RECORDING_ZIG_ZAG) {
 			if (sizeOfSteps == 2) {
 				SplayStep<T> step = new SplayStep<T>();
 				step.addAll(stepEvents);
 				steps.add(step);
 			} else {
 				throw new IllegalStateException(
 						"Number of Rotations in a ZigZig-Step or a ZigZag-Step must be 2. It is currently: "
 								+ sizeOfSteps);
 			}
 		}
 		recordingStatus = SUB_RECORDING_STATUS.RECORDING_NOTHING;
 		stepEvents.clear();		
 	}
 
 	private int numberOfRotations(ArrayList<TreeEvent<T>> stepsEvent) {
 		int count = 0;
 		for (TreeEvent<T> event : stepsEvent) {
 			if (event instanceof TreeLeftRotateEvent
 					|| event instanceof TreeRightRotateEvent) {
 				++count;
 			}
 		}
 		return count;
 	}
 
 	private void handleSplayStepStarted(SplayTreeEvent<T> event) {
 		if (recordingStatus != SUB_RECORDING_STATUS.RECORDING_NOTHING) {
 			throw new IllegalStateException(
 					"Must be in \"record nothing\"-state.");
 		}
 
 		if (event instanceof ZigStartedEvent) {
 			recordingStatus = SUB_RECORDING_STATUS.RECORDING_ZIG;
 		} else if (event instanceof ZigZigStartedEvent) {
 			recordingStatus = SUB_RECORDING_STATUS.RECORDING_ZIG_ZIG;
 		} else if (event instanceof ZigZagStartedEvent) {
 			recordingStatus = SUB_RECORDING_STATUS.RECORDING_ZIG_ZAG;
 		} else {
 			throw new IllegalStateException("No corresponding event found...");
 		}
 		this.stepEvents.add(event);
 	}
 
 	private void doHandleSplayStart() {
 		if (this.recordingOperation) {
 			throw new IllegalStateException(
 					"Should not be recording when receiving a SplayStartedEvent.");
 		} else {
 			this.recordingOperation = true;
 			this.steps.clear();
 		}
 	}
 
 	private void doHandleSplayEnded() {
 		if (this.recordingOperation) {
 			this.recordingOperation = false;
 			SplayOperation<T> operation = new SplayOperation<T>();
 			operation.addAll(steps);
 			operations.add(operation);
 		} else {
 			throw new IllegalStateException(
 					"Should be recording when receiving a SplayEndedEvent.");
 		}
 		relayStepsToListeners();
 	}
 
 	private void relayStepsToListeners() {
 		for (SplayOperation<T> operation : this.operations) {
 			List<SplayStep<T>> steps = operation.getOperationSteps();
 			for (SplayStep<T> step : steps) {
 				List<TreeEvent<T>> events = step.getEventsInStep();
 				for (TreeEvent<T> event : events) {
 					if (event instanceof SplayTreeStartEvent) {
 						fireNewStep((SplayTreeStartEvent<T>) event);
 					} else if (event instanceof TreeSearchEvent) {
 						fireNodeFound(event);
 					} else if (event instanceof TreeDeleteEvent) {
 						fireNodeDelete(event);
 					} else if (event instanceof TreeInsertEvent) {
 						fireNodeInsert(event);
 					}
 					animator.animate(event);
 				}
 			}
 		}
 	}
 
 	private void fireNewStep(SplayTreeStartEvent<T> step) {
 		for (SplayTreeStepListener<T> listener : this.splayOperationListeners) {
 			listener.newStep(step);
 		}
 	}
 }
