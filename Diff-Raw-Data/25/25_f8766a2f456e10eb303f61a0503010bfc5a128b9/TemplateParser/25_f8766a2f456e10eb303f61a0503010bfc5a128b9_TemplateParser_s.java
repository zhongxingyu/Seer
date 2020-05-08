 package granola.template.parser;
 
 import granola.template.context.GranolaContext;
 import granola.template.internal.model.commands.InlineCommandRegistry;
 import granola.template.internal.model.template.BlockCommand;
 import granola.template.internal.model.template.Echo;
 import granola.template.internal.model.template.Expression;
 import granola.template.internal.model.template.InlineCommand;
 import granola.template.internal.model.template.TemplateImpl;
 import granola.template.internal.model.template.Token;
 import granola.template.model.Template;
 
 import java.util.Collections;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TemplateParser {
 
 	
 	public Template fromString(GranolaContext ctx,String str)
 	{
 		TemplateImpl template = new TemplateImpl();
 		InlineCommandRegistry inlineRegistry = ctx.getInlines();
 		Pattern p;
 		Matcher m;
 		boolean r;	
 		Token t = template;
 		do {
 			p = Pattern.compile(".*\\{(\\{|%)(.+)(\\}|%)\\}.*",Pattern.DOTALL);
 			m = p.matcher(str);
 			if(r = m.matches()) 
 			{
 				if(m.group(1).equalsIgnoreCase("{") && m.group(3).equalsIgnoreCase("}"))
 				{					
 					t.addToSlot("main",new Echo(str.substring(m.end(2)+2,str.length())));
 					t.addToSlot("main",new Expression(str.substring(m.start(2),m.end(2))));					
 				}else if(m.group(1).equalsIgnoreCase("%") && m.group(3).equalsIgnoreCase("%"))
 				{		
 					t.addToSlot("main",new Echo(str.substring(m.end(2)+2,str.length())));
 					
 					String commandName = str.substring(m.start(2),m.end(2)).trim();
 					if(inlineRegistry.isInlineCommand(commandName.split(" ")[0]))
 					{
 						t.addToSlot("main",new InlineCommand(str.substring(m.start(2),m.end(2))));
 					}else{
 						t.addToSlot("main",new BlockCommand(str.substring(m.start(2),m.end(2))));
 					}
 				}
 				str = str.substring(0,m.start(2)-2);
 			}else{
 				t.addToSlot("main",new Echo(str));
 			}
 		}while(r);
		assert(t == this);		
 		Collections.reverse(t.getSlot("main").getChildren());
 		return new Template(template.normalize(ctx));
 	}
 	
 	
 }
