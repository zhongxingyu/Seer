 package org.atlasapi.media.entity;
 
 import com.google.common.base.Function;
 
 
 public class Series extends Container<Episode> {
 	
 	private Integer seriesNumber;
 	
 	public Series() {}
 	
 	public Series(String uri, String curie, Publisher publisher) {
 		 super(uri, curie, publisher);
 	}
 	
 	public Series toSummary() {
 	   Series summary = new Series(this.getCanonicalUri(), this.getCurie(), this.publisher);
        summary.setTitle(this.getTitle());
        summary.setDescription(this.getDescription());
        summary.withSeriesNumber(seriesNumber);
        summary.setLastUpdated(this.getLastUpdated());
        summary.setThumbnail(this.getThumbnail());
        summary.setImage(this.getImage());
        return summary;
 	}
 
 	public Series withSeriesNumber(Integer seriesNumber) {
 		this.seriesNumber = seriesNumber;
 		return this;
 	}
 	
 	@Override
 	protected void contentAdded(Episode content) {
 		if (content.getContainer() == null) {
 			super.contentAdded(content);
 		}
         content.setSeries(this);
 	}
 
 	public Integer getSeriesNumber() {
 		return seriesNumber;
 	}
 	
 	@Override
 	public Container<Episode> copy() {
 	    Series copy = new Series();
 	    Container.copyTo(this, copy);
 	    copy.seriesNumber = seriesNumber;
 	    return copy;
 	}
 	
 	public final static Function<Series, Series> COPY = new Function<Series, Series>() {
         @Override
         public Series apply(Series input) {
             return (Series) input.copy();
         }
     };
 }
