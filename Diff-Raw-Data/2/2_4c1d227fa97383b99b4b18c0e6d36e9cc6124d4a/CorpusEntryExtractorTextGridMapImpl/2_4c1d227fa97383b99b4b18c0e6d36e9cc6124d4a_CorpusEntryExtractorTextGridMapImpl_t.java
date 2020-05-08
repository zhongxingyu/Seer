 package org.spantus.externals.recognition.services.impl;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.spantus.core.marker.Marker;
 import org.spantus.core.marker.MarkerSet;
 import org.spantus.core.marker.MarkerSetHolder;
 import org.spantus.logger.Logger;
 import org.spantus.utils.Assert;
 import org.spantus.utils.FileUtils;
 import org.spantus.utils.StringUtils;
 import org.spantus.work.services.MarkerDao;
 import org.spantus.work.services.WorkServiceFactory;
 
 public class CorpusEntryExtractorTextGridMapImpl extends
 		CorpusEntryExtractorFileImpl {
 	
 	private File markerDir;
 	private MarkerDao markerDao;
 	private Logger log = Logger.getLogger(CorpusEntryExtractorTextGridMapImpl.class);
 	
 	
 
 	@Override
 	public String createLabel(File filePath, Marker marker, int result) {
 		String markersPath = FileUtils.replaceExtention(filePath,".TextGrid");
 		String text = createLabelFromTextGrid(new File(markerDir, markersPath), marker);
 		if(!StringUtils.hasText(text)){
 			return super.createLabel(filePath, marker, result);
 		}
 		return text;
 	}
 	
 	@Override
 	public String createLabelByMarkers(File filePath, Marker marker) {
 		String text = createLabelFromTextGrid(filePath, marker);
 		if(!StringUtils.hasText(text)){
 			return marker.getLabel();//super.createLabel(filePath, marker);
 		}
 		return text;
 	}
 	
 	/**
 	 * 
 	 * @param markerPath
 	 * @param marker
 	 * @return
 	 */
 	public String createLabelFromTextGrid(File markerPath, Marker marker) {
 		MarkerSetHolder markerSetHolder = getMarkerDao().read(markerPath);
 		Assert.isTrue(markerSetHolder != null, "Not initialized");
 		Assert.isTrue(markerSetHolder.getMarkerSets() != null, "Not initialized");
 		MarkerSet markerSet = findSegementedLowestMarkers(markerSetHolder);
 		Collection<Marker>  markers = findMappedMarkers(markerSet, marker);
 		StringBuilder buf = new StringBuilder();
 		for (Marker iMarker : markers) {
 			String lbl =cleanupLabel(iMarker.getLabel());
 			buf.append(lbl);
 		}
 		String bufStr = buf.toString();
 		log.debug("[createLabel]{0}: {1}", bufStr, marker);
 		return bufStr.toString();
 	}
 	
 	
 	public static String cleanupLabel(String label) {
 		String lbl = label.trim();
 		lbl = lbl.replace("...", "");
 		lbl = lbl.replace(":", "");
 		lbl = lbl.replace("'", "");
 		lbl = lbl.replace("^", "");
 		return lbl;
 	}
 	/**
 	 * 
 	 * @param markerSet
 	 * @param matchMarker
 	 * @return
 	 */
 	public Collection<Marker> findMappedMarkers(MarkerSet markerSet, Marker matchMarker){
 		List<Marker> rtnMarkers = new ArrayList<Marker>();
 		for (Marker iMarker : markerSet.getMarkers()) {
 			long deltaStartStart = iMarker.getStart()- matchMarker.getStart();
 			long deltaEndEnd = iMarker.getEnd()- matchMarker.getEnd();
 			long deltaStartEnd = iMarker.getStart()- matchMarker.getEnd();
 			long deltaEndStart = iMarker.getEnd()- matchMarker.getStart();
 			long matchLength =  matchMarker.getLength();
 			long iLength =  iMarker.getLength();
 			long deltaLendth = Math.abs(matchLength-iLength);
 			
 //			log.debug("[findMappedMarkers]??\n" +
 //					" {0}:{1}; \n " +
 //					"[deltaLendth:{6}];\n" +
 //					"[ss:{2};ee:{3};se:{4};es:{5}]",
 //					matchMarker, iMarker, deltaStartStart, deltaEndEnd , deltaStartEnd, deltaEndStart,
 //					deltaLendth);
 
 //			if(false){
 //			}else 
 			if((deltaStartStart>-deltaLendth && deltaStartStart<0) && (deltaEndEnd>=0 && deltaEndEnd<deltaLendth)){
 					//i ...xxxx...
 					//t ....xx....
 //					log.debug("[findMappedMarkers]ss>ee> {0}:{1}. add", matchMarker, iMarker );
 					rtnMarkers.add(iMarker);
			}else if(Math.abs(deltaStartStart)<iLength/2  && Math.abs(deltaEndEnd)<iLength/2 && iLength>Math.abs(deltaStartStart)+Math.abs(deltaEndEnd)){
 				//i ...xxxx...
 				//t ...xxx....
 				rtnMarkers.add(iMarker);
 			}else if(deltaStartStart > -50 && deltaStartStart<0 && deltaEndStart>10 && deltaEndEnd<0){
 				//i ...xxx...
 				//t ..xxx....
 //				log.debug("[findMappedMarkers]<>< {0}:{1}", matchMarker, iMarker );
 				rtnMarkers.add(iMarker);
 			}else if(deltaStartStart>0 && deltaEndEnd<0){
 				//i ...xxx...
 				//t ..xxx....
 //				log.debug("[findMappedMarkers]>< {0}:{1}", matchMarker, iMarker );
 				rtnMarkers.add(iMarker);
 			}else if(deltaStartStart>10 && (deltaStartEnd<-deltaLendth) && deltaEndEnd>0){
 //					log.debug("[findMappedMarkers]<<> {0}:{1}", matchMarker, iMarker );
 					rtnMarkers.add(iMarker);
 			}else if(deltaStartStart>500 ){
 //				log.debug("[findMappedMarkers]--- {0}:{1}", matchMarker, iMarker );
 				break;
 			}else if(deltaStartEnd>-41 && deltaStartEnd <0){
 //					log.debug("[findMappedMarkers]^^^ {0}:{1}. too much overlap. skip", matchMarker, iMarker );
 					break;
 			}else{
 //				log.debug("[findMappedMarkers]!! {0}:{1}", matchMarker, iMarker );
 			}
 
 		}
 		return rtnMarkers;
 	}
 	
 	public static String markerToString(Marker m){
 		return MessageFormat.format("{0}[{1}:{2}]", m.getLabel(),m.getStart(), m.getEnd());
 	}
 	
 	
 	public File getMarkerDir() {
 		return markerDir;
 	}
 
 	public void setMarkerDir(File file) {
 		this.markerDir = file;
 	}
 
 	public MarkerDao getMarkerDao() {
 		if(markerDao == null){
 			markerDao = WorkServiceFactory.createMarkerDao();
 		}
 		return markerDao;
 	}
 
 	public void setMarkerDao(MarkerDao markerDao) {
 		this.markerDao = markerDao;
 	}
 }
