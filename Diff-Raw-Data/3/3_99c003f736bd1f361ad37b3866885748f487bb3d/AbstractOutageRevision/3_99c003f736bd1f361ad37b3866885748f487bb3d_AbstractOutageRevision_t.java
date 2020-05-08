 package com.pocketcookies.pepco.model;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 /**
  * We like to keep track of all the things that happen to a revision over its
  * lifetime.
  * 
  * @author jack
  * 
  */
 @Entity
 @Table(name = "OUTAGEREVISIONS")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(name = "OUTAGETYPE")
 public abstract class AbstractOutageRevision implements Serializable, Comparable<AbstractOutageRevision> {
 
     private int id;
     /**
      * The number of customers affected. If fewer than 5 customers are affected,
      * Pepco will say "Less than 5". To represent this uncertainty, we will use
      * 0 to represent that there are fewer than 5 customers for that outage.
      */
     private int numCustomersAffected;
     private Timestamp estimatedRestoration;
     private Outage outage;
     // The parser run with which this revision is associated.  This lets us
     // group together outages so that we can know the state of all the
     // outages at a particular time.
     private ParserRun run;
 
     protected AbstractOutageRevision() {
 	super();
     }
 
     public AbstractOutageRevision(int numCustomersAffected,
 	    Timestamp estimatedRestoration,
 	    Outage outage, final ParserRun run) {
 	this();
 	setNumCustomersAffected(numCustomersAffected);
 	setEstimatedRestoration(estimatedRestoration);
 	setOutage(outage);
 	setRun(run);
     }
 
     /**
      * Checks whether this object is the same as o.
      * 
      * @param o The object to compare against.
      * @return True if this object and o are equal.
      */
     @Override
     public boolean equals(final Object o) {
 	if (!(o instanceof AbstractOutageRevision)) {
 	    return false;
 	}
 	final AbstractOutageRevision revision = (AbstractOutageRevision) o;
 	return equalsIgnoreRun(revision)
 		&& this.getRun().equals(revision.getRun());
     }
 
     /**
      * Checks that these two objects are the same except for the observationDate which is disregarded.
      * @param revision The revision to compare against.
      * @return True of the objects are the same ignoring the observationDate.
      */
     public boolean equalsIgnoreRun(AbstractOutageRevision revision) {
 	if (this.getEstimatedRestoration() != revision.getEstimatedRestoration()) {
 	    if (this.getEstimatedRestoration() == null) {
 		return false;
 	    } else if (!this.getEstimatedRestoration().equals(revision.getEstimatedRestoration())) {
 		return false;
 	    }
 	}
 	return this.getNumCustomersAffected() == revision.getNumCustomersAffected();
     }
 
     @Override
     public int hashCode() {
	final long estimatedRestoration=getEstimatedRestoration()==null?0:getEstimatedRestoration().getTime();
	return (int) (getNumCustomersAffected() + estimatedRestoration + getRun().getAsof().getTime());
     }
 
     @Id
     @GeneratedValue
     @Column(name = "ID")
     public int getId() {
 	return id;
     }
 
     @Column(name = "NUMCUSTOMERSAFFECTED")
     public int getNumCustomersAffected() {
 	return numCustomersAffected;
     }
 
     @Column(name = "ESTIMATEDRESTORATION")
     public Timestamp getEstimatedRestoration() {
 	return estimatedRestoration;
     }
 
     @ManyToOne
     @JoinColumn(name = "OUTAGE")
     public Outage getOutage() {
 	return outage;
     }
 
     @SuppressWarnings("unused")
     private void setId(int id) {
 	this.id = id;
     }
 
     private void setNumCustomersAffected(int numCustomersAffected) {
 	this.numCustomersAffected = numCustomersAffected;
     }
 
     private void setEstimatedRestoration(Timestamp estimatedRestoration) {
 	this.estimatedRestoration = estimatedRestoration;
     }
 
     public void setOutage(Outage outage) {
 	this.outage = outage;
     }
 
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "RUN")
     public ParserRun getRun() {
 	return run;
     }
 
     private void setRun(ParserRun run) {
 	this.run = run;
     }
 
     @Override
     public int compareTo(final AbstractOutageRevision o) {
 	final int comparison = getRun().compareTo(o.getRun());
 	if (comparison == 0) {
 	    //Make sure we don't say two objects are equal just because they occur at the same time.
 	    return getId() - o.getId();
 	} else {
 	    return -comparison;
 	}
     }
 }
