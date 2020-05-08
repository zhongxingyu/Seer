 package org.cotrix.domain.attributes;
 
 import static org.cotrix.domain.attributes.Facet.*;
 import static org.cotrix.domain.common.Ranges.*;
 import static org.cotrix.domain.dsl.Data.*;
 import static org.cotrix.domain.utils.Constants.*;
 import static org.cotrix.domain.utils.DomainUtils.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.common.Containers.Attributes;
 import org.cotrix.domain.common.Range;
 import org.cotrix.domain.trait.Described;
 import org.cotrix.domain.trait.Named;
 
 // wrappers over recurring, cross-codelist attribute definitions.
 public enum CommonDefinition implements Named {
 	
 	//overload make() further if we need different forms of definitions.
 	
 	CREATED 			(make("created",once),VISIBLE,PUSBLISHEABLE),
 	LAST_UPDATED 		(make("updated"),VISIBLE,PUSBLISHEABLE),
 	UPDATED_BY 			(make("updatedBy"),VISIBLE),
 	
 	DELETED				(make("deleted"),true),
 	NEW					(make("new"),true),
 	MODIFIED			(make("modified"),true),
 	INVALID				(make("invalid"),true),
 	
 	
 	PREVIOUS_VERSION 		(make("previous_version"),VISIBLE),
 	PREVIOUS_VERSION_ID 	(make("previous_version_id")),
 	PREVIOUS_VERSION_NAME 	(make("previous_version_name"),VISIBLE),
 	
 	SUPERSIDES         		(make("supersides"),VISIBLE);
 
 	
 	//---------------------------------- statics
 	
 	
 	//lookup table to re-constitute enums from persisted local names.
 	private static Map<String,CommonDefinition> defs = new HashMap<>();
 
 	private static List<CommonDefinition> markers;
 	
 	static {  
 
 		//cannot build this in constructor., but this does not need maintenance either.
 
 		List<CommonDefinition> mks = new ArrayList<>();
 		
 		for (CommonDefinition def : values()) {
 			defs.put(def.qname().getLocalPart(), def);
 			
 			if (def.isMarker())
 				mks.add(def);
 		}
 		
 		markers = Collections.unmodifiableList(mks);
 		
 	}
 	
 	//enables behavioural forks (e.g. store full def or just name...) 
 	public static boolean isCommon(String name) {
 		return commonDefinitionFor(name)!=null;
 	}
 	
 	public static boolean isCommon(String name, Facet facet) {
 		return commonDefinitionFor(name)!=null && commonDefinitionFor(name).is(facet);
 	}
 	
 	public static boolean isCommon(QName name) {
 		return isCommon(name.getLocalPart());
 	}
 	
 	public static boolean isCommon(QName name,Facet facet) {
 		return isCommon(name.getLocalPart(),facet);
 	}
 	
 	public static CommonDefinition commonDefinitionFor(String name) { 
 		return defs.get(name);
 	}
 	
 	public static List<CommonDefinition> markers() {
 		return markers;  ///it's immutable
 	}
 	
 	
 	//-----------------------------------------
 	
 	private final AttributeDefinition def;
 	private final boolean marker;
 	private final List<Facet> facets = new ArrayList<Facet>();
 	
 	
 	private CommonDefinition(AttributeDefinition def,Facet ... facets) {
 		this(def,false,facets);
 	}
 	
 	private CommonDefinition(AttributeDefinition def, boolean marker, Facet ... facets) {
 		
 		this.def= def;
 		this.marker=marker;
 		
 		for (Facet f : facets)
 			this.facets.add(f);
 	}
 
 	public QName qname() {
 		return def.qname(); //can use this wrapper in container' API.
 	}
 	
 	public boolean is(Facet facet) {
 		return facets.contains(facet);
 	}
 	
 	//trust this is not going to be changed..if paranoia ensues, copy-construct..
 	public AttributeDefinition get() {
 		return def;
 	}
 	
 	public boolean isMarker() {
 		return marker;
 	}
 	
 	//just handy, cleans client code
 	public AttributeDefinition.Bean bean() {
 		return reveal(def).bean();
 	}
 	
 	// conveniences
 	
 	public Attribute instance() {
 		return attribute().instanceOf(this).build();
 	}
 	
 	public SetTargetClause set() {
 		
 		return new SetTargetClause(defaultValue);
 	}
 
 	public SetTargetClause set(String value) {
 		
 		return new SetTargetClause(value);
 	}
 	
 	public CommonDefinition removeFrom(Described entity) {
 		
 		return removeFrom(entity.attributes());
 	}
 
 	public CommonDefinition removeFrom(Attributes as) {
 		
 		Attribute a = as.getFirst(this);
 		
 		if (a!=null)
 			as.beans().remove(a.id());
 		
 		return this;
 	}
 
 	public boolean isIn(Described entity) {
 		return isIn(entity.attributes());
 	}
 	
 	public boolean isIn(Attributes as) {
 		return as.contains(this);
 	}
 	
 	public String of(Described entity) {
 		return in (entity.attributes());
 	}
 	
 	public String in(Attributes as) {
 		return as.valueOf(this);
 	}
 	
 	public Date dateOf(Described entity) {
 		return dateIn(entity.attributes());
 	}
 	
 	public Date dateIn(Attributes as) {
 		return as.dateOf(this);
 	}
 	
 	public QName nameOf(Described entity) {
 		return nameIn(entity.attributes());
 	}
 	
 	public QName nameIn(Attributes as) {
 		return as.nameOf(this);
 	}
 	
 	
 	
 	//helpers
 	
 	private static AttributeDefinition make(String name) {
 		return make(name,atmostonce);
 	}
 	
 	private static AttributeDefinition make(String name,Range range) {
 		return attrdef().name(q(NS,name)).is(SYSTEM_TYPE).occurs(range).build();
 	}
 	
 	
 	public class SetTargetClause {
 		
 		private String value;
 		
 		SetTargetClause(String value) {
 			this.value=value; 
 		}
 		
 		public CommonDefinition on(Described entity) {
 			return in(entity.attributes());
 		}
 		
 		public CommonDefinition in(Attributes as) {
 			
			Attribute a = as.getFirst(CommonDefinition.this);
			
			if (a==null) {
				
				a = instance();
				
				as.beans().add(beanOf(a));
 
			}
 			
 			beanOf(a).value(value);
 			
 			return CommonDefinition.this;
 		}
 	}
 	
 	
 }
