 package cz.cvut.fel.bupro.model;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import cz.cvut.fel.bupro.TimeUtils;
 
 @Entity
@Table(uniqueConstraints = @UniqueConstraint(name="one_membership_per_project", columnNames = { "user_id", "project_id" }))
 public class Membership extends BaseEntity implements Serializable {
 	private static final long serialVersionUID = 5731617459882117644L;
 
 	@ManyToOne(optional = false, fetch = FetchType.EAGER)
 	private User user;
 
 	@ManyToOne(optional = false)
 	private Project project;
 
 	@Column(nullable = false)
 	private Timestamp created;
 	@Column(nullable = false)
 	private Timestamp changed;
 
 	@Enumerated
 	@Column(nullable = false)
 	private MembershipState membershipState = MembershipState.WAITING_APPROVAL;
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public Project getProject() {
 		return project;
 	}
 
 	public void setProject(Project project) {
 		this.project = project;
 	}
 
 	public Timestamp getCreated() {
 		return created;
 	}
 
 	public void setCreated(Timestamp created) {
 		this.created = created;
 	}
 
 	public Timestamp getChanged() {
 		return changed;
 	}
 
 	public void setChanged(Timestamp changed) {
 		this.changed = changed;
 	}
 
 	public MembershipState getMembershipState() {
 		return membershipState;
 	}
 
 	public void setMembershipState(MembershipState membershipState) {
 		this.membershipState = membershipState;
 	}
 
 	@PrePersist
 	public void onPrepesist() {
 		autosetTimestamps();
 		autoApprove();
 	}
 
 	private void autosetTimestamps() {
 		if (getCreated() == null) {
 			setCreated(TimeUtils.createCurrentTimestamp());
 		}
 		if (getChanged() == null) {
 			setChanged(getCreated());
 		}
 	}
 
 	private void autoApprove() {
 		Project project = getProject();
 		if (project != null && project.isAutoApprove()) {
 			setMembershipState(MembershipState.APPROVED);
 		}
 	}
 
 	@PreUpdate
 	public void autosetLastChangeTimestamp() {
 		setChanged(TimeUtils.createCurrentTimestamp());
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((getProject() == null) ? 0 : getProject().hashCode());
 		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (!(obj instanceof Membership)) {
 			return false;
 		}
 		Membership other = (Membership) obj;
 		if (getProject() == null) {
 			if (other.getProject() != null) {
 				return false;
 			}
 		} else if (!getProject().equals(other.getProject())) {
 			return false;
 		}
 		if (getUser() == null) {
 			if (other.getUser() != null) {
 				return false;
 			}
 		} else if (!getUser().equals(other.getUser())) {
 			return false;
 		}
 		return true;
 	}
 
 }
