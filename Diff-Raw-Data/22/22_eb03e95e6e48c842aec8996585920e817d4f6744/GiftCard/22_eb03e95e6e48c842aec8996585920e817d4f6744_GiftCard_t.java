 package is.idega.idegaweb.pheidippides.data;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = GiftCard.ENTITY_NAME)
 @NamedQueries({
 	@NamedQuery(name = "giftCard.findAll", query = "select g from GiftCard g where g.header = :header"),
	@NamedQuery(name = "giftCard.findByCode", query = "select g from GiftCard g where g.code = :code")
 })
 public class GiftCard implements Serializable {
 	private static final long serialVersionUID = 6216484960480690814L;
 
 	public static final String ENTITY_NAME = "ph_gift_card";
 
 	private static final String COLUMN_CODE = "code";
 	private static final String COLUMN_GIFT_CARD_HEADER = "gift_card_header";
 	private static final String COLUMN_AMOUNT = "amount";
 	private static final String COLUMN_GREETING_TEXT = "greeting_text";
 	
 	@Id
 	@Column(name = GiftCard.COLUMN_CODE, length = 20)
 	private String code;
 	
 	@ManyToOne
 	@JoinColumn(name = GiftCard.COLUMN_GIFT_CARD_HEADER)
 	private GiftCardHeader header;
 
 
 	@Column(name = GiftCard.COLUMN_AMOUNT)
 	private int amount;
 
 	@Column(name = GiftCard.COLUMN_GREETING_TEXT)
 	private String greeting;
 
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 	
 	public int getAmount() {
 		return amount;
 	}
 
 	public void setAmount(int amount) {
 		this.amount = amount;
 	}
 
 	public String getGreeting() {
 		return greeting;
 	}
 
 	public void setGreeting(String greeting) {
 		this.greeting = greeting;
 	}
 
 	public GiftCardHeader getHeader() {
 		return header;
 	}
 
 	public void setHeader(GiftCardHeader header) {
 		this.header = header;
 	}
 }
