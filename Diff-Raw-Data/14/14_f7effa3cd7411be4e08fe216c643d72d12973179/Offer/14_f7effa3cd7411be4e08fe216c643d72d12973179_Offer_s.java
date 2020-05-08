 package at.ac.tuwien.infosys.aicc11;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 
 public class Offer {
 	
 	private long offerId;
 	private String comments;
 	private InterestRate interestRate;
 	private CreditRequest request;
 	
 	public Offer()
 	{
		this(0, "", new InterestRate(), new CreditRequest());
 	}
 	
 	public Offer(long offerId, String comments, InterestRate interestRate, CreditRequest request) {
 		super();
 		this.offerId = offerId;
 		this.comments = comments;
 		this.interestRate = interestRate;
 		this.request = request;
 	}
 
 	@XmlAttribute(name="offer_id")
 	public long getOfferId() {
 		return offerId;
 	}
 
 
 	public void setOfferId(long offerId) {
 		this.offerId = offerId;
 	}
 
 
 	public String getComments() {
 		return comments;
 	}
 
 
 	public void setComments(String comments) {
 		this.comments = comments;
 	}
 
 
 	@XmlElement(name="interest_rate")
 	public InterestRate getInterestRate() {
 		return interestRate;
 	}
 
 
 	public void setInterestRate(InterestRate interestRate) {
 		this.interestRate = interestRate;
 	}
 	
	@XmlElement(name="credit_request")
 	public CreditRequest getRequest() {
 	    return request;
 	}
 
 	public void setRequest(CreditRequest request) {
 	    this.request = request;
 	}
 
 	@Override
 	public int hashCode() {
 	    final int prime = 31;
 	    int result = 1;
 	    result = prime * result + ((comments == null) ? 0 : comments.hashCode());
 	    result = prime * result + ((interestRate == null) ? 0 : interestRate.hashCode());
 	    result = prime * result + (int) (offerId ^ (offerId >>> 32));
 	    result = prime * result + ((request == null) ? 0 : request.hashCode());
 	    return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 	    if (this == obj)
 		return true;
 	    if (obj == null)
 		return false;
 	    if (getClass() != obj.getClass())
 		return false;
 	    Offer other = (Offer) obj;
 	    if (comments == null) {
 		if (other.comments != null)
 		    return false;
 	    } else if (!comments.equals(other.comments))
 		return false;
 	    if (interestRate == null) {
 		if (other.interestRate != null)
 		    return false;
 	    } else if (!interestRate.equals(other.interestRate))
 		return false;
 	    if (offerId != other.offerId)
 		return false;
 	    if (request == null) {
 		if (other.request != null)
 		    return false;
 	    } else if (!request.equals(other.request))
 		return false;
 	    return true;
 	}
 
 }
