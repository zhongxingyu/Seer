 package com.aavu.client.domain;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.aavu.client.domain.dto.TopicIdentifier;
 import com.aavu.client.domain.generated.AbstractTopic;
 import com.aavu.client.domain.util.SetUtils;
 import com.aavu.client.widget.autocompletion.Completable;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * The base storable db object. superclass of associations & metas.
  * 
  * 
  * NOTE that you may need to use the visitor pattern and TopicVisitor to get reliable results
  * when examining subclasses. See TopicVisitor and http://www.hibernate.org/280.html. This is not 
  * an issue in the GWT client bc the clone (below) used by the serializer gets rid of the proxies. 
  * 
  * @author Jeff Dwyer
  *
  */
 public class Topic extends AbstractTopic  implements Completable, IsSerializable, ReallyCloneable {
 
 
 	public Topic(){
 		setLastUpdated(new Date());
 		setCreated(new Date());
 		setTitle("");		
 	}
 
 	public Topic(JSONObject value) {
 		setId((long) ((JSONNumber)value.get("id")).getValue());
 		setTitle(((JSONString)value.get("title")).stringValue());
 		setTitle(((JSONString)value.get("data")).stringValue());
 	}
 
 	
 	public Topic(User u, String title) {
 		this();
 		setUser(u);
 		setTitle(title);
 	}
 
 	/**
 	 * basically a temporary wrapper for a TopicIdentifier to masquerade as a real Topic
 	 * 
 	 * Used by SeeAlso
 	 */
 	public Topic(TopicIdentifier topicIdent) {
 		setId(topicIdent.getTopicID());
 		setTitle(topicIdent.getTopicTitle());		
 	}
 
 	/**
 	 * TODO move these clones to their subclasses
 	 * 
 	 * NOTE!!: Forgetting to put an appropriate entry in here can really make you scratch
 	 * your head.
 	 * 
 	 * Switch is needed to keep type information over serialization
 	 * 
 	 * 
 	 */
 	public Object clone() {		
 		Topic o = null;
 		if(this instanceof MetaText)
 			o = new MetaText();
 		else if(this instanceof MetaDate)
 			o = new MetaDate();		
 		else if(this instanceof MetaTopic)
 			o = new MetaTopic();
 		else if(this instanceof MetaLocation)
 			o = new MetaLocation();
 		else if(this instanceof MetaSeeAlso)
 			o = new MetaSeeAlso();
 		else if(this instanceof HippoDate)
 			o = new HippoDate();
 		else if(this instanceof HippoText)
 			o = new HippoText();
 		else if(this instanceof HippoLocation)
 			o = new HippoLocation();		
 		else if(this instanceof Association)
 			o = new Association();
 		else if(this instanceof Tag)
 			o = new Tag();
 		else {
 			o = new Topic();
 		}
 		
 		copyPropsIntoParam(o);
 //		o.setId(getId());
 //		o.setCreated(getCreated());
 //		o.setUser(getUser());
 //		o.setLastUpdated(getLastUpdated());
 //		o.setLatitude(getLatitude());
 //		o.setLongitude(getLongitude());
 //		o.setTitle(getTitle());		
 		return o;
 	}
 	public void copyPropsIntoParam(Topic o){
 		o.setId(getId());
 		o.setCreated(getCreated());
 		o.setUser(getUser());
 		o.setLastUpdated(getLastUpdated());
 		o.setLatitude(getLatitude());
 		o.setLongitude(getLongitude());
 		o.setTitle(getTitle());
 	}
 	public void copyPropsButNotIDIntoParam(Topic o){		
 		o.setCreated(getCreated());
 		o.setUser(getUser());
 		o.setLastUpdated(getLastUpdated());
 		o.setLatitude(getLatitude());
 		o.setLongitude(getLongitude());
 		o.setTitle(getTitle());
 	}
 	
 	public void accept(TopicVisitor visitor) {
 		visitor.visit(this);
 	}
 	
 	
 	/**
 	 * NOTE! this will clear() the members. Shouldn't really be necessary and can 
 	 * lead to DB-leaks if you don't remember to actually delete things as well, but 
 	 * we're using it because of the set.add() duplication annoyances. 
 	 * 
 	 * @param meta
 	 * @param metaValue
 	 */
 	public void addMetaValue(Meta meta, Topic metaValue) {
 		addMetaValue(meta, metaValue,true);
 	}
 	/**
 	 * some meta values really only want 1 meta value per meta, so they'll ask us to clear the members
 	 * set before adding the new one. This currently results in a db-leak though.
 	 * 
 	 * If you don't want it to clear (multiple mv's ie MetaLocation) you need to use clear == false;
 	 * 
 	 * @param meta
 	 * @param metaValue
 	 * @param clear
 	 */
 	public void addMetaValue(Meta meta, Topic metaValue,boolean clear) {
 			
 		
 		System.out.println("Topic.addMetaValue: "+meta+" "+metaValue+" clear "+clear);
 		
 //		Topic cur_val = getSingleMetaValueFor(meta);
 //		if(cur_val != null){
 //			
 //		}		
 		
 		Association assoc = getAssociationForMetaOrNull(meta);
 		if(assoc == null){
 			System.out.println("Topic.addMetaValue: create new assoc");
 			assoc = new Association(this);
 			
 			
 			
 		}else{
 			System.out.println("Topic.addMetaValue assoc.member.size() "+assoc.getMembers().size());
 			if(assoc.getMembers().size() > 0){
 				Topic cur_val = (Topic) assoc.getMembers().iterator().next();
 				System.out.println("Topic.addMetaValue cur_v "+cur_val);			
 
 				if(cur_val.equals(metaValue)){
 					System.out.println("Topic.addMetaValue Were Equal, copy props to make sure. C: "+cur_val.getTitle()+" mv "+metaValue.getTitle());
 					metaValue.copyPropsIntoParam(cur_val);				
 					return;
 				}else{
 					System.out.println("Topic.addMetaValue != cur "+cur_val.getIdentifier());
 					System.out.println("Topic.addMetaValue != new "+metaValue.getIdentifier());
 				}
 			}
 		}
 		
 		System.out.println("assoc "+assoc+" metav "+metaValue);
 		assoc.setTitle(getTitle()+" to "+metaValue.getTitle());
 		
 		//redundant if we've already created
 		assoc.addType(meta);
 		
 		if(clear){
 			System.out.println("Topic.addMetaValue clearing");
 			assoc.getMembers().clear();
 		}
 		assoc.getMembers().add(metaValue);
 		
 		
 		System.out.println("Topic.addMetaValue bf assoc "+getAssociations().size()+" ");
 		
 		boolean contains = false;
 		if(!getAssociations().isEmpty()){
 			Association cur1 = (Association) getAssociations().iterator().next();
 			System.out.println("Topic.addMetaValue CMP "+cur1.compare(assoc));
 			
 			for (Iterator iter = getAssociations().iterator(); iter.hasNext();) {
 				Association cur = (Association) iter.next();
 				
 				System.out.println("Topic.addMetaValue cmp 2 "+cur.compare(assoc));
 				if(cur.equals(assoc)){
 					contains = true;
 				}
 			}
 		}
 		System.out.println("Contains: "+contains+" if true adding association.");
 		
 		//redundant if we've already created, which would be ok except for the fact that 
 		//set.add(assoc) will add it even if it loop, set.eq() says it contains it 
 		if(!contains){
 			getAssociations().add(assoc);
 		}
 		
 		System.out.println("Topic.addMetaValue after assoc "+getAssociations().size());
 		
 		
 		//System.out.println(topic+"size "+topic.getMetaValues());
 	}
 	
 	
 
 	public void setMetaValue(Meta meta, Topic metaValue){
 		getMetaValuesFor(meta).add(metaValue);
 	}
 
 
 	public boolean tagTopic(Topic tag){
 
 
 //		System.out.println("tagging with "+tag.getTitle());
 //		System.out.println("alread tagged "+tag.getInstances());
 //		System.out.println("alread tagged "+tag.getInstances().size());
 
 //		Window.alert("FOO ");
 //		Window.alert("f: "+tag.getInstances()+" ");
 //		for (Iterator iter = tag.getInstances().iterator(); iter.hasNext();) {
 //		Topic element = (Topic) iter.next();
 //		Window.alert("Tag's instances: "+element.toPrettyString());
 //		}
 //		Window.alert("this: "+toPrettyString());
 
 		
 		
 		//NOTE! shoudl be unecessary
 		//tag.getInstances().add(this);
 		
 		
 		//Window.alert("FOO2");
 //		Window.alert("f: "+getTypes());
 
 //		Window.alert("tag "+tag);
 //		Window.alert("pretty "+tag.toPrettyString());
 
 
 		return addType(tag);
 		
 //		Window.alert("FOO3");
 
 	}
 
 	/**
 	 * 
 	 * @param tag
 	 * @return
 	 */
 	public boolean removeTag(Tag tag) {
 
 //		System.out.println("REMOVE-------------------");
 //		System.out.println(tag.toPrettyString());
 //		System.out.println("FROM-------------------");
 //		System.out.println(toPrettyString());
 		
 		//boolean b = true;//tag.getInstances().remove(this);
 		
 		return removeType(tag);
 		
 	}
 	
 	/**
 	 * wack the association.
 	 * 
 	 * definitely don't wack the meta.
 	 * TODO would like to wack the associations, members... but only if they're
 	 * not referenced by anybody else. 
 	 * 
 	 * @param meta
 	 * @return
 	 */
 	public boolean removeMeta(Meta meta) {		
 		Association assoc = getAssociationForMetaOrNull(meta);
 		if(assoc == null){
 			return false;
 		}		
 		return SetUtils.removeFromSetById(getAssociations(), assoc.getId());		
 	}
 
 	
 	public Set getEntries(){
 		Set rtn = new HashSet();
 		for (Iterator iter = getOccurences().iterator(); iter.hasNext();) {
 			Occurrence occur = (Occurrence) iter.next();
 			if(occur instanceof Entry){
 				rtn.add((Entry) occur);
 			}
 		}
 		return rtn;
 	}
 	
 	public Entry getLatestEntry(){
 		
 		Set entries = getEntries();
 		
 		if(entries.isEmpty()){
 			Entry initialEntry = new Entry();
 			getOccurences().add(initialEntry);
 			initialEntry.getTopics().add(this);
 			return initialEntry;
 		}else{
 			return (Entry) entries.iterator().next();
 		}
 		
 	}
 
 	/**
 	 * limitted to just 1 for now.
 	 * 
 	 * @return
 	 */
 	public MindTreeOcc getMindTree() {
 		MindTreeOcc rtn = null;
 		for (Iterator iter = getOccurences().iterator(); iter.hasNext();) {
 			Occurrence occur = (Occurrence) iter.next();
 			if(occur instanceof MindTreeOcc){
 				rtn = (MindTreeOcc) occur;
 				break;
 			}
 		}
 		if(rtn == null){
 			MindTreeOcc initialTree = new MindTreeOcc(this);
 						
 			//getOccurences().add(initialTree);
 			
 			return initialTree;
 		}
 		return rtn;
 	}
 
 
 	public String toString(){
 		return getTitle();	
 	}
 
 	/**
 	 * Create an Association of type Meta.
 	 * @param meta
 	 */
 	public void addTagProperty(Meta meta) {		
 		Association a = getTagPropertyAssociation();	
 		a.getMembers().add(meta);		
 	}
 
 
 	public String getCompleteStr() {		
 		return getTitle();
 	}
 
 	public Set getTags() {
 		return getTypesAsTopics();
 	}	
 
 	
 
 	public TopicIdentifier getIdentifier() {
 		return new TopicIdentifier(getId(),getTitle());
 	}
 
 
 
 	public String toPrettyString() {
 		return toPrettyString("",this);
 	}
 	public String toPrettyString(String indent, Topic prev) {
 		
 		StringBuffer tagsStr = new StringBuffer();
 		StringBuffer metaVStr = new StringBuffer();
 		StringBuffer instanceStr = new StringBuffer();
 		StringBuffer occurencesStr = new StringBuffer();
 		
 		try{			
 			tagsStr.append(indent+"Types: \n"+indent);
 			for (Iterator iter = getTypesAsTopics().iterator(); iter.hasNext();) {
 				Topic element = (Topic) iter.next();				
 				tagsStr.append(element.getId()+" "+element.getTitle());
 				tagsStr.append("\n");
 				for (Iterator iterator = element.getTagProperties().iterator(); iterator.hasNext();) {
 					Meta meta = (Meta) iterator.next();
 					tagsStr.append(indent+"Meta: "+meta.getId()+" "+meta.getName());
 					tagsStr.append("\n");
 				}
 				tagsStr.append(indent+"     -----Type-----");
 				if(element == prev){
 					tagsStr.append("Found original. No recurse.");
 				}else{
 					tagsStr.append(element.toPrettyString("     ",this));
 				}
 				tagsStr.append("\n"+indent+"     -----End----");
 			}
 		}catch(Exception e){
 			tagsStr.append(indent+"Topic Pretty Types Errored");
 		}
 		try{			
 			metaVStr.append(indent+"Associations:\n"+indent);
 			for (Iterator iter = getAssociations().iterator(); iter.hasNext();) {
 				Association assoc = (Association) iter.next();
 				
 				metaVStr.append("ASS: "+assoc.getTitle()+" "+assoc.getId()+"\n"+indent);
 				metaVStr.append("Types:\n"+indent);
 				for (Iterator iterator = assoc.getTypesAsTopics().iterator(); iterator
 						.hasNext();) {
 					Topic type = (Topic) iterator.next();
 					metaVStr.append("T: "+type.getTitle()+" "+type.getId()+"\n"+indent);
 				}
 				metaVStr.append("Members:\n"+indent);
 				for (Iterator iterator = assoc.getMembers().iterator(); iterator
 						.hasNext();) {
 					Topic member = (Topic) iterator.next();
 					metaVStr.append("M: "+member.getTitle()+" "+member.getId()+"\n"+indent);
 				}
 								
 			}
 		}catch(Exception e){
 			metaVStr.append(indent+"Topic Pretty Association Errored");
 		}
 		
 //		try{			
 //			instanceStr.append(indent+"Instances:\n"+indent);
 //			for (Iterator iter = getInstances().iterator(); iter.hasNext();) {
 //				TopicTypeConnector conn = (TopicTypeConnector) iter.next();
 //				Topic instance = conn.getTopic();
 //				instanceStr.append("Instance: "+instance.getTitle()+" "+instance.getId()+"\n"+indent);							
 //			}
 //		}catch(Exception e){
 //			e.printStackTrace();
 //			instanceStr.append(indent+"Topic Pretty Instances Errored "+e);
 //		}
 		try{			
 			occurencesStr.append(indent+"Occurrences:\n"+indent);
 			for (Iterator iter = getOccurences().iterator(); iter.hasNext();) {
 				Occurrence occurence = (Occurrence) iter.next();
 				
 				occurencesStr.append("Occurrence: "+occurence.getTitle()+" "+occurence.getId()+" "+occurence.getData()+"\n"+indent);							
 			}
 		}catch(Exception e){
 			occurencesStr.append(indent+"Topic Pretty Occurrences Errored");
 		}
 		return "\nID "+getId()+" title "+getTitle()+"\n"+indent+
 		" "+tagsStr+"\n"+indent+
 		" "+metaVStr+"\n"+indent+
 		" "+instanceStr+"\n"+indent+
 		" "+occurencesStr+"\n"+indent+
 		"User:"+((getUser() == null)? "null" : getUser().getId()+"");
 
 	}
 
 	/**
 	 * Associations Contract will update our associations set in the DAO/Service layer.
 	 * Don't need to take care of that here.
 	 *
 	 * Will return the SeeAlso association or Create a new one if necessary.
 	 * 
 	 * WARN: for the time being don't let a Topic have multiple SeeAlso's!
 	 * (this would only be usefull if a Topic had one SeeAlso in one scope and 
 	 * a different SeeAlso in another)
 	 * 
 	 */
 	public Association getSeeAlsoAssociation() {
 		return getSeeAlsoAssociation(new MetaSeeAlso());	
 	}
 
 	private Association getSeeAlsoAssociation(MetaSeeAlso seeAlsoSingleton) {
 		
 		final Set found = new HashSet();
 		
 		TopicVisitor visitor = new TopicVisitorAdapter() {
 			public void visit(MetaSeeAlso meta) {
 				found.add(meta);
 			}			
 		};
 		
 		for (Iterator iter = getAssociations().iterator(); iter.hasNext();) {
 			Association association = (Association) iter.next();
 			for (Iterator iterator = association.getTypesAsTopics().iterator(); iterator.hasNext();) {
 				
 				Topic possibleSee = (Topic) iterator.next();				
 								
 				possibleSee.accept(visitor);
 				
 				if(!found.isEmpty()){
 					return association;
 				}				
 				
 			}
 		}		
 		System.out.println("getSeeAlsoAssociation: create new assoc");
 		
 		Association rtn = new Association(this);
 		rtn.setTitle(this.getTitle()+" to SeeAlsoUber");
 		rtn.addType(seeAlsoSingleton);
 		return rtn;
 	}
 	/**
 	 * Return all properties that this topic imposes upon child topics.
 	 * 
 	 * Use getPropertiesDueToTags() if you want the reverse of this.
 	 * 
 	 * NOTE: calling getTagProperties().add() won't do anything. Use addTagProperty()
 	 * 
 	 * These are defined by associations of no type, with a meta member.
 	 * 
 	 * Any topic should only have _1_ of these associations and that association should
 	 * have all the tagProperties as members. It could theoretically have a type of PropertiesOfThisTag.class 
 	 * 
 	 * //old comment
 	 * //This was returning MetaValue associations as well, until we added members.size() == 0 
 	 * //is this an ok fix?
 	 * 
 	 * @return
 	 */
 	public Set getTagProperties() {
 		Set metas = new HashSet();
 				
 		Association association = getTagPropertyAssociation();
 
 		for (Iterator iterator = association.getMembers().iterator(); iterator.hasNext();) {
 			Topic possibleMeta = (Topic) iterator.next();
 
			System.out.println("getTagProperties CHECKING "+possibleMeta);
 
 			//sanity check. All members of this association really should be 
 			//Metas. 
 			//
 			if (possibleMeta instanceof Meta) {							
 				metas.add(possibleMeta);				
 			}	
 		}			
 					
 		return metas;		
 	}
 	/**
 	 * Return the Association to our TagProperties, or create a new association and add it.
 	 * @return
 	 */
 	public Association getTagPropertyAssociation(){
 		for (Iterator iter = getAssociations().iterator(); iter.hasNext();) {
 			Association association = (Association) iter.next();
 				
 			//System.out.println("Topic.getTagPropertyAssociation: "+association+" Member size: "+association.getMembers().size());
 			
 			for (Iterator iterator = association.getMembers().iterator(); iterator.hasNext();) {
 				Topic possibleMeta = (Topic) iterator.next();
 			
 				//System.out.println("Checking: "+possibleMeta);
 				if (possibleMeta instanceof Meta) {							
 					return association;				
 				}	
 			}						
 		}
 		//System.out.println("No TagPropertyAssociation Found");
 		Association rtn = new Association(this);
 		rtn.setTitle("Tag Properties for "+getTitle());
 		getAssociations().add(rtn);
 		return rtn;
 	}
 	
 	/**
 	 * Inverse of getTagProperties(). Get's all metas of our tags.
 	 * 
 	 * @return
 	 */
 	public Set getPropertiesDueToTags() {
 		Set metas = new HashSet();
 		for (Iterator tagIter = getTypesAsTopics().iterator(); tagIter.hasNext();) {
 			Topic myTag = (Topic) tagIter.next();
 			for (Iterator iter = myTag.getAssociations().iterator(); iter.hasNext();) {
 				Association association = (Association) iter.next();
 										
 				for (Iterator iterator = association.getMembers().iterator(); iterator.hasNext();) {
 					Topic possibleMeta = (Topic) iterator.next();
 				
 					if (possibleMeta instanceof Meta) {							
 						metas.add(possibleMeta);				
 					}	
 				}							
 			}
 		}
 		
 		return metas;		
 	}
 	/**
 	 * Visitor pattern to avoid instanceof not working for CGLib enhanced objs.
 	 * Note, this problem only effect server side work, since our HibernateSerializer
 	 * cloning fixes this for the client.
 	 * 
 	 * See comments in TopicVisitor.java
 	 *  
 	 * @return
 	 */
 	public Set getMetas() {
 		final Set metas = new HashSet();
 		
 		TopicVisitor visitor = new TopicVisitorAdapter(){
 			//@Override
 			public void visit(Meta meta) {
 				metas.add(meta);
 			}
 
 			//@Override
 			public void visit(MetaSeeAlso meta) {
 				metas.add(meta);	
 			}			
 		};
 		
		System.out.println("getMetas() assoc size"+getAssociations().size());
 		for (Iterator iter = getAssociations().iterator(); iter.hasNext();) {
 			Association association = (Association) iter.next();
 			
			System.out.println("getMetas() assoc "+association);
 			
 			for (Iterator iterator = association.getTypesAsTopics().iterator(); iterator.hasNext();) {
 				Topic possibleMeta = (Topic) iterator.next();
 			
 				//carefull, these break GWT bc of .getClass
 //				System.out.println("getMetas() possible meta "+possibleMeta+" "+(possibleMeta instanceof Meta)+" "+possibleMeta.getId());
 //				System.out.println("poss "+possibleMeta.getClass());
 								
 				possibleMeta.accept(visitor);
 				
 			}			
 		}
 		return metas;		
 	}
 	private Association getAssociationForMetaOrNull(Meta meta) {
 		
		System.out.println("NUM assoc "+getAssociations().size());
 		for (Iterator iter = getAssociations().iterator(); iter.hasNext();) {
 			Association association = (Association) iter.next();
 			
			System.out.println("association.members.size() "+association.getMembers().size());
 			for (Iterator iterator = association.getTypesAsTopics().iterator(); iterator.hasNext();) {
 				Topic possible = (Topic) iterator.next();
 				
				System.out.println("getAssociationForMetaOrNull "+possible.getId()+" "+meta.getId());
 				
 				if (possible.getId() == meta.getId()) {	
 					return association;									
 				}	
 			}			
 		}
 		return null;
 	}
 	
 	public Set getMetaValuesFor(Meta meta){		
 		Association assoc = getAssociationForMetaOrNull(meta);
 		if(assoc != null){
 			return assoc.getMembers();
 		}else{
 			return new HashSet();	
 		}		
 	}
 	/**
 	 * Convenience method for when you know there's only one member.
 	 * 
 	 * @param meta
 	 * @return
 	 */
 	public Topic getSingleMetaValueFor(Meta meta){
 		Set s =  getMetaValuesFor(meta);
 		if(s.size() > 0)
 			return (Topic)s.iterator().next();
 		else
 			return null;
 	}
 
 	public String compare(Object other) {
 		StringBuffer sb = new StringBuffer();
 		if ( (this == other ) ) sb.append("true obj id");
 		if ( (other == null ) ) sb.append("false other null");
 		if ( !(other instanceof Topic) ) sb.append("false other diff type");
 		Topic castOther = ( Topic ) other; 
 		if(sb.length() > 0){
 			return sb.toString();
 		}
 		sb.append("Topic.Compare:\n");
 		sb.append(this.getId()+" id "+castOther.getId()+"\n");
 		sb.append("Topic.Compare ");
 		sb.append(this.getTitle()+" title "+castOther.getTitle()+"\n");
 		sb.append("Topic.Compare ");
 		sb.append(this.getCreated()+" created "+castOther.getCreated()+"\n");
 		sb.append("Topic.Compare ");
 		sb.append(this.getLatestEntry()+" data "+castOther.getLatestEntry()+"\n");
 		sb.append("Topic.Compare ");
 		sb.append(this.getUser()+" user "+castOther.getUser()+"\n");
 		sb.append("Topic.Compare ");
 		sb.append(this.isPublicVisible()+" public "+castOther.isPublicVisible()+"\n");
 		
 		sb.append("Topic.Compare EQ: "+(( (this.getTitle()==castOther.getTitle()) || ( this.getTitle()!=null && castOther.getTitle()!=null && this.getTitle().equals(castOther.getTitle()) ) )		
 		 && (this.isPublicVisible()==castOther.isPublicVisible())));
 		
 		return sb.toString();
 	}
 
 	/**
 	 * This is the add see also called in the GWT client. It will not have the 
 	 * SeeAlsoSingleton.
 	 * 
 	 * @param identifier
 	 */
 	public void addSeeAlso(TopicIdentifier identifier) {
 		Association cur = getSeeAlsoAssociation();
 		
 		cur.getMembers().add(new Topic(identifier));
 		
 		getAssociations().add(cur);
 	}
 	
 	/**
 	 * called from SaveSeeAlsoCommand.
 	 * 
 	 * This is where we inject the SeeAlsoSingleton
 	 * 
 	 * @param otherTopic
 	 * @param seeAlsoSingleton
 	 */
 	public void addSeeAlso(Topic otherTopic, MetaSeeAlso seeAlsoSingleton) {
 		Association cur = getSeeAlsoAssociation(seeAlsoSingleton);
 				
 		cur.getMembers().add(otherTopic);
 		
 		getAssociations().add(cur);
 	}
 
 
 	/**
 	 * do we need to update both sides of this association? 
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public boolean addType(Topic type) {
 		TopicTypeConnector conn = new TopicTypeConnector(this,type,-1.0,-1.0);
 		//type.getInstances().add(conn);
 		return getTypes().add(conn);
 		//return getTypes().add(new Topic(topic,-1,-1));		
 	}
 	
 	/**
 	 * Gar. This method has had some issues in the past. See r331, which once seemed to be necessary but
 	 * now doesn't seem to work. This is a good thing, as this method is much simpler.
 	 * Must have been something we did to our .equals() methods.
 	 */
 	public boolean removeType(Topic topic){
 		
 		boolean found = false;
 
 		for (Iterator iter = getTypes().iterator(); iter.hasNext();) {
 			
 			TopicTypeConnector twl = (TopicTypeConnector) iter.next();
 			
 			if(twl.getType().equals(topic)){
 				iter.remove();
 				found = true;
 			}			
 		}						
 		return found;
 	}
 	
 	
 	public Set getTypesAsTopics(){
 		
 		Set rtn = new HashSet();
 		for (Iterator iter = getTypes().iterator(); iter.hasNext();) {
 			TopicTypeConnector twl = (TopicTypeConnector) iter.next();
 			rtn.add(twl.getType());
 		}
 		return rtn;
 	}
 	
 	
 	/**
 	 * Overridable. 
 	 * 
 	 * Used by TopicDAO to ensure unique names (if we want them) 
 	 * 
 	 * @return
 	 */
 	public boolean mustHaveUniqueName() {		
 		return true;
 	}
 
 	public Set getWebLinks() {
 		Set rtn = new HashSet();
 		for (Iterator iter = getOccurences().iterator(); iter.hasNext();) {
 			Occurrence occ = (Occurrence) iter.next();			
 			if(occ instanceof WebLink){
 				rtn.add(occ);
 			}
 		}
 		return rtn;
 	}
 	public Set getFiles(){
 		Set rtn = new HashSet();
 		for (Iterator iter = getOccurences().iterator(); iter.hasNext();) {
 			Occurrence occ = (Occurrence) iter.next();
 			if(occ instanceof S3File){
 				rtn.add(occ);				
 			}
 		}		
 		return rtn;
 	}
 
 	/**
 	 * Return the set of all Meta Dates that this topic has. 
 	 * ie Movie will return 'Date Seen' & 'DateDue'
 	 * @return
 	 */
 	public Set getTagPropertyDates() {
 		Set rtn = new HashSet();
 		for (Iterator iter = getTagProperties().iterator(); iter.hasNext();) {
 			Meta meta = (Meta) iter.next();
 			if(meta instanceof MetaDate){
 				rtn.add(meta);				
 			}
 		}		
 		return rtn;
 	}
 
 	
 
 	/**
 	 * Return all actual meta dates that the topic has. 
 	 * ie 
 	 * Movie will return nothing, 
 	 * but Crimson Tide will return a Date Seen.
 	 * 
 	 * Important, Crimson Tide will rtn a Date Seen even if it doesn't have an 
 	 * entry for it, (if its movie tag has a Date Seen tag property)
 	 *  
 	 * @return
 	 */
 	public Set getAllMetas(Meta type) {		
 		Set s = getOnlyMetaOfType(getMetas(),type);
 		s.addAll(getOnlyMetaOfType(getPropertiesDueToTags(),type));
 		return s;
 	}
 	/**
 	 * Return all meta dates in that are due to tag properties.
 	 * 
 	 * 
 	 * @return
 	 */
 	public Set getTagPropertyBasedMetas(Meta type) {
 		return getOnlyMetaOfType(getPropertiesDueToTags(),type);
 	}
 	
 
 	/**
 	 * blech. can't use .getClass bc no GWT reflection. Ugly even with that.
 	 * 
 	 * now using the must-be-unique getType() and strcmp. still not super pretty
 	 * 
 	 * visitor pattern here?
 	 * 
 	 * NOTE: won't work for MetaTopics 
 	 *  
 	 * @param fromSet
 	 * @param date
 	 * @return
 	 */
 	public Set getOnlyMetaOfType(Set fromSet,Meta type) {
 		Set rtn = new HashSet();
 		for (Iterator iter = fromSet.iterator(); iter.hasNext();) {
 			Meta meta = (Meta) iter.next();
 
 			if(meta.getType().equals(type.getType())){
 				rtn.add(meta);
 			}			
 		}		
 		return rtn;
 	}
 	
 
 	
 	public boolean hasEntry(){
 		return !getEntries().isEmpty();
 	}
 	public boolean hasWebLinks(){
 		return !getWebLinks().isEmpty();
 	}
 	public boolean hasFiles(){
 		return !getFiles().isEmpty();
 	}
 	public boolean hasTagProperties(){
 		return (this instanceof Tag) && !getTagProperties().isEmpty();
 	}
 
 	public boolean hasMetas(Meta type) {		
 		return !getAllMetas(type).isEmpty();
 	}
 	
 
 	
 }
