 package com.redhat.topicindex.component.docbookrenderer.structures.tocformat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.redhat.ecs.commonstructures.Pair;
 import com.redhat.ecs.commonutils.XMLUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 import com.redhat.ecs.services.docbookcompiling.DocbookBuilderConstants;
 import com.redhat.topicindex.rest.entities.TagV1;
 import com.redhat.topicindex.rest.entities.TopicV1;
 
 /**
  * This class holds the details that allow a branch of a table of contents to be
  * generated and populated with topics. It also provides a way to hold the
  * topics themselves, as well the XML data that is used when building a docbook
  * book.
  * 
  * The purpose of this class is to provide a flexible way to define the
  * structure of a table of contents when given an unstructured group of topics.
  */
 public class TocFormatBranch
 {
 	/** defines the parent of this branch */
 	private final TocFormatBranch parent;
 
 	/**
 	 * Defines the tag that this branch represents. Will be null for a top level
 	 * branch
 	 */
 	private final TagV1 tag;
 
 	/** Defines the tags that a child must have to exist as a subbranch */
 	private final TagRequirements childTags;
 
 	/**
 	 * Defines the tags that a topic must have to be listed under this branch.
 	 * This is null if no topics are to be listed under this branch.
 	 */
 	private final TagRequirements displayTags;
 
 	/** Holds any sub branches */
 	private final List<TocFormatBranch> children = new ArrayList<TocFormatBranch>();
 
 	/** Holds any topics that should be listed under this branch */
 	private final Map<TopicV1, Document> topics = new HashMap<TopicV1, Document>();
 
 	public Map<TopicV1, Document> getTopics()
 	{
 		return topics;
 	}
 
 	public TocFormatBranch getParent()
 	{
 		return parent;
 	}
 
 	public List<TocFormatBranch> getChildren()
 	{
 		return children;
 	}
 
 	public TagRequirements getDisplayTags()
 	{
 		return displayTags;
 	}
 
 	public TagRequirements getTagCategoryId()
 	{
 		return childTags;
 	}
 
 	public TagV1 getTag()
 	{
 		return tag;
 	}
 
 	public TocFormatBranch()
 	{
 		this.parent = null;
 		this.tag = null;
 		this.childTags = new TagRequirements();
 		this.displayTags = new TagRequirements();
 	}
 
 	public TocFormatBranch(final TagV1 tag, final TocFormatBranch parent, final TagRequirements childTags, final TagRequirements displayTags)
 	{
 		this.tag = tag;
 		this.childTags = childTags == null ? new TagRequirements() : childTags;
 		this.displayTags = displayTags == null ? new TagRequirements() : displayTags;
 		this.parent = parent;
		
		if (parent != null)
			parent.getChildren().add(this);
 	}
 
 	public void getTagsWithParent(final TagRequirements requirements)
 	{
 		requirements.merge(childTags);
 		if (parent != null)
 			parent.getTagsWithParent(requirements);
 	}
 
 	/**
 	 * @return A string that can be appended to an ID attribute to uniquely identify a topic
 	 * within a particular branch of the TOC
 	 */
 	public String getTOCBranchID()
 	{
 		final StringBuilder retValue = new StringBuilder();
 
 		if (parent != null)
 			retValue.append(parent.getTOCBranchID());
 
 		if (this.tag != null)
 		{
 			retValue.append("-");
 			retValue.append(this.tag.getId());
 		}
 
 		return retValue.toString();
 	}
 
 	/**
 	 * Build the docbook chapter/section structure which reflects the table of contents defined by this
 	 * level of the TOC and its children
 	 * @param useFixedUrls True if the topics are saved in XML files named after their fiex url property tags
 	 * @return The Docbook XML that defines the TOC
 	 */
 	public String buildDocbook(final boolean useFixedUrls)
 	{
 		final StringBuilder docbook = new StringBuilder();
 
 		if (this.parent == null)
 		{
 			if (this.getTopicCount() != 0)
 			{
 				buildDocbookContents(docbook, useFixedUrls);
 			}
 			else
 			{
 				docbook.append("<chapter>");
 				docbook.append("<title>");
 				docbook.append("Error");
 				docbook.append("</title>");
 				docbook.append("<para>");
 				docbook.append("No Content");
 				docbook.append("</para>");
 				docbook.append("</chapter>");
 			}
 		}
 		else if (this.getTopicCount() != 0)
 		{
 			/*
 			 * If this is a top level toc element (i.e. its parent has no
 			 * parent) then build it as a chapter
 			 */
 			if (this.parent.parent == null)
 			{
 				docbook.append("<chapter>");
 				docbook.append("<title>");
 				docbook.append(this.getTag() == null ? "" : this.getTag().getName());
 				docbook.append("</title>");
 
 				buildDocbookContents(docbook, useFixedUrls);
 
 				docbook.append("</chapter>");
 			}
 			else
 			{
 				docbook.append("<section>");
 				docbook.append("<title>");
 				docbook.append(this.getTag() == null ? "" : this.getTag().getName());
 				docbook.append("</title>");
 
 				buildDocbookContents(docbook, useFixedUrls);
 
 				docbook.append("</section>");
 			}
 		}
 
 		return docbook.toString();
 	}
 
 	private void buildDocbookContents(final StringBuilder docbook, final boolean useFixedUrls)
 	{
 		/* append any child branches */
 		for (final TocFormatBranch child : children)
 			docbook.append(child.buildDocbook(useFixedUrls));
 
 		/* Add an xref to each topic that appears under this branch */
 		for (final TopicV1 topic : topics.keySet())
 		{
 			String fileName = "";
 			if (useFixedUrls)
 			{
 				fileName = topic.getXrefPropertyOrId(CommonConstants.FIXED_URL_PROP_TAG_ID) + this.getTOCBranchID() + ".xml";
 			}
 			else
 			{
 				fileName = "Topic" + topic.getId() + this.getTOCBranchID() + ".xml";
 			}
 
 			docbook.append("<xi:include href=\"" + fileName + "\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n");
 		}
 	}
 
 	/**
 	 * @return The number of topics held by this level of the TOC and its children
 	 */
 	public int getTopicCount()
 	{
 		int retValue = this.topics.size();
 
 		for (final TocFormatBranch child : children)
 			retValue += child.getTopicCount();
 
 		return retValue;
 	}
 
 	/**
 	 * @return All the topics that appear in this TOC. An individual topics
 	 *         might be represented by multiple TopicV1 objects in the returned
 	 *         collection.
 	 */
 	public List<TopicV1> getAllTopics()
 	{
 		final List<TopicV1> retValue = new ArrayList<TopicV1>();
 
 		retValue.addAll(this.topics.keySet());
 
 		for (final TocFormatBranch child : children)
 			retValue.addAll(child.getAllTopics());
 
 		return retValue;
 	}
 
 	/**
 	 * @param topicId
 	 *            The topic ID to search for
 	 * @return true if the topic is included this level of the TOC or any or its
 	 *         children, false otherwise
 	 */
 	public boolean isInToc(final Integer topicId)
 	{
 		final List<TopicV1> topics = getAllTopics();
 		for (final TopicV1 topic : topics)
 			if (topic.getId().equals(topicId))
 				return true;
 		return false;
 	}
 
 	/**
 	 * When inserting an xref to a topic, we need to find the closest topic to
 	 * link to. This is because a topic can appear multiple times in a TOC, and
 	 * therefore multiple times in a document.
 	 * 
 	 * This method will search any children of the current TOC branch, moving up
 	 * to a parent branch if the current branch does not contain the topic.
 	 * 
 	 * @param topicId
 	 *            The topic to find
 	 * @param referenceTopic
 	 *            The topic to use a search reference point
 	 * @return the xref postfix of the toc that is applied to the topic
 	 */
 	public String getClosestTopicXrefPostfix(final Integer topicId, final TopicV1 referenceTopic)
 	{
 		TocFormatBranch branch = this.getBranchThatContainsTopic(referenceTopic);
 
 		while (branch != null)
 		{
 			/* Search the reference branch */
 			final Pair<TopicV1, TocFormatBranch> topicInBranch = branch.getTopicInBranchAndChildren(topicId);
 			if (topicInBranch != null)
 				return topicInBranch.getSecond().getTOCBranchID();
 
 			/* go up to the parent and try again */
 			branch = branch.getParent();
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param topic
 	 *            The topic to find
 	 * @return The TOC Branch the contains the specific TopicV1 object
 	 */
 	public TocFormatBranch getBranchThatContainsTopic(final TopicV1 topic)
 	{
 		if (this.topics.containsKey(topic))
 			return this;
 
 		for (final TocFormatBranch child : children)
 		{
 			final TocFormatBranch branch = child.getBranchThatContainsTopic(topic);
 			if (branch != null)
 				return branch;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Searches this branch and its children for a topic with the supplied ID.
 	 * 
 	 * @param topicId
 	 *            The ID of the topic to locate in the TOC
 	 * @return A mapping of the TopicV1 object and the TOC branch it was found
 	 *         in, or null if the topic was not found
 	 */
 	public Pair<TopicV1, TocFormatBranch> getTopicInBranchAndChildren(final Integer topicId)
 	{
 		for (final TopicV1 topic : this.topics.keySet())
 			if (topicId.equals(topic.getId()))
 				return new Pair<TopicV1, TocFormatBranch>(topic, this);
 
 		for (final TocFormatBranch child : children)
 		{
 			final Pair<TopicV1, TocFormatBranch> retValue = child.getTopicInBranchAndChildren(topicId);
 			if (retValue != null)
 				return retValue;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Appends the contents of the topics held by this level of the toc to files
 	 * that will appear in the final ZIP file
 	 * 
 	 * @param files
 	 *            The mapping of file name to file data, which will become a ZIP
 	 *            file
 	 * @param useFixedUrls
 	 *            True if the topics should be saved in files named after their
 	 *            fixed url property tags
 	 */
 	public void addTopicsToZIPFile(final HashMap<String, byte[]> files, final boolean useFixedUrls)
 	{
 		for (final TopicV1 topic : this.topics.keySet())
 		{
 			if (useFixedUrls)
 			{
 				files.put("Book/en-US/" + topic.getXrefPropertyOrId(CommonConstants.FIXED_URL_PROP_TAG_ID) + this.getTOCBranchID() + ".xml", XMLUtilities.convertDocumentToString(this.topics.get(topic), "UTF-8", DocbookBuilderConstants.DOCBOOK_XML_PREAMBLE).getBytes());
 			}
 		}
 
 		for (final TocFormatBranch child : children)
 			child.addTopicsToZIPFile(files, useFixedUrls);
 	}
 
 	/**
 	 * @param topic
 	 *            The topic that the XML Document is associated with
 	 * @return the XML Document object that relates to a topic
 	 */
 	public Document getXMLDocument(final TopicV1 topic)
 	{
 		if (this.topics.containsKey(topic))
 			return this.topics.get(topic);
 
 		for (final TocFormatBranch child : children)
 		{
 			final Document doc = child.getXMLDocument(topic);
 			if (doc != null)
 				return doc;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Sets the XML Document object that relates to a topic, recursively calling
 	 * the same method on any children if the topic is not held by this level of
 	 * the toc.
 	 * 
 	 * @param topic
 	 *            The topic that the XML Document is associated with
 	 * @param doc
 	 *            The XML Document
 	 * @return true if the XML document has been assoicated with the topic
 	 */
 	public boolean setXMLDocument(final TopicV1 topic, final Document doc)
 	{
 		if (this.topics.containsKey(topic))
 		{
 			this.topics.put(topic, doc);
 			return true;
 		}
 
 		for (final TocFormatBranch child : children)
 		{
 			if (child.setXMLDocument(topic, doc))
 				return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Because each topic can appear several times in the final book, each id
 	 * attribute needs to be modified to make it unique.
 	 * 
 	 * This method scans through the nodes in the XML documents held by this
 	 * level of the TOC, and then recursivly calls the funtion for each child.
 	 */
 	public void setUniqueIds()
 	{
 		for (final Document doc : this.topics.values())
 			fixNodeId(doc);
 
 		for (final TocFormatBranch child : children)
 			child.setUniqueIds();
 	}
 
 	/**
 	 * This method is called by the setUniqueIds() method, and is used to
 	 * identify any attributes called "id", and update their ids to a unique
 	 * value.
 	 * 
 	 * @param node
 	 *            The XML node to process
 	 */
 	private void fixNodeId(final Node node)
 	{
 		final NamedNodeMap attributes = node.getAttributes();
 		if (attributes != null)
 		{
 			final Node idAttribute = attributes.getNamedItem("id");
 			if (idAttribute != null)
 			{
 				final String idAttibuteValue = idAttribute.getNodeValue();
 				final String fixedIdAttribute = idAttibuteValue + this.getTOCBranchID();
 				idAttribute.setNodeValue(fixedIdAttribute);
 
 				fixNodeIdReferences(node.getOwnerDocument(), idAttibuteValue, fixedIdAttribute);
 			}
 		}
 
 		final NodeList elements = node.getChildNodes();
 		for (int i = 0; i < elements.getLength(); ++i)
 			fixNodeId(elements.item(i));
 	}
 
 	/**
 	 * ID attributes modified in the fixNodeId() method may have been referenced
 	 * locally in the XML. When an ID is updated, and attribute that referenced
 	 * that ID is also updated.
 	 * 
 	 * @param node
 	 *            The node to check for attributes
 	 * @param id
 	 *            The old ID attribute value
 	 * @param fixedId
 	 *            The new ID attribute
 	 */
 	private void fixNodeIdReferences(final Node node, final String id, final String fixedId)
 	{
 		final NamedNodeMap attributes = node.getAttributes();
 		if (attributes != null)
 		{
 			for (int i = 0; i < attributes.getLength(); ++i)
 			{
 				final String attibuteValue = attributes.item(i).getNodeValue();
 				if (attibuteValue.equals(id))
 				{
 					attributes.item(i).setNodeValue(fixedId);
 				}
 			}
 		}
 
 		final NodeList elements = node.getChildNodes();
 		for (int i = 0; i < elements.getLength(); ++i)
 			fixNodeIdReferences(elements.item(i), id, fixedId);
 	}
 }
