 /**
  * Copyright (C) 2012 Ness Computing, Inc.
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
 package com.nesscomputing.hbase.spill;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.annotation.Nonnull;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Closeables;
 
import org.apache.hadoop.hbase.client.Put;

 import com.nesscomputing.logging.Log;
import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 public class SpilledFile
 {
     private static final Log LOG = Log.findLog();
 
     private final File file;
 
     private final int version;
     private final int elements;
     private final long timestamp;
 
     SpilledFile(@Nonnull final File file) throws IOException
     {
         Preconditions.checkNotNull(file, "the file must not be null!");
 
         this.file = file;
 
         InputStream is = null;
 
         try {
             is = new FileInputStream(file);
             version = BinaryConverter.readInt(is);
             elements = BinaryConverter.readInt(is);
             timestamp = BinaryConverter.readLong(is);
 
             LOG.trace("Opened spill file %s : v%d, count: %d, age: %d", file.getAbsolutePath(), version, elements, timestamp);
         }
         finally {
             Closeables.closeQuietly(is);
         }
     }
 
     public String getName()
     {
         return file.getName();
     }
 
     public int getVersion()
     {
         return version;
     }
 
     public int getElements()
     {
         return elements;
     }
 
     public long getTimestamp()
     {
         return timestamp;
     }
 
     public List<Put> load() throws IOException
     {
         InputStream is = null;
 
         final ImmutableList.Builder<Put> builder = ImmutableList.builder();
 
         try {
             is = new BufferedInputStream(new FileInputStream(file));
 
             final int skippedBytes = 4 + 4 + 8; // int, int, long
            Preconditions.checkState( skippedBytes == is.skip(skippedBytes), "skipped byte mismatch (you are in trouble...)");
 
             Put put = null;
 
             while ((put = BinaryConverter.PUT_READ_FUNCTION.apply(is)) != null) {
                 builder.add(put);
             }
 
             final List<Put> result = builder.build();
             Preconditions.checkState(result.size() == elements, "The preamble reported %d elements, but %d were found!", elements, result.size());
             return result;
         }
         finally {
             Closeables.closeQuietly(is);
         }
     }
 
     @Override
     public boolean equals(final Object other)
     {
        if (!(other instanceof SpilledFile))
             return false;
        SpilledFile castOther = (SpilledFile) other;
         return new EqualsBuilder().append(file, castOther.file).append(version, castOther.version).append(elements, castOther.elements).append(timestamp, castOther.timestamp).isEquals();
     }
 
     private transient int hashCode;
 
     @Override
     public int hashCode()
     {
         if (hashCode == 0) {
             hashCode = new HashCodeBuilder().append(file).append(version).append(elements).append(timestamp).toHashCode();
         }
         return hashCode;
     }
 
     private transient String toString;
 
     @Override
     public String toString()
     {
         if (toString == null) {
             toString = new ToStringBuilder(this).append("file", file).append("version", version).append("elements", elements).append("timestamp", timestamp).toString();
         }
         return toString;
     }
 


 }
