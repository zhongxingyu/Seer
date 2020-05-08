 package com.lolay.citygrid;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @XmlRootElement(name="editorial")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class Editorial implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@XmlAttribute(name="attribution_text",required=true)
 	private String attributionText = null;
 	@XmlAttribute(name="attribution_logo",required=true)
 	private URI attributionLogo = null;
 	@XmlAttribute(name="attribution_source",required=true)
 	private Integer attributionSource = null;
 	@XmlElement(name="editorial_id",required=true)
 	private Integer editorialId = null;
 	@XmlElement(name="editorial_url",required=true)
 	private URI editorialUrl = null;
 	@XmlElement(name="editorial_title",required=true)
 	private String editorialTitle = null;
 	@XmlElement(name="editorial_author",required=true)
 	private String editorialAuthor = null;
	@XmlElement(name="editorial_review",required=true)
	private String editorialReview = null;
 	@XmlElement(name="pros")
 	private String pros = null;
 	@XmlElement(name="cons")
 	private String cons = null;
 	@XmlElement(name="editorial_date",required=true)
 	private Date editorialDate = null;
 	@XmlElement(name="review_rating",required=true)
 	@XmlJavaTypeAdapter(value=ReviewRatingAdapter.class)
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
 	public Integer getEditorialId() {
 		return editorialId;
 	}
 	public void setEditorialId(Integer editorialId) {
 		this.editorialId = editorialId;
 	}
 	public URI getEditorialUrl() {
 		return editorialUrl;
 	}
 	public void setEditorialUrl(URI editorialUrl) {
 		this.editorialUrl = editorialUrl;
 	}
 	public String getEditorialTitle() {
 		return editorialTitle;
 	}
 	public void setEditorialTitle(String editorialTitle) {
 		this.editorialTitle = editorialTitle;
 	}
 	public String getEditorialAuthor() {
 		return editorialAuthor;
 	}
 	public void setEditorialAuthor(String editorialAuthor) {
 		this.editorialAuthor = editorialAuthor;
 	}
	public String getEditorialReview() {
		return editorialReview;
	}
	public void setEditorialReview(String editorialReview) {
		this.editorialReview = editorialReview;
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
 	public Date getEditorialDate() {
 		return editorialDate;
 	}
 	public void setEditorialDate(Date editorialDate) {
 		this.editorialDate = editorialDate;
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
