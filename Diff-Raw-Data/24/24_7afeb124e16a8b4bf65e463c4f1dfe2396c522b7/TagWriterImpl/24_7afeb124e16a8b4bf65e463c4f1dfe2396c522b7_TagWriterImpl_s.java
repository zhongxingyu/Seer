 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.database;
 
 import com.google.inject.Inject;
 
 public class TagWriterImpl implements TagWriter {
 
     private final DbFrontend mDbFrontend;
 
     @Inject
     public TagWriterImpl(DbFrontend dbFrontend) {
         mDbFrontend = dbFrontend;
     }
 
     @Override
     public void add(CharSequence geocacheId, Tag tag) {
         final ISQLiteDatabase mDatabase = mDbFrontend.getDatabase();
        
         mDatabase.execSQL("DELETE FROM TAGS WHERE Cache='" + geocacheId + "'");
         mDatabase.insert("TAGS", new String[] {
                 "Cache", "Id"
         }, new Object[] {
                 geocacheId, tag.ordinal()
         });
     }
 
     public boolean hasTag(CharSequence geocacheId, Tag tag) {
        final ISQLiteDatabase mDatabase = mDbFrontend.getDatabase();
         final boolean hasValue = mDatabase.hasValue("TAGS", "Cache='" + geocacheId + "' AND Id="
                 + tag.ordinal());
         return hasValue;
     }
 
 }
