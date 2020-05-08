 package swag49.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 @Entity
 public class Resource {
	public class Id implements Serializable {
 		private static final long serialVersionUID = 1L;
 		private Long playerId;
 		private ResourceType resourceType;
 
 		public Id() {
 		}
 
 		public Id(long playerId, ResourceType resourceType) {
 			this.playerId = playerId;
 			this.resourceType = resourceType;
 		}
 
 		@Override
 		public int hashCode() {
 			return playerId.hashCode() + resourceType.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj != null && obj instanceof Id) {
 				return this.playerId.equals(((Id) obj).playerId)
 						&& this.resourceType.equals(((Id) obj).playerId);
 			} else {
 				return false;
 			}
 		}
 	}
 
 	@EmbeddedId
 	private Id id = new Id();
 
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "playerId", insertable = false, updatable = false)
 	private Player player;
 
 	@Column(nullable = false)
 	private Integer amount;
 
 	public Resource() {
 	}
 
 	public Resource(Player player, ResourceType resourceType, int amount) {
 		this.player = player;
 		this.amount = amount;
 		id = new Id(player.getId(), resourceType);
 		
 		player.getResources().add(this);
 	}
 
 	public Id getId() {
 		return id;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public ResourceType getResourceType() {
 		return id.resourceType;
 	}
 
 	public Integer getAmount() {
 		return amount;
 	}
 
 	public void setAmount(Integer amount) {
 		this.amount = amount;
 	}
 }
