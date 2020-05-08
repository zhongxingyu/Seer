 /*
  * Copyright 2009 HPDI, LLC
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jvss.physical;
 
 import org.jvss.physical.DeltaOperation.DeltaCommand;
 
 import java.util.List;
 
 /**
  * Simulates stream-like traversal over a set of revision delta operations.
  */
 public class DeltaSimulator
 {
    public static interface FromLogCallback
    {
       int fromLog(byte[] data, int offset, int count);
    }
 
    public static interface FromSuccessorCallback
    {
       int fromSuccessor(int offset, int count);
    }
 
    private final List<DeltaOperation> operations;
 
    private IEnumerator<DeltaOperation> enumerator;
 
    private int operationOffset;
 
    private int fileOffset;
 
    private boolean eof;
 
    private DeltaOperation current;
 
    public DeltaOperation getCurrent()
    {
       if (enumerator != null)
       {
       }
       return null;
    }
 
    /**
     * @return the operations
     */
    public List<DeltaOperation> getOperations()
    {
       return operations;
    }
 
    /**
     * @return the operationOffset
     */
    public int getOperationOffset()
    {
       return operationOffset;
    }
 
    /**
     * @return the fileOffset
     */
    public int getFileOffset()
    {
       return fileOffset;
    }
 
    /**
     * @return the eof
     */
    public boolean isEof()
    {
       return eof;
    }
 
    public DeltaSimulator(List<DeltaOperation> operations)
    {
       this.operations = operations;
       Reset();
    }
 
    public void dispose()
    {
       if (enumerator != null)
       {
          //enumerator.
          enumerator = null;
       }
    }
 
    public void seek(int offset)
    {
       //System.err.println("Before seek>>>ni=" + ((ListIterator<DeltaOperation>)enumerator).nextIndex());
       //System.err.println("Before seek>>>pi=" + ((ListIterator<DeltaOperation>)enumerator).previousIndex());
       System.out.println(String.format("Before seek>>>fo=%d opf=%d off=%d", fileOffset, operationOffset, offset));
       if (offset != fileOffset)
       {
          if (offset < fileOffset)
          {
             Reset();
          }
          while (fileOffset < offset && !eof)
          {
             int seekRemaining = offset - fileOffset;
             int operationRemaining = enumerator.current().getLength() - operationOffset;
             if (seekRemaining < operationRemaining)
             {
                operationOffset += seekRemaining;
                fileOffset += seekRemaining;
             }
             else
             {
                fileOffset += operationRemaining;
                eof = !enumerator.moveNext();
                operationOffset = 0;
             }
          }
       }
       System.out.println(String.format("After seek>>>fo=%d opf=%d off=%d", fileOffset, operationOffset, offset));
       //System.err.println("Before seek>>>ni=" + ((ListIterator<DeltaOperation>)enumerator).nextIndex());
       //System.err.println("Before seek>>>pi=" + ((ListIterator<DeltaOperation>)enumerator).previousIndex());
 
    }
 
    public void read(int length, FromLogCallback fromLog, FromSuccessorCallback fromSuccessor)
    {
       while (length > 0 && !eof)
       {
          DeltaOperation operation = enumerator.current();
          int operationRemaining = operation.getLength() - operationOffset;
          int count = Math.min(length, operationRemaining);
          int bytesRead;
          if (operation.getCommand() == DeltaCommand.WriteLog)
          {
            bytesRead = fromLog.fromLog(operation.getData(), operation.getData().length + operationOffset, count);
          }
          else
          {
             bytesRead = fromSuccessor.fromSuccessor(operation.getOffset() + operationOffset, count);
          }
          if (bytesRead == 0)
          {
             break;
          }
          operationOffset += bytesRead;
          fileOffset += bytesRead;
          if (length >= operationRemaining)
          {
             eof = !enumerator.moveNext();
             operationOffset = 0;
          }
          length -= bytesRead;
       }
    }
 
    private void Reset()
    {
       if (enumerator != null)
       {
          enumerator = null;
       }
       enumerator = new IEnumerator<DeltaOperation>(operations.iterator());
       eof = !enumerator.hasNext();
       operationOffset = 0;
       fileOffset = 0;
    }
 }
