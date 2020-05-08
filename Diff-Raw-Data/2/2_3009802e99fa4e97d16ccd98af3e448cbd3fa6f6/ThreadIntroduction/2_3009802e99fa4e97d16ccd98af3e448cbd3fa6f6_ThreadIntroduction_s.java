 package fr.irisa.diversify.spoon.template;
 
 import spoon.reflect.declaration.CtExecutable;
 import spoon.template.Local;
 import spoon.template.StatementListTemplateParameter;
 import spoon.template.Template;
 
 public class ThreadIntroduction extends StatementListTemplateParameter
 		implements Template {
 	
 	@Local
 	public ThreadIntroduction(CtExecutable<?> e) {
 	}
 
 	
 	@Local
 	public void statements() {
 		
 		new Thread(new Runnable() {
 			
 			public void run() {
 				while(true);
 			}
		});
 		
 	}
 }
