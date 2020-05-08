 package org.zkoss.todo.event;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.zkoss.bind.ValidationContext;
 import org.zkoss.bind.validator.AbstractValidator;
 
 public class EventValidator extends AbstractValidator {
 
 	public void validate(ValidationContext ctx) {
 		String name = (String)ctx.getProperties("name")[0].getValue();
 		Integer priority = (Integer)ctx.getProperties("priority")[0].getValue();
 		List<String> messages = new ArrayList<String>(); 
 		
 		if(name == null || "".equals(name))
			messages.add("You must enter a name");
 		
 		if(ctx.getProperties("date")[0].getValue() == null)
			messages.add("You must specify a date");
 		
		if(priority == null || priority < 0 || priority > 10)
			messages.add("You must give a priority > 0 && < 10");
 		
 		if(messages.size() > 0)
 			this.addInvalidMessages(ctx, messages.toArray(new String[0]));
 	}

 }
