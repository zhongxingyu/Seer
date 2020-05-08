 package org.testtoolinterfaces.testsuite;
 
 import java.io.File;
 
 /**
  * Class for links to Test Groups or Test Case files.
  * This class is actually derived from java.io.File and extended with a type.
  * 
  * @author Arjan
  *
  */
 public class TestLinkImpl extends File implements TestLink
 {
 	private static final long	serialVersionUID	= 7596253157272610492L;
 
 	private String myType;
 	
 	/**
 	 * Creates a new File instance from a parent pathname string and a child pathname string.
 	 * 
 	 * If parent is null then the new File instance is created as if by invoking the single-argument
 	 * File constructor on the given child pathname string. 
 	 * 
 	 * Otherwise the parent pathname string is taken to denote a directory, and the child pathname
 	 * string is taken to denote either a directory or a file. If the child pathname string is
 	 * absolute then it is converted into a relative pathname in a system-dependent way. If parent is
 	 * the empty string then the new File instance is created by converting child into an abstract
 	 * pathname and resolving the result against a system-dependent default directory. Otherwise each
 	 * pathname string is converted into an abstract pathname and the child abstract pathname is
 	 * resolved against the parent. 
 	 * 
 	 * @param aDir		The parent pathname string, i.e. parent dir
 	 * @param aPathName	The (child) pathname string
 	 * @param aType		A Test Link type
 	 * @throws NullPointerException - If the child is null
 	 */
 	public TestLinkImpl(String aDir, String aPathName, String aType)
 	{
		super(aDir.replace('\\', File.separatorChar), aPathName.replace('\\', File.separatorChar));
 		myType = aType;
 	}
 
 	/**
 	 * Creates a new File instance from a parent abstract pathname and a child pathname string.
 	 * 
 	 * If parent is null then the new File instance is created as if by invoking the single-argument
 	 * File constructor on the given child pathname string.
 	 * 
 	 * Otherwise the parent abstract pathname is taken to denote a directory, and the child pathname
 	 * string is taken to denote either a directory or a file. If the child pathname string is
 	 * absolute then it is converted into a relative pathname in a system-dependent way. If parent is
 	 * the empty abstract pathname then the new File instance is created by converting child into an
 	 * abstract pathname and resolving the result against a system-dependent default directory.
 	 * Otherwise each pathname string is converted into an abstract pathname and the child abstract
 	 * pathname is resolved against the parent. 
 	 * 
 	 * @param aDir		The parent abstract pathname, i.e. parent dir
 	 * @param aPathName	A (child) pathname string
 	 * @param aType		A Test Link type
 	 * @throws NullPointerException - If the child is null
 	 */
 	public TestLinkImpl(File aDir, String aPathName, String aType)
 	{
		super(aDir, aPathName.replace('\\', File.separatorChar));
 		myType = aType;
 	}
 
 	/**
 	 * Creates a new File instance from a parent abstract pathname and a child pathname string.
 	 * 
 	 * If parent is null then the new File instance is created as if by invoking the single-argument
 	 * File constructor on the given child pathname string.
 	 * 
 	 * Otherwise the parent abstract pathname is taken to denote a directory, and the child pathname
 	 * string is taken to denote either a directory or a file. If the child pathname string is
 	 * absolute then it is converted into a relative pathname in a system-dependent way. If parent is
 	 * the empty abstract pathname then the new File instance is created by converting child into an
 	 * abstract pathname and resolving the result against a system-dependent default directory.
 	 * Otherwise each pathname string is converted into an abstract pathname and the child abstract
 	 * pathname is resolved against the parent. 
 	 * 
 	 * @param aDir		The parent abstract pathname, i.e. parent dir
 	 * @param aPathName	A (child) pathname string
 	 * @throws NullPointerException - If the child is null
 	 */
 	public TestLinkImpl(File aDir, String aPathName)
 	{
		this( aDir, aPathName.replace('\\', File.separatorChar), TestLink.TYPE_TTI );
 	}
 
 	/**
 	 * Creates a new TestLink instance by converting the given pathname string into an abstract pathname.
 	 * If the given string is the empty string, then the result is the empty abstract pathname. 
 	 * 
 	 * @param aPathName	A pathname string
 	 * @param aType		A Test Link type
 	 * @throws NullPointerException - If the pathname argument is null
 	 */
 	public TestLinkImpl(String aPathName, String aType)
 	{
		super(aPathName.replace('\\', File.separatorChar));
 		myType = aType;
 	}
 
 	/**
 	 * Creates a new TestLink instance by converting the given pathname string into an abstract pathname.
 	 * If the given string is the empty string, then the result is the empty abstract pathname. 
 	 * 
 	 * @param aPathName	A pathname string
 	 * @throws NullPointerException - If the pathname argument is null
 	 */
 	public TestLinkImpl(String aPathName)
 	{
		this( aPathName.replace('\\', File.separatorChar), TestLink.TYPE_TTI);
 	}
 
 	@Override
 	public File getFile()
 	{
 		return this;
 	}
 
 	@Override
 	public String getType()
 	{
 		return myType;
 	}
 }
