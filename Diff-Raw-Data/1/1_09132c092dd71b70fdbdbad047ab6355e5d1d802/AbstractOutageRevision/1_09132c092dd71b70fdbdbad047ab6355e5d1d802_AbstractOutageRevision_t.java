 package com.pocketcookies.pepco.model;
 
 import java.sql.Timestamp;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.Entity;
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
 @Table(name="OUTAGEREVISIONS")
 @Inheritance(strategy= InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(name="OUTAGETYPE")
 public abstract class AbstractOutageRevision {
 	private int id;
 	/**
 	 * The number of customers affected. If fewer than 5 customers are affected,
 	 * Pepco will say "Less than 5". To represent this uncertainty, we will use
 	 * 0 to represent that there are fewer than 5 customers for that outage.
 	 */
 	private int numCustomersAffected;
 	private Timestamp estimatedRestoration;
 	private Outage outage;
 	// The time at which we recorded this revision to the database.
 	private Timestamp observationDate;
 	// The parser run with which this change is associated. This is very similar
 	// to observationDate. It lets us group together a set of changes that don't
 	// occur at exactly the same time.
 	private ParserRun run;
 
 	protected AbstractOutageRevision() {
 		super();
 	}
 
 	public AbstractOutageRevision(int numCustomersAffected,
 			Timestamp estimatedRestoration, final Timestamp observationDate,
 			Outage outage, final ParserRun run) {
 		this();
 		setNumCustomersAffected(numCustomersAffected);
 		setEstimatedRestoration(estimatedRestoration);
 		setOutage(outage);
 		setObservationDate(observationDate);
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
 		if (!(o instanceof AbstractOutageRevision))
                 {
 			return false;
                 }
 		final AbstractOutageRevision revision = (AbstractOutageRevision) o;
 		return equalsIgnoreObservationDate(revision)
 				&& this.getObservationDate().equals(revision.getObservationDate());
 	}
 
         /**
          * Checks that these two objects are the same except for the observationDate which is disregarded.
          * @param revision The revision to compare against.
          * @return True of the objects are the same ignoring the observationDate.
          */
 	public boolean equalsIgnoreObservationDate(AbstractOutageRevision revision) {
 		if (this.getEstimatedRestoration() != revision.getEstimatedRestoration()) {
 			if (this.getEstimatedRestoration() == null)
 				return false;
 			else if (!this.getEstimatedRestoration()
 					.equals(revision.getEstimatedRestoration()))
 				return false;
 		}
 		return this.getNumCustomersAffected() == revision.getNumCustomersAffected();
 	}
 
 	@Override
 	public int hashCode() {
 		return (int) (getNumCustomersAffected() + getEstimatedRestoration().getTime() + getObservationDate()
 				.getTime());
 	}
 
         @Id
         @GeneratedValue
 	public int getId() {
 		return id;
 	}
 
         @Column(name="NUMCUSTOMERSAFFECTED")
 	public int getNumCustomersAffected() {
 		return numCustomersAffected;
 	}
 
         @Column(name="ESTIMATEDRESTORATION")
 	public Timestamp getEstimatedRestoration() {
 		return estimatedRestoration;
 	}
 
         @ManyToOne
         @JoinColumn(name="OUTAGE")
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
 
         @Column(name="OBSERVATIONDATE")
 	public Timestamp getObservationDate() {
 		return observationDate;
 	}
 
 	private void setObservationDate(Timestamp observationDate) {
 		this.observationDate = observationDate;
 	}
 
         @ManyToOne
        @JoinColumn(name="RUN")
 	public ParserRun getRun() {
 		return run;
 	}
 
 	private void setRun(ParserRun run) {
 		this.run = run;
 	}
 
 }
