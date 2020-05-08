 /* RPI Planner - Customized plans of study for RPI students.
  *
  * Copyright (C) 2008 Eric Allen allene2@rpi.edu
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package rpiplanner;
 
 import java.io.BufferedReader;
import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.bsf.BSFException;
 import org.apache.bsf.BSFManager;
 import org.jruby.exceptions.RaiseException;
 
 import rpiplanner.model.Degree;
 import rpiplanner.model.PlanOfStudy;
 import rpiplanner.model.ValidationError;
 
 public class PlanValidator {
 	private static PlanValidator instance;
 	private BSFManager rubyEnvironment;
 	
 	private PlanValidator(){
 		BSFManager.registerScriptingEngine("ruby", 
                 "org.jruby.javasupport.bsf.JRubyEngine", 
                 new String[] { "rb" });
 
 		rubyEnvironment = new BSFManager();
 		try {
 			rubyEnvironment.eval("ruby", "(java)", 0, 0, readFileAsString("plan_validation.rb"));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BSFException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static PlanValidator getInstance(){
 		if(instance == null)
 			instance = new PlanValidator();
 		return instance;
 	}
 	
 	public boolean isValid(PlanOfStudy plan){
 		for(Degree degree : plan.getDegrees()){
 			if(!satisfiesDegree(plan, degree))
 				return false;
 		}
 		return true;
 	}
 	
 	public boolean satisfiesDegree(PlanOfStudy plan, Degree degree){
 		return validate(plan, degree).length == 0;
 	}
 
 	public ValidationError[] validate(PlanOfStudy plan, Degree degree){
 		try {
 			ArrayList<String> errors = new ArrayList<String>();
 			ArrayList<String> warnings = new ArrayList<String>();
 			rubyEnvironment.declareBean("plan", plan, PlanOfStudy.class);
 			rubyEnvironment.declareBean("errors", errors, ArrayList.class);
 			rubyEnvironment.declareBean("warnings", warnings, ArrayList.class);
 			
 			rubyEnvironment.eval("ruby", "(java)", 1, 1, degree.getValidationCode());
 
 			rubyEnvironment.undeclareBean("plan");
 			rubyEnvironment.undeclareBean("errors");
 			rubyEnvironment.undeclareBean("warnings");
 			
 			if(errors.isEmpty() && warnings.isEmpty())
 				return new ValidationError[0];
 			
 			ArrayList<ValidationError> validationErrors = new ArrayList<ValidationError>(errors.size() + warnings.size());
 			for(String err : errors)
 				validationErrors.add(new ValidationError(ValidationError.Type.ERROR, err));
 			for(String warn : warnings)
 				validationErrors.add(new ValidationError(ValidationError.Type.WARNING, warn));
 			
 			return validationErrors.toArray(new ValidationError[0]);
 
 		} catch (BSFException e) {
 			RaiseException err = (RaiseException) e.getTargetException();
 			System.err.println(err.getException().asJavaString());
 			return new ValidationError[] {new ValidationError(ValidationError.Type.WARNING, "Bad degree specification")};
 		}
 	}
 	
 	private static String readFileAsString(String filePath)
 	throws java.io.IOException{
 	    StringBuffer fileData = new StringBuffer(1000);
	    InputStream fileStream = PlanValidator.class.getResourceAsStream("/"+filePath);
	    if(fileStream == null){
	    	try{
	    		fileStream = new FileInputStream(filePath);
	    	} catch (FileNotFoundException e){
	    		System.err.println("Critical file not found: "+filePath);
	    		return "";
	    	}
	    }
 	    BufferedReader reader = new BufferedReader(
	            new InputStreamReader(fileStream));
 	    char[] buf = new char[1024];
 	    int numRead=0;
 	    while((numRead=reader.read(buf)) != -1){
 	        fileData.append(buf, 0, numRead);
 	    }
 	    reader.close();
 	    return fileData.toString();
 	}
 }
