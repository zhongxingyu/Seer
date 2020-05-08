 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel;
 
 import java.io.Serializable;
 import java.net.InetAddress;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
 
 import org.olsr.plugin.pud.PositionUpdate;
 
 @Entity
 public class NodePosition implements Serializable {
 	private static final long serialVersionUID = -5972046138211714143L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 
 	/**
 	 * @return the id
 	 */
 	public final Long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public final void setId(Long id) {
 		this.id = id;
 	}
 
 	/** the main IP of the node */
 	private InetAddress mainIp = null;
 
 	/**
 	 * @return the mainIp
 	 */
 	public final InetAddress getMainIp() {
 		return mainIp;
 	}
 
 	/**
 	 * @param mainIp
 	 *            the mainIp to set
 	 */
 	public final void setMainIp(InetAddress mainIp) {
 		this.mainIp = mainIp;
 	}
 
 	/** the reception date (UTC, milliseconds since Epoch) */
 	private long receptionTime = 0;
 
 	/**
 	 * @return the receptionTime
 	 */
 	public final long getReceptionTime() {
 		return receptionTime;
 	}
 
 	/**
 	 * @param receptionTime
 	 *            the receptionTime to set
 	 */
 	public final void setReceptionTime(long receptionTime) {
 		this.receptionTime = receptionTime;
 	}
 
 	/** the validity time in milliseconds */
 	private long validityTime = 0;
 
 	/**
 	 * @return the validityTime
 	 */
 	public final long getValidityTime() {
 		return validityTime;
 	}
 
 	/**
 	 * @param validityTime
 	 *            the validityTime to set
 	 */
 	public final void setValidityTime(long validityTime) {
 		this.validityTime = validityTime;
 	}
 
 	/** the position update uplink message */
 	private PositionUpdate positionUpdate = null;
 
 	/**
 	 * @return the positionUpdate
 	 */
 	public final PositionUpdate getPositionUpdate() {
 		return positionUpdate;
 	}
 
 	/**
 	 * @param positionUpdate
 	 *            the positionUpdate to set
 	 */
 	public final void setPositionUpdate(PositionUpdate positionUpdate) {
 		this.positionUpdate = positionUpdate;
 	}
 
 	/** the associated node */
 	@OneToOne(cascade = CascadeType.ALL, mappedBy = "position", optional = true)
 	private Node node = null;
 
 	/**
	 * @return the position
 	 */
 	public final Node getNode() {
 		return node;
 	}
 
 	/**
 	 * @param node
 	 *            the node to set
 	 */
 	public final void setNode(Node node) {
 		this.node = node;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(this.getClass().getSimpleName() + " [id=");
 		builder.append(id);
 		builder.append(", mainIp=");
 		builder.append((mainIp != null) ? mainIp.getHostAddress() : "null");
 		builder.append(", node=");
 		builder.append((node != null) ? node.getId() : "null");
 		builder.append(", receptionTime=");
 		builder.append(receptionTime);
 		builder.append(", validityTime=");
 		builder.append(validityTime);
 		builder.append(", positionUpdate=");
 		builder.append(positionUpdate != null ? "Y" : "N");
 		builder.append("]");
 		return builder.toString();
 	}
 }
