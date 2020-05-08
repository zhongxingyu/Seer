 /*
 Copyright (c) Members of the EGEE Collaboration. 2004-2010.
 See http://www.eu-egee.org/partners for details on the copyright holders.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package org.glite.lb;
 
 import org.glite.jobid.Jobid;
 import org.glite.lb.Event;
 
 import org.glite.lb.EventAbort;
 import org.glite.lb.EventAccepted;
 import org.glite.lb.EventCancel;
 import org.glite.lb.EventChangeACL;
 import org.glite.lb.EventChkpt;
 import org.glite.lb.EventClear;
 import org.glite.lb.EventCollectionState;
 import org.glite.lb.EventCondorError;
 import org.glite.lb.EventCondorMatch;
 import org.glite.lb.EventCondorReject;
 import org.glite.lb.EventCondorResourceUsage;
 import org.glite.lb.EventCondorShadowExited;
 import org.glite.lb.EventCondorShadowStarted;
 import org.glite.lb.EventCondorStarterExited;
 import org.glite.lb.EventCondorStarterStarted;
 import org.glite.lb.EventCurDescr;
 import org.glite.lb.EventDeQueued;
 import org.glite.lb.EventDone;
 import org.glite.lb.EventEnQueued;
 import org.glite.lb.EventHelperCall;
 import org.glite.lb.EventHelperReturn;
 import org.glite.lb.EventListener;
 import org.glite.lb.EventMatch;
 import org.glite.lb.EventNotification;
 import org.glite.lb.EventPBSDequeued;
 import org.glite.lb.EventPBSDone;
 import org.glite.lb.EventPBSError;
 import org.glite.lb.EventPBSMatch;
 import org.glite.lb.EventPBSPending;
 import org.glite.lb.EventPBSQueued;
 import org.glite.lb.EventPBSRerun;
 import org.glite.lb.EventPBSResourceUsage;
 import org.glite.lb.EventPBSRun;
 import org.glite.lb.EventPending;
 import org.glite.lb.EventPurge;
 import org.glite.lb.EventReallyRunning;
 import org.glite.lb.EventRefused;
 import org.glite.lb.EventRegJob;
 import org.glite.lb.EventResourceUsage;
 import org.glite.lb.EventResubmission;
 import org.glite.lb.EventResume;
 import org.glite.lb.EventRunning;
 import org.glite.lb.EventSuspend;
 import org.glite.lb.EventTransfer;
 import org.glite.lb.EventUserTag;
 import org.glite.lb.Level;
 import org.glite.lb.SeqCode;
 import org.glite.lb.Sources;
 import org.glite.lb.Timeval;
 import org.glite.wsdl.types.lb.EventSource;
 
 
 /**
  * This class is only for internal purposes of this package. Users of this query API
  * do not need it.
  * The class serves for converting events from org.glite.wsdl.types.lb.Event type
  * to org.glite.lb.Event type. In other words, it maps given wsdl event to the event
  * from logging API.
  *
  * @author Tomas Kramec, 207545@mail.muni.cz
  *
  */
 public class EventConvertor {
 
     /**
      * This method sets common attributes for the given event. These attributes
      * are included in every event.
      *
      * @param timestamp
      * @param arrived
      * @param host
      * @param level
      * @param priority
      * @param jobId
      * @param seqcode
      * @param user
      * @param source
      * @param srcInstance
      */
     private void setCommonAttributes(org.glite.wsdl.types.lb.Timeval timestamp,
             org.glite.wsdl.types.lb.Timeval arrived, String host,
             org.glite.wsdl.types.lb.Level level, Integer priority, String jobId,
             String seqcode, String user, org.glite.wsdl.types.lb.EventSource source,
             String srcInstance, Event event) {
 
         if (event == null) throw new IllegalArgumentException("event cannot be null");
 
         if (arrived != null) event.setArrived(new Timeval(arrived.getTvSec(), arrived.getTvUsec()));
         if (host != null) event.setHost(host);
         if (jobId != null) event.setJobId(new Jobid(jobId));
         if (level != null) event.setLevel(getLevelFromString(level.getValue()));
         if (priority != null) event.setPriority(priority);
         if (seqcode != null) event.setSeqcode(new SeqCode(SeqCode.NORMAL,seqcode));	/* XXX */
         if (source != null) event.setSource(getSourceFromString(source.getValue()));
         if (srcInstance != null) event.setSrcInstance(srcInstance);
         if (timestamp != null) event.setTimestamp(new Timeval(timestamp.getTvSec(), timestamp.getTvUsec()));
         if (user != null) event.setUser(user);
 
 
     }
 
     /**
      * This private method returns event source depending on input string
      *
      * @param from represents symbolic name for the source
      * @return source value
      */
     private Sources getSourceFromString(String from) {
         if (from == null) throw new IllegalArgumentException("from cannot be null");
 
         Sources source = new Sources(Sources.NONE);
 
         if (from.equals(EventSource._UserInterface)) source = new Sources(Sources.USER_INTERFACE);
         else if (from.equals(EventSource._NetworkServer)) source = new Sources(Sources.NETWORK_SERVER);
         else if (from.equals(EventSource._WorkloadManager)) source = new Sources(Sources.WORKLOAD_MANAGER);
         else if (from.equals(EventSource._BigHelper)) source = new Sources(Sources.BIG_HELPER);
         else if (from.equals(EventSource._JobSubmission)) source = new Sources(Sources.JOB_SUBMISSION);
         else if (from.equals(EventSource._LogMonitor)) source = new Sources(Sources.LOG_MONITOR);
         else if (from.equals(EventSource._LRMS)) source = new Sources(Sources.LRMS);
         else if (from.equals(EventSource._Application)) source = new Sources(Sources.APPLICATION);
         else if (from.equals(EventSource._LBServer)) source = new Sources(Sources.LB_SERVER);
 
         return source;
     }
 
     /**
      * This private method returns level depending on the given symbolic name.
      *
      * @param name symbolic name of the level
      * @return level object
      */
 
      private Level getLevelFromString(String name) {
         if(name.equals("UNDEFINED")) return Level.LEVEL_UNDEFINED;
         else if(name.equals("EMERGENCY")) return Level.LEVEL_EMERGENCY;
         else if(name.equals("ALERT")) return Level.LEVEL_ALERT;
         else if(name.equals("ERROR")) return Level.LEVEL_ERROR;
         else if(name.equals("WARNING")) return Level.LEVEL_WARNING;
         else if(name.equals("AUTH")) return Level.LEVEL_AUTH;
         else if(name.equals("SECURITY")) return Level.LEVEL_SECURITY;
         else if(name.equals("USAGE")) return Level.LEVEL_USAGE;
         else if(name.equals("SYSTEM")) return Level.LEVEL_SYSTEM;
         else if(name.equals("IMPORTANT")) return Level.LEVEL_IMPORTANT;
         else if(name.equals("DEBUG")) return Level.LEVEL_DEBUG;
         else throw new IllegalArgumentException("wrong level type");
     }
 
     /**
      * These private methods serve for creating particular events from logging API.
      * They fill in all relevant attributes of output event
      * as consistent with the attributes of the given wsdl event.
      * 
      * @param event Wsdl event
      * @return appropriate event from logging API
      */
 
     private EventAbort createEventAbort(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventAbort wsdlEvent = event.getAbort();
         EventAbort ev = new EventAbort();
 
         if (wsdlEvent.getReason()!=null) ev.setReason(wsdlEvent.getReason());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
      }
 
     private EventAccepted createEventAccepted(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventAccepted wsdlEvent = event.getAccepted();
         EventAccepted ev = new EventAccepted();
 
         Sources source = new Sources(Sources.NONE);
         if (wsdlEvent.getFrom() != null) {
             source = getSourceFromString(wsdlEvent.getFrom().getValue());
         }
         ev.setFrom(source);
 
         if (wsdlEvent.getFromHost()!=null) ev.setFromHost(wsdlEvent.getFromHost());
         if (wsdlEvent.getFromInstance()!=null) ev.setFromInstance(wsdlEvent.getFromInstance());
         if (wsdlEvent.getLocalJobid()!=null) ev.setLocalJobid(wsdlEvent.getLocalJobid());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCancel createEventCancel(org.glite.wsdl.types.lb.Event event) {
         EventCancel ev = new EventCancel();
         org.glite.wsdl.types.lb.EventCancel wsdlEvent = event.getCancel();
 
         if (wsdlEvent.getReason()!=null) ev.setReason(wsdlEvent.getReason());
 
         if(wsdlEvent.getStatusCode() != null) {
             ev.setStatusCode(EventCancel.StatusCode.valueOf(wsdlEvent.getStatusCode().getValue()));
         } else ev.setStatusCode(EventCancel.StatusCode.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventChangeACL createEventChangeACL(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventChangeACL wsdlEvent = event.getChangeACL();
         EventChangeACL ev = new EventChangeACL();
 
         if (wsdlEvent.getOperation() != null) {
             ev.setOperation(EventChangeACL.Operation.valueOf(wsdlEvent.getOperation().getValue()));
         } else ev.setOperation(EventChangeACL.Operation.UNDEFINED);
 
         if (wsdlEvent.getPermission()!= null) {
             ev.setPermission(EventChangeACL.Permission.valueOf(wsdlEvent.getPermission().getValue()));
         } else ev.setPermission(EventChangeACL.Permission.UNDEFINED);
 
         if (wsdlEvent.getPermissionType() != null) {
             ev.setPermissionType(EventChangeACL.PermissionType.valueOf(wsdlEvent.getPermissionType().getValue()));
         } else ev.setPermissionType(EventChangeACL.PermissionType.UNDEFINED);
 
         if (wsdlEvent.getUserId() != null) ev.setUserId(wsdlEvent.getUserId());
 
         if (wsdlEvent.getUserIdType() != null) {
             ev.setUserIdType(EventChangeACL.UserIdType.valueOf(wsdlEvent.getUserIdType().getValue()));
         } else ev.setUserIdType(EventChangeACL.UserIdType.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventChkpt createEventChkpt(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventChkpt wsdlEvent = event.getChkpt();
         EventChkpt ev = new EventChkpt();
 
         if (wsdlEvent.getClassad() != null) ev.setClassad(wsdlEvent.getClassad());
         if (wsdlEvent.getTag() != null) ev.setTag(wsdlEvent.getTag());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventClear createEventClear(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventClear wsdlEvent = event.getClear();
         EventClear ev = new EventClear();
 
         if (wsdlEvent.getReason() != null) {
             ev.setReason(EventClear.Reason.valueOf(wsdlEvent.getReason().getValue()));
         } else ev.setReason(EventClear.Reason.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCollectionState createEventCollectionState(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCollectionState wsdlEvent = event.getCollectionState();
         EventCollectionState ev = new EventCollectionState();
 
         if (wsdlEvent.getChild() != null) {
             ev.setChild(new Jobid(wsdlEvent.getChild()));
         }
         if (wsdlEvent.getChildEvent() != null) {
             ev.setChildEvent(wsdlEvent.getChildEvent());
         }
         if (wsdlEvent.getDoneCode() != null) {
             ev.setDoneCode(wsdlEvent.getDoneCode());
         }
         if (wsdlEvent.getHistogram() != null) {
             ev.setHistogram(wsdlEvent.getHistogram());
         }
         if (wsdlEvent.getState() != null) {
             ev.setState(wsdlEvent.getState());
         }
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorError createEventCondorError(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorError wsdlEvent = event.getCondorError();
         EventCondorError ev = new EventCondorError();
 
         if (ev.getErrorDesc() != null) ev.setErrorDesc(wsdlEvent.getErrorDesc());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorMatch createEventCondorMatch(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorMatch wsdlEvent = event.getCondorMatch();
         EventCondorMatch ev = new EventCondorMatch();
 
         if (ev.getDestHost() != null) ev.setDestHost(wsdlEvent.getDestHost());
         if (ev.getOwner() != null) ev.setOwner(wsdlEvent.getOwner());
         if (ev.getPreempting() != null) ev.setPreempting(wsdlEvent.getPreempting());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorReject createEventCondorReject(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorReject wsdlEvent = event.getCondorReject();
         EventCondorReject ev = new EventCondorReject();
 
         if (ev.getOwner() != null) ev.setOwner(wsdlEvent.getOwner());
         if (wsdlEvent.getStatusCode() != null) {
             ev.setStatusCode(EventCondorReject.StatusCode.valueOf(wsdlEvent.getStatusCode().getValue()));
         } else ev.setStatusCode(EventCondorReject.StatusCode.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorResourceUsage createEventCondorResourceUsage(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorResourceUsage wsdlEvent = event.getCondorResourceUsage();
         EventCondorResourceUsage ev = new EventCondorResourceUsage();
 
         if (ev.getName() != null) ev.setName(wsdlEvent.getName());
         ev.setQuantity(wsdlEvent.getQuantity());
         if (ev.getUnit() != null) ev.setUnit(wsdlEvent.getUnit());
         if (wsdlEvent.getUsage() != null) {
             ev.setUsage(EventCondorResourceUsage.Usage.valueOf(wsdlEvent.getUsage().getValue()));
         } else ev.setUsage(EventCondorResourceUsage.Usage.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
         return ev;
     }
 
     private EventCondorShadowExited createEventCondorShadowExited(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorShadowExited wsdlEvent = event.getCondorShadowExited();
         EventCondorShadowExited ev = new EventCondorShadowExited();
 
         ev.setShadowExitStatus(wsdlEvent.getShadowExitStatus());
         ev.setShadowPid(wsdlEvent.getShadowPid());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorShadowStarted createEventCondorShadowStarted(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorShadowStarted wsdlEvent = event.getCondorShadowStarted();
         EventCondorShadowStarted ev = new EventCondorShadowStarted();
 
         if (ev.getShadowHost() != null) ev.setShadowHost(wsdlEvent.getShadowHost());
         ev.setShadowPid(wsdlEvent.getShadowPid());
         ev.setShadowPort(wsdlEvent.getShadowPort());
         if (ev.getShadowStatus() != null) ev.setShadowStatus(wsdlEvent.getShadowStatus());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorStarterExited createEventCondorStarterExited(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorStarterExited wsdlEvent = event.getCondorStarterExited();
         EventCondorStarterExited ev = new EventCondorStarterExited();
 
         if (wsdlEvent.getJobExitStatus() != null) {
             ev.setJobExitStatus(wsdlEvent.getJobExitStatus());
         }
         if (wsdlEvent.getJobPid() != null) {
             ev.setJobPid(wsdlEvent.getJobPid());
         }
         if (wsdlEvent.getStarterExitStatus() != null) {
             ev.setStarterExitStatus(wsdlEvent.getStarterExitStatus());
         }
         if (wsdlEvent.getStarterPid() != null) {
             ev.setStarterPid(wsdlEvent.getStarterPid());
         }
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCondorStarterStarted createEventCondorStarterStarted(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCondorStarterStarted wsdlEvent = event.getCondorStarterStarted();
         EventCondorStarterStarted ev = new EventCondorStarterStarted();
 
         if (wsdlEvent.getStarterPid() != null) {
             ev.setStarterPid(wsdlEvent.getStarterPid());
         }
         if (wsdlEvent.getUniverse() != null) ev.setUniverse(wsdlEvent.getUniverse());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventCurDescr createEventCurDescr(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventCurDescr wsdlEvent = event.getCurDescr();
         EventCurDescr ev = new EventCurDescr();
 
         if (wsdlEvent.getDescr() != null) ev.setDescr(wsdlEvent.getDescr());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventDeQueued createEventDeQueued(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventDeQueued wsdlEvent = event.getDeQueued();
         EventDeQueued ev = new EventDeQueued();
 
         if (wsdlEvent.getLocalJobid() != null) ev.setLocalJobid(wsdlEvent.getLocalJobid());
         if (wsdlEvent.getQueue() != null) ev.setQueue(wsdlEvent.getQueue());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventDone createEventDone(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventDone wsdlEvent = event.getDone();
         EventDone ev = new EventDone();
 
         ev.setExitCode(wsdlEvent.getExitCode());
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
         if (wsdlEvent.getStatusCode() != null) {
             ev.setStatusCode(EventDone.StatusCode.valueOf(wsdlEvent.getStatusCode().getValue()));
         } else ev.setStatusCode(EventDone.StatusCode.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventEnQueued createEventEnQueued(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventEnQueued wsdlEvent = event.getEnQueued();
         EventEnQueued ev = new EventEnQueued();
 
         if (wsdlEvent.getJob() != null) ev.setJob(wsdlEvent.getJob());
         if (wsdlEvent.getQueue() != null) ev.setQueue(wsdlEvent.getQueue());
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
         if (wsdlEvent.getResult() != null) {
             ev.setResult(EventEnQueued.Result.valueOf(wsdlEvent.getResult().getValue()));
         } else ev.setResult(EventEnQueued.Result.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventHelperCall createEventHelperCall(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventHelperCall wsdlEvent = event.getHelperCall();
         EventHelperCall ev = new EventHelperCall();
 
         if (wsdlEvent.getHelperName() != null) ev.setHelperName(wsdlEvent.getHelperName());
         if (wsdlEvent.getHelperParams() != null) ev.setHelperParams(wsdlEvent.getHelperParams());
         if (wsdlEvent.getSrcRole() != null) {
             ev.setSrcRole(EventHelperCall.SrcRole.valueOf(wsdlEvent.getSrcRole().getValue()));
         } else ev.setSrcRole(EventHelperCall.SrcRole.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventHelperReturn createEventHelperReturn(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventHelperReturn wsdlEvent = event.getHelperReturn();
         EventHelperReturn ev = new EventHelperReturn();
 
         if (wsdlEvent.getHelperName() != null) ev.setHelperName(wsdlEvent.getHelperName());
         if (wsdlEvent.getRetval() != null) ev.setRetval(wsdlEvent.getRetval());
         if (wsdlEvent.getSrcRole() != null) {
             ev.setSrcRole(EventHelperReturn.SrcRole.valueOf(wsdlEvent.getSrcRole().getValue()));
         } else ev.setSrcRole(EventHelperReturn.SrcRole.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventListener createEventListener(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventListener wsdlEvent = event.getListener();
         EventListener ev = new EventListener();
 
         if (wsdlEvent.getSvcHost() != null) ev.setSvcHost(wsdlEvent.getSvcHost());
         if (wsdlEvent.getSvcName() != null) ev.setSvcName(wsdlEvent.getSvcName());
         ev.setSvcPort(wsdlEvent.getSvcPort());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventMatch createEventMatch(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventMatch wsdlEvent = event.getMatch();
         EventMatch ev = new EventMatch();
 
         if (wsdlEvent.getDestId() != null) ev.setDestId(wsdlEvent.getDestId());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventNotification createEventNotification(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventNotification wsdlEvent = event.getNotification();
         EventNotification ev = new EventNotification();
 
         if (wsdlEvent.getDestHost() != null) ev.setDestHost(wsdlEvent.getDestHost());
         ev.setDestPort(wsdlEvent.getDestPort());
         ev.setExpires(wsdlEvent.getExpires());
         if (wsdlEvent.getJobstat() != null) ev.setJobstat(wsdlEvent.getJobstat());
         if (wsdlEvent.getNotifId() != null) {
             ev.setNotifId(new Jobid(wsdlEvent.getNotifId()));
         }
         if (wsdlEvent.getOwner() != null) ev.setOwner(wsdlEvent.getOwner());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSDequeued createEventPBSDequeued(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSDequeued wsdlEvent = event.getPBSDequeued();
         EventPBSDequeued ev = new EventPBSDequeued();
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSDone createEventPBSDone(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSDone wsdlEvent = event.getPBSDone();
         EventPBSDone ev = new EventPBSDone();
 
         if (wsdlEvent.getExitStatus() != null) {
             ev.setExitStatus(wsdlEvent.getExitStatus());
         }
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSError createEventPBSError(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSError wsdlEvent = event.getPBSError();
         EventPBSError ev = new EventPBSError();
 
         if (wsdlEvent.getErrorDesc() != null) ev.setErrorDesc(wsdlEvent.getErrorDesc());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSMatch createEventPBSMatch(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSMatch wsdlEvent = event.getPBSMatch();
         EventPBSMatch ev = new EventPBSMatch();
 
         if (wsdlEvent.getDestHost() != null) ev.setDestHost(wsdlEvent.getDestHost());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSPending createEventPBSPending(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSPending wsdlEvent = event.getPBSPending();
         EventPBSPending ev = new EventPBSPending();
 
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSQueued createEventPBSQueued(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSQueued wsdlEvent = event.getPBSQueued();
         EventPBSQueued ev = new EventPBSQueued();
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSRerun createEventPBSRerun(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSRerun wsdlEvent = event.getPBSRerun();
         EventPBSRerun ev = new EventPBSRerun();
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSResourceUsage createEventPBSResourceUsage(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSResourceUsage wsdlEvent = event.getPBSResourceUsage();
         EventPBSResourceUsage ev = new EventPBSResourceUsage();
 
        if (wsdlEvent.getName() != null) ev.setName(wsdlEvent.getName());
        if (wsdlEvent.getQuantity() != null) {
            ev.setQuantity(wsdlEvent.getQuantity());
        }
        if (wsdlEvent.getUnit() != null) ev.setUnit(wsdlEvent.getUnit());
         if (wsdlEvent.getUsage() != null) {
             ev.setUsage(EventPBSResourceUsage.Usage.valueOf(wsdlEvent.getUsage().getValue()));
         } else ev.setUsage(EventPBSResourceUsage.Usage.UNDEFINED);

         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPBSRun createEventPBSRun(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPBSRun wsdlEvent = event.getPBSRun();
         EventPBSRun ev = new EventPBSRun();
 
         if (wsdlEvent.getDestHost() != null) ev.setDestHost(wsdlEvent.getDestHost());
         if (wsdlEvent.getPid() != null) {
             ev.setPid(wsdlEvent.getPid());
         }
         if (wsdlEvent.getScheduler() != null) ev.setScheduler(wsdlEvent.getScheduler());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPending createEventPending(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPending wsdlEvent = event.getPending();
         EventPending ev = new EventPending();
 
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventPurge createEventPurge(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventPurge wsdlEvent = event.getPurge();
         EventPurge ev = new EventPurge();
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventReallyRunning createEventReallyRunning(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventReallyRunning wsdlEvent = event.getReallyRunning();
         EventReallyRunning ev = new EventReallyRunning();
 
         if (wsdlEvent.getWnSeq() != null) ev.setWnSeq(wsdlEvent.getWnSeq());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventRefused createEventRefused(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventRefused wsdlEvent = event.getRefused();
         EventRefused ev = new EventRefused();
 
         if (wsdlEvent.getFrom() != null) {
             ev.setFrom(getSourceFromString(wsdlEvent.getFrom().getValue()));
         } else ev.setFrom(new Sources(Sources.NONE));
         if (wsdlEvent.getFromHost() != null) ev.setFromHost(wsdlEvent.getFromHost());
         if (wsdlEvent.getFromInstance() != null) ev.setFromInstance(wsdlEvent.getFromInstance());
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventRegJob createEventRegJob(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventRegJob wsdlEvent = event.getRegJob();
         EventRegJob ev = new EventRegJob();
 
         if (wsdlEvent.getJdl() != null) ev.setJdl(wsdlEvent.getJdl());
 
         if (wsdlEvent.getJobtype() != null) {
             ev.setJobtype(EventRegJob.Jobtype.valueOf(wsdlEvent.getJobtype().getValue()));
         } else ev.setJobtype(EventRegJob.Jobtype.UNDEFINED);
 
         if (wsdlEvent.getNs() != null) ev.setNs(wsdlEvent.getNs());
 
         if (wsdlEvent.getNsubjobs() != null) ev.setNsubjobs(wsdlEvent.getNsubjobs());
 
         if (wsdlEvent.getParent() != null) ev.setParent(new Jobid(wsdlEvent.getParent()));
 
         if (wsdlEvent.getSeed() != null) ev.setSeed(wsdlEvent.getSeed());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventResourceUsage createEventResourceUsage(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventResourceUsage wsdlEvent = event.getResourceUsage();
         EventResourceUsage ev = new EventResourceUsage();
 
         ev.setQuantity(wsdlEvent.getQuantity());
         if (wsdlEvent.getResource() != null) ev.setResource(wsdlEvent.getResource());
         if (wsdlEvent.getUnit() != null) ev.setUnit(wsdlEvent.getUnit());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventResubmission createEventResubmission(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventResubmission wsdlEvent = event.getResubmission();
         EventResubmission ev = new EventResubmission();
 
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
         if (wsdlEvent.getResult() != null) {
             ev.setResult(EventResubmission.Result.valueOf(wsdlEvent.getResult().getValue()));
         } else ev.setResult(EventResubmission.Result.UNDEFINED);
         if (wsdlEvent.getTag() != null) ev.setTag(wsdlEvent.getTag());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventResume createEventResume(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventResume wsdlEvent = event.getResume();
         EventResume ev = new EventResume();
 
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventRunning createEventRunning(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventRunning wsdlEvent = event.getRunning();
         EventRunning ev = new EventRunning();
 
         if (wsdlEvent.getNode() != null) ev.setNode(wsdlEvent.getNode());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventSuspend createEventSuspend(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventSuspend wsdlEvent = event.getSuspend();
         EventSuspend ev = new EventSuspend();
 
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventTransfer createEventTransfer(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventTransfer wsdlEvent = event.getTransfer();
         EventTransfer ev = new EventTransfer();
 
         if (wsdlEvent.getDestHost() != null) ev.setDestHost(wsdlEvent.getDestHost());
         if (wsdlEvent.getDestInstance() != null) ev.setDestInstance(wsdlEvent.getDestInstance());
         if (wsdlEvent.getDestJobid() != null) ev.setDestJobid(wsdlEvent.getDestJobid());
 
         if (wsdlEvent.getDestination() != null) {
             ev.setDestination(getSourceFromString(wsdlEvent.getDestination().getValue()));
         } else ev.setDestination(new Sources(Sources.NONE));
 
         if (wsdlEvent.getJob() != null) ev.setJob(wsdlEvent.getJob());
         if (wsdlEvent.getReason() != null) ev.setReason(wsdlEvent.getReason());
 
         if (wsdlEvent.getResult() != null) {
             ev.setResult(EventTransfer.Result.valueOf(wsdlEvent.getResult().getValue()));
         } else ev.setResult(EventTransfer.Result.UNDEFINED);
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
     private EventUserTag createEventUserTag(org.glite.wsdl.types.lb.Event event) {
         org.glite.wsdl.types.lb.EventUserTag wsdlEvent = event.getUserTag();
         EventUserTag ev = new EventUserTag();
 
         if (wsdlEvent.getName() != null) ev.setName(wsdlEvent.getName());
         if (wsdlEvent.getValue() != null) ev.setValue(wsdlEvent.getValue());
 
         setCommonAttributes(wsdlEvent.getTimestamp(), wsdlEvent.getArrived(),
            wsdlEvent.getHost(), wsdlEvent.getLevel(), wsdlEvent.getPriority(),
            wsdlEvent.getJobId(), wsdlEvent.getSeqcode(), wsdlEvent.getUser(),
            wsdlEvent.getSource(), wsdlEvent.getSrcInstance(), ev);
 
         return ev;
     }
 
 
 
     /**
      * Converts given event of org.glite.wsdl.types.lb.Event type
      * into event of org.glite.lb.Event type
      * 
      * @param event Event to be converted
      * @return converted event
      */
     protected Event convert(org.glite.wsdl.types.lb.Event event) {
 
         if (event == null) throw new IllegalArgumentException("event cannot be null");
 
         //EventAbort
         if(event.getAbort()!=null) return createEventAbort(event);
 
         //EventAccepted
         if(event.getAccepted()!=null) return createEventAccepted(event);
 
         //EventCancel
         if (event.getCancel() != null) return createEventCancel(event);
 
         //EventChangeACL
         if (event.getChangeACL() != null) return createEventChangeACL(event);
 
         //EventChkpt
         if (event.getChkpt() != null) return createEventChkpt(event);
 
         //EventClear
         if (event.getClear() != null) return createEventClear(event);
 
         //EventCollectionState
         if (event.getCollectionState() != null) return createEventCollectionState(event);
 
         //EventCondorError
         if (event.getCondorError() != null) return createEventCondorError(event);
 
         //EventCondorMatch
         if (event.getCondorMatch() != null) return createEventCondorMatch(event);
 
         //EventCondorReject
         if (event.getCondorReject() != null) return createEventCondorReject(event);
 
         //EventCondorResourceUsage
         if (event.getCondorResourceUsage() != null) return createEventCondorResourceUsage(event);
 
         //EventCondorShadowExited
         if (event.getCondorShadowExited() != null) return createEventCondorShadowExited(event);
 
         //EventCondorShadowStarted
         if (event.getCondorShadowStarted() != null) return createEventCondorShadowStarted(event);
 
         //EventCondorStarterExited
         if (event.getCondorStarterExited() != null) return createEventCondorStarterExited(event);
 
         //EventCondorStarterStarted
         if (event.getCondorStarterStarted() != null) return createEventCondorStarterStarted(event);
 
         //EventCurDescr
         if (event.getCurDescr() != null) return createEventCurDescr(event);
 
         //EventDeQueued
         if (event.getDeQueued() != null) return createEventDeQueued(event);
 
         //EventDone
         if (event.getDone() != null) return createEventDone(event);
 
         //EventEnQueued
         if (event.getEnQueued() != null) return createEventEnQueued(event);
 
         //EventHelperCall
         if (event.getHelperCall() != null) return createEventHelperCall(event);
 
         //EventHelperReturn
         if (event.getHelperReturn() != null) return createEventHelperReturn(event);
 
         //EventListener
         if (event.getListener() != null) return createEventListener(event);
 
         //EventMatch
         if (event.getMatch() != null) return createEventMatch(event);
 
         //EventNotification
         if (event.getNotification() != null) return createEventNotification(event);
 
         //EventPBSDequeued
         if (event.getPBSDequeued() != null) return createEventPBSDequeued(event);
 
         //EventPBSDone
         if (event.getPBSDone() != null) return createEventPBSDone(event);
 
         //EventPBSError
         if (event.getPBSError() != null) return createEventPBSError(event);
 
         //EventPBSMatch
         if (event.getPBSMatch() != null) return createEventPBSMatch(event);
 
         //EventPBSPendig
         if (event.getPBSPending() != null) return createEventPBSPending(event);
 
         //EventPBSQueued
         if (event.getPBSQueued() != null) return createEventPBSQueued(event);
 
         //EventPBSRerun
         if (event.getPBSRerun() != null) return createEventPBSRerun(event);
 
         //EventPBSResourceUsage
         if (event.getPBSResourceUsage() != null) return createEventPBSResourceUsage(event);
 
         //EventPBSRun
         if (event.getPBSRun() != null) return createEventPBSRun(event);
 
         //EventPendig
         if (event.getPending() != null) return createEventPending(event);
 
         //EventPurge
         if (event.getPurge() != null) return createEventPurge(event);
 
         //EventReallyRunning
         if (event.getReallyRunning() != null) return createEventReallyRunning(event);
 
         //EventRefused
         if (event.getRefused() != null) return createEventRefused(event);
 
         //EventRegJob
         if (event.getRegJob() != null) return createEventRegJob(event);
 
         //EventResourceUsage
         if (event.getResourceUsage() != null) return createEventResourceUsage(event);
 
         //EventResubmission
         if (event.getResubmission() != null) return createEventResubmission(event);
 
         //EventResume
         if (event.getResume() != null) return createEventResume(event);
 
         //EventRunning
         if (event.getRunning() != null) return createEventRunning(event);
 
         //EventSuspend
         if (event.getSuspend() != null) return createEventSuspend(event);
 
         //EventTransfer
         if (event.getTransfer() != null) return createEventTransfer(event);
 
         //EventUserTag
         if (event.getUserTag() != null) return createEventUserTag(event);
 
         return null;
     }
 
 }
