 package com.crowdplatform.model;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.annotations.Formula;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 @Entity
 public class Project {
 
 	@Id
 	@GeneratedValue
 	private Integer id;
 	
 	@NotNull
 	@Size(min=4)
 	private String name;
 	
 	private Date creationDate;
 	
 	@OneToMany
 	private Set<Batch> batches;
 	
 	@OneToMany
 	private Set<Field> fields;
 	
 	@OneToMany
 	private Set<ProjectUser> users;
 	
	@Formula("(select count(*) from user_project up where up.projects_id=id)")
 	private Integer numUsers;
 	
 	public Project() {
 		batches = Sets.newHashSet();
 		fields = Sets.newHashSet();
 		users = Sets.newHashSet();
 		creationDate = new Date();
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Set<Batch> getBatches() {
 		return batches;
 	}
 
 	public void setBatches(Set<Batch> batches) {
 		this.batches = batches;
 	}
 	
 	public void addBatch(Batch batch) {
 		this.batches.add(batch);
 	}
 
 	public Set<Field> getFields() {
 		return fields;
 	}
 
 	public void setFields(Set<Field> inputFields) {
 		this.fields = inputFields;
 	}
 	
 	public void addField(Field field) {
 		this.fields.add(field);
 	}
 	
 	public Set<Field> getInputFields() {
 		Set<Field> result = Sets.newHashSet();
 		for (Field field : fields) {
 			if (field.getFieldType() == Field.FieldType.INPUT) {
 				result.add(field);
 			}
 		}
 		return result;
 	}
 	
 	public Set<Field> getOutputFields() {
 		Set<Field> result = Sets.newHashSet();
 		for (Field field : fields) {
 			if (field.getFieldType() == Field.FieldType.OUTPUT) {
 				result.add(field);
 			}
 		}
 		return result;
 	}
 
 	public Set<ProjectUser> getUsers() {
 		return users;
 	}
 
 	public void setUsers(Set<ProjectUser> users) {
 		this.users = users;
 	}
 	
 	public void addUser(ProjectUser user) {
 		this.users.add(user);
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public Integer getNumUsers() {
 		return numUsers;
 	}
 
 	public void setNumUsers(Integer numUsers) {
 		this.numUsers = numUsers;
 	}
 
 	public List<Field> getOrderedInputFields() {
 		List<Field> list = Lists.newArrayList();
 		list.addAll(getInputFields());
 		Collections.sort(list, new Comparator<Field>() {
 
 			@Override
 			public int compare(Field arg0, Field arg1) {
 				return arg0.getId().compareTo(arg1.getId());
 			}});
 		return list;
 	}
 	
 	public List<Field> getOrderedOutputFields() {
 		List<Field> list = Lists.newArrayList();
 		list.addAll(getOutputFields());
 		Collections.sort(list, new Comparator<Field>() {
 
 			@Override
 			public int compare(Field arg0, Field arg1) {
 				return arg0.getId().compareTo(arg1.getId());
 			}});
 		return list;
 	}
 	
 	public List<Batch> getOrderedBatches() {
 		List<Batch> list = Lists.newArrayList();
 		list.addAll(this.batches);
 		Collections.sort(list, new Comparator<Batch>() {
 
 			@Override
 			public int compare(Batch b1, Batch b2) {
 				return b1.getCreationDate().compareTo(b2.getCreationDate());
 			}});
 		return list;
 	}
 }
