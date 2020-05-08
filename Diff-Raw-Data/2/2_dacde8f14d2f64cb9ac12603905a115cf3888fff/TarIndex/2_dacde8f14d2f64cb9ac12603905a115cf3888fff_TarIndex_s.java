 /*
  * Copyright 2012 Hittapunktse AB (http://www.hitta.se/)
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
 
 package se.hitta.tar;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.lang3.time.DateUtils;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Iterators;
 
 /**
  * This class will build an index of a provided tar archive.
  */
 public class TarIndex
 {
     private final TarHeader[] headers;
     
     private final File tarFile;
     private final Date lastModified;
     
     /**
      * @param tarFile the tar archive to index
      * @throws IOException if the file cannot be opened for reading
      */
     public TarIndex(File tarFile) throws IOException
     {
         this.tarFile = tarFile;
         this.lastModified = DateUtils.truncate(new Date(tarFile.lastModified()), Calendar.SECOND); //trim milliseconds
         
         TarHeaderIterator tarHeaderIterator = new TarHeaderIterator(tarFile);
         this.headers = Iterators.toArray(tarHeaderIterator, TarHeader.class);
         Arrays.sort(this.headers);
     }
     
     /**
      * @param key the path of the file, as saved in the tar archive (i.e. including directories)
      * @return if found, a {@link TarHeader} will be present in the response, if not it will be absent
      */
     public Optional<TarHeader> get(String key)
     {
         int index = Arrays.binarySearch(headers, TarHeader.buildMatcher(key));
         
        return index > 0 ? Optional.of(this.headers[index]) : Optional.<TarHeader>absent(); 
     }
 
     /**
      * @return the tar archive this index was initialized with
      */
     public File getTarFile()
     {
         return tarFile;
     }
     
     /**
      * @return the number of indexed files
      */
     public long getSize()
     {
         return this.headers.length;
     }
     
     /**
      * @return the "last modified" property of the tar archive this index was initialized with (milliseconds trimmed)
      */
     public Date getLastModified()
     {
         return lastModified;
     }
 }
