 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.box.server.waveserver;
 
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 
 import java.io.Closeable;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Set;
 
 /**
  * Stores wavelet deltas.
  *
  * <p>
  * Callers must serialize all calls to
  * {@code create, open, delete} on the same wavelet.
  *
  * @author soren@google.com (Soren Lassen)
  */
 public interface DeltaStore {
 
   /**
    * Accesses the delta history for a wavelet.
    * Permits reading historical deltas and appending deltas to the history.
    */
   interface DeltasAccess extends WaveletDeltaRecordReader, Closeable {
     /**
      * Blocking call to append deltas to the end of the delta history.
      * If the call returns normally (doesn't throw an exception), then
      * the deltas have been successfully and "durably" stored, that is,
      * the method forces the data to disk.
      *
      * @param deltas contiguous deltas, beginning from the DeltaAccess
      *        object's end version. It is the caller's responsibility to
      *        ensure that everything matches up (applied and transformed
      *        deltas in the records match, that the hashes are correctly
      *        computed, etc).
      * @throws IOException if anything goes wrong with the underlying storage.
      */
     void append(Collection<WaveletDeltaRecord> deltas) throws IOException;
   }
 
   /**
   * Creates a {@link DeltasAccess} instance which can be used to
   * to store deltas beginning from version zero. Only when one or
   * more deltas are appended will this wavelet "exist" in the
   * sense that it will be returned if {@link #lookup(WaveId)}
   * is called with {@code waveletName.waveId}.
   *
   * @throws IOException if anything goes wrong with the underlying storage
   *         or if this wavelet already exists in the delta store.
   */
  DeltasAccess create(WaveletName waveletName) throws IOException;

  /**
   * Opens a non-empty wavelet, i.e., a wavelet with one or more deltas in
   * the delta store.
    *
    * @throws IOException if anything goes wrong with the underlying storage.
   * @throws FileNotFoundException if this wavelet doesn't exist in the delta
   *         store.
    */
   DeltasAccess open(WaveletName waveletName) throws FileNotFoundException, IOException;
 
   /**
    * Deletes a non-empty wavelet.
    *
    * @throws IOException if anything goes wrong with the underlying storage.
    * @throws FileNotFoundException if this wavelet doesn't exist in the delta
    *         store.
    */
   void delete(WaveletName waveletName) throws FileNotFoundException, IOException;
 
   /**
   * Looks up all wavelets with deltas in the delta store.
    *
    * @throws IOException if anything goes wrong with the underlying storage.
    */
   Set<WaveletId> lookup(WaveId waveId) throws IOException;
 }
