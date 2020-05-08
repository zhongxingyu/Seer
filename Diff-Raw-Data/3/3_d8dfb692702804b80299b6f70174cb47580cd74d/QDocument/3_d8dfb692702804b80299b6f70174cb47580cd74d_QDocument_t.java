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
 
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 
 import om.*;
 import om.question.*;
 import om.stdcomponent.RootComponent;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 
 /** Tree of QComponents used for a question */
 public class QDocument
 {
 	/**
 	 * Prefix used for items that aren't values nor actions.
 	 */
 	public final static String OM_PREFIX="om_";
 
 	/**
 	 * Prefix used on item form names for items that represent values that
 	 * should be updated in components. (Most forms return many values.)
 	 * Items using this prefix are automatically passed to the component with
 	 * ID that follows; if a single item uses multiple values it should use
 	 * a different prefix (e.g. plain old OM_PREFIX).
 	 */
 	public final static String VALUE_PREFIX="omval_";
 
 	/**
 	 * Prefix used on item form names for items that represent actions that
 	 * should be taken. (Most forms return only one action.)
 	 */
 	public final static String ACTION_PREFIX="omact_";
 
 	/**
 	 * Token to be placed before all XHTML IDs.
 	 */
 	public final static String ID_PREFIX = "%%IDPREFIX%%";
 
 	/** Component manager */
 	private QComponentManager qcm;
 
 	/** Owner question */
 	private StandardQuestion sqOwner;
 
 	/** Root component */
 	private QComponent qcRoot;
 
 	/** Map to speed finding of components */
 	private Map<String,QComponent> mFoundIDs=new HashMap<String,QComponent>();
 
 	/** CSS for question */
 	private String sCSS=null;
 
 	/** JS for question */
 	private String sJS=null;
 
 	/** List of group names (String) */
 	private List<String> lGroups=new LinkedList<String>();
 
 	/** Map of sequence numbers associated with elements (Element -> Integer) */
 	private Map<Element,Integer> mSequences=new HashMap<Element,Integer>();
 
 	/**
 	 * Constructs based on a given XML document.
 	 * @param sqOwner Owning question
 	 * @param d Document that this maps
 	 * @param qcm Manager with list of components
 	 * @throws OmException
 	 */
 	public QDocument(StandardQuestion sqOwner,Document d,QComponentManager qcm) throws OmException
 	{
 		this.qcm=qcm;
 		this.sqOwner=sqOwner;
 
 		// Attach sequence numbers to all elements
 		fillSequences(d.getDocumentElement(),0);
 
 		// Build component tree
 		qcRoot=new RootComponent();
 		qcRoot.init(null,this,d.getDocumentElement(),true);
 		mSequences=null;
 
 		if(sqOwner.isPlainMode())
 		{
 			sCSS=""; sJS="";
 		}
 		else
 		{
 			// Work out CSS and JS
 			StringBuffer sbCSS=new StringBuffer(),sbJS=new StringBuffer();
 			Set<Class<?> > sClassesDone=new HashSet<Class<?> >();
 			List<QComponent> lAll=new LinkedList<QComponent>();
 			unrollTree(qcRoot,lAll);
 			for(QComponent qc : lAll)
 			{
 				boolean bFirst=!sClassesDone.contains(qc.getClass());
 				if(bFirst) sClassesDone.add(qc.getClass());
 				String sThis=qc.getCSS(bFirst);
 				if(sThis!=null) sbCSS.append(sThis);
 				sThis=qc.getJS(bFirst);
 				if(sThis!=null) sbJS.append(sThis);
 			}
 			sCSS=sbCSS.toString();
 			sJS=sbJS.toString();
 		}
 	}
 
 	/**
 	 * Attaches a sequence number to each XML element in the document, for
 	 * use in generating IDs that change only based on XML changes, not
 	 * on program code changes.
 	 */
 	private int fillSequences(Element eStart,int iIndex)
 	{
 		mSequences.put(eStart,new Integer(iIndex++));
 		for(Node n=eStart.getFirstChild();n!=null;n=n.getNextSibling())
 		{
 			if(n instanceof Element)
 			{
 				iIndex=fillSequences((Element)n,iIndex);
 			}
 		}
 		return iIndex;
 	}
 
 	/**
 	 * Obtain unique sequence for given element from document. Valid only during
 	 * component init.
 	 * @param e Element
 	 * @return Unique sequence ID
 	 */
 	public int getSequence(Element e)
 	{
 		return mSequences.get(e);
 	}
 
 	/**
 	 * Unrolls the tree into a list of all components.
 	 * @param qcParent Parent component
 	 * @param c Collection that will receive parent and all its children
 	 * @throws OmDeveloperException
 	 */
 	private void unrollTree(QComponent qcParent,Collection<QComponent> c)
 		throws OmDeveloperException
 	{
 		c.add(qcParent);
 		QComponent[] acChildren=qcParent.getComponentChildren();
 		for(int iChild=0;iChild<acChildren.length;iChild++)
 		{
 			unrollTree(acChildren[iChild],c);
 		}
 	}
 
 	/**
 	 * Adds all components within the parent tag to the list of children. Does
 	 * not include text nodes.
 	 * @param qcParent Parent component that will own newly-created children
 	 * @param eParent Parent XML element
 	 * @throws OmException Any errors creating components
 	 */
 	public void buildInside(QComponent qcParent,Element eParent) throws OmException
 	{
 		for(Node n=eParent.getFirstChild();n!=null;n=n.getNextSibling())
 		{
 			if(n instanceof Element)
 			{
 				qcParent.addChild(build(qcParent,(Element)n,null));
 			}
 		}
 	}
 
 	/**
 	 * Adds all components within the parent tag to the list of children,
 	 * including text nodes.
 	 * @param qcParent Parent component that will own newly-created children
 	 * @param eParent Parent XML element
 	 * @throws OmException Any errors creating components
 	 */
 	public void buildInsideWithText(QComponent qcParent,Element eParent) throws OmException
 	{
 		StringBuffer sbText=new StringBuffer();
 		for(Node n=eParent.getFirstChild();n!=null;n=n.getNextSibling())
 		{
 			if(n instanceof Element)
 			{
 				if(sbText.length()>0)
 				{
 					qcParent.addChild(sbText.toString());
 					sbText.setLength(0);
 				}
 				qcParent.addChild(build(qcParent,(Element)n,null));
 			}
 			else if(n instanceof Text)
 			{
 				// Appending text to buffer allows us to join up text nodes where
 				// there are multiple nodes for one string (e.g. if there's CDATA
 				// in the middle or something)
 				sbText.append(n.getNodeValue());
 			}
 		}
 		if(sbText.length()>0)
 		{
 			qcParent.addChild(sbText.toString());
 			sbText.setLength(0);
 		}
 	}
 
 	/**
 	 * Adds all components within the parent tag to the list of children,
 	 * except ignoring all tags named.
 	 * @param qcParent Parent component that will own newly-created children
 	 * @param eParent Parent XML element
 	 * @param asExcludeTags Tags to exclude
 	 * @throws OmException Any errors creating components
 	 */
 	public void buildInsideExcept(QComponent qcParent,Element eParent,
 		String[] asExcludeTags) throws OmException
 	{
 		nodeloop: for(Node n=eParent.getFirstChild();n!=null;n=n.getNextSibling())
 		{
 			if(n instanceof Element)
 			{
 				Element e=(Element)n;
 				for(int i=0;i<asExcludeTags.length;i++)
 				{
 					if(e.getTagName().equals(asExcludeTags[i]))
 						continue nodeloop;
 				}
 				qcParent.addChild(build(qcParent,e,null));
 			}
 		}
 	}
 
 	/**
 	 * Builds component for the given element (which should also build any children)
 	 * @param qcParent the parent component.
 	 * @param e Element the element in question.xml that corresponds to this component.
 	 * @param sTagName Must be null if the element belongs to the component
 	 *   being created; set it to override the component name if the
 	 *   component is implicit (has no parent element)
 	 * @return QComponent that was created
 	 * @throws OmException Any errors creating component
 	 */
 	public QComponent build(QComponent qcParent,Element e,String sTagName) throws OmException
 	{
 		// Create component
 		QComponent qc=qcm.create(sTagName!=null ? sTagName : e.getTagName());
 
 		// Initialise it (this will recursively call build/buildInside again
 		// as necessary)
 		qc.init(qcParent,this,e,sTagName!=null);
 
 		return qc;
 	}
 
 	/**
 	 * Searches the tree for the component with given ID.
 	 * <p>
 	 * Works for both user-set IDs and self-assigned ones (though question
 	 * authors probably shouldn't rely on the latter).
 	 * <p>
 	 * This is efficient so call it lots if you want to; don't bother caching
 	 * the result.
 	 * @param sID Required ID
 	 * @return the component found
 	 * @throws OmDeveloperException If the component does not exist
 	 */
 	public QComponent find(String sID) throws OmDeveloperException
 	{
 		// If it's in the cache, just return it
 		if(mFoundIDs.containsKey(sID))
 			return mFoundIDs.get(sID);
 
 		// Look through tree for it
 		QComponent qcFound=qcRoot.findSubComponent(sID);
 		if(qcFound!=null)
 		{
 			mFoundIDs.put(sID,qcFound);
 			return qcFound;
 		}
 
 		throw new OmDeveloperException("Can't find component id: "+sID);
 	}
 
 	/**
 	 * Searches the tree for components of a given class. Returns an array of
 	 * the components, in document order. You may cast the array to one of the
 	 * specifed class.
 	 * @param <T> The type of component to find.
 	 * @param c Class to look for
 	 * @return Array of components (zero-length if none)
 	 */
 	public <T extends QComponent> List<T> find(Class<T> c)
 	{
 		List<T> l=new LinkedList<T>();
 		qcRoot.listSubComponents(c,l);
 		return l;
 	}
 
 	/**
 	 * Renders question components into output.
 	 * @param r Output for rendering
 	 * @param bInit True if this is the first call to components in question init
 	 * @throws OmException If there's any problem
 	 */
 	public void render(Rendering r,boolean bInit) throws OmException
 	{
 		// Create blank QContent
 		QContent qc=new QContent(XML.createDocument());
 
 		boolean bPlain=getQuestion().isPlainMode();
 
 		// First time round, we set the CSS and JS - except in plain mode which doesn't have 'em
 		if(bInit && !bPlain)
 		{
 			r.setCSS(sCSS);
 			try
 			{
 				r.addResource(new Resource("script.js","text/javascript","UTF-8",sJS.getBytes("UTF-8")));
 			}
 			catch(UnsupportedEncodingException e)
 			{
 				throw new OmUnexpectedException(e);
 			}
 		}
 
 		// Actually get output from all components
 		qcRoot.produceOutput(qc,bInit,bPlain);
 
 		// Set output in QContent to the Rendering object
 		r.setXHTML(qc.getXHTML());
 		Resource[] ar=qc.getResources();
 	  for(int iResource=0;iResource<ar.length;iResource++)
 		{
 			r.addResource(ar[iResource]);
 		}
 	}
 
 	/**
 	 * Handles a received action, calling into the given question if specified.
 	 * @param ap Parameters of received action
 	 * @throws OmException If there's any problem
 	 */
 	public void action(ActionParams ap) throws OmException
 	{
 		String[] asParams=ap.getParameterList();
 
 		// Send the specific value sets to their components
 		for(int iParam=0;iParam<asParams.length;iParam++)
 		{
 			if(asParams[iParam].startsWith(VALUE_PREFIX))
 				find(asParams[iParam].substring(VALUE_PREFIX.length())).formSetValue(
 					ap.getParameter(asParams[iParam]),ap);
 		}
 
 		// Tell all components that values were set (this allows components that
 		// either receive something or nothing, like checkboxes, to work)
 		informFormValuesSet(qcRoot,ap);
 
 		// Pass on action calls
 		for(int iParam=0;iParam<asParams.length;iParam++)
 		{
 			if(asParams[iParam].startsWith(ACTION_PREFIX))
 				find(asParams[iParam].substring(ACTION_PREFIX.length())).formCallAction(
 					ap.getParameter(asParams[iParam]),ap);
 		}
 	}
 
 	/**
 	 * Calls the formAllValuesSet() method on all components recursively.
 	 * @param qc Root component to begin with
 	 * @param ap ActionParams from form submit
 	 */
 	private void informFormValuesSet(QComponent qc,ActionParams ap) throws OmException
 	{
 		qc.formAllValuesSet(ap);
 
 		QComponent[] acChildren=qc.getComponentChildren();
 		for(int iChild=0;iChild<acChildren.length;iChild++)
 		{
 			informFormValuesSet(acChildren[iChild],ap);
 		}
 	}
 
 	/** @return Question that owns this document */
 	public StandardQuestion getQuestion()
 	{
 		return sqOwner;
 	}
 
 	/**
 	 * Informs document that a component (and any children it may have) has
 	 * been removed.
 	 * @param qc Removed component
 	 */
 	public void informRemoved(QComponent qc)
 	{
 		// Remove any entries from the cached ID map
 		for(Iterator<Map.Entry<String,QComponent> > i=mFoundIDs.entrySet().iterator();i.hasNext();)
 		{
 			Map.Entry<String,QComponent> me=i.next();
 			if(me.getValue()==qc)
 			{
 				i.remove();
 			}
 		}
 
 		// Recurse to children
 		QComponent[] aqc=qc.getComponentChildren();
 		for(int i=0;i<aqc.length;i++)
 		{
 			informRemoved(aqc[i]);
 		}
 	}
 
 	/**
 	 * Given a 'group' name, turns it into a number. The first group is assigned
 	 * 0, second is assigned 1, etc.
 	 * @param sGroup Group name
 	 * @return Group index
 	 */
 	public int getGroupIndex(String sGroup)
 	{
 		int iCount=0;
 		for(String s : lGroups)
 		{
			if (s.equals(sGroup)) return iCount;
			iCount++;
 		}
 		lGroups.add(sGroup);
 		return iCount;
 	}
 }
