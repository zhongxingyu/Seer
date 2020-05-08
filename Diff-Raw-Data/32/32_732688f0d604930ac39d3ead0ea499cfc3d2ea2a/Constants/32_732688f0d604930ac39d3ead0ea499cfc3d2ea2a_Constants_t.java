 package edu.mit.wi.haploview;
 
 import java.util.Hashtable;
 
 
 public interface Constants {
 
     //main jframe setup stuff & labels.
     public static final String TITLE_STRING = "Haploview v2.04";
     public static final String READ_GENOTYPES = "Open genotype data";
     public static final String READ_MARKERS = "Load marker data";
 
     public static final String EXPORT_TEXT = "Export current tab to text";
     public static final String EXPORT_PNG = "Export current tab to PNG";
     public static final String EXPORT_OPTIONS = "Export options";
 
     public static final String CLEAR_BLOCKS = "Clear all blocks";
     public static final String CUST_BLOCKS = "Customize Block Definitions";
 
    public static final String VIEW_DPRIME = "LD Plot";
     public static final String VIEW_HAPLOTYPES = "Haplotypes";
     public static final String VIEW_LOD = "Genotyping completeness";
     public static final String VIEW_CHECK_PANEL = "Check Markers";
     public static final String VIEW_TDT = "Association";
 
     //main frame tab numbers
     public static final int VIEW_D_NUM = 0;
     public static final int VIEW_HAP_NUM = 1;
     public static final int VIEW_CHECK_NUM = 2;
     public static final int VIEW_TDT_NUM = 3;
 
     //export modes
     public static final int PNG_MODE = 0;
     public static final int TXT_MODE = 1;
     public static final int TABLE_TYPE = 0;
     public static final int LIVE_TYPE = 1;
 
     //block defs
     public static final int BLOX_GABRIEL = 0;
     public static final int BLOX_4GAM = 1;
     public static final int BLOX_SPINE = 2;
     public static final int BLOX_CUSTOM = 3;
     public static final int BLOX_ALL = 4;
     public static final int BLOX_NONE = 5;
 
     //filetypes
     static final int GENO = 0;
     static final int INFO = 1;
     static final int HAPS = 2;
     static final int PED = 3;
     static final int DCC = 4;
 }
