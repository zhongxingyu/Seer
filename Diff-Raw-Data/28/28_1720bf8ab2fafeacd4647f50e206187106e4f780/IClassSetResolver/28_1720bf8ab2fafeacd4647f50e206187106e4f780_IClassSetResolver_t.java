 package com.mutation;
 
 import java.util.Set;
 
 import com.mutation.events.IEventListener;
import com.mutation.util.ToStringUtils;
 
 public interface IClassSetResolver {
 
 	public class ClassDescription {
 		
 		String className;
 
 		String classFile;
 
 		public String getClassFile() {
 			return classFile;
 		}
 
 		public void setClassFile(String classFile) {
 			this.classFile = classFile;
 		}
 
 		public String getClassName() {
 			return className;
 		}
 
 		public void setClassName(String className) {
 			this.className = className;
 		}

		@Override
		public String toString() {
			return ToStringUtils.toString(this);
		}
 	}
 
 	Set<ClassDescription> determineClassNames(IEventListener eventListener);
 
 }
