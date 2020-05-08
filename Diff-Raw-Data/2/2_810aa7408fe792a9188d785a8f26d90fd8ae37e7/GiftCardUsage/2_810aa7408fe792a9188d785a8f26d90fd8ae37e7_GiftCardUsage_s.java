 package is.idega.idegaweb.pheidippides.data;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 @Entity
@Table(name = GiftCard.ENTITY_NAME)
 @NamedQueries({
 	@NamedQuery(name = "giftCardUsage.findAllByGiftCard", query = "select g from GiftCardUsage g where g.card = :card"),
 	@NamedQuery(name = "giftCardUsage.sumByGiftCard", query = "select sum(g.amount) from GiftCardUsage g where g.card = :card")
 })
 public class GiftCardUsage implements Serializable {
 	private static final long serialVersionUID = -5035894763289201483L;
 
 	public static final String ENTITY_NAME = "ph_gift_card_usage";
 	private static final String COLUMN_ENTRY_ID = "usage_id";
 	private static final String COLUMN_GIFT_CARD = "gift_card";
 	private static final String COLUMN_REGISTRATION_HEADER = "registration_header";
 	private static final String COLUMN_AMOUNT = "amount";
 	private static final String COLUMN_CREATED_DATE = "created";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = GiftCardUsage.COLUMN_ENTRY_ID)
 	private Long id;
 
 	@ManyToOne
 	@JoinColumn(name = GiftCardUsage.COLUMN_GIFT_CARD)
 	private GiftCard card;
 	
 	@ManyToOne
 	@JoinColumn(name = GiftCardUsage.COLUMN_REGISTRATION_HEADER)
 	private RegistrationHeader header;
 
 	@Column(name = GiftCardUsage.COLUMN_AMOUNT)
 	private int amount;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = GiftCardUsage.COLUMN_CREATED_DATE)
 	private Date createdDate;
 
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public GiftCard getCard() {
 		return card;
 	}
 
 	public void setCard(GiftCard card) {
 		this.card = card;
 	}
 
 	public RegistrationHeader getHeader() {
 		return header;
 	}
 
 	public void setHeader(RegistrationHeader header) {
 		this.header = header;
 	}
 
 	public int getAmount() {
 		return amount;
 	}
 
 	public void setAmount(int amount) {
 		this.amount = amount;
 	}
 
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 
 }
