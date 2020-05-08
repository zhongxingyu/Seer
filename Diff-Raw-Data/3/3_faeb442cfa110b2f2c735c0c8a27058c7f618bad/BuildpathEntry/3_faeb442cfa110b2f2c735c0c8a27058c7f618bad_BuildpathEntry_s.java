 package org.eclipse.dltk.internal.core;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.AssertionFailedException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.compiler.env.AccessRule;
 import org.eclipse.dltk.internal.compiler.env.AccessRuleSet;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 
 public class BuildpathEntry implements IBuildpathEntry {
 	public static final String TAG_BUILDPATH = "buildpath"; //$NON-NLS-1$
 	public static final String TAG_BUILDPATHENTRY = "buildpathentry"; //$NON-NLS-1$	
 	public static final String TAG_KIND = "kind"; //$NON-NLS-1$
 	public static final String TAG_PATH = "path"; //$NON-NLS-1$
 	public static final String TAG_EXPORTED = "exported"; //$NON-NLS-1$
 	public static final String TAG_EXTERNAL = "external"; //$NON-NLS-1$
 	public static final String TAG_INCLUDING = "including"; //$NON-NLS-1$
 	public static final String TAG_EXCLUDING = "excluding"; //$NON-NLS-1$
 	public static final String TAG_ACCESS_RULES = "accessrules"; //$NON-NLS-1$
 	public static final String TAG_ACCESS_RULE = "accessrule"; //$NON-NLS-1$
 	public static final String TAG_PATTERN = "pattern"; //$NON-NLS-1$	
 	public static final String TAG_ATTRIBUTES = "attributes"; //$NON-NLS-1$
 	public static final String TAG_ATTRIBUTE = "attribute"; //$NON-NLS-1$
 	public static final String TAG_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
 	public static final String TAG_ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$	
 	public static final String TAG_COMBINE_ACCESS_RULES = "combineaccessrules"; //$NON-NLS-1$	
 	public static final String TAG_ACCESSIBLE = "accessible"; //$NON-NLS-1$
 	public static final String TAG_NON_ACCESSIBLE = "nonaccessible"; //$NON-NLS-1$
 	public static final String TAG_DISCOURAGED = "discouraged"; //$NON-NLS-1$
 	public static final String TAG_IGNORE_IF_BETTER = "ignoreifbetter"; //$NON-NLS-1$	
 	/**
 	 * Describes the kind of project fragment found on this buildpath entry -
 	 * either K_BINARY or K_SOURCE
 	 */
 	private int contentKind;
 	/**
 	 * Describes the kind of buildpath entry - one of BPE_PROJECT, BPE_SOURCE or
 	 * BPE_CONTAINER
 	 */
 	private int entryKind;
 	/**
 	 * The meaning of the path of a buildpath entry depends on its entry kind:
 	 * <ul>
 	 * <li>Source code in the current project (<code>BPE_SOURCE</code>) -
 	 * The path associated with this entry is the absolute path to the root
 	 * folder. </li>
 	 * <li>A required project (<code>BPE_PROJECT</code>) - the path of the
 	 * entry denotes the path to the corresponding project resource.</li>
 	 * <li> A container entry (<code>BPE_CONTAINER</code>) - the first
 	 * segment of the path is denoting the unique container identifier (for
 	 * which a <code>BuildpathContainerInitializer</code> could be
 	 * registered), and the remaining segments are used as additional hints for
 	 * resolving the container entry to an actual
 	 * <code>IBuildpathContainer</code>.</li>
 	 */
 	private IPath path;
 	/**
 	 * Patterns allowing to include/exclude portions of the resource tree
 	 * denoted by this entry path.
 	 */
 	private IPath[] inclusionPatterns;
 	private char[][] fullInclusionPatternChars;
 	private IPath[] exclusionPatterns;
 	private char[][] fullExclusionPatternChars;
 	private final static char[][] UNINIT_PATTERNS = new char[][] {
 		"Non-initialized yet".toCharArray()}; //$NON-NLS-1$
 	private boolean combineAccessRules;
 	private String rootID;
 	private AccessRuleSet accessRuleSet;
 	
 	boolean fIsContainerEntry = false;
 	static class UnknownXmlElements {
 		String[] attributes;
 		ArrayList children;
 	}
 	/*
 	 * Default inclusion pattern set
 	 */
 	public final static IPath[] INCLUDE_ALL = {};
 	/*
 	 * Default exclusion pattern set
 	 */
 	public final static IPath[] EXCLUDE_NONE = {};
 	/*
 	 * Default extra attributes
 	 */
 	public final static IBuildpathAttribute[] NO_EXTRA_ATTRIBUTES = {};
 	/*
 	 * Default access rules
 	 */
 	public final static IAccessRule[] NO_ACCESS_RULES = {};
 	/**
 	 * The export flag
 	 */
 	boolean isExported;
 	/*
 	 * The extra attributes
 	 */
 	IBuildpathAttribute[] extraAttributes;
 	
 	/**
 	 * External library extry.
 	 */
 	private boolean isExternal;
 
 	/**
 	 * Creates a build path entry of the specified kind with the given path.
 	 * @param externalLib TODO
 	 */
 	public BuildpathEntry(int contentKind, int entryKind, IPath path, boolean isExported, IPath[] inclusionPatterns,
 			IPath[] exclusionPatterns, org.eclipse.dltk.core.IAccessRule[] accessRules, boolean combineAccessRules,
 			IBuildpathAttribute[] extraAttributes, boolean externalLib) {
 		this.contentKind = contentKind;
 		this.entryKind = entryKind;
 		this.path = path;
 		this.isExported = isExported;
 		this.inclusionPatterns = inclusionPatterns;
 		this.exclusionPatterns = exclusionPatterns;
 		this.isExternal = externalLib;
 		int length;
 		if (accessRules != null && (length = accessRules.length) > 0) {
 			AccessRule[] rules = new AccessRule[length];
 			System.arraycopy(accessRules, 0, rules, 0, length);
 			this.accessRuleSet = new AccessRuleSet(rules, getMessageTemplates());
 		}
 		this.combineAccessRules = combineAccessRules;
 		this.extraAttributes = extraAttributes;
 		if (inclusionPatterns != INCLUDE_ALL && inclusionPatterns.length > 0) {
 			this.fullInclusionPatternChars = UNINIT_PATTERNS;
 		}
 		if (exclusionPatterns.length > 0) {
 			this.fullExclusionPatternChars = UNINIT_PATTERNS;
 		}
 	}
 
 	public int getEntryKind() {
 		return entryKind;
 	}
 	
 	public IPath[] getExclusionPatterns() {
 		return this.exclusionPatterns;
 	}
 
 	public IBuildpathAttribute[] getExtraAttributes() {
 		return this.extraAttributes;
 	}
 
 	private String[] getMessageTemplates() {
 		ModelManager manager = ModelManager.getModelManager();
 		String[] result = new String[AccessRuleSet.MESSAGE_TEMPLATES_LENGTH];
 		if (this.entryKind == BPE_PROJECT || this.entryKind == BPE_SOURCE) {
 			/*
 			 * can be remote source entry when reconciling
 			 */
 			result[0] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_project, new String[] {
 					"{0}", getPath().segment(0)})); //$NON-NLS-1$
 			result[1] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_constructor_project,
 					new String[] {
 							"{0}", getPath().segment(0)})); //$NON-NLS-1$
 			result[2] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_method_project,
 					new String[] {
 							"{0}", "{1}", getPath().segment(0)})); //$NON-NLS-1$ //$NON-NLS-2$
 			result[3] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_field_project,
 					new String[] {
 							"{0}", "{1}", getPath().segment(0)})); //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			IPath libPath = getPath();
 			Object target = Model.getTarget(ResourcesPlugin.getWorkspace().getRoot(), libPath, false);
 			String pathString;
 			if (target instanceof java.io.File)
 				pathString = libPath.toOSString();
 			else
 				pathString = libPath.makeRelative().toString();
 			result[0] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_library, new String[] {
 					"{0}", pathString})); //$NON-NLS-1$ 
 			result[1] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_constructor_library,
 					new String[] {
 							"{0}", pathString})); //$NON-NLS-1$ 
 			result[2] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_method_library,
 					new String[] {
 							"{0}", "{1}", pathString})); //$NON-NLS-1$ //$NON-NLS-2$ 
 			result[3] = manager.intern(Messages.bind(org.eclipse.dltk.internal.core.util.Messages.restrictedAccess_field_library,
 					new String[] {
 							"{0}", "{1}", pathString})); //$NON-NLS-1$ //$NON-NLS-2$ 
 		}
 		return result;
 	}
 
 	/**
 	 * @see IBuildpathEntry#getExclusionPatterns()
 	 */
 	public IPath[] getInclusionPatterns() {
 		return this.inclusionPatterns;
 	}
 
 	public IPath getPath() {
 		return path;
 	}
 
 	/**
 	 * @see IBuildpathEntry#isExported()
 	 */
 	public boolean isExported() {
 		return this.isExported;
 	}
 
 	public boolean isOptional() {
 		for (int i = 0, length = this.extraAttributes.length; i < length; i++) {
 			IBuildpathAttribute attribute = this.extraAttributes[i];
 			if (IBuildpathAttribute.OPTIONAL.equals(attribute.getName()) && "true".equals(attribute.getValue())) //$NON-NLS-1$
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Answers an ID which is used to distinguish entries during package
 	 * fragment root computations
 	 */
 	public String rootID() {
 		if (this.rootID == null) {
 			switch (this.entryKind) {
 			case IBuildpathEntry.BPE_PROJECT:
 				this.rootID = "[PRJ]" + this.path; //$NON-NLS-1$
 				break;
 			case IBuildpathEntry.BPE_LIBRARY :
 				this.rootID = "[LIB]"+this.path;  //$NON-NLS-1$
 				break;
 			case IBuildpathEntry.BPE_SOURCE:
 				this.rootID = "[SRC]" + this.path; //$NON-NLS-1$
 				break;
 			case IBuildpathEntry.BPE_CONTAINER:
 				this.rootID = "[CON]" + this.path; //$NON-NLS-1$
 				break;
 			default:
 				this.rootID = ""; //$NON-NLS-1$
 				break;
 			}
 		}
 		return this.rootID;
 	}
 
 	public boolean combineAccessRules() {
 		return this.combineAccessRules;
 	}
 
 	/**
 	 * Used to perform export/restriction propagation across referring
 	 * projects/containers
 	 */
 	public BuildpathEntry combineWith(BuildpathEntry referringEntry) {
 		if (referringEntry == null)
 			return this;
 		if (referringEntry.isExported() || referringEntry.getAccessRuleSet() != null) {
 			boolean combine = this.entryKind == BPE_SOURCE || referringEntry.combineAccessRules();
 			return new BuildpathEntry(
 					getContentKind(),
 					getEntryKind(),
 					getPath(),									
 					referringEntry.isExported() || this.isExported, // duplicate container entry for tagging it as exported
 					this.inclusionPatterns,	this.exclusionPatterns, 
 					combine(referringEntry.getAccessRules(), getAccessRules(), combine),
 					this.combineAccessRules, 
 					this.extraAttributes, 
 					this.isExternal);
 		}
 		// no need to clone
 		return this;
 	}
 
 	private IAccessRule[] combine(IAccessRule[] referringRules, IAccessRule[] rules, boolean combine) {
 		if (!combine)
 			return rules;
 		if (rules == null || rules.length == 0)
 			return referringRules;
 		// concat access rules
 		int referringRulesLength = referringRules.length;
 		int accessRulesLength = rules.length;
 		int rulesLength = referringRulesLength + accessRulesLength;
 		IAccessRule[] result = new IAccessRule[rulesLength];
 		System.arraycopy(referringRules, 0, result, 0, referringRulesLength);
 		System.arraycopy(rules, 0, result, referringRulesLength, accessRulesLength);
 		return result;
 	}
 
 	/**
 	 * @see IBuildpathEntry#getAccessRules()
 	 */
 	public IAccessRule[] getAccessRules() {
 		if (this.accessRuleSet == null)
 			return NO_ACCESS_RULES;
 		AccessRule[] rules = this.accessRuleSet.getAccessRules();
 		int length = rules.length;
 		if (length == 0)
 			return NO_ACCESS_RULES;
 		IAccessRule[] result = new IAccessRule[length];
 		System.arraycopy(rules, 0, result, 0, length);
 		return result;
 	}
 
 	public static IAccessRule[] getAccessRules(IPath[] accessibleFiles, IPath[] nonAccessibleFiles) {
 		int accessibleFilesLength = accessibleFiles == null ? 0 : accessibleFiles.length;
 		int nonAccessibleFilesLength = nonAccessibleFiles == null ? 0 : nonAccessibleFiles.length;
 		int length = accessibleFilesLength + nonAccessibleFilesLength;
 		if (length == 0)
 			return null;
 		IAccessRule[] accessRules = new IAccessRule[length];
 		for (int i = 0; i < accessibleFilesLength; i++) {
 			accessRules[i] = DLTKCore.newAccessRule(accessibleFiles[i], IAccessRule.K_ACCESSIBLE);
 		}
 		for (int i = 0; i < nonAccessibleFilesLength; i++) {
 			accessRules[accessibleFilesLength + i] = DLTKCore.newAccessRule(nonAccessibleFiles[i], IAccessRule.K_NON_ACCESSIBLE);
 		}
 		return accessRules;
 	}
 
 	/**
 	 * Returns a printable representation of this buildpath entry.
 	 */
 	public String toString() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(getPath().toString());
 		buffer.append('[');
 		switch (getEntryKind()) {
 		case IBuildpathEntry.BPE_LIBRARY:
 			buffer.append("BPE_LIBRARY"); //$NON-NLS-1$
 			break;
 		case IBuildpathEntry.BPE_PROJECT:
 			buffer.append("BPE_PROJECT"); //$NON-NLS-1$
 			break;
 		case IBuildpathEntry.BPE_SOURCE:
 			buffer.append("BPE_SOURCE"); //$NON-NLS-1$
 			break;
 		// case IBuildpathEntry.BPE_VARIABLE :
 		// buffer.append("BPE_VARIABLE"); //$NON-NLS-1$
 		// break;
 		case IBuildpathEntry.BPE_CONTAINER:
 			buffer.append("BPE_CONTAINER"); //$NON-NLS-1$
 			break;
 		}
 		buffer.append("]["); //$NON-NLS-1$
 		switch (getContentKind()) {
 		case IProjectFragment.K_SOURCE:
 			buffer.append("K_SOURCE"); //$NON-NLS-1$
 			break;
 		}
 		buffer.append(']');
 		buffer.append("[isExported:"); //$NON-NLS-1$
 		buffer.append(this.isExported);
 		buffer.append(']');
 		IPath[] patterns = this.inclusionPatterns;
 		int length;
 		if ((length = patterns == null ? 0 : patterns.length) > 0) {
 			buffer.append("[including:"); //$NON-NLS-1$
 			for (int i = 0; i < length; i++) {
 				buffer.append(patterns[i]);
 				if (i != length - 1) {
 					buffer.append('|');
 				}
 			}
 			buffer.append(']');
 		}
 		patterns = this.exclusionPatterns;
 		if ((length = patterns == null ? 0 : patterns.length) > 0) {
 			buffer.append("[excluding:"); //$NON-NLS-1$
 			for (int i = 0; i < length; i++) {
 				buffer.append(patterns[i]);
 				if (i != length - 1) {
 					buffer.append('|');
 				}
 			}
 			buffer.append(']');
 		}
 		if (this.accessRuleSet != null) {
 			buffer.append('[');
 			buffer.append(this.accessRuleSet.toString(false/* on one line */));
 			buffer.append(']');
 		}
 		if (this.entryKind == BPE_PROJECT) {
 			buffer.append("[combine access rules:"); //$NON-NLS-1$
 			buffer.append(this.combineAccessRules);
 			buffer.append(']');
 		}
 		if ((length = this.extraAttributes == null ? 0 : this.extraAttributes.length) > 0) {
 			buffer.append("[attributes:"); //$NON-NLS-1$
 			for (int i = 0; i < length; i++) {
 				buffer.append(this.extraAttributes[i]);
 				if (i != length - 1) {
 					buffer.append(',');
 				}
 			}
 			buffer.append(']');
 		}
 		return buffer.toString();
 	}
 
 	public AccessRuleSet getAccessRuleSet() {
 		return this.accessRuleSet;
 	}
 
 	public int getContentKind() {
 		return contentKind;
 	}
 
 	public static IBuildpathEntry elementDecode(Element element, IDLTKProject project, Map unknownElements) {
 		IPath projectPath = project.getProject().getFullPath();
 		NamedNodeMap attributes = element.getAttributes();
 		NodeList children = element.getChildNodes();
 		boolean[] foundChildren = new boolean[children.getLength()];
 		String kindAttr = removeAttribute(TAG_KIND, attributes);
 		String pathAttr = removeAttribute(TAG_PATH, attributes);		
 		// ensure path is absolute
 		IPath path = new Path(pathAttr);
 		int kind = kindFromString(kindAttr);
 		if (kind != IBuildpathEntry.BPE_CONTAINER && !path.isAbsolute()) {
 			path = projectPath.append(path);
 		}
 		// exported flag (optional)
 		boolean isExported = removeAttribute(TAG_EXPORTED, attributes).equals("true"); //$NON-NLS-1$
 		boolean isExternal = removeAttribute(TAG_EXTERNAL, attributes).equals("true"); //$NON-NLS-1$
 		// inclusion patterns (optional)
 		IPath[] inclusionPatterns = decodePatterns(attributes, TAG_INCLUDING);
 		if (inclusionPatterns == null)
 			inclusionPatterns = INCLUDE_ALL;
 		// exclusion patterns (optional)
 		IPath[] exclusionPatterns = decodePatterns(attributes, TAG_EXCLUDING);
 		if (exclusionPatterns == null)
 			exclusionPatterns = EXCLUDE_NONE;
 		// access rules (optional)
 		NodeList attributeList = getChildAttributes(TAG_ACCESS_RULES, children, foundChildren);
 		IAccessRule[] accessRules = decodeAccessRules(attributeList);
 		// combine access rules (optional)
 		boolean combineAccessRestrictions = !removeAttribute(TAG_COMBINE_ACCESS_RULES, attributes).equals("false"); //$NON-NLS-1$
 		// extra attributes (optional)
 		attributeList = getChildAttributes(TAG_ATTRIBUTES, children, foundChildren);
 		IBuildpathAttribute[] extraAttributes = decodeExtraAttributes(attributeList);
 		String[] unknownAttributes = null;
 		ArrayList unknownChildren = null;
 		if (unknownElements != null) {
 			// unknown attributes
 			int unknownAttributeLength = attributes.getLength();
 			if (unknownAttributeLength != 0) {
 				unknownAttributes = new String[unknownAttributeLength * 2];
 				for (int i = 0; i < unknownAttributeLength; i++) {
 					Node attribute = attributes.item(i);
 					unknownAttributes[i * 2] = attribute.getNodeName();
 					unknownAttributes[i * 2 + 1] = attribute.getNodeValue();
 				}
 			}
 			// unknown children
 			for (int i = 0, length = foundChildren.length; i < length; i++) {
 				if (!foundChildren[i]) {
 					Node node = children.item(i);
 					if (node.getNodeType() != Node.ELEMENT_NODE)
 						continue;
 					if (unknownChildren == null)
 						unknownChildren = new ArrayList();
 					StringBuffer buffer = new StringBuffer();
 					decodeUnknownNode(node, buffer, project);
 					unknownChildren.add(buffer.toString());
 				}
 			}
 		}
 		// recreate the BP entry
 		IBuildpathEntry entry = null;
 		switch (kind) {
 		case IBuildpathEntry.BPE_PROJECT:
 			entry = new BuildpathEntry(IProjectFragment.K_SOURCE, IBuildpathEntry.BPE_PROJECT, path, isExported,
 					BuildpathEntry.INCLUDE_ALL, // inclusion patterns
 					BuildpathEntry.EXCLUDE_NONE, // exclusion patterns
 					accessRules, combineAccessRestrictions, extraAttributes, false);
 			break;
 		case IBuildpathEntry.BPE_LIBRARY:
 			entry = DLTKCore.newLibraryEntry(path, accessRules, extraAttributes, inclusionPatterns, exclusionPatterns, isExported, isExternal );
 			break;
 		case IBuildpathEntry.BPE_SOURCE:
 			// entry = DLTKCore.newSourceEntry(path, inclusionPatterns,
 			// exclusionPatterns, extraAttributes);
 			String projSegment = path.segment(0);
 			if (projSegment != null && projSegment.equals(project.getElementName())) { // this
 				// project
 				entry = DLTKCore.newSourceEntry(path, inclusionPatterns, exclusionPatterns, extraAttributes);
 			} else {
 				if (path.segmentCount() == 1) {
 					// another project
 					entry = DLTKCore.newProjectEntry(path, accessRules, combineAccessRestrictions, extraAttributes, isExported);
 				} else {
 					// an invalid source folder
 					entry = DLTKCore.newSourceEntry(path, inclusionPatterns, exclusionPatterns, extraAttributes);
 				}
 			}
 			break;
 		case IBuildpathEntry.BPE_CONTAINER:
 			entry = DLTKCore.newContainerEntry(path, isExported);
 			break;
 		default:
 			throw new AssertionFailedException(Messages.bind(Messages.buildpath_unknownKind, kindAttr));
 		}
 		if (unknownAttributes != null || unknownChildren != null) {
 			UnknownXmlElements unknownXmlElements = new UnknownXmlElements();
 			unknownXmlElements.attributes = unknownAttributes;
 			unknownXmlElements.children = unknownChildren;
 			unknownElements.put(path, unknownXmlElements);
 		}
 		return entry;
 	}
 
 	static IBuildpathAttribute[] decodeExtraAttributes(NodeList attributes) {
 		if (attributes == null)
 			return NO_EXTRA_ATTRIBUTES;
 		int length = attributes.getLength();
 		if (length == 0)
 			return NO_EXTRA_ATTRIBUTES;
 		IBuildpathAttribute[] result = new IBuildpathAttribute[length];
 		int index = 0;
 		for (int i = 0; i < length; ++i) {
 			Node node = attributes.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				Element attribute = (Element) node;
 				String name = attribute.getAttribute(TAG_ATTRIBUTE_NAME);
 				if (name == null)
 					continue;
 				String value = attribute.getAttribute(TAG_ATTRIBUTE_VALUE);
 				if (value == null)
 					continue;
 				result[index++] = new BuildpathAttribute(name, value);
 			}
 		}
 		if (index != length)
 			System.arraycopy(result, 0, result = new IBuildpathAttribute[index], 0, index);
 		return result;
 	}
 
 	static IAccessRule[] decodeAccessRules(NodeList list) {
 		if (list == null)
 			return null;
 		int length = list.getLength();
 		if (length == 0)
 			return null;
 		IAccessRule[] result = new IAccessRule[length];
 		int index = 0;
 		for (int i = 0; i < length; i++) {
 			Node accessRule = list.item(i);
 			if (accessRule.getNodeType() == Node.ELEMENT_NODE) {
 				Element elementAccessRule = (Element) accessRule;
 				String pattern = elementAccessRule.getAttribute(TAG_PATTERN);
 				if (pattern == null)
 					continue;
 				String tagKind = elementAccessRule.getAttribute(TAG_KIND);
 				int kind;
 				if (TAG_ACCESSIBLE.equals(tagKind))
 					kind = IAccessRule.K_ACCESSIBLE;
 				else if (TAG_NON_ACCESSIBLE.equals(tagKind))
 					kind = IAccessRule.K_NON_ACCESSIBLE;
 				else if (TAG_DISCOURAGED.equals(tagKind))
 					kind = IAccessRule.K_DISCOURAGED;
 				else
 					continue;
 				boolean ignoreIfBetter = "true".equals(elementAccessRule.getAttribute(TAG_IGNORE_IF_BETTER)); //$NON-NLS-1$
 				result[index++] = new BuildpathAccessRule(new Path(pattern), ignoreIfBetter ? kind | IAccessRule.IGNORE_IF_BETTER : kind);
 			}
 		}
 		if (index != length)
 			System.arraycopy(result, 0, result = new IAccessRule[index], 0, index);
 		return result;
 	}
 
 	/**
 	 * Decode some element tag containing a sequence of patterns into IPath[]
 	 */
 	private static IPath[] decodePatterns(NamedNodeMap nodeMap, String tag) {
 		String sequence = removeAttribute(tag, nodeMap);
 		if (!sequence.equals("")) { //$NON-NLS-1$ 
 			char[][] patterns = CharOperation.splitOn('|', sequence.toCharArray());
 			int patternCount;
 			if ((patternCount = patterns.length) > 0) {
 				IPath[] paths = new IPath[patternCount];
 				int index = 0;
 				for (int j = 0; j < patternCount; j++) {
 					char[] pattern = patterns[j];
 					if (pattern.length == 0)
 						continue; // see
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=105581
 					paths[index++] = new Path(new String(pattern));
 				}
 				if (index < patternCount)
 					System.arraycopy(paths, 0, paths = new IPath[index], 0, index);
 				return paths;
 			}
 		}
 		return null;
 	}
 
 	private static void decodeUnknownNode(Node node, StringBuffer buffer, IDLTKProject project) {
 		ByteArrayOutputStream s = new ByteArrayOutputStream();
 		OutputStreamWriter writer;
 		try {
 			writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
 			XMLWriter xmlWriter = new XMLWriter(writer, project, false/*
 																		 * don't
 																		 * print
 																		 * XML
 																		 * version
 																		 */);
 			decodeUnknownNode(node, xmlWriter, true/* insert new line */);
 			xmlWriter.flush();
 			xmlWriter.close();
 			buffer.append(s.toString("UTF8")); //$NON-NLS-1$
 		} catch (UnsupportedEncodingException e) {
 			// ignore (UTF8 is always supported)
 		}
 	}
 
 	private static void decodeUnknownNode(Node node, XMLWriter xmlWriter, boolean insertNewLine) {
 		switch (node.getNodeType()) {
 		case Node.ELEMENT_NODE:
 			NamedNodeMap attributes = node.getAttributes();
 			HashMap parameters = new HashMap();
 			for (int i = 0, length = attributes == null ? 0 : attributes.getLength(); i < length; i++) {
 				Node attribute = attributes.item(i);
 				parameters.put(attribute.getNodeName(), attribute.getNodeValue());
 			}
 			NodeList children = node.getChildNodes();
 			int childrenLength = children.getLength();
 			String nodeName = node.getNodeName();
 			xmlWriter.printTag(nodeName, parameters, false/* don't insert tab */, false/*
 																						 * don't
 																						 * insert
 																						 * new
 																						 * line
 																						 */, childrenLength == 0/*
 									 * close tag if no children
 									 */);
 			if (childrenLength > 0) {
 				for (int i = 0; i < childrenLength; i++) {
 					decodeUnknownNode(children.item(i), xmlWriter, false/*
 																		 * don't
 																		 * insert
 																		 * new
 																		 * line
 																		 */);
 				}
 				xmlWriter.endTag(nodeName, false/* don't insert tab */, insertNewLine);
 			}
 			break;
 		case Node.TEXT_NODE:
 			String data = ((Text) node).getData();
 			xmlWriter.printString(data, false/* don't insert tab */, false/*
 																			 * don't
 																			 * insert
 																			 * new
 																			 * line
 																			 */);
 			break;
 		}
 	}
 
 	/*
 	 * Returns a char based representation of the exclusions patterns full path.
 	 */
 	public char[][] fullExclusionPatternChars() {
 		if (this.fullExclusionPatternChars == UNINIT_PATTERNS) {
 			int length = this.exclusionPatterns.length;
 			this.fullExclusionPatternChars = new char[length][];
 			IPath prefixPath = this.path.removeTrailingSeparator();
 			for (int i = 0; i < length; i++) {
 				this.fullExclusionPatternChars[i] = prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
 			}
 		}
 		return this.fullExclusionPatternChars;
 	}
 
 	/*
 	 * Returns a char based representation of the exclusions patterns full path.
 	 */
 	public char[][] fullInclusionPatternChars() {
 		if (this.fullInclusionPatternChars == UNINIT_PATTERNS) {
 			int length = this.inclusionPatterns.length;
 			this.fullInclusionPatternChars = new char[length][];
 			IPath prefixPath = this.path.removeTrailingSeparator();
 			for (int i = 0; i < length; i++) {
 				this.fullInclusionPatternChars[i] = prefixPath.append(this.inclusionPatterns[i]).toString().toCharArray();
 			}
 		}
 		return this.fullInclusionPatternChars;
 	}
 
 	public static NodeList getChildAttributes(String childName, NodeList children, boolean[] foundChildren) {
 		for (int i = 0, length = foundChildren.length; i < length; i++) {
 			Node node = children.item(i);
 			if (childName.equals(node.getNodeName())) {
 				foundChildren[i] = true;
 				return node.getChildNodes();
 			}
 		}
 		return null;
 	}
 
 	private static String removeAttribute(String nodeName, NamedNodeMap nodeMap) {
 		Node node = removeNode(nodeName, nodeMap);
 		if (node == null)
 			return ""; // //$NON-NLS-1$
 		return node.getNodeValue();
 	}
 
 	private static Node removeNode(String nodeName, NamedNodeMap nodeMap) {
 		try {
 			return nodeMap.removeNamedItem(nodeName);
 		} catch (DOMException e) {
 			if (e.code != DOMException.NOT_FOUND_ERR)
 				throw e;
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the kind of a <code>ProjectFragment</code> from its
 	 * <code>String</code> form.
 	 */
 	static int kindFromString(String kindStr) {
 		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
 			return IBuildpathEntry.BPE_PROJECT;
 		if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
 			return IBuildpathEntry.BPE_CONTAINER;
 		if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
 			return IBuildpathEntry.BPE_SOURCE;
 		if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
 			return IBuildpathEntry.BPE_LIBRARY;
 		return -1;
 	}
 
 	/**
 	 * Returns the XML encoding of the class path.
 	 */
 	public void elementEncode(XMLWriter writer, IPath projectPath, boolean indent, boolean newLine, Map unknownElements) {
 		HashMap parameters = new HashMap();
 		parameters.put(TAG_KIND, kindToString(this.entryKind));
 		IPath xmlPath = this.path;
 		if (this.entryKind != IBuildpathEntry.BPE_CONTAINER && this.entryKind != IBuildpathEntry.BPE_CONTAINER) {
 			// translate to project relative from absolute (unless a device
 			// path)
 			if (xmlPath.isAbsolute()) {
 				if (projectPath != null && projectPath.isPrefixOf(xmlPath)) {
 					if (xmlPath.segment(0).equals(projectPath.segment(0))) {
 						xmlPath = xmlPath.removeFirstSegments(1);
 						xmlPath = xmlPath.makeRelative();
 					} else {
 						xmlPath = xmlPath.makeAbsolute();
 					}
 				}
 			}
 		}
 		parameters.put(TAG_PATH, String.valueOf(xmlPath));
 		if (this.isExported) {
 			parameters.put(TAG_EXPORTED, "true");//$NON-NLS-1$
 		}
 		if (this.isExternal) {
 			parameters.put(TAG_EXTERNAL, "true");//$NON-NLS-1$
 		}
 		encodePatterns(this.inclusionPatterns, TAG_INCLUDING, parameters);
 		encodePatterns(this.exclusionPatterns, TAG_EXCLUDING, parameters);
 		if (this.entryKind == BPE_PROJECT && !this.combineAccessRules) {
 			parameters.put(TAG_COMBINE_ACCESS_RULES, "false"); //$NON-NLS-1$
 		}
 		// unknown attributes
 		UnknownXmlElements unknownXmlElements = unknownElements == null ? null : (UnknownXmlElements) unknownElements.get(this.path);
 		String[] unknownAttributes;
 		if (unknownXmlElements != null && (unknownAttributes = unknownXmlElements.attributes) != null) {
 			for (int i = 0, length = unknownAttributes.length; i < length; i += 2) {
 				String tagName = unknownAttributes[i];
 				String tagValue = unknownAttributes[i + 1];
 				parameters.put(tagName, tagValue);
 			}
 		}
 		boolean hasExtraAttributes = this.extraAttributes.length != 0;
 		boolean hasRestrictions = getAccessRuleSet() != null; // access rule
 		// set is null if no access rules
 		ArrayList unknownChildren = unknownXmlElements != null ? unknownXmlElements.children : null;
 		boolean hasUnknownChildren = unknownChildren != null;
 		writer.printTag(TAG_BUILDPATHENTRY, parameters, indent, newLine, !hasExtraAttributes && !hasRestrictions && !hasUnknownChildren/*
 																																		 * close
 																																		 * tag
 																																		 * if
 																																		 * no
 																																		 * extra
 																																		 * attributes,
 																																		 * no
 																																		 * restriction
 																																		 * and
 																																		 * no
 																																		 * unknown
 																																		 * children
 																																		 */);
 		if (hasExtraAttributes)
 			encodeExtraAttributes(writer, indent, newLine);
 		if (hasRestrictions)
 			encodeAccessRules(writer, indent, newLine);
 		if (hasUnknownChildren)
 			encodeUnknownChildren(writer, indent, newLine, unknownChildren);
 		if (hasUnknownChildren)
 			writer.endTag(TAG_BUILDPATHENTRY, indent, true/* insert new line */);
 		if (hasExtraAttributes || hasRestrictions || hasUnknownChildren)
 			writer.endTag(TAG_BUILDPATHENTRY, indent, true/* insert new line */);
 	}
 
 	void encodeExtraAttributes(XMLWriter writer, boolean indent, boolean newLine) {
 		writer.startTag(TAG_ATTRIBUTES, indent);
 		for (int i = 0; i < this.extraAttributes.length; i++) {
 			IBuildpathAttribute attribute = this.extraAttributes[i];
 			HashMap parameters = new HashMap();
 			parameters.put(TAG_ATTRIBUTE_NAME, attribute.getName());
 			parameters.put(TAG_ATTRIBUTE_VALUE, attribute.getValue());
 			writer.printTag(TAG_ATTRIBUTE, parameters, indent, newLine, true);
 		}
 		writer.endTag(TAG_ATTRIBUTES, indent, true/* insert new line */);
 	}
 
 	void encodeAccessRules(XMLWriter writer, boolean indent, boolean newLine) {
 		writer.startTag(TAG_ACCESS_RULES, indent);
 		AccessRule[] rules = getAccessRuleSet().getAccessRules();
 		for (int i = 0, length = rules.length; i < length; i++) {
 			encodeAccessRule(rules[i], writer, indent, newLine);
 		}
 		writer.endTag(TAG_ACCESS_RULES, indent, true/* insert new line */);
 	}
 
 	private void encodeAccessRule(AccessRule accessRule, XMLWriter writer, boolean indent, boolean newLine) {
 		HashMap parameters = new HashMap();
 		parameters.put(TAG_PATTERN, new String(accessRule.pattern));
 		switch (accessRule.getProblemId()) {
 		case org.eclipse.dltk.compiler.problem.IProblem.ForbiddenReference:
 			parameters.put(TAG_KIND, TAG_NON_ACCESSIBLE);
 			break;
 		case org.eclipse.dltk.compiler.problem.IProblem.DiscouragedReference:
 			parameters.put(TAG_KIND, TAG_DISCOURAGED);
 			break;
 		default:
 			parameters.put(TAG_KIND, TAG_ACCESSIBLE);
 			break;
 		}
 		if (accessRule.ignoreIfBetter())
 			parameters.put(TAG_IGNORE_IF_BETTER, "true"); //$NON-NLS-1$
 		writer.printTag(TAG_ACCESS_RULE, parameters, indent, newLine, true);
 	}
 
 	/**
 	 * Returns a <code>String</code> for the kind of a class path entry.
 	 */
 	static String kindToString(int kind) {
 		switch (kind) {
 		case IBuildpathEntry.BPE_PROJECT:
 			return "prj"; // backward compatibility //$NON-NLS-1$
 		case IBuildpathEntry.BPE_SOURCE:
 			return "src"; //$NON-NLS-1$
 		case IBuildpathEntry.BPE_LIBRARY:
 			return "lib"; //$NON-NLS-1$
 		case IBuildpathEntry.BPE_CONTAINER:
 			return "con"; //$NON-NLS-1$
 		default:
 			return "unknown"; //$NON-NLS-1$
 		}
 	}
 
 	private void encodeUnknownChildren(XMLWriter writer, boolean indent, boolean newLine, ArrayList unknownChildren) {
 		for (int i = 0, length = unknownChildren.size(); i < length; i++) {
 			String child = (String) unknownChildren.get(i);
 			writer.printString(child, indent, false/* don't insert new line */);
 		}
 	}
 
 	/**
 	 * Validate a given buildpath for a project, using the following rules:
 	 * <ul>
 	 * <li> Buildpath entries cannot collide with each other; that is, all entry
 	 * paths must be unique. *
 	 * <li> A project entry cannot refer to itself directly (that is, a project
 	 * cannot prerequisite itself).
 	 * <li> Buildpath entries cannot coincidate or be nested in each other.
 	 * </ul>
 	 * 
 	 * Note that the buildpath entries are not validated automatically. Only
 	 * bound variables or containers are considered in the checking process
 	 * (this allows to perform a consistency check on a buildpath which has
 	 * references to yet non existing projects, folders, ...).
 	 * <p>
 	 * This validation is intended to anticipate buildpath issues prior to
 	 * assigning it to a project. In particular, it will automatically be
 	 * performed during the buildpath setting operation (if validation fails,
 	 * the buildpath setting will not complete).
 	 * <p>
 	 * 
 	 * @param dltkProject
 	 *            the given script project
 	 * @param rawBuildpath
 	 *            a given buildpath
 	 * @return a status object with code <code>ModelStatus.VERIFIED_OK</code>
 	 *         if the given buildpath are correct, otherwise a status object
 	 *         indicating what is wrong with the buildpath
 	 */
 	public static IModelStatus validateBuildpath(IDLTKProject dltkProject, IBuildpathEntry[] rawBuildpath) {
 		// TODO implement
 		IProject project = dltkProject.getProject();
 		IPath projectPath = project.getFullPath();
 		String projectName = dltkProject.getElementName();
 		// tolerate null path, it will be reset to default
 		if (rawBuildpath == null) {
 			return ModelStatus.VERIFIED_OK;
 		}
 		// retrieve resolved buildpath
 		IBuildpathEntry[] buildpath;
 		try {
 			buildpath = ((DLTKProject)dltkProject).getResolvedBuildpath(rawBuildpath,true/*ignore pb*/, false/*no marker*/, null /*no reverse map*/);
 		} catch (ModelException e) {
 			return e.getModelStatus();
 		}
 		int length = buildpath.length;
 		//int sourceEntryCount = 0;
 		boolean disableExclusionPatterns = DLTKCore.DISABLED.equals(dltkProject.getOption(
 				DLTKCore.CORE_ENABLE_BUILDPATH_EXCLUSION_PATTERNS, true));
 		for (int i = 0; i < length; i++) {
 			IBuildpathEntry resolvedEntry = buildpath[i];
 			if (disableExclusionPatterns
 					&& ((resolvedEntry.getInclusionPatterns() != null && resolvedEntry.getInclusionPatterns().length > 0) || (resolvedEntry.getExclusionPatterns() != null && resolvedEntry.getExclusionPatterns().length > 0))) {
 				return new ModelStatus(IModelStatusConstants.DISABLED_BP_EXCLUSION_PATTERNS, dltkProject, resolvedEntry.getPath());
 			}
 		}
 		HashSet pathes = new HashSet(length);
 		// check all entries
 		for (int i = 0; i < length; i++) {
 			IBuildpathEntry entry = buildpath[i];
 			if (entry == null)
 				continue;
 			IPath entryPath = entry.getPath();
 			int kind = entry.getEntryKind();
 			// Build some common strings for status message
 			boolean isProjectRelative = projectName.equals(entryPath.segment(0));
 			String entryPathMsg = isProjectRelative ? entryPath.removeFirstSegments(1).toString() : entryPath.makeRelative().toString();
 			
 			// complain if duplicate path
 			if (!pathes.add(entryPath)) {
 				return new ModelStatus(IModelStatusConstants.NAME_COLLISION, Messages.bind(Messages.buildpath_duplicateEntryPath,
 						new String[] {
 								entryPathMsg, projectName
 						}));
 			}
 			// no further check if entry coincidates with project or output
 			// location
 			if (entryPath.equals(projectPath)) {
 				// complain if self-referring project entry
 				if (kind == IBuildpathEntry.BPE_PROJECT) {
 					return new ModelStatus(IModelStatusConstants.INVALID_PATH, Messages.bind(Messages.buildpath_cannotReferToItself,
 							entryPath.makeRelative().toString()));
 				}
 				// tolerate nesting output in src if src==prj
 				continue;
 			}
 			// allow nesting source entries in each other as long as the outer
 			// entry excludes the inner one
 			if (kind == IBuildpathEntry.BPE_SOURCE
 					|| (kind == IBuildpathEntry.BPE_LIBRARY 
 							&& !org.eclipse.dltk.compiler.util.Util.isArchiveFileName(entryPath.lastSegment()))) {
 				for (int j = 0; j < buildpath.length; j++) {
 					IBuildpathEntry otherEntry = buildpath[j];
 					if (otherEntry == null)
 						continue;
 					int otherKind = otherEntry.getEntryKind();
 					IPath otherPath = otherEntry.getPath();
 					if (entry != otherEntry
 							&& (otherKind == IBuildpathEntry.BPE_SOURCE || 
 							(otherKind == IBuildpathEntry.BPE_LIBRARY && !org.eclipse.dltk.compiler.util.Util.isArchiveFileName(otherPath.lastSegment())))) {
 						char[][] inclusionPatterns, exclusionPatterns;
 						if (otherPath.isPrefixOf(entryPath)
 								&& !otherPath.equals(entryPath)
 								&& !Util.isExcluded(
 										entryPath.append("*"), inclusionPatterns = ((BuildpathEntry) otherEntry).fullInclusionPatternChars(), exclusionPatterns = ((BuildpathEntry) otherEntry).fullExclusionPatternChars(), false)) { //$NON-NLS-1$
 							String exclusionPattern = entryPath.removeFirstSegments(otherPath.segmentCount()).segment(0);
 							if (Util.isExcluded(entryPath, inclusionPatterns, exclusionPatterns, false)) {
 								return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(
 										Messages.buildpath_mustEndWithSlash, new String[] {
 												exclusionPattern, entryPath.makeRelative().toString()
 										}));
 							} else {
 								if (otherKind == IBuildpathEntry.BPE_SOURCE) {
 									exclusionPattern += '/';
 									if (!disableExclusionPatterns) {
 										return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(
 												Messages.buildpath_cannotNestEntryInEntry, new String[] {
 														entryPath.makeRelative().toString(),
 														otherEntry.getPath().makeRelative().toString(), exclusionPattern
 												}));
 									} else {
 										return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(
 												Messages.buildpath_cannotNestEntryInEntryNoExclusion, new String[] {
 														entryPath.makeRelative().toString(),
 														otherEntry.getPath().makeRelative().toString(), exclusionPattern
 												}));
 									}
 								} else {
 									return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(
 											Messages.buildpath_cannotNestEntryInLibrary, new String[] {
 													entryPath.makeRelative().toString(), otherEntry.getPath().makeRelative().toString()
 											}));
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return ModelStatus.VERIFIED_OK;
 	}
 
 	/**
 	 * Returns a model status describing the problem related to this buildpath
 	 * entry if any, a status object with code <code>IStatus.OK</code> if the
 	 * entry is fine (that is, if the given buildpath entry denotes a valid
 	 * element to be referenced onto a buildpath).
 	 * 
 	 * @param project
 	 *            the given script project
 	 * @param entry
 	 *            the given buildpath entry
 	 * @param checkSourceAttachment
 	 *            a flag to determine if source attachement should be checked
 	 * @param recurseInContainers
 	 *            flag indicating whether validation should be applied to
 	 *            container entries recursively
 	 * @return a model status describing the problem related to this buildpath
 	 *         entry if any, a status object with code <code>IStatus.OK</code>
 	 *         if the entry is fine
 	 */
 	public static IModelStatus validateBuildpathEntry(IDLTKProject project, IBuildpathEntry entry, boolean recurseInContainers) {
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IPath path = entry.getPath();
 		// Build some common strings for status message
 		String projectName = project.getElementName();
 		boolean pathStartsWithProject = projectName.equals(path.segment(0));
 		String entryPathMsg = pathStartsWithProject ? path.removeFirstSegments(1).makeRelative().toString() : path.toString();
 		switch (entry.getEntryKind()) {
 		// container entry check
 		case IBuildpathEntry.BPE_CONTAINER:
 			if (path != null && path.segmentCount() >= 1) {
 				try {
 					IBuildpathContainer container = ModelManager.getModelManager().getBuildpathContainer(path, project);
 					// container retrieval is performing validation check on
 					// container entry kinds.
 					if (container == null) {
 						return new ModelStatus(IModelStatusConstants.BP_CONTAINER_PATH_UNBOUND, project, path);
 					} else if (container == ModelManager.CONTAINER_INITIALIZATION_IN_PROGRESS) {
 						// Validate extra attributes
 						IBuildpathAttribute[] extraAttributes = entry.getExtraAttributes();
 						if (extraAttributes != null) {
 							int length = extraAttributes.length;
 							HashSet set = new HashSet(length);
 							for (int i = 0; i < length; i++) {
 								String attName = extraAttributes[i].getName();
 								if (!set.add(attName)) {
 									return new ModelStatus(IModelStatusConstants.NAME_COLLISION, Messages.bind(
 											Messages.buildpath_duplicateEntryExtraAttribute, new String[] {
 													attName, entryPathMsg, projectName
 											}));
 								}
 							}
 						}
 						// don't create a marker if initialization is in
 						// progress (case of cp initialization batching)
 						return ModelStatus.VERIFIED_OK;
 					}
 					IBuildpathEntry[] containerEntries = container.getBuildpathEntries();
 					if (containerEntries != null) {
 						for (int i = 0, length = containerEntries.length; i < length; i++) {
 							IBuildpathEntry containerEntry = containerEntries[i];
 							int kind = containerEntry == null ? 0 : containerEntry.getEntryKind();
 							if (containerEntry == null || kind == IBuildpathEntry.BPE_SOURCE || kind == IBuildpathEntry.BPE_CONTAINER) {
 								String description = container.getDescription();
 								if (description == null)
 									description = path.makeRelative().toString();
 								return new ModelStatus(IModelStatusConstants.INVALID_BP_CONTAINER_ENTRY, project, path);
 							}
 							if (recurseInContainers) {
 								IModelStatus containerEntryStatus = validateBuildpathEntry(project, containerEntry, recurseInContainers);
 								if (!containerEntryStatus.isOK()) {
 									return containerEntryStatus;
 								}
 							}
 						}
 					}
 				} catch (ModelException e) {
 					return new ModelStatus(e);
 				}
 			} else {
 				return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_illegalContainerPath,
 						new String[] {
 								entryPathMsg, projectName
 						}));
 			}
 			break;
 		// library entry check
 		case IBuildpathEntry.BPE_LIBRARY:
 			if (path != null && path.isAbsolute() && !path.isEmpty()) {
 				Object target = Model.getTarget(workspaceRoot, path, true);
 				// TODO: Add here some library version cheking
 				if( !entry.isExternal() ) {
 					if (target instanceof IResource) {
 						IResource resolvedResource = (IResource) target;
 						switch (resolvedResource.getType()) {
 						case IResource.FILE:
 							break;
 						case IResource.FOLDER: // internal binary folder
 							break;
 						}
 					} else if (target instanceof File) {
 						File file = (File)target;
 						if (!file.exists()) {
 							return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(
 									Messages.buildpath_illegalLibraryArchive, new String[] {
 											path.toOSString(), projectName
 									}));
 						}
 					}
 				}
 				else {
 					File file = new File(entry.getPath().toOSString());
 					if(file == null || !file.exists()) {
 						return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(
 								Messages.buildpath_illegalExternalFolder, new String[] {
 										path.toOSString(), projectName
 								}));
 					}
 				}
 			} else {
 				return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_illegalLibraryPath,
 						new String[] {
 								entryPathMsg, projectName
 						}));
 			}
 			break;
 		// project entry check
 		case IBuildpathEntry.BPE_PROJECT:
 			if (path != null && path.isAbsolute() && path.segmentCount() == 1) {
 				IProject prereqProjectRsc = workspaceRoot.getProject(path.segment(0));
 				if (!prereqProjectRsc.exists() || !DLTKLanguageManager.hasScriptNature(prereqProjectRsc)) {
 					return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_unboundProject,
 							new String[] {
 									path.segment(0), projectName
 							}));
 				}
 				if (!prereqProjectRsc.isOpen()) {
 					return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_closedProject,
 							new String[] {
 								path.segment(0)
 							}));
 				}
 			} else {
 				return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_illegalProjectPath,
 						new String[] {
 								path.toString(), projectName
 						}));
 			}
 			break;
 		// project source folder
 		case IBuildpathEntry.BPE_SOURCE:
 			if (((entry.getInclusionPatterns() != null && entry.getInclusionPatterns().length > 0) || (entry.getExclusionPatterns() != null && entry.getExclusionPatterns().length > 0))
 					&& DLTKCore.DISABLED.equals(project.getOption(DLTKCore.CORE_ENABLE_BUILDPATH_EXCLUSION_PATTERNS, true))) {
 				return new ModelStatus(IModelStatusConstants.DISABLED_BP_EXCLUSION_PATTERNS, project, path);
 			}
 			if (path != null && path.isAbsolute() && !path.isEmpty()) {
 				IPath projectPath = project.getProject().getFullPath();
 				if (!projectPath.isPrefixOf(path) || Model.getTarget(workspaceRoot, path, true) == null) {
 					return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_unboundSourceFolder,
 							new String[] {
 									entryPathMsg, projectName
 							}));
 				}
 			} else {
 				return new ModelStatus(IModelStatusConstants.INVALID_BUILDPATH, Messages.bind(Messages.buildpath_illegalSourceFolderPath,
 						new String[] {
 								entryPathMsg, projectName
 						}));
 			}
 			break;
 		}
 		// Validate extra attributes
 		IBuildpathAttribute[] extraAttributes = entry.getExtraAttributes();
 		if (extraAttributes != null) {
 			int length = extraAttributes.length;
 			HashSet set = new HashSet(length);
 			for (int i = 0; i < length; i++) {
 				String attName = extraAttributes[i].getName();
 				if (!set.add(attName)) {
 					return new ModelStatus(IModelStatusConstants.NAME_COLLISION, Messages.bind(
 							Messages.buildpath_duplicateEntryExtraAttribute, new String[] {
 									attName, entryPathMsg, projectName
 							}));
 				}
 			}
 		}
 		return ModelStatus.VERIFIED_OK;
 	}
 
 	/**
 	 * Encode some patterns into XML parameter tag
 	 */
 	private static void encodePatterns(IPath[] patterns, String tag, Map parameters) {
 		if (patterns != null && patterns.length > 0) {
 			StringBuffer rule = new StringBuffer(10);
 			for (int i = 0, max = patterns.length; i < max; i++) {
 				if (i > 0)
 					rule.append('|');
 				rule.append(patterns[i]);
 			}
 			parameters.put(tag, String.valueOf(rule));
 		}
 	}
 
 	/**
 	 * Returns true if the given object is a equivalent buildpath entry.
 	 */
 	public boolean equals(Object object) {
 		if (this == object)
 			return true;
 		if (object instanceof BuildpathEntry) {
 			BuildpathEntry otherEntry = (BuildpathEntry) object;
 			if (this.contentKind != otherEntry.getContentKind())
 				return false;
 			if (this.entryKind != otherEntry.getEntryKind())
 				return false;
 			if (this.isExported != otherEntry.isExported())
 				return false;
 			if (this.isExternal != otherEntry.isExternal())
 				return false;
 			if (!this.path.equals(otherEntry.getPath()))
 				return false;
 			if (!equalPatterns(this.inclusionPatterns, otherEntry.getInclusionPatterns()))
 				return false;
 			if (!equalPatterns(this.exclusionPatterns, otherEntry.getExclusionPatterns()))
 				return false;
 			
 			AccessRuleSet otherRuleSet = otherEntry.getAccessRuleSet();
 			if (getAccessRuleSet() != null) {
 				if (!getAccessRuleSet().equals(otherRuleSet))
 					return false;
 			} else if (otherRuleSet != null)
 				return false;
 			if (this.combineAccessRules != otherEntry.combineAccessRules())
 				return false;			
 			if (!equalAttributes(this.extraAttributes, otherEntry.getExtraAttributes()))
 				return false;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private static boolean equalAttributes(IBuildpathAttribute[] firstAttributes, IBuildpathAttribute[] secondAttributes) {
 		if (firstAttributes != secondAttributes){
 		    if (firstAttributes == null) return false;
 			int length = firstAttributes.length;
 			if (secondAttributes == null || secondAttributes.length != length) 
 				return false;
 			for (int i = 0; i < length; i++) {
 				if (!firstAttributes[i].equals(secondAttributes[i]))
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	private static boolean equalPatterns(IPath[] firstPatterns, IPath[] secondPatterns) {
 		if (firstPatterns != secondPatterns){
 		    if (firstPatterns == null) return false;
 			int length = firstPatterns.length;
 			if (secondPatterns == null || secondPatterns.length != length) 
 				return false;
 			for (int i = 0; i < length; i++) {
 				// compare toStrings instead of IPaths 
 				// since IPath.equals is specified to ignore trailing separators
 				if (!firstPatterns[i].toString().equals(secondPatterns[i].toString()))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean isExternal() {
 		return isExternal;
 	}
 	public boolean isContainerEntry() {
 		return this.fIsContainerEntry;
 	}
 	public void setIsContainerEntry( boolean value) {
 		this.fIsContainerEntry = value;
 	}
 	/**
 	 * Returns the hash code for this buildpath entry
 	 */
 	public int hashCode() {
 		return this.path.hashCode();
 	}
 }
