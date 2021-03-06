 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.jackrabbit.oak.plugins.segment;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkPositionIndexes;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.jcr.PropertyType;
 
 import org.apache.jackrabbit.oak.api.Blob;
 import org.apache.jackrabbit.oak.api.PropertyState;
 import org.apache.jackrabbit.oak.api.Type;
 import org.apache.jackrabbit.oak.spi.state.ChildNodeEntry;
 import org.apache.jackrabbit.oak.spi.state.NodeState;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.io.ByteStreams;
 
 public class SegmentWriter {
 
     private static final int INITIAL_BUFFER_SIZE = 1 << 12; // 4kB
 
     static final int BLOCK_SIZE = 1 << 12; // 4kB
 
     static final int INLINE_BLOCKS = 16;
 
     static final int INLINE_SIZE = INLINE_BLOCKS * BLOCK_SIZE; // 64kB
 
     private final SegmentStore store;
 
     private final int blocksPerSegment;
 
     private final int blockSegmentSize;
 
     private final Map<String, RecordId> strings = Maps.newHashMap();
 
     private final Map<NodeTemplate, RecordId> templates = Maps.newHashMap();
 
     private UUID uuid = UUID.randomUUID();
 
     private List<UUID> uuids = new ArrayList<UUID>(255);
 
     private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
 
     public SegmentWriter(SegmentStore store) {
         this.store = store;
         this.blocksPerSegment = store.getMaxSegmentSize() / BLOCK_SIZE;
         this.blockSegmentSize = blocksPerSegment * BLOCK_SIZE;
     }
 
     public synchronized void flush() {
         if (buffer.position() > 0) {
             byte[] data = new byte[buffer.position()];
             buffer.flip();
             buffer.get(data);
 
             store.createSegment(new Segment(
                     uuid, data, uuids.toArray(new UUID[0])));
 
             uuid = UUID.randomUUID();
             uuids.clear();
             buffer.clear();
         }
     }
 
     private RecordId prepare(int size) {
         return prepare(size, Collections.<RecordId>emptyList());
     }
 
     private synchronized RecordId prepare(int size, Collection<RecordId> ids) {
         Set<UUID> segmentIds = new HashSet<UUID>();
         for (RecordId id : ids) {
             UUID segmentId = id.getSegmentId();
             if (!uuid.equals(segmentId) && !uuids.contains(segmentId)) {
                 segmentIds.add(segmentId);
             }
         }
 
         int fullSize = size + 4 * ids.size();
         if (buffer.position() + fullSize > store.getMaxSegmentSize()) {
             flush();
         }
         if (fullSize > buffer.remaining()) {
             int n = Math.min(buffer.capacity() * 2, store.getMaxSegmentSize());
            while (n < buffer.position() + fullSize) {
                n = Math.min(n * 2, store.getMaxSegmentSize());
            }
             ByteBuffer newBuffer = ByteBuffer.allocate(n);
             buffer.flip();
             newBuffer.put(buffer);
             buffer = newBuffer;
         }
         return new RecordId(uuid, buffer.position());
     }
 
     private synchronized void writeRecordId(RecordId id) {
         UUID segmentId = id.getSegmentId();
         int index = uuids.indexOf(segmentId);
         if (index == -1) {
             index = uuids.size();
             uuids.add(segmentId);
         }
         buffer.putInt(index << 24 | id.getOffset());
     }
 
     private void writeInlineBlocks(
             List<RecordId> blockIds, byte[] buffer, int offset, int length) {
         int begin = offset;
         int end = offset + length;
         while (begin + BLOCK_SIZE <= end) {
             blockIds.add(writeBlock(buffer, begin, BLOCK_SIZE));
             begin += BLOCK_SIZE;
         }
         if (begin < end) {
             blockIds.add(writeBlock(buffer, begin, end - begin));
         }
     }
 
     private void writeBulkSegment(
             List<RecordId> blockIds, byte[] buffer, int offset, int length) {
         UUID segmentId = UUID.randomUUID();
         store.createSegment(segmentId, buffer, offset, length);
         for (int position = 0; position < length; position += BLOCK_SIZE) {
             blockIds.add(new RecordId(segmentId, position));
         }
     }
 
     private synchronized RecordId writeListBucket(List<RecordId> bucket) {
         RecordId bucketId = prepare(0, bucket);
         for (RecordId id : bucket) {
             writeRecordId(id);
         }
         return bucketId;
     }
 
     class MapEntry implements Comparable<MapEntry> {
         private final int hashCode;
         private final RecordId key;
         private final RecordId value;
 
         MapEntry(int hashCode, RecordId key, RecordId value) {
             this.hashCode = hashCode;
             this.key = key;
             this.value = value;
         }
 
         @Override
         public int compareTo(MapEntry that) {
             // int diff = Integer.compare(hashCode, that.hashCode);
             int diff = hashCode == that.hashCode ? 0 : hashCode < that.hashCode ? -1 : 1;
             if (diff == 0) {
                 diff = key.compareTo(that.key);
             }
             if (diff == 0) {
                 diff = value.compareTo(that.value);
             }
             return diff;
         }
 
         public boolean equals(Object object) {
             if (this == object) {
                 return true;
             } else if (object instanceof MapEntry) {
                 MapEntry that = (MapEntry) object;
                 return hashCode == that.hashCode
                         && key.equals(that.key)
                         && value.equals(that.value);
             } else {
                 return false;
             }
         }
 
     }
 
     private synchronized RecordId writeMapBucket(
             List<MapEntry> entries, int level) {
         int size = 1 << MapRecord.LEVEL_BITS;
         int mask = size - 1;
         int shift = level * MapRecord.LEVEL_BITS;
 
         if (entries.size() <= size) {
             Collections.sort(entries);
 
             List<RecordId> ids = Lists.newArrayList();
             for (MapEntry entry : entries) {
                 ids.add(entry.key);
                 ids.add(entry.value);
             }
 
             RecordId bucketId = prepare(4 + entries.size() * 12, ids);
             buffer.putInt(entries.size());
             for (MapEntry entry : entries) {
                 buffer.putInt(entry.hashCode);
             }
             for (MapEntry entry : entries) {
                 writeRecordId(entry.key);
             }
             for (MapEntry entry : entries) {
                 writeRecordId(entry.value);
             }
             return bucketId;
         } else {
             List<MapEntry>[] buckets = new List[size];
             for (MapEntry entry : entries) {
                 int bucketIndex = (entry.hashCode >> shift) & mask;
                 if (buckets[bucketIndex] == null) {
                     buckets[bucketIndex] = Lists.newArrayList();
                 }
                 buckets[bucketIndex].add(entry);
             }
 
             List<RecordId> bucketIds = Lists.newArrayList();
             long bucketMap = 0L;
             for (int i = 0; i < buckets.length; i++) {
                 if (buckets[i] != null) {
                     bucketIds.add(writeMapBucket(buckets[i], level + 1));
                     bucketMap |= 1L << i;
                 }
             }
 
             RecordId bucketId = prepare(12 + bucketIds.size() * 4, bucketIds);
             buffer.putInt(entries.size());
             buffer.putLong(bucketMap);
             for (RecordId id : bucketIds) {
                 writeRecordId(id);
             }
             return bucketId;
         }
     }
 
     private synchronized RecordId writeValueRecord(
             long length, RecordId blocks) {
         RecordId valueId = prepare(8, Collections.singleton(blocks));
         buffer.putLong((length - 0x4080) | (0x3L << 62));
         writeRecordId(blocks);
         return valueId;
     }
 
     /**
      * Writes a block record containing the given block of bytes.
      *
      * @param bytes source buffer
      * @param offset offset within the source buffer
      * @param length number of bytes to write
      * @return block record identifier
      */
     public synchronized RecordId writeBlock(
             byte[] bytes, int offset, int length) {
         checkNotNull(bytes);
         checkPositionIndexes(offset, offset + length, bytes.length);
 
         RecordId blockId = prepare(length);
         buffer.put(bytes, offset, length);
         return blockId;
     }
 
     /**
      * Writes a list record containing the given list of record identifiers.
      *
      * @param list list of record identifiers
      * @return list record identifier
      */
     public RecordId writeList(List<RecordId> list) {
         checkNotNull(list);
 
         if (list.isEmpty()) {
             return prepare(0); // special case
         }
 
         List<RecordId> thisLevel = list;
         while (thisLevel.size() > 1) {
             List<RecordId> nextLevel = Lists.newArrayList();
             for (List<RecordId> bucket :
                     Lists.partition(thisLevel, ListRecord.LEVEL_SIZE)) {
                 nextLevel.add(writeListBucket(bucket));
             }
             thisLevel = nextLevel;
         }
         return thisLevel.iterator().next();
     }
 
     public RecordId writeMap(Map<String, RecordId> map) {
         List<MapEntry> entries = Lists.newArrayList();
         for (Map.Entry<String, RecordId> entry : map.entrySet()) {
             String key = entry.getKey();
             entries.add(new MapEntry(
                     key.hashCode(), writeString(key), entry.getValue()));
         }
         return writeMapBucket(entries, 0);
     }
 
     /**
      * Writes a string value record.
      *
      * @param string string to be written
      * @return value record identifier
      */
     public RecordId writeString(String string) {
         RecordId id = strings.get(string);
         if (id == null) {
             byte[] data = string.getBytes(Charsets.UTF_8);
             try {
                 id = writeStream(new ByteArrayInputStream(data));
             } catch (IOException e) {
                 throw new IllegalStateException("Unexpected IOException", e);
             }
             strings.put(string, id);
         }
         return id;
     }
 
     /**
      * Writes a stream value record. The given stream is consumed
      * <em>and closed</em> by this method.
      *
      * @param stream stream to be written
      * @return value record identifier
      * @throws IOException if the stream could not be read
      */
     public RecordId writeStream(InputStream stream) throws IOException {
         RecordId id = SegmentStream.getRecordIdIfAvailable(stream);
         if (id == null) {
             try {
                 List<RecordId> blockIds = new ArrayList<RecordId>();
 
                 // First read the head of the stream. This covers most small
                 // values and the frequently accessed head of larger ones.
                 // The head gets inlined in the current segment.
                 byte[] head = new byte[INLINE_SIZE];
                 int headLength = ByteStreams.read(stream, head, 0, head.length);
 
                 if (headLength < 0x80) {
                     id = prepare(1 + headLength);
                     buffer.put((byte) headLength);
                     buffer.put(head, 0, headLength);
                 } else if (headLength - 0x80 < 0x4000) {
                     id = prepare(2 + headLength);
                     buffer.putShort((short) ((headLength - 0x80) | 0x8000));
                     buffer.put(head, 0, headLength);
                 } else {
                     writeInlineBlocks(blockIds, head, 0, headLength);
                     long length = headLength;
 
                     // If the stream filled the full head buffer, it's likely
                     // that the bulk of the data is still to come. Read it
                     // in larger chunks and save in separate segments.
                     if (headLength == head.length) {
                         byte[] bulk = new byte[blockSegmentSize];
                         int bulkLength = ByteStreams.read(
                                 stream, bulk, 0, bulk.length);
                         while (bulkLength > INLINE_SIZE) {
                             writeBulkSegment(blockIds, bulk, 0, bulkLength);
                             length += bulkLength;
                             bulkLength = ByteStreams.read(
                                     stream, bulk, 0, bulk.length);
                         }
                         // The tail chunk of the stream is too small to put in
                         // a separate segment, so we inline also it.
                         if (bulkLength > 0) {
                             writeInlineBlocks(blockIds, bulk, 0, bulkLength);
                             length += bulkLength;
                         }
                     }
 
                     id = writeValueRecord(length, writeList(blockIds));
                 }
             } finally {
                 stream.close();
             }
         }
         return id;
     }
 
     private RecordId writeProperty(PropertyState state) {
         Type<?> type = state.getType();
         int count = state.count();
 
         List<RecordId> valueIds = Lists.newArrayList();
         for (int i = 0; i < count; i++) {
             if (type.tag() == PropertyType.BINARY) {
                 try {
                     Blob blob = state.getValue(Type.BINARY, i);
                     valueIds.add(writeStream(blob.getNewStream()));
                 } catch (IOException e) {
                     throw new IllegalStateException("Unexpected IOException", e);
                 }
             } else {
                 valueIds.add(writeString(state.getValue(Type.STRING, i)));
             }
         }
         RecordId valueId = writeList(valueIds);
 
         if (type.isArray()) {
             RecordId propertyId = prepare(4, Collections.singleton(valueId));
             buffer.putInt(count);
             writeRecordId(valueId);
             return propertyId;
         } else {
             return valueId;
         }
     }
 
     public synchronized RecordId writeTemplate(NodeTemplate template) {
         checkNotNull(template);
         RecordId id = templates.get(template);
         if (id == null) {
             Collection<RecordId> ids = Lists.newArrayList();
             int head = 0;
 
             RecordId primaryId = null;
             if (template.hasPrimaryType()) {
                 head |= 1 << 31;
                 primaryId = writeString(template.getPrimaryType());
                 ids.add(primaryId);
             }
 
             List<RecordId> mixinIds = null;
             if (template.hasMixinTypes()) {
                 head |= 1 << 30;
                 mixinIds = Lists.newArrayList();
                 for (String mixin : template.getMixinTypes()) {
                     mixinIds.add(writeString(mixin));
                 }
                 ids.addAll(mixinIds);
                 checkState(mixinIds.size() < (1 << 10));
                 head |= mixinIds.size() << 18;
             }
 
             RecordId childNameId = null;
             if (template.hasNoChildNodes()) {
                 head |= 1 << 29;
             } else if (template.hasManyChildNodes()) {
                 head |= 1 << 28;
             } else {
                 childNameId = writeString(template.getChildName());
                 ids.add(childNameId);
             }
 
             PropertyTemplate[] properties = template.getPropertyTemplates();
             RecordId[] propertyNames = new RecordId[properties.length];
             byte[] propertyTypes = new byte[properties.length];
             for (int i = 0; i < properties.length; i++) {
                 propertyNames[i] = writeString(properties[i].getName());
                 Type<?> type = properties[i].getType();
                 if (type.isArray()) {
                     propertyTypes[i] = (byte) -type.tag();
                 } else {
                     propertyTypes[i] = (byte) type.tag();
                 }
             }
             ids.addAll(Arrays.asList(propertyNames));
             checkState(propertyNames.length < (1 << 18));
             head |= propertyNames.length;
 
             id = prepare(4 + propertyTypes.length, ids);
             buffer.putInt(head);
             if (primaryId != null) {
                 writeRecordId(primaryId);
             }
             if (mixinIds != null) {
                 for (RecordId mixinId : mixinIds) {
                     writeRecordId(mixinId);
                 }
             }
             if (childNameId != null) {
                 writeRecordId(childNameId);
             }
             for (int i = 0; i < propertyNames.length; i++) {
                 writeRecordId(propertyNames[i]);
                 buffer.put(propertyTypes[i]);
             }
 
             templates.put(template, id);
         }
         return id;
     }
 
     public RecordId writeNode(NodeState state) {
         RecordId nodeId = SegmentNodeState.getRecordIdIfAvailable(state);
         if (nodeId != null) {
             return nodeId;
         }
 
         NodeTemplate template = new NodeTemplate(state);
 
         List<RecordId> ids = Lists.newArrayList();
         ids.add(writeTemplate(template));
 
         if (template.hasManyChildNodes()) {
             Map<String, RecordId> childNodes = Maps.newHashMap();
             for (ChildNodeEntry entry : state.getChildNodeEntries()) {
                 childNodes.put(entry.getName(), writeNode(entry.getNodeState()));
             }
             ids.add(writeMap(childNodes));
         } else if (!template.hasNoChildNodes()) {
             ids.add(writeNode(state.getChildNode(template.getChildName())));
         }
 
         for (PropertyTemplate property : template.getPropertyTemplates()) {
             ids.add(writeProperty(state.getProperty(property.getName())));
         }
 
         RecordId recordId = prepare(0, ids);
         for (RecordId id : ids) {
             writeRecordId(id);
         }
         return recordId;
     }
 
 }
