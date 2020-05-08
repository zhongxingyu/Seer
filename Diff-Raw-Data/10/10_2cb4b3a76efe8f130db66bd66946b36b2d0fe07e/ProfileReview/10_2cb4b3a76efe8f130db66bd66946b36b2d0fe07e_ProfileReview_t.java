 package com.lolay.citygrid;
 
 import java.io.Serializable;
 import java.net.URI;
import java.util.Date;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @XmlRootElement(name="review")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class ProfileReview implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@XmlAttribute(name="attribution_text",required=true)
 	private String attributionText = null;
 	@XmlAttribute(name="attribution_logo",required=true)
 	private URI attributionLogo = null;
 	@XmlAttribute(name="attribution_source",required=true)
 	private Integer attributionSource = null;
 	@XmlElement(name="review_id",required=true)
 	private Integer reviewId = null;
 	@XmlElement(name="review_url",required=true)
 	private URI reviewUrl = null;
 	@XmlElement(name="review_title",required=true)
 	private String reviewTitle = null;
 	@XmlElement(name="review_author",required=true)
 	private String reviewAuthor = null;
 	@XmlElement(name="review_text",required=true)
 	private String reviewText = null;
 	@XmlElement(name="pros")
 	private String pros = null;
 	@XmlElement(name="cons")
 	private String cons = null;
 	@XmlElement(name="review_date",required=true)
	private Date reviewDate = null;
 	@XmlElement(name="review_rating",required=true)
 	private Integer reviewRating = null;
 	@XmlElement(name="helpfulness_total_count",required=true)
 	private Integer helpfulnessTotalCount = null;
 	@XmlElement(name="helpful_count",required=true)
 	private Integer helpfulCount = null;
 	@XmlElement(name="unhelpful_count",required=true)
 	private Integer unhelpfulCount = null;
 	
 	public String getAttributionText() {
 		return attributionText;
 	}
 	public void setAttributionText(String attributionText) {
 		this.attributionText = attributionText;
 	}
 	public URI getAttributionLogo() {
 		return attributionLogo;
 	}
 	public void setAttributionLogo(URI attributionLogo) {
 		this.attributionLogo = attributionLogo;
 	}
 	public Integer getAttributionSource() {
 		return attributionSource;
 	}
 	public void setAttributionSource(Integer attributionSource) {
 		this.attributionSource = attributionSource;
 	}
 	public Integer getReviewId() {
 		return reviewId;
 	}
 	public void setReviewId(Integer reviewId) {
 		this.reviewId = reviewId;
 	}
 	public URI getReviewUrl() {
 		return reviewUrl;
 	}
 	public void setReviewUrl(URI reviewUrl) {
 		this.reviewUrl = reviewUrl;
 	}
 	public String getReviewTitle() {
 		return reviewTitle;
 	}
 	public void setReviewTitle(String reviewTitle) {
 		this.reviewTitle = reviewTitle;
 	}
 	public String getReviewAuthor() {
 		return reviewAuthor;
 	}
 	public void setReviewAuthor(String reviewAuthor) {
 		this.reviewAuthor = reviewAuthor;
 	}
 	public String getReviewText() {
 		return reviewText;
 	}
 	public void setReviewText(String reviewText) {
 		this.reviewText = reviewText;
 	}
 	public String getPros() {
 		return pros;
 	}
 	public void setPros(String pros) {
 		this.pros = pros;
 	}
 	public String getCons() {
 		return cons;
 	}
 	public void setCons(String cons) {
 		this.cons = cons;
 	}
	public Date getReviewDate() {
 		return reviewDate;
 	}
	public void setReviewDate(Date reviewDate) {
 		this.reviewDate = reviewDate;
 	}
 	public Integer getReviewRating() {
 		return reviewRating;
 	}
 	public void setReviewRating(Integer reviewRating) {
 		this.reviewRating = reviewRating;
 	}
 	public Integer getHelpfulnessTotalCount() {
 		return helpfulnessTotalCount;
 	}
 	public void setHelpfulnessTotalCount(Integer helpfulnessTotalCount) {
 		this.helpfulnessTotalCount = helpfulnessTotalCount;
 	}
 	public Integer getHelpfulCount() {
 		return helpfulCount;
 	}
 	public void setHelpfulCount(Integer helpfulCount) {
 		this.helpfulCount = helpfulCount;
 	}
 	public Integer getUnhelpfulCount() {
 		return unhelpfulCount;
 	}
 	public void setUnhelpfulCount(Integer unhelpfulCount) {
 		this.unhelpfulCount = unhelpfulCount;
 	}
 	
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 	@Override
 	public boolean equals(Object obj) {
 	   return EqualsBuilder.reflectionEquals(this, obj);
 	}
 	@Override
 	public String toString() {
 	   return ToStringBuilder.reflectionToString(this);
 	}
 }
