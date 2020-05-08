 package edu.syr.bytecast.interfaces.fsys;
 import java.util.*;
 import java.io.*;
 
 public class ExeObj {
     
    //Returns the entry point of the program of the program
     public long getEntryPointIndex() {
         return m_entryPointIndex;
     }
      
    //Sets the entry point of the program
     public void setEntryPointIndex(long entry_point) {
         m_entryPointIndex = entry_point;
     }
     
    //returns all memory segments
     public List<ExeObjSegment> getSegments() {
         return m_segments;
     }
     
    //Returns a group of bytes from a virtual address
     public List<Byte> getBytesFromAddr(long start_addr, int size) throws Exception
     {
         for(int i = 0; i < m_segments.size(); i++)
         {
             if(start_addr >= m_segments.get(i).getStartAddress() &&
               start_addr+size <  m_segments.get(i).getEndAddress())
             {
                 int offset = (int)(start_addr - m_segments.get(i).getStartAddress());
                 return m_segments.get(i).getBytes(offset,size);
             }
         }
         throw new Exception("Address range not found in any segment.");
     }
     
    //Sets all segments
     public void setSegments(List<ExeObjSegment> segments) {
         m_segments=segments;
     }
  
    //Sets all functions
     public void setFunctions(List<ExeObjFunction> functions){
         m_functions = functions;
     }
     
    //Returns all functions. 
     public List<ExeObjFunction> getFunctions()
     {
         return m_functions;        
     }
     
     private long m_entryPointIndex;
     private List<ExeObjSegment> m_segments;
     private List<ExeObjFunction> m_functions;
 
 }
