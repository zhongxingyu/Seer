 package com.khmelyuk.memory.vm;
 
 import com.khmelyuk.memory.OutOfBoundException;
 import com.khmelyuk.memory.OutOfMemoryException;
 import com.khmelyuk.memory.vm.table.Block;
 import com.khmelyuk.memory.vm.table.VirtualMemoryTable;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * Represents a virtual memory of fixed size.
  *
  * @author Ruslan Khmelyuk
  */
 public class FixedVirtualMemory implements VirtualMemory {
 
     private byte[] data;
     private VirtualMemoryTable table;
    private int size;
 
     public FixedVirtualMemory(int length, VirtualMemoryTable table) {
        this.size = length;
         this.data = new byte[length];
         this.table = table;
     }
 
     public int size() {
        return size;
     }
 
     public int getFreeSize() {
         return table.getFreeMemorySize();
     }
 
     public int getUsedSize() {
         return table.getUsedMemorySize();
     }
 
     public VirtualMemoryBlock allocate(int length) throws OutOfMemoryException, OutOfBoundException {
         if (length < 0) {
             throw new OutOfBoundException();
         }
 
         Block block = table.allocate(length);
         if (block == null) {
             throw new OutOfMemoryException();
         }
 
         return new VirtualMemoryBlock(this, block);
     }
 
     @Override
     public void free() {
         data = new byte[0];
         table.reset(0);
        size = 0;
     }
 
     @Override
     public void free(VirtualMemoryBlock block) {
         table.free(block.getBlock());
     }
 
     // ---------------------------------------------- Read/write support
 
     public InputStream getInputStream() {
         return new VMInputStream(this);
     }
 
     public InputStream getInputStream(int offset, int length) throws OutOfBoundException {
         if (offset + length > size()) {
             throw new OutOfBoundException();
         }
 
         return new VMInputStream(this, offset, length);
     }
 
     public OutputStream getOutputStream() {
         return new VMOutputStream(this);
     }
 
     public OutputStream getOutputStream(int offset, int length) throws OutOfBoundException {
         if (offset + length > size()) {
             throw new OutOfBoundException();
         }
 
         return new VMOutputStream(this, offset, length);
     }
 
     public void write(byte[] data) throws OutOfBoundException {
         if (data.length > this.data.length) {
             throw new OutOfBoundException();
         }
 
         System.arraycopy(data, 0, this.data, 0, data.length);
     }
 
     public void write(byte[] data, int offset) throws OutOfBoundException {
        if (offset >= size || data.length + offset > size) {
             throw new OutOfBoundException();
         }
 
         System.arraycopy(data, 0, this.data, offset, data.length);
     }
 
     public void write(byte[] data, int offset, int length) throws OutOfBoundException {
        if (offset >= size || length + offset > size) {
             throw new OutOfBoundException();
         }
 
         if (data.length < length) {
             length = data.length;
         }
 
         System.arraycopy(data, 0, this.data, 0, length);
     }
 
     public int read(byte[] data) throws OutOfBoundException {
         int length = data.length;
        if (length > size) {
            length = size;
         }
 
         System.arraycopy(this.data, 0, data, 0, length);
 
         return length;
     }
 
     public int read(byte[] data, int offset, int length) {
         if (data.length < length) {
             length = data.length;
         }
        if (offset + length > size) {
            length = size - offset;
         }
         if (length == 0) {
             return -1;
         }
 
         System.arraycopy(this.data, offset, data, 0, length);
 
         return length;
     }
 
     public void write(byte data, int offset) throws OutOfBoundException {
        if (offset >= size) {
             throw new OutOfBoundException();
         }
         this.data[offset] = data;
     }
 
     public byte read(int offset) {
        if (offset >= size) {
             return -1;
         }
         return this.data[offset];
     }
 }
