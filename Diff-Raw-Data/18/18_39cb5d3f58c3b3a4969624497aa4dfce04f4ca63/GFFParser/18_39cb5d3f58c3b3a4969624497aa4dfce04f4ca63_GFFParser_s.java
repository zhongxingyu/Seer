 package ch.epfl.bbcf.conversion.parser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.biojava.bio.BioException;
 import org.biojava.bio.program.gff.GFFDocumentHandler;
 import org.biojava.bio.program.gff.GFFErrorHandler;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.IgnoreRecordException;
 import org.biojava.bio.seq.StrandedFeature.Strand;
 import org.biojava.utils.ParserException;
 
 import ch.epfl.bbcf.conversion.convertor.Convertor;
 import ch.epfl.bbcf.conversion.convertor.JBrowseConvertor;
 import ch.epfl.bbcf.conversion.convertor.json.GFFCreator;
 import ch.epfl.bbcf.conversion.daemon.Launcher;
 import ch.epfl.bbcf.conversion.feature.GFFFeature;
 import ch.epfl.bbcf.exception.ParsingException;
 import ch.epfl.bbcf.parser.Handler;
 import ch.epfl.bbcf.parser.Parser;
 
 
 /**
  * this class parse GFF files - 
  * it use the GFFParser from biojava -
  * if you want more control over the file, you should use biojava parser instead
  * @author Yohan Jarosz
  *
  */
 public class GFFParser extends Parser implements GFFErrorHandler,GFFDocumentHandler{
 
 	private Handler handler;
 	private List<String> types;
 	private static final String subFeatureHeaders = "[\"start\",\"end\",\"strand\",\"type\"]";
 	public static final Logger logger = Launcher.initLogger(GFFParser.class.getName());
 
 	public GFFParser(Processing type) {
 		super(type);
 		types = new ArrayList<String>();
 	}
 
 	public void parse(File inputSamOrBamFile,Handler handler) throws IOException, ParsingException{
 		this.handler = handler;
 		BufferedReader br = null;
 		br = new BufferedReader(
 				new FileReader(inputSamOrBamFile));
 		org.biojava.bio.program.gff.GFFParser parser = new org.biojava.bio.program.gff.GFFParser();
 		try {
 			parser.parse(br, this);
 		} catch (BioException e) {
 			throw new ParsingException(e,"",0);
 		} catch (ParserException e) {
 			throw new ParsingException(e,"",0);
 		}
 	}
 	@Override
 	protected void processLine(String line, Handler handler)
 	throws ParsingException {
 		//not used
 	}
 
 	@Override
 	public void commentLine(String arg0) {
 	}
 
 	@Override
 	public void endDocument() {
 		if((handler instanceof Convertor)){
 			Convertor cv = (Convertor)handler;
 			if(cv.isDoJSON()){
 				JBrowseConvertor jc = cv.getJSONConvertor();
 				GFFCreator ext = jc.getExtCreator();
 				if(ext!=null){
 					ext.setSubFeatureHeaders(subFeatureHeaders);
 					ext.setTypes(types);
 				}
 			}
 		}
 		handler.end();
 	}
 
 	@Override
 	public void recordLine(GFFRecord record) {
 		String chr = record.getSeqName();
 		int start = record.getStart();
 		int end = record.getEnd();
 		String name = "";
 		String id = "";
 		Map<String,List<String>> attributes = record.getGroupAttributes();
 		if (!attributes.isEmpty()){
 			Iterator<String> it = attributes.keySet().iterator();
 			while(it.hasNext()){
 				String str = it.next();
 				if(str.equalsIgnoreCase("name") || str.equalsIgnoreCase("gene_name")){
 					name=attributes.get(str).get(0);
 					if((name==null || name.equalsIgnoreCase(""))
 							&& attributes.size()>=2){
 						name = attributes.get(str).get(1);
 						while(name.endsWith("\"")){//remove extra "
 							name = name.substring(0,name.length()-1);
 						}
 					}
 				} 
 				if(str.equalsIgnoreCase("id") || str.equalsIgnoreCase("gene_id")){
 					id=attributes.get(str).get(0);
 				} 
 			}
 		}
 		float score = new Float(record.getScore());
 		int strand = getStrand(record.getStrand());
 		String type = record.getFeature();
 		GFFFeature feature = new GFFFeature(chr,start,end,name,id,score,strand,type);
 		if(!types.contains(type)){
 			types.add(type);
 		}
 		newFeature(handler, null, feature);
 	}
 
 	private int getStrand(Strand strand) {
		logger.debug("Strand : "+strand);
		logger.debug("token : "+strand.getToken());
 		int s = 0;
		if(strand.getToken()==45){
 			s=-1;
		} else if(strand.getToken()==43){
 			s=1;
 		}
 		return s;
 	}
 
 	@Override
 	public void startDocument(String arg0) {
 		handler.start();
 	}
 
 	@Override
 	public int invalidEnd(String arg0) throws ParserException,
 	IgnoreRecordException {
 		return 0;
 	}
 
 	@Override
 	public int invalidFrame(String arg0) throws ParserException,
 	IgnoreRecordException {
 		return 0;
 	}
 
 	@Override
 	public double invalidScore(String arg0) throws ParserException,
 	IgnoreRecordException {
 		return 0;
 	}
 
 	@Override
 	public int invalidStart(String arg0) throws ParserException,
 	IgnoreRecordException {
 		return 0;
 	}
 
 	@Override
 	public Strand invalidStrand(String arg0) throws ParserException,
 	IgnoreRecordException {
 		return null;
 	}
 
 }
