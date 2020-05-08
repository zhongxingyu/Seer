 package edu.syr.bytecast.fsys;
 
 public class ELFSectionHeaderEntryStruct {
    int     sh_name;
    int     sh_type;
    int     sh_link;
    int     sh_info;
    long    sh_flags;
    long    sh_addr;
    long    sh_offset;
    long    sh_size;
    long    sh_addralign;
    long    sh_entsize;
 }
