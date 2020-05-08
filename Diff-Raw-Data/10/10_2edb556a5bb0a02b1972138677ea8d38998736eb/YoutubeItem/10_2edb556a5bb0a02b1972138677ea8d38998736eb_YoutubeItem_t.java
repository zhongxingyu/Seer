 package com.ippoippo.joplin.dto;
 
 import java.io.Serializable;
 
 import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;
 
 public class YoutubeItem implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2365995103355938584L;
 
 	private String id;
 	
 	@NotEmpty
 	private String articleId;
 
 	@NotEmpty
 	private String videoId;
 	
 	private double rate = 1600;
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getArticleId() {
 		return articleId;
 	}
 
 	public void setArticleId(String articleId) {
 		this.articleId = articleId;
 	}
 
 	public String getVideoId() {
 		return videoId;
 	}
 
 	public void setVideoId(String videoId) {
 		this.videoId = videoId;
 	}
 
 	public double getRate() {
 		return rate;
 	}
 
 	public void setRate(double rate) {
 		this.rate = rate;
 	}
 
 	private static final double WINNER_POINT = 1;
 	private static final double LOSER_POINT = 0;
 	private static final double TOURNAMENT_LEVEL = 32;
 
 	/**
 	 * 対決後のrate変動量
 	 */
	@Transient
 	private double rateVaried;
 
 	public void calcRateVaried(boolean win, double opponentRate) {
 		double point = (win) ? WINNER_POINT : LOSER_POINT;
 		double winProbability = 1 / (1 + Math.pow(10, (opponentRate - this.rate) / 400));
 		this.rateVaried = TOURNAMENT_LEVEL * (point - winProbability);
 	}
 
 	public double getRateVaried() {
 		return this.rateVaried;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
 		YoutubeItem other = (YoutubeItem) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "YoutubeItem [id=" + id
 				+ ", articleId=" + articleId + ", videoId=" + videoId
 				+ ", rate=" + rate + ", rateVaried=" + rateVaried + "]";
 	}
 
 	@Override
 	public YoutubeItem clone() {
 		YoutubeItem cloneItem = new YoutubeItem();
 		cloneItem.setId(this.id);
 		cloneItem.setArticleId(this.articleId);
 		cloneItem.setVideoId(this.videoId);
 		cloneItem.setRate(this.rate);
 		return cloneItem;
 	}
 }
