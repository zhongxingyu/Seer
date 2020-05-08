 /*
  * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
  * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
  * 
  * 	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pyromaniac;
 
 import java.util.HashMap;
 
 import pyromaniac.DataStructures.MIDPrimerCombo;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class AcaciaConstants.
  */
 public final class AcaciaConstants 
 {
 		
 		/** The Constant CONFIG_DELIMITER. */
 		public static final String CONFIG_DELIMITER = "=";
 		
 		//hidden constants [from GUI or parameterisation]
 		/** The Constant CLUSTERING_OLIGO_LENGTH. */
 		public static final int CLUSTERING_OLIGO_LENGTH = 6; 
 		
 		/** The Constant MAXIMUM_BIN. */
 		public static final int MAXIMUM_BIN = 16;		//maximum obs count.
 		
 		
 		//parameter names
 		/** The Constant OPT_FASTA. */
 		public static final String OPT_FASTA = "FASTA";
 		
 		/** The Constant OPT_FASTA_LOC. */
 		public static final String OPT_FASTA_LOC = "FASTA_LOCATION";
 		
 		/** The Constant OPT_QUAL_LOC. */
 		public static final String OPT_QUAL_LOC = "QUAL_LOCATION";
 		
 		/** The Constant OPT_FASTQ. */
 		public static final String OPT_FASTQ = "FASTQ";
 		
 		/** The Constant OPT_FASTQ_LOC. */
 		public static final String OPT_FASTQ_LOC = "FASTQ_LOCATION";
 		
 		/** The Constant OPT_MID. */
 		public static final String OPT_MID = "MID_OPTION";
 		
 		/** The Constant OPT_MID_FILE. */
 		public static final String OPT_MID_FILE = "MID_FILE";
 		
 		/** The Constant OPT_PERFORM_CALL_CORR. */
 		public static final String OPT_PERFORM_CALL_CORR = "PERFORM_ERROR_CORRECTION";
 		
 		/** The Constant OPT_TRIM_TO_LENGTH. */
 		public static final String OPT_TRIM_TO_LENGTH = "TRIM_TO_LENGTH";
 		
 		/** The Constant OPT_OUTPUT_PREFIX. */
 		public static final String OPT_OUTPUT_PREFIX = "OUTPUT_PREFIX";
 		
 		/** The Constant OPT_OUTPUT_DIR. */
 		public static final String OPT_OUTPUT_DIR = "OUTPUT_DIR";
 		
 		/** The Constant OPT_LOAD_MIDS. */
 		public static final String OPT_LOAD_MIDS = "LOAD_MIDS";
 		
 		/** The Constant OPT_ROCHE_5MID. */
 		public static final String OPT_ROCHE_5MID = "ROCHE_5MID";
 		
 		/** The Constant OPT_ROCHE_10MID. */
 		public static final String OPT_ROCHE_10MID = "ROCHE_10MID";
 		
 		/** The Constant OPT_NO_MID. */
 		public static final String OPT_NO_MID = "NO_MID";
 		
 		/** The Constant OPT_SIGNIFICANCE_LEVEL. */
 		public static final String OPT_SIGNIFICANCE_LEVEL = "SIGNIFICANCE_LEVEL";
 		
 		/** The Constant OPT_REPRESENTATIVE_SEQ. */
 		public static final String OPT_REPRESENTATIVE_SEQ = "REPRESENTATIVE_SEQUENCE";
 		
 		/** The Constant OPT_MIN_AVG_QUALITY. */
 		public static final String OPT_MIN_AVG_QUALITY = "AVG_QUALITY_CUTOFF";
 		
 		/** The Constant OPT_MODE_REPRESENTATIVE. */
 		public static final String OPT_MODE_REPRESENTATIVE = "Mode";
 		
 		/** The Constant OPT_MAX_REPRESENTATIVE. */
 		public static final String OPT_MAX_REPRESENTATIVE = "Max";
 		
 		/** The Constant OPT_MIN_REPRESENTATIVE. */
 		public static final String OPT_MIN_REPRESENTATIVE = "Min";
 		
 		/** The Constant OPT_MEDIAN_REPRESENTATIVE. */
 		public static final String OPT_MEDIAN_REPRESENTATIVE = "Median"; 
 		
 		/** The Constant OPT_SPLIT_ON_MID. */
 		public static final String OPT_SPLIT_ON_MID = "SPLIT_ON_MID";
 		
 		/** The Constant OPT_MAX_STD_DEV_LENGTH. */
 		public static final String OPT_MAX_STD_DEV_LENGTH = "MAX_STD_DEV_LENGTH";
 		
 		/** The Constant OPT_ERROR_MODEL. */
 		public static final String OPT_ERROR_MODEL = "ERROR_MODEL";
 		
 		/** The Constant OPT_FLOWSIM_ERROR_MODEL. */
 		public static final String OPT_FLOWSIM_ERROR_MODEL = "Balzer";
 		
 		/** The Constant OPT_PYRONOISE_ERROR_MODEL. */
 		public static final String OPT_PYRONOISE_ERROR_MODEL = "Quince";
 		
 		/** The Constant OPT_FILTER_READS_WITH_N_BEFORE_POS. */
 		public static final String OPT_FILTER_READS_WITH_N_BEFORE_POS = "FILTER_N_BEFORE_POS";
 		
 		
 		/** The Constant OPT_ACACIA_TITANIUM_ERROR_MODEL. */
 		public static final String OPT_ACACIA_TITANIUM_ERROR_MODEL = "Acacia_Emp_Titanium";
 		
 		/** The Constant OPT_FLOW_KEY. */
 		public static final String OPT_FLOW_KEY = "FLOW_KEY";	
 		
 		/** The Constant OPT_MAX_RECURSE_DEPTH. */
 		public static final String OPT_MAX_RECURSE_DEPTH = "MAX_RECURSE_DEPTH";
 		
 		/** The Constant OPT_MAXIMUM_MANHATTAN_DIST. */
 		public static final String OPT_MAXIMUM_MANHATTAN_DIST = "MAXIMUM_MANHATTAN_DISTANCE";
 		
 		/** The Constant OPT_TRUNCATE_CONSENSUS_TO_FLOW. */
 		public static final String OPT_TRUNCATE_READ_TO_FLOW = "TRUNCATE_READ_TO_FLOW";
 		
 		/** The Constant OPT_MIN_FLOW_TRUNCATION. */
 		public static final String OPT_MIN_FLOW_TRUNCATION = "MIN_FLOW_TRUNCATION"; //paired with the percentage of reads covering blah
 		
 		/** The Constant OPT_MIN_READ_REP_BEFORE_TRUNCATION. */
 		public static final String OPT_MIN_READ_REP_BEFORE_TRUNCATION = "MIN_READ_REP_BEFORE_TRUNCATION";
 		
 		
 		//parameter default values
 		/** The Constant DEFAULT_OPT_FASTA. */
 		public static final String DEFAULT_OPT_FASTA = "TRUE";
 		
 		/** The Constant DEFAULT_OPT_FASTQ. */
 		public static final String DEFAULT_OPT_FASTQ = "FALSE";
 		
 		/** The Constant DEFAULT_OPT_TRIM_LENGTH. */
 		public static final String DEFAULT_OPT_TRIM_LENGTH = "";
 		
 		/** The Constant DEFAULT_OPT_MID. */
 		public static final String DEFAULT_OPT_MID = OPT_NO_MID;
 		
 		/** The Constant DEFAULT_ALLOW_LOOKAHEAD. */
 		public static final String DEFAULT_ALLOW_LOOKAHEAD = "FALSE";
 		
 		/** The Constant DEFAULT_OPT_LOOKAHEAD. */
 		public static final String DEFAULT_OPT_LOOKAHEAD = "TRUE";
 		
 		/** The Constant DEFAULT_OPT_PERFORM_CALL_CORR. */
 		public static final String DEFAULT_OPT_PERFORM_CALL_CORR = "TRUE";
 		
 		/** The Constant DEFAULT_OPT_SIGNIFICANCE_LEVEL. */
 		public static final String DEFAULT_OPT_SIGNIFICANCE_LEVEL = "-9";
 		
 		/** The Constant DEFAULT_OPT_REPRESENTATIVE_SEQ. */
 		public static final String DEFAULT_OPT_REPRESENTATIVE_SEQ = OPT_MODE_REPRESENTATIVE;
 		
 		/** The Constant DEFAULT_OPT_MIN_AVG_QUALITY. */
 		public static final String DEFAULT_OPT_MIN_AVG_QUALITY = "30";
 		
 		/** The Constant DEFAULT_OPT_SPLIT_ON_MID. */
 		public static final String DEFAULT_OPT_SPLIT_ON_MID = "FALSE";
 		
 		/** The Constant DEFAULT_OPT_MAX_STD_DEV_LENGTH. */
 		public static final String DEFAULT_OPT_MAX_STD_DEV_LENGTH = "2";
 		
 		/** The Constant DEFAULT_OPT_ERROR_MODEL. */
 		public static final String DEFAULT_OPT_ERROR_MODEL = OPT_FLOWSIM_ERROR_MODEL;
 		
 		/** The Constant DEFAULT_OPT_FLOW_KEY. */
 		public static final String DEFAULT_OPT_FLOW_KEY = "TCAG";
 		
 		/** The Constant DEFAULT_OPT_TRUNCATE_CONSENSUS_TO_FLOW. */
 		public static final String DEFAULT_OPT_TRUNCATE_READ_TO_FLOW= "";
 		
 		/** The Constant DEFAULT_OPT_MIN_FLOW_TRUNCATION. */
 		public static final String DEFAULT_OPT_MIN_FLOW_TRUNCATION = "150"; //estimated flow.
 		
 		/** The Constant DEFAULT_OPT_MIN_READ_REP_BEFORE_TRUNCATION. */
 		public static final String DEFAULT_OPT_MIN_READ_REP_BEFORE_TRUNCATION = "0.0"; //this is the default
 		
 		/** The Constant DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST. */
 		public static final String DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST = "13";
 		
 		/** The Constant DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST_ALIGN. */
 		public static final String DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST_ALIGN = "21"; 
 		
 		/** The Constant DEFAULT_OPT_TRIM_COLLAPSED. */
 		public static final int DEFAULT_OPT_TRIM_COLLAPSED = 50;
 		
 		/** The Constant DEFAULT_OPT_MAX_RECURSE_DEPTH. */
 		public static final String DEFAULT_OPT_MAX_RECURSE_DEPTH = "2";
 		
 		/** The Constant DEFAULT_FILTER_N_BEFORE_POS. */
 		public static final String DEFAULT_FILTER_N_BEFORE_POS = "350";
 		
 		
 		/** The Constant DEFAULT_FILE_LOC. */
 		public static final String DEFAULT_FILE_LOC;
 		
 		/** The Constant DEFAULT_OPT_MID_LOC. */
 		public static final String DEFAULT_OPT_MID_LOC;
 		
 		/** The Constant DEFAULT_OPT_OUTPUT_PREFIX. */
 		public static final String DEFAULT_OPT_OUTPUT_PREFIX;
 
 		/** The Constant ROCHE_10MID_FILE. */
 		public static final String ROCHE_10MID_FILE;
 		
 		/** The Constant ROCHE_5MID_FILE. */
 		public static final String ROCHE_5MID_FILE;
 		
 		/** The Constant PYRONOISE_PROBS_LOCATION. */
 		public static final String PYRONOISE_PROBS_LOCATION;
 		
 		/** The Constant FLOWSIM_PROBS_LOCATION. */
 		public static final String FLOWSIM_PROBS_LOCATION;
 		
 		/** The Constant FLOWSIM_PROBS_LOCATION. */
 		public static final String ACACIA_EMP_MODEL_TITANIUM_LOCATION;
 		
 		
 		//IO constants
 		/** The Constant STANDARD_OUT_NAME. */
 		public static final String STANDARD_OUT_NAME;
 		
 		/** The Constant STANDARD_ERR_NAME. */
 		public static final String STANDARD_ERR_NAME;
 		
 		/** The Constant STANDARD_DEBUG_NAME. */
 		public static final String STANDARD_DEBUG_NAME;
 		
 		
 		/** The Constant STATS_SUFFIX. */
 		public static final String STATS_SUFFIX = "stats";
 		
 		/** The Constant SEQOUT_SUFFIX. */
 		public static final String SEQOUT_SUFFIX = "seqOut";
 		
 		/** The Constant REFOUT_SUFFIX. */
 		public static final String REFOUT_SUFFIX = "refOut";
 		
 		/** The Constant MAPOUT_SUFFIX. */
 		public static final String MAPOUT_SUFFIX = "mapOut";
 		
 		/** The Constant HISTOUT_SUFFIX. */
 		public static final String HISTOUT_SUFFIX = "histOut";
 		
 		/** The Constant STAT_OUT_FILE. */
 		public  static final String STAT_OUT_FILE = "STATOUT";
 		
 		/** The Constant SEQ_OUT_FILE. */
 		public static final String SEQ_OUT_FILE = "SEQOUT";
 		
 		/** The Constant REF_OUT_FILE. */
 		public static final String REF_OUT_FILE = "REFOUT";
 		
 		/** The Constant MAP_OUT_FILE. */
 		public static final String MAP_OUT_FILE = "MAPOUT";
 		
 		/** The Constant HIST_OUT_FILE. */
 		public static final String HIST_OUT_FILE = "HISTOUT";
 		
 		//GUI constants
 		/** The Constant MENU_STRING_EXIT. */
 		public static final String MENU_STRING_EXIT = "Quit";
 		
 		/** The Constant MENU_PROGRAM_INFO. */
 		public static final String MENU_PROGRAM_INFO = "Program Info";
 		
 	
 		//Code Constants
 		/** The Constant SIGN_THRESHOLD_ZERO. */
 		public static final String SIGN_THRESHOLD_ZERO = "-Inf";
 		
 		/** The Constant IUPAC_AMBIGUOUS_MAPPINGS. */
 		public static final HashMap <Character, char []> IUPAC_AMBIGUOUS_MAPPINGS = new HashMap<Character, char [] >();
 		
 		/** The Constant NO_MID_GROUP. */
 		public static final MIDPrimerCombo NO_MID_GROUP = new MIDPrimerCombo("", "", "all_tags"); //changing it to Titanium to be like Denoiser
 
 		
 		//should avoid the majority of introduced deletions observed in previous versions.
 		/** The Constant OPT_SIGNIFICANT_WHEN_TWO. */
 		public static final String OPT_SIGNIFICANT_WHEN_TWO = "ANY_DIFF_SIGNIFICANT_FOR_TWO_SEQS";
 		
 		
 		/** The Constant DEFAULT_OPT_SIGNIFICANT_WHEN_TWO. */
 		public static final String DEFAULT_OPT_SIGNIFICANT_WHEN_TWO = "TRUE";
 
 		
 		
 		static
 		{
 			ROCHE_10MID_FILE = null;
 			ROCHE_5MID_FILE = null;
 			PYRONOISE_PROBS_LOCATION = "/data/QuinceProbs.csv";
 			FLOWSIM_PROBS_LOCATION = "/data/maldeEmpiricalDistributions.csv"; 
 			ACACIA_EMP_MODEL_TITANIUM_LOCATION = "/data/titanium_emp_nuc.csv";
 			IUPAC_AMBIGUOUS_MAPPINGS.put('R', new char [] {'A', 'G'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('Y', new char [] {'C', 'T'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('S', new char [] {'G', 'C'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('W', new char [] {'A', 'T'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('K', new char [] {'G', 'T'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('M', new char [] {'A', 'C'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('B', new char [] {'C', 'G', 'T'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('D', new char [] {'A', 'G', 'T'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('H', new char [] {'A', 'C', 'T'});
 			IUPAC_AMBIGUOUS_MAPPINGS.put('V', new char [] {'A', 'C', 'G'});	
 			IUPAC_AMBIGUOUS_MAPPINGS.put('W', new char[] {'A', 'C', 'G', 'T'});
 			
 			STANDARD_OUT_NAME = "acacia_standard_output.txt";
 			STANDARD_ERR_NAME = "acacia_standard_error.txt";
 			STANDARD_DEBUG_NAME = "acacia_standard_debug.txt";
 
 			DEFAULT_FILE_LOC = null;
 			
 			DEFAULT_OPT_MID_LOC = "data" + AcaciaMain.getPlatformSpecificPathDivider()
 			+ "ROCHE_5BASE_ACACIA.mids";
			DEFAULT_OPT_OUTPUT_PREFIX = "acacia_out";
 		}
 		
 	  /**
   	 * Instantiates a new acacia constants.
   	 */
   	private AcaciaConstants()
 	  {
 		    //this prevents even the native class from 
 		    //calling this ctor as well :
 		  throw new AssertionError();
 	  }
 }
