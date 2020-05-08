 package com.operativus.senacrs.audit.output;
 
 import com.operativus.senacrs.audit.model.Form;
 
 
 public class TextOuptut
 		implements FormOutput {
 
 	@Override
 	public void print(Form input) {
 		
 		StringBuilder builder = null;
 		String output = null;
 
 		builder = this.buildOutput(input);
 		output = builder.toString();
 		System.out.println(output);
 	}
 	
 	protected StringBuilder buildOutput(Form input) {
 		
 		//TODO
		return null;
 	}
 
 }
