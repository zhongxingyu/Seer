 package org.xbrlapi.networks;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.Arc;
 import org.xbrlapi.ArcEnd;
 import org.xbrlapi.ExtendedLink;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.Locator;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.data.XBRLStore;
 import org.xbrlapi.impl.FragmentListImpl;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class NetworkImpl implements Network {
 
 	protected static Logger logger = Logger.getLogger(Loader.class);	
 	
 	/**
 	 * The link role for the network.
 	 */
 	private String linkRole = null;
 	
 	/**
 	 * The arc role for the network.
 	 */
 	private String arcRole = null;
 
 	/**
 	 * The data store to retrieve fragments if they are not already retrieved.
 	 */
 	private Store store = null;
 		
 	private HashMap<String,Relationship> relationships = new HashMap<String,Relationship>();
 	
 	/**
 	 * The map of fragments involved in the relationships in the network.
 	 */
 	private HashMap<String,Fragment> fragments = new HashMap<String,Fragment>();
 
 	/**
 	 * The map from sources to relationships. The maps are indexed
 	 * first by the source fragment index and then by the semantic key 
 	 * for the relationship.
 	 */
 	private HashMap<String,HashMap<String,EquivalentRelationships>> sourceRelationships = new HashMap<String,HashMap<String,EquivalentRelationships>>();
 	
 	/**
 	 * The map from targets to relationships.  The maps are indexed
 	 * first by the target fragment index and then by the semantic key 
 	 * for the relationship.
 	 */
 	private HashMap<String,HashMap<String,EquivalentRelationships>> targetRelationships = new HashMap<String,HashMap<String,EquivalentRelationships>>();
 	
 	/**
 	 * @param store The data store.
 	 * @param linkRole The link role defining the network.
 	 * @param arcRole The arc role defining the network.
 	 * @throws XBRLException if the data store is null.
 	 */
 	public NetworkImpl(Store store, String linkRole, String arcRole) throws XBRLException {
 		super();
 		if (store == null) throw new XBRLException("The store must not be null.");
 		setStore(store);
 		setLinkRole(linkRole);
 		setArcRole(arcRole);
 	}
 
 	/**
 	 * @see org.xbrlapi.networks.Network#getArcRole()
 	 */
 	public String getArcRole() {
 		return arcRole;
 	}
 
 	/**
 	 * @see org.xbrlapi.networks.Network#setArcRole(String)
 	 */
 	public void setArcRole(String arcRole) throws XBRLException {
 		 if (arcRole == null) throw new XBRLException("The network arcrole must not be set to null");
 		this.arcRole = arcRole;
 	}
 
 	/**
 	 * @see org.xbrlapi.networks.Network#getLinkRole()
 	 */
 	public String getLinkRole() {
 		return linkRole;
 	}
 
 	/**
 	 * @see org.xbrlapi.networks.Network#setLinkRole(String)
 	 */
 	public void setLinkRole(String linkRole) throws XBRLException {
 		 if (linkRole == null) throw new XBRLException("The network link role must not be set to null");
 		this.linkRole = linkRole;
 	}
 		
 	/** 
 	 * @see org.xbrlapi.networks.Network#hasFragment(String)
 	 */
 	public boolean hasFragment(String index) throws XBRLException {
 		if (fragments.containsKey(index)) return true;
 		return false;
 	}
 	
 	/** 
 	 * @see org.xbrlapi.networks.Network#getFragment(String)
 	 */
 	public Fragment getFragment(String index) throws XBRLException {
 		Fragment fragment = fragments.get(index);
 		if (fragment == null) {
 			if (getStore()== null) return null;
 			fragment = getStore().getFragment(index);
 			fragments.put(index,fragment);
 		}
 		return fragment;
 	}
 
 	/**
 	 * @see org.xbrlapi.networks.Network#getRootFragments()
 	 */
 	@SuppressWarnings("unchecked")
 	public <F extends Fragment> FragmentList<F> getRootFragments() {
 		FragmentList<F> fragmentList = new FragmentListImpl<F>();
 		Set<String> rootIndexes = getRootFragmentIndexes();
 		for (String index: rootIndexes) {
 			fragmentList.add((F) this.fragments.get(index));
 		}
 		return fragmentList;
 	}
 	
 	/**
 	 * @see org.xbrlapi.networks.Network#getRootFragmentIndexes()
 	 */
 	public Set<String> getRootFragmentIndexes() {
 		Set<String> sourceIndexes = sourceRelationships.keySet();
 		Set<String> targetIndexes = targetRelationships.keySet();
 		Set<String> rootIndexes = new HashSet<String>(sourceIndexes);
 		rootIndexes.removeAll(targetIndexes);
 		return rootIndexes;
 	}
 
 	/**
 	 * Add the fragment to the set of fragments participating in 
 	 * the network.  Initialises the store property if it has
 	 * not already been done.
 	 * @param fragment The fragment to add.
 	 */
 	private void addFragment(Fragment fragment) {
 		if (store == null) store = fragment.getStore();
 		fragments.put(fragment.getFragmentIndex(),fragment);
 	}
 	
 	/**
 	 * @return the data store for the network.
 	 */
 	private Store getStore() {
 		return store;
 	}
 	
 	/**
 	 * @param store The data store for the network.
 	 * @throws XBRLException if the data store is null.
 	 */
 	private void setStore(Store store) throws XBRLException {
 		if (store == null) throw new XBRLException("The data store for the network must not be null.");
 		this.store = store;
 	}
 	
 	/**
 	 * @see org.xbrlapi.networks.Network#addRelationship(org.xbrlapi.networks.Relationship)
 	 */
 	public void addRelationship(Relationship relationship) throws XBRLException {
 		
 		// Ensure that the relationship belongs
 		if (! getLinkRole().equals(relationship.getLinkRole())) throw new XBRLException("The network link role does not match that of the relationship.");
 		if (! getArcRole().equals(relationship.getArcRole())) throw new XBRLException("The network arc role does not match that of the relationship.");
 
         String semanticKey = relationship.getSemanticKey();
         Fragment source = relationship.getSource();
         String sourceIndex = source.getFragmentIndex();
 		
 		// Make sure the relationship is not already in the network.
 		String targetsSemanticKey = semanticKey + sourceIndex;
 		if (this.targetRelationships.containsKey(targetsSemanticKey)) {
 		    return;// The relationship is already recorded in the network
 		}
 
         Fragment target = relationship.getTarget();
         String targetIndex = target.getFragmentIndex();
         String sourcesSemanticKey = semanticKey + targetIndex;
 
         // Inform the relationship of the network it is in
         relationship.setNetwork(this);
 
         if (! fragments.containsKey(targetIndex)) fragments.put(targetIndex,target);
         if (! fragments.containsKey(sourceIndex)) fragments.put(sourceIndex,source);
 		
 		// Store the fragments in the relationship
 		Arc arc = relationship.getArc();
 		String arcIndex = arc.getFragmentIndex();
 		if (! fragments.containsKey(arcIndex)) fragments.put(arcIndex,arc);
 		
 		ExtendedLink link = relationship.getLink();
 		String linkIndex = arc.getFragmentIndex();
 		if (! fragments.containsKey(linkIndex)) fragments.put(linkIndex,link);
 		
 		HashMap<String,EquivalentRelationships> fragmentRelationships = null;
 		EquivalentRelationships er = null;
 		if (sourceRelationships.containsKey(sourceIndex)) {
 			fragmentRelationships = sourceRelationships.get(sourceIndex);
 			if (fragmentRelationships.containsKey(sourcesSemanticKey)) {
 				er = fragmentRelationships.get(sourcesSemanticKey);
 			} else {
 				er = new EquivalentRelationshipsImpl();
 	            fragmentRelationships.put(sourcesSemanticKey,er);
 			}
 		} else {
 			fragmentRelationships = new HashMap<String,EquivalentRelationships>();
 			er = new EquivalentRelationshipsImpl();
 			fragmentRelationships.put(sourcesSemanticKey,er);
 			sourceRelationships.put(sourceIndex,fragmentRelationships);
 		}
 		er.addRelationship(relationship);
 
 		if (targetRelationships.containsKey(targetIndex)) {
 			fragmentRelationships = targetRelationships.get(targetIndex);
 			if (fragmentRelationships.containsKey(targetsSemanticKey)) {
 				er = fragmentRelationships.get(targetsSemanticKey);
 			} else {
 				er = new EquivalentRelationshipsImpl();
                 fragmentRelationships.put(targetsSemanticKey,er);
 			}
 		} else {
 			fragmentRelationships = new HashMap<String,EquivalentRelationships>();
 			er = new EquivalentRelationshipsImpl();
 			fragmentRelationships.put(targetsSemanticKey,er);
 			targetRelationships.put(targetIndex,fragmentRelationships);
 		}
 		er.addRelationship(relationship);
 
 	}
 	
 	/**
 	 * @return a String that will be identical the 
 	 * same source and the same target.
 	 * @throws XBRLException
 	 */	
 	private String getEndFragmentsKey(String source, String target) {
 		return source + "|" + target;
 	}	
 	
 	/**
 	 * @see org.xbrlapi.networks.Network#getActiveRelationshipsFrom(String)
 	 */
 	public List<Relationship> getActiveRelationshipsFrom(String index) throws XBRLException {
 
 	    logger.debug("Getting active relationships from " + index + " for " + this);
 	    
 		List<Relationship> activeRelationships = new Vector<Relationship>();
 
 		if (! sourceRelationships.containsKey(index)) return activeRelationships;
 		
 		// Sort the relationships based on their order attribute.
 		HashMap<String,EquivalentRelationships> sr = sourceRelationships.get(index);
 		logger.debug("There are " + sr.size() + " source relationships.");
         TreeMap<Double,List<Relationship>> sortedRelationships = new TreeMap<Double,List<Relationship>>();
 		for (String key: sr.keySet()) {
 			EquivalentRelationships er = sr.get(key);
             Relationship relationship = er.getActiveRelationship();
             Double order = new Double(relationship.getOrder());
             if (sortedRelationships.containsKey(order)) {
                 sortedRelationships.get(order).add(relationship);
             } else {
                 List<Relationship> relationships = new LinkedList<Relationship>();
                 relationships.add(relationship);
                 sortedRelationships.put(order,relationships);
             }
 		}
 
 		for (List<Relationship> relationships: sortedRelationships.values()) {
 
 		    for (Relationship relationship: relationships) {
 		        activeRelationships.add(relationship);
 		    }
 		}
 		
 		return activeRelationships;
 	}
 	
 	/**
 	 * @see Network#hasSingleParent(String)
 	 */
     public boolean hasSingleParent(String index) throws XBRLException {
         return (this.getActiveRelationshipsTo(index).size() == 1);
     }
     
     /**
     * @see Network#isRoot(String)
      */
     public boolean isRoot(String index) throws XBRLException {
         return (this.getActiveRelationshipsTo(index).size() == 0);
     }
     
     /**
      * @see Network#isLeaf(String)
      */
     public boolean isLeaf(String index) throws XBRLException {
         return (this.getActiveRelationshipsFrom(index).size() == 0);
     }	
 	
     /**
      * @see org.xbrlapi.networks.Network#hasActiveRelationshipsFrom(String)
      */
     public boolean hasActiveRelationshipsFrom(String index) {
         return sourceRelationships.containsKey(index);
     }
     
     /**
      * @see org.xbrlapi.networks.Network#hasActiveRelationshipsTo(String)
      */
     public boolean hasActiveRelationshipsTo(String index) {
         return (targetRelationships.containsKey(index));
     }
     
 	
 	/**
 	 * @see org.xbrlapi.networks.Network#getActiveRelationshipsTo(String)
 	 */
 	public List<Relationship> getActiveRelationshipsTo(String index) throws XBRLException {
 
 		List<Relationship> activeRelationships = new LinkedList<Relationship>();
 
 		if (! targetRelationships.containsKey(index)) return activeRelationships;
 		
 		// Get the map from semantic keys to sets of equivalent relationships
 		HashMap<String,EquivalentRelationships> tr = targetRelationships.get(index);
 
 		Iterator<String> iterator = tr.keySet().iterator();
 		while (iterator.hasNext()) {
 			EquivalentRelationships er = tr.get(iterator.next());
 			activeRelationships.add(er.getActiveRelationship());
 		}
 
 		return activeRelationships;
 	}
 
     /**
      * @see Network#getChildren(String)
      */
     @SuppressWarnings("unchecked")
     public <F extends Fragment> FragmentList<F> getChildren(String index)
             throws XBRLException {
         FragmentList<F> children = new FragmentListImpl<F>();
         for (Relationship relationship: this.getActiveRelationshipsFrom(index)) {
             children.add((F) relationship.getTarget());
         }
         return children;
     }
 
 
     /**
      * @see Network#getParents(String)
      */
     @SuppressWarnings("unchecked")
     public <F extends Fragment> FragmentList<F> getParents(String index) throws XBRLException {
         FragmentList<F> parents = new FragmentListImpl<F>();
         for (Relationship relationship: this.getActiveRelationshipsTo(index)) {
             parents.add((F) relationship.getSource());
         }
         return parents;
     }
 
     /**
      * @see Network#getNumberOfRelationships()
      */
     public int getNumberOfRelationships() {
         int count = 0;
         for (HashMap<String,EquivalentRelationships> map: this.sourceRelationships.values()) {
             for (EquivalentRelationships relationships: map.values()) {
                 count += relationships.getLength();
             }
         }
         return count;
     }
 
     /**
      * @see Network#getNumberOfActiveRelationships()
      */
     public int getNumberOfActiveRelationships() {
         int count = 0;
         for (HashMap<String,EquivalentRelationships> map: this.sourceRelationships.values()) {
             count += map.size();
         }
         return count;
     }
     
     /**
      * @see Network#complete()
      */
     public void complete() throws XBRLException {
         
         logger.debug("Completing network with arcrole " + this.getArcRole() + " and link role " + getLinkRole());
 
         // Get the arcs that define relationships in the network
         FragmentList<ExtendedLink> links = ((XBRLStore) this.getStore()).getExtendedLinksWithRole(this.getLinkRole());
         for (ExtendedLink link: links) {
             FragmentList<Arc> arcs = link.getArcsByArcrole(this.getArcRole());
             for (Arc arc: arcs) {
                 FragmentList<ArcEnd> sources = arc.getSourceFragments();
                 FragmentList<ArcEnd> targets = arc.getTargetFragments();
                 for (Fragment source: sources) {
                     Fragment s = source;
                     if (source.isa("org.xbrlapi.impl.LocatorImpl")) s = ((Locator) source).getTargetFragment();
                     for (Fragment target: targets) {
                         Fragment t = target;
                         if (target.isa("org.xbrlapi.impl.LocatorImpl")) t = ((Locator) target).getTargetFragment();
                         Relationship relationship = new RelationshipImpl(arc,s,t);
                         this.addRelationship(relationship);
                     }
                 }
             }
         }
 
     }
 
     /**
      * @see Network#contains(String)
      */
     public boolean contains(String index) {
         return fragments.containsKey(index);
     }
     
 }
