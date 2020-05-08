 /*
  * $Id: DefaultActionFactory.java,v 1.4 2006/06/04 22:57:57 Marrink Exp $ $Revision: 1.4 $ $Date: 2006/06/04 22:57:57 $
  * ==================================================================== Copyright (c) 2005, Topicus B.V. All rights
  * reserved.
  */
 
 package org.apache.wicket.security.swarm.actions;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.authorization.Action;
 import org.apache.wicket.security.actions.Access;
 import org.apache.wicket.security.actions.ActionFactory;
 import org.apache.wicket.security.actions.Enable;
 import org.apache.wicket.security.actions.Inherit;
 import org.apache.wicket.security.actions.RegistrationException;
 import org.apache.wicket.security.actions.Render;
 import org.apache.wicket.security.actions.WaspAction;
 
 
 /**
  * Default implementation of an action factory. It handles access, inherit, read, write
  * and execute actions. The render and enable actions from wicket are mapped to
  * respectivly read and execute actions. Because actions are inmutable and in order to
  * improve performance, generated actions are cached.
  * @author marrink
  */
 public class SwarmActionFactory implements ActionFactory
 {
 	private static final Log log = LogFactory.getLog(SwarmActionFactory.class);
 	protected static final int maxAssingableAction=(int) Math.pow(2, 30);
 	
 	/**
 	 * list of powers of for checking registrations
 	 */
 //	private int[] powersOf2=new int[]{0,1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768,65536,131072,262144,524288,1048576
 //			,2097152,4194304,8388608,16777216,33554432,67108864,134217728,268435456,536870912,maxAssingableAction};
 	/**
 	 * maps ints to Strings.
 	 */
 	private Map stringValues = new HashMap(10);
 	
 	/**
 	 * cache that maps ints to actions and Strings to actions
 	 */
 	private Map cachedActions = new HashMap();
 	/**
 	 * Maps ints to actions. and classes to actions.
 	 */
 	private Map registeredActions=new HashMap();
 
 	private int power = -1;
 	private int maxAction=0;
 
 	/**
 	 * Registers the default actions: access, inherit, read, write and execute.
 	 */
 	public SwarmActionFactory()
 	{
 		super();
 		try
 		{
 			register(Access.class,"access");
 			register(Inherit.class, "inherit");
 			register(Render.class, "render");
 			register(Enable.class, new ImpliesReadAction(nextPowerOf2(),"enable",this));
 		}
 		catch (RegistrationException e)
 		{
 			log.fatal(e, e);
 		}
 	}
 
 	public WaspAction getAction(Action action)
 	{
 		if(action instanceof SwarmAction)
 			return (SwarmAction) action;
 		if (action != null)
 			try
 			{
 				return getAction(action.getName());
 			}
 			catch (IllegalArgumentException e)
 			{
 				//according to the spec we return null if the action does not exist
 			}
 		return null;
 	}
 
 	/**
 	 * @param actions empty string means Access
 	 */
 	public WaspAction getAction(String actions)
 	{
 		String saveActions=convertWicket2Wasp(actions);
 		SwarmAction ja = getCachedAction(saveActions);
 		if (ja == null)
 		{
 			int actionValues = parseActions(saveActions);
 			String nameValues = buildActionString(actionValues);
 			ja = new SwarmAction(actionValues, nameValues);
 			cacheAction(saveActions, ja);
 		}
 		return ja;
 	}
 
 	/**
 	 * Caches an action under its string vorm.
 	 * @param name
 	 * @param action
 	 */
 	protected synchronized final void cacheAction(String name, SwarmAction action)
 	{
 		cachedActions.put(name, action);
 	}
 
 	/**
 	 * Returns a cached action.
 	 * @param name
 	 * @return the cached action or null.
 	 */
 	protected synchronized final SwarmAction getCachedAction(String name)
 	{
 		return (SwarmAction) cachedActions.get(name);
 	}
 
 	
 	public SwarmAction getAction(int actions)
 	{
 		SwarmAction ja = getCachedAction(actions);
 		if (ja == null)
 		{
 			if(actions>maxAction)
 				throw new IllegalArgumentException("Max value for actions = "+maxAction+", you used "+actions);
 			if(actions<0)
 				throw new IllegalArgumentException("Min value for actions = 0, you used "+actions);
 			ja = new SwarmAction(actions, buildActionString(actions));
 			cacheAction(new Integer(actions), ja);
 		}
 		return ja;
 
 	}
 
 	/**
 	 * Caches an action under its int vorm.
 	 * @param actions
 	 * @param ja
 	 */
 	protected synchronized void cacheAction(Integer actions, SwarmAction ja)
 	{
 		cachedActions.put(actions, ja);
 	}
 
 	/**
 	 * Returns a cached action.
 	 * @param actions
 	 * @return the cached action or null.
 	 */
 	protected synchronized SwarmAction getCachedAction(int actions)
 	{
 		return (SwarmAction) cachedActions.get(new Integer(actions));
 	}
 
 	/**
 	 * Returns the registerd string value of the given action.
 	 * @return the registerd string or null if no string value was registered for this
 	 *         action.
 	 */
 	protected final String valueOf(Integer action)
 	{
 		return (String) stringValues.get(action);
 	}
 
 	/**
 	 * Builds a logically ordered comma separated string of all the actions this
 	 * permission has. Based on the logical and of the supplied actions. Subclasses should
 	 * always return the same string (action order) for the same action.
 	 * @return string containing all the actions.
 	 * @see #getActions()
 	 */
 	protected String buildActionString(int actions)
 	{
		StringBuilder buff = new StringBuilder(power>0?10*power:10);
 		//estimate 10 chars per name
 		for(int i=-1;i<power;i++)
 		{
 			appendActionString(buff, actions, ((SwarmAction)registeredActions.get(new Integer(i))).actions());
 		}
 		if (buff.length() > 0) // should always be the case
 			buff.delete(buff.length() - 2, buff.length());
 		return buff.toString();
 	}
 
 	/**
 	 * Appends the string value of the action only if the actions implie the waspAction
 	 * @param buff where the string wil be appended to.
 	 * @param actions the available actions
 	 * @param waspAction the action it should implie in order to append the string
 	 */
	protected final void appendActionString(StringBuilder buff, int actions, int waspAction)
 	{
 		if (implies(actions, waspAction))
 			buff.append(valueOf(new Integer(waspAction))).append(", ");
 	}
 
 	/**
 	 * Check if the action is available in the actions.
 	 * @param actions the actions that might contain action
 	 * @param action the action we check for in actions
 	 * @return true if actions contains action
 	 */
 	protected final boolean implies(int actions, int action)
 	{
 		return ((actions & action) == action);
 	}
 
 	/**
 	 * Parses a comma separated String containing actions. Access is the default and will also be substituted for any empty or null
 	 * String. using a string like 'read, read' is ofcourse pointless but does not brake
 	 * anything. Order of the actions is also not important.
 	 * @param actions
 	 * @return a logical and of the actions.
 	 * @throws IllegalArgumentException if (one of) the action(s) is not recognized.
 	 */
 	protected int parseActions(String actions)
 	{
 		int sum = 0; // no actions or empty actions equal access
 		if(actions != null)
 		{
 			String[] actionz = actions.split(",");
 			String action = null;
 			Set keys = stringValues.keySet();
 			for (int i = 0; i < actionz.length; i++)
 			{
 				action = actionz[i].trim();
 				if(action.equals(""))
 					break; // Access
 				boolean found = false;
 				Iterator it=keys.iterator();
 				Integer key;
 				while(it.hasNext() && !found)
 				{
 					key=(Integer)it.next();
 					if(action.equalsIgnoreCase(valueOf(key)))
 					{
 						sum = sum | key.intValue();
 						found = true;
 					}
 				}
 				if(!found)
 					throw new IllegalArgumentException("Invalid action: " + action + " in: " + actions);
 			}
 		}
 		return sum;
 	}
 
 	public synchronized WaspAction getAction(Class waspActionClass)
 	{
 		WaspAction action=(WaspAction)registeredActions.get(waspActionClass);
 		if(action==null)
 			throw new IllegalArgumentException(""+waspActionClass+" is not registered");
 		return action;
 			
 	}
 	/**
 	 * 
 	 * @see org.apache.wicket.security.actions.ActionFactory#register(java.lang.Class, java.lang.String)
 	 */
 	public synchronized WaspAction register(Class waspActionClass, String name) throws RegistrationException
 	{
 		WaspAction temp=(WaspAction)registeredActions.get(waspActionClass);
 		if(temp!=null)
 			return temp;
 		if (WaspAction.class.isAssignableFrom(waspActionClass))
 		{
 			if (power > 30)
 				throw new RegistrationException("Can not register more then 32 different actions.");
 			//32 since we start at 0 :)
 			int action = nextPowerOf2();
 			return register(waspActionClass,new SwarmAction(action,name));
 		}
 		else
 			throw new RegistrationException(waspActionClass
 					+ " is not a " + WaspAction.class.getName());
 	}
 	/**
 	 * Returns the number of registered classes.
 	 * By default there are 4 classes registered.
 	 * @return the number of registered classes.
 	 */
 	public final int getNumberOfRegisteredClasses()
 	{
 		return power+1;
 	}
 	/**
 	 * The next action value. Note that you can only register classes while this is < {@link Integer#MAX_VALUE}
 	 * @return the int value of 2^(numberOfRegisteredClasses()-1)
 	 */
 	protected final int nextPowerOf2()
 	{
 		return (int) Math.pow(2, power);
 	}
 	/**
 	 * Renames build in wicket actions to there wasp counterpart.
 	 * more specifically render to read and enable to execute. 
 	 * @param name
 	 * @return the converted name of the action.
 	 */
 	protected final String convertWicket2Wasp(String name)
 	{
 		if(name==null)
 			return "";
 		return name.toLowerCase();
 	}
 	/**
 	 * Registeres a new action. Use this is in combination with {@link SwarmAction#SwarmAction(int, String, ActionFactory)}.
 	 * <pre><code>
 	 * register(Enable.class, new ImpliesReadAction(nextPowerOf2(),"enable",this));
 	 * class ImpliesReadAction extends SwarmAction
 	 * {
 	 * 	public ImpliesReadAction(int actions, String name, ActionFactory factory)
 	 * 	{
 	 *		super(actions|((SwarmAction)factory.getAction(org.apache.wicket.security.actions.Read.class)).actions(), name);
 	 * 	}
 	 * }
 	 * </code>
 	 * <pre>
 	 * Note all actions registered in this way must use nextPowerOf2() and then inmediatly register the action to preserve consistency.
 	 * @param waspActionClass the class under which to register the action
 	 * @param action the actual implementation (note that it does not need to implement the supplied waspActionClass)
 	 * @return the action
 	 * @throws RegistrationException if the action can not be registered.
 	 * @see #nextPowerOf2()
 	 * @see SwarmAction#SwarmAction(int, String, ActionFactory)
 	 */
 	protected final synchronized SwarmAction register(Class waspActionClass,SwarmAction action) throws RegistrationException
 	{
 		//sanity chekcs
 		if (power > 30)
 			throw new RegistrationException("Can not register more then 32 different actions.");
 		int assignedPowerOf2=nextPowerOf2();
 		if(assignedPowerOf2>maxAssingableAction)
 			throw new RegistrationException("Unable to register an action with a base value greater then "+maxAssingableAction);
 		if(assignedPowerOf2<0)
 			throw new RegistrationException(assignedPowerOf2+" is not a positive value");
 		//includes any implied actions
 		Integer powerOf2 = new Integer(action.actions());
 		if(!implies(powerOf2.intValue(),assignedPowerOf2))
 			throw new RegistrationException("Unable to register action '"+action.getName()+"' with value "+powerOf2+" expected "+assignedPowerOf2+" or more.");
 		//end of checks
 		
 		stringValues.put(powerOf2, action.getName());
 		registeredActions.put(new Integer(power),action);
 		registeredActions.put(waspActionClass,action);
 		maxAction+=assignedPowerOf2;
 		power ++;
 		return action;
 	}
 	/**
 	 * Any class that implies a read action.
 	 * 
 	 * @author marrink
 	 *
 	 */
 	private static final class ImpliesReadAction extends SwarmAction
 	{
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * @param actions
 		 * @param name
 		 * @param factory
 		 */
 		public ImpliesReadAction(int actions, String name, ActionFactory factory)
 		{
 			super(actions|((SwarmAction)factory.getAction(org.apache.wicket.security.actions.Render.class)).actions(), name);
 		}
 		
 	}
 	/**
 	 * Clears registration and cached values.
 	 * After you destroy this factory you must not use it again.
 	 * @see org.apache.wicket.security.actions.ActionFactory#destroy()
 	 */
 	public void destroy()
 	{
 		power=31; //prevents new registrations
 		maxAction=0; //prevents lookups
 		registeredActions.clear();
 		cachedActions.clear();
 		stringValues.clear();
 		registeredActions=null;
 		cachedActions=null;
 		stringValues=null;
 	}
 }
