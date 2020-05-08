 package com.programmingteam.qsync;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import com.programmingteam.Log;
 
 ///
 /// \brief representation of single import to vcxproj/filters files
 /// Single import contains of:
 /// 	* destination filter path (Visual Studio specific)
 ///		* path to include files (used in create class for storing .h file)
 /// 	* path to source files	(used in create class for storing .cpp file)
 /// 	* list of misc folder to be included
 ///
 public class QSyncImport
 {
 	private String mToFilter;
 	private String mInclude;
 	private String mSrc;
 	
 	private String mRegexpInclude;
 	private String mRegexpSrc;
 	
 	private String mRegexpExcludeInc;
 	private String mRegexpExcludeSrc;
 	
 	private boolean mIncludeEmptyDirs =false;
 	
 	private List<String> mMisc;
 	
 	///
 	/// \brief constructs QSyncImport
 	///
 	QSyncImport(String toFilter)
 	{
 		mToFilter = toFilter;
 		mMisc = new ArrayList<String>();
 	}
 
 	///
 	/// \brief sets absolute path to include folder
 	///
 	public void setInclude(String inc)
 	{
 		this.mInclude = inc;
 	}
 	
 	///
 	/// \brief sets absolute path to source folder
 	/// \param[in] String absolute path to src folder to be imported
 	///
 	public void setSrc(String src)
 	{
 		this.mSrc = src;
 	}
 	
 	public void setRegexpInclude(String regexp)
 	{
 		mRegexpInclude = regexp;
 		try { Pattern.compile(mRegexpInclude); }
 		catch (PatternSyntaxException e) 
 		{  
 			Log.e("Error parsing regular expression (" + mRegexpInclude + ") " + e.getMessage());
 			System.exit(-1);
 		}
 	}
 	
 	public void setRegexpSrc(String regexp)
 	{
 		mRegexpSrc = regexp;
 		try { Pattern.compile(mRegexpSrc); }
 		catch (PatternSyntaxException e) 
 		{  
 			Log.e("Error parsing regular expression (" + mRegexpSrc + ") " + e.getMessage());
 			System.exit(-1);
 		}
 	}
 
 	public void setExcludeSrc(String regexp)
 	{
 		mRegexpExcludeSrc = regexp;
 		try { Pattern.compile(mRegexpExcludeSrc); }
 		catch (PatternSyntaxException e) 
 		{  
 			Log.e("Error parsing regular expression (" + mRegexpSrc + ") " + e.getMessage());
 			System.exit(-1);
 		}
 	}
 	
 	public void setExcludeInc(String regexp)
 	{
 		mRegexpExcludeInc = regexp;
 		try { Pattern.compile(mRegexpExcludeInc); }
 		catch (PatternSyntaxException e) 
 		{  
 			Log.e("Error parsing regular expression (" + mRegexpSrc + ") " + e.getMessage());
 			System.exit(-1);
 		}
 	}
 	
 	public void setIncludeEmptyDirs(boolean include)
 	{
 		mIncludeEmptyDirs = include;
 	}
 
 	///
 	/// \biref sets absolute path to misc folder
 	/// \param[in] String absolute path to include folder to be imported
 	///
 	public void addMisc(String misc)
 	{
 		mMisc.add(misc);
 	}
 
 	///
 	/// \brief ToFilter is a root for folder structure in visual studio project 
 	/// \returns String toFilter path
 	///
 	public String getToFilter()
 	{
 		return mToFilter;
 	}
 	
 	///
 	/// \brief returns include absolute path
 	///
 	public String getInclude()
 	{
 		return mInclude;
 	}
 	
 	public boolean matchesInclue(String file)
 	{
 		if(mRegexpInclude==null) return true;
 		return file.matches(mRegexpInclude);
 	}
 	
 	public boolean matchesSrc(String file)
 	{
 		if(mRegexpSrc==null) return true;
 		return file.matches(mRegexpSrc);
 	}
 
 	public boolean isExcludedInc(String file)
 	{		
 		if(mRegexpExcludeInc==null) return false;
 		else return file.matches(mRegexpExcludeInc);
 	}
 	
 	public boolean isExcludedSrc(String file)
 	{
 		if(mRegexpExcludeSrc==null) return false;
 		else return file.matches(mRegexpExcludeSrc);
 	}
 	
 	public boolean isIncludeEmptyDirs()
 	{
 		return mIncludeEmptyDirs;
 	}
 	
 	///
 	/// \brief returns sources absolute path
 	///
 	public String getSrc()
 	{
 		return mSrc;
 	}
 	
 	///
 	/// \brief prints all data contained in this object
 	///
 	public void debugPrint()
 	{
 		Log.v("mToFilter: " + mToFilter);
 		Log.v("\tmIncludeEmptyDirs: " + mIncludeEmptyDirs);
 		Log.v("\tmInclude: " + mInclude);
 		Log.v("\tmRegexpInclude: " + mRegexpInclude);
 		Log.v("\tmRegexpExcludeInc: " + mRegexpExcludeInc);
 		Log.v("\tmSrc: " + mSrc);
 		Log.v("\tmRegexpSrc: " + mRegexpSrc);
 		Log.v("\tmRegexpExcludeSrc: " + mRegexpExcludeSrc);
 		for(String s: mMisc)
 			Log.v("\tMisc: " + s);
 	}
 }
