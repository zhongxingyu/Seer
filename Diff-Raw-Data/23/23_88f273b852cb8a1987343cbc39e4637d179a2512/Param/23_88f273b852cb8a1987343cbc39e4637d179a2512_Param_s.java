 package org.imirsel.nema.model;
 
 import java.io.Serializable;
 
 /**
  * This class encapsulates the input output and the other parameters that are
  * sent to the executable program. These parameters are different from any flags
  * or runtime configuration options such min memory/max memory etc. sent to say
  * a JVM or -nosplash -nojvm sent to the MATLAB code. These parameters <b>do
  * not</b> include environment variables or system properties such as the ones
  * begining with -D for JVM.
  * 
  * <p>
  * An example of these variables are:<br/>
  * <b> --hopsize=0.01 $i1{org.imirsel.nema.analytics.util.io.RawAudioFile}
  * $o1{org.imirsel.nema.analytics.evaluation.melody.MelodyTextFile} </b> <br/>
  * are parameters to one of the melody extraction binaries. The first parameter
  * --hopsize=0.01 is of the type other. The second parameter which has a
  * data-type of org.imirsel.nema.analytics.util.io.RawAudioFile is of the type
  * input and the third parameter
  * org.imirsel.nema.analytics.evaluation.melody.MelodyTextFile is of the type
  * output.
  * 
  * The input and output parameters always have a data-type the data-type could
  * be an opaque file in the case the executable reads for example a binary file,
  * or it could be a serialized form of a core analytics data structure.
  * </p>
  * 
  * 
  * @author kumaramit01
  * @since 0.0.1
  */
 public class Param implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -303860357827666240L;
 
 	static public enum ParamType {
 		OTHER(-1), INPUT(0), OUTPUT(1);
 		private final int code;
 
 		private ParamType(int code) {
 			this.code = code;
 		}
 
 		public int getCode() {
 			return code;
 		}
 
 		public String toString() {
 			String name = null;
 			switch (code) {
 			case -1: {
 				name = "Other";
 				break;
 			}
 			case 0: {
 				name = "Input";
 				break;
 			}
 			case 2: {
 				name = "Output";
 				break;
 			}
 			}
 			return name;
 		}
 
 		static public ParamType toType(int code) {
 			ParamType type = null;
 			switch (code) {
 			case -1: {
 				type = ParamType.OTHER;
 				break;
 			}
 			case 0: {
 				type = ParamType.INPUT;
 				break;
 			}
 			case 1: {
 				type = ParamType.OUTPUT;
 				break;
 			}
 			}
 			return type;
 		}
 
 	}
 
 	// stores the parameter information -it can be the data-type
 	// or actual parameter -for example --hopsize=0.01
 	private String value;
 	private int typeCode;
 	// returns if this parameter is of datatype
 	private boolean dataType;
 	
 	// the sequence count
 	private int sequence;
 	
 	
 	public Param(String value, boolean dataType, int type, int sequence){
 	this.value = value;
 	this.typeCode = type;
 	this.dataType = dataType;
 	this.sequence = sequence;
 	}
 
 	public int getSequence(){
 		return this.sequence;
 	}
 	
 	public String getValue() {
 		return value;
 	}
 
 	public int getTypeCode() {
 		return typeCode;
 	}
 
 	public String getTypeName() {
 		return ParamType.toType(typeCode).name();
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public void setTypeCode(int typeCode) {
 		this.typeCode = typeCode;
 	}
 
 	public ParamType getParamType() {
 		return ParamType.toType(typeCode);
 	}
 
 	public boolean isDataType() {
 		return dataType;
 	}
 
 	public void setDataType(boolean dataType) {
 		this.dataType = dataType;
 	}
 	
 	/** Return the string representation of the parameter
 	 * 
 	 * @return the string value of the parameter
 	 */
 	public String getStringValue(){
 		StringBuilder sbuilder = new StringBuilder();
 		if(this.dataType==true){ // this is an input or output
 			if(this.getTypeCode() == ParamType.INPUT.getCode()){
 				sbuilder.append(" $i");
 				sbuilder.append(this.sequence);
 				sbuilder.append("{");
 				sbuilder.append(this.value+"} ");
 			}else if(this.getTypeCode() == ParamType.OUTPUT.getCode()){
 				sbuilder.append(" $o");
 				sbuilder.append(this.sequence);
 				sbuilder.append("{");
 				sbuilder.append(this.value+"} ");
 			}
 		}else{ // this is the other flag
 			sbuilder.append(" "+this.value+" ");
 		}
 		return sbuilder.toString();
 	}
 	
 	
 	public boolean equals(Object object){
 		if(!(object instanceof Param)){
 			return false;
 		}
 		Param p = (Param)object;
 		if(p==this){
 			return true;
 		}
 		if(this.value.equals(p.value) && this.sequence== p.sequence && 
 				this.isDataType()== p.dataType && this.typeCode == p.typeCode){
 			return true;
 		}
 		return false;
 	}
 	
 	public int hashCode(){
 		int hash = this.sequence;
 		hash = hash + this.value.hashCode();
 		hash= hash + this.typeCode;
 		if(this.dataType){
 			hash= hash + 1;
 		}
 		return hash;
 	}
 
 }
