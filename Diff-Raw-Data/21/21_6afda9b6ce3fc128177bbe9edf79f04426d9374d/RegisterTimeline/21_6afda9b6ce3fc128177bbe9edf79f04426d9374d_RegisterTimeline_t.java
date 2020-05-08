 package com.litecoding.smali2java.renderer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.litecoding.smali2java.entity.smali.Register.RegisterInfo;
 import com.litecoding.smali2java.entity.smali.Param;
 import com.litecoding.smali2java.entity.smali.SmaliEntity;
 import com.litecoding.smali2java.entity.smali.SmaliMethod;
 
 /**
  * RegisterTimeline is a scheme of register usage
  * @author Dmitry Vorobiev
  *
  */
 public class RegisterTimeline {
 	private boolean isInitialized = false;
 	
 	private int sliceLength = 0;
 	private int linesCount = 0;
 	
 	private ArrayList<ArrayList<RegisterInfo>> timeline = null;
 	
 	public RegisterTimeline() {
 		this.isInitialized = false;
 	}
 	
 	public RegisterTimeline(SmaliMethod method, int linesCount, boolean doMapParams) {
 		this.isInitialized = true;
 		init(method, linesCount, doMapParams);
 	}
 	
 	public void init(SmaliMethod method, int linesCount, boolean doMapParams) {
 		if(isInitialized)
 			return;
 		
		isInitialized = true;
		
 		this.linesCount = linesCount;
 		
 		this.sliceLength = method.getRegisters();
 		
 		this.timeline = new ArrayList<ArrayList<RegisterInfo>>(linesCount);
 		for(int i = 0; i < linesCount; i++) {
 			ArrayList<RegisterInfo> slice = new ArrayList<RegisterInfo>(sliceLength);
 			this.timeline.add(slice);
 			for(int j = 0; j < sliceLength; j++)
 				slice.add(new RegisterInfo());
 		}
 		
 		if(doMapParams) {
 			List<Param> params = method.getParams();
 			boolean isMethodStatic = method.isFlagSet(SmaliEntity.STATIC);
 			int localsCount = method.getLocals();
 			ArrayList<RegisterInfo> slice = this.timeline.get(0);
 			
 			int delta = 0;
 			if(!isMethodStatic) {
 				slice.get(localsCount).isThis = true;
 				slice.get(localsCount).type = method.getSmaliClass().getClassName();
 				delta = 1;
 			}
 			
 			for(int i = 0; i < params.size() ; i++) {
 				Param param = params.get(i);
 				if(param.info.is64bit) {
 					RegisterInfo info = slice.get(localsCount + delta + i); 
 					info.is64bit = true;
 					info.is64bitMaster = true;
 					info.type = param.info.type;
 					
 					delta++;
 					info = slice.get(localsCount + delta + i); 
 					info.is64bit = true;
 					info.is64bitMaster = false;
 					info.type = param.info.type;
 				} else {
 					RegisterInfo info = slice.get(localsCount + delta + i);
 					info.type = param.info.type;
 				}
 			}
 		} //end if(doMapParams)
 	}
 	
 	public boolean isInitialized() {
 		return isInitialized;
 	}
 	
 	public int getSliceLength() {
 		return sliceLength;
 	}
 	
 	public int getLinesCount() {
 		return linesCount;
 	}
 	
 	public List<? extends List<RegisterInfo>> getTimeline() {
 		return timeline;
 	}
 	
 	public List<RegisterInfo> getSlice(int line) {
 		return timeline.get(line);
 	}
 	
 	public RegisterInfo getRegister(int line, int register) {
 		return timeline.get(line).get(register);
 	}
 	
 	public static void copySliceTypeData(List<RegisterInfo> from, List<RegisterInfo> to) {
 		if(from.size() != to.size())
 			return;
 		
 		int size = from.size();
 		
 		for(int i = 0; i < size; i++) {
 			RegisterInfo fromInfo = from.get(i);
 			RegisterInfo toInfo = to.get(i);
 			toInfo.copyTypeDataFrom(fromInfo);
 		}
 	}
 }
