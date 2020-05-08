 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.stdquestion;
 
 import java.io.IOException;
 import java.lang.reflect.*;
 import java.util.*;
 
 import om.*;
 import om.helper.QEngineConfig;
 import om.question.*;
 import om.stdcomponent.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.misc.IO;
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
  * Standard Om question with question components defined in initial XML.
  */
 public abstract class StandardQuestion implements Question
 {
 	/** Component tree */
 	private QDocument qd;
 
 	/** Current action rendering (if we are inside an action() call) */
 	private ActionRendering arCurrent=null;
 
 	/** Map of all currently-set placeholders */
 	private Map<String,String> mPlaceholders=new HashMap<String,String>();
 
 	/** Set just to make sure that all callbacks are checked before they're called */
 	private Set<String> sCheckedCallbacks=new HashSet<String>();
 
 	/** Question results */
 	private Results rResults=new Results();
 
 	/** Stores text that has been logged for debugging */
 	private StringBuffer sbDebug=new StringBuffer();
 
 	/** Copy of init parameters */
 	private InitParams ip;
 
 	/** Path to resources within question classloader */
 	private String sResourcePath;
 
 	/** If not null, this indicates that a fixed variant is in use */
 	private NotSoRandom nsr=null;
 
 	private static final int MAGIC_RANDOM_SEED_INCREMENT = 12637946;
 
 
 	// Interface implementation and related
 	///////////////////////////////////////
 
 	public Rendering init(Document d,InitParams initParams) throws OmException
 	{
 		this.ip=initParams;
 
 		// Initialise available components and add question-specific components
 		QComponentManager qcm=new QComponentManager(initParams);
 		Element[] aeDefines=
 			XML.getChildren(d.getDocumentElement(),"define-component");
 		for(int i=0;i<aeDefines.length;i++)
 		{
 			String sClassName=null;
 			try
 			{
 				sClassName=XML.getRequiredAttribute(aeDefines[i],"class");
 				qcm.register(Class.forName(sClassName));
 			}
 			catch(XMLException e)
 			{
 				throw new OmDeveloperException(
 					"<define-component> is missing class= attribute");
 			}
 			catch(ClassNotFoundException e)
 			{
 				throw new OmDeveloperException(
 					"<define-component>: "+sClassName+" cannot be found");
 			}
 		}
 
 		// Set resource classpath location
 		if(d.getDocumentElement().hasAttribute("resources"))
 		{
 			sResourcePath=d.getDocumentElement().getAttribute("resources");
 			if(!sResourcePath.startsWith("/") || !sResourcePath.endsWith("/"))
 			{
 				throw new OmFormatException(
 					"<question> resources= must begin and end with /");
 			}
 		}
 		else
 		{
 			sResourcePath=null;
 		}
 
 		// Set not-so-random number if variant has been fixed
 		if(initParams.hasFixedVariant())
 			nsr=new NotSoRandom(initParams.getFixedVariant());
 
 		// Build component tree
 		qd=new QDocument(this,d,qcm);
 
 		// Return
 		Rendering r=new Rendering();
 
 		// User-overridable init
 		init();
 
 		// Render current state to XHTML
 		qd.render(r,true);
 
 		fixPlaceholders(r);
 
 		return r;
 	}
 
 	/**
 	 * Loads a question resource into memory.
 	 * @param sName Name of resource (may be relative path including /; may not
 	 *   be absolute path beginning /)
 	 * @return Byte array containing entire resource
 	 * @throws IOException If the resource doesn't exist or there's another problem
 	 */
 	public byte[] loadResource(String sName) throws IOException
 	{
 		if(sName.startsWith("/")) throw new IllegalArgumentException(
 			"loadResource with class reference must be relative name");
 		if(sResourcePath!=null)
 			return IO.loadResource(ip.getClassLoader(),sResourcePath+sName);
 		else
 		{
 			return IO.loadResource(getClass(),sName);
 		}
 	}
 
 	protected QEngineConfig getQEngineConfig() {
 		return ip.getQEngineConfig();
 	}
 
 	public synchronized ActionRendering action(ActionParams ap) throws OmException
 	{
 		arCurrent=new ActionRendering();
 
 		// Pass to document
 		qd.action(ap);
 
 		// Render current state
 		if(!arCurrent.isSessionEnd()) {
 			qd.render(arCurrent,false);
 		}
 		fixPlaceholders(arCurrent);
 
 		ActionRendering ar=arCurrent;
 		arCurrent=null;
 		return ar;
 	}
 
 	/**
 	 * Override if desired to handle question completion. Called whether a
 	 * question completes successfully or is cancelled.
 	 */
 	public void close()
 	{
 	}
 
 	/**
 	 * NO LONGER IN USE (marked final so it gives an error if your question
 	 * overrides it)
 	 */
 	protected final void init(InitParams initParams) throws OmException
 	{
 	}
 
 	/**
 	 * Override to set up the question if necessary.
 	 * @throws OmException For any error
 	 */
 	protected void init() throws OmException
 	{
 	}
 
 	/**
 	 * Override to handle Submit all and finish from Moodle, and similar.
 	 * @throws OmException For any error
 	 */
 	protected void finish() throws OmException
 	{
 	}
 
 	/**
 	 * Fix placeholders just before returning output.
 	 * @param r Output about to be returned
 	 */
 	private void fixPlaceholders(Rendering r)
 	{
 		XML.replaceTokens(r.getXHTML(),"__",mPlaceholders);
 	}
 
 	/**
 	 * Calls a particular callback function. The function must previously have
 	 * been checked using checkCallback().
 	 * @param sCallback Name of callback
 	 * @throws OmException If the callback throws an exception or there was
 	 *   an error calling it
 	 */
 	public void callback(String sCallback) throws OmException
 	{
 		if(!sCheckedCallbacks.contains(sCallback))
 			throw new OmDeveloperException(
 				"Error running callback "+sCallback+"(): checkCallback was not called");
 
 		try
 		{
 			Method m=getClass().getMethod(sCallback,new Class[0]);
 			m.invoke(this,new Object[0]);
 		}
 		catch(InvocationTargetException e)
 		{
 			if(e.getCause() instanceof OmException)
 				throw (OmException)e.getCause();
 			else
 				throw new OmException("Exception in callback "+sCallback+"()",e.getCause());
 		}
 		catch(Exception e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Checks that a callback method is present and correctly defined.
 	 * <p>
 	 * Having checked it, you can call it using callback().
 	 * @param sCallback Name of callback method
 	 * @throws OmDeveloperException If the method doesn't exist or is defined
 	 *   incorrectly
 	 */
 	public void checkCallback(String sCallback) throws OmDeveloperException
 	{
 		try
 		{
 			Method m=getClass().getMethod(sCallback,new Class[0]);
 
 			if(m.getReturnType()!=void.class)
 				throw new OmDeveloperException(
 					"Callback method "+sCallback+"() must return void");
 			if(!Modifier.isPublic(m.getModifiers()))
 				throw new OmDeveloperException(
 					"Callback method "+sCallback+"() must be public");
 			if(Modifier.isStatic(m.getModifiers()))
 				throw new OmDeveloperException(
 					"Callback method "+sCallback+"() may not be static");
 			if(Modifier.isAbstract(m.getModifiers()))
 				throw new OmDeveloperException(
 					"Callback method "+sCallback+"() may not be abstract");
 
 			sCheckedCallbacks.add(sCallback);
 		}
 		catch(NoSuchMethodException e)
 		{
 			throw new OmDeveloperException(
 				"Callback method "+sCallback+"() does not exist");
 		}
 	}
 
 	// System methods intended for subclasses
 	/////////////////////////////////////////
 
 	/**
 	 * Causes the question to end. (Call from within action processing; you can't
 	 * end a question in the init() call.) Control
 	 * returns from this method as usual, although you probably shouldn't do
 	 * anything else afterwards.
 	 */
 	protected void end() throws OmDeveloperException
 	{
 		if(arCurrent==null) throw new OmDeveloperException(
 			"Cannot call end() when not within action()");
 		arCurrent.setSessionEnd();
 	}
 
 	/**
 	 * Returns a Results object, which you can use to set up the
 	 * results or summary of the question. Changing things in the Results
 	 * object does not actually cause results to be sent to the test navigator.
 	 * You should call sendResults() once they are final.
 	 * @return A Results object for you to modify.
 	 */
 	protected Results getResults()
 	{
 		return rResults;
 	}
 
 	/**
 	 * Causes the information in the Results object to be sent to the test
 	 * navigator and stored. You should call this only once per question; it
 	 * doesn't have to be right at the end of the question (although it can be).
 	 * @throws OmDeveloperException
 	 */
 	protected void sendResults() throws OmDeveloperException
 	{
 		if(arCurrent==null) throw new OmDeveloperException(
 			"Cannot call sendResults() when not within action()");
 		arCurrent.sendResults(rResults);
 	}
 
 	/**
 	 * Sets the progress information text, which is displayed outside the
 	 * question to give the user an idea of their progress through this
 	 * question (e.g. 'You have 3 attempts left'.)
 	 * <p>
 	 * If you don't call this, the progress info remains the same as it was
 	 * before (blank, if you never set it).
 	 * @param sProgressInfo Progress string
 	 */
 	protected void setProgressInfo(String sProgressInfo) throws OmDeveloperException
 	{
 		if(arCurrent==null) throw new OmDeveloperException(
 			"Cannot call setProgressInfo() when not within action()");
 		arCurrent.setProgressInfo(sProgressInfo);
 	}
 
 
 	/** @return the debug display log, and clear it. */
 	public String eatLog()
 	{
 		String sReturn=sbDebug.toString();
 		sbDebug.setLength(0);
 		return sReturn;
 	}
 
 	/**
 	 * Logs a string to the debug display log (used only in development servlet).
 	 * @param s String to log
 	 */
 	public void log(String s)
 	{
 		sbDebug.append(s+"\n");
 	}
 
 	/**
 	 * Sets the value of a placeholder.
 	 * <p>
 	 * Placeholders are anything that ends up in the XHTML output with __ either
 	 * side e.g. __A__. When you set the value of a placeholder, it is replaced
 	 * everywhere it occurs with the given value. Values stay set so you don't
 	 * need to re-set them every time there is an action call.
 	 * <p>
 	 * Placeholders work in attributes as well as in text. They do not work in
 	 * tag names.
 	 * <p>
 	 * Placeholder values are set in the DOM not in the final XHTML string, so
 	 * there is no need to escape special characters with ampersands or similar.
 	 * @param sPlaceholder Name of placeholder (part between __ signs, e.g. "A")
 	 * @param sValue Value to replace placeholder with (e.g. "3.14")
 	 */
 	protected void setPlaceholder(String sPlaceholder,String sValue)
 	{
 		mPlaceholders.put(sPlaceholder,sValue);
 	}
 
 	/**
 	 * Applys placeholders to a string. Mostly useful for components that need to
 	 * handle strings in some other way than simply outputting XHTML (e.g. include
 	 * string in a graphc).
 	 * @param sValue Original string
 	 * @return String with any placeholders applied
 	 */
 	public String applyPlaceholders(String sValue)
 	{
 		return XML.replaceTokens(sValue,"__",mPlaceholders);
 	}
 
 	// Utility and helper methods
 	/////////////////////////////
 
 	/**
 	 * Obtains a random number generator for the question, based on the random
 	 * seed specifed by the test navigator but modified uniquely for this class
 	 * (so that if two questions in the test need a random number between 1 and 5,
 	 * they will have a chance of getting different ones).
 	 * @return Random number generator
 	 */
 	public Random getRandom()
 	{
 		return getRandom(getClass().getName());
 	}
 
 	/**
 	 * Obtains a random number generator based on the seed specified by the test
 	 * navigator but modified uniquely for this group. Use this variant when
 	 * several questions need the same sequence of random numbers (for example
 	 * they are about properties of an element and you want it to be the same
 	 * element). As long as you use the same group name for the questions, they
 	 * will receive an identical generator [when used by the same user].
 	 * @param sGroup Group name (arbitrary string)
 	 * @return Random number generator
 	 */
 	public Random getRandom(String sGroup)
 	{
 		return getRandom(sGroup, true);
 	}
 	
 	/**
 	 * Obtains a random number generator based on the seed, attempts and
 	 * navigatorVersion passed by the test navigator. If 'incrementSeed' 
 	 * is false attempts and navigatorVersion wont be used to get the random 
 	 * number generator.	
 	 * @param sGroup Group name (arbitrary string)
 	 * @param incrementSeed If true, then increments the random seed passed by 
 	 * the test navigator
 	 * @return Random number generator
 	 */
 	private Random getRandom(String sGroup, boolean incrementSeed)
 	{
 		// Returns NotSoRandom object if fixed variant is used.
 		if (nsr != null) {
 			return nsr;
 		}		
 		long randomSeed = ip.getRandomSeed();
 		
 		// Increment seed if the request is from OpenMark test navigator only
 		if (ip.getNavigatorVersion() != null && incrementSeed) {
 			int seedIncrement = MAGIC_RANDOM_SEED_INCREMENT;
 			if (OmVersion.compareVersions(ip.getNavigatorVersion(), "1.3.0") <= 0) {
 				seedIncrement = 1;
 			}
 			randomSeed = randomSeed + ip.getAttempt() * seedIncrement;
 		}
 		
 		long lHash=sGroup.hashCode();
 		return new Random(randomSeed ^ lHash);
 	}
 
 	/**	
 	 * Returns a variant number between 0 (inclusive) and maxVariant (exclusive).
 	 * Use this method to get the next variant of a question in sequence. 
 	 * For the first attempt it returns a random variant and for the repeat 
 	 * attempts it returns next variants in sequence, when the last variant is 
 	 * reached it cycles back to the beginning.
 	 * @param maxVariant
 	 * @return variant number
 	 */
 	public int getNextVariant(int maxVariant) {
 		// Passing false for the 'incrementSeed' parameter to get the same 
 		// random number generator for all question attempts.
 		Random r = getRandom(getClass().getName(), false);		
 		if (r instanceof NotSoRandom) {
 			// If it is an instanceof NotSoRandom that means variant is fixed 
 			// for testing, do not increment the variant
 			return r.nextInt(maxVariant);
 		}
 		return (r.nextInt(maxVariant) + ip.getAttempt() - 1) % maxVariant;
 	}
 
 	/** Special 'random' number generator that actually always returns the same int */
 	private class NotSoRandom extends Random
 	{
 		/** Number to return */
 		private int iNumber;
 
 		/** How many times we've returned it */
 		private int iCount=0;
 
 		/** How many times we'll fix the int before reverting to randomness */
 		private final static int FIXLIMIT=10;
 
 		/** @param iNumber Number to return */
 		private NotSoRandom(int iNumber)
 		{
 			super(iNumber); // Just to 'fix' the other stuff
 			this.iNumber=iNumber;
 		}
 
 		/**
 		 * Get the fixed number (note: all other members actually work randomly).
 		 * After returning the fixed number FIXLIMIT times, this reverts to
 		 * ordinary random generator behaviour.
 		 * @param iRange the bound on the number to be returned
 		 * @return the fixed number.
 		 */
 		@Override
 		public int nextInt(int iRange)
 		{
 			if(iCount<FIXLIMIT)
 			{
 				iCount++;
 				return (iNumber<iRange) ? iNumber : (iRange-1);
 			}
 			else
 			{
 				return super.nextInt(iRange);
 			}
 		}
 	}
 
 	/**
 	 * @param sID ID of desired component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist
 	 */
 	protected QComponent getComponent(String sID) throws OmDeveloperException
 	{
 		return qd.find(sID);
 	}
 
 	/**
 	 * @param sID ID of editfield component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected EditFieldComponent getEditField(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (EditFieldComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not an <editfield>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of checkbox component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected CheckboxComponent getCheckbox(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (CheckboxComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not an <checkbox>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of checkbox component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected RadioBoxComponent getRadioBox(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (RadioBoxComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not an <radiobox>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of advancedfield component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected AdvancedFieldComponent getAdvancedField(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (AdvancedFieldComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not an <advancedfield>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of dropbox component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected DropBoxComponent getDropBox(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (DropBoxComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not a <dropbox>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of canvas component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected CanvasComponent getCanvas(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (CanvasComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not a <canvas>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of text component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected TextComponent getText(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (TextComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not a <t>: "+sID);
 		}
 	}
 	/**
 	 * @param sID ID of JME component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected JMEComponent getJME(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (JMEComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not a <jme>: "+sID);
 		}
 	}
 	
	protected IFrameComponent getIFrame(String sID) throws OmDeveloperException
	{
		try
		{
			return (IFrameComponent)getComponent(sID);
		}
		catch(ClassCastException cce)
		{
			throw new OmDeveloperException("Component is not an <iframe>: "+sID);
		}
	}
	
 	/**
 	 * @param sID ID of Applet component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected AppletComponent getApplet(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (AppletComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not a <applet>: "+sID);
 		}
 	}
 	
 	/**
 	 * @param sID ID of component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected DropdownComponent getDropdown(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (DropdownComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not a <dropdown>: "+sID);
 		}
 	}
 
 	/**
 	 * @param sID ID of component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected ImageComponent getImage(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (ImageComponent)getComponent(sID);
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not an <image>: "+sID);
 		}
 	}
 
 	/**
 	 * @param sID ID of component
 	 * @return Component object
 	 * @throws OmDeveloperException If it doesn't exist or isn't correct type
 	 */
 	protected FlashComponent getFlash(String sID) throws OmDeveloperException
 	{
 		try
 		{
 			return (FlashComponent) getComponent(sID);
 		}
 		catch (ClassCastException cce)
 		{
 			throw new OmDeveloperException("Component is not an <flash>: " + sID);
 		}
 	}
 
 	// Accessibility information
 	////////////////////////////
 
 	/** @return True if colour has been fixed for accessibility reasons */
 	public boolean isFixedColour()
 	{
 		return ip.getFixedColourFG()!=null;
 	}
 
 	/** @return Fixed foreground colour in #rrggbb format */
 	public String getFixedColourFG()
 	{
 		return ip.getFixedColourFG();
 	}
 
 	/** @return Fixed background colour in #rrggbb format */
 	public String getFixedColourBG()
 	{
 		return ip.getFixedColourBG();
 	}
 
 	/** @return Fixed background colour in #rrggbb format */
 	public String getDisabledColourBG()
 	{
 		return ip.getFixedColourBG();
 	}
 	/** @return Fixed background colour in #rrggbb format */
 	public String getDisabledColourFG()
 	{
 		return ip.getFixedColourBG();
 	}
 
 
 
 	/** @return Zoom scale factor for accessibility */
 	public double getZoom()
 	{
 		return ip.getZoom();
 	}
 
 	/** @return True if in 'plain mode' for accessibility */
 	public boolean isPlainMode()
 	{
 		return ip.isPlainMode();
 	}
 }
