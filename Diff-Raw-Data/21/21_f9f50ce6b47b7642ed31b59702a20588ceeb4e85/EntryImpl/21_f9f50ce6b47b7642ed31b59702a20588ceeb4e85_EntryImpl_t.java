 /**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.bookkeeper.mledger.impl;
 
 import org.apache.bookkeeper.client.LedgerEntry;
 import org.apache.bookkeeper.mledger.Entry;
 import org.apache.bookkeeper.mledger.Position;
 
 class EntryImpl implements Entry {
 
    private final long ledgerId;
    private final long entryId;
     private byte[] data;
 
     EntryImpl(LedgerEntry ledgerEntry) {
        this.ledgerId = ledgerEntry.getLedgerId();
        this.entryId = ledgerEntry.getEntryId();
        this.data = ledgerEntry.getEntry();
     }
 
     @Override
     public byte[] getData() {
         return data;
     }
 
     @Override
     public long getLength() {
        return data.length;
     }
 
     @Override
     public Position getPosition() {
        return new PositionImpl(ledgerId, entryId);
     }
 
 }
