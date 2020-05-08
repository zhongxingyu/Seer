 package xsched.analysis.db;
 
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.classLoader.ProgramCounter;
 import com.ibm.wala.types.TypeReference;
 
 /**
  * since SSAInstruction doesn't know about its method "similar" SSAInstructions in different methods would
  * be considered equal(). Our analysis, however, uses the bytecodes also as object creation sites and therefore we
  * must distinguish those. We do this by wrapping the {SSAInstruction, Method} pair in a ObjectCreationBytecode
  * 
  *  other bytecodes are allowed to be conflated (all those that don't create object instances)
  */
 public abstract class Obj {
 
 	public static class SpecialObject extends Obj {
 		private final Object value;
 		
 		public SpecialObject(Object value) {
 			//don't accept type references because they include the classloader which are hierarchial and we cannot handle that well in our flat domains
 			//use the TypeName instead
 			assert (! (value instanceof TypeReference));
 			this.value = value;
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if(o instanceof SpecialObject) {
 				SpecialObject other = (SpecialObject)o;
 				return other.value.equals(value);
 			} else {
 				return false;
 			}
 		}
 		
 		@Override
 		public int hashCode() {
 			return value.hashCode();
 		}
 		
 		@Override
 		public String toString() {
 			return value.toString();
 		}
 	}
 	
 	public static class NewObject extends Obj {
 		private final IMethod method;
 		private final ProgramCounter programCounter;
 		
 		public NewObject(IMethod method, ProgramCounter newSiteReference) {
 			this.method = method;
 			this.programCounter = newSiteReference;
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if(o instanceof NewObject) {
 				NewObject other = (NewObject)o;
 				return other.method.equals(method) && other.programCounter.equals(programCounter);
 			} else {
 				return false;
 			}
 		}
 		
 		@Override
 		public int hashCode() {
 			return method.hashCode() * 7529 + programCounter.hashCode();
 		}
 		
 		@Override
 		public String toString() {
 			return method.toString() + " -> " + programCounter.toString();
 		}
 	}
 	
 }
