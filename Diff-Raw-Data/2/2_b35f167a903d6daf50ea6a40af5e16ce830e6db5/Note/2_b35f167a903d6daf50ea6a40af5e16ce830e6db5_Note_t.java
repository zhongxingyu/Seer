 package org.jtheque.persistence;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
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
 
 /**
  * A properties class for notes.
  *
  * @author Baptiste Wicht
  */
 public enum Note {
     NULL(1, "data.notes.null"),
     BAD(2, "data.notes.bad"),
     MIDDLE(3, "data.notes.middle"),
     GOOD(4, "data.notes.good"),
     VERYGOOD(5, "data.notes.verygood"),
     PERFECT(6, "data.notes.perfect"),
     UNDEFINED(7, "data.notes.undefined");
 
     private final int note;
     private final String key;
 
     /**
      * Construct a new NoteType.
      *
      * @param note The note value.
      * @param key  The i18n key of the Note.
      */
     Note(int note, String key) {
         this.note = note;
         this.key = key;
     }
 
     /**
      * Return the int value of the NoteType.
      *
      * @return The int value of the enum.
      */
     public int intValue() {
         return note;
     }
 
     /**
      * Return the i18n key of the Note.
      *
      * @return The i18n key of the Note.
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Return the enum with the enum int value.
      *
      * @param e The int value to search.
      *
      * @return The NoteType corresponding to the int value to search.
      */
     public static Note fromIntValue(int e) {
         Note note = MIDDLE;
 
         for (Note n : values()) {
            if (n.intValue() == e) {
                 note = n;
                 break;
             }
         }
 
         return note;
     }
 }
