 package com.github.hvasoares.pageobjects.mixin;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import com.github.hvasoares.pageobjects.PageObjectBuilder;
 
 import static com.google.common.base.Preconditions.*;
 public class Mixer implements MixerI {
 	private Map<String,MixinI> db;
 	public Mixer(){
 		db = new HashMap<String, MixinI>();
 	}
 
 	@Override
 	public PageObjectBuilder build(PageObjectBuilder builder,MixinI mixin){
 		Stack<String> stack = getStack(mixin,null);
 		
 		while(!stack.empty()){
 			getMixin(stack.pop()).build(builder);
 		}
 		mixin.build(builder);
 		return builder;
 	}
 	
 	private Stack<String> getStack(MixinI mixin,Stack<String> stack) {
 		stack = stack==null? new Stack<String>() : stack;
 		for(String dep : mixin.getDependencies()){
 			checkArgument(
 				db.containsKey(dep),
 				String.format(
 					"Huum, seems you didn't declare the depedency mixin '%s' for mixin '%s' ", 
 					dep,
 					mixin.getName()
 				)
 			);
 			if(stack.contains(dep)){
 				stack.remove(dep);
 			}
 			stack.push(dep);
 			getStack(getMixin(dep), stack);
 		}
 		return stack;
 	}
 
 	private MixinI getMixin(String depedency) {
 		return db.get(depedency);
 	}
 
 	@Override
 	public void add(MixinI mixin) {
 		this.db.put(mixin.getName(),mixin);
 	}
 
 }
