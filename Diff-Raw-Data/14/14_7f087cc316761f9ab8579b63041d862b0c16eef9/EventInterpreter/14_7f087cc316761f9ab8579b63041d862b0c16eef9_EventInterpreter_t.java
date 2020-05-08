 package com.prasanna.fileevents;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 /**
  * <p>
 * Given a list of events, based on the input sequence, interprets the final
  * action for a particular file and logs it using the Logger.
  * </p>
  * 
  * <p>
  * Interpreted events:
  * <ul>
  * <li>Added</li>
  * <li>Deleted</li>
  * <li>Renamed</li>
  * <li>Moved</li>
  * </ul>
  * </p>
  * 
  * <p>
 * <b>Added</b>: For a file, an add is interpreted as add if the previous event
  * in the sequence was not a delete for the same file name or if the previous
 * delete had the same file contents.
  * 
 * For a directory, an add is interpreted as add as long as it was not preceded
  * immediately by delete of the same directory. (Immediate preceding events may
 * also include delete event of all the files and directories within the deleted
  * directory.)
  * </p>
  * <p>
  * <b>Deleted</b>: For a file, delete is logged as long as its parent directory
  * was also not reported to be deleted and is not followed by a add with same
  * file name and contents with a different path (move) or with different file
  * name and contents with same path (rename).
  * 
  * For a directory, a delete event of the directory may be followed by delete of
  * all files in the directory. However in such a case only delete of directory
  * is logged while ignoring logging of deletion of files within directory.
  * </p>
  * 
  * <p>
  * <b>Renamed</b>: For a file, sequence of events starting with a delete
  * followed by a add with a different name as long as the contents of the file
  * being deleted and added is the same and both share the same parent path.
  * 
  * For a directory, sequence of events starting with a delete directory followed
  * by deletion of all the content within the directory followed by a add
  * directory at the same level followed by addition of the exact same deleted
  * contents with the same relative path under the new directory.
  * </p>
  * 
  * <p>
  * <b>Moved</b>: For a file, sequence of events starting with a delete followed
  * by a add as long as the name and contents of the file being deleted and added
  * is the same but have different parent paths.
  * 
  * For a directory, sequence of events starting with a delete directory followed
  * by deletion of all the content within the directory followed by a add
  * directory with the same name at a different directory level followed by
  * addition of the exact same deleted contents along with exact same relative
  * path.
  * </p>
  * 
  * @author prasanna
  * 
  */
 public class EventInterpreter
 {
 	private Stack<List<Event>> eventStack = new Stack<List<Event>>();
 
 	private static final Logger logger = new Logger();
 
 	private Event lastLoggedEvent = null;
 
 	public EventInterpreter()
 	{
 		logger.printHeader();
 	}
 
 	public void interpret(List<Event> events)
 	{
 		if (events != null)
 		{
 			for (Event event : events)
 			{
 				interpretEvent(event);
 			}
 
 			processRemainingInStack();
 		}
 	}
 
 	private void processRemainingInStack()
 	{
 		while (eventStack.isEmpty() == false)
 		{
 			detailEvent(eventStack.pop());
 		}
 	}
 
 	private void detailEvent(List<Event> eventHistory)
 	{
 		if (eventHistory.size() == 1)
 		{
 			Event event = eventHistory.get(0);
 
 			logger.prettyLog(event, getActionString(event.getEventType()), event.getPath());
 			lastLoggedEvent = event;
 		}
 		else
 		{
 			if (isADirOperation(eventHistory))
 			{
 				detailDirOperation(eventHistory);
 			}
 			else if (isAFileOperation(eventHistory))
 			{
 				detailFileOperation(eventHistory);
 			}
 			else
 			{
 				Event previousEvent = null;
 				for (Event event : eventHistory)
 				{
 					if (previousEvent != null && previousEvent.isDirectoryEvent()
 					        && previousEvent.getEventType().equals(EventType.DEL)
 					        && previousEvent.getPath().equals(event.getParentPath()))
 					{
 						continue;
 					}
 
 					logger.prettyLog(event, getActionString(event.getEventType()), event.getPath());
 					previousEvent = event;
 					lastLoggedEvent = event;
 				}
 			}
 		}
 	}
 
 	private void detailFileOperation(List<Event> eventHistory)
 	{
 		Action action = null;
 		String newPath = null;
 		Event firstEvent = eventHistory.get(0);
 
 		for (int i = 1; i < eventHistory.size(); i++)
 		{
 			Event event = eventHistory.get(i);
 
 			if (event.getEventType().equals(EventType.ADD))
 			{
 				action = determineMoveOrRename(firstEvent, event);
 				newPath = event.getPath();
 			}
 		}
 
 		logger.prettyLog(firstEvent, action, firstEvent.getPath() + " to " + newPath);
 		lastLoggedEvent = firstEvent;
 	}
 
 	private void detailDirOperation(List<Event> eventHistory)
 	{
 		Action action = null;
 		String newPath = null;
 		Event firstEvent = eventHistory.get(0);
 
 		for (int i = 1; i < eventHistory.size(); i++)
 		{
 			Event event = eventHistory.get(i);
 
 			if (event.isDirectoryEvent())
 			{
 				if (event.getEventType().equals(EventType.ADD))
 				{
 					action = determineMoveOrRename(firstEvent, event);
 					newPath = event.getPath();
 					break;
 				}
 			}
 		}
 
 		logger.prettyLog(firstEvent, action, firstEvent.getPath() + " to " + newPath);
 		lastLoggedEvent = firstEvent;
 	}
 
 	private Action determineMoveOrRename(Event firstEvent, Event event)
 	{
 		Action action;
 
 		if (firstEvent.getParentPath().equals(event.getParentPath()))
 		{
 			action = Action.RENAMED;
 		}
 		else
 		{
 			action = Action.MOVED;
 		}
 
 		return action;
 	}
 
 	private Action getActionString(EventType eventType)
 	{
 		return eventType.equals(EventType.ADD) ? Action.ADDED : Action.DELETED;
 	}
 
 	private boolean isADirOperation(List<Event> eventHistory)
 	{
 		boolean isADirOperation = false;
 		Map<String, Event> delFileContentMap = new HashMap<String, Event>();
 
 		String newDirPath = null;
 		String oldDirPath = null;
 
 		for (Event event : eventHistory)
 		{
 			if (event.getEventType().equals(EventType.DEL))
 			{
 				if (event.isDirectoryEvent())
 				{
 					oldDirPath = event.getPath();
 				}
 
 				delFileContentMap.put(event.getContentHash(), event);
 			}
 			else
 			{
 				Event deletedEvent = delFileContentMap.remove(event.getContentHash());
 				if (deletedEvent == null)
 				{
 					isADirOperation = false;
 					break;
 				}
 				else
 				{
 					if (event.isDirectoryEvent())
 					{
 						newDirPath = event.getPath();
 					}
 					else
 					{
 						if (oldDirPath == null || newDirPath == null)
 						{
 							isADirOperation = false;
 							break;
 						}
 
 						if (deletedEvent.getPath().replaceFirst(oldDirPath, newDirPath)
 						        .equals(event.getPath()) == false)
 						{
 							isADirOperation = false;
 							break;
 						}
 					}
 				}
 			}
 
 			isADirOperation = true;
 		}
 
 		logLeftContentsInMap(delFileContentMap);
 		return isADirOperation;
 	}
 
 	private void logLeftContentsInMap(Map<String, Event> delFileContentMap)
 	{
 		for (Map.Entry<String, Event> entrySet : delFileContentMap.entrySet())
 		{
 			logger.prettyLog(entrySet.getValue(), getActionString(entrySet.getValue()
 			        .getEventType()), entrySet.getValue().getPath());
 		}
 	}
 
 	private boolean isAFileOperation(List<Event> eventHistory)
 	{
 		boolean isAFileOperation = false;
 
 		if (eventHistory.get(0).isDirectoryEvent() == false && eventHistory.size() == 2)
 		{
 			for (int i = 1; i < eventHistory.size(); i++)
 			{
 				if (eventHistory.get(i).getContentHash()
 				        .equals(eventHistory.get(0).getContentHash()) == false)
 				{
 					break;
 				}
 
 				isAFileOperation = true;
 			}
 		}
 
 		return isAFileOperation;
 	}
 
 	private void interpretEvent(Event event)
 	{
 		if (event.getEventType().equals(EventType.DEL))
 		{
 			interpretDelEvent(event);
 		}
 		else
 		{
 			interpretAddEvent(event);
 		}
 	}
 
 	private void interpretDelEvent(Event event)
 	{
 		if (event.isDirectoryEvent())
 		{
 			emptyStackAndAddNewEvent(event);
 		}
 		else
 		{
 			if (eventStack.isEmpty() == false)
 			{
 				if (eventStack.peek().get(0).isDirectoryEvent()
 				        && eventStack.peek().get(0).getPath().equals(event.getParentPath()))
 				{
 					List<Event> dirEventHistory = eventStack.pop();
 					dirEventHistory.add(event);
 					eventStack.push(dirEventHistory);
 				}
 				else
 				{
 					emptyStackAndAddNewEvent(event);
 				}
 			}
 			else
 			{
 				if (lastLoggedEvent != null
 				        && (lastLoggedEvent.isDirectoryEvent() == false
 				                || lastLoggedEvent.getEventType().equals(EventType.DEL) == false || event
 				                .isUnderParent(lastLoggedEvent.getPath()) == false))
 				{
 					List<Event> dirEventHistory = new ArrayList<Event>();
 					dirEventHistory.add(event);
 					eventStack.push(dirEventHistory);
 				}
 			}
 		}
 	}
 
 	private void interpretAddEvent(Event event)
 	{
 		if (eventStack.isEmpty() == false)
 		{
 			if (isAPossibleMoveOrRename(event))
 			{
 				List<Event> dirEventHistory = eventStack.pop();
 				dirEventHistory.add(event);
 				eventStack.push(dirEventHistory);
 			}
 			else
 			{
 				emptyStackAndAddNewEvent(event);
 			}
 		}
 		else
 		{
 			logger.prettyLog(event, Action.ADDED, event.getPath());
 		}
 	}
 
 	public boolean isAPossibleMoveOrRename(Event event)
 	{
 		boolean isAPossibleMoveOrRename = false;
 		int size = eventStack.peek().size();
 
 		if (size > 0)
 		{
 			Event firstEvent = eventStack.peek().get(0);
 
 			isAPossibleMoveOrRename = isAConsecutiveDirEvent(event);
 
 			if (isAPossibleMoveOrRename == false)
 			{
 				Event lastDirEvent = null;
 
 				for (int i = size - 1; i >= 0; i--)
 				{
 					Event lastEvent = eventStack.peek().get(i);
 					if (lastEvent.isDirectoryEvent())
 					{
 						lastDirEvent = lastEvent;
 						break;
 					}
 
 				}
 
 				if (event.isDirectoryEvent() == false && lastDirEvent != null
 				        && lastDirEvent.getPath().equals(event.getParentPath()))
 				{
 					for (Event historicalEvent : eventStack.peek())
 					{
 						if (historicalEvent.getContentHash().equals(event.getContentHash()))
 						{
 							isAPossibleMoveOrRename = true;
 						}
 					}
 				}
 
 				if (isAPossibleMoveOrRename == false)
 				{
 					isAPossibleMoveOrRename = (event.isDirectoryEvent() == false
 					        && firstEvent.isDirectoryEvent() == false && firstEvent
 					        .getContentHash().equals(event.getContentHash()));
 				}
 			}
 		}
 
 		return isAPossibleMoveOrRename;
 	}
 
 	private boolean isAConsecutiveDirEvent(Event event)
 	{
 		return event.isDirectoryEvent() && eventStack.peek().get(0).isDirectoryEvent()
 		        && eventStack.peek().get(0).getEventType().equals(EventType.DEL);
 	}
 
 	private void emptyStackAndAddNewEvent(Event event)
 	{
 		processRemainingInStack();
 
 		if (lastLoggedEvent == null || lastLoggedEvent.isDirectoryEvent() == false
 		        || lastLoggedEvent.getEventType().equals(EventType.DEL) == false
 		        || event.getEventType().equals(EventType.DEL) == false
 		        || event.isUnderParent(lastLoggedEvent.getPath()) == false)
 		{
 			List<Event> dirEventHistory = new ArrayList<Event>();
 			dirEventHistory.add(event);
 			eventStack.push(dirEventHistory);
 		}
 	}
 }
