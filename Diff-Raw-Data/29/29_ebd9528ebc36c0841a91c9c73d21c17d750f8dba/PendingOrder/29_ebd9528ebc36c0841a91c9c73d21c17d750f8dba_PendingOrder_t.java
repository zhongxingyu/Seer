 package com.github.hippoom.food2go.domain.model.order;
 
 import java.util.Date;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.ToString;
 
 @ToString(of = "trackingId")
 @EqualsAndHashCode(of = "trackingId")
 @NoArgsConstructor
 // for frameworks only
 @Entity
 @Table(name = "t_f2g_pending_order")
 /*
  * @SequenceGenerator(name = "seq_f2g_pending_order", sequenceName =
  * "seq_f2g_pending_order") this doesn't work, so I have to use /import.sql to
 * generate schema in persistence tests.
  */
 public class PendingOrder {
 	@EmbeddedId
 	@AttributeOverrides({ @AttributeOverride(name = "value", column = @Column(name = "TRACKING_ID")) })
 	@Getter
 	private TrackingId trackingId;
 	@Embedded
 	@AttributeOverrides({
 			@AttributeOverride(name = "street1", column = @Column(name = "DELIVERY_ADDRESS_STREET1")),
 			@AttributeOverride(name = "street2", column = @Column(name = "DELIVERY_ADDRESS_STREET2")) })
 	@Getter
 	private Address deliveryAddress;
 	@Column(name = "delivery_Time")
 	@Getter
 	private Date deliveryTime;
 
 	public PendingOrder(TrackingId trackingId, Address deliveryAddress,
 			Date deliveryTime) {
 		this.trackingId = trackingId;
 		this.deliveryAddress = deliveryAddress;
 		this.deliveryTime = deliveryTime;
 	}
 
 }
