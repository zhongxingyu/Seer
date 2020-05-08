 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.model;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.WikiBase;
 import org.jamwiki.migrate.MediaWikiConstants;
 
 /**
  * Namespaces allow the organization of wiki topics by dividing topics into
  * groups.  A namespace will precede the topic, such as "Namespace:Topic".
  * Namespaces can be customized by modifying using configuration tools, but
  * the namesapces defined as constants always exist and are required for wiki
  * operation.
  */
 public class Namespace implements Serializable {
 
 	public static final String SEPARATOR = ":";
 	// default namespaces, used during setup.  additional namespaces may be added after setup.
 	// namespace IDs should match Mediawiki to maximize compatibility.
 	public static final Namespace MEDIA                = new Namespace(MediaWikiConstants.MEDIAWIKI_MEDIA_NAMESPACE_ID, "Media", null);
 	public static final Namespace SPECIAL              = new Namespace(MediaWikiConstants.MEDIAWIKI_SPECIAL_NAMESPACE_ID, "Special", null);
 	public static final Namespace MAIN                 = new Namespace(MediaWikiConstants.MEDIAWIKI_MAIN_NAMESPACE_ID, "", null);
 	public static final Namespace COMMENTS             = new Namespace(MediaWikiConstants.MEDIAWIKI_TALK_NAMESPACE_ID, "Comments", Namespace.MAIN);
 	public static final Namespace USER                 = new Namespace(MediaWikiConstants.MEDIAWIKI_USER_NAMESPACE_ID, "User", null);
 	public static final Namespace USER_COMMENTS        = new Namespace(MediaWikiConstants.MEDIAWIKI_USER_TALK_NAMESPACE_ID, "User comments", Namespace.USER);
 	public static final Namespace SITE_CUSTOM          = new Namespace(MediaWikiConstants.MEDIAWIKI_SITE_CUSTOM_NAMESPACE_ID, "Project", null);
 	public static final Namespace SITE_CUSTOM_COMMENTS = new Namespace(MediaWikiConstants.MEDIAWIKI_SITE_CUSTOM_TALK_NAMESPACE_ID, "Project comments", Namespace.SITE_CUSTOM);
 	public static final Namespace FILE                 = new Namespace(MediaWikiConstants.MEDIAWIKI_FILE_NAMESPACE_ID, "Image", null);
 	public static final Namespace FILE_COMMENTS        = new Namespace(MediaWikiConstants.MEDIAWIKI_FILE_TALK_NAMESPACE_ID, "Image comments", Namespace.FILE);
 	public static final Namespace JAMWIKI              = new Namespace(MediaWikiConstants.MEDIAWIKI_MEDIAWIKI_NAMESPACE_ID, "JAMWiki", null);
 	public static final Namespace JAMWIKI_COMMENTS     = new Namespace(MediaWikiConstants.MEDIAWIKI_MEDIAWIKI_TALK_NAMESPACE_ID, "JAMWiki comments", Namespace.JAMWIKI);
 	public static final Namespace TEMPLATE             = new Namespace(MediaWikiConstants.MEDIAWIKI_TEMPLATE_NAMESPACE_ID, "Template", null);
 	public static final Namespace TEMPLATE_COMMENTS    = new Namespace(MediaWikiConstants.MEDIAWIKI_TEMPLATE_TALK_NAMESPACE_ID, "Template comments", Namespace.TEMPLATE);
 	public static final Namespace HELP                 = new Namespace(MediaWikiConstants.MEDIAWIKI_HELP_NAMESPACE_ID, "Help", null);
 	public static final Namespace HELP_COMMENTS        = new Namespace(MediaWikiConstants.MEDIAWIKI_HELP_TALK_NAMESPACE_ID, "Help comments", Namespace.HELP);
 	public static final Namespace CATEGORY             = new Namespace(MediaWikiConstants.MEDIAWIKI_CATEGORY_NAMESPACE_ID, "Category", null);
 	public static final Namespace CATEGORY_COMMENTS    = new Namespace(MediaWikiConstants.MEDIAWIKI_CATEGORY_TALK_NAMESPACE_ID, "Category comments", Namespace.CATEGORY);
 	public static Map<Integer, Namespace> DEFAULT_NAMESPACES = new LinkedHashMap<Integer, Namespace>();
 	private Integer id;
 	private final String label;
 	private Namespace mainNamespace;
 	private Map<String, String> namespaceTranslations = new HashMap<String, String>();
 
 	static {
 		DEFAULT_NAMESPACES.put(Namespace.MEDIA.getId(), Namespace.MEDIA);
 		DEFAULT_NAMESPACES.put(Namespace.SPECIAL.getId(), Namespace.SPECIAL);
 		DEFAULT_NAMESPACES.put(Namespace.MAIN.getId(), Namespace.MAIN);
 		DEFAULT_NAMESPACES.put(Namespace.COMMENTS.getId(), Namespace.COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.USER.getId(), Namespace.USER);
 		DEFAULT_NAMESPACES.put(Namespace.USER_COMMENTS.getId(), Namespace.USER_COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.SITE_CUSTOM.getId(), Namespace.SITE_CUSTOM);
 		DEFAULT_NAMESPACES.put(Namespace.SITE_CUSTOM_COMMENTS.getId(), Namespace.SITE_CUSTOM_COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.FILE.getId(), Namespace.FILE);
 		DEFAULT_NAMESPACES.put(Namespace.FILE_COMMENTS.getId(), Namespace.FILE_COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.JAMWIKI.getId(), Namespace.JAMWIKI);
 		DEFAULT_NAMESPACES.put(Namespace.JAMWIKI_COMMENTS.getId(), Namespace.JAMWIKI_COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.TEMPLATE.getId(), Namespace.TEMPLATE);
 		DEFAULT_NAMESPACES.put(Namespace.TEMPLATE_COMMENTS.getId(), Namespace.TEMPLATE_COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.HELP.getId(), Namespace.HELP);
 		DEFAULT_NAMESPACES.put(Namespace.HELP_COMMENTS.getId(), Namespace.HELP_COMMENTS);
 		DEFAULT_NAMESPACES.put(Namespace.CATEGORY.getId(), Namespace.CATEGORY);
 		DEFAULT_NAMESPACES.put(Namespace.CATEGORY_COMMENTS.getId(), Namespace.CATEGORY_COMMENTS);
 	}
 
 	/**
 	 * Create a namespace and add it to the global list of namespaces.
 	 */
 	public Namespace(Integer id, String label) {
 		this.id = id;
 		this.label = label;
 		if (id != null && DEFAULT_NAMESPACES.get(id) != null) {
 			DEFAULT_NAMESPACES.put(id, this);
 		}
 	}
 
 	/**
 	 * Create a namespace and add it to the global list of namespaces.
 	 */
 	private Namespace(Integer id, String label, Namespace mainNamespace) {
 		this.id = id;
 		this.label = label;
 		this.mainNamespace = mainNamespace;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getId() {
 		return this.id;
 	}
 
 	/**
 	 *
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 *
 	 */
 	public String getDefaultLabel() {
 		return this.label;
 	}
 
 	/**
 	 * Return the virtual-wiki specific namespace, or if one has not been defined
 	 * return the default namespace label
 	 */
 	public String getLabel(String virtualWiki) {
 		return (virtualWiki != null && this.namespaceTranslations.get(virtualWiki) != null) ? this.namespaceTranslations.get(virtualWiki) : this.label;
 	}
 
 	/**
 	 *
 	 */
 	public Namespace getMainNamespace() {
 		return this.mainNamespace;
 	}
 
 	/**
 	 *
 	 */
 	public void setMainNamespace(Namespace mainNamespace) {
 		this.mainNamespace = mainNamespace;
 	}
 
 	/**
 	 *
 	 */
 	public Map<String, String> getNamespaceTranslations() {
 		return this.namespaceTranslations;
 	}
 
 	/**
 	 * Given a namespace, return the Namespace for the corresponding "comments"
 	 * namespace.  If no match exists return <code>null</code>.  Example: if this
 	 * method is called with Namespace.USER_COMMENTS or Namespace.USER as an
 	 * argument, Namespace.USER_COMMENTS will be returned.
 	 */
 	public static Namespace findCommentsNamespace(Namespace namespace) throws DataAccessException {
 		if (namespace == null) {
 			return null;
 		}
 		if (namespace.mainNamespace != null) {
 			// the submitted namespace IS a comments namespace, so return it.
 			return namespace;
 		}
 		// otherwise loop through all namespaces looking for a comments namespace that points
 		// to this namespace.
 		List<Namespace> namespaces = WikiBase.getDataHandler().lookupNamespaces();
 		for (Namespace candidateNamespace : namespaces) {
 			if (candidateNamespace.mainNamespace != null && candidateNamespace.mainNamespace.equals(namespace)) {
 				return candidateNamespace;
 			}
 		}
 		// no match found
 		return null;
 	}
 
 	/**
 	 * Given a namespace, return the Namespace for the "main" (ie, not comments)
 	 * namespace.  If no match exists return <code>null</code>.  Example: if this
 	 * method is called with Namespace.USER_COMMENTS or Namespace.USER as an
 	 * argument, Namespace.USER will be returned.
 	 */
 	public static Namespace findMainNamespace(Namespace namespace) {
 		if (namespace == null) {
 			return null;
 		}
 		return (namespace.mainNamespace == null) ? namespace : namespace.mainNamespace;
 	}
 
 	/**
 	 * Standard equals method.  Two namespaces are equal if they have the same ID.
 	 */
 	public boolean equals(Namespace namespace) {
		return (namespace != null && this.label.equals(namespace.getDefaultLabel()));
 	}
 }
