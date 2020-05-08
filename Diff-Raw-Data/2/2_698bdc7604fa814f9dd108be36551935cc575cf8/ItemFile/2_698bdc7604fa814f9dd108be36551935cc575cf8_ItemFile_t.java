 /*
  * Copyright (C) 2012 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jvss.physical;
 
 import org.jvss.physical.ItemHeaderRecord.ItemType;
 import org.jvss.physical.RevisionRecord.Action;
 import org.jvss.physical.RevisionRecord.ArchiveRevisionRecord;
 import org.jvss.physical.RevisionRecord.BranchRevisionRecord;
 import org.jvss.physical.RevisionRecord.CommonRevisionRecord;
 import org.jvss.physical.RevisionRecord.DestroyRevisionRecord;
 import org.jvss.physical.RevisionRecord.EditRevisionRecord;
 import org.jvss.physical.RevisionRecord.MoveRevisionRecord;
 import org.jvss.physical.RevisionRecord.RenameRevisionRecord;
 import org.jvss.physical.RevisionRecord.ShareRevisionRecord;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Represents a file containing VSS project/file records.
  */
 public class ItemFile extends VssRecordFile
 {
    private final ItemHeaderRecord header;
 
    public ItemFile(String filename, String encoding)
    {
       super(filename, encoding);
       try
       {
          String fileSig = reader.readString(0x20);
          if (!fileSig.equals("SourceSafe@Microsoft"))
          {
             throw new BadHeaderException("Incorrect file signature");
          }
 
          ItemType fileType = ItemType.valueOf(reader.readInt16());
          int fileVersion = reader.readInt16();
          if (fileVersion != 6)
          {
             throw new BadHeaderException("Incorrect file version");
          }
 
          reader.skip(16); // reserved; always 0
 
          if (fileType == ItemType.PROJECT)
          {
             header = new ProjectHeaderRecord();
          }
          else
          {
             header = new FileHeaderRecord();
          }
 
          readRecord(header);
          if (header.getItemType() != fileType)
          {
             throw new BadHeaderException("Header record type mismatch");
          }
       }
       catch (EndOfBufferException e)
       {
          throw new BadHeaderException("Truncated header", e);
       }
    }
 
    /**
     * @return the header
     */
    public ItemHeaderRecord getHeader()
    {
       return header;
    }
 
    public VssRecord getRecord(int offset)
    {
       return getRecord(new VssRecordCreator(), false, offset);
       //return GetRecord<VssRecord>(CreateRecord, false, offset);
    }
 
    public VssRecord GetNextRecord(boolean skipUnknown)
    {
       return getNextRecord(new VssRecordCreator(), skipUnknown);
       //         return GetNextRecord<VssRecord>(CreateRecord, skipUnknown);
    }
 
    public RevisionRecord GetFirstRevision()
    {
       if (header.getFirstRevOffset() > 0)
       {
          return (RevisionRecord)getRecord(new VssRecordCreator(), false, header.getFirstRevOffset());
          //return GetRecord<RevisionRecord>(CreateRevisionRecord, false, header.FirstRevOffset);
       }
       return null;
    }
 
    public RevisionRecord GetNextRevision(RevisionRecord revision)
    {
       reader.setOffset(revision.getHeader().getOffset() + revision.getHeader().getLength() + RecordHeader.LENGTH);
       return getNextRecord(new RevisionRecordCreator(), true);
    }
 
    public RevisionRecord GetLastRevision()
    {
       if (header.getLastRevOffset() > 0)
       {
          //return GetRecord<RevisionRecord>(CreateRevisionRecord, false, header.LastRevOffset);
          return getRecord(new RevisionRecordCreator(), false, header.getLastRevOffset());
       }
       return null;
    }
 
    public RevisionRecord getPreviousRevision(RevisionRecord revision)
    {
       if (revision.getPrevRevOffset() > 0)
       {
 
          //return GetRecord<RevisionRecord>(CreateRevisionRecord, false, revision.PrevRevOffset);
          return getRecord(new RevisionRecordCreator(), false, revision.getPrevRevOffset());
       }
       return null;
    }
 
    public DeltaRecord getPreviousDelta(EditRevisionRecord revision)
    {
       if (revision.getPrevDeltaOffset() > 0)
       {
          DeltaRecord record = new DeltaRecord();
          ReadRecord(record, revision.getPrevDeltaOffset());
          return record;
       }
       return null;
    }
 
    public List<String> GetProjects()
    {
       LinkedList<String> result = new LinkedList<String>();
       FileHeaderRecord fileHeader = (FileHeaderRecord)header;
       if (fileHeader != null)
       {
          ProjectRecord record = new ProjectRecord();
          int offset = fileHeader.getProjectOffset();
          while (offset > 0)
          {
             ReadRecord(record, offset);
             if (record.getProjectFile() != null && !record.getProjectFile().isEmpty())
             {
                result.addFirst(record.getProjectFile());
             }
             offset = record.getPrevProjectOffset();
          }
       }
       return result;
    }
 
    private static class VssRecordCreator implements RecordCreator<VssRecord>
    {
 
       /**
        * @see org.jvss.physical.VssRecordFile.RecordCreator#createRecord(org.jvss.physical.BufferReader,
        *      org.jvss.physical.RecordHeader)
        */
       @Override
       public VssRecord createRecord(BufferReader reader, RecordHeader header)
       {
          if (RevisionRecord.SIGNATURE.equals(header.getSignature()))
          {
             return new RevisionRecordCreator().createRecord(reader, header);
          }
          else if (CommentRecord.SIGNATURE.equals(header.getSignature()))
          {
             return new CommentRecord();
          }
          else if (CheckoutRecord.SIGNATURE.equals(header.getSignature()))
          {
             return new CheckoutRecord();
          }
          else if (ProjectRecord.SIGNATURE.equals(header.getSignature()))
          {
             return new ProjectRecord();
          }
          else if (BranchRecord.SIGNATURE.equals(header.getSignature()))
          {
             return new BranchRecord();
          }
          else if (DeltaRecord.SIGNATURE.equals(header.getSignature()))
          {
             return new DeltaRecord();
          }
 
          return null;
       }
    }
 
    private static class RevisionRecordCreator implements RecordCreator<RevisionRecord>
    {
 
       /**
        * @see org.jvss.physical.VssRecordFile.RecordCreator#createRecord(org.jvss.physical.BufferReader,
        *      org.jvss.physical.RecordHeader)
        */
       @Override
       public RevisionRecord createRecord(BufferReader reader, RecordHeader header)
       {
         if (!header.getSignature().equals(RevisionRecord.SIGNATURE))
          {
             return null;
          }
 
          RevisionRecord record;
          Action action = RevisionRecord.peekAction(reader);
          switch (action)
          {
             case Label :
                record = new RevisionRecord();
                break;
             case DestroyProject :
             case DestroyFile :
                record = new DestroyRevisionRecord();
                break;
             case RenameProject :
             case RenameFile :
                record = new RenameRevisionRecord();
                break;
             case MoveFrom :
             case MoveTo :
                record = new MoveRevisionRecord();
                break;
             case ShareFile :
                record = new ShareRevisionRecord();
                break;
             case BranchFile :
             case CreateBranch :
                record = new BranchRevisionRecord();
                break;
             case EditFile :
                record = new EditRevisionRecord();
                break;
             case ArchiveProject :
             case RestoreProject :
                record = new ArchiveRevisionRecord();
                break;
             case CreateProject :
             case AddProject :
             case AddFile :
             case DeleteProject :
             case DeleteFile :
             case RecoverProject :
             case RecoverFile :
             case CreateFile :
             default :
                record = new CommonRevisionRecord();
                break;
          }
          return record;
       }
    }
 
 }
