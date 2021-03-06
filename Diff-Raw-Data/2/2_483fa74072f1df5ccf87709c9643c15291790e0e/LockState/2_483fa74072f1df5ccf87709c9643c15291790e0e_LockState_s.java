 /**
  * Copyright 2011, Deft Labs.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.deftlabs.lock.mongo.impl;
 
 /**
  * The lock states.
  */
 enum LockState {
 
     LOCKED("locked"),
     UNLOCKED("unlocked");
 
     private LockState(final String pCode) { _code = pCode; }
     private final String _code;
 
     public String code() { return _code; }
 
     public boolean isLocked() { return this == LOCKED; }
     public boolean isUnlocked() { return this == UNLOCKED; }
 
     public static LockState findByCode(final String pCode) {
         if (pCode == null) throw new IllegalArgumentException("Invalid lock state code: " + pCode);
        for (final LockState s : values()) if (s.equals(pCode)) return s;
         throw new IllegalArgumentException("Invalid lock state code: " + pCode);
     }
 }
 
