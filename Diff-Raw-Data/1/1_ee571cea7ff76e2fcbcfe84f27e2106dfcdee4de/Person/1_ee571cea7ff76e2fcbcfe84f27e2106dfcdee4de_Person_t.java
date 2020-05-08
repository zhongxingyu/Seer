 /*
  * @(#)Person.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Organization Module for the MyOrg web application.
  *
  *   The Organization Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Organization Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Organization Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.organization.domain;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import module.organization.domain.PartyType.PartyTypeBean;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import myorg.domain.User.UserPresentationStrategy;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.plugins.luceneIndexing.IndexableField;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.IndexDocument;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Indexable;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Searchable;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class Person extends Person_Base implements Searchable, Indexable {
 
     static {
 	User.registerUserPresentationStrategy(new UserPresentationStrategy() {
 
 	    @Override
 	    public String present(User user) {
 		return user.getPerson() != null ? user.getPerson().getName() + " (" + user.getUsername() + ")" : user
 			.getUsername();
 	    }
 
 	    @Override
 	    public String shortPresent(User user) {
 		return user.getPerson() != null ? user.getPerson().getFirstAndLastName() + " (" + user.getUsername() + ")" : user
 			.getUsername();
 	    }
 
 	});
     }
 
     static public class PersonBean extends PartyBean {
 	private static final long serialVersionUID = -7516282978280402225L;
 
 	private Person person;
 	private String name;
 
 	public PersonBean() {
 	    setBegin(new LocalDate());
 	}
 
 	public PersonBean(final Person person) {
 	    this();
 
 	    setPerson(person);
 	    setName(person.getName());
 	}
 
 	public String getName() {
 	    return name;
 	}
 
 	public void setName(String name) {
 	    this.name = name;
 	}
 
 	public Person getPerson() {
 	    return this.person;
 	}
 
 	public void setPerson(Person person) {
 	    this.person = person;
 	}
 
 	@Override
 	public Party getParty() {
 	    return getPerson();
 	}
 
 	public void edit() {
 	    getPerson().edit(this);
 	}
 
 	@Override
 	public void addParent() {
 	    getPerson().addParent(getParent(), getAccountabilityType(), getBegin(), getEnd());
 	}
 
     }
 
     public Person(MultiLanguageString partyName, PartyType partyType) {
 	super();
 	setMyOrgFromPerson(MyOrg.getInstance());
 	setPartyName(partyName);
 	addPartyTypes(partyType);
     }
 
     public String getUserAliasses() {
 	try {
 	    return getRemotePerson().getUserAliass();
 	} catch (Throwable t) {
 	}
 	return getUser().getUsername();
     }
 
     @Override
     final public boolean isPerson() {
 	return true;
     }
 
     @Override
     protected void disconnect() {
	removeUser();
 	removeMyOrgFromPerson();
 	super.disconnect();
     }
 
     @Service
     public void edit(final PersonBean bean) {
 	final MultiLanguageString name = getPartyName();
 	name.setContent(bean.getName());
 	setPartyName(name);
     }
 
     public String getName() {
 	return getPartyName().getContent();
     }
 
     public String getFirstAndLastName() {
 	String name = getName();
 	int s1 = name.indexOf(' ');
 	int s2 = name.lastIndexOf(' ');
 	return s1 < 0 || s1 == s2 ? name : name.subSequence(0, s1) + name.substring(s2);
     }
 
     @Service
     static public Person create(final MultiLanguageString partyName, final PartyType partyType) {
 	return new Person(partyName, partyType);
     }
 
     @Service
     static public Person create(final PersonBean bean) {
 	return create(new MultiLanguageString(bean.getName()), getPartyTypeInstance());
     }
 
     static public Person readByPartyName(MultiLanguageString partyName) {
 	for (final Party party : MyOrg.getInstance().getParties()) {
 	    if (!party.isUnit()) {
 		if (party.getPartyName().equals(partyName)) {
 		    return (Person) party;
 		}
 	    }
 	}
 	return null;
     }
 
     static public PartyType getPartyTypeInstance() {
 	final String type = Person.class.getName();
 	PartyType partyType = PartyType.readBy(type);
 	if (partyType == null) {
 	    synchronized (Person.class) {
 		partyType = PartyType.readBy(Person.class.getName());
 		if (partyType == null) {
 		    final PartyTypeBean partyTypeBean = new PartyTypeBean();
 		    partyTypeBean.setType(type);
 		    partyTypeBean.setName(new MultiLanguageString("Pessoa"));
 		    partyType = PartyType.create(partyTypeBean);
 		}
 	    }
 	}
 	return partyType;
     }
 
     public static List<Person> searchPersons(String value) {
 	final List<Person> persons = new ArrayList<Person>();
 
 	final String trimmedValue = value.trim();
 	final String[] input = trimmedValue.split(" ");
 	StringNormalizer.normalize(input);
 
 	for (final Party party : MyOrg.getInstance().getPersonsSet()) {
 	    if (party.isPerson()) {
 		final Person person = (Person) party;
 		final String unitName = StringNormalizer.normalize(person.getPartyName().getContent());
 		if (hasMatch(input, unitName)) {
 		    persons.add(person);
 		}
 	    }
 	}
 
 	Collections.sort(persons, Party.COMPARATOR_BY_NAME);
 
 	return persons;
     }
 
     private static boolean hasMatch(final String[] input, final String unitNameParts) {
 	for (final String namePart : input) {
 	    if (unitNameParts.indexOf(namePart) == -1) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     /**
      * Enum used for the values of the indexes that are used for the lucene
      * plugin
      * 
      * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
      * 
      */
     public enum IndexableFields implements IndexableField {
 	PERSON_NAME, PERSON_USERNAME;
 
 	@Override
 	public String getFieldName() {
 	    return name();
 	}
 
     }
 
     @Override
     public IndexDocument getDocumentToIndex() {
 	IndexDocument indexDocument = new IndexDocument(this);
 	indexDocument.indexField(IndexableFields.PERSON_NAME, this.getName());
 	indexDocument.indexField(IndexableFields.PERSON_USERNAME, this.getUser().getUsername());
 	return indexDocument;
     }
 
     @Override
     public Set<Indexable> getObjectsToIndex() {
 	return Collections.singleton((Indexable) this);
     }
 
 }
