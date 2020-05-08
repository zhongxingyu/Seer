 package org.muis.core;
 
 import java.util.*;
 
 import org.muis.core.mgr.AbstractElementList;
 import org.muis.core.mgr.ElementList;
 import org.muis.core.mgr.MuisMessageCenter;
 import org.muis.core.parser.MuisContent;
 import org.muis.core.parser.MuisParseException;
 import org.muis.core.parser.WidgetStructure;
 import org.muis.core.style.StyleAttribute;
 import org.muis.core.style.attach.StyleAttributeType;
 import org.muis.core.tags.Template;
 
 /**
  * Allows complex widgets to be created more easily by addressing a template MUIS file with widget definitions that are reproduced in each
  * instance of the widget. The template file also may contain attach point definitions, allowing the widget's content to be specified from
  * the XML invoking the widget.
  */
 public abstract class MuisTemplate extends MuisElement {
 	/** An attach point under a template widget */
 	public static class AttachPoint {
 		/** The template structure that this attach point belongs to */
 		public final TemplateStructure template;
 
 		/** The widget structure that defined this attach point */
 		public final WidgetStructure source;
 
 		/** The name of the attach point */
 		public final String name;
 
 		/** The type of element that may occupy the attach point */
 		public final Class<? extends MuisElement> type;
 
 		/**
 		 * Whether the attach point may be specified externally
 		 *
 		 * @see TemplateStructure#EXTERNAL
 		 */
 		public final boolean external;
 
 		/**
 		 * Whether the attach point is required to be specified externally
 		 *
 		 * @see TemplateStructure#REQUIRED
 		 */
 		public final boolean required;
 
 		/**
 		 * Whether the attach point may be specified more than once
 		 *
 		 * @see TemplateStructure#MULTIPLE
 		 */
 		public final boolean multiple;
 
 		/**
 		 * Whether the attach point is the default attach point for its template structure
 		 *
 		 * @see TemplateStructure#DEFAULT
 		 */
 		public final boolean isDefault;
 
 		/**
 		 * Whether the attach point specifies an implementation
 		 *
 		 * @see TemplateStructure#IMPLEMENTATION
 		 */
 		public final boolean implementation;
 
 		/**
 		 * Whether the element or set of elements at the attach point can be changed generically. This only affects the mutability of the
 		 * attach point as accessed from the list returned from {@link MuisTemplate#initChildren(MuisElement[])}.
 		 *
 		 * @see TemplateStructure#MUTABLE
 		 */
 		public final boolean mutable;
 
 		AttachPoint(TemplateStructure temp, WidgetStructure src, String aName, Class<? extends MuisElement> aType, boolean ext,
 			boolean req, boolean mult, boolean def, boolean impl, boolean isMutable) {
 			template = temp;
 			source = src;
 			name = aName;
 			type = aType;
 			external = ext;
 			required = req;
 			multiple = mult;
 			isDefault = def;
 			implementation = impl;
 			mutable = isMutable;
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 
 	/** Represents the structure of a templated widget */
 	public static class TemplateStructure implements Iterable<AttachPoint> {
 		/** The prefix for template-related attributes in the template file */
 		public static final String TEMPLATE_PREFIX = "template-";
 
 		/**
 		 * The attribute specifying that an element is an attach point definition. The value of the attribute is the name of the attach
 		 * point.
 		 */
 		public static final String ATTACH_POINT = TEMPLATE_PREFIX + "attach-point";
 
 		/** The name of an element that is an attach point if the type of the attach point is not specified */
 		public static final String GENERIC_ELEMENT = TEMPLATE_PREFIX + "element";
 
 		/**
 		 * The attribute specifying whether an attach point may be specified externally (from the XML invoking the templated widget) or not.
 		 * Internal-only attach points may simply be used as binding points, preventing a templated widget implementation from having to
 		 * search and find a widget. Default is true.
 		 */
 		public static final String EXTERNAL = TEMPLATE_PREFIX + "external";
 
 		/**
 		 * The attribute specifying whether an attach point MUST be specified externally (from the XML invoking the templated widget).
 		 * Default is false.
 		 */
 		public static final String REQUIRED = TEMPLATE_PREFIX + "required";
 
 		/** The attribute specifying whether multiple widgets may be specified for the attach point. Default is false. */
 		public static final String MULTIPLE = TEMPLATE_PREFIX + "multiple";
 
 		/**
 		 * The attribute specifying an attach point as the default attach point for the widget. Content in a templated widget's invocation
 		 * from XML that does not specify a {@link #role} will be added at the default attach point. At most one default attach point may be
 		 * specified for a widget.
 		 */
 		public static final String DEFAULT = TEMPLATE_PREFIX + "default";
 
 		/**
 		 * The attribute specifying that the element defining the attach point also defines a widget that will be placed at the attach point
 		 * unless overridden externally. Attach points that specify {@link #MULTIPLE} may not be implementations. Default is false except
 		 * for {@link #EXTERNAL internal-only} attach points, which <b>MUST</b> be implementations.
 		 */
 		public static final String IMPLEMENTATION = TEMPLATE_PREFIX + "implementation";
 
 		/** The attribute specifying that the element or elements occupying the attach point may be modified dynamically. Default is true. */
 		public static final String MUTABLE = TEMPLATE_PREFIX + "mutable";
 
 		/** The cache key to use to retrieve instances of {@link TemplateStructure} */
 		public static MuisCache.CacheItemType<Class<? extends MuisTemplate>, TemplateStructure, MuisException> TEMPLATE_STRUCTURE_CACHE_TYPE;
 
 		static {
 			TEMPLATE_STRUCTURE_CACHE_TYPE = new MuisCache.CacheItemType<Class<? extends MuisTemplate>, MuisTemplate.TemplateStructure, MuisException>() {
 				@Override
 				public TemplateStructure generate(MuisEnvironment env, Class<? extends MuisTemplate> key) throws MuisException {
 					return genTemplateStructure(env, key);
 				}
 
 				@Override
 				public int size(TemplateStructure value) {
 					return 0; // TODO
 				}
 			};
 		}
 
 		/** The attribute in a child of a template instance which marks the child as replacing an attach point from the definition */
 		public final MuisAttribute<AttachPoint> role = new MuisAttribute<>("role", new MuisProperty.PropertyType<AttachPoint>() {
 			@Override
 			public <V extends AttachPoint> Class<V> getType() {
 				return (Class<V>) AttachPoint.class;
 			}
 
 			@Override
 			public <V extends AttachPoint> V parse(MuisClassView classView, String value, MuisMessageCenter msg) throws MuisException {
 				AttachPoint ret = theAttachPoints.get(value);
 				if(ret == null)
 					throw new MuisException("No such attach point \"" + value + "\" in template " + theDefiner.getName());
 				return (V) ret;
 			}
 
 			@Override
 			public <V extends AttachPoint> V cast(Object value) {
 				if(value instanceof AttachPoint)
 					return (V) value;
 				return null;
 			}
 		});
 
 		private final Class<? extends MuisTemplate> theDefiner;
 
 		private final TemplateStructure theSuperStructure;
 
 		private final WidgetStructure theWidgetStructure;
 
 		private AttachPoint theDefaultAttachPoint;
 
 		private Map<String, AttachPoint> theAttachPoints;
 
 		private Map<AttachPoint, WidgetStructure> theAttachPointWidgets;
 
 		/**
 		 * @param definer The templated class that defines the template structure
 		 * @param superStructure The parent template structure
 		 * @param widgetStructure The widget structure specified in the template MUIS file
 		 */
 		public TemplateStructure(Class<? extends MuisTemplate> definer, TemplateStructure superStructure, WidgetStructure widgetStructure) {
 			theDefiner = definer;
 			theSuperStructure = superStructure;
 			theWidgetStructure = widgetStructure;
 		}
 
 		/** @param attaches The map of attach points to the widget structure where the attach points point to */
 		void addAttaches(Map<AttachPoint, WidgetStructure> attaches) {
 			Map<String, AttachPoint> attachPoints = new java.util.LinkedHashMap<>(attaches.size());
 			AttachPoint defAP = null;
 			for(AttachPoint ap : attaches.keySet()) {
 				attachPoints.put(ap.name, ap);
 				if(ap.isDefault)
 					defAP = ap;
 			}
 			theDefaultAttachPoint = defAP;
 			theAttachPoints = java.util.Collections.unmodifiableMap(attachPoints);
 			theAttachPointWidgets = java.util.Collections.unmodifiableMap(attaches);
 		}
 
 		/** @return The templated class that defines this template structure */
 		public Class<? extends MuisTemplate> getDefiner() {
 			return theDefiner;
 		}
 
 		/** @return The parent template structure that this structure builds on */
 		public TemplateStructure getSuperStructure() {
 			return theSuperStructure;
 		}
 
 		/** @return The widget structure specified in the template MUIS file for this template */
 		public WidgetStructure getWidgetStructure() {
 			return theWidgetStructure;
 		}
 
 		/**
 		 * @param name The name of the attach point to get, or null to get the default attach point
 		 * @return The attach point definition with the given name, or the default attach point if name==null, or null if no attach point
 		 *         with the given name exists or name==null and this template structure has no default attach point
 		 */
 		public AttachPoint getAttachPoint(String name) {
 			if(name == null)
 				return theDefaultAttachPoint;
 			return theAttachPoints.get(name);
 		}
 
 		/**
 		 * @param child The child to get the role for
 		 * @return The attach point whose role the child is in, or null if the child is not in a role in this template structure
 		 */
 		public AttachPoint getRole(MuisElement child) {
 			AttachPoint ret = child.atts().get(role);
 			if(ret == null)
 				ret = theDefaultAttachPoint;
 			return ret;
 		}
 
 		/**
 		 * @param attachPoint The attach point to get the widget structure of
 		 * @return The widget structure associated with the given attach point
 		 */
 		public WidgetStructure getWidgetStructure(AttachPoint attachPoint) {
 			return theAttachPointWidgets.get(attachPoint);
 		}
 
 		@Override
 		public java.util.Iterator<AttachPoint> iterator() {
 			return Collections.unmodifiableList(new ArrayList<>(theAttachPoints.values())).listIterator();
 		}
 
 		/**
 		 * Generates a template structure for a template type
 		 *
 		 * @param env The MUIS environment to generate the structure within
 		 * @param templateType The template type to generate the structure for
 		 * @return The template structure for the given templated type
 		 * @throws MuisException If an error occurs generating the structure
 		 */
 		public static TemplateStructure genTemplateStructure(MuisEnvironment env, Class<? extends MuisTemplate> templateType)
 			throws MuisException {
 			if(!MuisTemplate.class.isAssignableFrom(templateType))
 				throw new MuisException("Only extensions of " + MuisTemplate.class.getName() + " may have template structures: "
 					+ templateType.getName());
 			if(templateType == MuisTemplate.class)
 				return null;
 			Class<? extends MuisTemplate> superType = (Class<? extends MuisTemplate>) templateType.getSuperclass();
 			TemplateStructure superStructure = null;
 			while(superType != MuisTemplate.class) {
 				if(superType.getAnnotation(Template.class) != null) {
 					superStructure = env.getCache().getAndWait(env, TEMPLATE_STRUCTURE_CACHE_TYPE, superType);
 					break;
 				}
 				superType = (Class<? extends MuisTemplate>) superType.getSuperclass();
 			}
 			Template template = templateType.getAnnotation(Template.class);
 			if(template == null) {
 				if(superStructure != null)
 					return superStructure;
 				throw new MuisException("Concrete implementations of " + MuisTemplate.class.getName() + " like " + templateType.getName()
 					+ " must be tagged with @" + Template.class.getName() + " or extend a class that does");
 			}
 
 			java.net.URL location;
 			try {
 				location = MuisUtils.resolveURL(templateType.getResource(templateType.getSimpleName() + ".class"), template.location());
 			} catch(MuisException e) {
 				throw new MuisException("Could not resolve template path " + template.location() + " for templated widget "
 					+ templateType.getName(), e);
 			}
 			org.muis.core.parser.MuisDocumentStructure docStruct;
 			try (java.io.Reader templateReader = new java.io.BufferedReader(new java.io.InputStreamReader(location.openStream()))) {
 				MuisClassView classView = new MuisClassView(env, null, (MuisToolkit) templateType.getClassLoader());
 				classView.addNamespace("this", (MuisToolkit) templateType.getClassLoader());
 				org.muis.core.mgr.MutatingMessageCenter msg = new org.muis.core.mgr.MutatingMessageCenter(env.msg(), "Template "
 					+ templateType.getName() + ": ", "template", templateType);
 				docStruct = env.getParser().parseDocument(location, templateReader, classView, msg);
 			} catch(java.io.IOException e) {
 				throw new MuisException("Could not read template resource " + template.location() + " for templated widget "
 					+ templateType.getName(), e);
 			} catch(MuisParseException e) {
 				throw new MuisException("Could not parse template resource " + template.location() + " for templated widget "
 					+ templateType.getName(), e);
 			}
 			if(docStruct.getHead().getTitle() != null)
 				env.msg().warn(
 					"title specified but ignored in template xml \"" + location + "\" for template class " + templateType.getName());
 			if(!docStruct.getHead().getStyleSheets().isEmpty())
 				env.msg().warn(
 					docStruct.getHead().getStyleSheets().size() + " style sheet "
 						+ (docStruct.getHead().getStyleSheets().size() == 1 ? "" : "s") + " specified but ignored in template xml \""
 						+ location + "\" for template class " + templateType.getName());
 			if(docStruct.getContent().getChildren().isEmpty())
 				throw new MuisException("No contents specified in body section of template XML \"" + location + "\" for template class "
 					+ templateType.getName());
 			if(docStruct.getContent().getChildren().size() > 1)
 				throw new MuisException("More than one content element (" + docStruct.getContent().getChildren().size()
 					+ ") specified in body section of template XML \"" + location + "\" for template class " + templateType.getName());
 			if(!(docStruct.getContent().getChildren().get(0) instanceof WidgetStructure))
 				throw new MuisException("Non-widget contents specified in body section of template XML \"" + location
 					+ "\" for template class " + templateType.getName());
 
			WidgetStructure content = docStruct.getContent();
 			TemplateStructure templateStruct = new TemplateStructure(templateType, superStructure, content);
 			Map<AttachPoint, WidgetStructure> attaches = new HashMap<>();
 			try {
 				pullAttachPoints(templateStruct, content, attaches);
 			} catch(MuisException e) {
 				throw new MuisException("Error in template resource " + template.location() + " for templated widget "
 					+ templateType.getName() + ": " + e.getMessage(), e);
 			}
 			List<String> defaults = new ArrayList<>();
 			for(AttachPoint ap : attaches.keySet())
 				if(ap.isDefault)
 					defaults.add(ap.name);
 			if(defaults.size() > 1)
 				throw new MuisException("More than one default attach point " + defaults + " present in template resource "
 					+ template.location() + " for templated widget " + templateType.getName());
 			templateStruct.addAttaches(attaches);
 			return templateStruct;
 		}
 
 		private static void pullAttachPoints(TemplateStructure template, WidgetStructure structure,
 			Map<AttachPoint, WidgetStructure> attaches) throws MuisException {
 			for(MuisContent content : structure.getChildren()) {
 				if(!(content instanceof WidgetStructure))
 					continue;
 				WidgetStructure child = (WidgetStructure) content;
 				String name = child.getAttributes().get(ATTACH_POINT);
 				if(name == null) {
 					pullAttachPoints(template, child, attaches);
 					continue;
 				}
 				if(attaches.containsKey(name))
 					throw new MuisException("Duplicate attach points named \"" + name + "\"");
 				Class<? extends MuisElement> type;
 				if(child.getNamespace() == null && child.getTagName().equals(TemplateStructure.GENERIC_ELEMENT))
 					type = MuisElement.class;
 				else
 					try {
 						type = child.getClassView().loadMappedClass(child.getNamespace(), child.getTagName(), MuisElement.class);
 					} catch(MuisException e) {
 						throw new MuisException("Could not load element type \""
 							+ (child.getNamespace() == null ? "" : (child.getNamespace() + ":")) + child.getTagName()
 							+ "\" for attach point \"" + name + "\": " + e.getMessage(), e);
 					}
 				boolean external = getBoolean(child, EXTERNAL, true, name); // Externally-specifiable by default
 				boolean implementation = getBoolean(child, IMPLEMENTATION, !external, name);
 				boolean multiple = getBoolean(child, MULTIPLE, false, name);
 				boolean required = getBoolean(child, REQUIRED, !implementation && !multiple, name);
 				boolean def = getBoolean(child, DEFAULT, false, name);
 				boolean mutable = getBoolean(child, MUTABLE, true, name);
 				if(!external && (required || multiple || def || !implementation)) {
 					throw new MuisException("Non-externally-specifiable attach points (" + name
 						+ ") may not be required, default, or allow multiples");
 				}
 				if(!external && !implementation)
 					throw new MuisException("Non-externally-specifiable attach points (" + name + ") must be implementations");
 				if(!external && multiple)
 					throw new MuisException("Non-externally-specifiable attach points (" + name + ") may not allow multiples");
 				if(implementation && multiple)
 					throw new MuisException("Attach points (" + name + ") that allow multiples cannot be implementations");
 				for(String attName : child.getAttributes().keySet()) {
 					if(!attName.startsWith(TEMPLATE_PREFIX))
 						continue;
 					if(attName.equals(ATTACH_POINT) || attName.equals(EXTERNAL) || attName.equals(IMPLEMENTATION)
 						|| attName.equals(REQUIRED) || attName.equals(MULTIPLE) || attName.equals(DEFAULT))
 						continue;
 					throw new MuisException("Template attribute " + attName + " not recognized");
 				}
 
 				attaches.put(new AttachPoint(template, child, name, type, external, required, multiple, def, implementation, mutable),
 					child);
 				if(external) {
 					Map<AttachPoint, WidgetStructure> check = new HashMap<>();
 					pullAttachPoints(template, child, check);
 					if(!check.isEmpty())
 						throw new MuisException("Externally-specifiable attach points (" + name + ") may not contain attach points: "
 							+ check.keySet());
 				} else {
 					pullAttachPoints(template, child, attaches);
 				}
 			}
 		}
 
 		private static boolean getBoolean(WidgetStructure child, String attName, boolean def, String attachPoint) throws MuisException {
 			if(!child.getAttributes().containsKey(attName))
 				return def;
 			else if("true".equals(child.getAttributes().get(attName)))
 				return true;
 			else if("false".equals(child.getAttributes().get(attName)))
 				return false;
 			else
 				throw new MuisException("Attach point \"" + attachPoint + "\" specifies illegal " + attName + " value \""
 					+ child.getAttributes().get(attName) + "\"--may be true or false");
 		}
 	}
 
 	/** Represents an attach point within a particular widget instance */
 	protected class AttachPointInstance {
 		/** The attach point this instance is for */
 		public final AttachPoint attachPoint;
 
 		private final ElementList<?> theParentChildren;
 
 		private final MuisContainer<MuisElement> theContainer;
 
 		AttachPointInstance(AttachPoint ap, MuisTemplate template, ElementList<?> pc) {
 			attachPoint = ap;
 			theParentChildren = pc;
 			if(attachPoint.multiple)
 				theContainer = new AttachPointInstanceContainer(attachPoint, template, theParentChildren);
 			else
 				theContainer = null;
 		}
 
 		/** @return The element occupying the attach point */
 		public MuisElement getValue() {
 			if(attachPoint.multiple)
 				throw new IllegalStateException("The " + attachPoint.name + " attach point allows multiple elements");
 			for(MuisElement el : theParentChildren)
 				if(attachPoint.template.getRole(el) == attachPoint)
 					return el;
 			return null;
 		}
 
 		/**
 		 * @param el The element to set as the occupant of the attach point
 		 * @return The element that was occupying the attach point before this call
 		 * @throws IllegalArgumentException If the given widget cannot occupy the attach point
 		 */
 		public MuisElement setValue(MuisElement el) throws IllegalArgumentException {
 			if(attachPoint.multiple)
 				throw new IllegalStateException("The " + attachPoint.name + " attach point allows multiple elements");
 			assertFits(attachPoint, el);
 			if(el == null) {
 				Iterator<? extends MuisElement> iter = theParentChildren.iterator();
 				while(iter.hasNext()) {
 					MuisElement ret = iter.next();
 					if(attachPoint.template.getRole(ret) == attachPoint) {
 						iter.remove();
 						return ret;
 					}
 				}
 				return null;
 			}
 
 			// Scan the parent children for either the element occupying the attach point (and replace it) or
 			// for the first element whose widget template occurs after the attach point declaration (and insert the element before it).
 			// If neither occurs, just add the element at the end
 			HashSet<MuisElement> postAttachEls = new HashSet<>();
 			HashSet<AttachPoint> postAttachAPs = new HashSet<>();
 			{
 				boolean foundAttach = false;
 				for(MuisContent sibling : attachPoint.source.getParent().getChildren()) {
 					if(foundAttach) {
 						if(sibling instanceof WidgetStructure
 							&& ((WidgetStructure) sibling).getAttributes().containsKey(TemplateStructure.ATTACH_POINT)) {
 							postAttachAPs.add(attachPoint.template.getAttachPoint(((WidgetStructure) sibling).getAttributes().get(
 								TemplateStructure.ATTACH_POINT)));
 						} else {
 							MuisElement staticEl = theStaticContent.get(sibling);
 							if(staticEl != null)
 								postAttachEls.add(staticEl);
 						}
 					} else if(sibling == attachPoint.source)
 						foundAttach = true;
 				}
 			}
 			ListIterator<? extends MuisElement> iter = theParentChildren.listIterator();
 			while(iter.hasNext()) {
 				MuisElement ret = iter.next();
 				if(attachPoint.template.getRole(ret) == attachPoint) {
 					((ListIterator<MuisElement>) iter).set(el);
 					return ret;
 				} else {
 					boolean postAttach = postAttachEls.contains(ret);
 					if(!postAttach && postAttachAPs.contains(ret.atts().get(attachPoint.template.role)))
 						postAttach = true;
 					if(postAttach) {
 						iter.hasPrevious();
 						iter.previous();
 						((ListIterator<MuisElement>) iter).add(el);
 						return null;
 					}
 				}
 			}
 			((ListIterator<MuisElement>) iter).add(el);
 			return null;
 		}
 
 		/** @return A container for the elements occupying the multiple-enabled attach point */
 		public MuisContainer<MuisElement> getContainer() {
 			if(!attachPoint.multiple)
 				throw new IllegalStateException("The " + attachPoint.name + " attach point allows multiple elements");
 			return theContainer;
 		}
 	}
 
 	private TemplateStructure theTemplateStructure;
 
 	/**
 	 * The {@link org.muis.core.mgr.AttributeManager#accept(Object, MuisAttribute) wanter} for the {@link TemplateStructure#role role}
 	 * attribute on this element's content
 	 */
 	private final Object theRoleWanter;
 
 	private final Map<AttachPoint, AttachPointInstance> theAttachPoints;
 
 	private final Map<MuisContent, MuisElement> theStaticContent;
 
 	// Valid during initialization only (prior to initChildren())--will be null after that
 
 	private Map<AttachPoint, List<MuisElement>> theAttachmentMappings;
 
 	private Set<MuisElement> theUninitialized;
 
 	/** Creates a templated widget */
 	public MuisTemplate() {
 		theRoleWanter = new Object();
 		theAttachPoints = new LinkedHashMap<>();
 		theStaticContent = new HashMap<>();
 		theAttachmentMappings = new HashMap<>();
 		theUninitialized = new HashSet<>();
 		life().runWhen(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					MuisEnvironment env = getDocument().getEnvironment();
 					theTemplateStructure = env.getCache().getAndWait(env, TemplateStructure.TEMPLATE_STRUCTURE_CACHE_TYPE,
 						MuisTemplate.this.getClass());
 				} catch(MuisException e) {
 					msg().fatal("Could not generate template structure", e);
 				}
 			}
 		}, MuisConstants.CoreStage.INIT_SELF.toString(), 1);
 	}
 
 	/** @return This template widget's template structure */
 	public TemplateStructure getTemplate() {
 		return theTemplateStructure;
 	}
 
 	private void initTemplate(TemplateStructure structure) throws MuisParseException {
 		if(structure.getSuperStructure() != null) {
 			initTemplate(structure.getSuperStructure());
 		}
 
 		for(Map.Entry<String, String> att : structure.getWidgetStructure().getAttributes().entrySet()) {
 			if(!atts().isSet(att.getKey())) {
 				try {
 					atts().set(att.getKey(), att.getValue());
 				} catch(MuisException e) {
 					msg().error(
 						"Templated root attribute " + att.getKey() + "=" + att.getValue() + " failed for templated widget "
 							+ theTemplateStructure.getDefiner().getName(), e);
 				}
 			} else if(att.getKey().equals("style")) {
 				org.muis.core.style.MuisStyle elStyle = atts().get(StyleAttributeType.STYLE_ATTRIBUTE);
 				org.muis.core.style.MuisStyle templateStyle;
 				org.muis.core.style.SealableStyle newStyle = new org.muis.core.style.SealableStyle();
 				boolean mod = false;
 				try {
 					templateStyle = StyleAttributeType.parseStyle(getClassView(), att.getValue(), getMessageCenter());
 					for(StyleAttribute<?> styleAtt : templateStyle) {
 						if(!elStyle.isSet(styleAtt)) {
 							mod = true;
 							newStyle.set((StyleAttribute<Object>) styleAtt, templateStyle.get(styleAtt));
 						}
 					}
 					if(mod) {
 						for(StyleAttribute<?> styleAtt : elStyle)
 							newStyle.set((StyleAttribute<Object>) styleAtt, elStyle.get(styleAtt));
 						atts().set(StyleAttributeType.STYLE_ATTRIBUTE, newStyle);
 					}
 				} catch(MuisException e) {
 					msg().error("Could not parse style attribute of template", e);
 				}
 			}
 		}
 
 		for(MuisContent content : structure.getWidgetStructure().getChildren()) {
 			MuisElement child = getChild(structure, this, content, getDocument().getEnvironment().getContentCreator());
 			if(child != null) {
 				if(structure.getSuperStructure() == null)
 					theStaticContent.put(content, child);
 				else
 					addContent(child, structure.getSuperStructure());
 			}
 		}
 	}
 
 	private MuisElement getChild(TemplateStructure template, MuisElement parent, MuisContent child,
 		org.muis.core.parser.MuisContentCreator creator) throws MuisParseException {
 		if(child instanceof WidgetStructure && ((WidgetStructure) child).getNamespace() == null
 			&& ((WidgetStructure) child).getTagName().equals(TemplateStructure.GENERIC_ELEMENT))
 			return null;
 		MuisElement ret;
 		if(child instanceof WidgetStructure && ((WidgetStructure) child).getAttributes().containsKey(TemplateStructure.ATTACH_POINT)) {
 			WidgetStructure apStruct = (WidgetStructure) child;
 			AttachPoint ap = template.getAttachPoint(apStruct.getAttributes().get(TemplateStructure.ATTACH_POINT));
 			List<MuisElement> attaches = new ArrayList<>();
 			theAttachmentMappings.put(ap, attaches);
 			if(ap.implementation) {
 				WidgetStructure implStruct = new WidgetStructure(apStruct.getParent(), getDocument().getEnvironment(),
 					apStruct.getClassView(), apStruct.getNamespace(), apStruct.getTagName());
 				for(Map.Entry<String, String> att : apStruct.getAttributes().entrySet()) {
 					if(!att.getKey().startsWith(TemplateStructure.TEMPLATE_PREFIX))
 						implStruct.addAttribute(att.getKey(), att.getValue());
 				}
 				for(MuisContent content : apStruct.getChildren())
 					implStruct.addChild(content);
 				implStruct.seal();
 				ret = creator.getChild(parent, implStruct, false);
 				ret.atts().accept(theRoleWanter, theTemplateStructure.role);
 				try {
 					ret.atts().set(theTemplateStructure.role, ap);
 					ret.atts().set(TemplateStructure.IMPLEMENTATION, "true");
 				} catch(MuisException e) {
 					throw new IllegalStateException("Should not have thrown exception here", e);
 				}
 
 				attaches.add(ret);
 			} else
 				return null;
 		} else {
 			ret = creator.getChild(parent, child, false);
 		}
 		theUninitialized.add(ret);
 		if(!(child instanceof WidgetStructure))
 			return ret;
 		for(MuisContent childStruct : ((WidgetStructure) child).getChildren()) {
 			String attach = ((WidgetStructure) childStruct).getAttributes().get(TemplateStructure.ATTACH_POINT);
 			MuisElement childEl = getChild(template, ret, childStruct, creator);
 			if(attach != null) {
 				List<MuisElement> childAttaches = new ArrayList<>();
 				if(childEl != null)
 					childAttaches.add(childEl);
 				theAttachmentMappings.put(theTemplateStructure.getAttachPoint(attach), childAttaches);
 			}
 		}
 
 		return ret;
 	}
 
 	private void addContent(final MuisElement child, final TemplateStructure template) {
 		child.atts().accept(theRoleWanter, template.role);
 		AttachPoint role = child.atts().get(template.role);
 		if(role == null) {
 			role = template.getAttachPoint(null);
 			try {
 				child.atts().set(template.role, role);
 			} catch(MuisException e) {
 				throw new IllegalArgumentException("Should not get error here", e);
 			}
 		}
 		/*child.addListener(MuisConstants.Events.ATTRIBUTE_CHANGED, new org.muis.core.event.AttributeChangedListener<AttachPoint>(
 			template.role) {
 			private boolean theCallbackLock;
 
 			@Override
 			public void attributeChanged(AttributeChangedEvent<AttachPoint> event) {
 				if(theCallbackLock)
 					return;
 				child.msg().error("The " + template.role.getName() + " attribute may not be changed");
 				theCallbackLock = true;
 				try {
 					try {
 						child.atts().set(template.role, role);
 					} catch(MuisException e) {
 						child.msg().error("Should not get an exception here", e);
 					}
 				} finally {
 					theCallbackLock = false;
 				}
 			}
 		});*/
 		if(role == null)
 			role = template.getAttachPoint(null);
 		if(role == null) {
 			msg().error("No role specified for child of templated widget " + template.getDefiner().getName(), "child", child);
 			return;
 		}
 		List<MuisElement> attaches = theAttachmentMappings.get(role);
 		if(!role.external) {
 			msg().error("Role \"" + role + "\" is not specifiable externally for templated widget " + template.getDefiner().getName(),
 				"child", child);
 			return;
 		}
 		if(!role.type.isInstance(child)) {
 			msg().error(
 				"Children fulfilling role \"" + role + "\" in templated widget " + template.getDefiner().getName() + " must be of type "
 					+ role.type.getName() + ", not " + child.getClass().getName(), "child", child);
 			return;
 		}
 		if(role.implementation && attaches.size() == 1 && attaches.get(0).atts().get(TemplateStructure.IMPLEMENTATION) != null)
 			attaches.clear(); // Override the provided implementation
 		if(!role.multiple && !attaches.isEmpty()) {
 			msg().error("Multiple children fulfilling role \"" + role + "\" in templated widget " + template.getDefiner().getName(),
 				"child", child);
 			return;
 		}
 		WidgetStructure widgetStruct = template.getWidgetStructure(role);
 		for(Map.Entry<String, String> attr : widgetStruct.getAttributes().entrySet()) {
 			if(attr.getKey().startsWith(TemplateStructure.TEMPLATE_PREFIX))
 				continue;
 			try {
 				child.atts().set(attr.getKey(), attr.getValue());
 			} catch(MuisException e) {
 				child.msg().error("Template-specified attribute " + attr.getKey() + "=" + attr.getValue() + " is not supported by content",
 					e);
 			}
 		}
 		attaches.add(child);
 	}
 
 	/**
 	 * @param attach The attach point to get the container for
 	 * @return The container of all elements occupying the attach point in this widget instance
 	 * @throws IllegalArgumentException If the attach point is not recognized in this templated widget or does not support multiple elements
 	 */
 	protected MuisContainer<MuisElement> getContainer(AttachPoint attach) throws IllegalArgumentException {
 		AttachPointInstance instance = theAttachPoints.get(attach);
 		if(instance == null)
 			throw new IllegalArgumentException("Unrecognized attach point: " + attach + " in " + getClass().getName());
 		return instance.getContainer();
 	}
 
 	/**
 	 * @param attach The attach point to get the element at
 	 * @return The element attached at the given attach point. May be null.
 	 */
 	protected MuisElement getElement(AttachPoint attach) {
 		AttachPointInstance instance = theAttachPoints.get(attach);
 		if(instance == null)
 			throw new IllegalArgumentException("Unrecognized attach point: " + attach + " in " + getClass().getName());
 		return instance.getValue();
 	}
 
 	/**
 	 * @param attach The attach point to set the element at
 	 * @param element The element to set for the given attach point. May be null if the attach point is not required.
 	 * @return The element that occupied the attach point before this call
 	 * @throws IllegalArgumentException If the given element may not be set as occupying the given attach point
 	 */
 	protected MuisElement setElement(AttachPoint attach, MuisElement element) throws IllegalArgumentException {
 		AttachPointInstance instance = theAttachPoints.get(attach);
 		if(instance == null)
 			throw new IllegalArgumentException("Unrecognized attach point: " + attach + " in " + getClass().getName());
 		return instance.setValue(element);
 	}
 
 	@Override
 	public ElementList<? extends MuisElement> initChildren(MuisElement [] children) {
 		if(theTemplateStructure == null)
 			return getChildManager(); // Failed to parse template structure
 		if(theAttachmentMappings == null)
 			throw new IllegalArgumentException("initChildren() may only be called once on an element");
 
 		/* Initialize this templated widget using theTemplateStructure from the top (direct extension of MuisTemplate2) down
 		 * (to this templated class) */
 		try {
 			initTemplate(theTemplateStructure);
 		} catch(MuisParseException e) {
 			msg().fatal("Failed to implement widget structure for templated type " + MuisTemplate.this.getClass().getName(), e);
 			return getChildManager();
 		}
 
 		for(MuisElement child : children)
 			addContent(child, theTemplateStructure);
 
 		// Verify we've got all required attach points satisfied, etc.
 		if(!verifyTemplateStructure(theTemplateStructure))
 			return super.ch();
 
 		initChildren(this, theTemplateStructure.getWidgetStructure());
 
 		// Don't need these anymore
 		theAttachmentMappings = null;
 		theUninitialized = null;
 
 		return new AttachPointSetChildList();
 	}
 
 	private boolean verifyTemplateStructure(TemplateStructure struct) {
 		boolean ret = true;
 		if(struct.getSuperStructure() != null)
 			ret &= verifyTemplateStructure(struct.getSuperStructure());
 		for(AttachPoint ap : struct)
 			ret &= verifyAttachPoint(struct, ap);
 		return ret;
 	}
 
 	private boolean verifyAttachPoint(TemplateStructure struct, AttachPoint ap) {
 		if(ap.required && theAttachmentMappings.get(struct.getWidgetStructure(ap)).isEmpty()) {
 			msg().error("No widget specified for role " + ap.name + " for template " + struct.getDefiner().getName());
 			return false;
 		}
 		return true;
 	}
 
 	private void initChildren(MuisElement parent, WidgetStructure structure) {
 		if(parent != this && !theUninitialized.contains(parent))
 			return;
 		List<MuisElement> ret = new ArrayList<>();
 		List<AttachPoint> attaches = new ArrayList<>();
 		for(MuisContent childStruct : structure.getChildren()) {
 			if(childStruct instanceof WidgetStructure
 				&& ((WidgetStructure) childStruct).getAttributes().get(TemplateStructure.ATTACH_POINT) != null) {
 				String attach = ((WidgetStructure) childStruct).getAttributes().get(TemplateStructure.ATTACH_POINT);
 				AttachPoint ap = theTemplateStructure.getAttachPoint(attach);
 				attaches.add(ap);
 				for(MuisElement child : theAttachmentMappings.get(ap)) {
 					ret.add(child);
 					initChildren(child, (WidgetStructure) childStruct);
 				}
 			} else {
 				MuisElement child = theStaticContent.get(childStruct);
 				ret.add(child);
 				initChildren(child, (WidgetStructure) childStruct);
 			}
 		}
 		MuisElement [] children = ret.toArray(new MuisElement[ret.size()]);
 
 		try {
 			parent.atts().set(TemplateStructure.IMPLEMENTATION, null);
 		} catch(MuisException e) {
 			throw new IllegalStateException("Should not get error here", e);
 		}
 
 		ElementList<?> childList;
 		if(parent == this)
 			childList = super.initChildren(children);
 		else
 			childList = parent.initChildren(children);
 
 		for(AttachPoint attach : attaches)
 			theAttachPoints.put(attach, new AttachPointInstance(attach, this, childList));
 	}
 
 	static void assertFits(AttachPoint attach, MuisElement e) {
 		if(e == null) {
 			if(attach.required)
 				throw new IllegalArgumentException("Attach point " + attach + " is required--may not be set to null");
 			return;
 		}
 		if(!attach.type.isInstance(e))
 			throw new IllegalArgumentException(e.getClass().getName() + " may not be assigned to attach point " + attach + " (type "
 				+ attach.type.getName() + ")");
 	}
 
 	private class AttachPointSetChildList extends AbstractElementList<MuisElement> {
 		AttachPointSetChildList() {
 			super(MuisTemplate.this);
 		}
 
 		@Override
 		public int size() {
 			int ret = 0;
 			for(@SuppressWarnings("unused")
 			MuisElement item : this)
 				ret++;
 			return ret;
 		}
 
 		@Override
 		public AttachPointSetIterator iterator() {
 			return listIterator();
 		}
 
 		@Override
 		public MuisElement [] toArray() {
 			ArrayList<MuisElement> ret = new ArrayList<>();
 			for(MuisElement item : this)
 				ret.add(item);
 			return ret.toArray(new MuisElement[ret.size()]);
 		}
 
 		@Override
 		public boolean add(MuisElement e) {
 			AttachPoint role = e.atts().get(theTemplateStructure.role);
 			if(role == null)
 				role = theTemplateStructure.getAttachPoint(null);
 			if(role == null) {
 				throw new UnsupportedOperationException("Templated widget " + MuisTemplate.class.getName()
 					+ " does not have a default attach point, and therefore does not support addition"
 					+ " of children without a role assignment");
 			}
 			if(!role.external)
 				throw new UnsupportedOperationException("The " + role.name + " attach point is not externally-exposed");
 			if(!role.mutable)
 				throw new UnsupportedOperationException("The " + role.name + " attach point is not mutable");
 			if(!role.type.isInstance(e))
 				throw new UnsupportedOperationException("The " + role.name + " attach point's elements must be of type "
 					+ role.type.getName() + ", not " + e.getClass().getName());
 			if(role.multiple)
 				return getContainer(role).getContent().add(e);
 			else {
 				if(getElement(role) != null)
 					throw new UnsupportedOperationException("The " + role.name
 						+ " attach point only supports a single element and is already occupied");
 				setElement(role, e);
 				return true;
 			}
 		}
 
 		@Override
 		public boolean remove(Object o) {
 			if(!(o instanceof MuisElement))
 				return false;
 			MuisElement e = (MuisElement) o;
 			AttachPoint role = e.atts().get(theTemplateStructure.role);
 			if(role == null)
 				role = theTemplateStructure.getAttachPoint(null);
 			if(role == null)
 				return false;
 			if(!role.type.isInstance(e))
 				return false;
 			if(!role.external)
 				throw new UnsupportedOperationException("The " + role.name + " attach point is not externally-exposed");
 			if(!role.mutable)
 				throw new UnsupportedOperationException("The " + role.name + " attach point is not mutable");
 			if(role.multiple) {
 				org.muis.core.mgr.ElementList<? extends MuisElement> content = getContainer(role).getContent();
 				if(role.required && content.size() == 1 && content.get(0).equals(e))
 					throw new UnsupportedOperationException("The " + role.name
 						+ " attach point is required and only has one element left in it");
 				return content.remove(e);
 			} else {
 				if(!e.equals(getElement(role)))
 					return false;
 				if(role.required)
 					throw new UnsupportedOperationException("The " + role.name + " attach point is required");
 				setElement(role, null);
 				return true;
 			}
 		}
 
 		@Override
 		public boolean addAll(Collection<? extends MuisElement> c) {
 			boolean ret = false;
 			for(MuisElement item : c)
 				ret |= add(item);
 			return ret;
 		}
 
 		@Override
 		public boolean addAll(int index, Collection<? extends MuisElement> c) {
 			for(MuisElement child : c) {
 				add(index, child);
 				index++;
 			}
 			return c.size() > 0;
 		}
 
 		@Override
 		public boolean removeAll(Collection<?> c) {
 			boolean ret = false;
 			for(Object item : c)
 				ret |= remove(item);
 			return ret;
 		}
 
 		@Override
 		public boolean retainAll(Collection<?> c) {
 			boolean ret = false;
 			Iterator<MuisElement> iter = iterator();
 			while(iter.hasNext()) {
 				if(!c.contains(iter.next())) {
 					iter.remove();
 					ret = true;
 				}
 			}
 			return ret;
 		}
 
 		@Override
 		public void clear() {
 			for(AttachPoint ap : theTemplateStructure) {
 				if(!ap.external)
 					continue;
 				if(ap.required)
 					throw new UnsupportedOperationException("Template has required attach points--can't be cleared");
 				if(!ap.mutable) {
 					if((ap.multiple && !getContainer(ap).getContent().isEmpty()) || (!ap.multiple && getElement(ap) != null))
 						throw new UnsupportedOperationException("Template has non-empty, immutable attach points--can't be cleared");
 				}
 			}
 			for(AttachPoint ap : theTemplateStructure) {
 				if(ap.multiple)
 					getContainer(ap).getContent().clear();
 				else
 					setElement(ap, null);
 			}
 		}
 
 		@Override
 		public MuisElement get(int index) {
 			if(index < 0)
 				throw new IndexOutOfBoundsException("" + index);
 			int origIndex = index;
 			int length = 0;
 			for(MuisElement item : this) {
 				length++;
 				if(index == 0)
 					return item;
 				index--;
 			}
 			throw new IndexOutOfBoundsException(origIndex + " out of " + length);
 		}
 
 		@Override
 		public MuisElement set(int index, MuisElement element) {
 			if(index < 0)
 				throw new IndexOutOfBoundsException("" + index);
 			ListIterator<MuisElement> iter = listIterator();
 			MuisElement ret = null;
 			int i = 0;
 			for(i = 0; i <= index && iter.hasNext(); i++)
 				ret = iter.next();
 			if(i == index + 1) {
 				iter.set(element);
 				return ret;
 			}
 			throw new IndexOutOfBoundsException(index + " out of " + (i - 1));
 		}
 
 		@Override
 		public void add(int index, MuisElement element) {
 			if(index < 0)
 				throw new IndexOutOfBoundsException("" + index);
 			ListIterator<MuisElement> iter = listIterator();
 			int i = 0;
 			for(i = 0; i < index && iter.hasNext(); i++)
 				iter.next();
 			if(i == index) {
 				iter.add(element);
 				return;
 			}
 			throw new IndexOutOfBoundsException(index + " out of " + i);
 		}
 
 		@Override
 		public MuisElement remove(int index) {
 			if(index < 0)
 				throw new IndexOutOfBoundsException("" + index);
 			ListIterator<MuisElement> iter = listIterator();
 			MuisElement ret = null;
 			int i = 0;
 			for(i = 0; i <= index && iter.hasNext(); i++)
 				ret = iter.next();
 			if(i == index + 1) {
 				iter.remove();
 				return ret;
 			}
 			throw new IndexOutOfBoundsException(index + " out of " + (i - 1));
 		}
 
 		@Override
 		public AttachPointSetIterator listIterator() {
 			return new AttachPointSetIterator();
 		}
 
 		@Override
 		public ListIterator<MuisElement> listIterator(int index) {
 			ListIterator<MuisElement> ret = listIterator();
 			int i;
 			for(i = 0; i < index && ret.hasNext(); i++)
 				ret.next();
 			if(i == index)
 				return ret;
 			throw new IndexOutOfBoundsException(index + " out of " + i);
 		}
 
 		@Override
 		public List<MuisElement> subList(int fromIndex, int toIndex) {
 			return new org.muis.core.mgr.SubList<>(this, fromIndex, toIndex);
 		}
 
 		@Override
 		public boolean addAll(MuisElement [] children) {
 			return addAll(Arrays.asList(children));
 		}
 
 		@Override
 		public boolean addAll(int index, MuisElement [] children) {
 			return addAll(index, Arrays.asList(children));
 		}
 
 		/** Iterates over the elements in the set this templated element's external attach points */
 		private class AttachPointSetIterator implements ListIterator<MuisElement> {
 			private ListIterator<AttachPoint> theAPIter = (ListIterator<AttachPoint>) theTemplateStructure.iterator();
 
 			private AttachPoint theLastAttachPoint;
 
 			private ListIterator<? extends MuisElement> theAPContainer;
 
 			private MuisElement theAPElement;
 
 			private boolean calledHasNext = true;
 
 			private boolean calledHasPrevious = true;
 
 			private int theDirection;
 
 			private int theIndex;
 
 			@Override
 			public boolean hasNext() {
 				if(calledHasNext)
 					return true;
 				calledHasNext = true;
 				calledHasPrevious = false;
 				while((theAPContainer == null || !theAPContainer.hasNext()) && theAPElement == null && theAPIter.hasNext()) {
 					theAPContainer = null;
 					theLastAttachPoint = theAPIter.next();
 					if(theLastAttachPoint.external) {
 						if(theLastAttachPoint.multiple)
 							theAPContainer = getContainer(theLastAttachPoint).getContent().listIterator();
 						else
 							theAPElement = getElement(theLastAttachPoint);
 					}
 				}
 				return (theAPContainer != null && theAPContainer.hasNext()) || theAPElement != null;
 			}
 
 			@Override
 			public MuisElement next() {
 				if(!calledHasNext && !hasNext())
 					throw new java.util.NoSuchElementException();
 				theIndex++;
 				theDirection = 1;
 				calledHasNext = false;
 				calledHasPrevious = false;
 				if(theAPContainer != null)
 					return theAPContainer.next();
 				else {
 					MuisElement ret = theAPElement;
 					theAPElement = null;
 					return ret;
 				}
 			}
 
 			/** @return The attach point that the last returned element belongs to */
 			@SuppressWarnings("unused")
 			AttachPoint getLastAttachPoint() {
 				return theLastAttachPoint;
 			}
 
 			@Override
 			public boolean hasPrevious() {
 				if(calledHasPrevious)
 					return true;
 				calledHasPrevious = true;
 				calledHasNext = false;
 				while((theAPContainer == null || !theAPContainer.hasPrevious()) && theAPElement == null && theAPIter.hasPrevious()) {
 					theAPContainer = null;
 					theLastAttachPoint = theAPIter.previous();
 					if(theLastAttachPoint.external) {
 						if(theLastAttachPoint.multiple)
 							theAPContainer = getContainer(theLastAttachPoint).getContent().listIterator();
 						else
 							theAPElement = getElement(theLastAttachPoint);
 					}
 				}
 				return (theAPContainer != null && theAPContainer.hasPrevious()) || theAPElement != null;
 			}
 
 			@Override
 			public MuisElement previous() {
 				if(!calledHasPrevious && !hasPrevious())
 					throw new java.util.NoSuchElementException();
 				theDirection = -1;
 				calledHasNext = false;
 				calledHasPrevious = false;
 				if(theAPContainer != null)
 					return theAPContainer.previous();
 				else {
 					MuisElement ret = theAPElement;
 					theAPElement = null;
 					return ret;
 				}
 			}
 
 			@Override
 			public int nextIndex() {
 				return theIndex;
 			}
 
 			@Override
 			public int previousIndex() {
 				return theIndex - 1;
 			}
 
 			@Override
 			public void set(MuisElement e) {
 				if(theDirection == 0)
 					throw new IllegalStateException("next() or previous() must be called prior to calling set");
 				if(!theLastAttachPoint.mutable)
 					throw new UnsupportedOperationException("The " + theLastAttachPoint.name + " attach point is not mutable");
 				if(!theLastAttachPoint.type.isInstance(e))
 					throw new UnsupportedOperationException("The " + theLastAttachPoint.name
 						+ " attach points only supports elements of type " + theLastAttachPoint.type.getName() + ", not "
 						+ e.getClass().getName());
 				if(theAPContainer != null)
 					((ListIterator<MuisElement>) theAPContainer).set(e);
 				else
 					setElement(theLastAttachPoint, e);
 			}
 
 			@Override
 			public void add(MuisElement e) {
 				if(theLastAttachPoint == null) {
 					AttachPointSetChildList.this.add(theIndex, e);
 					return;
 				}
 				if(!theLastAttachPoint.mutable)
 					throw new UnsupportedOperationException("The " + theLastAttachPoint.name + " attach point is not mutable");
 				if(!theLastAttachPoint.multiple)
 					throw new UnsupportedOperationException("The " + theLastAttachPoint.name
 						+ " attach point does not support multiple elements");
 				if(!theLastAttachPoint.type.isInstance(e))
 					throw new UnsupportedOperationException("The " + theLastAttachPoint.name
 						+ " attach points only supports elements of type " + theLastAttachPoint.type.getName() + ", not "
 						+ e.getClass().getName());
 				((ListIterator<MuisElement>) theAPContainer).add(e);
 			}
 
 			@Override
 			public void remove() {
 				if(theDirection == 0)
 					throw new IllegalStateException("next() or previous() must be called prior to calling remove");
 				if(!theLastAttachPoint.mutable)
 					throw new UnsupportedOperationException("The " + theLastAttachPoint.name + " attach point is not mutable");
 				if(theAPContainer != null)
 					theAPContainer.remove();
 				else
 					setElement(theLastAttachPoint, null);
 				if(theDirection > 0)
 					theIndex--;
 				else
 					theIndex++;
 			}
 		}
 	}
 
 	private static class AttachPointInstanceContainer implements MuisContainer<MuisElement> {
 		private AttachPointInstanceElementList theContent;
 
 		AttachPointInstanceContainer(AttachPoint ap, MuisTemplate template, ElementList<?> parentChildren) {
 			theContent = new AttachPointInstanceElementList(ap, template, parentChildren);
 		}
 
 		@Override
 		public ElementList<MuisElement> getContent() {
 			return theContent;
 		}
 	}
 
 	private static class AttachPointInstanceElementList extends AbstractElementList<MuisElement> {
 		private AttachPoint theAttach;
 
 		private ElementList<?> theParentChildren;
 
 		AttachPointInstanceElementList(AttachPoint ap, MuisTemplate template, ElementList<?> parentChildren) {
 			super(template);
 			theAttach = ap;
 			theParentChildren = parentChildren;
 		}
 
 		@Override
 		public int size() {
 			int ret = 0;
 			for(MuisElement child : theParentChildren) {
 				if(child.atts().get(theAttach.template.role) == theAttach)
 					ret++;
 			}
 			return ret;
 		}
 
 		@Override
 		public Iterator<MuisElement> iterator() {
 			return new Iterator<MuisElement>() {
 				private final Iterator<? extends MuisElement> theBacking = theParentChildren.iterator();
 
 				private MuisElement theNext;
 
 				private boolean isRemovable;
 
 				@Override
 				public boolean hasNext() {
 					isRemovable = false;
 					while(theNext == null && theBacking.hasNext()) {
 						MuisElement next = theBacking.next();
 						if(theAttach.template.getRole(next) == theAttach)
 							theNext = next;
 					}
 					return theNext != null;
 				}
 
 				@Override
 				public MuisElement next() {
 					if(theNext == null && !hasNext())
 						throw new java.util.NoSuchElementException();
 					MuisElement ret = theNext;
 					theNext = null;
 					isRemovable = true;
 					return ret;
 				}
 
 				@Override
 				public void remove() {
 					if(!isRemovable)
 						throw new IllegalStateException("next() must be called before remove()");
 					isRemovable = false;
 					assertMutable();
 					theBacking.remove();
 				}
 			};
 		}
 
 		void assertMutable() {
 			if(!theAttach.mutable)
 				throw new UnsupportedOperationException("Attach point " + theAttach + " of template "
 					+ theAttach.template.getDefiner().getName() + " is not mutable");
 		}
 
 		void assertFits(MuisElement e) {
 			if(e == null)
 				throw new IllegalArgumentException("Cannot add null elements to an element container");
 			MuisTemplate.assertFits(theAttach, e);
 			if(contains(e))
 				throw new IllegalArgumentException("Element is already in this container");
 		}
 
 		@Override
 		public boolean add(MuisElement e) {
 			assertMutable();
 			assertFits(e);
 			ListIterator<MuisElement> iter = listIterator();
 			while(iter.hasNext())
 				iter.next();
 			iter.add(e);
 			return true;
 		}
 
 		@Override
 		public boolean remove(Object o) {
 			assertMutable();
 			Iterator<MuisElement> iter = iterator();
 			boolean found = false;
 			while(iter.hasNext()) {
 				if(iter.next() == o) {
 					iter.remove();
 					found = true;
 					break;
 				}
 			}
 			return found;
 		}
 
 		@Override
 		public boolean containsAll(Collection<?> c) {
 			Collection<?> copy = new HashSet<>(c);
 			for(MuisElement el : this) {
 				copy.remove(el);
 				if(copy.isEmpty())
 					break;
 			}
 			return copy.isEmpty();
 		}
 
 		@Override
 		public boolean addAll(Collection<? extends MuisElement> c) {
 			assertMutable();
 			for(MuisElement e : c)
 				assertFits(e);
 			ListIterator<MuisElement> iter = listIterator();
 			while(iter.hasNext())
 				iter.next();
 			for(MuisElement e : c)
 				iter.add(e);
 			return true;
 		}
 
 		@Override
 		public boolean addAll(int index, Collection<? extends MuisElement> c) {
 			assertMutable();
 			for(MuisElement e : c)
 				assertFits(e);
 			ListIterator<MuisElement> iter = listIterator(index);
 			for(MuisElement e : c)
 				iter.add(e);
 			return true;
 		}
 
 		@Override
 		public boolean removeAll(Collection<?> c) {
 			assertMutable();
 			Iterator<MuisElement> iter = iterator();
 			boolean found = false;
 			while(iter.hasNext()) {
 				if(c.contains(iter.next())) {
 					iter.remove();
 					found = true;
 				}
 			}
 			return found;
 		}
 
 		@Override
 		public boolean retainAll(Collection<?> c) {
 			assertMutable();
 			Iterator<MuisElement> iter = iterator();
 			boolean found = false;
 			while(iter.hasNext()) {
 				if(!c.contains(iter.next())) {
 					iter.remove();
 					found = true;
 				}
 			}
 			return found;
 		}
 
 		@Override
 		public void clear() {
 			assertMutable();
 			Iterator<MuisElement> iter = iterator();
 			while(iter.hasNext()) {
 				iter.remove();
 			}
 		}
 
 		@Override
 		public MuisElement get(int index) {
 			for(MuisElement el : this) {
 				if(index == 0)
 					return el;
 				index--;
 			}
 			throw new IndexOutOfBoundsException();
 		}
 
 		@Override
 		public MuisElement set(int index, MuisElement element) {
 			assertMutable();
 			assertFits(element);
 			ListIterator<MuisElement> iter = listIterator(index);
 			if(!iter.hasNext())
 				throw new IndexOutOfBoundsException("" + index);
 			MuisElement ret = iter.next();
 			iter.set(element);
 			return ret;
 		}
 
 		@Override
 		public void add(int index, MuisElement element) {
 			assertMutable();
 			assertFits(element);
 			ListIterator<MuisElement> iter = listIterator(index);
 			iter.add(element);
 		}
 
 		@Override
 		public MuisElement remove(int index) {
 			assertMutable();
 			Iterator<MuisElement> iter = iterator();
 			int idx = index;
 			MuisElement ret = null;
 			while(idx >= 0 && iter.hasNext()) {
 				ret = iter.next();
 			}
 			if(idx >= 0)
 				throw new IndexOutOfBoundsException(index + " of " + (index - idx));
 			iter.remove();
 			return ret;
 		}
 
 		@Override
 		public int indexOf(Object o) {
 			int index = 0;
 			for(MuisElement el : this) {
 				if(el == o)
 					return index;
 				index++;
 			}
 			return -1;
 		}
 
 		@Override
 		public ListIterator<MuisElement> listIterator() {
 			return new ListIterator<MuisElement>() {
 				private final ListIterator<? extends MuisElement> theBacking = theParentChildren.listIterator();
 
 				private MuisElement theNext;
 
 				private MuisElement thePrev;
 
 				private int theIndex;
 
 				private boolean isRemovable;
 
 				@Override
 				public boolean hasNext() {
 					isRemovable = false;
 					while(theNext == null && theBacking.hasNext()) {
 						MuisElement next = theBacking.next();
 						if(theAttach.template.getRole(next) == theAttach)
 							theNext = next;
 					}
 					return theNext != null;
 				}
 
 				@Override
 				public MuisElement next() {
 					if(theNext == null && !hasNext())
 						throw new java.util.NoSuchElementException();
 					MuisElement ret = theNext;
 					theNext = null;
 					isRemovable = true;
 					theIndex++;
 					return ret;
 				}
 
 				@Override
 				public boolean hasPrevious() {
 					isRemovable = false;
 					while(thePrev == null && theBacking.hasPrevious()) {
 						MuisElement prev = theBacking.previous();
 						if(theAttach.template.getRole(prev) == theAttach)
 							thePrev = prev;
 					}
 					return thePrev != null;
 				}
 
 				@Override
 				public MuisElement previous() {
 					if(thePrev == null && !hasPrevious())
 						throw new java.util.NoSuchElementException();
 					MuisElement ret = thePrev;
 					thePrev = null;
 					isRemovable = true;
 					theIndex--;
 					return ret;
 				}
 
 				@Override
 				public int nextIndex() {
 					return theIndex;
 				}
 
 				@Override
 				public int previousIndex() {
 					return theIndex - 1;
 				}
 
 				@Override
 				public void remove() {
 					if(!isRemovable)
 						throw new IllegalStateException("next() or previous() must be called before remove()");
 					assertMutable();
 					isRemovable = false;
 					theIndex--;
 					theBacking.remove();
 				}
 
 				@Override
 				public void set(MuisElement e) {
 					if(!isRemovable)
 						throw new IllegalStateException("next() or previous() must be called before set()");
 					assertMutable();
 					assertFits(e);
 					((ListIterator<MuisElement>) theBacking).set(e);
 				}
 
 				@Override
 				public void add(MuisElement e) {
 					assertMutable();
 					assertFits(e);
 					isRemovable = false;
 					theIndex++;
 					((ListIterator<MuisElement>) theBacking).add(e);
 				}
 			};
 		}
 
 		@Override
 		public ListIterator<MuisElement> listIterator(int index) {
 			ListIterator<MuisElement> ret = listIterator();
 			int idx = index;
 			while(idx > 0 && ret.hasNext()) {
 				idx--;
 				ret.next();
 			}
 			if(idx > 0)
 				throw new IndexOutOfBoundsException(index + " of " + (index - idx));
 			return ret;
 		}
 
 		@Override
 		public List<MuisElement> subList(int fromIndex, int toIndex) {
 			return new org.muis.core.mgr.SubList<>(this, fromIndex, toIndex);
 		}
 
 		@Override
 		public MuisElement [] toArray() {
 			ArrayList<MuisElement> ret = new ArrayList<>();
 			for(MuisElement el : this)
 				ret.add(el);
 			return ret.toArray(new MuisElement[ret.size()]);
 		}
 
 		@Override
 		public boolean addAll(MuisElement [] children) {
 			assertMutable();
 			for(MuisElement child : children)
 				assertFits(child);
 			ListIterator<MuisElement> iter = listIterator();
 			while(iter.hasNext())
 				iter.next();
 			for(MuisElement child : children)
 				iter.add(child);
 			return children.length > 0;
 		}
 
 		@Override
 		public boolean addAll(int index, MuisElement [] children) {
 			assertMutable();
 			for(MuisElement child : children)
 				assertFits(child);
 			ListIterator<MuisElement> iter = listIterator(index);
 			for(MuisElement child : children)
 				iter.add(child);
 			return children.length > 0;
 		}
 	}
 }
