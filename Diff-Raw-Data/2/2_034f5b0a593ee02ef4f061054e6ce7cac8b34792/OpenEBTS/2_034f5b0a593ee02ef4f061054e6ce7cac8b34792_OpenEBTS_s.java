 package com.obi;
 
 import java.util.ArrayList;
 
 public class OpenEBTS
 {
 	//static 
	{
 		System.loadLibrary("OpenEBTS");
 	}
 
 	// OpenEBTS currently only support binary output
 	enum NISTFileFormat
 	{
 		fileFormatBinary,
 		fileFormatXML
 	}
 
 	// Useful for the imaging functions
 	public static final int IMAGEFORMAT_RAW   = 0;
 	public static final int IMAGEFORMAT_BMP   = 1; 
 	public static final int IMAGEFORMAT_JPG   = 2; 
 	public static final int IMAGEFORMAT_WSQ   = 3;
 	public static final int IMAGEFORMAT_JP2   = 4;
 	public static final int IMAGEFORMAT_FX4   = 5;
 	public static final int IMAGEFORMAT_CBEFF = 6;
 	public static final int IMAGEFORMAT_PNG   = 7;
 
 	// All OpenEBTS warning and error codes
 	public static final int IW_SUCCESS =                                 0;
 	public static final int IW_ERR_LOADING_VERICATION =                  1;
 	public static final int IW_ERR_OPENING_FILE_FOR_READING =            2;
 	public static final int IW_ERR_OPENING_FILE_FOR_WRITING =            3;
 	public static final int IW_ERR_READING_FILE =                        4;
 	public static final int IW_ERR_WRITING_FILE =                        5;
 	public static final int IW_ERR_RECORD_NOT_FOUND =                    6;
 	public static final int IW_ERR_TRANSACTION_NOT_LOADED =              7;
 	public static final int IW_ERR_VERIFICATION_NOT_LOADED =             8;
 	public static final int IW_ERR_NULL_POINTER =                        9;
 	public static final int IW_ERR_NULL_TRANSACTION_POINTER =           10;
 	public static final int IW_ERR_UNSUPPORTED_IMAGE_FORMAT =           11;
 	public static final int IW_ERR_UNSUPPORTED_RECORD_TYPE =            12;
 	public static final int IW_ERR_INDEX_OUT_OF_RANGE =                 13;
 	public static final int IW_ERR_INVALID_SUBFIELD_NUM =               14;
 	public static final int IW_ERR_MNEMONIC_NOT_FOUND =                 15;
 	public static final int IW_ERR_OUT_OF_MEMORY =                      16;
 	public static final int IW_ERR_WSQ_COMPRESS =                       17;
 	public static final int IW_ERR_WSQ_DECOMPRESS =                     18;
 	public static final int IW_ERR_IMAGE_CONVERSION =                   19;
 	public static final int IW_ERR_HEADER_ITEM =                        20;
 	public static final int IW_ERR_UNSUPPORTED_BIT_DEPTH =              21;
 	public static final int IW_WARN_TRANSACTION_FAILED_VERIFICATION = 1000;	 
 	public static final int IW_WARN_INVALID_FIELD_NUM =               1001;
 	public static final int IW_WARN_REQ_FIELD_MISSING =               1002;
 	public static final int IW_WARN_INCORRECT_ITEM_COUNT =            1003;
 	public static final int IW_WARN_TOO_FEW_DATA_CHARS =              1004;
 	public static final int IW_WARN_TOO_MANY_DATA_CHARS =             1005;
 	public static final int IW_WARN_DATA_NOT_NUMERIC =                1006;
 	public static final int IW_WARN_DATA_NOT_ALPHA =                  1007;
 	public static final int IW_WARN_DATA_NOT_ALPHANUMERIC =           1008;
 	public static final int IW_WARN_TOO_FEW_SUBFIELDS =               1009;
 	public static final int IW_WARN_TOO_MANY_SUBFIELDS =              1010;
 	public static final int IW_WARN_UNSUPPORT_FIELD_PRESENT =         1011;
 	public static final int IW_WARN_TRANSACTION_FAILED_XML_PARSE =    1012;
 	public static final int IW_WARN_DATA_NOT_NUMERIC_SPECIAL =        1013;
 	public static final int IW_WARN_DATA_NOT_ALPHA_SPECIAL =          1014;
 	public static final int IW_WARN_DATA_NOT_ALPHANUMERIC_SPECIAL =   1015;
 	public static final int IW_WARN_INVALID_DATE =                    1016;
 	public static final int IW_WARN_INVALID_DATA =                    1017;
 	public static final int IW_WARN_DATA_NOT_PRINT =                  1018;
 	public static final int IW_WARN_DATA_NOT_PRINTCTRL =              1019;
 	public static final int IW_WARN_REQ_RECORD_MISSING =              1020;
 	public static final int IW_WARN_UNSUPPORT_RECORD_PRESENT =        1021;
 	public static final int IW_WARN_UNKNOWN_TOT	=                     1022;
 
 	// Return value for IWGetRuleRestrictions, contains all field-creation rules
 	class NISTFieldRules
 	{
 		String	_sMNU;
 		int		_nRecordType;
 		int		_nField;
 		int		_nSubfield;
 		int		_nItem;
 		String	_sName;
 		String	_sDescription;
 		String	_sCharType;
 		String	_sSpecialChars;
 		String	_sDateFormat;
 		String	_sAdvancedRule;
 		int		_nSizeMin;
 		int		_nSizeMax;
 		int		_nOccurrencesMin;
 		int		_nOccurrencesMax;
 		int		_nOffset;
 		boolean	_bAutomaticallySet;
 		boolean	_bMandatory;
 	}
 
 	// Return value for IWGetValue list, is simply a list of name/value pairs,
 	// plus a mandatory/optional flag
 	class NISTValueList
 	{
 		boolean 	_bMandatory;
 		int			_nCount;
 		String[]	_saName;	
 		String[]	_saValue;	
 	}
 
 	//
 	// This object is used to handle the regular OpenEBTS integer return type
 	// of all the IW* functions.
 	//
 	class NISTReturn
 	{
 		int nRet;
 	}
 
 	// OpenEBTS functions dealing with NIST files
 	public native long IWNew(String sTOT, NISTReturn ret);
 	public native long IWReadFromFile(String sPath, long nVerification, NISTReturn ret);
 	public native long IWReadMem(byte[] mem, long nVerification, NISTReturn ret);
 	public native void IWWriteToFile(long nTransaction, String sPath, NISTFileFormat fmt, NISTReturn ret);
 	public native void IWClose(long nTransaction, NISTReturn ret);
 	public native int IWAddRecord(long nTransaction, int nRecordType, NISTReturn ret);
 	public native void IWDeleteRecord(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	public native int IWGetRecordTypeCount(long nTransaction, int nRecordType, NISTReturn ret);
 	public native int IWGetNumRecords(long nTransaction, NISTReturn ret);
 	public native void IWSetDataViaMnemonic(long nTransaction, String sMnemonic, int nRecordIndex,
 			int nSecondaryIndex, String sData, NISTReturn ret);
 	public native String IWGetDataViaMnemonic(long nTransaction, String sMnemonic, int nRecordIndex,
 			int nSecondaryIndex, NISTReturn ret);
 	public native void IWSetImage(long nTransaction, int nRecordType, int nRecordIndex, byte image[],
 			int nfmtIn, int nfmtOut, int nCompression, NISTReturn ret);
 	public native byte[] IWGetImage(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	public native int IWGetImageFormat(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	public native int IWGetImageWidth(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	public native int IWGetImageHeight(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	public native int IWGetImageDepth(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	public native void IWSetImageFromFile(long nTransaction, int nRecordType, int nRecordIndex, String sPath,
 			int nfmtIn, int nfmtOut, int nCompression, NISTReturn ret);
 	public native byte[] IWGetImageAs(long nTransaction, int nRecordType, int nRecordIndex, int nFmtOut, NISTReturn ret);
 	public native void IWGetImageAsToFile(long nTransaction, int nRecordType, int nRecordIndex, String sPath, int nFmtOut,
 			NISTReturn ret);
 	public native long IWReadVerification(String sPath, StringBuffer sParseErrorOut, NISTReturn ret);
 	public native void IWCloseVerification(long nVerification, NISTReturn ret);
 	public native void IWSetVerification(long nTransaction, long nVerification, NISTReturn ret);
 	public native void IWVerify(long nTransaction, NISTReturn ret);
 	public native int IWGetErrorCount(long nTransaction, NISTReturn ret);
 	public native String IWGetErrorString(long nTransaction, int nIndex, NISTReturn ret);
 	public native int IWGetErrorCode(long nTransaction, int nIndex, NISTReturn ret);
 	private native int IWGetFieldCount(long nTransaction, int nRecordType, int nRecordIndex, NISTReturn ret);
 	private native int IWGetNextField(long nTransaction, int nRecordType, int nRecordIndex, int nField, NISTReturn ret);
 	private native int IWNumSubfields(long nTransaction, int nRecordType, int nRecordIndex, int nFieldIndex, NISTReturn ret);
 	private native int IWNumItems(long nTransaction, int nRecordType, int nRecordIndex, int nFieldIndex,
 			int nSubfieldIndex, NISTReturn ret);
 	private native String IWFindItem(long nTransaction, int nRecordType, int nRecordIndex,
 			 int nFieldIndex, int nSubfieldIndex, int nItemIndex, NISTReturn ret);
 	private native void IWSetItem(long nTransaction, String sData, int nRecordType, int nRecordIndex,
 			int nFieldIndex, int nSubfieldIndex, int nlItemIndex, NISTReturn ret);
 	// OpenEBTS functions dealing with Verification files
 	private native String[] IWGetTransactionCategories(long nVerification, NISTReturn ret);
 	private native String[] IWGetTransactionTypeNames(long nVerification, String sCategory, NISTReturn ret);
 	private native String[] IWGetTransactionTypeDescriptions(long nVerification, String sCategory, NISTReturn ret);
 	private native int[][] IWGetRecordTypeOccurrences(long nVerification, String sTOT, NISTReturn ret);
 	private native String[] IWGetMnemonicsNames(long nVerification, String sTOT, NISTReturn ret);
 	private native String[] IWGetMnemonicsDescriptions(long nVerification, String sTOT, NISTReturn ret);
 	private native NISTFieldRules IWGetRuleRestrictions(long nVerification, String sTOT, String sMNU, NISTReturn ret);
 	private native NISTValueList IWGetValueList(long nVerification, String sTOT, String sMNU, NISTReturn _ret);
 
 	
 	//
 	// NISTFile is the topmost class in OpenEBTS representing
 	// a NIST transaction file.
 	//
 	public class NISTFile
 	{
 		private String _sTOT;
 		private long _nTransaction = 0;
 		private long _nVerification = 0;
 		private NISTReturn _ret = new NISTReturn();
 
 		public NISTFile()
 		{
 			_sTOT = "";
 		}
 
 		public NISTFile(String sTOT)
 		{
 			newTransaction(sTOT);
 		}
 
 		protected void finalize ()
 		{
 			if (_nTransaction != 0) IWClose(_nTransaction, _ret);
 		}
 
 		// Basic I/O
 
 		public String getTransactionType()
 		{
 			return _sTOT;
 		}
 
 		public int newTransaction(String sTOT)
 		{
 			if (_nTransaction != 0) IWClose(_nTransaction, _ret);
 			_nTransaction = IWNew(sTOT, _ret);
 			_sTOT = sTOT;
 			return _ret.nRet;
 		}
 
 		public int readFromFile(String sPath)
 		{
 			_nTransaction =  IWReadFromFile(sPath, _nVerification, _ret);
 			return _ret.nRet;
 		}
 
 		public int readFromMem(byte[] mem){
 			_nTransaction = IWReadMem(mem, _nVerification, _ret);
 			return _ret.nRet;
 		}
 
 		public int writeToFile(String sPath, NISTFileFormat fmt)
 		{
 			IWWriteToFile(_nTransaction, sPath, fmt, _ret);
 			return _ret.nRet;
 		}
 
 		public int writeToFile(String sPath)
 		{
 			return writeToFile(sPath, NISTFileFormat.fileFormatBinary);
 		}
 
 		// Content Editing, Records
 
 		public int addRecord(int nRecordType)
 		{
 			return IWAddRecord(_nTransaction, nRecordType, _ret);
 		}
 
 		public void deleteRecord(int nRecordType, int nRecordIndex)
 		{
 			IWDeleteRecord(_nTransaction, nRecordType, nRecordIndex, _ret);
 		}
 
 		public int getRecordCountofType(int nRecordType)
 		{
 			return IWGetRecordTypeCount(_nTransaction, nRecordType, _ret);
 		}
 
 		public NISTRecord getRecordOfType(int nRecordType, int nRecordIndex)
 		{
 			return new NISTRecord(_nTransaction, nRecordType, nRecordIndex);
 		}
 
 		// Content Editing, Data within Records
 
 		public int setDataViaMnemonic(String sMnemonic, int nRecordIndex, int nSecondaryIndex, String sData)
 		{
 			IWSetDataViaMnemonic(_nTransaction, sMnemonic, nRecordIndex, nSecondaryIndex, sData, _ret);
 			return _ret.nRet;
 		}
 
 		public String getDataViaMnemonic(String sMnemonic, int nRecordIndex, int nSecondaryIndex)
 		{
 			return IWGetDataViaMnemonic(_nTransaction, sMnemonic, nRecordIndex, nSecondaryIndex, _ret);
 		}
 
 		public int setImage(int nRecordType, int nRecordIndex, byte image[],
 							int nFmtIn, int nFmtOut, int nCompression)
 		{
 			IWSetImage(_nTransaction, nRecordType, nRecordIndex, image, nFmtIn, nFmtOut, nCompression, _ret);
 			return _ret.nRet;
 		}
 
 		public byte[] getImage(int nRecordType, int nRecordIndex)
 		{
 			return IWGetImage(_nTransaction, nRecordType, nRecordIndex, _ret);
 		}
 
 		public byte[] getImageAs(int nRecordType, int nRecordIndex, int nFmtOut)
 		{
 			return IWGetImageAs(_nTransaction, nRecordType, nRecordIndex, nFmtOut, _ret);
 		}
 
 		public int getImageAsToFile(int nRecordType, int nRecordIndex, int nFmtOut, String sPath)
 		{
 			IWGetImageAsToFile(_nTransaction, nRecordType, nRecordIndex, sPath, nFmtOut, _ret);
 			return _ret.nRet;
 		}
 
 		public int getImageFormat(int nRecordType, int nRecordIndex)
 		{
 			return IWGetImageFormat(_nTransaction, nRecordType, nRecordIndex, _ret);
 		}
 
 		public int getImageWidth(int nRecordType, int nRecordIndex)
 		{
 			return IWGetImageWidth(_nTransaction, nRecordType, nRecordIndex, _ret);
 		}
 
 		public int getImageHeight(int nRecordType, int nRecordIndex)
 		{
 			return IWGetImageHeight(_nTransaction, nRecordType, nRecordIndex, _ret);
 		}
 
 		public int getImageDepth(int nRecordType, int nRecordIndex)
 		{
 			return IWGetImageDepth(_nTransaction, nRecordType, nRecordIndex, _ret);
 		}
 
 		public int setImageFromFile(int nRecordType, int nRecordIndex, String sPath,
 									int nFmtIn, int nFmtOut, int nCompression)
 		{
 			IWSetImageFromFile(_nTransaction, nRecordType, nRecordIndex, sPath, nFmtIn, nFmtOut, nCompression, _ret);
 			return _ret.nRet;
 		}
 
 		// Verification
 
 		public int associateVerificationFile(String sPath, StringBuffer sbParseErrorOut)
 		{
 			// IWReadVerification uses a StringBuffer to return possible parsing errors,
 			// and we use this same logic with associateVerificationFile
 			_nVerification = IWReadVerification(sPath, sbParseErrorOut, _ret);
 			if (_ret.nRet == 0)
 			{
 				IWSetVerification(_nTransaction, _nVerification, _ret);
 			}
 			return _ret.nRet;
 		}
 
 		public int associateVerificationFile(NISTVerification ver)
 		{
 			IWSetVerification(_nTransaction, ver._nVerification, _ret);
 			if (_ret.nRet == 0)
 			{
 				_nVerification = ver._nVerification;
 			}
 			return _ret.nRet;
 		}
 
 		public int verify()
 		{
 			IWVerify(_nTransaction, _ret);
 			return _ret.nRet;
 		}
 
 		// Error Reporting
 
 		public int getErrorCount()
 		{
 			return IWGetErrorCount(_nTransaction, _ret);
 		}
 
 		public int getErrorCode(int nIndex)
 		{
 			return IWGetErrorCode(_nTransaction, nIndex, _ret);
 		}
 
 		public String getErrorString(int nIndex)
 		{
 			return IWGetErrorString(_nTransaction, nIndex, _ret);
 		}
 	}
 
 	//
 	// NISTRecord is a virtual representation of a NIST record
 	// that is defined by the triplet transaction-type-index.
 	//
 	public class NISTRecord
 	{
 		private long _nTransaction;
 		private int _nRecordType;
 		private int _nRecordIndex;
 		private NISTReturn _ret = new NISTReturn();
 
 		public NISTRecord(long nTransaction, int nRecordType, int nRecordIndex)
 		{
 			_nTransaction = nTransaction;
 			_nRecordType = nRecordType;
 			_nRecordIndex = nRecordIndex;
 		}
 
 		public int getRecordType() { return _nRecordType; }
 		public int getRecordIndex() { return _nRecordIndex; }
 
 		// Support simple enumeration of all NISTFields
 		public int getFieldCount()
 		{
 			return IWGetFieldCount(_nTransaction, _nRecordType, _nRecordIndex, _ret);
 		}
 
 		public NISTField getField(int nIndex)
 		// Since the fields are not sequenced we have to use the IWGetNextField API.
 		// Note that nField and nIndex are not necessarily the same. nField is our way of ordering the list of fields
 		// from 1 to N, but nField, although an ordered list, is not necessarily sequential (i.e., it can have empty slots).
 		{
 			int nField = 0;
 			int nFieldCount = 0;
 			boolean bFound = false;
 
 			nField = IWGetNextField(_nTransaction, _nRecordType, _nRecordIndex, nField, _ret);
 			while (nField != 0)
 			{
 				nFieldCount++;
 
 				if (nFieldCount == nIndex)
 				{
 					bFound = true;
 					break;
 				}
 				nField = IWGetNextField(_nTransaction, _nRecordType, _nRecordIndex, nField, _ret);
 			}
 
 			if (bFound)
 			{
 				return new NISTField(_nTransaction, _nRecordType, _nRecordIndex, nField);
 			}
 			else
 			{
 				return null;
 			}
 		}
 	}
 
 	//
 	// NISTField is a virtual representation of a NIST field that is
 	// defined by the quadruplet transaction-type-index-fieldnum.
 	//
 	public class NISTField
 	{
 		private long _nTransaction;
 		private int _nRecordType;
 		private int _nRecordIndex;
 		private int _nFieldIndex;
 		private NISTReturn _ret = new NISTReturn();
 	
 		public NISTField(long nTransaction, int nRecordType, int nRecordIndex, int nFieldIndex)
 		{
 			_nTransaction = nTransaction;
 			_nRecordType = nRecordType;
 			_nRecordIndex = nRecordIndex;
 			_nFieldIndex = nFieldIndex;
 		}
 	
 		public int getRecordType() { return _nRecordType; }
 		public int getRecordIndex() { return _nRecordIndex; }
 		public int getFieldIndex() { return _nFieldIndex; }
 	
 		public int getSubfieldCount()
 		{
 			return IWNumSubfields(_nTransaction, _nRecordType, _nRecordIndex, _nFieldIndex, _ret);
 		}
 	
 		public NISTSubfield getSubfield(int nIndex)
 		{
 			return new NISTSubfield(_nTransaction, _nRecordType, _nRecordIndex, _nFieldIndex, nIndex);
 		}
 	}
 
 	//
 	// NISTField is a virtual representation of a NIST field that is defined
 	// by the quintuplet transaction-type-index-fieldnum-subfieldnum.
 	//
 	public class NISTSubfield
 	{
 		private long _nTransaction;
 		private int _nRecordType;
 		private int _nRecordIndex;
 		private int _nFieldIndex;
 		private int _nSubfieldIndex;
 		private NISTReturn _ret = new NISTReturn();
 		
 		public NISTSubfield(long nTransaction, int nRecordType, int nRecordIndex, int nFieldIndex, int nSubfieldIndex)
 		{
 			_nTransaction = nTransaction;
 			_nRecordType = nRecordType;
 			_nRecordIndex = nRecordIndex;
 			_nFieldIndex = nFieldIndex;
 			_nSubfieldIndex = nSubfieldIndex;
 		}
 
 		public int getRecordType() { return _nRecordType; }
 		public int getRecordIndex() { return _nRecordIndex; }
 		public int getFieldIndex() { return _nFieldIndex; }
 		public int getSubfieldIndex() { return _nSubfieldIndex; }
 
 		public int getItemCount()
 		{
 			return IWNumItems(_nTransaction, _nRecordType, _nRecordIndex, _nFieldIndex, _nSubfieldIndex, _ret);
 		}
 
 		public NISTItem getItem(int nIndex)
 		{
 			return new NISTItem(_nTransaction, _nRecordType, _nRecordIndex, _nFieldIndex, _nSubfieldIndex, nIndex);
 		}
 	}
 
 	//
 	// NISTItem is a virtual representation of a NIST field that is defined
 	// by the sextuplet transaction-type-index-fieldnum-subfieldnum-itemnum.
 	//
 	public class NISTItem
 	{
 		private long _nTransaction;
 		private int _nRecordType;
 		private int _nRecordIndex;
 		private int _nFieldIndex;
 		private int _nSubfieldIndex;
 		private int _nItemIndex;
 		private NISTReturn _ret = new NISTReturn();
 		
 		public NISTItem(long nTransaction, int nRecordType, int nRecordIndex, int nFieldIndex, int nSubfieldIndex, int nItemIndex)
 		{
 			_nTransaction = nTransaction;
 			_nRecordType = nRecordType;
 			_nRecordIndex = nRecordIndex;
 			_nFieldIndex = nFieldIndex;
 			_nSubfieldIndex = nSubfieldIndex;
 			_nItemIndex = nItemIndex;
 		}
 
 		public int getRecordType() { return _nRecordType; }
 		public int getRecordIndex() { return _nRecordIndex; }
 		public int getFieldIndex() { return _nFieldIndex; }
 		public int getSubfieldIndex() { return _nSubfieldIndex; }
 		public int getItemIndex() { return _nItemIndex; }
 
 		public String getData()
 		{
 			return IWFindItem(_nTransaction, _nRecordType, _nRecordIndex, _nFieldIndex, _nSubfieldIndex, _nItemIndex, _ret);
 		}
 
 		public void setData(String sData)
 		{
 			IWSetItem(_nTransaction, sData, _nRecordType, _nRecordIndex, _nFieldIndex, _nSubfieldIndex, _nItemIndex, _ret);
 		}
 	}
 
 	// Publicizes the rule-system currently defined by a Verification File.
 	public class NISTVerification
 	{
 		private long _nVerification = 0;
 		private StringBuffer _sbParseError = new StringBuffer("");
 		private NISTTransactionCategories _cats = null; 
 		private NISTReturn _ret = new NISTReturn();
 
 		protected void finalize ()
 		{
 			if (_nVerification != 0) IWCloseVerification(_nVerification, _ret);
 		}
 
 		public int loadFromFile(String sPath)
 		{
 			_nVerification = IWReadVerification(sPath, _sbParseError, _ret);
 			_cats = new NISTTransactionCategories(_nVerification);
 			return _ret.nRet;
 		}
 
 		public String getParseError()
 		{
 			return _sbParseError.toString();
 		}
 
 		public NISTTransactionCategories getTransactionCategories()
 		{
 			String[] saCategories = {};
 
 			saCategories = IWGetTransactionCategories(_nVerification, _ret);
 
 			if (_ret.nRet == 0 )
 			{
 				_cats.setCategories(saCategories);
 			}
 
 			return _cats;
 		}
 	}
 
 	// A collection of TOT categories
 	public class NISTTransactionCategories
 	{
 		private long _nVerification = 0;
 		private String[] _saCategories;
 
 		public NISTTransactionCategories(long nVerification)
 		{
 			_nVerification = nVerification;
 		}
 		
 		protected void setCategories(String saCategories[])
 		{
 			_saCategories = saCategories;
 		}
 
 		public int getTransactionCategoryCount()
 		{
 			return _saCategories.length;
 		}
 
 		public NISTTransactionCategory getTransactionCategory(int nIndex)
 		{
 			return new NISTTransactionCategory(_nVerification, _saCategories[nIndex]);
 		}
 	}
 
 	// A TOT category has a name and a list of TOTs
 	public class NISTTransactionCategory
 	{
 		private long _nVerification = 0;
 		private String _sCategory = "";
 		private NISTTransactionList _trans = null;
 		private NISTReturn _ret = new NISTReturn();
 
 		public NISTTransactionCategory(long nVerification, String sCategory)
 		{
 			_nVerification = nVerification;
 			_sCategory = sCategory;
 			_trans = new NISTTransactionList(_nVerification);
 		}
 
 		public String getName()
 		{
 			return _sCategory;
 		}
 
 		public NISTTransactionList getTransactionList()
 		{
 			String[] saNames = {};
 			String[] saDescriptions = {};
 
 			saNames = IWGetTransactionTypeNames(_nVerification, _sCategory, _ret);
 			if (_ret.nRet == 0 ) _trans.setNames(saNames);
 			saDescriptions = IWGetTransactionTypeDescriptions(_nVerification, _sCategory, _ret);
 			if (_ret.nRet == 0 ) _trans.setDescriptions(saDescriptions);
 
 			return _trans;
 		}
 	}
 
 	// A collection of TOTs
 	public class NISTTransactionList
 	{
 		private long _nVerification = 0;
 		private String[] _saNames = {};
 		private String[] _saDescriptions = {};
 
 		public NISTTransactionList(long nVerification)
 		{
 			_nVerification = nVerification;
 		}
 
 		protected void setNames(String saNames[])
 		{
 			_saNames = saNames;
 		}
 
 		protected void setDescriptions(String saDescriptions[])
 		{
 			_saDescriptions = saDescriptions;
 		}
 
 		public int getTransactionCount()
 		{
 			return _saNames.length;
 		}
 
 		public NISTTransaction getTransaction(int nIndex)
 		{
 			return new NISTTransaction(_nVerification, _saNames[nIndex], _saDescriptions[nIndex]);
 		}
 	}
 
 	// A TOT is defined by the list of record types it may contain as well
 	// as by the fields it may contain.
 	public class NISTTransaction
 	{
 		private long _nVerification = 0;
 		private String _sTOT = "";
 		private String _sDescription = "";
 		private NISTRecordOccurrencesList _occurrences = null;
 		private NISTFieldDefinitionList _fielddefs = null;
 		private NISTReturn _ret = new NISTReturn();
 
 		public NISTTransaction(long nVerification, String sTOT, String sDescription)
 		{
 			_nVerification = nVerification;
 			_sTOT = sTOT;
 			_sDescription = sDescription;
 			_occurrences = new NISTRecordOccurrencesList(_nVerification, _sTOT);
 		}
 
 		public String getName()
 		{
 			return _sTOT;
 		}
 
 		public String getDescription()
 		{
 			return _sDescription;
 		}
 
 		public NISTRecordOccurrencesList getRecordOccurrencesList()
 		{
 			return _occurrences;
 		}
 
 		public NISTFieldDefinitionList getFieldList()
 		{
 			// We only create this on demand because it involves a lot of string operations
 			if (_fielddefs == null)
 			{
 				CreateNISTFieldDefinitionList();
 			}
 
 			return _fielddefs;
 		}
 
 		private void CreateNISTFieldDefinitionList()
 		// We only create the list on demand since it can be a slow process
 		{
 			if (_fielddefs == null)
 			{
 				ArrayList<NISTFieldDefinition> fielddefarray;
 				NISTFieldDefinition fielddefNew = null;
 				NISTFieldDefinition fielddefPast = null;
 				NISTFieldRules fieldrules = null;
 				String[] saMNU;
 				int nRecordTypeSave = 0;
 				int nFieldSave = 0;
 				boolean bSkip = false;
 
 				fielddefarray = new ArrayList<NISTFieldDefinition>();
 	
 				// Get array of all mnemonics that belong to this TOT
 				saMNU = IWGetMnemonicsNames(_nVerification, _sTOT, _ret);   
 	
 				// After having enumerated all MNUs we figure out which have sub-items. We do this by
 				// using IWGetRuleRestrictions to get the item index. If it is 0, we consider all
 				// subsequent MNUs with the same record type and field number as sub-items.
 				// This allows to include them in the originating parent NISTFieldDefinition.
 				for (int i = 0; i < saMNU.length; i++)
 				{
 					fieldrules = IWGetRuleRestrictions(_nVerification, _sTOT, saMNU[i], _ret);
 	
 					if (_ret.nRet == 0)
 					{
 						// Create a new NISTFieldDefinition containing its rules
 						fielddefNew =  new NISTFieldDefinition(_nVerification, _sTOT, fieldrules);
 
 						if ((fieldrules._nRecordType != nRecordTypeSave) || (fieldrules._nField != nFieldSave))
 						{
 							bSkip = false;
 						}
 	
 						if (!bSkip)
 						{
 							// This is a "top-level item", so we add a new NISTFieldDefinition
 							// to our main list.
 		 					fielddefarray.add(fielddefNew);
 						}
 						else
 						{
 							// This is a subitem, so we add it to the list of subitem MNUs of the
 							// the last NISTFieldDefinition added.
 							fielddefPast = fielddefarray.get(fielddefarray.size() - 1);
 							fielddefPast.addSubitemFieldDefinition(fielddefNew);
 						}
 	
 						if (fieldrules._nItem == 0)
 						{
 							bSkip = true;
 							nRecordTypeSave = fieldrules._nRecordType;
 							nFieldSave = fieldrules._nField;
 						} 				
 					}
 				}
 
 				_fielddefs = new NISTFieldDefinitionList(fielddefarray);
 			}		
 		}
 	}
 
 	// A collection of 'RecordOccurrences'
 	public class NISTRecordOccurrencesList
 	{
 		private long _nVerification = 0;
 		private String _sTOT = "";
 		private NISTReturn _ret = new NISTReturn();
 		private int[][] _naOccurrences = {};
 		private int _nOccurrences = 0;
 
 		public NISTRecordOccurrencesList(long nVerification, String sTOT)
 		{
 			_nVerification = nVerification;
 			_sTOT = sTOT;
 			// IWGetRecordTypeOccurrences returns a 2 dimensional array of int of size
 			// 3 x n, where index 0 represents the Record Type, and index 1 and 2 the min
 			// and max allowed occurrences, respectively.
 			_naOccurrences = IWGetRecordTypeOccurrences(_nVerification, _sTOT, _ret);
 			if (_naOccurrences != null && _naOccurrences[0] != null)
 			{
 				_nOccurrences = _naOccurrences[0].length;
 			}
 		}
 
 		public int getRecordOccurrenceCount()
 		{
 			return _nOccurrences;
 		}
 
 		public NISTRecordOccurrence getRecordOccurrence(int nIndex)
 		{
 			NISTRecordOccurrence occurrence = null;
 
 			if (nIndex >= 0 && nIndex < _nOccurrences)
 			{
 				occurrence = new NISTRecordOccurrence(_naOccurrences[0][nIndex], _naOccurrences[1][nIndex], _naOccurrences[2][nIndex]); 
 			}
 
 			return occurrence;
 		}
 	}
 
 	// Each RecordType has a min and max number of times it can appear
 	public class NISTRecordOccurrence
 	{
 		private int _nRecordType = 0;
 		private int _nMin = 0;
 		private int _nMax = 0;
 	
 		public NISTRecordOccurrence(int nRecordType, int nMin, int nMax)
 		{
 			_nRecordType = nRecordType;
 			_nMin = nMin;
 			_nMax = nMax;
 		}
 
 		public int getRecordType()
 		{
 			return _nRecordType;
 		}
 
 		public int getOccurrencesMin()
 		{
 			return _nMin;
 		}
 
 		public int getOccurrencesMax()
 		{
 			return _nMax;
 		}
 	}
 
 	// A collection of 'FieldDefinitions'
 	public class NISTFieldDefinitionList
 	{
 		private ArrayList<NISTFieldDefinition> _fielddefarray = new ArrayList<NISTFieldDefinition>();
 
 		public NISTFieldDefinitionList(ArrayList<NISTFieldDefinition> fielddefarray)
 		// We allow null to be passed in as an initializer but we make the sure there is always an array
 		{
 			_fielddefarray = fielddefarray;
 			if (_fielddefarray == null)
 			{
 				_fielddefarray = new ArrayList<NISTFieldDefinition>();
 			}
 		}
 
 		private void addFieldDefinition(NISTFieldDefinition fielddef)
 		{
 			_fielddefarray.add(fielddef);
 		}
 
 		public int getFieldDefinitionCount()
 		{
 			return _fielddefarray.size();
 		}
 
 		public NISTFieldDefinition getFieldDefinition(int nIndex)
 		{
 			if (nIndex >= 0 && nIndex < _fielddefarray.size())
 			{
 				return _fielddefarray.get(nIndex);
 			}
 			else
 			{
 				return null;
 			}
 		}
 	}
 
 	// All the rules for a specific field
 	public class NISTFieldDefinition
 	{
 		long _nVerification = 0;
 		String _sTOT = "";
 		NISTFieldRules _fieldrules = null;
 		// Each NISTFieldDefinition can contain "subitem" NISTFieldDefinitions 
 		private NISTFieldDefinitionList _subitemfielddefs = new NISTFieldDefinitionList(null);
 		private NISTFieldValueList _valuelist = null;
 
 		public NISTFieldDefinition(long nVerification, String sTOT, NISTFieldRules fieldrules)
 		{
 			_nVerification = nVerification;
 			_sTOT = sTOT;
 			_fieldrules = fieldrules;
 		}
 
 		private void addSubitemFieldDefinition(NISTFieldDefinition fielddef)
 		{
 			_subitemfielddefs.addFieldDefinition(fielddef);
 		}
 
 		// Basic field information
 		public int getRecordType()				// e.g., 2, as in "Type-2"
 		{
 			return _fieldrules._nRecordType;
 		}
 
 		public int getFieldIndex()				// e.g., 18
 		{
 			return _fieldrules._nField;
 		}
 
 		public int getItemIndex()				// e.g., 3 for 10.042..3
 		{
 			return _fieldrules._nItem;
 		}
 
 		public String getIdentifier()			// e.g., "NAM"
 		{
 			// portion to the right of the '_' in the MNU
 			int pos = _fieldrules._sMNU.indexOf("_");  
 			return _fieldrules._sMNU.substring(pos + 1); 
 		}
 
 		public String getMnemonic()				// e.g., "T2_NAM"
 		{
 			return _fieldrules._sMNU;
 		}
 
 		public String getName()					// e.g., "Name"
 		{
 			return _fieldrules._sName;
 		}
 
 		// Datatype, from the EBTS Standard's tables
 		public boolean getIsDataTypeA()			// e.g., Alphabetic
 		{
 			return _fieldrules._sCharType.contains("A");
 		}
 
 		public boolean getIsDataTypeN()			// e.g., Numeric
 		{
 			return _fieldrules._sCharType.contains("N");
 		}
 
 		public boolean getIsDataTypeB()			// e.g., Binary
 		{
 			return _fieldrules._sCharType.contains("B");
 		}
 
 		public boolean getIsDataTypeP()			// e.g., Printable
 		{
 			return _fieldrules._sCharType.contains("P");
 		}
 
 		public boolean getIsDataTypeC()			// e.g., Control Character
 		{
 			return _fieldrules._sCharType.contains("C");
 		}
 
 		public boolean getIsDataTypeSET()			// e.g., Set of multiple fields
 		{
 			return _fieldrules._sCharType.equals("SET");
 		}
 
 		public String getSpecialChars()			// e.g., ";-"
 		{
 			return _fieldrules._sSpecialChars;
 		}
 
 		public boolean getIsDate()				// e.g., True for T2_DOB
 		{
 			return !_fieldrules._sDateFormat.equals("");
 		}
 
 		public String getDateFormat()			// e.g., "ZCCYYMMDD"
 		{
 			return _fieldrules._sDateFormat;
 		}
 
 		public String getAdvancedRule()			// e.g., "greatereq("T2_DOA")"
 		{
 			return _fieldrules._sAdvancedRule;
 		}
 
 		// Field content's byte-count range
 		public int getFieldSizeMin()
 		{
 			return _fieldrules._nSizeMin;
 		}
 
 		public int getFieldSizeMax()
 		{
 			return _fieldrules._nSizeMax;
 		}
 
 		public int getOccurrencesMin()
 		{
 			return _fieldrules._nOccurrencesMin;
 		}
 
 		public int getOccurrencesMax()
 		{
 			return _fieldrules._nOccurrencesMax;
 		}
 
 		public String getDescription()
 		{
 			return _fieldrules._sDescription;
 		}
 
 		// True iff field is automatically set by underlying OpenEBTS. For example, T[X]_LEN,
 		// T1_VER, T1_CNT, T4_GCA, etc..
 		public boolean getIsAutomaticallySet()
 		{
 			return _fieldrules._bAutomaticallySet;
 		}
 
 		// True if field is mandatory, false if optional
 		public boolean getIsMandatory()
 		{
 			return _fieldrules._bMandatory;
 		}
 
 		public NISTFieldDefinitionList getItemList()
 		{
 			return _subitemfielddefs;
 		}
 
 		public NISTFieldValueList getValueList()
 		{
 			// we create this on demand only, to save time
 			if (_valuelist == null)
 			{
 				_valuelist = new NISTFieldValueList(_nVerification, _sTOT, _fieldrules._sMNU); 
 			}
 
 			return _valuelist;	
 		}
 	}
 
 	// A named value list of possible choices for field values 
 	public class NISTFieldValueList
 	{
 		private NISTReturn _ret = new NISTReturn();
 		private NISTValueList _values = null;
 
 		public NISTFieldValueList(long nVerification, String sTOT, String sMNU)
 		{
 			_values = IWGetValueList(nVerification, sTOT, sMNU, _ret);
 			// Note that _values can be NULL at this point 
 		}
 	
 		public boolean getIsMandatory()
 		{	
 			return _values == null ? false : _values._bMandatory;
 		}
 
 		public int getFieldValueCount()
 		{
 			return _values == null ? 0 : _values._nCount;
 		}
 
 		public String getFieldValueName(int nIndex)
 		{
 			if (_values._saName != null && (nIndex >= 0 && nIndex < _values._saName.length))
 			{
 				return _values._saName[nIndex];
 			}
 			else
 			{
 				return "";
 			}
 		}
 
 		public String getFieldValueValue(int nIndex)
 		{
 			if (_values._saValue != null && (nIndex >= 0 && nIndex < _values._saValue.length))
 			{
 				return _values._saValue[nIndex];
 			}
 			else
 			{
 				return "";
 			}
 		}
 	}
 }
