 package com.videoSearch.media;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.beldyk.util.FileNameParser;
 
 import com.moviejukebox.thetvdb.model.Series;
 
 public class VideoItem extends AbstractMediaItem {
 
 	protected String seriesName;
 	protected String title;
 	protected Integer seasonNum;
 	protected Integer episodeNum;
 	protected Series tvdbSeries;
 
 	public Series getTvdbSeries() {
 		return tvdbSeries;
 	}
 
 	public void setTvdbSeries(Series tvdbSeries) {
 		this.tvdbSeries = tvdbSeries;
 	}
 
 	public VideoItem(String fileUrl, FileNameParser nameParser) {
 		super(fileUrl, nameParser);
 
 	}
 
 	@Override
 	public void init(){
 		super.init();
 		if(this.tokenizedFname != null){
 			if(this.tokenizedFname.containsKey("series")){
 				this.seriesName = this.tokenizedFname.
 				get("series").
 				replaceAll("[\\s\\.\\_\\-]+", " ").toLowerCase();
 			}
 			this.title = this.tokenizedFname.get("title");
 
 			if(this.tokenizedFname.get("ep") != null){
 				this.episodeNum = new Integer(this.tokenizedFname.get("ep"));
 			}
 
 			if(this.tokenizedFname.get("season") != null){
 				this.seasonNum = new Integer(this.tokenizedFname.get("season"));
 			}
 		}
 	}
 	public String getSeriesName() {
 		return seriesName;
 	}
 
 	public void setSeriesName(String seriesName) {
 		this.seriesName = seriesName;
 	}
 
 	public Integer getSeasonNum() {
 		return seasonNum;
 	}
 
 	public void setSeasonNum(Integer seasonNum) {
 		this.seasonNum = seasonNum;
 	}
 
 	public Integer getEpisodeNum() {
 		return episodeNum;
 	}
 
 	public void setEpisodeNum(Integer episodeNum) {
 		this.episodeNum = episodeNum;
 	}
 
 	@Override
 	public Document toDocument(){
 		Document doc = super.toDocument();
 
 		if(this.seriesName != null){
			System.err.printf("adding name '%s' for '%s'", seriesName, fileUrl);
 			doc.add(new Field( "seriesName", this.seriesName, 
 					Field.Store.YES, Field.Index.ANALYZED));
 		}
 
 		if(this.tvdbSeries != null && this.tvdbSeries.getOverview() != null){
			System.err.println("ading a desc for " +this.fileUrl);
 			doc.add(new Field( "seriesDesc", this.tvdbSeries.getOverview(), 
 					Field.Store.YES, Field.Index.ANALYZED));
 		}
 
 		//TODO add more fields from tvdb foo
 
 		return doc;
 	}
 
 
 
 
 }
