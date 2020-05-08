 package org.genedb.crawl.elasticsearch.index.gff;
 
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import  uk.ac.sanger.artemis.io.GFFStreamFeature;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 public class GFFFeature {
 	
 	private Logger logger = Logger.getLogger(GFFFeature.class);
 	
 	public String seqid;
 	public String source;
 	public String type;
 	public int start;
 	public int end;
 	public String score;
 	public Strand strand;
 	public Phase phase;
 	
 	public GFFAttributeMap attributes = new GFFAttributeMap(this);
 	
 	public String id;
 	
 	public enum Strand {
 		POSITIVE ("+"),
 		NEGATIVE ("-"),
 		NOT_STRANDED ("."),
 		UNKNOWN ("?");
 		
 		private String text;
 		
 		Strand(String text) {
 			this.text = text;
 		}
 		
 		public String getStrand() {
 			return text;
 		}
 		
 		public int getStrandInt() {
 			if (text.equals("+")) {
 				return 1;
 			} else if (text.equals("-")) {
 				return 2;
 			}
 			return 0;
 		}
 		
 		public static Strand fromText(String text) {
 			if (text != null) {
 				for (Strand b : Strand.values()) {
 					if (text.equalsIgnoreCase(b.text)) {
 						return b;
 					}
 				}
 			}
 			return null;
 		}
 		
 	};
 	
 	
 	public enum Phase {
 		ZERO ("0"),
 		ONE ("1"),
 		TWO ("2"),
 		NULL (".");
 		
 		private String text;
 		
 		Phase(String text) {
 			this.text = text;
 		}
 		
 		public String getPhase() {
 			return text;
 		}
 		
 		public Integer getPhaseInt() {
 			
 			if (text.equals(ZERO)) {
 				return 0;
 			}
 			if (text.equals(ONE)) {
 				return 1;
 			}
 			if (text.equals(TWO)) {
 				return 2;
 			}
 			
 			return null;
 			
 		}
 		
 		public static Phase fromText(String text) {
 			if (text != null) {
 				for (Phase b : Phase.values()) {
 					if (text.equalsIgnoreCase(b.text)) {
 						return b;
 					}
 				}
 			}
 			return null;
 		}
 	}
 	
 	public GFFFeature(String line) {
 		this(line, true);
 	}
 	
 	
 	public GFFFeature(String line, boolean parseAttributes) {
 		
 		//logger.debug(line);
 		
 		String[] columns = line.split("\t");
 		
 		seqid = columns[0];
 		source = columns[1];
 		type = columns[2];
 		start = new Integer (columns[3]) - 1;
 		end = new Integer (columns[4]);
 		score = columns[5];
 		
 		strand = Strand.fromText(columns[6]);
 		phase = Phase.fromText(columns[7]);
 		
 		//logger.info(columns[7]);
 		
 //		try {
 //			phase = Integer.parseInt( columns[7] );
 //		} catch (NumberFormatException nfe) {
 //			if (type.equals("exon") || type.equals("CDS")) {
 //				logger.warn(String.format("%s features should have phase : \n\t %s", type, line));
 //			}
 //		}
 		
 		//logger.info(seqid);
 		
 		String attrs = columns[8];
 		
 		if (! attrs.endsWith(";")) {
 			attrs += ";";
 		}
 		
 		if (parseAttributes) {
 			this.attributes.parseAttributes(attrs);
 		}
 		
 		
 		//parseAttributes(attrs);
 		
 	}
 	
 	
 	
 	public String toString() {
 		StringBuffer s = new StringBuffer();
 		
 		s.append(seqid + "\t");
 		s.append(source + "\t");
 		s.append(type + "\t");
 		s.append(start + "\t");
 		s.append(end + "\t");
 		s.append(score+ "\t");
 		s.append(strand.getStrand() + "\t");
 		s.append(phase + "\t");
 		
 		String[] attrs = new String[attributes.map.size()];
 		int i = 0;
 		for (Entry<String, Object> entry : attributes.map.entrySet() ) {
 			attrs[i] = entry.getKey() + "=" + entry.getValue().toString();
 			i++;
 		}
 		
 		s.append(StringUtils.join(attrs, ";"));
 		
 		return s.toString();
 	}
 	
 	
 	public class GFFAttributeMap {
 		public Map<String,Object> map = new LinkedHashMap<String, Object>();
 		public boolean decode = true;
 		
 		public GFFFeature feature;
 		
 		public GFFAttributeMap() {
 			// deliberately empty
 		}
 		
 		public GFFAttributeMap(GFFFeature feature) {
 			this.feature = feature;
 		}
 		
 		public void put (String key, String obj) {
 			
 			if (obj.contains(";") && obj.contains(",")) {
				//logger.info("[" + key + "]" + obj);
				// then it's a list of records
 				GFFAttributeMapList list = new GFFAttributeMapList();
 				list.parseAttributes(obj);
 				
 				map.put(key, list);
 				
 			} else {
 				
 				String quote = "\"";
 				String quote2 = "'";
 				
				if ( (obj.startsWith(quote) && obj.endsWith(quote)) || (obj.startsWith(quote2) && obj.endsWith(quote2)) ){
					String oldObj = obj;
					obj = obj.substring(1, obj.length() -2);
 				}
 				
 				
 				map.put(key, obj);
 				
 				// attempt to store an ID for this feature.
 				if (key.equals("ID")) {
 					if (this.feature != null) {
 						this.feature.id = obj;
 					}
 				}
 				
 			}
 		}
 		
 		public void parseAttributes(String attrs) {
 			//logger.info(attrs);
 			
 			int start = 0;
 			int end = attrs.indexOf(";");
 			
 			while (end > 0) {
 				
 				String attribute = attrs.substring(start, end).trim();
 				//logger.info(attribute);
 				if (decode) {
 					attribute = GFFStreamFeature.decode(attribute);
 				}
 				
 				//logger.info(attribute);
 				
 				int equals = attribute.indexOf("=");
 				
 				if (equals == -1) {
 					start = end + 1;
 					end = attrs.indexOf(";", start);
 					continue;
 				}
 				
 				String key = attribute.substring(0, equals);
 				String value = attribute.substring(equals+1);
 				
 				put(key, value);
 				
 				start = end + 1;
 				end = attrs.indexOf(";", start);
 				
 			}
 			
 		}
 		
 		public String toString() {
 			StringBuffer sb = new StringBuffer();
 			for (Entry<String,Object> entry : map.entrySet()) {
 				sb.append(entry.getKey());
 				sb.append("=");
 				sb.append(entry.getValue());
 			}
 			if (decode) {
 				return GFFStreamFeature.encode(sb.toString());
 			}
 			return sb.toString();
 		}
 		
 	}
 	
 	public class GFFAttributeMapList {
 		public List<GFFAttributeMap> list = new ArrayList<GFFAttributeMap>();
 		
 		public void parseAttributes(String attrs) {
 			//logger.info(attrs);
 			String[] split = attrs.split(",");
 			
 			for (String attr : split) {
 				GFFAttributeMap map = new GFFAttributeMap();
 				map.decode = false;
 				map.parseAttributes(attr);
 				list.add(map);
 			}
 		}
 		
 		public String toString() {
 			StringBuffer sb = new StringBuffer();
 			for (GFFAttributeMap map : list) {
 				sb.append(map.toString());
 			}
 			return sb.toString();
 		}
 	}
 	
 	
 }
