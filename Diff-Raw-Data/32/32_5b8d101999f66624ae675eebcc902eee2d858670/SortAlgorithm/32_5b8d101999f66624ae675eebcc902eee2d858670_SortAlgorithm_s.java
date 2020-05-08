 package Classify;
 
 import java.util.*;
 import static java.lang.Thread.sleep;
 
 public class SortAlgorithm {
 
     protected String FDescription;
     protected Boolean FInProgress;
     protected Boolean FInterrupted;
     protected Boolean FTerminated;
     protected ArrayList<Integer> FElements;
     protected List FEventListeners;
     protected Integer FSortSteps;
     protected long FStartTime;
     protected long FEndTime;
     protected ArrayList<LogEntry> FLogEntries;
 
     protected void DoExecute() throws Exception{
         OneStepSorted();
     }
 
     protected synchronized void RaiseOneStepSortedEvent() {
         ClassifySortEvent lEvent = new ClassifySortEvent(this);
         lEvent.Elements = FElements;
         Iterator lItr = FEventListeners.iterator();
         while(lItr.hasNext())  {
             ((ClassifyEventListener)lItr.next()).handleOneStepSorted(lEvent);
         }
     }
 
     protected void OneStepSorted() throws Exception{
         IncreaseSortSteps();
         RaiseOneStepSortedEvent();
         while (FInterrupted) {
             try {
                 sleep(100);
             } catch (Exception e) {
             }
         }
         Thread.sleep(50);
         if (FTerminated) {
             throw new Exception();
         }
     }
 
     protected void BeforeExecute() {
         FInProgress = true;
         FInterrupted = false;
         FTerminated = false;
         FSortSteps = 0;
         FStartTime = System.currentTimeMillis();
         WriteLog("Start Sort of "+Integer.toString(GetElementSize())+" Elements");
     }
 
     protected Integer IncreaseSortSteps() {
         FSortSteps = FSortSteps + 1;
         return FSortSteps;
     }
 
     protected Integer GetElementSize() {
         if (FElements != null)
             return FElements.size();
         else
             return 0;
     }
 
     protected void AfterExecute() {
         FEndTime = System.currentTimeMillis();
         WriteLog("Time used "+((FEndTime-FStartTime)/1000)+" sec");
         WriteLog("Sort in "+Integer.toString(FSortSteps)+" Steps");
         WriteLog("End Sort of "+Integer.toString(GetElementSize())+" Elements");
         FInProgress = false;
     }
 
     protected void WriteLog(String aText) {
         Date lDate = new Date();
         LogEntry aLogEntry = new LogEntry(lDate, aText);
         FLogEntries.add(aLogEntry);
         RaiseLogEntryEvent(aLogEntry);
     }
 
     protected synchronized void RaiseLogEntryEvent(LogEntry aLogEntry) {
         ClassifyLogEvent lEvent = new ClassifyLogEvent(this);
         lEvent.LogEntry = aLogEntry;
         Iterator lItr = FEventListeners.iterator();
         while(lItr.hasNext())  {
             ((ClassifyEventListener)lItr.next()).handleWriteLog(lEvent);
         }
     }
 
     public SortAlgorithm() {
         FEventListeners= new ArrayList();
         FLogEntries = new ArrayList<LogEntry>();
         FInProgress = false;
         FInterrupted = false;
         FTerminated = false;
         FDescription = "";
         FSortSteps = 0;
         FStartTime = 0;
         FEndTime = 0;
     }
 
     public String GetDescription() {
         return(FDescription);
     }
 
     public void SetElements(ArrayList<Integer> aValue) {
         FElements = aValue;
     }
 
     public ArrayList<Integer> GetElements() {
         return(FElements);
     }
 
     public void SetInterrupted(Boolean aValue) {
         FInterrupted = aValue;
     }
 
     public Boolean GetInterrupted() {
         return(FInterrupted);
     }
 
     public synchronized void addEventListener(ClassifyEventListener aListener)  {
         FEventListeners.add(aListener);
     }
     public synchronized void removeEventListener(ClassifyEventListener aListener)   {
         FEventListeners.remove(aListener);
     }
 
     public void Execute() {
        if (!FInProgress) {
             BeforeExecute();
             try {
                 DoExecute();
             } catch (Exception e) {
                 FTerminated = false;
             }
             AfterExecute();
         }
     }
 
     public void Terminate() {
         FTerminated = true;
         FInterrupted = false;
     }
 
     public void ClearLog() {
         FLogEntries.clear();
     }
 
 }
