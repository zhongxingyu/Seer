 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 package org.sociotech.communitymashup.data.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
 import org.eclipse.emf.query.conditions.eobjects.EObjectTypeRelationCondition;
 import org.eclipse.emf.query.ocl.conditions.BooleanOCLCondition;
 import org.eclipse.emf.query.statements.IQueryResult;
 import org.eclipse.ocl.ParserException;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.data.Attachment;
 import org.sociotech.communitymashup.data.Binary;
 import org.sociotech.communitymashup.data.Category;
 import org.sociotech.communitymashup.data.Classification;
 import org.sociotech.communitymashup.data.Connection;
 import org.sociotech.communitymashup.data.Content;
 import org.sociotech.communitymashup.data.DataFactory;
 import org.sociotech.communitymashup.data.DataPackage;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.DeletedItem;
 import org.sociotech.communitymashup.data.Document;
 import org.sociotech.communitymashup.data.Email;
 import org.sociotech.communitymashup.data.Event;
 import org.sociotech.communitymashup.data.Extension;
 import org.sociotech.communitymashup.data.Identifier;
 import org.sociotech.communitymashup.data.Image;
 import org.sociotech.communitymashup.data.IndoorLocation;
 import org.sociotech.communitymashup.data.InformationObject;
 import org.sociotech.communitymashup.data.InstantMessenger;
 import org.sociotech.communitymashup.data.Item;
 import org.sociotech.communitymashup.data.Location;
 import org.sociotech.communitymashup.data.MetaInformation;
 import org.sociotech.communitymashup.data.MetaTag;
 import org.sociotech.communitymashup.data.Organisation;
 import org.sociotech.communitymashup.data.Person;
 import org.sociotech.communitymashup.data.Phone;
 import org.sociotech.communitymashup.data.Ranking;
 import org.sociotech.communitymashup.data.StarRanking;
 import org.sociotech.communitymashup.data.Tag;
 import org.sociotech.communitymashup.data.ThumbRanking;
 import org.sociotech.communitymashup.data.Transformation;
 import org.sociotech.communitymashup.data.Video;
 import org.sociotech.communitymashup.data.ViewRanking;
 import org.sociotech.communitymashup.data.WebAccount;
 import org.sociotech.communitymashup.data.WebSite;
 import org.sociotech.communitymashup.rest.ArgNotFoundException;
 import org.sociotech.communitymashup.rest.RequestType;
 import org.sociotech.communitymashup.rest.RestCommand;
 import org.sociotech.communitymashup.rest.RestUtil;
 import org.sociotech.communitymashup.rest.UnknownOperationException;
 import org.sociotech.communitymashup.rest.WrongArgCountException;
 import org.sociotech.communitymashup.rest.WrongArgException;
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Content</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getContents <em>Contents</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getContributors <em>Contributors</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getAuthor <em>Author</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getDocuments <em>Documents</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getParentContent <em>Parent Content</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getLocale <em>Locale</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getTransformations <em>Transformations</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.ContentImpl#getVideos <em>Videos</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ContentImpl extends InformationObjectImpl implements Content {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\nContributors:\n \tPeter Lachenmaier - Design and initial implementation";
 
 	/**
 	 * Singleton instance for <b>is main content</b> condition.
 	 */
 	private static EObjectCondition isMainContentCondition = null;
 
 	public static final String COMMENT_META_TAG_VALUE = "comment";
 	
 	/**
 	 * The cached value of the '{@link #getContents() <em>Contents</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContents()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Content> contents;
 
 	/**
 	 * The cached value of the '{@link #getContributors() <em>Contributors</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContributors()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Person> contributors;
 
 	/**
 	 * The cached value of the '{@link #getAuthor() <em>Author</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthor()
 	 * @generated
 	 * @ordered
 	 */
 	protected Person author;
 
 	/**
 	 * The cached value of the '{@link #getDocuments() <em>Documents</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDocuments()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Document> documents;
 
 	/**
 	 * The cached value of the '{@link #getParentContent() <em>Parent Content</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParentContent()
 	 * @generated
 	 * @ordered
 	 */
 	protected Content parentContent;
 
 	/**
 	 * The default value of the '{@link #getLocale() <em>Locale</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLocale()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LOCALE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLocale() <em>Locale</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLocale()
 	 * @generated
 	 * @ordered
 	 */
 	protected String locale = LOCALE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getTransformations() <em>Transformations</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTransformations()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Transformation> transformations;
 
 	/**
 	 * The cached value of the '{@link #getVideos() <em>Videos</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVideos()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Video> videos;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ContentImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DataPackage.Literals.CONTENT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Person getAuthor() {
 		if (author != null && author.eIsProxy()) {
 			InternalEObject oldAuthor = (InternalEObject)author;
 			author = (Person)eResolveProxy(oldAuthor);
 			if (author != oldAuthor) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DataPackage.CONTENT__AUTHOR, oldAuthor, author));
 			}
 		}
 		return author;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Person basicGetAuthor() {
 		return author;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAuthor(Person newAuthor, NotificationChain msgs) {
 		Person oldAuthor = author;
 		author = newAuthor;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DataPackage.CONTENT__AUTHOR, oldAuthor, newAuthor);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAuthor(Person newAuthor) {
 		if (newAuthor != author) {
 			NotificationChain msgs = null;
 			if (author != null)
 				msgs = ((InternalEObject)author).eInverseRemove(this, DataPackage.PERSON__AUTHORED, Person.class, msgs);
 			if (newAuthor != null)
 				msgs = ((InternalEObject)newAuthor).eInverseAdd(this, DataPackage.PERSON__AUTHORED, Person.class, msgs);
 			msgs = basicSetAuthor(newAuthor, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.CONTENT__AUTHOR, newAuthor, newAuthor));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Document> getDocuments() {
 		if (documents == null) {
 			documents = new EObjectResolvingEList<Document>(Document.class, this, DataPackage.CONTENT__DOCUMENTS);
 		}
 		return documents;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Content getParentContent() {
 		if (parentContent != null && parentContent.eIsProxy()) {
 			InternalEObject oldParentContent = (InternalEObject)parentContent;
 			parentContent = (Content)eResolveProxy(oldParentContent);
 			if (parentContent != oldParentContent) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DataPackage.CONTENT__PARENT_CONTENT, oldParentContent, parentContent));
 			}
 		}
 		return parentContent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Content basicGetParentContent() {
 		return parentContent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetParentContent(Content newParentContent, NotificationChain msgs) {
 		Content oldParentContent = parentContent;
 		parentContent = newParentContent;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DataPackage.CONTENT__PARENT_CONTENT, oldParentContent, newParentContent);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParentContent(Content newParentContent) {
 		if (newParentContent != parentContent) {
 			NotificationChain msgs = null;
 			if (parentContent != null)
 				msgs = ((InternalEObject)parentContent).eInverseRemove(this, DataPackage.CONTENT__CONTENTS, Content.class, msgs);
 			if (newParentContent != null)
 				msgs = ((InternalEObject)newParentContent).eInverseAdd(this, DataPackage.CONTENT__CONTENTS, Content.class, msgs);
 			msgs = basicSetParentContent(newParentContent, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.CONTENT__PARENT_CONTENT, newParentContent, newParentContent));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLocale() {
 		return locale;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLocale(String newLocale) {
 		String oldLocale = locale;
 		locale = newLocale;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.CONTENT__LOCALE, oldLocale, locale));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Transformation> getTransformations() {
 		if (transformations == null) {
 			transformations = new EObjectWithInverseResolvingEList.ManyInverse<Transformation>(Transformation.class, this, DataPackage.CONTENT__TRANSFORMATIONS, DataPackage.TRANSFORMATION__TRANSFORMED);
 		}
 		return transformations;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Video> getVideos() {
 		if (videos == null) {
 			videos = new EObjectResolvingEList<Video>(Video.class, this, DataPackage.CONTENT__VIDEOS);
 		}
 		return videos;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Finds all the organisations of the author and the other contributers of this content using an OCL condition.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Organisation> getOrganisations() {
 		// TODO: cache
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(this.getDataSet().getItems(), this.isOrgansiationOfContentCondition());
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> organisations = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return organisations;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Creates a new EList of persons and adds all contributers and authors.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Person> getPersons() {
 		// TODO cache
 		EList<Person> persons = new BasicEList<Person>(this.getContributors());
 		
 		Person author = this.getAuthor();
 		if(author != null)
 		{
 			persons.add(author);
 		}
 		
 		return persons;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Content comment(String comment) {
 		if(comment == null || this.getDataSet() == null)
 		{
 			return null;
 		}
 		
 		DataFactory dataFactory = DataPackage.eINSTANCE.getDataFactory();
 		
 		// create new content
 		Content commentObject = dataFactory.createContent();
 		commentObject.setName("Comment on " + this.getName());
 		commentObject.setParentContent(this);
 		commentObject.setStringValue(comment);
 	
 		// add content to data set
 		this.getDataSet().add(commentObject);
 	
 		// add a meta tag for every comment
 		commentObject.metaTag(COMMENT_META_TAG_VALUE);
 	
 		return commentObject;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Person addContributor(Person contributor) {
 		if(contributor == null)
 		{
 			return null;
 		}
 		
 		// add this content object as contributed content of the person
 		contributor.addContributedContent(this);
 		
 		return contributor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Document attachDocument(String fileUrl) {
 		if(fileUrl == null || fileUrl.equals("")) {
 			return null;
 		}
 		
 		for(Document document : this.getDocuments()) {
			if(document.getOriginalFileUrl() != null && document.getOriginalFileUrl().equals(fileUrl)) {
 				// return existing document
 				return document;
 			}
 		}
 		
 		DataFactory dataFactory = DataPackage.eINSTANCE.getDataFactory();
 		
 		// create document object
 		Document document = dataFactory.createDocument();
 		
 		// add it to the data set
 		DataSet dataSet = this.getDataSet();
 		if(dataSet != null) {
 			dataSet.add(document);
 		}
 		
 		// set the url
 		document.setFileUrl(fileUrl);
 		
 		// attach it to this information object
 		this.getDocuments().add(document);
 		
 		return document;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Content commentWithMetaTagList(String comment, String metaTagList) {
 		Content commentContent = this.comment(comment);
 		if(commentContent != null && metaTagList != null) {
 			// split comma separated list
 			String[] metaTagArray = metaTagList.split(",");
 			// add all meta tags
 			for(String metaTag : metaTagArray) {
 				commentContent.metaTag(metaTag);
 			}
 		}
 		return commentContent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContents() {
 		if (contents == null) {
 			contents = new EObjectWithInverseResolvingEList<Content>(Content.class, this, DataPackage.CONTENT__CONTENTS, DataPackage.CONTENT__PARENT_CONTENT);
 		}
 		return contents;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getContributors() {
 		if (contributors == null) {
 			contributors = new EObjectWithInverseResolvingEList.ManyInverse<Person>(Person.class, this, DataPackage.CONTENT__CONTRIBUTORS, DataPackage.PERSON__CONTRIBUTED);
 		}
 		return contributors;
 	}
 
 	/* (non-Javadoc)
 	 * Adds the documents and transformations which are attached to the Content to the attachments of the information object 
 	 * 
 	 * @see org.sociotech.communitymashup.data.impl.InformationObjectImpl#getAttachements()
 	 */
 	@Override
 	public EList<Attachment> getAttachments() {
 		// create new list with the attachements of the super class
 		EList<Attachment> attachements = new BasicEList<Attachment>(super.getAttachments());
 		
 		// add documents
 		attachements.addAll(getDocuments());
 		
 		// add transformations
 		attachements.addAll(getTransformations());
 		
 		return attachements;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case DataPackage.CONTENT__CONTENTS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getContents()).basicAdd(otherEnd, msgs);
 			case DataPackage.CONTENT__CONTRIBUTORS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getContributors()).basicAdd(otherEnd, msgs);
 			case DataPackage.CONTENT__AUTHOR:
 				if (author != null)
 					msgs = ((InternalEObject)author).eInverseRemove(this, DataPackage.PERSON__AUTHORED, Person.class, msgs);
 				return basicSetAuthor((Person)otherEnd, msgs);
 			case DataPackage.CONTENT__PARENT_CONTENT:
 				if (parentContent != null)
 					msgs = ((InternalEObject)parentContent).eInverseRemove(this, DataPackage.CONTENT__CONTENTS, Content.class, msgs);
 				return basicSetParentContent((Content)otherEnd, msgs);
 			case DataPackage.CONTENT__TRANSFORMATIONS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getTransformations()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case DataPackage.CONTENT__CONTENTS:
 				return ((InternalEList<?>)getContents()).basicRemove(otherEnd, msgs);
 			case DataPackage.CONTENT__CONTRIBUTORS:
 				return ((InternalEList<?>)getContributors()).basicRemove(otherEnd, msgs);
 			case DataPackage.CONTENT__AUTHOR:
 				return basicSetAuthor(null, msgs);
 			case DataPackage.CONTENT__PARENT_CONTENT:
 				return basicSetParentContent(null, msgs);
 			case DataPackage.CONTENT__TRANSFORMATIONS:
 				return ((InternalEList<?>)getTransformations()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case DataPackage.CONTENT__CONTENTS:
 				return getContents();
 			case DataPackage.CONTENT__CONTRIBUTORS:
 				return getContributors();
 			case DataPackage.CONTENT__AUTHOR:
 				if (resolve) return getAuthor();
 				return basicGetAuthor();
 			case DataPackage.CONTENT__DOCUMENTS:
 				return getDocuments();
 			case DataPackage.CONTENT__PARENT_CONTENT:
 				if (resolve) return getParentContent();
 				return basicGetParentContent();
 			case DataPackage.CONTENT__LOCALE:
 				return getLocale();
 			case DataPackage.CONTENT__TRANSFORMATIONS:
 				return getTransformations();
 			case DataPackage.CONTENT__VIDEOS:
 				return getVideos();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case DataPackage.CONTENT__CONTENTS:
 				getContents().clear();
 				getContents().addAll((Collection<? extends Content>)newValue);
 				return;
 			case DataPackage.CONTENT__CONTRIBUTORS:
 				getContributors().clear();
 				getContributors().addAll((Collection<? extends Person>)newValue);
 				return;
 			case DataPackage.CONTENT__AUTHOR:
 				setAuthor((Person)newValue);
 				return;
 			case DataPackage.CONTENT__DOCUMENTS:
 				getDocuments().clear();
 				getDocuments().addAll((Collection<? extends Document>)newValue);
 				return;
 			case DataPackage.CONTENT__PARENT_CONTENT:
 				setParentContent((Content)newValue);
 				return;
 			case DataPackage.CONTENT__LOCALE:
 				setLocale((String)newValue);
 				return;
 			case DataPackage.CONTENT__TRANSFORMATIONS:
 				getTransformations().clear();
 				getTransformations().addAll((Collection<? extends Transformation>)newValue);
 				return;
 			case DataPackage.CONTENT__VIDEOS:
 				getVideos().clear();
 				getVideos().addAll((Collection<? extends Video>)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case DataPackage.CONTENT__CONTENTS:
 				getContents().clear();
 				return;
 			case DataPackage.CONTENT__CONTRIBUTORS:
 				getContributors().clear();
 				return;
 			case DataPackage.CONTENT__AUTHOR:
 				setAuthor((Person)null);
 				return;
 			case DataPackage.CONTENT__DOCUMENTS:
 				getDocuments().clear();
 				return;
 			case DataPackage.CONTENT__PARENT_CONTENT:
 				setParentContent((Content)null);
 				return;
 			case DataPackage.CONTENT__LOCALE:
 				setLocale(LOCALE_EDEFAULT);
 				return;
 			case DataPackage.CONTENT__TRANSFORMATIONS:
 				getTransformations().clear();
 				return;
 			case DataPackage.CONTENT__VIDEOS:
 				getVideos().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case DataPackage.CONTENT__CONTENTS:
 				return contents != null && !contents.isEmpty();
 			case DataPackage.CONTENT__CONTRIBUTORS:
 				return contributors != null && !contributors.isEmpty();
 			case DataPackage.CONTENT__AUTHOR:
 				return author != null;
 			case DataPackage.CONTENT__DOCUMENTS:
 				return documents != null && !documents.isEmpty();
 			case DataPackage.CONTENT__PARENT_CONTENT:
 				return parentContent != null;
 			case DataPackage.CONTENT__LOCALE:
 				return LOCALE_EDEFAULT == null ? locale != null : !LOCALE_EDEFAULT.equals(locale);
 			case DataPackage.CONTENT__TRANSFORMATIONS:
 				return transformations != null && !transformations.isEmpty();
 			case DataPackage.CONTENT__VIDEOS:
 				return videos != null && !videos.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
 		switch (operationID) {
 			case DataPackage.CONTENT___GET_ORGANISATIONS:
 				return getOrganisations();
 			case DataPackage.CONTENT___GET_PERSONS:
 				return getPersons();
 			case DataPackage.CONTENT___COMMENT__STRING:
 				return comment((String)arguments.get(0));
 			case DataPackage.CONTENT___ADD_CONTRIBUTOR__PERSON:
 				return addContributor((Person)arguments.get(0));
 			case DataPackage.CONTENT___ATTACH_DOCUMENT__STRING:
 				return attachDocument((String)arguments.get(0));
 			case DataPackage.CONTENT___COMMENT_WITH_META_TAG_LIST__STRING_STRING:
 				return commentWithMetaTagList((String)arguments.get(0), (String)arguments.get(1));
 		}
 		return super.eInvoke(operationID, arguments);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (locale: ");
 		result.append(locale);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * Generates an EObjectCondition to check whether an Object is of the type Content.
 	 * 
 	 * @return An EObjectCondition whether the Object is of the type Content.
 	 * @generated
 	 */
 	public static EObjectCondition generateIsTypeCondition() {
 		return new EObjectTypeRelationCondition(DataPackageImpl.eINSTANCE.getContent());
 	}
 
 	/**
 	 * This method provides a generic access to the Getters of this class.
  	 * 
  	 * @param opName The name of the Feature to be gotten.
  	 *
  	 * @return The value of the Feature or null.
  	 * 
 	 * @generated
 	 */
 	protected Object getFeature(String featureName) throws UnknownOperationException {
 		if ( featureName.equalsIgnoreCase("contents") )
 			return this.getContents();		
 		if ( featureName.equalsIgnoreCase("contributors") )
 			return this.getContributors();		
 		if ( featureName.equalsIgnoreCase("author") )
 			return this.getAuthor();		
 		if ( featureName.equalsIgnoreCase("documents") )
 			return this.getDocuments();		
 		if ( featureName.equalsIgnoreCase("parentContent") )
 			return this.getParentContent();		
 		if ( featureName.equalsIgnoreCase("locale") )
 			return this.getLocale();		
 		if ( featureName.equalsIgnoreCase("transformations") )
 			return this.getTransformations();		
 		if ( featureName.equalsIgnoreCase("videos") )
 			return this.getVideos();			
 		return super.getFeature(featureName); 
 	}
 
 	/**
 	 * This method provides a generic access to the Setters of this class.
  	 * 
  	 * @param opName The name of the Feature to be set.
  	 * @param value The new value of the feature.
  	 * 
 	 * @generated
 	 */
 	protected Object setFeature(String featureName, Object value) throws WrongArgException, UnknownOperationException {
 		if ( featureName.equalsIgnoreCase("author") ) {
 				org.sociotech.communitymashup.data.Person fauthor = null;
 				try {
 					try {
 						fauthor = (org.sociotech.communitymashup.data.Person)(RestUtil.fromInput(value));
 					} catch (ClassNotFoundException e) {
 						fauthor = (org.sociotech.communitymashup.data.Person)value;
 					}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Content.setFeature", "org.sociotech.communitymashup.data.Person",value.getClass().getName());
 				}
 				this.setAuthor(fauthor);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("parentContent") ) {
 				org.sociotech.communitymashup.data.Content fparentContent = null;
 				try {
 					try {
 						fparentContent = (org.sociotech.communitymashup.data.Content)(RestUtil.fromInput(value));
 					} catch (ClassNotFoundException e) {
 						fparentContent = (org.sociotech.communitymashup.data.Content)value;
 					}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Content.setFeature", "org.sociotech.communitymashup.data.Content",value.getClass().getName());
 				}
 				this.setParentContent(fparentContent);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("locale") ) {
 				java.lang.String flocale = null;
 				try {
 					flocale = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Content.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setLocale(flocale);
 			return this;
 			}			
 		super.setFeature(featureName, value);
 		return this; 
 	}
 
 	/**
 	 * This method provides a generic access to the Operations of this class.
  	 * 
  	 * @param opName The name of the requested Operation.
  	 * @param values The arguments to be used.
  	 * 
  	 * @return The result of the Operation or null.
  	 * 
 	 * @generated
 	 */
 	protected Object doOperation(RestCommand command) throws ArgNotFoundException, WrongArgException, WrongArgCountException, UnknownOperationException {
 		if ( command.getCommand().equalsIgnoreCase("getOrganisations")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("Content.doOperation", 0, command.getArgCount()); 
 			return this.getOrganisations();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersons")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("Content.doOperation", 0, command.getArgCount()); 
 			return this.getPersons();
 		}
 		if ( command.getCommand().equalsIgnoreCase("comment")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("Content.doOperation", 1, command.getArgCount()); 
 			java.lang.String comment = null;
 			try {
 				comment = (java.lang.String)command.getArg("comment");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("Content.doOperation", "java.lang.String", command.getArg("comment").getClass().getName());
 			}
 			return this.comment(comment);
 		}
 		if ( command.getCommand().equalsIgnoreCase("addContributor")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("Content.doOperation", 1, command.getArgCount()); 
 			Person contributor = null;
 			try {
 				try {
 					contributor = (Person)(RestUtil.fromInput(command.getArg("contributor")));
 				} catch (ClassNotFoundException e) {
 					contributor = (Person)command.getArg("contributor");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("Content.doOperation", "Person", command.getArg("contributor").getClass().getName());
 			}
 			return this.addContributor(contributor);
 		}
 		if ( command.getCommand().equalsIgnoreCase("attachDocument")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("Content.doOperation", 1, command.getArgCount()); 
 			java.lang.String fileUrl = null;
 			try {
 				fileUrl = (java.lang.String)command.getArg("fileUrl");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("Content.doOperation", "java.lang.String", command.getArg("fileUrl").getClass().getName());
 			}
 			return this.attachDocument(fileUrl);
 		}
 		if ( command.getCommand().equalsIgnoreCase("commentWithMetaTagList")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("Content.doOperation", 2, command.getArgCount()); 
 			java.lang.String comment = null;
 			try {
 				comment = (java.lang.String)command.getArg("comment");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("Content.doOperation", "java.lang.String", command.getArg("comment").getClass().getName());
 			}
 			java.lang.String metaTagList = null;
 			try {
 				metaTagList = (java.lang.String)command.getArg("metaTagList");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("Content.doOperation", "java.lang.String", command.getArg("metaTagList").getClass().getName());
 			}
 			return this.commentWithMetaTagList(comment, metaTagList);
 		}	
 		return super.doOperation(command);
 	}
 
 	/**
 	 * This method can be used to recursively and generically call the Getter, Setters and Operations of the generated classes.
 	 * 
 	 * @param input The commands to be processed.
 	 * @param requestType The HTTP-Method of the request.
 	 * 
 	 * @return The result of the Getter/Operation or null.
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object process(LinkedList<RestCommand> input, RequestType requestType) throws ArgNotFoundException, WrongArgException, WrongArgCountException, UnknownOperationException {
 		Object o = null;
 		RestCommand c = input.poll();
 		// check for HTTP-Request method
 		if (requestType == RequestType.rtGet) {
 			// only Getters are allowed -> side-effects...
 			if (c.getCommand().startsWith("get")) {
 				if (c.getArgCount() != 0) throw new WrongArgCountException(c.getCommand(), 0, c.getArgCount());
 				o = this.getFeature(c.getCommand().substring(3));
 			}
 		} else {
 			// everything is allowed - at least for now
 			try {
 				o = this.doOperation(c);
 			} catch(Exception e) {
 				if (c.getCommand().startsWith("get")) {
 					if (c.getArgCount() != 0) throw new WrongArgCountException(c.getCommand(), 0, c.getArgCount());
 					o = this.getFeature(c.getCommand().substring(3));
 				} else if (c.getCommand().startsWith("set")) {
 					if (c.getArgCount() != 1) throw new WrongArgCountException(c.getCommand(), 1, c.getArgCount());
 					Object so = c.getArg("new" + c.getCommand().substring(3));
 					o = this.setFeature(c.getCommand().substring(3), so);
 				} else {
 					if (e instanceof ArgNotFoundException)
 						throw (ArgNotFoundException)e;
 					if (e instanceof WrongArgException)
 						throw (WrongArgException)e;
 					if (e instanceof WrongArgCountException)
 						throw (WrongArgCountException)e;
 					if (e instanceof UnknownOperationException)
 						throw (UnknownOperationException)e;
 				}
 			}
 		}
 		if (input.isEmpty()) {
 			return o;
 		} else { 
 			if (o instanceof PersonImpl) {
 				return ((Person) o).process(input, requestType);
 			}
 			if (o instanceof InformationObjectImpl) {
 				return ((InformationObject) o).process(input, requestType);
 			}
 			if (o instanceof ContentImpl) {
 				return ((Content) o).process(input, requestType);
 			}
 			if (o instanceof DataSetImpl) {
 				return ((DataSet) o).process(input, requestType);
 			}
 			if (o instanceof ItemImpl) {
 				return ((Item) o).process(input, requestType);
 			}
 			if (o instanceof ExtensionImpl) {
 				return ((Extension) o).process(input, requestType);
 			}
 			if (o instanceof ClassificationImpl) {
 				return ((Classification) o).process(input, requestType);
 			}
 			if (o instanceof CategoryImpl) {
 				return ((Category) o).process(input, requestType);
 			}
 			if (o instanceof TagImpl) {
 				return ((Tag) o).process(input, requestType);
 			}
 			if (o instanceof OrganisationImpl) {
 				return ((Organisation) o).process(input, requestType);
 			}
 			if (o instanceof MetaTagImpl) {
 				return ((MetaTag) o).process(input, requestType);
 			}
 			if (o instanceof PhoneImpl) {
 				return ((Phone) o).process(input, requestType);
 			}
 			if (o instanceof InstantMessengerImpl) {
 				return ((InstantMessenger) o).process(input, requestType);
 			}
 			if (o instanceof EmailImpl) {
 				return ((Email) o).process(input, requestType);
 			}
 			if (o instanceof WebAccountImpl) {
 				return ((WebAccount) o).process(input, requestType);
 			}
 			if (o instanceof WebSiteImpl) {
 				return ((WebSite) o).process(input, requestType);
 			}
 			if (o instanceof RankingImpl) {
 				return ((Ranking) o).process(input, requestType);
 			}
 			if (o instanceof AttachmentImpl) {
 				return ((Attachment) o).process(input, requestType);
 			}
 			if (o instanceof LocationImpl) {
 				return ((Location) o).process(input, requestType);
 			}
 			if (o instanceof ImageImpl) {
 				return ((Image) o).process(input, requestType);
 			}
 			if (o instanceof DocumentImpl) {
 				return ((Document) o).process(input, requestType);
 			}
 			if (o instanceof StarRankingImpl) {
 				return ((StarRanking) o).process(input, requestType);
 			}
 			if (o instanceof ViewRankingImpl) {
 				return ((ViewRanking) o).process(input, requestType);
 			}
 			if (o instanceof ThumbRankingImpl) {
 				return ((ThumbRanking) o).process(input, requestType);
 			}
 			if (o instanceof TransformationImpl) {
 				return ((Transformation) o).process(input, requestType);
 			}
 			if (o instanceof VideoImpl) {
 				return ((Video) o).process(input, requestType);
 			}
 			if (o instanceof ConnectionImpl) {
 				return ((Connection) o).process(input, requestType);
 			}
 			if (o instanceof BinaryImpl) {
 				return ((Binary) o).process(input, requestType);
 			}
 			if (o instanceof MetaInformationImpl) {
 				return ((MetaInformation) o).process(input, requestType);
 			}
 			if (o instanceof IndoorLocationImpl) {
 				return ((IndoorLocation) o).process(input, requestType);
 			}
 			if (o instanceof IdentifierImpl) {
 				return ((Identifier) o).process(input, requestType);
 			}
 			if (o instanceof EventImpl) {
 				return ((Event) o).process(input, requestType);
 			}
 			if (o instanceof DeletedItemImpl) {
 				return ((DeletedItem) o).process(input, requestType);
 			}
 			if (o instanceof List) {
 				return RestUtil.listProcess((List<?>) o, input, requestType);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the condition to evaluate if a content is a main content.
 	 * 
 	 * @return The condition to evaluate if a content is a main content, null in error case.
 	 */
 	public static EObjectCondition isMainContentCondition()
 	{
 		if(isMainContentCondition == null)
 		{
 			// condition will be created at first use
 			try
 			{
 				// main content has no parent content
 				String oclStatement = "self." + DataPackage.eINSTANCE.getContent_ParentContent().getName() + ".oclIsUndefined()";
 				
 				isMainContentCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( getOclEnvironment(),
 																								oclStatement,
 																							    DataPackage.eINSTANCE.getContent());
 				
 			}
 			catch (ParserException e)
 			{
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		return isMainContentCondition;
 	}
 	
 	/**
 	 * Returns the condition to evaluate if a organisation belongs to a content.
 	 * 
 	 * @return The condition to evaluate if a organisation belongs to a content., null in error case.
 	 */
 	public EObjectCondition isOrgansiationOfContentCondition()
 	{
 		EObjectCondition isOrgansiationOfContentCondition;
 		
 		if(this.getIdent() == null)
 		{
 			return null;
 		}
 		// condition will be created at first use
 		try
 		{
 			// for participants
 			
 			// contributers' organisations
 			String oclStatement = "self.participants->exists(p1 | p1.contributed->exists(c1 | c1.ident = '" + this.ident + "'))";
 			// or author's organisations
 			oclStatement = oclStatement + " or self.participants->exists(p2 | p2.authored->exists(c2 | c2.ident = '" + this.ident + "'))";
 			
 			// also for leaders
 			
 			// contributers' organisations
 			oclStatement = oclStatement + " or self.leader.contributed->exists(c3 | c3.ident = '" + this.ident + "')";
 			// or author's organisations
 			oclStatement = oclStatement + " or self.leader.authored->exists(c4 | c4.ident = '" + this.ident + "')";
 			
 			log("contents ocl: " + oclStatement, LogService.LOG_DEBUG);
 			
 			isOrgansiationOfContentCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( getOclEnvironment(),
 																							oclStatement,
 																						    DataPackage.eINSTANCE.getOrganisation());
 			
 		}
 		catch (ParserException e)
 		{
 			log("Exception (" + e.getMessage() + ") while parsing OCL expresion for organisation of content", LogService.LOG_WARNING);
 			//e.printStackTrace();
 			return null;
 		}
 		
 		return isOrgansiationOfContentCondition;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.data.impl.InformationObjectImpl#delete()
 	 */
 	@Override
 	public void delete() {
 		
 		// delete all sub contents
 		EList<Content> subContents = this.getContents();
 		
 		if (subContents != null && !subContents.isEmpty())
 		{
 			// copy list to avoid concurrent modification
 			LinkedList<Content> copiedSubContents = new LinkedList<Content>(subContents);
 			for(Content subContent : copiedSubContents)
 			{
 				subContent.delete();
 			}
 		}
 		
 		// delete this
 		super.delete();
 	}
 
 	
 } //ContentImpl
