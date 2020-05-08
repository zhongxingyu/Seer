 /*
  * This file is part of Bytecast.
  *
  * Bytecast is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Bytecast is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Bytecast.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package edu.syr.bytecast.fsys.elf;
 
 import edu.syr.bytecast.fsys.*;
 import edu.syr.bytecast.interfaces.fsys.*;
 import java.io.*;
 import java.util.*;
 
 public class ElfExeObjParser implements IBytecastFsys {
     
     ElfFileParser           m_elfFileParser;
     ElfSectionHeaderStruct  m_sectionHeaders;
     List<Byte>        m_stringTable;
     
     //Constructor
     public ElfExeObjParser()
     {
         m_preferSections = true;
     }
     
     //Constructor
     public ElfExeObjParser(boolean prefer_sections)
     {
         m_preferSections = prefer_sections;
     }
     
     //Sets the file path to parse
     @Override
     public void setFilepath(String file_path) {
         m_filePath = file_path;
     }
     
     //Gets the file path to parse
     @Override
     public String getFilepath() {
         return m_filePath;
     }
 
     //Determines whether sections or segments are desired fro an OBJ file
     //based on what's available and what view is preferred.
     private boolean wantSections(int num_sections, int num_segments) throws IOException
     {
         boolean ret = false;
         //return true if we prefer sections and we have them
         if(num_sections > 0 && m_preferSections)
             ret = true;
         //or if we have no segments
         if(num_sections > 0 && num_segments == 0)
             ret = true;
         
         if(num_sections <= 0 && num_segments <= 0)
             throw new IOException("Elf file contains no executable code"
                     + "(section header and segment header sizes are zero).");
         
         return ret;
     }
   
     
     //Public "parse" function.. parses the specified ELF file.
     @Override
     public ExeObj parse() throws Exception {
         m_elfFileParser = new ElfFileParser();
         m_elfFileParser.setFilePath(m_filePath);
         ExeObj ret = new ExeObj();
         
         //try to open the ELF file
         if(m_elfFileParser.attach())
         {
             //Pull the ELF header
             ElfElfHeaderStruct elf_header = m_elfFileParser.getElfHeader();
             ret.setEntryPointIndex(elf_header.e_entry);
             
             //Pull either the sections or segments
             if(wantSections(elf_header.e_shnum, elf_header.e_phnum))
             {
                 ElfSectionHeaderStruct sect_header = m_elfFileParser.getSectionHeader();
                 
                 //Generate the ExeObj segments from the sections
                 ret.setSegments(generateSegmentsFromSections(sect_header));
                 
             }
             else
             {
                 //Generate the ExeObj segments from the ELF segments
                 ret.setSegments(generateSegmentsFromSegments());
 
             }
  
             ret.setFunctions(generateFunctions());           
         }
         else
         {
             throw new FileNotFoundException(m_filePath + " could not be opened.");
         }
        
         return ret;
     }
 
 
     //Determines if a section is executable base on the eh_flags.
     private boolean isExecutableSection(long flags)
     {
         boolean ret = false;
         long mask = ElfSectionHeaderEntryStruct.SHF_ALLOC;
         
         if((flags & mask) != 0)
             ret = true;
         
         return ret;
     }
     
     //Determines if a segment is executable base on the p_type
     private boolean isExecutableSegment(int type)
     {
         boolean ret = false;
         if(type == ElfProgramHeaderEntryStruct.PT_LOAD)
             ret = true;
         
         return ret;
     }
     
     //Generates a label for the segment.
     private String getLabel(List<Byte> string_table, int string_id, int sh_index)
     {
         
         String ret = m_filePath + "_" + 
                      sh_index;
         
         if(string_id != -1 && string_table != null)
             ret += "_" + 
                    BytecastFsysUtil.parseStringFromBytes(string_table, string_id);
         
         return ret;
             
     }
     
     //Generates Segments from Section Headers.
     private List<ExeObjSegment> generateSegmentsFromSections(ElfSectionHeaderStruct sect_header) throws IOException
     {
         List<ExeObjSegment> ret = new ArrayList<ExeObjSegment>(); 
         
         List<Byte> sh_string_table = m_elfFileParser.getSectionStringTable();
 
         //Now find executable sections.
         for(int i = 0; i < sect_header.m_headerEntries.size(); i++)
         {
             ElfSectionHeaderEntryStruct entry = sect_header.m_headerEntries.get(i);
             if(isExecutableSection(entry.sh_flags))
             {
                 ExeObjSegment exe_seg = new ExeObjSegment();
                 exe_seg.setLabel(getLabel(sh_string_table, entry.sh_name, i));
                 exe_seg.setBytes(m_elfFileParser.getSection(i));
                 exe_seg.setStartAddress(entry.sh_addr);
                 ret.add(exe_seg);
             }
         }
         return ret;
     }
     
     //Generate the segments from the program header and segments.
     private List<ExeObjSegment> generateSegmentsFromSegments() throws IOException
     {
         ElfProgramHeaderStruct prog_header = m_elfFileParser.getProgramHeader();
         List<ExeObjSegment> ret = new ArrayList<ExeObjSegment>();
         
         for(int i = 0; i < prog_header.m_headerEntries.size(); i++)
         {
             ElfProgramHeaderEntryStruct entry = prog_header.m_headerEntries.get(i);
             if(isExecutableSegment(entry.p_type))
             {
                 ExeObjSegment exe_seg = new ExeObjSegment();
                 exe_seg.setLabel(getLabel(null, -1, i));
                 exe_seg.setBytes(m_elfFileParser.getSegment(i));
                 exe_seg.setStartAddress(entry.p_vaddr);
                 ret.add(exe_seg);               
             }
         }
         
         return ret;
     }
     
     private List<ExeObjFunction> generateFunctions() throws IOException
     {
        List<ExeObjFunction> ret = new ArrayList();
        
        ElfSymbolTableStruct sym_table = m_elfFileParser.getSymTable();
        List<Byte> str_table = m_elfFileParser.getMainStringTable();
        
        for(int i = 0; i < sym_table.m_symbolEntries.size();i++)
        {
            ElfSymbolTableEntryStruct entry = sym_table.m_symbolEntries.get(i);
            int st_info_masked = (int)entry.st_info&0x0f;
            
            if(st_info_masked == entry.STT_FUNC)
            {
                ExeObjFunction exObjFunc = new ExeObjFunction();
                
                exObjFunc.setName(BytecastFsysUtil.parseStringFromBytes(str_table, entry.st_name));
                exObjFunc.setSize((int)entry.st_size);
               exObjFunc.setStartAddress(entry.st_value);//m_elfFileParser.getSectionHeaderEntry(entry.st_shndx).sh_addr);
                
                ret.add(exObjFunc);
            }
        }
        return ret;
     }
     
 
     public static void main(String args[]) {
         ElfExeObjParser elf_parser = new ElfExeObjParser(true);
         //elf_parser.setFilepath("/home/adodds/code/bytecast-fsys/documents/testcase1_input_files/libc.so.6");
         //elf_parser.setFilepath("/lib32/libc.so.6");
 
         //elf_parser.setFilepath("../../documents/testcase1_input_files/a.out.static");
        elf_parser.setFilepath("../../../bytecast-documents/AsciiManip01Prototype/a.out.static");
         try {
            //ExeObj exeObj = elf_parser.parse();
            //ExeObjIOUtils.printExeObj(exeObj);
           // ExeObjIOUtils.writeToFile(exeObj, "/home/shawn/code/bytecast/bytecast-common/bytecast-common/test_input_files/fsys_mock1.eobj");
            ExeObj exeObj = ExeObjIOUtils.readFromFile( "/home/shawn/code/bytecast/bytecast-common/bytecast-common/test_input_files/fsys_mock1.eobj");
             ExeObjIOUtils.printExeObj(exeObj);
         } catch (FileNotFoundException e) {
             System.out.println("Could not parse file.");
         } catch (Exception e) {
             System.out.println(e.getLocalizedMessage());
         }
     }
 
     private String m_filePath;
     private boolean m_preferSections;
 }
