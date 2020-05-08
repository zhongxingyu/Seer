 package org.strategoxt.imp.debug.stratego.runtime.strategies;
 
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.debug.stratego.runtime.trans.dr_get_key_set_0_0;
 import org.strategoxt.lang.Context;
 
 public abstract class DebugCallStrategy extends org.strategoxt.lang.Strategy {
 
 	protected TermFactory factory = new TermFactory();
 	
 	/**
 	 * Name of the variable that contains the eventInfo. The type of the variable should be String.
 	 */
 	public static String EVENTINFO = "eventInfo";
 	
 	/**
 	 * String representation of the eventinfo tuple (filename, name, location).
 	 * 
 	 * Should be protected so it can be accessed in any of its subclasses.
 	 */
 	protected String eventInfo = null;
 	
 	/**
 	 * Name of the variable that contains the given Term. The type of the variable should be String.
 	 */
 	public static String CURRENTTERMSTRING = "givenTermString";
 	
 	/**
 	 * String representation of the given IStrategoTerm.
 	 * Fetching a String and parsing it in the debugger should be faster than using jdi reflection to generate the IStrategoTerm.
 	 * 
 	 * Should be protected so it can be accessed in any of its subclasses.
 	 */
 	protected String givenTermString = null;
 	
 	/**
 	 * Name of the variable that contains the varname. The type of the variable should be String.
 	 * The variable will only have a value during a s-var event.
 	 */
 	public static String VARNAMESTRING = "varnameString";
 	
 	/**
 	 * String representation of the varname during a s-var event.
 	 * 
 	 * Should be protected so it can be accessed in any of its subclasses.
 	 */
 	protected String varnameString = null;
 	
 	/**
 	 * Every invoke should set the Context, the context is used when calling getDRKeySet() or getDRKeySetString().
 	 */
 	private Context context = null;
 	
 	/**
 	 * Name of the method that returns an IStrategoTerm (IStrategoList actually) containing the active dynamic rule names.
 	 */
 	public static String GETDRKEYSET = "getDRKeySet";
 	
 	/**
 	 * Name of the method that returns a String representation of the active dynamic rules names IStrategoList.
 	 */
 	public static String GETDRKEYSETSTRING = "getDRKeySetString";
 	
 	/**
 	 * Returns the active dynamic rule names as an IStrategoTerm (IStrategoList)
 	 * @return
 	 */
 	public IStrategoTerm getDRKeySet()
 	{
		//Context context;
 		IStrategoTerm term = null; // the current term, can this be null?
		IStrategoTerm keyList = dr_get_key_set_0_0.instance.invoke(this.context, term);
		return keyList;
 	}
 	
 	/**
 	 * Returns the active dynamic rule names as a String representation of an IStrategoTerm (IStrategoList)
 	 * @return
 	 */
 	public String getDRKeySetString()
 	{
 		return this.getDRKeySet().toString();
 	}
 	
 	/**
 	 * Almost empty method stub, implementors of DebugCallStrategy should call this method in their invoke so we can set a breakpoint on that line.
 	 * The debug event info should be initialized before calling this method so the debugger can request that information.
 	 * @param context
 	 */
 	protected void nothing()
 	{
 
 	}
 
 	/*
 	public String getFullClassName(){
 		
 		System.out.println(this.getClass().getName());
 		System.out.println(this.getClass().getCanonicalName());
 		System.out.println(this.getClass().getSimpleName());
 		
 		return this.getClass().getName();
 	}*/
 	
 	public IStrategoTerm invoke(Context context, IStrategoTerm current,	IStrategoTerm filename, IStrategoTerm name, IStrategoTerm location, IStrategoTerm given) {
 		this.context = context;
 		this.eventInfo = factory.makeTuple(filename, name, location).toString();
 		this.givenTermString = given.toString();
 		return current;
 	}
 	
 	public IStrategoTerm invoke(Context context, IStrategoTerm current,	IStrategoTerm filename, IStrategoTerm name, IStrategoTerm varname, IStrategoTerm location, IStrategoTerm given) {
 		this.context = context;
 		this.eventInfo = factory.makeTuple(filename, name, location).toString();
 		this.givenTermString = given.toString();
 		String s = varname.toString();
 		this.varnameString = s;
 		return current;
 	}
 }
