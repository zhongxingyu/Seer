 package com.redhat.contentspec;
 
 /**
  * A class that is used to hold the data for a Content Specification. It holds a basic data(Title, Product, Version, etc...) and
  * the original pre processed text of the Content Specification and all of the levels (Appendixes, Chapters, Sections, etc...) inside the Content Spec.
  */
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.redhat.contentspec.constants.CSConstants;
 import com.redhat.contentspec.entities.BugzillaOptions;
 import com.redhat.contentspec.entities.InjectionOptions;
 import com.redhat.contentspec.enums.LevelType;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.HashUtilities;
 import com.redhat.ecs.commonutils.StringUtilities;
 
 public class ContentSpec {
 	
 	private int id = 0;
 	private String title = "";
 	private String product = "";
 	private String version = "";
 	private String brand = null;
 	private String subtitle = null;
 	private String edition = null;
 	private Integer pubsNumber = null;
 	private String publicanCfg = null;
 	private ArrayList<String> text = new ArrayList<String>();
 	private String dtd = "Docbook 4.5";
 	private final Level level = new Level("Initial Level", 0, null, LevelType.BASE);;
 	private String createdBy = null;
 	private Integer revision = null;
 	private String checksum = null;
 	private String copyrightHolder = "";
 	private String description = null;
 	private InjectionOptions injectionOptions = null;
 	private String bugzillaProduct = null;
 	private String bugzillaComponent = null;
 	private String bugzillaVersion = null;
 	private boolean injectBugLinks = true;
 	private boolean injectSurveyLinks = false;
 	private String locale = null;
 	private String outputStyle = CSConstants.CSP_OUTOUT_FORMAT;
 	private Boolean allowDuplicateTopics = true; 
 	private Boolean allowEmptyLevels = false; 
 	
 	private LinkedList<Comment> baseComments = new LinkedList<Comment>();
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param title The Title of the Content Specification.
 	 * @param product The Product that the Content Specification documents.
 	 * @param version The Version of the Product that the Content Specification documents.
 	 * @param copyrightHolder The Copyright Holder of the Content Specification and the book it creates.
 	 */
 	public ContentSpec(String title, String product, String version, String copyrightHolder) {
 		this.title = title;
 		this.product = product;
 		this.version = version;
 		this.copyrightHolder = copyrightHolder;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param title The title of the Content Specification.
 	 */
 	public ContentSpec(String title) {
 		this.title = title;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param id The Database ID of the Content Specification.
 	 * @param title The title of the Content Specification.
 	 */
 	public ContentSpec(int id, String title) {
 		this.id = id;
 		this.title = title;
 	}
 	
 	public ContentSpec() {
 	}
 	
 	// Start of the basic getter/setter methods for this Scope.
 	
 	/**
 	 * Get the base Level of the Content Specification. This level will contain all the other levels in the content specification.
 	 * 
 	 * @return The Base Level object of a Content Specification.
 	 */
 	public Level getBaseLevel() {
 		return level;
 	}
 	
 	/**
 	 * Gets the Product that the Content Specification documents.
 	 * 
 	 * @return The name of the product.
 	 */
 	public String getProduct() {
 		return product;
 	}
 	
 	/**
 	 * Sets the name of the product that the Content Specification documents.
 	 * 
 	 * @param product The name of the Product.
 	 */
 	public void setProduct(String product) {
 		this.product = product;
 	}
 	
 	/**
 	 * Get the version of the product that the Content Specification documents.
 	 * 
 	 * @return The version of the product.
 	 */
 	public String getVersion() {
 		return version;
 	}
 	
 	/**
 	 * Set the version of the product that the Content Specification documents.
 	 * 
 	 * @param version The product version.
 	 */
 	public void setVersion(String version) {
 		this.version = version;
 	}
 	
 	/**
 	 * Gets the brand of the product that the Content Specification documents.
 	 * 
 	 * @return The brand of the product or null if one doesn't exist.
 	 */
 	public String getBrand() {
 		return brand;
 	}
 	
 	/**
 	 * Set the brand of the product that the Content Specification documents.
 	 * 
 	 * @param brand The brand of the product. 
 	 */
 	public void setBrand(String brand) {
 		this.brand = brand;
 	}
 	
 	/**
 	 * Sets the ID of the Content Specification.
 	 * 
 	 * @param id The database ID of the content specification.
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Gets the ID of the Content Specification
 	 * 
 	 * @return The Content Specification database ID or 0 if one hasn't been set. 
 	 */
 	public int getId() {
 		return id;
 	}
 	
 	/**
 	 * Gets the title of the Content Specification.
 	 * 
 	 * @return The Content Specification title.
 	 */
 	public String getTitle() {
 		return title;
 	}
 	
 	/**
 	 * Gets the escaped version of the title for the Content Specification.
 	 * 
 	 * @return The Content Specification title.
 	 */
 	public String getEscapedTitle() {
 		return DocBookUtilities.escapeTitle(title);
 	}
 	
 	/**
 	 * Sets the Content Specifications title.
 	 * 
 	 * @param title The title for the content specification
 	 */
 	public void setTitle(String title) {
 		this.title= title;
 	}
 	
 	/**
 	 * Gets the Subtitle for the Content Specification.
 	 * 
 	 * @return The Subtitle of the Content Specification or null if one doesn't exist.
 	 */
 	public String getSubtitle() {
 		return subtitle;
 	}
 
 	/**
 	 * Sets the Subtitle for the Content Specification
 	 * 
 	 * @param subtitle
 	 */
 	public void setSubtitle(String subtitle) {
 		this.subtitle = subtitle;
 	}
 
 	/**
 	 * Gets the Edition of the book the Content Specification will create.
 	 * 
 	 * @return The Content Specifications Book Edition or null if one doesn't exist.
 	 */
 	public String getEdition() {
 		return edition;
 	}
 
 	/**
 	 * Set the Edition of the Book the Content Specification represents.
 	 * 
 	 * @param edition The Book Edition.
 	 */
 	public void setEdition(String edition) {
 		this.edition = edition;
 	}
 
 	/**
 	 * Get the publication number used by publican
 	 * 
 	 * @return The publication number or null if one doesn't exist.
 	 */
 	public Integer getPubsNumber() {
 		return pubsNumber;
 	}
 
 	/**
 	 * Set the publication number for the Content Specification.
 	 * 
 	 * @param pubsNumber The publication number.
 	 */
 	public void setPubsNumber(Integer pubsNumber) {
 		this.pubsNumber = pubsNumber;
 	}
 	
 	/**
 	 * Gets the data what will be appended to the publican.cfg file when built.
 	 * 
 	 * @return The data to be appended or null if none exist.
 	 */
 	public String getPublicanCfg() {
 		return publicanCfg;
 	}
 
 	/**
 	 * Set the data that will be appended to the publican.cfg file when built.
 	 * 
 	 * @param publicanCfg The data to be appended.
 	 */
 	public void setPublicanCfg(String publicanCfg) {
 		this.publicanCfg = publicanCfg;
 	}
 	
 	/**
 	 * Get the pre processed text of a specific line in the Content Specification.
 	 * 
 	 * @param line The line number of the text.
 	 * @return The text at "line" in the Content Specification or null if the line doesn't exist.
 	 */
 	public String getPreProcessedTextForLine(int line) {
 		return text.size() >= line ? text.get(line - 1) : null;
 	}
 	
 	/**
 	 * Gets a list of all the pre processed lines in the Content Specification.
 	 * 
 	 * @return A List of pre processed Content Specification lines.
 	 */
 	public List<String> getPreProcessedText() {
 		return text;
 	}
 	
 	/**
 	 * Sets the pre processed text of a content specification for a specific line. If the line doesn't
 	 * exist then a IndexOutOfBoundsException will be thrown.
 	 * 
 	 * @param text The text to be set at "line".
 	 * @param line The line number in the content specification to set the text for.
 	 */
 	public void setPreProcessedTextForLine (String text, int line) {
 		this.text.set(line-1, text);
 	}
 	
 	/**
 	 * Get the DTD of the Content Specification. The default value is "Docbook 4.5".
 	 * 
 	 * @return The DTD of the Content Specification or the default value if one isn't set.
 	 */
 	public String getDtd() {
 		return dtd;
 	}
 	
 	/**
 	 * Sets the DTD for a Content Specification.
 	 * 
 	 * @param dtd The DTD of the Content Specification.
 	 */
 	public void setDtd (String dtd) {
 		this.dtd = dtd;
 	}
 	
 	/**
 	 * Sets the created by Username for the author who created/uploaded the Content Specification.
 	 * 
 	 * @param username The Username of the User who created/uploaded the Content Specification or null if one doesn't exist. 
 	 */
 	public void setCreatedBy(String username) {
 		this.createdBy = username;
 	}
 	
 	/**
 	 * Get the Username of the user who created/uploaded the Content Specification.
 	 * 
 	 * @return The Username of the Content Specification creator.
 	 */
 	public String getCreatedBy() {
 		return createdBy;
 	}
 	
 	/**
 	 * Gets the SpecRevision number for the Content Specification. This number is used to validate 
 	 * that the Content Specification hasn't been modified since the last upload.
 	 * 
 	 * @return The SpecRevision number or null if one doesn't exist.
 	 */
 	@Deprecated
 	public Integer getRevision() {
 		return revision;
 	}
 
 	/**
 	 * Sets the SpecRevision number for a ContentSpecification.
 	 * 
 	 * @param revision The SpecRevision number.
 	 */
 	@Deprecated
 	public void setRevision(int revision) {
 		this.revision = revision;
 	}
 	
 	/**
 	 * Get the set Checksum value for a Content Specification.
 	 * 
 	 * Note: This function doesn't calculate the Checksum of a Content Specification.
 	 * 
 	 * @return The set value of the Content Specifications Checksum or null if one doesn't exist.
 	 */
 	public String getChecksum() {
 		return checksum;
 	}
 
 	/**
 	 * Set the Checksum value for a Content Specification.
 	 * 
 	 * Note: This value isn't used by the toString() function as it re calculates the Checksum.
 	 * 
 	 * @param checksum The Checksum of the Content Specification.
 	 */
 	public void setChecksum(String checksum) {
 		this.checksum = checksum;
 	}
 
 	/**
 	 * Gets the Abstract (or Description) for a Content Specification that will be added to a book when built.
 	 * 
 	 * @return The abstract for the Content Specification or null if one doesn't exist.
 	 */
 	public String getAbstract() {
 		return description;
 	}
 
 	/**
 	 * Sets the Abstract (or Description) for a Content Specification.
 	 * 
 	 * @param description The Abstract for the Content Specifications book.
 	 */
 	public void setAbstract(String description) {
 		this.description = description;
 	}
 	
 	/**
 	 * Get the Copyright Holder of the Content Specification and the book it creates.
 	 * 
 	 * @return The name of the Copyright Holder.
 	 */
 	public String getCopyrightHolder() {
 		return copyrightHolder;
 	}
 
 	/**
 	 * Set the Copyright Holder of the Content Specification and the book it creates.
 	 * 
 	 * @param copyrightHolder The name of the Copyright Holder.
 	 */
 	public void setCopyrightHolder(String copyrightHolder) {
 		this.copyrightHolder = copyrightHolder;
 	}
 	
 	/**
 	 * Gets the injection Options for the Content Specification that will be used when building a book.
 	 * 
 	 * @return A InjectionOptions object containing the Injection Options to be used when building.
 	 */
 	public InjectionOptions getInjectionOptions() {
 		return injectionOptions;
 	}
 
 	/**
 	 * Sets the InjectionOptions that will be used by the Builder when building a book.
 	 * 
 	 * @param injectionOptions The InjectionOptions to be used when building a book.
 	 */
 	public void setInjectionOptions(InjectionOptions injectionOptions) {
 		this.injectionOptions = injectionOptions;
 	}
 	
 	/**
 	 * Sets the description for the global tags.
 	 * 
 	 * @param desc The description.
 	 */
 	public void setDescription(String desc) {
 		level.setDescription(desc);
 	}
 	
 	/**
 	 * Get the description that will be applied globally.
 	 * 
 	 * @return The description as a String
 	 */
 	public String getDescription() {
 		return level.getDescription(false);
 	}
 	
 	/**
 	 * Gets the locale of the Content Specification.
 	 * 
 	 * @return The Content Specification locale.
 	 */
 	public String getLocale() {
 		return locale;
 	}
 	
 	/**
 	 * Sets the Content Specifications locale.
 	 * 
 	 * @param locale The locale for the content specification
 	 */
 	public void setLocale(String locale) {
 		this.locale= locale;
 	}
 	
 	/**
 	 * Gets the output style for the Content Specification. The default is CSP.
 	 * 
 	 * @return The Content Specification output style.
 	 */
 	public String getOutputStyle() {
 		return outputStyle;
 	}
 	
 	/**
 	 * Sets the Content Specifications output style. 
 	 * 
 	 * @param outputStyle The output style for the content specification
 	 */
 	public void setOutputStyle(String outputStyle) {
 		this.outputStyle= outputStyle;
 	}
 	
 	/**
 	 * Sets the Assigned Writer for the global tags.
 	 * 
 	 * @param writer The writers name that matches to the assigned writer tag in the database
 	 */
 	public void setAssignedWriter(String writer) {
 		level.setAssignedWriter(writer);
 	}
 	
 	/**
 	 * Gets the Assigned Writer that will be applied globally.
 	 * 
 	 * @return The Assigned Writers name as a String
 	 */
 	public String getAssignedWriter() {
 		return level.getAssignedWriter(false);
 	}
 	
 	/**
 	 * Sets the set of tags for the global tags
 	 * 
 	 * @param tags A List of tags by their name.
 	 */
 	public void setTags(List<String> tags) {
 		level.setTags(tags);
 	}
 	
 	/**
 	 * Gets the set of tags for the global tags.
 	 * 
 	 * @return A list of tag names
 	 */
 	public List<String> getTags() {
 		return level.getTags(false);
 	}
 	
 	/**
 	 * Sets the list of tags that are to be removed in the global options.
 	 * 
 	 * @param tags An ArrayList of tags to be removed
 	 */
 	public void setRemoveTags(List<String> tags) {
 		level.setRemoveTags(tags);
 	}
 	
 	/**
 	 * Gets an ArrayList of tags that are to be removed globally.
 	 * 
 	 * @return An ArrayList of tags
 	 */
 	public List<String> getRemoveTags() {
 		return level.getRemoveTags(false);
 	}
 	
 	/**
 	 * Sets the list of source urls to be applied globally
 	 * 
 	 * @param sourceUrls A List of urls
 	 */
 	public void setSourceUrls(List<String> sourceUrls) {
 		level.setSourceUrls(sourceUrls);
 	}
 	
 	/**
 	 * Get the Source Urls that are to be applied globally.
 	 * 
 	 * @return A List of Strings that represent the source urls
 	 */
 	public List<String> getSourceUrls() {
 		return level.getSourceUrls();
 	}
 	
 	public Boolean getAllowDuplicateTopics() {
 		return allowDuplicateTopics;
 	}
 
 	public void setAllowDuplicateTopics(Boolean allowDuplicateTopics) {
 		this.allowDuplicateTopics = allowDuplicateTopics;
 	}
 	
 	public Boolean getAllowEmptyLevels() {
 		return allowEmptyLevels;
 	}
 
 	public void setAllowEmptyLevels(Boolean allowEmptyLevels) {
 		this.allowEmptyLevels = allowEmptyLevels;
 	}
 
 	/**
 	 * Adds a tag to the global list of tags. If the tag starts with a - then its added to the remove tag list otherwise its added
 	 * to the normal tag mapping. Also strips off + & - from the start of tags.
 	 * 
 	 * @param tagName The name of the Tag to be added.
 	 * @return True if the tag was added successfully otherwise false.
 	 */
 	public boolean addTag(String tagName) {
 		return level.addTag(tagName);
 	}
 	
 	/**
 	 * Adds a list of tags to the global list of tags
 	 * 
 	 * @param tagArray A list of tags by name that are to be added.
 	 * @return True if all the tags were added successfully otherwise false.
 	 */
 	public boolean addTags(List<String> tagArray) {
 		return level.addTags(tagArray);
 	}
 	
 	/**
 	 * Adds a source URL to the list of global URL's
 	 * 
 	 * @param url The URL to be added
 	 */
 	public void addSourceUrl(String url) {
 		level.addSourceUrl(url);
 	}
 	
 	/**
 	 * Removes a specific Source URL from the list of global URL's
 	 * 
 	 * @param url The URL to be removed.
 	 */
 	public void removeSourceUrl(String url) {
 		level.removeSourceUrl(url);
 	}
 	
 	/**
 	 * Adds a Chapter to the Content Specification. If the Chapter already has a parent, then it is removed from that parent
 	 * and added to this level.
 	 * 
 	 * @param chapter A Chapter to be added to the Content Specification.
 	 */
 	public void appendChapter(Chapter chapter) {
 		level.appendChild(chapter);
 	}
 	
 	/**
 	 * Removes a Chapter from the Content Specification and removes the Content Specification as the Chapters parent.
 	 * 
 	 * @param chapter The Chapter to be removed from the Content Specification.
 	 */
 	public void removeChapter(Chapter chapter) {
 		level.appendChild(chapter);
 	}
 	
 	/**
 	 * Gets a ordered linked list of the child nodes within the Content Specification. This includes comments and chapters.
 	 * 
 	 * @return The ordered list of nodes for the Content Specification.
 	 */
 	public LinkedList<Node> getChildNodes() {
 		return level.getChildNodes();
 	}
 	
 	/**
 	 * Gets the number of Chapters in the Content Specification.
 	 * 
 	 * @return The number of Child Levels
 	 */
 	public int getNumberOfChapters() {
 		return level.getNumberOfChildLevels();
 	}
 	
 	/**
 	 * Gets a List of all the chapters in this level.
 	 * 
 	 * Note: The Chapters may not be in order.
 	 * 
 	 * @return A List of Chapters.
 	 */
 	public List<Level> getChapters() {
 		return level.getChildLevels();
 	}
 	
 	/**
 	 * Appends a Comment to the Content Specification.
 	 * 
 	 * @param comment The comment node to be appended to the Content Specification.
 	 */
 	public void appendComment(Comment comment) {
 		level.appendComment(comment);
 	}
 	
 	/**
 	 * Creates and appends a Comment node to the Content Specification.
 	 * 
 	 * @param comment The Comment to be appended.
 	 */
 	public void appendComment(String comment) {
 		level.appendComment(new Comment(comment));
 	}
 
 	/**
 	 * Removes a Comment from the Content Specification.
 	 * 
 	 * @param comment The Comment node to be removed.
 	 */
 	public void removeComment(Comment comment) {
 		level.removeComment(comment);
 	}
 	
 	/**
 	 * Adds a Comment to the top of the Content Specification before the Initial Content Specification data.
 	 * 
 	 * @param comment The Comment node to be added.
 	 */
 	public void appendInitialComment(Comment comment) {
 		baseComments.add(comment);
 	}
 	
 	/**
 	 * Creates and adds a Comment to the top of the Content Specification before the Initial Content Specification data.
 	 * 
 	 * @param comment The Comment to be added.
 	 */
 	public void appendInitialComment(String comment) {
 		baseComments.add(new Comment(comment));
 	}
 	
 	/**
 	 * Removes a Comment from the top of a Content Specification.
 	 * 
 	 * @param comment The Comment node to be removed.
 	 */
 	public void removeInitialComment(Comment comment) {
 		baseComments.remove(comment);
 	}
 	
 	// End of the basic getter/setter methods for this ContentSpec.
 
 	/**
 	 * Appends a pre processed line to the list of lines in the Content Specification.
 	 * 
 	 * @param line The Line to be added.
 	 */
 	public void appendPreProcessedLine(String line) {
 		this.text.add(line);
 	}
 
 	/**
 	 * Appends a String to a pre processed line that has already been added to the Content Specification.
 	 * Throws a IndexOutOfBoundsException if the line doesn't exist.
 	 * 
 	 * @param text The text to be appended.
 	 * @param line The line number of the original text that the new text should be appended to.
 	 */
 	public void appendPreProcessedLineText (String text, int line) {
 		String temp = this.text.get(line-1) + text;
 		this.text.set(line-1, temp);
 	}
 	
 	public List<SpecTopic> getSpecTopics() {
 		return getLevelSpecTopics(level);
 	}
 	
 	private List<SpecTopic> getLevelSpecTopics(Level level) {
 		List<SpecTopic> specTopics = level.getSpecTopics();
 		for (Level childLevel: level.getChildLevels()) {
 			specTopics.addAll(getLevelSpecTopics(childLevel));
 		}
 		return specTopics;
 	}
 
 	public String getBugzillaProduct() {
 		return bugzillaProduct;
 	}
 
 	public void setBugzillaProduct(String bugzillaProduct) {
 		this.bugzillaProduct = bugzillaProduct;
 	}
 
 	public String getBugzillaComponent() {
 		return bugzillaComponent;
 	}
 
 	public void setBugzillaComponent(String bugzillaComponent) {
 		this.bugzillaComponent = bugzillaComponent;
 	}
 
 	public String getBugzillaVersion() {
 		return bugzillaVersion;
 	}
 
 	public void setBugzillaVersion(String bugzillaVersion) {
 		this.bugzillaVersion = bugzillaVersion;
 	}
 	
 	public boolean isInjectBugLinks() {
 		return injectBugLinks;
 	}
 
 	public void setInjectBugLinks(boolean injectBugLinks) {
 		this.injectBugLinks = injectBugLinks;
 	}
 
 	public boolean isInjectSurveyLinks() {
 		return injectSurveyLinks;
 	}
 
 	public void setInjectSurveyLinks(boolean injectSurveyLinks) {
 		this.injectSurveyLinks = injectSurveyLinks;
 	}
 
 	public BugzillaOptions getBugzillaOptions() {
 		final BugzillaOptions bzOption = new BugzillaOptions();
 		bzOption.setProduct(bugzillaProduct);
 		bzOption.setComponent(bugzillaComponent);
 		bzOption.setVersion(bugzillaVersion);
 		bzOption.setBugzillaLinksEnabled(injectBugLinks);
 		return bzOption;
 	}
 
 	/**
 	 * Returns a String representation of the Content Specification. 
 	 */
 	@Override
 	public String toString() {
 		String output = "";
 		for (Comment baseComment: baseComments) {
 			output += baseComment.toString();
 		}
		String bugzillaDetails = "Bug Links = " + (injectBugLinks ? "On" : "Off") + "\n";
 		bugzillaDetails += (bugzillaProduct == null ? "" : ("BZPRODUCT = " + bugzillaProduct + "\n")) +
 				(bugzillaComponent == null ? "" : ("BZCOMPONENT = " + bugzillaComponent + "\n")) +
 				(bugzillaVersion == null ? "" : ("BZVERSION = " + bugzillaVersion + "\n"));
 		
 		String allowDetails = (allowDuplicateTopics == null || allowDuplicateTopics ? "" : ("Duplicate Topics = Off\n")) +
 				(allowEmptyLevels == null  || !allowEmptyLevels ? "" : ("Allow Empty Levels = " + allowEmptyLevels.toString() + "\n"));
 		
 		output += "Title = " + (title == null ? "" : title) + "\n" + 
 				"Product = " + (product == null ? "" : product) + "\n" + 
 				"Version = " + (version == null ? "" : version) + "\n" + 
 				"Copyright Holder = " + (copyrightHolder == null ? "" : copyrightHolder) + "\n" +
 				"DTD = " + (dtd == null ? "" : dtd) + "\n" +
 				(subtitle == null ? "" : ("Subtitle = " + subtitle + "\n")) +
 				(edition == null ? "" : ("Edition = " + edition + "\n")) +
 				(brand == null ? "" : ("Brand = " + brand + "\n")) +
 				(pubsNumber == null ? "" : ("Pubsnumber = " + pubsNumber + "\n")) +
 				(description == null ? "" : ("Abstract = " + description + "\n")) +
 				(allowDetails.isEmpty() ? "" : allowDetails) +
 				(outputStyle == null ? "" : ("Output Style = " + outputStyle + "\n")) +
 				(locale == null ? "" : ("Translation Locale = " + locale + "\n")) +
 				"\n" + bugzillaDetails +
 				(injectSurveyLinks ? "Survey Links = On\n" : "") + 
 				(publicanCfg == null ? "" : ("\npublican.cfg = [" + publicanCfg + "]\n"));
 		
 		// Add the injection options if they exist
 		if (injectionOptions != null && injectionOptions.getStrictTopicTypes() != null 
 				&& !injectionOptions.getStrictTopicTypes().isEmpty() && injectionOptions.getContentSpecType() != null) {
 			output += "Inline Injection = ";
 			if (injectionOptions.getContentSpecType() == InjectionOptions.UserType.STRICT) {
 				output += "on [" + StringUtilities.buildString(injectionOptions.getStrictTopicTypes().toArray(new String[0]), ", ") + "]";
 			} else if (injectionOptions.getContentSpecType() == InjectionOptions.UserType.ON) {
 				output += "on";
 			} else if (injectionOptions.getContentSpecType() == InjectionOptions.UserType.OFF) {
 				output += "off";
 			}
 			output += "\n";
 		}
 		
 		// Append a new line to separate the metadata from content
 		output += "\n";
 		
 		// Add any global options
 		String options = level.getOptionsString();
 		if (!options.equals("")) {
 			output += "[" + options + "]\n";
 		}
 		
 		// Append the String representation of each level
 		output +=level.toString();
 		
 		// If the id isn't null then add the id and checksum
 		if (id != 0) {
 			output = "CHECKSUM=" + HashUtilities.generateMD5("ID = " + id + "\n" + output) + "\n" + "ID = " + id + "\n" + output;
 		}
 		return output;
 	}
 }
