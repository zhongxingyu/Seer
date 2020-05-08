 /**
  * 
  */
 package ecologylab.bigsemantics.metametadata;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import ecologylab.bigsemantics.actions.SemanticAction;
 import ecologylab.bigsemantics.actions.SemanticActionTranslationScope;
 import ecologylab.bigsemantics.collecting.LinkedMetadataMonitor;
 import ecologylab.bigsemantics.metadata.Metadata;
 import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
 import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
 import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.XMLTools;
 import ecologylab.serialization.annotations.simpl_collection;
 import ecologylab.serialization.annotations.simpl_composite;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.annotations.simpl_map;
 import ecologylab.serialization.annotations.simpl_nowrap;
 import ecologylab.serialization.annotations.simpl_other_tags;
 import ecologylab.serialization.annotations.simpl_scalar;
 import ecologylab.serialization.annotations.simpl_scope;
 import ecologylab.serialization.annotations.simpl_tag;
 import ecologylab.serialization.types.element.IMappable;
 
 /**
  * @author damaraju
  * 
  */
 @SuppressWarnings({"rawtypes"})
 @simpl_inherit
 public class MetaMetadata extends MetaMetadataCompositeField
 implements IMappable<String>//, HasLocalTranslationScope
 {
 	
 	public enum Visibility
 	{
 		GLOBAL,
 		PACKAGE,
 	}
 
 	@simpl_scalar
 	protected String																ormInheritanceStrategy;
 
 	@simpl_collection("selector")
 	@simpl_nowrap
 	ArrayList<MetaMetadataSelector>									selectors;
 	
 	@simpl_collection("example_url")
 	@simpl_nowrap
 	ArrayList<ExampleUrl>														exampleUrls;
 	
 	public ArrayList<ExampleUrl> getExampleUrls()
 	{
 		return exampleUrls;
 	}
 
 	@simpl_composite
 	FilterLocation                                  filterLocation;
 	
 	public FilterLocation getFilterLocation()
 	{
 	  return filterLocation;
 	}
 
 	@simpl_scalar
 	private String																	parser												= null;
 
 	@simpl_collection
 	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
 	private ArrayList<SemanticAction>								beforeSemanticActions;
 
 	@simpl_collection
 	@simpl_tag("operations")
 	@simpl_other_tags({"semantic_actions"})
 	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
 	private ArrayList<SemanticAction>								semanticActions;
 
 	@simpl_collection
 	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
 	private ArrayList<SemanticAction>								afterSemanticActions;
 
 	@simpl_scalar
 	@mm_dont_inherit
 	private boolean																	builtIn;
 
 	@simpl_scalar
 	private RedirectHandling												redirectHandling;
 
 	/**
 	 * Mixins are needed so that we can have objects of multiple metadata classes in side a single
 	 * metadata class. It basically provide us to simulate the functionality of multiple inheritance
 	 * which is missing in java.
 	 */
 	@simpl_collection("mixins")
 	@simpl_nowrap
 	private ArrayList<String>												mixins;
 
 	@simpl_scalar
 	private String																	collectionOf;
 
 	@simpl_collection("url_generator")
 	@simpl_nowrap
 	private ArrayList<UrlGenerator>									urlGenerators;
 
 	private Map<String, MetaMetadataField>					naturalIds										= new HashMap<String, MetaMetadataField>();
 
 	@simpl_map("link_with")
 	@simpl_nowrap
 	private HashMap<String, LinkWith>								linkWiths;
 
 	@simpl_scalar
 	protected Visibility														visibility										= Visibility.GLOBAL;
 
 	@simpl_scalar
 	private boolean																	noCache;
 	
 	private Map<MetaMetadataSelector, MetaMetadata>	reselectMap;
 	
 	SimplTypesScope																localMetadataTranslationScope;
 
 	public MetaMetadata()
 	{
 		super();
 	}
 
 	protected MetaMetadata(MetaMetadataField copy, String name)
 	{
 		super(copy, name);
 	}
 
 	public String getParser()
 	{
 		return parser;
 	}
 
 	/**
 	 * @return the semanticActions
 	 */
 	public ArrayList<SemanticAction> getSemanticActions()
 	{
 		return semanticActions;
 	}
 	
 	public Metadata constructMetadata()
 	{
 		return constructMetadata(this.getRepository().metadataTranslationScope());
 	}
 
 	/**
 	 * Lookup the Metadata class that corresponds to the (tag) name of this, using the
 	 * DefaultMetadataTranslationSpace. Assuming that is found, use reflection to instantiate it.
 	 * 
 	 * @return An instance of the Metadata subclass that corresponds to this, or null, if there is
 	 *         none.
 	 */
 	public Metadata constructMetadata(SimplTypesScope ts)
 	{
 		Metadata result = null;
 //		Class<? extends Metadata> metadataClass = getMetadataClass(ts);
 		Class<? extends Metadata> metadataClass = (Class<? extends Metadata>) this.getMetadataClassDescriptor().getDescribedClass();
 
 		if (metadataClass != null)
 		{
 			Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
 			Object[] argObjects = new Object[] { this };
 
 			result = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
 			if (mixins != null && mixins.size() > 0)
 			{
 				for (String mixinName : mixins)
 				{
 					MetaMetadata mixinMM = getRepository().getMMByName(mixinName);
 					if (mixinMM != null)
 					{
 						Metadata mixinMetadata = mixinMM.constructMetadata(ts);
 						if (mixinMetadata != null)
 							result.addMixin(mixinMetadata);
 					}
 				}
 			}
 			result.setMetaMetadataName(getName());
 		}
 		return result;
 	}
 
 	@Override
 	protected String getMetaMetadataTagToInheritFrom()
 	{
 		return (extendsAttribute != null) ? extendsAttribute : super.getMetaMetadataTagToInheritFrom();
 	}
 
 	/**
 	 * @return the collectionOf
 	 */
 	public String getCollectionOf()
 	{
 		return collectionOf;
 	}
 
 	public String getUserAgentName()
 	{
 		return userAgentName;
 	}
 
 	/**
 	 * @return the mimeTypes
 	 */
 	public ArrayList<String> getMimeTypes()
 	{
 		ArrayList<String> result = null;
 
 		for (MetaMetadataSelector selector : getSelectors())
 		{
 			if (result == null)
 				result = new ArrayList<String>();
 
 			ArrayList<String> mimeTypes = selector.getMimeTypes();
 			if (mimeTypes != null)
 				result.addAll(mimeTypes);
 		}
 
 		return result;
 	}
 
 	public ArrayList<String> getSuffixes()
 	{
 		ArrayList<String> result = null;
 
 		for (MetaMetadataSelector selector : getSelectors())
 		{
 			if (result == null)
 				result = new ArrayList<String>();
 
 			ArrayList<String> suffixes = selector.getSuffixes();
 			if (suffixes != null)
 				result.addAll(suffixes);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String key()
 	{
 		return getName();
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isCompositeMmdWithTypeDecl()
 	{
 		return (this instanceof MetaMetadataCompositeField)
 				&& ((MetaMetadataCompositeField) this).type != null;
 	}
 
 	@Override
 	public boolean isBuiltIn()
 	{
 		return builtIn;
 	}
 
 	public ArrayList<MetaMetadataSelector> getSelectors()
 	{
 		if (selectors == null)
 			return MetaMetadataSelector.NULL_SELECTOR;
 		return selectors;
 	}
 
 	public void addSelector(MetaMetadataSelector s)
 	{
 		if (selectors == null)
 			selectors = new ArrayList<MetaMetadataSelector>();
 
 		selectors.add(s);
 	}
 
 	@Override
 	public String getTypeName()
 	{
 		if (getType() != null)
 			return getType();
 		return getName();
 	}
 
 	/**
 	 * @return for meta-metadata defining a new mmd type, return the super meta-metadata type name
 	 *         (extends= or "metadata"); for meta-metadata decorating an existent mmd type, return the
 	 *         decorated meta-metadata type name (type=).
 	 */
 	public String getSuperMmdTypeName()
 	{
 		// decorative
 		if (getType() != null)
 			return getType();
 
 		// definitive
 		if (getExtendsAttribute() != null)
 			return getExtendsAttribute();
 		else
 			return "metadata";
 	}
 
 	/**
 	 * this will always return null since meta-metadata doesn't inherit from any fields.
 	 */
 	@Override
 	public MetaMetadataField getInheritedField()
 	{
 		return null;
 	}
 
 	/**
 	 * @return the redirectHandling
 	 */
 	public RedirectHandling getRedirectHandling()
 	{
 		return redirectHandling;
 	}
 
 	public void addNaturalIdField(String naturalId, MetaMetadataField childField)
 	{
 		naturalIds.put(naturalId, childField);
 	}
 
 	public Map<String, MetaMetadataField> getNaturalIdFields()
 	{
 		return naturalIds;
 	}
 
 	public ParsedURL generateUrl(String naturalId, String value)
 	{
 		if (urlGenerators != null)
 		{
 			for (UrlGenerator ug : urlGenerators)
 			{
 				if (ug.canGenerate(naturalId))
 				{
 					return ug.generate(getRepository(), naturalId, value);
 				}
 			}
 		}
 		return null;
 	}
 
 	public Map<String, LinkWith> getLinkWiths()
 	{
 		return linkWiths;
 	}
 
 	public void addLinkWith(LinkWith lw)
 	{
 		if (linkWiths == null)
 		{
 			linkWiths = new HashMap<String, LinkWith>();
 		}
 		linkWiths.put(lw.key(), lw);
 	}
 	
 	void setUpLinkWith(MetaMetadataRepository repository)
 	{
 		LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
 		if (linkWiths != null)
 		{
 			for (String lwName : linkWiths.keySet())
 			{
 				LinkWith lw = linkWiths.get(lwName);
 				MetaMetadata targetMmd = this.getRepository().getMMByName(lw.getName());
 				if (targetMmd != null)
 				{
 					monitor.registerName(lw.getName());
 					monitor.registerName(name);
 	
 					if (targetMmd.getLinkWiths() != null && targetMmd.getLinkWiths().containsKey(name))
 					{
 						// if there is already a reverse link, just make sure the reverse link reference is set
 						LinkWith r = targetMmd.getLinkWiths().get(name);
 						if (!r.isReverse())
 						{
 							// warning("not encouraging explicitly defining reverse links!");
 							r.setReverse(true);
 							lw.setReverseLink(r);
 						}
 					}
 					else
 					{
 						// if there isn't, create a new one
 						LinkWith r = lw.createReverseLink(name);
 						targetMmd.addLinkWith(r);
 					}
 				}
 				else
 				{
 					error("link_with: meta-metadata not found: " + lw.getName());
 				}
 			}
 		}
 	}
 
 	public Map<MetaMetadataSelector, MetaMetadata> getReselectMap()
 	{
 		return reselectMap;
 	}
 
 	public void addReselectEntry(MetaMetadataSelector selector, MetaMetadata mmd)
 	{
 		if (reselectMap == null)
 		{
 			reselectMap = new HashMap<MetaMetadataSelector, MetaMetadata>();
 		}
 		reselectMap.put(selector, mmd);
 	}
 	
 	protected void inheritSemanticActions(MetaMetadata inheritedMmd)
 	{
 		if (semanticActions == null)
 		{
 			semanticActions = inheritedMmd.getSemanticActions();
 		}
 		if(afterSemanticActions == null)
 		{
 			afterSemanticActions = inheritedMmd.getAfterSemanticActions();
 			//if(afterSemanticActions != null)
 			//  debug("HEY, JUST GOT SOME AFTERSEMANTIC ACTIONs FROM MY PARENT!!!");
 			//else
 			//	debug("HEY, JUST GOT SOME REALLY REALLY EMPTY AFTERSEMANTIC ACTIONs FROM MY PARENT!!!");
 		}
 	}
 
 	public void inheritMetaMetadata()
 	{
 		this.inheritMetaMetadata(null);
 	}
 
 	@Override
 	protected void inheritMetaMetadataHelper(InheritanceHandler inheritanceHandler)
 	{
 //		debug("processing mmd: " + this);
 		inheritanceHandler = new InheritanceHandler(this);
 		
 		// init each field's declaringMmd to this (some of them may change during inheritance)
 		for (MetaMetadataField field : this.getChildMetaMetadata())
 			field.setDeclaringMmd(this);
 
 		super.inheritMetaMetadataHelper(inheritanceHandler);
 	}
 	
 	@Override
 	protected void inheritFromInheritedMmd(MetaMetadata inheritedMmd, InheritanceHandler inheritanceHandler)
 	{
 		super.inheritFromInheritedMmd(inheritedMmd, inheritanceHandler);
 		this.inheritAttributes(inheritedMmd);
 		if (this.genericTypeVars != null)
 			this.genericTypeVars.inheritFrom(inheritedMmd.genericTypeVars, inheritanceHandler);
 //		if (inheritedMmd.genericTypeVars != null)
 //		{
 //			if (this.genericTypeVars == null)
 //			{
 //				this.genericTypeVars = new MmdGenericTypeVarScope();
 //				inheritanceHandler.addGenericTypeVarScope(this.genericTypeVars);
 //			}
 //			this.genericTypeVars.inheritFrom(inheritedMmd.genericTypeVars, inheritanceHandler);
 //		}
 		inheritSemanticActions(inheritedMmd);
 	}
 	
 	@Override
 	protected MetaMetadata findOrGenerateInheritedMetaMetadata(MetaMetadataRepository repository, InheritanceHandler inheritanceHandler)
 	{
 		if (MetaMetadata.isRootMetaMetadata(this))
 		{
 			this.setNewMetadataClass(true);
 			return null;
 		}
 		
 		MetaMetadata inheritedMmd = this.getInheritedMmd();
 		if (inheritedMmd == null)
 		{
 			String inheritedMmdName = this.getType();
 			if (inheritedMmdName == null)
 			{
 				inheritedMmdName = this.getExtendsAttribute();
 				this.setNewMetadataClass(true);
 			}
 			if (inheritedMmdName == null)
 				throw new MetaMetadataException("no type/extends specified: " + this);
 			inheritedMmd = this.getMmdScope().get(inheritedMmdName);
 			if (inheritedMmd == null)
 				throw new MetaMetadataException("meta-metadata '" + inheritedMmdName + "' not found in " + this.getName() + ". If this meta-metadata is defined in another file, make sure it compiled correcty.");
 			this.setInheritedMmd(inheritedMmd);
 		}
 		return inheritedMmd;
 	}
 	
 	@Override
 	protected void inheritFrom(MetaMetadataRepository repository, MetaMetadataCompositeField inheritedStructure, InheritanceHandler inheritanceHandler)
 	{
 		super.inheritFrom(repository, inheritedStructure, inheritanceHandler);
 		
 		// for fields referring to this meta-metadata type
 		// need to do inheritMetaMetadata() again after copying fields from this.getInheritedMmd()
 //		for (MetaMetadataField f : this.getChildMetaMetadata())
 //		{
 //			if (f instanceof MetaMetadataNestedField)
 //			{
 //				MetaMetadataNestedField nested = (MetaMetadataNestedField) f;
 //				if (nested.getInheritedMmd() == this)
 //				{
 //					nested.clearInheritFinishedOrInProgressFlag();
 //					nested.inheritMetaMetadata();
 //				}
 //			}
 //		}
 	}
 	
 	@Override
 	void findOrGenerateMetadataClassDescriptor(SimplTypesScope tscope)
 	{
 		if (this.metadataClassDescriptor == null)
 		{
 //			this.inheritMetaMetadata(inheritanceHandler);
 			MetaMetadata inheritedMmd = this.getInheritedMmd();
 			if (inheritedMmd != null)
 				inheritedMmd.findOrGenerateMetadataClassDescriptor(tscope);
 			MetadataClassDescriptor superCd = inheritedMmd == null ? null : inheritedMmd.getMetadataClassDescriptor();
 			if (this.isNewMetadataClass())
 			{
 				String tagOrName = this.getTagOrName();
 				MetadataClassDescriptor cd = new MetadataClassDescriptor(
 						this,
 						tagOrName,
 						this.getComment(),
 						this.packageName(),
 						XMLTools.classNameFromElementName(this.getName()),
 						superCd,
 						null);
 				// setting this early allows referring to the same class in fields
 				this.metadataClassDescriptor = cd;
 				
 				for (MetaMetadataField f : this.getChildMetaMetadata())
 				{
 					if (f.getDeclaringMmd() == this && f.getInheritedField() == null)
 					{
 						MetadataFieldDescriptor fd = f.findOrGenerateMetadataFieldDescriptor(tscope, cd);
 						cd.addMetadataFieldDescriptor(fd);
 					}
 //					if (!f.isCloned() && f instanceof MetaMetadataNestedField)
 					if (f.parent() == this && f instanceof MetaMetadataNestedField)
 						((MetaMetadataNestedField) f).findOrGenerateMetadataClassDescriptor(tscope);
 				}
 				
 				MetadataClassDescriptor existingCdWithThisTag = (MetadataClassDescriptor) tscope.getClassDescriptorByTag(tagOrName);
 				if (existingCdWithThisTag != null)
 				{
 					warning("Class descriptor exists for tag [" + tagOrName + "]: " + existingCdWithThisTag);
 				}
 				tscope.addTranslation(cd);
 			}
 			else
 			{
 				this.metadataClassDescriptor = superCd;
 			}
 			
 			if (this.getMmdScope() != null)
 			{
 				for (MetaMetadata inlineMmd : this.getMmdScope().values())
 				{
 					inlineMmd.findOrGenerateMetadataClassDescriptor(tscope);
 				}
 			}
 		}
 	}
 	
 	public boolean isDerivedFrom(MetaMetadata base)
 	{
 		MetaMetadata mmd = this;
 		while (mmd != null)
 		{
 			if (mmd == base)
 				return true;
 			mmd = mmd.getInheritedMmd();
 		}
 		return false;
 	}
 	
 	public static boolean isRootMetaMetadata(MetaMetadata mmd)
 	{
 		return mmd.getName().equals(ROOT_MMD_NAME);
 	}
 
 	@Override
 	protected String getMetadataClassName()
 	{
 		return this.packageName() + "." + this.getMetadataClassSimpleName();
 	}
 	
 	/**
 	 * 
 	 * @return the corresponding Metadata class simple name.
 	 */
 	@Override
 	protected String getMetadataClassSimpleName()
 	{
 		if (this.isBuiltIn() || this.isNewMetadataClass())
 		{
 			// new definition
 			return XMLTools.classNameFromElementName(this.getName());
 		}
 		else
 		{
 			// re-using existing type
 			// do not use this.type directly because we don't know if that is a definition or just re-using exsiting type
 			MetaMetadata inheritedMmd = this.getInheritedMmd();
 			return inheritedMmd == null ? null : inheritedMmd.getMetadataClassSimpleName();
 		}
 	}
 	
 //	@Override
 //	public boolean isNewMetadataClass()
 //	{
 //		// for meta-metadata, except for looking at its contents, we should also look at its built_in
 //		// attribute to determine if it is a new type
 //		return super.isNewMetadataClass() && !this.isBuiltIn();
 //	}
 	
 	@Override
 	public MetadataClassDescriptor bindMetadataClassDescriptor(SimplTypesScope metadataTScope)
 	{
 		if (this.metadataClassDescriptor != null)
 			return this.metadataClassDescriptor;
 		
 		// create a temporary local metadata translation scope
 		SimplTypesScope localMetadataTScope = localMetadataTypesScope(metadataTScope);
 		
 		// record the initial number of classes in the local translation scope
 		int initialLocalTScopeSize = localMetadataTScope.entriesByClassName().size();
 		
 		// do actual stuff ...
 		super.bindMetadataClassDescriptor(localMetadataTScope);
 		
 		// if tag overlaps, or there are fields using classes not in metadataTScope, use localTScope
 		MetadataClassDescriptor thisCd = this.getMetadataClassDescriptor();
 		if (thisCd != null)
 		{
 			thisCd.setDefiningMmd(this);
 			
 			MetadataClassDescriptor thatCd = (MetadataClassDescriptor) metadataTScope.getClassDescriptorByTag(thisCd.getTagName());
 			if (thisCd != thatCd)
 			{
 				localMetadataTScope.addTranslation(thisCd);
 				this.localMetadataTranslationScope = localMetadataTScope;
 			}
 			else if (localMetadataTScope.entriesByClassName().size() > initialLocalTScopeSize)
 				this.localMetadataTranslationScope = localMetadataTScope;
 			else
 				this.localMetadataTranslationScope = metadataTScope;
 			
 			// we should have stuffs in the scope already
 			thisCd.resolvePolymorphicAnnotations();
 			thisCd.resolveUnresolvedClassesAnnotationFDs();
 			thisCd.resolveUnresolvedScopeAnnotationFDs();
 		}
 		
 		// return the bound metadata class descriptor
 		return thisCd;
 	}
 	
 	public SimplTypesScope getLocalMetadataTypesScope()
 	{
 		return this.localMetadataTranslationScope;
 	}
 	
 	SimplTypesScope localMetadataTypesScope(SimplTypesScope metadataTScope)
 	{
 		return localMetadataTranslationScope != null ? localMetadataTranslationScope : SimplTypesScope.get("mmd_local_tscope:" + this.getName(), new SimplTypesScope[] { metadataTScope });
 	}
 
 	public ArrayList<SemanticAction> getBeforeSemanticActions()
 	{
 		return beforeSemanticActions;
 	}
 
 	public ArrayList<SemanticAction> getAfterSemanticActions()
 	{
 		return afterSemanticActions;
 	}
 
 	public boolean isRootMetaMetadata()
 	{
 		return isRootMetaMetadata(this);
 	}
 
 	public boolean isNoCache()
 	{
 		return noCache;
 	}
 
 }
