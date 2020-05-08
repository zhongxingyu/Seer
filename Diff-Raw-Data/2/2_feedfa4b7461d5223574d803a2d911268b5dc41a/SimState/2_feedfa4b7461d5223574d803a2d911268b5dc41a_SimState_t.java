 /*
     Copyright (C) 2010 LearningWell AB (www.learningwell.com), Kärnkraftsäkerhet och Utbildning AB (www.ksu.se)
 
     This file is part of GIL (Generic Integration Layer).
 
     GIL is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     GIL is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with GIL.  If not, see <http://www.gnu.org/licenses/>.
 */
 package gil.core;
 
 import java.util.HashMap;
 
 /**
  * Defines constants for the common simulator states used in the process model adapter and the external system
  * adapter.
  * @author Göran Larsson @ LearningWell AB
  */
 public class SimState {
 
     public static final int UNKNOWN = 0;
     public static final int RUN = 1;
     public static final int FAIL = 2;
     public static final int FREEZE = 4;
     public static final int SLOW = 5;
     public static final int FAST = 6;
     public static final int STEP = 7;
     public static final int NOT_AVAILABLE = 8;
 
     private static HashMap<Integer, String> _names = new HashMap<Integer, String>() {
         {
             put(UNKNOWN, "Unknown");
             put(RUN, "Running");
             put(FAIL, "Fail");
             put(FREEZE, "Freeze");
             put(SLOW, "Slow");
             put(FAST, "Fast");
             put(STEP, "Step");
            put(NOT_AVAILABLE, "Not available");
         }
     };
 
     /**
      * Returns a readable name for the given state constant.
      */
     public static String getName(int state) {
         if (!_names.containsKey(state))
             throw new IllegalArgumentException("state");
 
         return _names.get(state);
     }
 }
