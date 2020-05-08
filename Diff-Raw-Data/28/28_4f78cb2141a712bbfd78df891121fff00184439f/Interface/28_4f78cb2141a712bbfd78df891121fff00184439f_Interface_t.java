 /*
    Copyright 2010 Andy Hedges <andy@hedges.net>
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package net.hedges.fandangled.bindings.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Interface {
 
     private Metadata metadata;
 	private String name;
 	private String serviceName;
 	private List<Author> authors = new ArrayList<Author>();
 	private Owner owner;
 	private Version version;
 	private String description;
    private String organisation;
	private String namespacePrefix;
    private String organisationDomainName;
 	private List<Operation> operations = new ArrayList<Operation>();
 	private List<Event> events = new ArrayList<Event>();
 	private List<Type> types = new ArrayList<Type>();
 	private List<Exception> exceptions = new ArrayList<Exception>();
 	private List<Enumeration> enumerations = new ArrayList<Enumeration>();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getServiceName() {
 		return serviceName;
 	}
 
 	public void setServiceName(String serviceName) {
 		this.serviceName = serviceName;
 	}
 
 	public List<Author> getAuthors() {
 		return authors;
 	}
 
 	public void setAuthors(List<Author> authors) {
 		this.authors = authors;
 	}
 
 	public Owner getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Owner owner) {
 		this.owner = owner;
 	}
 
 	public Version getVersion() {
 		return version;
 	}
 
 	public void setVersion(Version version) {
 		this.version = version;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List<Operation> getOperations() {
 		return operations;
 	}
 
 	public void setOperations(List<Operation> operations) {
 		this.operations = operations;
 	}
 
 	public List<Event> getEvents() {
 		return events;
 	}
 
 	public void setEvents(List<Event> events) {
 		this.events = events;
 	}
 
 	public List<Type> getTypes() {
 		return types;
 	}
 
 	public void setTypes(List<Type> types) {
 		this.types = types;
 	}
 
 	public List<Exception> getExceptions() {
 		return exceptions;
 	}
 
 	public void setExceptions(List<Exception> exceptions) {
 		this.exceptions = exceptions;
 	}
 
 	public List<Enumeration> getEnumerations() {
 		return enumerations;
 	}
 
 	public void setEnumerations(List<Enumeration> enumerations) {
 		this.enumerations = enumerations;
 	}
 
     public Metadata getMetadata() {
         return metadata;
     }
 
     public void setMetadata(Metadata metadata) {
         this.metadata = metadata;
     }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public String getOrganisationDomainName() {
        return organisationDomainName;
    }

    public void setOrganisationDomainName(String organisationDomainName) {
        this.organisationDomainName = organisationDomainName;
    }
 }
