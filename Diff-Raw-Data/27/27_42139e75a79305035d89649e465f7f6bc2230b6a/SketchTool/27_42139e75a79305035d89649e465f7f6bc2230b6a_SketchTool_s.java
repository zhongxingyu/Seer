 /*
   Copyright (c) 2009-2012 Malte Mader <mader@zbh.uni-hamburg.de>
   Copyright (c) 2009-2012 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
 
 package de.unihamburg.zbh.fishoracle.server.data;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import de.unihamburg.zbh.fishoracle.client.data.GWTImageInfo;
 import de.unihamburg.zbh.fishoracle.client.data.QueryInfo;
 
 import annotationsketch.*;
 import core.*;
 import de.unihamburg.zbh.fishoracle.client.data.RecMapInfo;
 
 /**
  *  Draws images visualizing the genomic data especially chromosome,
  *   gene and segment data. The drawing is done by AnnotaionsSketch
  *   of the GenomeTools which is a bioinformatics library written in C.
  *   The connection to C library is established via the java bindings
  *   genometools-java that are based on JNA (Java Native Access).
  * 
  * */
 public class SketchTool {
 
 	public SketchTool() {
 		
 	}
 	
 	/**
 	 * Draws an image and creates an image info object that stores the URL
 	 * to the image and addional image information liek a ReqMap of all
 	 * features in the image.
 	 * 
 	 * @param amps Array of segments normally retrieved from the database.
 	 * @param genes Array of genes normally retrieved from the database.
 	 * @param kband Array of karyobands normally retrieved from the database.
 	 * @param loc ensembl API location object
 	 * @param winWidth The inner width of the center panel.
 	 * @param query The original search query. Needed to mark the searched feature.
 	 * @param serverPath the realPath of a servlet context retrieved from 
 	 *         <code>getServletContext().getRealPath("/");<code>.
 	 * 
 	 * @return imgInfo image info object that stores additional information of
 	 *          the generated image that need to be displayed or further processed
 	 *          at the client side.
 	 * @throws UnsupportedEncodingException 
 	 * @throws NoSuchAlgorithmException 
 	 *          @see GWTImageInfo
  	 * */
 	public GWTImageInfo generateImage(FeatureCollection features,
									de.unihamburg.zbh.fishoracle_db_api.data.Location loc,
 									final QueryInfo query,
 									String serverPath) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		
 		Range range;
 		Style style;
 		Diagram diagram;
 		Layout layout;
 		CanvasCairoBase canvas = null;
 		long height;
 		
 		String imgUrl = "";
 
 		GWTImageInfo imgInfo = null;
 		
 		try {
 		
 		style = new Style();
 		
 		style.load_file(serverPath + "config" + System.getProperty("file.separator") + "default.style");
 		
 		for(int l=0; l < query.getTracks().length; l++ ){
 		
 			if(query.isSorted()){
 			
 				style.set_color(query.getTracks()[l].getTrackName(), "stroke", new Color(0.0,0.0,0.0,0.0));
 				style.set_color(query.getTracks()[l].getTrackName(), "fill", new Color(0.0,0.0,0.0,0.0));
 			
 				style.set_bool(query.getTracks()[l].getTrackName() + "_segments", "collapse_to_parent", true);
 				style.set_color(query.getTracks()[l].getTrackName() + "_segments", "fill", new Color(0.0,0.0,1.0,0.7));
 				
 			} else {
 				
 				style.set_color(query.getTracks()[l].getTrackName(), "stroke", new Color(0.0,0.0,1.0,0.7));
 				style.set_color(query.getTracks()[l].getTrackName(), "fill", new Color(0.0,0.0,1.0,0.7));
 				
 			}
 			
 		}
 		
 		range = new Range(loc.getStart(), loc.getEnd());
 		
 		TrackSelector ts = new TrackSelector() {
 		      @Override
 		      public String getTrackId(Block b)
 		      {
 		    	  String typeNumber = "";
 
 		    	  if(b.get_type().equals("karyoband") ){
 		    		  typeNumber = "1:";
 		    	  }
 		    	  else if(b.get_type().equals("gene") ){
 		    		  typeNumber = "2:";
 		    	  }
 		    	  else {
 		    		  for(int i = 0; i < query.getTracks().length; i++){
 		    			  if(b.get_type().equals(query.getTracks()[i].getTrackName())){
 		    				  typeNumber = (query.getTracks()[i].getTrackNumber() + 2) + ":";
 		    			  }
 		    		  }
 		    	  }
 		    	  
 		        return typeNumber + b.get_type();
 		      }};
 		
 		diagram = new Diagram(features, range, style);		
 		diagram.set_track_selector_func(ts);
 		
 		layout = new Layout(diagram, query.getWinWidth(), style);
 		
 		height = layout.get_height();
 
 		ImageInfo info = new ImageInfo();
 		
 		if(query.getImageType().equals("png")){	
 			canvas = (CanvasCairoBase) new CanvasCairoFilePNG(style, query.getWinWidth(), (int) height, info);
 		}	
 		if(query.getImageType().equals("pdf")){	
 			canvas = new CanvasCairoFilePDF(style, query.getWinWidth(), (int) height, info);
 		}	
 		if(query.getImageType().equals("ps")){	
 			canvas = new CanvasCairoFilePS(style, query.getWinWidth(), (int) height, info);
 		}	
 		if(query.getImageType().equals("svg")){	
 			canvas = new CanvasCairoFileSVG(style, query.getWinWidth(), (int) height, info);
 		}
 		
 		layout.sketch(canvas);
 		
 		ArrayList<RecMapInfo> recmapinfoArr = getRecMapElements(info);
 		
 		String dateStr;
 		Calendar cal = Calendar.getInstance();
 	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    dateStr = sdf.format(cal.getTime());
 		
 	    String shaStr;
 	    
 	    shaStr = SimpleSHA.SHA1(dateStr);
 	    
 		@SuppressWarnings("unused")
 		File file;
 
 		String fileName = shaStr + "_" + loc.getChrosmome() + ":" + loc.getStart() + "-" + loc.getEnd() + "_" + query;
 		
 		imgUrl = "as_output" + System.getProperty("file.separator") + fileName + "." + query.getImageType();
 		
 		file = new File(serverPath + imgUrl);
 
 	    canvas.to_file(serverPath + imgUrl);
 	    
 	    imgInfo = new GWTImageInfo(imgUrl, info.get_height(), query.getWinWidth(), recmapinfoArr);
 		
 	    for(int i = 0; i < features.size(); i++){
 	    	features.get(i).dispose();
 	    }
 	    
 	    style.dispose();
 	    diagram.dispose();
 	    layout.dispose();
 	    canvas.dispose();
 	    info.dispose();
 	    
 		} catch (GTerrorJava e) {
 			e.printStackTrace();
 			System.out.println("Error: " + e.getMessage());
 			System.out.println(e.getCause());
 		}
 	    return imgInfo;
 	}
 	
 	/*
 	 * Copies the GTReqMap object into the custom ReqMapInfo object
 	 * and stores additional information in it.
 	 *  
 	 * */
 	private ArrayList<RecMapInfo> getRecMapElements(ImageInfo info){
 		
 		ArrayList<RecMapInfo> recmapinfoArray = new ArrayList<RecMapInfo>();
 		String identifier = null;
 		
 		for(int i=0; i < info.num_of_rec_maps(); i++){
 		
 			// for genes we need to set an unique identifier like the ensembl stable id
 			if(info.get_rec_map(i).get_genome_feature().get_type().equals("gene")){
 				
 				identifier = info.get_rec_map(i).get_genome_feature().get_attribute("NAME");
 				
 			// the same applies to the segments but here we use the segment id
 			} else if (!info.get_rec_map(i).get_genome_feature().get_type().equals("gene") &&
 					!info.get_rec_map(i).get_genome_feature().get_type().equals("karyoband") && 
 					!info.get_rec_map(i).get_genome_feature().get_attribute("ROOT").equals("YES")){
 				
 				identifier = info.get_rec_map(i).get_genome_feature().get_attribute("NAME");
 				
 			}
 			
 			// we don't need reqmap information for the karyoband
 			if(!info.get_rec_map(i).get_genome_feature().get_type().equals("karyoband") &&
 				info.get_rec_map(i).get_genome_feature().get_attribute("ROOT").equals("NO")){
 				
 				RecMapInfo recmapinfo = new RecMapInfo(info.get_rec_map(i).get_northwest_x(),
 														info.get_rec_map(i).get_northwest_y(),
 														info.get_rec_map(i).get_southeast_x(),
 														info.get_rec_map(i).get_southeast_y(),
 														info.get_rec_map(i).get_genome_feature().get_type(),
 														identifier);
 			
 				recmapinfoArray.add(recmapinfo);
 			}
 		}		
 		return recmapinfoArray;
 	}
 }
