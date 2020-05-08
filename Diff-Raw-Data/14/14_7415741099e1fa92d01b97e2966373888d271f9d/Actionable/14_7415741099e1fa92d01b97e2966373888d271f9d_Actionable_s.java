 package edu.rit.taskers.model;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 
 @Entity(name = "Actionable")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(
         name="TypeID",
         discriminatorType = DiscriminatorType.INTEGER
 )
 public abstract class Actionable {
 	
 	public static final String ACTION_UI_DATEFORMAT = "yyyy-MM-dd";
 	public static final String ACTION_UI_TIMEFORMAT = "h:m a";
 	
 
     protected int id;
     //TODO space id
 	protected Contact creator;
     protected String name;
 	protected Date created;
 	protected String description;
 	protected Contact assignedTo;
 	protected Date targetdate;
	protected String priority;
 	//protected Date when;
 	//TODO protected List<Actionable> dependant;
     protected Set<Comment> comments = new HashSet<Comment>(0);
 
     protected Actionable() {}
 
     @Id
     @GeneratedValue
     @Column(name = "ID")
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Column(name="Name")
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @OneToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "Creator")
     public Contact getCreator() {
         return creator;
     }
 
 
     public void setCreator(Contact creator) {
         this.creator = creator;
     }
 
     @Column(name="Created")
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
 	}
 
     @Column(name="Description")
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
    @Column(name="Priority")
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

     @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Contact.class)
     @JoinColumn(name = "AssignedTo")
     public Contact getAssignedTo() {
         return assignedTo;
     }
 
     public void setAssignedTo(Contact assignedTo) {
         this.assignedTo = assignedTo;
     }
 //
 //    public List<Actionable> getDependant() {
 //        return dependant;
 //    }
 //
 //    public void setDependant(List<Actionable> dependant) {
 //        this.dependant = dependant;
 //    }
 
     @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Comment.class)
     @JoinColumn(name = "ItemID")
     public Set<Comment> getComments() {
         return comments;
     }
 
     public void setComments(Set<Comment> comments) {
         this.comments = comments;
     }
     
     @Column(name="TargetDate")
     public Date getTargetDate() {
     	return targetdate;
     }
 
     public void setTargetDate(Date targetdate) {
     	this.targetdate = targetdate;
     }
 
     @Override
     public String toString() {
         return "Actionable{" +
                 "id=" + id +
                 ", creator=" + creator +
                 ", name='" + name + '\'' +
                 ", created=" + created +
                 ", description='" + description + '\'' +
                 ", assignedTo=" + assignedTo +
                 ", targetdate=" + targetdate +
                ", priority='" + priority + '\'' +
                 ", comments=" + comments +
                 '}';
     }
 }
