 /*
  * Copyright 2000-2012 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jetbrains.buildServer.queueManager;
 
 /**
  * @author Oleg Rybak (oleg.rybak@jetbrains.com)
  */
 public final class PluginConstants {
 
   public interface WEB {
 
    public static final String QUEUE_ACTIONS_URL = "/queueStartStop.html";
 
     public static final String PARAM_NEW_QUEUE_STATE = "newQueueState";
 
     public static final String PARAM_STATE_CHANGE_REASON = "stateChangeReason";
 
     public static final String PARAM_QUEUE_STATE = "queueState";
 
     public static final String PARAM_CAN_MANAGE = "canManage";
   }
 }
