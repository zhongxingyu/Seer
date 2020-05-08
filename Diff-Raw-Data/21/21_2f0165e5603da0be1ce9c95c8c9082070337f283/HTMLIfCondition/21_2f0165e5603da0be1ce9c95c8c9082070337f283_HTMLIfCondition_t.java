 package supersql.codegenerator.HTML;
 
 import supersql.codegenerator.Attribute;
 import supersql.codegenerator.Connector;
 import supersql.codegenerator.Function;
 import supersql.codegenerator.IfCondition;
 import supersql.codegenerator.Manager;
 import supersql.codegenerator.TFE;
 import supersql.extendclass.ExtList;
 
 public class HTMLIfCondition extends IfCondition {
 
 	protected Manager manager;
 	protected HTMLEnv html_env;
 	protected HTMLEnv html_env2;
 	
 	public HTMLIfCondition(Manager manager, HTMLEnv html_env, HTMLEnv html_env2, Attribute condition, TFE thenTfe, TFE elseTfe) {
 		super(condition, thenTfe, elseTfe);
 		this.manager = manager;
 		this.html_env = html_env;
 		this.html_env2 = html_env;
 	}
 	
	public void work(ExtList data_info) {
 
 		String conditionResult = ((ExtList)(data_info.get(0))).toString();
		if(conditionResult.equals("t") || conditionResult.equals("1")) {
 			if(thenTfe instanceof Connector || thenTfe instanceof Attribute
 				|| thenTfe instanceof Function || thenTfe instanceof IfCondition)
 				thenTfe.work((ExtList)data_info.ExtsubList(1, thenTfe.countconnectitem()+1));
		}
 		if((((data_info.get(0))).toString()).equals("t")){
 			if(thenTfe instanceof Connector || thenTfe instanceof Attribute
 				|| thenTfe instanceof Function || thenTfe instanceof IfCondition)
 				thenTfe.work(data_info.ExtsubList(1, thenTfe.countconnectitem()+1));
 			else
 				thenTfe.work((ExtList)data_info.get(1));
 
 		}
 		else if(elseTfe != null){
 			if(elseTfe instanceof Connector || elseTfe instanceof Attribute
 					|| elseTfe instanceof Function || elseTfe instanceof IfCondition)
 				elseTfe.work(data_info.ExtsubList(2, data_info.size()));
 			else
 				elseTfe.work((ExtList)data_info.get(2));
 			}		
		}
 }
