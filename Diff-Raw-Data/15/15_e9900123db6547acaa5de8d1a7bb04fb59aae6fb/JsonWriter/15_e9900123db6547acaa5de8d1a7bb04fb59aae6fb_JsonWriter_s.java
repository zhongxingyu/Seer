 package org.bioinfo.infrared.lib.io.output;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.bioinfo.infrared.core.biopax.v3.BioEntity;
 import org.bioinfo.infrared.core.biopax.v3.NameEntity;
 import org.bioinfo.infrared.core.biopax.v3.Pathway;
 import org.bioinfo.infrared.core.cellbase.ConsequenceType;
 import org.bioinfo.infrared.core.cellbase.Cytoband;
 import org.bioinfo.infrared.core.cellbase.Exon;
import org.bioinfo.infrared.core.cellbase.FeatureMap;
 import org.bioinfo.infrared.core.cellbase.Gene;
 import org.bioinfo.infrared.core.cellbase.GenomeSequence;
 import org.bioinfo.infrared.core.cellbase.MirnaDisease;
 import org.bioinfo.infrared.core.cellbase.MirnaGene;
 import org.bioinfo.infrared.core.cellbase.MirnaMature;
 import org.bioinfo.infrared.core.cellbase.MirnaTarget;
 import org.bioinfo.infrared.core.cellbase.Protein;
 import org.bioinfo.infrared.core.cellbase.ProteinFeature;
 import org.bioinfo.infrared.core.cellbase.ProteinXref;
 import org.bioinfo.infrared.core.cellbase.Pwm;
 import org.bioinfo.infrared.core.cellbase.RegulatoryRegion;
 import org.bioinfo.infrared.core.cellbase.Snp;
 import org.bioinfo.infrared.core.cellbase.Tfbs;
 import org.bioinfo.infrared.core.cellbase.Transcript;
 import org.bioinfo.infrared.core.cellbase.Xref;
 import org.bioinfo.infrared.lib.common.GenomicVariantConsequenceType;
 import org.bioinfo.infrared.lib.impl.hibernate.GenomicRegionFeatures;
 
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class JsonWriter {
 
 	private Gson gson;
 	private ExclusionStrategy[] exclusionEstrategies;
 	
 	public JsonWriter() {
 		init();
 	}
 	
 	public JsonWriter(ExclusionStrategy ... exclusionEstrategies) {
 		this.exclusionEstrategies = exclusionEstrategies;
 		init();
 	}
 
 	private void init() {
 		if(exclusionEstrategies != null) {
 			gson = new GsonBuilder().serializeNulls().setExclusionStrategies(exclusionEstrategies).create();			
 		}else {
 			gson = new GsonBuilder().serializeNulls().create();
 		}
 	}
 	
 	public String serialize(Gene obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Exon obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Cytoband obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Xref obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(GenomeSequence obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Snp obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(ConsequenceType obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Transcript obj){
 		return gson.toJson(obj); 
 	}
 	
 	
 	public String serialize(MirnaGene obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(MirnaMature obj) {
 		return gson.toJson(obj); 
 	}
 	
 	
 	public String serialize(MirnaTarget obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public Object serialize(MirnaDisease obj) {
 		return gson.toJson(obj); 
 	}
 
 	public String serialize(Tfbs obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Pwm obj){
 		return gson.toJson(obj); 
 	}
 	
 	public Object serialize(GenomicRegionFeatures obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public Object serialize(RegulatoryRegion obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(Protein obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(ProteinFeature obj) {
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(ProteinXref obj) {
 		return gson.toJson(obj); 
 	}
 	
 	
 	public String serialize(GenomicVariantConsequenceType obj){
 		return gson.toJson(obj); 
 	}
 	
 	public String serialize(String string){
 		return string;
 	}
 	
 	public Object serialize(Pathway obj) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("{\"id\": ").append(obj.getPkPathway()).append(", \"name\": \"").append(getFirstName(obj.getBioEntity())).append("\", \"description\": \"");
 		if (obj.getBioEntity().getComment()!=null) {
 			sb.append(obj.getBioEntity().getComment().replace("\"", "'").replace("\n", "").replace("\r", "").replace("\n", ""));
 		}
 		sb.append("\"}");
 		
 		
 //		if (pathways!=null) {
 //			if ("json".equalsIgnoreCase(outputFormat) || "jsonp".equalsIgnoreCase(outputFormat)) {
 //				if ("jsonp".equalsIgnoreCase(outputFormat)) {
 //					sb.append("var response = (");
 //				}
 //				if (onlyTopLevel) {
 //					sb.append("{\"pathways\": [");
 //					Pathway pw = null;
 //					for(int i=0; i<pathways.size() ; i++) {
 //						pw = pathways.get(i);
 //						if (i!=0) {
 //							sb.append(",");
 //						}
 //						sb.append(getJsonPathway(pw));
 //					}
 //					sb.append("]}");
 //				} else {
 //					sb.append("{\"pathways\": [");
 //					Pathway pw = null;
 //					for(int i=0; i<pathways.size() ; i++) {
 //						pw = pathways.get(i);
 //						if (i!=0) {
 //							sb.append(",");
 //						}
 //						sb.append("{\"id\": ").append(pw.getPkPathway()).append(", \"name\": \"").append(getFirstName(pw.getBioEntity())).append("\", \"description\": \"");
 //						if (pw.getBioEntity().getComment()!=null) {
 //							sb.append(pw.getBioEntity().getComment().replace("\"", "'").replace("\n", "").replace("\r", "").replace("\n", ""));
 //						}
 //						sb.append("\"}");
 //					}
 //					sb.append("]}");
 //				}
 //				if ("jsonp".equalsIgnoreCase(outputFormat)) {
 //					sb.append(");");
 //				}
 //			} else {
 //				sb.append("#id").append("\t").append("name").append("\t").append("description").append("\n");
 //
 //				for(Pathway pw: pathways) {
 //					sb.append(pw.getPkPathway()).append("\t").append(getFirstName(pw.getBioEntity())).append("\t").append(pw.getBioEntity().getComment()).append("\n");
 //				}
 //			}
 //		} else {
 //			sb.append("Could not find any pathway"); 			
 //		}
 		return sb.toString();
 	}
 	
 	
 	public String serialize(List<?> list){
 		StringBuilder sb = new StringBuilder();
 		sb.append("[");
 		for (Object object : list) {
 			if (object instanceof Gene){
 				sb.append(serialize((Gene) object)).append(",");
 				continue;
 			}
 			if (object instanceof Exon){
 				sb.append(serialize((Exon) object)).append(",");
 				continue;
 			}
 			if (object instanceof Cytoband){
 				sb.append(serialize((Cytoband) object)).append(",");
 				continue;
 			}
 			if (object instanceof GenomeSequence){
 				sb.append(serialize((GenomeSequence) object)).append(",");
 				continue;
 			}
 			if (object instanceof Transcript){
 				sb.append(serialize((Transcript) object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof Snp){
 				sb.append(serialize((Snp) object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof ConsequenceType){
 				sb.append(serialize((ConsequenceType) object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof String){
 				sb.append(serialize((String) object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof Xref){
 				sb.append(serialize((Xref)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof Tfbs){
 				sb.append(serialize((Tfbs)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof RegulatoryRegion){
 				sb.append(serialize((RegulatoryRegion)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof MirnaGene){
 				sb.append(serialize((MirnaGene)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof MirnaMature){
 				sb.append(serialize((MirnaMature)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof MirnaTarget){
 				sb.append(serialize((MirnaTarget)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof MirnaDisease){
 				sb.append(serialize((MirnaDisease)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof Pwm){
 				sb.append(serialize((Pwm)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof Protein){
 				sb.append(serialize((Protein)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof ProteinFeature){
 				sb.append(serialize((ProteinFeature)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof ProteinXref){
 				sb.append(serialize((ProteinXref)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof GenomicVariantConsequenceType){
 				sb.append(serialize((GenomicVariantConsequenceType)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof GenomicRegionFeatures){
 				sb.append(serialize((GenomicRegionFeatures)object)).append(",");
 				continue;
 			}
 			
 			if (object instanceof List){
 				sb.append(serialize((List)object));
 				continue;
 			}
 			
 			// BioPax objects
 			if (object instanceof Pathway){
 				sb.append(serialize((Pathway)object)).append(",");
 				continue;
 			}
 			
 		}
 		sb.append("]");
 		return sb.toString();
 	}
 
 	public String getFirstName(BioEntity entity) {
 		String name = "NO-NAME";
 		try {
 			String aux = "";
 			Iterator it = entity.getNameEntities().iterator();
 			NameEntity ne = null;
 			while (it.hasNext()) {
 				ne = (NameEntity) it.next();
 				if (name.equalsIgnoreCase("NO-NAME") || ne.getNameEntity().length()<name.length()) {
 					name = ne.getNameEntity();
 				}
 			}
 			name = name.replace("\"", "'");
 		} catch (Exception e) {
 			name = "NO-NAME";
 		}
 		return name;
 	}
 	
 	
 
 	
 
 	private static String join(String sep, String ... items) {
 		StringBuilder stringBuilder = new StringBuilder();
 		for(int i=0; i<items.length-1; i++) {
 			stringBuilder.append(items[i]).append(sep);
 		}
 		stringBuilder.append(items[items.length-1]);
 		return stringBuilder.toString();
 	}
 
 	
 	public void resetExclusionEstrategies() {
 		this.exclusionEstrategies = null;
 		gson = new GsonBuilder().serializeNulls().create();
 	}
 	
 	public ExclusionStrategy[] getExclusionEstrategies() {
 		return exclusionEstrategies;
 	}
 
 	public void setExclusionEstrategies(ExclusionStrategy[] exclusionEstrategies) {
 		this.exclusionEstrategies = exclusionEstrategies;
 		gson = new GsonBuilder().serializeNulls().setExclusionStrategies(exclusionEstrategies).create();
 	}
 }
